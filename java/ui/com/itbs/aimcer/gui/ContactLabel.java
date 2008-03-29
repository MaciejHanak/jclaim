package com.itbs.aimcer.gui;

import com.itbs.aimcer.bean.ClientProperties;
import com.itbs.aimcer.bean.ContactWrapper;
import com.itbs.aimcer.bean.Group;
import com.itbs.aimcer.commune.MessageSupport;

import javax.swing.*;
import java.awt.*;

/**
 * Acts as a label representation for a contact.
 * Started this class since we are gonna need more contact labels than contacts. As more mediums support multiple contacts per group. Plus we need a separate one for meta contacts etc.
 * @author Alex Rass
 * @since Mar 28, 2008 2:55:43 AM
 */
public class ContactLabel extends JLabel {
    public static final Font NORM = new Font("sansserif", Font.PLAIN, ClientProperties.INSTANCE.getFontSize());
    public static final Font BOLD = new Font("sansserif", Font.BOLD, ClientProperties.INSTANCE.getFontSize());
    public static final Font OFF = new Font("sansserif", Font.ITALIC, ClientProperties.INSTANCE.getFontSize() - 1);

    public static final Color PRESENT = Color.BLACK;
    public static final Color AWAY = Color.GRAY;
    ContactWrapper contact;
    Group group;

    public ContactLabel(ContactWrapper contact) {
        this.contact = contact;
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
            setIcon(null);
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
}
