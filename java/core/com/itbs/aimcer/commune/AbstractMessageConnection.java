package com.itbs.aimcer.commune;

import com.itbs.aimcer.bean.Message;

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
    private boolean away;
    private Executor executor = Executors.newSingleThreadScheduledExecutor();

    /** These are for restoring connection info */

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
