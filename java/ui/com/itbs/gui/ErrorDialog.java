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

package com.itbs.gui;

import com.itbs.aimcer.gui.Main;
import org.jdesktop.jdic.desktop.Desktop;
import org.jdesktop.jdic.desktop.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alex Rass
 * @since Feb 12, 2006
 */
public class ErrorDialog {
    private static final Logger log = Logger.getLogger(ErrorDialog.class.getName());
    /** stack trace indent */
    static final String INDENT = "        ";

    /**
     * Instructs the container to display an error screen.
     * This is a blocking call - it MUST NOT RETURN until the error screen has been dismissed.
     *
     * @param tip topmost component to tie to
     * @param defaultMessage Default error message displayed when the screen comes up
     * @param e actual error
     */
    public static void displayError(Component tip, String defaultMessage, Throwable e)
    {
        log.log(Level.SEVERE, defaultMessage, e);
        // fix messages
        if (e.getCause() != null)
            defaultMessage += " " + e.getCause().getMessage();
        else if (e.getMessage() != null)
            defaultMessage += " " + e.getMessage();
        if (e instanceof SQLException)
            defaultMessage += ((SQLException)e).getErrorCode() + ": " + ((SQLException)e).getSQLState();
        displayError(tip, defaultMessage,  getStackTrace(e));
    }

    /**
     * Instructs the container to display an error screen.
     * This is a blocking call - it MUST NOT RETURN until the error screen has been dismissed.
     *
     * @param tip topmost component to tie to
     * @param defaultMessage Default error message displayed when the screen comes up
     * @param detailedMessage More detailed error message
     */
    public static void displayError(Component tip, String defaultMessage, String detailedMessage)
    {
        // fix messages
        detailedMessage = defaultMessage + "\n\n" + detailedMessage;
        defaultMessage = "There was an unforeseen error during the execution"
                              + " of this program.\n\n"
                              + defaultMessage
                              + "\n\nPlease notify someone from our support of the error if it persists.";

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Message", getDisplayTextArea(defaultMessage) );
        tabbedPane.add("Details", getDisplayTextArea(detailedMessage));

//        JOptionPane optionPane = new JOptionPane(tabbedPane, JOptionPane.ERROR_MESSAGE);
//        final JDialog dialog = optionPane.createDialog(tip, "Error");
        final JDialog dialog = new JDialog(JOptionPane.getFrameForComponent(tip), "Error:", true);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(tabbedPane);
        dialog.getContentPane().add(getButtonPane(dialog, detailedMessage), BorderLayout.SOUTH);
//        dialog.setModal(true);
        dialog.pack();
        dialog.setVisible(true);
    }

    private static Component getButtonPane(final JDialog dialog, final String detailedMessage) {
        JPanel panel = new JPanel();
        JButton button;
        button = new JButton(new ActionAdapter("Ok", (String)null, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        }));
        panel.add(button);
        button = new JButton(new ActionAdapter("Email Support", (String)null, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final Message msg = new Message();
                java.util.List <String> list  = new ArrayList<String>();
                list.add(Main.EMAIL_SUPPORT); // todo must fix this!
                msg.setToAddrs(list);
                msg.setSubject("Error report");
                msg.setBody(Main.DEBUG_INFO   // todo must fix this!
                         + "\nError:" + detailedMessage
                );
                new Thread() {
                    public void run() {
                        try {
                            Desktop.mail(msg);
                        } catch (Throwable e1) {
                            log.log(Level.SEVERE, "", e1);
                            JOptionPane.showMessageDialog(dialog, "Failed to create an email.", "Error:", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }.start();
                dialog.dispose();
            }
        }));
        panel.add(button);
        return panel;
    }

    private static JScrollPane getDisplayTextArea(String detailedMessage) {
        // Sometimes the stack can be quite large.  We need to make it scrollable.
        JTextArea detailText = new JTextArea();
        detailText.setEditable(false);
        detailText.setText(detailedMessage);
        detailText.setCaretPosition(0);
        JScrollPane detailScrollPane = new JScrollPane(detailText);
        detailScrollPane.setBorder(BorderFactory.createEtchedBorder());
        detailScrollPane.setPreferredSize(new Dimension(550, 300));
//        detailScrollPane.setMinimumSize(new Dimension(400, 250));
        return detailScrollPane;
    }

    /**
     * Get the specified throwable's stack trace string
     * @param ex throwable which you like to get the stack trace
     * @return String the stack trace of the throwable ex.
     */
    public static String getStackTrace(Throwable ex)
    {
        StringBuffer buf = new StringBuffer();

        // append stack trace
        buf.append(buildStackTrace(ex));

        // append stack trace of root cause
        Throwable cause = ex.getCause();
        while (cause != null)
        {
            buf.append("Caused By: ");
            buf.append(buildStackTrace(cause));

            // get next cause
            cause = cause.getCause();
        }

        return buf.toString();
    }

    /**
     * Build the stack trace string for a throwable.  Use this instead of
     * printStackTrace() because the Sun implementation of printStackTrace() sometimes
     * chops off sections of the stack.  If we do it ourselves then we won't lose stack info.
     * @param th throwable which you like to get the stack trace
     * @return the stack trace of the throwable ex
     */
    private static String buildStackTrace(Throwable th)
    {
        StringBuffer buf = new StringBuffer();
        buf.append(th.getClass().getName());
        buf.append(": ");
        buf.append(th.getMessage());
        buf.append("\n");

        StackTraceElement[] stack = th.getStackTrace();
        for (StackTraceElement aStack : stack) {
            buf.append(INDENT);
            buf.append("at ");
            buf.append(aStack.toString());
            buf.append("\n");
        }

        return buf.toString();
    }
}
