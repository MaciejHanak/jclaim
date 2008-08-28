package com.itbs.gui;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
     * Allows to easily disable all other buttons.
 */
public class TurnOffDependents implements ActionListener, ChangeListener {
    JComponent[] dependents;

    /**
     * Constructor.
     * @param components list of components in the group.
     */
    public TurnOffDependents(JComponent[] components) {
        dependents = components;
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() instanceof AbstractButton)
            for (JComponent dependent : dependents) {
                dependent.setEnabled(((AbstractButton) e.getSource()).isSelected());
            }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof AbstractButton)
            for (JComponent dependent : dependents) {
                dependent.setEnabled(((AbstractButton) e.getSource()).isSelected());
            }
    }
}
