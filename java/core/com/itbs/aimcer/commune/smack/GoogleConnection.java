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
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;

/**
 * Provides implementation differences for GoogleTalk.
 * 
 * @author Alex Rass
 * @since Aug 31, 2005
 */
public class GoogleConnection extends SmackConnection {
    protected XMPPConnection getNewConnection() throws XMPPException {
        ConnectionConfiguration configuration = new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
        return new XMPPConnection(configuration);
    }

    /**
     * Previously removed the @* trailer.  Now just calls super. --aa
     * Stop stripping @* part from username to allow logins other than @google to login.
     * @param userName to adjust/assign
     */
    public void setUserName(String userName) {
        //int index = userName==null?-1:userName.indexOf('@');
       // super.setUserName(index==-1?userName:userName.substring(0, index));
        super.setUserName(userName);
    }

    public void addContact(Nameable contact, Group group) {
        super.addContact(contact, group);
    }

    /**
     * Used to fix the usernames for the jabber protocol.
     * Usernames need server name.
     * @param name of the user's account to fix
     * @return name, including server.
     */
    protected String fixUserName(String name) {
        if (name==null || name.indexOf('@')>-1) return name;
        return name + "@gmail.com";
    }

    @Override
    /**
     * Just calls the parent if there's content to the message. Otherwise - don't bother.
     */
    protected void processSmackPacket(Packet packet) {
        boolean hasContent;
        if (packet instanceof org.jivesoftware.smack.packet.Message) {
            org.jivesoftware.smack.packet.Message smackMessage = (org.jivesoftware.smack.packet.Message) packet;
            hasContent = smackMessage.getBody().length()>0;
        } else {
            hasContent = ((String) packet.getProperty("body")).length()>0;
        }
        if (hasContent) {
            super.processSmackPacket(packet);
        }
    }

    public String getUserName() {
        return fixUserName(super.getUserName()); 
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
