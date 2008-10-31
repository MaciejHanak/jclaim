package com.itbs.aimcer.commune.joscsarlib;

import JOscarLib.Core.OscarConnection;
import JOscarLib.Integration.Event.*;
import JOscarLib.Packet.Sent.AddToContactList;
import JOscarLib.Setting.Enum.StatusModeEnum;
import JOscarLib.Tool.OscarInterface;
import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.commune.AbstractMessageConnection;
import com.itbs.aimcer.commune.ConnectionEventListener;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 *  W A R N I N G:
 *     This protocol is still very fresh and doesn't work for a client.
 * This is here on a "use how you want it/fix it yourself" basis.
 *
 * Implements (sorta) this protocol:
 * https://sourceforge.net/projects/ooimlib/ 
 *
 * @author Created by Alex Rass on Mar 26, 2005
 */
public class AIMConnection extends AbstractMessageConnection {
    // -----The session object - our way into the API
    private static final String HOST = "login.icq.com";
    private static final int PORT = 5190;

    private OscarConnection session;

    /** remembers last state. */
    private boolean disconnect;
    /** Login Mode */
    private static StatusModeEnum[] rotationStatus = new StatusModeEnum[4];
    private Nameable user;

    static {
        rotationStatus[0] = new StatusModeEnum(StatusModeEnum.OCCUPIED);
        rotationStatus[1] = new StatusModeEnum(StatusModeEnum.FREE_FOR_CHAT);
        rotationStatus[2] = new StatusModeEnum(StatusModeEnum.DND);
        rotationStatus[3] = new StatusModeEnum(StatusModeEnum.ONLINE);
    }

    public String getServiceName() {
        return "AIM";
    }

    public void connect() {
        notifyConnectionInitiated();
        user = getContactFactory().create(getUserName(), this);
        session = new OscarConnection(HOST, PORT, getUserName(), getPassword());
        session.getPacketAnalyser().setDebug(true);


        /* to be notify when connection is ready. */
        session.addObserver(new Observer() {
            public void update(Observable o, Object arg) {
                System.out.println("I've sent my buddy list.");
                notifyConnectionEstablished();
            }
        });
        /* To receive events. */
        session.addStatusListener(new StatusListener() {
            public void onIncomingUser(IncomingUserEvent e) {
                //TODO Change
                System.out.println(e.getIncomingUserId() + " went online.");
            }

            public void onStatusChange(StatusEvent e) {
                //TODO Change
                System.out.println(e + " - status changed.");
            }

            public void onLogout() {
                notifyConnectionLost();
            }

            public void onOffgoingUser(OffgoingUserEvent e) {
                //TODO Change
                System.out.println(e.getOffgoingUserId() + " went offline.");
            }
        });
        session.addContactListListener(new ContactListListener() {
            public void updateContactList(ContactListEvent contactListEvent) {
                System.out.println(contactListEvent + "  - updateContactList");
//                contactListEvent.
                //TODO Change
                // Supposed to do something here as per:
                // https://sourceforge.net/forum/forum.php?thread_id=1670176&forum_id=166562
//                session.sendFlap(ClientMeta.searchByUinTlv(session, ii.getId())) 
//                ii beeing a contact ContactListItem.

            }
        });
        session.addMetaInfoListener(new MetaInfoListener() {
            public void updateContactMetaInfo(MetaInfoEvent metaInfoEvent) {
                //TODO Change
                System.out.println(metaInfoEvent + "  - updateContactList");
            }
        });
        session.addMessagingListener(new MessagingListener() {
            public void onIncomingMessage(IncomingMessageEvent e) {
                Message message = new MessageImpl(getContactFactory().create(e.getSenderID(), AIMConnection.this), false, e.getMessage());
                for (ConnectionEventListener eventHandler : eventHandlers) {
                    try {
                        eventHandler.messageReceived(AIMConnection.this, message);
                    } catch (Exception ex) {
                        notifyErrorOccured("Error processing message.", ex);
                    }
                }
            }

            public void onMessageAck(MessageAckEvent messageAckEvent) {
                //TODO Change
            }

            public void onMessageError(MessageErrorEvent messageErrorEvent) {
                //TODO Change
            }

            public void onIncomingUrl(IncomingUrlEvent e) {
                //Todo change
            }

            public void onOfflineMessage(OfflineMessageEvent e) {
                Message message = new MessageImpl(getContactFactory().create(e.getSenderUin(), AIMConnection.this), false, e.getMessage());
                for (ConnectionEventListener eventHandler : eventHandlers) {
                    try {
                        eventHandler.messageReceived(AIMConnection.this, message);
                    } catch (Exception ex) {
                        notifyErrorOccured("Error processing message.", ex);
                    }
                }
            }
        });
        disconnect = false;
        // Following is a way to get the user list as per:
        // https://sourceforge.net/forum/forum.php?thread_id=1670176&forum_id=166562
        OscarInterface.checkRoster(session, 0, (short)0);
    }

    /**
     * Only use when adding new buddies.
     * @param contact to add
     * @param group to add to
     */
    public void addContact(Nameable contact, Group group) {
        session.sendFlap(new AddToContactList(new String[] {contact.getName()}));
        group.add(contact);
    }

    public void addContactGroup(Group group) {
        //todo implemement code for adding a group
    }

    public void removeContactGroup(Group group) {
        //todo implemement code for removing a group
    }

    public void moveContact(Nameable contact, Group oldGroup, Group newGroup) {
        //todo implemement code for moving a contact to a new group
    }

    /**
     * Use to remove contacts.
     * @param contact to delete
     * @param group
     */
    public boolean removeContact(Nameable contact, Group group) {
        // do it for the server
        notifyErrorOccured("Sorry, this funtionality is not supported by this library", null);
        return false;
    }

    public Nameable getUser() {
        return user;
    }

    public void disconnect() {
        disconnect = true;
        try {
            if (session!=null && isLoggedIn())
                session.close();
        } catch (Exception e) {
            e.printStackTrace();  //Todo change?
        }
        super.disconnect(true);
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
        return session != null && session.isLogged();
    }

    public void cancel() {
        // d/n apply to this library
    }

    public void setAway(boolean away) {
        if (away && getProperties().getIamAwayMessage().length() > 0) // STATUS_CUSTOM
            OscarInterface.changeStatus(session, new StatusModeEnum(StatusModeEnum.AWAY));
// todo see about the msg  session.setStatus(ClientProperties.INSTANCE.getIamAwayMessage(), true);
        else
            OscarInterface.changeStatus(session, new StatusModeEnum(away?StatusModeEnum.AWAY:StatusModeEnum.ONLINE));
    }

    public void setTimeout(int timeout) {
//        this.timeout = timeout;
    }

    public void processMessage(Message message) throws IOException {
        OscarInterface.sendBasicMessage(session, message.getContact().getName(), message.getText());
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

} // class JAIMConnecion
