/**
 HttpServlet - the HTTP servlet class

 API based on documentation from JavaSoft.

 Copyright (C) 1996,1998 by Jef Poskanzer <jef@acme.com>. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 Visit the ACME Labs Java page for up-to-date versions of this and other
 fine Java utilities: http://www.acme.com/java/
*/

package javax.servlet.http;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.io.Serializable;

/** The HTTP servlet class.
* <P>
* HttpServlets are little Java programs that can be hooked into an HTTP
* server and run in response to URLs.
* <P>
* This is taken from JavaSoft's Servlet API documentation.
* <P>
* <A HREF="/resources/classes/Acme/Serve/servlet/http/HttpServlet.java">Fetch the software.</A><BR>
* <A HREF="/resources/classes/Acme.tar.gz">Fetch the entire Acme package.</A>
* <P>
* @see Acme.Serve.TestServlet
* @see Acme.Serve.Serve
*/
public abstract class HttpServlet extends GenericServlet implements Serializable {
    private static boolean printRequests = true;

    /** Services a single HTTP request from the client.
    * @param req the HTTP request
    * @param req the HTTP response
    * @exception ServletException when a Servlet exception has occurred
    * @exception IOException when an I/O exception has occurred
     */
    protected void service( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException {
        if (req.getMethod().equalsIgnoreCase("get"))
            doGet(req, res);
        else if (req.getMethod().equalsIgnoreCase("put"))
            doPut(req, res);
        else if (req.getMethod().equalsIgnoreCase("head"))
            doHead(req, res);
        else if (req.getMethod().equalsIgnoreCase("post"))
            doPost(req, res);
        else if (req.getMethod().equalsIgnoreCase("delete"))
            doDelete(req, res);
        else if (req.getMethod().equalsIgnoreCase("options"))
            doOptions(req, res);
        else if (req.getMethod().equalsIgnoreCase("trace"))
            doTrace(req, res);
    }

    /** Services a single generic request from the client.
    * (This is required by the peculiar inheritance structure.)
    * @param req the servlet request
    * @param req the servlet response
    * @exception ServletException when a Servlet exception has occurred
    * @exception IOException when an I/O exception has occurred
    */
    public void service( ServletRequest req, ServletResponse res ) throws ServletException, IOException
    {
        if (printRequests) {
            System.out.println("["+req.getRemoteAddr()+":" + ((HttpServletRequest) req).getRequestURI() +"]");
        }
        try {
            service( (HttpServletRequest) req, (HttpServletResponse) res );
        } catch ( ClassCastException e ) {
            throw new ServletException( "not an HTTP request" );
        }
    }

    protected void doDelete(HttpServletRequest req,
                     HttpServletResponse resp)
              throws ServletException,
                     java.io.IOException {
    }
    protected void doGet(HttpServletRequest req,
                     HttpServletResponse resp)
              throws ServletException,
                     java.io.IOException {
    }
    protected void doHead(HttpServletRequest req,
                     HttpServletResponse resp)
              throws ServletException,
                     java.io.IOException {
    }
    protected void doOptions(HttpServletRequest req,
                     HttpServletResponse resp)
              throws ServletException,
                     java.io.IOException {
    }
    protected void doPost(HttpServletRequest req,
                     HttpServletResponse resp)
              throws ServletException,
                     java.io.IOException {
    }
    protected void doPut(HttpServletRequest req,
                     HttpServletResponse resp)
              throws ServletException,
                     java.io.IOException {
    }

    protected void doTrace(HttpServletRequest req,
                     HttpServletResponse resp)
              throws ServletException,
                     java.io.IOException {
    }

    protected long getLastModified(HttpServletRequest req)
              throws ServletException,
                     java.io.IOException {
        return -1;
    }
} // class HttpServlet
