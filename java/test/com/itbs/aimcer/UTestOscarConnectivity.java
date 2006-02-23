package com.itbs.aimcer;

import junit.framework.TestCase;
import net.kano.joustsim.Screenname;
import net.kano.joustsim.oscar.*;
import net.kano.joustsim.oscar.oscar.service.icbm.*;

/**
 * Created by  Administrator on Sep 21, 2004
 */
public class UTestOscarConnectivity extends TestCase {
    protected void setUp() throws Exception {

    }

    protected void tearDown() throws Exception {
    }

    public void testConnection() throws Exception {
        final Screenname screenName = new Screenname("yourbudyalex");

        AppSession appSession = new DefaultAppSession();
        AimSession session = appSession.openAimSession(screenName);
        AimConnection connection = session.openConnection(new AimConnectionProperties(screenName, "sashaaim"));
        connection.addStateListener(new StateListener() {
            public void handleStateChange(StateEvent event) {
                System.out.println("state changed " + event.getNewState());
            }
        });
        connection.connect();

        IcbmService icbmService = connection.getIcbmService();
        icbmService.addIcbmListener(new IcbmListener() {
            public void newConversation(IcbmService service, Conversation conv) {
                System.out.println("new conversation with " + conv.getBuddy());
            }

            public void buddyInfoUpdated(IcbmService service, Screenname buddy, IcbmBuddyInfo info) {
                System.out.println("info changed");
            }
        });
        ImConversation conversation = icbmService.getImConversation(screenName);
        conversation.sendMessage(new SimpleMessage("blah"));
        Thread.sleep(20000);
        connection.disconnect(true);

    }

}
