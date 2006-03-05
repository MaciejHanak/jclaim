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

import com.itbs.aimcer.bean.Contact;
import com.itbs.gui.ActionAdapter;
import com.itbs.gui.BetterButton;
import com.itbs.gui.GUIUtils;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * @author Created by Alex Rass on Oct 8, 2004
 */
public class FileTransferAdapter extends JDialog implements FileTransferListener{
    private String descr;
    private File file;
    private Contact contact;
    private JProgressBar progressBar;

    private JButton done, cancel;
    private JLabel status;
    private FileTransferService service; // cancel on this

    public FileTransferAdapter(Frame owner, String description, File file, Contact contact) throws HeadlessException {
        super(owner);
        setTitle("File " + file.getName() +" for " + contact.getName());
        descr = description;
        this.file = file;
        this.contact = contact;
        createComponents(getContentPane());
        pack();  // fix height
        setSize(300, (int)getSize().getHeight());
        GUIUtils.moveToScreenCenter(this);
        setVisible(true);
    }

    public void setTransferService(FileTransferService service) {
        this.service = service;
    }

    private void createComponents(Container dlg) {
        dlg.setLayout(new GridLayout(0, 1));
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        dlg.add(progressBar);
        status = new JLabel("Setting up.");
        dlg.add(status);

        dlg.add(new JLabel("File: " + file.getPath()));

        ActionListener callBack = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (service == null || e.getSource() == done)
                    dispose();
                else // cancel
                    service.cancelTransfer();
            }
        };

        JPanel buttons = new JPanel();
        done = new BetterButton(new ActionAdapter("Done", "Close", callBack, 'D'));
        done.setEnabled(false);
        cancel = new BetterButton(new ActionAdapter("Cancel", "Cancel action", callBack, 'C'));
        buttons.add(done);
        buttons.add(cancel);
        dlg.add(buttons);
    } // adds graphics

    public String getFileDescription() {
        return descr;
    }

    public String getContactName() {
        return contact.getName();
    }

    public void notifyCancel() {
        done.setEnabled(true);
        cancel.setEnabled(false);
        status.setText("Cancelled at " + progressBar.getValue() + "%");
    }

    public void notifyFail() {
        done.setEnabled(true);
        cancel.setEnabled(false);
        status.setText("Failed at " + progressBar.getValue() + "%");
    }

    public void setProgress(int status) {
        progressBar.setValue(status);
    }

    public void notifyDone() {
        if (contact.getConnection().getProperties().isAutoDismissDialogs())
            dispose();
        else {
            done.setEnabled(true);
            cancel.setEnabled(false);
            status.setText("Transfer finished.");
        }
    }

    public File getFile() {
        return file;
    }

    public void notifyWaiting() {
        status.setText("Waiting for connection.");
    }

    public void notifyNegotiation() {
        status.setText("Negotiating connection.");
    }

    public void notifyTransfer() {
        status.setText("Transfering...");
    }

} // class
