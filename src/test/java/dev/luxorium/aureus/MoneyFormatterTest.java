package dev.luxorium.aureus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import dev.luxorium.aureus.util.MoneyFormatter;

final class MoneyFormatterTest {
    @Test
    void formatsWithConfiguredSymbolAndSeparators() {
        MoneyFormatter formatter = new MoneyFormatter("Aureus", "Aurei", "⛃", 2, true, true, ",", ".");
        assertEquals("⛃1,234.56", formatter.format(123456L));
    }

    @Test
    void canFormatWithCurrencyNames() {
        MoneyFormatter formatter = new MoneyFormatter("Aureus", "Aurei", "⛃", 2, false, true, ",", ".");
        assertEquals("1.00 Aureus", formatter.format(100L));
        assertEquals("2.00 Aurei", formatter.format(200L));
    }
}
