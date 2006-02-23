package com.itbs.aimcer.gui;

import com.itbs.aimcer.bean.ClientProperties;
import com.itbs.aimcer.bean.ContactWrapper;
import com.itbs.aimcer.bean.GroupFactory;
import com.itbs.aimcer.bean.GroupWrapper;
import com.itbs.aimcer.commune.MessageSupport;
import com.itbs.aimcer.commune.weather.WeatherConnection;
import com.itbs.gui.AbstractFileTransferHandler;
import com.itbs.gui.ActionAdapter;
import com.itbs.gui.GradientPanel;
import org.jdesktop.jdic.desktop.Desktop;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Main screen of the UI.
 * <p>
 * Presents a list of contacts.
 *
 * @author Alex Rass
 * @since Sep 9, 2004
 */
final public class PeopleScreen extends GradientPanel  {
    private JList list;

    public PeopleScreen() {
        // create the screen
        setLayout(new BorderLayout());
        add(getCenterPanel());
        setBorder(new BevelBorder(BevelBorder.RAISED));
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                PeopleScreen.this.requestFocus();
            }
        });
    } // Constr

    /**
     * Returns the list of controls that shows people.
     *
     * @return panel
     */
    private Component getCenterPanel() {
        list = new JList() {
            /**
             * @see javax.swing.JComponent#getToolTipText
             */
            public String getToolTipText(MouseEvent event) {
                int index = locationToIndex(event.getPoint());
                Object entry = list.getModel().getElementAt(index);
                if (entry instanceof ContactWrapper) {
                    return ((ContactWrapper) entry).getToolTipText();
                } else
                    return super.getToolTipText(event);
            }
        };
        list.setCellRenderer(new ListRenderer());
        list.setOpaque(false);
        list.setDragEnabled(true);
        list.setSelectionMode(ClientProperties.INSTANCE.isMultiSelectAllowed()?ListSelectionModel.MULTIPLE_INTERVAL_SELECTION:ListSelectionModel.SINGLE_SELECTION);
        // Add a listener for mouse clicks
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() >= 2 && !evt.isPopupTrigger()) {
                    // Get item index
//                    int index = list.locationToIndex(evt.getPoint());
                    Object item = list.getSelectedValue();
                    handleItem(item);
                }
            }

            /**
             * Common handler
             * @param item to work
             */
            private void handleItem(Object item) {
                if (item instanceof ContactWrapper) {
                    if (((ContactWrapper)item).getConnection() instanceof MessageSupport) {
                        MessageWindow.openWindow((ContactWrapper)item, true);
                    } else if (((ContactWrapper)item).getConnection() instanceof WeatherConnection) {
                        try {
                            Desktop.browse(new URL(WeatherConnection.TOKEN_HOURLY + ((ContactWrapper)item).getName()));
                        } catch (Exception exc) {
                            Main.complain("Failed to launch url " + WeatherConnection.TOKEN_HOURLY  + ((ContactWrapper)item).getName() + "\n", exc);
                        } catch (UnsatisfiedLinkError exc) {
                            Main.complain("Failed to locate native libraries.  Please open "+ WeatherConnection.TOKEN_HOURLY  + ((ContactWrapper)item).getName() + " yourself.", new Exception(exc));
                        }
                    }
                }
                else if (item instanceof GroupFactory) {
                    ((GroupWrapper)item).swapShrunk();
                    ((ContactListModel)list.getModel()).runActionDataChanged();
                }
            }

            /**
             * Invoked when a mouse button has been released on a component.
             */
            public void mouseReleased(MouseEvent evt) {
                if (evt.getClickCount() == 1 && evt.isPopupTrigger()) {
                    int index = list.locationToIndex(evt.getPoint());
                    if (!list.isSelectedIndex(index)) {
                        list.setSelectedIndex(index);
                    }
                }
                if (evt.isPopupTrigger() && list.getSelectedIndex() > -1) { // show menus
                    final Object[] items = list.getSelectedValues();
                    boolean multiple = items.length > 1 || list.getSelectedValue() instanceof GroupFactory;

                    final JPopupMenu menu = new JPopupMenu();
                    JMenuItem item = new JMenuItem(multiple?"Message all...":"Message");
//                        item.setEnabled(multiple);
                    item.addActionListener(new ActionListener() {
                        /**
                         * Invoked when an action occurs.
                         */
                        public void actionPerformed(ActionEvent e) {
                            // do the do
                            List <ContactWrapper> allContacts = new ArrayList<ContactWrapper>(items.length);
                            for (Object selected : items) {
                                if (selected instanceof ContactWrapper) {
                                    allContacts.add((ContactWrapper) selected);
                                } else if (selected instanceof GroupFactory) {
                                    GroupWrapper group = (GroupWrapper) selected;
                                    for (int j = 0; j < group.size(); j++) {
                                        if (group.get(j) instanceof ContactWrapper)
                                            allContacts.add((ContactWrapper) group.get(j));
                                    }
                                } else {
                                    System.out.println("This is weird: " + selected.getClass() + ": " + selected);
                                }
                            }
                            if (allContacts.size() == 1) {
                                MessageWindow.openWindow(allContacts.get(0), false);
                            } else
                                MessageGroupWindow.openWindow(allContacts.toArray(new ContactWrapper[allContacts.size()]));
                        }
                    });
                    menu.add(item);
                    if (list.getSelectedValue() instanceof ContactWrapper) {
                        final JMenuItem itemHide = new JMenuItem("Hide/Unhide (Keep Offline)");
                        final boolean hide = !((ContactWrapper)list.getSelectedValue()).getPreferences().isHideFromList();
                        itemHide.addActionListener(new ActionListener() {
                            /**
                             * Invoked when an action occurs.
                             */
                            public void actionPerformed(ActionEvent e) {
                                // do the do
                                for (Object selected : items) {
                                    if (selected instanceof ContactWrapper) {
                                        ((ContactWrapper)selected).getPreferences().setHideFromList(hide);
                                        ((ContactWrapper)selected).updateDisplayComponent();
                                    }
                                }
                                list.repaint();
                            }
                        });
                        menu.add(itemHide);
                        menu.add(ActionAdapter.createMenuItem(MenuManager.COMMAND_BUDDY_REMOVE, new MenuManager.MenuHandler(), 'r'));
                        menu.add(ActionAdapter.createMenuItem(MenuManager.COMMAND_BUDDY_MOVE, new MenuManager.MenuHandler(), 'm'));
                    } // if contact wrapper

                    menu.show(evt.getComponent(), evt.getX(), evt.getY());
                } // if more than one selected
            } // mouseReleased
        });
        list.setModel(ContactListModel.getInstance());
        // transfer handler must be set after the model has been set.
        list.setTransferHandler(new AbstractFileTransferHandler() {
            protected void handle(JComponent c, List<File> fileList) {
                ContactWrapper wrapper = null;
                Object value = list.getSelectedValue();
                if (value != null && value instanceof ContactWrapper) {
                    wrapper = (ContactWrapper) value;
                }
                MenuManager.sendFileDialog(wrapper, fileList);
            }

        });

/*
        // add tooltips.  this one is slower.
        list.addMouseMotionListener(new MouseMotionListener() {
            public void mouseMoved(java.awt.event.MouseEvent e) {
                Point p = e.getPoint();
                int index = list.locationToIndex(p);
                Object entry = list.getModel().getElementAt(index);
                if (entry instanceof JComponent) {
                    list.setToolTipText(((JComponent) entry).getToolTipText());
                } else
                    list.setToolTipText("");
            }

            public void mouseDragged(java.awt.event.MouseEvent e) {
            }

        });
*/

        list.setToolTipText("");

        JScrollPane pane = new JScrollPane(list);
        pane.setOpaque(false);
        return pane;
    }

    JList getList() {
        return list;
    }

}
