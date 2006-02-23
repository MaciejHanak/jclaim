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

/**
 * @author Created by Alex Rass on Mar 19, 2005
 */
public class OptionsServlet extends HttpServlet {
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
    private static final String paramCancel = "cancel";

    static {
        // load loginScreen as a string
        try {
            optionsScreen = ClassUtil.getFileContentFromCallersClassDirectory("options.html");
        } catch (IOException e) {
            e.printStackTrace();  //Todo change
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
                        e.printStackTrace();  // don't really care.
                    }
                } // if
            } // for
        }
        if (req.getParameter(paramSet) != null
                || req.getParameter(paramCancel) != null
                || req.getParameter(paramLogin) != null
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
        String rows = "";
        for (Connection conn : Main.getConnections()) {
            if (conn instanceof MessageSupport) {
                if (!conn.isLoggedIn()) {
                   rows += getRow(conn.getServiceName(), ((MessageSupport)conn).getUserName());
                }
            }
        }
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

    private static String getRow(String connName, String as) {
        return "<FORM method=\"post\">" +
                "<input name=\"login\" type=\"submit\" value=\"Login\"> " +
                connName + " as " + as +
                "<input type=\"hidden\" name=\""+PeopleListServlet.paramMedium+"\" value=\""+connName+"\">" +
                "<input type=\"hidden\" name=\""+PeopleListServlet.paramAs+"\" value=\""+as+"\">" +
                "</form>";
    }

}
