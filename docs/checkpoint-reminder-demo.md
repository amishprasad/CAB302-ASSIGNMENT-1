# Week 11: Edit reminder (US-22 / US-23)

## Demo

1. Sign in and enable workout reminders in Settings.
2. Schedule a workout today, starting about 10 minutes from now, with No reminder.
3. Click Edit reminder on its scheduled row, choose 15 minutes before, and Save.
4. The row shows the new setting, and the banner appears immediately without waiting for the 30-second timer.
5. Change it back to No reminder. The banner disappears immediately.
6. Re-enable the reminder, dismiss the banner, and edit the reminder again. The changed workout can appear again; other workouts' dismissed banners remain dismissed.
7. Cancel an edit to demonstrate that its saved value stays unchanged. Restart and sign in to show that a saved value persists.

Rehearse the interface steps before presenting. Automated checks verify the service, persistence and notification behaviour; they do not click the JavaFX dialog.

## Design evidence

- Observer: ReminderSettingsService is the subject. It registers/removes IntConsumer listeners and announces a saved change with the workout id. Main registers AppShell.reminderSettingsChanged as an observer. The service does not depend on AppShell or JavaFX.
- Constructor dependency injection: Main supplies ScheduleDAO to ReminderSettingsService and supplies both objects to ScheduleView. Tests supply a failing DAO to exercise save errors.
- Encapsulation: only the service's methods manage its private listener collection; the changed reminder is stored through ScheduleDAO.
- The shell clears dismissal only for the edited workout and reloads due reminders. The existing timer still handles reminders becoming due as time passes.

## Actual test-first sequence

On 6 October 2026, ReminderSettingsServiceTest was created before ReminderSettingsService.

Red command:

```text
mvn -B -o -Dtest=ReminderSettingsServiceTest test
```

Observed result: BUILD FAILURE at test compilation, with four `cannot find symbol: class ReminderSettingsService` errors. This was a missing-API compilation failure, not an assertion failure. No historical red commit was created.

The service, listener registration, dialog, and immediate banner refresh were then implemented. ScheduleDAO now rejects invalid or missing rows and reports SQL errors instead of silently logging them, so failed saves cannot announce success.

Green verification:

```text
mvn -B -o verify
Tests run: 225, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

The six new tests cover enabling and disabling reminders, storage updated before notifications, multiple observers and removal, invalid input, missing rows, and storage failure. The previous scheduling fixes were implemented before their regression tests; do not describe those earlier changes as test-first.

Record your own actual time, review and understanding in your individual work log. These notes document this assisted implementation; they are not a claim of independently authored work or a replacement for CI run evidence.
