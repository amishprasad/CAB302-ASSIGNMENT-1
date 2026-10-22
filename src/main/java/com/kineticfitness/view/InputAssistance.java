package com.kineticfitness.view;

import com.kineticfitness.service.EntryFormats;
import com.kineticfitness.service.InputSuggestions;
import javafx.geometry.Bounds;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.stage.Popup;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Shared, local input assistance. No network requests or generated data. */
public final class InputAssistance {
    private static final String SUGGESTIONS = "kinetic.suggestions";
    private static final String DATE_INSTALLED = "kinetic.date-mask";
    private static final String DATE_DRAFT = "kinetic.date-draft";
    private InputAssistance() { }

    public static void suggest(TextField field, Supplier<List<InputSuggestions.Suggestion>> options) {
        suggest(field, options, value -> { });
    }

    public static void suggest(TextField field, Supplier<List<InputSuggestions.Suggestion>> options,
                               Consumer<String> selected) {
        if (field.getProperties().get(SUGGESTIONS) instanceof Suggestions existing) {
            existing.options = options;
            existing.selected = selected;
        } else {
            Suggestions suggestions = new Suggestions(field, options, selected);
            field.getProperties().put(SUGGESTIONS, suggestions);
        }
    }

    public static void date(DatePicker picker) {
        if (picker.getProperties().putIfAbsent(DATE_INSTALLED, true) != null) return;
        picker.setPromptText("DD/MM/YYYY");
        picker.setConverter(new StringConverter<>() {
            @Override public String toString(LocalDate value) {
                return value == null ? "" : EntryFormats.DATE.format(value);
            }
            @Override public LocalDate fromString(String text) {
                if (text == null || text.isBlank()) return null;
                // DatePicker commits automatically on blur. Preserve its
                // current value for an invalid draft instead of throwing on
                // the UI thread; dateValue still rejects the user's draft.
                return EntryFormats.parseDate(text).orElse(picker.getValue());
            }
        });
        mask(picker.getEditor(), true);
        // Keep the last user draft even if DatePicker restores an old value on
        // blur. Save handlers must validate the typed date, never a stale value.
        picker.getEditor().textProperty().addListener((o, before, after) -> {
            if (picker.getEditor().isFocused()) picker.getProperties().put(DATE_DRAFT, after);
        });
        picker.valueProperty().addListener((o, before, after) -> picker.getProperties().remove(DATE_DRAFT));
        picker.setOnShowing(e -> picker.getProperties().remove(DATE_DRAFT));
        picker.getEditor().setText(picker.getConverter().toString(picker.getValue()));
        picker.getEditor().setAccessibleHelp("Type day, month and year. Slashes are inserted automatically, or use the calendar.");
    }

    /** Returns null for an incomplete or impossible draft, even after blur. */
    public static LocalDate dateValue(DatePicker picker) {
        Object draft = picker.getProperties().get(DATE_DRAFT);
        return EntryFormats.parseDate(draft instanceof String text ? text : picker.getEditor().getText()).orElse(null);
    }

    public static void time(TextField field) {
        if (field.getProperties().putIfAbsent("kinetic.time-mask", true) == null) mask(field, false);
        field.setAccessibleHelp("Type 1830 for 18:30, or enter a time such as 6:30 PM.");
    }

    private static void mask(TextField field, boolean date) {
        field.setTextFormatter(new TextFormatter<String>(change -> {
            if (!change.isContentChange()) return change;
            EntryFormats.Edit edit = EntryFormats.edit(change.getControlText(), change.getRangeStart(),
                    change.getRangeEnd(), change.getText(), date, change.getControlCaretPosition());
            change.setRange(0, change.getControlText().length());
            change.setText(edit.text());
            change.selectRange(edit.caret(), edit.caret());
            return change;
        }));
    }

    private static final class Suggestions {
        private final Popup popup = new Popup();
        private final ListView<String> list = new ListView<>();
        private Supplier<List<InputSuggestions.Suggestion>> options;
        private Consumer<String> selected;
        private boolean accepting;

        Suggestions(TextField field, Supplier<List<InputSuggestions.Suggestion>> options, Consumer<String> selected) {
            this.options = options;
            this.selected = selected;
            popup.setAutoHide(true);
            popup.setHideOnEscape(true);
            list.setStyle("-fx-background-color: white; -fx-border-color: #CBD5E1; -fx-font-size: 13px;");
            list.setAccessibleText("Entry suggestions");
            list.setFixedCellSize(30);
            popup.getContent().add(list);
            field.textProperty().addListener((o, before, after) -> {
                if (!accepting && field.isFocused()) refresh(field);
            });
            field.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.UP) {
                    if (!popup.isShowing()) refresh(field);
                    if (popup.isShowing()) {
                        int index = list.getSelectionModel().getSelectedIndex();
                        list.getSelectionModel().select(Math.max(0, Math.min(list.getItems().size() - 1,
                                index + (event.getCode() == KeyCode.DOWN ? 1 : -1))));
                        event.consume();
                    }
                } else if (event.getCode() == KeyCode.ENTER && popup.isShowing()) {
                    accept(field);
                    event.consume();
                } else if (event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.TAB) {
                    popup.hide();
                }
            });
            list.setOnMouseClicked(e -> accept(field));
            list.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ENTER) accept(field);
            });
            field.sceneProperty().addListener((o, before, after) -> { if (after == null) popup.hide(); });
            field.setAccessibleHelp("Type to see suggestions. Use Up or Down and Enter to choose, or Escape to keep your own text.");
        }

        private void refresh(TextField field) {
            List<String> matches = InputSuggestions.match(field.getText(), options.get());
            if (matches.isEmpty() || matches.size() == 1 && matches.get(0).equalsIgnoreCase(field.getText())) {
                popup.hide(); return;
            }
            list.getItems().setAll(matches);
            list.getSelectionModel().clearSelection();
            list.setPrefSize(Math.max(260, field.getWidth()), matches.size() * 30 + 4);
            Bounds bounds = field.localToScreen(field.getBoundsInLocal());
            if (bounds != null && !popup.isShowing()) popup.show(field, bounds.getMinX(), bounds.getMaxY());
        }

        private void accept(TextField field) {
            String value = list.getSelectionModel().getSelectedItem();
            if (value == null && !list.getItems().isEmpty()) value = list.getItems().get(0);
            if (value == null) return;
            accepting = true;
            try {
                popup.hide(); field.setText(value); field.positionCaret(value.length());
                selected.accept(value); field.requestFocus();
            } finally { accepting = false; }
        }
    }
}
