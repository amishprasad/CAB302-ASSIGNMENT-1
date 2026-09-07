package com.kineticfitness.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Left-hand sidebar with a button per screen.
 */
public class NavigationPanel extends JPanel {

    private static final Color SIDEBAR_BG = new Color(33, 37, 41);
    private static final Color BUTTON_FG = Color.WHITE;

    public NavigationPanel(MainFrame mainFrame) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(SIDEBAR_BG);
        setPreferredSize(new Dimension(180, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JLabel appName = new JLabel("Kinetic Fitness");
        appName.setForeground(Color.WHITE);
        appName.setFont(new Font("SansSerif", Font.BOLD, 18));
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(appName);
        add(Box.createRigidArea(new Dimension(0, 30)));

        addNavButton("Dashboard", MainFrame.DASHBOARD, mainFrame);
        addNavButton("Goals", MainFrame.GOALS, mainFrame);
        addNavButton("Workout Log", MainFrame.WORKOUTS, mainFrame);
        addNavButton("Progress", MainFrame.PROGRESS, mainFrame);
        addNavButton("Profile", MainFrame.PROFILE, mainFrame);

        add(Box.createVerticalGlue());
    }

    private void addNavButton(String label, String screenName, MainFrame mainFrame) {
        JButton button = new JButton(label);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(160, 36));
        button.setFocusPainted(false);
        button.setForeground(BUTTON_FG);
        button.setBackground(SIDEBAR_BG.brighter());
        button.addActionListener(e -> mainFrame.showScreen(screenName));

        add(button);
        add(Box.createRigidArea(new Dimension(0, 10)));
    }
}
