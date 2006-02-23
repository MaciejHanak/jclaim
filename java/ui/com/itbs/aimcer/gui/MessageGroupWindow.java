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

import com.itbs.aimcer.bean.ClientProperties;
import com.itbs.aimcer.bean.ContactWrapper;
import com.itbs.aimcer.bean.Message;
import com.itbs.aimcer.bean.MessageImpl;
import com.itbs.aimcer.commune.MessageSupport;
import com.itbs.gui.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Created by Alex Rass on Sep 9, 2004
 */
public class MessageGroupWindow  {
    /** Size of the message box. */
    public static final Rectangle DEFAULT_SIZE = new Rectangle(420, 200, 350, 330);
    /** Where the line is. */
    private static final double DEFAULT_SEPARATION = 3.0 / 5.0;//150;

    /** Target Audience */
    private ContactWrapper[] contactWrapper;
    /** message frame. */
    JFrame frame;

    MutableAttributeSet ATT_NORMAL, ATT_RED, ATT_BLUE, ATT_GRAY;

    private BetterTextPane textPane;
    private JTextPane historyPane;
    private final AbstractAction ACTION_SEND;

    /**
     * Constructor
     * @param selectedBuddy buddy to work
     */
    private MessageGroupWindow(final ContactWrapper[] selectedBuddy) {
        contactWrapper = selectedBuddy;
        frame = GUIUtils.createFrame("Group Shout");
        frame.setIconImage(ImageCacheUI.ICON_JC.getIcon().getImage());
        frame.setBounds(DEFAULT_SIZE);
        GUIUtils.addCancelByEscape(frame, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        ACTION_SEND = new ActionAdapter("Send", "Send message (" + (ClientProperties.INSTANCE.isEnterSends()?"":"Ctrl-") + "Enter )",
                        new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                if (textPane.getText().trim().length() == 0)
                                    return;
                                Message message;
                                for (int i = 0; i < contactWrapper.length; i++) {
                                    ContactWrapper contact = contactWrapper[i];
                                    message = new MessageImpl(contact, true, textPane.getText());
                                    try {
                                        if (i==0) {
                                            addTextToHistoryPanel(message);
                                        }
                                        if (contact.getConnection() instanceof MessageSupport)
                                            ((MessageSupport) contact.getConnection()).sendMessage(message);
                                    } catch (Exception e1) {
                                        appendHistoryText("Failed to send message to " + contact + ". Error: " + e1.getMessage());
                                    }
                                }
                                textPane.setText(""); // wipe it
                                textPane.requestFocus();
                            }
                        }, 'S');
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(getComponents());
        frame.getContentPane().add(getButtons(), BorderLayout.SOUTH);
        frame.setVisible(true);
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                textPane.requestFocus();
            }
        });
        try {
            if (ClientProperties.INSTANCE.isSpellCheck())
                JazzyInterface.create().addSpellCheckComponent(textPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Find the window and open it.
     * If doesn't exist - create one.
     * @param wrappers to find the window by
     * @return reference to the window
     */
    public static MessageGroupWindow openWindow(ContactWrapper[] wrappers) {
        return new MessageGroupWindow(wrappers);
    }

    private Component getButtons() {
        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 3));
        panel.setOpaque(false);

        panel.add(new BetterButton(ACTION_SEND));
        south.add(panel);
        return south;
    }

    /**
     * Adds history pane and typing space.
     * @return panel with components
     */
    private Component getComponents() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, getHistory(), getMessage());
        splitPane.setDividerLocation((int)(frame.getHeight() * DEFAULT_SEPARATION));
        return splitPane;
    }

    /**
     * Created the typing window.
     * @return panel with typing space.
     */
    private Component getMessage() {
        textPane = new BetterTextPane(ACTION_SEND);
//        textPane.setContentType("text/html");
        if (!ClientProperties.INSTANCE.isEnterSends()) {
            textPane.addModifier(KeyEvent.SHIFT_DOWN_MASK);
            textPane.addModifier(KeyEvent.CTRL_DOWN_MASK);
        }
        JPanel typingSpace = new JPanel(new BorderLayout());
        typingSpace.add(new JScrollPane(textPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        return typingSpace;
    }

    private Component getHistory() {
//        historyPane = new JTextPane();
        historyPane = new BetterTextPane();
        historyPane.setEditable(false);
//        historyPane.setContentType("text/html");
        String contactList="";
        boolean allOnline = true;
        for (ContactWrapper aContactWrapper : contactWrapper) {
            contactList += aContactWrapper + " ";
            if (allOnline && !aContactWrapper.getStatus().isOnline())
                allOnline = false;
        }
        appendHistoryText("The following contacts will receive your message: " + contactList);
        if (!allOnline)
            appendHistoryText("\nSome contacts are offline. Your message may be missed.\n");
        historyPane.setCaretPosition(historyPane.getDocument().getLength());

        recalculateAttributes();
        if (historyPane.getFont().getSize() > 10)
            StyleConstants.setFontSize(ATT_GRAY, historyPane.getFont().getSize() - 1);
        final JScrollPane jScrollPane = new JScrollPane(historyPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane.invalidate();
        jScrollPane.validate();
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

    void appendHistoryText(final String text, final AttributeSet style) {
        GUIUtils.runOnAWT(new Runnable() {
            public void run() {
//                historyPane.setText(historyPane.getText() + text);
                final Document document = historyPane.getDocument();
                try {
                    document.insertString(document.getLength(), text, style);
                } catch (BadLocationException e) {
                    ErrorDialog.displayError(frame, "Failed to display proper text:\n\n"+ text, e);
                }
                historyPane.setCaretPosition(historyPane.getDocument().getLength());
            }
        });
    }

    private void appendHistoryText(final String prefix, final ContactWrapper sendTo, final String text) {
        GUIUtils.runOnAWT(new Runnable() {
            public void run() {
//                historyPane.setText(historyPane.getText() + text);
                final Document document = historyPane.getDocument();
                try {
                    document.insertString(document.getLength(), "\n", ATT_NORMAL);
                    document.insertString(document.getLength(),
                            prefix + (sendTo.getConnection().getUser()) + ": ",
                            ATT_BLUE);
                    document.insertString(document.getLength(), text, ATT_GRAY);
                } catch (BadLocationException e) {
                    ErrorDialog.displayError(frame, "Failed to display proper text:\n\n"+ text, e);
                }
                historyPane.setCaretPosition(historyPane.getDocument().getLength());
            }
        });
    }

    DateFormat timeFormat = new SimpleDateFormat("<hh:mm>");
    public void addTextToHistoryPanel(Message message) throws IOException {
/*
        ContactWrapper contactWrapper = ((ContactWrapper)message.getContact());
        if (ClientProperties.INSTANCE.getDisclaimerMessage().trim().length() > 0 && (contactWrapper.getLastDisclaimerTime() == 0 ||
                System.currentTimeMillis() - contactWrapper.getLastDisclaimerTime() > ClientProperties.INSTANCE.getDisclaimerInterval())) {
            contactWrapper.setLastDisclaimerTime();
            Message disclMessage = new MessageImpl(contactWrapper, true, false, ClientProperties.INSTANCE.getDisclaimerMessage());
            contactWrapper.getConnection().sendMessage(disclMessage);
            // take care of the disclaimer!
            //Main.getLogger().log(disclMessage);
            appendText(ClientProperties.INSTANCE.getDisclaimerMessage());
        }
*/
        try {
            // now the actual message
//            Message messageOut = new MessageImpl(contactWrapper, toBuddy, message);
            //Main.getLogger().log(messageOut);
            appendHistoryText((ClientProperties.INSTANCE.isShowTime() ? timeFormat.format(new Date()) : ""),
                    (ContactWrapper) message.getContact(), (message.isAutoResponse()?"Automatic response: ":"") + message.getPlainText());
            Toolkit.getDefaultToolkit().beep();
        } catch (Exception e) {
            ErrorDialog.displayError(frame, "Error processing command.  Try again.\n"+e.getMessage(), e);
        }
    }

} // class MessageWindow