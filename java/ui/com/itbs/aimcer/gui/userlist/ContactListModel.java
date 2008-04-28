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

package com.itbs.aimcer.gui.userlist;

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.commune.*;
import com.itbs.aimcer.gui.Main;
import com.itbs.gui.DelayedActionThread;
import com.itbs.gui.EditableJList;
import com.itbs.gui.GUIUtils;
import com.itbs.util.DelayedThread;

import javax.swing.*;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Maintains the model for the List which displays contacts.
 *
 * @author Alex Rass
 * @since Sep 9, 2004
 */
public class ContactListModel extends AbstractListModel implements ConnectionEventListener, EditableJList.MutableListModel {
    private static final Logger log = Logger.getLogger(ContactListModel.class.getName());
    private static ContactListModel instance = new ContactListModel();
    DelayedThread flagThread;


    private ContactListModel() {
        flagThread = new DelayedActionThread("TreeTableUpdateThread", 1000, null, null, new Runnable() {
            public void run() {
//                log.info("Notifying the table of updates.");
                GUIUtils.runOnAWT(new Runnable() {
                    public void run() {
                        fireContentsChanged(this, 1, getSize());
                    }
                });
            }
        }); // flagThread
        flagThread.start();
        
    }

    public static ContactListModel getInstance() {
        return instance;
    }


    public int getSize() {
        int sum = 0;
        GroupWrapper groupWrapper;
        ContactWrapper contactWrapper;
        GroupList glist = Main.standardGroupFactory.getGroupList(); // using trick that it's static
        for (int i = 0; i < glist.size(); i++) {
            Group group =  glist.get(i);
            if (ClientProperties.INSTANCE.isHideEmptyGroups()  && (group.size() == 0 || (ClientProperties.INSTANCE.isHideOffline() && (group instanceof GroupWrapper) && ((GroupWrapper) group).sizeOnline() == 0))) {
                continue;
            }
            sum++;
            if (group instanceof GroupWrapper)
                groupWrapper = (GroupWrapper) group;
            else
                groupWrapper = (GroupWrapper) Main.standardGroupFactory.create(group);
//            log.fine("Group: "+g.getName());
            if (groupWrapper.isShrunk())
                continue;
            for (int contactCount = 0; contactCount < group.size(); contactCount++) {
                Nameable b = group.get(contactCount);
                if (b instanceof ContactWrapper) {
                    contactWrapper = (ContactWrapper) b;//ContactWrapper.create(b, connection.get(connIndex));
                    if (contactWrapper.getStatus().isOnline() || !ClientProperties.INSTANCE.isHideOffline() || contactWrapper.getPreferences().isShowInList())
                        sum++;
                }
            }
        }
        return sum;
    }

    
    /**
     * Returns an element at index.
     * Sometimes returns nulls due to the fact that we don't wait for silly awt thread to do things with the underlying list.
     * 
     * @param index of element
     * @return element or null.
     */
    public Object getElementAt(final int index) {
        int count = 0;
        GroupWrapper groupWrapper;
        ContactWrapper contactWrapper;
        Group[] groups = Main.standardGroupFactory.getGroupList().toArray(); // using trick that it's static for the UI
        if (ClientProperties.INSTANCE.isSortContactList()) {
            Arrays.sort(groups, GroupWrapper.COMP_NAME);
        }
        for (Group group : groups) {
            if (group instanceof GroupWrapper)
                groupWrapper = (GroupWrapper) group;
            else
                groupWrapper = (GroupWrapper) Main.standardGroupFactory.create(group);
            // if we don't need to display members, move on.
            if (ClientProperties.INSTANCE.isHideEmptyGroups() && ClientProperties.INSTANCE.isHideOffline() && groupWrapper.sizeOnline() == 0) {
                continue;
            }
            if (index == count)
                return groupWrapper;
            count++;
            if (groupWrapper.isShrunk())
                continue;
            Nameable[] contacts = group.toArray();
            if (ClientProperties.INSTANCE.isSortContactList()) {
                Arrays.sort(contacts, ContactWrapper.COMP_NAME);
            }
            for (Nameable contact : contacts) {
                if (contact instanceof ContactWrapper) {
                    contactWrapper = (ContactWrapper) contact;//ContactWrapper.create(b, connection.get(connIndex));
                    if (contactWrapper.getStatus().isOnline() || !ClientProperties.INSTANCE.isHideOffline() || contactWrapper.getPreferences().isShowInList()) {
                        if (index == count)
                            return ContactLabel.construct(contactWrapper, groupWrapper);
                        count++;
                    }
                }
            }
        }
        return null;
    }

//    List<ListDataListener> listeners = new ArrayList<ListDataListener>();

    void runActionDataChanged() {
        flagThread.mark();
    }

    int delay;

    public synchronized void statusChanged(final Connection connection) {
        flagThread.mark();
    }

    public synchronized void statusChanged(final Connection connection, final Contact contact,
                              final Status oldStatus) {
        flagThread.mark();
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
     * @param connection connection
     * @param contact who initiated msg
     * @param filename proposed name of file
     * @param description of the file
     * @param connectionInfo  your private object used to store protocol specific data
     */
    public void fileReceiveRequested(FileTransferSupport connection, Contact contact, String filename, String description, Object connectionInfo) {
    }

    // ----------- MutableListModel

    public boolean isCellEditable(int index) {
        Object element  = getElementAt(index);
        return  element !=null && element instanceof ContactWrapper;
    }

    public void setValueAt(Object value, int index) {
        Object element  = getElementAt(index);
        if (element !=null && element instanceof ContactWrapper) {
            ((ContactWrapper) element).getPreferences().setDisplayName(""+value);
            ((ContactWrapper) element).updateDisplayComponent();
        }
    }
}
