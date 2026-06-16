package dev.luxorium.aureus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import dev.luxorium.aureus.api.EconomyFailure;
import dev.luxorium.aureus.api.EconomyResult;
import dev.luxorium.aureus.api.EconomyService;
import dev.luxorium.aureus.api.EconomySettings;
import dev.luxorium.aureus.api.TransferResult;
import dev.luxorium.aureus.model.TransactionType;
import dev.luxorium.aureus.storage.EconomyStorage;
import dev.luxorium.aureus.util.MoneyFormatter;
import dev.luxorium.aureus.util.MoneyParser;

final class EconomyServiceTest {
    @TempDir
    Path tempDir;

    private EconomyService service;
    private EconomyStorage storage;
    private final UUID alba = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID brutus = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @BeforeEach
    void setUp() throws Exception {
        storage = new EconomyStorage(tempDir.resolve("aureus.db"));
        storage.initialize();
        MoneyFormatter formatter = new MoneyFormatter("Aureus", "Aurei", "⛃", 2, true, true, ",", ".");
        EconomySettings settings = new EconomySettings(
                10_000L,
                1L,
                1_000_000_00L,
                60,
                10,
                formatter,
                new MoneyParser(2, ".", ",", "⛃")
        );
        Executor direct = Runnable::run;
        service = new EconomyService(storage, settings, direct);
        service.ensureAccount(alba, "Alba").join();
        service.ensureAccount(brutus, "Brutus").join();
    }

    @Test
    void depositsWriteBalanceAndTransaction() {
        EconomyResult result = service.deposit(alba, 500L, TransactionType.ADMIN_GIVE, "test", null).join();
        assertTrue(result.success());
        assertEquals(10_500L, result.balanceMinor());
        assertEquals(2, service.transactions(alba, 10).join().size());
    }

    @Test
    void withdrawalsWriteBalanceAndTransaction() {
        EconomyResult result = service.withdraw(alba, 250L, TransactionType.ADMIN_TAKE, "test", null).join();
        assertTrue(result.success());
        assertEquals(9_750L, result.balanceMinor());
        assertEquals(2, service.transactions(alba, 10).join().size());
    }

    @Test
    void transfersMoveFundsAndLogLedgerEntry() {
        TransferResult result = service.transfer(alba, brutus, 1_000L, "test").join();
        assertTrue(result.success());
        assertEquals(9_000L, result.sourceBalanceMinor());
        assertEquals(11_000L, result.targetBalanceMinor());
        assertEquals(2, service.transactions(alba, 10).join().size());
        assertEquals(2, service.transactions(brutus, 10).join().size());
    }

    @Test
    void insufficientFundsDoesNotMutateBalance() {
        EconomyResult result = service.withdraw(alba, 20_000L, TransactionType.ADMIN_TAKE, "test", null).join();
        assertFalse(result.success());
        assertEquals(EconomyFailure.INSUFFICIENT_FUNDS, result.failure());
        assertEquals(10_000L, service.getAccount(alba).join().balanceMinor());
        assertEquals(1, service.transactions(alba, 10).join().size());
    }
}
