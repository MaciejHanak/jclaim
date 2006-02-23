package com.itbs.aimcer.bean;

/**
 * Preferences per group.
 * Things you may want to save.
 * @author Created by Alex Rass on Sep 11, 2004
 */
public class GroupPreferences {
    /** UID for a given provider. */
    private String name;
    private boolean fold;

    public GroupPreferences(String name) {
        this.name = name;
    }

    /**
     * Determines if this contact will be displayed in the list.
     * @return true if hidden.
     */
    public boolean isFold() {
        return fold;
    }

    /**
     * Determines if this contact will be displayed in the list.
     * @param fold true if hidden.
     */
    public void setFold (boolean fold) {
        this.fold = fold;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns a hash code value for the object.
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the obj
     *         argument; <code>false</code> otherwise.
     * @see #hashCode()
     * @see java.util.Hashtable
     */
    public boolean equals(Object obj) {
        return obj.hashCode() == hashCode();
    }

    // **********<     XML Functions only!  do not use directly!   >***********
    /**
     * Recreation for XML only!
     */
    public GroupPreferences() {
    }
} // class GroupPreferences
