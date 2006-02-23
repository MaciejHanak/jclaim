package com.itbs.aimcer.bean;

import com.itbs.aimcer.commune.Connection;

/**
 * Allows an easy finding of contacts.
 *
 * Centralized and allows one to control creation (class etc).
 *
 * @author Alex Rass
 * @since Feb 11, 2006
 */
public interface ContactFactory {
    Contact create(Nameable buddy, Connection connection);
    Contact create(String name, Connection connection);

    /**
     * Just like create, but doesn't create it if it's not there.
     * @param name
     * @param connection
     * @return reference
     */
    Contact get(String name, Connection connection);
}
