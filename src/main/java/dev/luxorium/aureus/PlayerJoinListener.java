package dev.luxorium.aureus;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import dev.luxorium.aureus.api.EconomyService;

public final class PlayerJoinListener implements Listener {
    private final EconomyService economyService;

    public PlayerJoinListener(EconomyService economyService) {
        this.economyService = economyService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        economyService.ensureAccount(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }
}
