package com.itbs.aimcer.commune;

import com.itbs.aimcer.bean.Message;

/**
 * Methods which are used (as callbacks) by the Group Chat support.
 * 
 * @author Alex Rass
 * @since Oct 10, 2005
 */
public interface ChatRoomEventListener {
    /**
     * Server based notify events.
     * @param text to show to the user.
     */
    void serverNotification(String text);

    /**
     * Recevied a message.
     * Called with incoming and outgoing messages from any connection.
     * @param connection connection
     * @param message message
     * @return false if noone else needs to see this.
     */
    boolean messageReceived(ChatRoomSupport connection, Message message);

    /**
     * Gets called when an assynchronous error occurs.
     * @param message to display
     * @param exception exception for tracing
     */
    void errorOccured(String message, Exception exception);
    
}
