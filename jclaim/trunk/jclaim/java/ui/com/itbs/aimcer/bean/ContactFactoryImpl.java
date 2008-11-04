package com.itbs.aimcer.bean;

import com.itbs.aimcer.commune.Connection;

/**
 * @author Alex Rass
* @since Oct 15, 2008 11:10:51 AM
*/
public class ContactFactoryImpl implements ContactFactory {
    public Contact create(Nameable buddy, Connection connection) {
        return ContactWrapper.create(buddy, connection);
    }

    public Contact create(String name, Connection connection) {
        return ContactWrapper.create(name, connection);
    }

    public Contact get(String name, Connection connection) {
        return ContactWrapper.get( name, connection);
    }
}
