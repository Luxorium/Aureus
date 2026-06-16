package dev.luxorium.aureus.storage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import dev.luxorium.aureus.model.Account;
import dev.luxorium.aureus.model.BalanceEntry;
import dev.luxorium.aureus.model.Transaction;
import dev.luxorium.aureus.model.TransactionType;

public final class EconomyStorage implements AutoCloseable {
    private final Path databasePath;
    private final Object writeLock = new Object();

    public EconomyStorage(Path databasePath) {
        this.databasePath = databasePath;
    }

    public void initialize() throws SQLException {
        try {
            Files.createDirectories(databasePath.getParent());
        } catch (Exception exception) {
            throw new SQLException("Failed to create storage directory.", exception);
        }
        try (Connection connection = connection(); Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL");
            statement.execute("PRAGMA synchronous=NORMAL");
            statement.execute("PRAGMA busy_timeout=5000");
            statement.execute("PRAGMA foreign_keys=ON");
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS accounts (
                        uuid TEXT PRIMARY KEY,
                        username TEXT NOT NULL,
                        balance_minor INTEGER NOT NULL,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        source_uuid TEXT,
                        target_uuid TEXT,
                        amount_minor INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        reason TEXT NOT NULL,
                        actor_uuid TEXT,
                        created_at INTEGER NOT NULL
                    )
                    """);
            statement.execute("CREATE INDEX IF NOT EXISTS idx_accounts_username ON accounts(username COLLATE NOCASE)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_accounts_balance ON accounts(balance_minor DESC)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_transactions_source_created ON transactions(source_uuid, created_at DESC)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_transactions_target_created ON transactions(target_uuid, created_at DESC)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_transactions_created ON transactions(created_at DESC)");
        }
    }

    public Account ensureAccount(UUID uuid, String username, long startingBalanceMinor) throws SQLException {
        synchronized (writeLock) {
            try (Connection connection = connection()) {
                connection.setAutoCommit(false);
                try {
                    Optional<Account> existing = findAccount(connection, uuid);
                    long now = now();
                    if (existing.isPresent()) {
                        try (PreparedStatement statement = connection.prepareStatement(
                                "UPDATE accounts SET username = ?, updated_at = ? WHERE uuid = ?")) {
                            statement.setString(1, username);
                            statement.setLong(2, now);
                            statement.setString(3, uuid.toString());
                            statement.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement statement = connection.prepareStatement(
                                "INSERT INTO accounts(uuid, username, balance_minor, created_at, updated_at) VALUES(?, ?, ?, ?, ?)")) {
                            statement.setString(1, uuid.toString());
                            statement.setString(2, username);
                            statement.setLong(3, startingBalanceMinor);
                            statement.setLong(4, now);
                            statement.setLong(5, now);
                            statement.executeUpdate();
                        }
                        if (startingBalanceMinor > 0L) {
                            insertTransaction(connection, null, uuid, startingBalanceMinor, TransactionType.ACCOUNT_CREATE, "Starting balance", null, now);
                        }
                    }
                    connection.commit();
                    return findAccount(connection, uuid).orElseThrow(AccountMissingException::new);
                } catch (SQLException | RuntimeException exception) {
                    connection.rollback();
                    throw exception;
                }
            }
        }
    }

    public Optional<Account> findAccount(UUID uuid) throws SQLException {
        try (Connection connection = connection()) {
            return findAccount(connection, uuid);
        }
    }

    public Optional<Account> findAccountByUsername(String username) throws SQLException {
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT uuid, username, balance_minor, created_at, updated_at FROM accounts WHERE username = ? COLLATE NOCASE")) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(readAccount(resultSet)) : Optional.empty();
            }
        }
    }

    public BalanceMutation deposit(UUID targetUuid, long amountMinor, TransactionType type, String reason, UUID actorUuid) throws SQLException {
        synchronized (writeLock) {
            try (Connection connection = connection()) {
                connection.setAutoCommit(false);
                try {
                    Account account = findAccount(connection, targetUuid).orElseThrow(AccountMissingException::new);
                    long newBalance = Math.addExact(account.balanceMinor(), amountMinor);
                    updateBalance(connection, targetUuid, newBalance);
                    long transactionId = insertTransaction(connection, null, targetUuid, amountMinor, type, reason, actorUuid, now());
                    connection.commit();
                    return new BalanceMutation(newBalance, transactionId);
                } catch (SQLException | RuntimeException exception) {
                    connection.rollback();
                    throw exception;
                }
            }
        }
    }

    public BalanceMutation withdraw(UUID targetUuid, long amountMinor, TransactionType type, String reason, UUID actorUuid) throws SQLException {
        synchronized (writeLock) {
            try (Connection connection = connection()) {
                connection.setAutoCommit(false);
                try {
                    Account account = findAccount(connection, targetUuid).orElseThrow(AccountMissingException::new);
                    if (account.balanceMinor() < amountMinor) {
                        throw new InsufficientFundsException();
                    }
                    long newBalance = account.balanceMinor() - amountMinor;
                    updateBalance(connection, targetUuid, newBalance);
                    long transactionId = insertTransaction(connection, targetUuid, null, amountMinor, type, reason, actorUuid, now());
                    connection.commit();
                    return new BalanceMutation(newBalance, transactionId);
                } catch (SQLException | RuntimeException exception) {
                    connection.rollback();
                    throw exception;
                }
            }
        }
    }

    public BalanceMutation setBalance(UUID targetUuid, long balanceMinor, TransactionType type, String reason, UUID actorUuid) throws SQLException {
        synchronized (writeLock) {
            try (Connection connection = connection()) {
                connection.setAutoCommit(false);
                try {
                    Account account = findAccount(connection, targetUuid).orElseThrow(AccountMissingException::new);
                    long delta = balanceMinor - account.balanceMinor();
                    updateBalance(connection, targetUuid, balanceMinor);
                    UUID source = delta < 0L ? targetUuid : null;
                    UUID target = delta >= 0L ? targetUuid : null;
                    long transactionId = insertTransaction(connection, source, target, Math.abs(delta), type, reason, actorUuid, now());
                    connection.commit();
                    return new BalanceMutation(balanceMinor, transactionId);
                } catch (SQLException | RuntimeException exception) {
                    connection.rollback();
                    throw exception;
                }
            }
        }
    }

    public TransferMutation transfer(UUID sourceUuid, UUID targetUuid, long amountMinor, String reason) throws SQLException {
        synchronized (writeLock) {
            try (Connection connection = connection()) {
                connection.setAutoCommit(false);
                try {
                    Account source = findAccount(connection, sourceUuid).orElseThrow(AccountMissingException::new);
                    Account target = findAccount(connection, targetUuid).orElseThrow(AccountMissingException::new);
                    if (source.balanceMinor() < amountMinor) {
                        throw new InsufficientFundsException();
                    }
                    long sourceBalance = source.balanceMinor() - amountMinor;
                    long targetBalance = Math.addExact(target.balanceMinor(), amountMinor);
                    updateBalance(connection, sourceUuid, sourceBalance);
                    updateBalance(connection, targetUuid, targetBalance);
                    long transactionId = insertTransaction(connection, sourceUuid, targetUuid, amountMinor, TransactionType.PLAYER_PAYMENT, reason, sourceUuid, now());
                    connection.commit();
                    return new TransferMutation(sourceBalance, targetBalance, transactionId);
                } catch (SQLException | RuntimeException exception) {
                    connection.rollback();
                    throw exception;
                }
            }
        }
    }

    public List<BalanceEntry> balanceTop(int limit) throws SQLException {
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT uuid, username, balance_minor FROM accounts ORDER BY balance_minor DESC, username COLLATE NOCASE ASC LIMIT ?")) {
            statement.setInt(1, Math.max(1, limit));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<BalanceEntry> entries = new ArrayList<>();
                while (resultSet.next()) {
                    entries.add(new BalanceEntry(
                            UUID.fromString(resultSet.getString("uuid")),
                            resultSet.getString("username"),
                            resultSet.getLong("balance_minor")
                    ));
                }
                return entries;
            }
        }
    }

    public List<Transaction> transactionsForAccount(UUID accountUuid, int limit) throws SQLException {
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT id, source_uuid, target_uuid, amount_minor, type, reason, actor_uuid, created_at
                     FROM transactions
                     WHERE source_uuid = ? OR target_uuid = ? OR actor_uuid = ?
                     ORDER BY created_at DESC, id DESC
                     LIMIT ?
                     """)) {
            statement.setString(1, accountUuid.toString());
            statement.setString(2, accountUuid.toString());
            statement.setString(3, accountUuid.toString());
            statement.setInt(4, Math.max(1, limit));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Transaction> transactions = new ArrayList<>();
                while (resultSet.next()) {
                    transactions.add(readTransaction(resultSet));
                }
                return transactions;
            }
        }
    }

    @Override
    public void close() {
    }

    private Connection connection() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA busy_timeout=5000");
            statement.execute("PRAGMA foreign_keys=ON");
        }
        return connection;
    }

    private Optional<Account> findAccount(Connection connection, UUID uuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT uuid, username, balance_minor, created_at, updated_at FROM accounts WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(readAccount(resultSet)) : Optional.empty();
            }
        }
    }

    private void updateBalance(Connection connection, UUID uuid, long balanceMinor) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE accounts SET balance_minor = ?, updated_at = ? WHERE uuid = ?")) {
            statement.setLong(1, balanceMinor);
            statement.setLong(2, now());
            statement.setString(3, uuid.toString());
            statement.executeUpdate();
        }
    }

    private long insertTransaction(Connection connection, UUID sourceUuid, UUID targetUuid, long amountMinor, TransactionType type, String reason, UUID actorUuid, long createdAt) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO transactions(source_uuid, target_uuid, amount_minor, type, reason, actor_uuid, created_at)
                VALUES(?, ?, ?, ?, ?, ?, ?)
                """, Statement.RETURN_GENERATED_KEYS)) {
            setUuid(statement, 1, sourceUuid);
            setUuid(statement, 2, targetUuid);
            statement.setLong(3, amountMinor);
            statement.setString(4, type.name());
            statement.setString(5, reason == null ? "" : reason);
            setUuid(statement, 6, actorUuid);
            statement.setLong(7, createdAt);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("Failed to read generated transaction id.");
    }

    private Account readAccount(ResultSet resultSet) throws SQLException {
        return new Account(
                UUID.fromString(resultSet.getString("uuid")),
                resultSet.getString("username"),
                resultSet.getLong("balance_minor"),
                Instant.ofEpochMilli(resultSet.getLong("created_at")),
                Instant.ofEpochMilli(resultSet.getLong("updated_at"))
        );
    }

    private Transaction readTransaction(ResultSet resultSet) throws SQLException {
        return new Transaction(
                resultSet.getLong("id"),
                readUuid(resultSet, "source_uuid"),
                readUuid(resultSet, "target_uuid"),
                resultSet.getLong("amount_minor"),
                TransactionType.valueOf(resultSet.getString("type")),
                resultSet.getString("reason"),
                readUuid(resultSet, "actor_uuid"),
                Instant.ofEpochMilli(resultSet.getLong("created_at"))
        );
    }

    private UUID readUuid(ResultSet resultSet, String column) throws SQLException {
        String value = resultSet.getString(column);
        return value == null ? null : UUID.fromString(value);
    }

    private void setUuid(PreparedStatement statement, int index, UUID uuid) throws SQLException {
        if (uuid == null) {
            statement.setString(index, null);
            return;
        }
        statement.setString(index, uuid.toString());
    }

    private long now() {
        return System.currentTimeMillis();
    }

    public record BalanceMutation(long balanceMinor, long transactionId) {
    }

    public record TransferMutation(long sourceBalanceMinor, long targetBalanceMinor, long transactionId) {
    }

    public static final class AccountMissingException extends RuntimeException {
    }

    public static final class InsufficientFundsException extends RuntimeException {
    }
}
