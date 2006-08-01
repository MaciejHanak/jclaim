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

package com.itbs.aimcer.commune.msn;

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.commune.AbstractMessageConnection;
import com.itbs.aimcer.commune.ConnectionEventListener;
import com.itbs.aimcer.commune.FileTransferListener;
import rath.msnm.BuddyList;
import rath.msnm.MSNMessenger;
import rath.msnm.SwitchboardSession;
import rath.msnm.UserStatus;
import rath.msnm.entity.MsnFriend;
import rath.msnm.event.MsnAdapter;
import rath.msnm.ftp.VolatileDownloader;
import rath.msnm.ftp.VolatileTransferServer;
import rath.msnm.msg.MimeMessage;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides connection to MSN.
 *
 * @author Alex Rass
 * @since Dec 24, 2004
 */
public class MSNConnection extends AbstractMessageConnection { //implements FileTransferSupport {
    private static Logger log = Logger.getLogger(MSNConnection.class.getName());
    
    MSNMessenger connection = null;
    Map<String, SwitchboardSession> sessions = new ConcurrentHashMap<String, SwitchboardSession>();

    /**
     * Non-blocking call.
     */
    public void connect() throws SecurityException, Exception {
        super.connect();
        sessions.clear();
        notifyConnectionInitiated();
        String username = getUserName();
        if (username.indexOf('@')==-1)
            throw new SecurityException("MSN usernames must contain '@msn.com' or '@hotmail.com'");
//        connection = new MSNMessenger(getUserName(), getPassword());
        connection = new MSNMessenger("", "");
        connection.setInitialStatus( UserStatus.ONLINE );
        connection.addMsnListener(new ConnectionListener());
//        connection.setInitialStatus(UserStatus.INVISIBLE); // todo switch to this when working
        connection.setInitialStatus(UserStatus.ONLINE);
//        connection.login(getUserName(), getPassword());
        connection.login(getUserName(), getPassword());
    }

    class ConnectionListener extends MsnAdapter {
        public void loginComplete(MsnFriend own) {
            // tell everyone we are now running connected
            // use itertors b/c the size will change
//            buddyListModified();
            notifyConnectionEstablished();
        }

        public void progressTyping(SwitchboardSession ss, MsnFriend friend, String typingUser) {
            sessions.put(friend.getLoginName(), ss);
            for (ConnectionEventListener connectionEventListener : eventHandlers) {
                connectionEventListener.typingNotificationReceived(MSNConnection.this,
                        getContactFactory().create(friend.getLoginName(), MSNConnection.this));
            }
        }

        public void notifyUnreadMail( Properties Prop, int unread )
        {
            final String text = "Unread Email Count: " + unread;
            Message message = new MessageImpl(getContactFactory().create(getUserName(), MSNConnection.this), false, text);
            for (ConnectionEventListener eventHandler : eventHandlers) {
                try {
                    eventHandler.emailReceived(MSNConnection.this, message);
                } catch (Exception e) {
                    for (ConnectionEventListener eventListener: eventHandlers) {
                        eventListener.errorOccured("Error processing message.", e);
                    }
                }
            }
        }

        public void userOnline(MsnFriend friend) {
            Contact contact = getContactFactory().get(friend.getLoginName(), MSNConnection.this);
            if (contact != null) {
                for (ConnectionEventListener eventHandler : eventHandlers) { //online: info.getOnSince().getTime() > 0
                    eventHandler.statusChanged(MSNConnection.this, contact, true, false, 0);
                }
            } else {
                log.fine("got MSN contact status w/o it being in the list");
            }
        }

        public void userOffline(String loginName) {
            Contact contact = getContactFactory().get(loginName, MSNConnection.this);
            if (contact != null) {
                for (ConnectionEventListener eventHandler : eventHandlers) { //online: info.getOnSince().getTime() > 0
                    eventHandler.statusChanged(MSNConnection.this, contact, false, true, 0);
                }
            } else {
                log.fine("got MSN contact status w/o it being in the list");
            }
        }

        public void loginError(String header) {
            String message;
            if("911".equals(header) )
                message = "Incorrect logon name or password.";
            else if ("921".equals(header))
                message = "Can't connect to server - too many users.";
            else
                message = "Connection failed.";
            connection.logout();
            for (ConnectionEventListener eventHandler : eventHandlers) {
                eventHandler.connectionFailed(MSNConnection.this, message);
            }
        }

        public void listAdd(MsnFriend friend) {
            Contact cw  = getContactFactory().create(friend.getLoginName(), MSNConnection.this);
            cw.getStatus().setOnline(!friend.getStatus().equals(UserStatus.OFFLINE));
            cw.setDisplayName(friend.getFormattedFriendlyName());
            log.fine("friend " + friend.getStatus() + " group index " + friend.getGroupIndex());
            Group gw = getGroupFactory().create(connection.getBuddyGroup().getGroupList().getGroup(friend.getGroupIndex()).getName());
            gw.add(cw);
            getGroupList().add(gw);
        }

        public void instantMessageReceived(SwitchboardSession ss, MsnFriend friend, MimeMessage mime) {
            sessions.put(friend.getLoginName(), ss);
            Message message = new MessageImpl(getContactFactory().create(friend.getLoginName(), MSNConnection.this),
                    false, false, mime.getMessage());
            for (int i = 0; i < eventHandlers.size(); i++) {
                try {
                    (eventHandlers.get(i)).messageReceived(MSNConnection.this, message);
                } catch (Exception e) {
                    for (ConnectionEventListener eventHandler : eventHandlers) {
                        eventHandler.errorOccured("Failure while receiving a message", e);
                    }
                }
            }
        }

        public void buddyListModified() {
            BuddyList blist = connection.getBuddyGroup().getForwardList();
            for (int i = 0; i < blist.size(); i++) {
                MsnFriend friend = blist.get(i);
                listAdd(friend);
            }
            for (ConnectionEventListener eventHandler : eventHandlers) {
                eventHandler.statusChanged(MSNConnection.this);
            }
        }

        public void addFailed(int errorCode) {
            for (ConnectionEventListener eventHandler : eventHandlers) {
                eventHandler.errorOccured("Failed to add.  Error code " + errorCode, null);
            }
        }

        /**
         * ë¡œê·¸ì?¸ í•œ í›„, ìƒ?íƒœë¥¼ ì˜¨ë?¼ì?¸ìœ¼ë¡œ ë°”ê¾¸ì—ˆì?„ë•Œ, ìž?ì‹ ì?˜
         * Contact listì—? ìžˆëŠ” ì‚¬ìš©ìž?ì¤‘ì—? ìƒ?íƒœê°€ Online(í˜¹ì?€ substate)ì?¸
         * ì‚¬ìš©ìž?ë“¤ì?„ ì?´ ë©”ì†Œë“œë¥¼ í†µí•´ ìž„ì?˜ì?˜ ê¸¸ì?´ë¡œ ë‚ ë ¤ì¤€ë‹¤.
         * ë¬¸ì œì ?ì?€ í˜¸ì¶œ ì¢…ë£Œì§€ì ?ì?„ ì •í™•í•˜ê²Œ ì•Œ ìˆ˜ ì—†ë‹¤ëŠ” ê²ƒì?´ë‹¤.
         * <p/>
         * ë§Œì•½ Online Contact listë¥¼ ê°€ì§€ê³  ì‹¶ë‹¤ë©´, MsnFriend ê°?ì²´ë¥¼
         * Mapì—? ì €ìž¥í•´ë‘?ë©´ íŽ¸ë¦¬í•˜ë‹¤. (Keyê°’ì?€ loginNameìœ¼ë¡œ í•˜ë©´ ë?” ì¢‹ë‹¤)
         */
        public void listOnline(MsnFriend friend) {
            Contact cw = getContactFactory().create(friend.getLoginName(), MSNConnection.this);
//            cw.setOnline(true);
            for (ConnectionEventListener eventHandler : eventHandlers) {
                (eventHandler).statusChanged(MSNConnection.this, cw, true, false, 0);
            }
        }

        public void switchboardSessionStarted(SwitchboardSession ss) {
            sessions.put(ss.getMsnFriend().getLoginName(), ss);
        }

        public void switchboardSessionEnded(SwitchboardSession ss) {
            sessions.remove(ss.getName());
        }

        /**
         * ë¡œê·¸ì?¸ ì‹œ, Synchronization valueê°€ ë‹¬ëž?ì?„ë•Œ, ì„œë²„ë¡œë¶€í„°
         * FL/AL/BL/RL, Group listë“±ì?„ ëª¨ë‘? ë°›ê²Œ ë?˜ëŠ”ë?°, ë§Œë§Œì¹˜ ì•Šì?€
         * ìž‘ì—…ì?´ë¯€ë¡œ, ëª¨ë‘? ë‹¤ Updateë?˜ì—ˆì?„ë•Œ í†µì§€ë?˜ëŠ” ì?´ë²¤íŠ¸ì?´ë‹¤.
         */
        public void allListUpdated() {
            buddyListModified();
        }

        public void logoutNotify() {
            for (ConnectionEventListener eventHandler : eventHandlers) {
                eventHandler.connectionLost(MSNConnection.this);
            }
        }

        /**
         * ???? ???? ??? ???? ?? ???? ?????.
         *
         * @param ss       ??? ???? ? ??? ??? Switchboard??
         * @param cookie   ??? ??? ???.
         * @param filename ?????? ??? ??
         * @param filesize ?????? ??? ??(byte??)
         * @since 0.3
         */
        public void filePosted(SwitchboardSession ss, int cookie, String filename, int filesize) {
            super.filePosted(ss, cookie, filename, filesize);    //Todo change
            log.fine("MSNConnection$ConnectionListener.filePosted");
        }

        /**
         * ???? ?? ??? ??? ???? Accept????? ???? ?????.
         * ? ???? ????? ??? ??? ?????? ??.
         * ? ? ???? ??? ?, ??? ??? ??? ?????? thread?
         * ??? ??? 6891 port? bind??? ?? ???? ???.
         *
         * @param ss     ??? ???? ? ??? ??? Switchboard??
         * @param cookie ??? ????? ???.
         */
        public void fileSendAccepted(SwitchboardSession ss, int cookie) {
            super.fileSendAccepted(ss, cookie);    //Todo change
            log.fine("MSNConnection$ConnectionListener.fileSendAccepted");
        }

        /**
         * ???? ?? ??? ??? ???? Reject??? ???? ?????.
         * ? ???? ????? ??? ??? ?????? ??.
         *
         * @param ss     ??? ???? ? ??? ??? Switchboard??
         * @param cookie ??? ????? ???.
         * @param reason ??? ??? ???? ??? ????.
         */
        public void fileSendRejected(SwitchboardSession ss, int cookie, String reason) {
            super.fileSendRejected(ss, cookie, reason);    //Todo change
            log.fine("MSNConnection$ConnectionListener.fileSendRejected");
        }

        /**
         * ?? ??? ??? ?????? ????.
         *
         * @param server ?? ?? ?? ??.
         */
        public void fileSendStarted(VolatileTransferServer server) {
            super.fileSendStarted(server);    //Todo change
            log.fine("MSNConnection$ConnectionListener.fileSendStarted");
        }

        /**
         * ?? ??? ??? ?????? ????.
         */
        public void fileSendEnded(VolatileTransferServer server) {
            super.fileSendEnded(server);    //Todo change
            log.fine("MSNConnection$ConnectionListener.fileSendEnded");
        }

        /**
         * ?? ???? ??? ?????? ????.
         *
         * @param downloader ?? ???? thread ??.
         */
        public void fileReceiveStarted(VolatileDownloader downloader) {
            log.fine("MSNConnection$ConnectionListener.fileReceiveStarted");
            //Todo verify
            Contact contact = getContactFactory().get(downloader.getName(), MSNConnection.this);
            for (ConnectionEventListener eventHandler : eventHandlers) {
//                eventHandler.fileReceiveRequested(MSNConnection.this, contact, downloader.getFilename(), "", downloader);
            }
        }

        /**
         * ??? ????? ??? ?????? ???? ?????.
         * ???, ?? ?? thread? ????.
         */
        public void fileSendError(VolatileTransferServer server, Throwable e) {
            log.fine("MSNConnection$ConnectionListener.fileSendError");
            for (ConnectionEventListener eventHandler : eventHandlers) {
                eventHandler.errorOccured("ERROR while transfering file: " + server.getFilename() + " " + e.getMessage(), null);
            }
        }

        /**
         * ??? ????? ??? ?????? ???? ?????.
         * ???, ?? ?? thread? ????.
         */
        public void fileReceiveError(VolatileDownloader downloader, Throwable e) {
            log.log(Level.SEVERE, "fileReceiveError", e);
            for (ConnectionEventListener eventHandler : eventHandlers) {
                eventHandler.errorOccured("ERROR while transfering file: " + downloader.getFilename() + " " + e.getMessage(), null);
            }
        }

    } // class ConnectionListener


    public void disconnect(boolean intentional) {
        sessions.clear();
        if (connection!=null)
            connection.logout();
        super.disconnect(intentional);
    }

    public void reconnect() {
        disconnect(false);
        try {
            connect();
        } catch (Exception e) {
//            GeneralUtils.sleep(1000);
            log.log(Level.INFO, "Failed to reconnect", e);
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
            disconnect(false);
    }

    public void setTimeout(int timeout) {
    }

    public void addContact(Nameable contact, Group group) {
//        String[] groupNames = new String[1];
//        groupNames[0] = group.getName();
        try {
            connection.addGroup(group.getName());
            connection.addFriend(contact.getName());
        } catch (IOException e) {
            for (ConnectionEventListener connectionEventListener : eventHandlers) {
                connectionEventListener.errorOccured("Failed to add a contact", e);
            }
            return;
        }
        group.add(contact);
        Thread.yield();
        String index=null;
        Iterator target = connection.getBuddyGroup().getGroupList().iterator();
        while (target.hasNext()) {
            rath.msnm.entity.Group targetGroup = (rath.msnm.entity.Group) target.next();
            if (group.getName().equalsIgnoreCase(targetGroup.getName())) {
                index = targetGroup.getIndex();
                break;
            }
        }
        if (index != null) {
            BuddyList blist = connection.getBuddyGroup().getForwardList();
            for (int i = 0; i < blist.size(); i++) {
                MsnFriend friend = blist.get(i);
                if (contact.getName().equalsIgnoreCase(friend.getLoginName())) {
                    friend.setGroupIndex(index);
                    break;
                }
            }
        } else {
            log.fine("Never found the index.");
        }

    }// addContact()

    public void removeContact(Nameable contact) {
        try {
            connection.removeFriend(contact.getName());
        } catch (IOException e) {
            for (ConnectionEventListener eventHandler : eventHandlers) {
                eventHandler.errorOccured("ERROR removing a contact.", e);
            }
        }
    }

    public void addContactGroup(Group group) {
        try {
            connection.addGroup(group.getName());
        } catch (IOException e) {
            for (ConnectionEventListener eventHandler : eventHandlers) {
                eventHandler.errorOccured("ERROR adding a contact.", e);
            }
        }
    }

    public void removeContactGroup(Group group) {
        try {
            connection.removeGroup(group.getName());
        } catch (IOException e) {
            for (ConnectionEventListener eventHandler : eventHandlers) {
                eventHandler.errorOccured("ERROR removing a contact group.", e);
            }
        }
    }

    /**
     * Returns a short name for the service.
     * "AIM", "ICQ" etc.
     *
     * @return service name
     */
    public String getServiceName() {
        return "MSN";
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

    public boolean isAway() {
        if (connection == null)
            return super.isAway();
        return !UserStatus.ONLINE.equals(connection.getMyStatus());
    }

    /**
     * Sets the away flag.
     *
     * @param away true if so
     */
    public void setAway(boolean away) {
        if (connection != null)
            try {
                connection.setMyStatus(away?UserStatus.AWAY_FROM_COMPUTER:UserStatus.ONLINE);
            } catch (IOException e) {
                log.log(Level.SEVERE, "Failed to set status", e);
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
        final MimeMessage msg = new MimeMessage(message.getText());
        msg.setKind(MimeMessage.KIND_MESSAGE);
        SwitchboardSession ss = getSession(message.getContact().getName());
        try {
            ss.sendInstantMessage(msg);
        } catch (NullPointerException e) { // this catches both problems
            sessions.remove(message.getContact().getName());
            for (ConnectionEventListener eventHandler : eventHandlers) {
                eventHandler.errorOccured("Failed to send your message, try again.", null);
            }
        }
    } // processMessage()

    SwitchboardSession getSession(String name) throws IOException {
        SwitchboardSession ss = sessions.get(name);
        if (ss == null) {
            try {
                ss = connection.doCallWait(name);
                if (ss!=null)
                    sessions.put(name, ss);
            } catch (InterruptedException e) { //
            }
        }
        return ss;
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

    /**
     * Starts a file transfer.
     *
     * @param ftl listener
     */
    public void initiateFileTransfer(FileTransferListener ftl) throws IOException {
        SwitchboardSession session = getSession(ftl.getContactName());
        if( session!=null )
        {
            try {
                connection.sendFileRequest(getUserName(), ftl.getFile(), session);
            } catch( IOException e ) {
                ftl.notifyFail();
            }
        }
    }

    /**
     * Sets up file for receival
     *
     * @param ftl            param
     * @param connectionInfo
     */
    public void acceptFileTransfer(FileTransferListener ftl, Object connectionInfo) {
        //Todo verify
        if (connectionInfo instanceof VolatileDownloader)
            connection.fireFileReceiveStartedEvent((VolatileDownloader) connectionInfo);
        else
            log.fine("MSNConnection.acceptFileTransfer not the right class " + connectionInfo.getClass().getName());
    }

    /**
     * Request to cancel the file transfer in progress.
     */
    public void rejectFileTransfer() {
        //Todo change
    }
}
