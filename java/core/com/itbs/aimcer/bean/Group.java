package com.itbs.aimcer.bean;

import com.itbs.aimcer.commune.Connection;

/**
 * Used to describe a group of contacts.
 * Groups should not be exclusive.
 * Groups should allow collections of contacts from any place.
 * @author Alex Rass
 * @since Sep 22, 2004
 */
public interface Group extends Nameable {
    int size();
    void clear(Connection connection);
    Nameable get(int index);
    Nameable add(Nameable contact);
    boolean remove(Nameable contact);
    // inherits String getName() from Nameable
}
