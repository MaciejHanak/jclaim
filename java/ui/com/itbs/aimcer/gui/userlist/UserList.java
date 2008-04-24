package com.itbs.aimcer.gui.userlist;

import com.itbs.aimcer.commune.ConnectionEventListener;

import javax.swing.*;
import java.util.List;

/**
 * Defines a list of stuff that a good UI should support.
 * 
 * @author Alex Rass
 * @since Apr 15, 2008 12:17:10 PM
 */
public interface UserList {
    List getSelectedValues();
    ConnectionEventListener getConnectionEventListener();
    void update();
    JComponent getDisplayComponent();
}
