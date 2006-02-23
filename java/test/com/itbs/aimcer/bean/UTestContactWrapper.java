package com.itbs.aimcer.bean;

import com.itbs.aimcer.commune.joscar.OscarConnection;
import junit.framework.TestCase;

/**
 * Created by: ARass  on  Date: Sep 24, 2004
 */
public class UTestContactWrapper extends TestCase {
    String nameGood = "good";
    String nameBad = "bad";

    public void testEquals() throws Exception {
        ContactWrapper contact = ContactWrapper.create(nameGood, null);
        ContactWrapper contactBad = ContactWrapper.create(nameBad, null);
        Nameable contactNew = new Nameable() {
            public String getName() {
                return nameGood;
            }
        };
        assertEquals(contact, contactNew);
        assertFalse(contact.equals(contactBad));
        OscarConnection conn = new OscarConnection();
        ContactWrapper contact2 = ContactWrapper.create(nameGood, conn);
        assertNotSame(contact, contact2);
    }
}
