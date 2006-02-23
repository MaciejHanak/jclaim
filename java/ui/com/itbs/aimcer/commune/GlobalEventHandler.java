package com.itbs.aimcer.commune;

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.gui.Main;

import javax.swing.*;

/**
 * @author Created by Alex Rass
 * Date: Sep 9, 2004
 */
public class GlobalEventHandler implements ConnectionEventListener {
    private int connectionsInProgress;

    public GlobalEventHandler() {
    }

    public void statusChanged(Connection connection, Contact contact, boolean online, boolean away, int idleMins) {
        // List model takes care of this.
        //   ((ContactWrapper)contact).setAway(away);
        //   ((ContactWrapper)contact).setOnline(online);
    }
                       
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

    public void typingNotificationReceived(MessageSupport connection, Nameable contact) { }

    /**
     * Sent before connection is attempted
     *
     * @param connection in context
     */
    public void connectionInitiated(Connection connection) {
        Main.getStatusBar().setVisible(true);
        connectionsInProgress ++;
    }

    private void connectionDone() {
        connectionsInProgress --;
        if (connectionsInProgress <=0) {
            connectionsInProgress = 0; // jik
            Main.getStatusBar().setVisible(false);
        }
    }

    public void connectionEstablished(final Connection connection) {
        Main.setTitle(connection.getServiceName() + "- Online");
        connectionDone();
        connection.setAway(ClientProperties.INSTANCE.isIamAway());
    }

    public void connectionFailed(final Connection connection, final String message) {
        new Thread("Complain") {
            public void run() {
                Main.complain(connection.getServiceName() + ": " + message);
            }
        }.start();
        connectionDone();
    }

    int connectionLostTimes;
    public void connectionLost(Connection connection) {
        System.out.println("Connection to " + connection.getServiceName() + " lost " + ++connectionLostTimes + " time(s).");
        connectionDone();
        Main.setTitle(connection.getServiceName() + " Offline");
        handleDisconnect(connection);
//        if (Main.getFrame().isDisplayable())
//            connection.reconnect();
    }

    /**
     * Call this method after you disconnect.
     */
    private void handleDisconnect(Connection connection) {
        // run through the groups and kill all contacts which belong to this connection
        for (int i = 0; i < connection.getGroupList().size(); i++) {
            Group group = connection.getGroupList().get(i);
            for (int j = 0; j < group.size(); j++) {
                Nameable contact = group.get(j);
                if (contact instanceof ContactWrapper) {
                    ContactWrapper cw = (ContactWrapper) contact;
                    if (connection == cw.getConnection())
                        cw.getStatus().setOnline(false);
                }
            }
        }
    } // handleDisconnect()


    public boolean messageReceived(MessageSupport connection, Message message) {
        return true;
    }

    public boolean emailReceived(MessageSupport connection, Message message) throws Exception {
        Main.showTooltip(message.getText());
        return true;
    }

    /**
     * Gets called when an assynchronous error occurs.
     *
     * @param message   to display
     * @param exception exception for tracing
     */
    public void errorOccured(String message, Exception exception) {
        Main.complain(message,  exception);
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
        // prompt for filename and acceptance
        final JFileChooser chooser = new JFileChooser(ClientProperties.INSTANCE.getLastFolder());
        chooser.setDialogTitle(contact + " is sending you file " + filename);
        chooser.setToolTipText(description);
        int returnVal = chooser.showSaveDialog(null);//main.getFrame()); // no parent is ok
        ClientProperties.INSTANCE.setLastFolder(chooser.getCurrentDirectory().getAbsolutePath());
        if (returnVal != JFileChooser.APPROVE_OPTION)
            connection.rejectFileTransfer(connectionInfo);
        else if(chooser.getSelectedFile().exists() || chooser.getSelectedFile().isDirectory()) {
            JOptionPane.showMessageDialog(Main.getFrame(), "File already exists or is a folder.", "Error:", JOptionPane.ERROR_MESSAGE);
            connection.rejectFileTransfer(connectionInfo);
        } else
            connection.acceptFileTransfer(new FileTransferAdapter(Main.getFrame(), description, chooser.getSelectedFile(), contact), connectionInfo);
    }

    public boolean contactRequestReceived(final String user, final MessageSupport connection) {
        return JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(Main.getFrame(), "Following contact wants to add you to his/her list: " + user + " on " + connection.getServiceName());
    }

} // class AimHandler
