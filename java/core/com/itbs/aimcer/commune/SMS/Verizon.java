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

import com.itbs.aimcer.commune.WebHelper;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * @author Created by Alex Rass on Apr 24, 2005
 */
public class Verizon implements SMSMessage {

    private final static String emailurl = "number@vtext.com";

    private final static String url = "http://www.vtext.com";
    private final static String action="/customer_site/jsp/disclaimer.jsp";
    private final static String number = "min";
    private final static String from = "sender";
    private final static String form = "subject";
    private final static String text = "text";
    private final static String urgent = "type";
    private final static String disclaimer = "disclaimer_submit";
    private final static int MAX_LENGTH = 150;

    private final static String actionVerify="/customer_site/jsp/status_sent_lo.jsp";
    private final static String msgIdLocator = "type=\"text\" name=\"STATUS_MSGID\" value='";
    private final static String statusDestinationLocator = "type=\"text\" name=\"STATUS_DESTNUM\" value='";

    public String sendMessage(String whoFrom, String to, String message) throws InvalidDataException, IOException {
        if (message.length() > MAX_LENGTH)
            throw new InvalidDataException("Message is too long.  145 is max.");
        String result = WebHelper.getPage(url + action,
                  number + "=" + URLEncoder.encode(to, "UTF-8") + "&"
                + "DOMAIN_NAME=@vtext.com" + "&"
                + "trackResponses=No" + "&"
                + "showgroup=n" + "&"
                + "translatorButton=" + "&"
                + "count=" + (message.length() + to.length() + whoFrom.length())+ "&"
                + form  + "=" + URLEncoder.encode("IM from: " + whoFrom, "UTF-8") + "&"
                + "callback=" + "&"
                + urgent + "=0" + "&"
                + "Send.x=Yes" + "&"
                + "Send.y=10" + "&"
                + "sender="   + "&"
                + text + "=" + URLEncoder.encode(message, "UTF-8") + "&"
                + "message=" + "&"
                + "sender="
//                + disclaimer + ".x=10" + "&"
//                + disclaimer + ".y=27" + "&"
                );
        // And now for the real result
//        GeneralUtils.sleep(900);
//        String msgID = locateString(msgIdLocator, "'>", result, 5);
//        String destID = locateString(msgIdLocator, "'>", result, 5);

        return result;
    }

    String locateString(String locator, String end, String within, int minLength) {
        int msgIDstart = within.indexOf(locator);
        if (msgIDstart > -1) {
            int msgIDend = within.indexOf("'>", msgIDstart);
            String msgID = within.substring(msgIDstart+locator.length(), msgIDend);
            if (msgID.length() > minLength) {
                return msgID;
            }
        }
        return null;
    }

    public String getInvalidSubstring() {
        return "Internal Error: Service Type not supported";
    }

    public String getEmail() {
        return emailurl;
    }

} // class
