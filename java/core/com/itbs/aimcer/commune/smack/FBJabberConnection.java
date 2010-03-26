package com.itbs.aimcer.commune.smack;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import com.itbs.aimcer.commune.smack.SmackConnection;

/**
 * Provides implementation differences for Facebook chat.
 * 
 * @author aalliana, psanta
 * @since March 25, 2010
 */
public class FBJabberConnection extends SmackConnection {
	
    protected XMPPConnection getNewConnection() throws XMPPException {
    	
    	ConnectionConfiguration cc = new ConnectionConfiguration("chat.facebook.com", 5222);
    	cc.setSASLAuthenticationEnabled(true);
    	return new XMPPConnection(cc);    	
    }

    @Override
    protected void setAuthenticationMethod()
    {
        SASLAuthentication.registerSASLMechanism("DIGEST-MD5", FBSASLDigestMD5Mechanism.class);
        SASLAuthentication.supportSASLMechanism("DIGEST-MD5", 0);
    }


    public String getServiceName() {
        return "Facebook";
    }

    
}
