package com.itbs.aimcer.commune;

import com.itbs.aimcer.bean.*;

import java.util.Iterator;

/**
 * This is the main interface for all Connections.
 *
 * @author Created by Alex Rass
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
    void disconnect(boolean intentional);
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
}
