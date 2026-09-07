package com.kineticfitness.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Placeholder progress screen.
 * Shows a static example progress bar just to demonstrate layout —
 * no real data is calculated yet.
 */
public class ProgressPanel extends JPanel {

    public ProgressPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Progress");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        add(title, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JLabel note = new JLabel("Example weekly goal progress (placeholder data):");
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(note);
        body.add(Box.createRigidArea(new Dimension(0, 10)));

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(40);
        progressBar.setStringPainted(true);
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBar.setMaximumSize(new Dimension(300, 25));
        body.add(progressBar);

        add(body, BorderLayout.CENTER);
    }
}
