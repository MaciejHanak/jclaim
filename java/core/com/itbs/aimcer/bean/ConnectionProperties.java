package com.itbs.aimcer.bean;

/**
 * @author Alex Rass
 * @since Feb 11, 2006
 */
public interface ConnectionProperties {
    int getAway();

    void setAway(int awayDelay);

    int getDisconnectCount();

    void setDisconnectCount(int disconnectCount);

    String getIamAwayMessage();

    boolean isAutoDismissDialogs();

    boolean isShowWeather();

    String getWeatherZipCodes();
}
