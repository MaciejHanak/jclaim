package com.itbs.aimcer.commune.smack;

import org.jivesoftware.smack.SSLXMPPConnection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

/**
 * @author Alex Rass
 * @since Aug 31, 2005
 */
public class SecureSmackConnection extends SmackConnection {
    protected XMPPConnection getNewConnection() throws XMPPException {
        return new SSLXMPPConnection(
                System.getProperty("JABBER_HOST", DEFAULT_HOST),
                Integer.getInteger("JABBER_PORT_SSL", DEFAULT_PORT_SSL)
                );
    }

    /**
     * Returns a short name for the service.
     * "AIM", "ICQ" etc.
     *
     * @return service name
     */
    public String getServiceName() {
        return "SSL Jabber";
    }
}
