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

package org.jdesktop.jdic.misc.impl;

import com.itbs.util.GeneralUtils;
import org.jdesktop.jdic.misc.Alerter;

import java.awt.*;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Description of the Class
 *
 * @author F‡bio Castilho Martins (fcmartins@bol.com.br)
 * @since April 24, 2005
 */
public class WinAlerter extends Alerter {

    private boolean isLoaded;

    private static Set <Frame> alerted = new CopyOnWriteArraySet<Frame>();
    private static int delay;
    Logger logger  = Logger.getLogger(WinAlerter.class.getName());
    /**
     * Do not use. Call the Alerter.newInstance() factory method instead.
     * @throws SecurityException load failure
     * @throws UnsatisfiedLinkError load failure
     */
    public WinAlerter() throws SecurityException, UnsatisfiedLinkError {
        if (!isLoaded) {
            loadLib();
        }
    }

    private synchronized void loadLib() {
        if (!isLoaded) {
//            System.out.println("about to load the jdic_misc library");
            try {
                System.loadLibrary("jdic_misc");
            } catch (RuntimeException e) {
                logger.log(Level.SEVERE, "Exception loading WinAlerter", e);
                throw e;
            } catch (Error e) {
                logger.log(Level.SEVERE, "Error loading WinAlerter", e);
                throw e;
            }
            logger.log(Level.INFO, "loaded Alerter library");
            delay = (int) getBlinkRate(); //milliseconds
            isLoaded = true;
//            System.out.println("loaded");
        }
    }

    /**
     * It will blink the TaskBar button and the Program Window at a
     * regular rate (controlled by the user's preferences), until the Window
     * gets the focus.
     * @param frame  to flash
     */
    public synchronized void alert(final Frame frame) {
        if (frame.isFocused() || alerted.contains(frame)) return;
        alerted.add(frame);

        final Timer loop = new Timer("WinAlerter");
        loop.schedule(new TimerTask() {
            public void run() {
                GeneralUtils.sleep(200); // slow it down a bit
                logger.finest("WA: focused: " + frame.isFocused() + " dislayable: " + frame.isDisplayable() + " visible: " + frame.isVisible());
//                if (!frame.isFocusOwner()) {    // bug fix
                if (frame.isDisplayable() && !frame.isFocused()) { //frame.isVisible() && 
                    alertWindows(frame);
                } else {
                    loop.cancel();
                    alerted.remove(frame);
                }
            }
        }, 500, delay);
    }

    private native void alertWindows(Frame frame);

    /**
     * Return the Systems Blink Rate, this is a user preference, which controls the blink rate on Windows.
     * Microsoft's Design Guidelines states that an application should respect this user configuration.
     * @return The Native Blink Rate.
     */
    private native long getBlinkRate();


    public boolean isAlertSupported() {
        return true;
    }

}