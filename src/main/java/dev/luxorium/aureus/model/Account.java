package dev.luxorium.aureus.model;

import java.time.Instant;
import java.util.UUID;

public record Account(
        UUID uuid,
        String username,
        long balanceMinor,
        Instant createdAt,
        Instant updatedAt
) {
}
