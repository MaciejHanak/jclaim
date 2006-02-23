package com.itbs.aimcer.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author Created by Alex Rass on Mar 19, 2005
 */
public class LogoutServlet extends HttpServlet {
    static final String servletName = "/logoff";

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
        if (session != null) {
            session.invalidate();
        }
        new LoginServlet().service(req, res);
    }
}
