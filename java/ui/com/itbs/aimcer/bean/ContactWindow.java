package com.itbs.aimcer.bean;

import com.itbs.aimcer.commune.MessageSupport;

import java.io.IOException;

/**
 * Defines the minimal calls that a window messaging implementation needs to support.
 *
 * @author Alex Rass
 * @since Feb 27, 2008 12:26:39 AM
 */
public interface ContactWindow {
    void openWindow(Contact buddyWrapper, boolean forceToFront);
    boolean isWindowOpen(Contact buddyWrapper);
    void closeWindow(Contact buddyWrapper);

    public void addTextToHistoryPanel(Contact buddy, Message message, final boolean toBuddy) throws IOException;

    public void addConnection(MessageSupport connection);
    public void addPropertyChangeListener();    
}
