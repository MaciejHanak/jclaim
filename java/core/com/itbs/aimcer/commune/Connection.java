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

import java.util.Iterator;

/**
 * This is the main interface for all Connections.
 *
 * @author Alex Rass
 * @since Sep 22, 2004
 */
public interface Connection {
    /**
     * Allows properties to be passed in.
     * @param properties to use.
     */
    void setProperties(ConnectionProperties properties);
    ConnectionProperties getProperties();
    void assignContactFactory(ContactFactory factory);
    ContactFactory getContactFactory();
    void assignGroupFactory(GroupFactory factory);
    GroupFactory getGroupFactory();

    /**
     * Non-blocking call.
     */
    void connect() throws SecurityException, Exception;
    /**
     * Username for this connection.
     * @return name
     */
    Nameable getUser();
    /**
     * Allows one to check credentials withoout looking in.
     * For secondary services. (like web)
     * @param name
     * @param pass
     * @return true if match
     */
    public boolean isLoginInfoGood(String name, String pass);

    /**
     * Drop connection.
     * @param intentional was intentional (by user) or as a result of a problem.
     */
    void disconnect(boolean intentional);

    /**
     * Used to determine if last disconnect was intentional (by user) or as a result of a problem.
     * @return true if intentional
     */
    boolean isDisconnectIntentional();
    int getDisconnectCount();
    void incDisconnectCount();
    void resetDisconnectInfo();
    boolean isConnectionValid();

    void reconnect();
    boolean isLoggedIn();
    boolean isAutoLogin();
    void setAutoLogin(boolean set);

    /**
     * Cancel login.
     */
    void cancel();
    void setTimeout(int timeout);

    void addEventListener(ConnectionEventListener listener);
    void removeEventListener(ConnectionEventListener listener);
    /**
     * Allows one to iterate through the listeners.
     * @return iterator through the event listeners
     */
    public Iterator getEventListenerIterator();

    void addContact(Nameable contact, Group group);
    void removeContact(Nameable contact);
    void addContactGroup(Group group);
    void removeContactGroup(Group group);
    void moveContact(Nameable contact, Group group);
    void moveContact(Nameable contact, Group oldGroup, Group newGroup);
    GroupList getGroupList();
    /**
     * Returns a short display name for the service.
     * "AIM", "ICQ" etc.
     * @return service name
     */
    String getServiceName();

    /**
     * Sets the away flag.
     * @param away true if so
     */
    void setAway(boolean away);

    /**
     * Away flag.
     */
    boolean isAway();
} // class
