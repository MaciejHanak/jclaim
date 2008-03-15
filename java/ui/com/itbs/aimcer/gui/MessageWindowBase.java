package com.itbs.aimcer.gui;

import com.itbs.aimcer.bean.ClientProperties;
import com.itbs.gui.BetterTextPane;
import com.itbs.gui.GUIUtils;
import com.itbs.gui.JazzyInterface;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  Provides base methods for any messaging window (so far).
 *
 * @author Alex Rass
 * @since Jul 23, 2006 12:20:07 PM
 */
abstract public class MessageWindowBase {
    private static final Logger log = Logger.getLogger(MessageWindowBase.class.getName());

    public MutableAttributeSet ATT_NORMAL;
    public MutableAttributeSet ATT_RED;
    public MutableAttributeSet ATT_BLUE;
    public MutableAttributeSet ATT_GRAY;

    /** Size of the message box. */
    public static final Rectangle DEFAULT_SIZE = new Rectangle(420, 200, 350, 330);
    /** Where the line is. */
    public static final double DEFAULT_SEPARATION = 3.0 / 5.0;//150;
    /** Used to execute stuff off UI thread */
    static final Executor offUIExecutor = Executors.newFixedThreadPool(2);

    /** message frame. */
    protected JFrame frame;
    BetterTextPane textPane;
    JTextPane historyPane;
    /** lets us save the vertical separation, that's all. */
    JSplitPane splitPane;
    AbstractAction ACTION_SEND;



    /** The way the time in the window is formatted */
    protected DateFormat TIME_FORMAT = new SimpleDateFormat(ClientProperties.INSTANCE.getTimeFormat());

    void recalculateAttributes() {
        ATT_NORMAL = new SimpleAttributeSet();
        StyleConstants.setFontFamily(ATT_NORMAL,"Monospaced");
        StyleConstants.setFontSize(ATT_NORMAL, ClientProperties.INSTANCE.getFontSize()+1);
        ATT_BLUE = (MutableAttributeSet) ATT_NORMAL.copyAttributes();
        ATT_RED = (MutableAttributeSet) ATT_NORMAL.copyAttributes();
        ATT_GRAY = (MutableAttributeSet) ATT_NORMAL.copyAttributes();
        StyleConstants.setForeground(ATT_BLUE, Color.BLUE);
        StyleConstants.setForeground(ATT_RED,  Color.RED);
        StyleConstants.setForeground(ATT_GRAY, Color.GRAY);
    }

    /**
     * Helper.
     * @param text to append
     */
    void appendHistoryText(final String text) {
        appendHistoryText(text, ATT_NORMAL);
    }

    public void appendHistoryText(final String text, final AttributeSet style) {
        GUIUtils.appendText(historyPane, text, style);
    }


    public JTextPane getHistoryPane() {
        return historyPane;
    }

    /**
     * Just Close!
     */
    public void closeWindow() {
        frame.dispose();
    }

    /**
     * Adds history pane and typing space.
     * @return panel with components
     */
    protected JComponent getTextComponents() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, getHistory(), getMessage());
        splitPane.setDividerLocation((int)(frame.getHeight() * DEFAULT_SEPARATION));
        return splitPane;
    }

    abstract protected Component getHistory();
    abstract protected Component getButtons();

    protected void composeUI() {
        recalculateAttributes();
        frame.getContentPane().setLayout(new BorderLayout());
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                frame.getContentPane().add(getTextComponents());
                frame.getContentPane().add(getButtons(), BorderLayout.SOUTH);
                frame.setVisible(true);
                try {
                    if (ClientProperties.INSTANCE.isSpellCheck())
                        JazzyInterface.create().addSpellCheckComponent(textPane);
                } catch (IOException e) {
                    log.log(Level.SEVERE, "", e);
                }
                startUIDependent();
                textPane.requestFocus();
            }
        });
    }

    protected void startUIDependent() {};


    /**
     * Created the typing window.
     * @return panel with typing space.
     */
    JComponent getMessage() {
        textPane = new BetterTextPane(ACTION_SEND);
        textPane.setFont(textPane.getFont().deriveFont(0.0F + ClientProperties.INSTANCE.getFontSize()));
//        textPane.setFont(textPane.getFont().deriveFont(Font.PLAIN)); // this didn't appear to do anything, the font just looks bold, I guess.
//        textPane.setContentType("text/html");
        if (!ClientProperties.INSTANCE.isEnterSends()) {
            textPane.addModifier(KeyEvent.SHIFT_DOWN_MASK);
            textPane.addModifier(KeyEvent.CTRL_DOWN_MASK);
        }
        JPanel typingSpace = new JPanel(new BorderLayout());
        typingSpace.add(new JScrollPane(textPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        return typingSpace;
    }

}
