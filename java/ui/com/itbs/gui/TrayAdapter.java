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

import org.jdesktop.jdic.misc.Alerter;
import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a simple implementation of Tray Icon services for an application.
 * <p>
 * Minimizing frame will cause it to hide.<br>
 * Clicking the tray icon toggles the window.<br>
 * Icon services can be disabled<br>
 *
 * @author Alex Rass
 * @since Mar 25, 2005
 */
public class TrayAdapter {
    private static final Logger log = Logger.getLogger(TrayAdapter.class.getName());
    private static TrayIcon trayIcon;
    private static boolean lastState;
    private static Alerter alerter;

    static {
        try {
            alerter = Alerter.newInstance();
            log.info("Alter support: " + alerter.isAlertSupported());
        } catch (Throwable e) {
            log.log(Level.SEVERE, "Failed to load alerter", e);
        }

    }
    /**
     * Utility class.  No constructor.
     */
    private TrayAdapter() {
    }

    /**
     * Creates the tray icon and attaches minimization actions to the frame.
     * Call this once you create your frame.
     * It is up to the caller not to call this more than once per VM.
     * @param useTray if the icon is to be displayed and used
     * @param frame reference to window
     * @param icon to use
     * @param caption to display in tray.
     */
    public static void create(final boolean useTray, final Frame frame, Icon icon, String caption) {
        try {
            trayIcon = new TrayIcon(icon, caption);
            trayIcon.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    frame.setVisible(!frame.isVisible());
                    frame.setState(Frame.NORMAL);
                    frame.toFront();
                }
            });
            updateTrayIcon(useTray);
            frame.addWindowListener(new WindowAdapter() {
                /**
                 * Invoked when a window is iconified.
                 */
                public void windowIconified(WindowEvent e) {
                    if (lastState && trayIcon != null) {
                        frame.setVisible(false);
                    }
                }
            });
        } catch (NoClassDefFoundError e) {
            // failed to load libs, no big deal
            trayIcon = null;
            log.log(Level.SEVERE, "Failed to load Tray Adapter", e);
        }

    }

    /**
     * Convenience method.
     * @return true if mechanism is loaded and working
     */
    public static boolean isAvailable() {
        return trayIcon != null;
    }

    /**
     * Call to add or remove the tray icon based on usage boolean
     * @param useTray to use or not to use!
     */
    public static void updateTrayIcon(boolean useTray) {
        if (isAvailable() && lastState != useTray) {
            try {
                SystemTray tray = SystemTray.getDefaultSystemTray();
                if (useTray) {
                    tray.addTrayIcon(trayIcon);
                } else {
                    tray.removeTrayIcon(trayIcon);
                }
                lastState = useTray;
            } catch (UnsatisfiedLinkError e) {
                log.log(Level.SEVERE, "", e);
                trayIcon = null;
            }
        }
    }

    /**
     * Use to get to TrayIcon to change caption, change icons etc.
     * @return reference to TrayIcon
     */
    public static TrayIcon getIcon() {
        return trayIcon;
    }

    public static void showBubble(String caption, String text) {
        if (isAvailable() && lastState) {
            try {
                trayIcon.displayMessage(caption, text, TrayIcon.INFO_MESSAGE_TYPE);
            } catch (NullPointerException e) {
                // both strings are null
            } catch (UnsatisfiedLinkError e) {
                log.log(Level.SEVERE, "", e);
                trayIcon = null;
            }
        }
    }

    public static void alert(JFrame frame) {
        if (alerter!=null) {
            try {
                if (alerter.isAlertSupported()) {
                    alerter.alert(frame);
                }
            } catch (Throwable e) { // takes care of not found, no dlls etc
                log.log(Level.SEVERE, "Failed to Alert", e);
            }
        } // if alerter loaded
//            WinAlerter wal = new WinAlerter();
//            wal.alert(frame);
    }
}
