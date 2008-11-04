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
import com.itbs.util.DelayedThread;
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
import net.kano.joustsim.oscar.oscar.service.icbm.dim.Attachment;
import net.kano.joustsim.oscar.oscar.service.icbm.ft.*;
import net.kano.joustsim.oscar.oscar.service.icbm.ft.events.RvConnectionEvent;
import net.kano.joustsim.oscar.oscar.service.icbm.ft.events.TransferringFileEvent;
import net.kano.joustsim.oscar.oscar.service.ssi.*;
import net.kano.joustsim.oscar.proxy.AimProxyInfo;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
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
public class OscarConnection extends AbstractMessageConnection implements FileTransferSupport, IconSupport, SMSSupport {
    private static Logger log = Logger.getLogger(OscarConnection.class.getName());
    AimConnection connection;
    private AimConnectionProperties connectionProperties = new AimConnectionProperties(null, null); // use to hold on connection settings
    RvProcessor rvProcessor;
    /** Conversation support */
    IcbmListener lastIcbmListener;

    DelayedThread heartbeat;

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

    /**
     * Fake system account. Used for heartbeat. And notifying users of duplicate logins.
     * @return name of the system account.
     */
    public String getSystemName() {
        return "aolsystemmsg";
    }

    public boolean isSystemMessage(Nameable contact) {
        return getSystemName().equals(GeneralUtils.getSimplifiedName(contact.getName()));
    }

    public String getSupportAccount() {
        return "JClaimHelp";
    }

    public boolean isAway() {
        if (connection != null && connection.getInfoService()!=null)
            return connection.getInfoService().getLastSetAwayMessage() != null;
//            return connection.getInfoService().getCurrentAwayMessage() != null;
        return super.isAway();
    }

    public void setAway(boolean away) {
        heartbeat.mark();
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


    public String getServerName() {
        return connectionProperties.getLoginHost();
    }

    public void setServerName(String address) {
        if (System.getProperty("OSCAR_HOST") == null) { // if no forced overwrite
            connectionProperties.setLoginHost(address);
        }
    }

    public int getServerPort() {
        return connectionProperties.getLoginPort();
    }

    public void setServerPort(int port) {
        if (System.getProperty("OSCAR_PORT") == null) { // if no forced overwrite
            connectionProperties.setLoginPort(port);
        }
    }

    final public void connect() throws Exception {
        super.connect();
//        turnOnLogging();
        notifyConnectionInitiated();
        if (getUserName() == null || getPassword() == null) {
            throw new SecurityException("Login information was not available");
        }
        if (heartbeat!=null) {
            heartbeat.stopProcessing();
        }
        heartbeat = new DelayedThread(
                this.toString()+" heartbeat",
                29*1000*60, // 29 min
                null, // todo figure out how this should work, cause isLoggedIn isn't right.
                null,
                new Runnable() {
                    public void run() {
                        getUserInfo(getUserName());
                    }
                }

        );
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
        connectionProperties.setScreenname(screenName);
        connectionProperties.setPassword(getPassword());
        connectionProperties.setLoginHost(System.getProperty("OSCAR_HOST", connectionProperties.getLoginHost()));
        connectionProperties.setLoginPort(Integer.getInteger("OSCAR_PORT", connectionProperties.getLoginPort()));
        connection = session.openConnection(connectionProperties);
        if (getProperties() != null && getProperties().getProxyInfo(getServiceName()) !=null) {
            ConnectionInfo connectionInfo = getProperties().getProxyInfo(getServiceName());
            connection.setProxy(AimProxyInfo.forSocks4(connectionInfo.getIp(), connectionInfo.getPort(),
                    getUserName())); // this is for all those pesky logging systems that don't know how to get the uid
//                    System.getProperty("user.name")));
        }
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
                    log.log(Level.SEVERE, "StateListener: ", e);
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
                log.fine("no service for family " + family);
                return null;
            }
        }
    }
*/

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
//                                if (buddy.getAlias()==null) log.fine("!! Alias was null for " + buddy.getScreenname().getNormal() + " !!");
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
//                log.fine("DG: " + snac.getClass() + ":" + snac.getCommand() + ":" + snac.getFamily());
                                    SsiDataCmd sdc = (SsiDataCmd) snac;
//                log.fine("DG2: " + sdc.getItems().length);
                                    int count=0;
                                    SsiItem ssiItem;
                                    Group lastGroup = null;
                                    for (int i = 0; i < sdc.getItems().size(); i++) {
                                        ssiItem = sdc.getItems().get(i);
                                        if (ssiItem.getItemType() == SsiItem.TYPE_GROUP) {
//                        log.fine(" g " + ssiItem.getName());
                                            if (ssiItem.getName().length() == 0)
                                                continue;
                                            getGroupList().add(lastGroup = GroupWrapper.create(ssiItem.getName()));
//                            lastGroup.clear(OscarConnection.this); // b/c of reconnect // we offline everyone.  so don't delete
                                        } else if (ssiItem.getItemType() == SsiItem.TYPE_BUDDY) {
                                            if (lastGroup != null) {
                                                lastGroup.add(getContactFactory().create(ssiItem.getName(), OscarConnection.this));
                                                count++;
                                            } else
                                                log.fine("Missing a group for " + ssiItem.getName());
                                        } else if (ssiItem.getItemType() == SsiItem.TYPE_ICON_INFO) {
                                            // todo handle icons?
//                        Contact contact = getContactFactory().create(ssiItem.getName(), OscarConnection.this);
//                        ImageIcon icon = new ImageIcon(ssiItem.getData().toByteArray());
//                        contact.setIcon(icon);
                                        } else {
                                            log.fine("Ignoring: "+ ssiItem.getItemType() + ":" + ssiItem.getName());
                                        }
                                    } // for
                                    log.fine("Added "+ count + " buddies.");
                                    for (ConnectionEventListener eventHandler : eventHandlers) {
                                        eventHandler.statusChanged(OscarConnection.this);
                                    }
                                } catch (Exception e) {
                                    log.log(Level.SEVERE, "",e);
                                    
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
                        log.log(Level.SEVERE, "",e1);
                    }
                }
            });
        } catch (Exception e) {
            log.log(Level.SEVERE, "",e);            
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
                            log.log(Level.SEVERE, "Problem handling file transfers", e);
                        }
                    } // for all eventHandlers
                } // if
            }
        });

/*  Old Code
        rvProcessor.addListener(new RvProcessorListener() {
            public void handleNewSession(NewRvSessionEvent event) {
                log.fine("new session.\n" +
                        "  incoming:"+ NewRvSessionEvent.TYPE_INCOMING.equals(event.getSessionType()) +
                        "  outgoing:"+ NewRvSessionEvent.TYPE_OUTGOING.equals(event.getSessionType()) +
                        "\n  Event:" + event + "|" + event.getSession());
                if (NewRvSessionEvent.TYPE_INCOMING.equals(event.getSessionType()))
                    event.getSession().addListener(new RvSessionListener() {
                        public void handleRv(RecvRvEvent event) {
                            log.fine( "RvSessionListener.handleRv:  " + event + "|" + event.getRvCommand());
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
                                        log.log(Level.SEVERE, "",e);
                                    }
                                }
                            }
                        }

                        public void handleSnacResponse(RvSnacResponseEvent event) {
                            //no care
                            log.fine("RvSessionListener.handleSnacResponse: incoming snac");
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
//                log.fine("DG: " +
//                        snac.getClass() +
//                        ":" + snac.getCommand() +
//                        ":" + snac.getFamily());
                    SsiDataCmd sdc = (SsiDataCmd) snac;
//                log.fine("DG2: " + sdc.getItems().length);
                    int count=0;
                    SsiItem ssiItem;
                    Group lastGroup = null;
                    for (int i = 0; i < sdc.getItems().length; i++) {
                        ssiItem = sdc.getItems()[i];
                        if (ssiItem.getItemType() == SsiItem.TYPE_GROUP) {
//                        log.fine(" g " + ssiItem.getName());
                            if (ssiItem.getName().length() == 0)
                                continue;
                            getGroupList().add(lastGroup = GroupWrapper.create(ssiItem.getName()));
//                            lastGroup.clear(OscarConnection.this); // b/c of reconnect // we offline everyone.  so don't delete
                        } else if (ssiItem.getItemType() == SsiItem.TYPE_BUDDY) {
                            if (lastGroup != null) {
                                lastGroup.add(getContactFactory().create(ssiItem.getName(), OscarConnection.this));
                                count++;
                            } else
                                log.fine("Missing a group for " + ssiItem.getName());
                        } else if (ssiItem.getItemType() == SsiItem.TYPE_ICON_INFO) {
                            // todo handle icons?
//                        Contact contact = getContactFactory().create(ssiItem.getName(), OscarConnection.this);
//                        ImageIcon icon = new ImageIcon(ssiItem.getData().toByteArray());
//                        contact.setIcon(icon);
                        } else {
                            log.fine("Ignoring: "+ ssiItem.getItemType() + ":" + ssiItem.getName());
                        }
                    } // for
                    log.fine("Added "+ count + " buddies.");
                    for (int i = 0; i < eventHandlers.size(); i++) {
                        ((ConnectionEventListener) eventHandlers.get(i)).statusChanged(OscarConnection.this);
                    }
                } catch (Exception e) {
                    log.log(Level.SEVERE, "",e);

                }
            }

            public void handleTimeout(SnacRequestTimeoutEvent event) {
                log.fine("Received a timeout for buddy list request. " + OscarConnection.this);
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

                public void sendAutomaticallyFailed(IcbmService service, net.kano.joustsim.oscar.oscar.service.icbm.Message message, Set<Conversation> triedConversations) {
                    log.warning("Automatically Failed message.");
                    notifyErrorOccured("The service provider has failed to deliver previous message. ", null);
                }

                public void buddyInfoUpdated(IcbmService service, Screenname buddy, IcbmBuddyInfo info) {
                    // don't care yet
                    log.fine("Buddy Info Updated. - " + buddy.getNormal() + " " + info);
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
//                log.fine("Buddy status update. " + buddy.getFormatted() + " " + buddy.getFormatted());
                    Contact contact = getContactFactory().create(buddy.getNormal(), OscarConnection.this);
                    // update to the latest
                    contact.setDisplayName(buddy.getFormatted());
                    Status oldStatus = (Status) contact.getStatus().clone();
                    contact.getStatus().setWireless((info.getFlags() & FullUserInfo.MASK_WIRELESS) > 0);
                    contact.getStatus().setOnline(true);
                    contact.getStatus().setAway(info.getAwayStatus());
                    contact.getStatus().setIdleTime(info.getIdleMins());
//                    fullUserInfoCache.put(contact, info); // no longer using this. it's been tracked
//                    requestPictureForUser(contact, info);
                    for (ConnectionEventListener eventHandler : eventHandlers) { //online: info.getOnSince().getTime() > 0
                        eventHandler.statusChanged(OscarConnection.this, contact, oldStatus);
                    }
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Problem with buddy service", e);
                }
            } // f-n

            public void buddyOffline(BuddyService service, Screenname buddy) {
//                log.fine("Buddy went offline. " + buddy.getNormal() + " " + buddy.getFormatted());
                Contact contact = getContactFactory().create(buddy.getFormatted(), OscarConnection.this);
                Status oldStatus = (Status) contact.getStatus().clone();
                contact.getStatus().setOnline(false);
                for (ConnectionEventListener eventHandler : eventHandlers) { //online: info.getOnSince().getTime() > 0
                    eventHandler.statusChanged(OscarConnection.this, contact, oldStatus);
                }
            }
        });
    }

    class TypingAdapter extends ConversationAdapter implements TypingListener {
            public void gotMessage(Conversation c, final MessageInfo minfo) {
                String text  = minfo.getMessage().getMessageBody();
                if (minfo.getMessage() instanceof DirectMessage) {
                    log.info("Got a DIM message");
                    DirectMessage directMessage = (DirectMessage) minfo.getMessage();
                    String attachments="";
                    for (Attachment attachment :directMessage.getAttachments()) {
                        attachments += attachment.getId() + "  ";
                    }
                    if (attachments.length()>0) {
                        text += "Attachments to the original message (all ignored): " + attachments;
                    }
                }
                Message message = new MessageImpl(getContactFactory().create(minfo.getFrom().getFormatted(), OscarConnection.this),
                        false, minfo.getMessage().isAutoResponse(), text);

                for (ConnectionEventListener eventHandler : eventHandlers) {
                    try {
                        eventHandler.messageReceived(OscarConnection.this, message);
                    } catch (Exception e) {
                        notifyErrorOccured("Failure while receiving a message", e);
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
        notifyConnectionFailed("Failed to connect");
    }

    private void fireDisconnect() {
        notifyConnectionLost();
    }


    public void reconnect() {
        if (connectionProperties != null && !connection.wantedDisconnect() && getGroupList().size() > 0)
            try {
                connect();
            } catch (Exception e) {
                log.log(Level.INFO, "Failed to reconnect" ,e); // no big deal, but lets see 
            }
    }

    public void disconnect(boolean intentional) {
        if (heartbeat!=null) {
            heartbeat.stopProcessing();
        }
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
        heartbeat.mark();
//        Old way:
//        Conversation conversation = connection.getIcbmService().getImConversation(new Screenname(message.getContact().getName()));
//        conversation.sendMessage(new  SimpleMessage(GeneralUtils.makeHTML(message.getText()), message.isAutoResponse()));
//        conversation.close(); // DON'T do that
        connection.getIcbmService().sendAutomatically(
                new Screenname(message.getContact().getName()),
                new  SimpleMessage(GeneralUtils.makeHTML(message.getText()), message.isAutoResponse())
        ); // send message, use DC if needed.
    }

    public void processSecureMessage(Message message) throws IOException {
        Conversation conversation = connection.getIcbmService().getSecureAimConversation(new Screenname(message.getContact().getName()));
        conversation.sendMessage(new SimpleMessage(GeneralUtils.makeHTML(message.getText())));
    }

    /**
     * Finds an Oscar group from Our Group.  Helper.
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

    /**
     * Finds a contact.
     * It's either matched to be in the group or not to be in the group.
     * @param contact to find
     * @param group to search
     * @param inGroup only match those in the group or only not in group
     * @return buddy reference
     */
    Buddy findBuddyViaGroup(Nameable contact, Group group, boolean inGroup) {
        // get a list of all groups
        java.util.List<? extends net.kano.joustsim.oscar.oscar.service.ssi.Group> list = connection.getSsiService().getBuddyList().getGroups();
        // for each group
        for (net.kano.joustsim.oscar.oscar.service.ssi.Group aimGroup : list) {
            // gm = groups matched
            boolean groupMatch = group.getName().equalsIgnoreCase(aimGroup.getName());
            // define type of search
            if (!inGroup)
              groupMatch = !groupMatch;
            // if really a group and not smth else, and if we match/not, then search for buddy in it.
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
        heartbeat.mark();
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
            notifyErrorOccured("Failed to create the group.  Try again.", null);
            return;
        }
        if (aimGroup instanceof MutableGroup) {
            ((MutableGroup) aimGroup).addBuddy(contact.getName());
        } else {
            notifyErrorOccured("This is a special group. Can not add contacts.", null);
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
//                        log.fine(" g " + ssiItem.getName());
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
                                    log.fine("Problem creating a user: " + response.toString());
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
                    log.fine("Problem: couldn't find any groups! " + ssiItem + " | " + group.getName());
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
     * @param group to erase from
     */
    public boolean removeContact(final Nameable contact, final Group group) {
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
                                    log.fine("Problem deleting a user: " + response.toString());
                                } else {
                                    cleanGroup(group, contact);
                                    for (ConnectionEventListener eventHandler : eventHandlers) {
                                        eventHandler.statusChanged(OscarConnection.this);
                                    }
                                }

                            }

                        }
                    }));

                } else {
                    log.warning("Problem: couldn't find contact! " + contact.getName());
                    // todo what to do if group isn't found
                }
            }
        }));
        return true; // SO FAKE! But we have moveContact implemented properly.
    } // removeContact

    /**
     * Tells the protocol to add a group.
     * @param group to add
     */
    public void addContactGroup(Group group) {
        connection.getSsiService().getBuddyList().addGroup(group.getName());
    }

    public void removeContactGroup(Group group) {
        heartbeat.mark();
        net.kano.joustsim.oscar.oscar.service.ssi.Group aimGroup = findGroup(group);
        if (aimGroup != null && aimGroup instanceof MutableGroup) {
            connection.getSsiService().getBuddyList().deleteGroupAndBuddies(aimGroup);
        }
    }

    public void moveContact(Nameable contact, Group group) {
        moveContact(contact, findGroupViaBuddy(contact), group);
    }
    
    public void moveContact(Nameable contact, Group oldGroup, Group newGroup) {
        heartbeat.mark();
        net.kano.joustsim.oscar.oscar.service.ssi.Group aimGroup = findGroup(newGroup);
        if (aimGroup == null) {
            addContactGroup(newGroup);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {// no need
            }
            aimGroup = findGroup(newGroup);
        }
        if (aimGroup == null) { // still null?
            notifyErrorOccured("Failed to create the newGroup.  Try again.", null);
            return;
        }

        List <Buddy> buddies = new ArrayList<Buddy>();
        Buddy buddy;
        if (oldGroup==null) {
            buddy = findBuddyViaGroup(contact, newGroup, false);
        } else { // better way
            buddy = findBuddyViaGroup(contact, oldGroup, true);
        }
        
        if (buddy == null) {
            notifyErrorOccured("Failed to find buddy " + contact.getName() + " not in source group.  Try again.", null);
            return;
        }
        buddies.add(buddy);
        if (aimGroup instanceof MutableGroup) {
            connection.getSsiService().getBuddyList().moveBuddies(buddies, (MutableGroup)aimGroup);
        }
    }

    public void initiateFileTransfer(final FileTransferListener ftl) throws IOException {
        heartbeat.mark();
        OutgoingFileTransfer oft = connection.getIcbmService().getRvConnectionManager().createOutgoingFileTransfer(new Screenname(ftl.getContactName()));
        oft.addEventListener(new FileTransferEventListener(ftl));


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
            log.log(Level.SEVERE, "",e);
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
                    log.fine("hr: " + event);
                }

                public void handleSnacResponse(RvSnacResponseEvent event) {
                    log.fine("hsr: " + event);
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
            log.log(Level.SEVERE, "",e);
            service.cancelTransfer();
        }
    } // initiateFileTransfer
*/
    public void rejectFileTransfer(Object connectionInfo) {
        ((IncomingFileTransfer) connectionInfo).close();
    }

    /**
     * Provides support for files sent and received.
     */
    static class FileTransferEventListener implements RvConnectionEventListener {
        FileTransferListener ftl;
        public FileTransferEventListener(FileTransferListener ftl) {
            this.ftl = ftl;
        }

        public void handleEventWithStateChange(final RvConnection transfer, RvConnectionState state, RvConnectionEvent event) {
            log.fine("handleEventWithStateChange " + event.getClass() + ": " + event);
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
                    final ProgressStatusProvider psp = ((TransferringFileEvent)event).getProgressProvider();
                    log.fine("TFEWSC: Starting thread for file progress.");
                    new Thread("Transfer for " + transfer.getBuddyScreenname()) {
                        public void run() {
                            log.fine("TFEWSC: Starting with isOpen " + transfer.isOpen());
                            while (transfer.isOpen()) {
                                int progress = (int) Math.min(
                                    Math.abs((float)psp.getPosition() / (float)(psp.getLength() - psp.getStartPosition()) * 100),
                                    100);
                                log.fine("TFEWSC: " + progress + " Details:" + psp.getLength() + " " + psp.getStartPosition() + " " + psp.getPosition());
                                ftl.setProgress(progress);
                                GeneralUtils.sleep(700);
                            }
                        }
                    }.start();
                } else {
                    log.fine("Got a transferring event and it wasn't TransferringFileEvent! " + event);
                }
            }
        }

        public void handleEvent(RvConnection transfer, RvConnectionEvent event) {
            log.fine("FTE: " + event.getClass());
        }
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

        transfer.addEventListener(new FileTransferEventListener(ftl));
        transfer.accept();

/*        ReceiveFileThread service = null;
        try {
            service = new ReceiveFileThread(ftl, (ConnectionInfo) connectionInfo);
            ftl.setTransferService(service);
            service.start();
        } catch (Exception e) {
            log.log(Level.SEVERE, "",e);
            ftl.notifyFail();
            if (service != null)
                service.cancelTransfer();
            return;
        }

//        try {
//            session.sendRv(new GetFileReqRvCmd(RvConnectionInfo.createForOutgoingRequest(InetAddress.getLocalHost(),
//                    socket.getLocalPort())));
//        } catch (UnknownHostException e) {
//            log.log(Level.SEVERE, "",e);
//        }
*/
    } // getFile
/*
    public void acceptFileTransfer(FileTransferListener ftl) {
        RvSession session = rvProcessor.createRvSession(connection.getScreenname().getNormal());

        session.addListener(new RvSessionListener() {
            public void handleRv(RecvRvEvent event) {
                log.fine("aft: here 1");
            }

            public void handleSnacResponse(RvSnacResponseEvent event) {
                log.fine("aft: here 2");
            }
        });

        ServerSocket socket;
        try {
            socket = new ServerSocket(0);
            new GetFileThread(ftl, session, socket).start();
        } catch (IOException e) {
            log.log(Level.SEVERE, "",e);

            return;
        }

        try {
            session.sendRv(new GetFileReqRvCmd(RvConnectionInfo.createForOutgoingRequest(InetAddress.getLocalHost(),
                    socket.getLocalPort())));
        } catch (UnknownHostException e) {
            log.log(Level.SEVERE, "",e);
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
                                    log.log(Level.SEVERE, "",e1);
                                }
                            } // handleResponse
                        });
                    } // if
                } // if
            } // for
*/
        if (!isLoggedIn()) return;
        BuddyInfo binfo = connection.getBuddyInfoManager().getBuddyInfo(new Screenname(contact.getName()));
        ByteBlock byteBlock = binfo.getIconData();
        if (byteBlock != null) {
            contact.setPicture(new ImageIcon(byteBlock.toByteArray()));
            for (ConnectionEventListener eventHandler : eventHandlers) {
                eventHandler.pictureReceived(OscarConnection.this, contact);
            }
        }
    } // requestPictureForUser

    public List<String> getUserInfoColumns() {
        List<String> result = new ArrayList<String>(10);
        result.add("Screen Name");
        result.add("Screen ID");
        result.add("Away Message");
        result.add("Capabilities");
        result.add("iTunes URL");
        result.add("Last Expression");
        result.add("Idle Since");
        result.add("Online Since");
        result.add("Status Message");
        result.add("User Profile");
        result.add("Warning Level");
        result.add("Aol User");
        result.add("Away");
        result.add("Mobile");
        result.add("On Buddy List");
        result.add("Online");
        result.add("Robot");
        result.add("Typing Notifications");
        return result;
    }
    
    public List<String> getUserInfo(String userName) {
        if (!isLoggedIn()) return null;
        List<String> result = new ArrayList<String>(10);
        BuddyInfo binfo = connection.getBuddyInfoManager().getBuddyInfo(new Screenname(userName));
        result.add(binfo.getScreenname().getFormatted());
        result.add(binfo.getScreenname().getNormal());
        result.add(binfo.getAwayMessage());
        result.add(binfo.getCapabilities().toString());
        result.add(binfo.getItunesUrl());
        result.add(binfo.getLastAimExpression());
        result.add(binfo.getIdleSince().toString());
        result.add(binfo.getOnlineSince()==null?"Not Available": binfo.getOnlineSince().toString());
        result.add(binfo.getStatusMessage());
        result.add(binfo.getUserProfile());
        result.add(binfo.getWarningLevel()+"");
        result.add(""+binfo.isAolUser());
        result.add(""+binfo.isAway());
        result.add(""+binfo.isMobile());
        result.add(""+binfo.isOnBuddyList());
        result.add(""+binfo.isOnline());
        result.add(""+binfo.isRobot());
        result.add(""+binfo.supportsTypingNotifications());
        return result;
    }

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
        heartbeat.mark();
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
                log.fine("requesting " + Integer.toHexString(family)
                        + " service.");
                snacMgr.setPending(family, true);
                snacMgr.addRequest(request);
                request(new ServiceRequest(family));
            } else {
                log.fine("eep! can't find a service redirector " +
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

        public void awaitingAuthChanged(Buddy buddy, boolean oldAwaitingAuth, boolean newAwaitingAuth) {
        }
    } // class AliasBuddyListener


    public String veryfySupport(String id) {
        if (!GeneralUtils.isNotEmpty(id))
            return "Number can't be empty";
        return id.startsWith("+1")?null:"Must start with +1, like: +18005551234";
    }

    // todo To find maximums for stuff:
    // todo (01:08)klea: look for a snac ending with LimitsCmd
    // todo (01:08)klea: or something
    // todo (01:08)klea: like SnacTypeLimitsCmd

} // class OscarConnection
