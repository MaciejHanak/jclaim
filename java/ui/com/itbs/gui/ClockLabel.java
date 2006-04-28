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

package com.itbs.gui;

import javax.swing.*;
import java.text.DateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Provides a VERY simple implementatin of a clock.
 * JLabel with a tooltip.
 */
public class ClockLabel extends JLabel {
    Timer timer = new Timer("ClockLabel", true); 
    DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT);
    boolean started;
    
    public ClockLabel() {
        setOpaque(false);
        setVisible(true);
        ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
        toolTipManager.registerComponent(this);
        started = true;
    }
    
    @Override
    public void setVisible(boolean aFlag) {
        if (aFlag) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    setText(timeFormatter.format(new Date()));
                }
            }, 500, 60 * 1000);
        } else {
            timer.cancel();
        }
        super.setVisible(aFlag);
    }

    @Override
    public String getToolTipText() {
        return new Date().toString();
    }
}