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

package com.itbs.aimcer.commune.smack;

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.commune.AbstractMessageConnection;
import com.itbs.aimcer.commune.ConnectionEventListener;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * Provides connection to Jabber.
 *
 * @author Created by Alex Rass on Dec 24, 2004
 */
public class SmackConnection extends AbstractMessageConnection {
    public static final String DEFAULT_HOST = "jabber.org";
    public static final int DEFAULT_PORT = 5222;
    public static final int DEFAULT_PORT_SSL = 5223;
    XMPPConnection connection;

    protected XMPPConnection getNewConnection() throws XMPPException {
        return new XMPPConnection(
                System.getProperty("JABBER_HOST", DEFAULT_HOST),
                Integer.getInteger("JABBER_PORT", DEFAULT_PORT)
                );
    }
    /**
     * Non-blocking call.
     */
    public void connect() throws SecurityException, Exception {
        super.connect();
        notifyConnectionInitiated();
        new Thread() {
            public void run() {
                connectReal();
            }
        }.start();
    }

    public void connectReal(){
        try {
            connection = getNewConnection();
//            connection.loginAnonymously();
            connection.login(getUserName(), getPassword());
            fireConnect();
        } catch (XMPPException e) {
            e.printStackTrace();
            disconnect(false);
            for (ConnectionEventListener eventHandler : eventHandlers) {
                eventHandler.connectionFailed(this, "Connection Failed. " + (e.getXMPPError()==null?e.getMessage():e.getXMPPError().getMessage()));
//                eventHandler.connectionEstablished(this);
            }
        }
    }

    private void fireConnect() {
          //////////////////////
         // Allow All to Add //
        /////////////////////
        connection.getRoster().setSubscriptionMode(Roster.SUBSCRIPTION_ACCEPT_ALL);

          ///////////////////
         // get user list //
        //////////////////
        Group lastGroup;
        Contact contact;
        Iterator iter = connection.getRoster().getGroups();
        while (iter.hasNext()) {
            RosterGroup rosterGroup = (RosterGroup) iter.next();
            getGroupList().add(lastGroup = getGroupFactory().create(rosterGroup.getName()));
            lastGroup.clear(this); // b/c of reconnect
            Iterator entries = rosterGroup.getEntries();
            while (entries.hasNext()) {
                RosterEntry rosterEntry = (RosterEntry) entries.next();
                contact = getContactFactory().create(rosterEntry.getUser(), this);
                contact.setDisplayName(rosterEntry.getName());
                lastGroup.add(contact);
            }
        } // while

          /////////////////////
         // Handle Messages //
        ////////////////////
        // Create a packet filter to listen for new messages from a particular
        // user. We use an AndFilter to combine two other filters.
        PacketFilter filter = new MessageTypeFilter(org.jivesoftware.smack.packet.Message.Type.CHAT);
        // Next, create a packet listener. We use an anonymous inner class for brevity.
        PacketListener messageListener = new PacketListener() {
            public void processPacket(Packet packet) {
                try { // try and not kill Smack!
                    Message message;
                    String from = normalizeName(packet.getFrom());
                    if (packet instanceof org.jivesoftware.smack.packet.Message) {
                        org.jivesoftware.smack.packet.Message smackMessage = (org.jivesoftware.smack.packet.Message) packet;
                        message = new MessageImpl(getContactFactory().create(from, SmackConnection.this),
                                false, false, smackMessage.getBody());
                    } else
                        message = new MessageImpl(getContactFactory().create(from, SmackConnection.this),
                                false, false, (String)packet.getProperty("body"));
                    for (int i = 0; i < eventHandlers.size(); i++) {
                        try {
                            (eventHandlers.get(i)).messageReceived(SmackConnection.this, message);
                        } catch (Exception e) {
                            for (ConnectionEventListener eventHandler : eventHandlers) {
                                eventHandler.errorOccured(i + ": Failure while processing a received message.", e);
                            }
                        }
                    }
                } catch (Exception e) {
                    for (ConnectionEventListener eventHandler : eventHandlers) {
                        eventHandler.errorOccured("Failure while receiving a message", e);
                    }
                }
            }
        };

        // Register the listener.
        connection.addPacketListener(messageListener, filter);


        connection.getRoster().addRosterListener(new RosterListener() {
            public void entriesAdded(Collection addresses) {
                // Ignore event for now
            }

            public void entriesUpdated(Collection addresses) {
                // Ignore event for now
            }

            public void entriesDeleted(Collection addresses) {
                // Ignore event for now
            }

            /** keep for compatibility with earlier versions. */
            public void rosterModified() {
            }

            public void presenceChanged(String user) {
                user = normalizeName(user);
                // If the presence is unavailable then "null" will be printed,
                // which is fine for this example.
                Presence presence = connection.getRoster().getPresence(user);
                Contact contact = getContactFactory().create(user, SmackConnection.this);
                for (ConnectionEventListener eventHandler : eventHandlers) { //online: info.getOnSince().getTime() > 0
                    eventHandler.statusChanged(SmackConnection.this,
                            contact, presence != null, presence == null || presence.getType() == Presence.Type.UNAVAILABLE, 0);
                }
            }
        });

        for (ConnectionEventListener eventHandler : eventHandlers) {
            eventHandler.statusChanged(this);
        }

        // tell everyone we are now running connected
        // use itertors b/c the size will change
        notifyConnectionEstablished();
    }

    private String normalizeName(String userName) {
        int index = userName.indexOf('/');
        if (index > 0 ) { // yes, 0
            return userName.substring(0, index);
        }
        return userName;
    }

    public void disconnect(boolean intentional) {
        if (connection!=null)
            connection.close();
        super.disconnect(intentional);
    }

    public void reconnect() {
        try {
            connection.close();
            connect();
        } catch (Exception e) {
//            GeneralUtils.sleep(1000);
            e.printStackTrace();
        }
    }

    public boolean isLoggedIn() {
        return connection!=null && connection.isConnected();
    }

    /**
     * Cancel login.
     */
    public void cancel() {
        if (!isLoggedIn())
            connection.close();
    }

    public void setTimeout(int timeout) {
    }

    public void addContact(Nameable contact, Group group) {
        String[] groupNames = new String[1];
        groupNames[0] = group.getName();
        try {
            connection.getRoster().createEntry(contact.getName(), contact.getName(), groupNames);
        } catch (XMPPException e) {
            for (ConnectionEventListener connectionEventListener : eventHandlers) {
                connectionEventListener.errorOccured("Failed to add a contact " + contact.getName(), e);
            }
        }
        group.add(contact);
    }

    public void removeContact(Nameable contact) {
        Iterator iter = connection.getRoster().getGroups();
        while (iter.hasNext()) {
            RosterGroup rosterGroup = (RosterGroup) iter.next();
            Iterator entries = rosterGroup.getEntries();
            while (entries.hasNext()) {
                RosterEntry rosterEntry = (RosterEntry) entries.next();
                if (rosterEntry.getName().equals(contact.getName())) {
                    try {
                        rosterGroup.removeEntry(rosterEntry);
                    } catch (XMPPException e) {
                        for (ConnectionEventListener connectionEventListener : eventHandlers) {
                            connectionEventListener.errorOccured("Found, but failed to remove the contact", e);
                        }
                    }
                    return;
                }
            }
        }
    }

    public void moveContact(Nameable contact, Group group) {
        removeContact(contact);
        addContact(contact, group);
    }

    public void addContactGroup(Group group) {
        connection.getRoster().createGroup(group.getName());
    }

    public void removeContactGroup(Group group) {
//        connection.getRoster(). // todo figure out
    }

    /**
     * Returns a short name for the service.
     * "AIM", "ICQ" etc.
     *
     * @return service name
     */
    public String getServiceName() {
        return "Jabber";
    }


    /**
     * True if this is a system message.
     *
     * @param contact to check
     * @return true if a system message
     */
    public boolean isSystemMessage(Nameable contact) {
        return false;
    }

    /**
     * Sets the away flag.
     *
     * @param away true if so
     */
    public void setAway(boolean away) {
        Presence presence = new Presence(away?Presence.Type.UNAVAILABLE:Presence.Type.AVAILABLE);
        presence.setStatus(away ? getProperties().getIamAwayMessage() : null);
        connection.sendPacket(presence);
        super.setAway(away);
    }

    /**
     * Overide this message with code that sends the message out.
     *
     * @param message to send
     * @throws java.io.IOException problems
     */
    protected void processMessage(Message message) throws IOException {
        Chat chat = connection.createChat(message.getContact().getName());
        try {
            chat.sendMessage(message.getText());
        } catch (XMPPException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Overide this message with code that sends the message out.
     *
     * @param message to send
     * @throws java.io.IOException problems
     */
    protected void processSecureMessage(Message message) throws IOException {
        processMessage(message); // todo go secure at some point
    }

/*
    public void createAccount(AccountInfo accountInfo) {
        Registration registration = new Registration();
        Map <String, String> attributes = new HashMap<String, String>(2);
        attributes.put("name", accountInfo.getUserName());
        attributes.put("password", accountInfo.getPassword());
        registration.setAttributes(attributes);
        // todo populate other fields
        connection.sendPacket(registration);
    }

    public AccountInfo getAccountInfo() {
        Registration registration = new Registration();
        connection.sendPacket(registration);
        AccountInfo accountInfo = new AccountInfo(registration.getUsername(), registration.getPassword());
        // todo collect other fields
        return accountInfo;
    }
*/

    public String getDefaultIconName() {
        return "jabber.gif";
    }
}
