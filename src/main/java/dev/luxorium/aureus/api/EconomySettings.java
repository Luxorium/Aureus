package dev.luxorium.aureus.api;

import dev.luxorium.aureus.util.MoneyFormatter;
import dev.luxorium.aureus.util.MoneyParser;

public record EconomySettings(
        long startingBalanceMinor,
        long minimumPaymentMinor,
        long maximumPaymentMinor,
        int balanceTopCacheSeconds,
        int transactionsPageSize,
        MoneyFormatter formatter,
        MoneyParser parser
) {
}
