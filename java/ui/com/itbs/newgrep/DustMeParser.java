package com.itbs.newgrep;

import com.itbs.gui.*;
import com.itbs.util.CSVHelper;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alex Rass
 * @since Dec 16, 2008 11:21:45 PM
 */
public class DustMeParser implements Runnable {
    Logger log = Logger.getLogger(DustMeParser.class.getName());
    File dustmeCSV, css;
    /** 0-based column inside csv file <p> default is 0*/
    int column=0;
    PrintStream out = System.out;
    JTextComponent outTF;

    // ------------------------vvv  GUI  vvv---------------------------
    protected static class GUI {
        FileChooserButton fileCSS;
        FileChooserButton fileCSV;
        BetterTextField columnField;
        BetterTextPane outputPane;
        JDialog dialog;

        protected GUI() {
            dialog = new JDialog((Frame)null, "CSS Cleaner:", false);
            GUIUtils.addCancelByEscape(dialog);
            dialog.getContentPane().setLayout(new BorderLayout());
            dialog.getContentPane().add(getExplanationPane(), BorderLayout.NORTH);
            dialog.getContentPane().add(getTabbedPane());
            dialog.getContentPane().add(getButtonPane(), BorderLayout.SOUTH);
            dialog.pack();
            dialog.setVisible(true);
        }

        private static Component getExplanationPane() {
            JPanel panel = new JPanel();
            JLabel label = new JLabel("<html><p>This screen will help you with cleaning your CSS files.</p>" +
                    "<p>This program takes csv file which lists unused css styles and removes them from the css file.</p>" +
                    "<hr>"+
                    "<p>Step 1: Get/open FireFox. And get a plugin called <i>Dust-Me Selectors</i></p>"+
                    "<p>Step 2: Run it, get your results and save the csv file off to some location</p>"+
                    "<p>Step 3: populate the edit boxes below with data and hit Go.</p>"+
                    "<hr>"+
                    "<p>You will then have the new results below.</p>"+
                    "<p>You may have to clean up a few untagged styles afterwards.<br> They are easy to find as they have no class or id names.<br>They are usually kept because of comments preceding them.</p>"+
                    "<p>If you improve this program, please send it to us for everyone's benefit.</p>"+
                    "</html>"
//                "<p></p>"
            );
            panel.add(label);
            return panel;
        }
        private Component getTabbedPane() {
            JPanel panel = new JPanel();
            fileCSS = new FileChooserButton(dialog, "CSS File");
            fileCSV = new FileChooserButton(dialog, "CSV File");
            columnField = new BetterTextField(2);
            columnField.setDocument(new BetterTextField.NumberDocument());
            columnField.setToolTipText("Column overrided for CSV file. 0 is default.");
            columnField.setText("0");
            panel.add(fileCSS);
            panel.add(fileCSV);
            panel.add(columnField);
            outputPane = new BetterTextPane();
            outputPane.setText("Output will go here");
            JScrollPane pane = new JScrollPane(outputPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            pane.setPreferredSize(new Dimension(300, 200));
            panel.add(pane);
            return panel;
        }

        private Component getButtonPane() {
            JPanel panel = new JPanel();
            JButton button;
            button = new JButton(new ActionAdapter("Go", (String)null, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (!fileCSV.getFile().exists() || !fileCSS.getFile().exists()) {
                        JOptionPane.showMessageDialog(dialog, "Files aren't set.");
                        return;
                    }
                    try {
                        outputPane.setText("");
                        DustMeParser dmp = new DustMeParser(fileCSV.getFile(), fileCSS.getFile());
                        dmp.column = Integer.parseInt(columnField.getText());
                        dmp.outTF = outputPane;
                        dmp.run();
                        dialog.pack();
                    } catch (Exception ex) {
                        ErrorDialog.displayError(dialog, "Failed to run dustme parser", ex);
                    }
                }
            }));
            panel.add(button);
            button = new JButton(new ActionAdapter("Close", (String)null, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dialog.dispose();
                }
            }));
            panel.add(button);
            return panel;
        }
    } // class GUI
    // ------------------------^^^  GUI  ^^^---------------------------

    DustMeParser(File csv, File css) {
        this.dustmeCSV = csv;
        this.css = css;
    }
    
    public static void main(String[] args) {
        if (args.length<2) {
            presentItself();
            return;
        }
        File file1 = new File(args[0]);
        File file2 = new File(args[1]);
        if (!ensure(file1) || !ensure(file1)) {
            printUsage();
            return;
        }
        DustMeParser parser;
        if (file1.getName().endsWith(".csv")) {
            parser = new DustMeParser(file1, file2);
        } else if (file1.getName().endsWith(".css")) {
            parser = new DustMeParser(file2, file1);
        } else {
            printUsage();
            return;
        }
        parser.run();
    }

    public static void presentItself() {
        new GUI();
    }

    private static void printUsage() {
        System.err.println("Must have 2 files: csv file from DustMe and the css file to parse.");
    }

    private static boolean ensure(File file) {
        if (file.exists() && !file.isDirectory() && file.length()>0 && file.canRead()) return true;
        
        System.err.println("File doesn't exist or empty or unreadeable: "+file);
        return false;
    }


/*
    public void run2() {
        int lineNo = 0;
        try {
            loadDust();
            BufferedReader cssReader = new BufferedReader(new FileReader(css));
            Parser parser = new Parser(new Generic_CharStream(cssReader, 1, 1));
            parser.expr();
            parser = parser;
        } catch (Throwable e) {
            log.log(Level.SEVERE, "Failed to process line " + lineNo, e);
        }
    }
*/

    List<String> removables = new ArrayList<String>(500);    
    protected void loadDust() throws Exception {
        BufferedReader dustReader = new BufferedReader(new FileReader(dustmeCSV));
        List<String> items;
        String line;
        int lineNo = 0;
        while ((line = dustReader.readLine()) != null) {
            lineNo++;
            items = CSVHelper.performCSVParsing(line);
            if (lineNo >= 4) { // process titles
                removables.add(items.get(column).trim());
            } // if
        } // while

    }
    public void run() {
        String line;
        int lineNo = 0;
        try {
            loadDust();
            
            BufferedReader cssReader = new BufferedReader(new FileReader(css));
            List<String> items;
            String key;
            boolean braceInside=false, lonely=true, deleting=false;

            // run till we find the key.
            while ((line = cssReader.readLine()) != null) {
                    // found the line
                items = CSVHelper.performCSVParsing(line, ",{}", true);
                if (items.size()==0) continue;
                for (String subString : items) {
                    subString = subString.trim();
                    if ("{".equals(subString)) {
                        braceInside = true;
                    }
                    // process removal/keep
                    if (removables.contains(subString)
                            || (deleting && ",".equals(subString))
                            || (deleting && lonely && braceInside)
                        ) {
                        // remove it
                        deleting = true;
                    } else {
                        deleting = false;
                        print(subString);
                        lonely = false;
                    }
                    if ("}".equals(subString)) {
                        braceInside = false;
                        lonely=true;
                    }
                }
                if (!(lonely && deleting)) {
                    println("");
                }
            } // while
            log.info("Loaded " + lineNo + " lines ok.");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to process line " + lineNo, e);
        }
    } // run

    protected void print(String line) {
        if (outTF != null) {
            GUIUtils.appendText(outTF, line, null);
        } else {
            out.print(line);
        }    
    }

    protected void println(String line) {
        print(line);
        print("\n");
    }

} // class
