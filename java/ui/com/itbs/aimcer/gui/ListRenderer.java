package com.itbs.aimcer.gui;

import com.itbs.aimcer.bean.Renderable;
import com.itbs.gui.ErrorDialog;

import javax.swing.*;
import java.awt.*;

/**
 * @author Created by Alex Rass on Sep 9, 2004
 */
public class ListRenderer implements ListCellRenderer {
    public static final Color SELECTED = Color.BLUE;
    public static final Color NOT_SELECTED = Color.WHITE;
    public static final Color SELECTED_NO_FOCUS = Color.BLUE.brighter().brighter();

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof Renderable) {
            Renderable wrapper = (Renderable) value;
            return wrapper.getDisplayComponent(isSelected, cellHasFocus);
        } else {
            if (value != null) // complain
                ErrorDialog.displayError(list, "Fatal error occured.\nPlease email us the trace from details tab.",
                    new Exception("Found a non-renderable component " + (value.getClass() + "/" + value.toString())));
            return new JLabel("Component Error ");
        }
    }
}