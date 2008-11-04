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

import net.kano.joscar.CopyOnWriteArrayList;
import net.kano.joscar.DefensiveTools;
import net.kano.joustsim.Screenname;
import net.kano.joustsim.trust.*;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class DefaultCertificateTrustManager implements CertificateTrustManager {
    private static final Logger logger
            = Logger.getLogger(DefaultCertificateTrustManager.class.getName());

    private final Screenname buddy;

    private final Set<DefaultCertificateHolder> trusted = new HashSet<DefaultCertificateHolder>();

    private final CopyOnWriteArrayList<CertificateTrustListener> listeners = new CopyOnWriteArrayList<CertificateTrustListener>();

    public DefaultCertificateTrustManager(Screenname buddy) {
        this.buddy = buddy;
    }

    public final Screenname getBuddy() { return buddy; }

    public void addTrustListener(CertificateTrustListener l) {
        listeners.addIfAbsent(l);
    }

    public void removeTrustChangeListener(CertificateTrustListener l) {
        listeners.remove(l);
    }

    public boolean trustCertificate(X509Certificate cert) throws TrustException {
        checkCanBeAdded(cert);
        boolean added = addTrust(cert);
        if (added) fireTrustedEvent(cert);
        return added;
    }

    protected void checkCanBeAdded(X509Certificate cert)
            throws CantBeAddedException {
        if (!canBeAdded(cert)) {
            logger.warning("Can't add certificate to " + this + ": ");
            throw new CantBeAddedException();
        }
    }

    public synchronized boolean isTrusted(X509Certificate cert) {
        return trusted.contains(new DefaultCertificateHolder(cert));
    }

    public boolean revokeTrust(X509Certificate cert) {
        boolean removed = removeTrust(cert);
        if (removed) fireNoLongerTrustedEvent(cert);
        return removed;
    }

    public List<X509Certificate> getTrustedCertificates() {
        List<X509Certificate> certs = new ArrayList<X509Certificate> (trusted.size());
        int i = 0;
        for (DefaultCertificateHolder aTrusted : trusted) {
            certs.add(aTrusted.getCertificate());
            i++;
        }
        return certs;
    }


    protected synchronized boolean addTrust(X509Certificate cert)
            throws CantBeAddedException {
        checkCanBeAdded(cert);
        return trusted.add(new DefaultCertificateHolder(cert));
    }

    protected synchronized boolean removeTrust(X509Certificate cert) {
        return trusted.remove(new DefaultCertificateHolder(cert));
    }

    protected void fireTrustedEvent(X509Certificate cert) {
        assert !Thread.holdsLock(this);

        DefensiveTools.checkNull(cert, "cert");

        for (Object listener1 : listeners) {
            CertificateTrustListener listener = (CertificateTrustListener) listener1;
            listener.trustAdded(this, cert);
        }
    }

    protected void fireNoLongerTrustedEvent(X509Certificate cert) {
        assert !Thread.holdsLock(this);

        DefensiveTools.checkNull(cert, "cert");

        for (Object listener1 : listeners) {
            CertificateTrustListener listener = (CertificateTrustListener) listener1;
            listener.trustRemoved(this, cert);
        }
    }

    public boolean importCertificate(File file) throws TrustException {
        DefensiveTools.checkNull(file, "file");

        X509Certificate cert;
        try {
            cert = TrustTools.loadX509Certificate(file);
        } catch (Exception e) {
            throw new TrustException(e);
        }
        checkCanBeAdded(cert);
        if (cert == null) {
            throw new TrustException("Certificate could not be loaded");
        }

        return trustCertificate(cert);
    }

    protected boolean canBeAdded(X509Certificate certificate) {
        return true;
    }

    public static class CantBeAddedException extends TrustException {
        public CantBeAddedException() {
        }

        public CantBeAddedException(String message) {
            super(message);
        }

        public CantBeAddedException(Throwable cause) {
            super(cause);
        }

        public CantBeAddedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
