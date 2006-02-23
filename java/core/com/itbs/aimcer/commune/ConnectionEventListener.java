package com.itbs.aimcer.commune;

import com.itbs.aimcer.bean.Contact;
import com.itbs.aimcer.bean.Message;
import com.itbs.aimcer.bean.Nameable;

/**
 * @author Alex Rass
 * @since Sep 22, 2004
 */
public interface ConnectionEventListener {
    /**
     * Sent before connection is attempted
     * @param connection in context
     */
    void connectionInitiated(Connection connection);
    /**
     * Recevied a message.
     * Called with incoming and outgoing messages from any connection.
     * @param connection connection
     * @param message message
     * @return false if noone else needs to see this.
     */
    boolean messageReceived(MessageSupport connection, Message message) throws Exception;
    
    boolean emailReceived(MessageSupport connection, Message message) throws Exception;

    /**
     * Tells that other side is typing a message.
     * @param connection on which the notification is sent
     */
    void typingNotificationReceived(MessageSupport connection, Nameable contact);

    /**
     * Connection with server was interrupted.
     * This is a failure to maintain a connection.
     * @param connection itself
     */
    void connectionLost(Connection connection);

    /**
     * Connection with server has failed.
     * This is a Failure to connect.
     * @param connection itself
     * @param message to display
     */
    void connectionFailed(Connection connection, String message);

    /**
     * Indicates we are now logged in and can start using the connection.
     * Place to run connection-related properties etc.
     * @param connection that has finished stabilizing
     */
    void connectionEstablished(Connection connection);

    /**
     * Nameable's status changed.
     * @param contact contact
     * @param idleMins
     */
    void statusChanged(Connection connection, Contact contact, boolean online, boolean away, int idleMins);

    /**
     * Statuses for contacts that belong to this connection have changed.
     * @param connection connection
     */
    void statusChanged(Connection connection);

    /**
     * A previously requested icon has arrived.
     * Icon will be a part of the contact.
     *
     * @param connection connection
     * @param contact contact
     */
    void pictureReceived(IconSupport connection, Contact contact);

    /**
     * Other side requested a file transfer.
     * @param connection connection
     * @param contact
     * @param filename
     * @param description
     * @param connectionInfo
     */
    void fileReceiveRequested(FileTransferSupport connection, Contact contact, String filename, String description, Object connectionInfo);

    /**
     * Gets called when an assynchronous error occurs.
     * @param message to display
     * @param exception exception for tracing
     */
    void errorOccured(String message, Exception exception);

    public boolean contactRequestReceived(final String user, final MessageSupport connection);
}
