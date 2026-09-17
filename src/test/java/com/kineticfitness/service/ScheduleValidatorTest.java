package com.kineticfitness.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The rules and parsing behind the "Schedule New Workout" form.
 *
 * <p>{@code today} is passed in rather than read from the clock, so the
 * "not in the past" rule is tested at a fixed date and cannot start failing
 * tomorrow.</p>
 *
 * <p>US-10 &mdash; Schedule a workout.</p>
 */
class ScheduleValidatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 16);
    private static final LocalDate TOMORROW = TODAY.plusDays(1);

    @Nested
    @DisplayName("Form rules")
    class FormRules {

        @Test
        void acceptsACompleteForm() {
            ValidationResult result =
                    ScheduleValidator.validate("Leg Day", TOMORROW, "18:00", "45", TODAY);
            assertTrue(result.valid());
            assertEquals("", result.message());
        }

        @Test
        @DisplayName("today itself is allowed - you can plan this evening's session")
        void todayIsNotInThePast() {
            assertTrue(ScheduleValidator.validate("Leg Day", TODAY, "18:00", "45", TODAY).valid());
        }

        @Test
        void rejectsAMissingName() {
            assertEquals("Please enter a workout name.",
                    ScheduleValidator.validate("   ", TOMORROW, "18:00", "45", TODAY).message());
        }

        @Test
        void rejectsAMissingDate() {
            assertEquals("Please choose a date.",
                    ScheduleValidator.validate("Leg Day", null, "18:00", "45", TODAY).message());
        }

        @Test
        void rejectsADateInThePast() {
            assertEquals("You can't schedule a workout in the past.",
                    ScheduleValidator.validate("Leg Day", TODAY.minusDays(1), "18:00", "45", TODAY).message());
        }

        @Test
        @DisplayName("the old form accepted 'banana' as a date - this is what stops that")
        void rejectsATimeItCannotUnderstand() {
            assertEquals("Enter a start time like 18:00 or 6:30 PM.",
                    ScheduleValidator.validate("Leg Day", TOMORROW, "banana", "45", TODAY).message());
        }

        @Test
        void rejectsAMissingTime() {
            assertEquals("Please enter a start time.",
                    ScheduleValidator.validate("Leg Day", TOMORROW, "", "45", TODAY).message());
        }

        @Test
        void rejectsANonNumericDuration() {
            assertEquals("Duration must be a whole number of minutes.",
                    ScheduleValidator.validate("Leg Day", TOMORROW, "18:00", "a while", TODAY).message());
        }

        @Test
        void rejectsAZeroOrNegativeDuration() {
            assertEquals("Duration must be at least 1 minute.",
                    ScheduleValidator.validate("Leg Day", TOMORROW, "18:00", "0", TODAY).message());
            assertEquals("Duration must be at least 1 minute.",
                    ScheduleValidator.validate("Leg Day", TOMORROW, "18:00", "-30", TODAY).message());
        }

        @Test
        void rejectsAnImplausiblyLongSession() {
            assertEquals("Duration must be 600 minutes or less.",
                    ScheduleValidator.validate("Leg Day", TOMORROW, "18:00", "601", TODAY).message());
            assertTrue(ScheduleValidator.validate("Leg Day", TOMORROW, "18:00", "600", TODAY).valid());
        }

        @Test
        @DisplayName("the user is told about the first problem, not the last")
        void reportsProblemsInFieldOrder() {
            ValidationResult result =
                    ScheduleValidator.validate("", null, "banana", "nonsense", TODAY);
            assertEquals("Please enter a workout name.", result.message());
        }

        @Test
        void requiresACurrentDateToCompareAgainst() {
            assertThrows(IllegalArgumentException.class,
                    () -> ScheduleValidator.validate("Leg Day", TOMORROW, "18:00", "45", null));
        }
    }

    @Nested
    @DisplayName("Time parsing")
    class TimeParsing {

        @Test
        void reads24HourTimes() {
            assertEquals(LocalTime.of(18, 0), ScheduleValidator.parseTime("18:00").orElseThrow());
            assertEquals(LocalTime.of(6, 30), ScheduleValidator.parseTime("6:30").orElseThrow());
            assertEquals(LocalTime.of(6, 30), ScheduleValidator.parseTime("06:30").orElseThrow());
            assertEquals(LocalTime.of(23, 59), ScheduleValidator.parseTime("23:59").orElseThrow());
        }

        @Test
        void reads12HourTimes() {
            assertEquals(LocalTime.of(18, 30), ScheduleValidator.parseTime("6:30 PM").orElseThrow());
            assertEquals(LocalTime.of(6, 30), ScheduleValidator.parseTime("6:30 AM").orElseThrow());
            assertEquals(LocalTime.of(19, 0), ScheduleValidator.parseTime("7 PM").orElseThrow());
        }

        @Test
        @DisplayName("people leave out the space and use lower case")
        void toleratesSpacingAndCase() {
            assertEquals(LocalTime.of(18, 30), ScheduleValidator.parseTime("6:30pm").orElseThrow());
            assertEquals(LocalTime.of(18, 30), ScheduleValidator.parseTime("  6:30   PM  ").orElseThrow());
            assertEquals(LocalTime.of(6, 0), ScheduleValidator.parseTime("6am").orElseThrow());
        }

        @Test
        void rejectsNonsense() {
            assertTrue(ScheduleValidator.parseTime("banana").isEmpty());
            assertTrue(ScheduleValidator.parseTime("25:00").isEmpty());
            assertTrue(ScheduleValidator.parseTime("18:99").isEmpty());
            assertTrue(ScheduleValidator.parseTime("").isEmpty());
            assertTrue(ScheduleValidator.parseTime(null).isEmpty());
        }
    }

    @Nested
    @DisplayName("Duration parsing")
    class DurationParsing {

        @Test
        void readsAPlainNumber() {
            assertEquals(45, ScheduleValidator.parseDurationMinutes("45").getAsInt());
        }

        @Test
        @DisplayName("the placeholder says '75' but people type '75 mins'")
        void toleratesATrailingUnit() {
            assertEquals(75, ScheduleValidator.parseDurationMinutes("75 mins").getAsInt());
            assertEquals(75, ScheduleValidator.parseDurationMinutes("75 min").getAsInt());
            assertEquals(75, ScheduleValidator.parseDurationMinutes("75 minutes").getAsInt());
            assertEquals(75, ScheduleValidator.parseDurationMinutes("  75  ").getAsInt());
        }

        @Test
        void rejectsAnythingElse() {
            assertTrue(ScheduleValidator.parseDurationMinutes("45.5").isEmpty());
            assertTrue(ScheduleValidator.parseDurationMinutes("an hour").isEmpty());
            assertTrue(ScheduleValidator.parseDurationMinutes("").isEmpty());
            assertTrue(ScheduleValidator.parseDurationMinutes(null).isEmpty());
        }
    }

    @Nested
    @DisplayName("Date parsing for rows written before dates were stored properly")
    class DateParsing {

        @Test
        void readsIsoDates() {
            assertEquals(LocalDate.of(2026, 9, 20), ScheduleValidator.parseDate("2026-09-20").orElseThrow());
        }

        @Test
        void readsTheOldDisplayFormat() {
            assertEquals(LocalDate.of(2026, 9, 15), ScheduleValidator.parseDate("15 Sep 2026").orElseThrow());
        }

        @Test
        void readsTheAustralianSlashFormat() {
            assertEquals(LocalDate.of(2026, 9, 15), ScheduleValidator.parseDate("15/09/2026").orElseThrow());
        }

        @Test
        void givesUpOnAnythingElse() {
            assertTrue(ScheduleValidator.parseDate("banana").isEmpty());
            assertTrue(ScheduleValidator.parseDate(null).isEmpty());
            assertFalse(ScheduleValidator.parseDate("2026-13-45").isPresent());
        }
    }
}
