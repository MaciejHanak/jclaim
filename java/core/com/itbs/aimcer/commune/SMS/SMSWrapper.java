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

package com.itbs.aimcer.commune.SMS;

import com.itbs.aimcer.bean.ContactWrapper;
import com.itbs.aimcer.commune.Connection;
import com.itbs.aimcer.commune.SMSSupport;
import com.itbs.aimcer.gui.MessageWindow;
import com.itbs.util.GeneralUtils;

import java.io.IOException;
import java.util.List;

/**
 * @author Alex Rass on May 28, 2005
 */
public class SMSWrapper {
    private SMSWrapper() {
    }
    public static final SMSMessage TMOBILE = new TMobile();
    public static final SMSMessage CINGULAR = new Cingular();
    public static final SMSMessage VERISON = new Verizon();

    /**
     * Sends message via any available media.
     * @param conns All available connections to try (can be null)
     * @param conn Preferred connection to first use. Used to get From preferences. (can not be null)
     * @param to whom
     * @return  true whem message appeared to be sent
     * @throws InvalidDataException when number is wrong
     */
    public static void sendMessage(List<Connection> conns, Connection conn, ContactWrapper to) throws InvalidDataException {
        if (GeneralUtils.isNotEmpty(conn.getUser().getName()) && GeneralUtils.isNotEmpty(to.getPreferences().getPhone())) {
            String result="";
            boolean found = false;
            if (conn instanceof SMSSupport && conn.isLoggedIn()) {
                result = ((SMSSupport)conn).veryfySupport(to.getPreferences().getPhone());
                if (result == null) {
                    found = true;
                    ContactWrapper cw = ContactWrapper.create(to.getPreferences().getPhone(), conn);
                    cw.getPreferences().setName(to.getPreferences().getName());
                    cw.getPreferences().setDisplayName(to.getPreferences().getDisplayName() + " (Phone)");
                    MessageWindow.openWindow(cw, true);
                }
            }
            if (!found && conns!=null) { // Search
                String tempResult;
                for (Connection connection : conns) {
                    if (connection instanceof SMSSupport){
                        if (!connection.isLoggedIn()) {
                            result += "Connection " + connection.getUser().getName() + " on " + connection.getServiceName() + " can't be used (not logged in).\n";
                            continue;
                        }
                        tempResult = ((SMSSupport)connection).veryfySupport(to.getPreferences().getPhone());
                        if (tempResult!=null) {
                            result += tempResult + "\n";
                            continue;
                        }
                        found = true;
                        ContactWrapper cw = ContactWrapper.create(to.getPreferences().getPhone(), connection);
                        cw.getPreferences().setName(to.getPreferences().getName());
                        cw.getPreferences().setDisplayName(to.getPreferences().getDisplayName() + " (Phone)");
                        MessageWindow.openWindow(cw, true);
                    }
                }
                if (!found) {
                    throw new InvalidDataException("Failed to send for any one of the following reasons:\n" + result);
                }
            }
        }
        throw new InvalidDataException("Missing required parameter.");
    }

    /**
     * Sends message via any available media.
     * @param whoFrom who from?
     * @param to whom
     * @param message to send
     * @return  true whem message appeared to be sent
     * @throws InvalidDataException number problems
     * @throws IOException sending problems
     */
    public static boolean sendMessage(String whoFrom, String to, String message) throws InvalidDataException, IOException {
        if (GeneralUtils.isNotEmpty(whoFrom) && GeneralUtils.isNotEmpty(to) && GeneralUtils.isNotEmpty(message)) {
            String result;
            result = CINGULAR.sendMessage(whoFrom, to, message); // order is important.  Cingular is less restrictive
            if (result.indexOf(CINGULAR.getInvalidSubstring()) > -1) {
                result = TMOBILE.sendMessage(whoFrom, to, message);
                if (result.indexOf(TMOBILE.getInvalidSubstring()) > -1) {
                    result = VERISON.sendMessage(whoFrom, to, message);
                    if (result.indexOf(VERISON.getInvalidSubstring()) > -1) {
                        return false;
                    }
                }
            }
            return true;
        }
        throw new InvalidDataException("Missing required parameter.");
    }
}
