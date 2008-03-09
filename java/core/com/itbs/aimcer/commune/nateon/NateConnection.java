package com.itbs.aimcer.commune.nateon;

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.commune.AbstractMessageConnection;
import com.itbs.aimcer.commune.ConnectionEventListener;
import kfmes.natelib.NateonMessenger;
import kfmes.natelib.SwitchBoardSession;
import kfmes.natelib.entity.GroupList;
import kfmes.natelib.entity.NateFile;
import kfmes.natelib.entity.NateFriend;
import kfmes.natelib.entity.NateGroup;
import kfmes.natelib.event.NateListener;
import kfmes.natelib.ftp.FileRecver;
import kfmes.natelib.ftp.FileSender;
import kfmes.natelib.msg.InstanceMessage;
import kfmes.natelib.msg.MimeMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides connection to Nate.com.
 *
 * @author Alex Rass
 * @since Feb 15, 2008
 */
public class NateConnection extends AbstractMessageConnection {
    private static final Logger log = Logger.getLogger(NateConnection.class.getName());
    NateonMessenger connection;

    public NateConnection() {
    }

    /**
     * Non-blocking call.
     */
    public void connect() throws SecurityException, Exception {
        super.connect();
        notifyConnectionInitiated();
        new Thread() {
            public void run() {
                connectReal();
            }
        }.start();
    }

    public void connectReal(){
        try {
            connection = new NateonMessenger();
            NateonMessenger.setDebugMode(true);
//            connection.loginAnonymously();
            connection.login(getUserName(), getPassword());
            fireConnect();
            connection.fireBuddyListModified();
        } catch (Exception e) {
            log.log(Level.INFO, "Login exception ",e);
            disconnect(false);
            notifyConnectionFailed("Connection Failed. " + e.getMessage());
        }
    }

    private void fireConnect() {
        connection.addNateListener(new NateListener() {
            public void instanceMessageReceived(InstanceMessage instanceMessage) {
                try { // try and not kill Smack!
                    Message message;
                    String from = instanceMessage.getFrom();
                    message = new MessageImpl(getContactFactory().create(from, NateConnection.this),
                                false, false, instanceMessage.getMessage());
                    for (int i = 0; i < eventHandlers.size(); i++) {
                        try {
                            (eventHandlers.get(i)).messageReceived(NateConnection.this, message);
                        } catch (Exception e) {
                            for (ConnectionEventListener eventHandler : eventHandlers) {
                                eventHandler.errorOccured(i + ": Failure while processing a received message.", e);
                            }
                        }
                    }
                } catch (Exception e) {
                    for (ConnectionEventListener eventHandler : eventHandlers) {
                        eventHandler.errorOccured("Failure while receiving a message", e);
                    }
                }
            }

            public void loginComplete() {
                notifyConnectionEstablished();
            }

            public void loginError(String string) {
                notifyConnectionFailed(string);
            }

            public void listAdd(NateFriend nateFriend) {
                //TODO Change
            }

            public void listOnline(NateFriend nateFriend) {
                //TODO Change
            }

            public void userOnline(NateFriend nateFriend) {
                Contact contact = getContactFactory().create(nateFriend.getID(), NateConnection.this);
                contact.getStatus().setOnline(true);
            }

            public void userOffline(NateFriend nateFriend) {
                Contact contact = getContactFactory().create(nateFriend.getID(), NateConnection.this);
                contact.getStatus().setOnline(false);
            }

            public void switchboardSessionStarted(SwitchBoardSession switchBoardSession) {
                //TODO Change
            }

            public void whoJoinSession(SwitchBoardSession switchBoardSession, NateFriend nateFriend) {
                //TODO Change
            }

            public void whoPartSession(SwitchBoardSession switchBoardSession, NateFriend nateFriend) {
                //TODO Change
            }

            public void switchboardSessionEnded(SwitchBoardSession switchBoardSession) {
                //TODO Change
            }

            public void switchboardSessionAbandon(SwitchBoardSession switchBoardSession, String string) {
                //TODO Change
            }

            public void progressTyping(SwitchBoardSession switchBoardSession, NateFriend nateFriend, int i) {
                //TODO Change
            }

            public void chatMessageReceived(SwitchBoardSession switchBoardSession, NateFriend nateFriend, MimeMessage mimeMessage) {
                //TODO Change
            }

            public void filePosted(SwitchBoardSession switchBoardSession, FileRecver fileRecver, NateFriend nateFriend, ArrayList<NateFile> arrayList) {
                //TODO Change
            }

            public void fileSend(SwitchBoardSession switchBoardSession, FileSender fileSender, NateFriend nateFriend, ArrayList<NateFile> arrayList) {
                //TODO Change
            }

            public void fileSendAccepted(SwitchBoardSession switchBoardSession, String string) {
                //TODO Change
            }

            public void fileSendRejected(SwitchBoardSession switchBoardSession, String string, String string1) {
                //TODO Change
            }

            public void fileSendStarted(FileSender fileSender) {
                //TODO Change
            }

            public void fileSendEnded(FileSender fileSender, NateFile nateFile) {
                //TODO Change
            }

            public void fileSendError(FileSender fileSender, Throwable throwable) {
                //TODO Change
            }

            public void whoAddedMe(NateFriend nateFriend) {
                //TODO Change
            }

            public void whoRemovedMe(NateFriend nateFriend) {
                //TODO Change
            }

            public void buddyListModified() {
                buddyListInit(connection.getBuddyGroup());
            }

            public void addFailed(int i) {
                //TODO Change
            }

            public void renameNotify(NateFriend nateFriend) {
                Contact contact = getContactFactory().create(nateFriend.getID(), NateConnection.this);
                contact.setDisplayName(nateFriend.getNameNick());
            }

            public void allListUpdated() {
                buddyListInit(connection.getBuddyGroup());
            }

            public void logoutNotify() {
                notifyConnectionLost();
            }

            public void notifyUnreadMail(Properties properties, int i) {
                notifyEmailReceived(new MessageImpl(getContactFactory().create(getUserName(), NateConnection.this), false, properties.toString()));
            }

            public void buddyListInit(GroupList groupList) {
                ///////////////////
                // get user list //
                //////////////////
                Group lastGroup;
                Contact contact;
                for (NateGroup rosterGroup : groupList.getList()) {
                    getGroupList().add(lastGroup = getGroupFactory().create(rosterGroup.getName()));
                    lastGroup.clear(NateConnection.this); // b/c of reconnect
                    for (NateFriend rosterEntry : rosterGroup.getList()) {
                        contact = getContactFactory().create(rosterEntry.getID(), NateConnection.this);
                        contact.setDisplayName(rosterEntry.getNickName());
                        lastGroup.add(contact);
                    }
                } // while
                for (ConnectionEventListener eventHandler : eventHandlers) {
                    eventHandler.statusChanged(NateConnection.this);
                }

            }

            public void buddyModified(NateFriend nateFriend) {
                Contact contact = getContactFactory().create(nateFriend.getID(), NateConnection.this);
                log.info("Friend: " + nateFriend + " FABR: " + nateFriend.getFABR() + " Status: " + nateFriend.getStatus());
                for (ConnectionEventListener eventHandler : eventHandlers) {
                    eventHandler.statusChanged(NateConnection.this, contact, true, false, 0);
                }

/*              // Next version will have this code:
                Status oldStatus = (Status) contact.getStatus().clone();
                contact.getStatus().setOnline(true);
                contact.getStatus().setAway(false);
                contact.getStatus().setIdleTime(0);

                for (ConnectionEventListener eventHandler : eventHandlers) {
                    eventHandler.statusChanged(NateConnection.this, contact, oldStatus);
                }
*/
            }

            public void killed() {
                log.info("Killed");
                //TODO Change
            }

            public void OwnerStatusUpdated() {
                log.info("OwnerStatusUpdated");
                //TODO Change
            }

            public void fileRecved(FileRecver fileRecver, NateFile nateFile) {
                //TODO Change
            }

            public void fileTransferAborted(String string) {
                //TODO Change
            }
        });

/*
        fileTransferManager = new FileTransferManager(connection);
        fileTransferManager.addFileTransferListener(new org.jivesoftware.smackx.filetransfer.FileTransferListener() {
            public void fileTransferRequest(FileTransferRequest fileTransferRequest) {
                for (ConnectionEventListener eventHandler : eventHandlers) {
                    eventHandler.fileReceiveRequested(NateConnection.this, getContactFactory().create(fileTransferRequest.getRequestor(), NateConnection.this), fileTransferRequest.getFileName(), fileTransferRequest.getDescription(), fileTransferRequest);
                }
            }
        });
*/

    } // fireConnect

    public void disconnect(boolean intentional) {
        if (connection!=null) {
            try {
                connection.logout();
            } catch (IOException e) {
                log.log(Level.WARNING, "Failed to log out? WOW!", e);
            }
            connection = null;  // they don't quite get it, so we have to.
        }
        super.disconnect(intentional);
    }

    public void reconnect() {
        try {
            try {
                connection.logout();
            } catch (IOException e) {
                log.log(Level.WARNING, "Failed to log out? WOW!", e);
            }
            connect();
        } catch (Exception e) {
//            GeneralUtils.sleep(1000);
            log.log(Level.SEVERE, "",e);
        }
    }

    public boolean isLoggedIn() {
        return connection!=null && connection.isLoggedIn();
    }

    /**
     * Cancel login.
     */
    public void cancel() {
        if (!isLoggedIn())
            disconnect(true);
    }

    public void setTimeout(int timeout) {
    }

    public void addContact(Nameable contact, Group group) {
//        String[] groupNames = new String[1];
//        groupNames[0] = group.getName();
        try {
            NateFriend friend = connection.getBuddyGroup().getFriendUid(contact.getName());
            connection.getBuddyGroup().addFriend(1, friend);
        } catch (Exception e) {
            for (ConnectionEventListener connectionEventListener : eventHandlers) {
                connectionEventListener.errorOccured("Failed to add a contact " + contact.getName(), e);
            }
        }
        group.add(contact);
    }

    public void removeContact(Nameable contact) {
//        NateFriend friend = connection.getBuddyGroup().getFriendUid(contact.getName());
        connection.getBuddyGroup().removeFriend(contact.getName());
    }

    public void moveContact(Nameable contact, Group group) {
        removeContact(contact);
        addContact(contact, group);
    }

    public void addContactGroup(Group group) {
        connection.getBuddyGroup().add(1, new NateGroup("G1", "G2", group.getName()));
    }

    public void removeContactGroup(Group group) {
        connection.getBuddyGroup().remove(new NateGroup("G1", "G2", group.getName()));
    }

    /**
     * Returns a short name for the service.
     * "AIM", "ICQ" etc.
     *
     * @return service name
     */
    public String getServiceName() {
        return "Nate.com";
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
     * Sets the away flag.
     *
     * @param away true if so
     */
    public void setAway(boolean away) {
        connection.setMyStatus(away?getProperties().getIamAwayMessage():"");
        super.setAway(away);
    }

    /**
     * Overide this message with code that sends the message out.
     *
     * @param message to send
     * @throws java.io.IOException problems
     */
    protected void processMessage(Message message) throws IOException {
        NateFriend owner = connection.getOwner();
        InstanceMessage msg = new InstanceMessage(owner.getNateID(), message.getContact().getName(), message.getText());
        connection.sendIMessage(msg);
//        connection.sendMessage(message.getContact().getName(), message.getText()); // this is old/bad method.
    }

    /**
     * Overide this message with code that sends the message out.
     *
     * @param message to send
     * @throws java.io.IOException problems
     */
    protected void processSecureMessage(Message message) throws IOException {
        processMessage(message); // todo go secure at some point
    }

    public String getDefaultIconName() {
        return "jabber.gif";
    }



} // class SmackConnection
