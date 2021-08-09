package com.damianryan.octopus;

import com.damianryan.octopus.model.Consumption;
import com.damianryan.octopus.model.Price;
import com.damianryan.octopus.model.Product;
import com.damianryan.octopus.model.ProductStub;
import com.damianryan.octopus.model.ProductStubWrapper;
import com.damianryan.octopus.model.Reading;
import com.damianryan.octopus.model.Response;
import com.damianryan.octopus.model.StandardUnitRate;
import com.damianryan.octopus.model.StandingCharge;
import com.damianryan.octopus.model.Tariff;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
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
        logConsumption();
    }

    private void logGoTariffs() {
        log.info("""
                                 
                         getting Octopus product stub wrappers then
                         filtering out products that aren't 'Octopus Go' then
                         getting tariffs for 'Octopus Go' products then
                         extracting monthly direct debit tariffs for Eastern region from 'Octopus Go' products then
                         extracting standing charge and all standard unit rates from tariffs then
                         extracting low and high standard unit rates then
                         sorting products from lowest to highest price for 1kW per hour over 24 hours
                         """);
        getProductStubWrappers().stream()                                                           // Stream<ProductStubWrappers>
                                .map(ProductStubWrapper::getResults)                                // Stream<List<ProductStub>>
                                .flatMap(List::stream)                                              // Stream<ProductStub>
                                .filter(octopusGoProductStubs())                                    // Stream<ProductStub>
                                .map(this::getProductsWithTariffs)                                  // Stream<Product>
                                .map(this::extractEasternRegionMonthlyDirectDebitTariffs)           // Stream<Pair<String, Tariff>>
                                .map(this::extractStandingChargeAndAllStandardUnitRates)            // Stream<Pair<String, Pair<StandingCharge, List<StandardUnitRate>>>>
                                .map(this::extractStandingChargeAndJustLowAndHighStandardUnitRates) // Stream<Prices>
                                .sorted(Comparator.comparing(Prices::amountValue))                  // Stream<Prices> (sorted)
                                .forEach(prices -> log.info("{}", prices));
    }

    private List<ProductStubWrapper> getProductStubWrappers() {
        return getMany(client, properties.getProductsUrl(), ProductStubWrapper.class);
    }

    private Predicate<ProductStub> octopusGoProductStubs() {
        return productStub -> productStub.getFullName().contains(GO);
    }

    private Product getProductsWithTariffs(ProductStub product) {
        return get(client, product.getLinks().iterator().next().getHref(), Product.class);
    }

    private Pair<String, Tariff> extractEasternRegionMonthlyDirectDebitTariffs(Product product) {
        return Pair.of(product.getFullName(), product.getSingleRegisterElectricityTariffs()
                                                     .get(properties.getRegion())
                                                     .get(properties.getTariffType()));
    }

    private Pair<String, Pair<StandingCharge, List<StandardUnitRate>>> extractStandingChargeAndAllStandardUnitRates(Pair<String, Tariff> pair) {
        return Pair.of(pair.getLeft(),
                       Pair.of(get(client, pair.getRight().getLinks().get(0).getHref(), StandingCharge.class),
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

    private void logConsumption() {
        logElectricityConsumption();
        logGasConsumption();
    }

    private void logElectricityConsumption() {
        log.info("getting electricity consumption...");
        logReadings(getReadings(properties.getElectricityUrl()));
    }

    private void logReadings(List<Reading> readings) {
        if (!readings.isEmpty()) {
            Instant earliest = readings.get(0).getFrom();
            Instant latest = readings.get(readings.size() - 1).getTo();
            log.info("used {}kWh between {} and {} ", readings.stream().mapToDouble(Reading::getConsumption).sum(), earliest, latest);
            log.info("calculating daily usage...");
            MultiValueMap<LocalDate, Reading> dailyResults = new LinkedMultiValueMap<>();
            readings.forEach(result -> dailyResults.add(LocalDate.ofInstant(result.getFrom(), ZoneOffset.UTC), result));
            for (Map.Entry<LocalDate, List<Reading>> entry : dailyResults.entrySet()) {
                log.info("{}: {}kWh", entry.getKey(), entry.getValue().stream().mapToDouble(Reading::getConsumption).sum());
            }
        } else {
            log.info("no readings available");
        }
    }

    private List<Reading> getReadings(String url) {
        return getMany(client, url, Consumption.class).stream().map(Consumption::getResults).flatMap(List::stream).sorted().collect(Collectors.toList());
    }

    private void logGasConsumption() {
        log.info("getting gas consumption...");
        logReadings(getReadings(properties.getGasUrl()));
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
                      type.getSimpleName(),
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