package com.itbs.aimcer.commune;

import java.io.File;

/**
 * @author Created by Alex Rass
 * @since Oct 8, 2004
 */
public interface FileTransferListener {
    String getFileDescription();
    void notifyCancel();
    void notifyFail();
    void setProgress(int status);
    void notifyDone();
    File getFile();
    String getContactName();
    void setTransferService(FileTransferService service);
    
    /**
     * Notifies whoever that we have not connected
     */
    void notifyWaiting();
    /**
     * Notifies whoever that we have started talking to other side
     */
    void notifyNegotiation();
    /**
     * Notifies whoever that we started the transfer
     */
    void notifyTransfer();
}
