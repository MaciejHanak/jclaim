package com.itbs.aimcer.bean;

/**
 * Interface which defines a status of a contact.
 * 
 * @author Alex Rass
 * @since Sep 22, 2004
 */
public interface Status {
    Contact getParent();

    boolean isOnline();
    void setOnline(boolean online);

    boolean isAway();
    void setAway(boolean away);

    /**
     *
     * @return True means the (contact?) is on wireless connection.  False implies a normal connection.
     */
    boolean isWireless();
    void setWireless(boolean wireless);

    /**
     * Sets idle time.
     * @param idleMins minuts inactive
     */
    void setIdleTime(int idleMins);

    /**
     * Idle time in minutes
     * @return minutes active
     */
    int getIdleTime();
}
