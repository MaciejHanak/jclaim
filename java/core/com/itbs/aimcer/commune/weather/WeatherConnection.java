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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
    private static Logger logger = Logger.getLogger(WeatherConnection.class.getName());
    
    private Group weather;
    private Timer timer;
    private final Lock updateLock = new ReentrantLock();

    TokenProvider currentProvider = new WeatherCom();
    public static final String TOKEN_HOURLY = "http://www.weather.com/weather/hourbyhour/"; // + zip
//http://www.weather.com/outlook/travel/businesstraveler/hourbyhour/08837

    /** Used to prefix the display string when updates fail */
    private static final String PREFIX_OLD = "<HTML>Old: ";
    private static final boolean DEBUG = false;
    transient Nameable user = new Nameable() {
        public String getName() {
            return "weather";
        }
    };

    public WeatherConnection() {
    }

    interface TokenProvider {
        String getPlace();
        String getTemp();
        String getIcon();
        String getIconLocation();
        /** Used to get hourly weather */
        URL getHourlyURL(String zip) throws MalformedURLException;
        /** Used to get current weather */
        URL getURL(String zip) throws MalformedURLException;
    }

    class AccuWeatherCom implements TokenProvider {
        public String getPlace() {
            return "\" class=\"city_heading\">";
        }

        public String getTemp() {
            return "id=\"quicklook_current_temps\">";
        }

        public String getIcon() {
            return "http://vortex.accuweather.com/adc2004/common/images/wxicons/120x90/"; //images/icons/standard/wx/45x45/"; //33_31x31.gif
        }

        public String getIconLocation() {
            return "http://vortex.accuweather.com/adc2004/common/images/wxicons/120x90/"; //images/icons/standard/wx/45x45/"; //33_31x31.gif
        }

        public URL getHourlyURL(String zip) throws MalformedURLException {
            return new URL(TOKEN_HOURLY+zip);
        }

        public URL getURL(String zip) throws MalformedURLException {
            return new URL("http://www.accuweather.com/index-forecast.asp?&partner=accuweather&zipcode="+zip);
        }
    }

    class WeatherCom implements TokenProvider {
        public String getPlace() {
//            return "<h1 class=\"wxH1\">";
//            return " pn=\"";
            return "locName: \"";
        }

        public String getTemp() {
//            return " class=\"ccTemp\">";
            return "  realTemp: \"";
        }

        public String getIconLocation() {
            return "http://i.imwx.com/web/common/wxicons/130/";
        }

        public String getIcon() {
//            return "http://image.weather.com/web/common/wxicons/";
//            return "http://i.imwx.com/web/common/wxicons/130/";
            return "/img/wxicon/130/";
        }

        public URL getHourlyURL(String zip) throws MalformedURLException {
            return new URL(TOKEN_HOURLY+zip);
        }

        public URL getURL(String zip) throws MalformedURLException {
            return new URL("http://www.weather.com/weather/local/"+zip);
        }
    }

    private void processSettings() {
        try {
            updateLock.lock();
            weather.clear(this);
            StringTokenizer tok = new StringTokenizer(getProperties().getWeatherZipCodes(), " ;,.#");
            while (tok.hasMoreElements()) {
                Contact cw = getContactFactory().create(tok.nextToken(), this);
                cw.getStatus().setOnline(true);
                weather.add(cw);
    //            addContact(tok.nextToken());
            }
        } finally {
            updateLock.unlock();
        }
    }

    public void connect() throws SecurityException, Exception {
        if (!getProperties().isShowWeather())
            return;
        weather = getGroupList().add(getGroupFactory().create("Weather"));
        processSettings();
        notifyStatusChanged();
        reconnect();
    }

    private void updateList() {
        if (updateLock.tryLock()) { // no need to go again if old one still hasn't timed out.
            try {
//            updateLock.lockInterruptibly();

                //grab weather for each contact (zip code) listed
                for (int i = 0; i < weather.size(); i++) {
                    Contact zip = (Contact) weather.get(i);
                    String page;
                    String weather = null;
                    Icon icon = null;
                    try {
                        page = WebHelper.getPage(currentProvider.getURL(zip.getName()).toString(), null);
                        weather = getWeather(page);
                        icon = getWeatherIcon(page);
                        //                log.info("Set " + zip.getName() + " to " + zip.getDisplayName() + " " + zip.oldToString());
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Failed to get a page", e);
                    }
                    if (weather != null && weather.length() > 0 && weather.length() < 100)
                        zip.setDisplayName(weather);
                    if (icon != null && icon.getIconHeight() > 0 && icon.getIconWidth() > 0)
                        zip.setIcon(icon);
                    if ((weather == null || weather.length() >= 100 || icon == null) && (!zip.getDisplayName().startsWith(PREFIX_OLD))) {
                        zip.setDisplayName(PREFIX_OLD + zip.getDisplayName());
                    }
                }
            } finally {
                updateLock.unlock();
                notifyStatusChanged();
            }
        }
    }

    /**
     * Gets the icon to diplay.
     * @param page to parse
     * @return icon
     */
    private Icon getWeatherIcon(final String page) {
        int index = page.indexOf(currentProvider.getIcon());
        String result = getSection(index + currentProvider.getIcon().length(), page);
        try {
            final URL url = new URL(currentProvider.getIconLocation() + result);

            ImageIcon scaledInstance;
            scaledInstance = ImageCache.getImage(url.getFile());
            if (scaledInstance == null) {
                scaledInstance = new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH));
                ImageCache.addImage(url.getFile(), scaledInstance);
            }
            return scaledInstance;
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, "Icon URL failed: "+result, e);
            return null;
        }
    }


    private String getWeather(final String page) {
        int index = page.indexOf(currentProvider.getPlace());
//        int index = page.indexOf("Current Conditions for ");
        String result = getSection(index + currentProvider.getPlace().length(), page);
        index = page.indexOf(currentProvider.getTemp());
        result += " - <b>"+(getSection(index+currentProvider.getTemp().length(), page))+"</b>";
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
        for (int i=index; index > 0 && i < page.length() && page.charAt(i) != '(' && page.charAt(i) != '<' && page.charAt(i) != '"' && page.charAt(i) != '\'' && page.charAt(i) != '&'; i++) {
            result += page.charAt(i);
        } // got the real name
        return result.trim();
    }

    public Nameable getUser() {
        return user;
    }

    public void disconnect(boolean intentional) {
        logger.fine("Disconnected weather.");
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
                        logger.fine("Updating weather..." + new Date());
                    try {
                        updateList();
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Failed to update list", e);
                    }
                }
            }, 2000, 30*60*1000); // each half-hour
        } // synchronized
    }

    public boolean isLoggedIn() {
        return timer != null;
    }

    public void cancel() {
        logger.fine("Cancelled weather.");
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

    public boolean removeContact(Nameable contact, Group group) {
        weather.remove(contact);
        return true;
    }

    public void addContactGroup(Group group) {
    }

    public void removeContactGroup(Group group) {
    }

    public void moveContact(Nameable contact, Group oldGroup, Group newGroup) {
        oldGroup.remove(contact);
        newGroup.add(contact);
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
