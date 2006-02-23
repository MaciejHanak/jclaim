/*
 *  Copyright (c) 2002-2003, The Joust Project
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
 *  File created by keith @ Apr 27, 2003
 *  Modified by Alex Rass to actually transfer a file.
 */

package com.itbs.aimcer.commune.joscar;

import com.itbs.aimcer.commune.FileTransferListener;
import com.itbs.aimcer.commune.FileTransferService;
import net.kano.joscar.rvcmd.SegmentedFilename;
import net.kano.joscar.rvproto.ft.FileTransferHeader;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SendFileThread extends Thread implements FileTransferService {
    private ServerSocket serverSocket;
    FileTransferListener listener;
    boolean goOn = true;

    public SendFileThread(FileTransferListener listener, ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.listener = listener;
        setName("File Transfer Thread");
    }

    public void cancelTransfer() {
        goOn = false;
        listener.notifyCancel();
        try {
            serverSocket.close();   // takes care of the accept call hanging
        } catch (IOException e) {
        }
    }

    public void run() {
        RandomAccessFile reader=null;
        try {
            System.out.println("waiting for connection..");
            listener.notifyWaiting();
            Socket socket = serverSocket.accept();
            System.out.println("got connection from " + socket.getInetAddress());
            listener.notifyNegotiation();

            final OutputStream out = socket.getOutputStream();
            final InputStream in = socket.getInputStream();

            FileTransferHeader fsh = new FileTransferHeader();
            fsh.setDefaults();
            fsh.setFileCount(1);
            fsh.setFilesLeft(1);
            fsh.setFilename(SegmentedFilename.fromNativeFilename(listener.getFile().getName()));
            final File file = listener.getFile();
            final long fileLength = file.length();
            fsh.setFileSize(fileLength);
            fsh.setTotalFileSize(fileLength);
            fsh.setHeaderType(FileTransferHeader.HEADERTYPE_SENDHEADER);
            fsh.setPartCount(1);
            fsh.setPartsLeft(1);
            fsh.setLastmod(file.lastModified() / 1000);
            fsh.setChecksum(100);    // todo see about this, use FileTransferChecksum

            System.out.println("writing: " + fsh);

            fsh.write(out); // sent out the header

            System.out.println("waiting for ack header..");

            FileTransferHeader inFsh = FileTransferHeader.readHeader(in);

            System.out.println("got ack:" + inFsh);

            if (inFsh.getHeaderType() == FileTransferHeader.HEADERTYPE_RESUME) {
                fsh.setHeaderType(FileTransferHeader.HEADERTYPE_RESUME_SENDHEADER);
                fsh.write(out);

                inFsh = FileTransferHeader.readHeader(in);

                System.out.println("resumesendresponse: " + inFsh);
            }

            reader = new RandomAccessFile(file, "r");
            byte[] buffer = new byte[1024*10];
            int read;
            long totalRead = 0;
            do {
                read = reader.read(buffer);
                if (read == -1) break;
                out.write(buffer, 0, read);
                totalRead += read;
                listener.setProgress((int)(totalRead*100/fileLength));
            } while (goOn);
            reader.close();

            if (!goOn)
                listener.notifyCancel();
            else
                listener.notifyDone();
/*
            for (;;) {
                System.out.println("trying to read..");
                int b = in.read();

                if (b == -1) break;

                System.out.println("got stuff: "
                        + BinaryTools.describeData(ByteBlock.wrap(
                                new byte[] { (byte) b })));
            }
*/

        } catch (IOException e) {
            e.printStackTrace();
            cancelTransfer();
            listener.notifyFail();
        } finally {
            if (reader!=null)
                try {
                    reader.close();
                } catch (IOException e) {
                    ; // no care
                }
            try {
                serverSocket.close();
            } catch (IOException e) {
            }

        }
    }
}
