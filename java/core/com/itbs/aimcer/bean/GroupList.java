package com.itbs.aimcer.bean;



/**
 * @author Alex Rass
 * @since Sep 22, 2004
 */
public interface GroupList {
    int size();
    Group get(int index);
    Group add(Group group);
    void remove(Group group);
    Object[] toArray();
    void clear();
}
