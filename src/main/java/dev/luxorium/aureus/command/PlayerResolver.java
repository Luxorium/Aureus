package dev.luxorium.aureus.command;

import java.util.concurrent.CompletableFuture;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import dev.luxorium.aureus.AureusPlugin;
import dev.luxorium.aureus.api.EconomyService;
import dev.luxorium.aureus.model.Account;

final class PlayerResolver {
    private final AureusPlugin plugin;
    private final EconomyService economyService;

    PlayerResolver(AureusPlugin plugin, EconomyService economyService) {
        this.plugin = plugin;
        this.economyService = economyService;
    }

    CompletableFuture<Account> resolveAccount(String username) {
        Player online = plugin.getServer().getPlayerExact(username);
        if (online != null) {
            return economyService.ensureAccount(online.getUniqueId(), online.getName());
        }
        OfflinePlayer cached = plugin.getServer().getOfflinePlayerIfCached(username);
        if (cached != null && cached.getName() != null) {
            return economyService.ensureAccount(cached.getUniqueId(), cached.getName());
        }
        return economyService.findAccountByUsername(username);
    }
}
