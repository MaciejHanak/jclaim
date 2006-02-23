package com.itbs.aimcer.commune.smack;

import org.jivesoftware.smack.GoogleTalkConnection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

/**
 * @author Alex Rass
 * @since Aug 31, 2005
 */
public class GoogleConnection extends SmackConnection {
    protected XMPPConnection getNewConnection() throws XMPPException {
        return new GoogleTalkConnection();
    }

    /**
     * Returns a short name for the service.
     * "AIM", "ICQ" etc.
     *
     * @return service name
     */
    public String getServiceName() {
        return "GoogleTalk"; 
    }
}
