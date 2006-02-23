package com.itbs.aimcer.commune.weather;

import com.itbs.aimcer.bean.ContactWrapper;
import com.itbs.aimcer.gui.UTestFrameTest;

/**
 * @author Created by  Administrator on Oct 10, 2004
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
        conn.connect();
        for (String aZipCode : zipCode) {
            ContactWrapper cw = ContactWrapper.create(aZipCode, conn);
            conn.addContact(aZipCode);
            assertEquals(cw.getName(), aZipCode);
        }
        conn.connect();
        window.setVisible(true);
        Thread.sleep(20000*1);
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
