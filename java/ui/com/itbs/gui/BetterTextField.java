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

import com.itbs.aimcer.bean.ClientProperties;
import org.jdesktop.jdic.desktop.Desktop;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.*;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.Serializable;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a richness of a windows-like component.
 * Provides:
 *   length limitor
 *   copy/paste shortcuts
 *   select word shortcute
 *   built in undo functionality
 *   automatic copy on select in read only mode
 * @author Alex Rass
 * @since Date: Mar 25, 2004
 */
public class BetterTextField extends JTextField {
    private static final Logger log = Logger.getLogger(BetterTextField.class.getName());
    CaretListener autoCopy;
    private static BetterTextPopupMenu pmenu;

    static {
        pmenu = new BetterTextPopupMenu();
        JMenuItem menu;
        menu = ActionAdapter.createMenuItem("Copy", new DefaultEditorKit.CopyAction(), 'C');
        pmenu.add(menu);
        menu = ActionAdapter.createMenuItem("Paste", new DefaultEditorKit.PasteAction(), 'P');
        pmenu.add(menu);
        menu = ActionAdapter.createMenuItem("Google", new GoogleAction(), 'G');
        pmenu.add(menu);
        menu = ActionAdapter.createMenuItem("Use as Away message", new SetAwayAction(), 'G');
        pmenu.add(menu);
    }

    public BetterTextField(int columns) {
        super(columns);
        init();
    }

    public BetterTextField(String text) {
        super(text, 5);
        init();
    }

    public BetterTextField() {
        super(5);
        init();
    }

    public BetterTextField(String text, int columns) {
        super(text, columns);
        init();
    }

    private void init() {
        // setup the auto-copy
        autoCopy = new AutoCopyCaretListener(this);
        typicalInit(this);
    }

    public static class SetAwayAction extends BasicTextAction {
        public SetAwayAction() {
            super("Set as Away message");
        }

        protected void doStuff(String selectedText) {
            ClientProperties.INSTANCE.setIamAwayMessage(selectedText);
        }
    }

    public static abstract class BasicTextAction extends TextAction {

        protected BasicTextAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent evt) {
            JTextComponent c = getTextComponent(evt);
            evt.setSource(c);
            // if nothing is selected, select out, google
            if (c.getSelectionStart() == c.getSelectionEnd()) {
                selectOutAction.actionPerformed(evt);
            }
            if (c.getSelectionStart() == c.getSelectionEnd() || c.getSelectedText().trim().length() == 0) {
                Toolkit.getDefaultToolkit().beep();
                return; // nothing to google.
            }                                                       
            // if something is selected, google that
            doStuff(c.getSelectedText().trim());
        }

        abstract protected void doStuff(String s);

    }
    public static class GoogleAction extends BasicTextAction {
        public static final String GOOGLE_URL = "http://www.google.com/custom?client=pub-3922476703716903&forid=1&ie=ISO-8859-1&oe=ISO-8859-1&cof=GALT%3A%23008000%3BGL%3A1%3BDIV%3A%23336699%3BVLC%3A663399%3BAH%3Acenter%3BBGC%3AFFFFFF%3BLBGC%3A336699%3BALC%3A0000FF%3BLC%3A0000FF%3BT%3A000000%3BGFNT%3A0000FF%3BGIMP%3A0000FF%3BFORID%3A1%3B&hl=en&q=";

        public GoogleAction() {
            super("Google");
        }

        protected void doStuff(String selected) {
            try {
//                Desktop.browse(new URL("http://www.google.com/search?q="+ URLEncoder.encode(c.getSelectedText().trim(), "UTF-8") + "&ie=utf-8&oe=utf-8"));
                // Using a partner link instead or the regular one.
                Desktop.browse(new URL(GOOGLE_URL + URLEncoder.encode(selected, "UTF-8") ));
            } catch (Exception e) {
                log.log(Level.SEVERE, "Failed to lookup.", e);
            }
        }
    }

    public static Action selectOutAction = new SelectOutAction();

    static void typicalInit(final JTextComponent textComp) {
        // clear bad ones:
//        textComp.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.CTRL_DOWN_MASK));
//        textComp.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.CTRL_MASK));

        Action textAction;
        textAction = TextActionLib.paste; // todo remove
        GUIUtils.addAction(textComp, KeyEvent.VK_INSERT, KeyEvent.SHIFT_DOWN_MASK, TextActionLib.paste);
        textComp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK), TextActionLib.paste);

        GUIUtils.addAction(textComp, KeyEvent.VK_INSERT, KeyEvent.CTRL_DOWN_MASK, TextActionLib.copy);
        textComp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), TextActionLib.copy);

        GUIUtils.addAction(textComp, KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK, TextActionLib.cut);
        textComp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.SHIFT_DOWN_MASK), TextActionLib.cut);

        GUIUtils.addAction(textComp, KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK, selectOutAction);

        // undo mechanism
        // http://javafaq.nu/books22-6.html
        final UndoManager undoManager = new UndoManager();
//        JButton m_undoButton = new JButton(), m_redoButton = new JButton();
//        m_undoButton.setEnabled(false);
//        m_redoButton.setEnabled(false);
        textComp.getDocument().addUndoableEditListener(
                new UndoableEditListener() {
                    public void undoableEditHappened(UndoableEditEvent e) {
                        undoManager.addEdit(e.getEdit());
//                        updateButtons();
                    }
                });
        Action undoAction = new AbstractAction("Undo") {
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (undoManager.canUndo())
                        undoManager.undo();
                } catch (CannotUndoException e) {
                    log.log(Level.SEVERE, "", e);
                }
            }
        };
        Action redoAction = new AbstractAction("Redo") {
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (undoManager.canRedo())
                        undoManager.redo();
                } catch (CannotUndoException e) {
                    log.log(Level.SEVERE, "", e);
                }
            }
        };
        GUIUtils.addAction(textComp, KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK, undoAction);
        GUIUtils.addAction(textComp, KeyEvent.VK_BACK_SPACE, KeyEvent.ALT_DOWN_MASK, undoAction);
        GUIUtils.addAction(textComp, KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, redoAction);
        GUIUtils.addAction(textComp, KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK, redoAction);

        // Register for the Menu clicks
        textComp.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger())
                    try {
                        int dotPosition = textComp.viewToModel(e.getPoint());
                        Rectangle popupLocation = textComp.modelToView(dotPosition);
                        pmenu.show(textComp, popupLocation.x, popupLocation.y, textComp, dotPosition);
                    } catch (BadLocationException badLocationException) {
                        System.err.println("Oops - bad location");
                    }
            }
        });

        // Register for the Menu keystrokes
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    int dotPosition = textComp.getCaretPosition();
                    Rectangle popupLocation = textComp.modelToView(dotPosition);
                    pmenu.show(textComp, popupLocation.x, popupLocation.y, textComp, dotPosition);
                } catch (BadLocationException badLocationException) {
                    System.err.println("Oops, bad location");
                }
            }
        };
        // Does the shift-F10 (windows)
        KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_F10, Event.SHIFT_MASK, true);
        textComp.registerKeyboardAction(actionListener, keystroke, JComponent.WHEN_FOCUSED);
    } // typicalInit

    static BetterTextPopupMenu getPopupMenu() {
        return pmenu;
    }

    static void setPopupMenu(BetterTextPopupMenu menu) {
        pmenu = menu;
    }


    public static void updateButtons() {
//        m_undoButton.setText(m_undoManager.getUndoPresentationName());
//        m_redoButton.setText(m_undoManager.getRedoPresentationName());
//        m_undoButton.setEnabled(m_undoManager.canUndo());
//        m_redoButton.setEnabled(m_undoManager.canRedo());
    }


    /**
     * Sets whether or not this component is enabled.
     * A component that is enabled may respond to user input,
     * while a component that is not enabled cannot respond to
     * user input.
     *
     * @param enabled true if this component should be enabled, false otherwise
     */
    public void setEditable(boolean enabled) {
        super.setEditable(enabled);
        if (enabled)
            removeCaretListener(autoCopy);
        else
            addCaretListener(autoCopy);
    }

    static class AutoCopyCaretListener implements CaretListener, Serializable {
        JTextComponent tf;

        public AutoCopyCaretListener(JTextComponent tf) {
            this.tf = tf;
        }

        public void caretUpdate(CaretEvent e) {
            if (e.getDot() != e.getMark()) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(tf.getSelectedText()), null);
            }
        }
    }

    /**
     * Makes the enter move to next item.
     *
     * @param e event
     */
    protected void processKeyEvent(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER
                && e.getID() == KeyEvent.KEY_PRESSED) {
            if ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) == 0)
                transferFocus();
            else
                transferFocusBackward();
        } else {
            super.processKeyEvent(e);
        }
    }

    /**
     * Sets the max number of chars allowed
     *
     * @param charCount the max number of chars allowed
     */
    public void setMaxLength(String charCount) {
        int count = Integer.parseInt(charCount);
        setDocument(new LengthDocument(count));
    }

    /**
     * Sets the max number of chars allowed
     *
     * @param charCount the max number of chars allowed
     */
    public void setMaxLength(int charCount) {
        setDocument(new LengthDocument(charCount));
    }

    /**
     * @see PlainDocument
     */
    public static class LengthDocument extends PlainDocument {
        /**
         * max lenght of document content...?
         */
        private int maxLength;

        /**
         * Constructor
         *
         * @param charCount max char len
         */
        public LengthDocument(int charCount) {
            maxLength = charCount;
        }

        /**
         * @see javax.swing.text.PlainDocument
         */
        public void insertString(int offs, String str, AttributeSet a)
                throws BadLocationException {
            if (str == null) {
                return;
            }
            if (maxLength == 0) {
                super.insertString(offs, str, a);
            } else if (getLength() + str.length() > maxLength) {
                Toolkit.getDefaultToolkit().beep();
            } else if (offs < maxLength) {
                super.insertString(offs, str, a);
            }
        }
    } // class LengthDocument

    /**
     * @see javax.swing.text.PlainDocument
     */
    public static class NumberDocument extends PlainDocument {

        /**
         * @see javax.swing.text.PlainDocument
         */
        public void insertString(int offs, String str, AttributeSet a)
                throws BadLocationException {
            if (str == null) {
                return;
            }
            String insertString="";
            for (int i=0; i<str.length(); i++) {
                if (Character.isDigit(str.charAt(i))) {
                    insertString += str.charAt(i);
                }
            }
            super.insertString(offs, insertString, a);

            if (str.length() != insertString.length()) { // complain otherwise
                Toolkit.getDefaultToolkit().beep();
            }
        }
    } // class NumberDocument


} // class BetterTextField
