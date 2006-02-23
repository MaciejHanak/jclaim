package com.itbs.aimcer.gui;

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.commune.*;

import javax.swing.*;
import java.util.Date;

/**
 * Maintains the model for the List which displays contacts.
 *
 * @author Alex Rass
 * @since Sep 9, 2004
 */
public class ContactListModel extends AbstractListModel implements ConnectionEventListener {
    Connection connection;
    private static final boolean DEBUG = false;
    private static ContactListModel instance = new ContactListModel();
    private ContactListModel() {}

    public static ContactListModel getInstance() {
        return instance;
    }

    public static void setConnection(Connection connection) {
        instance.connection = connection;
    }
    
    public int getSize() {
        if (connection == null) return 0;
        int sum = 0;
        GroupWrapper groupWrapper;
        ContactWrapper contactWrapper;
        GroupList glist = connection.getGroupList(); // using trick that it's static
        for (int i = 0; i < glist.size(); i++) {
            Group group =  glist.get(i);
            if (ClientProperties.INSTANCE.isHideEmptyGroups()  && (group.size() == 0 || (ClientProperties.INSTANCE.isHideOffline() && (group instanceof GroupWrapper) && ((GroupWrapper) group).sizeOnline() == 0))) {
                continue;
            }
            sum++;
            if (group instanceof GroupWrapper)
                groupWrapper = (GroupWrapper) group;
            else
                groupWrapper = (GroupWrapper) connection.getGroupFactory().create(group);
//            System.out.println("Group: "+g.getName());
            if (groupWrapper.isShrunk())
                continue;
            for (int contactCount = 0; contactCount < group.size(); contactCount++) {
                Nameable b = group.get(contactCount);
                if (b instanceof ContactWrapper) {
                    contactWrapper = (ContactWrapper) b;//ContactWrapper.create(b, connection.get(connIndex));
                    if (contactWrapper.getStatus().isOnline() || !ClientProperties.INSTANCE.isHideOffline())
                        sum++;
                }
            }
        }
        return sum;
    }

    /**
     * Returns an element at index.
     * Todo find out why it returns nulls
     * @param index of element
     * @return element or null.
     */
    public Object getElementAt(final int index) {
        if (connection == null) return null;
        int count = 0;
        GroupWrapper groupWrapper;
        ContactWrapper contactWrapper;
        GroupList glist = connection.getGroupList(); // using trick that it's static
        for (int i = 0; i < glist.size(); i++) {
            Group group = glist.get(i);
            if (group instanceof GroupWrapper)
                groupWrapper = (GroupWrapper) group;
            else
                groupWrapper = (GroupWrapper) connection.getGroupFactory().create(group);
            // if we don't need to display members, move on.
            if (ClientProperties.INSTANCE.isHideEmptyGroups()  && ClientProperties.INSTANCE.isHideOffline() && groupWrapper.sizeOnline() == 0) {
                continue;
            }
            if (index == count)
                return groupWrapper;
            count++;
            if (groupWrapper.isShrunk())
                continue;
            for (int contactCount = 0; contactCount < group.size(); contactCount++) {
                Nameable b = group.get(contactCount);
                if (b instanceof ContactWrapper) {
                    contactWrapper = (ContactWrapper) b;//ContactWrapper.create(b, connection.get(connIndex));
                    if (contactWrapper.getStatus().isOnline() || !ClientProperties.INSTANCE.isHideOffline()) {
                        if (index == count)
                            return contactWrapper;
                        count++;
                    }
                }
            }
        }
        return null;
    }

//    List<ListDataListener> listeners = new ArrayList<ListDataListener>();

    void runActionDataChanged() {
        fireContentsChanged(this, 1, getSize());
    }

    int delay;
    public synchronized void statusChanged(final Connection connection) {
        try { // todo move this into genetic utils
            if (SwingUtilities.isEventDispatchThread())
                runActionDataChanged();
            else
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (DEBUG)
                            System.out.println("status Changed All" + new Date());
                        runActionDataChanged();
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void statusChanged(final Connection connection, final Contact contact,
                              final boolean online, final boolean away, final int idleMins) {
        try {
            boolean redraw = (contact.getStatus().isAway() != away || contact.getStatus().isOnline() != online);
            contact.getStatus().setAway(away);
            contact.getStatus().setOnline(online);
            contact.getStatus().setIdleTime(idleMins);
            if (redraw) {
                if (delay % 10 == 0) {
                    System.gc();
                    delay = 0;
                }
                delay++;
                statusChanged(connection);
            } // redraw
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A previously requested icon has arrived.
     * Icon will be a part of the contact.
     *
     * @param connection connection
     * @param contact    contact
     */
    public void pictureReceived(IconSupport connection, Contact contact) {
        //Todo change?
    }

    public boolean messageReceived(MessageSupport connection, Message message) {
        return true;
    }

    public boolean emailReceived(MessageSupport connection, Message message) throws Exception {
        return true;
    }

    public void typingNotificationReceived(MessageSupport connection, Nameable contact) { }

    public void connectionLost(Connection connection) {
        statusChanged(connection);
    }

    public void connectionFailed(Connection connection, String message) {
        //do nothing
    }

    public void connectionInitiated(Connection connection) {
    }

    public void connectionEstablished(Connection connection) {
        statusChanged(connection);
    }
    public boolean contactRequestReceived(final String user, final MessageSupport connection) {  return true; }
    
    /**
     * Gets called when an assynchronous error occurs.
     *
     * @param message   to display
     * @param exception exception for tracing
     */
    public void errorOccured(String message, Exception exception) {
        //don't care
    }

    /**
     * Other side requested a file transfer.
     *
     @param connection connection
      * @param contact
     * @param filename
     * @param description
     * @param connectionInfo
     */
    public void fileReceiveRequested(FileTransferSupport connection, Contact contact, String filename, String description, Object connectionInfo) {
    }

}
