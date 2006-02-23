package com.itbs.aimcer.commune;

import com.itbs.aimcer.bean.Message;
import com.itbs.aimcer.bean.Nameable;

import javax.swing.text.html.HTMLEditorKit;
import java.io.IOException;

/**
 * Describes a collection which offers message support.
 * 
 * @author Alex Rass
 * @since Apr 24, 2005
 */
public interface MessageSupport extends Connection {
    public String getUserName();
    public void setUserName(String userName);
    void setPassword(String password);
    String getPassword();

    void sendMessage(Message message);
    void sendTypingNotification();

    boolean isSecureMessageSupported();

    void sendSecureMessage(Message message) throws IOException;

    /**
     * True if this is a system message.
     * @param contact to check
     * @return true if a system message
     */
    boolean isSystemMessage(Nameable contact);

    /**
     * Returns support Account
     * @return support account or null
     */
    String getSupportAccount();

    /**
     * Which editor kit to use to display the incomming content.
     * For future use.
     * @return editor kit
     */
    public HTMLEditorKit getEditorKit();
}
