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

package com.itbs.aimcer.commune.joscar;

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.bean.Group;
import com.itbs.aimcer.bean.Message;
import com.itbs.aimcer.commune.*;
import com.itbs.util.GeneralUtils;
import net.kano.joscar.ByteBlock;
import net.kano.joscar.FileWritable;
import net.kano.joscar.flapcmd.SnacCommand;
import net.kano.joscar.rv.RvProcessor;
import net.kano.joscar.rvcmd.DefaultRvCommandFactory;
import net.kano.joscar.rvcmd.InvitationMessage;
import net.kano.joscar.rvcmd.SegmentedFilename;
import net.kano.joscar.snac.ClientSnacProcessor;
import net.kano.joscar.snac.SnacRequest;
import net.kano.joscar.snac.SnacRequestAdapter;
import net.kano.joscar.snac.SnacResponseEvent;
import net.kano.joscar.snaccmd.CapabilityBlock;
import net.kano.joscar.snaccmd.FullUserInfo;
import net.kano.joscar.snaccmd.ssi.*;
import net.kano.joscar.ssiitem.BuddyItem;
import net.kano.joustsim.Screenname;
import net.kano.joustsim.oscar.*;
import net.kano.joustsim.oscar.oscar.service.Service;
import net.kano.joustsim.oscar.oscar.service.buddy.BuddyService;
import net.kano.joustsim.oscar.oscar.service.buddy.BuddyServiceListener;
import net.kano.joustsim.oscar.oscar.service.icbm.*;
import net.kano.joustsim.oscar.oscar.service.icbm.ft.*;
import net.kano.joustsim.oscar.oscar.service.icbm.ft.events.RvConnectionEvent;
import net.kano.joustsim.oscar.oscar.service.icbm.ft.events.TransferringFileEvent;
import net.kano.joustsim.oscar.oscar.service.ssi.*;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Manages AIM connectivity.  Singlehandedly.
 *
 * @author Alex Rass
 * @since Sep 22, 2004
 */
public class OscarConnection extends AbstractMessageConnection implements FileTransferSupport, IconSupport {
    AimConnection connection;
    private AimConnectionProperties connectionProperties; // use to hold on connection settings
    RvProcessor rvProcessor;
    /**
     * Handle buddy alias changes.
     * Could be static, but for some implementation it may be a problem.  Leaving non-static
     */
    private AliasBuddyListener aliasBuddyListener = new AliasBuddyListener();
//    BasicConnection iconConnection;
//    Map<Contact,FullUserInfo> fullUserInfoCache = Collections.synchronizedMap(new HashMap<Contact, FullUserInfo>(50));

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
        if (connection != null && connection.getInfoService()!=null)
            return connection.getInfoService().getCurrentAwayMessage() != null;
        return super.isAway();
    }

    public void setAway(boolean away) {
        if (connection != null && connection.getInfoService() != null)
            connection.getInfoService().setAwayMessage(away ? getProperties().getIamAwayMessage() : null);
        super.setAway(away);
    }

    private void turnOnLogging() {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new java.util.logging.Formatter() {
            private String lineSeparator = System.getProperty("line.separator");

            public String format(LogRecord record) {
                StringBuffer sb = new StringBuffer();
                sb.append("[");
                sb.append(record.getLevel().getLocalizedName());
                sb.append("] ");
                sb.append(record.getMessage());
                sb.append(lineSeparator);
                if (record.getThrown() != null) {
                    try {
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        record.getThrown().printStackTrace(pw);
                        pw.close();
                        sb.append(sw.toString());
                    } catch (Exception ex) {
                        // SimpleFormatter in the JDK does this, so I do too
                    }
                }
                return sb.toString();
            }
        });
//        Level level = Level.parse(levelstr.toUpperCase());
        handler.setLevel(Level.ALL);
        Logger logger = Logger.getLogger("net.kano.joscar");
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
    }

    final public void connect() throws Exception {
        super.connect();
//        turnOnLogging();
        notifyConnectionInitiated();
        if (getUserName() == null || getPassword() == null) {
            throw new SecurityException("Login information was not available");
        }
        final Screenname screenName = new Screenname(getUserName());
        AppSession appSession = new AppSession() {
            public AimSession openAimSession(Screenname sn) {
                return new DefaultAimSession(this, sn) {
                    // todo finish off secure stuff
//                    public TrustPreferences getTrustPreferences() {
//                        return new PermanentSignerTrustManager(screenName);
//                    }
                };
            }
        };

        AimSession session = appSession.openAimSession(screenName);
        connectionProperties = new AimConnectionProperties(screenName, getPassword());
        connectionProperties.setLoginHost(System.getProperty("OSCAR_HOST", connectionProperties.getLoginHost()));
        connectionProperties.setLoginPort(Integer.getInteger("OSCAR_PORT", connectionProperties.getLoginPort()));
        connection = session.openConnection(connectionProperties);
        catchBuddyList();
        connection.addStateListener(new StateListener() {
            public void handleStateChange(StateEvent event) {
                try {
                    if (State.ONLINE == event.getNewState()) {
                        fireAlmostConnected();
                        notifyConnectionEstablished();
                    } else if (State.FAILED == event.getNewState()) {
                        fireFailedToConnect();
                    } else if (State.DISCONNECTED == event.getNewState()) {
                        fireDisconnect();
                    }
                } catch (Exception e) {
                    System.out.println("StateListener: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        connection.getCapabilityManager().setCapabilityHandler(
                CapabilityBlock.BLOCK_FILE_SEND, new CapabilityHandler() {
                    public boolean isEnabled() { return true; }
                    public void handleAdded(CapabilityManager manager) { }
                    public void handleRemoved(CapabilityManager manager) { }

                    public void addCapabilityListener(CapabilityListener capabilityListener) {}
                    public void removeCapabilityListener(CapabilityListener capabilityListener) {}
                });
        connection.connect();
    }

/*
    /**
     *Used for icon service
     * /
    private class OscarServiceFactory implements ServiceFactory {
        public Service getService(net.kano.joustsim.oscar.oscar.OscarConnection conn, int family) {
            if (family == ConnCommand.FAMILY_CONN) {
                return new BosService(connection, conn){

                };
            } else if (family == IcbmCommand.FAMILY_ICBM) {
                return new IcbmService(connection, conn);
            } else if (family == BuddyCommand.FAMILY_BUDDY) {
                return new BuddyService(connection, conn);
            } else if (family == LocCommand.FAMILY_LOC) {
                return new InfoService(connection, conn);
            } else if (family == SsiCommand.FAMILY_SSI) {
                return new SsiService(connection, conn);
            } else {
                System.out.println("no service for family " + family);
                return null;
            }
        }
    }
*/

    IcbmListener lastIcbmListener;
    /**
     * Manage a list of ppl.
     */
    private void catchBuddyList() {
        connection.addOpenedServiceListener(new OpenedServiceListener() {
            public void closedServices(AimConnection aimConnection, Collection<? extends Service> collection) {
                //Todo change
            }

            public void openedServices(AimConnection conn, Collection<? extends Service> services) {
                for (Service service:services) {
                    if (service instanceof SsiService) {
                        ((SsiService) service).getBuddyList().addRetroactiveLayoutListener(new BuddyListLayoutListener() {
                            public void groupsReordered(BuddyList list, List<? extends net.kano.joustsim.oscar.oscar.service.ssi.Group> oldOrder, List<? extends net.kano.joustsim.oscar.oscar.service.ssi.Group> newOrder) {
                                for (net.kano.joustsim.oscar.oscar.service.ssi.Group group:newOrder) {
                                    Group bGroup = getGroupFactory().create(group.getName());
                                    getGroupList().remove(bGroup);
                                    getGroupList().add(bGroup);
                                } // that should reorder it.
                                notifyListeners();
                            }

                            public void groupAdded(BuddyList list, List<? extends net.kano.joustsim.oscar.oscar.service.ssi.Group> oldItems, List<? extends net.kano.joustsim.oscar.oscar.service.ssi.Group> newItems, net.kano.joustsim.oscar.oscar.service.ssi.Group group, List<? extends Buddy> buddies) {
                                Group bGroup = getGroupFactory().create(group.getName());
                                getGroupList().add(bGroup);
                                notifyListeners();
                            }

                            public void groupRemoved(BuddyList list, List<? extends net.kano.joustsim.oscar.oscar.service.ssi.Group> oldItems, List<? extends net.kano.joustsim.oscar.oscar.service.ssi.Group> newItems, net.kano.joustsim.oscar.oscar.service.ssi.Group group) {
                                Group bGroup = getGroupFactory().create(group.getName());
                                for (int i = 0; i < bGroup.size(); i++) {
                                    Nameable contact = bGroup.get(i);
                                    if (contact instanceof Contact) {
                                        if (!OscarConnection.this.equals(((Contact) contact).getConnection()))
                                            return; // others found, don't nuke group
                                    } else {
                                        return; // others found, don't nuke group
                                    }
                                }
                                getGroupList().remove(bGroup);
                                notifyListeners();
                            }

                            public void buddyAdded(BuddyList list, net.kano.joustsim.oscar.oscar.service.ssi.Group group, List<? extends Buddy> oldItems, List<? extends Buddy> newItems, Buddy buddy) {
                                Group bGroup = getGroupFactory().create(group.getName());
                                Contact contact = getContactFactory().create(buddy.getScreenname().getNormal(), OscarConnection.this);
//                                if (buddy.getAlias()==null) System.out.println("!! Alias was null for " + buddy.getScreenname().getNormal() + " !!");
                                // all offline ppl will have null aliases.
                                contact.setDisplayName(buddy.getAlias()!=null?buddy.getAlias():buddy.getScreenname().getFormatted());
                                buddy.addBuddyListener(aliasBuddyListener);
                                bGroup.add(contact);
                                notifyListeners();
                            }

                            public void buddyRemoved(BuddyList list, net.kano.joustsim.oscar.oscar.service.ssi.Group group, List<? extends Buddy> oldItems, List<? extends Buddy> newItems, Buddy buddy) {
                                Group bGroup = getGroupFactory().create(group.getName());
                                bGroup.remove(getContactFactory().create(buddy.getScreenname().getNormal(), OscarConnection.this));
                                notifyListeners();
                            }

                            public void buddiesReordered(BuddyList list, net.kano.joustsim.oscar.oscar.service.ssi.Group group, List<? extends Buddy> oldBuddies, List<? extends Buddy> newBuddies) {
                                Group bGroup = getGroupFactory().create(group.getName());
                                for (Buddy buddy : newBuddies) {
                                    Contact contact = getContactFactory().create(buddy.getScreenname().getNormal(), OscarConnection.this);
                                    bGroup.remove(contact);
                                    bGroup.add(contact);
                                } // that should reorder it.
                                notifyListeners();
                            }

                            private void notifyListeners() {
                                for (ConnectionEventListener eventHandler : eventHandlers) {
                                    eventHandler.statusChanged(OscarConnection.this);
                                }

                            }
                        });
                    }
                }
            }
        });
    }

/*
    private void catchBuddyListOld() {
        connection.addOpenedServiceListener(new OpenedServiceListener() {
            public void closedServices(AimConnection aimConnection, Collection<? extends Service> collection) {
                //Todo change
            }

            public void openedServices(AimConnection aimConnection, Collection<? extends Service> services) {
                for (Service service:services) {
                    if (service instanceof SsiService) {
                        service.getOscarConnection().getSnacProcessor().addGlobalResponseListener(new SnacResponseListener() {
                            public void handleResponse(SnacResponseEvent event) {
                                SnacCommand snac = event.getSnacCommand();
                                if (!(snac instanceof SsiDataCmd))
                                    return;
                                try {
//                System.out.println("DG: " + snac.getClass() + ":" + snac.getCommand() + ":" + snac.getFamily());
                                    SsiDataCmd sdc = (SsiDataCmd) snac;
//                System.out.println("DG2: " + sdc.getItems().length);
                                    int count=0;
                                    SsiItem ssiItem;
                                    Group lastGroup = null;
                                    for (int i = 0; i < sdc.getItems().size(); i++) {
                                        ssiItem = sdc.getItems().get(i);
                                        if (ssiItem.getItemType() == SsiItem.TYPE_GROUP) {
//                        System.out.println(" g " + ssiItem.getName());
                                            if (ssiItem.getName().length() == 0)
                                                continue;
                                            getGroupList().add(lastGroup = GroupWrapper.create(ssiItem.getName()));
//                            lastGroup.clear(OscarConnection.this); // b/c of reconnect // we offline everyone.  so don't delete
                                        } else if (ssiItem.getItemType() == SsiItem.TYPE_BUDDY) {
                                            if (lastGroup != null) {
                                                lastGroup.add(getContactFactory().create(ssiItem.getName(), OscarConnection.this));
                                                count++;
                                            } else
                                                System.out.println("Missing a group for " + ssiItem.getName());
                                        } else if (ssiItem.getItemType() == SsiItem.TYPE_ICON_INFO) {
                                            // todo handle icons?
//                        Contact contact = getContactFactory().create(ssiItem.getName(), OscarConnection.this);
//                        ImageIcon icon = new ImageIcon(ssiItem.getData().toByteArray());
//                        contact.setIcon(icon);
                                        } else {
                                            System.out.println("Ignoring: "+ ssiItem.getItemType() + ":" + ssiItem.getName());
                                        }
                                    } // for
                                    System.out.println("Added "+ count + " buddies.");
                                    for (ConnectionEventListener eventHandler : eventHandlers) {
                                        eventHandler.statusChanged(OscarConnection.this);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }
        });
    }
*/
    /**
     * Called when a connection has been established.
     */
    private void fireAlmostConnected() {
        // get the list
        ClientSnacProcessor processor = connection.getBosService().getOscarConnection().getSnacProcessor();

          ///////////////////
         // icon service //
        //////////////////

/*
        try {
            connection.getBosService().sendSnacRequest(new ServiceRequest(IconCommand.FAMILY_ICON), new SnacRequestAdapter() {
                public void handleResponse(SnacResponseEvent e) {
                    try {
                        if (e.getSnacCommand() instanceof ServiceRedirect) {
                            ServiceRedirect sr = (ServiceRedirect) e.getSnacCommand();
                            iconConnection = new BasicConnection(sr.getRedirectHost(),
                                    sr.getRedirectPort() > 0 ? sr.getRedirectPort() : connectionProperties.getLoginPort());
                            iconConnection.setCookie(sr.getCookie());
                            iconConnection.setServiceFactory(new OscarServiceFactory());
                            iconConnection.getClientFlapConn().getFlapProcessor().addExceptionHandler(new ConnProcessorExceptionHandler() {
                                public void handleException(ConnProcessorExceptionEvent event) {
                                    event.getException().printStackTrace();
                                }
                            });
                            iconConnection.connect();
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

*/


          //////////////////////////////
         //   setup the rvProcessor  //
        /////////////////////////////
        rvProcessor = new RvProcessor(processor);
        rvProcessor.registerRvCmdFactory(new DefaultRvCommandFactory());

        //////////////////////////////////////
       //   setup file transfer listener  //
      /////////////////////////////////////

        connection.getIcbmService().getRvConnectionManager().addConnectionManagerListener(new RvConnectionManagerListener() {
            public void handleNewIncomingConnection(RvConnectionManager manager, IncomingRvConnection transfer) {
                if (transfer instanceof FileTransfer) {
                    FileTransfer fileTransfer = (FileTransfer) transfer;
                    for (ConnectionEventListener eventHandler : eventHandlers) {
                        try {
                            eventHandler.fileReceiveRequested(
                                    OscarConnection.this,
                                    getContactFactory().create(transfer.getBuddyScreenname().getFormatted(), OscarConnection.this),
                                    fileTransfer.getRequestFileInfo().getFilename(),
                                    fileTransfer.getInvitationMessage().getMessage(),
                                    transfer);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } // for all eventHandlers
                } // if
            }
        });

/*  Old Code
        rvProcessor.addListener(new RvProcessorListener() {
            public void handleNewSession(NewRvSessionEvent event) {
                System.out.println("new session.\n" +
                        "  incoming:"+ NewRvSessionEvent.TYPE_INCOMING.equals(event.getSessionType()) +
                        "  outgoing:"+ NewRvSessionEvent.TYPE_OUTGOING.equals(event.getSessionType()) +
                        "\n  Event:" + event + "|" + event.getSession());
                if (NewRvSessionEvent.TYPE_INCOMING.equals(event.getSessionType()))
                    event.getSession().addListener(new RvSessionListener() {
                        public void handleRv(RecvRvEvent event) {
                            System.out.println( "RvSessionListener.handleRv:  " + event + "|" + event.getRvCommand());
                            // if it's a FileSendReqRvCmd, start the process
                            if (event.getRvCommand() instanceof FileSendReqRvCmd) {
                                // determine it's a file transfer, cast what's needed and extract file information
                                FileSendReqRvCmd fileSendReqRvCmd = (FileSendReqRvCmd) event.getRvCommand();
                                ConnectionInfo info = new ConnectionInfo(
//                                        fileSendReqRvCmd.getConnInfo().getInternalIP(),
                                        fileSendReqRvCmd.getConnInfo().getExternalIP(),
                                        fileSendReqRvCmd.getConnInfo().getPort());
                                for (ConnectionEventListener eventHandler : eventHandlers) {
                                    try {
                                        eventHandler.fileReceiveRequested(
                                                OscarConnection.this,
                                                getContactFactory().create(event.getRvSession().getScreenname(), OscarConnection.this),
                                                fileSendReqRvCmd.getFileSendBlock().getFilename(), // file
                                                fileSendReqRvCmd.getMessage().getMessage() == null ? "" :
                                                        GeneralUtils.stripHTML(fileSendReqRvCmd.getMessage().getMessage()), // message
                                                info);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                        public void handleSnacResponse(RvSnacResponseEvent event) {
                            //no care
                            System.out.println("RvSessionListener.handleSnacResponse: incoming snac");
                        }
                    });
            }
        });
  */
          //////////////////////////////
         //     get list of People  //
        /////////////////////////////

/*
        processor.sendSnac(new SnacRequest(new SsiDataRequest(), new SnacRequestAdapter() {
            public void handleResponse(SnacResponseEvent event) {
                try {
                    SnacCommand snac = event.getSnacCommand();
//                System.out.println("DG: " +
//                        snac.getClass() +
//                        ":" + snac.getCommand() +
//                        ":" + snac.getFamily());
                    SsiDataCmd sdc = (SsiDataCmd) snac;
//                System.out.println("DG2: " + sdc.getItems().length);
                    int count=0;
                    SsiItem ssiItem;
                    Group lastGroup = null;
                    for (int i = 0; i < sdc.getItems().length; i++) {
                        ssiItem = sdc.getItems()[i];
                        if (ssiItem.getItemType() == SsiItem.TYPE_GROUP) {
//                        System.out.println(" g " + ssiItem.getName());
                            if (ssiItem.getName().length() == 0)
                                continue;
                            getGroupList().add(lastGroup = GroupWrapper.create(ssiItem.getName()));
//                            lastGroup.clear(OscarConnection.this); // b/c of reconnect // we offline everyone.  so don't delete
                        } else if (ssiItem.getItemType() == SsiItem.TYPE_BUDDY) {
                            if (lastGroup != null) {
                                lastGroup.add(getContactFactory().create(ssiItem.getName(), OscarConnection.this));
                                count++;
                            } else
                                System.out.println("Missing a group for " + ssiItem.getName());
                        } else if (ssiItem.getItemType() == SsiItem.TYPE_ICON_INFO) {
                            // todo handle icons?
//                        Contact contact = getContactFactory().create(ssiItem.getName(), OscarConnection.this);
//                        ImageIcon icon = new ImageIcon(ssiItem.getData().toByteArray());
//                        contact.setIcon(icon);
                        } else {
                            System.out.println("Ignoring: "+ ssiItem.getItemType() + ":" + ssiItem.getName());
                        }
                    } // for
                    System.out.println("Added "+ count + " buddies.");
                    for (int i = 0; i < eventHandlers.size(); i++) {
                        ((ConnectionEventListener) eventHandlers.get(i)).statusChanged(OscarConnection.this);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public void handleTimeout(SnacRequestTimeoutEvent event) {
                System.out.println("Received a timeout for buddy list request. " + OscarConnection.this);
            }
        }));
*/

        { // setup conversation support
            IcbmService icbmService = connection.getIcbmService();
            icbmService.removeIcbmListener(lastIcbmListener);
            lastIcbmListener = new IcbmListener() {
                public void newConversation(IcbmService service, Conversation conv) {
                    // Adds a conversation listener that tells every listener when a message has been received.
                    conv.addConversationListener(new TypingAdapter());
                }

                public void buddyInfoUpdated(IcbmService service, Screenname buddy, IcbmBuddyInfo info) {
                    // don't care yet
                    System.out.println("Buddy Info Updated. - " + buddy.getNormal() + " " + info);
                }
            };
            icbmService.addIcbmListener(lastIcbmListener);
        } // setup conversation support

//        final DelayedThread updateStatus = new DelayedThread(1000, new Runnable() {
//
//        });

        connection.getBuddyService().addBuddyListener(new BuddyServiceListener() {
            public void gotBuddyStatus(BuddyService service, Screenname buddy, FullUserInfo info) {
                try {
//                System.out.println("Buddy status update. " + buddy.getFormatted() + " " + buddy.getFormatted());
                    Contact contact = getContactFactory().create(buddy.getNormal(), OscarConnection.this);
                    // update to the latest
                    contact.setDisplayName(buddy.getFormatted());
                    contact.getStatus().setWireless((info.getFlags() & FullUserInfo.MASK_WIRELESS) > 0);
//                    fullUserInfoCache.put(contact, info); // no longer using this. it's been tracked
//                    requestPictureForUser(contact, info);
                    for (ConnectionEventListener eventHandler : eventHandlers) { //online: info.getOnSince().getTime() > 0
                        eventHandler.statusChanged(OscarConnection.this, contact, true, info.getAwayStatus(), info.getIdleMins());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } // f-n

            public void buddyOffline(BuddyService service, Screenname buddy) {
//                System.out.println("Buddy went offline. " + buddy.getNormal() + " " + buddy.getFormatted());
                Contact contact = getContactFactory().create(buddy.getFormatted(), OscarConnection.this);
                for (ConnectionEventListener eventHandler : eventHandlers) { //online: info.getOnSince().getTime() > 0
                    eventHandler.statusChanged(OscarConnection.this, contact, false, true, 0);
                }
                contact.getStatus().setOnline(false);
            }
        });
    }

    class TypingAdapter extends ConversationAdapter implements TypingListener {
            public void gotMessage(Conversation c, final MessageInfo minfo) {
                Message message = new MessageImpl(getContactFactory().create(minfo.getFrom().getFormatted(), OscarConnection.this),
                        false, minfo.getMessage().isAutoResponse(), minfo.getMessage().getMessageBody());
                for (int i = 0; i < eventHandlers.size(); i++) {
                    try {
                        (eventHandlers.get(i)).messageReceived(OscarConnection.this, message);
                    } catch (Exception e) {
                        for (ConnectionEventListener eventHandler : eventHandlers) {
                            eventHandler.errorOccured("Failure while receiving a message", e);
                        }
                    }
                }
            }

        public void gotTypingState(Conversation conversation, TypingInfo typingInfo) {
            if (typingInfo.getTypingState().equals(TypingState.TYPING))
                for (ConnectionEventListener connectionEventListener : eventHandlers) {
                    connectionEventListener.typingNotificationReceived(OscarConnection.this,
                            getContactFactory().create(typingInfo.getFrom().getFormatted(), OscarConnection.this));
                }
        }
    }

    private void fireFailedToConnect() {
        for (ConnectionEventListener eventHandler : eventHandlers) {
            eventHandler.connectionFailed(this, "Failed to connect");
        }
    }

    private void fireDisconnect() {
        System.out.println("firing Connection lost.");
        for (ConnectionEventListener eventHandler : eventHandlers) {
            eventHandler.connectionLost(this);
        }
    }


    public void reconnect() {
        if (connectionProperties != null && !connection.wantedDisconnect() && getGroupList().size() > 0)
            try {
                connect();
            } catch (Exception e) {
                e.printStackTrace(); // no big deal, but lets see 
            }
    }

    public void disconnect(boolean intentional) {
        if (connection!=null)
            connection.disconnect(intentional);
        super.disconnect(intentional);
    }

    public boolean isLoggedIn() {
        return connection!=null && State.ONLINE == connection.getState();
    }

    public void cancel() {
        if (connection!=null)
            connection.disconnect();
    }

    public void setTimeout(int timeout) {
    }

    // todo when offline, getIcbmService will return null
    public void processMessage(Message message) {
        Conversation conversation = connection.getIcbmService().getImConversation(new Screenname(message.getContact().getName()));
        conversation.sendMessage(new  SimpleMessage(GeneralUtils.makeHTML(message.getText()), message.isAutoResponse()));
//        conversation.close(); // DON'T do that
    }

    public void processSecureMessage(Message message) throws IOException {
        Conversation conversation = connection.getIcbmService().getSecureAimConversation(new Screenname(message.getContact().getName()));
        conversation.sendMessage(new SimpleMessage(GeneralUtils.makeHTML(message.getText())));
    }

    /**
     * Finds a group.  Helper.
     * @param group to find
     * @return group or null
     */
    net.kano.joustsim.oscar.oscar.service.ssi.Group findGroup(Group group) {
        java.util.List<? extends net.kano.joustsim.oscar.oscar.service.ssi.Group> list = connection.getSsiService().getBuddyList().getGroups();
        for (net.kano.joustsim.oscar.oscar.service.ssi.Group aimGroup : list) {
            if (aimGroup instanceof MutableGroup && group.getName().equalsIgnoreCase(aimGroup.getName())) {
                return aimGroup;
            }
        }
        return null;
    }

    Buddy findBuddy(Contact contact) {
        java.util.List<? extends net.kano.joustsim.oscar.oscar.service.ssi.Group> list = connection.getSsiService().getBuddyList().getGroups();
        for (net.kano.joustsim.oscar.oscar.service.ssi.Group aimGroup : list) {
            if (aimGroup instanceof MutableGroup) {
                for(Buddy buddy: aimGroup.getBuddiesCopy()) {
                    if (contact.getName().equalsIgnoreCase(buddy.getScreenname().getNormal()))
                        return buddy;
                }
            }
        }
        return null;
    }
    Buddy findBuddyViaGroup(Nameable contact, Group group, boolean inGroup) {
        java.util.List<? extends net.kano.joustsim.oscar.oscar.service.ssi.Group> list = connection.getSsiService().getBuddyList().getGroups();
        for (net.kano.joustsim.oscar.oscar.service.ssi.Group aimGroup : list) {
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
        return null;
    }

    /**
     * Call to add a new contact to your list.
     *
     * @param contact to add
     * @param group   to add to
     */
    public void addContact(final Nameable contact, final Group group) {
        net.kano.joustsim.oscar.oscar.service.ssi.Group aimGroup = findGroup(group);
        if (aimGroup == null) {
            addContactGroup(group);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {// no need
            }
            aimGroup = findGroup(group);
        }
        if (aimGroup == null) { // still null?
            for (ConnectionEventListener eventHandler : eventHandlers) {
                eventHandler.errorOccured("Failed to create the group.  Try again.", null);
            }
            return;
        }
        if (aimGroup instanceof MutableGroup) {
            ((MutableGroup) aimGroup).addBuddy(contact.getName());
        } else {
            for (ConnectionEventListener eventHandler : eventHandlers) {
                eventHandler.errorOccured("This is a special group. Can not add contacts.", null);
            }
        }
        /*
        // add it for the server
        final ClientSnacProcessor processor = connection.getBosService().getOscarConnection().getSnacProcessor();
        processor.sendSnac(new SnacRequest(new SsiDataRequest(), new SnacRequestAdapter() {
            public void handleResponse(SnacResponseEvent event) {
                SnacCommand snac = event.getSnacCommand();
                SsiDataCmd sdc = (SsiDataCmd) snac;
                SsiItem ssiItem = null;
                for (int i = 0; i < sdc.getItems().size(); i++) {
                    ssiItem = sdc.getItems().get(i);
                    if (ssiItem.getItemType() == SsiItem.TYPE_GROUP) {
//                        System.out.println(" g " + ssiItem.getName());
                        if (group.getName().equals(ssiItem.getName())) {
                            break;
                        }
                    }
                } // for
                // and now that we have found the group:
                if (ssiItem != null) {
                    BuddyItem newContact = new BuddyItem(contact.getName(), ssiItem.getParentId(), (int)(Math.random() * 50 + 10));
//                            new SsiItem(contact.getName(), ssiItem.getId(), ssiItem.getTotalSize()+1, SsiItem.TYPE_BUDDY, null);
                    SsiItem inparam[] = new SsiItem[1];
                    inparam[0] = newContact.toSsiItem();
                    processor.sendSnac(new SnacRequest(new CreateItemsCmd(inparam), new SnacRequestAdapter() {
                        public void handleResponse(SnacResponseEvent event) {
                            // handle errors
                            if (event.getSnacCommand() instanceof SsiDataModResponse) {
                                SsiDataModResponse response = (SsiDataModResponse) event.getSnacCommand();
                                if (response.getResults()[0] != SsiDataModResponse.RESULT_SUCCESS) {
                                    System.out.println("Problem creating a user: " + response.toString());
                                } else {
                                    group.add(contact);
                                    for (int i = 0; i < eventHandlers.size(); i++) {
                                        ((ConnectionEventListener) eventHandlers.get(i)).statusChanged(OscarConnection.this);
                                    }
                                } // everything went ok

                            }

                        }
                    }));
                } else { // ssItem == null
                    System.out.println("Problem: couldn't find any groups! " + ssiItem + " | " + group.getName());
                    for (int i = 0; i < eventHandlers.size(); i++) {
                        ((ConnectionEventListener) eventHandlers.get(i)).errorOccured("Could not find any groups!", null);
                    }
                }
            }
        }));

*/
    } // addContact

    /**
     * Call to remove a contact you no longer want.
     *
     * @param contact to remove
     */
    public void removeContact(final Nameable contact) {
//        if (contact instanceof ContactWrapper) {
//            Group group = ((ContactWrapper) contact).get
//        }
//        net.kano.joustsim.oscar.oscar.service.ssi.Group aimGroup = findGroup(group);
//        if (contact instanceof ContactWrapper) {
//            Buddy buddy = findBuddy((Contact) contact);
//            ((MutableGroup)aimGroup).re
//            buddy.getBuddyList().
//            connection.getSsiService().getBuddyList().
//        }


        // add it for the server
        final ClientSnacProcessor processor = connection.getBosService().getOscarConnection().getSnacProcessor();

        processor.sendSnac(new SnacRequest(new SsiDataRequest(), new SnacRequestAdapter() {
            public void handleResponse(SnacResponseEvent event) {
                SnacCommand snac = event.getSnacCommand();
                SsiDataCmd sdc = (SsiDataCmd) snac;
                SsiItem ssiItem;
//                SsiItem rightGroup=null;
                SsiItem rightContact = null;
                for (int i = 0; i < sdc.getItems().size(); i++) {
                    ssiItem = sdc.getItems().get(i);
                    if (ssiItem.getItemType() == SsiItem.TYPE_BUDDY) {
                        if (contact.getName().equalsIgnoreCase(ssiItem.getName())) {
                            rightContact = ssiItem;
                            break;
                        }
                    }
                } // for

                if (rightContact != null) {
                    BuddyItem newContact = new BuddyItem(rightContact);
                    SsiItem inparam[] = new SsiItem[1];
                    inparam[0] = newContact.toSsiItem();
                    processor.sendSnac(new SnacRequest(new DeleteItemsCmd(inparam), new SnacRequestAdapter() {
                        public void handleResponse(SnacResponseEvent event) {
                            // handle errors
                            if (event.getSnacCommand() instanceof SsiDataModResponse) {
                                SsiDataModResponse response = (SsiDataModResponse) event.getSnacCommand();
                                if (response.getResults()[0] != SsiDataModResponse.RESULT_SUCCESS) {
                                    System.out.println("Problem deleting a user: " + response.toString());
                                } else {
                                    GroupList list = getGroupList();
                                    for (int i = 0; i < list.size(); i++) {
                                        list.get(i).remove(contact);
                                    }
                                    for (ConnectionEventListener eventHandler : eventHandlers) {
                                        eventHandler.statusChanged(OscarConnection.this);
                                    }
                                }

                            }

                        }
                    }));

                } else {
                    System.out.println("Problem: couldn't find contact! " + contact.getName());
                    // todo what to do if group isn't found
                }
            }
        }));
    } // removeContact

    public void addContactGroup(Group group) {
        connection.getSsiService().getBuddyList().addGroup(group.getName());
    }

    public void removeContactGroup(Group group) {
        net.kano.joustsim.oscar.oscar.service.ssi.Group aimGroup = findGroup(group);
        if (aimGroup != null && aimGroup instanceof MutableGroup) {
            connection.getSsiService().getBuddyList().deleteGroupAndBuddies(aimGroup);
        }
    }

    public void moveContact(Nameable contact, Group group) {
        net.kano.joustsim.oscar.oscar.service.ssi.Group aimGroup = findGroup(group);
        if (aimGroup == null) {
            addContactGroup(group);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {// no need
            }
            aimGroup = findGroup(group);
        }
        if (aimGroup == null) { // still null?
            for (ConnectionEventListener eventHandler : eventHandlers) {
                eventHandler.errorOccured("Failed to create the group.  Try again.", null);
            }
            return;
        }

        List <Buddy> buddies = new ArrayList<Buddy>();
        Buddy buddy = findBuddyViaGroup(contact, group, false);
        if (buddy == null) {
            for (ConnectionEventListener eventHandler : eventHandlers) {
                eventHandler.errorOccured("Failed to find buddy " + contact.getName() + " not in target group.  Try again.", null);
            }
            return;
        }
        buddies.add(buddy);
        if (aimGroup instanceof MutableGroup) {
            connection.getSsiService().getBuddyList().moveBuddies(buddies, (MutableGroup)aimGroup);
        }
    }


    public void initiateFileTransfer(final FileTransferListener ftl) throws IOException {
        OutgoingFileTransfer oft = connection.getIcbmService().getRvConnectionManager().createOutgoingFileTransfer(new Screenname(ftl.getContactName()));
        oft.addEventListener(new RvConnectionEventListener() {
            public void handleEventWithStateChange(RvConnection transfer, RvConnectionState state, RvConnectionEvent event) {
                System.out.println("handleEventWithStateChange");
                if (state==FileTransferState.CONNECTING)
                    ftl.notifyNegotiation();
                else if (state==FileTransferState.FINISHED)
                    ftl.notifyDone();
                else if (state==FileTransferState.FAILED)
                    ftl.notifyFail();
                else if (state==FileTransferState.TRANSFERRING)
                    ftl.notifyTransfer();
            }

            public void handleEvent(RvConnection transfer, RvConnectionEvent event) {
                //Todo change
                System.out.println("handleEvent");
            }
        });


        oft.setSingleFile(ftl.getFile());
        oft.sendRequest(new InvitationMessage(ftl.getFileDescription()));
    }

/*
    public void initiateFileTransferOld(FileTransferListener ftl) {
        Logger logger = Logger.getLogger("net.kano");
        logger.setLevel(Level.ALL);
        Handler h = new ConsoleHandler();
        h.setLevel(Level.ALL);
        logger.addHandler(h);

        RvSession session = rvProcessor.createRvSession(ftl.getContactName());

        ServerSocket socket;
        SendFileThread service;
        try {
            socket = new ServerSocket(4477);
        } catch (IOException e) {
            e.printStackTrace();
            ftl.notifyFail();
            return;
        }
        service = new SendFileThread(ftl, socket);
        ftl.setTransferService(service);
        service.start();

//        InetAddress localHost = socket.getInetAddress(); //InetAddress.getAllByName(InetAddress.getLocalHost().getHostName())
        try {
            int port = socket.getLocalPort();
            InetAddress localHosts[] = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
            session.addListener(new RvSessionListener() {
                public void handleRv(RecvRvEvent event) {
                    System.out.println("hr: " + event);
                }

                public void handleSnacResponse(RvSnacResponseEvent event) {
                    System.out.println("hsr: " + event);
                }
            });

            for (int i = 0; i < localHosts.length; i++) {
                session.sendRv(new FileSendReqRvCmd(new InvitationMessage(ftl.getFileDescription()),
                        RvConnectionInfo.createForOutgoingRequest(localHosts[i], port),
                        new FileSendBlock(ftl.getFile().getName(), ftl.getFile().length())));

                if (i + 1 < localHosts.length)
                    Thread.sleep(30000);
            }
        } catch (InterruptedException e) {
            service.cancelTransfer();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            service.cancelTransfer();
        }
    } // initiateFileTransfer
*/
    public void rejectFileTransfer(Object connectionInfo) {
        ((IncomingFileTransfer) connectionInfo).close();
    }

    public void acceptFileTransfer(final FileTransferListener ftl, Object connectionInfo) {
        IncomingFileTransfer transfer = (IncomingFileTransfer) connectionInfo;
        transfer.setFileMapper(new FileMapper() {
            public File getDestinationFile(SegmentedFilename filename) {
                return ftl.getFile();
            }

            public File getUnspecifiedFilename() {
                return ftl.getFile();
            }
        });

        transfer.addEventListener(new RvConnectionEventListener() {
            public void handleEventWithStateChange(RvConnection transfer, RvConnectionState state, RvConnectionEvent event) {
                System.out.println("handleEventWithStateChange " + event);
                if (state==FileTransferState.CONNECTING)
                    ftl.notifyNegotiation();
                else if (state==FileTransferState.FINISHED) {
                    ftl.notifyDone();
                    ftl.setProgress(100);
                } else if (state==FileTransferState.FAILED)
                    ftl.notifyFail();
                else if (state==FileTransferState.TRANSFERRING) {
                    ftl.notifyTransfer();
                    if (event instanceof TransferringFileEvent) {
                        ProgressStatusProvider psp = ((TransferringFileEvent)event).getProgressProvider();
                        ftl.setProgress((int) Math.max(
                                Math.abs(psp.getLength() - psp.getStartPosition() / psp.getPosition()),
                                100));
                    }
                }
            }

            public void handleEvent(RvConnection transfer, RvConnectionEvent event) {
                if (event instanceof TransferringFileEvent) {
                    ProgressStatusProvider psp = ((TransferringFileEvent)event).getProgressProvider();
                    ftl.setProgress((int) Math.max(
                            Math.abs(psp.getLength() - psp.getStartPosition() / psp.getPosition()),
                            100));
                }
            }
        });
        transfer.accept();

/*        ReceiveFileThread service = null;
        try {
            service = new ReceiveFileThread(ftl, (ConnectionInfo) connectionInfo);
            ftl.setTransferService(service);
            service.start();
        } catch (Exception e) {
            e.printStackTrace();
            ftl.notifyFail();
            if (service != null)
                service.cancelTransfer();
            return;
        }

//        try {
//            session.sendRv(new GetFileReqRvCmd(RvConnectionInfo.createForOutgoingRequest(InetAddress.getLocalHost(),
//                    socket.getLocalPort())));
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
*/
    } // getFile
/*
    public void acceptFileTransfer(FileTransferListener ftl) {
        RvSession session = rvProcessor.createRvSession(connection.getScreenname().getNormal());

        session.addListener(new RvSessionListener() {
            public void handleRv(RecvRvEvent event) {
                System.out.println("aft: here 1");
            }

            public void handleSnacResponse(RvSnacResponseEvent event) {
                System.out.println("aft: here 2");
            }
        });

        ServerSocket socket;
        try {
            socket = new ServerSocket(0);
            new GetFileThread(ftl, session, socket).start();
        } catch (IOException e) {
            e.printStackTrace();

            return;
        }

        try {
            session.sendRv(new GetFileReqRvCmd(RvConnectionInfo.createForOutgoingRequest(InetAddress.getLocalHost(),
                    socket.getLocalPort())));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    } // getFile
*/

    public void requestPictureForUser(final Contact contact) {
/*
        FullUserInfo fui = fullUserInfoCache.get(contact);
        if (fui == null)
            return;
        if (fui.getExtraInfoBlocks() != null)
            for (int i = 0; i < fui.getExtraInfoBlocks().size(); i++) {
                ExtraInfoBlock extraInfoBlock = fui.getExtraInfoBlocks().get(i);
                if (ExtraInfoBlock.TYPE_ICONHASH == extraInfoBlock.getType()) {
                    if (iconConnection != null) {                                       //ExtraInfoData
                        iconConnection.sendSnacRequest(new IconRequest(contact.getName(), extraInfoBlock.getExtraData()), new SnacRequestAdapter() {
                            public void handleResponse(SnacResponseEvent e) {
                                try {
                                    if (e.getSnacCommand() instanceof IconDataCmd) {
                                        IconDataCmd idc = (IconDataCmd) e.getSnacCommand();
                                        contact.setPicture(new ImageIcon(idc.getIconData().toByteArray()));
                                        for (ConnectionEventListener eventHandler : eventHandlers) {
                                            eventHandler.pictureReceived(OscarConnection.this, contact);
                                        }
                                    }
                                } catch (Exception e1) {
                                    e1.printStackTrace();  //Todo change
                                }
                            } // handleResponse
                        });
                    } // if
                } // if
            } // for
*/
        BuddyInfo binfo = connection.getBuddyInfoManager().getBuddyInfo(new Screenname(contact.getName()));
        ByteBlock byteBlock = binfo.getIconData();
        if (byteBlock != null) {
            contact.setPicture(new ImageIcon(byteBlock.toByteArray()));
            for (ConnectionEventListener eventHandler : eventHandlers) {
                eventHandler.pictureReceived(OscarConnection.this, contact);
            }
        }
    } // requestPictureForUser

    /**
     * Will remove the picture.
     */
    public void clearPicture() {
//           iconConnection.sendSnacRequest( new DeleteItemsCmd(new SsiItem[] {
//                    new IconItem("", 0, null)
//                        .toSsiItem()
//                }));
        connection.getMyBuddyIconManager().requestClearIcon();
    }

    /**
     * Use this picture for me.
     *
     * @param picture filename
     */
    public void uploadPicture(final File picture) {
/*
        final SnacRequestListener listener = new SnacRequestListener() {
                            public void handleResponse(SnacResponseEvent e) {
                                e.getRequest(); // need to?
                            }

                            public void handleSent(SnacRequestSentEvent e) {
                                // great!
                                e.getRequest(); // need to?
                            }

                            public void handleTimeout(SnacRequestTimeoutEvent event) {
                                for (ConnectionEventListener eventHandler : eventHandlers) {
                                    eventHandler.errorOccured("Timed out trying to set an icon.", null);
                                }
                            }
                        };
        iconConnection.sendSnacRequest(new UploadIconCmd(ByteBlock.createByteBlock(
                new FileWritable(picture.getAbsolutePath()))), listener);
*/
        connection.getMyBuddyIconManager().requestSetIcon(ByteBlock.createByteBlock(
                new FileWritable(picture.getAbsolutePath())));
    } // uploadPicture

/*    private SnacRequest request(SnacCommand cmd, SnacRequestListener listener) {
        SnacRequest req = new SnacRequest(cmd, listener);
        handleRequest(req);
        return req;
    }
    synchronized void handleRequest(SnacRequest request) {
        int family = request.getCommand().getFamily();
        if (snacMgr.isPending(family)) {
            snacMgr.addRequest(request);
            return;
        }

        BasicConn conn = snacMgr.getConn(family);

        if (conn != null) {
            conn.sendRequest(request);
        } else {
            // it's time to request a service
            if (!(request.getCommand() instanceof ServiceRequest)) {
                System.out.println("requesting " + Integer.toHexString(family)
                        + " service.");
                snacMgr.setPending(family, true);
                snacMgr.addRequest(request);
                request(new ServiceRequest(family));
            } else {
                System.out.println("eep! can't find a service redirector " +
                        "server.");
            }
        }
    }
  */
    private class AliasBuddyListener implements BuddyListener {
        public void screennameChanged(Buddy buddy, Screenname oldScreenname, Screenname newScreenname) {
            // some day perhaps allow that.
        }

        public void aliasChanged(Buddy buddy, String oldAlias, String newAlias) {
            Contact contact = getContactFactory().get(oldAlias, OscarConnection.this);
            if (contact!=null) {
                contact.setDisplayName(newAlias);
            }
        }

        public void buddyCommentChanged(Buddy buddy, String oldComment, String newComment) {
        }

        public void alertActionChanged(Buddy buddy, int oldAlertAction, int newAlertAction) {
        }

        public void alertTimeChanged(Buddy buddy, int oldAlertEvent, int newAlertEvent) {
        }

        public void alertSoundChanged(Buddy buddy, String oldAlertSound, String newAlertSound) {
        }
    }
} // class OscarConnection
