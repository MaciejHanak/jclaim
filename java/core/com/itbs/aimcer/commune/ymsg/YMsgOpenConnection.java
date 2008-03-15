package com.itbs.aimcer.commune.ymsg;

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.commune.AbstractMessageConnection;
import com.itbs.aimcer.commune.ConnectionEventListener;
import com.itbs.aimcer.commune.FileTransferListener;
import com.itbs.aimcer.commune.FileTransferSupport;
import org.openymsg.network.*;
import org.openymsg.network.Status;
import org.openymsg.network.event.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Support for Yahoo medium.
 *
 * @author Alex Rass
 * @since Dec 12, 2007
 */
public class YMsgOpenConnection extends AbstractMessageConnection implements FileTransferSupport {
    private static final Logger log = Logger.getLogger(YMsgOpenConnection.class.getName());

    // -----The session object - our way into the Yahoo API
    private Session session;
    private static String EMPTY_EMAIL_STATE = "Unread Email Count: 0";
    private String emailState = EMPTY_EMAIL_STATE;

    /** Login Mode */
    public static final int SOCKS=0;
    public static final int HTTP=1;
    public static final int DIRECT=2;
    public static final int OTHER=3;

    public int PREFERRED_MODE = DIRECT;

    public final static String SERVER_JAPAN = "cs.yahoo.co.jp";
    public final static String SERVER_WORLD = "scs.msg.yahoo.com";

    private static final String ESCAPE = "\u001B[";

    public String getServiceName() {
        return "YAHOO";
    }

    public YMsgOpenConnection() {
        // Info is not public (prolly a bug), so have to get it this way.
        // if the code is changed to change mode, need to inset that there instead.
        // Doing it here allows us to overwrite it prior to use in connect().
        if (PREFERRED_MODE == DIRECT) {
            DirectConnectionHandler dch = new DirectConnectionHandler();
            setServerName(dch.getHost());
            setServerPort(dch.getPort());
        }
    }

/*
    public String getSupportAccount() {
        return "SashasEmail";
    }
*/

    public void connect() throws Exception {
        super.connect();
        notifyConnectionInitiated();
        // -----Set the connection handler as per command line
        if (PREFERRED_MODE == SOCKS) {
            session = new Session(new SOCKSConnectionHandler("autoproxy", 1080));
        } else if (PREFERRED_MODE == HTTP) {
            session = new Session(new HTTPConnectionHandler("http.pager.yahoo.com", 80));
//            session = new Session(new HTTPConnectionHandler("proxy", 8080));
        } else if (PREFERRED_MODE == DIRECT) {
            // The following line (while ugly) allows us to send japanese users to the right server,
            // while perserving the ability to override servers for rest of the users.
//            String serverName = (getUserName()!=null && getUserName().endsWith(".jp"))?SERVER_JAPAN:getServerName();
            DirectConnectionHandler dch = new DirectConnectionHandler(getServerName(), getServerPort());
            session = new Session(dch);
            // ports 5050,23,25,80
        } else {
            session = new Session();
        }
        // -----Register a listener
        session.addSessionListener(new YMsgOpenConnection.SessionHandler());
        log.fine(session.getConnectionHandler().toString());

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
            if (session!=null && session.getSessionStatus()== SessionState.LOGGED_ON) {
                // -----Update identities list

                Set<YahooGroup> yg = session.getGroups();
                for (YahooGroup aYg : yg) {
                    Group group = getGroupFactory().create(aYg.getName());
                    Set<YahooUser> v = aYg.getUsers();
                    for (YahooUser yu:v) {
                        Contact contact = getContactFactory().create(yu.getId(), YMsgOpenConnection.this);
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
            } else {
                notifyConnectionFailed("Failed to login.");
            }
        } catch (LoginRefusedException e) {
            String msg = "Login Refused.";
            if (e.getStatus() == AuthenticationState.BADUSERNAME) {
                    msg = "Yahoo doesn't recognise that username.";
            } else if (e.getStatus() == AuthenticationState.BAD) {
                    msg = "Yahoo refused our connection.  Password incorrect?";
            } else if (e.getStatus() == AuthenticationState.LOCKED) {
                    msg = "Your account is locked";
                    AccountLockedException e2 = (AccountLockedException) e;
                    if (e2.getWebPage() != null)
                        msg += "\nPlease visit: " + e2.getWebPage().toString();
            }
            notifyConnectionFailed(msg);
        } catch(InterruptedIOException e) {
            notifyConnectionFailed("Timeout during connection.\n" + e.getMessage());
        } catch(Exception e) {
            log.log(Level.SEVERE, "Problem connecting", e);
            notifyConnectionFailed("Problem connecting.\n" + e.getMessage());
        }

    }

    /**
     * Only use when adding new buddies.
     * @param contact to add
     * @param group to add to
     */
    public void addContact(Nameable contact, Group group) {
        if (session != null) {
            // do it for the server
            try {
                session.addFriend(contact.getName(), group.getName());
                group.add(contact);
//              Why was this done!?  Causes contacts to appear online when they are not! Testing, maybe?
//                if (contact instanceof Contact) {
//                    ((Contact) contact).getStatus().setOnline(true);
//                }
            } catch (IOException e) {
                for (ConnectionEventListener eventHandler : eventHandlers) {
                    eventHandler.errorOccured("Failed to add your contact", e);
                }
            }
        }
    }

    public void moveContact(Nameable contact, Group group) {
        if (session != null) {
            boolean found = false;
            GroupList list = getGroupList();
            for (int i = list.size(); i>0 && !found; i--) {
                try {
                    if (list.get(i).remove(contact) && !list.get(i).getName().equalsIgnoreCase(group.getName())) {
                        found = true;
                        session.removeFriend(contact.getName(), list.get(i).getName());
                    }
                } catch (IOException e) {
                    log.log(Level.SEVERE, "", e);//Todo change?
                }
            }
            if (found) {
                addContact(contact, group);
            }
        }
    }

    public void addContactGroup(Group group) {
        //todo Yahoo d/n support this directly. Use addContact... keep checking if new versions implemement code for adding a group
    }

    public void removeContactGroup(Group group) {
        //todo Yahoo d/n support this directly. Use addContact... keep checking if new versions implemement code for removing a group
    }

    /**
     * Use to remove contacts.
     * @param contact to delete
     */
    public void removeContact(Nameable contact) {
        if (session != null) {
            // do it for the server
            GroupList list = getGroupList();
            for (int i = list.size()-1; i >= 0; i--) {
                try {
                    if (list.get(i).remove(contact)) {
                        session.removeFriend(contact.getName(), list.get(i).getName());
                        return; // just 1 at a time, jik
                    }
                } catch (IOException e) {
                    log.log(Level.SEVERE, "", e);  //Todo change?
                }
            }
        }
    }

    public void disconnect(boolean intentional) {
        try {
            if (isLoggedIn()) {
                session.logout();
            }
        } catch (Exception e) {
           // log.log(Level.SEVERE, "", e); //We really don't care
        }
        session = null; // clear, gc
        super.disconnect(intentional);
    }

    public void reconnect() {
        try {
            connect();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to connect", e);
        }
    }

    public boolean isLoggedIn() {
        return session != null && session.getSessionStatus() == SessionState.LOGGED_ON;
    }

    public void cancel() {
        // d/n apply to this library
    }

    public boolean isAway() {
        if (session != null) {
            return session.getStatus() != Status.AVAILABLE;
        }
        return super.isAway();
    }

    public void setAway(boolean away) {
        if (session != null) {
            try {
                if (away && getProperties().getIamAwayMessage().length() > 0) // STATUS_CUSTOM
                    session.setStatus(getProperties().getIamAwayMessage(), true);
                else
                    session.setStatus(away? Status.BUSY:Status.AVAILABLE);
            } catch (Exception e) {
                log.log(Level.SEVERE, "", e);
            }
        }
        super.setAway(away);
    }

    public void setTimeout(int timeout) {
//        this.timeout = timeout;
    }

    public void processMessage(Message message) throws IOException {
        if (session != null) {
            session.sendMessage(message.getContact().getName(), message.getText());
        }
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
        return yu.getStatus() != Status.AVAILABLE && yu.getStatus() != Status.TYPING;
    }

    /**
     * Starts a file transfer.
     *
     * @param ftl listener
     * @throws java.io.IOException exc
     */
    public void initiateFileTransfer(FileTransferListener ftl) throws IOException {
        if (session != null) {
            session.sendFileTransfer(ftl.getContactName(), ftl.getFile(), ftl.getFileDescription());
        }
    }

    /**
     * Sets up file for receival
     *
     * @param ftl param
     * @param connectionInfo contains details of the transfer
     */
    public void acceptFileTransfer(FileTransferListener ftl, Object connectionInfo) {
        if (session != null) {
            try {
                session.saveFileTransferAs((SessionFileTransferEvent) connectionInfo, ftl.getFile().getAbsolutePath());
            } catch (IOException e) {
                for (ConnectionEventListener eventHandler : eventHandlers) {
                    eventHandler.errorOccured("ERROR while transfering file: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Request to cancel the file transfer in progress.
     */
    public void rejectFileTransfer(Object connectionInfo) {
    }

    // *****************************************************************
    // YMSG9 session handler
    // *****************************************************************
    class SessionHandler extends SessionAdapter {
        public void messageReceived(SessionEvent ev) { //<font size="10">\u001B[30mk\u001B[x1m<font size="10">
            String decodedMessage = "";
            if (ev.getMessage()!=null)
                decodedMessage = decodeMessage(ev.getMessage());
            Message message = new MessageImpl(getContactFactory().create(ev.getFrom(), YMsgOpenConnection.this), false, decodedMessage);
            for (ConnectionEventListener eventHandler : eventHandlers) {
                try {
                    eventHandler.messageReceived(YMsgOpenConnection.this, message);
                } catch (Exception exc) {
                    for (ConnectionEventListener eventListener: eventHandlers) {
                        eventListener.errorOccured("Error processing message.", exc);
                    }
                }
            }
        }

        public void errorPacketReceived(SessionErrorEvent ev) {
            if (ev.getService() != ServiceType.CONTACTIGNORE) {
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
                log.log(Level.SEVERE, "", ex.getCause());
            }
        }

        public void offlineMessageReceived(SessionEvent ev) {
            messageReceived(ev);
//            decoder.decodeToText(ev.getMessage());
        }

        public void notifyReceived(SessionNotifyEvent sessionNotifyEvent) {
            if ("TYPING".equals(sessionNotifyEvent.getType()))
                for (ConnectionEventListener eventHandler : eventHandlers) {
                    eventHandler.typingNotificationReceived(YMsgOpenConnection.this,  getContactFactory().create(sessionNotifyEvent.getFrom(), YMsgOpenConnection.this));
                }
        }

        public void fileTransferReceived(SessionFileTransferEvent ev) {
            String decodedMessage = "File: " + ev.getFilename();
            if (ev.getLocation()!=null)
                decodedMessage +=  " " + ev.getLocation().toString();
            Message message = new MessageImpl(getContactFactory().create(ev.getFrom(), YMsgOpenConnection.this), false, decodedMessage);
            for (ConnectionEventListener eventHandler : eventHandlers) {
                try {
                    eventHandler.messageReceived(YMsgOpenConnection.this, message);
                } catch (Exception exc) {
                    for (ConnectionEventListener eventListener: eventHandlers) {
                        eventListener.errorOccured("Error processing transfer.", exc);
                    }
                }
            }

/*
            log.fine(ev.getLocation().toString());
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
            log.fine("list received " + ev);
        }

        public void friendsUpdateReceived(SessionFriendEvent ev) {
            YahooUser aYu  = ev.getUser();
//          log.fine("Updated: " + yu[i].toString());
            if (aYu==null) return;
            Contact contact = getContactFactory().create(aYu.getId(), YMsgOpenConnection.this);
            com.itbs.aimcer.bean.Status oldStatus = (com.itbs.aimcer.bean.Status) contact.getStatus().clone();
            contact.getStatus().setOnline(aYu.isLoggedIn());
            contact.getStatus().setAway(isAway(aYu));
            contact.getStatus().setIdleTime(0);

            for (ConnectionEventListener eventHandler : eventHandlers) { //online: info.getOnSince().getTime() > 0
                eventHandler.statusChanged(YMsgOpenConnection.this, contact, oldStatus);
            }
        }

        public void friendAddedReceived(SessionFriendEvent ev) {
            log.fine("friendAddedReceived " + ev.toString());
            Contact contact = getContactFactory().create(ev.getUser().getId(), YMsgOpenConnection.this);
            Set<String> groups = ev.getUser().getGroupIds();
            for (String groupName : groups) {
                Group group = getGroupFactory().create(groupName);
                group.add(contact);
                getGroupList().add(group);
            }
        }

        public void friendRemovedReceived(SessionFriendEvent ev) {
            log.fine("friendRemovedReceived " + ev.toString());
        }

        public void contactRequestReceived(SessionEvent ev) {
            if (ev.getFrom() == null) return; // this is a bug in the library.
            boolean accept = true;
            for (ConnectionEventListener eventHandler : eventHandlers) { //online: info.getOnSince().getTime() > 0
                accept = eventHandler.contactRequestReceived(ev.getFrom(), YMsgOpenConnection.this);
                if (!accept) break;
            }
            if (!accept)
                try {
                    session.rejectContact(ev, "Not now, thanks");
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Failed to reject contact " + ev.getFrom(), e);
                }

        }

        public void conferenceInviteReceived(SessionConferenceEvent ev) {
            log.fine(ev.toString());
            try {
                session.declineConferenceInvite(ev, "Sorry!");
            } catch (IOException e) {
                //
            }
        }

        public void conferenceInviteDeclinedReceived(SessionConferenceEvent ev) {
            log.fine(ev.toString());
        }

        public void conferenceLogonReceived(SessionConferenceEvent ev) {
            log.fine(ev.toString());
        }

        public void conferenceLogoffReceived(SessionConferenceEvent ev) {
            log.fine(ev.toString());
        }

        public void conferenceMessageReceived(SessionConferenceEvent ev) {
            log.fine("conferenceMessageReceived " + ev.toString());
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
                log.fine("Blocked: " + u + "/" + spam + ": " + m);
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
            log.fine("**Chat connection closed**");
        }

        public void newMailReceived(SessionNewMailEvent event) {
            final String text = (event.getEmailAddress()==null?"":("Yahoo! mail from: " + event.getFrom() + " (" + event.getEmailAddress() + ")\n"))
                    + (event.getSubject()==null?"":("Subject: " + event.getSubject() + "\n"))
                    + "Unread Email Count: " + event.getMailCount();
            if (EMPTY_EMAIL_STATE.equals(text)) { // || emailState.equals(text)) { // same as empty or last
                return;
            }
            emailState = text;

            Message message = new MessageImpl(getContactFactory().create(getUserName(), YMsgOpenConnection.this), false, text);
            notifyEmailReceived(message);
        }
    } // class SessionHandler

} // class JAIMConnecion
