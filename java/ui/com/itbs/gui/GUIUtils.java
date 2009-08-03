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
import javax.swing.border.TitledBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bunch of UI utilities together for convenience.
 * 
 * @author Alex Rass on Jul 20, 2005
 */
public class GUIUtils {
    private static final Logger log = Logger.getLogger(GUIUtils.class.getName());
    private static final String CANCEL_ACTION_KEY = "CANCEL_ACTION_KEY";

    /**
     * Force the escape key to call the same action as pressing the Cancel button.
     *
     * This does not always work. See class comment.
     * @param parentWindow frame
     */
    public static void addCancelByEscape(final RootPaneContainer parentWindow) {
        Action action = new AbstractAction(CANCEL_ACTION_KEY) {
            public void actionPerformed(ActionEvent e) {
                if (parentWindow instanceof JFrame) {
                    ((JFrame) parentWindow).dispose();
                } else if (parentWindow instanceof JDialog) {
                    ((JDialog) parentWindow).dispose();
                }
            }
        };
        addAction(parentWindow, KeyEvent.VK_ESCAPE, 0, action);
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
    public static void runOnAWTAndWait(Runnable runnable) {
        if (EventQueue.isDispatchThread())
            runnable.run();
        else
            try {
                EventQueue.invokeAndWait(runnable);
            } catch (Exception e) {
                log.log(Level.SEVERE, "AWT Thread crashed: ", e);  //don't care, but lets see it.
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

    /**
     * Used to add keystrokes to components that do things.
     * @param frame top frame
     * @param keyCode key
     * @param modifiers any modifiers
     * @param action what to do when even occurs in a form of an action.
     */
    public static void addAction(RootPaneContainer frame, int keyCode, int modifiers, Action action) {
        if (action.getValue(Action.NAME)==null) {
            throw new NullPointerException("Must set name for the action!");
        }
//        frame.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(keyCode, modifiers), action.getValue(Action.NAME));
        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, modifiers), action.getValue(Action.NAME));
        frame.getRootPane().getActionMap().put(action.getValue(Action.NAME), action);
    }
    /**
     * Used to add keystrokes to components that do things.
     * @param comp component
     * @param keyCode key
     * @param modifiers any modifiers
     * @param action what to do when even occurs in a form of an action.
     */
    public static void addAction(JComponent comp, int keyCode, int modifiers, Action action) {
        if (action.getValue(Action.NAME)==null) {
            throw new NullPointerException("Must set name for the action!");
        }
        comp.getInputMap().put(KeyStroke.getKeyStroke(keyCode, modifiers), action.getValue(Action.NAME));
        comp.getActionMap().put(action.getValue(Action.NAME), action);
    }

    public static JComponent fixedWidget(final JComponent comp, String title) {
//        Font font = new Font("Comic Sans MS", Font.PLAIN, 8);
//        comp.setFont(font);
        comp.setOpaque(false);
        if (title!=null && !(comp instanceof JLabel)) { // &&!(comp instanceof JXDatePicker)
            comp.setBorder(new TitledBorder(comp.getBorder(), title, TitledBorder.CENTER, TitledBorder.ABOVE_TOP)); // font
        }
        return comp;
    }

}
