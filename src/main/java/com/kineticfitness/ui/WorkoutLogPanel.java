package com.kineticfitness.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Placeholder workout logging screen.
 */
public class WorkoutLogPanel extends JPanel {

    public WorkoutLogPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Workout Log");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        String[] labels = {"Activity type:", "Sets:", "Reps:", "Steps:", "Duration (min):"};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            form.add(new JLabel(labels[i]), gbc);

            gbc.gridx = 1;
            if (i == 0) {
                form.add(new JTextField(15), gbc);
            } else {
                form.add(new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1)), gbc);
            }
        }

        gbc.gridx = 0;
        gbc.gridy = labels.length;
        gbc.gridwidth = 2;
        JButton logButton = new JButton("Log Workout (not yet functional)");
        logButton.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                        "This is a UI placeholder",
                        "Prototype", JOptionPane.INFORMATION_MESSAGE));
        form.add(logButton, gbc);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrapper.add(form);
        add(wrapper, BorderLayout.CENTER);
    }
}
