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

import com.itbs.aimcer.bean.Group;
import com.itbs.aimcer.bean.Nameable;
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

    public void addContact(Nameable contact, Group group) {
        super.addContact(contact, group);    //Todo change
    }

    /**
     * Used to fix the usernames for the jabber protocol.
     * Usernames need server name.
     * @param name of the user's account to fix
     * @return name, including server.
     */
    protected String fixUserName(String name) {
        if (name.indexOf('@')>-1) return name;
        return name + "@gmail.com";
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
