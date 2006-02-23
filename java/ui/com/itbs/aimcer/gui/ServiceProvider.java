package com.itbs.aimcer.gui;

import com.itbs.aimcer.commune.MessageSupport;
import com.itbs.aimcer.commune.jaim.JAIMConnection;
import com.itbs.aimcer.commune.joscar.ICQConnection;
import com.itbs.aimcer.commune.joscar.OscarConnection;
import com.itbs.aimcer.commune.msn.MSNConnection;
import com.itbs.aimcer.commune.smack.GoogleConnection;
import com.itbs.aimcer.commune.smack.SecureSmackConnection;
import com.itbs.aimcer.commune.smack.SmackConnection;
import com.itbs.aimcer.commune.ymsg.YMsgConnection;

/**
 *  Manages the list of available services.
 *
 * @author Alex Rass
 */
class ServiceProvider {
    String name;
    Class classRef;

    /**
     * List of all available providers.
     */
    static ServiceProvider[] providers = {
//        new ServiceProvider(UTestFakeConnection.class, "Fake"),
        new ServiceProvider(OscarConnection.class,       "AIM - Oscar"   ),
        new ServiceProvider(ICQConnection.class,         "ICQ"           ),
//        new ServiceProvider(AIMConnection.class,       "AIM - Oscar 2" ),
        new ServiceProvider(YMsgConnection.class,        "Yahoo!"        ),
        new ServiceProvider(MSNConnection.class,         "MSN"           ),
        new ServiceProvider(GoogleConnection.class,      "Google Talk"   ),
        new ServiceProvider(SmackConnection.class,       "Jabber"        ),
        new ServiceProvider(SecureSmackConnection.class, "Secure Jabber" ),
        new ServiceProvider(JAIMConnection.class,        "AIM - TOC"     ),
    };

    ServiceProvider(Class provider, String name) {
        classRef = provider;
        this.name = name;
    }

    public String toString() {
        return name;
    }

    MessageSupport getInstance() throws IllegalAccessException, InstantiationException {
        return (MessageSupport) classRef.newInstance();
    }

    static ServiceProvider[] getProviders() {
        return providers;
    }
} // class ServiceProvider
