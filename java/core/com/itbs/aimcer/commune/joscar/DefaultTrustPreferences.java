package com.itbs.aimcer.commune.joscar;

import net.kano.joustsim.Screenname;
import net.kano.joustsim.trust.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * @author Created by Alex Rass on Oct 6, 2004
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
