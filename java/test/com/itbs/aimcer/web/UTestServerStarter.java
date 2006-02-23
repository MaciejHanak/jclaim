package com.itbs.aimcer.web;

import junit.framework.TestCase;

/**
 *
 */
public class UTestServerStarter extends TestCase {
    protected void setUp() throws Exception {
        ServerStarter.startServer();
    }

    protected void tearDown() throws Exception {
        ServerStarter.stopServer();
    }

    public void testStuff() throws Exception {
        
        Thread.sleep(60*60*1000);
    }
}
