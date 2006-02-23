package com.itbs.aimcer.web;

import com.itbs.aimcer.commune.Connection;
import com.itbs.aimcer.gui.LoginPanel;
import com.itbs.aimcer.gui.Main;
import com.itbs.gui.ErrorDialog;
import com.itbs.util.ClassUtil;
import com.itbs.util.ParseUtils;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Created by Alex Rass on Mar 19, 2005
 */
public class LoginServlet extends HttpServlet {
    private static String loginScreen;
    static final String paramName = "name";
    private static final String paramPassword = "password";
    private static final String paramErrors = "errors";
    private static final String paramDisclaimer = "disclaimer";

    static {
        // load loginScreen as a string
        try {
            loginScreen = ClassUtil.getFileContentFromCallersClassDirectory("login.html");
        } catch (IOException e) {
            ErrorDialog.displayError(null, "Failed to load resources", e);
        }
    }

/*
    private static String convertInputStreamToString(InputStream is) throws IOException {
        String result="";
        byte[] buffer = new byte[1024];
        int size;
        while (is.available()>0 && (size = is.read(buffer)) > 0) {
            result += new String(buffer, 0, size);
        }
        return result;
    }
*/


    /** Services a single HTTP request from the client.
    * @param req the HTTP request
    * @param req the HTTP response
    * @exception javax.servlet.ServletException when a Servlet exception has occurred
    * @exception IOException when an I/O exception has occurred
    */
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String valueName;
//        req.getParameterNames();
//        String result = convertInputStreamToString(req.getInputStream());
        // try name and passwords,
        HttpSession session = req.getSession(true);
        valueName = (String) session.getValue(paramName);
        if (valueName != null) {
            new PeopleListServlet().service(req, res);
            return;
        }

        valueName = req.getParameter(paramName);
        String valuePass = req.getParameter(paramPassword);
        // if pass, go to list screen
        if (Main.getConnections()!=null && isLoginGood(valueName, valuePass)) {
            // save info in the session
            session.putValue(paramName, valueName);
            new PeopleListServlet().service(req, res);
        } else {
            res.setHeader("Cache-Control","no-cache"); //HTTP 1.1
            res.setHeader("Pragma","no-cache"); //HTTP 1.0
            res.setDateHeader ("Expires", 0); //prevents caching at the proxy server
            
            valueName = req.getParameter(paramName);
            // if fail, send the login screen in
            res.setContentType( "text/html" );
            ServletOutputStream pw = res.getOutputStream();
//            PrintWriter pw = res.getWriter();
            Map <String, String> fields = new HashMap<String, String>();
            fields.put(paramName, valueName!=null?valueName:"");
            fields.put(paramDisclaimer, LoginPanel.DISPLAYED_TEXT);
            if (Main.getConnections() == null)
                fields.put(paramErrors, "The client has not logged in.  Remote login is not allowed.<br>Please log in with the actual client and try again.");
            pw.print(ParseUtils.replace(loginScreen, fields));
            res.setStatus( HttpServletResponse.SC_OK );
            pw.flush();
            pw.close();
        }
    }

    private static boolean isLoginGood(String valueName, String valuePass) {
        for (int i = 0; i < Main.getConnections().size(); i++) {
            Connection con = Main.getConnections().get(i);
            // only verified connections and connection's info matched
            if (con.isLoggedIn() && con.isLoginInfoGood(valueName, valuePass))
                return true;
        }
        return false;
    }
}
