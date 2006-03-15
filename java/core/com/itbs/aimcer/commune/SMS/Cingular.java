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
 * Provides a way to page cingular users (US).
 *
 * @author Alex Rass
 * @since Apr 24, 2005
 */
public class Cingular implements SMSMessage {

    private final static String emailurl = "number@mobile.mycingular.com";

    private final static String url = "http://www.cingularme.com";
    private final static String form = "publicForm";
    private final static String action="/do/public/send;jsessionid=aBGI8AYmwFyd";
    private final static String number = "min";
    private final static String from = "from";
    private final static String text = "msg";
    private final static String hidden = "priority";// value="Normal"
    private final static int MAX_LENGTH = 145;

    public String sendMessage(String whoFrom, String to, String message) throws InvalidDataException, IOException {
        if (message.length() > MAX_LENGTH)
            throw new InvalidDataException("Message is too long.  145 is max.");
        String result = WebHelper.getPage(url + action,
                  number + "=" + URLEncoder.encode(to, "UTF-8") + "&"
                + from + "=" + URLEncoder.encode(whoFrom, "UTF-8") + "&"
                + hidden + "=Normal" + "&"
                + text + "=" + URLEncoder.encode(message, "UTF-8")
                );
        return result;
    }

    public String getInvalidSubstring() {
        return "is invalid recipient";
    }

    public String getEmail() {
        return emailurl;
    }
}
