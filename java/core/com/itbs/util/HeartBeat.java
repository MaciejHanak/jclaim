package com.itbs.util;

import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Performs Heartbeat Services for all.
 * 
 * @author Alex Rass
 * @since Nov 10, 2008 8:43:54 PM
 */
public class HeartBeat extends Thread {
    private static final Logger log = Logger.getLogger(HeartBeat.class.getName());
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private static long TIMEOUT = 20*1000; // 20 seconds
    private List <MonitoredItem> monitored = new CopyOnWriteArrayList<MonitoredItem>();
    private boolean shutDown = false;
    public static HeartBeat INSTANCE;
    
    static {
        INSTANCE = new HeartBeat();
        INSTANCE.start();
    }
    
    /** Implement to monitor self */
    static public abstract class MonitoredItem {
        boolean running;
        long lastRun;

        protected MonitoredItem() {
            update();
        }

        public abstract boolean testAlive();
        /** Implement to do something when action fails */
        public abstract void actionFail();
        /**
         * Implement to do something when action doesn't fail.
         * For debugging mostly.
         */
        protected void actionSuccseed() {}

        /** Updates time */
        void update() {
            lastRun = System.currentTimeMillis();
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        boolean isTimeToGo() {
            return !running && System.currentTimeMillis() - lastRun > TIMEOUT;
        }

        public String toString() {
            return "MonitoredItem [last run:"+new Date(lastRun)+", running:"+running+"]"+hashCode(); 
        }
    }


    public HeartBeat() {
        executorService = Executors.newCachedThreadPool(new ThreadFactory() {
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "HeartBeat.Executor");
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    public void startMonitoring(MonitoredItem monitoredItem) {
        if (!monitored.contains(monitoredItem)) {
            monitored.add(monitoredItem);
        }
        synchronized (monitored) {
            monitored.notifyAll();
        }
    }

    public void stopMonitoring(MonitoredItem monitoredItem) {
        monitored.remove(monitoredItem);
    }

    public void stopHeartbeat() {
        shutDown = true;
        monitored.notifyAll();
    }

    public void run() {
        while (!shutDown) {
            try {
                synchronized(monitored) {
                    monitored.wait(TIMEOUT+100);
                }
            } catch (InterruptedException e) {
            }
            for (final MonitoredItem monitoredItem : monitored) {
                if (monitoredItem.isTimeToGo()) {
                    monitoredItem.setRunning(true);
                    Future <Boolean> future = executorService.submit(new Callable<Boolean>() {
                        public Boolean call() {
                            return testAlive(monitoredItem);
                        }
                    });
                    Boolean result = Boolean.FALSE;
                    try {
                        result = future.get(TIMEOUT, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.log(Level.INFO, "Failed alive test "+ monitoredItem, e);
                    }
                    if (result) {
                        monitoredItem.actionSuccseed();
                    } else {
                        monitoredItem.actionFail();
                    }
                    monitoredItem.setRunning(false);
                }
            }
        }
    }

    /**
     * Tests the THINGIE properly.
     * @param monitoredItem item to monitor
//     * @return true if tests passed. false if it blew up or failed.
     */
    boolean testAlive(MonitoredItem monitoredItem) {
        monitoredItem.update();
        long timeIn = System.currentTimeMillis();
        boolean result = false;
        try {
            result = monitoredItem.testAlive();
        } catch (Exception e) {
            log.log(Level.INFO, "Failed alive test", e);
        }
        long duration = System.currentTimeMillis() - timeIn;

        result = result && duration<=TIMEOUT;
        return result;
    }
}
