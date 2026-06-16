package dev.luxorium.aureus.model;

import java.time.Instant;
import java.util.UUID;

public record Transaction(
        long id,
        UUID sourceUuid,
        UUID targetUuid,
        long amountMinor,
        TransactionType type,
        String reason,
        UUID actorUuid,
        Instant createdAt
) {
}
