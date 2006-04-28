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

import com.itbs.aimcer.bean.Renderable;
import com.itbs.gui.ErrorDialog;

import javax.swing.*;
import java.awt.*;

/**
 * Provides mechanism for drawing items in a list.
 * Handles selection for list items.
 *
 * @author Alex Rass
 * @since Sep 9, 2004
 */
public class ListRenderer implements ListCellRenderer {
    public static final Color SELECTED = Color.BLUE;
    public static final Color NOT_SELECTED = Color.WHITE;
    public static final Color SELECTED_NO_FOCUS = Color.BLUE.brighter().brighter();

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof Renderable) {
            Renderable wrapper = (Renderable) value;
            return wrapper.getDisplayComponent(isSelected, cellHasFocus);
        } 
        if (value != null) // complain
            ErrorDialog.displayError(list, "Fatal error occured.\nPlease email us the trace from details tab.",
                new Exception("Found a non-renderable component " + (value.getClass() + "/" + value.toString())));
        return new JLabel("Component Error ");
    }
}