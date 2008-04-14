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

package com.itbs.aimcer.log;

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.commune.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Performs management of the logs.
 * Knows how to store logs in a tree form off the root specified in properties
 * @author Alex Rass
 * @since Sep 9, 2004
 */
public class LoggerEventListener implements ConnectionEventListener {
    private static final Logger log = Logger.getLogger(LoggerEventListener.class.getName());

    File centralPath;
    PreparedStatement statement;
    private static final String CR = System.getProperty("line.separator");
    DateFormat timeFormat = new SimpleDateFormat("(hh:mm)");

    Map<String,HoldingHandle> cachedHandles = new ConcurrentHashMap <String, HoldingHandle>();

    public static class HoldingHandle {
        RandomAccessFile raf;
        File path;
        String lastDate;

        public HoldingHandle(File path) throws IOException {
            this.path = path;
            raf = new RandomAccessFile(path, "rw");
            raf.seek(raf.length());
        }
        public void verifyDate() throws IOException {
            if (lastDate==null || !(new java.sql.Date(System.currentTimeMillis()).toString()).equals(lastDate)) {
                raf.writeBytes("Accessed" + ": " + new Date() + CR);
                lastDate = new java.sql.Date(System.currentTimeMillis()).toString();
            }
        }

        public RandomAccessFile getRaf() {
            return raf;
        }


        public File getPath() {
            return path;
        }

        public String getLastDate() {
            return lastDate;
        }
    }

    /**
     * Constuctor.
     *  
     * @param path Starting path.
     * @throws IOException IO errors
     */
    public LoggerEventListener(File path) throws IOException {
        centralPath = path;
        if (path.isFile())
            throw new IOException("Logging path should not be a file, please choose a folder instead.");
        path.mkdirs();
        if (!path.exists()){
            throw new IOException("Could not create or find the logging folder.");
        }
        if (ClientProperties.INSTANCE.getDatabaseURL() != null) {
            try {
                Class.forName(ClientProperties.INSTANCE.getDatabaseDriver());
                java.sql.Connection conn = DriverManager.getConnection(ClientProperties.INSTANCE.getDatabaseURL(), ClientProperties.INSTANCE.getDatabaseUsername(), ClientProperties.INSTANCE.getDatabasePassword());
                statement = conn.prepareStatement(ClientProperties.INSTANCE.getDatabaseStore());
                // insert into _table (from, to, message) values (?,?,?)
            } catch (Exception e) {
                throw new RuntimeException("Failed to addConnection to database storage", e);
            }
        }
    }

    public synchronized void closeFile(Connection connection, String buddy) throws IOException {
        String key = connection.getServiceName() + connection.getUser().getName() + buddy;
        HoldingHandle holdingHandle = cachedHandles.get(key);
        if (holdingHandle != null) {
            cachedHandles.remove(key);
            holdingHandle.getRaf().close();
        }
    }
    
    public synchronized HoldingHandle openFile(Connection connection, String buddy) throws IOException {
        String key = connection.getServiceName() + connection.getUser().getName() + buddy;
        HoldingHandle holdingHandle = cachedHandles.get(key);
        if (holdingHandle != null) {
            return holdingHandle;
        } else {
            File path = new File(centralPath, connection.getServiceName() + "." + connection.getUser().getName());
            path.mkdirs();
            path = new File(path, buddy+".txt");
            path.createNewFile();
            HoldingHandle result = new HoldingHandle(path);
            cachedHandles.put(connection.getServiceName() + connection.getUser().getName() + buddy, result);
            return result;
        }
    }

    public File getLog(MessageSupport connection, String buddy) {
        File path = new File(centralPath, connection.getServiceName() + "." + connection.getUserName());
        if (path.exists() && path.isDirectory())
            path = new File(path, buddy+".txt");
        return path;
    }

    /**
     * Returns last portion of the log.
     * @param connection connection
     * @param buddy to load the log for
     * @return logged lines
     * @throws IOException when things blow up
     */
    public String loadLog(MessageSupport connection, String buddy) throws IOException {
        HoldingHandle holdingHandle = openFile(connection, buddy);
        byte[] text;
        synchronized(holdingHandle) {
            holdingHandle.verifyDate();
            long readFrom = holdingHandle.raf.length() - (ClientProperties.INSTANCE.getDisplayBuffer() * 1024);
            readFrom = readFrom < 0 ? 0 : readFrom;
            holdingHandle.raf.seek(readFrom);
            text = new byte[(int)(holdingHandle.raf.length() - readFrom)];
            holdingHandle.raf.readFully(text);
            holdingHandle.raf.seek(holdingHandle.raf.length()); // todo see if we still need this
        }
        return new String(text);
    }

    /////////////////////////////////////////////////////////////////////
   // *******************  ConnectionEventListener   **************  //
  ///////////////////////////////////////////////////////////////////

    /**
     * Recevied a message.
     * Called with incoming and outgoing messages from any connection.
     * @param connection connection
     * @param message message
     */
    public boolean messageReceived(MessageSupport connection, Message message) throws IOException {
        return messageReceived(((Connection) connection), message);
    }

    /**
     * Recevied a message.
     * Called with incoming and outgoing messages from any connection.
     * @param connection connection
     * @param message message
     * @return true if ok
     * @throws IOException when can't complete
     */
    public boolean messageReceived(Connection connection, Message message) throws IOException {
        final String buddy = message.getContact().getName();
        final String userName = connection.getUser().getName();
        HoldingHandle holdingHandle = openFile(connection, buddy);
        synchronized(holdingHandle) {
            holdingHandle.verifyDate();
            holdingHandle.raf.writeBytes(timeFormat.format(new Date()));
            holdingHandle.raf.writeBytes((message.isOutgoing()?userName:buddy));
            holdingHandle.raf.writeBytes(": ");
            holdingHandle.raf.writeBytes(message.isOutgoing()?message.getText():message.getPlainText());
            holdingHandle.raf.writeBytes(CR);
        }
        if (ClientProperties.INSTANCE.getDatabaseURL() != null) {
            try {
                statement.clearParameters();
                statement.setString(1, message.isOutgoing()?userName:buddy);
                statement.setString(2, !message.isOutgoing()?userName:buddy);
                statement.setString(3, message.getText());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new IOException(e.getMessage()+"\n"+e.getErrorCode()+"\n"+e.getLocalizedMessage());
            }
        } // if
        return true;
    } // log

    public boolean emailReceived(MessageSupport connection, Message message) throws Exception {
        return messageReceived(connection, message);
    }

    public void typingNotificationReceived(MessageSupport connection, Nameable contact) { }

    public void connectionInitiated(Connection connection) {
    }

    public void connectionLost(com.itbs.aimcer.commune.Connection connection) {
    }

    public void connectionFailed(com.itbs.aimcer.commune.Connection connection, String message) {
    }

    public void connectionEstablished(com.itbs.aimcer.commune.Connection connection) {
    }

    public void statusChanged(com.itbs.aimcer.commune.Connection connection, Contact contact, Status oldStatus) { }

    /**
     * Statuses for contacts that belong to this connection have changed.
     * @param connection connection
     */
    public void statusChanged(Connection connection) {
    }

    /**
     * A previously requested icon has arrived.
     * Icon will be a part of the contact.
     *
     * @param connection connection
     * @param contact    contact
     */
    public void pictureReceived(IconSupport connection, Contact contact) {
    }

    /**
     * Gets called when an assynchronous error occurs.
     *
     * @param message   to display
     * @param exception exception for tracing
     */
    public void errorOccured(String message, Exception exception) {
        //don't care
    }

    /**
     * Other side requested a file transfer.
     * @param connection connection
     * @param contact who initiated msg
     * @param filename proposed name of file
     * @param description of the file
     * @param connectionInfo  your private object used to store protocol specific data
     */
    public void fileReceiveRequested(FileTransferSupport connection, Contact contact, String filename, String description, Object connectionInfo)  {
        String text = "\n" + contact.getName() + " is trying to send you a file: " + filename + (description==null || description.trim().length()==0?"":"\nDescription: " + description);
        Message message = new MessageImpl(contact, false, text);
        try {
            messageReceived(connection, message);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to log incoming file info!", e);
        }
    }

    public boolean contactRequestReceived(final String user, final MessageSupport connection) {  return true; }

}
