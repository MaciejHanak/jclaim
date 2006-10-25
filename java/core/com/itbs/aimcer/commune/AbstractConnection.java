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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements basic things one needs to support a connection.
 * Most of these will be useful to pretty much everyone.
 *
 * @author Alex Rass
 * @since Oct 10, 2004
 */
abstract public class AbstractConnection implements Connection {
    private static Logger logger = Logger.getLogger(AbstractConnection.class.getName());
    protected List<ConnectionEventListener> eventHandlers = new  CopyOnWriteArrayList<ConnectionEventListener>();
    private boolean autoLogin;
    protected boolean disconnectIntentional;
    private int disconnectCount;
    private ConnectionProperties properties;
    private ContactFactory contactFactory;
    private GroupFactory groupFactory;

    // ********************   General stuff   ********************

    /**
     * Allows one to check credentials withoout looking in.
     * For secondary services. (like web)
     *                         N
     * @param name username to try
     * @param pass password to try
     * @return true if match
     */
    public boolean isLoginInfoGood(String name, String pass) {
        return false;
    }

    /**
     * Bean property to assist in automatic logins
     * @return true if so
     */
    public boolean isAutoLogin() {
        return autoLogin;
    }

    /**
     * Bean property to assist in automatic logins
     * @param autoLogin if so
     */
    public void setAutoLogin(boolean autoLogin) {
        this.autoLogin = autoLogin;
    }

    public boolean isDisconnectIntentional() {
        return disconnectIntentional;
    }

    public int getDisconnectCount() {
        return disconnectCount;
    }

    public void incDisconnectCount() {
        disconnectCount ++;
    }

    public void resetDisconnectInfo() {
        disconnectCount = 0;
        disconnectIntentional = false;
    }

    public void disconnect(boolean intentional) {
        if (intentional)
            disconnectIntentional = true; // one way switch b/c we'll get called by finalization stuff
        notifyConnectionLost();
        System.gc();
    }

    public void connect() throws Exception {
        if (groupFactory == null)
            throw new NullPointerException("Programmer, you forgot to assign the groupFactory for the connection.");
        if (contactFactory == null)
            throw new NullPointerException("Programmer, you forgot to assign the contactFactory for the connection.");
        if (getProperties() == null) { // things may still work, so just warn.
            logger.warning("Please setProperties on connection prior to use.");
        }

//        disconnectIntentional = false; // seems more appropriate in the notify on connection this way menu disconnect will stop it
    }
    // ********************   Group List   ********************

    private static GroupList groupList  = new GroupList() {
        private List<Group> arrayList = new CopyOnWriteArrayList<Group>();
        public int size() {
            return arrayList.size();
        }

        public Group get(int index) {
            return arrayList.get(index);
        }

        public Group add(Group group) {
            if (!arrayList.contains(group)) // no duplicates
                arrayList.add(group);
            return group;
        }

        public void remove(Group group) {
            arrayList.remove(group);
        }

        public Object[] toArray() {
            return arrayList.toArray();
        }

        public void clear() {
            arrayList.clear();
        }
    };

    public GroupList getGroupList() {
        return groupList;
    }

    /**
     * Moves contact from one group to another.
     * Anyone who can do a better job
     * @param contact  to move
     * @param group destination group
     */
    public void moveContact(Nameable contact, Group group) {
        removeContact(contact);
        addContact(contact, group);
    }

    // ********************   Event Listeners   ********************

    public void removeEventListener(ConnectionEventListener listener) {
        eventHandlers.remove(listener);
    }

    public void addEventListener(ConnectionEventListener listener) {
        eventHandlers.remove(listener); // make sure old one is dead
        eventHandlers.add(listener);
    }

    public Iterator <ConnectionEventListener> getEventListenerIterator() {
        return eventHandlers.iterator();
    }

    /**
     * Tells everyone connection was initiated.
     */
    protected void notifyConnectionInitiated() {
        Iterator <ConnectionEventListener >iter = getEventListenerIterator();
        while (iter.hasNext()) {
            iter.next().connectionInitiated(this);
        }
    }

    /**
     * Tells everyone connection was established.
     */
    protected void notifyConnectionEstablished() {
        Iterator <ConnectionEventListener >iter = getEventListenerIterator();
        while (iter.hasNext()) {
            iter.next().connectionEstablished(this);
        }
    }

    /**
     * Tells everyone connection was lost.
     */
    protected void notifyConnectionLost() {
        Iterator <ConnectionEventListener >iter = getEventListenerIterator();
        while (iter.hasNext()) {
            try {
                iter.next().connectionLost(this);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Should not be failing on logout.", e); 
            }
        }
    }
    /**
     * Tells everyone connection was lost.
     * @param message message to pass
     */
    protected void notifyConnectionFailed(String message) {
        Iterator <ConnectionEventListener >iter = getEventListenerIterator();
        while (iter.hasNext()) {
            try {
                iter.next().connectionFailed(this, message);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failure while notifying listeners of connectionFailure", e);
            }
        }
    }
    /**
     * Tells everyone connection was lost.
     * @param message message to pass
     */
    protected void notifyErrorOccured(String message, Exception exception) {
        Iterator <ConnectionEventListener >iter = getEventListenerIterator();
        while (iter.hasNext()) {
            try {
                iter.next().errorOccured(message, exception);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failure while notifying listeners of connectionFailure", e);
            }
        }
    }
    // ********************   Icons   ********************

    public final void setProperties(ConnectionProperties properties) {
        this.properties = properties;
    }

    public ConnectionProperties getProperties() {
        return properties;
    }

    public void assignContactFactory(ContactFactory factory) {
        contactFactory = factory;
    }

    public ContactFactory getContactFactory() {
        return contactFactory;
    }

    public GroupFactory getGroupFactory() {
        return groupFactory;
    }

    public void assignGroupFactory(GroupFactory groupFactory) {
        this.groupFactory = groupFactory;
    }
}
