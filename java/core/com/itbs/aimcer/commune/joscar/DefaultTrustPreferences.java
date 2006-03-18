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

package com.itbs.aimcer.commune.joscar;

import net.kano.joustsim.Screenname;
import net.kano.joustsim.trust.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * This will be needed for secure IM support.
 *
 * @author Alex Rass
 * @since Oct 6, 2004
 */
public class DefaultTrustPreferences implements TrustPreferences{
    Screenname screenname;
    CertificateTrustManager certificateTrustManager;
    SignerTrustManager signerTrustManager;

    public DefaultTrustPreferences(Screenname screenname) {
        this.screenname = screenname;
        // even though they are the same thing, Keith said we need 2 instances
        certificateTrustManager = new PermanentSignerTrustManager(screenname);
        signerTrustManager = new PermanentSignerTrustManager(screenname);
    }

    public Screenname getScreenname() {
        return screenname;
    }

    public PrivateKeysPreferences getPrivateKeysPreferences() {
        return new PrivateKeysPreferences() {
            private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

            public Screenname getScreenname() {
                return screenname;
            }

            public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
                pcs.addPropertyChangeListener(propertyChangeListener);
            }

            public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
                pcs.removePropertyChangeListener(propertyChangeListener);
            }

            public PrivateKeys getKeysInfo() {
                return null;// todo new PrivateKeys();
            }
        };
    }

    public CertificateTrustManager getCertificateTrustManager() {
        return certificateTrustManager;
    }

    public SignerTrustManager getSignerTrustManager() {
        return signerTrustManager;
    }
}
