package com.itbs.aimcer.commune.daim;

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.commune.*;
import com.itbs.util.GeneralUtils;
import org.walluck.oscar.AIMConnection;
import org.walluck.oscar.AIMConstants;
import org.walluck.oscar.AIMSession;
import org.walluck.oscar.client.Buddy;
import org.walluck.oscar.client.Oscar;

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
public class DaimConnection extends AbstractMessageConnection implements IconSupport, FileTransferSupport {
    private static Logger log = Logger.getLogger(DaimConnection.class.getName());
    AIMSession connection = null;
    Oscar oscar = new Oscar();
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
        if (connection!=null) {
            try {
                oscar.setAwayAIM(connection, away?getProperties().getIamAwayMessage():null);
            } catch (IOException e) {
                notifyErrorOccured("Failed to set away.", e);
            }
        }
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
        super.connect();
        notifyConnectionInitiated();
        if (getUserName() == null || getPassword() == null) {
            throw new SecurityException("Login information was not available");
        }
        connection = new AIMSession();
        oscar.setSN(getUserName());
        connection.setSN(getUserName());

        // this paragraph should be in icq. but what the hell!
        char first = getUserName().charAt(0);
        if (Character.isDigit(first)) {
            connection.setICQ(true);
        }

        oscar.setPassword(getPassword());
//        connection.registerListener(); 
        connection.init();
        // todo can't specify connection info, figure that out.
        oscar.login(connection, getUserName());
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
            AIMConnection.killAllInSess(connection);
            connection = null;
        }
        super.disconnect(intentional);
    }

    public boolean isLoggedIn() {
        return connection!=null;
    }

    public void cancel() {
        if (connection!=null)
            disconnect(true);
    }

    public void setTimeout(int timeout) {
    }

    public void processMessage(Message message) {
        try {
            oscar.sendIM(connection, message.getContact().getName(), message.getText(), oscar.getAIMCaps());
        } catch (IOException e) {
            notifyErrorOccured("Failed to send", e);
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
            oscar.addBuddy(connection, contact.getName(), group.getName());
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
            oscar.removeBuddy(connection, contact.getName(), group.getName());
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
            oscar.moveBuddy(connection, contact.getName(), findGroupViaBuddy(contact).getName(), group.getName());
        } catch (IOException e) {
            notifyErrorOccured("Error while removing contact", e);
        }
    }


    public void initiateFileTransfer(final FileTransferListener ftl) throws IOException {
        oscar.sendFile(connection, ftl.getContactName(), ftl.getFile().getAbsolutePath());
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

} // class OscarConnection
