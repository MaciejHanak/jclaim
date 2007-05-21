package com.itbs.aimcer.commune;

/**
 * @author Alex Rass
 * @since May 21, 2007 1:21:45 PM
 */
public interface SMSSupport {
    /**
     * Provides a way to check if id (number) is supported.
     *
     * @param id to verify
     * @return null if this id is acceptable.  Otherwise explanation why it isn't.
     */
    String veryfySupport(String id);
//    String sendSMSMessage(Message message);
}
