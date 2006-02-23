package com.itbs.aimcer.commune;

import com.itbs.aimcer.bean.*;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Created by Alex Rass
 * @since Oct 10, 2004
 */
abstract public class AbstractConnection implements Connection {
    protected List<ConnectionEventListener> eventHandlers = new  CopyOnWriteArrayList<ConnectionEventListener>();
    private boolean autoLogin;
    protected boolean disconnectIntentional;
    protected int disconnectCount;
    private ConnectionProperties properties;
    private ContactFactory contactFactory;
    private GroupFactory groupFactory;

    // ********************   General stuff   ********************

    /**
     * Allows one to check credentials withoout looking in.
     * For secondary services. (like web)
     *                         N
     * @param name
     * @param pass
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

    public void disconnect(boolean intentional) {
        if (intentional)
            disconnectIntentional = true; // one way switch b/c we'll get called by finalization stuff
        disconnectCount++;
        for (ConnectionEventListener connectionEventListener : eventHandlers) {
            connectionEventListener.connectionLost(this);
        }
        if (!disconnectIntentional && disconnectCount < getProperties().getDisconnectCount()) {
            new Thread("Reconnect") {
                public void run() {
                    try {
                        sleep(60*1000);
                        if (!disconnectIntentional && disconnectCount < getProperties().getDisconnectCount() && !isLoggedIn()) {
                            System.out.println("Trying to reconnecting " + getServiceName() + " for " + disconnectCount + " time.");
                            reconnect();
                        }
                    } catch (InterruptedException e) { //
                    }
                }
            }.start();
        }
    }

    public void connect() throws SecurityException, Exception {
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
     * @param contact
     * @param group
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

    protected void notifyConnectionInitiated() {
        Iterator <ConnectionEventListener >iter = getEventListenerIterator();
        while (iter.hasNext()) {
            iter.next().connectionInitiated(this);
        }
    }
    protected void notifyConnectionEstablished() {
        disconnectCount = 0;
        disconnectIntentional = false;
        Iterator <ConnectionEventListener >iter = getEventListenerIterator();
        while (iter.hasNext()) {
            iter.next().connectionEstablished(this);
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
