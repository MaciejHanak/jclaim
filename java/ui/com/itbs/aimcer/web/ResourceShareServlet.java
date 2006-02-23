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

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Alex Rass
 * @since Nov 1, 2005
 */
public class ResourceShareServlet extends HttpServlet {
    public static final String CLASS_EXTENTION=".class";
    public static final String servletName="/image";
    private String startPath;

    /**
     *
     * @param startPath path to the start of the browse tree.  "/com/itbs/aimcer/gui/"
     */
    public ResourceShareServlet(String startPath) {
        this.startPath = startPath;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletOutputStream out = response.getOutputStream();

        String parameter = request.getParameter(FileShareServlet.PARAM_PATH);
        if (parameter != null && !parameter.endsWith(CLASS_EXTENTION)) {
//            response.setContentType("image/gif");
            response.setContentType(getServletContext().getMimeType(parameter));
            // get file
            response.setHeader("Content-Disposition", "attachment; filename=" + parameter);
            runStreams(ResourceShareServlet.class.getResourceAsStream(startPath + parameter), out);
        } else {
            response.sendError(401);
        }
        out.flush();
        out.close();
    }

    protected static void runStreams(InputStream in, ServletOutputStream out) {
        if (out != null && in != null) {
            try {
                byte[] input = new byte[32*1024];
                int len;
                while ((len = in.read(input)) != -1) {
                    out.write(input, 0, len);
                }
            } catch (IOException e) {
//                Logger.getLogger(BdapLoggerServlet.class).error("", e);
                try {
                    out.println("Failed to read from source " +  e.getMessage());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

}
