package com.itbs.util;

import javax.swing.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alex Rass
 * @since Feb 12, 2006
 */
public class ImageCache {
    private static Map<Object,ImageIcon> imageCache = Collections.synchronizedMap(new HashMap<Object, ImageIcon>());
    private static Map<Object,String> imageNames = Collections.synchronizedMap(new HashMap<Object, String>());

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
        return imageCache.get(key);
    }

}
