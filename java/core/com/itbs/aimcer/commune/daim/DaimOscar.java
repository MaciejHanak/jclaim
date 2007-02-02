package com.itbs.aimcer.commune.daim;

import com.itbs.aimcer.commune.ConnectionInfo;
import org.walluck.oscar.*;
import org.walluck.oscar.channel.aolim.AOLIM;
import org.walluck.oscar.channel.rendezvous.BuddyIconRendezvous;
import org.walluck.oscar.channel.rendezvous.ChatRendezvous;
import org.walluck.oscar.channel.rendezvous.Rendezvous;
import org.walluck.oscar.channel.rendezvous.TrillianSecureIMRendezvous;
import org.walluck.oscar.client.Buddy;
import org.walluck.oscar.client.BuddyGroup;
import org.walluck.oscar.handlers.*;
import org.walluck.oscar.handlers.directim.DirectIM;
import org.walluck.oscar.handlers.filetransfer.FT;
import org.walluck.oscar.handlers.filetransfer.FTTLV;
import org.walluck.oscar.handlers.filetransfer.FileTransfer;
import org.walluck.oscar.handlers.icq.*;
import org.walluck.oscar.handlers.news.NewsHandler;
import org.walluck.oscar.handlers.secureim.SecureIM;
import org.walluck.oscar.handlers.stocks.StocksHandler;
import org.walluck.oscar.handlers.trilliansecureim.TrillianSecureIM;

import java.io.*;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is just a demo to show that some of this stuff actually works. All of
 * this needs to be modularized and put into the corresponding tool anyway.
 *
 * <p><em>Any volunteers?</em></p>
 *
 * @author <a href="mailto:walluck@dev.java.net">David Walluck</a>
 * @version 1.0
 * @see ServiceListener LocateListener BuddyListListener ICBMListener
 * UserLookupListener PopupListener BOSListener StatsListener ChatNavListener
 * ChatListener ODirListener IconListener SSIListener ICQListener LoginListener
 * MailListener MiscListener
 * @since 1.0
 *
 *
 * Notes:  This class is a rip from David's class by name Oscar.  Trying to adopt to our needs by doing callbacks.
 */
public class DaimOscar implements ServiceListener, LocateListener,
                              BuddyListListener, ICBMListener,
                              UserLookupListener, PopupListener, BOSListener,
                              StatsListener, ChatNavListener, ChatListener,
                              ODirListener, IconListener, SSIListener,
                              ICQListener, LoginListener, MailListener,
                              MiscListener {


    DaimConnection callbacks;

    /**
     * Constructor.
     * @param callbacks to use
     */
    public DaimOscar(DaimConnection callbacks) {
        this.callbacks = callbacks;
    }


    /** The log. */
    private static final Logger LOG =
        Logger.getLogger(org.walluck.oscar.client.Oscar.class.getName());

    /** Rate clear thread. */
    private RateClearTask rct;

    /** The screen name. */
    private String sn;
    /** The password. */
    private String password;

    /** Unavailable. */
    private static final int UC_UNAVAILABLE = 0x01;
    /** AOL. */
    private static final int UC_AOL = 0x02;
    /** Admin. */
    private static final int UC_ADMIN = 0x04;
    /** Unconfirmed. */
    private static final int UC_UNCONFIRMED = 0x08;
    /** Normal. */
    private static final int UC_NORMAL = 0x10;
    /** ActiveBuddy. */
    private static final int UC_AB = 0x20;
    /** Wireless. */
    private static final int UC_WIRELESS = 0x40;
    /** Hiptop. */
    private static final int UC_HIPTOP = 0x80;

    /** AIM memory data. */
    private static final String AIMHASHDATA =
        "http://gaim.sourceforge.net/aim_data.php3";

    /* All AIM capabilities.
     private static final int AIM_CAPS_ALL = AIMConstants.AIM_CAPS_GETFILE
        | AIMConstants.AIM_CAPS_GAMES | AIMConstants.AIM_CAPS_SENDBUDDYLIST
        | AIMConstants.AIM_CAPS_CHAT | AIMConstants.AIM_CAPS_VOICE
        | AIMConstants.AIM_CAPS_SENDFILE | AIMConstants.AIM_CAPS_IMIMAGE
        | AIMConstants.AIM_CAPS_BUDDYICON | AIMConstants.AIM_CAPS_SAVESTOCKS
        | AIMConstants.AIM_CAPS_INTEROPERATE
        | AIMConstants.AIM_CAPS_SECUREIM_CAPABLE | AIMConstants.AIM_CAPS_SHORT
        | AIMConstants.AIM_CAPS_SECUREIM
        | AIMConstants.AIM_CAPS_TRILLIANCRYPT; */
    /**
     * The AIM capabilities that we support (need Get File and Voice
     * support.
     */
    private int aimCaps = AIMConstants.AIM_CAPS_GETFILE
        | AIMConstants.AIM_CAPS_GAMES | AIMConstants.AIM_CAPS_SENDBUDDYLIST
        | AIMConstants.AIM_CAPS_CHAT | AIMConstants.AIM_CAPS_VOICE
        | AIMConstants.AIM_CAPS_SENDFILE | AIMConstants.AIM_CAPS_IMIMAGE
        | AIMConstants.AIM_CAPS_BUDDYICON | AIMConstants.AIM_CAPS_SAVESTOCKS
        | AIMConstants.AIM_CAPS_INTEROPERATE
        | AIMConstants.AIM_CAPS_SECUREIM_CAPABLE | AIMConstants.AIM_CAPS_SHORT
        | AIMConstants.AIM_CAPS_SECUREIM | AIMConstants.AIM_CAPS_TRILLIANCRYPT;

    /**
     * ICQ capabilities.
     */
    private static final int ICQ_CAPS = AIMConstants.AIM_CAPS_ICQSERVERRELAY
        | AIMConstants.AIM_CAPS_ICQUTF8 | AIMConstants.AIM_CAPS_ICQRTF
        | AIMConstants.AIM_CAPS_ICQ | AIMConstants.AIM_CAPS_TRILLIANCRYPT;

    /**
     * Describe <code>getAIMCaps</code> method here.
     *
     * @return an <code>int</code> value
     */
    public int getAIMCaps() {
        return aimCaps;
    }

    /**
     * Describe <code>getICQCaps</code> method here.
     *
     * @return an <code>int</code> value
     */
    public static int getICQCaps() {
        return ICQ_CAPS;
    }

    /** BOS connection. */
    private AIMConnection bosconn;

    /** Buddy groups. */
    private ArrayList buddyGroups;

    /** Buddy icons. */
    private Hashtable icons = new Hashtable();

    /** Trillian SecureIM sessions. */
    private Hashtable trillianSessions = new Hashtable();

    /** Stocks. */
    private ArrayList stocks;

    /** News. */
    private ArrayList news;

    /** Format screenname. */
    private boolean formatScreenname = false;

    /** Change password. */
    private boolean changePassword = false;

    /** Confirm account. */
    private boolean confirmAccount = false;

    /** Get email address. */
    private boolean getEmail = false;

    /** Set email address. */
    private boolean setEmail = false;

    /** Email. */
    private String email;

    /** Nick. */
    private String nick;

    /** Old password. */
    private String oldPassword;

    /** New password. */
    private String newPassword;

    /** Max sig len. */
    private int maxSigLen = 0;

    /**
     * Get an ICQ packet from the given frame.
     *
     * @param frame the frame
     * @return the ICQ packet
     */
     public ICQPacket getICQPacket(AIMFrame frame) {
        ICQPacket recvicqPacket = frame.getICQPacket();
        int pid = recvicqPacket.getId();

        ICQPacket sendicqPacket
            = (ICQPacket) frame.getConn().getICQHashtable().
                          remove(new Integer(pid));

        if (sendicqPacket == null) {
            LOG.warning("No ICQ packet found for recvdpid=0x"
                     + Integer.toHexString(pid));
            return null;
        }

        return sendicqPacket;
     }

    /**
     * Describe <code>getBuddyGroups</code> method here.
     *
     * @return a <code>ArrayList</code> value
     */
    public synchronized ArrayList getBuddyGroups() {
        return buddyGroups;
    }

    /**
     * Describe <code>printBuddyList</code> method here.
     *
     */
    public synchronized void printBuddyList() {
        if (buddyGroups == null) {
            return;
        }

        for (Iterator i = buddyGroups.iterator(); i.hasNext();) {
            BuddyGroup buddyGroup = (BuddyGroup) i.next();
            System.out.println(buddyGroup.getName());

            for (Iterator i2 = buddyGroup.iterator();
                 i2.hasNext();) {
                Buddy buddy = (Buddy) i2.next();
                System.out.println("  " + buddy.getName());
            }
        }
    }

    /**
     * Find a group in the buddy list.
     *
     * @param group the group
     * @return the position
     */
    private synchronized int findGroupInBuddyList(String group) {
        int j = 0;

        for (Iterator i = buddyGroups.iterator(); i.hasNext();) {
            BuddyGroup buddyGroup = (BuddyGroup) i.next();

            if (AIMUtil.snCmp(buddyGroup.getName(), group)) {
                return j;
            }

            j++;
        }

        return -1;
    }

    /**
     * Find a buddy in the buddy list.
     *
     * @param name the buddy name
     * @param group the group
     * @return the position
     */
    private synchronized int findBuddyInBuddyList(String name, String group) {
        int j = findGroupInBuddyList(group);

        if (j == -1) {
            return -1;
        }

        BuddyGroup buddyGroup = (BuddyGroup) buddyGroups.get(j);

        j = 0;

        for (Iterator i = buddyGroup.iterator(); i.hasNext();) {
            Buddy buddy = (Buddy) i.next();

            if (AIMUtil.snCmp(buddy.getName(), name)) {
                return j;
            }

            j++;
        }

        return -1;
    }

    /**
     * Find a Buddy by name.
     *
     * @param sn the screenname of the buddy
     * @return the buddy
     */
    public synchronized Buddy findBuddy(String sn) {
        int size = buddyGroups.size();

        for (int i = 0; i < size; i++) {
            BuddyGroup g = (BuddyGroup) buddyGroups.get(i);
            Buddy b = g.findBuddy(sn);

            if (b != null) {
                return b;
            }
        }

        return null;
    }

    /**
     * Find a BuddyGroup by a buddy.
     *
     * @param sn the screenname of the buddy
     * @return the group
     */
    public synchronized BuddyGroup findBuddyGroupByBuddy(String sn) {
        int size = buddyGroups.size();

        for (int i = 0; i < size; i++) {
            BuddyGroup g = (BuddyGroup) buddyGroups.get(i);

            if (g.findBuddy(sn) != null) {
                return g;
            }
        }

        return null;
    }

    /**
     * Describe <code>getSN</code> method here.
     *
     * @return a <code>String</code> value
     */
    public String getSN() {
        return sn;
    }

    /**
     * Describe <code>setSN</code> method here.
     *
     * @param sn a <code>String</code> value
     */
    public void setSN(String sn) {
        this.sn = sn;
    }

    /**
     * Describe <code>setPassword</code> method here.
     *
     * @param password a <code>String</code> value
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Describe <code>getPassword</code> method here.
     *
     * @return a <code>String</code> value
     */
    public String getPassword() {
        return password;
    }

    /**
     * Print user type as text.
     *
     * @param type the user type
     * @return the string
     */
    private String typeToText(int type) {
        if ((type & UC_AB) != 0) {
            return "ActiveBuddy!";
        } else if ((type & UC_AOL) != 0) {
            return "AOL";
        } else if ((type & UC_ADMIN) != 0) {
            return "Administrator";
        } else if ((type & UC_UNCONFIRMED) != 0) {
            return "Unconfirmed Internet";
        } else if ((type & UC_NORMAL) != 0) {
            return "Internet";
        } else if ((type & UC_WIRELESS) != 0) {
            return "Mobile Device User";
        } else if ((type & UC_UNAVAILABLE) != 0) {
            return "Unavailable";
        } else if ((type & UC_HIPTOP) != 0) {
            return "Hiptop";
        }

        return null;
    }

    /**
     * Print user capabilities as text.
     *
     * @param caps the capabilities
     * @return the string
     */
    public String capsToText(int caps) {
        StringBuffer sb = new StringBuffer();

        if ((caps & AIMConstants.AIM_CAPS_ABINTERNAL) != 0) {
            sb.append("Active Buddy, ");
        }

        if ((caps & AIMConstants.AIM_CAPS_GAMES) != 0
            || (caps & AIMConstants.AIM_CAPS_GAMES2) != 0
            || (caps & AIMConstants.AIM_CAPS_SAVESTOCKS) != 0) {
            sb.append("Add-Ins, ");
        }

        if ((caps & AIMConstants.AIM_CAPS_BUDDYICON) != 0) {
            sb.append("Buddy Icon, ");
        }

        if ((caps & AIMConstants.AIM_CAPS_CHAT) != 0) {
            sb.append("Chat, ");
        }

        if ((caps & AIMConstants.AIM_CAPS_IMIMAGE) != 0) {
            sb.append("Direct IM, ");
        }

        if ((caps & AIMConstants.AIM_CAPS_EMPTY) != 0) {
            sb.append("Empty, ");
        }

        if ((caps & AIMConstants.AIM_CAPS_GETFILE) != 0) {
            sb.append("File Sharing, ");
        }

        if ((caps & AIMConstants.AIM_CAPS_SENDFILE) != 0) {
            sb.append("File Transfer, ");
        }

        if ((caps & AIMConstants.AIM_CAPS_INTEROPERATE) != 0) {
            sb.append("Interoperate, ");
        }

        if ((caps & AIMConstants.AIM_CAPS_TRILLIANCRYPT) != 0) {
            sb.append("SecureIM, ");
        }

        if ((caps & AIMConstants.AIM_CAPS_SECUREIM) != 0) {
            sb.append("Security Enabled, ");
        }

        if ((caps & AIMConstants.AIM_CAPS_SENDBUDDYLIST) != 0) {
            sb.append("Send Buddy List, ");
        }

        if ((caps & AIMConstants.AIM_CAPS_VOICE) != 0) {
            sb.append("Talk, ");
        }

        if ((caps & AIMConstants.AIM_CAPS_ICQ) != 0) {
            sb.append("ICQ, ");
        }

        if ((caps & AIMConstants.AIM_CAPS_ICQRTF) != 0) {
            sb.append("ICQ RTF, ");
        }

        if ((caps & AIMConstants.AIM_CAPS_ICQSERVERRELAY) != 0) {
            sb.append("ICQ Server Relay, ");
        }

        if ((caps & AIMConstants.AIM_CAPS_ICQUNKNOWN) != 0) {
            sb.append("ICQ Unknown, ");
        }

        if ((caps & AIMConstants.AIM_CAPS_ICQUTF8) != 0) {
            sb.append("ICQ UTF-8, ");
        }

        String s = sb.toString();

        if (s != null && s.length() >= 2) {
            s = s.substring(0, s.length() - 2);
        }

        return s;
    }

    /**
     * Describe <code>sendIM</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param name a <code>String</code> value
     * @param message a <code>String</code> value
     * @param imflags an <code>int</code> value
     * @exception IOException if an error occurs
     */
    public void sendIM(AIMSession sess, String name, String message,
                       int imflags) throws IOException {
        AOLIM args = new AOLIM();

        args.setFlags(args.getFlags() | AIMConstants.AIM_IMFLAGS_ACK);

        if (sess.isICQ()) {
            args.setFlags(args.getFlags()
                          | AIMConstants.AIM_IMFLAGS_OFFLINE);
        } else {
            if ((imflags & AIMConstants.AIM_IMFLAGS_AWAY) != 0) {
                args.setFlags(args.getFlags()
                              | AIMConstants.AIM_IMFLAGS_AWAY);
            }
        }

        args.setDestSN(name);

        if ((args.getFlags() & AIMConstants.AIM_IMFLAGS_UNICODE) != 0) {
            args.setCharset(AIMConstants.AIM_CHARSET_UTF_16BE);
            args.setCharSubset(AIMConstants.AIM_CHARSUBSET_NONE);
        } else if ((args.getFlags() & AIMConstants.AIM_IMFLAGS_ISO_8859_1)
                   != 0) {
            args.setCharset(AIMConstants.AIM_CHARSET_ISO_8859_1);
            args.setCharSubset(AIMConstants.AIM_CHARSUBSET_NONE);
        } else {
            args.setCharset(AIMConstants.AIM_CHARSET_US_ASCII);
            args.setCharSubset(AIMConstants.AIM_CHARSUBSET_NONE);
        }

        args.setMsg(message);

        ICBMHandler im = (ICBMHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_MSG);

        im.sendCH1Ext(sess, args);
    }

    /**
     * Describe <code>sendFile</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param sn a <code>String</code> value
     * @param file a <code>String</code> value
     */
    public void sendFile(AIMSession sess, String sn, String file) {
        try {
            byte[] ck = new byte[8];
            MsgCookie cookie = new MsgCookie(bosconn, ck,
                                             AIMConstants.
                                             AIM_COOKIETYPE_OFTSEND, null);
            FT ft = new FT(sess, ck);
            ft.sendFile(sn, file, FTTLV.SUBTYPE_SEND_FILE, null);
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE, "IOException", ioe);
        }
    }

    /**
     * Describe <code>getFile</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param sn a <code>String</code> value
     */
    public void getFile(AIMSession sess, String sn) {
        try {
            MsgCookie cookie = new MsgCookie(bosconn, null,
                                             AIMConstants.
                                             AIM_COOKIETYPE_OFTSEND, null);
            FT ft = new FT(sess, cookie.getCookie());
            ft.sendFile(sn, null, FTTLV.SUBTYPE_GET_LIST, null);
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE,"IOException", ioe);
        }
    }

    /**
     * Describe <code>trillianEncryption</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param sn a <code>String</code> value
     * @param msg a <code>String</code> value
     */
    public void trillianEncryption(AIMSession sess, String sn, String msg) {
        if (msg != null) {
            TrillianSecureIM tsi
                = (TrillianSecureIM)
                   trillianSessions.get(AIMUtil.normalize(sn));

            if (tsi != null) {
                LOG.fine("Session found, sending message...");
                tsi.sendMsg(msg);
            } else {
                LOG.fine("No previous session found. Please start one first");
            }

            return;
        }

        try {
            LOG.fine("Creating Trillian SecureIM session with " + sn + "...");

            TrillianSecureIM tsi = new TrillianSecureIM(sess, sn);

            LOG.fine("Requesting that " + sn + " join session...");

            tsi.sendRequest();

            trillianSessions.put(AIMUtil.normalize(sn), tsi);
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE,"IOException", ioe);
        }
    }

    /**
     * Describe <code>join</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param chat a <code>String</code> value
     */
    public void join(AIMSession sess, String chat) {
        try {
            ChatNavHandler chatNav = (ChatNavHandler) sess.
                getHandler(SNACFamily.AIM_CB_FAM_CTN);
            /* XXX: Set exchange? Gaim always uses 4. */
            chatNav.createRoom(sess, bosconn, chat, 4);
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE,"IOException", ioe);
        }
    }

    /**
     * Describe <code>invite</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param sn a <code>String</code> value
     * @param name a <code>String</code> value
     */
    public void invite(AIMSession sess, String sn, String name) {
        try {
            ChatRendezvous rv = new ChatRendezvous();

            Chat chat = new Chat();

            chat.setName(name);
            chat.setExchange(4);
            chat.setInstance(0);

            rv.setChat(chat);
            rv.setMsg("Join " + name + " now!");
            rv.setRequest();

            ICBMHandler im = (ICBMHandler) sess.
                getHandler(SNACFamily.AIM_CB_FAM_MSG);

            im.sendRendezvous(sess, sn, rv);
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE,"IOException", ioe);
        }
    }

    /**
     * Describe <code>getInfo</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param sn a <code>String</code> value
     * @exception IOException if an error occurs
     */
    public void getInfo(AIMSession sess, String sn) throws IOException {
        LocateHandler loc = (LocateHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_LOC);

        if (sess.isICQ()) {
            return;
        }


        loc.getInfo(sess, bosconn, sn, AIMConstants.AIM_GETINFO_AWAYMESSAGE);
    }

    /**
     * Describe <code>getAway</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param sn a <code>String</code> value
     * @exception IOException if an error occurs
     */
    public void getAway(AIMSession sess, String sn) throws IOException {
        LocateHandler loc = (LocateHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_LOC);

        if (sess.isICQ()) {
            return;
        }


        loc.getInfo(sess, bosconn, sn, AIMConstants.AIM_GETINFO_GENERALINFO);
    }

    /**
     * Describe <code>setInfo</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param encoding a <code>String</code> value
     * @param text a <code>String</code> value
     * @exception IOException if an error occurs
     */
    public void setInfo(AIMSession sess, String encoding, String text)
        throws IOException {
        LocateHandler loc = (LocateHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_LOC);

        if (sess.isICQ()) {
            loc.setProfile(sess, bosconn, null, null, null, null, ICQ_CAPS,
                           null);
            return;
        }

        loc.setProfile(sess, bosconn, encoding, text, null, null, aimCaps,
                       null);
    }

    /**
     * Describe <code>setAwayAIM</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param encoding a <code>String</code> value
     * @param awaymb a <code>byte[]</code> value
     * @exception IOException if an error occurs
     */
    public void setAwayAIM(AIMSession sess, String encoding, byte[] awaymb)
        throws IOException {
        LocateHandler loc = (LocateHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_LOC);

        String awaymsg = new String(awaymb,
                                    AIMUtil.charsetAOLToJava(encoding));

        loc.setProfile(sess, bosconn, encoding, null, encoding, awaymsg,
                       aimCaps, null);
    }

    /**
     * Describe <code>setAwayAIM</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param awaymsg a <code>String</code> value
     * @exception IOException if an error occurs
     */
    public void setAwayAIM(AIMSession sess, String awaymsg)
        throws IOException {
        setAwayAIM(sess, "us-ascii", awaymsg.getBytes("US-ASCII"));
    }

    /**
     * Describe <code>setAwayICQ</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param awaymsg a <code>String</code> value
     * @exception IOException if an error occurs
     */
    public void setAwayICQ(AIMSession sess, String awaymsg)
        throws IOException {
        ServiceHandler service = (ServiceHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_GEN);

        if (awaymsg.equals("Online")) {
            service.setExtStatus(sess, AIMConstants.AIM_ICQ_STATE_NORMAL);
        } else if (awaymsg.equals("Away")) {
            service.setExtStatus(sess, AIMConstants.AIM_ICQ_STATE_AWAY);
        }  else if (awaymsg.equals("Do Not Disturb")) {
            service.setExtStatus(sess, AIMConstants.AIM_ICQ_STATE_AWAY
                                       | AIMConstants.AIM_ICQ_STATE_DND
                                       | AIMConstants.AIM_ICQ_STATE_OCC);
        } else if (awaymsg.equals("Not Available")) {
            service.setExtStatus(sess, AIMConstants.AIM_ICQ_STATE_AWAY
                                       | AIMConstants.AIM_ICQ_STATE_NA);
        } else if (awaymsg.equals("Occupied")) {
            service.setExtStatus(sess, AIMConstants.AIM_ICQ_STATE_AWAY
                                       | AIMConstants.AIM_ICQ_STATE_OCC);
        } else if (awaymsg.equals("Free For Chat")) {
            service.setExtStatus(sess, AIMConstants.AIM_ICQ_STATE_FFC);
        } else if (awaymsg.equals("Invisible")) {
            service.setExtStatus(sess, AIMConstants.AIM_ICQ_STATE_INVISIBLE);
        }
    }

    /**
     * Print ICQ status as text.
     *
     * @param state the user state
     * @return the string
     */
    private static String icqStatus(int state) {
        if ((state & AIMConstants.AIM_ICQ_STATE_FFC) != 0) {
            return "Free For Chat";
        } else if ((state & AIMConstants.AIM_ICQ_STATE_DND) != 0) {
            return "Do Not Disturb";
        } else if ((state & AIMConstants.AIM_ICQ_STATE_NA) != 0) {
            return "Not Available";
        } else if ((state & AIMConstants.AIM_ICQ_STATE_OCC) != 0) {
            return "Occupied";
        } else if ((state & AIMConstants.AIM_ICQ_STATE_AWAY) != 0) {
            return "Away";
        } else if ((state & AIMConstants.AIM_ICQ_STATE_WEBAWARE) != 0) {
            return "Web Aware";
        } else if ((state & AIMConstants.AIM_ICQ_STATE_INVISIBLE) != 0) {
            return "Invisible";
        } else {
            return "Online";
        }
    }

    /**
     * Describe <code>addBuddy</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param name a <code>String</code> value
     * @param group a <code>String</code> value
     * @exception IOException if an error occurs
     */
    public void addBuddy(AIMSession sess, String name, String group)
        throws IOException {
        SSIHandler ssi = (SSIHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_SSI);

        if (sess.getSSI().getReceivedData()
            && ssi.itemListExists(sess.getSSI().getLocal(), name) == null) {
            ssi.addBuddy(sess, name, group, null, null, null, false);
        }

        int i = findGroupInBuddyList(group);
                Buddy b;

        if (i != -1) {
            b = ((BuddyGroup) buddyGroups.get(i)).addBuddy(name);
        } else {
            buddyGroups.add(new BuddyGroup(group));
            b = ((BuddyGroup) buddyGroups.get(buddyGroups.size() - 1)).
                addBuddy(name);
        }
                b.setProperty(Buddy.SESSION, sess);
    }

    /**
     * Describe <code>moveBuddy</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param name a <code>String</code> value
     * @param oldGroup a <code>String</code> value
     * @param newGroup a <code>String</code> value
     * @exception IOException if an error occurs
     */
    public void moveBuddy(AIMSession sess, String name, String oldGroup,
                          String newGroup) throws IOException {
        int f = findBuddyInBuddyList(name, oldGroup);

        if (f == -1) {
            return;
        }

        if (sess.getSSI().getReceivedData()) {
            SSIHandler ssi = (SSIHandler) sess.getHandler(SNACFamily.
                                                          AIM_CB_FAM_SSI);
            ssi.moveBuddy(sess, oldGroup, newGroup, name);
        }

        int i = findGroupInBuddyList(oldGroup);

        if (i != -1) {
            int j = findBuddyInBuddyList(name, oldGroup);

            if (j != -1) {
                ((BuddyGroup) buddyGroups.get(i)).remove(j);
            }
        }
    }

    /**
     * Describe <code>removeBuddy</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param name a <code>String</code> value
     * @param group a <code>String</code> value
     * @exception IOException if an error occurs
     */
    public void removeBuddy(AIMSession sess, String name,
                            String group) throws IOException {
        int j = findBuddyInBuddyList(name, group);

        if (j == -1) {
            LOG.fine("Tried to remove non-existent buddy=" + name);
            return;
        }

        SSIHandler ssi = (SSIHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_SSI);

        if (sess.getSSI().getReceivedData()) {
            ssi.delBuddy(sess, name, group);
        }
    }

    /**
     * Describe <code>renameGroup</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param oldGroup a <code>String</code> value
     * @param newGroup a <code>String</code> value
     * @exception IOException if an error occurs
     */
    public void renameGroup(AIMSession sess, String oldGroup, String newGroup)
        throws IOException {
        int f = findGroupInBuddyList(oldGroup);

        if (f == -1) {
            return;
        }

        ((BuddyGroup) buddyGroups.get(f)).setName(newGroup);

        SSIHandler ssi = (SSIHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_SSI);

        ssi.renameGroup(sess, oldGroup, newGroup);
    }

    /**
     * Describe <code>login</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param sn a <code>String</code> value
     * @exception java.io.IOException if an error occurs
     */
    public void login(ConnectionInfo connectionInfo, AIMSession sess, String sn) throws IOException {
//        String dest = AIMConstants.LOGIN_SERVER_DEFAULT + ":" + AIMConstants.LOGIN_PORT;
        String dest = connectionInfo.getIp() +":"+connectionInfo.getPort();
        AIMConnection conn = new AIMConnection(sess,
                                               AIMConstants.AIM_CONN_TYPE_AUTH,
                                               dest);

        conn.registerListener(SNACFamily.AIM_CB_FAM_ATH,
                              SNACFamily.AIM_CB_ATH_AUTHRESPONSE, this);
        conn.registerListener(SNACFamily.AIM_CB_FAM_ATH,
                              SNACFamily.AIM_CB_ATH_LOGINRESPONSE, this);

        conn.connect();

        LoginHandler login = (LoginHandler) sess.getHandler(SNACFamily.AIM_CB_FAM_ATH);

        login.requestLogin(sess, conn, sn);
    }

    /**
     * Describe <code>checkMail</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @exception IOException if an error occurs
     */
    public void checkMail(AIMSession sess) throws IOException {
        MailHandler mail = (MailHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_MAL);

        mail.sendCookies(sess);
        mail.activate(sess);
    }

    /**
     * Describe <code>addPermit</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param who a <code>String</code> value
     * @exception IOException if an error occurs
     */
    public void addPermit(AIMSession sess, String who) throws IOException {
        SSIHandler ssi = (SSIHandler) sess.
                         getHandler(SNACFamily.AIM_CB_FAM_SSI);

        if (sess.getSSI().getReceivedData()) {
            ssi.addPermit(sess, who);
        }
    }

    /**
     * Describe <code>addDeny</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param who a <code>String</code> value
     * @exception IOException if an error occurs
     */
    public void addDeny(AIMSession sess, String who) throws IOException {
        SSIHandler ssi = (SSIHandler) sess.
                         getHandler(SNACFamily.AIM_CB_FAM_SSI);

        if (sess.getSSI().getReceivedData()) {
            ssi.addDeny(sess, who);
        }
    }

    /**
     * Describe <code>remPermit</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param who a <code>String</code> value
     * @exception IOException if an error occurs
     */
    public void remPermit(AIMSession sess, String who) throws IOException {
        SSIHandler ssi = (SSIHandler) sess.
                         getHandler(SNACFamily.AIM_CB_FAM_SSI);

        if (sess.getSSI().getReceivedData()) {
            ssi.delPermit(sess, who);
        }
    }

    /**
     * Describe <code>remDeny</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param who a <code>String</code> value
     * @exception IOException if an error occurs
     */
    public void remDeny(AIMSession sess, String who) throws IOException {
        SSIHandler ssi = (SSIHandler) sess.
                         getHandler(SNACFamily.AIM_CB_FAM_SSI);

        if (sess.getSSI().getReceivedData()) {
            ssi.delDeny(sess, who);
        }
    }

    /**
     * Describe <code>getEmail</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @exception IOException if an error occurs
     */
    public void getEmail(AIMSession sess) throws IOException {
        AIMConnection conn = AIMConnection.findByType(sess,
                                                      AIMConstants.
                                                      AIM_CONN_TYPE_AUTH);

        if (conn == null) {
            getEmail = true;
            ServiceHandler service = (ServiceHandler) sess.
                getHandler(SNACFamily.AIM_CB_FAM_GEN);

            service.reqService(sess, bosconn,
                               AIMConstants.AIM_CONN_TYPE_AUTH);
        } else {
            AdminHandler admin = (AdminHandler) sess.
                getHandler(SNACFamily.AIM_CB_FAM_ADM);
            admin.getInfo(sess, conn, 0x0011);
        }
    }

    /**
     * Describe <code>confirmAccount</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @exception IOException if an error occurs
     */
    public void confirmAccount(AIMSession sess) throws IOException {
        AIMConnection conn = AIMConnection.findByType(sess,
                                                      AIMConstants.
                                                      AIM_CONN_TYPE_AUTH);

        if (conn == null) {
            confirmAccount = true;
            ServiceHandler service = (ServiceHandler) sess.
                getHandler(SNACFamily.AIM_CB_FAM_GEN);

            service.reqService(sess, bosconn,
                               AIMConstants.AIM_CONN_TYPE_AUTH);
        } else {
            AdminHandler admin = (AdminHandler) sess.
                getHandler(SNACFamily.AIM_CB_FAM_ADM);
            admin.reqConfirm(sess, conn);
        }
    }

    /**
     * Describe <code>changeEmail</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param eml a <code>String</code> value
     * @exception IOException if an error occurs
     */
    public void changeEmail(AIMSession sess, String eml) throws IOException {
        if (AIMConnection.findByType(sess,
                                         AIMConstants.AIM_CONN_TYPE_AUTH)
                != null) {
            setEmail = true;
            email = eml;
            ServiceHandler service = (ServiceHandler) sess.
                getHandler(SNACFamily.AIM_CB_FAM_GEN);

            service.reqService(sess, bosconn, AIMConstants.AIM_CONN_TYPE_AUTH);
        } else {
            AdminHandler admin = (AdminHandler) sess.
                getHandler(SNACFamily.AIM_CB_FAM_ADM);

            admin.setEmail(sess, AIMConnection.findByType(sess,
                                                          AIMConstants.
                                                          AIM_CONN_TYPE_AUTH),
                                                          email);
        }
    }

    /**
     * Describe <code>formatScreenname</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param sn a <code>String</code> value
     * @exception IOException if an error occurs
     */
    public void formatScreenname(AIMSession sess, String sn)
        throws IOException {
        if (!AIMUtil.snCmp(nick, sess.getSN())) {
            if (AIMConnection.findByType(sess,
                                         AIMConstants.AIM_CONN_TYPE_AUTH)
                != null) {
                formatScreenname = true;
                nick = sn;
                ServiceHandler service = (ServiceHandler) sess.
                    getHandler(SNACFamily.AIM_CB_FAM_GEN);

                service.reqService(sess, bosconn,
                                   AIMConstants.AIM_CONN_TYPE_AUTH);
            } else {
                AdminHandler admin = (AdminHandler) sess.
                    getHandler(SNACFamily.AIM_CB_FAM_ADM);

                admin.setNick(sess, AIMConnection.
                              findByType(sess, AIMConstants.
                                         AIM_CONN_TYPE_AUTH),
                              nick);
            }

        }
    }

    /**
     * Describe <code>changePassword</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param oldp a <code>String</code> value
     * @param newp a <code>String</code> value
     * @exception IOException if an error occurs
     */
    public void changePassword(AIMSession sess, String oldp, String newp)
        throws IOException {
        if (AIMConnection.findByType(sess, AIMConstants.AIM_CONN_TYPE_AUTH)
            != null) {
            changePassword = true;
            oldPassword = oldp;
            newPassword = newp;
            ServiceHandler service = (ServiceHandler) sess.
                getHandler(SNACFamily.AIM_CB_FAM_GEN);

            service.reqService(sess, bosconn,
                               AIMConstants.AIM_CONN_TYPE_AUTH);
        } else {
            AdminHandler admin = (AdminHandler) sess.
                getHandler(SNACFamily.AIM_CB_FAM_ADM);

            admin.changePassword(sess,
                                 AIMConnection.findByType(sess,
                                                          AIMConstants.
                                                          AIM_CONN_TYPE_AUTH),
                                 newp,
                                 oldp);
        }
    }

    /**
     * Describe <code>convoClosed</code> method here.
     *
     * @param dims an <code>ArrayList</code> value
     * @param who a <code>DirectIM</code> value
     */
    public void convoClosed(ArrayList dims, DirectIM who) {
        if (!dims.contains(who)) {
            return;
        }

        dims.remove(who);
        who.end();
    }

   /**
     * Describe <code>infoChange</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param infoChange a <code>boolean</code> value
     * @param perms a <code>short</code> value
     * @param err a <code>short</code> value
     * @param url a <code>String</code> value
     * @param sn a <code>String</code> value
     * @param email a <code>String</code> value
     */
    public void infoChange(AIMSession sess, AIMFrame frame, boolean infoChange,
                           short perms, short err, String url, String sn,
                           String email) {

    }

    /**
     * Describe <code>accountConfirm</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param status an <code>int</code> value
     */
    public void accountConfirm(AIMSession sess, AIMFrame frame, int status) {

    }

    /**
     * Describe <code>godDamnICQ</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param unused a <code>String</code> value
     */
    public void godDamnICQ(AIMSession sess, AIMFrame frame, String unused) {
        try {
            LoginHandler login = (LoginHandler) sess.
                getHandler(SNACFamily.AIM_CB_FAM_ATH);

            login.sendLogin(sess, frame.getConn(), sn, password, null, null,
                            null);
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE,"IOException", ioe);
        }
    }

    /**
     * Describe <code>parse</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param info a <code>LoginResponseInfo</code> value
     * @param family an <code>int</code> value
     * @param subtype an <code>int</code> value
     */
    public void parse(AIMSession sess, AIMFrame frame, LoginResponseInfo info,
                      int family, int subtype) {
        sess.setLoginInfo(info);

        try {
            if (info.getBosip() == null) {
                LOG.severe("Error in signon code=" + info.getErrorCode()
                          + " url=" + info.getErrorURL());
                String errorMsg = null;

                switch (info.getErrorCode()) {
                case 0x01:
                case 0x05:
                    errorMsg = "Incorrect nickname or password.";
                    break;
                case 0x11:
                    errorMsg = "Your account is currently suspended.";
                    break;
                case 0x14:
                    errorMsg = "The AOL Instant Messenger service is "
                        + "temporarily unavailable.";
                    break;
                case 0x18:
                    errorMsg = "You have been connecting and disconnecting too "
                        + "frequently. If you continue to try, you will need "
                        + "to wait even longer.";
                    break;
                case 0x1c:
                    errorMsg = "The client version you are using is too old. "
                        + "Please upgrade.";
                    break;
                default:
                    errorMsg = "Unknown.";
                }

                LOG.severe(errorMsg);
                frame.getConn().close();
                callbacks.notifyConnectionFailed(errorMsg);
                return;
            }

            LOG.fine("Closing authorizer connection...");
            frame.getConn().close();

            LOG.fine("Creating BOS connection...");
            bosconn = new AIMConnection(sess, AIMConstants.AIM_CONN_TYPE_BOS,
                                        info.getBosip());

            bosconn.registerListener(SNACFamily.AIM_CB_FAM_SPL,
                                     SNACFamily.AIM_CB_SPL_ICBM_REMOVE_COOKIE,
                                     this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_SPL,
                                     SNACFamily.
                                     AIM_CB_SPL_SNAC_REQUEST_TIMED_OUT, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_SPL,
                                     SNACFamily.AIM_CB_SPL_UNHANDLED_FRAME,
                                     this);

            bosconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                     SNACFamily.AIM_CB_GEN_RATEINFO, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_BOS,
                                     SNACFamily.AIM_CB_BOS_RIGHTS, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                     SNACFamily.AIM_CB_GEN_REDIRECT, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_LOC,
                                     SNACFamily.AIM_CB_LOC_RIGHTS, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_BUD,
                                     SNACFamily.AIM_CB_BUD_RIGHTS, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_BUD,
                                     SNACFamily.AIM_CB_BUD_ONCOMING, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_BUD,
                                     SNACFamily.AIM_CB_BUD_OFFGOING, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_MSG,
                                     SNACFamily.AIM_CB_MSG_INCOMING, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_LOC,
                                     SNACFamily.AIM_CB_LOC_ERROR, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_MSG,
                                     SNACFamily.AIM_CB_MSG_MISSEDCALL, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_MSG,
                                     SNACFamily.AIM_CB_MSG_CLIENTAUTORESP,
                                     this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                     SNACFamily.AIM_CB_GEN_RATECHANGE, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                     SNACFamily.AIM_CB_GEN_EVIL, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_LOK,
                                     SNACFamily.AIM_CB_LOK_ERROR, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_MSG,
                                     SNACFamily.AIM_CB_MSG_ERROR, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                     SNACFamily.AIM_CB_GEN_ERROR, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_BUD,
                                     SNACFamily.AIM_CB_BUD_ERROR, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_BOS,
                                     SNACFamily.AIM_CB_BOS_ERROR, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_LOK,
                                     SNACFamily.AIM_CB_LOK_SEARCH_REPLY, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_LOC,
                                     SNACFamily.AIM_CB_LOC_USERINFO, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_MSG,
                                     SNACFamily.AIM_CB_MSG_ACK, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                     SNACFamily.AIM_CB_GEN_MOTD, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_MSG,
                                     SNACFamily.AIM_CB_MSG_PARAMINFO, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                     SNACFamily.AIM_CB_GEN_MEMREQUEST, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                     SNACFamily.AIM_CB_GEN_SELFINFO, this);
            /* TODO: Add handler for 0x0001/0x0021 */
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_ICQ,
                                     SNACFamily.AIM_CB_ICQ_REPLY, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_POP,
                                     SNACFamily.AIM_CB_POP_DISPLAY, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_SSI,
                                     SNACFamily.AIM_CB_SSI_RIGHTSINFO, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_SSI,
                                     SNACFamily.AIM_CB_SSI_LIST, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_SSI,
                                     SNACFamily.AIM_CB_SSI_NOLIST, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_MAL,
                                     SNACFamily.AIM_CB_MAL_REPLY, this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_STS,
                                     SNACFamily.AIM_CB_STS_SETREPORTINTERVAL,
                                     this);
            bosconn.registerListener(SNACFamily.AIM_CB_FAM_STS,
                                     SNACFamily.AIM_CB_STS_REPORTACK, this);

            bosconn.registerListener(SNACFamily.AIM_CB_FAM_MSG,
                                     SNACFamily.AIM_CB_MSG_TYPING, this);

            bosconn.getTransmitQueue().
                setMode(AIMConstants.AIM_TX_QUEUE_INTERNAL_RATE_THROTTLE);

            bosconn.connect();

            LoginHandler login = (LoginHandler) sess.getHandler(SNACFamily.AIM_CB_FAM_ATH);

            login.sendCookie(sess, bosconn, info.getCookie());
            callbacks.notifyConnectionEstablished();
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE, "IOException", ioe);
            callbacks.notifyErrorOccured("Error during login", ioe);
        }
    }

    /**
     * Describe <code>keyParse</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param keystr a <code>String</code> value
     */
    public void keyParse(AIMSession sess, AIMFrame frame, String keystr) {
        ClientInfo ci;

        if (!sess.isIChat()) {
            ci = new AIMClientInfo();
        } else {
            ci = new IChatClientInfo();
        }

        try {
            LoginHandler login = (LoginHandler) sess.getHandler(SNACFamily.AIM_CB_FAM_ATH);

            login.sendLogin(sess, frame.getConn(), sn, password, ci, keystr, AIMConstants.AIM_MD5_STRING);
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE,"IOException", ioe);
            callbacks.notifyErrorOccured("Error during login", ioe);
        }
    }

    /**
     * Describe <code>newUIN</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param uin an <code>int</code> value
     * @param ip an <code>int</code> value
     * @param port an <code>int</code> value
     */
    public void newUIN(AIMSession sess, AIMFrame frame, int uin, int ip,
                       int port) {
        LOG.fine("Got new UIN=" + uin);
    }

    /**
     * Describe <code>securID</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     */
    public void securID(AIMSession sess, AIMFrame frame) {
        LOG.fine("Got SecurID request");
    }

    /**
     * Describe <code>bosRights</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param maxpermits an <code>int</code> value
     * @param maxdenies an <code>int</code> value
     */
    public void bosRights(AIMSession sess, AIMFrame frame, int maxpermits,
                          int maxdenies) {
        try {
            LOG.fine("BOS rights: Max permit = " + maxpermits
                      + " / Max deny = " + maxdenies);


            ServiceHandler service = (ServiceHandler) sess.
                getHandler(SNACFamily.AIM_CB_FAM_GEN);

            if (sess.isICQ()) {
                service.setExtStatus(sess, AIMConstants.AIM_ICQ_STATE_NORMAL);
            }

            if (!sess.isICQ()) {
                service.reqService(sess, frame.getConn(),
                                   SNACFamily.AIM_CB_FAM_CTN);
                service.reqService(sess, frame.getConn(),
                                   SNACFamily.AIM_CB_FAM_MAL);
                service.reqService(sess, frame.getConn(),
                                   SNACFamily.AIM_CB_FAM_ICO);
                service.reqService(sess, frame.getConn(),
                                   SNACFamily.AIM_CB_FAM_ODR);
            }

            service.clientReady(sess, frame.getConn());
            
            throw new RuntimeException("Failed to support this funtion");
//            ScriptInterpreter si = new ScriptInterpreter(sess,
//                                                         frame.getConn(),
//                                                         this);
//            sess.setScriptInterpreter(si);
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE,"IOException", ioe);
        }
    }

    /**
     * Describe <code>buddyChange</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param info an <code>UserInfo</code> value
     */
    public void buddyChange(AIMSession sess, AIMFrame frame, UserInfo info) {
        SNAC snac = frame.getSNAC();
        Buddy b = null;

        for (int i = 0; i < buddyGroups.size(); i++) {
            b = ((BuddyGroup) buddyGroups.get(i)).findBuddy(info.getSN());
            if (b != null) {
                break;
            }
        }

        String state = "Invalid";

        if (snac.getFamily() == 0x0003) {
            if (frame.getSNAC().getSubtype()
                == SNACFamily.AIM_CB_BUD_ONCOMING) {
                LOG.fine(info.getSN() + " signed on");
                state = "Online";
            } else if (frame.getSNAC().getSubtype()
                       == SNACFamily.AIM_CB_BUD_OFFGOING) {
                LOG.fine(info.getSN() + " signed off");
                state = "Offline";

                /* Remove any existing Trillian SecureIM session. */
                TrillianSecureIM tsi
                    = (TrillianSecureIM)
                      trillianSessions.
                      remove(AIMUtil.normalize(info.getSN()));

                if (tsi != null) {
                    LOG.fine("Removed Trillian SecureIM session with "
                              + info.getSN());
                }

                if (b != null) {
                    b.setProperty(Buddy.STATE, state);
                    LOG.fine("[3] " + info.getSN() + "'s state set to "
                              + state);
                }

                return;
            }
        }

        int caps = 0;

        if ((info.getPresent()
             & AIMConstants.AIM_USERINFO_PRESENT_CAPS) != 0) {
            caps = info.getCapabilities();
        }

        if ((info.getPresent()
             & AIMConstants.AIM_USERINFO_PRESENT_CAPS_AOL) != 0) {
            caps |= info.getCapabilities2();
        }

        if ((info.getPresent()
             & AIMConstants.AIM_USERINFO_PRESENT_CAPS_SHORT) != 0) {
            caps |= info.getCapabilities3();
        }

        int type = 0;

        if ((info.getCapabilities() & AIMConstants.AIM_CAPS_HIPTOP) != 0) {
            type |= UC_HIPTOP;
        }

        String userclass = "";

        if (!sess.isICQ()
            && (info.getPresent() & AIMConstants.AIM_USERINFO_PRESENT_FLAGS)
            != 0) {
            if ((info.getFlags() & AIMConstants.AIM_FLAG_UNCONFIRMED) != 0) {
                type |= UC_UNCONFIRMED;
                userclass = "trial";
            }

            if ((info.getFlags() & AIMConstants.AIM_FLAG_ADMINISTRATOR) != 0) {
                type |= UC_ADMIN;
                userclass = "admin";
            }

            if ((info.getFlags() & AIMConstants.AIM_FLAG_AOL) != 0) {
                type |= UC_AOL;
                userclass = "aol";
            }

            if ((info.getFlags() & AIMConstants.AIM_FLAG_FREE) != 0) {
                type |= UC_NORMAL;
                userclass = "normal";
            }

            if ((info.getFlags() & AIMConstants.AIM_FLAG_AWAY) != 0) {
                type |= UC_UNAVAILABLE;
                state = "Away";
            } else if (info.getAvailableMsg() != null) {
                LOG.fine(info.getSN() + " is available: "
                          + info.getAvailableMsg());
                state = info.getAvailableMsg();
            } else {
                LOG.warning(info.getSN() + " is not away, and has no available"
                         + " message");
            }

            if ((info.getFlags() & AIMConstants.AIM_FLAG_ACTIVEBUDDY) != 0) {
                type |= UC_AB;
                userclass = "bot";
            }

            if ((info.getFlags() & AIMConstants.AIM_FLAG_WIRELESS) != 0) {
                type |= UC_WIRELESS;
                userclass = "wireless";
            }

            if ((info.getCapabilities() & AIMConstants.AIM_CAPS_HIPTOP) != 0) {
                type |= UC_HIPTOP;
                userclass = "hiptop";
            }
        }

        if ((info.getPresent()
             & AIMConstants.AIM_USERINFO_PRESENT_ICQEXTSTATUS) != 0) {
            type = info.getIcqInfo().getStatus();

            if ((info.getIcqInfo().getStatus()
                 & AIMConstants.AIM_ICQ_STATE_FFC) == 0
                && info.getIcqInfo().getStatus()
                != AIMConstants.AIM_ICQ_STATE_NORMAL) {
                type = UC_UNAVAILABLE;
            }
        }

        if ((caps & AIMConstants.AIM_CAPS_ICQ) != 0) {
            caps ^= AIMConstants.AIM_CAPS_ICQ;
        }

        long idleTime = 0;

        if ((info.getPresent()
             & AIMConstants.AIM_USERINFO_PRESENT_IDLE) != 0) {
            /* idle time is in minutes, so convert to milliseconds */
            idleTime = info.getIdleTime() * 60L * 1000L;
        }

        long signon = 0;

        if ((info.getPresent()
             & AIMConstants.AIM_USERINFO_PRESENT_SESSIONLEN) != 0) {
            /* session length is in seconds, so convert to milliseconds */
            signon = info.getSessionLen() * 1000L;
        }

        float warnLevel = info.getWarnLevel();

        long memberSince = info.getMemberSince();

        if (!sess.isICQ() && snac.getFamily() == 0x0003) {
            LOG.fine(info.getSN() + ": warnLevel=" + warnLevel + "%, signon="
                      + AIMUtil.prettyPrintTime(signon)
                      + " ago, idleTime="
                      + AIMUtil.prettyPrintTime(idleTime)
                      + ", user=" + type + "/" + typeToText(type)
                      + ", capabilities=" + capsToText(caps)
                      + (memberSince > 0 ? ", member since="
                      + new Date(memberSince * 1000L) : "")
                      + ", state=" + state);

            if (b != null) {
                if (caps != 0) {
                    b.setProperty(Buddy.CAPABILITIES, new Integer(caps));
                }

                b.setProperty(Buddy.STATE, state);
                LOG.fine("[3] " + info.getSN() + "'s state set to " + state);
                b.setProperty(Buddy.WARN_LEVEL, new Float(warnLevel));
                b.setProperty(Buddy.CLASS, typeToText(type));
                b.setProperty(Buddy.MEMBER_SINCE,
                              new Date(memberSince * 1000L));
                b.setProperty(Buddy.SIGNON_TIME, new Long(signon));
                b.setProperty(Buddy.IDLE_TIME, new Long(idleTime));
            }
        } else if (snac.getFamily() == 0x0003) {
            LOG.fine(info.getSN() + ": warnLevel=" + warnLevel + "%, signon="
                      + signon + " minutes ago, idleTime=" + idleTime
                      + " minutes, user=" + typeToText(type)
                      + ", capabilities=" + capsToText(caps));

            if (b != null) {
                b.setProperty(Buddy.CAPABILITIES, new Integer(caps));
                b.setProperty(Buddy.STATE, state);
                LOG.fine("[3] " + info.getSN() + "'s state set to " + state);
                b.setProperty(Buddy.WARN_LEVEL, new Float(warnLevel));
                b.setProperty(Buddy.CLASS, typeToText(type));
                b.setProperty(Buddy.SIGNON_TIME,
                              new Long(signon * 60L * 1000L));
                b.setProperty(Buddy.IDLE_TIME,
                              new Long(info.getIdleTime() * 60L * 1000L));
            }

            TLV tlv;

            if ((tlv = info.getIconData()) != null) {
                try {
                    LOG.fine("icon data TLV, length=" + tlv.getLength());
                    LOG.fine(AIMUtil.hexdump(tlv.getValue()));

                    AIMInputStream tmp =
                        new
                        AIMInputStream(new
                                       ByteArrayInputStream(tlv.getValue()));

                    while (!tmp.isEmpty()) {
                        short ttype = tmp.readShort();
                        byte flag = tmp.readByte();
                        byte length = tmp.readByte();
                        byte[] data = tmp.readBytes(length);

                        LOG.fine("type=0x" + ttype + ", flag=0x" + flag
                                   + ", length=" + length + ", data="
                                   + (data != null
                                      ? AIMUtil.byteArrayToHexString(data)
                                      : "empty"));

                        if (ttype == 0x0001 && flag == 0x01) {
                            if (checkIcon(info.getSN(), data)) {
                                LOG.fine("[10] icon for " + info.getSN()
                                          + " the same as on disk");
                                return;
                            }

                            String nsn = AIMUtil.normalize(info.getSN());

                            if (icons.get(nsn) != null) {
                                LOG.fine("[10] Pending request for "
                                          + info.getSN());
                                return;
                            } else {
                                icons.put(nsn, data);
                                LOG.fine("[10] " + info.getSN()
                                          + ": getting icon...");
                                getIcons(sess);
                            }
                        } else if (ttype == 0x0002 && flag == 0x04) {
                            AIMInputStream tmp2
                                = new
                                  AIMInputStream(new
                                                 ByteArrayInputStream(data));

                            byte[] availableMsgBytes
                                = tmp2.readBytes(tmp2.readShort());

                            /* Check if we have an encoding. */
                            if (tmp2.readShort() == 0x0001) {
                                LOG.fine("AvailableMsgEncoding short value=0x"
                                          + Integer.
                                            toHexString(tmp2.readShort()));
                                info.setAvailableMsgEncoding(tmp2.
                                                             readStringLL());
                            } else {
                                /*
                                 * No explicit encoding, client should use
                                 * UTF-8.
                                 */
                                info.setAvailableMsgEncoding("utf-8");
                            }

                            /*
                             * Now set the available message with the given
                             * encoding.
                             */
                            String enc = info.getAvailableMsgEncoding();

                            String availableMsg
                                = new String(availableMsgBytes,
                                             AIMUtil.charsetAOLToJava(enc));

                            info.setAvailableMsg(availableMsg);
                        }
                    }
                } catch (IOException ioe) {
                    LOG.log(Level.SEVERE,"IOException", ioe);
                }
            }
        }
    }

    /**
     * Describe <code>buddylistRights</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param maxbuddies an <code>int</code> value
     * @param maxwatchers an <code>int</code> value
     */
    public void buddylistRights(AIMSession sess, AIMFrame frame,
                                int maxbuddies, int maxwatchers) {

    }

    /**
     * Describe <code>outgoingIM</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param channel an <code>int</code> value
     * @param sn a <code>String</code> value
     * @param msg a <code>String</code> value
     * @param icbmflags an <code>int</code> value
     * @param flag1 a <code>short</code> value
     * @param flag2 a <code>short</code> value
     */
    public void outgoingIM(AIMSession sess, AIMFrame frame, int channel,
                           String sn, String msg, int icbmflags, short flag1,
                           short flag2) {

    }

    /**
     * Describe <code>incomingIMCH1</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param channel an <code>int</code> value
     * @param userinfo an <code>UserInfo</code> value
     * @param args an <code>AOLIM</code> value
     */
    public void incomingIMCH1(AIMSession sess, AIMFrame frame, int channel,
                              UserInfo userinfo, AOLIM args) {
        String client = "unknown client";

        if (args.getFeatures() != null) {
            int id = AIMFingerPrintInfo.getFingerPrintId(args.getFeatures());

            switch (id) {
            case AIMConstants.AIM_CLIENTTYPE_MC:
                client = "AOL Mobile Communicator";
                break;
            case AIMConstants.AIM_CLIENTTYPE_WINAIM:
                client = "WinAIM";
                break;
            case AIMConstants.AIM_CLIENTTYPE_WINAIM41:
                client = "WinAIM 4.1";
                break;
            case AIMConstants.AIM_CLIENTTYPE_AOL_TOC:
                client = "AOL, CompuServe, TOC, or iChat";
                break;
            case AIMConstants.AIM_CLIENTTYPE_ICQ:
                client = "ICQ";
                break;
            default:
            }
        }

        LOG.fine(userinfo.getSN() + " (using " + client + "): "
                  + args.getMsg());
    }

    /**
     * @param sn the screenname
     * @param iconcsum the icon checksum
     * @return true if icons are equal, false otherwise
     */
    public boolean checkIcon(String sn, int iconcsum) {
        File file = new File(System.getProperty("user.home")
                             + System.getProperty("file.separator")
                             + AIMUtil.normalize(sn) + ".gif");

        short oursum = 0x00;

        try {
            oursum = BuddyIconRendezvous.calculateChecksum(file);
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE,"IOException", ioe);
        }

        if (oursum == iconcsum) {
            LOG.fine("[10] Icon sums equal, no need to save icon");
            return true;
        } else {
            LOG.fine("[10] Icon sums differ, no need to save icon");
        }

        return false;
    }

    /**
     * @param sn the screenname
     * @param iconcsum the icon checksum
     * @return true if icons are equal, false otherwise
     */
    public boolean checkIcon(String sn, byte[] iconcsum) {
        File file = new File(System.getProperty("user.home")
                             + System.getProperty("file.separator")
                             + AIMUtil.normalize(sn) + ".gif");

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            if (file.exists()) {
                DataInputStream dis
                    = new DataInputStream(new FileInputStream(file));

                byte[] ourdata = new byte[(int) file.length()];

                dis.readFully(ourdata);

                md.update(ourdata);

                byte[] oursum = md.digest();

                if (Arrays.equals(oursum, iconcsum)) {
                    LOG.fine("[10] Icon sums equal");
                    return true;
                } else {
                    LOG.fine("[10] Icon sums differ");
                }
           } else {
               LOG.fine("[10] Icon file doesn't exist");
           }
        } catch (NoSuchAlgorithmException nsae) {
            LOG.log(Level.SEVERE,"NoSuchAlgorithmException", nsae);
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE,"IOException", ioe);
        }

        return false;
    }

    /**
     * Write an icon to a file.
     *
     * @param sn the screenname
     * @param icon the icon data
     */
    private void writeIcon(String sn, byte[] icon) {
        File file = new File(System.getProperty("user.home")
                             + System.getProperty("file.separator")
                             + AIMUtil.normalize(sn) + ".gif");

        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(icon);
            fos.close();
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE,"IOException", ioe);
        }
    }

    /**
     * Describe <code>incomingIMCH2</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param channel an <code>int</code> value
     * @param userinfo an <code>UserInfo</code> value
     * @param args an <code>IncomingIMCH2</code> value
     */
    public void incomingIMCH2(AIMSession sess, AIMFrame frame, int channel,
                              UserInfo userinfo, IncomingIMCH2 args) {
        LOG.fine(userinfo.getSN() + " sent us the message=" + args.getMsg());

        if ((args.getReqClass() & AIMConstants.AIM_CAPS_IMIMAGE) != 0) {
            LOG.fine(sess.getSN() + " received direct im request from "
                      + userinfo.getSN() + " (" + args.getVerifiedIP() + ")");

            LOG.fine(args.getClientIP1() + ":" + args.getClientIP2());

            DirectIM dim = new DirectIM(sess, args.getCookie(),
                                        userinfo.getSN(), args.getVerifiedIP(),
                                        args.getPort(), false);
            dim.setListener(false);
            dim.start();
        } else if ((args.getReqClass() & AIMConstants.AIM_CAPS_CHAT) != 0) {
            Chat chat2 = args.getInfo().getChat().getRoomInfo();

            LOG.fine("Want to join room: "
                      + AIMUtil.extractRoomName(chat2.getName()) + ", "
                      + chat2.getExchange() + ", " + chat2.getInstance());

            ChatHandler chat = (ChatHandler) sess.
                getHandler(SNACFamily.AIM_CB_FAM_CHT);
            AIMConnection chatConn = AIMConnection.
                findByGroup(sess, SNACFamily.AIM_CB_FAM_CTN);

            /* FIXME: Ask user whether or not they wish to join. */
            try {
                LOG.fine("FIXME: we are about to join room "
                          +  chat2.getName() + " without asking");
                chat.join(sess, bosconn, chat2.getExchange(),
                          chat2.getName(), chat2.getInstance());
            } catch (IOException ioe) {
                LOG.log(Level.SEVERE,"IOException", ioe);
            }
        } else if ((args.getReqClass()
                    & AIMConstants.AIM_CAPS_BUDDYICON) != 0) {
            byte[] icon = args.getInfo().getIcon().getIcon();
            int iconcsum = args.getInfo().getIcon().getChecksum();

            if (!checkIcon(sn, iconcsum)) {
                writeIcon(sn, icon);
            }
        } else if ((args.getReqClass()
                   & AIMConstants.AIM_CAPS_SENDFILE) != 0) {
            String ip = null;
            String ip1 = args.getClientIP1();
            String ip2 = args.getClientIP2();
            String ip3 = args.getVerifiedIP();

            LOG.fine(ip3 + "/" + ip2 + "/" + ip1);

            if (args.isProxied() && ip1 != null) {
                ip = ip1;
            } else if (ip3 != null) {
                ip = ip3;
            } else if (ip1 != null) {
                ip = ip1;
            } else if (ip2 != null) {
                ip = ip2;
            } else if (ip == null) {
                return;
            }

            /*
             * Generate a random message cookie
             */
            MsgCookie cookie =
                new MsgCookie(frame.getConn(), null,
                              AIMConstants.AIM_COOKIETYPE_OFTGET, null);

            FileTransfer ftrans = new FileTransfer(sess);

            ftrans.sendFileAccept(cookie.getCookie(), args.getCookie(),
                                  userinfo.getSN(), ip, args.getPort());
        } else if ((args.getReqClass()
                   & AIMConstants.AIM_CAPS_GETFILE) != 0) {
            String ip = null;
            String ip1 = args.getClientIP1();
            String ip2 = args.getClientIP2();
            String ip3 = args.getVerifiedIP();

            LOG.fine(ip3 + "/" + ip2 + "/" + ip1);

            if (args.isProxied() && ip1 != null) {
                ip = ip1;
            } else if (ip3 != null) {
                ip = ip3;
            } else if (ip1 != null) {
                ip = ip1;
            } else if (ip2 != null) {
                ip = ip2;
            } else if (ip == null) {
                return;
            }

            /*
             * Generate a random message cookie
             */
            MsgCookie cookie =
                new MsgCookie(frame.getConn(), null,
                              AIMConstants.AIM_COOKIETYPE_OFTGET, null);

            FileTransfer ftrans = new FileTransfer(sess);

            ftrans.getFileAccept(cookie.getCookie(), args.getCookie(),
                                 userinfo.getSN(), ip, args.getPort());
        } else if ((args.getReqClass() & AIMConstants.AIM_CAPS_GAMES) != 0
                    || (args.getReqClass() & AIMConstants.AIM_CAPS_GAMES2) != 0
                   || (args.getReqClass() & AIMConstants.AIM_CAPS_SAVESTOCKS)
                   != 0) {
            LOG.fine("Game requested: game=" + args.getInfo().getGame()
                      + ", computer=" + args.getInfo().getComputer());
        } else if ((args.getReqClass()
                   & AIMConstants.AIM_CAPS_TRILLIANCRYPT) != 0) {
            if (args.getStatus() == Rendezvous.TYPE_CANCEL
                || args.getTrillianEncryption().getCmdType()
                   == TrillianSecureIMRendezvous.CMDTYPE_CLOSE) {
                TrillianSecureIM tsi
                    = (TrillianSecureIM)
                      trillianSessions.
                      remove(AIMUtil.normalize(userinfo.getSN()));

                String present = "not present";

                if (tsi != null) {
                    present = "present";
                }

                if (args.getStatus() == Rendezvous.TYPE_CANCEL) {
                    LOG.fine(userinfo.getSN()
                              + " cancelled Tril SecureIM and tsi=" + present);
                }

                if (args.getTrillianEncryption().getCmdType()
                    == TrillianSecureIMRendezvous.CMDTYPE_CLOSE) {
                    LOG.fine(userinfo.getSN()
                              + " closed Tril SecureIM and tsi=" + present);
                }
            }

            TrillianSecureIM tsi
                = (TrillianSecureIM)
                  trillianSessions.get(AIMUtil.normalize(userinfo.getSN()));

            if (tsi != null) {
                LOG.fine("Found existing Trillian SecureIM session with "
                          + userinfo.getSN());

                tsi.handleSecureIM(args);
            } else {
                LOG.fine("No previous Trillian SecureIM session with "
                          + userinfo.getSN() + " found. Creating...");
                tsi = new TrillianSecureIM(sess, userinfo.getSN());

                trillianSessions.put(AIMUtil.normalize(userinfo.getSN()), tsi);

                tsi.handleSecureIM(args);
            }
        } else if ((args.getReqClass()
                   & AIMConstants.AIM_CAPS_ICQRTF) != 0) {
            LOG.fine(userinfo.getSN() + " (using ICQ): "
                      + args.getInfo().getRTFMsg().getRTFMsg());
        }
    }

    /**
     * Describe <code>incomingIMCH4</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param channel an <code>int</code> value
     * @param userinfo an <code>UserInfo</code> value
     * @param args an <code>IncomingIMCH4</code> value
     */
    public void incomingIMCH4(AIMSession sess, AIMFrame frame, int channel,
                              UserInfo userinfo, IncomingIMCH4 args) {
        String typeString = "";
        String icqSep = "";

        try {
            icqSep = new String(new byte[] {(byte) 0xfe}, "US-ASCII");
        } catch (UnsupportedEncodingException uee) {
            LOG.log(Level.SEVERE,"UnsupportedEncodingException", uee);
        }

        switch (args.getType()) {
        case AIMConstants.AIM_ICQMSG_NORMAL:
            typeString = "Normal message";
            break;
        case AIMConstants.AIM_ICQMSG_URL:
            typeString = "URL";
            StringTokenizer st = new StringTokenizer(args.getMsg(), icqSep);
            String url = st.nextToken();
            String desc = st.nextToken();
            LOG.fine("URL=" + url + (desc != null ? " (" + desc + ")" : ""));
            break;
        case AIMConstants.AIM_ICQMSG_AUTHREQUEST:
            typeString = "Requesting authorization from you";
            break;
        case AIMConstants.AIM_ICQMSG_AUTHDENIED:
            typeString = "User has denied your authorization request";
            break;
        case AIMConstants.AIM_ICQMSG_AUTHGRANTED:
            typeString = "User has granted your authorization request";
            break;
        case AIMConstants.AIM_ICQMSG_USERADD:
            typeString = "User has added you to their contact list";
            break;
        case AIMConstants.AIM_ICQMSG_WEBPAGER:
            typeString = "User has sent you this message via the web";
            break;
        case AIMConstants.AIM_ICQMSG_EMAILEX:
            typeString = "User has sent you this message via email";
            break;
        case AIMConstants.AIM_ICQMSG_ACK:
            typeString = "Acknowledgement";
            break;
        case AIMConstants.AIM_ICQMSG_CONTACT:
            StringTokenizer st2 = new StringTokenizer(args.getMsg(), icqSep);
            int buddyCount = Integer.parseInt(st2.nextToken());

            for (int i = 0; i < buddyCount; i++) {
                LOG.fine("UIN=" + st2.nextToken() + ", nick="
                          + st2.nextToken());
            }

            break;
        case AIMConstants.AIM_ICQMSG_SMS:
            typeString = "User sent you this message via SMS";
            break;
        default:
            typeString = "Unknown message type="
                + Integer.toHexString(args.getType());
        }

        if ((args.getType() & AIMConstants.AIM_ICQMSG_FLAG_MASS) != 0) {
            typeString += " [Mass message]";
        }

        LOG.fine(userinfo.getSN() + ": " + args.getMsg() + " (" + typeString
                  + ")");

        if (args.getType() == AIMConstants.AIM_ICQMSG_SMS) {
            ICQSMSMessage ism = new ICQSMSMessage(args.getMsg());
            LOG.fine(ism.toString());
        }
    }

    /**
     * Describe <code>paramInfo</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param params an <code>ICBMParams</code> value
     */
    public void paramInfo(AIMSession sess, AIMFrame frame, ICBMParams params) {
        params.setMaxChan(0x0000);
        /* TODO: Add typing notification constant to use here. */
        params.setFlags(sess.isICQ() ? 0x00000003 : 0x0000000b);
        params.setMaxMsgLen(0x1f40);
        params.setMaxSenderWarn(0x03e7);
        params.setMaxReceiverWarn(0x03e7);
        params.setMinMsgInterval(0x00000000);

        try {
            ICBMHandler im = (ICBMHandler) sess.
                getHandler(SNACFamily.AIM_CB_FAM_MSG);

            im.setICBMParams(sess, params);
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE,"IOException", ioe);
        }
    }

    /**
     * Describe <code>missedCall</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param channel an <code>int</code> value
     * @param userinfo an <code>UserInfo</code> value
     * @param numMissed an <code>int</code> value
     * @param reason an <code>int</code> value
     */
    public void missedCall(AIMSession sess, AIMFrame frame, int channel,
                           UserInfo userinfo, int numMissed, int reason) {
        String s = "unknown reason";

        switch (reason) {
        case 0:
            /* Invalid */
            s = "invalid";
            break;
        case 1:
            /* Message too large */
            s = "too large";
            break;
        case 2:
            /* Rate exceeded */
            s = "rate exceeded";
            break;
        case 3:
            /* Evil Sender */
            s = "sender too evil";
            break;
        case 4:
            /* Evil Receiver */
            s = "receiver too evil";
            break;
        default:
            break;
        }

        LOG.fine("You missed " + numMissed + " message(s) from "
                  + userinfo.getSN() + " because " + s);
    }

    /**
     * Describe <code>clientAutoresp</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param channel an <code>int</code> value
     * @param sn a <code>String</code> value
     * @param reason an <code>int</code> value
     */
    public void clientAutoresp(AIMSession sess, AIMFrame frame, int channel,
                               String sn, int reason) {

    }

    /**
     * Describe <code>clientAutoresp</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param channel an <code>int</code> value
     * @param sn a <code>String</code> value
     * @param reason an <code>int</code> value
     * @param state an <code>int</code> value
     * @param msg a <code>String</code> value
     */
    public void clientAutoresp(AIMSession sess, AIMFrame frame, int channel,
                               String sn, int reason, int state, String msg) {
        LOG.fine("screenname=" + sn + ", status=" + icqStatus(state)
                  + ", msg=" + msg);
    }

    /**
     * Describe <code>msgAck</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param type an <code>int</code> value
     * @param sn a <code>String</code> value
     */
    public void msgAck(AIMSession sess, AIMFrame frame, int type, String sn) {
        LOG.fine("Sent message to " + sn);
    }

    /**
     * Describe <code>typingNotification</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param ck a <code>byte[]</code> value
     * @param type a <code>short</code> value
     * @param sn a <code>String</code> value
     * @param typing a <code>short</code> value
     */
    public void typingNotification(AIMSession sess, AIMFrame frame, byte[] ck,
                                   short type, String sn, short typing) {
        LOG.fine("Got typing code=" + Integer.toHexString(typing));
    }

    /**
     * Describe <code>locateRights</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param maxsiglen an <code>int</code> value
     */
    public void locateRights(AIMSession sess, AIMFrame frame, int maxsiglen) {
        try {
            LocateHandler loc = (LocateHandler) sess.
                getHandler(SNACFamily.AIM_CB_FAM_LOC);

            maxSigLen = maxsiglen;

            AIMConnection conn = AIMConnection.findByType(sess,
                    AIMConstants.AIM_CONN_TYPE_BOS);

            if (!sess.isICQ()) {
                String certFilename =  System.getProperty("user.home")
                                       + System.getProperty("file.separator")
                                       + "mycert.p12";

                if (new File(certFilename).exists()) {
                    SecureIM secureIM
                        = new SecureIM(sess, certFilename, "password");


                    try {
                        secureIM.init();
                    } catch (Exception e) {
                        LOG.log(Level.SEVERE,"Exception", e);
                    }

                    loc.setProfile(sess, conn, "us-ascii", null, "us-ascii",
                                   null, aimCaps, secureIM.getEncoded());
                } else {
                    loc.setProfile(sess, conn, "us-ascii", null, "us-ascii",
                                   null, aimCaps, null);

                }
            } else {
                loc.setProfile(sess, conn, "us-ascii", null, "us-ascii", null,
                               ICQ_CAPS, null);
            }
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE,"IOException", ioe);
        }
    }

    /**
     * Describe <code>userInfo</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param info an <code>UserInfo</code> value
     * @param infoType an <code>int</code> value
     * @param textEncoding a <code>String</code> value
     * @param text a <code>String</code> value
     */
    public void userInfo(AIMSession sess, AIMFrame frame, UserInfo info,
                         int infoType, String textEncoding, String text) {
        String onlineSince = null;
        String memberSince = null;
        String idleTime = null;

        if (text != null && text.length() > 0) {
            LOG.fine("Profile: " + text);
        }

        if ((info.getPresent()
             & AIMConstants.AIM_USERINFO_PRESENT_ONLINESINCE) != 0) {
            LOG.fine("onlineSince=" + info.getOnlineSince());
            onlineSince = "Online since: "
                + new Date(info.getOnlineSince() * 1000L);
        }

        if ((info.getPresent()
             & AIMConstants.AIM_USERINFO_PRESENT_MEMBERSINCE) != 0) {
            memberSince = "Member since: "
                + new Date(info.getMemberSince() * 1000L);
        }

        if ((info.getPresent()
             & AIMConstants.AIM_USERINFO_PRESENT_IDLE) != 0) {
            idleTime = "Idle: " + info.getIdleTime() + " minutes";
        } else {
            idleTime = "Idle: Active";
        }

        LOG.fine("Username: " + info.getSN() + "\n"
                  + "Warning Level: " + (info.getWarnLevel() / 10) + "%" + "\n"
                  + (onlineSince != null ? onlineSince : "") + "\n"
                  + (memberSince != null ? memberSince : "") + "\n"
                  + (idleTime != null ? idleTime : "") + "\n");

        if (infoType == AIMConstants.AIM_GETINFO_AWAYMESSAGE) {
            LOG.fine("Away Message: "
                      + (text != null ? text : "User has no away message"));
        } else if (infoType == AIMConstants.AIM_GETINFO_CAPABILITIES) {
            LOG.fine("Client Capabilities: "
                      + capsToText(info.getCapabilities()));
        } else {
            LOG.fine("No information provided");
        }
    }

    /**
     * Describe <code>invitationSent</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param buffer an <code>AIMInputStream</code> value
     */
    public void invitationSent(AIMSession sess, AIMFrame frame,
                               AIMInputStream buffer) {
        LOG.fine("invitationSent: invitation sent successfully!");
    }

    /**
     * Describe <code>reply</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param searchAddr a <code>String</code> value
     * @param j an <code>int</code> value
     * @param buf a <code>ArrayList</code> value
     */
    public void reply(AIMSession sess, AIMFrame frame, String searchAddr,
                      int j, ArrayList buf) {
        String s = "";

        while (j-- > 0) {
            s += buf.get(j) + " ";
        }

        LOG.fine(searchAddr + " has the following screennames: " + s);
    }

    /**
     * Describe <code>parsePopup</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param popup an <code>AIMPopup</code> value
     */
    public void parsePopup(AIMSession sess, AIMFrame frame, AIMPopup popup) {
        LOG.fine("parsePopup: Got popup: msg=" + popup.getMsg() + ", url="
                  + popup.getURL() + ", width=" + popup.getWidth()
                  + ", height=" + popup.getHeight() + ", delay="
                  + popup.getDelay());
    }

    /**
     * Describe <code>reportInterval</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param interval an <code>int</code> value
     */
    public void reportInterval(AIMSession sess, AIMFrame frame, int interval) {
        LOG.fine("reportInterval: Server wants you to report stats every "
                  + (int) (interval / 60) + " minutes");
    }

    /**
     * Describe <code>reportAck</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param buffer an <code>AIMInputStream</code> value
     */
    public void reportAck(AIMSession sess, AIMFrame frame,
                          AIMInputStream buffer) {
        LOG.fine("reportAck");
    }

    /**
     * Describe <code>parseInfoPerms</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param subtype an <code>int</code> value
     * @param maxrooms an <code>int</code> value
     * @param curexchange an <code>int</code> value
     * @param exchanges a <code>ArrayList</code> value
     */
    public void parseInfoPerms(AIMSession sess, AIMFrame frame, int subtype,
                               int maxrooms, int curexchange,
                               ArrayList exchanges) {
        switch (subtype) {
        case 0x0002:
            LOG.fine("chat info: Chat Rights:\n");
            LOG.fine("chat info: \tMax Concurrent Rooms: " + maxrooms);
            LOG.fine("chat info: \tExchange List: (" + exchanges.size()
                      + " total)");

            for (int i = 0; i < exchanges.size(); i++) {
                ExchangeInfo ei = (ExchangeInfo) exchanges.get(i);
                LOG.fine("chat info: Exchange #" + ei.getNumber()
                          + ":\tExchange name:\t"
                          + (ei.getName() != null ? ei.getName() : "(none)"));
            }

            break;
        default:
            LOG.warning("Unknown permissions=" + Integer.toHexString(subtype));
        }
    }

    /**
     * Describe <code>parseInfoCreate</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param subtype an <code>int</code> value
     * @param fqcn a <code>String</code> value
     * @param instance an <code>int</code> value
     * @param exchange an <code>int</code> value
     * @param flags an <code>int</code> value
     * @param createtime an <code>int</code> value
     * @param maxmsglen an <code>int</code> value
     * @param maxoccupancy an <code>int</code> value
     * @param createperms an <code>int</code> value
     * @param unknown an <code>int</code> value
     * @param name a <code>String</code> value
     * @param ck a <code>String</code> value
     */
    public void parseInfoCreate(AIMSession sess, AIMFrame frame, int subtype,
                                String fqcn, int instance, int exchange,
                                int flags, int createtime, int maxmsglen,
                                int maxoccupancy, int createperms, int unknown,
                                String name, String ck) {
        switch (subtype) {
        case 0x0008:
            LOG.fine("created room: " + fqcn + " " + exchange + " "
                      + instance + " " + flags + " " + createtime + " "
                      + maxmsglen + " " + maxoccupancy + " " + createperms
                      + " " + unknown + " " + name + " " + ck);
            ChatHandler chat = (ChatHandler) sess.
                getHandler(SNACFamily.AIM_CB_FAM_CHT);

            try {
                chat.join(sess, bosconn, exchange, ck, instance);
            } catch (IOException ioe) {
                LOG.log(Level.SEVERE,"IOException", ioe);
            }

            break;
        default:
            LOG.warning("chatnav info: unknown type "
                     + Integer.toHexString(subtype));
        }
    }

    /**
     * Describe <code>infoUpdate</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param roominfo a <code>Chat</code> value
     * @param roomName a <code>String</code> value
     * @param userCount an <code>int</code> value
     * @param occupants a <code>ArrayList</code> value
     * @param roomdesc a <code>String</code> value
     * @param flags an <code>int</code> value
     * @param creationTime an <code>int</code> value
     * @param maxmsglen an <code>int</code> value
     * @param maxOccupancy an <code>int</code> value
     * @param creationPerms an <code>int</code> value
     * @param maxvisiblemsglen an <code>int</code> value
     */
    public void infoUpdate(AIMSession sess, AIMFrame frame,
                           Chat roominfo, String roomName,
                           int userCount, ArrayList occupants, String roomdesc,
                           int flags, int creationTime, int maxmsglen,
                           int maxOccupancy, int creationPerms,
                           int maxvisiblemsglen) {

    }

    /**
     * Describe <code>userlistChange</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param curcount an <code>int</code> value
     * @param userinfo a <code>ArrayList</code> value
     */
    public void userlistChange(AIMSession sess, AIMFrame frame, int curcount,
                               ArrayList userinfo) {
        int type = frame.getSNAC().getSubtype();

        if (type == 0x0003) {
            LOG.fine("user joined room");
        } else if (type == 0x0004) {
            LOG.fine("user left room");
        }
    }

    /**
     * Describe <code>incomingChatMsg</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param userinfo an <code>UserInfo</code> value
     * @param msg a <code>String</code> value
     */
    public void incomingChatMsg(AIMSession sess, AIMFrame frame,
                                UserInfo userinfo, String msg) {
        LOG.fine("<" + userinfo.getSN() + "> " + msg + "[[CHAT MSG]]");
    }

    /**
     * Describe <code>searchReply</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param matches a <code>ArrayList</code> value
     */
    public void searchReply(AIMSession sess, AIMFrame frame,
                            ArrayList matches) {
        int count = matches.size();
        LOG.fine("Got searchReply: " + count + " matches");

        for (int i = 0; i < count; i++) {
            LOG.fine("Match " + i + ":\n" + (ODirInfo) matches.get(i));
        }
    }

    /**
     * Describe <code>interestsReply</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param interests a <code>ArrayList</code> value
     */
    public void interestsReply(AIMSession sess, AIMFrame frame,
                               ArrayList interests) {
        int count = interests.size();
        LOG.fine("Got interestsReply: " + count + " interests");

        ODirHandler odir = (ODirHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_ODR);

        /*
         * FIXME: We really need to wait for the interests reply to come back
         * before we send. This might be why we get dropped a lot of the time.
         */
        ArrayList myInterests = new ArrayList(1);
        myInterests.add("Computers and Technology");

        /*try {
            odir.searchInterests(sess, "us-ascii", myInterests);
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE,"IOException", ioe);
        }*/
    }

    /**
     * Describe <code>uploadAck</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param cksum a <code>byte[]</code> value
     */
    public void uploadAck(AIMSession sess, AIMFrame frame, byte[] cksum) {
        LOG.fine("Got upload ack " + AIMUtil.byteArrayToHexString(cksum));
    }

    /**
     * Describe <code>parseIcon</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param sn a <code>String</code> value
     * @param iconcsum a <code>byte[]</code> value
     * @param icon a <code>byte[]</code> value
     */
    public void parseIcon(AIMSession sess, AIMFrame frame, String sn,
                         byte[] iconcsum, byte[] icon) {
        writeIcon(sn, icon);
        icons.remove(AIMUtil.normalize(sn));
    }

    /**
     * Describe <code>youveGotMail</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param ei an <code>EmailInfo</code> value
     * @param haveNewMail a <code>boolean</code> value
     */
    public void youveGotMail(AIMSession sess, AIMFrame frame, EmailInfo ei,
                             boolean haveNewMail) {
        LOG.fine("url=" + ei.getURL() + ", numUnread=" + ei.getNumMsgs()
                  + ", haveMail=" + (haveNewMail ? "yes" : "no")
                  + ", domain=" + ei.getDomain() + ", flag=" + ei.getFlag());
    }

    /**
     * Describe <code>snacError</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param error an <code>int</code> value
     * @param data an <code>Object</code> value
     */
    public void snacError(AIMSession sess, AIMFrame frame, int error,
                          Object data) {
        LOG.log(Level.SEVERE,"SNAC threw error code=" + error + ", reason="
                  + AIMUtil.snacErrorToString(error));
    }

    /**
     * Describe <code>icbmRemoveCookie</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param cookie a <code>MsgCookie</code> value
     */
    public void icbmRemoveCookie(AIMSession sess, MsgCookie cookie) {

    }

    /**
     * Describe <code>snacRequestTimedOut</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param snac a <code>SNAC</code> value
     */
    public void snacRequestTimedOut(AIMSession sess, SNAC snac) {
        LOG.log(Level.SEVERE,"Request timed out for SNAC request ID="
                  + Integer.toHexString(snac.getId()));
    }

    /**
     * Describe <code>unhandledFrame</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     */
    public void unhandledFrame(AIMSession sess, AIMFrame frame) {

    }

    /**
     * Describe <code>parseRights</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param maxitems a <code>short[]</code> value
     */
    public void parseRights(AIMSession sess, AIMFrame frame, short[] maxitems) {
        LOG.fine("ssi rights: ");

        for (int i = 0; i < maxitems.length; i++) {
            LOG.fine("max type 0x" + Integer.toHexString(i) + "="
                      + maxitems[i]);
        }

        if (maxitems.length >= 0) {
            LOG.fine("maxbuddies=" + maxitems[0]);
        }

        if (maxitems.length >= 1) {
            LOG.fine("maxgroups=" + maxitems[1]);
        }

        if (maxitems.length >= 2) {
            LOG.fine("maxpermits=" + maxitems[2]);
        }

        if (maxitems.length >= 3) {
            LOG.fine("maxdenies=" + maxitems[3]);
        }
    }

   /**
     * Describe <code>parseData</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param fmtVer an <code>int</code> value
     * @param numitems an <code>int</code> value
     * @param list a <code>List</code> value
     * @param timestamp an <code>int</code> value
     */
    public void parseData(AIMSession sess, AIMFrame frame, int fmtVer,
                          int numitems, List list, int timestamp) {
        LOG.fine("ssi: syncing local list and server list");

        try {
            SSIHandler ssi = (SSIHandler) sess.
                getHandler(SNACFamily.AIM_CB_FAM_SSI);

            /* Clean the buddy list */
            ssi.cleanList(sess);

            /* Add from server list to local list */
            BuddyGroup buddyGroup = null;

            if (list != null && !list.isEmpty()) {
                ssi.cleanList(sess);

                buddyGroups = new ArrayList(100);

                for (Iterator i = list.iterator(); i.hasNext();) {
                    SSIItem item = (SSIItem) i.next();

                    if (item.getType() == 0x0000 && item.getName() != null) {
                        buddyGroup.addBuddy(item.getName()).
                        setProperty(Buddy.SESSION, sess);
                    } else if (item.getType() == 0x0001
                               && item.getName() != null
                               && item.getName().length() > 0) {
                        buddyGroup = new BuddyGroup(item.getName());
                        buddyGroups.add(buddyGroup);
                    }
                }
            }

            String infoString = "Visit the daim website at "
                + "<a href=\"http://daim.dev.java.net/\">"
                + "http://daim.dev.java.net/</a>.";

            setInfo(sess, "utf-8", infoString);

            ServiceHandler service = (ServiceHandler) sess.
                getHandler(SNACFamily.AIM_CB_FAM_GEN);

            service.setAvailableMsg(sess, "I'm using daim! Find out more at "
                                    + "http://daim.dev.java.net/>.", null);
            service.setIdle(sess, 0);
            service.setSecureIM(sess);

            if (sess.isICQ()) {
                ICQHandler icq = (ICQHandler) sess.
                    getHandler(SNACFamily.AIM_CB_FAM_ICQ);

                icq.reqOfflineMsgs(sess);
                icq.metaSomething(sess); // get random done?
                icq.reqXML(sess);
                // 0x04ba ?
                //icq.identifyClient(sess);

                if (buddyGroups != null) {
                    for (Iterator i = buddyGroups.iterator();
                         i.hasNext();) {
                        buddyGroup = (BuddyGroup) i.next();

                        for (Iterator i2 = buddyGroup.iterator();
                             i2.hasNext();) {
                            Buddy buddy = (Buddy) i2.next();
                            icq.metaReqBasicInfo(sess,
                                                 Integer.
                                                 parseInt(buddy.getName()));
                        }
                    }
                } else {
                    LOG.fine("No buddylist found!");
                }

                // 0x04b2 ???
            }

            ssi.enable(sess);
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE,"IOException", ioe);
        }
    }

    /**
     * Describe <code>parseAck</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     */
    public void parseAck(AIMSession sess, AIMFrame frame) {

    }

    /**
     * Describe <code>parseDataUnchanged</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     */
    public void parseDataUnchanged(AIMSession sess, AIMFrame frame) {
        parseData(sess, frame, 0, 0, null, 0);
    }

    /**
     * Describe <code>parseAuthReq</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param sn a <code>String</code> value
     * @param reason a <code>String</code> value
     */
    public void parseAuthReq(AIMSession sess, AIMFrame frame, String sn,
                             String reason) {

    }

    /**
     * Describe <code>receiveAuthGranted</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param sn a <code>String</code> value
     * @param reply a <code>byte</code> value
     * @param msg a <code>String</code> value
     */
    public void receiveAuthGranted(AIMSession sess, AIMFrame frame, String sn,
                                   byte reply, String msg) {
        LOG.fine("User " + sn + " has granted you authorization");
    }

    /**
     * Describe <code>receiveAdded</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param sn a <code>String</code> value
     */
    public void receiveAdded(AIMSession sess, AIMFrame frame, String sn) {
        LOG.fine(sn + " has added you to their contact list");
    }

    /**
     * Describe <code>parseAdd</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     */
    public void parseAdd(AIMSession sess, AIMFrame frame) {

    }

    /**
     * Describe <code>parseMod</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     */
    public void parseMod(AIMSession sess, AIMFrame frame) {

    }

    /**
     * Describe <code>parseDel</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     */
    public void parseDel(AIMSession sess, AIMFrame frame) {

    }

    /**
     * Describe <code>receiveAuthGrant</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param sn a <code>String</code> value
     * @param msg a <code>String</code> value
     */
    public void receiveAuthGrant(AIMSession sess, AIMFrame frame,
                                 String sn, String msg) {

    }

    /**
     * Describe <code>receiveAuthRequest</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param sn a <code>String</code> value
     * @param msg a <code>String</code> value
     */
    public void receiveAuthRequest(AIMSession sess, AIMFrame frame,
                                   String sn, String msg) {

    }

    /**
     * Describe <code>receiveAuthReply</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param sn a <code>String</code> value
     * @param reply a <code>byte</code> value
     * @param msg a <code>String</code> value
     */
    public void receiveAuthReply(AIMSession sess, AIMFrame frame,
                                 String sn, byte reply, String msg) {

    }

    /* Begin ICQ */
    /**
     * Describe <code>srvOfflineMsgDone</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     */
    public void srvOfflineMsgDone(AIMSession sess, AIMFrame frame) {

    }

    /**
     * Describe <code>srvMetaGeneralDone</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     */
    public void srvMetaGeneralDone(AIMSession sess, AIMFrame frame) {

    }

    /**
     * Describe <code>srvMetaMoreDone</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     */
    public void srvMetaMoreDone(AIMSession sess, AIMFrame frame) {

    }

    /**
     * Describe <code>srvMetaAboutDone</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     */
    public void srvMetaAboutDone(AIMSession sess, AIMFrame frame) {

    }

    /**
     * Describe <code>srvMetaPassDone</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     */
    public void srvMetaPassDone(AIMSession sess, AIMFrame frame) {

    }

    /**
     * Describe <code>srvMetaGeneral</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param img an <code>ICQMetaGeneral</code> value
     */
    public void srvMetaGeneral(AIMSession sess, AIMFrame frame,
                               ICQMetaGeneral img) {

    }

    /**
     * Describe <code>srvMetaWork</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param imw an <code>ICQMetaWork</code> value
     */
    public void srvMetaWork(AIMSession sess, AIMFrame frame, ICQMetaWork imw) {

    }

    /**
     * Describe <code>srvMetaMore</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param imm an <code>ICQMetaMore</code> value
     */
    public void srvMetaMore(AIMSession sess, AIMFrame frame, ICQMetaMore imm) {

    }

    /**
     * Describe <code>srvMetaAbout</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param about a <code>String</code> value
     */
    public void srvMetaAbout(AIMSession sess, AIMFrame frame, String about) {

    }

    /**
     * Describe <code>srvMetaMoreEmail</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param country a <code>byte</code> value
     * @param flags a <code>byte</code> value
     * @param email a <code>String</code> value
     */
    public void srvMetaMoreEmail(AIMSession sess, AIMFrame frame, byte country,
                                 byte flags, String email) {

    }

    /**
     * Describe <code>srvMetaInterest</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param interestCount a <code>byte</code> value
     * @param interestAreas a <code>ArrayList</code> value
     * @param interestDescriptions a <code>ArrayList</code> value
     */
    public void srvMetaInterest(AIMSession sess, AIMFrame frame,
                                byte interestCount, ArrayList interestAreas,
                                ArrayList interestDescriptions) {

    }

    /**
     * Describe <code>srvMetaBackground</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param pastCount a <code>byte</code> value
     * @param pastAreas a <code>ArrayList</code> value
     * @param pastDescriptions a <code>ArrayList</code> value
     * @param affiliationCount a <code>byte</code> value
     * @param affiliationAreas a <code>ArrayList</code> value
     * @param affiliationDescriptions a <code>ArrayList</code> value
     */
    public void srvMetaBackground(AIMSession sess, AIMFrame frame,
                                  byte pastCount, ArrayList pastAreas,
                                  ArrayList pastDescriptions,
                                  byte affiliationCount,
                                  ArrayList affiliationAreas,
                                  ArrayList affiliationDescriptions) {

    }

    /**
     * Describe <code>srvMetaInfo</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param img an <code>ICQMetaGeneral</code> value
     */
    public void srvMetaInfo(AIMSession sess, AIMFrame frame,
                            ICQMetaGeneral img) {
        ICQPacket icqPacket = getICQPacket(frame);

        if (icqPacket == null) {
            return;
        }

        byte[] data = icqPacket.getData();
        AIMInputStream buffer
            = new AIMInputStream(new ByteArrayInputStream(data));

        String uin = "invalid";

        try {
            uin = "" + buffer.readIntLE();
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE,"IOException", ioe);
        }

        LOG.fine("Got info for " + uin + "/" + img.getNick() + "/"
                  + img.getFirst() + "/" + img.getLast() + "/"
                  + img.getEmail());
    }

    /**
     * Describe <code>srvMeta10E</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param unknown an <code>int</code> value
     */
    public void srvMeta10E(AIMSession sess, AIMFrame frame, int unknown) {

    }

    /**
     * Describe <code>srvMetaFound</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param imf an <code>ICQMetaFound</code> value
     */
    public void srvMetaFound(AIMSession sess, AIMFrame frame,
                             ICQMetaFound imf) {

    }

    /**
     * Describe <code>srvMetaLast</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param imf an <code>ICQMetaFound</code> value
     * @param missed an <code>int</code> value
     */
    public void srvMetaLast(AIMSession sess, AIMFrame frame, ICQMetaFound imf,
                            int missed) {

    }

    /**
     * Describe <code>srvMetaRandom</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param uin an <code>int</code> value
     * @param group a <code>short</code> value
     * @param externalIP a <code>byte[]</code> value
     * @param port an <code>int</code> value
     * @param internalIP a <code>byte[]</code> value
     * @param tcpFlags a <code>byte</code> value
     * @param tcpVersion a <code>short</code> value
     * @param unknown a <code>byte[]</code> value
     */
    public void srvMetaRandom(AIMSession sess, AIMFrame frame, int uin,
                              short group, byte[] externalIP, int port,
                              byte[] internalIP, byte tcpFlags,
                              short tcpVersion, byte[] unknown) {

    }

    /**
     * Describe <code>srvMetaRandomDone</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     */
    public void srvMetaRandomDone(AIMSession sess, AIMFrame frame) {

    }

    /**
     * Describe <code>srvOfflineMsg</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param msg an <code>ICQOfflineMsg</code> value
     */
    public void srvOfflineMsg(AIMSession sess, AIMFrame frame,
                              ICQOfflineMsg msg) {
        LOG.fine("Received offline message from " + msg.getSender()
                  + ", type=" + Integer.toHexString(msg.getType()) + ", msg="
                  + msg.getMsg());
    }
    /* End ICQ */

    /**
     * Describe <code>rateResp</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     */
    public void rateResp(AIMSession sess, AIMFrame frame) {
        try {
            switch (frame.getConn().getType()) {
            case AIMConstants.AIM_CONN_TYPE_AUTH:
                connInitDoneLogin(sess, frame);
                break;
            case AIMConstants.AIM_CONN_TYPE_BOS:
                connInitDoneBOS(sess, frame);
                break;
            case AIMConstants.AIM_CONN_TYPE_CHATNAV:
                connInitDoneChatNav(sess, frame);
                break;
            case AIMConstants.AIM_CONN_TYPE_CHAT:
                connInitDoneChat(sess, frame);
                break;
            case AIMConstants.AIM_CONN_TYPE_ODIR:
                connInitDoneODir(sess, frame);
                break;
            case AIMConstants.AIM_CONN_TYPE_ICO:
                connInitDoneIcon(sess, frame);
                break;
            case AIMConstants.AIM_CONN_TYPE_MAIL:
                connInitDoneMail(sess, frame);
                break;
            default:
                LOG.fine("Unknown connection type="
                          + Integer.toHexString(frame.getConn().getType()));
            }
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE,"IOException", ioe);
        }
    }

    /**
     * Describe <code>rateChange</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param code an <code>int</code> value
     * @param rc a <code>RateClass</code> value
     */
    public void rateChange(AIMSession sess, AIMFrame frame, int code,
                           RateClass rc) {
        final String[] codes = new String[] {
            "invalid",
            "change",
            "warning",
            "limit",
            "limit cleared",
        };

        LOG.fine("Rate change, code="
                   + (code < 5 ? codes[code] : codes[0]));

        /* Other codes are (should be) handled in service handler */
        if (code == AIMConstants.AIM_RATE_CODE_LIMIT) {
            LOG.fine("The last message was not sent because you are "
                      + "exceeding the rate limit. Please try again later.");
        }
    }

    /**
     * Describe <code>serverPause</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     */
    public void serverPause(AIMSession sess, AIMFrame frame) {
        LOG.fine("Got server pause");
    }

    /**
     * Describe <code>serverResume</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     */
    public void serverResume(AIMSession sess, AIMFrame frame) {
        LOG.fine("Got server resume");
    }

    /**
     * Describe <code>selfInfo</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param userinfo an <code>UserInfo</code> value
     */
    public void selfInfo(AIMSession sess, AIMFrame frame, UserInfo userinfo) {

    }

    /**
     * Describe <code>evilNotify</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param newEvil an <code>int</code> value
     * @param userinfo an <code>UserInfo</code> value
     */
    public void evilNotify(AIMSession sess, AIMFrame frame, int newEvil,
                           UserInfo userinfo) {
        LOG.fine("evilNotify: Evil now " + (newEvil / 10) + ". Eviled by "
                  + userinfo.getSN());
    }

    /**
     * Describe <code>migrate</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param ip a <code>String</code> value
     * @param cookie a <code>byte[]</code> value
     */
    public void migrate(AIMSession sess, AIMFrame frame, String ip,
                        byte[] cookie) {

    }

    /**
     * Describe <code>motd</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param id an <code>int</code> value
     * @param msg a <code>String</code> value
     */
    public void motd(AIMSession sess, AIMFrame frame, int id, String msg) {

    }


    /**
     * Describe <code>memRequest</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param offset an <code>int</code> value
     * @param len an <code>int</code> value
     * @param modname a <code>String</code> value
     */
    public void memRequest(AIMSession sess, AIMFrame frame, int offset,
                           int len, String modname) {
        LOG.fine("offset=" + offset + ", length=" + len + ", file="
                  + (modname != null ? modname : "aim.exe"));

        ServiceHandler service = (ServiceHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_GEN);

        try {
            if (len == 0) {
                LOG.fine("len is 0, hashing null");
                service.sendMemBlock(sess, frame.getConn(), offset, len, null,
                                     AIMConstants.
                                     AIM_SENDMEMBLOCK_FLAG_ISREQUEST);
                return;
            }

            URL url = new URL(AIMHASHDATA + "?offset=" + offset + "&len="
                              + len + "&modname=" + modname);

            BufferedReader br
                = new BufferedReader(new InputStreamReader(url.openStream()));

            String line = br.readLine();

            br.close();

            if (line == null) {
                LOG.warning("Unable to get a valid hash for logging in");
                return;
            }

            service.sendMemBlock(sess, frame.getConn(), 0, 16, line.getBytes(),
                                 AIMConstants.AIM_SENDMEMBLOCK_FLAG_ISHASH);
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE,"IOException", ioe);
        }
    }

    /**
     * Done login.
     *
     * @param sess the oscar session
     * @param frame the frame
     * @exception IOException if an error occurs
     */
    private void connInitDoneLogin(AIMSession sess, AIMFrame frame)
        throws IOException {
        AIMConnection tstconn = frame.getConn();

        tstconn.registerListener(SNACFamily.AIM_CB_FAM_ADM,
                                 SNACFamily.AIM_CB_ADM_ERROR, this);
        tstconn.registerListener(SNACFamily.AIM_CB_FAM_ADM,
                                 SNACFamily.AIM_CB_ADM_INFOREPLY, this);
        tstconn.registerListener(SNACFamily.AIM_CB_FAM_ADM,
                                 SNACFamily.AIM_CB_ADM_INFOCHANGE_REPLY, this);
        tstconn.registerListener(SNACFamily.AIM_CB_FAM_ADM,
                                 SNACFamily.AIM_CB_ADM_ACCT_CONFIRM_REPLY,
                                 this);

        ServiceHandler service = (ServiceHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_GEN);
        service.clientReady(sess, tstconn);

        if (changePassword) {
            changePassword(sess, oldPassword, newPassword);
        } else if (formatScreenname) {
            formatScreenname(sess, nick);
        } else if (confirmAccount) {
            confirmAccount(sess);
        } else if (getEmail) {
            getEmail(sess);
        } else if (setEmail) {
            changeEmail(sess, email);
        }
    }

    /**
     * Done bos.
     *
     * @param sess the oscar session
     * @param frame the frame
     * @exception IOException if an error occurs
     */
    private void connInitDoneBOS(AIMSession sess, AIMFrame frame)
        throws IOException {
        AIMConnection tstconn = frame.getConn();

        rct = new RateClearTask(tstconn);
        sess.getTimer().schedule(rct, RateClearTask.TIME, RateClearTask.TIME);

        ServiceHandler service = (ServiceHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_GEN);

        service.reqPersonalInfo(sess, tstconn);

        SSIHandler ssi = (SSIHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_SSI);

        ssi.reqRights(sess);
        ssi.reqData(sess);

        LocateHandler loc = (LocateHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_LOC);
        loc.reqLocateRights(sess, tstconn);

        BuddyListHandler buddy = (BuddyListHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_BUD);
        buddy.reqBuddyRights(sess, tstconn);

        ICBMHandler im = (ICBMHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_MSG);

        im.reqICBMParams(sess);

        /*
         * The next two bos methods probably aren't called when using SSI, but
         * right now we need them to trigger certain callbacks for the signon
         * process.
         */
        BOSHandler bos = (BOSHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_BOS);

        bos.reqRights(sess, tstconn);

        if (!sess.isICQ()) {
            bos.setGroupPerm(sess, frame.getConn(),
                             AIMConstants.AIM_FLAG_ALLUSERS);
            service.setPrivacyFlags(sess, frame.getConn(),
                                    AIMConstants.AIM_PRIVFLAGS_ALLOWIDLE
                                    | AIMConstants.
                                    AIM_PRIVFLAGS_ALLOWMEMBERSINCE);

            StocksHandler sh = new StocksHandler();
            stocks = sh.getStocks("AOL,MSFT");
            //sh.saveStocks(stocks);

            NewsHandler nh = new NewsHandler();
            news = nh.getNews();
        }
    }

    /**
     * Done chat nav.
     *
     * @param sess the oscar session
     * @param frame the frame
     * @exception IOException if an error occurs
     */
    private void connInitDoneChatNav(AIMSession sess, AIMFrame frame)
        throws IOException {
        AIMConnection tstconn = frame.getConn();

        tstconn.registerListener(SNACFamily.AIM_CB_FAM_CTN,
                                 SNACFamily.AIM_CB_CTN_ERROR, this);
        tstconn.registerListener(SNACFamily.AIM_CB_FAM_CTN,
                                 SNACFamily.AIM_CB_CTN_INFO, this);

        ServiceHandler service = (ServiceHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_GEN);
        service.clientReady(sess, tstconn);

        ChatNavHandler chatNav = (ChatNavHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_CTN);
        chatNav.reqRights(sess, tstconn);
    }

    /**
     * Done chat.
     *
     * @param sess the oscar session
     * @param frame the frame
     * @exception IOException if an error occurs
     */
    private void connInitDoneChat(AIMSession sess, AIMFrame frame)
        throws IOException {
        AIMConnection tstconn = frame.getConn();

        tstconn.registerListener(SNACFamily.AIM_CB_FAM_CHT,
                                 SNACFamily.AIM_CB_CHT_ERROR, this);
        tstconn.registerListener(SNACFamily.AIM_CB_FAM_CHT,
                                 SNACFamily.AIM_CB_CHT_USERJOIN, this);
        tstconn.registerListener(SNACFamily.AIM_CB_FAM_CHT,
                                 SNACFamily.AIM_CB_CHT_USERLEAVE, this);
        tstconn.registerListener(SNACFamily.AIM_CB_FAM_CHT,
                                 SNACFamily.AIM_CB_CHT_ROOMINFOUPDATE, this);
        tstconn.registerListener(SNACFamily.AIM_CB_FAM_CHT,
                                 SNACFamily.AIM_CB_CHT_INCOMINGMSG, this);

        ServiceHandler service = (ServiceHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_GEN);
        service.clientReady(sess, tstconn);
    }

    /**
     * Done mail.
     *
     * @param sess the oscar session
     * @param frame the frame
     * @exception IOException if an error occurs
     */
    private void connInitDoneMail(AIMSession sess, AIMFrame frame)
        throws IOException {
        AIMConnection tstconn = frame.getConn();

        tstconn.registerListener(SNACFamily.AIM_CB_FAM_MAL,
                                 SNACFamily.AIM_CB_MAL_ERROR, this);
        tstconn.registerListener(SNACFamily.AIM_CB_FAM_MAL,
                                 SNACFamily.AIM_CB_MAL_REPLY, this);

        ServiceHandler service = (ServiceHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_GEN);
        service.clientReady(sess, tstconn);

        checkMail(sess);
    }

    /**
     * Done oscar directory.
     *
     * @param sess the oscar session
     * @param frame the frame
     * @exception IOException if an error occurs
     */
    private void connInitDoneODir(AIMSession sess, AIMFrame frame)
        throws IOException {
        AIMConnection tstconn = frame.getConn();

        tstconn.registerListener(SNACFamily.AIM_CB_FAM_ODR,
                                 SNACFamily.AIM_CB_ODR_ERROR, this);
        tstconn.registerListener(SNACFamily.AIM_CB_FAM_ODR,
                                 SNACFamily.AIM_CB_ODR_SEARCH_REPLY, this);
        tstconn.registerListener(SNACFamily.AIM_CB_FAM_ODR,
                                 SNACFamily.AIM_CB_ODR_INTERESTS_REPLY, this);

        ServiceHandler service = (ServiceHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_GEN);
        service.clientReady(sess, tstconn);

        /*
         * FIXME: Disconnect problems with ODir, seems random, packet looks OK
         * to me.
         */
        /*ODirHandler odir = (ODirHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_ODR);

        odir.reqInterests(sess);

        odir.searchEmail(sess, "us-ascii", "billg@microsoft.com");

        ODirInfo info = new ODirInfo();

        info.setFirst("Bill");
        info.setLast("Gates");

        odir.searchDirectoryInfo(sess, "us-ascii", info);*/
    }

    /**
     * Get everyone's icon.
     *
     * @param sess the oscar session
     */
    private void getIcons(AIMSession sess) {
        AIMConnection conn
            = AIMConnection.findByGroup(sess, SNACFamily.AIM_CB_FAM_ICO);

        if (conn == null) {
            LOG.fine("[10] Can't request icons now, since no conn available");
            return;
        }

        IconHandler icon = (IconHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_ICO);

        LOG.fine("[10] Icon connection done!");

        for (Iterator i = icons.keySet().iterator(); i.hasNext();) {
            String sn = (String) i.next();
            byte[] cookie = (byte[]) icons.get(sn);
            i.remove();

            if (cookie == null || sn == null) {
                LOG.warning("icon request error: sn=" + sn + ", cookie="
                         + AIMUtil.byteArrayToHexString(cookie));
                break;
            } else {
                try {
                    LOG.fine("[10] Requesting icon for " + sn + "...");
                    icon.request(sess, sn, cookie);
                } catch (IOException ioe) {
                    LOG.log(Level.SEVERE,"IOException", ioe);
                }
            }
        }
    }

    /**
     * Done icon.
     *
     * @param sess the oscar session
     * @param frame the frame
     * @exception IOException if an error occurs
     */
    private void connInitDoneIcon(AIMSession sess, AIMFrame frame)
        throws IOException {
        AIMConnection tstconn = frame.getConn();

        tstconn.registerListener(SNACFamily.AIM_CB_FAM_ICO,
                                 SNACFamily.AIM_CB_ICO_ERROR, this);
        tstconn.registerListener(SNACFamily.AIM_CB_FAM_ICO,
                                 SNACFamily.AIM_CB_ICO_UPLOAD_ACK, this);
        tstconn.registerListener(SNACFamily.AIM_CB_FAM_ICO,
                                 SNACFamily.AIM_CB_ICO_ICO_REPLY, this);

        ServiceHandler service = (ServiceHandler) sess.
            getHandler(SNACFamily.AIM_CB_FAM_GEN);
        service.clientReady(sess, tstconn);
    }

    /**
     * Describe <code>redirect</code> method here.
     *
     * @param sess an <code>AIMSession</code> value
     * @param frame an <code>AIMFrame</code> value
     * @param redir a <code>Redir</code> value
     */
    public void redirect(AIMSession sess, AIMFrame frame, Redir redir) {
        AIMConnection tstconn = null;

        try {
            AIMConnection exist = AIMConnection.findByType(sess,
                                                           redir.getGroup());

            if (exist != null) {
                exist.close();
            }

            String dest = redir.getIp();
            LoginHandler login = null;

            switch (redir.getGroup()) {
            case AIMConstants.AIM_CONN_TYPE_AUTH:
                tstconn = new AIMConnection(sess,
                                            AIMConstants.AIM_CONN_TYPE_AUTH,
                                            dest);

                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_RATEINFO, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_REDIRECT, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_RATECHANGE,
                                         this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_EVIL, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_ERROR, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_MOTD, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_MEMREQUEST,
                                         this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_SELFINFO, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_CLIENTREADY,
                                         this);

                tstconn.connect();

                login = (LoginHandler) sess.
                    getHandler(SNACFamily.AIM_CB_FAM_ATH);
                login.sendCookie(sess, tstconn, redir.getCookie());

                break;
            case AIMConstants.AIM_CONN_TYPE_CHATNAV:
                tstconn = new AIMConnection(sess,
                                            AIMConstants.
                                            AIM_CONN_TYPE_CHATNAV, dest);

                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_RATEINFO, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_REDIRECT, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_RATECHANGE,
                                         this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_EVIL, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_ERROR, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_MOTD, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_MEMREQUEST,
                                         this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_SELFINFO, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_CLIENTREADY,
                                         this);

                tstconn.connect();

                login = (LoginHandler) sess.
                    getHandler(SNACFamily.AIM_CB_FAM_ATH);
                login.sendCookie(sess, tstconn, redir.getCookie());

                break;
            case AIMConstants.AIM_CONN_TYPE_CHAT:
                tstconn = new AIMConnection(sess,
                                            AIMConstants.AIM_CONN_TYPE_CHAT,
                                            dest);

                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_RATEINFO, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_REDIRECT, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_RATECHANGE,
                                         this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_EVIL, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_ERROR, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_MOTD, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_MEMREQUEST,
                                         this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_SELFINFO, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_CLIENTREADY,
                                         this);

                tstconn.connect();

                login = (LoginHandler) sess.
                    getHandler(SNACFamily.AIM_CB_FAM_ATH);
                login.sendCookie(sess, tstconn, redir.getCookie());

                break;
            case AIMConstants.AIM_CONN_TYPE_ODIR:
                tstconn = new AIMConnection(sess,
                                            AIMConstants.AIM_CONN_TYPE_ODIR,
                                            dest);

                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_RATEINFO, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_REDIRECT, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_RATECHANGE,
                                         this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_EVIL, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_ERROR, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_MOTD, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_MEMREQUEST,
                                         this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_SELFINFO, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_CLIENTREADY,
                                         this);

                tstconn.connect();

                login = (LoginHandler) sess.
                    getHandler(SNACFamily.AIM_CB_FAM_ATH);
                login.sendCookie(sess, tstconn, redir.getCookie());

                break;
            case AIMConstants.AIM_CONN_TYPE_ICO:
                tstconn = new AIMConnection(sess,
                                            AIMConstants.AIM_CONN_TYPE_ICO,
                                            dest);

                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_RATEINFO, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_REDIRECT, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_RATECHANGE,
                                         this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_EVIL, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_ERROR, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_MOTD, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_MEMREQUEST,
                                         this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_SELFINFO, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_CLIENTREADY,
                                         this);

                tstconn.connect();

                login = (LoginHandler) sess.
                    getHandler(SNACFamily.AIM_CB_FAM_ATH);
                login.sendCookie(sess, tstconn, redir.getCookie());

                break;
            case AIMConstants.AIM_CONN_TYPE_MAIL:
                tstconn = new AIMConnection(sess,
                                            AIMConstants.AIM_CONN_TYPE_MAIL,
                                            dest);

                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_RATEINFO, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_REDIRECT, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_RATECHANGE,
                                         this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_EVIL, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_ERROR, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_MOTD, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_MEMREQUEST,
                                         this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_SELFINFO, this);
                tstconn.registerListener(SNACFamily.AIM_CB_FAM_GEN,
                                         SNACFamily.AIM_CB_GEN_CLIENTREADY,
                                         this);

                tstconn.connect();

                login = (LoginHandler) sess.
                    getHandler(SNACFamily.AIM_CB_FAM_ATH);
                login.sendCookie(sess, tstconn, redir.getCookie());

                break;
            default:
                LOG.warning("redirect: unhandled group=" + redir.getGroup());
            }
        } catch (IOException ioe) {
            tstconn.close();
            LOG.log(Level.SEVERE,"IOException", ioe);
        }
    }
} // class

