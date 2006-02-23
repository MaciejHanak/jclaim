package com.itbs.aimcer.commune.SMS;

import com.itbs.util.GeneralUtils;

import java.io.IOException;

/**
 * @author Alex Rass on May 28, 2005
 */
public class SMSWrapper {
    private SMSWrapper() {
    }
    public static final SMSMessage TMOBILE = new TMobile();
    public static final SMSMessage CINGULAR = new Cingular();
    public static final SMSMessage VERISON = new Verizon();

    /**
     * Sends message via any available media.
     * @param whoFrom who from?
     * @param to whom
     * @param message to send
     * @return  true whem message appeared to be sent
     * @throws InvalidDataException
     * @throws IOException
     */
    public static boolean sendMessage(String whoFrom, String to, String message) throws InvalidDataException, IOException {
        if (GeneralUtils.isNotEmpty(whoFrom) && GeneralUtils.isNotEmpty(to) && GeneralUtils.isNotEmpty(message)) {
            String result;
            result = CINGULAR.sendMessage(whoFrom, to, message); // order is important.  Cingular is less restrictive
            if (result.indexOf(CINGULAR.getInvalidSubstring()) > -1) {
                result = TMOBILE.sendMessage(whoFrom, to, message);
                if (result.indexOf(TMOBILE.getInvalidSubstring()) > -1) {
                    result = VERISON.sendMessage(whoFrom, to, message);
                    if (result.indexOf(VERISON.getInvalidSubstring()) > -1) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            throw new InvalidDataException("Missing required parameter.");
        }
    }
}
