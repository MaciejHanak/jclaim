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
import com.itbs.aimcer.gui.userlist.GroupLabel;
import com.itbs.util.GeneralUtils;

import javax.swing.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Managed things related to a Group.
 *
 * This class takes care of both - model and view (JLabel).
 * If you want to enchance the behavior, either inherit this class or write your own, but then don't
 * forget to change the group factory.
 *
 * @author Alex Rass
 * @since Sep 9, 2004
 */
public class GroupWrapper implements Group, Renderable, Comparable {
    private static final Logger log = Logger.getLogger(GroupWrapper.class.getName());
    private static Map<String,GroupWrapper> wrappers = new HashMap<String, GroupWrapper>(10);
    public static Comparator <Nameable> COMP_NAME = new NameComparator();

    // Local stuff
    String name;
    private List<Nameable> contacts = new CopyOnWriteArrayList<Nameable>();
    GroupPreferences preferences;
    private GroupLabel displayComponent;


//    private static Map<String, GroupWrapper> wrappers = new HashMap<String, GroupWrapper>(10);

    /**
     * Constructor
     * @param name of the group
     */
    private GroupWrapper(String name) {
        this.name = name;
        displayComponent = new GroupLabel(this);
        preferences = ClientProperties.findGroupPreferences(name);
    }

    public int size() {
        return contacts.size();
    }

    public int sizeOnline() {
        int count = 0;
        for (Nameable nameable: contacts) {
            if (nameable instanceof ContactWrapper) {
                if (((ContactWrapper)nameable).getStatus().isOnline() || ((ContactWrapper)nameable).getPreferences().isShowInList()) {
                    count++;
                }
            }
        }
        return count;
    }

    public void clear(Connection connection) {
        for (Nameable nameable: contacts) {
            if (nameable instanceof ContactWrapper) {
                if (nameable == connection) {
                    contacts.remove(nameable);
                }
            }
        }
//        contacts.clear();
    }

    public Nameable get(int index) {
        return contacts.get(index);
    }

    public Nameable add(Nameable contact) {
        if (!contacts.contains(contact))
            contacts.add(contact);
        return contact;
    }

    public boolean remove(Nameable contact) {
        return contacts.remove(contact);
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }


    public int compareTo(Object o) {
        if (o==null) return 0;
        if (o instanceof GroupWrapper) return getName().compareToIgnoreCase(((GroupWrapper)o).getName());
        if (name!=null && o.toString()!=null) {
            return getName().compareToIgnoreCase(o.toString());
        } else {
            return 0;
        }
    }

    /**
     * Is Folded
     * @return true if group is folded and no items are displayed for that list.
     */
    public boolean isShrunk() {
        return preferences.isFold();
    }

    public void swapShrunk() {
        preferences.setFold(!preferences.isFold());
    }

    public Nameable[] toArray() {
        return contacts.toArray(new Nameable[contacts.size()]);
    }

    public static GroupWrapper create(String group) {
        GroupWrapper result = wrappers.get(GeneralUtils.getSimplifiedName(group));
        if (result==null) {
            log.finest("creating a new group wrapper for " + group);
            result = new GroupWrapper(group);
            wrappers.put(GeneralUtils.getSimplifiedName(group), result);
        }
        return result;
    }

    public static GroupWrapper create(Group group) {
        return create(group.getName());
    }

    public JComponent getDisplayComponent(boolean isSelected, boolean cellHasFocus) {
        return displayComponent.getDisplayComponent(isSelected, cellHasFocus);
    }

    static class NameComparator implements Comparator<Nameable> {
        public int compare(Nameable o1, Nameable o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
/*
        public int compare(Object o1, Object o2) {
            if (o1 instanceof Group && o2 instanceof Group) {
                return ((Group) o1).getName().compareToIgnoreCase(((Group) o2).getName());
            }
            return 0;
        }
*/
    }
}
