package com.itbs.util;

/**
 * Allows sychronized changes.
 * @author Alex Rass on Nov 7, 2004
 *
 */
public class SyncBoolean {
    boolean value;

    public SyncBoolean(boolean value) {
        this.value = value;
    }

    public synchronized boolean isValue() {
        return value;
    }

    public synchronized void setValue(boolean value) {
        this.value = value;
    }

    public String toString() {
        return super.toString() + ": " + value; 
    }
}
