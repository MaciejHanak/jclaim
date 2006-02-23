package com.itbs.gui;

import com.itbs.aimcer.gui.UTestFrameTest;
import com.itbs.util.JNIHelper;

import javax.swing.*;
import java.awt.*;

/**
 * @author Created by  Administrator on Nov 7, 2004
 */
public class UTestTrayAdapter extends UTestFrameTest {
    JTextArea comp = new JTextArea(5, 20);




    public void testMain() throws Exception {
        comp.setText("This is the story\nof the hare who\nlost his spectacles.");

        window.add(new JScrollPane(comp), BorderLayout.CENTER);

        window.setSize(500, 400);
        window.setVisible(true);
        Thread.sleep(1000);
        System.out.println("" + JNIHelper.setForegroundWindow("Visual Slick"));
        Thread.sleep(200);
        TrayAdapter.alert(window);
        waitForMe(70000);
    }

}
