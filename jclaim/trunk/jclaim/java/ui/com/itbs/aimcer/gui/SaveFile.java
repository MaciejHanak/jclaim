package com.itbs.aimcer.gui;

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.commune.Connection;
import com.itbs.aimcer.commune.MessageSupport;
import com.itbs.aimcer.gui.userlist.ContactLabel;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Alex Rass
 * @since Apr 28, 2008 3:37:35 PM
 */
public class SaveFile {
    private static final Logger log = Logger.getLogger(SaveFile.class.getName());
    /** Locks */
    private static final ReentrantLock lock = new ReentrantLock();

    /** Settings file */
    public static final File CONFIG_FILE = new File(System.getProperty("user.home"), "jclaim.ini");
    /** Backup file */
    public static final File CONFIG_FILE_SAV = new File(System.getProperty("user.home"), "jclaim.sav");

    public static void loadProperties() {
        lock.lock();
        try {

        // load data
        if (CONFIG_FILE.exists() && CONFIG_FILE.isFile() && CONFIG_FILE.length() > 0) {
            Object data;
            XMLDecoder d = null;
            try {
/*
                boolean newFormat = true;
                RandomAccessFile raf = new RandomAccessFile(configFile, "r");
                if (raf.length()>0) {
                    newFormat = raf.readChar() != '<';
                }
                raf.close();
                if (newFormat)
*/
                d = new XMLDecoder(new GZIPInputStream(new FileInputStream(CONFIG_FILE)));
/*
                else
                    d = new XMLDecoder(new FileInputStream(configFile));
*/
                data = d.readObject();
                ClientProperties.setInstance((ClientProperties)data);
/*
                data = d.readObject();
                if (data !=null) {
                    if (data instanceof GroupList) {
                        ((GroupWrapperFactory) standardGroupFactory).setGroupList((GroupList) data);
                    } else { // to make sure old properties files are still working. remove some day.
                        ((Connection) data).setProperties(ClientProperties.INSTANCE);
                        ((Connection) data).assignGroupFactory(standardGroupFactory);
                        ((Connection) data).assignContactFactory(standardContactFactory);
                        getConnections().add((MessageSupport) data);
                    }
                }
*/
                try {
                    while (true) {
                        data = d.readObject();
                        if (data != null) {
                            if (data instanceof Connection) {
                                ((Connection) data).setProperties(ClientProperties.INSTANCE);
                                ((Connection) data).assignGroupFactory(Main.standardGroupFactory);
                                ((Connection) data).assignContactFactory(Main.standardContactFactory);
                                Main.getConnections().add((MessageSupport) data);
                            } if (data instanceof List) {
                                generateObjects((List) data);
                            }
                        } // data != null
                    }
                } catch (ArrayIndexOutOfBoundsException  e) {
                    // no care
                }
            } catch (IOException e) {
                if (e.getMessage().startsWith("Not in ") && e.getMessage().endsWith(" format")) {
                    Main.complain("File is corrupt.\n To cure the problem, change a setting.  Your setting will be lost, but the error will be fixed.", e);
                } else {
                    Main.complain("Failed to load settings.\n" + e.getMessage(), e);
                }
            } catch (Exception e) {
                Main.complain("Failed to load settings\n" + e.getMessage(), e);
            } finally {
                if (d!=null)
                    d.close();
            }
        } else {
            ClientProperties.setFirstTimeUse(true);
        }
        } finally {
            lock.unlock();
        }
    }

    public static void saveProperties() {
        lock.lock();
        try {

            CONFIG_FILE_SAV.delete();
            final OutputStream out = new GZIPOutputStream(new FileOutputStream(CONFIG_FILE_SAV));
            XMLEncoder e = new XMLEncoder(out);
            e.writeObject(ClientProperties.INSTANCE);
//            e.writeObject(standardGroupFactory.getGroupList());
            // addConnection what you loaded
            if (Main.getConnections().size() > 1) {
                for (Connection connection : Main.getConnections()) {
                    if (connection instanceof MessageSupport) {
                            e.writeObject(connection);
                    }
                }
            }
            e.writeObject(generateTree());  // must never precede connections.
            e.flush();
            e.close();
            out.flush();
            out.close();
            Thread.yield();
            // save, rename to old.
            if (!CONFIG_FILE.delete()) { log.severe("Failed to delete " + CONFIG_FILE);  Main.complain("Failed to delete old config file."); }
            if (!CONFIG_FILE_SAV.renameTo(CONFIG_FILE)) { log.severe("Failed to rename " + CONFIG_FILE_SAV + " to " + CONFIG_FILE); Main.complain("Failed to rename config file."); }
        } catch (Exception ex) {
            Main.complain("Failed to save config file.", ex);
        } finally {
            lock.unlock();
        }
    }

    public static class ContactStub {
        String name;
        String group;
        String connectionType;
        String loginAs;
        boolean fake;

        /** for xml to use */
        public ContactStub() {
        }

        public ContactStub(ContactWrapper cw, Group group, boolean fake) {
            name = cw.getName();
            connectionType = cw.getConnection().getServiceName();
            loginAs = cw.getConnection().getUser().getName();
            this.group = group.getName();
            this.fake = fake;
        }

        // don't think this one's used yet
        public ContactStub(ContactLabel cl) {
            name = cl.getContact().getName();
            connectionType = cl.getContact().getConnection().getServiceName();
            loginAs = cl.getContact().getConnection().getUser().getName();
            this.group = cl.getGroup().getName();
            this.fake = cl.isFake();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getConnectionType() {
            return connectionType;
        }

        public void setConnectionType(String connectionType) {
            this.connectionType = connectionType;
        }

        public String getLoginAs() {
            return loginAs;
        }

        public void setLoginAs(String loginAs) {
            this.loginAs = loginAs;
        }


        public boolean isFake() {
            return fake;
        }

        public void setFake(boolean fake) {
            this.fake = fake;
        }
    }
    
    public static class GroupStub {
        String name;
        public GroupStub() { }
        public GroupStub(Group group) { name = group.getName();}
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    /**
     * Creates a List of objects to store for ordering information.
     * @return ordered list of simple storable objects.
     */
    static List generateTree() {
//        if (true) return new ArrayList(); // enabling this line and restarting will kill all fake lists.
        List <Object> list  = new ArrayList<Object>(100);
        GroupWrapper groupWrapper;
        GroupList glist = Main.standardGroupFactory.getGroupList(); // using trick that it's static
        for (int i = 0; i < glist.size(); i++) {
            Group group =  glist.get(i);
            if (group instanceof GroupWrapper) {
                groupWrapper = (GroupWrapper) group;
                list.add(new GroupStub(groupWrapper));
//              log.fine("Group: "+g.getName());
                for (int contactCount = 0; contactCount < group.size(); contactCount++) {
                    Nameable contact = group.get(contactCount);
                    if (contact instanceof ContactWrapper) {
                        ContactLabel cl = ContactLabel.construct((ContactWrapper) contact, groupWrapper); // look up: fake?
                        list.add(new ContactStub((ContactWrapper) contact, groupWrapper, cl.isFake()));
                    }
                }
            }
        }
        return list;
    }

    /**
     * Generates the Group/Contact objects from the stored ones.
     * Creates a list of Objects out of the List of Objects.
     * @param list of saved stub objects
     */
   static void generateObjects(List list) {
        Group lastGroup = null;
        for (Object aList : list) {
            if (aList instanceof GroupStub) {
                lastGroup = Main.standardGroupFactory.create(((GroupStub) aList).getName());
                Main.standardGroupFactory.getGroupList().add(lastGroup);
            } else if (lastGroup!=null && aList instanceof ContactStub) {
                Connection connection = findConnection(Main.getConnections(), (ContactStub) aList);
                if (connection!=null) {
                    Contact lastContact = Main.standardContactFactory.create(((ContactStub) aList).getName(), connection);
                    if (((ContactStub) aList).isFake()) {
                        ContactLabel cl = ContactLabel.construct((ContactWrapper) lastContact, lastGroup);
                        cl.setFake(true);
                    }
                    lastGroup.add(lastContact);
                }
            } // if ContactStub
        } // for
    }

    static private Connection findConnection(List<Connection> connections, ContactStub clientStub) {
        for (Connection connection : connections) {
            if (connection.getServiceName().equalsIgnoreCase(clientStub.getConnectionType())
                    && connection.getUser().getName().equalsIgnoreCase(clientStub.getLoginAs())
                    ) {
                return connection;
            }
        }
        return null;
    }
}
