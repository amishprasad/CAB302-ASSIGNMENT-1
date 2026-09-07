package com.kineticfitness.ui;

import javax.swing.*;
import java.awt.*;

/**
 * The main application window.
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    // Names used to identify each "card" (screen) in the CardLayout
    public static final String DASHBOARD = "Dashboard";
    public static final String GOALS = "Goals";
    public static final String WORKOUTS = "Workout Log";
    public static final String PROGRESS = "Progress";
    public static final String PROFILE = "Profile";

    public MainFrame() {
        setTitle("Kinetic Fitness (Prototype)");
        setSize(900, 600);
        setMinimumSize(new Dimension(700, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        add(new NavigationPanel(this), BorderLayout.WEST);

        contentPanel.add(new DashboardPanel(), DASHBOARD);
        contentPanel.add(new GoalsPanel(), GOALS);
        contentPanel.add(new WorkoutLogPanel(), WORKOUTS);
        contentPanel.add(new ProgressPanel(), PROGRESS);
        contentPanel.add(new ProfilePanel(), PROFILE);

        add(contentPanel, BorderLayout.CENTER);

        showScreen(DASHBOARD);
    }

    /**
     * Switches the visible screen in the content area.
     * Called by the sidebar navigation buttons.
     */
    public void showScreen(String screenName) {
        cardLayout.show(contentPanel, screenName);
    }
}
