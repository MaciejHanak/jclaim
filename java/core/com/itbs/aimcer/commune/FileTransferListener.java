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
