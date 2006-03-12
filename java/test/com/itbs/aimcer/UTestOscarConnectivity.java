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

package com.itbs.aimcer;

import junit.framework.TestCase;
import net.kano.joustsim.Screenname;
import net.kano.joustsim.oscar.*;
import net.kano.joustsim.oscar.oscar.service.icbm.*;

/**
 * @author Alex Rass
 * @since Sep 21, 2004
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
