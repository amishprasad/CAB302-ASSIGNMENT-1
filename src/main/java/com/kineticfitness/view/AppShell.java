package com.kineticfitness.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The application frame every screen plugs into. It owns the window and the sidebar, and
 * swaps only the centre content when a nav item is clicked, so pages never redraw the
 * sidebar themselves. Register pages with {@link #add(Page)} then call {@link #show()}.
 *
 * <p>How a teammate adds their screen: implement {@link Page} and add it in Main —
 * {@code new AppShell(stage).add(new MyView()) ... .show();}
 */
public class AppShell {

    private static final String NAVY = "#0F172A";
    private static final String NAV_TEXT = "#CBD5E1";
    private static final String ORANGE = "#F97316";

    private final Stage stage;
    private final BorderPane root = new BorderPane();
    private final List<Page> pages = new ArrayList<>();
    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private final Map<String, Node> contentCache = new LinkedHashMap<>();

    public AppShell(Stage stage) {
        this.stage = stage;
    }

    /** Register a page. Order controls sidebar order. Returns this for chaining. */
    public AppShell add(Page page) {
        pages.add(page);
        return this;
    }

    public void show() {
        root.setLeft(buildSidebar());
        if (!pages.isEmpty()) {
            navigate(pages.get(0).label());
        }
        stage.setTitle("Kinetic Fitness");
        stage.setScene(new Scene(root, 1280, 800));
        stage.show();
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(6);
        sidebar.setPrefWidth(240);
        sidebar.setMinWidth(240);
        sidebar.setPadding(new Insets(20, 16, 20, 16));
        sidebar.setStyle("-fx-background-color: " + NAVY + ";");

        Region mark = new Region();
        mark.setMinSize(26, 26);
        mark.setPrefSize(26, 26);
        mark.setMaxSize(26, 26);
        mark.setStyle("-fx-background-color: " + ORANGE + "; -fx-background-radius: 7;");

        Label brand = new Label("Kinetic Fitness");
        brand.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        HBox logo = new HBox(10, mark, brand);
        logo.setAlignment(Pos.CENTER_LEFT);
        logo.setPadding(new Insets(4, 0, 20, 4));

        VBox nav = new VBox(4);
        for (Page page : pages) {
            Button button = new Button(page.label());
            button.setMaxWidth(Double.MAX_VALUE);
            button.setAlignment(Pos.CENTER_LEFT);
            button.setPadding(new Insets(10, 14, 10, 14));
            button.setOnAction(e -> navigate(page.label()));
            navButtons.put(page.label(), button);
            nav.getChildren().add(button);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logout = new Button("Logout");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setAlignment(Pos.CENTER_LEFT);
        logout.setPadding(new Insets(10, 14, 10, 14));
        styleNav(logout, false);

        sidebar.getChildren().addAll(logo, nav, spacer, logout);
        return sidebar;
    }

    private void navigate(String label) {
        Node content = contentCache.get(label);
        if (content == null) {
            for (Page page : pages) {
                if (page.label().equals(label)) {
                    content = page.getContent();
                    break;
                }
            }
            contentCache.put(label, content);
        }
        root.setCenter(content);
        for (Map.Entry<String, Button> entry : navButtons.entrySet()) {
            styleNav(entry.getValue(), entry.getKey().equals(label));
        }
    }

    private void styleNav(Button button, boolean active) {
        if (active) {
            button.setStyle("-fx-background-color: " + ORANGE + "; -fx-text-fill: white;"
                    + " -fx-font-weight: bold; -fx-background-radius: 8; -fx-font-size: 13px;");
        } else {
            button.setStyle("-fx-background-color: transparent; -fx-text-fill: " + NAV_TEXT + ";"
                    + " -fx-background-radius: 8; -fx-font-size: 13px;");
        }
    }
}
