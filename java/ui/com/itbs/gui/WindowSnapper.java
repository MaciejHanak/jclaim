package com.itbs.gui;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Provides an easy framework for snapping windows.
 * <p>
 * Singleton.
 * <p>
 * <b>Assumption:</b>  You can only move one window at a time.
 * <p>
 * Usage: <br>
 *      frame.addComponentListener(WindowSnapper.instance());
 *      frame.setVisible(true);
 * @author Alex Rass
 * @since Nov 27, 2005
 */
public class WindowSnapper  extends ComponentAdapter {
    private boolean locked = false;
    private int snapDistance = 25;
    private boolean enabled=true;
    private static WindowSnapper INSTANCE;

    private WindowSnapper() { }

    public static synchronized WindowSnapper instance() {
        if (INSTANCE == null)
            INSTANCE = new WindowSnapper();
        return INSTANCE;
    }

    /**
     * Sets the distance.
     * <p>
     * Does not allow the distance to be more than 1/2 height of the screen.<br>
     * Setting to 0 will turn off snapping. <br>
     * Setting to a valid value will reenabled snapping if disabled. <br>
     * @param distance from which to snap
     */
    public void setSnapDistance(int distance) {
        if (distance == 0)
            enabled = false;
        if (distance > 0 && distance < Toolkit.getDefaultToolkit().getScreenSize().height / 2) {
            snapDistance = distance;
            enabled = true;
        }
    }

    /**
     * Distance from which to snap
     * @return distance from which to snap
     */
    public int getSnapDistance() {
        return snapDistance;
    }

    /**
     *
     * @return if snapping is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     *
     * @param enabled snapping state
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Handles snapping.
     * @param evt from the window.
     */
    public void componentMoved(ComponentEvent evt) {
        if (!enabled) {
            locked = false;
            return;
        }
        if (locked) return;
        Dimension windowSize = Toolkit.getDefaultToolkit().getScreenSize();
        Component component = evt.getComponent(); // need to cache - slow call
        int newX = component.getX();
        int newY = component.getY();
        int compWidth = component.getWidth();
        int compHeight = component.getHeight();
        // top
        if(newY < snapDistance) {
            newY = 0;
        } else // bottom
            if(newY > windowSize.getHeight() - compHeight - snapDistance) {
                newY = (int)windowSize.getHeight()-compHeight;
            }
        // left
        if(newX < snapDistance) {
            newX = 0;
        } else // right
            if(newX > windowSize.getWidth() - compWidth - snapDistance) {
                newX = (int)windowSize.getWidth()-compWidth;
            }
        // make sure we don't get into a recursive loop when the
        // set location generates more events
        locked = true;
        component.setLocation(newX,newY);
        locked = false;
    }

}