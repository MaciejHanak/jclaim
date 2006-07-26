package com.itbs.aimcer.gui;

import com.itbs.aimcer.bean.Contact;
import com.itbs.aimcer.bean.Message;
import com.itbs.aimcer.bean.Nameable;
import com.itbs.aimcer.commune.*;
import com.itbs.gui.ActionAdapter;
import com.itbs.gui.BetterButton;
import com.itbs.gui.BetterTextPane;
import com.itbs.gui.GUIUtils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Alex Rass
 * @since Jul 25, 2006 8:49:37 AM
 */
//public class MessageCollaborationWindow {}

public class MessageCollaborationWindow  extends MessageWindowBase implements ConnectionEventListener {
    private JTextPane historyPane, messagePane;
    JLabel status = new JLabel();
    ChatRoomSupport connection;

    /**
     * Creates a new <code>JPanel</code> with a double buffer
     * and a flow layout.
     * @param connection connection
     * @param groupName name of the group to join
     */
    public MessageCollaborationWindow(final ChatRoomSupport connection, final String groupName) {
        this.connection = connection;
        frame = GUIUtils.createFrame(groupName + " on " + connection.getServiceName());
        frame.setIconImage(ImageCacheUI.ICON_JC.getIcon().getImage());
        frame.setBounds(DEFAULT_SIZE);
        GUIUtils.addCancelByEscape(frame, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        composeUI();
        offUIExecutor.execute(new Runnable() { public void run () {
           join(groupName, connection.getUser().getName());
        }});
    }

    public JComponent getHistory() {
        historyPane = new BetterTextPane();
        historyPane.setEditable(false);
        return new JScrollPane(historyPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    }

    public void addHistoryText(final String text, final MutableAttributeSet style) {
        GUIUtils.runOnAWT(new Runnable() {
            public void run() {
                if (historyPane == null)
                    return;
                final Document document = historyPane.getDocument();
                try {
                    document.insertString(document.getLength(), text + "\n", style);
                } catch (BadLocationException e) {
                    e.printStackTrace(); // this should never happen unless getLength() is broken.
                }
                historyPane.setCaretPosition(document.getLength());
            }
        });
    }

    protected Component getButtons() {
        Action sendAction = new ActionAdapter("Send", new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if (connection.isLoggedIn()) {
                    if (connection.isJoined()) {
                        addHistoryText("You: " + messagePane.getText(),  ATT_GRAY);
                        connection.sendChatMessage(messagePane.getText());
                        messagePane.setText("");
                    } else {
                        addHistoryText("Failed to join the chat group.  Please reopen the window.", ATT_RED);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Not logged in.   Please login.", "Error:", JOptionPane.ERROR_MESSAGE);
                }
            }
        }, 'S');
        messagePane = new BetterTextPane(sendAction);
        JPanel messageArea = new JPanel(new BorderLayout());
        messageArea.add(new JScrollPane(messagePane), BorderLayout.CENTER);

        messageArea.add(new BetterButton(sendAction), BorderLayout.EAST);
        return messageArea;
    }

    public void join(String room, final String nickname) {
        connection.join(room, nickname, new ChatRoomEventListener() {
            public void serverNotification(String text) {
                addHistoryText(text, ATT_BLUE);
            }

            public boolean messageReceived(ChatRoomSupport connection, Message message) {
                addHistoryText(message.getContact() + ": " + message.getPlainText(), ATT_NORMAL);
                return false;
            }

            public void errorOccured(String message, Exception exception) {
                exception.printStackTrace();
                addHistoryText("Error connecting to the room: " + exception.getMessage(), ATT_RED);
            }
        });
    }

    // ******************** connection stuff.

    public void connectionInitiated(Connection connection) {
    }

    public boolean messageReceived(MessageSupport connection, Message message) throws Exception {
        addHistoryText(message.getText(), ATT_NORMAL);
        return false;
    }

    public void typingNotificationReceived(MessageSupport connection, Nameable contact) {
    }

    public void connectionLost(Connection connection) {
        addHistoryText("Connection Lost", ATT_RED);
    }

    public void connectionFailed(Connection connection, String message) {
        addHistoryText("Connection Failed", ATT_RED);
    }

    public void connectionEstablished(Connection connection) {
        addHistoryText("Connection Established", ATT_RED);
    }

    public void statusChanged(Connection connection) {
    }

    public void errorOccured(String message, Exception exception) {
    }

    public boolean contactRequestReceived(final String user, final MessageSupport connection) {
        return false;
    }


    public boolean emailReceived(MessageSupport connection, Message message) throws Exception {
        return false;
    }

    public void statusChanged(Connection connection, Contact contact, boolean online, boolean away, int idleMins) {
    }

    public void pictureReceived(IconSupport connection, Contact contact) {
    }

    public void fileReceiveRequested(FileTransferSupport connection, Contact contact, String filename, String description, Object connectionInfo) {
    }
}

