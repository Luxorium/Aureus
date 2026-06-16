package dev.luxorium.aureus.command;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.luxorium.aureus.AureusPlugin;
import dev.luxorium.aureus.MessageDispatcher;
import dev.luxorium.aureus.api.EconomyResult;
import dev.luxorium.aureus.api.EconomyService;
import dev.luxorium.aureus.model.Account;
import dev.luxorium.aureus.model.TransactionType;

public final class EcoCommand implements CommandExecutor {
    private final EconomyService economyService;
    private final MessageDispatcher messages;
    private final PlayerResolver resolver;

    public EcoCommand(AureusPlugin plugin, EconomyService economyService, MessageDispatcher messages) {
        this.economyService = economyService;
        this.messages = messages;
        this.resolver = new PlayerResolver(plugin, economyService);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            messages.send(sender, "Usage: /eco <give|take|set|reset> <player> [amount]");
            return true;
        }
        String action = args[0].toLowerCase(Locale.ROOT);
        if (!action.equals("reset") && args.length < 3) {
            messages.send(sender, "Usage: /eco " + action + " <player> <amount>");
            return true;
        }
        UUID actorUuid = sender instanceof Player player ? player.getUniqueId() : null;
        resolver.resolveAccount(args[1]).thenCompose(account -> {
            if (account == null) {
                return CompletableFuture.completedFuture(new EcoView(null, EconomyResult.failure(dev.luxorium.aureus.api.EconomyFailure.ACCOUNT_NOT_FOUND, "")));
            }
            return execute(action, account, args, actorUuid).thenApply(result -> new EcoView(account, result));
        }).thenAccept(view -> {
            if (!view.result().success()) {
                messages.send(sender, CommandMessages.failure(view.result().failure(), view.result().message()));
                return;
            }
            messages.send(sender, "Updated " + view.account().username() + " to " + economyService.format(view.result().balanceMinor()) + ".");
        }).exceptionally(exception -> {
            messages.send(sender, "Unable to update balance.");
            return null;
        });
        return true;
    }

    private CompletableFuture<EconomyResult> execute(String action, Account account, String[] args, UUID actorUuid) {
        return switch (action) {
            case "give" -> economyService.deposit(account.uuid(), economyService.parseAmount(args[2]), TransactionType.ADMIN_GIVE, "Admin give", actorUuid);
            case "take" -> economyService.withdraw(account.uuid(), economyService.parseAmount(args[2]), TransactionType.ADMIN_TAKE, "Admin take", actorUuid);
            case "set" -> economyService.setBalance(account.uuid(), economyService.parseAmount(args[2]), "Admin set", actorUuid);
            case "reset" -> economyService.resetBalance(account.uuid(), "Admin reset", actorUuid);
            default -> CompletableFuture.completedFuture(EconomyResult.failure(dev.luxorium.aureus.api.EconomyFailure.INVALID_AMOUNT, "Unknown action."));
        };
    }

    private record EcoView(Account account, EconomyResult result) {
    }
}
