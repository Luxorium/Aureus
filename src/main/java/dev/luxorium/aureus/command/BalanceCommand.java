package dev.luxorium.aureus.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.luxorium.aureus.AureusPlugin;
import dev.luxorium.aureus.MessageDispatcher;
import dev.luxorium.aureus.api.EconomyService;

public final class BalanceCommand implements CommandExecutor {
    private final EconomyService economyService;
    private final MessageDispatcher messages;

    public BalanceCommand(AureusPlugin plugin, EconomyService economyService, MessageDispatcher messages) {
        this.economyService = economyService;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "Only players have balances.");
            return true;
        }
        economyService.ensureAccount(player.getUniqueId(), player.getName())
                .thenAccept(account -> messages.send(sender, "Balance: " + economyService.format(account.balanceMinor())))
                .exceptionally(exception -> {
                    messages.send(sender, "Unable to load your balance.");
                    return null;
                });
        return true;
    }
}
