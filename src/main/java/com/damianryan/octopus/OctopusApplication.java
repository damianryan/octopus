package com.damianryan.octopus;

import com.damianryan.octopus.model.*;
import com.damianryan.octopus.util.Pair;
import com.damianryan.octopus.util.Prices;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableConfigurationProperties(OctopusProperties.class)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OctopusApplication implements CommandLineRunner {

    private static final String GO = "Go";

    OctopusProperties properties;

    WebClient client;

    public static void main(String... args) {
        SpringApplication.run(OctopusApplication.class, args);
    }

    @Override
    public void run(String... args) {
        logGoTariffs();
        logConsumption(client);
    }

    private void logGoTariffs() {
        log.info("fetching products, filtering just 'Octopus Go' products, requesting tariffs, extracting Eastern region monthly direct debit tariff and sorting prices from lowest to highest");
        // stage 1 - retrieve ALL product stub wrappers
        List<Prices> goPrices = getProductStubWrappers().stream()                                                           // Stream<Products>
                                                        .map(ProductStubWrapper::getResults)                                // Stream<List<Product>> (stub products)
                                                        .flatMap(List::stream)                                              // Stream<Product> (stub products)
                                                        .filter(octopusGoProductStubs())                                        // Stream<Product> (stub products)
                                                        .map(this::getProductsWithTariffs)                                  // Stream<Product> (products with tariffs)
                                                        .map(this::extractEasternRegionMonthlyDirectDebitTariffs)           // Stream<Pair<String, Tariff>>
                                                        .map(this::extractStandingChargeAndAllStandardUnitRates)            // Stream<Pair<String, Pair<StandingCharge, List<StandardUnitRate>>>>
                                                        .map(this::extractStandingChargeAndJustLowAndHighStandardUnitRates) // Stream<Prices>
                                                        .sorted(Comparator.comparing(Prices::amount))                     // Stream<Prices> (sorted)
                                                        .collect(Collectors.toList());                                      // List<Prices>
        goPrices.forEach(prices -> log.info("{}", prices));
    }

    private List<ProductStubWrapper> getProductStubWrappers() {
        return getMany(client, properties.getProductsUrl(), ProductStubWrapper.class);
    }

    private Predicate<ProductStub> octopusGoProductStubs() {
        return productStub -> productStub.getFullName().contains(GO);
    }

    private Product getProductsWithTariffs(ProductStub product) {
        return get(client, product.getLinks().iterator() .next().getHref(), Product.class);
    }

    private Pair<String, Tariff> extractEasternRegionMonthlyDirectDebitTariffs(Product product) {
        return Pair.of(product.getFullName(), product.getSingleRegisterElectricityTariffs()
                                                     .get(properties.getRegion())
                                                     .get(properties.getTariffType()));
    }

    private Pair<String, Pair<StandingCharge, List<StandardUnitRate>>> extractStandingChargeAndAllStandardUnitRates(Pair<String, Tariff> pair) {
        return Pair.of(pair.getLeft(),
                       Pair.of(get(client, pair.getRight() .getLinks().get(0).getHref(), StandingCharge.class),
                               getMany(client, pair.getRight().getLinks().get(1).getHref(), StandardUnitRate.class)));
    }

    private Prices extractStandingChargeAndJustLowAndHighStandardUnitRates(Pair<String, Pair<StandingCharge, List<StandardUnitRate>>> pair) {
        return Prices.of(pair.getLeft(),
                         pair.getRight().getLeft().getResults().iterator().next(),
                         pair.getRight().getRight().stream().map(Response::getResults).flatMap(List::stream).sorted(Comparator.comparing(Price::getValidFrom)).collect(lastN(2)));
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
        List<Reading> readings = getMany(client, properties.getElectricityUrl(), Consumption.class).stream().map(Consumption::getResults).flatMap(List::stream).sorted().collect(Collectors.toList());
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
                     .headers(header -> header.setBasicAuth(properties.getApiKey(), ""))
                     .retrieve()
                     .bodyToMono(type)
                     .block();
    }
}