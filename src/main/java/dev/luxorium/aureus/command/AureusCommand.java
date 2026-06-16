package dev.luxorium.aureus.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import dev.luxorium.aureus.AureusPlugin;
import dev.luxorium.aureus.MessageDispatcher;

public final class AureusCommand implements CommandExecutor {
    private final AureusPlugin plugin;
    private final MessageDispatcher messages;

    public AureusCommand(AureusPlugin plugin, MessageDispatcher messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadAureusConfig();
            messages.send(sender, "Aureus configuration reloaded.");
            return true;
        }
        messages.send(sender, "Usage: /aureus reload");
        return true;
    }
}
