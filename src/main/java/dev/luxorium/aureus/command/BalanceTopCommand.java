package dev.luxorium.aureus.command;

import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import dev.luxorium.aureus.AureusPlugin;
import dev.luxorium.aureus.MessageDispatcher;
import dev.luxorium.aureus.api.EconomyService;
import dev.luxorium.aureus.model.BalanceEntry;

public final class BalanceTopCommand implements CommandExecutor {
    private final EconomyService economyService;
    private final MessageDispatcher messages;

    public BalanceTopCommand(AureusPlugin plugin, EconomyService economyService, MessageDispatcher messages) {
        this.economyService = economyService;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        economyService.balanceTop(10).thenAccept(entries -> messages.send(sender, render(entries)))
                .exceptionally(exception -> {
                    messages.send(sender, "Unable to load balance top.");
                    return null;
                });
        return true;
    }

    private String render(List<BalanceEntry> entries) {
        if (entries.isEmpty()) {
            return "No Aureus accounts have been created yet.";
        }
        StringBuilder builder = new StringBuilder("Top Aurei balances:");
        for (int index = 0; index < entries.size(); index++) {
            BalanceEntry entry = entries.get(index);
            builder.append('\n')
                    .append(index + 1)
                    .append(". ")
                    .append(entry.username())
                    .append(" - ")
                    .append(economyService.format(entry.balanceMinor()));
        }
        return builder.toString();
    }
}
