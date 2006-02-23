package com.itbs.aimcer.commune;

import java.io.IOException;

/**
 * @author Alex Rass
 * @since Apr 24, 2005
 */
public interface FileTransferSupport extends Connection {
    /**
     * Starts a file transfer.
     * @param ftl listener
     */
    void initiateFileTransfer(FileTransferListener ftl) throws IOException;

    /**
     * Sets up file for receival
     * @param ftl param
     * @param connectionInfo
     */
    void acceptFileTransfer(FileTransferListener ftl, Object connectionInfo);

    /**
     * Request to cancel the file transfer in progress.
     */
    void rejectFileTransfer(Object connectionInfo);
}
