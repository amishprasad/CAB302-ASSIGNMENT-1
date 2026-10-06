package com.kineticfitness.service;

import com.kineticfitness.model.BodyPart;
import com.kineticfitness.model.Exercise;
import com.kineticfitness.model.Workout;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExportServiceTest {

    private static final String NL = "\r\n";
    private static final String HEADER = "Date,Exercise,Body Part,Sets,Reps,Total Reps" + NL;
    private static final LocalDate OCT_5 = LocalDate.of(2026, 10, 5);
    private static final LocalDate OCT_7 = LocalDate.of(2026, 10, 7);

    private static Workout workout(LocalDate date, Exercise... exercises) {
        Workout workout = new Workout(date);
        for (Exercise exercise : exercises) {
            workout.addExercise(exercise);
        }
        return workout;
    }

    // ---- workoutsToCsv ----

    @Test
    void noWorkoutsGivesJustTheHeader() {
        assertEquals(HEADER, ExportService.workoutsToCsv(List.of()));
    }

    @Test
    void oneExerciseBecomesOneRowWithTotalReps() {
        BodyPart part = BodyPart.values()[0];
        String csv = ExportService.workoutsToCsv(
                List.of(workout(OCT_5, new Exercise("Bench Press", 3, 10, part))));

        assertEquals(HEADER + "2026-10-05,Bench Press," + part.name() + ",3,10,30" + NL, csv);
    }

    @Test
    void missingBodyPartLeavesThatFieldEmpty() {
        String csv = ExportService.workoutsToCsv(
                List.of(workout(OCT_5, new Exercise("Squat", 4, 5))));

        assertEquals(HEADER + "2026-10-05,Squat,,4,5,20" + NL, csv);
    }

    @Test
    void everyExerciseInAWorkoutGetsItsOwnRowWithTheSameDate() {
        String csv = ExportService.workoutsToCsv(List.of(workout(OCT_5,
                new Exercise("Squat", 4, 5),
                new Exercise("Lunge", 3, 12))));

        assertEquals(HEADER
                + "2026-10-05,Squat,,4,5,20" + NL
                + "2026-10-05,Lunge,,3,12,36" + NL, csv);
    }

    @Test
    void workoutsAreListedOldestFirst() {
        String csv = ExportService.workoutsToCsv(List.of(
                workout(OCT_7, new Exercise("Squat", 1, 1)),
                workout(OCT_5, new Exercise("Lunge", 1, 1))));

        assertEquals(HEADER
                + "2026-10-05,Lunge,,1,1,1" + NL
                + "2026-10-07,Squat,,1,1,1" + NL, csv);
    }

    @Test
    void aWorkoutWithNoExercisesIsStillListed() {
        String csv = ExportService.workoutsToCsv(List.of(workout(OCT_5)));

        assertEquals(HEADER + "2026-10-05,,,,," + NL, csv);
    }

    @Test
    void aNameContainingACommaIsQuoted() {
        String csv = ExportService.workoutsToCsv(
                List.of(workout(OCT_5, new Exercise("Push-ups, wide", 3, 10))));

        assertEquals(HEADER + "2026-10-05,\"Push-ups, wide\",,3,10,30" + NL, csv);
    }

    @Test
    void aQuoteInANameIsDoubledInsideQuotes() {
        String csv = ExportService.workoutsToCsv(
                List.of(workout(OCT_5, new Exercise("8\" Curl", 3, 10))));

        assertEquals(HEADER + "2026-10-05,\"8\"\" Curl\",,3,10,30" + NL, csv);
    }

    @Test
    void aNameStartingLikeAFormulaIsDefused() {
        String csv = ExportService.workoutsToCsv(
                List.of(workout(OCT_5, new Exercise("=SUM(A1:A2)", 1, 1))));

        assertEquals(HEADER + "2026-10-05,'=SUM(A1:A2),,1,1,1" + NL, csv);
    }

    // ---- fileName ----

    @Test
    void fileNameIncludesUsernameAndDate() {
        assertEquals("kinetic-fitness-workouts-alex-2026-10-05.csv",
                ExportService.fileName("alex", OCT_5));
    }

    @Test
    void fileNameReplacesCharactersThatAreUnsafeInFileNames() {
        assertEquals("kinetic-fitness-workouts-alex_r_x-2026-10-05.csv",
                ExportService.fileName("alex.r@x", OCT_5));
    }

    @Test
    void fileNameFallsBackWhenThereIsNoUsername() {
        assertEquals("kinetic-fitness-workouts-user-2026-10-05.csv",
                ExportService.fileName("  ", OCT_5));
    }
}