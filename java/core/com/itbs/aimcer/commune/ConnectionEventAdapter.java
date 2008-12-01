package com.itbs.aimcer.commune;

import com.itbs.aimcer.bean.Contact;
import com.itbs.aimcer.bean.Message;
import com.itbs.aimcer.bean.Nameable;
import com.itbs.aimcer.bean.Status;

/**
 * Helper class for ConnectionEventListener.
 * <p>
 *
 * Simplifies and cleans up implementation of ConnectionEventListener.
 *
 * @author Alex Rass
 * @since Dec 1
 */
public class ConnectionEventAdapter implements ConnectionEventListener {
    public void connectionInitiated(Connection connection) {
    }

    public boolean messageReceived(MessageSupport connection, Message message) throws Exception {
        return true;
    }

    public boolean emailReceived(MessageSupport connection, Message message) throws Exception {
        return true;
    }

    public void typingNotificationReceived(MessageSupport connection, Nameable contact) {
    }

    public void connectionLost(Connection connection) {
    }

    public void connectionFailed(Connection connection, String message) {
    }

    public void connectionEstablished(Connection connection) {
    }

    public void statusChanged(Connection connection, Contact contact, Status oldStatus) {
    }

    public void statusChanged(Connection connection) {
    }

    public void pictureReceived(IconSupport connection, Contact contact) {
    }

    public void fileReceiveRequested(FileTransferSupport connection, Contact contact, String filename, String description, Object connectionInfo) {
    }

    public void errorOccured(String message, Exception exception) {
    }

    public boolean contactRequestReceived(final String user, final MessageSupport connection) {
        return true;
    }
}
