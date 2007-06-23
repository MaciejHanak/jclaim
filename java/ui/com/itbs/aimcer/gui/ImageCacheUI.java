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

import com.itbs.aimcer.commune.joscar.ICQConnection;
import com.itbs.aimcer.commune.joscar.OscarConnection;
import com.itbs.aimcer.commune.msn.JmlMsnConnection;
import com.itbs.aimcer.commune.msn.MSNConnection;
import com.itbs.aimcer.commune.smack.GoogleConnection;
import com.itbs.aimcer.commune.smack.SmackConnection;
import com.itbs.aimcer.commune.ymsg.YMsgConnection;
import com.itbs.gui.ErrorDialog;
import com.itbs.util.ClassUtil;
import com.itbs.util.ImageCache;

import javax.swing.*;
import java.io.IOException;


/**
 * Provides an easy and simple cache mechanism for images.
 * All static assignments.
 *
 * @author Alex Rass
 * @since Mar 27, 2004
 */
public class ImageCacheUI extends ImageCache {
    public static ImagePair ICON_DELETE;

    public static ImagePair ICON_PLUS;
    public static ImagePair ICON_PLUS_PRESSED;
    public static ImagePair ICON_MINUS;
    public static ImagePair ICON_MINUS_PRESSED;
    public static ImagePair ICON_MINUS_ROLLOVER;
    public static ImagePair ICON_RELOAD;
    public static ImagePair ICON_RELOAD_PRESSED;
    public static ImagePair ICON_JABBER, ICON_AIM, ICON_ICQ, ICON_YAHOO, ICON_MSN, ICON_GOOGLE;
    public static ImagePair ICON_JC, ICON_JC_ANIM;
    public static ImagePair ICON_WIRELESS, ICON_EMAIL, ICON_TYPE, ICON_HISTORY, ICON_INFO;
    
    public static ImagePair ICON_AD;

    static {
        try {
            ICON_DELETE = new ImagePair("trash.gif");
            ICON_PLUS = new ImagePair("plus.gif");
            ICON_PLUS_PRESSED = new ImagePair("plus_down.gif");
            ICON_MINUS = new ImagePair("minus.gif");
            ICON_MINUS_PRESSED = new ImagePair("minus_down.gif");
//            ICON_MINUS_ROLLOVER = new ImagePair("minus_roll.gif"));
            ICON_RELOAD = new ImagePair("reload.gif");
            ICON_RELOAD_PRESSED = new ImagePair("reload_down.gif");
            ICON_JABBER = new ImagePair("jabber.gif", SmackConnection.class);
            ICON_AIM = new ImagePair("aim.gif", OscarConnection.class);
            ICON_ICQ = new ImagePair("icq.gif", ICQConnection.class);
            ICON_YAHOO = new ImagePair("yahoo.gif", YMsgConnection.class);
            ICON_GOOGLE = new ImagePair("google.gif", GoogleConnection.class);
            ICON_MSN = new ImagePair("msn.gif", MSNConnection.class);
            ICON_MSN = new ImagePair("msn.gif", JmlMsnConnection.class);
            ICON_WIRELESS = new ImagePair("wireless.gif");
            ICON_EMAIL = new ImagePair("email.gif");
            ICON_HISTORY = new ImagePair("history.gif");
            ICON_INFO = new ImagePair("info.gif");
            if (System.getProperty("USE_ADS") != null) {
                ICON_JC = new ImagePair("vois.gif");
                ICON_JC_ANIM = new ImagePair("vois_anim.gif");
            } else {
                ICON_JC = new ImagePair("jc.gif");
                ICON_JC_ANIM = new ImagePair("jc_anim.gif");
            }
            ICON_TYPE = new ImagePair("typing.gif");
        } catch (IOException e) {
            ErrorDialog.displayError(null, "Could not load image resources", e);
        }
        
        try {
            ICON_AD = new ImagePair("logo_vois.gif");
        } catch (IOException e) {
            // noone cares
        }

    }


    /**
     * Manages the mapping and dualistic relation b/w name and image.
     * Too bad stadnard library d/n do this.  Description shouldn't be used for this, imho.
     */
    public static class ImagePair {
        String name;
        ImageIcon icon;

        public ImagePair(String name) throws IOException {
            this.name = name;
            this.icon = new ImageIcon(ClassUtil.getURLFromCallersClassDirectory(name));
        }

        public ImagePair(String name, Class refClass) throws IOException {
            this(name);
            addImage(refClass, icon);
            addImageName(refClass, name);
        }

        public String getName() {
            return name;
        }

        public ImageIcon getIcon() {
            return icon;
        }
    }
} // class ImageCacheUI
