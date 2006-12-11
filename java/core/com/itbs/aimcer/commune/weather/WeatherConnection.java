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

package com.itbs.aimcer.commune.weather;

import com.itbs.aimcer.bean.Contact;
import com.itbs.aimcer.bean.Group;
import com.itbs.aimcer.bean.Nameable;
import com.itbs.aimcer.commune.AbstractConnection;
import com.itbs.aimcer.commune.WebHelper;
import com.itbs.util.ImageCache;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides access to weather information.
 * Done via web page scraping.
 * 
 * @author Alex Rass
 * @since Oct 10, 2004
 */
public class WeatherConnection extends AbstractConnection {
    private static Logger log = Logger.getLogger(WeatherConnection.class.getName());
    
    private Group weather;
    private Timer timer;
    private static final String TOKEN_PLACE = "class=\"cityTitle\">";
    private static final String TOKEN_TEMP = "id=\"quicklook_current_temps\">";
//    private static final String TOKEN_ICON = "http://vortex.accuweather.com/phoenix2/images/common/icons/"; //33_31x31.gif
    private static final String TOKEN_ICON = "http://vortex.accuweather.com/adc2004/common/images/wxicons/120x90/"; //images/icons/standard/wx/45x45/"; //33_31x31.gif
    public static final String TOKEN_HOURLY = "http://www.weather.com/weather/hourbyhour/"; // + zip
    /** Used to prefix the display string when updates fail */
    private static final String PREFIX_OLD = "<HTML>Old: ";
    private static final boolean DEBUG = false;

    public WeatherConnection() {
    }

    private synchronized void processSettings() {
        weather.clear(this);
        StringTokenizer tok = new StringTokenizer(getProperties().getWeatherZipCodes(), " ;,.#");
        while (tok.hasMoreElements()) {
            Contact cw = getContactFactory().create(tok.nextToken(), this);
            cw.getStatus().setOnline(true);
            weather.add(cw);
//            addContact(tok.nextToken());
        }
    }

    public void connect() throws SecurityException, Exception {
        if (!getProperties().isShowWeather())
            return;
        weather = getGroupList().add(getGroupFactory().create("Weather"));
        processSettings();
        reconnect();
    }

    private synchronized void updateList() {
        //grab weather for each contact (zip code) listed
        for (int i = 0; i < weather.size(); i++) {
            Contact zip = (Contact) weather.get(i);
            String page;
            String weather=null;
            Icon icon = null;
            try {
//                page = WebHelper.getPage(new URL("http://www.w2.weather.com/weather/local/" + zip));
//                page = WebHelper.getPage(new URL("http://www.weather.com/weather/local/" + zip));
                page = WebHelper.getPage(new URL("http://wwwa.accuweather.com/index-forecast.asp?&partner=accuweather&zipcode="+zip.getName()));
//                page = WebHelper.getPage(new URL("http://www.weather.com/weather/local/"+zip+"?lswe="+zip+"&lwsa=WeatherLocalUndeclared"));
//                page = WebHelper.getPage(new URL("http://www.w2.weather.com/weather/local/"+zip+"?lswe="+zip+"&lwsa=WeatherLocalUndeclared"));
                weather = getWeather(page);
                icon = getWeatherIcon(page);
//                log.info("Set " + zip.getName() + " to " + zip.getDisplayName() + " " + zip.oldToString());
            } catch (Exception e) {
                log.log(Level.SEVERE, "Failed to get a page", e);
            }
            if (weather != null && weather.length() < 100)
                zip.setDisplayName(weather);
            if (icon != null && icon.getIconHeight()>0 && icon.getIconWidth()>0)
                zip.setIcon(icon);
            if ((weather == null || weather.length() >= 100 || icon == null) && (!zip.getDisplayName().startsWith(PREFIX_OLD))) {
                zip.setDisplayName(PREFIX_OLD + zip.getDisplayName());
            }
        }
    }

    /**
     * Gets the icon to diplay.
     * @param page to parse
     * @return icon
     */
    private Icon getWeatherIcon(final String page) {
        int index = page.indexOf(TOKEN_ICON);
        String result = getSection(index + TOKEN_ICON.length(), page);
        try {
            final URL url = new URL( TOKEN_ICON + result);

            ImageIcon scaledInstance;
            scaledInstance = ImageCache.getImage(url.getFile());
            if (scaledInstance == null) {
                scaledInstance = new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH));
                ImageCache.addImage(url.getFile(), scaledInstance);
            }
            return scaledInstance;
        } catch (MalformedURLException e) {
            log.log(Level.SEVERE, "Icon URL failed: "+result, e);
            return null;
        }
    }


    private static String getWeather(final String page) {
        int index = page.indexOf(TOKEN_PLACE);
//        int index = page.indexOf("Current Conditions for ");
        String result = getSection(index + TOKEN_PLACE.length(), page);
        index = page.indexOf(TOKEN_TEMP, index);
        result += " - <b>"+(getSection(index+TOKEN_TEMP.length(), page))+"</b>";
        return "<HTML>"+ result.replaceAll("&nbsp;", " ").trim() + "</HTML>";
    }

    /**
     * Returns a clean data section.
     * @param index starting index.
     * @param page to parse
     * @return clean data section
     */
    private static String getSection(int index, String page) {
        String result = "";
        for (int i=index; index > 0 && i < page.length() && page.charAt(i) != '(' && page.charAt(i) != '<' && page.charAt(i) != '"'&& page.charAt(i) != '&'; i++) {
            result += page.charAt(i);
        } // got the real name
        return result.trim();
    }

    public Nameable getUser() {
        return null;
    }

    public void disconnect(boolean intentional) {
        log.fine("Disconnected weather.");
        synchronized(this) {
            if (timer != null)
                timer.cancel();
        }
        weather.clear(this);
//        super.disconnect(); // todo work this out
    }

    public void reconnect() {
        if (!getProperties().isShowWeather())
            return;
        synchronized(this) {
            disconnect(false);
            timer = new Timer("Weather Timer", true);
//          updateList();

            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    if (DEBUG)
                        log.fine("Updating weather..." + new Date());
                    try {
                        updateList();
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "Failed to update list", e);
                    }
                }
            }, 2000, 30*60*1000); // each half-hour
        } // synchronized
    }

    public boolean isLoggedIn() {
        return timer != null;
    }

    public void cancel() {
        log.fine("Cancelled weather.");
        synchronized(this) {
            if (timer != null)
                timer.cancel();
        }
    }

    public void setTimeout(int timeout) { }

    public void addContact(String zip) {
        Contact cw = getContactFactory().create(zip, this);
        cw.getStatus().setOnline(true);
        addContact(cw, weather);
    }

    public void addContact(Nameable contact, Group group) {
        group.add(contact);
        updateList();
    }

    public void removeContact(Nameable contact) {
        weather.remove(contact);
    }

    public void addContactGroup(Group group) {
    }

    public void removeContactGroup(Group group) {
    }

    public String getServiceName() {
        return "Weather";
    }

    public void setAway(boolean away) {
    }

    public boolean isAway() {
        return false;
    }

}
