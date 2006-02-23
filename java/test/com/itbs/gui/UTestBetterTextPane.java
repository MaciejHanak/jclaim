package com.itbs.gui;

import com.itbs.aimcer.gui.UTestFrameTest;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Created by  Administrator on Sep 12, 2004
 */
public class UTestBetterTextPane extends UTestFrameTest {
    boolean trigger;
    /**
     * Didn't feel like working the robot. this unit test is a hands on one.
     * @throws InterruptedException stuff broke
     */
    public void testTextPane() throws InterruptedException {
        Action action = new ActionAdapter("nm", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                trigger = true;
                System.out.println("Fired.");
            }
        });
        BetterTextPane pane = new BetterTextPane(action);
        pane.addModifier(KeyEvent.SHIFT_DOWN_MASK);
        pane.addModifier(KeyEvent.CTRL_DOWN_MASK);
        add(pane);
        window.setVisible(true);
        waitForMe(50);
        System.out.println(""+ trigger);
    }
}
