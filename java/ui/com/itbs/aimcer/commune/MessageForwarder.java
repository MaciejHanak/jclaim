package com.itbs.aimcer.commune;

import com.itbs.aimcer.bean.Contact;
import com.itbs.aimcer.bean.Message;
import com.itbs.aimcer.bean.MessageImpl;

/**
 * Provides ability to forward messages to another contact.
 * @author Alex Rass
 * @since Oct 29, 2007 10:41:17 PM
 */
public class MessageForwarder extends ConnectionEventAdapter {
    private Contact forwardContact;
    private static final int MAX_LEN = 125; // tons of characters are gone due to fillers.

    public void setForwardContact(Contact forwardContact) {
        this.forwardContact = forwardContact;
    }

    public Contact getForwardContact() {
        return forwardContact;
    }

    /**
     * Only method that really does anything. And that's forward the IMs to someone who we picked earlier.
     * @param connection message is received on.
     * @param message received
     * @return always false since we never want to interfere with others.
     * @throws Exception if smth blew up.
     */
    public boolean messageReceived(MessageSupport connection, Message message) throws Exception {
//            incoming message         not and auto-response        forwarder is set        and connection for it is good                       and supports messages                                 and message is not from a forwardee
        if (!message.isOutgoing() && !message.isAutoResponse() && forwardContact != null && forwardContact.getConnection().isLoggedIn() && forwardContact.getConnection() instanceof MessageSupport && !message.getContact().equals(forwardContact)) {
            // recompose using new data.
            Contact cw = connection.getContactFactory().get(message.getContact().toString(), connection);

            String newText = ((cw!=null)?cw.getDisplayName():message.getContact()) + ": " + message.getPlainText();
            Message newMessage = new MessageImpl(forwardContact, true, newText.substring(0, Math.min(MAX_LEN, newText.length())));
            ((MessageSupport)(forwardContact.getConnection())).sendMessage(newMessage);
        }
        return false; // always stays false.
    }

    public boolean emailReceived(MessageSupport connection, Message message) throws Exception {
        if (forwardContact != null && forwardContact.getConnection().isLoggedIn() && forwardContact.getConnection() instanceof MessageSupport) {
            String newText = message.getPlainText();
            Message newMessage = new MessageImpl(forwardContact, true, newText.substring(0, Math.min(MAX_LEN, newText.length())));
            ((MessageSupport)(forwardContact.getConnection())).sendMessage(newMessage);
        } // if
        return false;
    } // func

}
