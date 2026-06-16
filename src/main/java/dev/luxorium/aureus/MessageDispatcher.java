package dev.luxorium.aureus;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class MessageDispatcher {
    private final AureusPlugin plugin;

    public MessageDispatcher(AureusPlugin plugin) {
        this.plugin = plugin;
    }

    public void send(CommandSender sender, String message) {
        if (sender instanceof Player player) {
            player.getScheduler().run(plugin, task -> player.sendMessage(message), null);
            return;
        }
        plugin.getServer().getGlobalRegionScheduler().run(plugin, task -> sender.sendMessage(message));
    }
}
