package com.itbs.aimcer.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

/**
 *
 * This is so that white box vendors can have the ads.
 *
 * @author Alex Rass
 * @since Feb 13, 2007 1:26:02 AM
 */
public class AdPanel extends JPanel {
    String url = "http://www.vois.com";
    long lastClick;
    public AdPanel() {
        super(new BorderLayout());
        JLabel comp = new JLabel(ImageCacheUI.ICON_AD.getIcon());
        comp.setToolTipText(url);
        add(comp);
        comp.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (lastClick + 5000 < System.currentTimeMillis()) { // if they haven't clicked in 2 seconds, go!
                    try {
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (Exception e1) {
                        // noone cares
                    }
                }
                lastClick = System.currentTimeMillis();
            }
        });
    }
}
