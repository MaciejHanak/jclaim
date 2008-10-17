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

import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * Makes sure the action is only done once, within a given delay.                  <br>
 * Along with the alive monitor (can monitor a window).                            <br>
 * Useful for statuses etc.                                                        <br>
 * Marks will trigger new delay each time.                                         <br>
 * If the code needs to run on AWT, arrange for it yourself.                       <br>
 *                                                                                 <br>
 * <code>                                                                          <br>
 *   JLabel label = new JLabel("Saving");                                          <br>
 *   ...                                                                           <br>
 *   DelayedThread dat = new DelayedThread("Status Visibility",                    <br>
 *     new StillAliveMonitor() {                                                   <br>
 *          public boolean isAlive() {                                             <br>
 *              return label.isDisplayable();                                      <br>
 *          }                                                                      <br>
 *     }                                                                           <br>
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
public class DelayedThread extends Thread {
    private Logger logger = Logger.getLogger(DelayedThread.class.getName());
    /**
     * Determines if we should run the block b/c someone said "mark!".
     * False if no one has initiated or if the block already ran.
     *
     * In other words: is the final block allowed to run yet? T/F
     */
    private SyncBoolean flag = new SyncBoolean(false);
    /**
     * Determines if we want run block to be executed each time someone hits mark
     */
    private boolean runStartEachMark = false;
    
    private final long delay;
    /** Object on which to sync. */
    protected final Object notifyObject = new Object();
    private final ReentrantLock lock = new ReentrantLock();

    /** Used to control the loop.
     * <br>Other processes can call stopProcessing() to control the run. */
    private boolean internalCheck = true;

    /** Snippet of code to run. */
    protected Runnable runThisFirst, runThisLast;

    /** Parent window. */
    private StillAliveMonitor stillAliveMonitor;

    /** Date of last check. */
    private long clock;

    /**
     * Allows thread to passively detect when to terminate by itself (next time it's awake).
     */
    static public interface StillAliveMonitor {
        /**
         * Returns the alive status.
         * @return true if the monitored element is still alive
         */
        boolean isAlive();
    }
    
    /**
     * Constructor.
     * @param threadName      Name of the thread
     * @param delayMillis     delay in milliseconds
     * @param aliveMonitor    allows to see when it's time to die (window is destroyed etc.)
     * @param snippetStart    code to run before.  nulls allowed. will run on each mark().
     * @param snippetEnd      code to run after @NotNull
     */
    public DelayedThread(String threadName, long delayMillis, StillAliveMonitor aliveMonitor, Runnable snippetStart, Runnable snippetEnd) {
        this(threadName, delayMillis, aliveMonitor, snippetStart, false, snippetEnd);
    }

    public DelayedThread(String threadName, long delayMillis, StillAliveMonitor aliveMonitor, Runnable snippetStart, boolean runStartEachMark, Runnable snippetEnd) {
        runThisFirst = snippetStart;
        if (snippetEnd == null)
            throw new NullPointerException("If you don't need anything run with a delay - why this class?");
        runThisLast = snippetEnd;
        delay = delayMillis;
        stillAliveMonitor = aliveMonitor;
        this.runStartEachMark = runStartEachMark;
        setName(threadName);
        setPriority(Thread.MIN_PRIORITY);
        setDaemon(true); // don't stop VM from exit
    }

    /**
     * Starts the (delayed) processing request.
     */
    public void mark() {
        clock = System.currentTimeMillis(); // make sure it d/n start w/o us running first code
        lock.lock(); // make sure we don't run/lock from multiple callers.
        try {
            if (runThisFirst != null && (!flag.isValue() || runStartEachMark))
                runThisFirst.run();
//            runThisFirst = null; // done
            flag.setValue(true);
            clock = System.currentTimeMillis();
            synchronized(notifyObject) { // wake it up
                notifyObject.notify();
            }
        } finally {
            lock.unlock();
        }
        Thread.yield();
    }

    /**
     * Kills the thread.
     */
    public void stopProcessing() {
        internalCheck = false;
        lock.lock();
        try {
            synchronized(notifyObject) { // wake it up
                notifyObject.notify();
            }
        } finally {
            lock.unlock();
        }
    }

    public void run() {

        try {
//            sleep(delay); // this way the window gets created.
            while (internalCheck && stillAliveMonitor.isAlive()) {
                if (!flag.value) { // if nothing to do - then wait for notify
                    logger.finest("Waiting indefinitely.");
                    synchronized(notifyObject) { // wake it up
                        notifyObject.wait();
                    }
                }
                long newDelay = delay - (System.currentTimeMillis() - clock); // correct for other marks()
                Thread.sleep(newDelay>0?newDelay:100); // negative - we are cycling through
                logger.finest("Waited for " + newDelay);
                //    no one stopped us && first one ran && (old clock + del < now)
                    if (internalCheck && flag.isValue() && (clock + delay <= System.currentTimeMillis())) {
                        flag.setValue(false); // setting so no one will enter here again
                        runThisLast.run();
                    } else {
                        logger.finest("No run for now. flag:" + flag
                                + " clock: " + (clock + delay < System.currentTimeMillis())
                                + " clock details: " + clock + " " + delay + " " + System.currentTimeMillis());
                    }
                } // while
        } catch (InterruptedException e) {
            // don't care
        }
//        log.info("Delayed Thread Finished work.");
    }

    /**
     * Someone may want it back.
     * @return reference
     */
    public Runnable getRunThisFirst() {
        return runThisFirst;
    }

    /**
     * Someone may want it back.
     * @return reference
     */
    public Runnable getRunThisLast() {
        return runThisLast;
    }
} // class FlagThread
