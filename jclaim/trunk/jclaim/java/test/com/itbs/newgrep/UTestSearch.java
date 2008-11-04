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

package com.itbs.newgrep;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;

/**
 * Tester.
 *
 * @author <Authors name>
 * @since <pre>09/30/2005</pre>
 * @version 1.0
 */
public class UTestSearch extends TestCase
{
    public UTestSearch(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
    }

    public void tearDown() throws Exception
    {
    }

    public void testAppend() throws Exception
    {
        //TODO: Test goes here...
    }

    int cnt;
    public void testFind() throws Exception
    {
        Search.What searchSimple;
        searchSimple = new SingleString("oneotion");
//        searchSimple = new SingleString("0000");
        searchSimple.find(new File("c:\\addcontact.txt"), searchSimple, new Search.Result() {
//        searchSimple.find(new File("g:\\calctool\\hdsn_openorder_dump.dat"), searchSimple, new Search.Result() {
            public void startResource(String name) {
                System.out.println(name);
            }

            public void append(Search.Where whereFound) {
                cnt++;
                System.out.println(whereFound.getLineNumber() + " at " + whereFound.getFilePosition() + ":" + whereFound.getLine());
            }
        }, new Options());
        System.out.println("---" + cnt);
        searchSimple = new DoubleString("run", "Sep ");
        searchSimple.find(new File("c:\\addcontact.txt"), searchSimple, new Search.Result() {
            public void startResource(String name) {
                System.out.println(name);
            }
            public void append(Search.Where whereFound) {
                System.out.println(whereFound.getLineNumber() + " at " + whereFound.getFilePosition());
            }
        }, new Options());
    }


    public static Test suite()
    {
        return new TestSuite(UTestSearch.class);
    }
}
