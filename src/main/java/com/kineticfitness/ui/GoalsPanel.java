package com.kineticfitness.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Placeholder goal-setting screen.
 */
public class GoalsPanel extends JPanel {

    public GoalsPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Goals");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Fitness level:"), gbc);
        gbc.gridx = 1;
        form.add(new JComboBox<>(new String[]{"Beginner", "Intermediate", "Advanced"}), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Goal timeframe:"), gbc);
        gbc.gridx = 1;
        form.add(new JComboBox<>(new String[]{"Daily", "Weekly", "Monthly", "Annual"}), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        form.add(new JLabel("Target sessions:"), gbc);
        gbc.gridx = 1;
        form.add(new JSpinner(new SpinnerNumberModel(3, 1, 30, 1)), gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JButton saveButton = new JButton("Save Goal (not yet functional)");
        saveButton.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                        "This is supposed to do something",
                        "Prototype", JOptionPane.INFORMATION_MESSAGE));
        form.add(saveButton, gbc);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrapper.add(form);
        add(wrapper, BorderLayout.CENTER);
    }
}
