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
