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

import java.awt.Event;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

import com.itbs.aimcer.bean.ClientProperties;
import com.itbs.util.ClassUtil;
import com.itbs.util.DelayedThread;
import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.event.DocumentWordTokenizer;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellChecker;

/**
 * @author Alex Rass on Nov 7, 2004
 *         Based on examples from Robert Gustavsson (robert@lindesign.se)
 */
public class JazzyInterface {
    private static final String englishDictionary = "/english.0";
    private static final String englishPhonetic = "/phonet.en";
    private static final String wordsEnglish []= {
            "Alex",
            "cancelled",
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
        if (word!=null && word.trim().length()>0)
            return spellCheck.getSuggestions(word, 2);
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
     */
    public void setUserDictionary(SpellDictionary dictionary) {
        if (spellCheck != null)
            spellCheck.setUserDictionary(dictionary);
    }


    // --------------------------   HANDLING of SPELLING ------------------------------------------

    public synchronized void addSpellCheckComponent(final JTextComponent textComp) {
        SpellCheckingDocumentListener spellCheckingDocumentListener = new SpellCheckingDocumentListener(textComp);
        textComp.getDocument().addDocumentListener(spellCheckingDocumentListener);
        
        KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, Event.CTRL_MASK, true);
        textComp.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JTextComponent tc = (JTextComponent) e.getSource();
                int dotPosition = tc.getCaretPosition();
                try {
                    Rectangle popupLocation = textComp.modelToView(dotPosition);
                    EditorTools.WordPopup popup = new EditorTools.WordPopup();
                    popup.show(textComp, popupLocation.x, popupLocation.y, textComp, dotPosition);
                } catch (BadLocationException badLocationException) {
                    System.err.println("Oops, bad location");
                }
            }
        }, keystroke, JComponent.WHEN_FOCUSED);

    }

    private class SpellCheckingDocumentListener implements DocumentListener {
        private JTextComponent textComp;
        private DelayedThread flagThread;

        public SpellCheckingDocumentListener(JTextComponent textCompIn) {
            this.textComp = textCompIn;
            flagThread = new DelayedActionThread("SpellingThread", 500, textComp, null, new Runnable() {
                public void run() {
//                    System.out.println(textComp.hashCode() + " running scpellcheck.");
                    DocumentWordTokenizer tokenizer = new DocumentWordTokenizer(textComp.getDocument());
                    Highlighter h = textComp.getHighlighter();
                    h.removeAllHighlights();
                    List <SpellCheckEvent> errors = null;
                    try {
                        errors = getSpellChecker().checkSpellingSilent(tokenizer);
                    } catch (NullPointerException e) {
                        System.out.println("JI: Cought a NPE trying to check: " + textComp.getText());
                    } catch (Exception e) {
                        System.out.println("JI: Cought a funny exception: " + textComp.getText());
                    }
                    if (errors != null)
                        for (SpellCheckEvent event : errors) {
                            spellingError(event);
                        }
                }
            });
            flagThread.start();
        }

        public void insertUpdate(DocumentEvent e) {
            check();
        }

        public void removeUpdate(DocumentEvent e) {
            check();
        }

        public void changedUpdate(DocumentEvent e) {
            check();
        }

        public void check() {
            flagThread.mark();
        }

        public void spellingError(final SpellCheckEvent event) {
//            System.out.println(textComp.hashCode() + " parsing results for spellcheck.");
//        java.util.List suggestions = event.getSuggestions();
//            event.getSuggestions();
            if (event.getInvalidWord().length()==1)
                return;
            int start = event.getWordContextPosition();
            int end = start + event.getInvalidWord().length();
//            System.out.println("event: " + event.getInvalidWord() + " " + start + " " + end);

            // Mark the invalid word in TextComponent
            Highlighter h = textComp.getHighlighter();
            try {
//                h.addHighlight(start, end, DefaultHighlighter.DefaultPainter);
                h.addHighlight(start, end, SpellHighlightPainter.singleton);
            } catch (BadLocationException e) {
                e.printStackTrace(); // don't really care, but lets see them
            }
        } // spellingError

    } // class SpellCheckingDocument
} // class JazzyInterface