package dev.luxorium.aureus.config;

import org.bukkit.configuration.file.FileConfiguration;
import dev.luxorium.aureus.api.EconomySettings;
import dev.luxorium.aureus.util.MoneyFormatter;
import dev.luxorium.aureus.util.MoneyParser;

public record AureusConfig(
        EconomySettings economy,
        String storageFile
) {
    public static AureusConfig from(FileConfiguration config) {
        MoneyFormatter formatter = new MoneyFormatter(
                config.getString("currency.singular", "Aureus"),
                config.getString("currency.plural", "Aurei"),
                config.getString("currency.symbol", "⛃"),
                config.getInt("currency.decimal-places", 2),
                config.getBoolean("currency.show-symbol", true),
                config.getBoolean("currency.symbol-before-amount", true),
                config.getString("currency.thousands-separator", ","),
                config.getString("currency.decimal-separator", ".")
        );
        MoneyParser parser = new MoneyParser(formatter.decimalPlaces(), formatter.decimalSeparator(), formatter.thousandsSeparator(), formatter.symbol());
        long startingBalance = parser.parse(config.getString("economy.starting-balance", "100.00")).orElseThrow();
        long minimumPayment = parser.parse(config.getString("economy.minimum-payment", "0.01")).orElseThrow();
        long maximumPayment = parser.parse(config.getString("economy.maximum-payment", "1000000.00")).orElseThrow();
        EconomySettings settings = new EconomySettings(
                startingBalance,
                minimumPayment,
                maximumPayment,
                Math.max(1, config.getInt("economy.baltop-cache-seconds", 60)),
                Math.max(1, config.getInt("economy.transactions-page-size", 10)),
                formatter,
                parser
        );
        return new AureusConfig(settings, config.getString("storage.sqlite-file", "aureus.db"));
    }
}
