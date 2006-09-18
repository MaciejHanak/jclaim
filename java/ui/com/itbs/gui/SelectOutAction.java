package com.itbs.gui;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Selects out in 1 word amounts.
 *
 * @author Alex Rass
 * @since Sep 18, 2006 1:06:10 AM
 */
public class SelectOutAction extends TextAction {
    private static final Logger log = Logger.getLogger(SelectOutAction.class.getName());
    /**
     * Instance Variable.
     * This works b/c this should only be done on event thread.
     * Therefor this is threadsafe.
     */
    private static final ActionListener INSTANCE = new SelectOutAction();

    public SelectOutAction() {
        super("Select Out");
    }

    public static ActionListener getInstance() {
        return INSTANCE;
    }

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
            log.log(Level.SEVERE, "", e);
        }
    }
} // class SelectOutAction