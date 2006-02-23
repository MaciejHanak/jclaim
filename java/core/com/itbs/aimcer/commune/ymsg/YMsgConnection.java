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

package com.itbs.aimcer.commune.ymsg;

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.commune.AbstractMessageConnection;
import com.itbs.aimcer.commune.ConnectionEventListener;
import com.itbs.aimcer.commune.FileTransferListener;
import ymsg.network.*;
import ymsg.network.event.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Vector;

/**
 * @author Created by Alex Rass on Mar 26, 2005
 */
public class YMsgConnection extends AbstractMessageConnection {//implements FileTransferSupport {
    // -----The session object - our way into the Yahoo API
    private Session session;
    private Nameable user;
    /** remembers last state. */
    private boolean disconnect;
    private static String EMPTY_EMAIL_STATE = "Unread Email Count: 0";
    private String emailState = EMPTY_EMAIL_STATE;

    /** Login Mode */
    public static final int SOCKS=0;
    public static final int HTTP=1;
    public static final int DIRECT=2;
    public static final int OTHER=3;
    private static final String ESCAPE = "\u001B[";

    public String getServiceName() {
        return "YAHOO";
    }

/*
    public String getSupportAccount() {
        return "SashasEmail";
    }
*/

    public void connect() throws SecurityException, Exception {
        super.connect();
        notifyConnectionInitiated();
        int mode = DIRECT;
        // -----Set the connection handler as per command line
        if (mode == SOCKS)
            session = new Session(new SOCKSConnectionHandler("autoproxy", 1080));
        else if (mode == HTTP)
            session = new Session(new HTTPConnectionHandler("http.pager.yahoo.com", 80));
//            session = new Session(new HTTPConnectionHandler("proxy", 8080));
        else if (mode == DIRECT)
            session = new Session(new DirectConnectionHandler());
            // ports 5050,23,25,80
        else
            session = new Session();
        // -----Register a listener
        session.addSessionListener(new SessionHandler());
        System.out.println(session.getConnectionHandler().toString());

//        todo see about this later
//        session.addTypingNotification(inputTF,username);
        new Thread() {
            public void run() {
                connectReal();
            }
        }.start();
    }

    public void connectReal() {
        try {
            session.login(getUserName(), getPassword());
            // tell everyone we are now running connected
            // use itertors b/c the size will change

            // go with login
            // -----Are we cooking with gas?
            if (session!=null && session.getSessionStatus()==StatusConstants.MESSAGING) {
                // -----Update identities list

                YahooGroup[] yg = session.getGroups();
                for (YahooGroup aYg : yg) {
                    Group group = getGroupFactory().create(aYg.getName());
                    Vector v = aYg.getMembers();
                    for (int j = 0; j < v.size(); j++) {
                        YahooUser yu = (YahooUser) v.elementAt(j);
                        Contact contact = getContactFactory().create(yu.getId(), YMsgConnection.this);
                        contact.getStatus().setOnline(yu.isLoggedIn());
                        contact.getStatus().setAway(isAway(yu));
//                        contact.setDisplayName(yu.toString());
                        group.add(contact);
                    }
                    getGroupList().add(group);
                }
/*
                YahooIdentity[] ids = session.getIdentities();
                // todo make up the groups
                Group group = GroupWrapper.create("Yahoo");
                for (int i = 0; i < ids.length; i++) {
                    YahooIdentity id = ids[i];
                    Contact contact = getContactFactory().create(id.getId(), this);
                    group.add(contact);
                }
                getGroupList().add(group);
*/

// todo see about this               currentIdentity=null;
                notifyConnectionEstablished();
                user = getContactFactory().create(getUserName(), this);
                disconnect = false;
            } else {
                for (ConnectionEventListener eventHandler : eventHandlers) {
                    eventHandler.connectionFailed(this, "Sorry, there was a problem connecting.");
                }
            }
        } catch (LoginRefusedException e) {
            String msg = "Login Refused.";
            switch ((int) e.getStatus()) {
                case (int) StatusConstants.STATUS_BADUSERNAME:
                    msg = "Yahoo doesn't recognise that username.";
                    break;
                case (int) StatusConstants.STATUS_BAD:
                    msg = "Yahoo refused our connection.  Password incorrect?";
                    break;
                case (int) StatusConstants.STATUS_LOCKED:
                    msg = "Your account is locked";
                    AccountLockedException e2 = (AccountLockedException) e;
                    if (e2.getWebPage() != null)
                        msg += "\nPlease visit: " + e2.getWebPage().toString();
                    break;
            }
            for (ConnectionEventListener eventHandler : eventHandlers) {
                eventHandler.connectionFailed(this, msg);
            }
        } catch(InterruptedIOException e) {
            for (ConnectionEventListener eventHandler : eventHandlers) {
                eventHandler.connectionFailed(this, "Timeout during connection.\n" + e.getMessage());
            }
        } catch(IOException e) {
            e.printStackTrace();
            for (ConnectionEventListener eventHandler : eventHandlers) {
                eventHandler.connectionFailed(this, "Problem connecting.\n" + e.getMessage());
            }
        }

    }

    /**
     * Only use when adding new buddies.
     * @param contact to add
     * @param group to add to
     */
    public void addContact(Nameable contact, Group group) {
        // do it for the server
        try {
            session.addFriend(contact.getName(), group.getName());
            group.add(contact);
            if (contact instanceof Contact)
                ((Contact) contact).getStatus().setOnline(true);
        } catch (IOException e) {
            for (ConnectionEventListener eventHandler : eventHandlers) {
                eventHandler.errorOccured("Failed to add your contact", e);
            }
        }
    }

    public void moveContact(Nameable contact, Group group) {
        boolean found = false;
        GroupList list = getGroupList();
        for (int i = list.size(); i>0 && !found; i--) {
            try {
                if (list.get(i).remove(contact) && !list.get(i).getName().equalsIgnoreCase(group.getName())) {
                    found = true;
                    session.removeFriend(contact.getName(), list.get(i).getName());
                }
            } catch (IOException e) {
                e.printStackTrace();  //Todo change?
            }
        }
        if (found)
            addContact(contact, group);
    }

    public void addContactGroup(Group group) {
        //todo implemement code for adding a group
    }

    public void removeContactGroup(Group group) {
        //todo implemement code for removing a group
    }

    /**
     * Use to remove contacts.
     * @param contact to delete
     */
    public void removeContact(Nameable contact) {
        // do it for the server
        GroupList list = getGroupList();
        for (int i = list.size(); i>0; i--) {
            try {
                if (list.get(i).remove(contact)) {
                    session.removeFriend(contact.getName(), list.get(i).getName());
                    return; // just 1 at a time, jik
                }
            } catch (IOException e) {
                e.printStackTrace();  //Todo change?
            }
        }
    }

    public Nameable getUser() {
        return user;
    }

    public void disconnect(boolean intentional) {
        disconnect = true;
        try {
            if (isLoggedIn())
                session.logout();
        } catch (Exception e) {
           // e.printStackTrace();  //We really don't care
        }
        session = null; // clear, gc
        super.disconnect(intentional);
        System.gc();
    }

    public void reconnect() {
        if (!disconnect && getGroupList().size() > 0)
            try {
                connect();
            } catch (Exception e) {
                e.printStackTrace();  //Todo change
            }
    }

    public boolean isLoggedIn() {
        return session != null && session.getSessionStatus() == StatusConstants.MESSAGING;
    }

    public void cancel() {
        // d/n apply to this library
    }

    public boolean isAway() {
        if (session != null) {
            return session.getStatus() != StatusConstants.STATUS_AVAILABLE;
        }
        return super.isAway();
    }

    public void setAway(boolean away) {
        if (session != null)
            try {
                if (away && getProperties().getIamAwayMessage().length() > 0) // STATUS_CUSTOM
                    session.setStatus(getProperties().getIamAwayMessage(), true);
                else
                    session.setStatus(away?StatusConstants.STATUS_BRB:StatusConstants.STATUS_AVAILABLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        super.setAway(away);
    }

    public void setTimeout(int timeout) {
//        this.timeout = timeout;
    }

    public void processMessage(Message message) throws IOException {
        session.sendMessage(message.getContact().getName(), message.getText());
    }

    public void processSecureMessage(Message message) throws IOException {
        throw new IOException("Sending secure messages is not possible with TOC");
    }


    /**
     * True if this is a system message.
     * @param contact to check
     * @return true if a system message
     */
    public boolean isSystemMessage(Nameable contact) {
        return false;
    }

    /**
     * Drops the escape seqences.
     * @param messageIn message to decode
     * @return html+plain text only message
     */
    private static String decodeMessage(String messageIn) {
        StringBuffer sb = new StringBuffer(messageIn);
        int startPos;
        while ((startPos = sb.indexOf(ESCAPE)) > -1 ) {
            // replace it
            sb.delete(startPos, startPos + ESCAPE.length());
            while (sb.length() > startPos) {
                sb.deleteCharAt(startPos);
                if (sb.length() > startPos && sb.charAt(startPos) == 'm') {
                    sb.deleteCharAt(startPos);
                    break;
                }
            }
        }
        return sb.toString();
    }

    private boolean isAway(YahooUser yu) {
        return yu.getStatus() != StatusConstants.STATUS_AVAILABLE && yu.getStatus() != StatusConstants.STATUS_TYPING;
    }

    /**
     * Starts a file transfer.
     *
     * @param ftl listener
     */
    public void initiateFileTransfer(FileTransferListener ftl) throws IOException {
        session.sendFileTransfer(ftl.getContactName(), ftl.getFile().getAbsolutePath(), ftl.getFileDescription());
    }

    /**
     * Sets up file for receival
     *
     * @param ftl param
     * @param connectionInfo
     */
    public void acceptFileTransfer(FileTransferListener ftl, Object connectionInfo) {
        try {
            session.saveFileTransferAs((SessionFileTransferEvent) connectionInfo, ftl.getFile().getAbsolutePath());
        } catch (IOException e) {
            for (ConnectionEventListener eventHandler : eventHandlers) {
                eventHandler.errorOccured("ERROR while transfering file: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Request to cancel the file transfer in progress.
     */
    public void rejectFileTransfer() {
    }

    // *****************************************************************
    // YMSG9 session handler
    // *****************************************************************
    class SessionHandler extends SessionAdapter {
        public void messageReceived(SessionEvent ev) { //<font size="10">\u001B[30mk\u001B[x1m<font size="10">
            String decodedMessage = "";
            if (ev.getMessage()!=null)
                decodedMessage = decodeMessage(ev.getMessage());
            Message message = new MessageImpl(getContactFactory().create(ev.getFrom(), YMsgConnection.this), false, decodedMessage);
            for (ConnectionEventListener eventHandler : eventHandlers) {
                try {
                    eventHandler.messageReceived(YMsgConnection.this, message);
                } catch (Exception exc) {
                    for (ConnectionEventListener eventListener: eventHandlers) {
                        eventListener.errorOccured("Error processing message.", exc);
                    }
                }
            }
        }

        public void errorPacketReceived(SessionErrorEvent ev) {
            if (ev.getService() != ServiceConstants.SERVICE_CONTACTIGNORE) {
                for (ConnectionEventListener eventHandler : eventHandlers) {
                    eventHandler.errorOccured("ERROR received from yahoo network: " + ev.getMessage(), null);
                }
            }
        }

        public void inputExceptionThrown(SessionExceptionEvent ev) {
            for (ConnectionEventListener eventHandler : eventHandlers) {
                eventHandler.errorOccured("Input exception: " + ev.getMessage(), ev.getException());
            }
            if (ev.getException() instanceof YMSG9BadFormatException) {
                YMSG9BadFormatException ex = (YMSG9BadFormatException) ev.getException();
                System.err.println("**Cause:");
                ex.getCausingThrowable().printStackTrace();
            }
        }

        public void offlineMessageReceived(SessionEvent ev) {
            messageReceived(ev);
//            decoder.decodeToText(ev.getMessage());
        }

        public void notifyReceived(SessionNotifyEvent sessionNotifyEvent) {
            if ("TYPING".equals(sessionNotifyEvent.getType()))
                for (ConnectionEventListener eventHandler : eventHandlers) {
                    eventHandler.typingNotificationReceived(YMsgConnection.this,  getContactFactory().create(sessionNotifyEvent.getFrom(), YMsgConnection.this));
                }
        }

        public void fileTransferReceived(SessionFileTransferEvent ev) {
            String decodedMessage = "File: " + ev.getFilename();
            if (ev.getLocation()!=null)
                decodedMessage +=  " " + ev.getLocation().toString();
            Message message = new MessageImpl(getContactFactory().create(ev.getFrom(), YMsgConnection.this), false, decodedMessage);
            for (ConnectionEventListener eventHandler : eventHandlers) {
                try {
                    eventHandler.messageReceived(YMsgConnection.this, message);
                } catch (Exception exc) {
                    for (ConnectionEventListener eventListener: eventHandlers) {
                        eventListener.errorOccured("Error processing transfer.", exc);
                    }
                }
            }

/*
            System.out.println(ev.getLocation().toString());
            Contact contact = getContactFactory().get(ev.getFrom(), YMsgConnection.this);
            if (contact == null) return; // ignore random ppl sending files.  leads to no-good
            for (ConnectionEventListener eventHandler : eventHandlers) {
//                eventHandler.fileReceiveRequested(YMsgConnection.this, contact, ev.getFilename(), ev.getMessage(), ev);
            }
*/
        }

        public void connectionClosed(SessionEvent ev) {
            disconnect(false);
        }

        public void listReceived(SessionEvent ev) {
            System.out.println("list received " + ev);
        }

        public void friendsUpdateReceived(SessionFriendEvent ev) {
            for (YahooUser aYu : ev.getFriends()) {
//                System.out.println("Updated: " + yu[i].toString());
                Contact contact = getContactFactory().create(aYu.getId(), YMsgConnection.this);
                for (ConnectionEventListener eventHandler : eventHandlers) { //online: info.getOnSince().getTime() > 0
                    eventHandler.statusChanged(YMsgConnection.this, contact, aYu.isLoggedIn(), isAway(aYu), 0);
                }
            }
        }

        public void friendAddedReceived(SessionFriendEvent ev) {
            System.out.println("friendAddedReceived " + ev.toString());
            Group group = getGroupFactory().create(ev.getGroup());
            group.add(getContactFactory().create(ev.getFriend().getId(), YMsgConnection.this));
            getGroupList().add(group);
        }

        public void friendRemovedReceived(SessionFriendEvent ev) {
            System.out.println("friendRemovedReceived " + ev.toString());
        }

        public void contactRequestReceived(SessionEvent ev) {
            boolean accept = true;
            for (ConnectionEventListener eventHandler : eventHandlers) { //online: info.getOnSince().getTime() > 0
                accept = eventHandler.contactRequestReceived(ev.getFrom(), YMsgConnection.this);
                if (!accept) break;
            }
            if (!accept)
                try {
                    session.rejectContact(ev, "Not now, thanks");
                } catch (IOException e) {
                    e.printStackTrace();  //Todo change
                }

        }

        public void conferenceInviteReceived(SessionConferenceEvent ev) {
            System.out.println(ev.toString());
            try {
                session.declineConferenceInvite(ev.getRoom(), "Sorry!");
            } catch (IOException e) {
                //
            }
        }

        public void conferenceInviteDeclinedReceived(SessionConferenceEvent ev) {
            System.out.println(ev.toString());
        }

        public void conferenceLogonReceived(SessionConferenceEvent ev) {
            System.out.println(ev.toString());
        }

        public void conferenceLogoffReceived(SessionConferenceEvent ev) {
            System.out.println(ev.toString());
        }

        public void conferenceMessageReceived(SessionConferenceEvent ev) {
            System.out.println("conferenceMessageReceived " + ev.toString());
        }

        public void chatLogonReceived(SessionChatEvent ev) {
/*
            _appendOutput("[" + ev.getLobby().getNetworkName() + "]  " + ev.getChatUser().getId() + " joined\n");
            _pushDown();
*/
        }

        public void chatLogoffReceived(SessionChatEvent ev) {
/*
            _appendOutput("[" + ev.getLobby().getNetworkName() + "]  " + ev.getChatUser().getId() + " has left\n");
            _pushDown();
*/
        }

        public void chatMessageReceived(SessionChatEvent ev) {
/*
            String u = ev.getChatUser().getId(), m = ev.getMessage();
            int spam = spamBlock.getViolations(u, m);
            if (spam > 0) {
                System.out.println("Blocked: " + u + "/" + spam + ": " + m);
            } else {
                if (ev.isEmote()) m = "<" + m + ">";
                _appendOutput("[" + ev.getLobby().getNetworkName() + "]  " + u + " : ");
                decoder.appendToDocument(m, outputDoc);
                _pushDown();
                _log(m);
            }
*/
        }

        public void chatConnectionClosed(SessionEvent ev) {
            System.out.println("**Chat connection closed**");
        }

        public void newMailReceived(SessionNewMailEvent event) {
            final String text = (event.getEmailAddress()==null?"":("Yahoo! mail from: " + event.getEmailAddress() + "\n"))
                    + (event.getSubject()==null?"":("Subject: " + event.getSubject() + "\n"))
                    + "Unread Email Count: " + event.getMailCount();
            if (EMPTY_EMAIL_STATE.equals(text) || emailState.equals(text)) // same as empty or last 
                return;
            emailState = text;

            Message message = new MessageImpl(getContactFactory().create(getUserName(), YMsgConnection.this), false, text);
            for (ConnectionEventListener eventHandler : eventHandlers) {
                try {
                    eventHandler.emailReceived(YMsgConnection.this, message);
                } catch (Exception exc) {
                    for (ConnectionEventListener eventListener: eventHandlers) {
                        eventListener.errorOccured("Error processing email.", exc);
                    }
                }
            }
        }
    } // class SessionHandler

} // class JAIMConnecion
