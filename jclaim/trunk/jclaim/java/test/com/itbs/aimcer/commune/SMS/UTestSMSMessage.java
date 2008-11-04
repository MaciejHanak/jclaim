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

import junit.framework.TestCase;

/**
 * Tests SMS.
 * @author Administrator
 * @since Apr 24, 2005
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
