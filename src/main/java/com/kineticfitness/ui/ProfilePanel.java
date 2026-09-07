package com.kineticfitness.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Placeholder user profile screen.
 */
public class ProfilePanel extends JPanel {

    public ProfilePanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Profile");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        form.add(new JTextField(15), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Fitness level:"), gbc);
        gbc.gridx = 1;
        form.add(new JComboBox<>(new String[]{"Beginner", "Intermediate", "Advanced"}), gbc);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrapper.add(form);
        add(wrapper, BorderLayout.CENTER);
    }
}
