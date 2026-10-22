package com.kineticfitness.view;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Native control regression; needs a desktop, unlike the pure formatting tests. */
class DateInputRegressionTest {
    @Test
    void cancellingTheCalendarDoesNotAcceptAnInvalidDateDraft() throws Exception {
        boolean desktop = System.getProperty("os.name").startsWith("Windows")
                || System.getProperty("os.name").startsWith("Mac")
                || System.getenv("DISPLAY") != null;
        assumeTrue(desktop, "JavaFX control test requires a desktop display");
        try { Platform.startup(() -> { }); }
        catch (IllegalStateException alreadyStarted) { /* Another control test started JavaFX. */ }

        CountDownLatch finished = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.runLater(() -> {
            Stage stage = new Stage();
            stage.setOpacity(0);
            DatePicker date = new DatePicker(LocalDate.of(2026, 10, 20));
            InputAssistance.date(date);
            TextField next = new TextField();
            stage.setScene(new Scene(new VBox(date, next), 400, 180));
            stage.show();
            date.getEditor().requestFocus();
            Platform.runLater(() -> {
                try {
                    assertTrue(date.getEditor().isFocused());
                    date.getEditor().selectAll();
                    date.getEditor().replaceSelection("31022026");
                    next.requestFocus();
                    assertNull(InputAssistance.dateValue(date));
                    date.show();
                    date.hide();
                    assertNull(InputAssistance.dateValue(date),
                            "Cancelling the calendar must not silently restore a valid old date");
                    // Selecting the same date that preceded the invalid edit
                    // must still be accepted as a deliberate new selection.
                    date.setValue(LocalDate.of(2026, 10, 20));
                    assertEquals(LocalDate.of(2026, 10, 20), InputAssistance.dateValue(date));
                } catch (Throwable e) {
                    failure.set(e);
                } finally {
                    stage.close();
                    finished.countDown();
                }
            });
        });
        assertTrue(finished.await(15, TimeUnit.SECONDS), "JavaFX test timed out");
        if (failure.get() != null) throw new AssertionError("Date input regression", failure.get());
    }
}
