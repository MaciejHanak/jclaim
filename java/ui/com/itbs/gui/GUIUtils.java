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
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alex Rass on Jul 20, 2005
 */
public class GUIUtils {
    private static final Logger log = Logger.getLogger(GUIUtils.class.getName());
    private static final String CANCEL_ACTION_KEY = "CANCEL_ACTION_KEY";

    /**
     * Force the escape key to call the same action as pressing the Cancel button.
     *
     * This does not always work. See class comment.
     */
    public static void addCancelByEscape(JFrame parentFrame, final ActionListener callback)
    {
        KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        InputMap inputMap = parentFrame.getRootPane().
                getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(escapeKey, CANCEL_ACTION_KEY);
        AbstractAction cancelAction = new AbstractAction()
         {
            public void actionPerformed(ActionEvent e)
            {
                callback.actionPerformed(null);
            }
        };
        parentFrame.getRootPane().getActionMap().put(CANCEL_ACTION_KEY, cancelAction);
    }

    /**
     * Force the escape key to call the same action as pressing the Cancel button.
     *
     * This does not always work. See class comment.
     */
    public static void addCancelByEscape(final JDialog parentFrame)
    {
        KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        InputMap inputMap = parentFrame.getRootPane().
                getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(escapeKey, CANCEL_ACTION_KEY);
        AbstractAction cancelAction = new AbstractAction()
         {
            public void actionPerformed(ActionEvent e)
            {
                parentFrame.dispose();
            }
        };
        parentFrame.getRootPane().getActionMap().put(CANCEL_ACTION_KEY, cancelAction);
    }

    /**
     * Positions component in the middle of another
     * @param parent to use as reference
     * @param window to position
     */
    public static void moveToParentCenter(Window parent, Window window)
    {
        int x = parent.getX() + parent.getWidth() / 2 - window.getWidth() / 2;
        int y = parent.getY() + parent.getHeight() / 2 - window.getHeight() / 2;
        window.setLocation(x, y);
    }

    /**
     * Positions component in the middle of screen.
     * @param window to position
     */
    public static void moveToScreenCenter(Window window)
    {
        Dimension dim = window.getToolkit().getScreenSize();
        int x = ((int) dim.getWidth() - window.getWidth()) / 2;
        int y = ((int) dim.getHeight() - window.getHeight()) / 2;
        window.setLocation(x, y);
    }

    public static void runOnAWT(Runnable runnable) {
        if (EventQueue.isDispatchThread())
            runnable.run();
        else
            try {
                EventQueue.invokeLater(runnable);
            } catch (Exception e) {
                log.log(Level.SEVERE, "", e);  //don't care, but lets see it.
            }
    }

    /**
     * Sets up the frame with some basic options.
     * @return reference to the frame
     */
    public static JFrame createFrame(final String title) {
        JFrame jFrame = new JFrame(title);
        jFrame.addComponentListener(WindowSnapper.instance());
        return jFrame;
    }
    
    public static void appendText(final JTextComponent textPane, final String text, final AttributeSet style) {
        GUIUtils.runOnAWT(new Runnable() {
            public void run() {
                if (textPane == null)
                    return;
                final Document document = textPane.getDocument();
                try {
                    document.insertString(document.getLength(), text, style);
                } catch (BadLocationException e) {
                    log.log(Level.SEVERE, "", e); // this should never happen unless getLength() is broken.
                }
                textPane.setCaretPosition(textPane.getDocument().getLength());
            }
        });
    }

}
