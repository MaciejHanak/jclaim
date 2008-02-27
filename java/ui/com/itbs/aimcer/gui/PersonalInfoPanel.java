/*
 * Copyright (c) 2006, ITBS LLC. All Rights Reserved.
 *
 *     This file is part of JClaim.
 *
 *     JClaim is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; version 2 of the License.
 *
 *     JClaim is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with JClaim; if not, find it at gnu.org or write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package com.itbs.aimcer.gui;

import com.itbs.aimcer.bean.ContactPreferences;
import com.itbs.aimcer.bean.ContactWrapper;
import com.itbs.gui.ActionAdapter;
import com.itbs.gui.BetterTextField;
import com.itbs.gui.BetterTextPane;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Hosts the additional user info.
 * <p>
 * Displayed in Message Window at the moment. 
 * <p>
 * todo make an object:
 * Fields:
 *   Forced DisplayName
 *   Picture (bin or a pointer to folder for show)
 *   Full Name
 *   Gender
 *   Phone(s)
 *   Notes (Likes, Dislikes)
 *   DOB (age)
 *   Address (addConnection with mappoint?)
 *   LastWindowBounds
 *
 * @author Alex Rass
 * @since Oct 21, 2004
 */
public class PersonalInfoPanel extends JPanel {
    ContactPreferences preferences;

    public PersonalInfoPanel(final ContactWrapper contact) {
        preferences = contact.getPreferences();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JTextComponent symbol;
        JCheckBox checkBox;
        JPanel linePanel;

        linePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        symbol = new BetterTextField(preferences.getName(), 14);
        ComponentFactory.fixWidget(symbol, "Full Name");
//      symbol.setBorder(new TitledBorder("Full Name"));
        symbol.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                preferences.setName(((JTextComponent)e.getSource()).getText());
            }
        });
        linePanel.add(symbol);

        symbol = new BetterTextField(preferences.getDisplayName(), 10);
        ComponentFactory.fixWidget(symbol, "Display Name");
//        symbol.setBorder(new TitledBorder());
        symbol.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                preferences.setDisplayName(((JTextComponent)e.getSource()).getText());
                contact.updateDisplayComponent(); //nicely enough - right on AWT thread
            }
        });
        linePanel.add(symbol);
        add(linePanel);

        linePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        symbol = new BetterTextField(preferences.getPhone(), 14);
        ComponentFactory.fixWidget(symbol, "Cell Phone");
        symbol.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                preferences.setPhone(((JTextComponent)e.getSource()).getText());
            }
        });
        linePanel.add(symbol);

        symbol = new BetterTextField(preferences.getEmailAddress(), 14);
        ComponentFactory.fixWidget(symbol, "Email");
        symbol.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                preferences.setEmailAddress(((JTextComponent)e.getSource()).getText());
            }
        });
        linePanel.add(symbol);
        add(linePanel);

        // Checkboxes:
        linePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        checkBox = new JCheckBox(new ActionAdapter("Keep Offile", "Always offline.", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                preferences.setHideFromList(!preferences.isHideFromList());
                ((JCheckBox) e.getSource()).setSelected(preferences.isHideFromList());
                contact.updateDisplayComponent(); //nicely enough - right on AWT thread
            }
        }));
        checkBox.setSelected(preferences.isHideFromList());
        linePanel.add(checkBox);
        checkBox = new JCheckBox(new ActionAdapter("Always Show", "Always show even if offline.", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                preferences.setShowInList(!preferences.isShowInList());
                ((JCheckBox) e.getSource()).setSelected(preferences.isShowInList());
                contact.updateDisplayComponent(); //nicely enough - right on AWT thread
            }
        }));
        checkBox.setSelected(preferences.isShowInList());
        linePanel.add(checkBox);
        checkBox = new JCheckBox(new ActionAdapter("Notify", "Popup message notification.", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                preferences.setNotifyOnConnect(!preferences.isNotifyOnConnect());
                ((JCheckBox) e.getSource()).setSelected(preferences.isNotifyOnConnect());
                contact.updateDisplayComponent(); //nicely enough - right on AWT thread
            }
        }));
        checkBox.setSelected(preferences.isNotifyOnConnect());
        linePanel.add(checkBox);
        checkBox = new JCheckBox(new ActionAdapter("Show Icon", "Always offline.", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                preferences.setShowIcon(!preferences.isShowIcon());
                ((JCheckBox) e.getSource()).setSelected(preferences.isShowIcon());                
            }
        }));
        checkBox.setSelected(preferences.isShowIcon());
        linePanel.add(checkBox);
        add(linePanel);
        // ^^^ Checkboxes ^^^

        symbol = new BetterTextPane();
        ComponentFactory.fixWidget(symbol, "Notes");
        symbol.setOpaque(true);
        symbol.setText(preferences.getNotes());
        symbol.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                preferences.setNotes(((JTextComponent)e.getSource()).getText());
            }
        });
        JScrollPane scrollPane = new JScrollPane(symbol);
        scrollPane.setPreferredSize(new Dimension(-1, 150)); 
        add(scrollPane);
        setVisible(preferences.isInfoPanelVisible());
    }
}
