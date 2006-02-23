package com.itbs.aimcer.bean;

import com.itbs.util.GeneralUtils;

/**
 * @author Alex Rass
 * Copyright 2004
 */
final public class MessageImpl implements Message {
    boolean outgoing, autoresponse;
    String text, plainText;
    Nameable name;

    public MessageImpl(Nameable name, boolean outgoing, String text) {
        this.outgoing = outgoing;
        this.text = text;
        this.name = name;
    }

    public MessageImpl(Nameable name, boolean outgoing, boolean autoresponse, String text) {
        this.outgoing = outgoing;
        this.autoresponse = autoresponse;
        this.text = text;
        this.name = name;
    }


    public boolean isOutgoing() {
        return outgoing;
    }

    public void setOutgoing(boolean out) {
        outgoing = out;
    }

    public String getText() {
        return text;
    }

    public String getPlainText() {
        if (plainText == null)
          plainText = GeneralUtils.stripHTML(text);
        return plainText;
    }

    public boolean isAutoResponse() {
        return autoresponse;
    }

    public Nameable getContact() {
        return name;
    }

    public String toString() {
        return name.getName() + " " + outgoing + " " + autoresponse + " [" + text + "]";
    }
}
