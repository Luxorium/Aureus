package dev.luxorium.aureus.model;

import java.util.UUID;

public record BalanceEntry(
        UUID uuid,
        String username,
        long balanceMinor
) {
}
