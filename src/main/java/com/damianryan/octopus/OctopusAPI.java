package com.damianryan.octopus;

import com.damianryan.octopus.model.Account;
import com.damianryan.octopus.model.Agreement;
import com.damianryan.octopus.model.Consumption;
import com.damianryan.octopus.model.Page;
import com.damianryan.octopus.model.Price;
import com.damianryan.octopus.model.Product;
import com.damianryan.octopus.model.Products;
import com.damianryan.octopus.model.Reading;
import com.damianryan.octopus.model.StandardUnitRate;
import com.damianryan.octopus.model.StandingCharge;
import com.damianryan.octopus.model.Tariff;
import com.damianryan.octopus.util.Pair;
import com.damianryan.octopus.util.Prices;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Component
@EnableConfigurationProperties(OctopusProperties.class)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OctopusAPI {

    private static final DateTimeFormatter ISO_LOCAL_DATE_TIME_HH_MM = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral('T')
            .append(DateTimeFormatter.ofPattern("HH:mm"))
            .toFormatter();

    WebClient client;

    OctopusProperties config;

    Account getAccount() {
        return get(config.getAccountUrl(), Account.class);
    }

    List<Reading> getGasReadings() {
        return getReadings(config.getGasConsumptionUrl());
    }

    List<Reading> getElectricityReadings() {
        return getReadings(config.getElectricityConsumptionUrl());
    }

    List<Product> getAllProducts() {
        log.info("fetching products...");
        StopWatch timer = new StopWatch("products");
        timer.start();
        List<Product> products = getMany(config.getProductsUrl(), Products.class).stream()
                                                                                 .map(Products::getContent)
                                                                                 .flatMap(List::parallelStream)
                                                                                 .map(this::populateTariffsFor)
                                                                                 .collect(Collectors.toList());
        timer.stop();
        log.info("{}", timer);
        return products;
    }

    List<Prices> getElectricityPrices(List<Agreement> electricityAgreements) {
        return electricityAgreements.stream()
                                    .map(agreement -> Pair.of(agreement.getTariffCode(),
                                                              Pair.of(getElectricityTariffUrlBase(agreement) +
                                                                      config.getStandingChargesPath() +
                                                                      getAgreementPeriodParameters(agreement),
                                                                      getElectricityTariffUrlBase(agreement) +
                                                                      config.getStandardUnitRatesPath() +
                                                                      getAgreementPeriodParameters(agreement))))
                                    .map(urls -> Pair.of(urls.getLeft(),
                                                         Pair.of(get(urls.getRight().getLeft(), StandingCharge.class),
                                                                 getMany(urls.getRight().getRight(), StandardUnitRate.class))))
                                    .map(this::extractStandingChargeAndJustLowAndHighStandardUnitRates)
                                    .collect(Collectors.toList());
    }

    private Product populateTariffsFor(Product product) {
        return get(product.getLinks().iterator().next().getHref(), Product.class);
    }

    private List<Reading> getReadings(String url) {
        return getMany(url, Consumption.class).stream()
                                              .map(Consumption::getContent)
                                              .flatMap(List::stream)
                                              .sorted()
                                              .collect(Collectors.toList());
    }

    private Pair<String, Pair<StandingCharge, List<StandardUnitRate>>> extractStandingChargeAndAllStandardUnitRates(Pair<String, Tariff> pair) {
        return Pair.of(pair.getLeft(),
                       Pair.of(get(pair.getRight().getLinks().get(0).getHref(), StandingCharge.class),
                               getMany(pair.getRight().getLinks().get(1).getHref(), StandardUnitRate.class)));
    }

    private Prices extractStandingChargeAndJustLowAndHighStandardUnitRates(Pair<String, Pair<StandingCharge, List<StandardUnitRate>>> pair) {
        return Prices.of(pair.getLeft(),
                         pair.getRight().getLeft().getContent().iterator().next(),
                         pair.getRight().getRight().stream().map(Page::getContent).flatMap(List::stream).sorted(Comparator.comparing(Price::getValidFrom)).collect(lastN(2)));
    }

    private static <T> Collector<T, ?, List<T>> lastN(int n) {
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

    private <T> T get(String url, Class<T> type) {
        return client.get()
                     .uri(url)
                     .accept(MediaType.APPLICATION_JSON)
                     .headers(header -> header.setBasicAuth(config.getApiKey(), ""))
                     .retrieve()
                     .bodyToMono(type)
                     .block();
    }

    private <T extends Page<?>> List<T> getMany(String initialUrl, Class<T> type) {
        List<T> responses = new ArrayList<>();
        String url = initialUrl;
        int page = 1;
        long runningCount = 0;
        while (null != url) {
            log.debug("getting page {} of {} from {}...", page++, type.getSimpleName(), url);
            T response = get(url, type);
            responses.add(response);
            long thisCount = response.getContent().size();
            runningCount += thisCount;
            log.debug("got {}/{} {} results, running total {}",
                      thisCount,
                      response.getCount(),
                      type.getSimpleName(),
                      runningCount);
            url = (null != response.getNext()) ? URLDecoder.decode(response.getNext(), StandardCharsets.UTF_8) : null;
        }
        return responses;
    }

    private static String dateTime(Instant instant) {
        return ISO_LOCAL_DATE_TIME_HH_MM.format(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
    }

    private String getProductCode(String tariffCode) {
        if (tariffCode.contains("GO")) {
            return config.getGoProductCode();
        } else if (tariffCode.contains("FIX")) {
            return config.getFixedRateProductCode();
        }
        throw new IllegalArgumentException("Unhandled tariff code: " + tariffCode);
    }

    private String getElectricityTariffUrlBase(Agreement agreement) {
        return config.getElectricityTariffsUrl()
                     .replaceAll("@product-code", getProductCode(agreement.getTariffCode()))
                     .replaceAll("@tariff-code", agreement.getTariffCode()) + "/";
    }

    private static String getAgreementPeriodParameters(Agreement agreement) {
        return "/?period_from=" + dateTime(agreement.getValidFrom()) +
               "&period_to=" + dateTime(agreement.getValidTo());
    }
}