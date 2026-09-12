package com.kineticfitness.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * A stand-in page for screens that haven't been built yet. Lets the shell show the full
 * navigation immediately; each owner swaps their real {@link Page} in when ready.
 */
public class PlaceholderPage implements Page {

    private final String label;
    private final String owner;

    public PlaceholderPage(String label, String owner) {
        this.label = label;
        this.owner = owner;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public Node getContent() {
        Label title = new Label(label);
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        String message = (owner == null || owner.isEmpty())
                ? "Not claimed yet — implement Page and plug this screen into the shell here."
                : "Owned by " + owner + " — their page plugs into the shell here.";
        Label note = new Label(message);
        note.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B;");

        VBox box = new VBox(12, title, note);
        box.setPadding(new Insets(32, 40, 32, 40));
        box.setAlignment(Pos.TOP_LEFT);
        box.setStyle("-fx-background-color: #F1F5F9;");
        return box;
    }
}
