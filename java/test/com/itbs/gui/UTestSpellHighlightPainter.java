package com.itbs.gui;

import com.itbs.aimcer.gui.UTestFrameTest;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Created by  Administrator on Nov 7, 2004
 */
public class UTestSpellHighlightPainter extends UTestFrameTest implements ActionListener {
    JTextArea comp = new JTextArea(5, 20);

    private String charsToHighlight = "aeiouAEIOU";

    public void actionPerformed(ActionEvent e) {
        highlight();

    }

    private void highlight() {
        // highlight all characters that appear in charsToHighlight
        Highlighter h = comp.getHighlighter();
        h.removeAllHighlights();
        String text = comp.getText().toUpperCase();

        for (int j = 0; j < text.length(); j += 1) {
            char ch = text.charAt(j);
            if (charsToHighlight.indexOf(ch) >= 0)
                try {
                    h.addHighlight(j, j + 1, SpellHighlightPainter.singleton);
                } catch (BadLocationException ble) {
                }
        }

        try {
            h.addHighlight(15, 25, SpellHighlightPainter.singleton);
        } catch (BadLocationException ble) {
        }
    }

    public void testMain() throws InterruptedException {
        comp.setText("This is the story\nof the hare who\nlost his spectacles.");
        comp.getDocument().addDocumentListener(new DocumentListener() {
            public void count(DocumentEvent e) { highlight(); }
            public void insertUpdate(DocumentEvent e) { count(e); }
            public void removeUpdate(DocumentEvent e) { count(e); }
            public void changedUpdate(DocumentEvent e) { count(e); }
        });
        window.add(new JScrollPane(comp), BorderLayout.CENTER);

        JButton b = new JButton("Highlight All Vowels");
        b.addActionListener(this);
        window.add(b, BorderLayout.SOUTH);
        window.setVisible(true);
        waitForMe(10000);
    }

}
