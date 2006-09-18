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

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import java.awt.event.ActionEvent;

/**
 * Provides a standard action for replacing words.
 * Works outside of the mapped paradigm sun came up with to allow for complete reuse in any field.
 *
 * @since  Apr 17, 2006
 * @author Alex Rass
 */
class ReplaceWordAction extends TextAction {
    JTextComponent tc;
    public ReplaceWordAction(JTextComponent parent, String string) {
        super(string);
        tc = parent;
    }

    public void actionPerformed(ActionEvent e) {
        e.setSource(tc); // source comes in as JMenu.  Useless.
        if (tc.getSelectionStart() == tc.getSelectionEnd()) {
            // reuse existing action selectWordAction
            
/*          This causes a bug when the cursor is at the end of the text
            Action action = tc.getActionMap().get(DefaultEditorKit.selectWordAction);
            if (action!=null)
                action.actionPerformed(e);
*/
            SelectOutAction.getInstance().actionPerformed(e);
        }
        tc.replaceSelection(e.getActionCommand());
    }

    /**
     * Generic function which returns the word around the carret.
     *
     * @param parent text component
     * @param carret position
     * @return word, never null.
     * @throws javax.swing.text.BadLocationException
     */
    public static String getWord(JTextComponent parent, int carret) throws BadLocationException {
        Document doc = parent.getDocument();
        String word="";
        int lastPos = carret;
        while (lastPos>=0 && lastPos<doc.getLength()) {
            String  res = doc.getText(lastPos, 1);
            if (" ".equals(res))
                break;
            word +=res;
            lastPos++;
        }
        lastPos = carret-1;
        while (lastPos>=0 && lastPos<doc.getLength()) {
            String  res = doc.getText(lastPos, 1);
            if (" ".equals(res))
                break;
            word =res + word;
            lastPos--;
        }
//        log.fine("R:" + word + "<");
        return word;
    }
}
