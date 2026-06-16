package dev.luxorium.aureus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import dev.luxorium.aureus.util.MoneyParser;

final class MoneyParserTest {
    private final MoneyParser parser = new MoneyParser(2, ".", ",", "⛃");

    @Test
    void parsesMinorUnitsSafely() {
        assertEquals(123456L, parser.parse("⛃1,234.56").orElseThrow());
        assertEquals(100L, parser.parse("1").orElseThrow());
        assertEquals(105L, parser.parse("1.05").orElseThrow());
    }

    @Test
    void rejectsUnsafeAmounts() {
        assertTrue(parser.parse("-1.00").isEmpty());
        assertTrue(parser.parse("1.005").isEmpty());
        assertTrue(parser.parse("abc").isEmpty());
    }
}
