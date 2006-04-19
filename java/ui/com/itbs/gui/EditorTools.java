package com.itbs.gui;

import java.awt.Component;
import java.awt.Event;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;


public class EditorTools {
    final static SpecialPopup popup = new SpecialPopup();
    
    public static void addSuggestionPopup(final JTextComponent textField) {
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    int dotPosition = textField.getCaretPosition();
                    Rectangle popupLocation = textField.modelToView(dotPosition);
                    popup.show(textField, popupLocation.x, popupLocation.y, textField, dotPosition);
                } catch (BadLocationException badLocationException) {
                    System.err.println("Oops, bad location");
                }
            }
        };
        KeyStroke keystroke;

        // Does the shift-F10 (windows)
        keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_F10, Event.SHIFT_MASK, true);
        textField.registerKeyboardAction(actionListener, keystroke, JComponent.WHEN_FOCUSED);
        
        textField.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger())
                    try {
                        int dotPosition = textField.viewToModel(e.getPoint());
                        Rectangle popupLocation = textField.modelToView(dotPosition);
                        popup.show(textField, popupLocation.x, popupLocation.y, textField, dotPosition);
                    } catch (BadLocationException badLocationException) {
                        System.err.println("Oops - bad location");
                    }
            }
        });
    }
    
    public static void addStandardMenuOptions(JMenuItem menuItem) {
        popup.add(menuItem);
    }

    /**
     * Generic function which returns the word around the carret.
     * 
     * @param parent text component
     * @param carret position
     * @return word, never null.
     * @throws BadLocationException
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
//        System.out.println("R:" + word + "<");
        return word;
    }

    
    /**
     * Provides a standard action for replacing words.
     * Works outside of the mapped paradigm sun came up with to allow for complete reuse in any field.
     * 
     * @since  Apr 17, 2006
     * @author Alex Rass
     *
     */
    static class ReplaceWordAction extends AbstractAction {
        JTextComponent tc;
        public ReplaceWordAction(JTextComponent parent, String string) {
            super(string);
            tc = parent;
        }

        public void actionPerformed(ActionEvent e) {
            e.setSource(tc); // source comes in as JMenu.  Useless.
            if (tc.getSelectionStart() == tc.getSelectionEnd()) {
                // reuse existing action selectWordAction 
                Action action = tc.getActionMap().get(DefaultEditorKit.selectWordAction);
                if (action!=null)
                    action.actionPerformed(e);
            }
            tc.replaceSelection(e.getActionCommand());
        }
    }
   
    /**
     * Singleton Popup class.  
     * Allows one to maintain a dictionary based popup thingie.
     * 
     * @since  Apr 3, 2006
     * @author Alex Rass
     *
     */
    static class SpecialPopup extends JPopupMenu {
        JMenu suggestions;
//        JMenu synonyms; // add these later?
        int carret;
        Object source;
        
        public SpecialPopup() {
            suggestions = new JMenu("Suggestions");
            add(suggestions);
//            synonyms = new JMenu("Synonyms");
//            add(synonyms);
            addSeparator();
        }
        
        
        public void show(Component invoker, int x, int y, JTextComponent parent, int carret) throws BadLocationException {
            this.carret = carret;
            source = parent;
            addReplaceWords(parent, suggestions, getSuggestions(getWord(parent, carret)));
            super.show(invoker, x, y);
        }
        
        
        protected List getSuggestions(String word) {
            try {
                return JazzyInterface.create().getSuggestions(word);
            } catch (IOException e) {
                System.out.println("Failed to create spell checker." + e.getMessage());
                return new ArrayList();
            }
        }
        
        private void addReplaceWords(JTextComponent parent, JMenu menu, List words) {
            menu.removeAll();
            Iterator iter = words.iterator();
            while (iter.hasNext()) {
                Object word = iter.next();
                menu.add(new ReplaceWordAction(parent, word.toString()));
            }
        }
        
        Object getSource() {
            return source;
        }
        
        @Override
        public void processKeyEvent(KeyEvent e, MenuElement[] path, MenuSelectionManager manager) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE
                    && e.getID() == KeyEvent.KEY_PRESSED
                && (e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK & KeyEvent.CTRL_DOWN_MASK & KeyEvent.ALT_DOWN_MASK) == 0) {
                    setVisible(false);
                    e.consume();
            } else {
                super.processKeyEvent(e, path, manager);
            }
        }
    }
    
    static class WordPopup extends SpecialPopup {
        private void addReplaceWords(JTextComponent parent, List words) {
            removeAll();
            Iterator iter = words.iterator();
            while (iter.hasNext()) {
                Object word = iter.next();
                add(new ReplaceWordAction(parent, word.toString()));
            }
        }
        
        public void show(Component invoker, int x, int y, JTextComponent parent, int carret) throws BadLocationException {
            this.carret = carret;
            source = parent;
            String word = getWord(parent, carret);
            List returnedSuggestions;
            if (word.length()>0 && (returnedSuggestions = getSuggestions(word)).size()>0) {
                addReplaceWords(parent, returnedSuggestions);
                super.show(invoker, x, y);
            }
        }

    }


}
