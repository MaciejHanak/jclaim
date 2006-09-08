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

package com.itbs.aimcer.commune.smack;

import org.jivesoftware.smack.SSLXMPPConnection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

/**
 * Provides secure Jabber connectivity.
 *
 * @author Alex Rass
 * @since Aug 31, 2005
 */
public class SecureSmackConnection extends SmackConnection {

    public SecureSmackConnection() {
        super();
        serverPort = DEFAULT_PORT_SSL;
    }

    protected XMPPConnection getNewConnection() throws XMPPException {
        return new SSLXMPPConnection(
                System.getProperty("JABBER_HOST", getServerName()),
                Integer.getInteger("JABBER_PORT_SSL", getServerPort())
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
