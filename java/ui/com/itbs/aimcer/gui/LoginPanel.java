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
import com.itbs.aimcer.bean.Contact;
import com.itbs.aimcer.bean.Message;
import com.itbs.aimcer.bean.Status;
import com.itbs.aimcer.commune.Connection;
import com.itbs.aimcer.commune.ConnectionEventAdapter;
import com.itbs.aimcer.commune.MessageSupport;
import com.itbs.gui.*;
import com.itbs.util.GeneralUtils;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.ConnectException;
import java.util.logging.Logger;

/**
 * This is a JDialog which is used to prompt for login information.
 * Provides a place to pick username, password, medium type and autologin property. 
 *
 * @author Alex Rass
 * @since Mar 25, 2004
 */
public final class LoginPanel extends JDialog implements ActionListener {
    private static final Logger log = Logger.getLogger(LoginPanel.class.getName());
    private static final String NAME = "Name: ";
    private static final String PASSWORD = "Password: ";
    private static final String SERVER = "Server: ";
    private static final String ADD = "Add Connection";
    private static final String SAVE = "Save";
    private final JTextField name, password, server;
    JLabel serverLabel;
    private JComboBox service;
    /** Login button */
    private JButton login;
    /** Auto-Login checkbox */
    JCheckBox autoLogin;
    /** Server override */
    JCheckBox serverOverride;

    /** Comment displayed on the login panel. */
    public static final String DISPLAYED_TEXT = "<HTML><FONT SIZE=2>Use login credential for selected service.<p>" +
                    "Only proceed to use this software if you have<br>read and agreed with our disclaimers and notes.<p><p>" +
                    "Note: Communications are logged locally.<p><p>" +
/*
                    "Disclaimer:<p>" +
                    "We advise that all the issues regarding the<br>" +
                    "legal and compliance adherence should be<br>" +
                    "addressed with your compliance office or<br>" +
                    "the appropriate parties.<br>" +
                    "We would be happy to provide them with any<br>" +
                    "pertaining information.<p><p>" +
*/
                    "For reporting problems or requesting features<br>" +
                    "use contact information from the website.<p><p>" +
                    Main.VERSION + "</FONT></HTML>";

    MessageSupport connRef;

    public LoginPanel(MessageSupport connection) {
//        main.setTitle("Login");
        super(Main.getFrame());
        setModal(true);
        JPanel panel = new GradientPanel(new BorderLayout());
        panel.setOpaque(false);
        Box inner = new Box(BoxLayout.Y_AXIS);
        connRef = connection;

        JPanel innerPanel = new JPanel(new GridLayout(3,2)); // 4 Login fields.
        innerPanel.setOpaque(false);

        innerPanel.add(new JLabel(NAME, JLabel.RIGHT)); // name
        name = new BetterTextField(connection==null?"":connection.getUserName(), 10);
        innerPanel.add(name);

        innerPanel.add(new JLabel(PASSWORD, JLabel.RIGHT)); // pw
        password = new BetterPasswordField(connection==null?"":connection.getPassword(), 10);
        innerPanel.add(password);

        serverLabel = new JLabel(SERVER, JLabel.RIGHT);
        innerPanel.add(serverLabel); // Server w/ Label
        server = new BetterTextField(connection==null?"":connection.getServerName(), 10);
        server.setEnabled(false); // start out with it off
        innerPanel.add(server);

        panel.add(ComponentFactory.getTopBar(connection==null?"Please login:":"Login Info"), BorderLayout.NORTH);
        inner.add(innerPanel);
        inner.add(getButtons(connection==null));
        inner.add(getSettings(connection));

        service = new JComboBox(ServiceProvider.getProviders());
        service.setEnabled(connection == null);
        service.setToolTipText("Connection type.");
        if (connection != null) {
            // do not convert to foreach since we need the index.  and it's all static anyway.
            for (int i = 0; i < ServiceProvider.getProviders().length; i++) { // find the right one
                if (connection.getClass().isAssignableFrom(ServiceProvider.getProviders()[i].classRef)) {
                    service.setSelectedIndex(i);
                    break;
                }
            }
        }
        inner.add(service);

        panel.add(inner);
        panel.add(ComponentFactory.getTopBar(DISPLAYED_TEXT), BorderLayout.SOUTH);
        panel.setBorder(new EtchedBorder());
        add(panel);
        // This is for developers. Makes life a little easier.
        if (connection == null && System.getProperty("uname") != null) {
            name.setText(System.getProperty("uname"));
            password.setText(System.getProperty("pw"));
        }
        pack();
        GUIUtils.moveToParentCenter(Main.getFrame(), this);
        GUIUtils.addCancelByEscape(this);
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                    name.requestFocus();
            }
        });
    }

    private JComponent getSettings(Connection connection) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        autoLogin = new JCheckBox("Auto login on startup");
        autoLogin.setOpaque(false);
        autoLogin.setSelected(connection==null || connection.isAutoLogin());
        panel.add(autoLogin);
        serverOverride = new JCheckBox("Server override");
        serverOverride.setOpaque(false);
//        serverOverride.setSelected(connection==null || connection.isAutoLogin());
        panel.add(serverOverride);
        serverOverride.addActionListener(new TurnOffDependents(new JComponent[] {server, serverLabel}));
        
        return panel;
    }


    private JPanel getButtons(boolean add) {
        if (login == null) {
            login = new BetterButton(new ActionAdapter(add?ADD:SAVE, this, 'l'));
        }
        JPanel panel = new JPanel(); // flow
        panel.setOpaque(false);
        panel.add(login);
        return panel;
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e) {
        setControlsEnabled(false);
        new Thread("Login") {
            public void run() {
                MessageSupport connection=null; // pick from list
                try {
                    Main.waitCursor();
                    if (connRef == null) {
                        connection = ((ServiceProvider)service.getSelectedItem()).getInstance();
                        connection.setProperties(ClientProperties.INSTANCE);
                        connection.assignGroupFactory(Main.standardGroupFactory);
                        connection.assignContactFactory(Main.standardContactFactory);
                        connection.setUserName(name.getText());  // set prior to menu add
                        connection.setPassword(password.getText());
                        if (server.isEnabled() && GeneralUtils.isNotEmpty(server.getText())) {
                            connection.setServerName(server.getText());
                        }
                        Main.addConnection(connection);
                    } else {
                        connection = connRef;
                    }
                    connection.setUserName(name.getText());  // set in case we already had a connection
                    connection.setPassword(password.getText());
                    if (server.isEnabled() && GeneralUtils.isNotEmpty(server.getText())) {
                        connection.setServerName(server.getText());
                    }
                    connection.setAutoLogin(autoLogin.isSelected());
                    connection.addEventListener(new LoginMonitor());

                    connection.disconnect(true);
                    Thread.yield();
                    connection.connect(); // start asynchronous connection going
                    dispose();
                } catch (ConnectException ex) {
                    JOptionPane.showMessageDialog(LoginPanel.this, "Could not log in.\nConnection with server timed out.\nPlease retry in a minute or so.", "Error:", JOptionPane.ERROR_MESSAGE);
                    if (connection!=null)
                        connection.disconnect(true);
                    setControlsEnabled(true);
                } catch (SecurityException ex) {
                    if (connection!=null)
                        connection.disconnect(true);
                    JOptionPane.showMessageDialog(LoginPanel.this, "Connectivity problems.\nCould not log in.\nCheck your username and password.\n" + ex.getMessage(), "Error:", JOptionPane.ERROR_MESSAGE);
                    password.requestFocus();
                    password.selectAll();
                    setControlsEnabled(true);
                } catch (Exception ex) {
                    connection.cancel();
                    setControlsEnabled(true);
                    ErrorDialog.displayError(LoginPanel.this, "Failed to add connection. ", ex);
                } finally {
                    Main.normalCursor();
//            setControlsEnabled(true);  // may still be connecting
                }
            }
        }.start();
    }

    private void setControlsEnabled(boolean value) {
        login.setEnabled(value);
        name.setEnabled(value);
        password.setEnabled(value);
        server.setEnabled(value && serverOverride.isSelected());
        service.setEnabled(value);
        autoLogin.setEnabled(value);
    }

    class LoginMonitor extends ConnectionEventAdapter {
        public boolean messageReceived(MessageSupport connection, Message message) {
            connectionLost(connection);
            return true;
        }

        public boolean emailReceived(MessageSupport connection, Message message) throws Exception {
            connectionLost(connection);
            return true;
        }

        public void connectionLost(Connection connection) {
            connection.removeEventListener(this);
            setControlsEnabled(true);
        }

        /**
         * Sent before connection is attempted
         *
         * @param connection in context
         */
        public void connectionInitiated(Connection connection) {
            setControlsEnabled(false);
        }

        public void connectionFailed(Connection connection, String message) {
            connection.removeEventListener(this);
            setControlsEnabled(true);
            password.requestFocus();
            password.selectAll();
            JOptionPane.showMessageDialog(LoginPanel.this, "Connectivity problems.\nCould not log in.\nCheck your username and password.", "Error:", JOptionPane.ERROR_MESSAGE);
        }

        public void connectionEstablished(Connection connection) {
            connection.removeEventListener(this);
            setControlsEnabled(true);
        }

        public void statusChanged(Connection connection, Contact contact, Status oldStatus) {
            connection.removeEventListener(this);
        }

        public void statusChanged(Connection connection) {
            connection.removeEventListener(this);
        }
    } // class LoginMonitor
}
