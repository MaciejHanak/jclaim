package com.itbs.aimcer.gui;

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.commune.*;
import com.itbs.aimcer.commune.SMS.InvalidDataException;
import com.itbs.aimcer.commune.SMS.SMSWrapper;
import com.itbs.gui.*;
import com.itbs.util.DelayedThread;
import com.itbs.util.GeneralUtils;
import com.itbs.util.SoundHelper;
import org.jdesktop.jdic.desktop.Desktop;
import org.jdesktop.jdic.desktop.DesktopException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alex Rass
 * @since Sep 9, 2004
 */
public class MessageWindow  {
    private static final Logger log = Logger.getLogger(MessageWindow.class.getName());
    /** Size of the message box. */
    public static final Rectangle DEFAULT_SIZE = new Rectangle(400, 200, 350, 330);
    /** Where the line is. */
    private static final double DEFAULT_SEPARATION = 3.0 / 5.0;//150;
    /** The way the time in the window is formatted */
    DateFormat timeFormat = new SimpleDateFormat(ClientProperties.INSTANCE.getTimeFormat());

    private static List<MessageWindow> messageWindows = new CopyOnWriteArrayList<MessageWindow>();
    private static Executor offUIExecutor = Executors.newFixedThreadPool(2);

    private static ConnectionEventListener cel = new MessageWindowConnectionEventListener();
    public static ConnectionEventListener getConnectionEventListener() {
        return cel;
    }


    /** Target Audience */
    final private ContactWrapper contactWrapper;
    final private FileTransferHandler ftHandler;

    /** message frame. */
    final JFrame frame;
    /** Typing display with dalay. */
    private DelayedThread delayedShow;
    private JSplitPane splitPane; // lets us save the vertical separation, that's all.
    private long lastBeep;
    private JLabel userIcon;
    private String historyText;

    public MutableAttributeSet ATT_NORMAL, ATT_RED, ATT_BLUE, ATT_GRAY;

    public MessageSupport getConnection() {
        return (MessageSupport) contactWrapper.getConnection();
    }

    private BetterTextPane textPane;
    private JTextPane historyPane;
    private final AbstractAction ACTION_SEND, ACTION_ADD, ACTION_INFO, ACTION_LOG, ACTION_PAGE, ACTION_EMAIL;
    private JCheckBox secureIM;
    private final JLabel charCount = new JLabel();
    private JPanel personalInfo;

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
        GUIUtils.addCancelByEscape(frame, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                onWindowClose();
            }
        });
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
                                    // todo log if failed to send
                                    ErrorDialog.displayError(frame, "Failed to send message", e1);
                                }
                                textPane.requestFocus();
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
                if (textPane.getText().trim().length() == 0) {
                    Main.complain("Please enter the text to page with.");
                    return;
                }
                int result = JOptionPane.showConfirmDialog(frame, "Page user?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    try {
                        Message message = new MessageImpl(contactWrapper, true, "Via Cell: "  + textPane.getText());
                        addTextToHistoryPanel(message, true);

                        SMSWrapper.sendMessage(getConnection().getUserName(), contactWrapper.getPreferences().getPhone(), textPane.getText().trim());
                        textPane.setText(""); // wipe it
                    } catch (InvalidDataException ex) {
                        Main.complain(ex.getMessage());
                    } catch (IOException ex) {
                        Main.complain(ex.getMessage(), ex);
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
        frame.getContentPane().setLayout(new BorderLayout());

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
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                frame.getContentPane().add(getTextComponents());
                frame.getContentPane().add(getButtons(), BorderLayout.SOUTH);
                frame.setVisible(true);
                try {
                    if (ClientProperties.INSTANCE.isSpellCheck())
                        JazzyInterface.create().addSpellCheckComponent(textPane);
                } catch (IOException e) {
                    log.log(Level.SEVERE, "", e);
                }
                delayedShow.start();
                textPane.requestFocus();
            }
        });
        if (SoundHelper.playSound(ClientProperties.INSTANCE.getSoundNewWindow()))
            lastBeep = System.currentTimeMillis(); // don't forget to update this puppy
    }

    private void onWindowClose() {
        synchronized(messageWindows) {
            savePosition();
            frame.setVisible(false);
            frame.dispose();
            messageWindows.remove(this);
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
        synchronized(messageWindows) {
            messageWindows.add(messageWindow);
        }
    }

    public static MessageWindow findWindow(Nameable buddyWrapper) {
        for(MessageWindow messageWindow: messageWindows) {
            if (messageWindow.frame.isDisplayable()) { // todo see if this is better than isVisible();
                if (messageWindow.contactWrapper.equals(buddyWrapper)) {
                    return messageWindow;
                }
            } else {
                synchronized(messageWindows) {
                    messageWindows.remove(messageWindow);
                }
            }
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
    public static synchronized MessageWindow openWindow(Nameable buddyWrapper, boolean forceToFront) {
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
        if (ClientProperties.INSTANCE.isUseAlert())
            TrayAdapter.alert(messageWindow.frame);
        return messageWindow;
    }
    /**
     * Just Close!
     */
    public void closeWindow() {
        frame.dispose();
    }

    /**
     * Sends a message to buddy as long as this is the right window
     * @param message msg
     */
    public void feedForBuddy(Message message) {
//        if (ClientProperties.INSTANCE.isForceFront())
//            frame.toFront();
        if (ClientProperties.INSTANCE.isEasyOpen() && frame.getState() != Frame.NORMAL)
            frame.setState(Frame.NORMAL);
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
        offUIExecutor.execute(new Runnable() { public void run () { Main.saveProperties(); } });
    }

    private Component getButtons() {
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
        panel.add(new BetterButton(ACTION_INFO));

         // see if we already added this one some place
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

        panel.add(charCount);
        charCount.setToolTipText("Character Count");
        charCount.setVisible(ClientProperties.INSTANCE.isShowCharCounter());

        if (getConnection() != null && getConnection().isSecureMessageSupported())
            panel.add(secureIM = new JCheckBox("Secure"));
        panel.add(new BetterButton(ACTION_SEND));
        south.add(panel);
        personalInfo = new PersonalInfoPanel(contactWrapper);
        south.add(personalInfo);

        return south;
    }

    /**
     * Adds history pane and typing space.
     * @return panel with components
     */
    private JComponent getTextComponents() {
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
    private Component getMessage() {
        textPane = new BetterTextPane(ACTION_SEND);
        addDNDSupport(textPane);
//        EditorTools.addSuggestionPopup(textPane); // TODO finish
//        textPane.setContentType("text/html");
        if (!ClientProperties.INSTANCE.isEnterSends()) {
            textPane.addModifier(KeyEvent.SHIFT_DOWN_MASK);
            textPane.addModifier(KeyEvent.CTRL_DOWN_MASK);
        }
        textPane.getDocument().addDocumentListener(new DocumentListener() {
            public void count(DocumentEvent e) { charCount.setText(""+e.getDocument().getLength()); }
            public void insertUpdate(DocumentEvent e) { count(e); }
            public void removeUpdate(DocumentEvent e) { count(e); }
            public void changedUpdate(DocumentEvent e) { count(e); }
        });
        JPanel typingSpace = new JPanel(new BorderLayout());
        typingSpace.add(new JScrollPane(textPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        userIcon = new JLabel();
        if (ClientProperties.INSTANCE.isShowPictures())
            if (contactWrapper.getPicture() == null && getConnection() instanceof IconSupport)
                ((IconSupport) getConnection()).requestPictureForUser(contactWrapper);
             else
                userIcon.setIcon(contactWrapper.getPicture());
        typingSpace.add(userIcon, BorderLayout.EAST);
        return typingSpace;
    }

    private Component getHistory() {
//        historyPane = new JTextPane();
        historyPane = new BetterTextPane();
        addDNDSupport(historyPane);
        historyPane.setEditable(false);
//        historyPane.setContentType("text/html");
        appendHistoryText(historyText);
        if (!contactWrapper.getStatus().isOnline())
            appendHistoryText("\nStatus of this contact is offline or unknown. Your message may not get delivered.\n");
//        historyPane.setCaretPosition(historyPane.getDocument().getLength()); not needed since we do that in appendHistoryText() call
        recalculateAttributes();
        if (historyPane.getFont().getSize() > 10)
            StyleConstants.setFontSize(ATT_GRAY, historyPane.getFont().getSize() - 1);
        final JScrollPane jScrollPane = new JScrollPane(historyPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane.getVerticalScrollBar().setValue(jScrollPane.getVerticalScrollBar().getMaximum());
//        jScrollPane.invalidate();
//        jScrollPane.validate();
        return jScrollPane;
    }

    private void recalculateAttributes() {
        ATT_NORMAL = new SimpleAttributeSet();
        StyleConstants.setFontFamily(ATT_NORMAL,"Monospaced");
        StyleConstants.setFontSize(ATT_NORMAL, historyPane.getFont().getSize());
        ATT_BLUE = (MutableAttributeSet) ATT_NORMAL.copyAttributes();
        ATT_RED = (MutableAttributeSet) ATT_NORMAL.copyAttributes();
        ATT_GRAY = (MutableAttributeSet) ATT_NORMAL.copyAttributes();
        StyleConstants.setForeground(ATT_BLUE, Color.BLUE);
        StyleConstants.setForeground(ATT_RED,  Color.RED);
        StyleConstants.setForeground(ATT_GRAY, Color.GRAY);
    }

    /** Helper. */
    void appendHistoryText(final String text) {
        appendHistoryText(text, ATT_NORMAL);
    }

    public void appendHistoryText(final String text, final AttributeSet style) {
        GUIUtils.appendText(historyPane, text, style);
    }

    private void appendHistoryText(final String prefix, final boolean toBuddy, final String text) {
        GUIUtils.runOnAWT(new Runnable() {
            public void run() {
//                historyPane.setText(historyPane.getText() + text);
                final Document document = historyPane.getDocument();
                try {
                    document.insertString(document.getLength(), "\n", ATT_NORMAL);
                    document.insertString(document.getLength(),
                            prefix + (toBuddy?getConnection().getUser():contactWrapper.getDisplayName()) + ": ",
                            toBuddy?ATT_BLUE:ATT_RED);
                    document.insertString(document.getLength(), text, toBuddy?ATT_GRAY:ATT_NORMAL);
                } catch (BadLocationException e) {
                    ErrorDialog.displayError(frame, "Failed to display proper text:\n\n"+ text, e);
                }
                historyPane.setCaretPosition(historyPane.getDocument().getLength());
            }
        });
    }

    public JTextPane getHistoryPane() {
        return historyPane;
    }

    public void addTextToHistoryPanel(Message message, final boolean toBuddy) throws IOException {
        if (ClientProperties.INSTANCE.getDisclaimerMessage().trim().length() > 0 && (contactWrapper.getLastDisclaimerTime() == 0 ||
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
            appendHistoryText((ClientProperties.INSTANCE.isShowTime() ? timeFormat.format(new Date()) : ""),
                    toBuddy, (message.isAutoResponse()?"Automatic response: ":"") + (toBuddy?message.getText():message.getPlainText()));
            if (ClientProperties.INSTANCE.isSoundAllowed()
//                    && (toBuddy && ClientProperties.INSTANCE.isSoundSend()) || (!toBuddy && ClientProperties.INSTANCE.isSoundReceiveAllowed()))
                    && (!getConnection().isAway() || ClientProperties.INSTANCE.isSoundIdle())
                    && (lastBeep== 0 || lastBeep + ClientProperties.INSTANCE.getBeepDelay()*1000 < System.currentTimeMillis())) {
                SoundHelper.playSound(toBuddy?ClientProperties.INSTANCE.getSoundSend():ClientProperties.INSTANCE.getSoundReceive());
            }
            lastBeep = System.currentTimeMillis();
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
    static class FileTransferHandler extends AbstractFileTransferHandler {
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
                        MessageWindow window = openWindow(message.getContact(), false);
                        window.feedForBuddy(message);
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
                    messageWindow.appendHistoryText(text, ATT_ERROR);
                }
            }
        }

        /**
         * Nameable's status changed.
         *
         * @param contact  contact
         * @param idleMins
         */
        public void statusChanged(Connection connection, Contact contact, boolean online, boolean away, int idleMins) {
            if (contact.getStatus().isOnline() != online) {
                notifyUser("\n" + contact + (online?" is now online.":" has disconnected."), contact);
            }
        }

        /**
         * Statuses for contacts that belong to this connection have changed.
         *
         * @param connection
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
         * @param contact
         * @param filename
         * @param description
         * @param connectionInfo
         */
        public void fileReceiveRequested(FileTransferSupport connection, Contact contact, String filename, String description, Object connectionInfo) {
//            todo do
//            Iterator iter= messageWindows.iterator();
//            while (iter.hasNext()) {
//                MessageWindow messageWindow = (MessageWindow) iter.next();
//                messageWindow.appendText("\nFile send requesteed by contact" + messageWindow.contactWrapper), ATT_ERROR);
//            }
        }

    } // class
} // class MessageWindow