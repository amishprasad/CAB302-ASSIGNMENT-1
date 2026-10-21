package com.kineticfitness.service;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class EntryFormatsTest {
    @Test void pastingOverASelectionKeepsTheCaretAtTheEnd() {
        assertEquals(new EntryFormats.Edit("21/10/2026", 10),
                EntryFormats.edit("20/10/2026", 0, 10, "21102026", true));
        assertEquals(new EntryFormats.Edit("18:30", 5),
                EntryFormats.edit("", 0, 0, "1830", false));
    }
    @Test void forwardDeleteOverASeparatorRemovesTheNextDigit() {
        assertEquals(new EntryFormats.Edit("20/02/026", 3),
                EntryFormats.edit("20/10/2026", 2, 3, "", true, 2));
    }
    @Test void insertsDateSeparatorsWhileTypingAndPasting() {
        assertEquals("20/", EntryFormats.dateDraft("20"));
        assertEquals("20/10/", EntryFormats.dateDraft("2010"));
        assertEquals("20/10/2026", EntryFormats.dateDraft("20102026"));
        assertEquals("20/10/2026", EntryFormats.dateDraft("20/10/2026"));
    }
    @Test void formatsTimeWithoutChangingTwelveHourInput() {
        assertEquals("18:", EntryFormats.timeDraft("18"));
        assertEquals("18:30", EntryFormats.timeDraft("1830"));
        assertEquals("6:30 PM", EntryFormats.timeDraft("6:30 PM"));
        assertEquals("6pm", EntryFormats.timeDraft("6pm"));
    }
    @Test void parsesDatesStrictlyInsteadOfCorrectingInvalidDays() {
        assertEquals(LocalDate.of(2026, 10, 20), EntryFormats.parseDate("20/10/2026").orElseThrow());
        assertEquals(LocalDate.of(2026, 10, 20), EntryFormats.parseDate("20102026").orElseThrow());
        assertTrue(EntryFormats.parseDate("31/02/2026").isEmpty());
        assertTrue(EntryFormats.parseDate("20/10/").isEmpty());
        assertTrue(EntryFormats.parseDate("").isEmpty());
        assertEquals(LocalDate.of(2024, 2, 29), EntryFormats.parseDate("29/02/2024").orElseThrow());
    }
    @Test void rejectsOversizedOrNonDateInputWithoutTruncatingIt() {
        assertEquals("201020267", EntryFormats.dateDraft("201020267"));
        assertTrue(EntryFormats.parseDate("201020267").isEmpty());
        assertTrue(EntryFormats.parseDate("banana").isEmpty());
    }
    @Test void deletingASeparatorAlsoRemovesTheAdjacentDigit() {
        assertEquals(new EntryFormats.Edit("2", 1),
                EntryFormats.edit("20/", 2, 3, "", true));
        assertEquals(new EntryFormats.Edit("20/02/026", 3),
                EntryFormats.edit("20/10/2026", 3, 4, "", true));
    }
}
