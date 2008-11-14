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

package com.itbs.aimcer.commune;

import com.itbs.aimcer.bean.*;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Support for Sending and receiving messages.
 *
 * @author Alex Rass
 * @since Apr 24, 2005
 */
abstract public class AbstractMessageConnection extends AbstractConnection implements MessageSupport {
    private static Logger log = Logger.getLogger(AbstractMessageConnection.class.getName());
    
    private String userName, password;
    protected String serverName;
    protected int serverPort;
    private Nameable user;

    private boolean away;
    private Executor executor = Executors.newSingleThreadScheduledExecutor();

    /** These are for restoring connection info */

    public String getUserName() {
        return userName;
    }

    public void assignContactFactory(ContactFactory factory) {
        super.assignContactFactory(factory);
        // this will make sure the User is up to date in case order got reversed in calling assignnments.
        setUserName(getUserName());
    }

    public void setUserName(String userName) {
        this.userName = userName;
        if (userName == null)
            user = null;
        else if (getContactFactory() != null) // update user.  But it doesn't make sense to do it before connect.
            user = getContactFactory().create(getUserName(), this);
    }

    //      M E S S A G I N G     S E R V E R      I N F O
    /**
     * Messaging server address.
     * @return address
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Sets Messaging server address.
     * @param address to set
     */
    public void setServerName(String address) {
        serverName = address;
    }

    /**
     * Messaging server port.
     * @return port number
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * Messaging server port.
     * @param port number
     */
    public void setServerPort(int port) {
        serverPort = port;
    }

    //   ^^^     M E S S A G I N G     S E R V E R      I N F O    ^^^

    /**
     * Most of the time, one would overwrite it.
     * Make sure you call super, if need the coverage.
     *
     * @throws SecurityException exception
     * @throws Exception exception
     */
    public void connect() throws Exception {
        setUserName(getUserName()); // update user variable (not same as username, as it is a user object)
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Username for this connection.
     * @return name
     */
    public Nameable getUser() {
        return user;
    }

    /**
     * Allows one to check credentials withoout looking in
     * @param name username to try
     * @param pass password to try
     * @return true if match
     */
    public boolean isLoginInfoGood(String name, String pass) {
        return name!=null && pass!=null && name.equalsIgnoreCase(getUserName()) && pass.equals(getPassword());
    }

    /**
     * Default implementation - none.
     * @return null
     */
    public String getSupportAccount() {
        return null;
    }

    public boolean isAway() {
        return away;
    }

    /**
     * Notifies everyone that status has been updated.
     * If overwriting, call at the end of your method.
     * @param away status
     */
    public void setAway(boolean away) {
        this.away = away;
        // Notify all of a status change
        for (ConnectionEventListener connectionEventListener : eventHandlers) {
            try {
                connectionEventListener.statusChanged(this);
            } catch (Exception e) {
                log.log(Level.SEVERE, "Failed to set Away", e); // shhh.  keep this quiet for now. no biggie
            }
        }
    }

    // ------------------------------- G R O U P ----------------------------------------------

    /**
     * Will try to delete contact from group.
     * Will find it if it has to.
     * @param group to delete from
     * @param contact to delete
     */
    protected void cleanGroup(Group group, Nameable contact) {
        if (group != null) {
            group.remove(contact);
        } else {
            GroupList list = getGroupList();
            for (int i = list.size(); i>0; i--) {
                if (list.get(i).remove(contact)) break;
            }
        }
    }

    /**
     * Finds a group.  Helper.
     * @param contact to find by
     * @return group or null
     */
    protected Group findGroupViaBuddy(com.itbs.aimcer.bean.Nameable contact) {
        GroupList list = getGroupList();
        for (int i = list.size(); i>0; i--) {
            Group group = list.get(i);
            for (int j = group.size(); j>0; j--) {
                if (group.get(j).getName().equalsIgnoreCase(contact.getName()))
                    return group;
            }
        }
        return null;
    }


    // ------------------------------- M E S S A G E ----------------------------------------------
    public boolean isSecureMessageSupported() {
        return false;
    }

    final public void sendMessage(final Message message) {
        if (!isLoggedIn()) {
            notifyErrorOccured("Not logged in.", null);
        } else {
            executor.execute(new Runnable() {
                public void run() {
                    if (isLoggedIn()) {
                        try {
                            processMessage(message);
                            notifyOfAMessage(message);
                        } catch (Exception e) {
                            notifyErrorOccured("Failed to send a message", e);
                        }
                    } else {
                        notifyErrorOccured("You are not logged in.", null);
                    }
                }
            });
        }
    }

    public void sendTypingNotification() {
        // implementation is not required
    }

    final public void sendSecureMessage(final Message message) {
        executor.execute(new Runnable() {
            public void run() {
                try {
                    processSecureMessage(message);
                    notifyOfAMessage(message);
                } catch (Exception e) {
                    notifyErrorOccured("Failed to send a secure message", e);
                }
            }
        });
    }

    private void notifyOfAMessage(Message message) {
        for (ConnectionEventListener connectionEventListener:eventHandlers) {
            try {
                connectionEventListener.messageReceived(this, message);
            } catch (Exception e) {
                notifyErrorOccured("Error processing message.", e);
            }
        }
    }

    /**
     * Overide this message with code that sends the message out.
     * @param message to send
     * @throws IOException problems
     */
    abstract protected void processMessage(Message message) throws IOException;

    /**
     * Overide this message with code that sends the message out.
     * @param message to send
     * @throws IOException problems
     */
    abstract protected void processSecureMessage(Message message) throws IOException;
}
