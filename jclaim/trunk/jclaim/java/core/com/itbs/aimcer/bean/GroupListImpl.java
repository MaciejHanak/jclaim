package com.itbs.aimcer.bean;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This is a very simplistic implementation of GroupList.
 * You may want yours to be more complex.
 * @author Alex Rass
 * @since Jul 5, 2007 2:06:34 AM
 */
public class GroupListImpl implements GroupList {
    private List<Group> arrayList = new CopyOnWriteArrayList<Group>();
    public int size() {
        return arrayList.size();
    }

    public Group get(int index) {
        return arrayList.get(index);
    }

    public Group add(Group group) {
        if (!arrayList.contains(group)) // no duplicates
            arrayList.add(group);
        return group;
    }

    public void remove(Group group) {
        arrayList.remove(group);
    }

    public Group[] toArray() {
        return arrayList.toArray(new Group[arrayList.size()]);
    }

    public void clear() {
        arrayList.clear();
    }

} // class