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
import com.itbs.aimcer.commune.MessageSupport;
import com.itbs.aimcer.gui.ImageCacheUI;
import com.itbs.aimcer.gui.ListRenderer;
import com.itbs.util.GeneralUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a basic implementation needed to maintain contacts.
 * This class takes care of both - model and view (JLabel).
 * If you want to enchance the behavior, either inherit this class or write your own, but then don't
 * forget to change the contact factory.
 *
 * @author Alex Rass
 * @since Sep 9, 2004
 */
public class ContactWrapper implements Contact, Renderable {
    public static final Font NORM = new Font("Arial", Font.PLAIN, ClientProperties.INSTANCE.getFontSize());
    public static final Font BOLD = new Font("Arial", Font.BOLD, ClientProperties.INSTANCE.getFontSize());
    public static final Font OFF = new Font("Arial", Font.ITALIC, ClientProperties.INSTANCE.getFontSize() - 1);

    public static final Color PRESENT = Color.BLACK;
    public static final Color AWAY = Color.GRAY;
    
    private final static Color SELECTED = new Color(127, 190, 240);

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
    private JLabel displayComponent;
    private JPanel offsetPanel;

//    private static Map<String, ContactWrapper> wrappers = new HashMap<String, ContactWrapper>(50);
    static Map<String,ContactWrapper> wrappers = Collections.synchronizedMap(new HashMap<String, ContactWrapper>(50));

    public static ContactWrapper create(Nameable buddy, Connection connection){
        return create(buddy.getName(), connection);
    }
    /**
     * Factory
     * @param name to use as template
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

    public static Object[] toArray() {
        return wrappers.values().toArray();
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
        offsetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        displayComponent = new JLabel();
        updateDisplayComponent();
        JPanel spacer = new JPanel() {
            public Dimension getPreferredSize() {
                return new Dimension(5, 0);
            }
        };
        offsetPanel.add(spacer);
        offsetPanel.add(displayComponent);
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
        if (getStatus().isOnline() && !preferences.isHideFromList()) {
            displayComponent.setText(getDisplayName());
            displayComponent.setIcon(getIcon());
            displayComponent.setFont(NORM);
            displayComponent.setForeground(getStatus().isAway() ? AWAY : PRESENT);
            if (connection instanceof MessageSupport)
                displayComponent.setToolTipText(getName() + " on " + getConnection().getServiceName() + " as " + ((MessageSupport) getConnection()).getUserName() + (getStatus().isAway() ? (" Idle for " + getStatus().getIdleTime() + "m") : ""));
            else
                displayComponent.setToolTipText(null);
        } else {
            displayComponent.setText(getDisplayName() + (getStatus().isOnline()?" (Online)":" (Offline)"));
            displayComponent.setIcon(null);
            displayComponent.setFont(OFF);
            displayComponent.setForeground(AWAY);
            displayComponent.setToolTipText("Last Seen: " + (preferences.getLastConnected()==null?"Not yet.":preferences.getLastConnected())); // turn off tooltip
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

    public JComponent getDisplayComponent(boolean isSelected, boolean cellHasFocus) {
//        displayComponent.setBackground(isSelected ? ListRenderer.SELECTED : ListRenderer.NOT_SELECTED);
        displayComponent.setBackground(isSelected ? SELECTED: ListRenderer.NOT_SELECTED);
        displayComponent.setOpaque(isSelected);
//        if (isSelected && !cellHasFocus)
//            displayComponent.setBackground(ListRenderer.SELECTED_NO_FOCUS);
        return offsetPanel;
    }

    public String getToolTipText() {
        return displayComponent.getToolTipText();
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
            setIcon(getStatus().isWireless()? ImageCacheUI.ICON_WIRELESS.getIcon(): ImageCacheUI.getImage(parent.getConnection().getClass()));
            super.setWireless(wireless);
        }
    }
}

