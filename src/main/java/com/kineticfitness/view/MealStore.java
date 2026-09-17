package com.kineticfitness.view;

import com.kineticfitness.model.DailyLog;

import java.time.LocalDate;

/**
 * One shared meal log for the running app, so the Dashboard can read today's totals
 * without the Meal Log page having to hand them over. Same singleton convention as
 * {@link LocalProfileStore}.
 *
 * <p>Meals are held in memory only — they are lost when the app closes. Persisting
 * them to the database is a separate job (a MealDAO), left for later.
 */
public final class MealStore {

    private static final MealStore INSTANCE = new MealStore();

    public static MealStore getInstance() {
        return INSTANCE;
    }

    private DailyLog today = new DailyLog(LocalDate.now());

    private MealStore() {
    }

    /**
     * Today's log. If the app has been left running past midnight the old log is
     * dropped and a fresh one started, so "today" always means the real today.
     */
    public DailyLog today() {
        if (!today.getDate().equals(LocalDate.now())) {
            today = new DailyLog(LocalDate.now());
        }
        return today;
    }
}