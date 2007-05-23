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

package com.itbs.util;


import net.kano.joscar.OscarTools;

import java.awt.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.NumberFormat;
import java.util.Enumeration;

/**
 * Some place to dump genetic utilities.
 *
 * @author Alex Rass
 * @since Mar 29, 2004
 */
public class GeneralUtils {

    /** Used to format numbers */
//    private static NumberFormat FORMATTER = NumberFormat.getInstance();
    private static NumberFormat FORMATTER = NumberFormat.getInstance();
    /**
     * Used to format float numbers
     * @param num to format
     * @return readable representation of the float.
     */
    public static String formatFloat(float num) {
        return FORMATTER.format(num);
    }


    /**
     * Helper method for finding components in a tree
     * @param container dunno
     * @param name dunno
     * @return dunno
     */
    public static Component getComponentByName(final Container container, final String name)
    {
        Component temp;
        if (name.equals(container.getName()))
        {
            return container;
        }
        for (int lcv = container.getComponentCount() - 1; lcv >= 0; lcv--)
        {
            temp = container.getComponent(lcv);
            if (name.equals(temp.getName()))
            {
                return temp;
            }
            if (Container.class.isInstance(temp))
            {
                temp = getComponentByName((Container) temp, name);
                if (temp != null)
                {
                    return temp;
                }
            }
        } // for
        return null; // not in this level
    } // getComponentByName

    public static String sqlEscapeChars(String in) {
        if (in == null) {
            return "";
        }
        int i = 0;
        while ((i = in.indexOf("'", i)) != -1) {
            in = in.substring(0, i) + "''" + in.substring(i + 1, in.length());
            i += 2;
        }
        return in;
    }


    public static void sleep(long delay) {
        try {
            Thread.sleep(delay); // settle
        } catch (InterruptedException e) {// no more mess
        }
    }

    public static String stripHTML(String stringToStrip) {
        String temp = stringToStrip;
        temp = replace(temp, "<br>\n",   "\n");
        temp = replace(temp, "<br>\r",   "\n");
        temp = replace(temp, "<br>",   "\n");
        temp = replace(temp, "<p>",    "\n");
        temp = OscarTools.stripHtml(temp);
        temp = replace(temp, "&lt;",   "<");
        temp = replace(temp, "&gt;",   ">");
        temp = replace(temp, "&quot;", "\"");
        temp = replace(temp, "&amp;",  "&");
        temp = replace(temp, "%20",    " ");
        temp = replace(temp, "%22",    "\"");
        temp = replace(temp, "&nbsp;", " ");
        return temp;
    }

    /**
     * Recursively search within input for any segment match searchFor exactly,
     * replaceWith will be replaced.
     * Replaced string will not be search again.
     * So searchFor could be a substring of replaceWith, without running into overflow error.
     *
     * @param input original String to be replaced
     * @param searchFor String to be replaced in the original String
     * @param replaceWith String to be replaced with in the target String
     * @return the resulting string
     */
    public static String replace(
            final String input, final String searchFor, final String replaceWith)
    {
        if (input == null)
        {
            return "";
        }
        String current = input;
        final int pos = current.indexOf(searchFor);
        if (pos != -1)
        {
            current = current.substring(0, pos) + replaceWith
                + replace(current.substring(pos + searchFor.length()),
                          searchFor, replaceWith);
        }
        return current;
    }

    public static boolean isNotEmpty(String string) {
        return string!=null && string.trim().length()>0;
    }

    /**
     * Makes a name unique.
     * Trims any spaces.  Lowercases it.
     * Precondition: not null!
     * 
     * @param in string to trim
     * @return result
     */
    public static String getSimplifiedName(String in) {
        return in.trim().replaceAll(" ", "").toLowerCase(); // todo veffy trim speeds it up or not
    }

    /**
     * Replaces all weird characters for HTML tags.
     */
    public static String makeHTML(String stringToStrip) {
        String temp = stringToStrip;
        temp = replace(temp, "&",  "&amp;"); // should do those first or it will break all &lt;
        temp = replace(temp, "<",  "&lt;");
        temp = replace(temp, ">",  "&gt;");
        temp = replace(temp, "\"", "&quot;");
        temp = replace(temp, "\n", "<br>");
//        return "<HTML><BODY BGCOLOR=\"#ffffff\"><FONT FACE=\"Arial\" lang=\"0\">" + temp + "</FONT></BODY></HTML>";
        return "<HTML><BODY>" + temp + "</BODY></HTML>";
    }
    
    public static String getInterfaces(boolean showInterfaceNames) {
        String result="";
        try {
           Enumeration interfaces = NetworkInterface.getNetworkInterfaces();

           while(interfaces.hasMoreElements()) {
              NetworkInterface ipStack = (NetworkInterface)interfaces.nextElement();
              if ("lo".equals(ipStack.getName())) continue; // noone cares about that one.
              if (showInterfaceNames)
              result += ipStack.getName() + ":";

              Enumeration ips = ipStack.getInetAddresses();
              while (ips.hasMoreElements()){
                 InetAddress ip = (InetAddress) ips.nextElement();
                 result += " " + ip.toString();
              }
              result+="\n";
           }
        }
        catch (Exception e) {                                       
           result += "Failed to retrieve rest of the interface information";
        }
        return result;
     }

    /**
     * Removes all the crap that isn't the phone number.
     * @param phone to strip
     * @return clean number
     */
    public static String stripPhone(String phone) {
        String result="";
        for (int i = 0; i < phone.length(); i++) {
            char ca = phone.charAt(i);
            if (Character.isDigit(ca) || ca=='+')
              result += ca;
        }
        return result;
    }


}
