package com.itbs.aimcer.commune;

import com.itbs.aimcer.bean.ContactWrapper;
import com.itbs.aimcer.gui.UTestFrameTest;

import java.io.File;

/**
 * @author Created by  Administrator on Oct 8, 2004
 */
public class UTestFileTransferAdapter extends UTestFrameTest {
    boolean failed;

    public void testTransferDialog() throws Exception {
        window.setVisible(true);
        final FileTransferAdapter adapter = new FileTransferAdapter(window, "descr", new File("c:\\aimlogs\\name.txt"), ContactWrapper.create("buddy!", null));
        adapter.setTransferService(new FileTransferService() {
            public void cancelTransfer() {
                System.out.println("Cancelled pressed and worked.");
            }
        });

        new Thread(){
            public void run() {
                try {
                    adapter.notifyWaiting();
                    sleep(1000);
                    adapter.notifyNegotiation();
                    sleep(1000);
                    adapter.notifyTransfer();
                    sleep(1000);
                    adapter.setProgress(10);
                    sleep(1000);
                    adapter.setProgress(20);
                    sleep(1000);
                    adapter.setProgress(30);
                    sleep(1000);
                    adapter.notifyCancel();
                    sleep(1000);
                    adapter.notifyFail();
                    sleep(1000);
                    adapter.notifyDone();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    failed = true;
                }
            }
        }.start();
        waitForMe(5);
        window.setVisible(false);
        adapter.dispose();
        if (failed)
            fail("failed somehow");
    }
} // class
