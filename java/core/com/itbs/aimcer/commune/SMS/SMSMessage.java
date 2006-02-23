package com.itbs.aimcer.commune.SMS;

import java.io.IOException;

/**
 * @author Alex Rass on May 28, 2005
 */
public interface SMSMessage {
    String sendMessage(String whoFrom, String to, String message) throws InvalidDataException, IOException;
    String getInvalidSubstring();
    String getEmail();
}
