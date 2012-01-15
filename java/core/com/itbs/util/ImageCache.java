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

package com.itbs.util;

import javax.swing.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * @author Alex Rass
 * @since Feb 12, 2006
 */
public class ImageCache {
    private static Map<Object,ImageIcon> imageCache = new ConcurrentHashMap<Object, ImageIcon>();
    private static Map<Object,String> imageNames = new ConcurrentHashMap<Object, String>();
    private static transient final Logger log = Logger.getLogger(ImageCache.class.getName());

    public static void addImageName(Object key, String name) {
        if (key!=null && name!=null)
            imageNames.put(key, name);
    }

    public static String getImageName(Object key) {
        return imageNames.get(key);
    }

    public static void addImage(Object key, ImageIcon icon) {
        if (key!=null && icon!=null)
            imageCache.put(key, icon);
    }

    public static ImageIcon getImage(Object key) {
        ImageIcon imageIcon = imageCache.get(key);
        if (imageIcon == null) {
            log.info("Failed to find an icon for "+ key);
        }
        return imageIcon;
    }

}
