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

import com.itbs.aimcer.commune.Connection;
import com.itbs.aimcer.gui.ImageCacheUI;
import com.itbs.aimcer.gui.Main;
import com.itbs.util.GeneralUtils;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Provides a basic implementation needed to maintain contacts.
 * This class takes care of both - model and view (JLabel).
 * If you want to enchance the behavior, either inherit this class or write your own, but then don't
 * forget to change the contact factory.
 *
 * @author Alex Rass
 * @since Sep 9, 2004
 */
public class ContactWrapper implements Contact {

    public static Comparator <Nameable> COMP_NAME = new NameComparator();

    /**
     * Minimal Name.
     */
    private String screenName;
    private Status status;

    /**
     * Name that should be used, but not trusted.
     */
    private String displayName;

    private Icon icon, picture;
    private ContactPreferences preferences;
    private long lastDisclaimerTime;
    private Connection connection;

    //    private static Map<String, ContactWrapper> wrappers = new HashMap<String, ContactWrapper>(50);
    static Map<String,ContactWrapper> wrappers = Collections.synchronizedMap(new HashMap<String, ContactWrapper>(50));

    public static ContactWrapper create(Nameable buddy, Connection connection){
        return create(buddy.getName(), connection);
    }
    /**
     * Factory
     * @param name to use as template
     * @param connection connection
     * @return working wrapper
     */
    public static ContactWrapper create(String name, Connection connection) {
        ContactWrapper result = get(name, connection);
        if (result==null) {
//            log.fine("Creating a new wrapper for " + name);
            result = new ContactWrapper(name, connection);
            if (connection != null) // NPE for unit tests
                result.setIcon(ImageCacheUI.getImage(connection.getClass()));
            put(result);
        }
        return result;
    }

    public static ContactWrapper[] toArray() {
        return wrappers.values().toArray(new ContactWrapper[wrappers.size()]);
    }

    /**
     * Retrieves the contact
     * @param name name
     * @param connection connection
     * @return contact or null if none found
     */
    public static ContactWrapper get(String name, Connection connection) {
        return wrappers.get(GeneralUtils.getSimplifiedName(name) + connection);
    }
    /**
     * Stores the contact
     * @param cw contact
     */
    private static void put(ContactWrapper cw) {
        wrappers.put(GeneralUtils.getSimplifiedName(cw.getName()) + cw.getConnection(), cw);
    }

    private ContactWrapper(String name, Connection con) {
        this.screenName = name;
        this.displayName = name;
        this.connection = con;
        this.status = createStatus();
        preferences = ClientProperties.findBuddyPreferences(name + "|" + (connection==null?"":connection.getServiceName()));
    }

    /**
     * This way one can overwrite it.
     * @return status to be used.
     */
    public Status createStatus() {
        return new ContactStatus(this);
    }

    public Status getStatus() {
        return status;
    }

    /**
     * Short name
     * @return short name
     */
    public String getName() {
        return screenName;
    }

    /**
     * Name that should be displayed everywhere where nice print is needed.
     * @return name that should be displayed
     */
    public String getDisplayName() {
        return preferences.isUseDisplayName()?preferences.getDisplayName():displayName;
    }

    /**
     * Name that should be displayed.
     * <p>
     * Ignores the existence of the override.
     * @param displayName Name that should be displayed.
     */
    public void setDisplayName(String displayName) {
        // it's ok to just set it.  getDisplayName doesn't use it straight.
        this.displayName = displayName;
        if (!preferences.isUseDisplayName()) { //no point wasting cycles if we are not using it.
            updateDisplayComponent();
        }
    }

    public String toString() {
        return displayName;
    }

    public void statusChanged() {
        updateDisplayComponent();
    }


    /**
     * Resets display's properties.
     * <p>
     * In case someone needs to update the component based on some settings outside of the few supported here,
     * for example contactPreferences: made this call public.
     */
    public void updateDisplayComponent() {
        if (Main.getPeoplePanel()!=null) {
            Main.getPeoplePanel().update();
        }
    } // updateDisplayComponent()


    public ContactPreferences getPreferences() {
        return preferences;
    }


    public long getLastDisclaimerTime() {
        return lastDisclaimerTime;
    }

    public void setLastDisclaimerTime() {
        lastDisclaimerTime = System.currentTimeMillis();
    }

    /**
     * Connection.
     * Nameable is not unique by name, but by the pair with connection.
     * @return connection
     */
    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        if (icon != null && icon.getIconHeight() > 15) {
            final Image scaledInstance = ((ImageIcon)icon).getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH);
            this.icon  = new ImageIcon(scaledInstance);
        } else
            this.icon = icon;
        updateDisplayComponent();
    }
    public Icon getPicture() {
        return picture;
    }

    public void setPicture(Icon icon) {
        picture = icon;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * @param obj the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the obj
     *         argument; <code>false</code> otherwise.
     * @see #hashCode()
     * @see java.util.Hashtable
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Nameable))
            return false;
        Nameable other = (Nameable) obj;
        if (!getName().equals(other.getName()))
            return false;
        if (!(obj instanceof ContactWrapper))
            return true; // can't compare any more.  and name matched.
        return getConnection().equals(((ContactWrapper)other).getConnection());
    }

    public String oldToString() {
        return super.toString();
    }


    private class ContactStatus extends StatusImpl {
        public ContactStatus(Contact parent) {
            super(parent);
        }

        public boolean isOnline() {
            return super.isOnline() && !preferences.isHideFromList();
        }

        public void setOnline(boolean online) {
            if (online)
                preferences.setLastConnected(new Date()); // update on show-up
            else if (this.online && !online)
                preferences.setLastConnected(new Date()); // update on exit
            super.setOnline(online);
        }

        public void setWireless(boolean wireless) {
            this.wireless = wireless;
            setIcon(wireless? ImageCacheUI.ICON_WIRELESS.getIcon(): ImageCacheUI.getImage(parent.getConnection().getClass()));
            parent.statusChanged();
        }
    }

    static class NameComparator implements Comparator<Nameable> {
        public int compare(Nameable o1, Nameable o2) {
            if (o1 instanceof Contact && o2 instanceof Contact) {
                return ((Contact) o1).getDisplayName().compareToIgnoreCase(((Contact) o2).getDisplayName());
            }
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }

} // class

