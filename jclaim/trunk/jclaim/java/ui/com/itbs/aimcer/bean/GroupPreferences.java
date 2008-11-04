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

/**
 * Preferences per group.
 * Things you may want to save pertaining to a single group.
 * @author Alex Rass
 * @since Sep 11, 2004
 */
public class GroupPreferences {
    /** UID for a given provider. */
    private String name;
    private boolean fold;

    public GroupPreferences(String name) {
        this.name = name;
    }

    /**
     * Determines if the contacts will be displayed in the list.
     * @return true if hidden.
     */
    public boolean isFold() {
        return fold;
    }

    /**
     * Determines if the contacts will be displayed in the list.
     * @param fold true if hidden.
     */
    public void setFold (boolean fold) {
        this.fold = fold;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns a hash code value for the object.
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return name.hashCode();
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
    public GroupPreferences() {
    }
} // class GroupPreferences
