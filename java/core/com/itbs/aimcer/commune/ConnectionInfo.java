package com.itbs.aimcer.commune;

import java.net.InetAddress;

/**
 * @author Created by Alex Rass on Dec 19, 2004
 */
public class ConnectionInfo {
    InetAddress ip;
    private int port;
    public ConnectionInfo(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
