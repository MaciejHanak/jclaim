package com.itbs.aimcer.gui;

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.commune.Connection;
import com.itbs.aimcer.commune.ConnectionEventAdapter;
import com.itbs.aimcer.commune.MessageSupport;
import com.itbs.aimcer.tabbed.TabbedWindow;
import com.itbs.util.SoundHelper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Knows about individual ContactWindow classes.
 * Unlike GlobalWindowHandler class which does very generic management.
 * 
 * @author Alex Rass
 * @since Feb 27, 2008 12:55:33 AM
 */
public class WindowManager {
    public static final int INTERFACE_WINDOWED = 0;
    public static final int INTERFACE_TABBED   = 1;

    public static class Individual implements ContactWindow {
        public void addConnection(MessageSupport connection) {
            connection.addEventListener( MessageWindow.getConnectionEventListener());
        }

        public void openWindow(Contact buddyWrapper, boolean forceToFront) {
            if (ClientProperties.INSTANCE.getInterfaceIndex()==INTERFACE_WINDOWED) { // || buddyWrapper.getPreferences().isWindowPreferred
                MessageWindow.openWindow(buddyWrapper, forceToFront);
            }
        }

        public void openWindow(List<Contact> allContacts, List<Group> allGroups, boolean forceToFront) {
            if (ClientProperties.INSTANCE.getInterfaceIndex()==INTERFACE_WINDOWED) { // || buddyWrapper.getPreferences().isWindowPreferred
                MessageGroupWindow.openWindow(allContacts, allGroups);
            }
        }

        public boolean isWindowOpen(Contact buddyWrapper) {
            return MessageWindow.findWindow(buddyWrapper)!=null;
        }

        public void closeWindow(Contact buddyWrapper) {
            MessageWindow mw = MessageWindow.findWindow(buddyWrapper);
            if (mw!=null) {
                mw.closeWindow();
            }
        }

        public void addTextToHistoryPanel(Contact buddyWrapper, Message message, final boolean toBuddy) throws IOException {
            MessageWindow mw;
            if (ClientProperties.INSTANCE.getInterfaceIndex()==INTERFACE_WINDOWED) {
                mw = MessageWindow.openWindow(buddyWrapper, false);
            } else {
                mw = MessageWindow.findWindow(buddyWrapper);
            }
            if (mw!=null) {
                mw.addTextToHistoryPanel(message, true);
            }
        }

        public void addPropertyChangeListener() {
            // no need, we already go straight to properties for this stuff.
        }

    } // Individual

    public static class Tabbed implements ContactWindow {
        public void addConnection(MessageSupport connection) {
            connection.addEventListener( TabbedWindow.getINSTANCE().getConnectionEventListener());
        }

        public void openWindow(Contact buddyWrapper, boolean forceToFront) {
            if (ClientProperties.INSTANCE.getInterfaceIndex()==INTERFACE_TABBED) {
                TabbedWindow.getINSTANCE().addTab(buddyWrapper, forceToFront);
            }
        }

        public void openWindow(List<Contact> allContacts, List<Group> allGroups, boolean forceToFront) {
            if (ClientProperties.INSTANCE.getInterfaceIndex()==INTERFACE_TABBED) {
                TabbedWindow.getINSTANCE().addTab(allContacts, allGroups, forceToFront);
//                MessageGroupWindow.openWindow(allContacts.toArray(new ContactWrapper[allContacts.size()]));
            }
        }

        public boolean isWindowOpen(Contact buddyWrapper) {
            return TabbedWindow.getINSTANCE().isTabOpenedFor(buddyWrapper);
        }

        public void closeWindow(Contact buddyWrapper) {
            TabbedWindow.getINSTANCE().closeTab(buddyWrapper);
        }

        public void addTextToHistoryPanel(Contact buddyWrapper, Message message, final boolean toBuddy) throws IOException {
            if (ClientProperties.INSTANCE.getInterfaceIndex()==INTERFACE_TABBED || TabbedWindow.getINSTANCE().isTabOpenedFor(buddyWrapper)) {
                TabbedWindow.getINSTANCE().addTextToHistoryPanel(buddyWrapper, message, toBuddy);
            }
        }

        public void addPropertyChangeListener() {
            // no need, we already go straight to properties for this stuff.
        }
    }

    /**
     * Sound Provider Interface.
     */
    public static class Sounds implements ContactWindow {
        /** Used to make sure we don't beep too often */
        protected Map <Contact, Long> map = new WeakHashMap<Contact, Long>(20);
        
        public void addConnection(MessageSupport connection) {
            connection.addEventListener(new ConnectionEventAdapter() {
                public void connectionInitiated(Connection connection) { }
                public boolean messageReceived(MessageSupport connection, Message message) throws Exception {
                    boolean toBuddy = message.isOutgoing();
                    Long lastBeep = map.get(message.getContact());
                    if (ClientProperties.INSTANCE.isSoundAllowed()
                            && (!message.getContact().getConnection().isAway() || ClientProperties.INSTANCE.isSoundIdle())
                            && (lastBeep == null || lastBeep.longValue() + ClientProperties.INSTANCE.getBeepDelay() * 1000 < System.currentTimeMillis())) {
                        SoundHelper.playSoundOffThread(toBuddy ? ClientProperties.INSTANCE.getSoundSend() : ClientProperties.INSTANCE.getSoundReceive());
                    }
                    map.put(message.getContact(), System.currentTimeMillis());
                    return false;
                }
            });
        }

        public void addPropertyChangeListener() {
            // no need, we already go straight to properties for this stuff.
        }

        public void openWindow(Contact buddyWrapper, boolean forceToFront) {
            if (SoundHelper.playSound(ClientProperties.INSTANCE.getSoundNewWindow())) {
                map.put(buddyWrapper, System.currentTimeMillis()); // don't forget to update this puppy
            }
        }

        public void openWindow(List<Contact> allContacts, List<Group> allGroups, boolean forceToFront) {
        }

        public boolean isWindowOpen(Contact buddyWrapper) {
            return false;
        }

        public void closeWindow(Contact buddyWrapper) { }

        public void addTextToHistoryPanel(Contact buddyWrapper, Message message, final boolean toBuddy) throws IOException { }
    } // Sounds

}
