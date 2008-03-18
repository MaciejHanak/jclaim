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

package com.itbs.aimcer.gui;

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.commune.*;
import com.itbs.aimcer.gui.order.OrderEntryLog;
import com.itbs.gui.*;
import com.itbs.newgrep.Grep;
import org.jdesktop.jdic.desktop.Desktop;
import org.jdesktop.jdic.desktop.Message;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Alex Rass
 * @since May 18, 2005
 */
public class MenuManager {
    private static final Logger log = Logger.getLogger(MenuManager.class.getName());
    /** Menu */
    private static final String MENU_FILE   = "General";
    private static final String COMMAND_EXIT = "Exit";
    private static final String COMMAND_SETTINGS = "Settings...";
    private static final String COMMAND_GREP = "Search...";
    private static final String COMMAND_FILE_ORDERS = "Manage Orders";

    private static final String MENU_CONNECTION         = "Connection";
    private static final String COMMAND_CONNECTION_ADD  = "Add Connection...";
            static final String COMMAND_FILE_SEND       = "Send File";
    private static final String COMMAND_BUDDY_ADD       = "Add Contact";
            static final String COMMAND_BUDDY_REMOVE    = "Remove Contact";
            static final String COMMAND_BUDDY_MOVE      = "Move Contact";
    private static final String COMMAND_PICTURE_SEND    = "Set Picture";
    private static final String COMMAND_PICTURE_RESET   = "Clear Picture";
    private static final String COMMAND_GLOBAL_AWAY     = "Away All";
    private static final String COMMAND_LOGIN           = "Login All";
    private static final String COMMAND_LOGOUT          = "Logout All";
    private static final String COMMAND_FORWARD         = "Forward To...";

    private static final String MENU_HELP               = "Help";
    private static final String COMMAND_HELP_FAQ        = "FAQ";
    private static final String COMMAND_HELP_FEATURE    = "Request a Feature";
    private static final String COMMAND_HELP_REQUEST    = "Request Help";
    private static final String COMMAND_HELP_ABOUT      = "About";

    private static JMenu connectionMenu;
    private static JCheckBoxMenuItem globalAway;
    private static JCheckBoxMenuItem globalForward;
    private static final String COMMAND_CONN_AWAY =     "Away";
    private static final String COMMAND_CONN_MANAGE =   "Manage...";
    private static final String COMMAND_CONN_LOGIN =    "Login";
    private static final String COMMAND_CONN_LOGOFF =   "Logoff";
    private static final String COMMAND_CONN_REMOVE =   "Remove";
    private static final String COMMAND_CONN_JOIN =     "Join Chat";
    private static final String COMMAND_CONN_SEND_MSG = "Send Message";

    protected static JMenuBar getMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu;
        menu = new JMenu(MENU_FILE + "    ");
        menu.setMnemonic('G');
        ActionListener eventHandler = new MenuHandler();
        menu.add(ActionAdapter.createMenuItem(COMMAND_SETTINGS, eventHandler, 's'));    //   Settings
        menu.add(ActionAdapter.createMenuItem(COMMAND_GREP, eventHandler, 'p'));    //   Settings
        if (ClientProperties.INSTANCE.isEnableOrderEntryInSystem())
            menu.add(ActionAdapter.createMenuItem(COMMAND_FILE_ORDERS, eventHandler, 'm'));   //   Manage Orders
        menu.add(new JSeparator());                                       //   -------------
        menu.add(ActionAdapter.createMenuItem(COMMAND_EXIT, eventHandler,   'x'));      //   Exit
        menuBar.add(menu);                                              // File Menu

        connectionMenu = new JMenu(MENU_CONNECTION);
        connectionMenu.setMnemonic('c');
//        menu.add(createMenuItem(COMMAND_FILE_RECEIVE, eventHandler, 'r'));   //   Allow Receive File
        connectionMenu.add(ActionAdapter.createMenuItem(COMMAND_CONNECTION_ADD, eventHandler, 'n'));   //   Add Connection
        connectionMenu.add(ActionAdapter.createMenuItem(COMMAND_FILE_SEND, eventHandler, 'e'));   //   Send File
        connectionMenu.add(ActionAdapter.createMenuItem(COMMAND_BUDDY_MOVE, eventHandler, 'm'));   //   move
        connectionMenu.add(ActionAdapter.createMenuItem(COMMAND_BUDDY_REMOVE, eventHandler, 'r'));   //   Remove
        connectionMenu.addSeparator();
        connectionMenu.add(globalAway = ActionAdapter.createCheckMenuItem(COMMAND_GLOBAL_AWAY, eventHandler, 'a', ClientProperties.INSTANCE.isIamAway()));// Global Away
        connectionMenu.add(globalForward = ActionAdapter.createCheckMenuItem(COMMAND_FORWARD, eventHandler, 'f', Main.isForwarding()));// Global Forward
        connectionMenu.add(ActionAdapter.createMenuItem(COMMAND_PICTURE_SEND, eventHandler, 'p'));   //   Set Picture
        connectionMenu.add(ActionAdapter.createMenuItem(COMMAND_PICTURE_RESET, eventHandler, 'c'));   //   Clear Picture
        connectionMenu.add(ActionAdapter.createMenuItem(COMMAND_LOGIN, eventHandler, 'i'));    //   Logout
        connectionMenu.add(ActionAdapter.createMenuItem(COMMAND_LOGOUT, eventHandler, 'o'));    //   Logout
        connectionMenu.addSeparator();
        menuBar.add(connectionMenu);                                              // File Menu


        menu = new JMenu(MENU_HELP);
        menu.setMnemonic('h');
        menu.add(ActionAdapter.createMenuItem(COMMAND_HELP_FAQ, eventHandler, 'f'));   //   Request Feature
        menu.add(ActionAdapter.createMenuItem(COMMAND_HELP_FEATURE, eventHandler, 'r'));   //   Request Feature
        menu.add(ActionAdapter.createMenuItem(COMMAND_HELP_REQUEST, eventHandler, 'e'));   //   Request Help
        menu.addSeparator();                                       //   -------------
        menu.add(ActionAdapter.createMenuItem(COMMAND_HELP_ABOUT, eventHandler, 'a'));   //   About
        menuBar.add(menu);                                                 // Help Menu
        // Add Menu
        return menuBar;
    }

    public static void addConnection(MessageSupport connection) {
        JMenu submenu = new JMenu(connection.getServiceName() + " - " + connection.getUserName());
        ActionListener connectionMenuEventHandler = new ConnectionMenuEventHandler(connection, submenu);
        final JMenuItem menuItemBuddyAdd = ActionAdapter.createMenuItem(COMMAND_BUDDY_ADD, connectionMenuEventHandler, 'c');
        final JMenuItem menuItemSendMessage = ActionAdapter.createMenuItem(COMMAND_CONN_SEND_MSG, connectionMenuEventHandler, 's');
        final JMenuItem menuItemLogin = ActionAdapter.createMenuItem(COMMAND_CONN_LOGIN, connectionMenuEventHandler);
        final JMenuItem menuItemLogOff = ActionAdapter.createMenuItem(COMMAND_CONN_LOGOFF, connectionMenuEventHandler);
        final JCheckBoxMenuItem checkMenuItemAway = ActionAdapter.createCheckMenuItem(COMMAND_CONN_AWAY, connectionMenuEventHandler, 'a', connection.isAway());
        checkMenuItemAway.setSelected(ClientProperties.INSTANCE.isIamAway()); // catch up on the menu
        final MenuEventListener onOffListener = new MenuEventListener(checkMenuItemAway);
        connection.addEventListener(onOffListener);
        submenu.add(checkMenuItemAway); onOffListener.add(checkMenuItemAway, true);
        submenu.add(menuItemBuddyAdd);  onOffListener.add(menuItemBuddyAdd, true); //   Add
        submenu.add(menuItemSendMessage);  onOffListener.add(menuItemSendMessage, true); //   Send Message
        submenu.add(menuItemLogin);     onOffListener.add(menuItemLogin, false);
        submenu.add(menuItemLogOff);    onOffListener.add(menuItemLogOff, true);
        submenu.addSeparator();
        submenu.add(ActionAdapter.createMenuItem(COMMAND_CONN_MANAGE, connectionMenuEventHandler, 'm'));
        submenu.add(ActionAdapter.createMenuItem(COMMAND_CONN_REMOVE, connectionMenuEventHandler));
        if (connection instanceof ChatRoomSupport) {
            submenu.add(ActionAdapter.createMenuItem(COMMAND_CONN_JOIN, connectionMenuEventHandler));
        }

        connectionMenu.add(submenu);
    }

    /**
     * Sets the checkbox.  does not run disco
     * @param away status
     */
    public static void setGlobalAway(boolean away) {
        if (globalAway!=null && globalAway.isSelected() != away) // should prevent infinite recall
            globalAway.setSelected(away);
        Main.getStatusBar().setAway(away);
        ClientProperties.INSTANCE.setIamAway(away);
        for (int i = 0; i < Main.getConnections().size(); i++) {
            if (Main.getConnections().get(i).isLoggedIn())
                Main.getConnections().get(i).setAway(away);
        }
    }

    private static void setForwardingContact() {
        // If we are already forwarding... clear it.
        globalForward.setSelected(Main.isForwarding()); // to stop default behavior
        if (Main.isForwarding()) {
            Main.setForwardingContact(null);
            globalForward.setSelected(false);
            globalForward.setText(COMMAND_FORWARD);
//            globalForward.invalidate();
            return;
        }
        final JDialog dialog = new JDialog(Main.getFrame(), "Fordard All Messages To:", true);
        Container pane = dialog.getContentPane();
        pane.setLayout(new GridLayout(0, 2));
        pane.add(PropertiesDialog.getLabel("Contact: ", "Which person to send the messages to?"));
        final JComboBox contact;
        ContactWrapper[] contactWrappers = ContactWrapper.toArray();
        pane.add(contact = new JComboBox(contactWrappers));
        for (ContactWrapper wrapper : contactWrappers) {
            if (wrapper.toString().equalsIgnoreCase(ClientProperties.INSTANCE.getForwardee())) {
                contact.setSelectedItem(wrapper);
            }
        }
        ActionListener react = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if ("OK".equals(e.getActionCommand())) {
                    final ContactWrapper wrapper = (ContactWrapper) contact.getSelectedItem();
                    new Thread() {
                        public void run() {

                            if (wrapper.getConnection() instanceof MessageSupport) {
                                Main.setForwardingContact(wrapper);                                
                                globalForward.setSelected(Main.isForwarding());
                                globalForward.setText("Forwarded to " + wrapper.getDisplayName());
                            } else {
                                Main.complain("This protocol for this contact does not support messages - "+wrapper.getConnection().getServiceName());
                            }
                        }
                    }.start();
                } else {
                    Main.setForwardingContact(null);
                    globalForward.setSelected(false);
                    globalForward.setText(COMMAND_FORWARD);
                }
                dialog.dispose();
            }
        };
        pane.add(new BetterButton(new ActionAdapter("OK", react, 'O')));
        pane.add(new BetterButton(new ActionAdapter("Cancel", react, 'C')));
        dialog.pack();
        GUIUtils.addCancelByEscape(dialog);
        GUIUtils.moveToScreenCenter(dialog);
        dialog.setVisible(true);
    }

    /**
     * Monitors connection so it can turn menu items on and off.
     */
    static class MenuEventListener implements ConnectionEventListener {
        List <JComponent> enabledWhenOn = new ArrayList<JComponent>();
        List <Boolean> enabledFlag = new ArrayList<Boolean>();
        JCheckBoxMenuItem away;

        public MenuEventListener(JCheckBoxMenuItem away) {
            this.away = away;
        }

        void add(JComponent item, boolean on) {
            enabledWhenOn.add(item);
            enabledFlag.add(on);
            item.setEnabled(!on);
        }
        /**
         * Connection with server was interrupted.
         *
         * @param connection itself
         */
        public void connectionLost(Connection connection) {
            for (int i = 0; i < enabledWhenOn.size(); i++) {
                enabledWhenOn.get(i).setEnabled(!enabledFlag.get(i));
            }
        }

        public void connectionInitiated(Connection connection) {
            for (JComponent aEnabledWhenOn : enabledWhenOn) {
                aEnabledWhenOn.setEnabled(false);
            }
        }

        /**
         * Connection with server has failed.
         *
         * @param connection itself
         * @param message to relay
         */
        public void connectionFailed(Connection connection, String message) {
            for (int i = 0; i < enabledWhenOn.size(); i++) {
                enabledWhenOn.get(i).setEnabled(!enabledFlag.get(i));
            }
        }

        /**
         * Indicates we are now logged in and can start using the connection.
         * Place to run connection-related properties etc.
         *
         * @param connection that has finished stabilizing
         */
        public void connectionEstablished(Connection connection) {
            for (int i = 0; i < enabledWhenOn.size(); i++) {
                JComponent comp = enabledWhenOn.get(i);
                comp.setEnabled(enabledFlag.get(i));
                if (comp instanceof JCheckBox)
                    ((JCheckBox) comp).setSelected(false);
            }
        }

        public boolean messageReceived(MessageSupport connection, com.itbs.aimcer.bean.Message message) throws Exception {
            return false;
        }

        public boolean emailReceived(MessageSupport connection, com.itbs.aimcer.bean.Message message) throws Exception {
            return false;
        }

        public void typingNotificationReceived(MessageSupport connection, Nameable contact) { }
        
        public void statusChanged(Connection connection, Contact contact, Status status) {  }

        public void statusChanged(Connection connection) {
            away.setSelected(connection.isAway());
        }
        public boolean contactRequestReceived(final String user, final MessageSupport connection) {  return true; }

        public void pictureReceived(IconSupport connection, Contact contact) {
        }

        public void fileReceiveRequested(FileTransferSupport connection, Contact contact, String filename, String description, Object connectionInfo) {
        }

        public void errorOccured(String message, Exception exception) {
        }
    }
    /**
     * Provides actions for each connection submenu.
     * Looked appropriate to separate this out.
     */
    static class ConnectionMenuEventHandler implements ActionListener {
        MessageSupport connRef;
        JMenu menuRef;
        ConnectionMenuEventHandler(MessageSupport connection, JMenu submenu) {
            connRef = connection;
            menuRef = submenu;
        }

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            try {
                if (COMMAND_CONN_AWAY.equals(e.getActionCommand())) {
                    connRef.setAway(((JCheckBoxMenuItem) e.getSource()).getState());
                } else if (COMMAND_CONN_MANAGE.equals(e.getActionCommand())) {
                    new LoginPanel(connRef).setVisible(true);
                } else if (COMMAND_CONN_LOGIN.equals(e.getActionCommand())) {
                    if (connRef.isLoggedIn())
                        JOptionPane.showMessageDialog(Main.getFrame(), "Already connected.");
                    else {
                        connRef.resetDisconnectInfo();
                        connRef.connect();
                    }
                } else if (COMMAND_CONN_LOGOFF.equals(e.getActionCommand())) {
                    connRef.disconnect(true);
                } else if (COMMAND_BUDDY_ADD.equals(e.getActionCommand())) {
                    if (pleaseLogIn())
                        return;
                    addContact(connRef, Main.getFrame(), "");
                    updateContactList(connRef);
                } else if (COMMAND_CONN_JOIN.equals(e.getActionCommand())) {
                    String result = JOptionPane.showInputDialog(Main.getFrame(), "Please enter the name of the group", "Group Name", JOptionPane.QUESTION_MESSAGE);
                    if (result!=null) {
                        new MessageCollaborationWindow((ChatRoomSupport) connRef, result);
                    }
                } else if (COMMAND_CONN_SEND_MSG.equals(e.getActionCommand())) {
                    sendMessageDialog(connRef);
                } else if (COMMAND_CONN_REMOVE.equals(e.getActionCommand())) {
                    int result = JOptionPane.showConfirmDialog(Main.getFrame(), "Delete " + connRef.getServiceName() + " for " + connRef.getUserName(), "Delete connection?", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        connRef.disconnect(true);
                        Main.getConnections().remove(connRef);
                        connectionMenu.remove(menuRef); // remove from self
                    }
                }
            } catch (Exception ex) {
                Main.complain("Failed to execute command " + e.getActionCommand(), ex);
            }
        }
    }

    /**
     * Provides menu command support for all menues in main GUI.
     */
    static class MenuHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            new ActionProceed(e).start();
        }
    }
    
    static class ActionProceed extends Thread {
        /** Command reference */
        String command;
        ActionProceed(ActionEvent e) {
            command = e.getActionCommand();
        }
        ActionProceed(String e) {
            this.command = e;
        }
        public void run() {
            if (COMMAND_LOGIN.equals(command)
            || COMMAND_LOGOUT.equals(command)) {
                if (Main.getConnections() == null)
                    return;
                for (int i = 0; i < Main.getConnections().size(); i++) {
                    if (Main.getConnections() != null && Main.getConnections().get(i) != null) {
                        if (Main.getConnections().get(i).isLoggedIn()) {
                            if (COMMAND_LOGOUT.equals(command))
                                Main.getConnections().get(i).disconnect(true);
                        } else {
                            if (COMMAND_LOGIN.equals(command))
                                try {
                                    Main.getConnections().get(i).resetDisconnectInfo();
                                    Main.getConnections().get(i).connect();
                                } catch (Exception e1) {
                                    Main.complain("Failed to connect " + Main.getConnections().get(i) + " for user " + Main.getConnections().get(i).getUser(), e1);
                                }
                        }
                    } // if real connection
                }
            } else if (COMMAND_EXIT.equals(command)) {
                Main.exit();
            } else if (COMMAND_GREP.equals(command)) {
                new Grep();
            } else if (COMMAND_SETTINGS.equals(command)) {
                final JDialog properties = new PropertiesDialog(Main.getFrame());
                properties.setVisible(true);
            } else if (COMMAND_GLOBAL_AWAY.equals(command)) {
                setGlobalAway(globalAway.isSelected());
            } else if (COMMAND_FORWARD.equals(command)) {
                setForwardingContact();
            } else if (COMMAND_CONNECTION_ADD.equals(command)) {
                new LoginPanel(null).setVisible(true);
            } else if (COMMAND_FILE_SEND.equals(command)) {
                if (pleaseLogIn())
                    return;
                sendFileDialog(null, null);
            } else if (COMMAND_PICTURE_RESET.equals(command)) {
                if (pleaseLogIn())
                    return;
                for (int i = 0; i < Main.getConnections().size(); i++) {
                    Connection connection = Main.getConnections().get(i);
                    if (connection.isLoggedIn() && connection instanceof IconSupport)
                        ((IconSupport) connection).clearPicture();
                }
            } else if (COMMAND_PICTURE_SEND.equals(command)) {
                if (pleaseLogIn())
                    return;
                final JFileChooser chooser = new JFileChooser(ClientProperties.INSTANCE.getLastFolder());
                int returnVal = chooser.showOpenDialog(Main.getFrame());
                ClientProperties.INSTANCE.setLastFolder(chooser.getCurrentDirectory().getAbsolutePath());
                if(returnVal != JFileChooser.APPROVE_OPTION)
                    return;
                if (!chooser.getSelectedFile().exists() || chooser.getSelectedFile().isDirectory()) {
                    JOptionPane.showMessageDialog(Main.getFrame(), "To assign a picture, you need to select a valid picture file.");
                    return;
                }
                if (chooser.getSelectedFile().length() > 7*1024) {
                    JOptionPane.showMessageDialog(Main.getFrame(), "The picture you selected is too large (7k is max)");
                    return;
                }
                // get picture filename
                for (int i = 0; i < Main.getConnections().size(); i++) {
                    Connection connection = Main.getConnections().get(i);
                    if (connection.isLoggedIn() && connection instanceof IconSupport)
                        ((IconSupport) connection).uploadPicture(chooser.getSelectedFile());
                }
                // send to each connection
            } else if (COMMAND_FILE_ORDERS.equals(command)) {
                OrderEntryLog.getInstance().showFrame();
            } else if (COMMAND_BUDDY_MOVE.equals(command)) {
                if (pleaseLogIn()) return;
                JList list = Main.getPeoplePanel().getList();
                Object []value = list.getSelectedValues();
                if (value == null || value.length == 0)
                    return;
                // learn group
                Returned result = genericPrompter(Main.getConnections().get(0), Main.getFrame(), null, "Move contacts?");
                if (result == null) return;
                Group group = result.group;
                for (Object o : value) {
                    if (o instanceof ContactWrapper) {
                        ((ContactWrapper)o).getConnection().moveContact((ContactWrapper)o, group);
                    }
                }
                Main.getPeoplePanel().getList().repaint();
            } else if (COMMAND_BUDDY_REMOVE.equals(command)) {
                JList list = Main.getPeoplePanel().getList();
                Object value = list.getSelectedValue();
                if (value != null) {
                    if (value instanceof ContactWrapper) {
                        if (!((ContactWrapper)value).getConnection().isLoggedIn()) {
                            JOptionPane.showMessageDialog(Main.getFrame(), "Need to be online via " + ((ContactWrapper)value).getConnection().getServiceName() + " to delete contacts.", "Can not proceed:", JOptionPane.ERROR_MESSAGE);
                        } else {
                            int result = JOptionPane.showConfirmDialog(Main.getFrame(), "Delete " + ((ContactWrapper)value).getDisplayName() + " (" + ((ContactWrapper)value).getName()+ ")?", "Delete a contact?", JOptionPane.YES_NO_OPTION);
                            if (result == JOptionPane.YES_OPTION) {
                                ((ContactWrapper)value).getConnection().removeContact((Contact) value);
                                updateContactList(((ContactWrapper)value).getConnection());
                            }
                        }
                    } else if (value instanceof GroupWrapper) {
                        int result = JOptionPane.showConfirmDialog(Main.getFrame(), "Delete entire group " + value + "?", "Delete a group?", JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.YES_OPTION) {
                            for (Connection conn:Main.getConnections()) {
                                conn.removeContactGroup((Group) value);
                                updateContactList(conn);
                                conn.getGroupList().remove((Group) value);
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(Main.getFrame(), "Can only delete normal contacts.", "Can not proceed:", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else if (COMMAND_HELP_FAQ.equals(command)) {
                try {
                    Desktop.browse(new URL(Main.URL_FAQ));
                } catch (Exception exc) {
                    ErrorDialog.displayError(Main.getFrame(), "Failed to launch url" + Main.URL_FAQ + "\n", exc);
                } catch (UnsatisfiedLinkError exc) {
                    ErrorDialog.displayError(Main.getFrame(), "Failed to locate native libraries.  Please open "+Main.URL_FAQ + " yourself.", exc);
                }

            } else if (COMMAND_HELP_FEATURE.equals(command)) {
                try {
                    final Message msg = new Message();
                    java.util.List <String> list  = new ArrayList<String> ();
                    list.add(Main.EMAIL_SUPPORT);
                    msg.setToAddrs(list);
                    msg.setSubject("Feature Request");
                    Desktop.mail(msg);
                } catch (Throwable e1) {
                    JOptionPane.showMessageDialog(Main.getFrame(), Main.ABOUT_MESSAGE, "About:", JOptionPane.INFORMATION_MESSAGE);
                }
            } else if (COMMAND_HELP_REQUEST.equals(command)) {
                try {
                    final Message msg = new Message();
                    java.util.List <String> list  = new ArrayList<String>();
                    list.add(Main.EMAIL_SUPPORT);
                    msg.setToAddrs(list);
                    msg.setSubject("JClaim support");
                    msg.setBody("To speed up processing, please include your username (do not include password) and medium (AOL, Yahoo etc).\n" + Main.DEBUG_INFO);
                    Desktop.mail(msg);
                } catch (Throwable e1) {
                    JOptionPane.showMessageDialog(Main.getFrame(), Main.ABOUT_MESSAGE, "About:", JOptionPane.INFORMATION_MESSAGE);
                }
            } else if (COMMAND_HELP_ABOUT.equals(command)) {
                log.info("Mem free:" + NumberFormat.getInstance().format(Runtime.getRuntime().freeMemory()) + " total:" + NumberFormat.getInstance().format(Runtime.getRuntime().totalMemory()));
                JOptionPane.showMessageDialog(Main.getFrame(), Main.ABOUT_MESSAGE, "About:", JOptionPane.INFORMATION_MESSAGE);
            }
        }

    }

    public static void sendMessageDialog(final Connection conn) {
        final JDialog dialog = new JDialog(Main.getFrame(), "Send Message", true);
        Container pane = dialog.getContentPane();
        pane.setLayout(new GridLayout(0, 2));
        pane.add(PropertiesDialog.getLabel("Contact: ", "Which person to send the file to?"));
        final JComboBox contact;
        ContactWrapper[] contactWrappers = ContactWrapper.toArray();
        List <ContactWrapper> belongingOnes = new ArrayList<ContactWrapper>();
        for (ContactWrapper contactWrapper : contactWrappers) {
            if (conn.equals(contactWrapper.getConnection())) {
                belongingOnes.add(contactWrapper);
            }
        }
        pane.add(contact = new JComboBox(belongingOnes.toArray()));
        contact.setEditable(true);
        ActionListener react = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if ("OK".equals(e.getActionCommand())) {
                    Object result = contact.getSelectedItem();
                    if (result.toString().trim().length()==0) return;
                    final ContactWrapper contact = ContactWrapper.create(result.toString(), conn);
                    Main.globalWindowHandler.openWindow(contact, true);
                }
                dialog.dispose();
            }
        };
        pane.add(new BetterButton(new ActionAdapter("OK", react, 'O')));
        pane.add(new BetterButton(new ActionAdapter("Cancel", react, 'C')));
        dialog.pack();
        GUIUtils.addCancelByEscape(dialog);
        GUIUtils.moveToScreenCenter(dialog);
        dialog.setVisible(true);
//        dialog.toFront();
    }
    public static void sendFileDialog(ContactWrapper contactWrapper, List <File> fileList) {
        final JDialog dialog = new JDialog(Main.getFrame(), "File to Send", true);
        Container pane = dialog.getContentPane();
        pane.setLayout(new GridLayout(0, 2));
        pane.add(PropertiesDialog.getLabel("Contact: ", "Which person to send the file to?"));
        final JComboBox contact;
        pane.add(contact = new JComboBox(ContactWrapper.toArray()));
        contact.setSelectedItem(contactWrapper);
        pane.add(PropertiesDialog.getLabel("File: ", "Which file to send?"));
        final FileChooserButton chooser;
        pane.add(chooser = new FileChooserButton(Main.getFrame()));
        if (fileList!=null && fileList.size()>0) {
            chooser.setFileName(fileList.get(0));
        }
        pane.add(PropertiesDialog.getLabel("Describe: ", "Describe the file (optional)"));
        final JTextField description = new BetterTextField();
        pane.add(description);
        ActionListener react = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if ("OK".equals(e.getActionCommand())) {
                    if (chooser.getFile() == null) {
                        JOptionPane.showMessageDialog(Main.getFrame(), "Please select a file.");
                        return;
                    }
                    final ContactWrapper wrapper = (ContactWrapper) contact.getSelectedItem();
                    new Thread() {
                        public void run() {

                            if (wrapper.getConnection() instanceof FileTransferSupport)
                                try {
                                    ((FileTransferSupport)wrapper.getConnection()).initiateFileTransfer(
                                        new FileTransferAdapter(Main.getFrame(), description.getText(), chooser.getFile(), (Contact) contact.getSelectedItem()));
                                } catch (IOException ex) {
                                    Main.complain("Error trying to transfer a file", ex);
                                }
                            else {
                                Main.complain("This protocol for this contact does not support file transfers");
                            }
                        }
                    }.start();
                }
                dialog.dispose();
            }
        };
        pane.add(new BetterButton(new ActionAdapter("OK", react, 'O')));
        pane.add(new BetterButton(new ActionAdapter("Cancel", react, 'C')));
        dialog.pack();
        GUIUtils.addCancelByEscape(dialog);
        GUIUtils.moveToScreenCenter(dialog);
        dialog.setVisible(true);
//        dialog.toFront();
    }

    private static boolean pleaseLogIn() {
        if (Main.getConnections() == null) {
            JOptionPane.showMessageDialog(Main.getFrame(), "Please log in.");
            return true;
        }
        return false;
    }

    private static void updateContactList(Connection connection) {
        Iterator iter = connection.getEventListenerIterator();
        while (iter.hasNext()) {
            ConnectionEventListener listener =  (ConnectionEventListener) iter.next();
            listener.statusChanged(connection);
        }
    }

    /**
     * Used to return 3-touple value back to UI.
     */
    static class Returned {
        Group group;
        String contact;
        String comment;

        public Returned(Group selectedGroup, String selectedContact, String selectedComment) {
            group = selectedGroup;
            contact = selectedContact;
            this.comment = selectedComment;
        }
    }
    /**
     * Prompts and then adds the contact.
     * @param conn Connection
     * @param parent Top frame
     * @param defaultName name of contact
     */
    public static void addContact(Connection conn, final Frame parent, String defaultName) {
        Returned result = genericPrompter(conn, parent, defaultName, "Add contact?");
        if (result!=null) {
            try {
                conn.getGroupList().add(result.group);
                ContactWrapper contactWrapper = ContactWrapper.create(result.contact, conn);
                conn.addContact(contactWrapper, result.group);
                if (result.comment.length()>0) { // if contact already existed, don't want to loose comment.
                    contactWrapper.getPreferences().setNotes(result.comment);
                }
            } catch (Exception e) {
                ErrorDialog.displayError(parent, "Failed to add contact. \n" + e.getMessage(), e);
            }

        }
    }

    static Returned genericPrompter(Connection conn, final Frame parent, String defaultContactName, String title) {
        final boolean showContact = defaultContactName != null;
        final JTextComponent contactName, comment;
        final JComboBox groupBox;
        final JDialog dialog = new JDialog(parent, title, true);
        Container pane = dialog.getContentPane();
        pane.setLayout(new GridLayout(0, 2));
/*
        if (false && conn == null) {  // todo finish adding any connection
            pane.add(PropertiesDialog.getLabel("Connection: ", "Which connection to use"));
            pane.add(connectionBox = new JComboBox(Main.getConnections().toArray()));
        }
*/
        pane.add(PropertiesDialog.getLabel("Group: ", "Which group to add the contact to"));
        groupBox = new JComboBox(conn.getGroupList().toArray());
        groupBox.setEditable(true);
        pane.add(groupBox);

        contactName = new BetterTextField();
        comment = new BetterTextPane();
        if (showContact) {
            pane.add(PropertiesDialog.getLabel("Name: ", "Which contact to add"));
            pane.add(contactName);
            contactName.setText(defaultContactName);
            pane.add(PropertiesDialog.getLabel("Comment: ", "Optional comment"));
            pane.add(comment);
        }
        ActionListener react = new ActionListener() {
            boolean ok = false;
            public void actionPerformed(ActionEvent e) {
                if ("OK".equals(e.getActionCommand())) {
                    if (groupBox.getSelectedItem().toString().trim().length() == 0) {
                        JOptionPane.showMessageDialog(parent, "No group");
                        return;
                    }
                    if (showContact && contactName.getText().trim().length() == 0) {
                        JOptionPane.showMessageDialog(parent, "No name");
                        return;
                    }
                    ok = true;
                }
                dialog.dispose();
            }

            public int hashCode() {
                return ok?1:0;
            }
        };
        pane.add(new JButton(new ActionAdapter("OK", react, 'O')));
        pane.add(new JButton(new ActionAdapter("Cancel", react, 'C')));
        dialog.pack();
        GUIUtils.addCancelByEscape(dialog);
        GUIUtils.moveToScreenCenter(dialog);
        dialog.setVisible(true);

        if (react.hashCode() > 0) {
                Group selectedGroup;
                if (groupBox.getSelectedItem() instanceof Group)
                    selectedGroup = (Group) groupBox.getSelectedItem();
                else
                    selectedGroup = conn.getGroupFactory().create((String)groupBox.getSelectedItem());
                return new Returned(selectedGroup, contactName.getText().trim(), comment.getText().trim());
        }
        return null;
    }

} // class MenuManager
