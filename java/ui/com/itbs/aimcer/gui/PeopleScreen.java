/*
 * Copyright (c) 2006, ITBS LLC. All Rights Reserved.
 *
 *     This file is part of JClaim.
 *
 *     JClaim is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; version 2 of the License.
 *
 *     JClaim is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with JClaim; if not, find it at gnu.org or write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package com.itbs.aimcer.gui;

import com.itbs.aimcer.bean.ClientProperties;
import com.itbs.aimcer.bean.ContactWrapper;
import com.itbs.aimcer.bean.GroupWrapper;
import com.itbs.aimcer.commune.MessageSupport;
import com.itbs.aimcer.commune.weather.WeatherConnection;
import com.itbs.gui.AbstractFileTransferHandler;
import com.itbs.gui.ActionAdapter;
import com.itbs.gui.EditableJList;
import org.jdesktop.jdic.desktop.Desktop;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Main screen of the UI.
 * <p>
 * Presents a list of contacts.
 *
 * @author Alex Rass
 * @since Sep 9, 2004
 */
final public class PeopleScreen extends JPanel  {
    private static final Logger log = Logger.getLogger(PeopleScreen.class.getName());
    private JList list;

    public PeopleScreen() {
//        setOpaque(true);
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
        else if (item instanceof GroupWrapper) {
            ((GroupWrapper)item).swapShrunk();
            ((ContactListModel)list.getModel()).runActionDataChanged();
        }
    }

    /**
     * Returns the list of controls that shows people.
     *
     * @return panel
     */
    private Component getCenterPanel() {
        list = new EditableJList(ContactListModel.getInstance()) {
//        list = new JList() {
            /**
             * @see javax.swing.JComponent#getToolTipText
             */
            public String getToolTipText(MouseEvent event) {
                int index = locationToIndex(event.getPoint());
                Object entry = list.getModel().getElementAt(index);
                if (entry instanceof ContactWrapper) {
                    return ((ContactWrapper) entry).getToolTipText();
                } 
                return super.getToolTipText(event);
            }
        };
        list.setCellRenderer(new ListRenderer());
        list.setOpaque(false);
        list.setDragEnabled(true);
        list.setSelectionMode(ClientProperties.INSTANCE.isMultiSelectAllowed()?ListSelectionModel.MULTIPLE_INTERVAL_SELECTION:ListSelectionModel.SINGLE_SELECTION);

        list.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if ((e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_MINUS) && e.getModifiers() == 0) {
                    if (list.getSelectedIndices().length==1) {
                        handleItem(list.getSelectedValue());
                        e.consume();
                    }
                }
            }
        });

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
             * Invoked when a mouse button has been released on a component.
             */
            public void mouseReleased(MouseEvent evt) {
                if (evt.getClickCount() == 1 && evt.isPopupTrigger()) {
                    int index = list.locationToIndex(evt.getPoint());
                    if (!list.isSelectedIndex(index)) {
                        list.setSelectedIndex(index);
                    }
                }
                // show popup menus
                if (evt.isPopupTrigger() && list.getSelectedIndex() > -1) {
                    final Object[] items = list.getSelectedValues();
                    boolean multiple = items.length > 1 || list.getSelectedValue() instanceof GroupWrapper;

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
                                } else if (selected instanceof GroupWrapper) {
                                    GroupWrapper group = (GroupWrapper) selected;
                                    for (int j = 0; j < group.size(); j++) {
                                        if (group.get(j) instanceof ContactWrapper)
                                            allContacts.add((ContactWrapper) group.get(j));
                                    }
                                } else {
                                    log.info("This is weird: " + selected.getClass() + ": " + selected);
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
//        list.setModel(ContactListModel.getInstance());
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
