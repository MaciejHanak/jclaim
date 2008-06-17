package com.itbs.aimcer.tabbed;

import com.itbs.aimcer.bean.Contact;
import com.itbs.aimcer.bean.Message;
import com.itbs.aimcer.bean.MessageImpl;
import com.itbs.aimcer.commune.MessageSupport;
import com.itbs.aimcer.gui.Main;
import com.itbs.aimcer.gui.userlist.ContactLabel;
import com.itbs.gui.BetterTabbedPane;
import com.itbs.gui.ButtonTabComponent;
import com.itbs.gui.CheckBoxJList;
import com.itbs.gui.GUIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author Alex Rass
 * @since May 5, 2008 4:50:55 PM
 */
public class TabMultiContact extends TabItself {
    List<Contact> contacts;
    CheckBoxJList list;
    public TabMultiContact(List<Contact> contacts, BetterTabbedPane tabbedPane) {
        super(null, tabbedPane);
        this.contacts = contacts;
    }

    /**
     * Provides a list of contacts with checkboxes.
     * @return checbox list.
     */
    protected JComponent getPersonalInfo() {
        list =  new CheckBoxJList();
        return new JScrollPane(list);
    }


    protected void setHistoryText() {
        String contactList="";
        boolean allOnline = true;
        for (Contact contact : contacts) {
            contactList += contact + " ";
            if (allOnline && !contact.getStatus().isOnline())
                allOnline = false;
        }
        GUIUtils.appendText(historyPane, "The following contacts will receive your message: " + contactList, ATT_NORMAL);
        if (!allOnline) {
            GUIUtils.appendText(historyPane, "\nSome contacts are offline. Your message may be missed.\n", ATT_RED);
        }
    }


    /**
     * This version does nothing.
     */
    protected void setFileTransferHandler() {
        // do nothing
    }


    protected void addDNDSupport(JComponent comp) {
        // do nothing
    }

    public void send(Message messageOverwrite) {
        String text = messageOverwrite!=null?messageOverwrite.getText():textPane.getText().trim();
        if (text.length() == 0)
            return;
        int index=0;
        for( Contact contact: contacts) {
            if (list.isSelectedIndex(index++) && contact.getConnection().isLoggedIn()) {
                try {
                    Message message = new MessageImpl(contact, true, text);
                    appendHistoryText(message, true);
                    ((MessageSupport)contact.getConnection()).sendMessage(message);
                    textPane.setText(""); // wipe it
                } catch (Exception e1) {
                    Main.complain("Failed to send message", e1);
                }
                textPane.requestFocusInWindow();
            } // if
        } // for

    }

    public void addTabComponent() {
        setHistoryText();

        // set what's in the checkbox list
        DefaultListModel defModel = new DefaultListModel();
        list.setModel (defModel);
        for (Contact contact : contacts) {
            defModel.addElement(contact);

        }
        list.setSelectionInterval(0, contacts.size());  // default everyone to on


        int realHeight = SwingUtilities.getWindowAncestor(tabbedPane).getHeight();
        
        splitHistoryTextPane.setDividerLocation((int) (realHeight * DEFAULT_SEPARATION));
        int realWidth= SwingUtilities.getWindowAncestor(tabbedPane).getWidth();
        splitNotes.setDividerLocation((int) (realWidth * DEFAULT_SEPARATION));

        int index = tabbedPane.indexOfComponent(this);
        tabControl = new ButtonTabComponent(tabbedPane, this, new ContactLabel(null){
            public void update() {
                setText("Group");
            }
        });
        tabbedPane.setTabComponentAtReflect(index, tabControl);

        if (BetterTabbedPane.oldVM) {
            tabbedPane.setIconAt(index, getContact().getIcon());
        }
        tabControl.setBackground(Color.RED);
    }

    protected void onClose() {
        // do nothing.
    }
}
