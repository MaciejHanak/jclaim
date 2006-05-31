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

package com.itbs.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Alex Rass on Feb 13, 2005
 */
public class LookAndFeelManager {
    static {
        try {
            Class.forName("org.jvnet.substance.SubstanceLookAndFeel");
            UIManager.LookAndFeelInfo substance = new UIManager.LookAndFeelInfo("Substance", "org.jvnet.substance.SubstanceLookAndFeel");
            UIManager.installLookAndFeel(substance);
        } catch (ClassNotFoundException e) {
            // Class is not present
        }
/*
        UIManager.LookAndFeelInfo apple = new UIManager.LookAndFeelInfo("Jaguar", QuaquaManager.getLookAndFeelClassName());
        UIManager.installLookAndFeel(apple);
        UIManager.LookAndFeelInfo windowsXP = new UIManager.LookAndFeelInfo("Windows XP", "net.java.plaf.windows.WindowsLookAndFeel");
        UIManager.installLookAndFeel(windowsXP);
*/
    }

    private static UIManager.LookAndFeelInfo[] AVAILABLES = UIManager.getInstalledLookAndFeels();

    public static void setLookAndFeel(final int option) {
        if (option > -1 && option < AVAILABLES.length) {
            Runnable runnable = new Runnable() {
                public void run() {
                    try{
                        UIManager.setLookAndFeel(AVAILABLES[option].getClassName());
                        // Update all existing frames
                        Frame[] frames = JFrame.getFrames();
                        for (Frame frame : frames) {
                            SwingUtilities.updateComponentTreeUI(frame);
                            Window[] windows = frame.getOwnedWindows();
                            for (Window window : windows) {
                                SwingUtilities.updateComponentTreeUI(window);
                                window.validate();
                            }
                        }
                    } catch(Exception ex) {
                        // the heck with it
                    }
                }
            };
            GUIUtils.runOnAWT(runnable);
        }
    }

    public static JMenu getLookAndFeelMenu(String menuName, char mnemonics, ActionListener eventHandler) {
        JMenu menu;
        menu = new JMenu(menuName);
        menu.setMnemonic(mnemonics);
        for (UIManager.LookAndFeelInfo available : AVAILABLES) {
            menu.add(new JRadioButtonMenuItem(new ActionAdapter(available.getName(), eventHandler)));
        }
        return menu;
    }

    /**
     * Created a look and feel dropdown usable for selecting the UI.
     * @param defaultIndex within selected
     * @return list.  ready to use.
     */
    public static JComboBox getLookAndFeelCombo(int defaultIndex) {
        String[] items = new String[AVAILABLES.length];
        for (int i = 0; i < items.length; i++) {
            items[i] = AVAILABLES[i].getName();
        }
        JComboBox combo = new JComboBox(items);
        combo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    final JComboBox jComboBox = ((JComboBox)e.getSource());
                    setLookAndFeel(jComboBox.getSelectedIndex());
                } catch (Exception e1) {
                    ErrorDialog.displayError((Component) e.getSource(), "Failure switching to new skin.", e1);
                }
            }
        });
        if (defaultIndex > -1 && defaultIndex < AVAILABLES.length)
            combo.setSelectedIndex(defaultIndex);
        return combo;
    }
}
