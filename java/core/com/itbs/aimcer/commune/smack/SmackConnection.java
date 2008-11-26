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

package com.itbs.aimcer.commune.smack;

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.commune.*;
import com.itbs.util.GeneralUtils;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jivesoftware.smackx.muc.SubjectUpdatedListener;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides connection to Jabber.
 *
 * @author Alex Rass
 * @since Dec 24, 2004
 */
public class SmackConnection extends AbstractMessageConnection implements FileTransferSupport, ChatRoomSupport {
    private static final Logger log = Logger.getLogger(SmackConnection.class.getName());
    public static final int DEFAULT_PORT = 5222;
    public static final int DEFAULT_PORT_SSL = 5223;
    XMPPConnection connection;
    FileTransferManager fileTransferManager;
    MultiUserChat multiUserChat;


    public SmackConnection() {
        serverName = "jabber.org";
        serverPort = DEFAULT_PORT;
    }

    protected XMPPConnection getNewConnection() throws XMPPException {
        return new XMPPConnection(new ConnectionConfiguration(
                System.getProperty("JABBER_HOST", getServerName()),
                Integer.getInteger("JABBER_PORT", getServerPort())
                ));
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
            connection = getNewConnection();
//            connection.loginAnonymously();
            connection.connect();
            SASLAuthentication.supportSASLMechanism("PLAIN", 0);
            connection.login(getUserName(), getPassword());
            fireConnect();
        } catch (XMPPException e) {
            log.log(Level.INFO, "",e);
            disconnect(false);
            String error = e.getXMPPError() == null ? e.getMessage() : e.getXMPPError().getMessage();
            error = error == null ? "" : error;
            notifyConnectionFailed("Connection Failed. " + error);
        } catch (RuntimeException e) { // Lets see if we see this with Google.
            log.log(Level.SEVERE, "UNCOUGHT EXCEPTION! PLEASE FIX!", e);
            disconnect(false);
            notifyConnectionFailed("Connection Failed. UNUSUAL TERMINATION!" + e.getMessage());
        }
    }

    private void fireConnect() {
        connection.addConnectionListener(new ConnectionListener() {
            public void connectionClosed() {
                notifyConnectionLost();
            }

            public void connectionClosedOnError(Exception e) {
                notifyConnectionLost();
            }

            /**
             * The connection will retry to reconnect in the specified number of seconds.
             *
             * @param seconds remaining seconds before attempting a reconnection.
             */
            public void reconnectingIn(int seconds) {
                //TODO Change
            }

            /**
             * The connection has reconnected successfully to the server. Connections will
             * reconnect to the server when the previous socket connection was abruptly closed.
             */
            public void reconnectionSuccessful() {
                notifyConnectionEstablished();
            }

            /**
             * An attempt to connect to the server has failed. The connection will keep trying
             * reconnecting to the server in a moment.
             *
             * @param e the exception that caused the reconnection to fail.
             */
            public void reconnectionFailed(Exception e) {
                notifyConnectionLost();
            }
        });
          //////////////////////
        // Ask the user for individual requests //
        /////////////////////
        //connection.getRoster().setSubscriptionMode(Roster.SubscriptionMode.accept_all);
        //connection.getRoster().setSubscriptionMode(Roster.SubscriptionMode.reject_all);
        connection.getRoster().setSubscriptionMode(Roster.SubscriptionMode.manual);        
        //~~~~~~~~~~~~~~~~~~~~~
        // ! aa
        
        PacketFilter addedFiter = new org.jivesoftware.smack.filter.PacketTypeFilter(org.jivesoftware.smack.packet.Presence.class);
        		      
        // aa: create a packet listener for presence packets.
        PacketListener subscribeRequestListener = new PacketListener() {
                public void processPacket(Packet packet) {
                	Presence pp = (Presence) packet;  //shouldn't be a problem since we are filtering for presence packets.
                	// only listen to presence requests
                	if (pp. getType() == Presence.Type.subscribe) { //Request subscription to recipient's presence.
                        boolean accept = true;
                        for (ConnectionEventListener eventHandler : eventHandlers) { //online: info.getOnSince().getTime() > 0
                            accept = eventHandler.contactRequestReceived(packet.getFrom(), SmackConnection.this);
                            if (!accept) break;
                        }    
                        
                        Presence response;
                        if (accept)  
                        	response = new Presence(Presence.Type.subscribed);
                        else 
                        	response  = new Presence(Presence.Type.unsubscribed);
                        	                     
                        response.setTo(pp.getFrom());
                        connection.sendPacket(response);                           
                	} //type subscribe         	
                } // processPacket
            };
//         Register the subscribeRequestListener.
        connection.addPacketListener(subscribeRequestListener, addedFiter);
        
        //~aa
        //~~~~~~~~~~~~~~~~~~~~~

          ///////////////////
         // get user list //
        //////////////////
        Group lastGroup;
        Contact contact;
        for (RosterGroup rosterGroup : connection.getRoster().getGroups()) {
            getGroupList().add(lastGroup = getGroupFactory().create(rosterGroup.getName()));
            lastGroup.clear(this); // b/c of reconnect
            for (RosterEntry rosterEntry : rosterGroup.getEntries()) {
                contact = getContactFactory().create(rosterEntry.getUser(), this);
                if (rosterEntry.getName() != null) {
                    contact.setDisplayName(rosterEntry.getName());
                }
                lastGroup.add(contact);
            }
        } // while

        /////////////////////
         // Handle Messages //
        ////////////////////
        // Create a packet filter to listen for new messages from a particular
        // user. We use an AndFilter to combine two other filters.
        PacketFilter filter = new MessageTypeFilter(org.jivesoftware.smack.packet.Message.Type.chat);
        // Next, create a packet listener. We use an anonymous inner class for brevity.
        PacketListener messageListener = new PacketListener() {
            public void processPacket(Packet packet) {
                try { // try and not kill Smack!
                    Message message;
                    String from = normalizeName(packet.getFrom());
                    if (packet instanceof org.jivesoftware.smack.packet.Message) {
                        org.jivesoftware.smack.packet.Message smackMessage = (org.jivesoftware.smack.packet.Message) packet;
                        message = new MessageImpl(getContactFactory().create(from, SmackConnection.this),
                                false, false, smackMessage.getBody());
                    } else
                        message = new MessageImpl(getContactFactory().create(from, SmackConnection.this),
                                false, false, (String)packet.getProperty("body"));
                    for (int i = 0; i < eventHandlers.size(); i++) {
                        try {
                            (eventHandlers.get(i)).messageReceived(SmackConnection.this, message);
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
        };

        // Register the listener.
        connection.addPacketListener(messageListener, filter);


        connection.getRoster().addRosterListener(new RosterListener() {
            public void entriesAdded(Collection<String> addresses) {
/*            	for (String adr: addresses) {
            		System.out.println("entriesAdded " + adr);            		
            	}     */       	
            }

            public void entriesUpdated(Collection<String> addresses) {
/*            	for (String adr: addresses) {
            		System.out.println("entriesUpdated " + adr);            		
            	}            	*/
            }

            public void entriesDeleted(Collection<String> addresses) {
/*            	for (String adr: addresses) {
            		System.out.println("entriesDeleted " + adr);            		
            	}*/
            }

            public void presenceChanged(Presence presence) {
                Contact contact = getContactFactory().create(normalizeName(presence.getFrom()), SmackConnection.this);
                Status status = (Status) contact.getStatus().clone();
                contact.getStatus().setOnline(presence.isAvailable());
                //isAway also includes do not disturb (dnd), so we should not update in that case.
                contact.getStatus().setAway(presence.isAway() && Presence.Mode.dnd != presence.getMode());
                contact.getStatus().setIdleTime(0);
                notifyStatusChanged(contact, status);
            }

        }); // class RosterListener

        
        
        // File transfers support:
        fileTransferManager = new FileTransferManager(connection);
        fileTransferManager.addFileTransferListener(new org.jivesoftware.smackx.filetransfer.FileTransferListener() {
            public void fileTransferRequest(FileTransferRequest fileTransferRequest) {
                for (ConnectionEventListener eventHandler : eventHandlers) {
                    eventHandler.fileReceiveRequested(SmackConnection.this, getContactFactory().create(fileTransferRequest.getRequestor(), SmackConnection.this), fileTransferRequest.getFileName(), fileTransferRequest.getDescription(), fileTransferRequest);
                }
            }
        });

        for (ConnectionEventListener eventHandler : eventHandlers) {
            eventHandler.statusChanged(this);
        }

        // tell everyone we are now running connected
        // use itertors b/c the size will change
        notifyConnectionEstablished();
    } // fireConnect

    private String normalizeName(String userName) {
        int index = userName.indexOf('/');
        if (index > 0 ) { // yes, 0
            return userName.substring(0, index);
        }
        return userName;
    }

    public void disconnect(boolean intentional) {
        if (connection!=null) {
            connection.disconnect();
            connection = null;  // they don't quite get it, so we have to.
        }
        super.disconnect(intentional);
    }

    public void reconnect() {
        try {
            if (connection!=null) {
                connection.disconnect();
            }
            connect();
        } catch (Exception e) {
//            GeneralUtils.sleep(1000);
            log.log(Level.SEVERE, "",e);
        }
    }

    public boolean isLoggedIn() {
        return connection!=null && connection.isConnected();
    }

    /**
     * Cancel login.
     */
    public void cancel() {
        if (!isLoggedIn())
            connection.disconnect();
    }

    public void setTimeout(int timeout) {
    }

    public void addContact(Nameable contact, Group group) {
        String[] groupNames = new String[1];
        groupNames[0] = group.getName();
        try {
            connection.getRoster().createEntry(fixUserName(contact.getName()), contact.getName(), groupNames);
        } catch (XMPPException e) {
            for (ConnectionEventListener connectionEventListener : eventHandlers) {
                connectionEventListener.errorOccured("Failed to add a contact " + contact.getName(), e);
            }
        }
        group.add(contact);
    }

    /**
     * Used to fix the usernames for the jabber protocol.<br>
     * Usernames need server name.
     * @param name of the user's account to fix
     * @return name, including server.
     */
    protected String fixUserName(String name) {
        if (name.indexOf('@')>-1) return name;
        return name + "@" + getServerName();
    }

    public boolean removeContact(Nameable contact, Group group) {
        for (RosterGroup rosterGroup : connection.getRoster().getGroups()) {
            if (group==null || rosterGroup.getName().equalsIgnoreCase(group.getName())) { // for the right group
                for (RosterEntry rosterEntry : rosterGroup.getEntries()) {
                    if (rosterEntry.getName().equals(contact.getName())) {
                        try {
                            rosterGroup.removeEntry(rosterEntry);
                            cleanGroup(group, contact); // if you change code, make sure this executes once only!
                        } catch (XMPPException e) {
                            notifyErrorOccured("Found, but failed to remove the contact", e);
                            return false;
                        }
                        return true;
                    }
                }
            }
        } // for
        return false;
    }

    public void moveContact(Nameable contact, Group oldGroup, Group newGroup) {
        removeContact(contact, oldGroup);
        addContact(contact, newGroup);
    }

    public void addContactGroup(Group group) {
        connection.getRoster().createGroup(group.getName());
    }

    public void removeContactGroup(Group group) {
//        connection.getRoster(). // todo figure out
    }

    /**
     * Returns a short name for the service.
     * "AIM", "ICQ" etc.
     *
     * @return service name
     */
    public String getServiceName() {
        return "Jabber";
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
        if (connection != null && connection.isConnected()) {
            Presence presence = new Presence(Presence.Type.available);
            if (away) { // change default values to:
                presence.setMode(Presence.Mode.away);
                presence.setStatus(getProperties().getIamAwayMessage());
            }
            connection.sendPacket(presence);
        } else {
            // Shouldn't be calling us for this unless smth broke inside smack lib again.
            notifyConnectionFailed("Connection unavailable.");
        }
        super.setAway(away);
    }

    /**
     * Overide this message with code that sends the message out.
     *
     * @param message to send
     * @throws java.io.IOException problems
     */
    protected void processMessage(Message message) throws IOException {
        Chat chat = connection.getChatManager().createChat(message.getContact().getName(), null); 
        try {
            chat.sendMessage(message.getText());
        } catch (XMPPException e) {
            log.log(Level.SEVERE, "",e);
            throw new IOException(e.getMessage());
        }
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

/*
    public void createAccount(AccountInfo accountInfo) {
        Registration registration = new Registration();
        Map <String, String> attributes = new HashMap<String, String>(2);
        attributes.put("name", accountInfo.getUserName());
        attributes.put("password", accountInfo.getPassword());
        registration.setAttributes(attributes);
        // todo populate other fields
        connection.sendPacket(registration);
    }

    public AccountInfo getAccountInfo() {
        Registration registration = new Registration();
        connection.sendPacket(registration);
        AccountInfo accountInfo = new AccountInfo(registration.getUsername(), registration.getPassword());
        // todo collect other fields
        return accountInfo;
    }
*/

    public String getDefaultIconName() {
        return "jabber.gif";
    }

    public void initiateFileTransfer(FileTransferListener ftl) throws IOException {
        ftl.notifyNegotiation();
        OutgoingFileTransfer oft = fileTransferManager.createOutgoingFileTransfer(fixUserName(ftl.getContactName()));
        try {
            ftl.notifyTransfer();
            oft.sendFile(ftl.getFile(), ftl.getFileDescription());
        } catch (XMPPException e) {
            ftl.notifyFail();
            log.log(Level.SEVERE, "Transfer failed", e);
        }
    }

    public void acceptFileTransfer(FileTransferListener ftl, Object connectionInfo) {
        FileTransferRequest fileTransferRequest = (FileTransferRequest) connectionInfo;
        ftl.notifyNegotiation();
        IncomingFileTransfer ift = fileTransferRequest.accept();
        try {
            ftl.notifyTransfer();
            ift.recieveFile(ftl.getFile());
        } catch (XMPPException e) {
            ftl.notifyFail();
            log.log(Level.SEVERE, "Transfer failed", e);
        }
    }

    public void rejectFileTransfer(Object connectionInfo) {
        FileTransferRequest fileTransferRequest = (FileTransferRequest) connectionInfo;
        fileTransferRequest.reject();
    }

    // ***********************  GROUP CHAT  *************************
    public void join(String room, final String nickname, final ChatRoomEventListener listener) {
        join(false, room, nickname, listener);
    }
    public void create(String room, String nickname, ChatRoomEventListener listener) {
        join(true, room, nickname, listener);
    }

    private void join(boolean create, String room, final String nickname, final ChatRoomEventListener listener) {
        try {
            multiUserChat = new MultiUserChat(connection, room);

            // The room service will decide the amount of history to send
            if (create)
                multiUserChat.create(nickname);
            else
                multiUserChat.join(nickname);

            // Discover information about the room roomName@conference.myserver
            RoomInfo info = MultiUserChat.getRoomInfo(connection, room);
            if (info.getOccupantsCount() != -1)
                listener.serverNotification("Number of occupants: " + info.getOccupantsCount());
//                addHistoryText("Number of occupants: " + info.getOccupantsCount(), ATT_BLUE);
            if (GeneralUtils.isNotEmpty(info.getSubject()))
                listener.serverNotification("Room Subject: " + info.getSubject());
            // listen for subject change and update
            multiUserChat.addSubjectUpdatedListener(new SubjectUpdatedListener() {
                public void subjectUpdated(String subject, String from) {
                    listener.serverNotification("Room Subject:" + subject);
                }
            });
            multiUserChat.addMessageListener(new PacketListener() {
                public void processPacket(Packet packet) {
                    int lastSlash = packet.getFrom().lastIndexOf('/');

                    String from = lastSlash == -1 || lastSlash >= packet.getFrom().length()?
                            packet.getFrom().trim() :
                            packet.getFrom().substring(lastSlash+1);
                    // ignore messages from self!
                    if (from.equalsIgnoreCase(nickname))
                        return;
                    if (packet instanceof org.jivesoftware.smack.packet.Message) {
                        org.jivesoftware.smack.packet.Message message = (org.jivesoftware.smack.packet.Message) packet;
                        try {
                            listener.messageReceived(SmackConnection.this, new MessageImpl(getContactFactory().create(from, SmackConnection.this), false, message.getBody()));
                        } catch (Exception e) {
                            listener.errorOccured("Error receiving message: " + e.getMessage(), e);
                        }
                    }
                }
            });
        } catch (XMPPException e) {
            listener.errorOccured("Error connecting to the room: " + e.getMessage(), e);
        }

    }

    public void sendChatMessage(String message) {
        if (isJoined())
            try {
                multiUserChat.sendMessage(message);
            } catch (XMPPException e) {
                for (ConnectionEventListener eventHandler : eventHandlers) {
                    eventHandler.errorOccured("Failure while sending a message", e);
                }
            }
    }

    public boolean isJoined() {
        return multiUserChat!=null && multiUserChat.isJoined();
    }
} // class SmackConnection
