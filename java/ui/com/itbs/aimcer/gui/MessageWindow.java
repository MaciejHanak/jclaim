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
import com.itbs.aimcer.commune.SMS.InvalidDataException;
import com.itbs.aimcer.gui.order.IOIEntryPanel;
import com.itbs.aimcer.gui.order.OrderEntryLog;
import com.itbs.aimcer.gui.order.OrderEntryPanel;
import com.itbs.gui.*;
import com.itbs.util.DelayedThread;
import com.itbs.util.GeneralUtils;
import org.jdesktop.jdic.desktop.Desktop;
import org.jdesktop.jdic.desktop.DesktopException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Provides the UI for the messaging window.
 *
 * @author Alex Rass
 * @since Sep 9, 2004
 */
public class MessageWindow extends MessageWindowBase {
    private static final Logger log = Logger.getLogger(MessageWindow.class.getName());

    private static final List<MessageWindow> messageWindows = new CopyOnWriteArrayList<MessageWindow>();

    private static final ConnectionEventListener cel = new MessageWindowConnectionEventListener();
    public static ConnectionEventListener getConnectionEventListener() {
        return cel;
    }

    /** Locks */
    private static final ReentrantLock lock = new ReentrantLock();

    /** Target Audience */
    final private ContactWrapper contactWrapper;
    final private FileTransferHandler ftHandler;

    /** Typing icon display with delay. */
    private DelayedThread delayedShow;
    private JLabel userIcon= new JLabel();
    private String historyText;

    public MessageSupport getConnection() {
        return (MessageSupport) contactWrapper.getConnection();
    }

    private final AbstractAction ACTION_ADD, ACTION_ORDER, ACTION_INFO, ACTION_LOG, ACTION_PAGE, ACTION_EMAIL;
    private JCheckBox secureIM;
    private JPanel orderEntry, personalInfo, adPanel;

    /**
     * Constructor
     * @param selectedBuddy buddy to work
     */
    private MessageWindow(ContactWrapper selectedBuddy) {
        contactWrapper = selectedBuddy;
        ftHandler = new FileTransferHandler(contactWrapper);
        registerScreen(this); // maintains a list of open windows
        frame = GUIUtils.createFrame(GeneralUtils.stripHTML(contactWrapper.getDisplayName()));
        ImageIcon icon = ImageCacheUI.getImage(selectedBuddy.getConnection().getClass());
        if (icon==null) icon = ImageCacheUI.ICON_JC.getIcon(); // in case there's nothing - go for default.
        frame.setIconImage(icon.getImage());
        frame.pack();
        // Load history now so that we get the message before this new one got written to the logs
        try {
            historyText = Main.getLogger().loadLog(getConnection(), contactWrapper.getName());
        } catch (IOException e) {
            historyText = "History failed to load. " + (e.getMessage()==null?e.toString():e.getMessage());
        }
        Rectangle bounds = contactWrapper.getPreferences().getWindowBounds();
        frame.setBounds(bounds==null?DEFAULT_SIZE:bounds);
        GUIUtils.addCancelByEscape(frame);
        ACTION_SEND = new ActionAdapter("Send", "Send message (" + (ClientProperties.INSTANCE.isEnterSends()?"":"Ctrl-") + "Enter )",
                        new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                if (textPane.getText().trim().length() == 0)
                                    return;
                                try {
                                    Message message = new MessageImpl(contactWrapper, true, textPane.getText());
                                    addTextToHistoryPanel(message, true);
                                    if (secureIM != null && secureIM.isSelected())
                                        getConnection().sendSecureMessage(message);
                                    else
                                        getConnection().sendMessage(message);
                                    textPane.setText(""); // wipe it
                                } catch (Exception e1) {
                                    log.log(Level.SEVERE, "Failed to send message", e1);
                                    ErrorDialog.displayError(frame, "Failed to send message", e1);
                                }
                                textPane.requestFocusInWindow();
                            }
                        }, 'S');
        ACTION_ADD = new ActionAdapter("Add", "Add to the list", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    MenuManager.addContact(getConnection(), frame, contactWrapper.getName());
                } catch (Exception e1) {
                    ErrorDialog.displayError(frame, "Failure while adding a contact.\n"+e1.getMessage(), e1);
                }
            }
        }, 'A');
        ACTION_ORDER = new ActionAdapter("Order", "Make an order", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                workExtraPanel(orderEntry);
                if (orderEntry.isVisible() && ClientProperties.INSTANCE.isOrderCausesShowManageScreen()) { // bring up the screen
                    OrderEntryLog.getInstance().showFrame();
                    frame.toFront();
                }
            }
        }, 'O');
        ACTION_INFO = new ActionAdapter(ImageCacheUI.ICON_INFO.getIcon(), "Personal Info", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                workExtraPanel(personalInfo);
            }
        });
        ACTION_EMAIL = new ActionAdapter(ImageCacheUI.ICON_EMAIL.getIcon(), "Send email", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (contactWrapper.getPreferences().getEmailAddress() != null && contactWrapper.getPreferences().getEmailAddress().trim().length() > 0) {
                    new Thread() {
                        public void run() {
                            try {
                                final org.jdesktop.jdic.desktop.Message msg = new org.jdesktop.jdic.desktop.Message();
                                java.util.List<String> list  = new ArrayList<String>();
                                list.add(contactWrapper.getPreferences().getEmailAddress());
                                msg.setToAddrs(list);
                                msg.setBody(textPane.getText());
                                Desktop.mail(msg);
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
                if (contactWrapper.getPreferences().getPhone() == null || contactWrapper.getPreferences().getPhone().trim().length() == 0) {
                    Main.complain("Please populate the entry for cell phone on the info panel.");
                    return;
                }
                int result = JOptionPane.showConfirmDialog(frame, "This will open a new IM window.\nContinue?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    try {
                        sendSMSMessage(Main.getConnections(),  getConnection(), contactWrapper);
                    } catch (InvalidDataException ex) {
                        Main.complain(ex.getMessage());
                    }
                } // if Yes
            }
        });
        ACTION_LOG = new ActionAdapter(ImageCacheUI.ICON_HISTORY.getIcon(), "Show the log file", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file = Main.getLogger().getLog(getConnection(), contactWrapper.getName());
                if (!file.exists() || file.isDirectory())
                    JOptionPane.showMessageDialog(frame, "Failed to locate the log file", "Error", JOptionPane.ERROR_MESSAGE);
                else {
                    // start default editor
                    try {
                        Desktop.open(file);
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

        // Make sure to save last coordinates.
        frame.addWindowListener(new WindowAdapter() {
            /**
             * Invoked when a window is in the process of being closed.
             * The close operation can be overridden at this point.
             */
            public void windowClosing(WindowEvent e) {
                onWindowClose();
            }
        });
        composeUI();
    }

    protected void startUIDependent() {
        delayedShow.start();
    }

    private void onWindowClose() {
        lock.lock();
        try {
            savePosition();
            frame.setVisible(false);
            frame.dispose();
            messageWindows.remove(this);
        } finally {
            lock.unlock();
        }
    }

    private void addDNDSupport(JComponent comp) {
        if (contactWrapper.getConnection() instanceof FileTransferSupport) {
            comp.setTransferHandler(ftHandler);
        }
    }

    /**
     * Shows and hides the extra panel.
     * Adjusts the size of the frame as well.
     * @param extraPanel to work with
     */
    private void workExtraPanel(JPanel extraPanel) {
        frame.setSize(extraPanel.getPreferredSize().width <= frame.getSize().width || extraPanel.isVisible() ? frame.getSize().width : extraPanel.getPreferredSize().width + 3,
                frame.getSize().height + extraPanel.getPreferredSize().height * (extraPanel.isVisible()?-1:1));
        extraPanel.setVisible(!extraPanel.isVisible());
        frame.validate();
    }

    private static void registerScreen(MessageWindow messageWindow) {
        lock.lock();
        try {
            messageWindows.add(messageWindow);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Finds the window to see if it's open.
     * @param buddyWrapper to find
     * @return window or null if not open
     */
    public static MessageWindow findWindow(Nameable buddyWrapper) {
        lock.lock();
        try {
            for(MessageWindow messageWindow: messageWindows) {
                if (messageWindow.frame.isDisplayable()) { // todo see if this is better than isVisible();
                    if (messageWindow.contactWrapper.equals(buddyWrapper)) {
                        return messageWindow;
                    }
                } else {
                    messageWindows.remove(messageWindow);
                }
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    /**
     * Find the window and open it.
     * If doesn't exist - create one.
     * @param buddyWrapper to find the window by
     * @param forceToFront to force or not to force (properties are checked inside)
     * @return reference to the window
     */
    public static MessageWindow openWindow(Nameable buddyWrapper, boolean forceToFront) {
        lock.lock();
        try {
            MessageWindow messageWindow = findWindow(buddyWrapper);
            if (messageWindow!=null) {
                if (ClientProperties.INSTANCE.isForceFront() || forceToFront) {
                    if (!messageWindow.frame.isVisible())
                        messageWindow.frame.setVisible(true);
                    messageWindow.frame.setState(Frame.NORMAL);
                    messageWindow.frame.toFront();
                }
            }
            // if I got here - noone wanted it
            if (messageWindow == null)
                messageWindow = new MessageWindow((ContactWrapper) buddyWrapper);
            if (ClientProperties.INSTANCE.isEasyOpen() && messageWindow.frame.getState() != Frame.NORMAL) {
                messageWindow.frame.setState(Frame.NORMAL);
                GeneralUtils.sleep(500); // this isn't helping either
            }
            if (ClientProperties.INSTANCE.isUseAlert())
                TrayAdapter.alert(messageWindow.frame);
            return messageWindow;
        } finally {
            lock.unlock();
        }

    }

    /**
     * Sends a message to buddy as long as this is the right window
     * @param message msg
     */
    public void feedForBuddy(Message message) {
        try {
            addTextToHistoryPanel(message, false);
        } catch (IOException e) {
            ErrorDialog.displayError(frame, "Error processing command.  Try again.\n"+e.getMessage(), e);
        }
    }

    private void savePosition() {
        contactWrapper.getPreferences().setWindowBounds(frame.getBounds());
        contactWrapper.getPreferences().setVerticalSeparation(splitPane.getDividerLocation());
        // these are just getting set for the heck of it. looked like a good place.
        contactWrapper.getPreferences().setInfoPanelVisible(personalInfo.isVisible());
        if (orderEntry != null)
            contactWrapper.getPreferences().setOrderPanelVisible(orderEntry.isVisible());
        offUIExecutor.execute(new Runnable() { public void run () { Main.saveProperties(); } });
    }

    protected Component getButtons() {
        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 3));
        addDNDSupport(panel);
        panel.setOpaque(false);
//        ((OscarConnection)connection).setIconForUser(contactWrapper);
        final JLabel typing = new JLabel(ImageCacheUI.ICON_TYPE.getIcon());
        typing.setVisible(false);
        delayedShow = new DelayedActionThread("Typing Status Update", 2000, frame,
                new Runnable() {
                    public void run() {
                        typing.setVisible(true);
                    }
                },
                new Runnable() {
                    public void run() {
                        typing.setVisible(false);
                    }
                }
        );
        panel.add(typing);
        panel.add(new BetterButton(ACTION_EMAIL));
        panel.add(new BetterButton(ACTION_PAGE));
        panel.add(new BetterButton(ACTION_LOG));
        if (ClientProperties.INSTANCE.isEnableOrderEntryInSystem())
            if (ClientProperties.INSTANCE.isShowOrderEntry())
                panel.add(new BetterButton(ACTION_ORDER));

        panel.add(new BetterButton(ACTION_INFO));

        // see if we already added this one some place and if so, we don't need the add button
        // todo consider doing the search off screen and making button visible
        if (!contactWrapper.getStatus().isOnline()) {
            boolean found = false;
            GroupList gl = contactWrapper.getConnection().getGroupList();
            for (int i = 0; i < gl.size(); i++) {
                Group group =  gl.get(i);
                for (int g = 0; g<group.size(); g++) {
                    Nameable cw = group.get(g);
                    if (contactWrapper.equals(cw))
                        found = true;
                }
            }
            if (!found)
                panel.add(new BetterButton(ACTION_ADD));
        }

        if (ClientProperties.INSTANCE.isShowCharCounter()) {
            final JLabel charCount = new JLabel();
            textPane.getDocument().addDocumentListener(new DocumentListener() {
                public void count(DocumentEvent e) { charCount.setText(""+e.getDocument().getLength()); }
                public void insertUpdate(DocumentEvent e) { count(e); }
                public void removeUpdate(DocumentEvent e) { count(e); }
                public void changedUpdate(DocumentEvent e) { count(e); }
            });
            panel.add(charCount);
            charCount.setToolTipText("Character Count");
        }

        if (getConnection() != null && getConnection().isSecureMessageSupported())
            panel.add(secureIM = new JCheckBox("Secure"));
        panel.add(new BetterButton(ACTION_SEND));
        south.add(panel);
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

        personalInfo = new PersonalInfoPanel(contactWrapper);
        south.add(personalInfo);

        if (System.getProperty("USE_ADS") != null) {
            adPanel = new AdPanel();
            south.add(adPanel);
        }

        return south;
    }

    /**
     * Adds history pane and typing space.
     * @return panel with components
     */
    protected JComponent getTextComponents() {
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, getHistory(), getMessage());
        addDNDSupport(splitPane);
        if (contactWrapper.getPreferences().getVerticalSeparation() == -1)
            splitPane.setDividerLocation((int)(frame.getHeight() * DEFAULT_SEPARATION));
        else
            splitPane.setDividerLocation(contactWrapper.getPreferences().getVerticalSeparation());
//        splitPane.setVisible(false);
        return splitPane;
    }

    /**
     * Created the typing window.
     * @return panel with typing space.
     */
    JComponent getMessage() {
        JComponent typingSpace = super.getMessage();
        addDNDSupport(textPane);
        if (ClientProperties.INSTANCE.isShowPictures())
            if (contactWrapper.getPicture() == null && getConnection() instanceof IconSupport)
                ((IconSupport) getConnection()).requestPictureForUser(contactWrapper);
             else
                userIcon.setIcon(contactWrapper.getPicture());
        typingSpace.add(userIcon, BorderLayout.EAST);
        userIcon.setVisible(contactWrapper.getPreferences().isShowIcon());
        return typingSpace;
    }

    protected Component getHistory() {
//        historyPane = new JTextPane();
        historyPane = new BetterTextPane();
//        historyPane.setDocument(new HTMLDocLinkDetector(historyPane));
        addDNDSupport(historyPane);
        historyPane.setEditable(false);

//        historyPane.setContentType("text/html");
        appendHistoryText(historyText);
        if (!contactWrapper.getStatus().isOnline())
            appendHistoryText("\nStatus of this contact is offline or unknown. Your message may not get delivered.\n");
//        historyPane.setCaretPosition(historyPane.getDocument().getLength()); not needed since we do that in appendHistoryText() call
        if (historyPane.getFont().getSize() > 10)
            StyleConstants.setFontSize(ATT_GRAY, ClientProperties.INSTANCE.getFontSize());
        final JScrollPane jScrollPane = new JScrollPane(historyPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane.getVerticalScrollBar().setValue(jScrollPane.getVerticalScrollBar().getMaximum());
//        jScrollPane.invalidate();
//        jScrollPane.validate();
        return jScrollPane;
    }


    void appendHistoryText(final String prefix, final boolean toBuddy, final String text) {
        GUIUtils.runOnAWT(new Runnable() {
            public void run() {
//                historyPane.setText(historyPane.getText() + text);
                final Document document = historyPane.getDocument();
                try {
                    document.insertString(document.getLength(), "\n", ATT_NORMAL);
                    document.insertString(document.getLength(),
                            prefix + (toBuddy?getConnection().getUserName():contactWrapper.getDisplayName()) + ": ",
                            toBuddy?ATT_BLUE:ATT_RED);
                    document.insertString(document.getLength(), text, toBuddy?ATT_GRAY:ATT_NORMAL);
                } catch (BadLocationException e) {
                    ErrorDialog.displayError(frame, "Failed to display proper text:\n\n"+ text, e);
                }
                historyPane.setCaretPosition(historyPane.getDocument().getLength());
            }
        });
    }


    public void addTextToHistoryPanel(Message message, final boolean toBuddy) throws IOException {
        if (ClientProperties.INSTANCE.getDisclaimerMessage().trim().length() > 0 && ClientProperties.INSTANCE.getDisclaimerInterval() > 0 && (contactWrapper.getLastDisclaimerTime() == 0 ||
                System.currentTimeMillis() - contactWrapper.getLastDisclaimerTime() > ClientProperties.INSTANCE.getDisclaimerInterval())) {
            contactWrapper.setLastDisclaimerTime();
            Message disclMessage = new MessageImpl(contactWrapper, true, false, ClientProperties.INSTANCE.getDisclaimerMessage());
            getConnection().sendMessage(disclMessage);
            // take care of the disclaimer!
            //Main.getLogger().log(disclMessage);
            appendHistoryText(ClientProperties.INSTANCE.getDisclaimerMessage());
        }
        try {
            // now the actual message
//            Message messageOut = new MessageImpl(contactWrapper, toBuddy, message);
            //Main.getLogger().log(messageOut);
            appendHistoryText((ClientProperties.INSTANCE.isShowTime() ? TIME_FORMAT.format(new Date()) : ""),
                    toBuddy, (message.isAutoResponse()?"Automatic response: ":"") + (toBuddy?message.getText():message.getPlainText()));
        } catch (Exception e) {
            ErrorDialog.displayError(frame, "Error processing command.  Try again.\n"+e.getMessage(), e);
        }
    }

    public boolean isVisible() {
        return frame!=null && frame.isDisplayable();
    }

    /////////////////////////////////////////////////////////////////////
   // *******************  File Transfers   **************  //
  ///////////////////////////////////////////////////////////////////
    public static class FileTransferHandler extends AbstractFileTransferHandler {
        ContactWrapper wrapper;
        public FileTransferHandler(ContactWrapper contactWrapper) {
            wrapper = contactWrapper;
        }

        protected void handle(JComponent c, List<File> fileList) {
            MenuManager.sendFileDialog(wrapper, fileList);
        }
    }
    /////////////////////////////////////////////////////////////////////
   // *******************  ConnectionEventListener   **************  //
  ///////////////////////////////////////////////////////////////////
    static class MessageWindowConnectionEventListener implements ConnectionEventListener {
        static SimpleAttributeSet ATT_ERROR;

        MessageWindowConnectionEventListener() {
            if (ATT_ERROR == null) {
                ATT_ERROR = new SimpleAttributeSet();
                StyleConstants.setFontFamily(ATT_ERROR, "Monospaced");
                StyleConstants.setFontSize(ATT_ERROR, ClientProperties.INSTANCE.getFontSize());
                StyleConstants.setForeground(ATT_ERROR,  Color.RED);
            }
        }
        /**
         * Recevied a message.
         * Called with incoming and outgoing messages from any connection.
         *
         * @param connection connection
         @param message    message
         */
        public boolean messageReceived(MessageSupport connection, final Message message) {
            if (connection.getSupportAccount()!=null && !message.isOutgoing() && message.isAutoResponse()                        // incoming autoresponse
                    && connection.getSupportAccount().equalsIgnoreCase(message.getContact().getName())   // from me
                    && !connection.getSupportAccount().equalsIgnoreCase(connection.getUser().getName())) // and not to me
                return false; // skip system notification messages for everyone else
            if (!message.isOutgoing() && (!ClientProperties.INSTANCE.isIgnoreSystemMessages() || !connection.isSystemMessage(message.getContact()))) {
//                offUIExecutor.execute(new Runnable() {
//                    public void run() {
//                        MessageWindow window = openWindow(message.getContact(), false);
                        MessageWindow window;
                        if (ClientProperties.INSTANCE.getInterfaceIndex() == WindowManager.INTERFACE_WINDOWED) {
                            window = openWindow(message.getContact(), ClientProperties.INSTANCE.isEasyOpen());
                        } else {
                            window = findWindow(message.getContact());
                        }
                        if (window != null) {
                            window.feedForBuddy(message);
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
            MessageWindow window = findWindow(contact);
            if (window != null && window.delayedShow != null) // 2nd is in case things haven't finished initializing
                window.delayedShow.mark();
        }

        public void connectionLost(Connection connection) {
            notifyAllUsers("Lost connection to " + connection.getServiceName(), connection);
        }

        public void connectionInitiated(Connection connection) {
        }

        public void connectionFailed(Connection connection, String message) {
            notifyAllUsers("Connection has failed: " + connection.getServiceName(), connection);
        }

        /**
         * Indicates we are now logged in and can start using the connection.
         * Place to run connection-related properties etc.
         *
         * @param connection that has finished stabilizing
         */
        public void connectionEstablished(Connection connection) {
            notifyAllUsers("Connection established to " + connection.getServiceName(), connection);
        }

        /**
         * Messages all users for this connection
         * @param text to send
         * @param connection that notifies
         */
        public void notifyAllUsers(String text, Connection connection) {
            for (MessageWindow messageWindow : messageWindows) {
                if (messageWindow.contactWrapper.getConnection().equals(connection)) {
                    messageWindow.appendHistoryText("\n" + text, ATT_ERROR);
                }
            }
        }

        /**
         * Nameable's status changed.
         *
         * @param connection connection
         * @param contact    contact with updated status
         * @param oldStatus  status of the contact before this event happened.
         */
        public void statusChanged(Connection connection, Contact contact, Status oldStatus) {
            if (contact.getStatus().isOnline() != oldStatus.isOnline()) {
                notifyUser("\n" + contact + (contact.getStatus().isOnline()?" is now online.":" has disconnected."), contact);
            }
        }

        /**
         * Statuses for contacts that belong to this connection have changed.
         *
         * @param connection to use
         */
        public void statusChanged(Connection connection) {
/*
            for (MessageWindow messageWindow : messageWindows) {
                if (messageWindow.contactWrapper.getConnection().equals(connection)) {
                    messageWindow.appendHistoryText("\n"
                            + messageWindow.contactWrapper.getDisplayName()
                            + (messageWindow.contactWrapper.isOnline() ? " is now online." : " has disconnected."),
                            ATT_ERROR);
                }
            }
*/
        }

        private void notifyUser(String message, Contact contact) {
            for (MessageWindow messageWindow : messageWindows) {
                if (contact.equals(messageWindow.contactWrapper))
                    messageWindow.appendHistoryText(message, ATT_ERROR);
            }
        }

        /**
         * A previously requested icon has arrived.
         * Icon will be a part of the contact.
         *
         * @param connection connection
         * @param contact    contact
         */
        public void pictureReceived(IconSupport connection, Contact contact) {
            //set the icon for the user
            for (MessageWindow messageWindow : messageWindows) {
                if (messageWindow.contactWrapper.equals(contact)) {
                    messageWindow.userIcon.setIcon(contact.getPicture());
                }
            }
        }

        public boolean contactRequestReceived(final String user, final MessageSupport connection) {  return true; }

        /**
         * Gets called when an assynchronous error occurs.
         *
         * @param message   to display
         * @param exception exception for tracing
         */
        public void errorOccured(String message, Exception exception) {
            //don't care
        }

        /**
         * Other side requested a file transfer.
         * @param connection connection
         * @param contact who initiated msg
         * @param filename proposed name of file
         * @param description of the file
         * @param connectionInfo  your private object used to store protocol specific data
         */
        public void fileReceiveRequested(FileTransferSupport connection, Contact contact, String filename, String description, Object connectionInfo) {
            MessageWindow window = openWindow(contact, false);
            description = description==null ? "": GeneralUtils.stripHTML(description);
            description = description.trim().length()==0?"":"\nDescription: " + description;
            String message = "\n" + contact.getName() + " is trying to send you a file: " + filename + description;
            window.appendHistoryText(message, ATT_ERROR);
        }

    } // class MessageWindowConnectionEventListener

    /**
     * Sends message via any available media.
     * @param conns All available connections to try (can be null)
     * @param conn Preferred connection to first use. Used to get From preferences. (can not be null)
     * @param to whom
     * @throws InvalidDataException when number is wrong
     */
    public static void sendSMSMessage(List<Connection> conns, Connection conn, ContactWrapper to) throws InvalidDataException {
        if (GeneralUtils.isNotEmpty(conn.getUser().getName()) && GeneralUtils.isNotEmpty(to.getPreferences().getPhone())) {
            String result="";
            boolean found = false;
            // Check default one first
            if (conn instanceof SMSSupport && conn.isLoggedIn()) {
                result = ((SMSSupport)conn).veryfySupport(GeneralUtils.stripPhone(to.getPreferences().getPhone()));
                if (result == null) {
                    found = true;
                    ContactWrapper cw = (ContactWrapper) conn.getContactFactory().create(GeneralUtils.stripPhone(to.getPreferences().getPhone()), conn);
                    cw.getPreferences().setName(to.getPreferences().getName());
                    cw.getPreferences().setDisplayName((to.getPreferences().getDisplayName()==null?to.getName():to.getPreferences().getDisplayName()) + " (Phone)");
                    Main.globalWindowHandler.openWindow(cw, true);
                }
            }
            if (!found && conns!=null) { // Search
                String tempResult;
                for (Connection connection : conns) {
                    if (connection instanceof SMSSupport){
                        if (!connection.isLoggedIn()) {
                            result += "Connection " + connection.getUser().getName() + " on " + connection.getServiceName() + " can't be used (not logged in).\n";
                            continue;
                        }
                        tempResult = ((SMSSupport)connection).veryfySupport(GeneralUtils.stripPhone(to.getPreferences().getPhone()));
                        if (tempResult!=null) {
                            result += tempResult + "\n";
                            continue;
                        }
                        found = true;
                        ContactWrapper cw = (ContactWrapper) connection.getContactFactory().create(GeneralUtils.stripPhone(to.getPreferences().getPhone()), connection);
                        cw.getPreferences().setName(to.getPreferences().getName());
                        cw.getPreferences().setDisplayName(to.getPreferences().getDisplayName() + " (Phone)");
                        Main.globalWindowHandler.openWindow(cw, true);
                    }
                }
                if (!found) {
                    throw new InvalidDataException("Failed to send for any one of the following reasons:\n" + result);
                }
            }
        } else {
            throw new InvalidDataException("Missing required parameter.");
        }
    }

} // class MessageWindow