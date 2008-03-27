package com.itbs.aimcer.tabbed;

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.commune.*;
import com.itbs.aimcer.commune.SMS.InvalidDataException;
import com.itbs.aimcer.gui.*;
import com.itbs.gui.*;
import com.itbs.util.DelayedThread;
import com.itbs.util.GeneralUtils;
import org.jdesktop.jdic.desktop.DesktopException;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Manages the actual window with many tabs each of which is a messaging client.
 *
 * @author Alex Rass
 * @since Feb 28, 2008 1:21:41 AM
 */
public class TabbedWindow {
    private static final Logger log = Logger.getLogger(TabbedWindow.class.getName());

    /** Container window */
    protected JFrame frame;
    protected BetterTabbedPane tabbedPane;
    protected TabItself currentTab;
    protected ConnectionEventListener connectionEventListener;

    /** Size of the message box. */
    public static final Rectangle DEFAULT_SIZE = new Rectangle(420, 200, 800, 600);
    public static TabbedWindow INSTANCE = new TabbedWindow();

    /** Used to execute stuff off UI thread */
    static final Executor offUIExecutor = Executors.newFixedThreadPool(2);


    /** Typing icon display with delay. */
    private DelayedThread delayedShow;
    private JLabel charCount = new JLabel();
    DocumentListener documentListenerForCounter = new DocumentListener() {
        public void count(DocumentEvent e) { charCount.setText("" + e.getDocument().getLength()); }
        public void insertUpdate(DocumentEvent e) { count(e); }
        public void removeUpdate(DocumentEvent e) { count(e); }
        public void changedUpdate(DocumentEvent e) { count(e); }
    };
    DocumentListener documentListenerJazzy;
    private final AbstractAction ACTION_SEND, ACTION_SEND_ALL, ACTION_ADD, ACTION_LOG, ACTION_PAGE, ACTION_EMAIL, ACTION_SORT;

    public static TabbedWindow getINSTANCE() {
        return INSTANCE;
    }

    public TabbedWindow() {
        frame = new JFrame("JClaim Chats");
        frame.setBounds(DEFAULT_SIZE);
        frame.setLayout(new BorderLayout());
        frame.addWindowListener(new WindowAdapter() {
            /**
             * Invoked when a window is in the process of being closed.
             * The close operation can be overridden at this point.
             */
            public void windowClosing(WindowEvent e) {
                tabbedPane.removeAll();
                offUIExecutor.execute(new Runnable() { public void run () { Main.saveProperties(); } });
                System.gc();
                frame.setVisible(false);
            }

            /**
             * Do window init stuff here
             * @param e event
             */
            public void windowOpened(WindowEvent e) {
                delayedShow.start();
            }
        });

        ACTION_SEND = new ActionAdapter("Send", "Send message (" + (ClientProperties.INSTANCE.isEnterSends()?"":"Ctrl-") + "Enter )",
                        new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                final TabItself tab = getCurrentTab();
                                if (tab == null) {
                                    Main.complain("Please select a tab first.");
                                    return;
                                }
                                if (tab.textPane.getText().trim().length() == 0)
                                    return;
                                try {
                                    Message message = new MessageImpl(tab.getContact(), true, tab.textPane.getText());
                                    addTextToHistoryPanel(tab.getContact(), message, true);
                                    ((MessageSupport)tab.getContact().getConnection()).sendMessage(message);
                                    tab.textPane.setText(""); // wipe it
                                } catch (Exception e1) {
                                    log.log(Level.SEVERE, "Failed to send message", e1);
                                    ErrorDialog.displayError(frame, "Failed to send message", e1);
                                }
                                tab.textPane.requestFocusInWindow();
                            }
                        }, 'S');
        ACTION_SEND_ALL = new ActionAdapter("Send All", "Send message to all open contacts.",
                        new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                // use input box on this tab
                                final TabItself tab = getCurrentTab();
                                if (tab == null) {
                                    Main.complain("Please select a tab first.");
                                    return;
                                }
                                if (tab.textPane.getText().trim().length() == 0)
                                    return;

                                Component[] components = tabbedPane.getComponents();
                                if (components.length == 0) return;
                                int result = JOptionPane.showConfirmDialog(frame, "This will send an IM to all " + components.length + " contacts.\nContinue?", "Confirm", JOptionPane.YES_NO_OPTION);
                                if (result == JOptionPane.YES_OPTION) {
                                    MessageImpl message = new MessageImpl(tab.getContact(), true, tab.textPane.getText());

                                    for (Component eachComponent : components) {
                                        if (eachComponent instanceof TabItself) {
                                            TabItself eachTab = (TabItself) eachComponent;
                                            message.setName(eachTab.getContact());
                                            try {
                                                addTextToHistoryPanel(eachTab.getContact(), message, true);
                                                ((MessageSupport) eachTab.getContact().getConnection()).sendMessage(message);
                                            } catch (Exception e1) {
                                                log.log(Level.SEVERE, "Failed to send message", e1);
                                                ErrorDialog.displayError(frame, "Failed to send message", e1);
                                            }
                                        }
                                    }

                                    tab.textPane.setText(""); // wipe it
                                    tab.textPane.requestFocusInWindow();
                                } // if yes
                            }
                        });

        ACTION_ADD = new ActionAdapter("Add", "Add to the list", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final TabItself tab = getCurrentTab();
                if (tab == null) {
                    Main.complain("Please select a tab first.");
                    return;
                }
                try {
                    MenuManager.addContact(tab.getContact().getConnection(), frame, tab.getContact().getName());
                } catch (Exception e1) {
                    ErrorDialog.displayError(frame, "Failure while adding a contact.\n"+e1.getMessage(), e1);
                }
            }
        }, 'A');

        ACTION_EMAIL = new ActionAdapter(ImageCacheUI.ICON_EMAIL.getIcon(), "Send email", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final TabItself tab = getCurrentTab();
                if (tab == null) {
                    Main.complain("Please select a tab first.");
                } else if (tab.getContact().getPreferences().getEmailAddress() != null && tab.getContact().getPreferences().getEmailAddress().trim().length() > 0) {
                    new Thread() {
                        public void run() {
                            try {
                                final org.jdesktop.jdic.desktop.Message msg = new org.jdesktop.jdic.desktop.Message();
                                java.util.List<String> list  = new ArrayList<String>();
                                list.add(tab.getContact().getPreferences().getEmailAddress());
                                msg.setToAddrs(list);
                                msg.setBody(tab.textPane.getText());
                                org.jdesktop.jdic.desktop.Desktop.mail(msg);
                            } catch (Throwable ex) {
                                Main.complain("Failed to create an email", ex);
                            }
                        } // run
                    }.start();
                } else { // if
                    Main.complain("Please populate the email address on the info panel.");
                }
            }
        });

        ACTION_PAGE = new ActionAdapter(ImageCacheUI.ICON_WIRELESS.getIcon(), "Send message to the cell phone", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                final TabItself tab = getCurrentTab();
                if (tab == null) {
                    Main.complain("Please select a tab first.");
                    return;
                }
                if (tab.getContact().getPreferences().getPhone() == null || tab.getContact().getPreferences().getPhone().trim().length() == 0) {
                    Main.complain("Please populate the entry for cell phone on the info panel.");
                    return;
                }
                int result = JOptionPane.showConfirmDialog(frame, "This will open a new IM window.\nContinue?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    try {
                        MessageWindow.sendSMSMessage(Main.getConnections(),  tab.getContact().getConnection(), tab.getContact());
                    } catch (InvalidDataException ex) {
                        Main.complain(ex.getMessage());
                    }
                } // if Yes
            }
        });
        ACTION_LOG = new ActionAdapter(ImageCacheUI.ICON_HISTORY.getIcon(), "Show the log file", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final TabItself tab = getCurrentTab();
                if (tab == null) {
                    Main.complain("Please select a tab first.");
                    return;
                }
                File file = Main.getLogger().getLog((MessageSupport) tab.getContact().getConnection(), tab.getContact().getName());
                if (!file.exists() || file.isDirectory())
                    JOptionPane.showMessageDialog(frame, "Failed to locate the log file", "Error", JOptionPane.ERROR_MESSAGE);
                else {
                    // start default editor
                    try {
                        org.jdesktop.jdic.desktop.Desktop.open(file);
                    } catch (DesktopException exc) {
                        ErrorDialog.displayError(frame, "Failed to launch " + file.getAbsolutePath() + "\n", exc);
                    } catch (UnsatisfiedLinkError exc) {
                        ErrorDialog.displayError(frame, "Failed to locate native libraries.", exc);
                    }
/*
                    try {
                        if (File.pathSeparatorChar == '/')
                            Runtime.getRuntime().exec("xedit " + file.getAbsolutePath());
                        else
                            Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL \"file:///" + file.getAbsolutePath() + "\"");
                    } catch (IOException exc) {
                        GeneralUtils.displayError(frame, "Failed to launch " + file.getAbsolutePath() + "\n", exc);
                    }
*/
                }
            }
        });
        ACTION_SORT = new ActionAdapter("Sort", "Sort the tabs by name", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sortTabs();
            }
        });

        tabbedPane = new BetterTabbedPane();
        frame.getContentPane().add(tabbedPane);
        frame.getContentPane().add(getButtons(), BorderLayout.SOUTH);
        tabbedPane.setupKeys(frame);
//        frame.setVisible(true);
        connectionEventListener = new TabbedConnectionEventListener();
        tabbedPane.addChangeListener(new ChangeListener() { // tells me when tabs are changed.
            public void stateChanged(ChangeEvent e) {
                if (currentTab == tabbedPane.getSelectedComponent()) {
                    return; // optimization. Nothing really changed.
                }
                unSetupTab(currentTab);
                if (tabbedPane.getSelectedIndex()>-1) {
                    setupTab();
                }
                currentTab = (TabItself) tabbedPane.getSelectedComponent();
            }
        });
        tabbedPane.addVetoableChangeListener(new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                evt=null;
            }
        });
        tabbedPane.addContainerListener(new ContainerAdapter() {
            /**
             * Invoked when a component has been added to the container.
             */
            public void componentAdded(ContainerEvent e) {
                // do stuff here that needs to be done to tab components even if it's invisible
//                setupTab();
                ACTION_SEND.setEnabled(true);
                ACTION_SEND_ALL.setEnabled(true);
            }

            /**
             * Invoked when a component has been removed from the container.
             */
            public void componentRemoved(ContainerEvent e) {
                if (e.getChild() instanceof TabItself) {
                    unSetupTab((TabItself) e.getChild());
                    if (currentTab == e.getChild()) { // same reference?
                        currentTab = null; // tab no more.
                    }
                    ACTION_SEND.setEnabled(tabbedPane.getComponentCount()>0);
                    ACTION_SEND_ALL.setEnabled(tabbedPane.getComponentCount()>0);
                    ACTION_ADD.setEnabled(tabbedPane.getComponentCount()>0);
                    ACTION_LOG.setEnabled(tabbedPane.getComponentCount()>0);
                    ACTION_PAGE.setEnabled(tabbedPane.getComponentCount()>0);
                    ACTION_EMAIL.setEnabled(tabbedPane.getComponentCount()>0);
                }
            }
        });
    }

    /**
     * Remove all attached events and button listeners since they no longer apply
     * @param tab which tab to "clean". nulls will simply be skipped. 
     */
    protected void unSetupTab(TabItself tab) {
        if (tab != null) {  // old tab
            log.info("unSetupTab for " + tab.getContact());
            charCount.setText("");
            try {
                JazzyInterface.create().removeSpellCheckComponent(tab.textPane, documentListenerJazzy);
            } catch (IOException e) {
                // no care
            }
            tab.textPane.getDocument().removeDocumentListener(documentListenerForCounter);
            tab.textPane.setAction(null);
        } // if
    }

    /**
     * Call when new tab is in focus.
     * Connects all the buttons to it etc.
     */
    protected void setupTab() {
        final TabItself tab = getCurrentTab();
        if (tab == null) {
            Main.complain("Please select a tab first.");
            return;
        }
        log.info("setupTab for " + tab.getContact());
        if (ClientProperties.INSTANCE.isSpellCheck()) {
            try {
                documentListenerJazzy = JazzyInterface.create().addSpellCheckComponent(tab.textPane);
            } catch (IOException e) {
                // don't really care
            }
        }

        boolean found = true;
        if (!tab.getContact().getStatus().isOnline()) {
            found = false;
            GroupList gl = tab.getContact().getConnection().getGroupList();
            for (int i = 0; i < gl.size(); i++) {
                Group group =  gl.get(i);
                for (int g = 0; g<group.size(); g++) {
                    Nameable cw = group.get(g);
                    if (tab.getContact().equals(cw))
                        found = true;
                }
            }
        }
        ACTION_ADD.setEnabled(!found);

        charCount.setVisible(ClientProperties.INSTANCE.isShowCharCounter());
        charCount.setText("" + tab.textPane.getDocument().getLength());
        tab.textPane.getDocument().addDocumentListener(documentListenerForCounter);
        tab.textPane.setAction(ACTION_SEND);
        delayedShow.getRunThisLast().run(); // clears typing flag
        // All GUI runtime stuff here:
        GUIUtils.runOnAWT(new Runnable() {
            public void run() {
                tab.textPane.requestFocusInWindow();
            }
        });
        tab.setTabHighlighted(false);
    }

    protected Component getButtons() {
        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 3));
//        addDNDSupport(panel);
        panel.setOpaque(false);
//        ((OscarConnection)connection).setIconForUser(contactWrapper);
        final JLabel typing = new JLabel(ImageCacheUI.ICON_TYPE.getIcon());
        typing.setVisible(false);
        delayedShow = new DelayedActionThread("Typing Status Update", 2000, frame,
                new Runnable() {
                    public void run() {
                        GUIUtils.runOnAWT(new Runnable() {
                            public void run() {
                                typing.setVisible(true);
                            }
                        });
                    }
                },
                new Runnable() {
                    public void run() {
                        GUIUtils.runOnAWT(new Runnable() {
                            public void run() {
                                typing.setVisible(false);
                            }
                        });
                    }
                }
        );
        panel.add(typing);


        BetterButton btnSort = new BetterButton(ACTION_SORT);
        btnSort.setFont(btnSort.getFont().deriveFont(btnSort.getFont().getSize()-1));
        
        panel.add(btnSort);
        panel.add(new BetterButton(ACTION_EMAIL));
        panel.add(new BetterButton(ACTION_PAGE));
        panel.add(new BetterButton(ACTION_LOG));
/*
        if (ClientProperties.INSTANCE.isEnableOrderEntryInSystem())
            if (ClientProperties.INSTANCE.isShowOrderEntry())
                panel.add(new BetterButton(ACTION_ORDER));
*/

        panel.add(new BetterButton(ACTION_ADD));

        // see if we already added this one some place and if so, we don't need the add button

/*
        if (getConnection() != null && getConnection().isSecureMessageSupported())
            panel.add(secureIM = new JCheckBox("Secure"));
*/
        panel.add(charCount);
        charCount.setToolTipText("Character Count");

        BetterButton sendAll = new BetterButton(ACTION_SEND_ALL);
//        sendAll.setBackground(sendAll.getBackground().darker());
        sendAll.setFont(sendAll.getFont().deriveFont(sendAll.getFont().getSize()-1));
        panel.add(sendAll);
        panel.add(new BetterButton(ACTION_SEND));
        south.add(panel);
/*
        if (ClientProperties.INSTANCE.isEnableOrderEntryInSystem()) {
            if (orderEntry == null && ClientProperties.INSTANCE.isShowOrderEntry()) {
                if (contactWrapper.getName().endsWith(IOIEntryPanel.NAME_ENDS_WITH))
                    orderEntry = new IOIEntryPanel(getConnection(), contactWrapper, this);
                else
                    orderEntry = new OrderEntryPanel(getConnection(), contactWrapper, this);
                orderEntry.setVisible(contactWrapper.getPreferences().isOrderPanelVisible());  // activated by order button.
            }
            if (orderEntry != null && ClientProperties.INSTANCE.isShowOrderEntry()) {
                south.add(orderEntry);
            }
        }
*/

        return south;
    }

    /**
     * Returns current tab properly cast.
     *
     * @return tab or null
     */
    private TabItself getCurrentTab() {
        Component tab = tabbedPane.getSelectedComponent();
        if (tab == null || !(tab instanceof TabItself)) {
            return null;
        }
        return (TabItself) tab;
    }

    public boolean isTabOpenedFor(Contact buddyWrapper) {
        return findTab(buddyWrapper)!=null;
    }

    public void addTextToHistoryPanel(Contact buddyWrapper, Message message, boolean toBuddy) {
        TabItself tab = addTab(buddyWrapper, false);
        tab.appendHistoryText(message, toBuddy);
    }

    public void closeTab(Contact buddyWrapper) {
        tabbedPane.lock();
        try {
            // close tab
            TabItself tab = findTab(buddyWrapper);
            if (tab!=null) {
                tabbedPane.remove(tab);
            }
        } finally {
            tabbedPane.unlock();
        }
    }

    class TabbedConnectionEventListener implements ConnectionEventListener {

        public void connectionInitiated(Connection connection) {
            //TODO set tabs gray, but not disabled
        }
        public void connectionLost(Connection connection) {
            notifyAllUsers("Lost connection to " + connection.getServiceName(), connection);
            //TODO set tabs gray, but not disabled
        }

        public void connectionFailed(Connection connection, String message) {
            notifyAllUsers("Lost has failed: " + connection.getServiceName(), connection);
            //TODO set tabs gray, but not disabled
        }

        public void connectionEstablished(Connection connection) {
            notifyAllUsers("Connection established to " + connection.getServiceName(), connection);
            //TODO un-set tabs from gray
        }

        public boolean messageReceived(MessageSupport connection, Message message) throws Exception {
            if (connection.getSupportAccount()!=null && !message.isOutgoing() && message.isAutoResponse()                        // incoming autoresponse
                    && connection.getSupportAccount().equalsIgnoreCase(message.getContact().getName())   // from me
                    && !connection.getSupportAccount().equalsIgnoreCase(connection.getUser().getName())) // and not to me
                return false; // skip system notification messages for everyone else
            if (!message.isOutgoing() && (!ClientProperties.INSTANCE.isIgnoreSystemMessages() || !connection.isSystemMessage(message.getContact()))) {
//                offUIExecutor.execute(new Runnable() {
//                    public void run() {
//                      addTab(message.getContact(), false);
                        final TabItself tab;
                        if (ClientProperties.INSTANCE.getInterfaceIndex() == WindowManager.INTERFACE_TABBED) {
                            tab = addTab(message.getContact(), false);
                        } else {
                            tab = findTab(message.getContact());
                        }
                        if (tab!=null) {
                            feedForBuddy(message.getContact(), message);
                            if (getCurrentTab() == tab) {
                                tab.setTabHighlighted(false);
                            } else {  // not the current tab, turn it red
                                tab.setTabHighlighted(true);
                            }
                        }
//                    }
//                });
            }
            return true;
        }

        public boolean emailReceived(MessageSupport connection, Message message) throws Exception {
            return messageReceived(connection, message);
        }

        public void typingNotificationReceived(MessageSupport connection, Nameable contact) {
            final TabItself tab = getCurrentTab();
            if (tab != null && tab.getContact().equals(contact)) {
                if (delayedShow != null) { // 2nd is in case things haven't finished initializing
                    delayedShow.mark();
                }
            }
        }

        private void notifyUser(String message, Contact contact) {
            tabbedPane.lock();
            try {
                TabItself tab = findTab(contact);
                if (tab!=null) {
                    tab.appendHistoryText(new MessageImpl(contact, false, true, message), false);
                }
            } finally {
                tabbedPane.unlock();
            }
        }
        private void notifyAllUsers(String message, Connection conn) {
            tabbedPane.lock();
            try {
                Component[] components = tabbedPane.getComponents();
                for (Component eachComponent : components) {
                    if (eachComponent instanceof TabItself) {
                        TabItself tab = (TabItself) eachComponent;
                        if (conn.equals(tab.getContact().getConnection())) {
                            tab.appendHistoryText(new MessageImpl(tab.getContact(), false, true, message), false);
                        }                            
                    }
                }
            } finally {
                tabbedPane.unlock();
            }
        }

        public void statusChanged(Connection connection, Contact contact, Status oldStatus) {
            if (contact.getStatus().isOnline() != oldStatus.isOnline()) {
                notifyUser("\n" + contact + (contact.getStatus().isOnline()?" is now online.":" has disconnected."), contact);
            }
            tabbedPane.lock();
            try {
                Component[] components = tabbedPane.getComponents();
                for (Component eachComponent : components) {
                    if (eachComponent instanceof TabItself) {
                        TabItself tab = (TabItself) eachComponent;
                        if (contact.equals(tab.getContact())) {
                            tab.setLabelFromStatus();
                        }
                    }
                }
            } finally {
                tabbedPane.unlock();
            }
        }

        public void statusChanged(Connection connection) {
            tabbedPane.lock();
            try
            {
                Component[] components = tabbedPane.getComponents();
                for (Component eachComponent : components) {
                    if (eachComponent instanceof TabItself) {
                        ((TabItself) eachComponent).setLabelFromStatus();
                    }
                }
            } finally {
                tabbedPane.unlock();
            }
        }

        public void pictureReceived(IconSupport connection, Contact contact) {
            tabbedPane.lock();
            try {
                TabItself tab = findTab(contact);
                if (tab!=null) {
                    tab.userIcon.setIcon(contact.getPicture());
                }
            } finally {
                tabbedPane.unlock();
            }
        }

        public void fileReceiveRequested(FileTransferSupport connection, Contact contact, String filename, String description, Object connectionInfo) {
            tabbedPane.lock();
            try {
                TabItself tab = addTab(contact, true);
                description = description==null ? "": GeneralUtils.stripHTML(description);
                description = description.trim().length()==0?"":"\nDescription: " + description;
                String message = "\n" + contact.getName() + " is trying to send you a file: " + filename + description;
                tab.appendHistoryText(new MessageImpl(contact, false, true, message), false); // error
            } finally {
                tabbedPane.unlock();
            }
        }

        public void errorOccured(String message, Exception exception) {

        }

        public boolean contactRequestReceived(final String user, final MessageSupport connection) {
            return false;
        }
    }

    /**
     * Sends a message to buddy as long as this is the right window
     * @param contact which contact
     * @param message msg
     */
    public void feedForBuddy(Contact contact, Message message) {
        if (!frame.isVisible()) {
            frame.setVisible(true);
        }
        addTextToHistoryPanel(contact, message, false);
    }

    /**
     * This will add a tab and possible force it open.
     *  
     * @param cw contact for the window
     * @param forceToFront override to force the tab to front
     * @return tab reference
     */
    public TabItself addTab(final Contact cw, final boolean forceToFront) { //
        tabbedPane.lock();
        try {
            TabItself tab = findTab(cw);
            final TabItself finalTab;
            // only if tab d/n exist do we do this
            if (tab==null) {
                tab = new TabItself(cw, tabbedPane);
                finalTab = tab;
                tabbedPane.addTab(cw.getDisplayName(), tab);
                tab.addTabComponent();
                tab.setLabelFromStatus();
            } else {
                finalTab = tab;
            }
            
            GUIUtils.runOnAWT(new Runnable() {
                public void run() {
                    if (forceToFront || ClientProperties.INSTANCE.isForceFront()) {
                        tabbedPane.setSelectedComponent(finalTab);
                    } else {
                        tabbedPane.setSelectedComponent(currentTab);
                    }
                    if (forceToFront || ClientProperties.INSTANCE.isEasyOpen()) {
                        frame.setVisible(true);
                        frame.toFront();
                        frame.setState(Frame.NORMAL);
                    }
                    if (ClientProperties.INSTANCE.isUseAlert()) {
                        TrayAdapter.alert(frame);
                    }
                }
            });
            return tab;
        } finally {
            tabbedPane.unlock();
        }
    }

    /**
     * Locate the tab via the contact.
     * @param cw contact
     * @return tab or null
     */
    private TabItself findTab(Contact cw) {
        Component[] components = tabbedPane.getComponents();
        for (Component eachComponent : components) {
            if (eachComponent instanceof TabItself) {
                TabItself component = (TabItself) eachComponent;
                if (cw.equals(component.getContact())) return component;
            }
        }
        return null;
    }

    public void sortTabs() {
        GUIUtils.runOnAWT(new Runnable() {
            public void run() {
                boolean locked = tabbedPane.tryLock();
                if (locked) {
                    try {
                        // loop to control number of passes
                        Component[] components = tabbedPane.getComponents();
                        Arrays.sort(components, new Comparator<Component>() {
                            public int compare(Component o1, Component o2) {
                                if (o1 instanceof TabItself && o2 instanceof TabItself) {
                                    TabItself component1 = (TabItself) o1;
                                    TabItself component2 = (TabItself) o2;
                                    return component1.getContact().getDisplayName().compareTo(component2.getContact().getDisplayName());
                                }
                                return 0;
                            }
                        });
                        tabbedPane.removeAll();
                        for (Component component3 : components) {
                            if (component3 instanceof TabItself) {
                                TabItself component = (TabItself) component3;
                                tabbedPane.addTab("", component);
                                component.addTabComponent();
                                component.setLabelFromStatus();
                            }
                        } // end loop to control passes
                    } finally {
                        tabbedPane.unlock();
                    }
                } else { // didn't lock!
                    log.severe("Failed to lock in a GUI thread!!!!");
                }
            }
        });
    } // sortTabs

    public ConnectionEventListener getConnectionEventListener() {
        return connectionEventListener;
    }
} // class TabbedWindow
