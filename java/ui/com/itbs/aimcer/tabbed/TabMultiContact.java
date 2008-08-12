package com.itbs.aimcer.tabbed;

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.commune.MessageSupport;
import com.itbs.aimcer.gui.Main;
import com.itbs.aimcer.gui.MessageGroupWindow;
import com.itbs.aimcer.gui.userlist.ContactLabel;
import com.itbs.gui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * @author Alex Rass
 * @since May 5, 2008 4:50:55 PM
 */
public class TabMultiContact extends TabItself {
    List<Contact> contacts;
    CheckBoxJList list;
    String tabName;

    public TabMultiContact(List<Contact> contacts, List<Group> allGroups, BetterTabbedPane tabbedPane) {
        super(null, tabbedPane);
        this.contacts = MessageGroupWindow.getContacts(contacts, allGroups);
        tabName = MessageGroupWindow.getGroupName(contacts, allGroups);
    }


    public String getTabName() {
        return tabName;
    }

    /**
     * Provides a list of contacts with checkboxes.
     * @return checbox list.
     */
    protected JComponent getPersonalInfo() {
        list =  new CheckBoxJList();
        list.setCellRenderer(new ContactLabelListCellRenderer());
        return getSurround(list);
    }

    /**
     * Adds a pane around it.
     * Adds the 3 selection buttons.
     * @param list to enclose.
     * @return panel with list and all of the extra features.
     */
    public static JComponent getSurround(final CheckBoxJList list) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(list));
        JPanel buttonPanel = new JPanel();
//        buttonPanel.setOpaque(false);
        buttonPanel.setBackground(UIManager.getLookAndFeel().getDefaults().getColor ("List.background"));
        buttonPanel.add(new BetterButton(new ActionAdapter("All", "Select All", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                list.setSelectAll();  // default everyone to on
            }
        })));
        buttonPanel.add(new BetterButton(new ActionAdapter("None", "Select None", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                list.setSelectionNone();
            }
        })));
        buttonPanel.add(new BetterButton(new ActionAdapter("Invert", "Invert Selection", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                list.setSelectionInvert();
            }
        })));
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
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
            if (contact instanceof ContactWrapper) {
                defModel.addElement(new ContactLabel((ContactWrapper) contact));
            }
        }
        list.setSelectionInterval(0, contacts.size());  // default everyone to on


        int realHeight = SwingUtilities.getWindowAncestor(tabbedPane).getHeight();
        
        splitHistoryTextPane.setDividerLocation((int) (realHeight * DEFAULT_SEPARATION));
        int realWidth= SwingUtilities.getWindowAncestor(tabbedPane).getWidth();
        splitNotes.setDividerLocation((int) (realWidth * DEFAULT_SEPARATION));

        int index = tabbedPane.indexOfComponent(this);
        tabControl = new ButtonTabComponent(tabbedPane, this, new ContactLabel(null){
            public void update() {
                setText(tabName);
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

    /**
     * Does the drawing of the 2 components as one.
     */
    static class ContactLabelListCellRenderer extends JComponent
        implements ListCellRenderer {
        DefaultListCellRenderer defaultComp;
        JCheckBox checkbox;
        public ContactLabelListCellRenderer() {
            setLayout (new BorderLayout());
            defaultComp = new DefaultListCellRenderer();
            checkbox = new JCheckBox();
        }

        public Component getListCellRendererComponent(JList list,
                                                      Object  value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus){
            checkbox.setSelected (isSelected);
            if (value instanceof ContactLabel) {
                ((ContactLabel) value).update();
            }
            if (value instanceof JComponent) {
                checkbox.setBackground(((JComponent) value).getBackground());
            }
            removeAll();
            add (checkbox, BorderLayout.WEST);
            add ((Component) value, BorderLayout.CENTER);
            return this;
        }
    }

}
