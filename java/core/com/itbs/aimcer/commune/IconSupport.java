package com.itbs.aimcer.commune;

import com.itbs.aimcer.bean.Contact;

import java.io.File;

/**
 * @author Alex Rass
 * @since Apr 24, 2005
 */
public interface IconSupport extends Connection {
    /**
     * Makes a request to download the icon.
     * Later event listener is notified when the icon arrives.
     * @param contact to request the icon for
     */
    void requestPictureForUser(Contact contact);

    /**
     * Use this picture for me.
     *
     * @param picture filename
     */
    void uploadPicture(File picture);

    /**
     * Will remove the picture.
     */
    void clearPicture();
}
