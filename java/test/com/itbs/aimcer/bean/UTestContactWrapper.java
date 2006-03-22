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

package com.itbs.aimcer.bean;

import com.itbs.aimcer.commune.joscar.OscarConnection;
import junit.framework.TestCase;

/**
 * Tests the bean class.
 *
 * @author ARass
 * @since Date: Sep 24, 2004
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
