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
    private static TrayIcon trayIcon;
    private static boolean lastState;
    private static Alerter alerter;

    static {
        try {
            alerter = Alerter.newInstance();
        } catch (Throwable e) {
            System.out.println("Failed to load alerter");
            e.printStackTrace();
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
            e.printStackTrace();
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
                e.printStackTrace();
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
                e.printStackTrace();
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
                e.printStackTrace();
            }
        } // if alerter loaded
//            WinAlerter wal = new WinAlerter();
//            wal.alert(frame);
    }
}
