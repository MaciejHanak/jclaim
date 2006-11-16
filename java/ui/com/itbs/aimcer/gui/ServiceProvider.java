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

package com.itbs.aimcer.gui;

import com.itbs.aimcer.commune.MessageSupport;
import com.itbs.aimcer.commune.jaim.JAIMConnection;
import com.itbs.aimcer.commune.joscar.ICQConnection;
import com.itbs.aimcer.commune.joscar.OscarConnection;
import com.itbs.aimcer.commune.msn.JmlMsnConnection;
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
        new ServiceProvider(JmlMsnConnection.class,      "MSN (JML)"     ),
        new ServiceProvider(MSNConnection.class,         "MSN (JMSN-Old)"),
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
