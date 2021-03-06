/*
 * Copyright (c) 2012, ITBS LLC. All Rights Reserved.
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

package com.itbs.aimcer.commune.twitter;

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.commune.AbstractMessageConnection;
import com.itbs.aimcer.commune.Connection;
import com.itbs.aimcer.commune.ConnectionEventListener;
import twitter4j.*;
import twitter4j.Status;

import javax.swing.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides connection to Twitter.
 *
 * Examples for using the API:
 * http://twitter4j.org/en/code-examples.html
 *
 * @author Alex Rass
 * @since Dec 24, 2011
 */
public class TwitterConnection extends AbstractMessageConnection {
    private static final Logger log = Logger.getLogger(TwitterConnection.class.getName());
    private static final String GROUP = "Followers";
    protected Twitter connection;

    /**
     * Non-blocking call.
     */
    public void connect() throws Exception {
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
            connection = new TwitterFactory().getInstance();
            if (getUserName()!=null && getUserName().length()>0) {
                connection.setOAuthConsumer(getUserName(), getPassword());
            }
            getList();
            fireConnect();
        } catch (Exception e) { // Lets see if we see this with Google.
            log.log(Level.SEVERE, "UNCAUGHT EXCEPTION! PLEASE FIX!", e);
            disconnect(false);
            notifyConnectionFailed("Connection Failed. UNUSUAL TERMINATION!" + e.getMessage());
        }
    }

    private void getList() throws TwitterException {
        Group lastGroup;
        Contact contact;
        getGroupList().add(lastGroup = getGroupFactory().create(GROUP));
        lastGroup.clear(this); // b/c of reconnect
        IDs followersIDs = connection.getFollowersIDs(connection.getId());
        ResponseList<User> users = connection.lookupUsers(followersIDs.getIDs());
        for (User user : users) {
            contact = getContactFactory().create(user.getName(), this);
            lastGroup.add(contact);
        } // while
	}


    protected void fireConnect() {
        notifyStatusChanged();
        notifyConnectionEstablished();
    } // fireConnect



    public void disconnect(boolean intentional) {
        if (connection!=null) {
            connection.shutdown();
            connection = null;
        }
        super.disconnect(intentional);
    }

    public void reconnect() {
        try {
            disconnect(false);
            connect();
        } catch (Exception e) {
//            GeneralUtils.sleep(1000);
            log.log(Level.SEVERE, "",e);
        }
    }

    public boolean isLoggedIn() {
        try {
            return connection!=null && connection.test();
        } catch (TwitterException e) {
            return false;
        }
    }

    /**
     * Cancel login.
     */
    public void cancel() {
        disconnect(false);
    }

    public void setTimeout(int timeout) {
    }

    public void addContact(Nameable contact, Group group) {
        String[] groupNames = new String[1];
        groupNames[0] = group.getName();
        try {
            connection.createFriendship(fixUserName(contact.getName()));
        } catch (TwitterException e) {
            for (ConnectionEventListener connectionEventListener : eventHandlers) {
                connectionEventListener.errorOccured("Failed to add a contact " + contact.getName(), e);
            }
        }
        group.add(contact);
    }

    /**
     * Used to fix the usernames for the jabber protocol.<br>
     * Usernames need server name.
     * @param name of the user's account to fix
     * @return name, including server.
     */
    protected String fixUserName(String name) {
//        if (name.indexOf('@')>-1) return name;
//        return name + "@" + getServerName();
        return name;
    }

    public boolean removeContact(Nameable contact, Group group) {
        try {
            connection.destroyFriendship(fixUserName(contact.getName()));
            return true;
        } catch (TwitterException e) {
            notifyErrorOccured("Failed to break a friendship\n"+e.getMessage(), e);
        }
        return false;
    }

    public void moveContact(Nameable contact, Group oldGroup, Group newGroup) {
    }

    public void addContactGroup(Group group) {
    }

    public void removeContactGroup(Group group) {
    }

    /**
     * Returns a short name for the service.
     * "AIM", "ICQ" etc.
     *
     * @return service name
     */
    public String getServiceName() {
        return "Twitter";
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
    }

    /**
     * Update twitter
     * @param message to update with
     * @return id of the message for a re-tweet
     */
    public long setStatus(String message) {
        long result=0;
        try {
            Status status = connection.updateStatus(message);
            if (status!=null) {
                result = status.getId();
            }
        } catch (TwitterException e) {
            notifyErrorOccured("Failed to update status", e);
        }
        notifyStatusChanged();
        return result;
    }

    public Twitter getUnderlyingConnection() {
        return connection;
    }

    /**
     * Update twitter
     * @param message to update with
     * @return id of the message for a re-tweet
     */
    public long retweetStatus(long message) {
        long result=0;
        try {
            Status status = connection.retweetStatus(message);
            if (status!=null) {
                result = status.getId();
            }
        } catch (TwitterException e) {
            notifyErrorOccured("Failed to update status", e);
        }
        notifyStatusChanged();
        return result;
    }

    public void reportSpam(String message) {
        try {
            connection.reportSpam(message);
        } catch (TwitterException e) {
            notifyErrorOccured("Failed to update status", e);
        }
    }

    /**
     * Overide this message with code that sends the message out.
     *
     * @param message to send
     * @throws java.io.IOException problems
     */
    protected void processMessage(Message message) throws IOException {
        try {
            connection.sendDirectMessage(message.getContact().getName(), message.getPlainText());
        } catch (TwitterException e) {
            log.log(Level.SEVERE, "", e);
            throw new IOException(e.getMessage());
        }
    }

//    Iterable<ExternalMessage> getMessages()
    public ResponseList<Status> getMentions(Paging paging) throws TwitterException {
        return connection.getMentions(paging);
    }

    /**
     * Overide this message with code that sends the message out.
     *
     * @param message to send
     * @throws java.io.IOException problems
     */
    protected void processSecureMessage(Message message) throws IOException {
        processMessage(message);
    }

    public class TwitterContact implements Contact {
        User user;
        StatusImpl status;

        TwitterContact(User user) {
            this.user = user;
            status = new StatusImpl(this);
            status.setOnline(true);
        }

        public void statusChanged() { }

        public Icon getIcon() { return null; }

        public void setIcon(Icon icon) { }

        public Icon getPicture() { return null; }

        public void setPicture(Icon icon) { }

        public String getDisplayName() { return user.getScreenName(); }

        public void setDisplayName(String name) { }

        public com.itbs.aimcer.bean.Status getStatus() {
            return status;
        }

        public Connection getConnection() {
            return TwitterConnection.this;
        }

        public String getName() {
            return user.getName();
        }

        public User getUnderlyingUser() {
            return user;
        }
    }

    public Contact getUserInfo(String name) throws Exception {
        return new TwitterContact(connection.showUser(name));
    }

    public QueryResult search(Query query) throws Exception {
        return connection.search(query);
    }

    public String getDefaultIconName() {
        return "twitter.png";
    }
} // class SmackConnection
