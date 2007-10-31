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

package com.itbs.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Used to clean up some code by making creation of Actions easy.
 * @author ARass
 * @since Jan 16, 2004
 */
final public class ActionAdapter extends AbstractAction {
    private final ActionListener callBack;
    ActionAdapter (String actionName, ActionListener callBack) {
        super(actionName);
        this.callBack = callBack;
    }
    public ActionAdapter (String actionName, ActionListener callBack, char mnemonicKey) {
        this(actionName,  callBack);
        putValue(Action.MNEMONIC_KEY, new Integer(mnemonicKey));
    }
    public ActionAdapter (String actionName, String toolTip, ActionListener callBack, char mnemonicKey) {
        this(actionName,  callBack, mnemonicKey);
        setTooltip(toolTip);
    }
    public ActionAdapter (String actionName, String toolTip, ActionListener callBack) {
        this(actionName,  callBack);
        setTooltip(toolTip);
    }
    public ActionAdapter (String actionName, Icon icon, ActionListener callBack) {
        super(actionName, icon);
        this.callBack = callBack;
    }

    public ActionAdapter(Icon icon, String toolTip, ActionListener callBack) {
        setIcon(icon);
        setTooltip(toolTip);
        this.callBack = callBack;
    }

    public void setTooltip(String tip) {
        putValue(Action.SHORT_DESCRIPTION, tip);
    }

    public void setIcon(Icon icon) {
        putValue(Action.SMALL_ICON, icon);
    }

    public String getCommand() {
        return getValue(Action.NAME).toString();
    }

    public void actionPerformed(ActionEvent e)
    {
        callBack.actionPerformed(e);
    }

    public boolean equals(Object obj) {
        if (obj instanceof ActionEvent)
            return getValue(Action.NAME).equals(((ActionEvent)obj).getActionCommand());
        else if (obj instanceof AbstractAction)
            return getValue(Action.NAME).equals(((AbstractAction)obj).getValue(Action.NAME));
        else
            return super.equals(obj);
    }

    public static JCheckBoxMenuItem createCheckMenuItem(String command, ActionListener eventHandler, char mnemonic, boolean checked)
    {
        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(new ActionAdapter(command, eventHandler));
        menuItem.setMnemonic(mnemonic);
        menuItem.setSelected(checked);
        menuItem.setActionCommand(command);
        return menuItem;
    }
    
    public static JMenuItem createMenuItem(String command, ActionListener eventHandler)
    {
        JMenuItem menuItem;
        menuItem = new JMenuItem(new ActionAdapter(command, eventHandler));
        return menuItem;
    }

    public static JMenuItem createMenuItem(String command, ActionListener eventHandler, char mnemonic)
    {
        JMenuItem menuItem = createMenuItem(command, eventHandler);
        menuItem.setMnemonic(mnemonic);
        return menuItem;
    }
}
