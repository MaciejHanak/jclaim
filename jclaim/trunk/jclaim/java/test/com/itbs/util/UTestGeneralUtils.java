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

package com.itbs.util;

import junit.framework.TestCase;

/**
 * @author Alex Rass
 * @since Apr 11, 2006
 */
public class UTestGeneralUtils extends TestCase {
    public void testPlay() throws Exception {
        System.out.println(GeneralUtils.getInterfaces(true));
        assertTrue(GeneralUtils.getInterfaces(true).length()>0);
        assertTrue(GeneralUtils.getInterfaces(false).length()>0);
    }
}
