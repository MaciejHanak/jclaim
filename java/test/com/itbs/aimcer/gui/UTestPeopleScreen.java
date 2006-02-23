package com.itbs.aimcer.gui;

import com.itbs.gui.ActionAdapter;
import com.itbs.gui.BetterTextField;
import com.itbs.gui.GUIUtils;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Created by  Administrator on Sep 26, 2004
 */
public class UTestPeopleScreen extends UTestFrameTest {

    String name;
    public void testDialog() {
        final JTextComponent contactName;
        final JComboBox groupBox;
        final JDialog dialog = new JDialog(window, "Add contact?", true);
        Container pane = dialog.getContentPane();
        pane.setLayout(new GridLayout(0, 2));
        pane.add(PropertiesDialog.getLabel("Group: ", "Which group to add the contact to"));
        pane.add(groupBox = new JComboBox());
        pane.add(PropertiesDialog.getLabel("Name: ", "Which contact to add"));
        pane.add(contactName = new BetterTextField());
        ActionListener react = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if ("OK".equals(e.getActionCommand())) {
                    name = contactName.getText();
                }
                dialog.dispose();
            }
        };
        pane.add(new JButton(new ActionAdapter("OK", react, 'O')));
        pane.add(new JButton(new ActionAdapter("Cancel", react, 'C')));
        dialog.pack();
        GUIUtils.addCancelByEscape(dialog);
        GUIUtils.moveToScreenCenter(dialog);
        dialog.setVisible(true);
        // test values
        System.out.println("Result: " + groupBox.getSelectedItem() + "/" + name);
    }

}
