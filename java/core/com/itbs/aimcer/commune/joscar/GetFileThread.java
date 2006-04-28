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

package com.itbs.aimcer.commune.joscar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.kano.joscar.rvcmd.SegmentedFilename;
import net.kano.joscar.rvproto.ft.FileTransferHeader;

import com.itbs.aimcer.commune.ConnectionInfo;
import com.itbs.aimcer.commune.FileTransferListener;
import com.itbs.aimcer.commune.FileTransferService;

/**
 * Not used!
 * Was used in the previous implementation for AIM.
 * But since then the library includes the trasfer support.
 * @author Alex Rass
 */
public class GetFileThread extends Thread implements FileTransferService {
    private static Logger log = Logger.getLogger(GetFileThread.class.getName());
    
    private Socket socket;
    FileTransferListener listener;
    boolean goOn = true;
    ConnectionInfo info;

    public GetFileThread(FileTransferListener listener, ConnectionInfo info) {
        this.info = info;
        this.listener = listener;
        if (listener == null)
            throw new NullPointerException("Listener can not be null");
        if (info == null)
            throw new NullPointerException("Connection information can not be null");
    }

    public void cancelTransfer() {
        goOn = false;
        if (socket!=null && socket.isBound())
            try {
                socket.close();
            } catch (IOException e) {
                log.log(Level.SEVERE, "",e);
            }
    }

    public void run() {
        RandomAccessFile writer = null;
        try {
            listener.notifyWaiting();
            log.fine("Connecting to: "  + listener.getContactName() + "@" + info.getIp() + ":" + info.getPort());
            socket = new Socket(info.getIp(), info.getPort());
            listener.notifyNegotiation();

            final InputStream in = socket.getInputStream();
            final OutputStream out = socket.getOutputStream();

            FileTransferHeader firstHdr = FileTransferHeader.readHeader(in);

            log.fine("got header: " + firstHdr);

            if (firstHdr == null) {
                listener.notifyCancel();
                return;
            }

            FileTransferHeader ack = new FileTransferHeader(firstHdr);

//            ack.setHeaderType(FileTransferHeader.HEADERTYPE_FILELIST_ACK);
            ack.setHeaderType(FileTransferHeader.HEADERTYPE_ACK);
//            ack.setFlags(FileTransferHeader.FLAG_DEFAULT | FileTransferHeader.FLAG_DONE);
            ack.setFlags(firstHdr.getFlags());// | FileTransferHeader.FLAG_DONE);

            log.fine("sending ack: " + ack);

            ack.write(out);
            listener.notifyTransfer();

            writer = new RandomAccessFile(listener.getFile(), "rw");
//            byte[] buffer = new byte[1024*10];
            byte[] buffer = new byte[1];
            int count;
            long totalCount = 0;
            do {
                count = in.read(buffer);
                if (count == -1) break;
                writer.write(buffer, 0, count); // write to file
                totalCount += count;
                listener.setProgress((int) (totalCount*100/firstHdr.getFileSize()));
            } while (goOn);


            if (!goOn)
                listener.notifyCancel();
            else
                listener.notifyDone();

            FileTransferHeader fin = new FileTransferHeader(ack);
            fin.setHeaderType(FileTransferHeader.HEADERTYPE_FILELIST_RECEIVED);
            fin.setFlags(FileTransferHeader.FLAG_DEFAULT
                    | FileTransferHeader.FLAG_DONE);

            fin.write(out);

            FileTransferHeader dirreq = new FileTransferHeader(fin);

            dirreq.setHeaderType(FileTransferHeader.HEADERTYPE_FILELIST_REQDIR);
            dirreq.setFilename(new SegmentedFilename(
                    new String[] { "in", "ebaything" }));
            dirreq.setTotalFileSize(0);
            dirreq.setFileSize(0);
            dirreq.setFileCount(1);
            dirreq.setFilesLeft(0);
            dirreq.write(out);

            FileTransferHeader resp = FileTransferHeader.readHeader(in);

            log.fine("got response: " + resp);

        } catch (IOException e) {
            log.log(Level.SEVERE, "",e);
            listener.notifyFail();
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e1) {
                }
        }
    }
}