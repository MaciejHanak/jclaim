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

package com.itbs.aimcer.commune.joscar;

import com.itbs.aimcer.bean.Message;
import net.kano.joustsim.Screenname;
import net.kano.joustsim.oscar.oscar.service.icbm.Conversation;
import net.kano.joustsim.oscar.oscar.service.icbm.SimpleMessage;

/**
 * @author Alex Rass on Jul 9, 2005
 */
public class ICQConnection extends OscarConnection {

    {
        connectionProperties.setLoginHost("login.icq.com");
    }
    
    public String getServiceName() {
        return "ICQ";
    }

    // todo when offline, getIcbmService will return null
    public void processMessage(Message message) {
        Conversation conversation = connection.getIcbmService().getImConversation(new Screenname(message.getContact().getName()));
        // the offline thing is to send properly in offline.
        SimpleMessage actualMessage = new SimpleMessage(message.getText(), message.isAutoResponse() || !message.getContact().getStatus().isOnline());
        conversation.sendMessage(actualMessage);
    }
}
