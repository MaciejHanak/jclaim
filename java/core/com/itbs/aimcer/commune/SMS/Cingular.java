package com.itbs.aimcer.commune.SMS;

import com.itbs.aimcer.commune.WebHelper;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * @author Created by Alex Rass on Apr 24, 2005
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
