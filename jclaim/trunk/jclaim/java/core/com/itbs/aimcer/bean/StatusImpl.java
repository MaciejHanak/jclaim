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
 * Maintains a simple set of statuses.
 * Updates contact of any change.
 * Keeping this class separate allows for cleaneleness in implementation.
 *
 * @author Alex Rass
 * @since Feb 11, 2006
 */
public class StatusImpl implements Status {
    Contact parent;
    boolean online;
    boolean away;
    boolean wireless;
    int idleTime;

    public StatusImpl(Contact parent) {
        this.parent = parent;
    }

    public Contact getParent() {
        return parent;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
        if (!online) {
            this.away=false;
            this.idleTime=0;
        }
        parent.statusChanged();
    }

    public boolean isAway() {
        return away;
    }

    public void setAway(boolean away) {
        this.away = away;
        parent.statusChanged();
    }

    public boolean isWireless() {
        return wireless;
    }

    public void setWireless(boolean wireless) {
        this.wireless = wireless;
        parent.statusChanged();
    }

   public int getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(int idleTime) {
        this.idleTime = idleTime;
        parent.statusChanged();
    }

    /**
     * Deel copy of the object, but shallow copy of the parent.
     * @return copy.
     */
    public Object clone() {
        StatusImpl newStatus = new StatusImpl(parent);
        newStatus.online =online;
        newStatus.away= away;
        newStatus.wireless= wireless;
        newStatus.idleTime= idleTime;
        return newStatus;
    }
} // StatusImpl
