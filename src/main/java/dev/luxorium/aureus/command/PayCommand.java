package dev.luxorium.aureus.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.luxorium.aureus.AureusPlugin;
import dev.luxorium.aureus.MessageDispatcher;
import dev.luxorium.aureus.api.EconomyService;
import dev.luxorium.aureus.model.Account;

public final class PayCommand implements CommandExecutor {
    private final EconomyService economyService;
    private final MessageDispatcher messages;
    private final PlayerResolver resolver;

    public PayCommand(AureusPlugin plugin, EconomyService economyService, MessageDispatcher messages) {
        this.economyService = economyService;
        this.messages = messages;
        this.resolver = new PlayerResolver(plugin, economyService);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "Only players can pay other players.");
            return true;
        }
        if (args.length != 2) {
            messages.send(sender, "Usage: /pay <player> <amount>");
            return true;
        }
        long amountMinor = economyService.parseAmount(args[1]);
        resolver.resolveAccount(args[0]).thenCompose(target -> {
            if (target == null) {
                return java.util.concurrent.CompletableFuture.completedFuture(null);
            }
            return economyService.ensureAccount(player.getUniqueId(), player.getName())
                    .thenCompose(source -> economyService.transfer(source.uuid(), target.uuid(), amountMinor, "Player payment to " + target.username())
                            .thenApply(result -> new PaymentView(target, result)));
        }).thenAccept(view -> {
            if (view == null) {
                messages.send(sender, "No Aureus account was found for that player.");
                return;
            }
            if (!view.result().success()) {
                messages.send(sender, CommandMessages.failure(view.result().failure(), view.result().message()));
                return;
            }
            messages.send(sender, "Paid " + view.target().username() + " " + economyService.format(amountMinor) + ".");
        }).exceptionally(exception -> {
            messages.send(sender, "Unable to complete payment.");
            return null;
        });
        return true;
    }

    private record PaymentView(Account target, dev.luxorium.aureus.api.TransferResult result) {
    }
}
