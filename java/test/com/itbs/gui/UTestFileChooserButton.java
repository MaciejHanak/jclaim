package com.itbs.gui;

import com.itbs.aimcer.gui.UTestFrameTest;

/**
 * @author Created by  Administrator on Oct 8, 2004
 */
public class UTestFileChooserButton extends UTestFrameTest {

    public void testTextPane() throws InterruptedException {
        FileChooserButton pane = new FileChooserButton(window);
        add(pane);
        window.setVisible(true);
        waitForMe(500);
        if (pane.getFile()!=null)
            System.out.println(""+ pane.getFile().getPath());
    }


}
