package com.itbs.aimcer.gui.userlist;

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.commune.MessageSupport;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Acts as a label representation for a contact.
 * Started this class since we are gonna need more contact labels than contacts.
 * As more mediums support multiple contacts per group. Plus we need a separate one for meta contacts etc.
 *
 * @author Alex Rass
 * @since Mar 28, 2008 2:55:43 AM
 */
public class ContactLabel extends JLabel implements Renderable {
    private final static Color SELECTED = new Color(127, 190, 240);
    public static final int SIZE_WIDTH_INC = 8;
    public static final int SIZE_HEIGHT_INC = 2;

    public static final Font NORM = new Font("sansserif", Font.PLAIN, ClientProperties.INSTANCE.getFontSize());
    public static final Font BOLD = new Font("sansserif", Font.BOLD, ClientProperties.INSTANCE.getFontSize());
    public static final Font OFF = new Font("sansserif", Font.ITALIC, ClientProperties.INSTANCE.getFontSize() - 1);

    public static final Color PRESENT = Color.BLACK;
    public static final Color AWAY = Color.GRAY;
    ContactWrapper contact;
    Group group;
    static Map<String, ContactLabel> INSTANCES = new ConcurrentHashMap <String, ContactLabel> ();

    public ContactLabel(ContactWrapper contact) {
        this.contact = contact;
        update();
    }

    private ContactLabel(ContactWrapper contact, Group group) {
        this.contact = contact;
        this.group = group;
        INSTANCES.put(generateUID(contact, group), this);
        update();
    }

    public static String generateUID(Contact cw) {
        return (cw.getName()+ "|" + cw.getConnection() + "|" + cw.getConnection().getUser().getName()).intern(); // will likely get it again and again
    }

    protected static String generateUID(Contact cw, Group group) {
        return (cw.getName()+ "|" + cw.getConnection() + "|" + cw.getConnection().getUser().getName() + "|" + group.getName()).intern(); // will likely get it again and again
    }

    public static ContactLabel construct(ContactWrapper contact, Group group) {
        ContactLabel returnable = INSTANCES.get(generateUID(contact, group)); // put is inside constructor
        if (returnable==null) {
            return new ContactLabel(contact, group);
        } else {
            returnable.update();
            return returnable;
        }
    }

    public JComponent getDisplayComponent(boolean isSelected, boolean cellHasFocus) {
        setBackground(isSelected ? SELECTED: ListRenderer.NOT_SELECTED);
        setOpaque(isSelected);
        return this;
    }

    public void update() {
        if (contact.getStatus().isOnline()) {
            setText(contact.getDisplayName());
            setIcon(contact.getIcon());
            setFont(NORM);
            setForeground(contact.getStatus().isAway() ? AWAY : PRESENT);
            if (contact.getConnection() instanceof MessageSupport) {
                final String temp = contact.getName() + " on " + contact.getConnection().getServiceName() + " as " + ((MessageSupport) contact.getConnection()).getUserName() + (contact.getStatus().isAway() ? (" Idle for " + contact.getStatus().getIdleTime() + "m") : "");
                setToolTipText(temp);
            }
        } else {
            setText(contact.getDisplayName() + (contact.getStatus().isOnline()?" (Online)":" (Offline)"));
//            setIcon(null);
            setIcon(contact.getIcon());
            setFont(OFF);
            setForeground(AWAY);
            final String temp = "Last Seen on " + contact.getConnection().getServiceName() + (contact.getPreferences().getLastConnected() == null ? " Not yet." : (" " + contact.getPreferences().getLastConnected()));
            setToolTipText(temp);
        }
    }

    public ContactWrapper getContact() {
        return contact;
    }

    public Group getGroup() {
        return group;
    }

    // -------------- This removes the need for a whole panel.
    /**
     * Up the width.
     * @return new width.
     */
    public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        dim.width += SIZE_WIDTH_INC;
        dim.height += SIZE_HEIGHT_INC;
        return dim;
    }

    protected void paintComponent(Graphics g) {
        g.translate(SIZE_WIDTH_INC, SIZE_HEIGHT_INC/2);
        super.paintComponent(g);  
    }
}
