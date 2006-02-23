package com.itbs.gui;

import com.itbs.util.DelayedThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Makes sure the action is only done once, based on a delay.                      <br>
 * Persists along with the window                                                  <br>
 * Useful for statuses etc.                                                        <br>
 * If the code needs to run on AWT, arrange for it yourself.                       <br>
 * Make sure window is displayable!                                                <br>
 *                                                                                 <br>
 * <code>                                                                          <br>
 *   JLabel label = new JLabel("Saving");                                          <br>
 *   ...                                                                           <br>
 *   DelayedThread dat = new DelayedThread("Status Visibility",                    <br>
 *     new Runnable() {                                                            <br>
 *        public void run() {                                                      <br>
 *            label.setVisible(true);                                              <br>
 *        }                                                                        <br>
 *     },                                                                          <br>
 *     new Runnable() {                                                            <br>
 *        public void run() {                                                      <br>
 *            label.setVisible(false);                                             <br>
 *        }                                                                        <br>
 *     },                                                                          <br>
 *   );                                                                            <br>
 *   dat.start();                                                                  <br>
 *   ...                                                                           <br>
 *   dat.mark();                                                                   <br>
 *   ...                                                                           <br>
 * </code>                                                                         <br>
 * @author Alex Rass
 * @since July 23, 2005
 * Copyright 2004.
 */
public class DelayedActionThread extends DelayedThread {
    /**
     * Constructor.
     * @param threadName       Name of the thread
     * @param ownerComponent  component who's window to monitor (dies when window is destroyed)
     * @param snippetStart    code to run before
     * @param snippetEnd      code to run after
     */
    public DelayedActionThread(String threadName, long delay, Component ownerComponent, Runnable snippetStart, Runnable snippetEnd) {
        this(threadName, delay,
             SwingUtilities.getWindowAncestor(ownerComponent),
             snippetStart,
             snippetEnd
        );
    }
    /**
     * Constructor.
     * @param threadName       Name of the thread
     * @param window          window to monitor (dies when window is destroyed)
     * @param snippetStart    code to run before
     * @param snippetEnd      code to run after
     */
    public DelayedActionThread(String threadName, long delay, final Window window, Runnable snippetStart, Runnable snippetEnd) {
        super (threadName,
              delay,
              new StillAliveMonitor() {
                  public boolean isAlive() {
                      return window.isDisplayable();
                  }
              },
              snippetStart,
              snippetEnd
        );
        if (window == null)
            throw new NullPointerException("Component is not contained within a window.  If intentional, override to ensure no infinite loops.");
        window.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                stopProcessing();
            }
        });
    }
} // class FlagThread
