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

/**
 * @author Created by Alex Rass on Oct 10, 2004
 */
public class WeatherConnection extends AbstractConnection {
    private Group weather;
    private Timer timer;
    private static final String TOKEN_PLACE = "View the Forecast Center for ";//"Weather Forecast for ";
    private static final String TOKEN_TEMP = "Temperature: ";
//    private static final String TOKEN_ICON = "http://vortex.accuweather.com/phoenix2/images/common/icons/"; //33_31x31.gif
    private static final String TOKEN_ICON = "http://vortex.accuweather.com/adc2004/common/images/icons/standard/wx/45x45/"; //33_31x31.gif
    public static final String TOKEN_HOURLY = "http://www.weather.com/weather/hourbyhour/"; // + zip
    /** Used to prefix the display string when updates fail */
    private static final String PREFIX_OLD = "<HTML>Old: ";
    private static final boolean DEBUG = false;

    public WeatherConnection() {
    }

    private synchronized void processSettings() {
        weather.clear(this);
        StringTokenizer tok = new StringTokenizer(getProperties().getWeatherZipCodes(), " ;,.#");
        while (tok.hasMoreElements())
            addContact(tok.nextToken());
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
//                page = WebHelper.getPage(new URL("http://wwwa.accuweather.com/adcbin/public/local_index.asp?zipcode="+zip.getName()+"&partner=TRILLIAN"));
                page = WebHelper.getPage(new URL("http://wwwa.accuweather.com/index-forecast.asp?&partner=accuweather&zipcode="+zip.getName()));
//                page = WebHelper.getPage(new URL("http://www.weather.com/weather/local/"+zip+"?lswe="+zip+"&lwsa=WeatherLocalUndeclared"));
//                page = WebHelper.getPage(new URL("http://www.w2.weather.com/weather/local/"+zip+"?lswe="+zip+"&lwsa=WeatherLocalUndeclared"));
                weather = getWeather(page);
                icon = getWeatherIcon(page);
//                System.out.println("Set " + zip.getName() + " to " + zip.getDisplayName() + " " + zip.oldToString());
            } catch (Exception e) {
                e.printStackTrace();
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
            System.out.println("Icon URL failed: "+result);
            e.printStackTrace();
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
     * @param index
     * @param page
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
        System.out.println("Disconnected weather.");
        if (timer != null)
            timer.cancel();
        weather.clear(this);
//        super.disconnect(); // todo work this out
    }

    public void reconnect() {
        if (!getProperties().isShowWeather())
            return;
        disconnect(false);
        timer = new Timer();
//        updateList();

        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (DEBUG)
                    System.out.println("Updating weather..." + new Date());
                try {
                    updateList();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 2000, 30*60*1000); // each half-hour
    }

    public boolean isLoggedIn() {
        return timer != null;
    }

    public void cancel() {
        System.out.println("Cancelled weather.");
        if (timer != null)
            timer.cancel();
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
