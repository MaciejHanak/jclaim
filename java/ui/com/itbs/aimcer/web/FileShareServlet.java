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
import com.itbs.util.ClassUtil;
import com.itbs.util.GeneralUtils;
import com.itbs.util.ParseUtils;
import com.oreilly.servlet.ServletUtils;
import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;
import com.oreilly.servlet.multipart.Part;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.html.HTML;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Provides a file sharing mechanism.
 *
 * @author Alex Rass
 */
public class FileShareServlet extends HttpServlet {
    private static String screen;
    protected static final String servletName = "/FileShare";

    public static final String HTML_FONT_COLOR = "\"#FF9999\"";
    public static final String HTML_TABLE_BACK = "\"#7FBEF0\"";

    public static final String PARAM_PATH = "path";
    public static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.getDefault());
    private static final String paramList = "list";

    static {
        // load loginScreen as a string
        try {
            screen = ClassUtil.getFileContentFromCallersClassDirectory("fileshare.html");
        } catch (IOException e) {
            e.printStackTrace();  //Todo change
        }
    }

    protected static String getRow(String column1, String column2) {
        return startTag(HTML.Tag.TR)
                 + startTag(HTML.Tag.TD + " valign=top")+ column1 + endTag(HTML.Tag.TD)
                 + startTag(HTML.Tag.TD)+ column2 + endTag(HTML.Tag.TD)
               + endTag(HTML.Tag.TR);
    }


    /** Handles file uploads */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      ServletOutputStream out = init(response);
      try {
        // Use an advanced form of the constructor that specifies a character
        // encoding of the request (not of the file contents) and a file
        // rename policy.
        MultipartParser mp = new MultipartParser(request, 100*1024*1024); // 100 megs
        Part part;
        while ((part = mp.readNextPart()) != null) {
          String name = part.getName();
          if (part.isParam()) {
            // it's a parameter part
            ParamPart paramPart = (ParamPart) part;
            String value = paramPart.getStringValue();
            out.println("param: name=" + name + "; value=" + value);
          }
          else if (part.isFile()) {
              if (GeneralUtils.isNotEmpty(ClientProperties.INSTANCE.getUploadFolder()) && new File(ClientProperties.INSTANCE.getUploadFolder()).isDirectory()) {
                // it's a file part
                FilePart filePart = (FilePart) part;
                String fileName = filePart.getFileName();
                if (fileName != null) {
                  // the part actually contained a file
                  File writeToFile = new File (ClientProperties.INSTANCE.getUploadFolder() + File.separator + fileName + ".fresh");

                  long size = filePart.writeTo(writeToFile);
                  out.println("file: part name=" + name + "; fileName=" + fileName
                          + ", filePath=" + filePart.getFilePath()
                          + ", contentType=" + filePart.getContentType()
                          + ", size=" + size);
                } else {
                  // the field did not contain a file
                  out.println("file: name=" + name + "; EMPTY");
                }
              } else {
                  out.println("File upload is not allowed: <b>invalid setting for the upload folder.</b>");
              }
            out.println("<CENTER><a href=\""+"http://" + request.getHeader("host") + servletName + "\">Done</a></CENTER>");
            out.flush();
          }
        }
      }
      catch (IOException lEx) {
        System.out.println("error reading or saving file");
        lEx.printStackTrace();
      }
    }

    /**
     * Handles file downloads and listing.
     * @param request to handle
     * @param response to provide
     * @throws IOException when errors occur
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletOutputStream out = init(response);

        if (request.getParameter(PARAM_PATH) == null) {
            Map<String,String> params = new HashMap<String, String>(); // used for parameters to the template
            params.put(paramList, getList("http://" + request.getHeader("host") + servletName, ClientProperties.INSTANCE.getDownloadFolder()));
            params.put(PeopleListServlet.paramPage, servletName);
            params.put(PeopleListServlet.paramPageLogoff, LogoutServlet.servletName);
            params.put(PeopleListServlet.paramPageOptions, OptionsServlet.servletName);
            params.put(PeopleListServlet.paramPageHelp, HelpServlet.servletName);
            params.put(PeopleListServlet.paramDate, PeopleListServlet.sdf.format(new Date()));
            out.print(ParseUtils.replace(screen, params));
            response.setStatus( HttpServletResponse.SC_OK );
        } else {
            response.setContentType("application/x-download");
//            response.setContentType(getServletContext().getMimeType(filename));
            // get file
            String pathParameter = request.getParameter(PARAM_PATH);
            response.setHeader("Content-Disposition", "attachment; filename=" + new File(pathParameter).getName());
            sendFile(pathParameter, out);
        }
        out.flush();
        out.close();
    }


    public static ServletOutputStream init(HttpServletResponse response) throws IOException {
        // no caching:
        response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
        response.setHeader("Pragma","no-cache"); //HTTP 1.0
        response.setDateHeader ("Expires", 0);   //prevents caching at the proxy server
        ServletOutputStream out =  response.getOutputStream();
        response.setContentType("text/html");
        return out;
    }

    /**
     * Sends file.  Verifies the file is allowed.
     *
     * @param pathParameter path to the file
     * @param out stream
     * @throws FileNotFoundException when file can not be located.
     */
    protected void sendFile(String pathParameter, ServletOutputStream out) throws IOException {
        File log = new File(ClientProperties.INSTANCE.getDownloadFolder());
        String[] list = log.list();
        if (list!=null) {
            for (String aList : list) {
                File file = new File(ClientProperties.INSTANCE.getDownloadFolder(), aList);
                if (file.getAbsolutePath().equalsIgnoreCase(pathParameter)) {
                    if (file.exists() && file.isFile() && file.canRead()) {
                        ServletUtils.returnFile(pathParameter, out);
//                        FileInputStream fis = new FileInputStream(file);
//                        runStreams(fis, out);
//                        fis.close();
                        break;
                    } else {
                        out.println("Failed to locate your file.");
                    }
                }
            }
        }
    }

    protected String getList(String baseServlet, String startLocation) throws UnsupportedEncodingException {
        File log = new File(startLocation);// + File.separator + "log4j.log");
        if (!log.exists()) {
            return startLocation + " is not a valid folder";
        }
        String result = "";
//            HTML.Tag.BR
        String[] list = log.list();
        File file;
        if (list.length == 0) {
            return startLocation + " contains no files";
        }
        for (String aList : list) {
            file = new File(startLocation, aList);
            if (file.isFile()) {
                result += "<A href=\"" + baseServlet + "?" + PARAM_PATH + "=" + URLEncoder.encode(file.getAbsolutePath(), "UTF-8") + "\"> " + file.getName() + "</A>"
                        + " <Font size=-1>" + new Date(file.lastModified()) + " (" + NUMBER_FORMAT.format(file.length()) + " bytes) </Font>"
                        + startTag(HTML.Tag.BR);
            }
        }
        return result;
    }

    public static String startTag(HTML.Tag tag) {
        return "<" + tag + ">";
    }
    public static String startTag(String tag) {
        return "<" + tag + ">";
    }

    public static String endTag(HTML.Tag tag) {
        return "</" + tag + ">";
    }

    /**
     * STOP.  USE UNIT TESTS!
     * @param args none
     */
    public static final void main(String[] args) {
    }
}