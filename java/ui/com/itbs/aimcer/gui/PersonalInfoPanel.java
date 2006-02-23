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
import com.itbs.gui.BetterTextField;
import com.itbs.gui.BetterTextPane;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;
import java.awt.*;

/**
 * Hosts the additional user info.
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
 * @author Created by Alex Rass on Oct 21, 2004
 * @since Jan, 2004
 * Copyright 2004.
 */
public class PersonalInfoPanel extends JPanel {
    ContactPreferences preferences;

    public PersonalInfoPanel(final ContactWrapper contact) {
        preferences = contact.getPreferences();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JTextComponent symbol;
        JPanel namesPanel;

        namesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        symbol = new BetterTextField(preferences.getName(), 14);
        ComponentFactory.fixWidget(symbol, "Full Name");
//      symbol.setBorder(new TitledBorder("Full Name"));
        symbol.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                preferences.setName(((JTextComponent)e.getSource()).getText());
            }
        });
        namesPanel.add(symbol);

        symbol = new BetterTextField(preferences.getDisplayName(), 10);
        ComponentFactory.fixWidget(symbol, "Display Name");
//        symbol.setBorder(new TitledBorder());
        symbol.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                preferences.setDisplayName(((JTextComponent)e.getSource()).getText());
                contact.updateDisplayComponent(); //nicely enough - right on AWT thread
            }
        });
        namesPanel.add(symbol);
        add(namesPanel);

        namesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        symbol = new BetterTextField(preferences.getPhone(), 14);
        ComponentFactory.fixWidget(symbol, "Cell Phone");
        symbol.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                preferences.setPhone(((JTextComponent)e.getSource()).getText());
            }
        });
        namesPanel.add(symbol);

        symbol = new BetterTextField(preferences.getEmailAddress(), 14);
        ComponentFactory.fixWidget(symbol, "Email");
        symbol.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                preferences.setEmailAddress(((JTextComponent)e.getSource()).getText());
            }
        });
        namesPanel.add(symbol);
        add(namesPanel);

        symbol = new BetterTextPane();
        ComponentFactory.fixWidget(symbol, "Notes");
        symbol.setOpaque(true);
        symbol.setText(preferences.getNotes());
        symbol.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                preferences.setNotes(((JTextComponent)e.getSource()).getText());
            }
        });
        add(symbol);
        setVisible(preferences.isInfoPanelVisible());
    }
}
