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

import Acme.Serve.Serve;
import com.itbs.aimcer.bean.ClientProperties;

/**
 * Provides an easy way to start/stop the server.
 * Also a Security feature.
 *
 * @author Alex Rass 
 * @since Mar 19, 2005
 */
public class ServerStarter {
    private static Serve server;

    public synchronized static void update() {
        if (ClientProperties.INSTANCE.isHTTPServerEnabled()) {
            if (server != null && server.getPort() != ClientProperties.INSTANCE.getHTTPServerPort())
                stopServer();
            if (server == null) {
                startServer();
            }
        } else {
            if (server != null) {
                stopServer();
            }
        }
    }

    static void startServer() {
        server = new Serve(ClientProperties.INSTANCE.getHTTPServerPort());
        // todo add my servlets
        server.addServlet("/TestServlet", new Acme.Serve.TestServlet());
        server.addServlet(PeopleListServlet.servletName, new PeopleListServlet());
        server.addServlet(LogoutServlet.servletName, new LogoutServlet());
        server.addServlet(OptionsServlet.servletName, new OptionsServlet());
        server.addServlet(HelpServlet.servletName, new HelpServlet());
        server.addServlet(FileShareServlet.servletName, new FileShareServlet());
        server.addServlet(ResourceShareServlet.servletName, new ResourceShareServlet("/com/itbs/aimcer/gui/"));
//        server.addServlet("/h:/", new FileServlet());
//        server.addServlet("/login", new LoginServlet());
        server.addServlet("/", new LoginServlet());
        new Thread() {
            /**
             * @see Runnable#run()
             */
            public void run() {
                server.serve();
            }
        }.start();
    }

    static void stopServer() {
        server.stop();
        server = null;
    }
}
