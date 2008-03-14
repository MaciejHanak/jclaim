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

import java.io.IOException;

/**
 * @author Alex Rass
 * @since Apr 24, 2005
 */
public interface FileTransferSupport extends Connection {
    /**
     * Starts a file transfer.
     * @param ftl listener
     * @throws java.io.IOException when file errors occur
     */
    void initiateFileTransfer(FileTransferListener ftl) throws IOException;

    /**
     * Sets up file for receival
     * @param ftl descriptor param
     * @param connectionInfo object from the underlying protocol that needs to be passed around. Or wrap if you need more.
     */
    void acceptFileTransfer(FileTransferListener ftl, Object connectionInfo);

    /**
     * Request to cancel the file transfer in progress.
     * @param connectionInfo object from the underlying protocol that needs to be passed around. Or wrap if you need more.
     */
    void rejectFileTransfer(Object connectionInfo);
}
