package com.itbs.aimcer.commune;

import com.itbs.aimcer.bean.Contact;
import com.itbs.aimcer.bean.ContactWindow;
import com.itbs.aimcer.bean.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Calls all listeners.
 * Knows nothing about specific window managers and just does very generic management.
 * 
 * @author Alex Rass
 * @since Feb 27, 2008
 */
public class GlobalWindowHandler implements ContactWindow {
    List <ContactWindow> windowInterfaces = new ArrayList<ContactWindow>(2);
    // ------ Management ---------------
    public void add(ContactWindow windowInterface) {
        if (!windowInterfaces.contains(windowInterface)) {
            windowInterfaces.add(windowInterface);
        }
    }

    public void remove(ContactWindow windowInterface) {
        windowInterfaces.remove(windowInterface);
    }

    // ------ Callbacks ---------------
    public void openWindow(Contact buddyWrapper, boolean forceToFront) {
        for (ContactWindow contactWindow : windowInterfaces) {
            contactWindow.openWindow(buddyWrapper, forceToFront);
        }
    }

    public void openWindow(List <? extends Contact> allContacts, boolean forceToFront) {
        for (ContactWindow contactWindow : windowInterfaces) {
            contactWindow.openWindow(allContacts, forceToFront);
        }
    }

    public boolean isWindowOpen(Contact buddyWrapper) {
        for (ContactWindow contactWindow : windowInterfaces) {
            boolean wo = contactWindow.isWindowOpen(buddyWrapper);
            if (wo) return true;
        }
        return false;
    }

    public void closeWindow(Contact buddyWrapper) {
        for (ContactWindow contactWindow : windowInterfaces) {
            contactWindow.closeWindow(buddyWrapper);
        }
    }

    public void addTextToHistoryPanel(Contact buddyWrapper, Message message, final boolean toBuddy) throws IOException {
        for (ContactWindow contactWindow : windowInterfaces) {
            contactWindow.addTextToHistoryPanel(buddyWrapper, message, toBuddy);
        }
    }

    public void addConnection(MessageSupport connection) {
        for (ContactWindow contactWindow : windowInterfaces) {
            contactWindow.addConnection(connection);
        }
    }

    public void addPropertyChangeListener() {
        for (ContactWindow contactWindow : windowInterfaces) {
            contactWindow.addPropertyChangeListener();
        }
    }

}
