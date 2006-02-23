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

import com.itbs.aimcer.commune.ConnectionInfo;
import com.itbs.aimcer.commune.FileTransferListener;
import com.itbs.aimcer.commune.FileTransferService;
import net.kano.joscar.rvproto.ft.FileTransferHeader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

public class ReceiveFileThread extends Thread implements FileTransferService {
    private Socket socket;
    FileTransferListener listener;
    boolean goOn = true;
    ConnectionInfo info;

    public ReceiveFileThread(FileTransferListener listener, ConnectionInfo info) {
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
                e.printStackTrace();
            }
    }

    public void run() {
        RandomAccessFile writer = null;
        try {
            listener.notifyWaiting();
            System.out.println("Connecting to: "  + listener.getContactName() + "@" + info.getIp() + ":" + info.getPort());
            socket = new Socket(info.getIp(), info.getPort());
            listener.notifyNegotiation();

            final InputStream in = socket.getInputStream();
            final OutputStream out = socket.getOutputStream();

            FileTransferHeader header = FileTransferHeader.readHeader(in);

            System.out.println("got header: " + header);

            if (header == null) {
                listener.notifyCancel();
                return;
            }

            FileTransferHeader ack = new FileTransferHeader(header);
            ack.setHeaderType(FileTransferHeader.HEADERTYPE_ACK);
            ack.setIcbmMessageId(0);
//            ack.setIcbmMessageId(cookie);
            ack.setBytesReceived(0);
//            ack.setReceivedChecksum(sum);
            ack.setReceivedChecksum(0);

            System.out.println("sending ack: " + ack);

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
                listener.setProgress((int) (totalCount*100/header.getFileSize()));
            } while (goOn);


            if (!goOn)
                listener.notifyCancel();
            else
                listener.notifyDone();

            FileTransferHeader doneHeader = new FileTransferHeader(header);
            doneHeader.setHeaderType(FileTransferHeader.HEADERTYPE_RECEIVED);
            doneHeader.setFlags(doneHeader.getFlags()
                    | FileTransferHeader.FLAG_DONE);
            doneHeader.setBytesReceived(doneHeader.getBytesReceived() + 1);
//            doneHeader.setIcbmMessageId(cookie);
            doneHeader.setFilesLeft(doneHeader.getFilesLeft() - 1);
            doneHeader.write(out);
            if (doneHeader.getFilesLeft() - 1 <= 0) {
                socket.close();
//                break;
            }
            System.out.println("got response: " + doneHeader);

        } catch (IOException e) {
            e.printStackTrace();
            listener.notifyFail();
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e1) {
                }
        }
    }
}