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

import net.kano.joscar.DefensiveTools;
import net.kano.joustsim.Screenname;
import net.kano.joustsim.trust.SignerTrustManager;
import net.kano.joustsim.trust.TrustTools;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class PermanentSignerTrustManager
        extends DefaultCertificateTrustManager implements SignerTrustManager {

    public PermanentSignerTrustManager(Screenname ownerAccount) {
        super(ownerAccount);
    }

    protected boolean canBeAdded(X509Certificate certificate) {
        DefensiveTools.checkNull(certificate, "certificate");

        return TrustTools.isCertificateAuthority(certificate);
    }

    public synchronized boolean isSignedByTrustedSigner(X509Certificate cert) {
        DefensiveTools.checkNull(cert, "cert");

        List<X509Certificate> certs = getTrustedCertificates();
        for (int i = 0; i < certs.size(); i++) {
            X509Certificate signer = certs.get(i);
            try {
                cert.verify(signer.getPublicKey());

                // if no exception was thrown, this certificate is verified
                return true;
            } catch (Exception ignored) {
                return false;
            }
        }
        return false;
    }

    public synchronized List<X509Certificate> getTrustedSigners(X509Certificate cert) {
        DefensiveTools.checkNull(cert, "cert");

        List signed = new ArrayList();
        List<X509Certificate> certs = getTrustedCertificates();
        for (int i = 0; i < certs.size(); i++) {
            X509Certificate signer = certs.get(i);
            try {
                cert.verify(signer.getPublicKey());

                // if no exception was thrown, this certificate is verified
                signed.add(cert);
            } catch (Exception ignored) { }
        }

        return signed;
    }
}
