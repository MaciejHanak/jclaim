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
import com.itbs.aimcer.commune.weather.WeatherConnection;
import com.itbs.aimcer.gui.order.OrderEntryLog;
import com.itbs.aimcer.log.LoggerEventListener;
import com.itbs.aimcer.web.ServerStarter;
import com.itbs.gui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Main class for the JClaim.
 * Sets up UI screens, connections and loads properties.
 *
 * @author Alex Rass
 * @since Sep 9, 2004
 */
public class Main {
    static String TITLE = "JCLAIM";
    public static String VERSION = "Version: 5.10";
    public static final String URL_FAQ = "http://www.itbsllc.com/jclaim/User%20Documentation.htm";
    public static final String EMAIL_SUPPORT = "support@itbsllc.com";
    private static final String LICENSE = System.getProperty("client");
    public static final String DEBUG_INFO = "JDK: " + System.getProperty("java.version") + "\n"
                                           + "OS: " + System.getProperty("os.name") + "\n"
                                            + "Program " + Main.VERSION;
    public static final String ABOUT_MESSAGE =
            "Java Compliant Logging & Auditing Instant Messenger.\n" +
            DEBUG_INFO + "\n\n" +
            "This is a Messaging Client Program.\n" +
            "Provides a UI for connecting to IM Services.\n" +
            "Follows logging requirements for financial institutions.\n" +
            "\nhttp://www.jclaim.com\n" +
            "\nDeveloped by ITBS LLC, Copyright 2004 - 2008." +
            "\nAll rights reserved.\n" +
            "\nTo request a feature or submit a bug, visit 'Contact Us' section on the web site."+
           (LICENSE==null?"":"\n\nThis version is licensed to: " + LICENSE);

    private static final Logger log = Logger.getLogger(Main.class.getName());
    /** Settings file */
    private static final File CONFIG_FILE = new File(System.getProperty("user.home"), "jclaim.ini");
    /** Backup file */
    private static final File CONFIG_FILE_SAV = new File(System.getProperty("user.home"), "jclaim.sav");

    public static final Dimension INITIAL_SIZE = new Dimension(260, 600);

    private static JFrame motherFrame;
    private static java.util.List <Connection> connections = new CopyOnWriteArrayList<Connection>();
    public static LoggerEventListener logger;
    private GlobalEventHandler globalEventHandler;
    public static GlobalWindowHandler globalWindowHandler;
    private MessageForwarder messageForwarder;
    private static Main main;

    private PeopleScreen peopleScreen;
    private StatusPanel statusBar;
    
    public static GroupFactory standardGroupFactory = new GroupWrapperFactory();


    /**
     * Provides a way to set a forwarder. 
     * @param contact if null - removes the listeneers all together. Otherwise sets forwarding contact.
     */
    public static void setForwardingContact(Contact contact) {
        main.messageForwarder.setForwardContact(contact);
        if (contact!=null) { // update settings
            ClientProperties.INSTANCE.setForwardee(contact.toString());
        }
        for (Connection connection:Main.getConnections()) {
            if (contact==null) {
                connection.removeEventListener(main.messageForwarder);
            } else if (connection instanceof MessageSupport) {
                connection.addEventListener(main.messageForwarder);
            }
        }
    }

    public static boolean isForwarding() {
        return main.messageForwarder!=null && main.messageForwarder.getForwardContact()!=null;
    }

    static class GroupWrapperFactory implements GroupFactory {
        public GroupWrapperFactory() {
//            new Exception("creating gwf").printStackTrace();
        }

        public Group create(String group) {
            return GroupWrapper.create(group);
        }

        public Group create(Group group) {
            return GroupWrapper.create(group);
        }

        /**
         * Returns the reference to main static list.
         * Anyone who has sessions:
         *   should not have this be static, but session based hash.
         *   basically map the factory based on session.
         *
         * @return list of groups.
         */
        public GroupList getGroupList() {
            return groupList;
        }

        /**
         * Anyone who has sessions - should not have this be static, but session based hash.
         */
        private static GroupList groupList  = new GroupListImpl();

    }

    public static ContactFactory standardContactFactory = new ContactWrapperFactory();
    static class ContactWrapperFactory implements ContactFactory {
        public Contact create(Nameable buddy, Connection connection) {
            return ContactWrapper.create(buddy, connection);
        }

        public Contact create(String name, Connection connection) {
            return ContactWrapper.create(name, connection);
        }

        public Contact get(String name, Connection connection) {
            return ContactWrapper.get( name, connection);
        }
    }

//    private final static int SCREEN_PROPERTIES    = 2;
//    private static final String MENU_ACTION = "Action";
    /**
     * Starts the GUI.
     * Standard main.
     * @param args not used
     */
    public static void main(String[] args) throws Exception
    {
        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        // if all else fails: security manager to null in the first line in you code
        main  = new Main();
        System.setProperty("com.apple.macos.useScreenMenuBar", "true");
        System.out.println("" + System.getProperty("proxyHost") + System.getProperty("proxyPort") );
        loadProperties();
        LookAndFeelManager.setLookAndFeel(ClientProperties.INSTANCE.getLookAndFeelIndex());
        motherFrame = GUIUtils.createFrame(TITLE);
        motherFrame.setIconImage(ImageCacheUI.ICON_JC.getIcon().getImage());
        main.createGUI();
        main.peopleScreen = new PeopleScreen();
        motherFrame.getContentPane().setLayout(new BorderLayout());
        motherFrame.getContentPane().add(main.peopleScreen);
        main.statusBar = new StatusPanel();
        motherFrame.getContentPane().add(main.statusBar, BorderLayout.SOUTH);
        motherFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent event) {
                ClientProperties.INSTANCE.setWindowBounds(motherFrame.getBounds());
//                log.fine("Last window: " + motherFrame.getBounds());
                exit();
            }
        });
        main.globalEventHandler = new GlobalEventHandler();
        globalWindowHandler = new GlobalWindowHandler();
        main.messageForwarder = new MessageForwarder();

        globalWindowHandler.add(new WindowManager.Sounds());
        globalWindowHandler.add(new WindowManager.Individual());
        globalWindowHandler.add(new WindowManager.Tabbed());

        try {
            // addConnection what you loaded
            for (Connection connection : connections) {
                if (connection instanceof MessageSupport)
                    try {
                        Main.addConnection((MessageSupport) connection);
                        if (!connection.isLoggedIn() && connection.isAutoLogin())
                            connection.connect();
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "", e);
                    }
            }
        } catch (Exception e) {
            motherFrame.setVisible(true);
            Main.complain("Failed to load connection settings.\nPlease restore connections by hand.", e);
        }
        motherFrame.setVisible(true);
        if (ClientProperties.isFirstTimeUse() || connections.size() == 0) {
            // todo do first time stuff
            // show disclaimer accept screen

            // show add connection dialog
            new LoginPanel(null).setVisible(true);
        }
    }

    public static PeopleScreen getPeoplePanel() {
        return main.peopleScreen;
    }
    public static StatusPanel getStatusBar() {
        return main.statusBar;
    }

    public static List <Connection>getConnections() {
        return connections;
    }

    public static JFrame getFrame() {
        return motherFrame;
    }

    public static void setTitle(String message) {
        motherFrame.setTitle(TITLE + " - " + message);
        showTooltip(message);
    }

    /**
     * Creates the guts of the window and populate motherFrame with it.
     */
    public void createGUI() {
        boolean firstRun = motherFrame.getContentPane().getComponentCount() == 0;
        if (firstRun) {
            JPanel topPanel = new JPanel(new BorderLayout());
            motherFrame.getContentPane().add(topPanel);
            if (ClientProperties.INSTANCE.getWindowBounds() == null)
                motherFrame.setSize(INITIAL_SIZE);//motherFrame.pack();
            else
                motherFrame.setBounds(ClientProperties.INSTANCE.getWindowBounds());
            motherFrame.setJMenuBar(MenuManager.getMenuBar());
            TrayAdapter.create(ClientProperties.INSTANCE.isUseTray(), motherFrame, ImageCacheUI.ICON_JC.getIcon(), "JClaim");
            WindowSnapper.instance().setEnabled(ClientProperties.INSTANCE.isSnapWindows());
        }
    }

    public static void addConnection(MessageSupport connection) throws Exception {
        if (logger == null) { // on 1st addConnection:
            logger = new LoggerEventListener(ClientProperties.INSTANCE.getLogPath());
            final WeatherConnection weather = new WeatherConnection();
            weather.setProperties(ClientProperties.INSTANCE);
            weather.assignContactFactory(standardContactFactory);
            weather.assignGroupFactory(standardGroupFactory);
            connections.add(weather);
            if (ClientProperties.INSTANCE.isShowWeather())
                weather.connect();
            ServerStarter.update(); // bring the web server up now, if enabled.
            ContactListModel.setConnection(weather);
        }
        connection.addEventListener(main.globalEventHandler);
        main.globalWindowHandler.addConnection(connection);
        connection.addEventListener(logger); // if logger is before MW, we see it log the incoming msg first
        JList list = main.peopleScreen.getList();
        connection.addEventListener((ConnectionEventListener) list.getModel()); // is also self-added
        connection.addEventListener(OrderEntryLog.getInstance());
        if (ClientProperties.INSTANCE.isEnableOrderEntryInSystem())
            if (ClientProperties.INSTANCE.isShowOrderEntry())
                connection.addEventListener(OrderEntryLog.getInstance());
            else
                connection.removeEventListener(OrderEntryLog.getInstance());
        MenuManager.addConnection(connection);
        if (!connections.contains(connection)) {
            connections.add(connection); // as long as it logged in ok
        }
    }

    static void exit() {
        try {
            motherFrame.setVisible(false);
            motherFrame.dispose();
            if (connections != null)
                for (Connection connection : connections) {
                    if (connection != null && connection.isLoggedIn())
                        connection.disconnect(true);
                }
            saveProperties();
            Thread.yield();
        } finally {
            System.exit(0);
        }
    }


    public static synchronized void loadProperties() {
        // load data
        if (CONFIG_FILE.exists() && CONFIG_FILE.isFile() && CONFIG_FILE.length() > 0) {
            Object data;
            XMLDecoder d = null;
            try {
/*
                boolean newFormat = true;
                RandomAccessFile raf = new RandomAccessFile(configFile, "r");
                if (raf.length()>0) {
                    newFormat = raf.readChar() != '<';
                }
                raf.close();
                if (newFormat)
*/
                d = new XMLDecoder(new GZIPInputStream(new FileInputStream(CONFIG_FILE)));
/*
                else
                    d = new XMLDecoder(new FileInputStream(configFile));
*/
                data = d.readObject();
                ClientProperties.setInstance((ClientProperties)data);

                try {
                    while (true) {
                        data = d.readObject();
                        if (data != null) {
                            ((Connection) data).setProperties(ClientProperties.INSTANCE);
                            ((Connection) data).assignGroupFactory(standardGroupFactory);
                            ((Connection) data).assignContactFactory(standardContactFactory);
                            getConnections().add((MessageSupport) data);
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException  e) {
                    // no care
                }
            } catch (IOException e) {
                if (e.getMessage().startsWith("Not in ") && e.getMessage().endsWith(" format")) {
                    Main.complain("File is corrupt.\n To cure the problem, change a setting.  Your setting will be lost, but the error will be fixed.", e);
                } else {
                    Main.complain("Failed to load settings.\n" + e.getMessage(), e);
                }
            } catch (Exception e) {
                Main.complain("Failed to load settings\n" + e.getMessage(), e);
            } finally {
                if (d!=null)
                    d.close();
            }
        } else {
            ClientProperties.setFirstTimeUse(true);
        }
    }

    public static synchronized void saveProperties() {
        try {
            
            CONFIG_FILE_SAV.delete();
            final OutputStream out = new GZIPOutputStream(new FileOutputStream(CONFIG_FILE_SAV));
            XMLEncoder e = new XMLEncoder(out);
            e.writeObject(ClientProperties.INSTANCE);

            // addConnection what you loaded
            if (connections.size() > 1) {
                for (Connection connection : connections) {
                    if (connection instanceof MessageSupport) {
                        if (connection!=null)
                            e.writeObject(connection);
                    }
                }
            }
            e.flush();
            e.close();
            out.flush();
            out.close();
            Thread.yield();
            // save, rename to old.
            if (!CONFIG_FILE.delete()) { log.severe("Failed to delete " + CONFIG_FILE);  Main.complain("Failed to delete old config file."); }
            if (!CONFIG_FILE_SAV.renameTo(CONFIG_FILE)) { log.severe("Failed to rename " + CONFIG_FILE_SAV + " to " + CONFIG_FILE); Main.complain("Failed to rename config file."); }
        } catch (Exception ex) {
            Main.complain("Failed to save config file.", ex);
        }
    }

    public static boolean isMyself(String talkingTo, String medium, String as) {
        for (Connection connection: Main.getConnections()) {
            if (connection.getServiceName().equals(medium)
                    && connection  instanceof MessageSupport
                    && ((MessageSupport) connection).getUserName().equals(talkingTo)) {
                return true;
            }
        }
        return false;
    }
    public static ContactWrapper findContact(String talkingTo, String medium, String as) {
        ContactWrapper wrapper = null;
        for (int i = 0; i < Main.getConnections().size() && wrapper == null; i++) {
            if (Main.getConnections().get(i).getServiceName().equals(medium)
                    && Main.getConnections().get(i).getUser().getName().equals(as)) {
                wrapper = ContactWrapper.get(talkingTo, Main.getConnections().get(i));
            }
        }
        return wrapper;
    }

    public static void waitCursor() {
        if (motherFrame != null)
            motherFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    }

    public static void normalCursor() {
        if (motherFrame != null)
            motherFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
    boolean isDisplayable() {
        return motherFrame!=null && motherFrame.isDisplayable();
    }

    public static LoggerEventListener getLogger() {
        return logger;
    }

    public static void complain(String message) {
        JOptionPane.showMessageDialog(motherFrame, message, "Error:", JOptionPane.ERROR_MESSAGE);
    }

    public static void complain(String message, Throwable exception) {
        if (exception == null)
            complain(message);
        else
            ErrorDialog.displayError(motherFrame, message, exception);
    }

    /**
     * Shows bubble.
     * @param caption of the bubble
     * @param text for the bubble
     */
    public static void showTooltip(String caption, String text) {
        TrayAdapter.showBubble(caption, text);
    }
    /**
     * Shows bubble.
     * @param text for the bubble
     */
    public static void showTooltip(String text) {
        showTooltip(null, text);
    }
}
