package dev.luxorium.aureus.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyFormatter {
    private final String singular;
    private final String plural;
    private final String symbol;
    private final int decimalPlaces;
    private final boolean showSymbol;
    private final boolean symbolBeforeAmount;
    private final String thousandsSeparator;
    private final String decimalSeparator;

    public MoneyFormatter(
            String singular,
            String plural,
            String symbol,
            int decimalPlaces,
            boolean showSymbol,
            boolean symbolBeforeAmount,
            String thousandsSeparator,
            String decimalSeparator
    ) {
        this.singular = singular;
        this.plural = plural;
        this.symbol = symbol;
        this.decimalPlaces = Math.max(0, decimalPlaces);
        this.showSymbol = showSymbol;
        this.symbolBeforeAmount = symbolBeforeAmount;
        this.thousandsSeparator = thousandsSeparator;
        this.decimalSeparator = decimalSeparator;
    }

    public String format(long minor) {
        BigDecimal amount = BigDecimal.valueOf(minor, decimalPlaces).setScale(decimalPlaces, RoundingMode.UNNECESSARY);
        String numeric = amount.toPlainString();
        if (!".".equals(decimalSeparator)) {
            numeric = numeric.replace(".", decimalSeparator);
        }
        numeric = addThousandsSeparators(numeric);
        if (!showSymbol || symbol.isBlank()) {
            return numeric + " " + currencyName(minor);
        }
        return symbolBeforeAmount ? symbol + numeric : numeric + symbol;
    }

    public String currencyName(long minor) {
        return Math.abs(minor) == scale() ? singular : plural;
    }

    public int decimalPlaces() {
        return decimalPlaces;
    }

    public String decimalSeparator() {
        return decimalSeparator;
    }

    public String thousandsSeparator() {
        return thousandsSeparator;
    }

    public String symbol() {
        return symbol;
    }

    private String addThousandsSeparators(String numeric) {
        if (thousandsSeparator.isEmpty()) {
            return numeric;
        }
        int decimalIndex = numeric.indexOf(decimalSeparator);
        String whole = decimalIndex >= 0 ? numeric.substring(0, decimalIndex) : numeric;
        String fraction = decimalIndex >= 0 ? numeric.substring(decimalIndex) : "";
        StringBuilder builder = new StringBuilder();
        int firstGroup = whole.length() % 3;
        if (firstGroup == 0) {
            firstGroup = 3;
        }
        for (int index = 0; index < whole.length(); index++) {
            if (index > 0 && (index - firstGroup) % 3 == 0) {
                builder.append(thousandsSeparator);
            }
            builder.append(whole.charAt(index));
        }
        return builder.append(fraction).toString();
    }

    private long scale() {
        long scale = 1L;
        for (int index = 0; index < decimalPlaces; index++) {
            scale *= 10L;
        }
        return scale;
    }
}
