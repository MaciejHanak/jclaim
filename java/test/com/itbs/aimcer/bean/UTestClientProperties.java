package com.itbs.aimcer.bean;

import com.itbs.aimcer.gui.Main;
import junit.framework.TestCase;

/**
 * @author Created by  Administrator on Jan 1, 2005
 */
public class UTestClientProperties extends TestCase {
    public void testAquireConfig() throws Exception {
        Main.loadProperties();
    }
}
