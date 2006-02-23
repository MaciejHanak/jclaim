package com.itbs.aimcer.bean;

import com.itbs.aimcer.commune.Connection;
import com.itbs.util.GeneralUtils;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Created by Alex Rass on Sep 9, 2004
 */
public class GroupWrapper implements Group, Renderable {
    private static Map<String,GroupWrapper> wrappers = new HashMap<String, GroupWrapper>(10);

    private final static Color SELECTED = new Color(127, 127, 240);

    // Local stuff
    String name;
    private List<Nameable> contacts = new CopyOnWriteArrayList<Nameable>();
    GroupPreferences preferences;
    private SelectableLabel displayComponent;
    static class SelectableLabel extends JLabel{
        private boolean selected;
        public boolean isSelected() {
            return selected;
        }
        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public void paint(Graphics g) {
            if (!selected) {
                Graphics2D g2d = (Graphics2D)g;
                g2d.setPaint(new GradientPaint(0, (getHeight() + 1) / 2, Color.WHITE, 0, getHeight(), Color.GRAY, false));
                g2d.fillRect(0,0,getWidth(),getHeight());
            } else {
                setBackground(SELECTED);
            }
            super.paint(g);   //Todo change
        }
    }
//    private static Map<String, GroupWrapper> wrappers = new HashMap<String, GroupWrapper>(10);

    private GroupWrapper(String name) {
        this.name = name;
        displayComponent = new SelectableLabel();
        preferences = ClientProperties.findGroupPreferences(name);
    }

    public int size() {
        return contacts.size();
    }

    public int sizeOnline() {
        int count = 0;
        for (Nameable nameable: contacts) {
            if (nameable instanceof ContactWrapper) {
                if (((ContactWrapper)nameable).getStatus().isOnline()) {
                    count++;
                }
            }
        }
        return count;
    }

    public void clear(Connection connection) {
        for (Nameable nameable: contacts) {
            if (nameable instanceof ContactWrapper) {
                if (nameable == connection) {
                    contacts.remove(nameable);
                }
            }
        }
//        contacts.clear();
    }

    public Nameable get(int index) {
        return contacts.get(index);
    }

    public Nameable add(Nameable contact) {
        if (!contacts.contains(contact))
            contacts.add(contact);
        return contact;
    }

    public boolean remove(Nameable contact) {
        return contacts.remove(contact);
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

    public boolean isShrunk() {
        return preferences.isFold();
    }

    public void swapShrunk() {
        preferences.setFold(!preferences.isFold());
    }

    public static GroupWrapper create(String group) {
        GroupWrapper result = wrappers.get(GeneralUtils.getSimplifiedName(group));
        if (result==null) {
//            System.out.println("creating a new group wrapper for " + group);
            result = new GroupWrapper(group);
            wrappers.put(GeneralUtils.getSimplifiedName(group), result);
        }
        return result;
    }

    public static GroupWrapper create(Group group) {
        return create(group.getName());
    }

    public static final Font NORM = new Font("Arial", Font.BOLD, ClientProperties.INSTANCE.getFontSize() + 1);
    public static final Color GROUP = Color.YELLOW;

    public JComponent getDisplayComponent(boolean isSelected, boolean cellHasFocus) {
        String sign = isShrunk() ? "+ " : "- ";
        displayComponent.setFont(NORM);
        displayComponent.setText("  "  + sign + getName() + " - " + sizeOnline() + "/" + size());
        /* {
            public void paint(Graphics g) {
                g.setColor(GROUP);
                g.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
                super.paint(g);
            }
        };*/
        displayComponent.setSelected(isSelected);
//        displayComponent.setBackground(isSelected ? SELECTED : ListRenderer.NOT_SELECTED);
        displayComponent.setOpaque(isSelected);
//        if (isSelected && !cellHasFocus)
//            displayComponent.setBackground(ListRenderer.SELECTED_NO_FOCUS);
        return displayComponent;

    }
}
