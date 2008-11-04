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

package com.itbs.aimcer.gui.order;

import com.itbs.aimcer.bean.Contact;
import com.itbs.aimcer.commune.Connection;
import com.itbs.aimcer.gui.MessageWindow;
import com.itbs.gui.ActionAdapter;
import com.itbs.gui.BetterButton;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Helps with the automated entry.
 *
 * @author Alex Rass
 * @since Feb 20, 2006
 */
public class IOIEntryPanel extends OrderEntryBase {
    public static final String NAME_ENDS_WITH="bot";

    /** Actions */
    static class IOIType {
        String name;
        String command;
        Action action;
        JButton button;
        Color color;
        MutableAttributeSet attribute;


        public IOIType(String name, String command, Color color, MutableAttributeSet attribute) {
            this.name = name;
            this.command = command;
            this.color = color;
            this.attribute = attribute;
        }

        public void setAction(Action action) {
            this.action = action;
            this.action.setEnabled(false);
            button = new BetterButton(action);
            if (color!=null)
                button.setBackground(color);
        }
    }

    private IOIType aBuy = new IOIType("Buy", "B", Color.GREEN.darker(), ATT_BUY);
    private IOIType aSell = new IOIType("Sell", "S", Color.RED, ATT_SELL);
    private IOIType aTraded = new IOIType("Traded", "T", null, ATT_NORMAL);
//    private IOIType aBought = new IOIType("Bought", "BOT", Color.GREEN.darker(), ATT_BUY);
//    private IOIType aSold = new IOIType("Sold", "SLD", Color.RED, ATT_SELL);
//    private IOIType aCrossed = new IOIType("Crossed", "X", null, ATT_NORMAL);

    List<IOIType> types = new ArrayList<IOIType>(6);
    public String getCommand(String action) {
        for (IOIType ioiType : types) {
            if (ioiType.name.equalsIgnoreCase(action)) {
                return ioiType.command;
            }
        }
        System.out.println("IOIEntry: Failed to match " + action);
        return "F"; // to fail
    }

/*
    private static final String[] SIDE_OPTIONS = {
        "Buy",
        "Sell",
        "Traded",
        "Sold",
        "Crossed",
        "Bought"
    }; // SIDE_OPTIONS
    static final String[] SIZE_TRANSLATIONS = {
            "B",
            "S",
            "T",
            "SLD",
            "X",
            "BOT"
    };
*/
//    JComboBox ioiType;
//    JButton go;

    public IOIEntryPanel() {
        types.add(aBuy);
        types.add(aSell);
        types.add(aTraded);
    }

    public IOIEntryPanel(final Connection connection, final Contact name, final MessageWindow historyPane) {
        this();
        setLayout(new BorderLayout());
/*      This one would have done it as a dropdown.
        add(ioiType = new JComboBox(SIDE_OPTIONS));
        ComponentFactory.fixWidget(ioiType, "Type");
        go = new BetterButton("Submit");
        go.setEnabled(false);
        go.setBorder(new EmptyBorder(14, 6, 6, 10));
        go.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int index=ioiType.getSelectedIndex();
                MutableAttributeSet attributes = index==0?ATT_BUY:index==1? ATT_SELL: ATT_NORMAL;
                OrderAction oa = new OrderAction(attributes, ioiType.getSelectedItem().toString(), connection, cw, messageWindow);
                oa.actionPerformed(null);
            }
        });
        add(go);
*/

//        types.add(aSold);
//        types.add(aCrossed);
//        types.add(aBought);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        // Assign all the actions:
        for (IOIType ioiType : types) {
            ioiType.setAction(new ActionAdapter(ioiType.name, new OrderAction(ioiType.name, connection, name), ioiType.command.charAt(0)));
        }

        JPanel center = new JPanel();
        center.setOpaque(false);

        center.add(aBuy.button);
        addSQP(center);
        add(center);
        center.add(aSell.button);

//        bottom.add(aBought.button);
        bottom.add(aTraded.button);
//        bottom.add(aCrossed.button);
//        bottom.add(aSold.button);
        add(bottom, BorderLayout.SOUTH);

    }

    protected void resetEntry() {
        symbol.setText("");
        qty.setText("");
        price.setText("");
        symbol.requestFocus();
//        ioiType.setSelectedIndex(0);
    }

    void addCaretListener(JComponent comp) {
        ((JTextComponent)comp).addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                boolean enabled =
                        price.getText().trim().length()!=0 &&
                        symbol.getText().trim().length()!=0 &&
                        qty.getText().trim().length()!=0;
                for (IOIType ioiType : types) {
                    ioiType.action.setEnabled(enabled);
                }
                aTraded.action.setEnabled(
                        price.getText().trim().length()==0 &&
                        symbol.getText().trim().length()!=0 &&
                        qty.getText().trim().length()!=0);
//                go.setEnabled(enabled); // enable right button
            }
        });
    }

}
