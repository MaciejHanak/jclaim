package com.itbs.aimcer.bean;

/**
 * @author Alex Rass
 * @since Feb 12, 2006
 */
public interface GroupFactory {
    Group create(String group);
    Group create(Group group);
}
