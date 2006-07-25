package com.itbs.aimcer.gui;

/**
 * @author Alex Rass
 * @since Jul 25, 2006 8:49:37 AM
 */
public class MessageCollaborationWindow {}
/*
public class MessageCollaborationWindow  extends MessageWindowBase implements ConnectionEventListener {
    private JTextPane historyPane, messagePane;
    JLabel status = new JLabel();
    CRApplet applet;
    MutableAttributeSet ATT_NORMAL, ATT_RED, ATT_BLUE, ATT_GRAY;

    */
/**
     * Creates a new <code>JPanel</code> with a double buffer
     * and a flow layout.
     */
/*
    public MessageCollaborationWindow(CRApplet crApplet) {
        this.applet = crApplet;
        frame.setLayout(new BorderLayout());
        JPanel panel = LoginTab.getTitleLabel("How Can We Help You?");
        frame.add(panel, BorderLayout.NORTH);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, getHistoryPane(), getMessagePane());
        splitPane.setDividerLocation(85);
        frame.add(splitPane);
        recalculateAttributes();
    }

    public JComponent getHistoryPane() {
        historyPane = new BetterTextPane();
        historyPane.setEditable(false);
        return new JScrollPane(historyPane);
    }

    public void addHistoryText(final String text, final MutableAttributeSet style) {
        GeneralUtils.runOnAWT(new Runnable() {
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
    private Component getMessagePane() {
        Action sendAction = new ActionAdapter("Send", new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if (applet.connection.isLoggedIn()) {
                    if (applet.connection.isJoined()) {
                        addHistoryText("You: " + messagePane.getText(),  ATT_GRAY);
                        applet.connection.sendChatMessage(messagePane.getText());
                        messagePane.setText("");
                    } else {
                        JOptionPane.showMessageDialog(frame, "Failed to join the chat group.  Please relogin.", "Error:", JOptionPane.ERROR_MESSAGE);
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
        JPanel south = new JPanel(new BorderLayout());
        south.add(new JLabel(" You are chatting with CyberCreek"));
        south.add(status, BorderLayout.EAST);
        messageArea.add(south, BorderLayout.SOUTH);
        return messageArea;
    }

    public void join(String room, final String nickname) {
        applet.connection.join(room, nickname, new ChatRoomEventListener() {
            public void serverNotification(String text) {
                addHistoryText(text, ATT_BLUE);
            }

            public boolean messageReceived(ChatRoomSupport connection, com.itbs.commune.Message message) throws Exception {
                addHistoryText(message.getContact() + ": " + message.getPlainText(), ATT_NORMAL);
                return false;
            }

            public void errorOccured(String message, Exception exception) {
                exception.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error connecting to the room: " + exception.getMessage(), "Error:", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // ******************** connection stuff.

    public void connectionInitiated(Connection connection) {
    }

    public boolean messageReceived(MessageSupport connection, com.itbs.commune.Message message) throws Exception {
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

}
*/
