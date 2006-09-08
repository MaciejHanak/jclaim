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

package com.itbs.aimcer.commune.jaim;

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.bean.Group;
import com.itbs.aimcer.commune.AbstractMessageConnection;
import com.itbs.aimcer.commune.ConnectionEventListener;
import com.itbs.util.GeneralUtils;
import com.wilko.jaim.*;

import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TOC AIM.
 * Uses the TOC protocol.
 * Rather unreliable and featureless.
 *
 * @author Alex Rass
 * @since Sep 25, 2004
 */
public class JAIMConnection extends AbstractMessageConnection implements JaimEventListener {
    private static Logger log = Logger.getLogger(JAIMConnection.class.getName());
    private JaimConnection connection;
    /** remembers last state. */
    private boolean disconnect;
    private int timeout = 30000;

    public String getServiceName() {
        return "AIM";
    }

    public JAIMConnection() {
        serverName = "toc.oscar.aol.com";
        serverPort = 9898;
    }

    public String getSupportAccount() {
        return "JClaimHelp";
    }

    public void connect() throws SecurityException, Exception {
        super.connect();
        for (ConnectionEventListener el:eventHandlers) {
            el.connectionInitiated(this);
        }
        connection = new JaimConnection(getServerName(), getServerPort());
        connection.setDebug(true);   // Send debugging to standard output
        connection.connect();
        connection.addEventListener(this);
        Thread.sleep(100);  // too fast and it dies
        connection.watchBuddy(getUserName());
        Thread.sleep(100);
        try {
            connection.logIn(getUserName(), getPassword(), timeout);
        } catch (JaimException e) {
            log.log(Level.SEVERE, "login error", e);
            throw new SecurityException(e.getMessage());
        }
        Thread.sleep(100);
        connection.addBlock("");     // Set Deny None
        connection.setInfo("This client is using <a href=\"http://www.itbsllc.com/software/aimcer.htm\"> JClaim </a>.");
        receiveConfig();
        Thread.yield();
        disconnect = false;
        // tell everyone we are now running connected
        // use itertors b/c the size will change
        notifyConnectionEstablished();
    }

    /**
     * Only use when adding new buddies.
     * @param contact to add
     * @param group to add to
     */
    public void addContact(Nameable contact, Group group) {
        // do it for the server
        connection.addBuddy(contact.getName(), group.getName());
        group.add(contact);
    }

    public void addContactGroup(Group group) {
    }

    public void removeContactGroup(Group group) {
    }

    /**
     * Use to remove contacts.
     * @param contact to delete
     */
    public void removeContact(Nameable contact) {
        // do it for the server
        connection.unwatchBuddy(contact.getName());
        GroupList list = getGroupList();
        for (int i = list.size(); i>0; i--) {
            list.get(i).remove(contact);
        }
    }

    private void receiveConfig() {
        //run through them all and add watches for all
        for (Object o : connection.getGroups()) {
            com.wilko.jaim.Group g = (com.wilko.jaim.Group) o;
            Group group = getGroupFactory().create(g.getName());
            Enumeration e = g.enumerateBuddies();
            while (e.hasMoreElements()) {
                Buddy b = (Buddy) e.nextElement();
                group.add(getContactFactory().create(b.getName(), this)); // add to the list of contacts
                try {
                    connection.watchBuddy(b.getName());
                } catch (JaimException e1) {
                    log.log(Level.SEVERE, "", e1);
                }
            }
            getGroupList().add(group);
        }
        for (ConnectionEventListener eventHandler : eventHandlers) {
            eventHandler.statusChanged(this);
        }
    }

    public void disconnect(boolean intentional) {
        disconnect = true;
        if (connection!=null)
            connection.destroy();
        super.disconnect(intentional);
    }

    public void reconnect() {

    }

    public boolean isLoggedIn() {
        return connection!=null && connection.isLoginComplete();
    }

    public void cancel() {
        // d/n apply to this library
    }

    public void setAway(boolean away) {
        try {
            connection.setIdle(1);
        } catch (IOException e) {
            log.log(Level.SEVERE, "setAway", e);
        }
        super.setAway(away);
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void unWatchBuddy(Nameable contact) {
        connection.unwatchBuddy(contact.getName());
    }

    public void processMessage(Message message) throws IOException {
        connection.sendIM(message.getContact().getName(), message.getText());
    }

    public void processSecureMessage(Message message) throws IOException {
        throw new IOException("Sending secure messages is not possible with TOC");
    }

    /**
     * Receive an event and process it according to its content
     *
     * @param event The JaimEvent to be processed
     */
    public void receiveEvent(JaimEvent event) {
        TocResponse tr = event.getTocResponse();
        String responseType = tr.getResponseType();
        if (responseType.equalsIgnoreCase(BuddyUpdateTocResponse.RESPONSE_TYPE)) {
            receiveBuddyUpdate((BuddyUpdateTocResponse) tr);
        } else
        if (responseType.equalsIgnoreCase(IMTocResponse.RESPONSE_TYPE)) {
            receiveIM((IMTocResponse) tr);
        } else if (responseType.equalsIgnoreCase(EvilTocResponse.RESPONSE_TYPE)) {
//            receiveEvil((EvilTocResponse) tr);
        } else if (responseType.equalsIgnoreCase(GotoTocResponse.RESPONSE_TYPE)) {
//            receiveGoto((GotoTocResponse) tr);
        } else if (responseType.equalsIgnoreCase(ConfigTocResponse.RESPONSE_TYPE)) {
//            receiveConfig();
        } else if (responseType.equalsIgnoreCase(ErrorTocResponse.RESPONSE_TYPE)) {
            receiveError((ErrorTocResponse) tr);
        } else if (responseType.equalsIgnoreCase(LoginCompleteTocResponse.RESPONSE_TYPE)) {
            notifyConnectionEstablished();
        } else if (responseType.equalsIgnoreCase(ConnectionLostTocResponse.RESPONSE_TYPE)) {
            receiveConnectionLost();
        } else {
            log.severe("Unknown TOC Response:" + tr.toString());
        }
    }

    private void receiveBuddyUpdate(BuddyUpdateTocResponse response) {
        Contact contact = getContactFactory().create(response.getBuddy(), this);
        for (ConnectionEventListener connectionEventListener: eventHandlers) {
            connectionEventListener.statusChanged(this, contact, response.isOnline(), response.isAway(), response.getIdleTime());
        }
    }


    private void receiveError(ErrorTocResponse et) {
        for (ConnectionEventListener connectionEventListener: eventHandlers) {
            connectionEventListener.errorOccured("Error: " + et.getErrorText()+"\n\n"+et.getErrorDescription(), null);
        }
    }

    private void receiveIM(final IMTocResponse conv) {
        Message message = new MessageImpl(getContactFactory().create(conv.getFrom(), JAIMConnection.this), false, conv.getMsg());
        for (ConnectionEventListener connectionEventListener: eventHandlers) {
            try {
                connectionEventListener.messageReceived(this, message);
            } catch (Exception e) {
                for (ConnectionEventListener eventListener: eventHandlers) {
                    eventListener.errorOccured("Error processing message.", e);
                }
            }
        }
    }

    int connectionLostTimes;
    private void receiveConnectionLost() {
//        main.setTitle("Offline (reconnecting)");
        log.info("Connection lost! "+ ++connectionLostTimes);
        for (ConnectionEventListener cel : eventHandlers) {
            cel.connectionLost(this);
        }
        synchronized(connection) {
            try {
                if (connection.isLoginComplete() || getUser() == null || disconnect) // recovered or quit
                    return;
                connection.disconnect();
                log.fine("Attempting a reconnect.");
                connect();
            } catch (Exception e) {
                log.log(Level.INFO,"login" ,e);
                try {
                    Thread.sleep(60*1000);
                } catch (InterruptedException e1) {  //
                }
            }
        }
    }

    /**
     * True if this is a system message.
     * @param contact to check
     * @return true if a system message
     */
    public boolean isSystemMessage(Nameable contact) {
        return "aolsystemmsg".equals(GeneralUtils.getSimplifiedName(contact.getName()));
    }
} // class JAIMConnecion
