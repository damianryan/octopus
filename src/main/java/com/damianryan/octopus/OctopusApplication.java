package com.damianryan.octopus;

import com.damianryan.octopus.model.*;
import com.damianryan.octopus.util.Pair;
import com.damianryan.octopus.util.Prices;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@SpringBootApplication
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class OctopusApplication implements CommandLineRunner {

    @Value("${octopus.base-url}")
    String baseUrl;

    @Value("${octopus.elec-url}")
    String electricityUrl;

    @Value("${octopus.account-url}")
    String accountUrl;

    @Value("${octopus.products-url}")
    String productsUrl;

    @Value("${octopus.api-key}")
    String apiKey;

    @Value("${octopus.region}")
    String region;

    @Value("${octopus.tariff-type}")
    String tariffType;

    public static void main(String[] args) {
        SpringApplication.run(OctopusApplication.class, args);
    }

    @Override
    public void run(String... args) {
        WebClient client = WebClient.create();
        List<Products> productsList = getMany(client, baseUrl + productsUrl, Products.class);
        List<Product> productStubs = new ArrayList<>();
        productsList.stream().map(Products::getResults).forEach(productStubs::addAll);
        log.info("filtering Go products and requesting fuller information...");
        List<Product> goProducts = productStubs.stream()
                                             .filter(product -> product.getFullName().contains("Go"))
                                             .map(product -> get(client, product.getLinks().iterator().next().getHref(), Product.class))
                                             .collect(Collectors.toList());
        Map<String, Tariff> goTariffs = new HashMap<>();
        log.info("getting monthly direct debit tariffs for eastern region for Go products...");
        goProducts.forEach(product -> goTariffs.put(product.getFullName(), product.getSingleRegisterElectricityTariffs().get(region).get(tariffType)));
        Map<String, Pair<StandingCharge, List<StandardUnitRate>>> goRates = new HashMap<>();
        log.info("getting standing charges and standard unit rates for Go tariffs");
        goTariffs.forEach((name, tariff) -> goRates.put(name, Pair.of(get(client, tariff.getLinks().get(0).getHref(), StandingCharge.class),
                                                                       getMany(client, tariff.getLinks().get(1).getHref(), StandardUnitRate.class))));
        List<Prices> goPrices = new ArrayList<>();
        log.info("extracting prices from tariffs...");
        goRates.forEach((name, pair) -> goPrices.add(Prices.of(name,
                                                               pair.getLeft()
                                                                   .getResults()
                                                                   .iterator()
                                                                   .next(),
                                                               pair.getRight()
                                                                   .stream()
                                                                   .map(Response::getResults)
                                                                   .flatMap(List::stream)
                                                                   .sorted(Comparator.comparing(Price::getValidFrom))
                                                                   .collect(lastN(2)))));
        goPrices.sort(Comparator.comparing(Prices::getValue));
        goPrices.forEach(prices -> log.info("{}", prices));
        logConsumption(client);
    }

    public static <T> Collector<T, ?, List<T>> lastN(int n) {
        return Collector.<T, Deque<T>, List<T>>of(ArrayDeque::new, (acc, t) -> {
            if (acc.size() == n) {
                acc.pollFirst();
            }
            acc.add(t);
        }, (acc1, acc2) -> {
            while (acc2.size() < n && !acc1.isEmpty()) {
                acc2.addFirst(acc1.pollLast());
            }
            return acc2;
        }, ArrayList::new);
    }

    private void logConsumption(WebClient client) {
        List<Consumption> consumptions = getMany(client, baseUrl + electricityUrl, Consumption.class);
        List<Reading> readings = new ArrayList<>();
        consumptions.stream().map(Consumption::getResults).forEach(readings::addAll);
        Collections.sort(readings);
        Instant earliest = readings.get(0).getFrom();
        Instant latest = readings.get(readings.size() - 1).getTo();
        log.info("between {} and {} used {}kWh", earliest, latest, readings.stream().mapToDouble(Reading::getConsumption).sum());
        MultiValueMap<LocalDate, Reading> dailyResults = new LinkedMultiValueMap<>();
        readings.forEach(result -> dailyResults.add(LocalDate.ofInstant(result.getFrom(), ZoneOffset.UTC), result));
        for (Map.Entry<LocalDate, List<Reading>> entry : dailyResults.entrySet()) {
            log.info("{}: {}kWh", entry.getKey(), entry.getValue().stream().mapToDouble(Reading::getConsumption).sum());
        }
    }

    private <T extends Response<?>> List<T> getMany(WebClient client, String initialUrl, Class<T> type) {
        List<T> responses = new ArrayList<>();
        String url = initialUrl;
        int page = 1;
        long runningCount = 0;
        while (null != url) {
            log.debug("getting page {} of {}...", page++, type.getSimpleName());
            T response = get(client, url, type);
            responses.add(response);
            long thisCount = response.getResults().size();
            runningCount += thisCount;
            log.debug("got {}/{} {} results, running total {}",
                     thisCount,
                     response.getCount(),
                     response.getResults().get(0).getClass().getSimpleName(),
                     runningCount);
            url = (null != response.getNext()) ? response.getNext() : null;
        }
        return responses;
    }

    private <T> T get(WebClient client, String url, Class<T> type) {
        return client.get()
                     .uri(url)
                     .accept(MediaType.APPLICATION_JSON)
                     .headers(header -> header.setBasicAuth(apiKey, ""))
                     .retrieve()
                     .bodyToMono(type)
                     .block();
    }
}