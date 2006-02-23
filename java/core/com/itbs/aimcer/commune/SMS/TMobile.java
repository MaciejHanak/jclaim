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
public class TMobile implements SMSMessage {

    private final static String emailurl = "???";

    private final static String url = "http://www.t-mobile.com/messaging/";
    private final static String form = "frmSMS";
    private final static String action="default.asp";
    private final static String number = "txtNum";
    private final static String from = "txtFrom";
    private final static String text = "txtMessage";
    private final static String hidden = "hdnpublic";// value="1"
    private final static String agreeCheck = "msgTermsUse";
    private final static int MAX_LENGTH = 140;

    public String sendMessage(String whoFrom, String to, String message) throws InvalidDataException, IOException {
        if (message.length() > MAX_LENGTH)
            throw new InvalidDataException("Message is too long.  140 is max.");
        String result = WebHelper.getPage(url + action + "?To=" + URLEncoder.encode(to, "UTF-8"),
                  number + "=" + URLEncoder.encode(to, "UTF-8") + "&"
                + from + "=" + URLEncoder.encode(whoFrom, "UTF-8") + "&"
                + hidden + "=1" + "&"
                + agreeCheck + "=true" + "&"
                + text + "=" + URLEncoder.encode(message, "UTF-8")
                );
        return result;
    }

    public String getInvalidSubstring() {
        return "Your message could not be delivered to ";
    }

    public String getEmail() {
        return emailurl;
    }
}
