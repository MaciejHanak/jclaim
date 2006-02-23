package com.itbs.aimcer.bean;

/**
 * @author Alex Rass
 * @since Sep 22, 2004
 */
public interface Message {
    boolean isOutgoing();
    void setOutgoing(boolean out);
    String getText();
    String getPlainText();
    boolean isAutoResponse();
    Nameable getContact();
}
