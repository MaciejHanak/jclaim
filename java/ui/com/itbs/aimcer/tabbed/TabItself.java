package com.itbs.aimcer.tabbed;

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.commune.FileTransferSupport;
import com.itbs.aimcer.commune.IconSupport;
import com.itbs.aimcer.commune.MessageSupport;
import com.itbs.aimcer.gui.Main;
import com.itbs.aimcer.gui.MessageWindow;
import com.itbs.aimcer.gui.PersonalInfoPanel;
import com.itbs.gui.*;

import javax.swing.*;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Manages the individual tab.
 * Some of the functionality and code is similar to WindowBase/MessageWindow classes,
 * but kept separate so that one doesn't impact the other.
 * 
 * @author Alex Rass
 * @since Feb 28, 2008
 */
public class TabItself extends JPanel {
    private static final Logger log = Logger.getLogger(TabItself.class.getName());
    protected ContactWrapper contactWrapper;

    public ContactWrapper getContact() {
        return contactWrapper;
    }

    public MutableAttributeSet ATT_NORMAL;
    public MutableAttributeSet ATT_RED;
    public MutableAttributeSet ATT_BLUE;
    public MutableAttributeSet ATT_GRAY;

    public static final double DEFAULT_SEPARATION = 3.0 / 5.0;//150;
    /** The way the time in the window is formatted */
    protected DateFormat TIME_FORMAT = new SimpleDateFormat(ClientProperties.INSTANCE.getTimeFormat());

    ButtonTabComponent tabControl;
    BetterTabbedPane tabbedPane;

    /** Used to execute stuff off UI thread */
    static final Executor offUIExecutor = Executors.newFixedThreadPool(2);
    /** Reusable for any component here. */
    final private MessageWindow.FileTransferHandler ftHandler;

    BetterTextPane textPane;
    JTextPane historyPane;
    /** lets us save the vertical separation, that's all. */
    JSplitPane splitHistoryTextPane, splitNotes;

    /** Display Icon for the user */
    JLabel userIcon;

    public TabItself(Contact cw, BetterTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
        setLayout(new BorderLayout());
        contactWrapper = (ContactWrapper) cw;
        ftHandler = new MessageWindow.FileTransferHandler(contactWrapper); // do this before using any components.
        recalculateAttributes();
        splitHistoryTextPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, getHistory(), getMessage());
        splitHistoryTextPane.setOneTouchExpandable(true);
        splitHistoryTextPane.setResizeWeight(1);
        if (contactWrapper.getPreferences().getVerticalSeparation() == -1)
            splitHistoryTextPane.setDividerLocation((int)(tabbedPane.getHeight() * DEFAULT_SEPARATION));
        else
            splitHistoryTextPane.setDividerLocation(contactWrapper.getPreferences().getVerticalSeparation());
        JPanel temp = new JPanel(new BorderLayout());
        temp.add(splitHistoryTextPane);
        PersonalInfoPanel personalInfo = new PersonalInfoPanel(contactWrapper);
        personalInfo.setVisible(true); // always visible for the tabbed interface
        splitNotes = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, temp, personalInfo);
        splitNotes.setOneTouchExpandable(true);
        splitNotes.setResizeWeight(1);
        if (contactWrapper.getPreferences().getHorizontalSeparation() == -1) {
            splitNotes.setDividerLocation((int) (tabbedPane.getWidth() * DEFAULT_SEPARATION));
        } else {
            splitNotes.setDividerLocation((contactWrapper.getPreferences().getHorizontalSeparation()));
        }
        add(splitNotes);
        tabbedPane.addContainerListener(new ContainerAdapter() {
            /**
             * Invoked when a component has been removed from the container.
             */
            public void componentRemoved(ContainerEvent e) {
                if (e.getChild().equals(TabItself.this)) {
                    e.getContainer().removeContainerListener(this); // yeah, can you believe we have to do this?
                    onClose();
                }
            }
        });
        // This is insanely lame! But damn jdk insists on not allowing normal mapping.
        tabbedPane.setupKeys(textPane);
        tabbedPane.setupKeys(historyPane);
        tabbedPane.setupKeys(personalInfo.notes);
    }

    public void addTabComponent() {
        int index = tabbedPane.indexOfComponent(this);
        tabControl = new ButtonTabComponent(tabbedPane, this);
        tabbedPane.setTabComponentAtReflect(index, tabControl);

        if (BetterTabbedPane.oldVM) {
            tabbedPane.setIconAt(index, getContact().getIcon());
        }
        tabControl.setBackground(Color.RED);
    }

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

    private void onClose() {
        contactWrapper.getPreferences().setVerticalSeparation(splitHistoryTextPane.getDividerLocation());
        contactWrapper.getPreferences().setHorizontalSeparation(splitNotes.getDividerLocation());
//        if (orderEntry != null)
//            contactWrapper.getPreferences().setOrderPanelVisible(orderEntry.isVisible());
//        offUIExecutor.execute(new Runnable() { public void run () { Main.saveProperties(); } });
    }


    /**
     * Created the typing window.
     * @return panel with typing space.
     */
    JComponent getMessage() {
        textPane = new BetterTextPane();
        textPane.setFont(textPane.getFont().deriveFont(0.0F + ClientProperties.INSTANCE.getFontSize()));
        if (!ClientProperties.INSTANCE.isEnterSends()) {
            textPane.addModifier(KeyEvent.SHIFT_DOWN_MASK);
            textPane.addModifier(KeyEvent.CTRL_DOWN_MASK);
        }
        JPanel typingSpace = new JPanel(new BorderLayout());
        typingSpace.add(new JScrollPane(textPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        addDNDSupport(textPane);
        userIcon = new JLabel();
        if (ClientProperties.INSTANCE.isShowPictures())
            if (contactWrapper.getPicture() == null && contactWrapper.getConnection() instanceof IconSupport)
                ((IconSupport) contactWrapper.getConnection()).requestPictureForUser(contactWrapper);
             else
                userIcon.setIcon(contactWrapper.getPicture());
        typingSpace.add(userIcon, BorderLayout.EAST);
        userIcon.setVisible(contactWrapper.getPreferences().isShowIcon());
        return typingSpace;
    }

    private void addDNDSupport(JComponent comp) {
        if (contactWrapper.getConnection() instanceof FileTransferSupport) {
            comp.setTransferHandler(ftHandler);
        }
    }

    protected Component getHistory() {
        historyPane = new BetterTextPane();
        addDNDSupport(historyPane);
        historyPane.setEditable(false);
        final JScrollPane jScrollPane = new JScrollPane(historyPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane.getVerticalScrollBar().setValue(jScrollPane.getVerticalScrollBar().getMaximum());

        String historyText;
        try {
            historyText = Main.getLogger().loadLog((MessageSupport) contactWrapper.getConnection(), contactWrapper.getName());
        } catch (IOException e) {
            historyText = "History failed to load. " + (e.getMessage()==null?e.toString():e.getMessage());
        }

        GUIUtils.appendText(historyPane, historyText, ATT_NORMAL);

        if (!contactWrapper.getStatus().isOnline())
            GUIUtils.appendText(historyPane, "\nStatus of this contact is offline or unknown. Your message may not get delivered.\n", ATT_RED);
        if (historyPane.getFont().getSize() > 10)
            StyleConstants.setFontSize(ATT_GRAY, ClientProperties.INSTANCE.getFontSize());
        return jScrollPane;
    }

    /**
     * Convenience method for appending message text.
     * @param message text
     * @param toBuddy receiving or sending.
     */
    void appendHistoryText(Message message, final boolean toBuddy) {
        if (ClientProperties.INSTANCE.getDisclaimerMessage().trim().length() > 0 && ClientProperties.INSTANCE.getDisclaimerInterval() > 0 && (contactWrapper.getLastDisclaimerTime() == 0 ||
                System.currentTimeMillis() - contactWrapper.getLastDisclaimerTime() > ClientProperties.INSTANCE.getDisclaimerInterval())) {
            contactWrapper.setLastDisclaimerTime();
            Message disclMessage = new MessageImpl(contactWrapper, true, false, ClientProperties.INSTANCE.getDisclaimerMessage());
            ((MessageSupport) contactWrapper.getConnection()).sendMessage(disclMessage);
            GUIUtils.appendText(historyPane, ClientProperties.INSTANCE.getDisclaimerMessage(), ATT_NORMAL);
        }
        try {
            GUIUtils.appendText(historyPane, "\n", ATT_NORMAL);
            String prefix = (ClientProperties.INSTANCE.isShowTime() ? TIME_FORMAT.format(new Date()) : "");
            GUIUtils.appendText(historyPane,
                    prefix + (toBuddy? ((MessageSupport) contactWrapper.getConnection()).getUserName():contactWrapper.getDisplayName()) + ": ",
                    toBuddy?ATT_BLUE:ATT_RED);
            MutableAttributeSet color = toBuddy ? ATT_GRAY : ATT_NORMAL;
            color=message.isAutoResponse() ? ATT_RED : color;
            GUIUtils.appendText(historyPane, (message.isAutoResponse()?"Automatic: ":"") + (toBuddy?message.getText():message.getPlainText()), color);

        } catch (Exception e) {
            ErrorDialog.displayError(SwingUtilities.getWindowAncestor(this), "Error processing command.  Try again.\n"+e.getMessage(), e);
        }
    }

    public void setTabHighlighted(final boolean highlight) {
        GUIUtils.runOnAWT(new Runnable() {
            public void run() {
                if (BetterTabbedPane.oldVM) {
                    int index = tabbedPane.indexOfComponent(TabItself.this);
                    tabbedPane.setBackgroundAt(index, highlight?Color.RED:null);
                } else if (tabControl!=null) {
                    tabControl.setOpaque(highlight); // doesn't trigger anything, so I guess we have to do the dance:
                    tabControl.invalidate();
                    tabControl.validate();
                    tabControl.repaint();
                }
            }
        });
    }

    void setLabelFromStatus() {
        if (tabControl!=null) {
            JLabel displayComponent = tabControl.getLabel();
            ContactWrapper contact = getContact();
            if (contact.getStatus().isOnline()) {
                displayComponent.setText(contact.getDisplayName());
                displayComponent.setIcon(contact.getIcon());
                displayComponent.setFont(ContactWrapper.NORM);
                displayComponent.setForeground(contact.getStatus().isAway() ? ContactWrapper.AWAY : ContactWrapper.PRESENT);
                if (contact.getConnection() instanceof MessageSupport) {
                    displayComponent.setToolTipText(contact.getName() + " on " + contact.getConnection().getServiceName() + " as " + ((MessageSupport) contact.getConnection()).getUserName() + (contact.getStatus().isAway() ? (" Idle for " + contact.getStatus().getIdleTime() + "m") : ""));
                }
            } else {
                displayComponent.setText(contact.getDisplayName() + (contact.getStatus().isOnline()?" (Online)":" (Offline)"));
                displayComponent.setIcon(null);
                displayComponent.setFont(ContactWrapper.OFF);
                displayComponent.setForeground(ContactWrapper.AWAY);
                displayComponent.setToolTipText("Last Seen: " + (contact.getPreferences().getLastConnected()==null?"Not yet.":contact.getPreferences().getLastConnected())); // turn off tooltip
            }
        }
    }

}
