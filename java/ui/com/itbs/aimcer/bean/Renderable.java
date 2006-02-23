package com.itbs.aimcer.bean;

import javax.swing.*;

/**
 * Implemented by those who want to be rendered in the PeopleList. 
 * @author Created by Alex Rass on Sep 26, 2004
 */
public interface Renderable {
    JComponent getDisplayComponent(boolean isSelected, boolean cellHasFocus);
}
