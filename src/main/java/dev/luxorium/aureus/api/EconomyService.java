package dev.luxorium.aureus.api;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import dev.luxorium.aureus.model.Account;
import dev.luxorium.aureus.model.BalanceEntry;
import dev.luxorium.aureus.model.Transaction;
import dev.luxorium.aureus.model.TransactionType;
import dev.luxorium.aureus.storage.EconomyStorage;

public final class EconomyService {
    private final EconomyStorage storage;
    private final Executor executor;
    private volatile EconomySettings settings;
    private volatile List<BalanceEntry> cachedTop = List.of();
    private volatile Instant cachedTopExpiresAt = Instant.EPOCH;

    public EconomyService(EconomyStorage storage, EconomySettings settings, Executor executor) {
        this.storage = Objects.requireNonNull(storage, "storage");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    public void updateSettings(EconomySettings settings) {
        this.settings = Objects.requireNonNull(settings, "settings");
        invalidateBalanceTop();
    }

    public EconomySettings settings() {
        return settings;
    }

    public CompletableFuture<Account> ensureAccount(UUID uuid, String username) {
        return supply(() -> storage.ensureAccount(uuid, username, settings.startingBalanceMinor()));
    }

    public CompletableFuture<Account> getAccount(UUID uuid) {
        return supply(() -> storage.findAccount(uuid).orElse(null));
    }

    public CompletableFuture<Account> findAccountByUsername(String username) {
        return supply(() -> storage.findAccountByUsername(username).orElse(null));
    }

    public CompletableFuture<EconomyResult> deposit(UUID targetUuid, long amountMinor, TransactionType type, String reason, UUID actorUuid) {
        return supplyResult(() -> {
            EconomyResult validation = validatePositiveAmount(amountMinor, false);
            if (!validation.success()) {
                return validation;
            }
            EconomyStorage.BalanceMutation mutation = storage.deposit(targetUuid, amountMinor, type, reason, actorUuid);
            invalidateBalanceTop();
            return EconomyResult.success(mutation.balanceMinor(), mutation.transactionId());
        });
    }

    public CompletableFuture<EconomyResult> withdraw(UUID targetUuid, long amountMinor, TransactionType type, String reason, UUID actorUuid) {
        return supplyResult(() -> {
            EconomyResult validation = validatePositiveAmount(amountMinor, false);
            if (!validation.success()) {
                return validation;
            }
            EconomyStorage.BalanceMutation mutation = storage.withdraw(targetUuid, amountMinor, type, reason, actorUuid);
            invalidateBalanceTop();
            return EconomyResult.success(mutation.balanceMinor(), mutation.transactionId());
        });
    }

    public CompletableFuture<EconomyResult> setBalance(UUID targetUuid, long amountMinor, String reason, UUID actorUuid) {
        return supplyResult(() -> {
            if (amountMinor < 0L) {
                return EconomyResult.failure(EconomyFailure.INVALID_AMOUNT, "Amount must not be negative.");
            }
            EconomyStorage.BalanceMutation mutation = storage.setBalance(targetUuid, amountMinor, TransactionType.ADMIN_SET, reason, actorUuid);
            invalidateBalanceTop();
            return EconomyResult.success(mutation.balanceMinor(), mutation.transactionId());
        });
    }

    public CompletableFuture<EconomyResult> resetBalance(UUID targetUuid, String reason, UUID actorUuid) {
        return setBalance(targetUuid, settings.startingBalanceMinor(), reason, actorUuid);
    }

    public CompletableFuture<TransferResult> transfer(UUID sourceUuid, UUID targetUuid, long amountMinor, String reason) {
        return supplyTransferResult(() -> {
            if (sourceUuid.equals(targetUuid)) {
                return TransferResult.failure(EconomyFailure.SAME_ACCOUNT, "You cannot pay yourself.");
            }
            EconomyResult validation = validatePositiveAmount(amountMinor, true);
            if (!validation.success()) {
                return TransferResult.failure(validation.failure(), validation.message());
            }
            EconomyStorage.TransferMutation mutation = storage.transfer(sourceUuid, targetUuid, amountMinor, reason);
            invalidateBalanceTop();
            return TransferResult.success(mutation.sourceBalanceMinor(), mutation.targetBalanceMinor(), mutation.transactionId());
        });
    }

    public CompletableFuture<List<BalanceEntry>> balanceTop(int limit) {
        Instant now = Instant.now();
        List<BalanceEntry> snapshot = cachedTop;
        if (now.isBefore(cachedTopExpiresAt) && snapshot.size() >= limit) {
            return CompletableFuture.completedFuture(snapshot.stream().limit(limit).toList());
        }
        return supply(() -> {
            List<BalanceEntry> entries = storage.balanceTop(limit);
            cachedTop = entries;
            cachedTopExpiresAt = Instant.now().plus(Duration.ofSeconds(settings.balanceTopCacheSeconds()));
            return entries;
        });
    }

    public CompletableFuture<List<Transaction>> transactions(UUID accountUuid, int limit) {
        return supply(() -> storage.transactionsForAccount(accountUuid, limit));
    }

    public long parseAmount(String input) {
        return settings.parser().parse(input).orElse(-1L);
    }

    public String format(long minor) {
        return settings.formatter().format(minor);
    }

    private EconomyResult validatePositiveAmount(long amountMinor, boolean enforcePaymentLimits) {
        if (amountMinor <= 0L) {
            return EconomyResult.failure(EconomyFailure.INVALID_AMOUNT, "Amount must be greater than zero.");
        }
        if (enforcePaymentLimits && amountMinor < settings.minimumPaymentMinor()) {
            return EconomyResult.failure(EconomyFailure.BELOW_MINIMUM_PAYMENT, "Amount is below the minimum payment.");
        }
        if (enforcePaymentLimits && amountMinor > settings.maximumPaymentMinor()) {
            return EconomyResult.failure(EconomyFailure.ABOVE_MAXIMUM_PAYMENT, "Amount is above the maximum payment.");
        }
        return EconomyResult.success(0L, 0L);
    }

    private void invalidateBalanceTop() {
        cachedTopExpiresAt = Instant.EPOCH;
    }

    private <T> CompletableFuture<T> supply(SqlSupplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (SQLException exception) {
                throw new EconomyStorageException(exception);
            }
        }, executor);
    }

    private CompletableFuture<EconomyResult> supplyResult(SqlSupplier<EconomyResult> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (EconomyStorage.AccountMissingException exception) {
                return EconomyResult.failure(EconomyFailure.ACCOUNT_NOT_FOUND, "Account not found.");
            } catch (EconomyStorage.InsufficientFundsException exception) {
                return EconomyResult.failure(EconomyFailure.INSUFFICIENT_FUNDS, "Insufficient funds.");
            } catch (SQLException exception) {
                return EconomyResult.failure(EconomyFailure.STORAGE_ERROR, "Storage error.");
            }
        }, executor);
    }

    private CompletableFuture<TransferResult> supplyTransferResult(SqlSupplier<TransferResult> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (EconomyStorage.AccountMissingException exception) {
                return TransferResult.failure(EconomyFailure.ACCOUNT_NOT_FOUND, "Account not found.");
            } catch (EconomyStorage.InsufficientFundsException exception) {
                return TransferResult.failure(EconomyFailure.INSUFFICIENT_FUNDS, "Insufficient funds.");
            } catch (SQLException exception) {
                return TransferResult.failure(EconomyFailure.STORAGE_ERROR, "Storage error.");
            }
        }, executor);
    }

    @FunctionalInterface
    private interface SqlSupplier<T> {
        T get() throws SQLException;
    }

    private static final class EconomyStorageException extends RuntimeException {
        private EconomyStorageException(Throwable cause) {
            super(cause);
        }
    }
}
