package com.itbs.aimcer.log;

import com.itbs.aimcer.bean.ClientProperties;
import com.itbs.aimcer.bean.Group;
import com.itbs.aimcer.bean.Nameable;
import com.itbs.aimcer.commune.AbstractConnection;
import com.itbs.aimcer.gui.Main;
import com.itbs.util.FileCopy;

import javax.swing.*;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Converts Gnome's log files.
 *
 * @author Alex Rass
 * @since Mar 24, 2008 5:05:27 PM
 */
public class LogsPidgin implements Runnable{
    File folder;
    File destination;
    public static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //2004-06-16 13:55:35
    Map<String, LoggerEventListener.HoldingHandle> cachedHandles = new ConcurrentHashMap<String, LoggerEventListener.HoldingHandle>();

    public LogsPidgin(String folderPath, String destinationPath) throws IOException {
        folder = new File(folderPath);
        if (!folder.exists()) {
            throw new IOException("Path doesn't exist. " + folderPath);
        }
        destination = new File(destinationPath);
        if (!destination.exists()) {
            if (!destination.mkdirs()) {
                throw new IOException("Destination folders couldn't be created. " + destinationPath);
            }
        } else { // doesn't exist
            if (!destination.isDirectory()) {
                throw new IOException("Destination isn't a folder. " + destinationPath);
            }
        }
    }

    /** Using these to define the person. */
    class NoConnection extends AbstractConnection {
        /** User who owns the connection */
        Nameable user;
        /** protocol */
        String medium;
        /** contact */
        String contactName;
        Date date;
        /** pidgin log file */
        File fileRef;

        /**
         *
         * @param line "Conversation with yourbudyalex at 2004-06-16 13:55:35 on cloudedprophecy (aim)"
         * @throws  java.text.ParseException when date is off
         */
        NoConnection(final String line, File ref) throws ParseException {
            fileRef = ref;
            if (line.endsWith("(aim)")) {
                medium = "AIM";
            } else if (line.endsWith("(icq)")) {
                medium = "ICQ";
            } else if (line.endsWith("(msn)")) {
                medium = "MSN";
            } else if (line.endsWith("(googletalk)")) {
                medium = "GoogleTalk";
            } else if (line.endsWith("(yahoo)")) {
                medium = "YAHOO";
            } else if (line.endsWith("(jabber)")) {
                medium = "Jabber";
            }
            // get word before last
            StringTokenizer st = new StringTokenizer(line);
            int index = 0;
            // Conv with
            while (st.hasMoreTokens() && index<2) {
                index++;
                st.nextToken();
            }

            // username
            String temp="";
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                if ("at".equals(token)) {
                    break;
                }
                temp+=token;
            }
            contactName = temp;

            temp="";
            index=0;
            while (st.hasMoreTokens() && index<2) {
                String token = st.nextToken();
                if ("on".equals(token)) {
                    break;
                }
                temp+=" "+token;
            }
            date = format.parse(temp);
            temp="";
            if (st.hasMoreTokens()) {
                temp = st.nextToken();
            }
            final String userS = temp;

            this.user = new Nameable() {
                public String getName() {
                    return userS;
                }
            };
        }


        public File getFileRef() {
            return fileRef;
        }

        public Date getDate() {
            return date;
        }

        /**
         * Username for this connection.
         *
         * @return name
         */
        public Nameable getUser() {
            return user;
        }


        public String getContactName() {
            return contactName;
        }

        /**
         * Returns a short display name for the service.
         * "AIM", "ICQ" etc.
         *
         * @return service name
         */
        public String getServiceName() {
            return medium;
        }
        public void reconnect() { }
        public boolean isLoggedIn() { return false; }
        public void cancel() { }
        public void setTimeout(int timeout) { }
        public void addContact(Nameable contact, Group group) { }
        public void removeContact(Nameable contact) { }
        public void addContactGroup(Group group) { }
        public void removeContactGroup(Group group) { }
        public void setAway(boolean away) { }
        public boolean isAway() { return false; }
    }

    public void run() {
        // get full list of files
        File[] logFiles = folder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".txt");
            }
        });
        NoConnection[] info = new  NoConnection[logFiles.length];
        for (int i = 0; i < logFiles.length; i++) {
            File logFile = logFiles[i];
            info[i] = null;
            try {
                BufferedReader br = new BufferedReader(new FileReader(logFile));
                String line = br.readLine();
                info[i] = new NoConnection(line, logFiles[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // no longer care about log files since we can now pull them by
        Arrays.sort(info, new Comparator<NoConnection>() {
            public int compare(NoConnection o1, NoConnection o2) {
                int name = o1.getUser().getName().compareToIgnoreCase(o2.getUser().getName());
                if (name==0)
                    return o1.getDate().compareTo(o2.getDate());
                else
                    return name;
            }
        });

        // ok, now it's sorted by name and date, we can dump them all out.
        try {
            LoggerEventListener lel = new LoggerEventListener(destination);
            for (int i = 0; i < info.length; i++) {
                NoConnection noConnection = info[i];
                LoggerEventListener.HoldingHandle hh = lel.openFile(noConnection, noConnection.getContactName());
                lel.closeFile(noConnection, noConnection.getContactName()); // don't want file references
                FileCopy.copyFile(noConnection.fileRef.getAbsolutePath(), hh.getPath().getAbsolutePath(), true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        LogsPidgin lg = new LogsPidgin(args[0], args[1]);
        lg.run();
    }

    public static void presentItself() {
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setApproveButtonText("Use");
        chooser.setDialogTitle("Locate Pidgin log folder");
//        chooser.setSelectedFile(new File(ClientProperties.INSTANCE.getLastFolder(), name));
        int returnVal = chooser.showSaveDialog(null);
        try {
            if (returnVal == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile().isDirectory() && chooser.getSelectedFile().exists()) {
                LogsPidgin lg = new LogsPidgin(chooser.getSelectedFile().getAbsolutePath(), ClientProperties.INSTANCE.getLogPath().getAbsolutePath());
                lg.run();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Main.complain(e.getMessage(), e);
        }
    }
}
