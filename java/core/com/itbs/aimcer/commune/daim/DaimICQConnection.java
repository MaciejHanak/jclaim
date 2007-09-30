package com.itbs.aimcer.commune.daim;

import com.itbs.aimcer.bean.Message;
import org.walluck.oscar.client.Oscar;

import java.io.IOException;

/**
 * @author Alex Rass on Jul 9, 2005
 */
public class DaimICQConnection extends DaimConnection {

    public String getServiceName() {
        return "ICQ";
    }


    public void setAway(boolean away) {
        if (connection!=null) {
            try {
                oscar.setAwayICQ(connection, away?getProperties().getIamAwayMessage():null);
            } catch (IOException e) {
                notifyErrorOccured("Failed to set away.", e);
            }
        }
        super.setAway(away);
    }
    public void processMessage(Message message) {
        try {
            oscar.sendIM(connection, message.getContact().getName(), message.getText(), Oscar.getICQCaps());
        } catch (IOException e) {
            notifyErrorOccured("Failed to send", e);
        }
    }

}
