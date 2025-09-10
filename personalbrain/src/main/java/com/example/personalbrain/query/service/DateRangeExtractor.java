package com.example.personalbrain.query.service;

import org.springframework.stereotype.Component;

import java.time.*;
import java.util.Optional;
import java.util.regex.*;

@Component
public class DateRangeExtractor {

    private static final Pattern pattern = Pattern.compile(
        "(?i)(in|from)?\\s*(january|february|march|april|may|june|july|august|september|october|november|december)\\s*(\\d{4})?"
    );
    public record EpochRange(long startEpoch, long endEpoch) {}

    public Optional<EpochRange> extractDateRange(String input) {
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String monthName = matcher.group(2);
            String yearStr = matcher.group(3) != null ? matcher.group(3) : String.valueOf(LocalDate.now().getYear());

            Month month = Month.valueOf(monthName.toUpperCase());
            int year = Integer.parseInt(yearStr);

            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
            long startEpoch = start.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
            long endEpoch   = end.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
            return Optional.of(new EpochRange(startEpoch, endEpoch));
        }
        return Optional.empty();
    }
}