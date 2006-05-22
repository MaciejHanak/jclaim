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

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Used in file transfers and other things requiring an IP and port.
 * 
 * @author Alex Rass
 * @since Dec 19, 2004
 */
public class ConnectionInfo {
    InetAddress ip;
    private int port;

    /**
     * Do not use this constructor unless you set the things youself!
     * @deprecated This is for XML persistance.  Do not use!
     */
    public ConnectionInfo() {
    }

    /**
     *  Constructor.
     *
     * @param ip of the thing
     * @param port of the thing
     * @throws UnknownHostException when host information is wrong, see this for details: {@link InetAddress#getByName(String)}
     * @see InetAddress
     */
    public ConnectionInfo(String ip, int port) throws UnknownHostException {
        this(InetAddress.getByName(ip), port);
    }

    /**
     * Constructor
     * @param ip of the thing
     * @param port of the thing
     * @see InetAddress
     */
    public ConnectionInfo(InetAddress ip, int port) {
        setIp(ip);
        setPort(port);
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        if (ip == null) throw new NullPointerException("IP can not be null");
        this.ip = ip;
    }

    public void setPort(int port) {
        if (port < 1) throw new IllegalArgumentException("Port must be a number greater than 0");
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public boolean equals(Object obj) {
        return obj instanceof ConnectionInfo && ((ConnectionInfo) obj).port == port && ((ConnectionInfo) obj).ip.equals(ip);
    }

    public String toString() {
        return super.toString() + "[" + ip + ":" + port + "]";
    }
}
