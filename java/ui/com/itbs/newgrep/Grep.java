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

package com.itbs.newgrep;

import com.itbs.aimcer.bean.ClientProperties;
import com.itbs.aimcer.gui.ComponentFactory;
import com.itbs.gui.*;
import org.jdesktop.jdic.desktop.Desktop;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Alex Rass
 * @since Jul 20, 2005
 */
public class Grep {
    public static final Dimension INITIAL_SIZE = new Dimension(370, 350);
    public static final MutableAttributeSet ATT_NORMAL;
    private JFrame motherFrame;
    private JTextPane outputTextPane, fileListPane;
    private final Search.Result result;
    ActionAdapter stopAction;
    /** Allows us to stop all we are doing */
    boolean stopIsQuiet=true;
    private final Options uiOptions = new Options();
    private static final String HELP_TEXT = "<HTML><H1>File Search</H1>" +
            "<FONT SIZE=3>Search your files for a text match.<p>"
            + "On the File List Tab:<br><FONT SIZE=2>Drag and drop files and folder you want searched</FONT><p>"
            + "On the Main Tab:<br><FONT SIZE=2>Enter the search text, select options and hit Search</FONT><p>"
            + "Output List Tab:<br><FONT SIZE=2>Will contain the results of your search</FONT><p>"
            + "Notes:<br><FONT SIZE=2>Use 'Filename Search' to just find files.</FONT><p>"
            + "<p>Regular Expressions:<br><FONT SIZE=2>" +
            " &nbsp; &nbsp; text|line - will match lines which contain either 'text' or 'line'<br>" +
            " &nbsp; &nbsp; a*b - will match lines which contain some 'a' followed by a 'b'<br>" +
            " &nbsp; &nbsp; . - Any character<br>" +
            " &nbsp; &nbsp; \\d - Digit [0-9], \\D - Non digit<br>" +
            " &nbsp; &nbsp; \\s - any space character [ \\t\\n\\x0B\\f\\r], \\S - non white space character<br>" +
            " &nbsp; &nbsp; \\w - any word character [a-zA-Z_0-9], \\W - non word character<br>" +
            " &nbsp; &nbsp; Use \\\\ in place of \\ <br>" +
            " &nbsp; &nbsp; For more details Google: pattern java<br>" +
//            " &nbsp; &nbsp; Special characters: \\t - tab<br>" +
            "</FONT>"
            + "</FONT></HTML>";
    Executor offUIThread = Executors.newSingleThreadExecutor();

    static {
        ATT_NORMAL = new SimpleAttributeSet();
        StyleConstants.setFontFamily(ATT_NORMAL,"Monospaced");
        StyleConstants.setFontSize(ATT_NORMAL, ClientProperties.INSTANCE.getFontSize());
    }

    public Grep() {
        motherFrame = GUIUtils.createFrame("Grep");
        motherFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel topPanel = prepScreen();
        TransferHandler fileHandler = new TransferHandler();
        topPanel.setTransferHandler(fileHandler);
        fileListPane.setTransferHandler(fileHandler);
        motherFrame.getContentPane().setLayout(new GridLayout(1, 1));
        motherFrame.getContentPane().add(topPanel);
        motherFrame.setSize(INITIAL_SIZE);//motherFrame.pack();
//        motherFrame.setJMenuBar(MenuManager.getMenuBar());
//        TrayAdapter.create(ClientProperties.INSTANCE.isUseTray(), motherFrame, ImageCacheUI.ICON_JC, "JClaim");
        motherFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent event) {
                stopIsQuiet = false; // stop all we are doing
                motherFrame.dispose();
            }
        });
        result = new Search.Result() {
            public void startResource(String name) {
                if (uiOptions.printFilenameInOuput && !uiOptions.fileSearch) {
                    addOutputText("== " + name + " ==\n");
                }
            }

            public void append(Search.Where whatFound) {
                if (uiOptions.fileSearch)
                    addOutputText(whatFound.getFile()+"\n");
                else
                    addOutputText(whatFound.getLineNumber() + ":" + whatFound.getLine() + "\n");
            }
        };
        motherFrame.setVisible(true);
    }

    public static void main(String[] args) {
        System.setProperty("com.apple.macos.useScreenMenuBar", "true");
        new Grep();
    }

    private JPanel prepScreen() {
        JPanel panel = new JPanel(new GridLayout(1, 1));
        JTabbedPane tabbedPane = new JTabbedPane();

        Component panel1 = getMainPanel();
        tabbedPane.addTab("Main", panel1);
        tabbedPane.setSelectedIndex(0);

        Component panel2 = getFileListPanel();
        tabbedPane.addTab("File List", panel2);

        Component panel3 = getOutputPanel();
        tabbedPane.addTab("Output List", panel3);

        Component panel4 = getHelpPanel();
        tabbedPane.addTab("Help", panel4);

        panel.add(tabbedPane);
        return panel;
    }

    private Component getHelpPanel() {
        return new JScrollPane(ComponentFactory.getTopBar(HELP_TEXT));
    }

    private Component getFileListPanel() {
        fileListPane = new BetterTextPane();
        fileListPane.setBorder(new BevelBorder(BevelBorder.LOWERED));
        fileListPane.setToolTipText("Drag and drop files and folders");
        return new JScrollPane(fileListPane);
    }

    private Component getOutputPanel() {
        outputTextPane = new BetterTextPane();
        outputTextPane.setBorder(new BevelBorder(BevelBorder.LOWERED));
        return new JScrollPane(outputTextPane);
    }

    void addOutputText(String text) {
        GUIUtils.appendText(outputTextPane, text, ATT_NORMAL);
    }

    private Component getMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel searchPanel = new JPanel(new FlowLayout());
        final JTextField string = new BetterTextField(9);
        searchPanel.add(string);
        searchPanel.add(new BetterButton(new ActionAdapter("Search", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // call to do the search
                if (string.getText().length()==0) {
                    JOptionPane.showMessageDialog(string, "Please enter what to search for");
                } else {
                    stopAction.setEnabled(true);
                    stopIsQuiet = true; // reset flag
                    offUIThread.execute(new Runnable() {
                        public void run() {
                            BufferedReader br = new BufferedReader(new StringReader(fileListPane.getText()));
                            String line;
                            Search.What what;
                            if (uiOptions.fileSearch)
                                what = new FileSearch(string.getText());
                            else
                                what = new SingleString(string.getText());
                            Options options = (Options) uiOptions.clone();
                            if (options.clearOutput) {
                                outputTextPane.setText("");
                            }
                            try {
                                if (options.logSearch)
                                    performSearch(new File[]{ClientProperties.INSTANCE.getLogPath()}, what, options);
                                while ((line = br.readLine()) != null && stopIsQuiet) {
                                    if (line.trim().length() > 0)
                                        performSearch(new File[]{new File(line)}, what, options);
                                }
                            } catch (IOException ex) {
                                addOutputText("Error: " + ex.toString());
                            }
                            if (!uiOptions.silentFinish)
                                Toolkit.getDefaultToolkit().beep();
                            stopAction.setEnabled(false);
                        }
                    });
                }
            }
        }, 'S')));
        stopAction = new ActionAdapter("Stop", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                stopIsQuiet = false;
            }
        }, 'P');
        stopAction.setEnabled(false);
        searchPanel.add(new BetterButton(stopAction));
        ActionAdapter googleAction = new ActionAdapter("Google", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
//                String url = "http://www.google.com/search?q=" + string.getText();

                String url = null;
                try {
                    url = URLEncoder.encode(string.getText().trim(), "UTF-8");
                    url = BetterTextField.GoogleAction.GOOGLE_URL + url;
                    try {
                        Desktop.browse(new URL(url));
                    } catch (Exception exc) {
                        ErrorDialog.displayError(motherFrame, "Failed to launch url " + url, exc);
                    } catch (UnsatisfiedLinkError exc) {
                        ErrorDialog.displayError(motherFrame, "Failed to locate native libraries.  Please open "+url + " yourself.", exc);
                    }
                } catch (UnsupportedEncodingException ex) {
                    ErrorDialog.displayError(motherFrame, "Failed to convert URL.  Please check your search.", ex);
                }
            }
        }, 'G');
        searchPanel.add(new BetterButton(googleAction));
        panel.add(searchPanel, BorderLayout.NORTH);
        JPanel optionsPanel = new JPanel(new GridLayout(0, 2));
        optionsPanel.setBorder(new TitledBorder(" Options: "));
        optionsPanel.add(new JCheckBox(new ActionAdapter("Case Sensitive", "", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                uiOptions.caseSensitive = ((JCheckBox) e.getSource()).isSelected();
            }
        })));
        optionsPanel.add(new JCheckBox(new ActionAdapter("Reverse Logic", "", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                uiOptions.reverseLogic = ((JCheckBox) e.getSource()).isSelected();
            }
        })));
        optionsPanel.add(new JCheckBox(new ActionAdapter("Starts with", "", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                uiOptions.beginningOfALine = ((JCheckBox) e.getSource()).isSelected();
            }
        })));
        optionsPanel.add(new JCheckBox(new ActionAdapter("Ends with", "", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                uiOptions.endOfALine = ((JCheckBox) e.getSource()).isSelected();
            }
        })));
        optionsPanel.add(new JCheckBox(new ActionAdapter("Regular Expression", "Interpret as regular expression", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                uiOptions.regularExpression = ((JCheckBox) e.getSource()).isSelected();
            }
        })));
        optionsPanel.add(new JCheckBox(new ActionAdapter("Clear Output", "", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                uiOptions.clearOutput = ((JCheckBox) e.getSource()).isSelected();
            }
        })));
        optionsPanel.add(new JCheckBox(new ActionAdapter("No Filenames in output", "", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                uiOptions.printFilenameInOuput = !((JCheckBox) e.getSource()).isSelected();
            }
        })));
        optionsPanel.add(new JCheckBox(new ActionAdapter("No Sound", "", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                uiOptions.silentFinish = ((JCheckBox) e.getSource()).isSelected();
            }
        })));
        optionsPanel.add(new JCheckBox(new ActionAdapter("Filename Search", "Find files with this name", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                uiOptions.fileSearch = ((JCheckBox) e.getSource()).isSelected();
            }
        })));
        optionsPanel.add(new JCheckBox(new ActionAdapter("Search log", "Include the logs in your search", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                uiOptions.logSearch = ((JCheckBox) e.getSource()).isSelected();
            }
        })));
        panel.add(optionsPanel);

        return panel;
    }

    private void performSearch(File[] files, final Search.What what, final Options options) throws IOException {
        // go through all the files
        // do files first
        for (int i = 0; i < files.length && motherFrame.isDisplayable() && stopIsQuiet; i++) { // go through all files
            if (files[i].exists()) {
                if (! files[i].isDirectory()) {
                    result.startResource(files[i].getName());
                    what.find(files[i], what, result, options);
                }
            } else  {
                result.startResource(files[i].getName() + " - Not Found!");
            }
        }
        // do folders after - looks better when they are grouped separately.
        for (int i = 0; i < files.length && motherFrame.isDisplayable() && stopIsQuiet; i++) {// go through all folders.
            if (files[i].isDirectory()) {
                performSearch(files[i].listFiles(), what, options);
            }
        }

    }

    private class TransferHandler extends AbstractFileTransferHandler {

        protected void handle(JComponent c, List<File> fileList) {
            for (File file : fileList) {
                GUIUtils.appendText(fileListPane, "\n" + file.getAbsolutePath(), ATT_NORMAL);
            }
        }
    }
} // class Grep
