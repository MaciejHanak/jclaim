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

package com.itbs.aimcer.commune;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

/**
 * Utility class to help with Web Sites.
 * Has helper functions to get/post pages.
 *
 * @author Alex Rass
 * @since Oct 10, 2004
 */
public class WebHelper {

    /** This is the only way to specify timeouts in java for connections.  Sad. */
    static {
        System.getProperties().setProperty("sun.net.client.defaultConnectTimeout", ""+ (2 * 60 * 1000));
        System.getProperties().setProperty("sun.net.client.defaultReadTimeout", ""+ (2 * 60 * 1000));
    }

    /**
     * Returns the page.
     * @param url or the page
     * @return page info
     * @throws Exception page fails to load.
     */
    public static String getPage(URL url)
            throws Exception {
        return getPage(url.getHost(), url.getPort()==-1?80:url.getPort(), url.getFile());

    }

    /**
     * Generic web query method for web page referenced by url.
     * Return the page as a string.
     *
     * @param host - website
     * @param port - port (80)
     * @param url - url portion of address.
     * @return page info
     */
    public static String getPage(String host, int port, String url)
            throws Exception {
        Socket httpPipe;   // the TCP socket to the Web server
        InputStream inn;        // the raw byte input stream from server
        OutputStream outt;       // the raw byte output stream to server
        PrintStream out;        // the enhanced textline output stream
        InetAddress webServer;  // the address of the Web server

        webServer = InetAddress.getByName(host);

        httpPipe = new Socket(webServer, port);

        inn = httpPipe.getInputStream();    // get raw streams
        outt = httpPipe.getOutputStream();

        DataInputStream in = new DataInputStream(inn);     // turn into higher-level ones
        out = new PrintStream(outt);

        if (inn == null || outt == null) {
            System.err.println("Failed to open streams to socket.");
            return null;
        }

        // send GET HTTP request

        out.println("GET " + url + " HTTP/1.0");
//        out.println("User-Agent: Mozilla/2.0 (compatible; MyAgent; Windows95)");
/* IMDB does not like web robots. Therefore, We fake a famous browser. */
        out.println("\n");

        // read response until blank separator line
        String response;
        BufferedReader source = new BufferedReader(new InputStreamReader(in));
        StringBuffer buf = new StringBuffer();
        while ((response = source.readLine()) != null) {  // timeout should carry us through bad stuff.
            buf.append(response);
            buf.append("\n");
        }
        out.close();
        source.close();
        // to do this right, really need to cover a bunch more error codes, and only look at headers etc. (AR)
        if (buf.indexOf("301 Moved Permanently")>0) { // found that a page has moved
            int indexOfLocation = buf.indexOf("\nLocation:");
            if (indexOfLocation>0) {
                indexOfLocation += "\nLocation:".length();
                int len = buf.indexOf("\n", indexOfLocation);
                if (len>0) {
                    String newUrl = buf.substring(indexOfLocation, len).trim();
                    return getPage(new URL(newUrl));
                }
            }
        }
        return buf.toString();      // return InputStream to allow client to read resource
    }

    public static String getPage(String url, String post) throws IOException {
        URLConnection connection = new URL(url).openConnection();
        if (post!=null) {
            connection.setDoOutput(true);
            PrintStream out = new PrintStream(connection.getOutputStream());
            out.println(post); // param=value
            out.close();
        }
        String response;
        BufferedReader source = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuffer buf = new StringBuffer();
        while ((response = source.readLine()) != null) {
            buf.append(response);
        }
        source.close();
        return buf.toString();      // return InputStream to allow client to read resource
    }
} // class
