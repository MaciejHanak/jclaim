package com.itbs.gui;

import com.itbs.aimcer.gui.UTestFrameTest;

import javax.swing.*;
import java.awt.*;

/**
 * @author Created by  Administrator on Nov 7, 2004
 */
public class UTestJazzyInterface extends UTestFrameTest {
    JTextArea comp = new JTextArea(5, 20);




    public void testMain() throws Exception {
        comp.setText("This is the story\nof the hare who\nlost his spectacles.");
        JazzyInterface.create().addSpellCheckComponent(comp);

        window.add(new JScrollPane(comp), BorderLayout.CENTER);

        window.setSize(500, 400);
        window.setVisible(true);
        waitForMe(10000);
    }

}
