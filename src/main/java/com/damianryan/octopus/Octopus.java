package com.damianryan.octopus;

import com.damianryan.octopus.model.ElectricityMeterPoint;
import com.damianryan.octopus.model.GasMeterPoint;
import com.damianryan.octopus.model.Property;
import com.damianryan.octopus.model.Reading;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class Octopus implements CommandLineRunner {

    OctopusAPI api;

    public static void main(String... args) {
        SpringApplication.run(Octopus.class, args);
    }

    @Override
    public void run(String... args) {
        logConsumption();
//        logAccount();
    }

    private void logConsumption() {
        logElectricityConsumption();
        logGasConsumption();
    }

    private void logElectricityConsumption() {
        log.info("getting electricity consumption...");
        logReadings(api.getElectricityReadings(), "electricity");
    }

    private void logGasConsumption() {
        log.info("getting gas consumption...");
        logReadings(api.getGasReadings(), "gas");
    }

    private void logReadings(List<Reading> readings, String type) {
        if (!readings.isEmpty()) {
            Instant earliest = readings.get(0).getFrom();
            Reading lastReading = readings.get(readings.size() - 1);
            Instant latest = lastReading.getTo();

            double totalUsage = readings.stream().mapToDouble(Reading::getConsumption).sum();
            MultiValueMap<LocalDate, Reading> readingsByDate = new LinkedMultiValueMap<>();
            readings.forEach(result -> readingsByDate.add(toLocalDate(result.getFrom()), result));

            Map<LocalDate, Integer> numberOfReadingsByDate = readingsByDate.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size()));
            int numberOfReadingsOnLastDay = numberOfReadingsByDate.get(toLocalDate(latest));
            log.info("number of readings on last day: {}", numberOfReadingsOnLastDay);

            Reading penultimateReading = readings.get(readings.size() - 1 - numberOfReadingsOnLastDay);
            int totalDays = readingsByDate.size();
            log.info("total {} usage {}kWh between {} and {} ({} days)", type, totalUsage, formatDateTime(earliest), formatDateTime(latest), totalDays);

            if (numberOfReadingsOnLastDay < 48) {
                totalDays -= 1;
                List<Reading> lastDaysReadings = readingsByDate.get(toLocalDate(latest));
                if (!CollectionUtils.isEmpty(lastDaysReadings)) {
                    double lastDaysConsumption = lastDaysReadings.stream().mapToDouble(Reading::getConsumption).sum();
                    totalUsage -= lastDaysConsumption;
                    log.info("ignoring last day's {} reading{}, total {} usage {} between {} and {} ({} days)",
                             numberOfReadingsOnLastDay,
                             s(numberOfReadingsOnLastDay),
                             type,
                             totalUsage,
                             formatDateTime(earliest),
                             formatDateTime(penultimateReading.getTo()),
                             totalDays);
                }
            }

            double lowest = Double.MAX_VALUE;
            double highest = Double.MIN_VALUE;
            int count = 0;
            Map<LocalDate, Double> usageByDate = new LinkedHashMap<>();
            for (Map.Entry<LocalDate, List<Reading>> entry : readingsByDate.entrySet()) {
                double usage = entry.getValue().stream().mapToDouble(Reading::getConsumption).sum();
                ++count;
                if (usage > highest) {
                    highest = usage;
                }
                if (usage < lowest && count < totalDays) {
                    lowest = usage;
                }
                usageByDate.put(entry.getKey(), usage);
            }

            for (Map.Entry<LocalDate, Double> entry : usageByDate.entrySet()) {
                LocalDate date = entry.getKey();
                double usage = entry.getValue();
                log.info("{}: {} ({} readings){}{}",
                         date,
                         kWh(usage),
                         numberOfReadingsByDate.get(date),
                         lowest == usage ? " (lowest)" : "",
                         highest == usage ? " (highest)" : "");
            }

            log.info("mean usage per day over {} days was: {}", totalDays, kWh(totalUsage / totalDays));
            log.info("median usage per day over {} days was: {}",
                     totalDays,
                     kWh(median(usageByDate.values().stream().sorted().collect(Collectors.toList()))));
        } else {
            log.info("no readings available");
        }
    }

    private static double median(List<Double> numbers) {
        List<Double> scratch = new ArrayList<>(numbers);
        scratch.sort(Comparator.naturalOrder());
        scratch.remove(0); // remove the small reading for the final day that only includes one 30-minute period
        int count = scratch.size();
        boolean isOdd = 0 != (count % 2);
        int midPoint = count / 2;
        if (isOdd) {
            return scratch.get(midPoint);
        } else {
            return (scratch.get(midPoint - 1) + scratch.get(midPoint)) / 2;
        }
    }

    private static String s(int count) {
        return count == 1 ? "" : "s";
    }

    private static LocalDate toLocalDate(Instant time) {
        return LocalDate.ofInstant(time, ZoneId.systemDefault());
    }

    private static LocalDateTime toLocalDateTime(Instant time) {
        return LocalDateTime.ofInstant(time, ZoneId.systemDefault());
    }

    private static String formatDateTime(Instant time) {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(toLocalDateTime(time));
    }

    private static String kWh(double kWh) {
        return String.format("%.2fkWh", kWh);
    }

    private void logAccount() {
        var allProducts = api.getAllProducts();
        var properties = api.getAccount().getProperties();
        var electricityAgreements = properties.stream()
                                              .map(Property::getElectricityMeterPoints)
                                              .flatMap(List::stream)
                                              .map(ElectricityMeterPoint::getAgreements)
                                              .flatMap(List::stream)
                                              .collect(Collectors.toList());
        var gasAgreements = properties.stream()
                                      .map(Property::getGasMeterPoints)
                                      .flatMap(List::stream)
                                      .map(GasMeterPoint::getAgreements)
                                      .flatMap(List::stream)
                                      .collect(Collectors.toList());
        var electricityPrices = api.getElectricityPrices(electricityAgreements);
        log.info("done");
    }
}