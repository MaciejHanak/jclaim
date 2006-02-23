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
