package com.kineticfitness.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SessionNamesTest {

    @Test
    void trimsWhitespace() {
        assertEquals("Push Day", SessionNames.normalize("  Push Day  "));
    }

    @Test
    void blankBecomesNull() {
        assertNull(SessionNames.normalize("   "));
    }

    @Test
    void emptyBecomesNull() {
        assertNull(SessionNames.normalize(""));
    }

    @Test
    void nullStaysNull() {
        assertNull(SessionNames.normalize(null));
    }
}