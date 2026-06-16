package dev.luxorium.aureus.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.OptionalLong;
import java.util.regex.Pattern;

public final class MoneyParser {
    private static final Pattern VALID = Pattern.compile("[0-9]+(\\.[0-9]+)?");

    private final int decimalPlaces;
    private final String decimalSeparator;
    private final String thousandsSeparator;
    private final String symbol;

    public MoneyParser(int decimalPlaces, String decimalSeparator, String thousandsSeparator, String symbol) {
        this.decimalPlaces = Math.max(0, decimalPlaces);
        this.decimalSeparator = decimalSeparator;
        this.thousandsSeparator = thousandsSeparator;
        this.symbol = symbol;
    }

    public OptionalLong parse(String input) {
        if (input == null) {
            return OptionalLong.empty();
        }
        String normalized = input.trim();
        if (normalized.isEmpty() || normalized.startsWith("-") || normalized.startsWith("+")) {
            return OptionalLong.empty();
        }
        if (!symbol.isBlank()) {
            normalized = normalized.replace(symbol, "");
        }
        if (!thousandsSeparator.isEmpty()) {
            normalized = normalized.replace(thousandsSeparator, "");
        }
        if (!".".equals(decimalSeparator) && !decimalSeparator.isEmpty()) {
            normalized = normalized.replace(decimalSeparator, ".");
        }
        normalized = normalized.trim();
        if (!VALID.matcher(normalized).matches()) {
            return OptionalLong.empty();
        }
        try {
            BigDecimal amount = new BigDecimal(normalized);
            if (amount.scale() > decimalPlaces) {
                return OptionalLong.empty();
            }
            BigDecimal minor = amount.movePointRight(decimalPlaces).setScale(0, RoundingMode.UNNECESSARY);
            return OptionalLong.of(minor.longValueExact());
        } catch (ArithmeticException | NumberFormatException exception) {
            return OptionalLong.empty();
        }
    }
}
