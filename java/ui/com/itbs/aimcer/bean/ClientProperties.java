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

package com.itbs.aimcer.bean;

import com.itbs.util.ReadableRectangle;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Saved as a whole to maintain all preferences.
 *
 * @author Alex Rass
 * @since Sep 11, 2004
 */
public class ClientProperties implements ConnectionProperties {
    private static final Logger log = Logger.getLogger(ClientProperties.class.getName());
    /** Property field names. */
    private static final String PROP_DISCLAIMER = "disclaimer";
    private static final String PROP_DISCLAIMER_DELAY = "delay";
    private static final String PROP_ENABLE_ORDER_ENTRY_IN_SYSTEM = "enableorderentry";
    private static final String PROP_PROXY_HOST = "http.proxyHost";
    private static final String PROP_PROXY_PORT = "http.proxyPort";

    /** Idle delay in seconds before setting away */
    private int away;
    private int serviceIndex;
/*  This was used to manage TOC - when it was very unstable.
    private String aimServer[] = {
        "",
        "205.188.248.193",
        "205.188.248.194",
        "205.188.248.195",
        "205.188.248.196",
        "64.12.163.172",
        "64.12.163.173",
        "64.12.163.174",
        "64.12.163.175"
    };
*/
    private String logPath=File.separatorChar=='\\'?"c:\\AimLogs":"AimLogs";
    private static final String DEFAULT_PROXY_HOST = ""; // "All the messages are being logged."
    private static final int DEFAULT_PROXY_PORT = 8080;
    private static final String DISCLAIMER_DEFAULT=""; // "All the messages are being logged."
    private static final int DEFAULT_DISCLAIMER_INTERVAL = 20*60*1000;
    private static final int DEFAULT_FONT_SIZE = 10;
    private static final int DEFAULT_BEEP_DELAY = 5;
    private String proxyHost = getPresetOrDefault(PROP_PROXY_HOST, DEFAULT_PROXY_HOST);
    private int proxyPort = getPresetOrDefault(PROP_PROXY_PORT, DEFAULT_PROXY_PORT);
    private String disclaimerMessage = getPresetOrDefault(PROP_DISCLAIMER, DISCLAIMER_DEFAULT);
    private long disclaimerInterval = getPresetOrDefault(PROP_DISCLAIMER_DELAY, DEFAULT_DISCLAIMER_INTERVAL);
    private static boolean enableOrderEntryInSystem = getPresetOrDefault(PROP_ENABLE_ORDER_ENTRY_IN_SYSTEM, false);

    /** Tells if this is the first time this user has started the software. todo make use of this for disclaimers. */
    private static boolean firstTimeUse;
    private boolean statusbarAlwaysVisible = false;
    private boolean useAlert = false;
    private boolean hideOffline = true;
    private boolean showEmptyGroups = true;
    private boolean easyOpen = true;
    private boolean colorMessages = true;
    private boolean showCharCounter = true;
    private boolean showPictures = true;
    private boolean hideEmptyGroups = true;
    private boolean showTime = true;
    private boolean enterSends = true;
    private String timeFormat = "(hh:mm)";
    private int beepDelay = DEFAULT_BEEP_DELAY;
    /** Forces a window to front when message arrives */
    private boolean forceFront = true;
    private boolean ignoreSystemMessages = false;
    private int fontSize = DEFAULT_FONT_SIZE;
    private ReadableRectangle windowPosition;
    public static ClientProperties INSTANCE = new ClientProperties();
    private String ipQuery = "";
    private String iamAwayMessage = "";
    private boolean iamAway;
    private boolean autoDismissDialogs;
    private String weatherZipCodes = "10001";
    private boolean showWeather = true;
    private boolean showOrderEntry = false;
    private boolean allowCommissionEntry = false;
    private boolean orderCausesShowManageScreen = true;
    private boolean spellCheck = true;
    private boolean spellCheckAllowSlang = true;
    private int lookAndFeelIndex = -1;
    private boolean serverEnabled = false;
    private int serverPort = 2000;
    private boolean useTray = true;
    private boolean snapWindows = false;
    private Rectangle windowBounds;
    Map<String,ContactPreferences> buddyPreferences = new HashMap<String, ContactPreferences>();
    Map<String,GroupPreferences> groupPreferences = new HashMap<String, GroupPreferences>();

    private int disconnectCount = 3;

    private String lastFolder = "";
    private String uploadFolder = "";
    private String downloadFolder = "";
    private boolean showWebIcons = true;
    private int displayBuffer = 2;

    private boolean sound = true;
    private boolean soundIdle = true;
    private String soundReceive="1";
    private String soundSend="1";
    private String soundNewWindow="";


    private String databaseDriver = System.getProperty("DB_DRIVER");
    private String databaseURL = System.getProperty("DB_URL");
    private String databaseUsername = System.getProperty("DB_UN");
    private String databasePassword = System.getProperty("DB_PW");
    /** Prepared statement String for storing the DB data. */
    private String databaseStore = System.getProperty("DB_STORE");

    /**
     * Helper method.
     * Saves the problem with keeping to check if property is already set through -D
     * @param constantName name of the constant
     * @return true of line matches and property isn't already enforced
     */
    private static boolean isTimeToSetProperty(String constantName) {
        return System.getProperty(constantName) == null;
//        return line.startsWith(constantName) && System.getProperty(constantName) == null;
    }

    /**
     * Gets the looked up value or the defaults.
     * @param propName property name
     * @param defaultValue yes.
     * @return looked up value or the defaults.
     */
    private static  int getPresetOrDefault(String propName, int defaultValue) {
        if (System.getProperty(propName) == null)
            return defaultValue;
        return getInt(System.getProperty(propName), propName, defaultValue);
    }
    /**
     * Gets the looked up value or the defaults.
     * @param propName property name
     * @param defaultValue yes.
     * @return looked up value or the defaults.
     */
    private static boolean getPresetOrDefault(String propName, boolean defaultValue) {
        if (System.getProperty(propName) == null)
            return defaultValue;
        return getBoolean(System.getProperty(propName), defaultValue);
    }

    /**
     * Gets the looked up value or the defaults.
     * @param propName property name
     * @param defaultValue yes.
     * @return looked up value or the defaults.
     */
    private String getPresetOrDefault(String propName, String defaultValue) {
        if (System.getProperty(propName) == null)
            return defaultValue;
        return System.getProperty(propName);
    }


    /**
     * Helper method.  Gets long from a line.
     * @param value parse this
     * @param name of the fiels
     * @param defaultValue
     * @return value
     */
    private static int getInt(String value, String name, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.severe("Failed to load " + name + " from " + value);
            return defaultValue;
        }
    }

    /**
     * Helper method.  Gets long from a line.
     * @param value parse this
     * @param name of the value
     * @param defaultValue
     * @return value
     */
    private static long getLong(String value, String name, long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.severe("Failed to load " + name + " from " + value);
            return defaultValue;
        }
    }
    /**
     * Helper method.  Gets long from a line.
     * @param value parse this
     * @return value
     */
    private static boolean getBoolean(String value, boolean defaultValue) {
        if (value == null)
            return defaultValue;
        return Boolean.valueOf(value).booleanValue();
    }

    public int getAway() {
        return away;
    }

    public void setAway(int awayDelay) {
        away = awayDelay;
    }

    public boolean isHideOffline() {
        return hideOffline;
    }

    public void setHideOffline(boolean hideOfflineContacts) {
        hideOffline = hideOfflineContacts;
    }

    public long getDisclaimerInterval() {
        return disclaimerInterval;
    }

    public void setDisclaimerInterval(long interval) {
        if (isTimeToSetProperty(PROP_DISCLAIMER_DELAY))
            disclaimerInterval = interval;
    }

    public void setDisclaimerInterval(String text) {
        try {
            setDisclaimerInterval(Long.parseLong(text) * 1000 * 60);
        } catch (NumberFormatException e) {
            disclaimerInterval = 0;
        }
    }

    public boolean isDisclaimerLocked() {
        return System.getProperty(PROP_DISCLAIMER) != null;
    }

    public String getDisclaimerMessage() {
        return disclaimerMessage;
    }

    public void setDisclaimerMessage(String disclaimerMessage) {
        if (isTimeToSetProperty(PROP_DISCLAIMER))
            this.disclaimerMessage = "\n\n" + disclaimerMessage + "\n\n";
    }

    /**
     * Path describing superfolder of where the logs are stored.
     * @return folder
     */
    public File getLogPath() {
        return new File(INSTANCE.logPath);
    }

    public Rectangle getWindowPosition() {
        return windowPosition;
    }

    public void setWindowPosition(Rectangle windowPosition) {
        this.windowPosition = new ReadableRectangle(windowPosition);
    }

    public boolean isEasyOpen() {
        return easyOpen;
    }

    public void setEasyOpen(boolean easyOpen) {
        this.easyOpen = easyOpen;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }
    public void setFontSize(String fontSize) {
        this.fontSize = getInt(fontSize, "fontsize", DEFAULT_FONT_SIZE);
    }

    public int getServiceIndex() {
        return serviceIndex;
    }

    public void setServiceIndex(int serviceIndex) {
        this.serviceIndex = serviceIndex;
    }

    /**
     * Sends window to front.
     * @return true if so
     */
    public boolean isForceFront() {
        return forceFront;
    }

    public void setForceFront(boolean forceFront) {
        this.forceFront = forceFront;
    }

    //todo use somewhere
    public boolean isShowEmptyGroups() {
        return showEmptyGroups;
    }

    public void setShowEmptyGroups(boolean showEmptyGroups) {
        this.showEmptyGroups = showEmptyGroups;
    }

    // -----------------------   S O U N D   -------------------
    public boolean isSoundAllowed() {
        return sound;
    }

    public void setSound(boolean sound) {
        this.sound = sound;
    }

    /**
     * Sound when receiving a message.
     * @return path to file or "1"
     */
    public String getSoundReceive() {
        return soundReceive;
    }

    /**
     * Sound when receiving a message.
     * @param soundReceive path to file or "1"
     */
    public void setSoundReceive(String soundReceive) {
        this.soundReceive = soundReceive;
    }

    /**
     * Sound when sending a message.
     * @return path to file or "1"
     */
    public String getSoundSend() {
        return soundSend;
    }

    /**
     * Sound when sending a message.
     * @param soundSend path to file or "1"
     */
    public void setSoundSend(String soundSend) {
        this.soundSend = soundSend;
    }

    public String getSoundNewWindow() {
        return soundNewWindow;
    }

    public void setSoundNewWindow(String soundNewWindow) {
        this.soundNewWindow = soundNewWindow;
    }

    /**
     * Allow sound when connection is away.
     * @return true if sound is allowed
     */
    public boolean isSoundIdle() {
        return soundIdle;
    }

    public void setSoundIdle(boolean soundIdle) {
        this.soundIdle = soundIdle;
    }

    public int getBeepDelay() {
        return beepDelay;
    }

    public void setBeepDelay(int beepDelay) {
        this.beepDelay = beepDelay;
    }

    /**
     * In seconds.
     * @param beepDelay delay
     */
    public void setBeepDelay(String beepDelay) {
        int check = getInt(beepDelay, "beep delay", DEFAULT_BEEP_DELAY);
        if (check >= 0)
            this.beepDelay = check;
    }

    // -----------------------   S O U N D   -------------------

    public boolean isShowTime() {
        return showTime;
    }

    public void setShowTime(boolean showTime) {
        this.showTime = showTime;
    }

    public boolean isEnterSends() {
        return enterSends;
    }

    public void setEnterSends(boolean enterSends) {
        this.enterSends = enterSends;
    }

    public boolean isIgnoreSystemMessages() {
        return ignoreSystemMessages;
    }

    public void setIgnoreSystemMessages(boolean ignoreSystemMessages) {
        this.ignoreSystemMessages = ignoreSystemMessages;
    }

    public String getIamAwayMessage() {
        return iamAwayMessage;
    }

    public void setIamAwayMessage(String iamAwayMessage) {
        this.iamAwayMessage = iamAwayMessage;
    }

    public boolean isIamAway() {
        return iamAway;
    }

    public void setIamAway(boolean iamAway) {
        this.iamAway = iamAway;
    }

    public String getDatabaseDriver() {
        return databaseDriver;
    }

    public String getDatabaseURL() {
        return databaseURL;
    }

    public String getDatabaseUsername() {
        return databaseUsername;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    /**
     * Prepared statement String for storing the DB data.
     * @return prepared statement
     */
    public String getDatabaseStore() {
        return databaseStore;
    }

    public boolean isAutoDismissDialogs() {
        return autoDismissDialogs;
    }
    public void setAutoDismissDialogs(boolean value) {
        autoDismissDialogs = value;
    }

    public String getWeatherZipCodes() {
        return weatherZipCodes;
    }

    public void setWeatherZipCodes(String weatherZipCodes) {
        this.weatherZipCodes = weatherZipCodes;
    }

    public boolean isShowWeather() {
        return showWeather;
    }

    public void setShowWeather(boolean showWeather) {
        this.showWeather = showWeather;
    }

    public boolean isColorMessages() {
        return colorMessages;
    }

    public void setColorMessages(boolean colorMessages) {
        this.colorMessages = colorMessages;
    }

    public boolean isShowOrderEntry() {
        return showOrderEntry;
    }

    public void setShowOrderEntry(boolean showOrderEntry) {
        this.showOrderEntry = showOrderEntry;
    }
    
    public boolean isSpellCheck() {
        return spellCheck;
    }

    public void setSpellCheck(boolean spellCheck) {
        this.spellCheck = spellCheck;
    }

    public boolean isSpellCheckAllowSlang() {
        return spellCheckAllowSlang;
    }

    public void setSpellCheckAllowSlang(boolean allow) {
        spellCheckAllowSlang = allow;
    }

    public boolean isShowCharCounter() {
        return showCharCounter;
    }

    public void setShowCharCounter(boolean showCharCounter) {
        this.showCharCounter = showCharCounter;
    }

    public boolean isShowPictures() {
        return showPictures;
    }

    public void setShowPictures(boolean showPictures) {
        this.showPictures = showPictures;
    }

    public String getLastFolder() {
        return lastFolder;
    }

    public void setLastFolder(String folder) {
        lastFolder = folder;
    }

    public boolean isOrderCausesShowManageScreen() {
        return orderCausesShowManageScreen;
    }
    public void setOrderCausesShowManageScreen(boolean value) {
        orderCausesShowManageScreen = value;
    }

    public boolean isAllowCommissionEntry() {
        return allowCommissionEntry;
    }
    public void setAllowCommissionEntry(boolean value) {
        allowCommissionEntry = value;
    }

    public int getLookAndFeelIndex() {
        return lookAndFeelIndex;
    }
    public void setLookAndFeelIndex(int value) {
        lookAndFeelIndex = value;
    }

    public boolean isEnableOrderEntryInSystem() {
        return enableOrderEntryInSystem;
    }

    public int getHTTPServerPort() {
        return serverPort;
    }
    public void setHTTPServerPort(int value) {
        serverPort = value;
    }
    public void setHTTPServerPort(String value) {
        serverPort = Integer.parseInt(value);
    }

    public boolean isHTTPServerEnabled() {
        return serverEnabled;
    }

    public void setHTTPServerEnabled(boolean value) {
        serverEnabled = value;
    }

    public boolean isUseTray() {
        return useTray;
    }

    public void setUseTray(boolean value) {
        useTray = value;
    }

    public boolean isMultiSelectAllowed() {
        return true;
    }

    public Rectangle getWindowBounds() {
        return windowBounds;
    }

    public void setWindowBounds(Rectangle windowBounds) {
        this.windowBounds = windowBounds;
    }

    public String getUploadFolder() {
        return uploadFolder;
    }

    public void setUploadFolder(String uploadFolder) {
        this.uploadFolder = uploadFolder;
    }

    public String getDownloadFolder() {
        return downloadFolder;
    }

    public void setDownloadFolder(String downloadFolder) {
        this.downloadFolder = downloadFolder;
    }

    public boolean isShowWebIcons() {
        return showWebIcons;
    }

    public void setShowWebIcons(boolean showWebIcons) {
        this.showWebIcons = showWebIcons;
    }

    public static ContactPreferences findBuddyPreferences(String id) {
        ContactPreferences bPref = INSTANCE.buddyPreferences.get(id);
        if (bPref == null) {
            bPref = new ContactPreferences(id);
            INSTANCE.buddyPreferences.put(id, bPref);
        }
        return bPref;
    }

    public Map<String,ContactPreferences> getBuddyPreferences() {
        return buddyPreferences;
    }

    public void setBuddyPreferences(Map<String,ContactPreferences> buddyPreferences) {
        this.buddyPreferences = buddyPreferences;
    }

    public static GroupPreferences findGroupPreferences(String id) {
        GroupPreferences pref = INSTANCE.groupPreferences.get(id);
        if (pref == null) {
            pref = new GroupPreferences(id);
            INSTANCE.groupPreferences.put(id, pref);
        }
        return pref;
    }

    public Map<String,GroupPreferences> getGroupPreferences() {
        return groupPreferences;
    }
    public void setGroupPreferences(Map<String,GroupPreferences> preferences) {
        this.groupPreferences = preferences;
    }

    public static synchronized void setInstance(ClientProperties data) {
        INSTANCE = data;
    }

    public static void setFirstTimeUse(boolean value) {
        firstTimeUse  = value;
    }

    public static boolean isFirstTimeUse() {
        return firstTimeUse;
    }

    public boolean isHideEmptyGroups() {
        return hideEmptyGroups;
    }

    public void setHideEmptyGroups(boolean hideEmptyGroups) {
        this.hideEmptyGroups = hideEmptyGroups;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    /**
     * Reconnect count.
     * How many times allowed to disconnect while trying to automatically reconnect.
     */
    public int getDisconnectCount() {
        return disconnectCount;
    }

    public void setDisconnectCount(String disconnectCount) {
        this.disconnectCount = getInt(disconnectCount, "disconnectCount", this.disconnectCount);
    }
    
    public void setDisconnectCount(int disconnectCount) {
        this.disconnectCount = disconnectCount;
    }

    public boolean isSnapWindows() {
        return snapWindows;
    }

    public void setSnapWindows(boolean snapWindows) {
        this.snapWindows = snapWindows;
    }

    /**
     * Allows the use of Alerts
     * @return true if allowed
     */
    public boolean isUseAlert() {
        return useAlert;
    }

    public void setUseAlert(boolean useAlert) {
        this.useAlert = useAlert;
    }

    /**
     * Determines if the status bar should never be hidden
     * @return true if so.
     */
    public boolean isStatusbarAlwaysVisible() {
        return statusbarAlwaysVisible;
    }

    public void setStatusbarAlwaysVisible(boolean statusbarAlwaysVisible) {
        this.statusbarAlwaysVisible = statusbarAlwaysVisible;
    }

    /**
     * Display buffer size.
     * In Killobytes.
     * @return Display buffer size. In Killobytes.
     */
    public int getDisplayBuffer() {
        return displayBuffer;
    }

    /**
     * Display buffer size.
     * In Killobytes.
     * @param displayBuffer Display buffer size. In Killobytes.
     */
    public void setDisplayBuffer(int displayBuffer) {
        if (displayBuffer<0)
            displayBuffer=1;
        if (displayBuffer>10)
            displayBuffer=10;
        this.displayBuffer = displayBuffer;
    }

    public void setDisplayBuffer(String displayBuffer) {
        try {
            setDisplayBuffer(Integer.parseInt(displayBuffer));
        } catch (NumberFormatException e) {
            // no care
        }
    }

    public String getIpQuery() {
        return ipQuery;
    }

    public void setIpQuery(String ipQuery) {
        this.ipQuery = ipQuery;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        if (isTimeToSetProperty(PROP_PROXY_HOST)) {
            this.proxyHost = proxyHost;
//            System.setProperty(PROP_PROXY_HOST, proxyHost);
        }
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        if (isTimeToSetProperty(PROP_PROXY_PORT)) {
            this.proxyPort = proxyPort;
//            System.setProperty(PROP_PROXY_PORT, ""+proxyPort);
        }
    }
}
