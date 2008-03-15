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

import com.itbs.util.GeneralUtils;

import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

/**
 * Preferences per buddy.
 * Things you may want to save.
 * 
 * @author Alex Rass
 * @since Sep 11, 2004
 */
public class ContactPreferences {
    private String displayName;
    /** UID for a given provider. */
    private String uid;
    private Rectangle windowBounds;
    /** Between history and text */
    private int verticalSeparation = -1;
    /** Between history/text and secondary panels */
    private int horizontalSeparation = -1;
    private boolean hideFromList;
    private boolean showInList;
    private boolean notifyOnConnect;
    private boolean infoPanelVisible;
    private boolean orderPanelVisible;
    private boolean showIcon = true; // default to true
    private String name;
    private String phone;
    private String emailAddress;
    private String notes;
    private Date lastConnected;
    private transient boolean useDisplayName;
    private List<String> offlineMessages;

    public ContactPreferences(String uid) {
        this.uid = uid;
    }

    public Rectangle getWindowBounds() {
        return windowBounds;
    }

    public void setWindowBounds(Rectangle windowBounds) {
        this.windowBounds = windowBounds;
    }

    public int getVerticalSeparation() {
        return verticalSeparation;
    }

    public void setVerticalSeparation(int verticalSeparation) {
        this.verticalSeparation = verticalSeparation;
    }


    public int getHorizontalSeparation() {
        return horizontalSeparation;
    }

    public void setHorizontalSeparation(int horizontalSeparation) {
        this.horizontalSeparation = horizontalSeparation;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        useDisplayName = displayName != null && displayName.trim().length() != 0;
        if (!useDisplayName) { // make life easier?
            this.displayName = null;
        }
    }

    public boolean isUseDisplayName() {
        return useDisplayName;
    }

    /**
     * Determines if this contact will be displayed in the list.
     * @return true if hidden.
     */
    public boolean isHideFromList() {
        return hideFromList;
    }

    /**
     * Determines if this contact will be displayed in the list.
     * @param hideFromList true if hidden.
     */
    public void setHideFromList(boolean hideFromList) {
        this.hideFromList = hideFromList;
    }

    /**
     * Show in the list even if contact list doesn't show offline.
     * @return true if contact is to always be shown.
     */
    public boolean isShowInList() {
        return showInList;
    }

    /**
     * Show in the list even if contact list doesn't show offline. 
     * @param showInList true if contact is to always be shown.
     */
    public void setShowInList(boolean showInList) {
        this.showInList = showInList;
    }

    /**
     * If true, notify the user when a contact comes online.
     * @return If true, notify the user when a contact comes online.
     */
    public boolean isNotifyOnConnect() {
        return notifyOnConnect;
    }

    /**
     * If true, notify the user when a contact comes online.
     * @param notifyOnConnect If true, notify the user when a contact comes online.
     */
    public void setNotifyOnConnect(boolean notifyOnConnect) {
        this.notifyOnConnect = notifyOnConnect;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getEmailAddressAsURL() throws UnsupportedEncodingException {
        if (GeneralUtils.isNotEmpty(emailAddress))
            return "<a href=\"mailto:"+URLEncoder.encode(emailAddress, "UTF-8")+ "\">Email</a>";
        return "";
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getLastConnected() {
        return lastConnected;
    }

    public void setLastConnected(Date lastConnected) {
        this.lastConnected = lastConnected;
    }

    public boolean isInfoPanelVisible() {
        return infoPanelVisible;
    }

    public void setInfoPanelVisible(boolean infoPanelVisible) {
        this.infoPanelVisible = infoPanelVisible;
    }

    public boolean isOrderPanelVisible() {
        return orderPanelVisible;
    }

    public void setOrderPanelVisible(boolean orderPanelVisible) {
        this.orderPanelVisible = orderPanelVisible;
    }

    /**
     * Display the buddy icon for the user?
     * @return true if so
     */
    public boolean isShowIcon() {
        return showIcon;
    }

    public void setShowIcon(boolean showIcon) {
        this.showIcon = showIcon;
    }

    /**
     * Returns a hash code value for the object.
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return uid.hashCode();
    }
    
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<String> getOfflineMessages() {
        return offlineMessages;
    }

    public void setOfflineMessages(List<String> offlineMessages) {
        this.offlineMessages = offlineMessages;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the obj
     *         argument; <code>false</code> otherwise.
     * @see #hashCode()
     * @see java.util.Hashtable
     */
    public boolean equals(Object obj) {
        return obj.hashCode() == hashCode();
    }

    // **********<     XML Functions only!  do not use directly!   >***********
    /**
     * Recreation for XML only!
     */
    public ContactPreferences() {
    }

} // class ContactPreferences
