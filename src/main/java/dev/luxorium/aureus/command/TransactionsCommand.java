package dev.luxorium.aureus.command;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.luxorium.aureus.AureusPlugin;
import dev.luxorium.aureus.MessageDispatcher;
import dev.luxorium.aureus.api.EconomyService;
import dev.luxorium.aureus.model.Account;
import dev.luxorium.aureus.model.Transaction;

public final class TransactionsCommand implements CommandExecutor {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    private final EconomyService economyService;
    private final MessageDispatcher messages;
    private final PlayerResolver resolver;

    public TransactionsCommand(AureusPlugin plugin, EconomyService economyService, MessageDispatcher messages) {
        this.economyService = economyService;
        this.messages = messages;
        this.resolver = new PlayerResolver(plugin, economyService);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                messages.send(sender, "Usage: /transactions <player>");
                return true;
            }
            economyService.ensureAccount(player.getUniqueId(), player.getName())
                    .thenCompose(account -> economyService.transactions(account.uuid(), economyService.settings().transactionsPageSize())
                            .thenApply(transactions -> new TransactionView(account, transactions)))
                    .thenAccept(view -> messages.send(sender, render(view)))
                    .exceptionally(exception -> {
                        messages.send(sender, "Unable to load transactions.");
                        return null;
                    });
            return true;
        }
        if (!sender.hasPermission("aureus.admin.transactions.other")) {
            messages.send(sender, "You do not have permission to view other players' transactions.");
            return true;
        }
        resolver.resolveAccount(args[0])
                .thenCompose(account -> {
                    if (account == null) {
                        return java.util.concurrent.CompletableFuture.completedFuture(null);
                    }
                    return economyService.transactions(account.uuid(), economyService.settings().transactionsPageSize())
                            .thenApply(transactions -> new TransactionView(account, transactions));
                })
                .thenAccept(view -> {
                    if (view == null) {
                        messages.send(sender, "No Aureus account was found for that player.");
                        return;
                    }
                    messages.send(sender, render(view));
                })
                .exceptionally(exception -> {
                    messages.send(sender, "Unable to load transactions.");
                    return null;
                });
        return true;
    }

    private String render(TransactionView view) {
        if (view.transactions().isEmpty()) {
            return "No transactions found for " + view.account().username() + ".";
        }
        StringBuilder builder = new StringBuilder("Transactions for ").append(view.account().username()).append(':');
        for (Transaction transaction : view.transactions()) {
            builder.append('\n')
                    .append('#').append(transaction.id())
                    .append(' ')
                    .append(transaction.type())
                    .append(' ')
                    .append(economyService.format(transaction.amountMinor()))
                    .append(" at ")
                    .append(FORMATTER.format(transaction.createdAt()));
        }
        return builder.toString();
    }

    private record TransactionView(Account account, List<Transaction> transactions) {
    }
}
