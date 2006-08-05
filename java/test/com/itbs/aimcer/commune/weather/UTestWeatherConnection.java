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

package com.itbs.aimcer.commune.weather;

import com.itbs.aimcer.bean.ClientProperties;
import com.itbs.aimcer.bean.ContactWrapper;
import com.itbs.aimcer.gui.Main;
import com.itbs.aimcer.gui.UTestFrameTest;

/**
 * Test class.
 * @author Alex Rass
 * @since Oct 10, 2004
 */
public class UTestWeatherConnection extends UTestFrameTest {
    final static String zipCode[] = {"08837", "10001"};

    public void XtestCheckEquals() throws Exception {
        WeatherConnection conn = new WeatherConnection();
        for (int i=0; i<zipCode.length; i++) {
            ContactWrapper cw = ContactWrapper.create(zipCode[i], conn);
            ContactWrapper cw2 = ContactWrapper.create(zipCode[i], conn);
            assertEquals(cw, cw2);
            cw.getStatus().setOnline(true);
            cw.setDisplayName("blah");
            cw2 = ContactWrapper.create(zipCode[i], conn);
            assertEquals(cw, cw2);
        }
    }

    public void testCheckWeather() throws Exception {
        WeatherConnection conn = new WeatherConnection();
        conn.setProperties(ClientProperties.INSTANCE);
        conn.assignGroupFactory(Main.standardGroupFactory);
        conn.assignContactFactory(Main.standardContactFactory);
        conn.connect();
        for (String aZipCode : zipCode) {
            ContactWrapper cw = ContactWrapper.create(aZipCode, conn);
            conn.addContact(aZipCode);
            assertEquals(cw.getName(), aZipCode);
        }
        conn.connect();
        window.setVisible(true);
        Thread.sleep(10000*1);
        for (String aZipCode1 : zipCode) {
            ContactWrapper cw = ContactWrapper.create(aZipCode1, conn);
            window.getContentPane().add(cw.getDisplayComponent(false, false));
            System.out.println("Result: " + cw.getDisplayName() + " " + cw.oldToString());
            assertFalse("Should have been set by now" + cw.getDisplayName(), cw.getDisplayName().equals(aZipCode1));
            assertTrue("Should have been set by now" + cw.getDisplayName(), cw.getDisplayName().indexOf(", NJ - ") > -1 || cw.getDisplayName().indexOf(", NY - ") > -1);
            assertNotNull("Should have set the icon" + cw.getIcon(), cw.getIcon());
        }
        waitForMe(60);
    }

}
