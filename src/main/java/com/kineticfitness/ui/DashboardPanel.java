package com.kineticfitness.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Placeholder homepage/dashboard screen.
 */
public class DashboardPanel extends JPanel {

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        add(title, BorderLayout.NORTH);

        JTextArea placeholder = new JTextArea(
                "Welcome back!\n\n" +
                "This is where your goal progress, recent activity, and " +
                "next suggested workout will appear once the app is fully built.\n\n" +
                "Use the sidebar to explore the other planned screens: Goals, " +
                "Workout Log, Progress, and Profile."
        );
        placeholder.setFont(new Font("SansSerif", Font.PLAIN, 14));
        placeholder.setEditable(false);
        placeholder.setOpaque(false);
        placeholder.setLineWrap(true);
        placeholder.setWrapStyleWord(true);
        placeholder.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        add(placeholder, BorderLayout.CENTER);
    }
}
