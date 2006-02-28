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

package com.itbs.aimcer.web;

import com.itbs.aimcer.bean.*;
import com.itbs.aimcer.commune.Connection;
import com.itbs.aimcer.commune.MessageSupport;
import com.itbs.aimcer.commune.SMS.InvalidDataException;
import com.itbs.aimcer.commune.SMS.SMSWrapper;
import com.itbs.aimcer.gui.ImageCacheUI;
import com.itbs.aimcer.gui.Main;
import com.itbs.aimcer.gui.MessageWindow;
import com.itbs.util.ClassUtil;
import com.itbs.util.GeneralUtils;
import com.itbs.util.ParseUtils;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** TODO make it less reliant on MessageWindow
 * @author Created by Alex Rass on Mar 19, 2005
 */
public class PeopleListServlet extends HttpServlet {
    private static String screen;
    private static final String paramMsgTitle = "messagetitle";
    private static final String paramClose = "close";
    private static final String paramSendNClose = "sendnclose";
    private static final String paramSendCell = "sendncell";
    private static final String paramMsg = "message";
    private static final String paramScroll = "scroll";
    private static final String paramText = "text";
    private static final String paramName = "talkingto";
    public static final String paramMedium = "medium";
    public static final String paramAs = "as";
    private static final String paramList = "contactlist";
    public static final String paramPage = "page";
    private static final String paramRefresh = "refresh";
    static final String paramDate = "date";
    static final String servletName = "/PeopleList";
    static final String paramPageLogoff = "logoffpage";
    static final String paramPageOptions = "optionspage";
    static final String paramPageHelp = "helppage";
    private static final String MESSAGE_REST = "<br>\n"
            + "<TEXTAREA rows=4 cols=40 name=\"text\"></TEXTAREA>"
            + "<br>"
            + "<input name=\"send\" type=\"submit\" value=\"Send\"> "
            + "<input name=\"sendnclose\" type=\"submit\" value=\"Send&Close\"> "
            + "&nbsp;|&nbsp;"
            + "<input name=\"close\" type=\"submit\" value=\"Close\">"

            + "    <script>\n"
            + "    <!--\n"
//            + "      window.scroll(0,document.body.scrollHeight);\n"
            + "      document.form.text.focus();\n"
            + "    -->\n"
            + "    </script>";
    private static final String SCROLL =
        "<script>\n" +
        "<!--\n" +
        "    window.scroll(0,document.body.scrollHeight);\n" +
        "-->\n" +
        "</script>";
    public static final String REFRESH = "<META HTTP-EQUIV=\"refresh\" CONTENT=\"900;URL=" + servletName + "\">";
    static SimpleDateFormat sdf = new SimpleDateFormat("E, MM d, h:mma");

    static {
        // load loginScreen as a string
        try {
            screen = ClassUtil.getFileContentFromCallersClassDirectory("people.html");
        } catch (IOException e) {
            e.printStackTrace();  //Todo change
        }
    }

    String contact(ContactWrapper contact, boolean openWindow) throws UnsupportedEncodingException {
        String display;
        if (!contact.getStatus().isOnline())
            display = contact.getDisplayName() +" (off)";
        else if (contact.getStatus().isAway())
            display = "<i>" + contact.getDisplayName() + "</i>";
        else // prime time
            display = "<b>" + contact.getDisplayName() +"</b>";
        if (openWindow)
            display = "! " + display;
        final String realName = URLEncoder.encode(contact.getName(), "UTF-8");
        if (contact.getConnection() instanceof MessageSupport) {
            String href = "<a href=\"" + servletName + "?" + paramName + "=" + realName
                    + "&" + paramMedium + "=" + contact.getConnection().getServiceName()
                    + "&" + paramAs + "=" + contact.getConnection().getUser().getName()
                    + "\" title=\"" + realName + " on " + contact.getConnection().getServiceName() + " via " + contact.getConnection().getUser().getName() + (contact.getStatus().isOnline()?"":"&nbsp; Last seen: " + contact.getPreferences().getLastConnected()) + "\">";
            if (!ClientProperties.INSTANCE.isShowWebIcons()) {
                return href + display + "</a>";
            } else {
                return "<tr>\n" +
                        "    <td width=\"26\">"
                        +       href
//                        +       href.substring(0, 2) + " style=\"text-decoration:none\" " + href.substring(2)
                        +      "<img border=\"0\" src=\"/image?path=" + ImageCacheUI.getImageName(contact.getConnection().getClass()) + "\" height=\"18\" width=\"18\"/>"
                        +      "</a>"
                        +    "</td>\n"
                        + "   <td >"
                        +       href + display + "</a>"
                        +    "</td>"
                        + "</tr>\n";
            }
        } else
            return display;
    }

    String group(String name) {
        return "<Font size=+1>" + name + ":</Font>";
    }

    /**
     * Services a single HTTP request from the client.
     *
     * @param req the HTTP request
     * @param req the HTTP response
     * @throws javax.servlet.ServletException
     *                             when a Servlet exception has occurred
     * @throws java.io.IOException when an I/O exception has occurred
     */
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getValue(LoginServlet.paramName) == null) {
            new LoginServlet().service(req, res);
            return;
        }
        res.setHeader("Cache-Control","no-cache"); //HTTP 1.1
        res.setHeader("Pragma","no-cache"); //HTTP 1.0
        res.setDateHeader ("Expires", 0); //prevents caching at the proxy server

        ServletOutputStream pw = res.getOutputStream();
        GroupWrapper groupWrapper;
        ContactWrapper contactWrapper;
        List<Connection> connection = Main.getConnections();
        if (connection == null) // test mode or not connected
            pw.println("Your client has not tried to add a Connection.");
        else {
            String buddyList=""; // list of people
            Map <String, String> params = new HashMap<String, String>(); // used for parameters to the template
            ContactWrapper wrapper;
            String talkingTo = req.getParameter(paramName);
            if (talkingTo == null) { // pull up from history
                wrapper = (ContactWrapper) req.getSession(false).getValue(paramName);
            } else {
                // set wrapper
                wrapper = Main.findContact(URLDecoder.decode(talkingTo, "UTF-8"), req.getParameter(paramMedium), req.getParameter(paramAs));
            }

            if (wrapper!=null) {
                String sendMessage = req.getParameter(paramText); // handle talkingTo
                if (sendMessage != null && req.getParameter(paramClose) == null) {
                    try {
                        final MessageImpl message = new MessageImpl(wrapper, true, sendMessage);
                        if (wrapper.getConnection() instanceof MessageSupport) {
                            ((MessageSupport) wrapper.getConnection()).sendMessage(message);
                            MessageWindow mw = MessageWindow.openWindow(wrapper, false);
                            mw.addTextToHistoryPanel(message, true);
                            Thread.yield();
                        }
                    } catch (IOException e) {
                        pw.println("Error trying to notify user via " + wrapper.getConnection().getServiceName() + ": "+ e.getMessage());
                        pw.flush();
                        pw.close();
                        return;
                    }
                    if (req.getParameter(paramSendCell) != null) {
                        try {
                            SMSWrapper.sendMessage(wrapper.getConnection().getUser().getName(), wrapper.getPreferences().getPhone(), sendMessage.trim());
                        } catch (InvalidDataException ex) {
                            pw.println("Error trying to notify user via cell: " + ex.getMessage());
                        } catch (IOException ex) {
                            pw.println("Error trying to notify user via cell: " + ex.getMessage());
                            ex.printStackTrace();
                        }

                    }
                } // sent a message
                if (req.getParameter(paramClose) != null || req.getParameter(paramSendNClose) != null) { // close the window, quick
                    final MessageWindow window = MessageWindow.findWindow(wrapper);
                    if (window != null) // label open windows
                        window.closeWindow();
                    talkingTo = null; // no more.  and later - loose it from session
                    wrapper = null;
                }
                params.put(paramRefresh, ""); // do not autorefresh
            } else  { // wrapper == null.
                params.put(paramRefresh, REFRESH);
            }
            // update
            req.getSession(false).putValue(paramName, wrapper);

            GroupList glist = connection.get(0).getGroupList();
            for (int i = 0; i < glist.size(); i++) {
                Group group = glist.get(i);
                if (group instanceof GroupWrapper)
                    groupWrapper = (GroupWrapper) group;
                else
                    groupWrapper = (GroupWrapper) connection.get(0).getGroupFactory().create(group);
                if (ClientProperties.INSTANCE.isHideEmptyGroups()  && (group.size() == 0 || (ClientProperties.INSTANCE.isHideOffline() && groupWrapper.sizeOnline() == 0))) {
                    continue;
                }
                buddyList += "<fieldset><legend>" + group(group.getName()) + " - " + groupWrapper.sizeOnline() + " / " + group.size() + "</legend>";
//              System.out.println("Group: "+group.getName());
                if (groupWrapper.isShrunk()) {
                    buddyList +="</fieldset>\n";
                    continue;
                }
                if (ClientProperties.INSTANCE.isShowWebIcons())
                    buddyList += "<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n";
                for (int contactCount = 0; contactCount < group.size(); contactCount++) {
                    Nameable b = group.get(contactCount);
                    if (b instanceof ContactWrapper) {
                        contactWrapper = (ContactWrapper) b;
                        if (contactWrapper.getStatus().isOnline() || !ClientProperties.INSTANCE.isHideOffline()) {
                        buddyList += contact(contactWrapper, MessageWindow.findWindow(b) != null) + (ClientProperties.INSTANCE.isShowWebIcons()?"":"<br>\n");

                            if (b.equals(wrapper) && contactWrapper.getConnection() instanceof MessageSupport) {
                                MessageWindow mw = MessageWindow.openWindow(b, false);
                                if (mw != null) {
                                    params.put(paramMsgTitle, "Message: " + contactWrapper.getDisplayName());
                                    params.put(paramMsg, GeneralUtils.makeHTML(Main.getLogger().loadLog((MessageSupport) contactWrapper.getConnection(), contactWrapper.getName()))
                                            + MESSAGE_REST
                                            + contactWrapper.getPreferences().getEmailAddressAsURL()
                                            + (GeneralUtils.isNotEmpty(contactWrapper.getPreferences().getPhone())?" | <input name=\"" + paramSendCell + "\" type=\"submit\" value=\"Page\">":"")
                                            + "  <input name=\"" + paramName + "\" type=\"hidden\" value=\"" + URLEncoder.encode(contactWrapper.getName(), "UTF-8") + "\">"
                                            + "  <input name=\"" + paramMedium + "\" type=\"hidden\" value=\"" + contactWrapper.getConnection().getServiceName() + "\">"
                                            + "  <input name=\"" + paramAs + "\" type=\"hidden\" value=\"" + contactWrapper.getConnection().getUser().getName() + "\">"
                                    );
                                    params.put(paramScroll, SCROLL);
                                    params.put(paramText, ""); // todo fix it so that it remembers typed text even if I hit another link
                                }
                            }
                        }
                    }

                }
                if (ClientProperties.INSTANCE.isShowWebIcons())
                    buddyList += "</table>";
                buddyList +="</fieldset>\n";
            }
            params.put(paramList, buddyList);
            params.put(paramPage, servletName);
            params.put(paramPageLogoff, LogoutServlet.servletName);
            params.put(paramPageOptions, OptionsServlet.servletName);
            params.put(paramPageHelp, HelpServlet.servletName);
            params.put(paramDate, sdf.format(new Date()));
            pw.print(ParseUtils.replace(screen, params));
            res.setStatus( HttpServletResponse.SC_OK );
        }
        pw.flush();
        pw.close();
    }
}
