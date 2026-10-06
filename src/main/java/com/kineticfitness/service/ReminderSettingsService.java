package com.kineticfitness.service;

import com.kineticfitness.db.ScheduleDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;

/**
 * Saves reminder settings and announces successful changes (US-22).
 * This is the Observer subject: listeners receive the changed workout id,
 * without the service knowing about JavaFX, banners or screens.
 * The data source is supplied through constructor dependency injection.
 */
public final class ReminderSettingsService {
    private final ScheduleDAO repository;
    private final List<IntConsumer> listeners = new ArrayList<>();

    public ReminderSettingsService(ScheduleDAO repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public void addListener(IntConsumer listener) {
        listeners.add(Objects.requireNonNull(listener));
    }

    public void removeListener(IntConsumer listener) {
        listeners.remove(listener);
    }

    /** Called on the UI thread; zero disables the reminder. */
    public void updateReminder(int workoutId, int leadMinutes) {
        if (workoutId <= 0 || leadMinutes < 0) {
            throw new IllegalArgumentException("A saved workout and a non-negative reminder time are required.");
        }
        repository.updateReminderLead(workoutId, leadMinutes);
        for (IntConsumer listener : List.copyOf(listeners)) {
            listener.accept(workoutId);
        }
    }
}
