package com.itbs.aimcer;

import com.itbs.aimcer.bean.ClientProperties;
import com.itbs.aimcer.bean.Contact;
import com.itbs.aimcer.bean.Message;
import com.itbs.aimcer.bean.Nameable;
import com.itbs.aimcer.commune.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alex Rass on Sep 9, 2004
 */
public class Logger implements ConnectionEventListener {
    File centralPath;
    PreparedStatement statement;
    private static final String CR = System.getProperty("line.separator");
    DateFormat timeFormat = new SimpleDateFormat("(hh:mm)");

    Map<String,HoldingHandle> cachedHandles = Collections.synchronizedMap(new HashMap<String, HoldingHandle>());

    static class HoldingHandle {
        RandomAccessFile raf;
        String lastDate;

        public HoldingHandle(RandomAccessFile raf) {
            this.raf = raf;
        }
        public void verifyDate() throws IOException {
            if (lastDate==null || !(new java.sql.Date(System.currentTimeMillis()).toString()).equals(lastDate)) {
                raf.writeBytes("Accessed" + ": " + new Date() + CR);
                lastDate = new java.sql.Date(System.currentTimeMillis()).toString();
            }
        }
    }

//    Map<String, RandomAccessFile> cachedHandles = new HashMap<String, RandomAccessFile>();
    public Logger(File path) throws IOException {
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

    private HoldingHandle openFile(MessageSupport connection, String buddy) throws IOException {
        RandomAccessFile raf;
        File path = new File(centralPath, connection.getServiceName() + "." + connection.getUserName());
        path.mkdirs();
        path = new File(path, buddy+".txt");
        path.createNewFile();
        raf = new RandomAccessFile(path, "rw");
        raf.seek(raf.length());
        HoldingHandle result = new HoldingHandle(raf);
        cachedHandles.put(connection.getServiceName() + connection.getUserName() + buddy, result);
        return result;
    }

    public File getLog(MessageSupport connection, String buddy) {
        File path = new File(centralPath, connection.getServiceName() + "." + connection.getUserName());
        if (path.exists() && path.isDirectory())
            path = new File(path, buddy+".txt");
        return path;
    }

    /**
     * Returns last portion of the log.
     * @param buddy to load the log for
     * @return logged lines
     * @throws IOException when things blow up
     */
    public String loadLog(MessageSupport connection, String buddy) throws IOException {
        HoldingHandle holdingHandle = cachedHandles.get(connection.getServiceName() + connection.getUserName() + buddy);
        if (holdingHandle == null) {
            holdingHandle = openFile(connection, buddy);
        }
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
     @param message message
     */
    public boolean messageReceived(MessageSupport connection, Message message) throws IOException {
        final String buddy = message.getContact().getName();
        HoldingHandle holdingHandle = cachedHandles.get(connection.getServiceName() + connection.getUserName() + buddy);
        if (holdingHandle == null) {
            holdingHandle = openFile(connection, buddy);
        }
        synchronized(holdingHandle) {
            holdingHandle.verifyDate();
            holdingHandle.raf.writeBytes(timeFormat.format(new Date()));
            holdingHandle.raf.writeBytes((message.isOutgoing()?connection.getUserName():buddy));
            holdingHandle.raf.writeBytes(": ");
            holdingHandle.raf.writeBytes(message.isOutgoing()?message.getText():message.getPlainText());
            holdingHandle.raf.writeBytes(CR);
        }
        if (ClientProperties.INSTANCE.getDatabaseURL() != null) {
            try {
                statement.clearParameters();
                statement.setString(1, message.isOutgoing()?connection.getUserName():buddy);
                statement.setString(2, !message.isOutgoing()?connection.getUserName():buddy);
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

    public void statusChanged(com.itbs.aimcer.commune.Connection connection, Contact contact, boolean online, boolean away, int idleMins) {
    }

    /**
     * Statuses for contacts that belong to this connection have changed.
     * @param connection
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
     @param connection connection
      * @param contact
     * @param filename
     * @param description
     * @param connectionInfo
     */
    public void fileReceiveRequested(FileTransferSupport connection, Contact contact, String filename, String description, Object connectionInfo) {
    }
    public boolean contactRequestReceived(final String user, final MessageSupport connection) {  return true; }
    
}
