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

    String getDisplayName();
    void setDisplayName(String name);

    Status createStatus();
    Status getStatus();

    Connection getConnection();
}
