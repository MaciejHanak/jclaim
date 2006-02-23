/**
 *  Copyright (c) 2003, The Joust Project
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  - Neither the name of the Joust Project nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 *  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 *  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *  LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *  ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 *
 *  File created by Keith @ 5:04:14 AM
 *
 *
 * Used to request files from a share.
 */

package com.itbs.aimcer.commune.joscar;

import com.itbs.aimcer.commune.ConnectionInfo;
import com.itbs.aimcer.commune.FileTransferListener;
import com.itbs.aimcer.commune.FileTransferService;
import net.kano.joscar.rvcmd.SegmentedFilename;
import net.kano.joscar.rvproto.ft.FileTransferHeader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

public class GetFileThread extends Thread implements FileTransferService {
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

            FileTransferHeader firstHdr = FileTransferHeader.readHeader(in);

            System.out.println("got header: " + firstHdr);

            if (firstHdr == null) {
                listener.notifyCancel();
                return;
            }

            FileTransferHeader ack = new FileTransferHeader(firstHdr);

//            ack.setHeaderType(FileTransferHeader.HEADERTYPE_FILELIST_ACK);
            ack.setHeaderType(FileTransferHeader.HEADERTYPE_ACK);
//            ack.setFlags(FileTransferHeader.FLAG_DEFAULT | FileTransferHeader.FLAG_DONE);
            ack.setFlags(firstHdr.getFlags());// | FileTransferHeader.FLAG_DONE);

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

            System.out.println("got response: " + resp);

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