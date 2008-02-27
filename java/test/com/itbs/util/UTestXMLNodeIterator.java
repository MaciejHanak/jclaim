package com.itbs.util;

import com.itbs.bot.UTestBot;
import junit.framework.TestCase;

import java.io.StringBufferInputStream;

/**
 * @author Alex Rass
 * @since Apr 25, 2005
 */
public class UTestXMLNodeIterator extends TestCase {
    public void testPlay() throws Exception {
        XMLNodeIterator xni = new XMLNodeIterator(new StringBufferInputStream(UTestBot.testXML));
        while (xni.hasNext()) {
            System.out.println(""+xni.next());
        }
    }
}
