package com.kineticfitness.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * One planned training session: what, when, how long, whether to remind, and
 * whether it has been done.
 *
 * <p>Immutable &mdash; {@link #withStatus(ScheduleStatus)} and
 * {@link #rescheduledTo(LocalDate, LocalTime)} return new instances rather than
 * mutating, so a workout can never be left half-updated.</p>
 *
 * <p>Date and time are real {@link LocalDate} / {@link LocalTime} values, not
 * strings. That is what makes it possible to ask "is this due yet?" at all, and
 * it means an unparseable date is rejected at the form instead of being stored.</p>
 *
 * <p>US-10 &mdash; Schedule a workout. US-22 &mdash; Set an activity reminder.</p>
 */
public class ScheduledWorkout {

    /** Reminder lead time meaning "do not remind me about this one". */
    public static final int NO_REMINDER = 0;

    /** Id given to a workout that has not been saved yet. */
    public static final int UNSAVED = 0;

    private final int id;
    private final String name;
    private final LocalDate date;
    private final LocalTime startTime;
    private final int durationMinutes;
    private final ScheduleStatus status;
    private final int reminderLeadMinutes;

    /** A brand-new workout the user has just filled in, not yet persisted. */
    public ScheduledWorkout(String name, LocalDate date, LocalTime startTime,
                            int durationMinutes, int reminderLeadMinutes) {
        this(UNSAVED, name, date, startTime, durationMinutes, ScheduleStatus.SCHEDULED, reminderLeadMinutes);
    }

    /**
     * Full constructor, used when loading a row back out of the database.
     *
     * @throws IllegalArgumentException if the name is blank, the date or time is
     *         missing, the duration is not positive, or the reminder lead is negative
     */
    public ScheduledWorkout(int id, String name, LocalDate date, LocalTime startTime,
                            int durationMinutes, ScheduleStatus status, int reminderLeadMinutes) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("a scheduled workout needs a name");
        }
        if (date == null) {
            throw new IllegalArgumentException("a scheduled workout needs a date");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("a scheduled workout needs a start time");
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("duration must be at least one minute");
        }
        if (reminderLeadMinutes < 0) {
            throw new IllegalArgumentException("reminder lead time cannot be negative");
        }
        this.id = id;
        this.name = name.trim();
        this.date = date;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.status = status == null ? ScheduleStatus.SCHEDULED : status;
        this.reminderLeadMinutes = reminderLeadMinutes;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    /** Minutes before the start time to remind, or {@link #NO_REMINDER}. */
    public int getReminderLeadMinutes() {
        return reminderLeadMinutes;
    }

    /** The moment this session begins. */
    public LocalDateTime startsAt() {
        return LocalDateTime.of(date, startTime);
    }

    /** The moment this session is over. */
    public LocalDateTime endsAt() {
        return startsAt().plusMinutes(durationMinutes);
    }

    /** True when the user asked to be reminded about this workout. */
    public boolean hasReminder() {
        return reminderLeadMinutes > NO_REMINDER;
    }

    /**
     * The moment the reminder should first appear.
     *
     * @throws IllegalStateException if no reminder was set &mdash; check
     *         {@link #hasReminder()} first
     */
    public LocalDateTime reminderAt() {
        if (!hasReminder()) {
            throw new IllegalStateException("no reminder was set for " + name);
        }
        return startsAt().minusMinutes(reminderLeadMinutes);
    }

    /** True when this workout has not been completed or skipped yet. */
    public boolean isOpen() {
        return status.isOpen();
    }

    /**
     * A copy of this workout in a new status.
     *
     * @throws IllegalArgumentException if the transition is not allowed
     *         (see {@link ScheduleStatus#canTransitionTo(ScheduleStatus)})
     */
    public ScheduledWorkout withStatus(ScheduleStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new IllegalArgumentException("cannot move a " + status + " workout to " + target);
        }
        return new ScheduledWorkout(id, name, date, startTime, durationMinutes, target, reminderLeadMinutes);
    }

    /**
     * A copy moved to a new date and time, returned to {@code SCHEDULED} because
     * rescheduling something means you intend to do it.
     */
    public ScheduledWorkout rescheduledTo(LocalDate newDate, LocalTime newStartTime) {
        return new ScheduledWorkout(id, name, newDate, newStartTime, durationMinutes,
                ScheduleStatus.SCHEDULED, reminderLeadMinutes);
    }

    /** A copy with a different reminder lead time. */
    public ScheduledWorkout withReminderLead(int newReminderLeadMinutes) {
        return new ScheduledWorkout(id, name, date, startTime, durationMinutes, status, newReminderLeadMinutes);
    }

    /** A copy carrying the id the database assigned on insert. */
    public ScheduledWorkout withId(int newId) {
        return new ScheduledWorkout(newId, name, date, startTime, durationMinutes, status, reminderLeadMinutes);
    }

    /** The duration written for people, e.g. "45 mins" or "1 hr 30 mins". */
    public String durationLabel() {
        int hours = durationMinutes / 60;
        int minutes = durationMinutes % 60;
        if (hours == 0) {
            return minutes + " mins";
        }
        String hourPart = hours + (hours == 1 ? " hr" : " hrs");
        return minutes == 0 ? hourPart : hourPart + " " + minutes + " mins";
    }

    @Override
    public String toString() {
        return name + " on " + date + " at " + startTime + " (" + status + ")";
    }
}
