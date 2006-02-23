package com.itbs.aimcer.commune.joscar;

import com.itbs.aimcer.bean.Message;
import net.kano.joustsim.Screenname;
import net.kano.joustsim.oscar.oscar.service.icbm.Conversation;
import net.kano.joustsim.oscar.oscar.service.icbm.SimpleMessage;

/**
 * @author Alex Rass on Jul 9, 2005
 */
public class ICQConnection extends OscarConnection {

    public String getServiceName() {
        return "ICQ";
    }

    // todo when offline, getIcbmService will return null
    public void processMessage(Message message) {
        Conversation conversation = connection.getIcbmService().getImConversation(new Screenname(message.getContact().getName()));
        conversation.sendMessage(new  SimpleMessage(message.getText(), message.isAutoResponse()));
    }
}
