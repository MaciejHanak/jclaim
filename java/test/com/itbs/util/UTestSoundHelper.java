package com.itbs.util;

import junit.framework.TestCase;

/**
 * @author Created by  Administrator on Apr 25, 2005
 */
public class UTestSoundHelper extends TestCase {
    public void testPlay() throws Exception {
        SoundHelper.playSound("g:\\jclaim\\sounds\\bottle-open.wav");
//        SoundHelper.playSound("..\\sounds\\bottle-open.wav");
        Thread.sleep(2000);
    }
}
