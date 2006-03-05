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

import com.itbs.aimcer.bean.Message;
import com.itbs.aimcer.bean.Nameable;

import javax.swing.text.html.HTMLEditorKit;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Support for Sending and receiving messages.
 *
 * @author Alex Rass
 * @since Apr 24, 2005
 */
abstract public class AbstractMessageConnection extends AbstractConnection implements MessageSupport {
    private String userName, password;
    private Nameable user;

    private boolean away;
    private Executor executor = Executors.newSingleThreadScheduledExecutor();

    /** These are for restoring connection info */

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        if (userName == null)
            user = null;
        else if (getContactFactory() != null) // update user.  But it doesn't make sense to do it before connect.
            user = getContactFactory().create(getUserName(), this);
    }

    /**
     * Most of the time, one would overwrite it.
     * Make sure you call super, if need the coverage.
     *
     * @throws SecurityException exception
     * @throws Exception exception
     */
    public void connect() throws SecurityException, Exception {
        setUserName(getUserName()); // update user 
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
     * @param name
     * @param pass
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
     * @param away
     */
    public void setAway(boolean away) {
        this.away = away;
        // Notify all of a status change
        for (ConnectionEventListener connectionEventListener : eventHandlers) {
            try {
                connectionEventListener.statusChanged(this);
            } catch (Exception e) {
                e.printStackTrace(); // shhh.  keep this quiet for now. no biggie
            }
        }
    }

    // ------------------------------- M E S S A G E ----------------------------------------------
    public boolean isSecureMessageSupported() {
        return false;
    }

    final public void sendMessage(final Message message) {
        executor.execute(new Runnable() {
            public void run() {
                try {
                    processMessage(message);
                    notifyOfAMessage(message);
                } catch (Exception e) {
                    for (ConnectionEventListener eventHandler : eventHandlers) {
                        eventHandler.errorOccured("Failed to send a message", e);
                    }
                }
            }
        });
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
                    for (ConnectionEventListener eventHandler : eventHandlers) {
                        eventHandler.errorOccured("Failed to send a message", e);
                    }
                }
            }
        });
    }

    private void notifyOfAMessage(Message message) {
        for (ConnectionEventListener connectionEventListener:eventHandlers) {
            try {
                connectionEventListener.messageReceived(this, message);
            } catch (Exception e) {
                for (ConnectionEventListener eventListener:eventHandlers) {
                    eventListener.errorOccured("Error processing message.", e);
                }
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

    public HTMLEditorKit getEditorKit() {
        return new HTMLEditorKit();
    }
}
