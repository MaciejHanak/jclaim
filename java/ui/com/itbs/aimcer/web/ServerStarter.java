package com.itbs.aimcer.web;

import Acme.Serve.Serve;
import com.itbs.aimcer.bean.ClientProperties;

/**
 * @author Created by Alex Rass on Mar 19, 2005
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
