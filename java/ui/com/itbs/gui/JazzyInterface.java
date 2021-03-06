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
import com.itbs.util.ClassUtil;
import com.itbs.util.DelayedThread;
import com.itbs.util.GeneralUtils;
import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.event.DocumentWordTokenizer;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellChecker;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a set of utilities to provide a rich spell checking support to an app.
 *
 * Each instance is huge, hence this is a singleton.
 * @author Alex Rass on Nov 7, 2004
 *         Based on examples from Robert Gustavsson (robert@lindesign.se)
 */
public class JazzyInterface {
    private static final Logger log = Logger.getLogger(JazzyInterface.class.getName());
    private static final String englishDictionary = "/english.0";
//    private static final String englishPhonetic = "/phonet.en";
    private static final String wordsEnglish []= {
            "Alex",
            "behavior",
            "Charlotte",
            "Chloe",
            "Daniel",
            "download",
            "Ella",
            "Emily",
            "Emma",
            "Ethan",
            "Isabella",
            "Jack",
            "James",
            "Janet",
            "jclaim",
            "Jessica",
            "Joe",
            "Joseph",
            "Josh",
            "Joshua",
            "Lachlan",
            "laptop",
            "Maria",
            "Michael",
            "ok",
            "Olivia",
            "Owen",
            "Ryan",
            "Sam",
            "Samuel",
            "Sarah",
            "Sophie",
            "Thomas",
            "William"
    };
    private static final String wordsSlang []= {
            "alot",
            "blah",
            "btw",
            "cya",
            "dumbass",
            "fyi",
            "hehe",
            "kinda",
            "l8r",
            "lol",
            "lemme",
            "min",
            "nah",
            "problemo",
            "smth",
            "ttyl"
    };
    private static JazzyInterface instance;
    private SpellChecker spellCheck = null;

    // Convinient Constructors, for those lazy guys.

    public JazzyInterface(String dictFile) throws IOException {
        spellCheck = new SpellChecker(new SpellDictionaryHashMap(ClassUtil.getReader(dictFile)));
    }
    public JazzyInterface(String dictFile, String phoneticFile) throws IOException {
        spellCheck = new SpellChecker(new SpellDictionaryHashMap(ClassUtil.getReader(dictFile), ClassUtil.getReader(phoneticFile)));
    }

    public static synchronized JazzyInterface create() throws IOException {
        if (instance == null) {
            synchronized (JazzyInterface.class) {
                if (instance == null)
                    instance = new JazzyInterface(englishDictionary);
                final UserSpellDictionary dictionary = new UserSpellDictionary();
                for (String aWordsEnglish : wordsEnglish) {
                    dictionary.addWord(aWordsEnglish);
                }
                if (ClientProperties.INSTANCE.isSpellCheckAllowSlang())
                    for (String aWordsSlang : wordsSlang) {
                        dictionary.addWord(aWordsSlang);
                    }
                instance.spellCheck.setUserDictionary(dictionary);
            }
        }
        return instance;
    }

    /**
     * Use this within for the singleton model to work.
     * Assuming it's not thread safe.
     *
     * @return instance ofthe spellChecker
     */
    private synchronized  SpellChecker getSpellChecker() {
        return spellCheck;
    }

    public List getSuggestions(String word) {
        if (GeneralUtils.isNotEmpty(word)) {
            return spellCheck.getSuggestions(word, 2);
        }
        return new ArrayList();
    }

    static class UserSpellDictionary implements SpellDictionary {
        List<String> words = new ArrayList<String>();
        final static List emptyList = new ArrayList(0);

        public void addWord(String word) {
            words.add(word);
        }

        public boolean isCorrect(String word) {
            return words.contains(word);
        }

        public List getSuggestions(String sourceWord, int scoreThreshold) {
            return emptyList;
        }
    }
    // MEMBER METHODS

    /**
     * Set user dictionary (used when a word is added)
     * @param dictionary dictionary to use
     */
    public void setUserDictionary(SpellDictionary dictionary) {
        if (spellCheck != null)
            spellCheck.setUserDictionary(dictionary);
    }


    // --------------------------   HANDLING of SPELLING ------------------------------------------
    static KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, Event.CTRL_MASK, true);
    
    public synchronized void removeSpellCheckComponent(final JTextComponent textComp, SpellCheckingDocumentListener docListener) {
        if (textComp==null) {
            return;
        }
        textComp.unregisterKeyboardAction(keystroke);

        if (docListener != null) {
            textComp.getDocument().removeDocumentListener(docListener);
            docListener.stopProcessing();
        }
    }

    public synchronized SpellCheckingDocumentListener addSpellCheckComponent(final JTextComponent textComp, DelayedThread.StillAliveMonitor monitor) {
        SpellCheckingDocumentListener spellCheckingDocumentListener = new SpellCheckingDocumentListener(textComp, monitor);
        textComp.getDocument().addDocumentListener(spellCheckingDocumentListener);
        addSpellCheckComponentCommon(textComp);
        return spellCheckingDocumentListener;
    }
    
    public synchronized SpellCheckingDocumentListener addSpellCheckComponent(final JTextComponent textComp) {
        SpellCheckingDocumentListener spellCheckingDocumentListener = new SpellCheckingDocumentListener(textComp);
        textComp.getDocument().addDocumentListener(spellCheckingDocumentListener);
        addSpellCheckComponentCommon(textComp);
        return spellCheckingDocumentListener;
    }

    protected synchronized void addSpellCheckComponentCommon(final JTextComponent textComp) {
        textComp.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JTextComponent tc = (JTextComponent) e.getSource();
                int dotPosition = tc.getCaretPosition();
                try {
                    Rectangle popupLocation = textComp.modelToView(dotPosition);
                    WordPopupMenu popup = new WordPopupMenu();
                    popup.show(textComp, popupLocation.x, popupLocation.y, textComp, dotPosition);
                } catch (BadLocationException badLocationException) {
                    System.err.println("Oops, bad location");
                }
            }
        }, keystroke, JComponent.WHEN_FOCUSED);

        // Swap the text menues:
        // Yes, this is cheating, but what the heck.  It lets me keep BetterText* components independenet of the spell stuff
        if (!(BetterTextField.getPopupMenu() instanceof SpecialPopupMenu)) {
            SpecialPopupMenu menu = new SpecialPopupMenu();
            // copy other menu over
            while (BetterTextField.getPopupMenu().getComponentCount() > 0) {
                menu.add(BetterTextField.getPopupMenu().getComponent(0));
            }
            BetterTextField.setPopupMenu(menu);
        }
    }

    public static class SpellCheckingDocumentListener implements DocumentListener {
        private JTextComponent textComp;
        private DelayedThread flagThread;

        public SpellCheckingDocumentListener(JTextComponent textCompIn) {
            this.textComp = textCompIn;                          // 1/2 sec is about right for that
            flagThread = new DelayedActionThread("SpellingThread", 500, textComp, null, getRunnable());
            flagThread.start();
        }
        public SpellCheckingDocumentListener(JTextComponent textCompIn, DelayedThread.StillAliveMonitor monitor) {
            this.textComp = textCompIn;                          // 1/2 sec is about right for that
            flagThread = new DelayedThread("SpellingThread", 500, monitor, null, getRunnable());
            flagThread.start();
        }
        
        public void stopProcessing() {
            if (flagThread!=null) {
                flagThread.stopProcessing();
            }
        }

        private Runnable getRunnable() {
            return new Runnable() {
                public void run() {
//                    log.fine(textComp.hashCode() + " running scpellcheck.");
                    Document document = textComp.getDocument();
                    if (document.getLength() == 0) return; // no need to do this for empty document
                    DocumentWordTokenizer tokenizer = new DocumentWordTokenizer(document);
                    Highlighter h = textComp.getHighlighter();
                    h.removeAllHighlights();
                    List<SpellCheckEvent> errors = null;
                    try {
                        errors = create().getSpellChecker().checkSpellingSilent(tokenizer);
                    } catch (NullPointerException e) {
                        log.log(Level.SEVERE, "JI: Cought a NPE trying to check: " + textComp.getText(), e);
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "JI: Cought a funny exception: " + textComp.getText(), e);
                    }
                    if (errors != null) {
                        for (SpellCheckEvent event : errors) {
                            spellingError(event);
                        }
                    }
                }
            };
        }

        public void insertUpdate(DocumentEvent e) {
            check(e);
        }

        public void removeUpdate(DocumentEvent e) {
            check(e);
        }

        public void changedUpdate(DocumentEvent e) {
            check(e);
        }

        public void check(DocumentEvent e) {
            if (e.getDocument().getLength()>0) { // save effort for empty docs.
                flagThread.mark();
            }
        }

        public void spellingError(final SpellCheckEvent event) {
//            log.fine(textComp.hashCode() + " parsing results for spellcheck.");
//        java.util.List suggestions = event.getSuggestions();
//            event.getSuggestions();
            if (event.getInvalidWord().length()==1)
                return;
            int start = event.getWordContextPosition();
            int end = start + event.getInvalidWord().length();
//            log.fine("event: " + event.getInvalidWord() + " " + start + " " + end);

            // Mark the invalid word in TextComponent
            Highlighter h = textComp.getHighlighter();
            try {
//                h.addHighlight(start, end, DefaultHighlighter.DefaultPainter);
                h.addHighlight(start, end, SpellHighlightPainter.singleton);
            } catch (BadLocationException e) {
                log.log(Level.SEVERE, "Highlighting error", e); // don't really care, but lets see them
            }
        } // spellingError

    } // class SpellCheckingDocument

    /**
     * Singleton Popup class.
     * Allows one to maintain a dictionary based popup thingie.
     *
     * @since  Apr 3, 2006
     * @author Alex Rass
     *
     */
    class SpecialPopupMenu extends BetterTextPopupMenu {
        JMenu suggestions;

        public SpecialPopupMenu() {
            suggestions = new JMenu("Suggestions");
            add(suggestions);
            addSeparator();
        }


        public void show(Component invoker, int x, int y, JTextComponent parent, int carret) throws BadLocationException {
            super.show(invoker, x, y, parent, carret);
            addReplaceWords(parent, suggestions, getSuggestions(ReplaceWordAction.getWord(parent, carret)));
            super.show(invoker, x, y);
        }

        private void addReplaceWords(JTextComponent parent, JMenu menu, List words) {
            menu.removeAll();
            for (Object word : words) {
                menu.add(new ReplaceWordAction(parent, word.toString()));
            }
        }
    } // SpecialPopupMenu

    /**
     * This version simply adds words straight to the JPopupMenu.
     */
    class WordPopupMenu extends BetterTextPopupMenu {
        private void addReplaceWords(JTextComponent parent, List words) {
            removeAll();
            for (Object word : words) {
                add(new ReplaceWordAction(parent, word.toString()));
            }
        }

        public void show(Component invoker, int x, int y, JTextComponent parent, int carret) throws BadLocationException {
            super.show(invoker, x, y, parent, carret);
            String word = ReplaceWordAction.getWord(parent, carret);
            List returnedSuggestions;
            if (word.length()>0 && (returnedSuggestions = getSuggestions(word)).size()>0) {
                addReplaceWords(parent, returnedSuggestions);
                super.show(invoker, x, y);
            }
        }
    } // WordPopupMenu
} // class JazzyInterface