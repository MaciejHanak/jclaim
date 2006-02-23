package com.itbs.aimcer.gui;

import com.itbs.aimcer.bean.ContactWrapper;
import com.itbs.aimcer.bean.Group;
import com.itbs.aimcer.bean.Message;
import com.itbs.aimcer.bean.Nameable;
import com.itbs.aimcer.commune.AbstractMessageConnection;
import com.itbs.aimcer.commune.ConnectionEventListener;
import com.itbs.aimcer.commune.FileTransferListener;
import com.itbs.aimcer.commune.FileTransferSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex Rass 
 * @since Oct 16, 2004
 */
public class UTestFakeConnection extends AbstractMessageConnection implements FileTransferSupport {
    private boolean loggedIn;
    List groups = new ArrayList();
    public UTestFakeConnection() {
    }


    public String getServiceName() {
        return "Fake";
    }

    public void connect() throws Exception {
        final Group sampleGroup = getGroupFactory().create("Group1");
        groups.add(sampleGroup);
        sampleGroup.add(ContactWrapper.create("Bob", null));
        sampleGroup.add(ContactWrapper.create("Steve", null));
        sampleGroup.add(ContactWrapper.create("Jill", null));
        sampleGroup.add(ContactWrapper.create("Jack", null));

        super.connect();
        notifyConnectionInitiated();
        System.out.println("FC: addConnection");
        getGroupList().add(getGroupFactory().create("FakeGroup"));
        getGroupFactory().create("FakeGroup").add(ContactWrapper.create("Fake User", this));
        ContactWrapper.create("Fake User", this).getStatus().setOnline(true);
        loggedIn = true;
        notifyConnectionEstablished();
    }

    public void disconnect(boolean intentional) {
        loggedIn = false;
        ContactWrapper.create("Fake User", this).getStatus().setOnline(false);
        System.out.println("FC: disconnect");
        for (ConnectionEventListener eventHandler : eventHandlers) {
            eventHandler.connectionLost(this);
        }
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void cancel() {
    }

    public void setTimeout(int timeout) {
    }

    public void addContact(Nameable contact, Group group) {
        ((ContactWrapper)contact).getStatus().setOnline(true);
        group.add(contact);
    }

    public void removeContact(Nameable contact) {

    }

    public void moveContact(Nameable contact, Group group) {
        removeContact(contact);
        addContact(contact, group);
    }

    public void addContactGroup(Group group) {
    }

    public void removeContactGroup(Group group) {
    }

    public Nameable getUser() {
        return new Nameable() {
            public String getName() {
                return getUserName();
            }

            public String toString() {
                return getUserName();
            }
        };
    }

    public void reconnect() {
        loggedIn = true;
    }

    /**
     * Overide this message with code that sends the message out.
     *
     * @param message to send
     * @throws java.io.IOException problems
     */
    protected void processMessage(Message message) throws IOException {
        // return a message back.
        message.setOutgoing(false);
    }

    /**
     * Overide this message with code that sends the message out.
     *
     * @param message to send
     * @throws java.io.IOException problems
     */
    protected void processSecureMessage(Message message) throws IOException {
    }

    /**
     * True if this is a system message.
     *
     * @param contact to check
     * @return true if a system message
     */
    public boolean isSystemMessage(Nameable contact) {
        return false;
    }

    /**
     * Starts a file transfer.
     *
     * @param ftl listener
     */
    public void initiateFileTransfer(FileTransferListener ftl) {
        //Todo change
    }

    /**
     * Sets up file for receival
     *
     * @param ftl            param
     * @param connectionInfo
     */
    public void acceptFileTransfer(FileTransferListener ftl, Object connectionInfo) {
        //Todo change
    }

    /**
     * Request to cancel the file transfer in progress.
     */
    public void rejectFileTransfer(Object object) {
        //Todo change
    }
} // class
