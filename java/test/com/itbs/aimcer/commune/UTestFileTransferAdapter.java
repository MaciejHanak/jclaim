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

package com.itbs.aimcer.commune;

import com.itbs.aimcer.bean.ContactWrapper;
import com.itbs.aimcer.gui.UTestFrameTest;

import java.io.File;

/**
 * Used to test file transfers.
 *
 * @author Alex Rass
 * @since Oct 8, 2004
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
