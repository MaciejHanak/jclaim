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
import com.itbs.aimcer.commune.Connection;
import com.itbs.aimcer.commune.ConnectionEventListener;
import com.itbs.aimcer.gui.order.OrderEntryLog;
import com.itbs.aimcer.web.ServerStarter;
import com.itbs.gui.*;
import com.itbs.util.SoundHelper;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Iterator;

/**
 * Used for editing program's properties.
 *
 * Uses multiple tabs for organization.
 * 
 * @author Alex Rass
 * @since Sep 9, 2004
 */
final public class PropertiesDialog extends JDialog implements ActionListener {
//    private static final Dimension PROPERTIES_SIZE = new Dimension(400, 300);;
    private final JCheckBox hide = new JCheckBox("", ClientProperties.INSTANCE.isHideOffline());
    private final JCheckBox hideGroups = new JCheckBox("", ClientProperties.INSTANCE.isHideEmptyGroups());
    private final JCheckBox easyOpen = new JCheckBox("", ClientProperties.INSTANCE.isEasyOpen());
    private final JCheckBox forceFront = new JCheckBox("", ClientProperties.INSTANCE.isForceFront());
    private final JCheckBox useAlerts = new JCheckBox("", ClientProperties.INSTANCE.isUseAlert());
    private final JCheckBox colorMessages = new JCheckBox("", ClientProperties.INSTANCE.isColorMessages());
    private final JCheckBox showCharCounter = new JCheckBox("", ClientProperties.INSTANCE.isShowCharCounter());
    private final JCheckBox showPictures = new JCheckBox("", ClientProperties.INSTANCE.isShowPictures());
    private final JCheckBox enterSends = new JCheckBox("", ClientProperties.INSTANCE.isEnterSends());
    private final JCheckBox showTime = new JCheckBox("", ClientProperties.INSTANCE.isShowTime());
    private final JCheckBox ignoreSystem = new JCheckBox("", ClientProperties.INSTANCE.isIgnoreSystemMessages());
    private final JCheckBox iamAway = new JCheckBox("", ClientProperties.INSTANCE.isIamAway());
    private final JCheckBox sortContactList = new JCheckBox("", ClientProperties.INSTANCE.isSortContactList());
    private final JTextComponent iamAwayMessage = new BetterTextField(ClientProperties.INSTANCE.getIamAwayMessage().trim());
    private final JTextComponent disclaimer = new BetterTextField(ClientProperties.INSTANCE.getDisclaimerMessage().trim());
    private final JTextComponent disclaimerDelay = new BetterTextField(""+(ClientProperties.INSTANCE.getDisclaimerInterval()/1000/60));
    private final JTextComponent scrollBackSize = new BetterTextField(""+ClientProperties.INSTANCE.getDisplayBuffer());
    private final JTextComponent fontSize = new BetterTextField(""+ClientProperties.INSTANCE.getFontSize());
    private final JCheckBox showStatusbar = new JCheckBox("", ClientProperties.INSTANCE.isStatusbarAlwaysVisible());
    private final JCheckBox showWeather = new JCheckBox("", ClientProperties.INSTANCE.isShowWeather());
    private final JTextComponent weather = new BetterTextField(ClientProperties.INSTANCE.getWeatherZipCodes().trim());
    private final JCheckBox showOrderEntry = new JCheckBox("", ClientProperties.INSTANCE.isShowOrderEntry());
    private final JCheckBox orderCausesShowManageScreen = new JCheckBox("", ClientProperties.INSTANCE.isOrderCausesShowManageScreen());
    private final JCheckBox allowCommissionEntry = new JCheckBox("", ClientProperties.INSTANCE.isAllowCommissionEntry());
    private final JCheckBox spellCheck = new JCheckBox("", ClientProperties.INSTANCE.isSpellCheck());
    private final JCheckBox spellCheckAllowSlang = new JCheckBox("", ClientProperties.INSTANCE.isSpellCheckAllowSlang());
    private final JComboBox lookAndFeelIndex = LookAndFeelManager.getLookAndFeelCombo(ClientProperties.INSTANCE.getLookAndFeelIndex());
    private final JCheckBox serverEnable = new JCheckBox("", ClientProperties.INSTANCE.isHTTPServerEnabled());
    private final JTextComponent serverPort = new BetterTextField(""+ClientProperties.INSTANCE.getHTTPServerPort());
    private final JTextComponent serverFolder = new BetterTextField(""+ClientProperties.INSTANCE.getDownloadFolder());
    private final JCheckBox showWebIcons = new JCheckBox("", ClientProperties.INSTANCE.isShowWebIcons());
    private final JCheckBox useTray = new JCheckBox("", ClientProperties.INSTANCE.isUseTray());
    private final JCheckBox snapWindows = new JCheckBox("(also limits to 1 monitor)", ClientProperties.INSTANCE.isSnapWindows());
    private final JTextComponent disconnectRetries = new BetterTextField(""+ClientProperties.INSTANCE.getDisconnectCount());
    private final JTextComponent ipQuery = new BetterTextField(ClientProperties.INSTANCE.getIpQuery().trim());

    private final JCheckBox allowSound = new JCheckBox("", ClientProperties.INSTANCE.isSoundAllowed());
//    private final JCheckBox allowSoundSend = new JCheckBox("", ClientProperties.INSTANCE.isSoundSend());
//    private final JCheckBox allowSoundReceive = new JCheckBox("", ClientProperties.INSTANCE.isSoundReceive());
    private final JCheckBox allowSoundIdle = new JCheckBox("", ClientProperties.INSTANCE.isSoundIdle());
    private final JTextComponent beepDelay = new BetterTextField(""+ClientProperties.INSTANCE.getBeepDelay());

    private final SoundProperty soundReceived = new SoundProperty(ClientProperties.INSTANCE.getSoundReceive());
    private final SoundProperty soundSend = new SoundProperty(ClientProperties.INSTANCE.getSoundSend());
    private final SoundProperty soundNewWindow = new SoundProperty(ClientProperties.INSTANCE.getSoundNewWindow());

    private final RadioProperty windowingInterface = new RadioProperty(
            new String[][] {
                    {"Windows", "Interface where each message is it's own window"},
                    {"Tabbed", "Interface where each message is a tab in a larger window"},
            }, ClientProperties.INSTANCE.getInterfaceIndex());

    static class RadioProperty extends JPanel {
        JRadioButton[] buttons;

        RadioProperty(String[] []labels, int startIndex) {
            super(new FlowLayout(FlowLayout.LEFT, 0, 0));
            setOpaque(false);
            ButtonGroup bg = new ButtonGroup();
            buttons = new JRadioButton[labels.length];
            for (int i = 0; i < labels.length; i++) {
                buttons[i] = new JRadioButton(labels[i][0]);
                buttons[i].setToolTipText(labels[i][1]);
                buttons[i].setOpaque(false);
                if (startIndex==i)
                    buttons[i].setSelected(true);
                bg.add(buttons[i]);
                add(buttons[i]);
            }

            if (startIndex<0 || startIndex>1) // if out of range - set to min one.
                buttons[0].setSelected(true);
        }

        int getResult() {
            for (int i = 0; i < buttons.length; i++) {
                if (buttons[i].isSelected()) return i;
            }
            return -1; // should never happen, but...
        }
    }
    static class SoundProperty extends JPanel {
        JRadioButton none = new JRadioButton();
        JRadioButton beep = new JRadioButton();
        JRadioButton sound = new JRadioButton();
        FileChooserButton file = new FileChooserButton(this);

        SoundProperty(String in) {
            super(new FlowLayout(FlowLayout.LEFT, 0, 0));
            setOpaque(false);
            ButtonGroup bg = new ButtonGroup();
            bg.add(none);
            bg.add(beep);
            bg.add(sound);
            none.setToolTipText("None");
            none.setOpaque(false);
            add(none);
            beep.setToolTipText("System beep");
            beep.setOpaque(false);
            add(beep);
            sound.setToolTipText("WAV file");
            sound.setOpaque(false);
            add(sound);

            if ("".equals(in)) none.setSelected(true);
            else if ("1".equals(in)) beep.setSelected(true);
            else {
                File location = new File(in);
                if (location.exists()) {
                    file.setFileName(location);
                    sound.setSelected(true);
                } else {
                    beep.setSelected(true);
                }
            }

            sound.addChangeListener(new TurnOffDependents(new JComponent[] {file}));
            file.setEnabled(sound.isSelected());
            file.setFont(file.getFont().deriveFont(ClientProperties.INSTANCE.getFontSize()-4));
            file.setMargin(new Insets(3,3,3,3));
            file.setFileFilter(SoundHelper.filter);
            add(file);
        }

        String getResult() {
            if (none.isSelected()) return "";
            else if (beep.isSelected()) return "1";
            else if (sound.isSelected()) return file.getFile().getAbsolutePath();
            else return "1";
        }
    }

    private void makeNumberic(JTextComponent tc) {
        String text = tc.getText();
        tc.setDocument(new BetterTextField.NumberDocument());
        tc.setText(text);
    }
    public PropertiesDialog(JFrame parent) {
        super(parent, "Settings", true);
        makeNumberic(disconnectRetries);
        makeNumberic(fontSize);
        makeNumberic(scrollBackSize);
        makeNumberic(disclaimerDelay);
        makeNumberic(serverPort);

        populateThis(getContentPane());
        GUIUtils.addCancelByEscape(this);
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                PropertiesDialog.this.requestFocus();
//                new BackgroundClassLoader(); // start loading classes after all paining is notifyDone
//            }
//        });
    } // Constr

    private void populateThis(Container pane) {
        pane.setLayout(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();
        JComponent props;
        JPanel spacer;

        //  C O N T A C T    L I S T
        props = new JPanel(new GridLayout(0, 2));
        props.setOpaque(false);
        props.add(getLabel("Hide Offline Contacts: ", "Hides anyone who is offline from the list", hide));
        props.add(hide);
        props.add(getLabel("Hide Empty Groups: ", "Hides empty groups (only when 'Hide Offline' is checked)", hideGroups));
        props.add(hideGroups);
        props.add(getLabel("Sort Contacts: ", "Sort entire list, groups and contacts within.", sortContactList));
        props.add(sortContactList);
        props.add(getLabel("Status Bar: ", "Makes status bar always visible", showStatusbar));
        props.add(showStatusbar);
        props.add(getLabel("Show Weather: ", "Select if you want the weather to show", showWeather));
        props.add(showWeather);
        props.add(getLabel("Weather (Zip Codes): ", "List the zip codes you want the weather for (space separated).", weather));
        props.add(new ThinPanel(weather));
        showWeather.addActionListener(new TurnOffDependents(new JComponent[] {weather}));
        props.add(getLabel("Snap to screen's edge: ", "Turn off for multi-display systems.", snapWindows));
        props.add(snapWindows);
        props.add(getLabel("Tray Icon: ", "Will show a tray icon.", useTray));
        props.add(useTray);
        props.add(getLabel("Skin: ", "Defined the way the program should look.", lookAndFeelIndex));
        props.add(lookAndFeelIndex);
        props.add(getLabel("Retry Connecting: ", "How may times to try and reconnect.  Defaults to 3.", disconnectRetries));
        props.add(new ThinPanel(disconnectRetries, new JLabel("times.")));
        props.add(getLabel("Away from PC: ", "Use to signify that you have stepped away for a while.", iamAway));
        props.add(iamAway);
        props.add(getLabel("Away Message: ", "Use notify others why you are away.", iamAwayMessage));
        props.add(iamAwayMessage);
        props.add(getLabel("Default Interface: ", "Defines where messages show up.", windowingInterface));
        props.add(windowingInterface);

        spacer = new GradientPanel(new BorderLayout());
        spacer.add(props, BorderLayout.NORTH);
        tabbedPane.addTab("Contact List", spacer);

        //  M E S S A G E S
        props = new JPanel(new GridLayout(0, 2));
        props.setOpaque(false);
        props.add(getLabel("De-iconify messages: ", "Restore the message dialogs when message is received", easyOpen));
        props.add(easyOpen);
        props.add(getLabel("Bring to front: ", "Bring the message dialogs to the front when message is received", forceFront));
        props.add(forceFront);
        props.add(getLabel("Flash Window: ", "Alerts of new messages by flashing the window", useAlerts));
        props.add(useAlerts);
//        props.add(new ThinPanel(useAlerts, new JLabel("(Unstable)")));
        props.add(getLabel("Color Messages: ", "Use colors in message dialogs to improve readability", colorMessages));
        props.add(colorMessages);
        props.add(getLabel("Display Character Count: ", "Show character count while you type", showCharCounter));
        props.add(showCharCounter);
        props.add(getLabel("Display Pictures: ", "Show contact's picture", showPictures));
        props.add(showPictures);
        props.add(getLabel("Enter Sends: ", "Use Enter to send messages.  (Ctrl-Enter always works)", enterSends));
        props.add(enterSends);
        props.add(getLabel("Show Time: ", "Display when a message was sent", showTime));
        props.add(showTime);
        props.add(getLabel("Spell Check: ", "Select if you want your text checked", spellCheck));
        props.add(spellCheck);
        props.add(getLabel("Allow Slang: ", "Spell checker allows slang tems", spellCheckAllowSlang));
        props.add(spellCheckAllowSlang);
        spellCheck.addActionListener(new TurnOffDependents(new JComponent[] {spellCheckAllowSlang}));
        if (ClientProperties.INSTANCE.isEnableOrderEntryInSystem()) {
            props.add(getLabel("Show Order Entry: ", "Select if you want the enter and send orders", showOrderEntry));
            props.add(showOrderEntry);
            props.add(getLabel("Auto Show Order Entry: ", "Will cause Order Entry to popup when Order buton is used", orderCausesShowManageScreen));
            props.add(orderCausesShowManageScreen);
            props.add(getLabel("Show Commission Field: ", "Will cause Order Entry to show Commission field", allowCommissionEntry));
            props.add(allowCommissionEntry);
        }
        props.add(getLabel("Font Size: ", "How big to make the list.  Defaults to 10.", fontSize));
        props.add(new ThinPanel(fontSize, new JLabel("pt.")));
        props.add(getLabel("History Size: ", "How long is the histoty list.  1 - 10.", scrollBackSize));
        props.add(new ThinPanel(scrollBackSize, new JLabel("kb")));
        props.add(getLabel("Disclaimer Message: ", "Text to be sent to the other party", disclaimer));
        props.add(disclaimer); // props.add(new JScrollPane(disclaimer));
        props.add(getLabel("Disclaimer Delay: ", "Minimum time between disclaimers.", disclaimerDelay));
        props.add(new ThinPanel(disclaimerDelay, new JLabel("min")));
        props.add(getLabel(" Ignore System Mesasges: ", "Will stop displaying system messages", ignoreSystem));
        props.add(ignoreSystem);
        spacer = new GradientPanel(new BorderLayout());
        spacer.add(props, BorderLayout.NORTH);

        tabbedPane.addTab("Messages", spacer);

        //  R E M O T E    A C C E S S
        props = new JPanel(new GridLayout(0, 2));
        props.setOpaque(false);
        props.add(getLabel("Web Server Enabled:", "Enable web server for your session http://machine:port", serverEnable));
        props.add(serverEnable);
        props.add(getLabel("Web Server Port:", "Which port to enable web server on", serverPort));
        props.add(new ThinPanel(serverPort));
        props.add(getLabel("Web Server Folder:", "Which folder would you like shared (blank for none)", serverFolder));
        props.add(serverFolder);
        props.add(getLabel("Display Icons: ", "Show icons in the list", showWebIcons));
        props.add(showWebIcons);
        props.add(getLabel("IP Query: ", "Command to get machine's IP address", ipQuery));
        props.add(ipQuery);
        serverEnable.addActionListener(new TurnOffDependents(new JComponent[] {serverPort, serverFolder, showWebIcons}));
//      .addActionListener(new TurnOffDependents(new JComponent[] {}));

        spacer = new GradientPanel(new BorderLayout());
        spacer.add(props, BorderLayout.NORTH);
        tabbedPane.addTab("Remote Access", spacer);


        // S O U N D
        props = new JPanel(new GridLayout(0, 2));
        props.setOpaque(false);
        props.add(getLabel("Allow Sound: ", "Allow sound when messages are sent and received", allowSound));
        props.add(allowSound);
        props.add(getLabel("No Sound Delay: ", "How long to wait after last beep (s.)", beepDelay));
        props.add(new ThinPanel(beepDelay, new JLabel("sec.")));
//        props.add(getLabel("Sound on send: ", "Allow sound when messages are received", allowSoundSend));
//        props.add(allowSoundSend);
//        props.add(getLabel("Sound on receive: ", "Allow sound when messages are sent", allowSoundReceive));
//        props.add(allowSoundReceive);
        props.add(getLabel("Sound when away: ", "Allow sound when connection is set as away", allowSoundIdle));
        props.add(allowSoundIdle);
//        props.add(getLabel("On disconnect: ", "Sound when connection is lost", soundDisconnect));
//        props.add(soundDisconnect);
        props.add(getLabel("Click below to play   ", ""));
        props.add(new JLabel("None / Beep / WAV File"));
        JLabel label = getSoundLabel("On Send: ", "Sound when message is received", soundSend);
        props.add(label);
        props.add(soundSend);
        props.add(getSoundLabel("On receive: ", "Sound when message is received", soundReceived));
        props.add(soundReceived);
        props.add(getSoundLabel("On new window: ", "Sound when new message window is open", soundNewWindow));
        props.add(soundNewWindow);

        allowSound.addActionListener(new TurnOffDependents(new JComponent[] {beepDelay, soundSend, soundReceived, allowSoundIdle}));
        spacer = new GradientPanel(new BorderLayout());
        spacer.add(props, BorderLayout.NORTH);
        tabbedPane.addTab("Sound", spacer);

        // Final add: tabbed pane.
        pane.add(tabbedPane);

        disclaimer.setEditable(!ClientProperties.INSTANCE.isDisclaimerLocked());
        disclaimerDelay.setEditable(!ClientProperties.INSTANCE.isDisclaimerLocked());

        props = new JPanel(new BorderLayout());
        props.add(getLabel("Please Note: Some settings require an application restart.", ""), BorderLayout.CENTER);

        JPanel buttonPane = new JPanel();
        JButton button =  new JButton("Done");
        button.setMnemonic('D');
        button.addActionListener(this);
        buttonPane.add(button);
        button =  new JButton("Cancel");
        button.setMnemonic('C');
        button.addActionListener(this);
        buttonPane.add(button);
        props.add(buttonPane, BorderLayout.SOUTH);
        pane.add(props, BorderLayout.SOUTH);
//        setSize(PROPERTIES_SIZE);
        pack();
        setLocation(260, 300);
    }

    private JLabel getSoundLabel(String caption, String toolTip, final SoundProperty soundProperty) {
        JLabel label = getLabel(caption, toolTip);
        label.addMouseListener(new MouseAdapter(){
            public void mouseReleased(MouseEvent e) {
                SoundHelper.playSound(soundProperty.getResult());
            }
        });
        return label;
    }

    static JLabel getLabel(String caption, String tooltip) {
        JLabel label = new JLabel(caption, JLabel.RIGHT);
        label.setToolTipText(tooltip);
        return label;
    }

    static JLabel getLabel(String caption, String tooltip, JComponent comp) {
        JLabel label = getLabel(caption, tooltip);
        comp.setToolTipText(tooltip);
        comp.setOpaque(false);
        return label;
    }

    private void setControlsEnabled(boolean value) {
//        login.setEnabled(value);
//        name.setEnabled(value);
//        password.setEnabled(value);
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e) {
        if ("Cancel".equals(e.getActionCommand())) {
            try {
                LookAndFeelManager.setLookAndFeel(ClientProperties.INSTANCE.getLookAndFeelIndex());
            } catch (Exception e1) {
                ErrorDialog.displayError(this, "Failed to skin settings. ", e1);
            }
            dispose();
            return;
        }

        setControlsEnabled(false);
        new Thread("pd - action") {
            public void run() {
                doAction();
            }
        }.start();
        setVisible(false);
    }

    private void doAction() {
        try {
            Main.waitCursor();
            ClientProperties.INSTANCE.setHideOffline(hide.isSelected());
            ClientProperties.INSTANCE.setHideEmptyGroups(hideGroups.isSelected());
            ClientProperties.INSTANCE.setDisclaimerMessage(disclaimer.getText());
            ClientProperties.INSTANCE.setDisclaimerInterval(disclaimerDelay.getText());
            ClientProperties.INSTANCE.setEasyOpen(easyOpen.isSelected());
            ClientProperties.INSTANCE.setForceFront(forceFront.isSelected());
            ClientProperties.INSTANCE.setUseAlert(useAlerts.isSelected());
            ClientProperties.INSTANCE.setColorMessages(colorMessages.isSelected());
            ClientProperties.INSTANCE.setShowCharCounter(showCharCounter.isSelected());
            ClientProperties.INSTANCE.setShowPictures(showPictures.isSelected());
            ClientProperties.INSTANCE.setFontSize(fontSize.getText());
            ClientProperties.INSTANCE.setDisplayBuffer(scrollBackSize.getText());
            ClientProperties.INSTANCE.setSound(allowSound.isSelected());
            ClientProperties.INSTANCE.setSoundNewWindow(soundNewWindow.getResult());
            ClientProperties.INSTANCE.setSoundSend(soundSend.getResult());
            ClientProperties.INSTANCE.setSoundReceive(soundReceived.getResult());
            ClientProperties.INSTANCE.setSoundIdle(allowSoundIdle.isSelected());
            ClientProperties.INSTANCE.setShowTime(showTime.isSelected());
            ClientProperties.INSTANCE.setBeepDelay(beepDelay.getText());
            ClientProperties.INSTANCE.setEnterSends(enterSends.isSelected());
            ClientProperties.INSTANCE.setIgnoreSystemMessages(ignoreSystem.isSelected());
            ClientProperties.INSTANCE.setIamAway(iamAway.isSelected());
            ClientProperties.INSTANCE.setSortContactList(sortContactList.isSelected());
            ClientProperties.INSTANCE.setIamAwayMessage(iamAwayMessage.getText());
            ClientProperties.INSTANCE.setWeatherZipCodes(weather.getText());
            ClientProperties.INSTANCE.setStatusbarAlwaysVisible(showStatusbar.isSelected());
            ClientProperties.INSTANCE.setShowWeather(showWeather.isSelected());
            ClientProperties.INSTANCE.setShowOrderEntry(showOrderEntry.isSelected());
            ClientProperties.INSTANCE.setSpellCheck(spellCheck.isSelected());
            ClientProperties.INSTANCE.setSpellCheckAllowSlang(spellCheckAllowSlang.isSelected());
            ClientProperties.INSTANCE.setOrderCausesShowManageScreen(orderCausesShowManageScreen.isSelected());
            ClientProperties.INSTANCE.setAllowCommissionEntry(allowCommissionEntry.isSelected());
            ClientProperties.INSTANCE.setLookAndFeelIndex(lookAndFeelIndex.getSelectedIndex());
            ClientProperties.INSTANCE.setHTTPServerEnabled(serverEnable.isSelected());
            ClientProperties.INSTANCE.setHTTPServerPort(serverPort.getText());
            ClientProperties.INSTANCE.setDownloadFolder(serverFolder.getText());
            ClientProperties.INSTANCE.setUploadFolder(serverFolder.getText()); // todo make it's own when properties screen is larger
            ClientProperties.INSTANCE.setShowWebIcons(showWebIcons.isSelected());
            ClientProperties.INSTANCE.setSnapWindows(snapWindows.isSelected());
            ClientProperties.INSTANCE.setUseTray(useTray.isSelected());
            ClientProperties.INSTANCE.setDisconnectCount(disconnectRetries.getText());
            ClientProperties.INSTANCE.setIpQuery(ipQuery.getText());
            ClientProperties.INSTANCE.setInterfaceIndex(windowingInterface.getResult());

            postCheck();
            dispose();
        } catch (Exception e1) {
            ErrorDialog.displayError(this, "Failed to addConnection. ", e1);
        } finally {
             Main.normalCursor();
            setControlsEnabled(true);
        }
    }

    public static void postCheck() {
        ClientProperties.INSTANCE.setWindowBounds(Main.getFrame().getBounds());
        SaveFile.saveProperties(); // just in case smth crashes, lets do this early.
        TrayAdapter.updateTrayIcon(ClientProperties.INSTANCE.isUseTray());
        MenuManager.setGlobalAway(ClientProperties.INSTANCE.isIamAway());
        Main.getStatusBar().setVisible(false); // this will display properly based on setting.
        WindowSnapper.instance().setEnabled(ClientProperties.INSTANCE.isSnapWindows());
        if (Main.getConnections()!=null) {
            for (Connection connection: Main.getConnections()) {
                if (!connection.isLoggedIn())
                    continue;
                // Used to throw CME, but now switched to a safer list implementation.
//                synchronized(connection) { // otherwise we get concurrent modification in while loop below
                    if (ClientProperties.INSTANCE.isShowOrderEntry())
                        connection.addEventListener(OrderEntryLog.getInstance());
                    else
                        connection.removeEventListener(OrderEntryLog.getInstance());
//                }
                Iterator iter = connection.getEventListenerIterator();
                while (iter.hasNext()) {
                    ConnectionEventListener listener =  (ConnectionEventListener) iter.next();
                    listener.statusChanged(connection);
                }
            }
//                if (connections != null && connections[0] != null && connections[0].isLoggedIn()) {
//                    goScreen(SCREEN_PEOPLE);
//                } // if connected
        }
        ServerStarter.update();
        Main.getPeoplePanel().update();
    }


    /**
     * Allows to easily disable all other buttons.
     */
    private static class TurnOffDependents implements ActionListener, ChangeListener {
        JComponent[] dependents;

        /**
         * Constructor. 
         * @param components list of components in the group.
         */
        public TurnOffDependents(JComponent[] components) {
            dependents = components;
        }

        public void stateChanged(ChangeEvent e) {
            if (e.getSource() instanceof AbstractButton)
            for (int i = 0; i < dependents.length; i++) {
                dependents[i].setEnabled(((AbstractButton) e.getSource()).isSelected());
            }
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof AbstractButton)
            for (int i = 0; i < dependents.length; i++) {
                dependents[i].setEnabled(((AbstractButton) e.getSource()).isSelected());
            }
        }
    }
}
