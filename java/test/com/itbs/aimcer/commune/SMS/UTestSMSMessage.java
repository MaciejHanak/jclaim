package com.itbs.aimcer.commune.SMS;

import junit.framework.TestCase;

/**
 * @author Created by  Administrator on Apr 24, 2005
 */
public class UTestSMSMessage extends TestCase {
    public void testPost() throws Exception {
        final SMSMessage pager = new TMobile();
        String result = pager.sendMessage("Service", "7325555678", "The service will be unavailable for 5 minutes.");
        assertTrue("did not find proper delivery failure method:", (result.indexOf(pager.getInvalidSubstring()) > -1));
    }
    public void testPostCingular() throws Exception {
//        String result = Cingular.sendMessage("Service", "7323069053", "The service will be unavailable for 5 minutes.");
//        assertTrue("did not find proper delivery info:", (result.indexOf("Your message has been submitted for delivery. To send another message, select Reset to clear the fields below.") > -1));
        final SMSMessage pager = new Cingular();
        String result = pager.sendMessage("Service", "7325555678", "The service will be unavailable for 5 minutes.");
        assertTrue("did not find proper delivery failure method:", (result.indexOf(pager.getInvalidSubstring()) > -1));
    }
    public void testPostVerizon() throws Exception {
//        String result = Cingular.sendMessage("Service", "7323069053", "The service will be unavailable for 5 minutes.");
//        assertTrue("did not find proper delivery info:", (result.indexOf("Your message has been submitted for delivery. To send another message, select Reset to clear the fields below.") > -1));
        final SMSMessage pager = new Verizon();
//        String result = pager.sendMessage("Service", "7325555678", "The service will be unavailable for 5 minutes.");
        String result = pager.sendMessage("Alex", "7327542153", "It's gonna rain at 4 (weather.com).");
//        assertTrue("did not find proper delivery failure method:", (result.indexOf(pager.getInvalidSubstring()) > -1));
    }
}
