package com.itbs.aimcer.web;

import com.itbs.aimcer.gui.Main;
import com.itbs.util.ClassUtil;
import com.itbs.util.GeneralUtils;
import com.itbs.util.ParseUtils;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Created by Alex Rass on Mar 19, 2005
 */
public class HelpServlet extends HttpServlet {
    private static String helpScreen;
    // global page stuff
    static final String paramPage = "page";
    static final String servletName = "/Help";
    private static final String paramPeoplePage = "peoplepage";
    private static final String paramOptionsPage = "optionspage";
    // form parameters
    private static final String paramAbout = "about";
    private static final String paramFAQ = "faq";

    static {
        // load loginScreen as a string
        try {
            helpScreen = ClassUtil.getFileContentFromCallersClassDirectory("help.html");
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
        // Feed page info
//        res.setHeader("Cache-Control","no-cache"); //HTTP 1.1
//        res.setHeader("Pragma","no-cache"); //HTTP 1.0
//        res.setDateHeader ("Expires", 0); //prevents caching at the proxy server
        ServletOutputStream pw = res.getOutputStream();
        Map <String, String> params = new HashMap<String, String>(); // used for parameters to the template\

        params.put(paramAbout, GeneralUtils.makeHTML(Main.ABOUT_MESSAGE));
        params.put(paramFAQ, Main.URL_FAQ);

        // bottom of the page
        params.put(paramPage, servletName);
        params.put(PeopleListServlet.paramPageLogoff, LogoutServlet.servletName);
        params.put(paramOptionsPage, OptionsServlet.servletName);
        params.put(paramPeoplePage, PeopleListServlet.servletName);
        params.put(PeopleListServlet.paramDate, PeopleListServlet.sdf.format(new Date()));
        pw.print(ParseUtils.replace(helpScreen, params));
        res.setStatus( HttpServletResponse.SC_OK );
        pw.flush();
        pw.close();
    }
}
