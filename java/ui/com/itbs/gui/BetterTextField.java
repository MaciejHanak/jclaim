package com.itbs.gui;

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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;

/**
 * Created by: ARass  on  Date: Mar 25, 2004
 */
public class BetterTextField extends JTextField {
    CaretListener autoCopy;
    private static final JPopupMenu pmenu;

    static {
        pmenu = new JPopupMenu();
        JMenuItem menu;
        menu = ActionAdapter.createMenuItem("Copy", new DefaultEditorKit.CopyAction(), 'C');
        pmenu.add(menu);
        menu = ActionAdapter.createMenuItem("Paste", new DefaultEditorKit.PasteAction(), 'P');
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

    public static Action selectOutAction = new AbstractAction("Select Out") {
        public void actionPerformed(ActionEvent evt) {
            JTextComponent c = (JTextComponent) evt.getSource();

            try {
                int startPos = c.getSelectionStart();
                int endPos = c.getSelectionEnd();
                if (startPos != endPos) { // starting with selection
                    if (startPos > c.getDocument().getStartPosition().getOffset())
                        startPos--;
                    if (endPos < c.getDocument().getEndPosition().getOffset())
                        endPos++;
                }
                // go left till space
                while (startPos > c.getDocument().getStartPosition().getOffset() &&
                        !" ".equals(c.getDocument().getText(startPos - 1, 1)))
                    startPos--;
                // go right till space
                while (endPos < c.getDocument().getEndPosition().getOffset() && !" ".equals(c.getDocument().getText(endPos, 1)))
                    endPos++;
                c.setSelectionStart(startPos);
                c.setSelectionEnd(endPos);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    };

    static void typicalInit(JTextComponent textComp) {
        textComp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, KeyEvent.SHIFT_DOWN_MASK), DefaultEditorKit.pasteAction);
        textComp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK), DefaultEditorKit.pasteAction);

        textComp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, KeyEvent.CTRL_DOWN_MASK), DefaultEditorKit.copyAction);
        textComp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), DefaultEditorKit.copyAction);

        textComp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.SHIFT_DOWN_MASK), DefaultEditorKit.cutAction);
        textComp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK), DefaultEditorKit.cutAction);

        addAction(textComp, KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK, selectOutAction);

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
                    e.printStackTrace();
                }
            }
        };
        Action redoAction = new AbstractAction("Redo") {
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (undoManager.canRedo())
                        undoManager.redo();
                } catch (CannotUndoException e) {
                    e.printStackTrace();
                }
            }
        };
        addAction(textComp, KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK, undoAction);
        addAction(textComp, KeyEvent.VK_BACK_SPACE, KeyEvent.ALT_DOWN_MASK, undoAction);
        addAction(textComp, KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, redoAction);
        addAction(textComp, KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK, redoAction);

        textComp.addMouseListener( new MouseAdapter() {
               public void mousePressed(MouseEvent evt) {
                      if (SwingUtilities.isRightMouseButton(evt)) {
                           pmenu.show((Component)evt.getSource(), evt.getX(), evt.getY());
                  }
               }
        });
    } // typicalInit

    static JPopupMenu getPopupMenu() {
        return pmenu;
    }
    static void addAction(JTextComponent textComp, int keyCode, int modifiers, Action action) {
        textComp.getInputMap().put(KeyStroke.getKeyStroke(keyCode, modifiers), action.getValue(Action.NAME));
        textComp.getActionMap().put(action.getValue(Action.NAME), action);
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
    }

}
