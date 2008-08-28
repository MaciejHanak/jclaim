package com.itbs.gui;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Allows to easily disable all other buttons.
 * To use, just assign it to the component:
 *    cbComponent.addActionListener(new TurnOffDependents(new JComponent[] {clientComponent}));
 * 
 */
public class TurnOffDependents implements ActionListener, ChangeListener {
    /** Keeps the list of the components to control */
    JComponent[] dependents;

    /**
     * Constructor.
     * @param components list of components in the group.
     */
    public TurnOffDependents(JComponent[] components) {
        dependents = components;
    }

    /**
     * ChangeListener call. For checkboxes.
     * @param e change event
     */
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() instanceof AbstractButton)
            for (JComponent dependent : dependents) {
                dependent.setEnabled(((AbstractButton) e.getSource()).isSelected());
            }
    }

    /**
     * ActionListener call. For buttons.
     * @param e action event
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof AbstractButton)
            for (JComponent dependent : dependents) {
                dependent.setEnabled(((AbstractButton) e.getSource()).isSelected());
            }
    }
}
