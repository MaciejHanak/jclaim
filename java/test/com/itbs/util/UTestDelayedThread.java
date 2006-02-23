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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Created by  Administrator on Apr 25, 2005
 */
public class UTestDelayedThread extends TestCase {
//    private Logger logger = Logger.getLogger(DelayedThread.class.getPackage().getName());
    private Logger logger = Logger.getLogger(UTestDelayedThread.class.getName());
    boolean alive = true;
    int runsPre, runsPost;
    private static long DELAY = 1000;
    public void testDialog() throws Exception {
//        Logger.getLogger("").addHandler(new ConsoleHandler());
//        logger.setLevel(Level.ALL);
        logger.log(Level.INFO, "start");
        DelayedThread delayedThread = new DelayedThread(getClass().getName(),
                DELAY,
                new DelayedThread.StillAliveMonitor() {
                    public boolean isAlive() {
                        return alive;
                    }
                },
                new Runnable() {
                    public void run() {
                        runsPre++;
                        logger.fine("Pre");
                    }
                },
                new Runnable() {
                    public void run() {
                        runsPost++;
                        logger.fine("Post");
                    }
                }
        );
        // Preconditions
        verify(0, 0, true);
        delayedThread.start();
        Thread.sleep(100);
        verify(0, 0, true);

        // simple mark -> execute
        delayedThread.mark();
        verify(1, 0, true);
        Thread.sleep(DELAY + 100);
        verify(1, 1, true);

        // mark -> delay -> execute
        delayedThread.mark();
        verify(2, 1, true);
        Thread.sleep(DELAY / 2);
        delayedThread.mark();
        Thread.sleep(DELAY / 2);
        verify(3, 1, true);
        Thread.sleep(DELAY / 2 + 100);
        verify(3, 2, true);

        // die
        alive = false;
        delayedThread.mark();
        verify(4, 2, false);
        Thread.sleep(DELAY + 100);
        assertFalse(delayedThread.isAlive());
        verify(4, 3, false);
    }
    public void verify(int pre, int post, boolean live) {
        assertEquals(runsPre, pre);
        assertEquals(runsPost, post);
        assertEquals(alive, live);
    }
}
