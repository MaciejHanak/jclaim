package com.itbs.aimcer.commune.desktop;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

/**
 * @author Alex Rass
 * @since 1/15/12 5:52 PM
 */
public class Message {
    String to, subject, body;

    public Message(String to) {
        this(to,  null, null);
    }
    public Message(String to, String subject) {
        this(to, subject, null);
    }
    public Message(String to, String subject, String body) {
        this.to = to;
        if (subject!=null)
        try {
            this.subject = URLEncoder.encode(subject, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // subject not set
        }
        if (body!=null)
        try {
            this.body = URLEncoder.encode(body, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // subject not set
        }
    }

    public static URI getURI(String to, String subject, String body) throws URISyntaxException {
        return new Message(to, subject,  body).getURI();
    }

    public URI getURI() throws URISyntaxException {
        return new URI("mailto:"+
                to+
                subject==null?"":subject+
                body==null?"":body);
    }
}
