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

package com.itbs.aimcer.gui.userlist;

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.commune.ConnectionEventListener;
import com.itbs.aimcer.commune.MessageSupport;
import com.itbs.aimcer.commune.weather.WeatherConnection;
import com.itbs.aimcer.gui.Main;
import com.itbs.aimcer.gui.MenuManager;
import com.itbs.gui.AbstractFileTransferHandler;
import com.itbs.gui.ActionAdapter;
import com.itbs.gui.EditableJList;
import com.itbs.gui.TypingFilter;
import org.jdesktop.jdic.desktop.Desktop;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
final public class PeopleScreen extends JPanel implements UserList {
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
        if (item instanceof ContactLabel) {
            if (((ContactLabel)item).getContact().getConnection() instanceof MessageSupport) {
                Main.globalWindowHandler.openWindow(((ContactLabel)item).getContact(), true);
            } else if (((ContactLabel)item).getContact().getConnection() instanceof WeatherConnection) {
                try {
                    Desktop.browse(new URL(WeatherConnection.TOKEN_HOURLY + (((ContactLabel)item).getContact()).getName()));
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
        } else {
            log.warning("Failed to find a handler of item of class "+ (item==null?"null":item.getClass()));
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
                if (entry instanceof ContactLabel) {
                    return ((ContactLabel) entry).getToolTipText();
                } 
                return super.getToolTipText(event);
            }
        };
        list.setCellRenderer(new ListRenderer());
        list.setOpaque(false);
        list.setDragEnabled(true);
        list.setSelectionMode(ClientProperties.INSTANCE.isMultiSelectAllowed()?ListSelectionModel.MULTIPLE_INTERVAL_SELECTION:ListSelectionModel.SINGLE_SELECTION);

        list.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if ((e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_MINUS) && e.getModifiers() == 0) {
                    if (list.getSelectedIndices().length==1) {
                        handleItem(list.getSelectedValue());
                        e.consume();
                    }
                }
            }
        });
        list.addKeyListener(new TypingFilter(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ((ContactListModel)list.getModel()).setFilterBy(e.getActionCommand());
                update();
            }
        }));
        
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
                            List <Contact> allContacts = new ArrayList<Contact>(items.length);
                            List <Group> allGroups = new ArrayList<Group>(5);
                            for (Object selected : items) {
                                if (selected instanceof ContactLabel) {
                                    ContactWrapper contactWrapper = ((ContactLabel) selected).getContact();
                                    if (!allContacts.contains(contactWrapper)) {
                                        allContacts.add(contactWrapper);
                                    }
                                } else if (selected instanceof GroupWrapper) {
                                    GroupWrapper group = (GroupWrapper) selected;
                                    allGroups.add(group);
                                } else {
                                    log.info("This is weird: " + selected.getClass() + ": " + selected);
                                }
                            }
                            if (allContacts.size() == 1 && allGroups.size()==0) {
                                Main.globalWindowHandler.openWindow(allContacts.get(0), true);
                            } else {
                                Main.globalWindowHandler.openWindow(allContacts, allGroups, true);
                            }
                        }
                    });
                    menu.add(item);

                    // Copy into a group code
                    menu.add(ActionAdapter.createMenuItem(MenuManager.COMMAND_BUDDY_COPY, new MenuManager.MenuHandler(), 'c'));

                    if (list.getSelectedValue() instanceof ContactLabel) {
                        final JMenuItem itemHide = new JMenuItem("Hide/Unhide (Keep Offline)");
                        final boolean hide = !((ContactLabel)list.getSelectedValue()).getContact().getPreferences().isHideFromList();
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
                        menu.add(ActionAdapter.createMenuItem(MenuManager.COMMAND_BUDDY_MOVE, new MenuManager.MenuHandler(), 'm'));
                    } // if contact wrapper
                    menu.add(ActionAdapter.createMenuItem(MenuManager.COMMAND_BUDDY_REMOVE, new MenuManager.MenuHandler(), 'r'));

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

    public List getSelectedValues() {
        return Arrays.asList(list.getSelectedValues());
    }

    public ConnectionEventListener getConnectionEventListener() {
        return (ConnectionEventListener) list.getModel();
    }

    public void update() {
        ((ContactListModel)list.getModel()).runActionDataChanged();
//        list.repaint(); // this was causing silly repaint problems.
    }

    public JComponent getDisplayComponent() {
        return this;
    }
}
