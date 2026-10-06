package com.kineticfitness.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Behaviour tests for {@link UnitSystem}, mainly {@link UnitSystem#parse}, which will
 * read the saved preference back from the database. A blank or damaged value must never
 * stop the app from loading.
 */
class UnitSystemTest {

    @Test
    @DisplayName("Each system reports its own display name and units")
    void eachSystem_exposesItsLabelsAndUnits() {
        assertEquals("Metric", UnitSystem.METRIC.displayName());
        assertEquals("kg", UnitSystem.METRIC.weightUnit());
        assertEquals("cm", UnitSystem.METRIC.heightUnit());
        assertEquals("Imperial", UnitSystem.IMPERIAL.displayName());
        assertEquals("lb", UnitSystem.IMPERIAL.weightUnit());
        assertEquals("in", UnitSystem.IMPERIAL.heightUnit());
    }

    @Test
    @DisplayName("Saved text is read back regardless of case or spacing")
    void parse_acceptsEitherNameFormat() {
        assertEquals(UnitSystem.IMPERIAL, UnitSystem.parse("IMPERIAL"));
        assertEquals(UnitSystem.IMPERIAL, UnitSystem.parse("imperial"));
        assertEquals(UnitSystem.IMPERIAL, UnitSystem.parse("  Imperial "));
        assertEquals(UnitSystem.METRIC, UnitSystem.parse("Metric"));
    }

    @Test
    @DisplayName("Missing or unrecognised text falls back to metric")
    void parse_defaultsToMetric() {
        assertEquals(UnitSystem.METRIC, UnitSystem.parse(null));
        assertEquals(UnitSystem.METRIC, UnitSystem.parse(""));
        assertEquals(UnitSystem.METRIC, UnitSystem.parse("stones"));
    }
}
