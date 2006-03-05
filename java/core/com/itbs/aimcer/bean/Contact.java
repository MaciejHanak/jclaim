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

import javax.swing.*;

/**
 * This interface assumes one needs to manage a contact.
 *
 * @author Alex Rass
 * @since Sep 27, 2004
 */
public interface Contact extends Nameable {
    void statusChanged();

    /**
     * While most connections don't support it, some may.
     * @return icon to use for this person.
     */
    Icon getIcon();
    void setIcon(Icon icon);

    /**
     * Contact's picture.
     * @return Contact's picture.
     */
    Icon getPicture();
    void setPicture(Icon icon);

    /**
     * Displayname is what the UI should display.
     * It is often not the same as what getName() returns.
     * @return name to display.
     */
    String getDisplayName();

    /**
     * Displayname is what the UI should display.
     * It is often not the same as what getName() returns.
     * @param name to display.
     */
    void setDisplayName(String name);

    /**
     * Status support.
     * @return status
     */
    Status createStatus();
    Status getStatus();

    /**
     * Connection that this contact belongs to.
     * @return connection.
     */
    Connection getConnection();
}
