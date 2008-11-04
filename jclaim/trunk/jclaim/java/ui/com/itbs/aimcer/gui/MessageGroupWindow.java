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
import com.itbs.aimcer.commune.MessageSupport;
import com.itbs.gui.*;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Provies a way to mass message people.
 * TODO stop this cut and paste mess and start using regular message window.
 *
 * @author Alex Rass 
 * @since Sep 9, 2004
 */
public class MessageGroupWindow  extends MessageWindowBase {
    private static final Logger log = Logger.getLogger(MessageGroupWindow.class.getName());

    /** Target Audience */
    private List<Contact> contactWrapper;

    /**
     * Constructor
     * @param selectedBuddy buddy to work
     */
    private MessageGroupWindow(final List<Contact> selectedBuddy) {
        contactWrapper = selectedBuddy;
        frame = GUIUtils.createFrame("Group Shout");
        frame.setIconImage(ImageCacheUI.ICON_JC.getIcon().getImage());
        frame.setBounds(DEFAULT_SIZE);
        GUIUtils.addCancelByEscape(frame);
        ACTION_SEND = new ActionAdapter("Send", "Send message (" + (ClientProperties.INSTANCE.isEnterSends()?"":"Ctrl-") + "Enter )",
                        new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                if (textPane.getText().trim().length() == 0)
                                    return;
                                Message message;
                                boolean first = true;
                                for (Contact contact : contactWrapper) {
                                    message = new MessageImpl(contact, true, textPane.getText());
                                    try {
                                        if (first) {
                                            addTextToHistoryPanel(message);
                                            first = false;
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
        composeUI();
    }

    /**
     * Find the window and open it.
     *
     * @param allContacts to find the window by
     * @param allGroups to find the window by
     * 
     * @return reference to the window
     */
    public static MessageGroupWindow openWindow(List<Contact> allContacts, List<Group> allGroups) {
        return new MessageGroupWindow(getContacts(allContacts, allGroups));
    }

    /**
     * Calculates optimum name for the group.
     * @param allContacts involved
     * @param allGroups  involved
     * @return name
     */
    public static String getGroupName(java.util.List<Contact> allContacts, java.util.List <Group> allGroups) {
        for (Group group : allGroups) {
            // remove blanks
            if (group.size()==0) {
                allGroups.remove(group);
            }
        }

        int globalSize = allContacts.size();
        for (Group group : allGroups) {
            globalSize += group.size();
        }

        boolean manyGroups =  allGroups.size()>1;
        boolean manyContacts =  allContacts.size()>1;
        boolean contactPresent =  allContacts.size()>0;
        boolean groupPresent =  allGroups.size()>0;
        if (!groupPresent && !contactPresent) {
            return "Empty";
        }
        if (!groupPresent && contactPresent) {
            return (manyContacts?"[G]":"") + allContacts.get(0).getDisplayName() + (manyContacts?"+ "+globalSize:"");
        }
        // By now - we at least have one group.
        return "[G] "+allGroups.get(0).getName() + ((contactPresent || manyGroups)?"+ ":" ") + globalSize;
    }

    /**
     * Simplifies the list down to the ordered ppl.
     * @param allContacts involved
     * @param allGroups  involved
     * @return short and simple list. 1 dimensional. no dups.
     */
    public static List <Contact> getContacts(List<Contact> allContacts, List <Group> allGroups) {
        // do the do
        List <Contact> result = new ArrayList<Contact>(allContacts.size() + allGroups.size());
        for (Contact contact : allContacts) {
            if (!result.contains(contact)) {
                result.add(contact);
            }
        }
        for (Group group : allGroups) {
            for (int i = 0; i < group.size(); i++) {
              Nameable contact = group.get(i);
                if (contact instanceof Contact && !result.contains(contact)) {
                    result.add((Contact) contact);
                }
            }
        }
        return result;
    }

    protected Component getButtons() {
        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 3));
        panel.setOpaque(false);

        panel.add(new BetterButton(ACTION_SEND));
        south.add(panel);
        return south;
    }

    protected Component getHistory() {
//        historyPane = new JTextPane();
        historyPane = new BetterTextPane();
        historyPane.setEditable(false);
//        historyPane.setContentType("text/html");
        String contactList="";
        boolean allOnline = true;
        for (Contact aContactWrapper : contactWrapper) {
            contactList += aContactWrapper + " ";
            if (allOnline && !aContactWrapper.getStatus().isOnline())
                allOnline = false;
        }
        appendHistoryText("The following contacts will receive your message: " + contactList);
        if (!allOnline)
            appendHistoryText("\nSome contacts are offline. Your message may be missed.\n");
        historyPane.setCaretPosition(historyPane.getDocument().getLength());

        if (historyPane.getFont().getSize() > 10)
            StyleConstants.setFontSize(ATT_GRAY, historyPane.getFont().getSize() - 1);
        final JScrollPane jScrollPane = new JScrollPane(historyPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane.invalidate();
        jScrollPane.validate();
        return jScrollPane;
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
    public void addTextToHistoryPanel(Message message) throws IOException {
        try {
            // now the actual message
//            Message messageOut = new MessageImpl(contactWrapper, toBuddy, message);
            //Main.getLogger().log(messageOut);
            appendHistoryText((ClientProperties.INSTANCE.isShowTime() ? TIME_FORMAT.format(new Date()) : ""),
                    (ContactWrapper) message.getContact(), (message.isAutoResponse()?"Automatic response: ":"") + message.getPlainText());
            Toolkit.getDefaultToolkit().beep();
        } catch (Exception e) {
            ErrorDialog.displayError(frame, "Error processing command.  Try again.\n"+e.getMessage(), e);
        }
    }

} // class MessageWindow