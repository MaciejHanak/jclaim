package com.itbs.aimcer.commune.daim;

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.commune.*;
import com.itbs.util.GeneralUtils;
import org.walluck.oscar.*;
import org.walluck.oscar.channel.aolim.AOLIM;
import org.walluck.oscar.client.AbstractOscarClient;
import org.walluck.oscar.client.Buddy;
import org.walluck.oscar.client.DaimLoginEvent;
import org.walluck.oscar.client.Oscar;
import org.walluck.oscar.handlers.icq.ICQSMSMessage;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages AIM connectivity.  Singlehandedly.
 *
 * @author Alex Rass
 * @since Sep 22, 2004
 */
public class DaimConnection extends AbstractMessageConnection implements IconSupport, FileTransferSupport, SMSSupport {
    private static Logger log = Logger.getLogger(DaimConnection.class.getName());
    DaimClient connection;
    ConnectionInfo connectionInfo = new ConnectionInfo(AIMConstants.LOGIN_SERVER_DEFAULT, AIMConstants.LOGIN_PORT);

    public String getServiceName() {
        return "AIM";
    }

    public boolean isSystemMessage(Nameable contact) {
        return "aolsystemmsg".equals(GeneralUtils.getSimplifiedName(contact.getName()));
    }

    public String getSupportAccount() {
        return "JClaimHelp";
    }

    public boolean isAway() {
        return super.isAway();
    }

    public void setAway(boolean away) {
/*
        if (connection!=null) {
            try {
                oscar.setAwayAIM(connection.getSession(), away?getProperties().getIamAwayMessage():null);
            } catch (IOException e) {
                notifyErrorOccured("Failed to set away.", e);
            }
        }
*/
        super.setAway(away);
    }

    public String getServerName() {
        return connectionInfo.getIp();
    }

    public void setServerName(String address) {
        if (System.getProperty("OSCAR_HOST") == null) { // if no forced overwrite
            connectionInfo.setIp(address);
        }
    }

    public int getServerPort() {
        return connectionInfo.getPort();
    }

    public void setServerPort(int port) {
        if (System.getProperty("OSCAR_PORT") == null) { // if no forced overwrite
            connectionInfo.setPort(port);
        }
    }

    final public void connect() throws Exception {
        try {
            super.connect();
            notifyConnectionInitiated();
            if (getUserName() == null || getPassword() == null) {
                throw new SecurityException("Login information was not available");
            }
            connection = new DaimClient();
            connection.login(getUserName(), getPassword());
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to login", e);
            connection = null;
            notifyConnectionFailed(e.getMessage());
        }
    }


    public void reconnect() {
        if (getGroupList().size() > 0)
            try {
                connect();
            } catch (Exception e) {
                log.log(Level.INFO, "Failed to reconnect" ,e); // no big deal, but lets see
            }
    }

    public void disconnect(boolean intentional) {
        if (connection!=null) {
            AIMConnection.killAllInSess(connection.getSession());
            connection = null;
        }
        super.disconnect(intentional);
    }

    public boolean isLoggedIn() {
        return connection!=null;   // todo make sure connection is connected.
    }

    public void cancel() {
        if (connection!=null)
            disconnect(true);
    }

    public void setTimeout(int timeout) {
    }

    public void processMessage(Message message) {
        if (connection != null) {
            try {
                connection.sendIM(message.getContact().getName(), message.getText(), Oscar.getICQCaps());
            } catch (IOException e) {
                notifyErrorOccured("Failed to send", e);
            }
        } else {
            notifyErrorOccured("Not connected.", null);
        }
    }

    public void processSecureMessage(Message message) throws IOException {
        processMessage(message);
    }

    /**
     * Finds a group.  Helper.
     * @param group to find
     * @return group or null
     * todo implement
     */
    Group findGroup(com.itbs.aimcer.bean.Group group) {
/*
        java.util.List<? extends net.kano.joustsim.oscar.oscar.service.ssi.Group> list = connection.getSsiService().getBuddyList().getGroups();
        for (net.kano.joustsim.oscar.oscar.service.ssi.Group aimGroup : list) {
            if (aimGroup instanceof MutableGroup && group.getName().equalsIgnoreCase(aimGroup.getName())) {
                return aimGroup;
            }
        }
*/
        return null;
    }
    /**
     * Finds a group.  Helper.
     * @param contact to find by
     * @return group or null
     */
    Group findGroupViaBuddy(com.itbs.aimcer.bean.Nameable contact) {
        GroupList list = getGroupList();
        for (int i = list.size(); i>0; i--) {
            Group group = list.get(i);
            for (int j = group.size(); j>0; j--) {
                if (group.get(j).getName().equalsIgnoreCase(contact.getName()))
                    return group;
            }
        }
        return null;
    }

    /**
     * Helper method for finding a buddy.
     * @param contact to find
     * @return
     * todo implement
     */
    Buddy findBuddy(Contact contact) {
/*
        List<? extends Group> list = connection.getSsiService().getBuddyList().getGroups();
        for (Group aimGroup : list) {
            if (aimGroup instanceof MutableGroup) {
                for(Buddy buddy: aimGroup.getBuddiesCopy()) {
                    if (contact.getName().equalsIgnoreCase(buddy.getScreenname().getNormal()))
                        return buddy;
                }
            }
        }
*/
        return null;
    }

    /**
     *
     * @param contact
     * @param group
     * @param inGroup
     * @return
     * todo implement
     */
    Buddy findBuddyViaGroup(Nameable contact, com.itbs.aimcer.bean.Group group, boolean inGroup) {
/*
        List<? extends Group> list = connection.getSsiService().getBuddyList().getGroups();
        for (Group aimGroup : list) {
            boolean groupMatch = group.getName().equalsIgnoreCase(aimGroup.getName());
            if (!inGroup)
              groupMatch = !groupMatch;
            if (aimGroup instanceof MutableGroup && groupMatch) {
                for(Buddy buddy: aimGroup.getBuddiesCopy()) {
                    if (contact.getName().equalsIgnoreCase(buddy.getScreenname().getNormal()))
                        return buddy;
                }
            }
        }
*/
        return null;
    }

    /**
     * Call to add a new contact to your list.
     *
     * @param contact to add
     * @param group   to add to
     */
    public void addContact(final Nameable contact, final com.itbs.aimcer.bean.Group group) {
        try {
            connection.addBuddy(contact.getName(), group.getName());
        } catch (IOException e) {
            notifyErrorOccured("Failed to add buddy.", e);
        }
    } // addContact

    /**
     * Call to remove a contact you no longer want.
     *
     * @param contact to remove
     */
    public void removeContact(final Nameable contact) {
        Group group = findGroupViaBuddy(contact);
        try {
            connection.removeBuddy(contact.getName(), group.getName());
        } catch (IOException e) {
            notifyErrorOccured("Error while removing contact", e);
        }
    } // removeContact

    /**
     *
     * @param group
     * todo implement
     */
    public void addContactGroup(com.itbs.aimcer.bean.Group group) {
        // nothing needed
//        connection.getSsiService().getBuddyList().addGroup(group.getName());
    }

    /**
     *
     * @param group
     * todo implement
     */
    public void removeContactGroup(com.itbs.aimcer.bean.Group group) {
    }

    /**
     *
     * @param contact to move
     * @param group to move to
     * todo implement
     */
    public void moveContact(Nameable contact, com.itbs.aimcer.bean.Group group) {
        try {
            connection.moveBuddy(contact.getName(), findGroupViaBuddy(contact).getName(), group.getName());
        } catch (IOException e) {
            notifyErrorOccured("Error while removing contact", e);
        }
    }


    public void initiateFileTransfer(final FileTransferListener ftl) throws IOException {
        connection.sendFile(ftl.getContactName(), ftl.getFile());
/*
        OutgoingFileTransfer oft = connection.getIcbmService().getRvConnectionManager().createOutgoingFileTransfer(new Screenname(ftl.getContactName()));
        oft.addEventListener(new DaimConnection.FileTransferEventListener(ftl));


        oft.setSingleFile(ftl.getFile());
        oft.sendRequest(new InvitationMessage(ftl.getFileDescription()));
*/
    }

    public void rejectFileTransfer(Object connectionInfo) {
//        ((IncomingFileTransfer) connectionInfo).close();
    }


    public void acceptFileTransfer(FileTransferListener ftl, Object connectionInfo) {
        //TODO Change
    }

    public void requestPictureForUser(final Contact contact) {
    } // requestPictureForUser

    /**
     * Will remove the picture.
     * todo implement
     */
    public void clearPicture() {
    }

    /**
     * Use this picture for me.
     *
     * @param picture filename
     * todo implement
     */
    public void uploadPicture(final File picture) {
    } // uploadPicture

    /**
     * Doing this internally so I have access to all the variables and methods available directly in DaimConnection.
     */
    class DaimClient extends AbstractOscarClient  {

        public DaimClient() {
            super();
        }

        AIMSession getSession() {
            return session;
        }


        public void loginDone(DaimLoginEvent dle) {
            notifyConnectionEstablished();
        }

        public void loginError(DaimLoginEvent dle) {
            String errorMsg = dle.getErrorMsg()==null?"Unknown":dle.getErrorMsg();
            notifyConnectionFailed(errorMsg); 
        }

        public void incomingIM(Buddy buddy, UserInfo from, AOLIM args) {
            Message message = new MessageImpl(getContactFactory().create(AIMUtil.normalize(buddy.getName()), DaimConnection.this),
            false, (args.getFlags() & AIMConstants.AIM_IMFLAGS_AWAY) != 0, args.getMsg());  // todo the away flag d/n look right

            for (ConnectionEventListener eventHandler : eventHandlers) {
                try {
                    eventHandler.messageReceived(DaimConnection.this, message);
                } catch (Exception e) {
                    notifyErrorOccured("Failure while receiving a message", e);
                }
            }
        }

        public void incomingICQ(UserInfo from, int uin, int args, String msg) {
            Message message = new MessageImpl(getContactFactory().create(from.getSN(), DaimConnection.this),
            false, from.getIdleTime()>0, msg);  // todo the away flag d/n look right

            for (ConnectionEventListener eventHandler : eventHandlers) {
                try {
                    eventHandler.messageReceived(DaimConnection.this, message);
                } catch (Exception e) {
                    notifyErrorOccured("Failure while receiving an ICQ message", e);
                }
            }
        }

        public void receivedICQSMS(UserInfo from, int uin, ICQSMSMessage msg, boolean massmessage) {
            Message message = new MessageImpl(getContactFactory().create(from.getSN(), DaimConnection.this),
            false, massmessage, msg.getText());


            for (ConnectionEventListener eventHandler : eventHandlers) {
                try {
                    eventHandler.messageReceived(DaimConnection.this, message);
                } catch (Exception e) {
                    notifyErrorOccured("Failure while receiving an ICQ message", e);
                }
            }
        }

        private void removeOldBuddies() {
            for (int i = 0; i < getGroupList().size(); i++) {
                Group group = getGroupList().get(i);
                for (int j = 0; j < group.size(); j++) {
                    Nameable contact = group.get(j);
                    if (contact instanceof Contact) {
                        Contact cw = (Contact) contact;
                        if (DaimConnection.this == cw.getConnection()) {
                            group.remove(contact);
                        }
                    }
                }
            }
        }

        public void newBuddyList(Buddy[] buddies) {
            removeOldBuddies();
            for (Buddy buddy:buddies) {
                Group bGroup = getGroupFactory().create(buddy.getProperty(Buddy.GROUP).toString());
                Contact contact = getContactFactory().create(AIMUtil.normalize(buddy.getName()), DaimConnection.this);
                contact.setDisplayName(buddy.getName());
                bGroup.add(contact);
                getGroupFactory().getGroupList().add(bGroup);
            } // that should reorder it.

            for (ConnectionEventListener eventHandler : eventHandlers) {
                try {
                    eventHandler.statusChanged(DaimConnection.this);
                } catch (Exception e) {
                    notifyErrorOccured("Failure while receiving an ICQ message", e);
                }
            }
        }

        public void buddyOffline(String sn, Buddy buddy) {
            Contact contact = getContactFactory().create(AIMUtil.normalize(buddy.getName()), DaimConnection.this);
            Status status = (Status) contact.getStatus().clone();
            contact.getStatus().setOnline(false);
            for (ConnectionEventListener eventHandler : eventHandlers) {
                try {
                    eventHandler.statusChanged(DaimConnection.this, contact, status);
                } catch (Exception e) {
                    notifyErrorOccured("Failure while receiving an ICQ message", e);
                }
            }
        }

        public void buddyOnline(String sn, Buddy buddy) {
            Contact contact = getContactFactory().create(AIMUtil.normalize(buddy.getName()), DaimConnection.this);
            Status status = (Status) contact.getStatus().clone();
            contact.getStatus().setOnline(true);
            contact.getStatus().setAway(buddy.isTrue(Buddy.STATE, Buddy.BUDDY_STATE_AWAY));
            int idle = GeneralUtils.getInt(buddy.getProperty(Buddy.IDLE_TIME));
            contact.getStatus().setIdleTime(idle);
            for (ConnectionEventListener eventHandler : eventHandlers) {
                try {
                    eventHandler.statusChanged(DaimConnection.this, contact, status);
                } catch (Exception e) {
                    notifyErrorOccured("Failure while receiving an ICQ message", e);
                }
            }
        }

        public void sendFile(String contactName, File file) {
            filtool.sendFile(contactName,  file.getAbsolutePath());
        }

        public void typingNotification(String sn, short typing) {
            Contact contact = getContactFactory().create(AIMUtil.normalize(sn), DaimConnection.this);
            
            for (ConnectionEventListener eventHandler : eventHandlers) {
                    eventHandler.typingNotificationReceived(DaimConnection.this, contact);
            }
        }
    }

    public String veryfySupport(String id) {
        if (!GeneralUtils.isNotEmpty(id))
            return "Number can't be empty";
        return id.startsWith("+1")?null:"Must start with +1, like: +18005551234";
    }

} // class OscarConnection
