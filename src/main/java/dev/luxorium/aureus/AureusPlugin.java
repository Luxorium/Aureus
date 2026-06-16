package dev.luxorium.aureus;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import dev.luxorium.aureus.api.EconomyService;
import dev.luxorium.aureus.command.AureusCommand;
import dev.luxorium.aureus.command.BalanceCommand;
import dev.luxorium.aureus.command.BalanceTopCommand;
import dev.luxorium.aureus.command.EcoCommand;
import dev.luxorium.aureus.command.PayCommand;
import dev.luxorium.aureus.command.TransactionsCommand;
import dev.luxorium.aureus.config.AureusConfig;
import dev.luxorium.aureus.storage.EconomyStorage;

public final class AureusPlugin extends JavaPlugin {
    private EconomyStorage storage;
    private EconomyService economyService;
    private MessageDispatcher messageDispatcher;
    private AureusConfig aureusConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadAureusConfig();

        Executor asyncExecutor = task -> getServer().getAsyncScheduler().runNow(this, scheduledTask -> task.run());
        Path databasePath = getDataFolder().toPath().resolve(aureusConfig.storageFile());
        storage = new EconomyStorage(databasePath);
        messageDispatcher = new MessageDispatcher(this);

        asyncExecutor.execute(() -> {
            try {
                storage.initialize();
                getLogger().info("SQLite storage initialized with WAL mode.");
            } catch (Exception exception) {
                getLogger().log(Level.SEVERE, "Failed to initialize Aureus storage.", exception);
                getServer().getGlobalRegionScheduler().run(this, task -> getServer().getPluginManager().disablePlugin(this));
            }
        });

        economyService = new EconomyService(storage, aureusConfig.economy(), asyncExecutor);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(economyService), this);
        registerCommands();
    }

    @Override
    public void onDisable() {
        if (storage != null) {
            storage.close();
        }
    }

    public EconomyService economyService() {
        return economyService;
    }

    public AureusConfig aureusConfig() {
        return aureusConfig;
    }

    public void reloadAureusConfig() {
        reloadConfig();
        aureusConfig = AureusConfig.from(getConfig());
        if (economyService != null) {
            economyService.updateSettings(aureusConfig.economy());
        }
    }

    private void registerCommands() {
        setExecutor("balance", new BalanceCommand(this, economyService, messageDispatcher));
        setExecutor("pay", new PayCommand(this, economyService, messageDispatcher));
        setExecutor("baltop", new BalanceTopCommand(this, economyService, messageDispatcher));
        setExecutor("eco", new EcoCommand(this, economyService, messageDispatcher));
        setExecutor("transactions", new TransactionsCommand(this, economyService, messageDispatcher));
        setExecutor("aureus", new AureusCommand(this, messageDispatcher));
    }

    private void setExecutor(String commandName, org.bukkit.command.CommandExecutor executor) {
        PluginCommand command = Objects.requireNonNull(getCommand(commandName), "Missing command: " + commandName);
        command.setExecutor(executor);
    }
}
