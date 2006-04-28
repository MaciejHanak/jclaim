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

import com.itbs.aimcer.bean.ClientProperties;
import com.itbs.aimcer.commune.Connection;
import com.itbs.aimcer.commune.MessageSupport;
import com.itbs.aimcer.gui.Main;
import com.itbs.aimcer.gui.MenuManager;
import com.itbs.aimcer.gui.PropertiesDialog;
import com.itbs.util.ClassUtil;
import com.itbs.util.ParseUtils;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles some display and connection options.
 *
 * @author Alex Rass
 * @since Mar 19, 2005
 */
public class OptionsServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(OptionsServlet.class.getName());
    private static String optionsScreen;
    // global page stuff
    static final String paramPage = "page";
    static final String servletName = "/Options";
    private static final String paramPeoplePage = "peoplepage";
    // form parameters
    private static final String CHECKED = "checked";
    private static final String paramHideOffline = "hideoffline";
    private static final String paramShowPictures = "showpictures";
    private static final String paramAway = "away";
    private static final String paramWebServer = "server";
    private static final String paramLoginForms = "loginform";

    private static final String paramSet = "submit";
    private static final String paramLogin = "login";
    private static final String paramLogout = "logout";
    private static final String paramCancel = "cancel";

    static {
        // load loginScreen as a string
        try {
            optionsScreen = ClassUtil.getFileContentFromCallersClassDirectory("options.html");
        } catch (IOException e) {
            log.log(Level.SEVERE, "", e);  //Todo change
        }
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
        if (req.getParameter(paramSet) != null) { // set
            ClientProperties.INSTANCE.setHideOffline(req.getParameter(paramHideOffline) != null);
            MenuManager.setGlobalAway(req.getParameter(paramAway) != null);
            ClientProperties.INSTANCE.setShowWebIcons(req.getParameter(paramShowPictures) != null);
            ClientProperties.INSTANCE.setHTTPServerEnabled(req.getParameter(paramWebServer) != null);
            PropertiesDialog.postCheck();
        } else if (req.getParameter(paramLogin) != null) { // login
            for (Connection conn : Main.getConnections()) {
                if (conn instanceof MessageSupport && !conn.isLoggedIn()
                        && conn.getServiceName().equals(req.getParameter(PeopleListServlet.paramMedium))
                        && ((MessageSupport)conn).getUserName().equals(req.getParameter(PeopleListServlet.paramAs))) {
                    try {
                        conn.connect();
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "", e);  // don't really care.
                    }
                } // if
            } // for
        } else if (req.getParameter(paramLogout) != null) { // login
            for (Connection conn : Main.getConnections()) {
                if (conn instanceof MessageSupport && conn.isLoggedIn()
                        && conn.getServiceName().equals(req.getParameter(PeopleListServlet.paramMedium))
                        && ((MessageSupport)conn).getUserName().equals(req.getParameter(PeopleListServlet.paramAs))) {
                    try {
                        conn.disconnect(true);
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "", e);  // don't really care.
                    }
                } // if
            } // for
        }
        if (req.getParameter(paramSet) != null
                || req.getParameter(paramCancel) != null
                || req.getParameter(paramLogin) != null
                || req.getParameter(paramLogout) != null
                ) { // ok or cancel
            new PeopleListServlet().service(req, res);
            return;
        }

        // Feed page info

        res.setHeader("Cache-Control","no-cache"); //HTTP 1.1
        res.setHeader("Pragma","no-cache"); //HTTP 1.0
        res.setDateHeader ("Expires", 0); //prevents caching at the proxy server
        ServletOutputStream pw = res.getOutputStream();
        Map <String, String> params = new HashMap<String, String>(); // used for parameters to the template\

        // vvv Display all relogin boxes vvv
        String rows = "\n<p>";
        for (Connection conn : Main.getConnections()) {
            if (conn instanceof MessageSupport) {
               rows += getRow(conn.getServiceName(), ((MessageSupport)conn).getUserName(), conn.isLoggedIn());
            }
        }
        rows += "</p>\n";
        params.put(paramLoginForms, rows);
        // ^^^ relogin boxes ^^^

        params.put(paramHideOffline, ClientProperties.INSTANCE.isHideOffline()?CHECKED:"");
        params.put(paramShowPictures, ClientProperties.INSTANCE.isShowWebIcons()?CHECKED:"");
        params.put(paramAway, ClientProperties.INSTANCE.isIamAway()?CHECKED:"");
        params.put(paramWebServer, ClientProperties.INSTANCE.isHTTPServerEnabled()?CHECKED:"");

        // bottom of the page
        params.put(paramPage, servletName);
        params.put(PeopleListServlet.paramPageLogoff, LogoutServlet.servletName);
        params.put(paramPeoplePage, PeopleListServlet.servletName);
        params.put(PeopleListServlet.paramPageHelp, HelpServlet.servletName);
        params.put(PeopleListServlet.paramDate, PeopleListServlet.sdf.format(new Date()));
        pw.print(ParseUtils.replace(optionsScreen, params));
        res.setStatus( HttpServletResponse.SC_OK );
        pw.flush();
        pw.close();
    }

    private static String getRow(String connName, String as, boolean loggedIn) {
//        return "<p><FORM method=\"post\">" + "<table align=left width=400>" +
        return "<FORM method=\"post\">" + "<table width=350>" +
                    "<tr>" +
                    "<td width=15%>" +
                    (loggedIn?"&nbsp;":
                    "<input name=\"" + paramLogin + "\" " +
                           "type=\"submit\" " +
                           "value=\"" + "Login" + "\"> ") +
                    "</td><td>" +
                    connName + " as " + as +
                    "<input type=\"hidden\" name=\""+PeopleListServlet.paramMedium+"\" value=\""+connName+"\">" +
                    "<input type=\"hidden\" name=\""+PeopleListServlet.paramAs+"\" value=\""+as+"\">" +
                    "</td><td align=right>" +
                    (loggedIn?
                       "<input name=\"" + paramLogout + "\" " +
                       "type=\"submit\" " +
                       "value=\"" + "Logoff" + "\"> ":
                        "&nbsp;") +
                    "</td>" +
                    "</tr>" +
                "</table><br/>" +
                "</FORM>";
//                "</FORM></p>";
    }

}
