package com.itbs.aimcer.bean;

/**
 * @author Alex Rass
* @since Oct 15, 2008 11:06:30 AM
*/
public class GroupFactoryImpl implements GroupFactory {
    public GroupFactoryImpl() {
//            new Exception("creating gwf").printStackTrace();
    }

    public Group create(String group) {
        return GroupWrapper.create(group);
    }

    public Group create(Group group) {
        return GroupWrapper.create(group);
    }

    /**
     * Returns the reference to main static list.
     * Anyone who has sessions:
     *   should not have this be static, but session based hash.
     *   basically map the factory based on session.
     *
     * @return list of groups.
     */
    public GroupList getGroupList() {
        return groupList;
    }

    /**
     * Used to assign stored list.
     * @param groupList to assign
     */
    public void setGroupList(GroupList groupList) {
        this.groupList = groupList;
    }

    /**
     * Anyone who has sessions - should not have this be static, but session based hash.
     */
    private static GroupList groupList  = new GroupListImpl();

}
