package com.kineticfitness;

import com.kineticfitness.ui.MainFrame;

import javax.swing.SwingUtilities;

/**
 * Kinetic Fitness - Very Early GUI Prototype
 *
 * This prototype has NO real backend logic or data persistence.
 * Its only purpose is to demonstrate basic window navigation between
 * the app's planned screens, using a sidebar menu and a card-based
 * content area.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
