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

package com.itbs.aimcer.bean;

import com.itbs.util.GeneralUtils;

/**
 * @author Alex Rass
 * Copyright 2004
 */
final public class MessageImpl implements Message {
    boolean outgoing, autoresponse;
    String text, plainText;
    Contact name;

    public MessageImpl(Contact name, boolean outgoing, String text) {
        this.outgoing = outgoing;
        this.text = text;
        this.name = name;
    }

    public MessageImpl(Contact name, boolean outgoing, boolean autoresponse, String text) {
        this.outgoing = outgoing;
        this.autoresponse = autoresponse;
        this.text = text;
        this.name = name;
    }


    public boolean isOutgoing() {
        return outgoing;
    }

    public void setOutgoing(boolean out) {
        outgoing = out;
    }

    public String getText() {
        return text;
    }

    public String getPlainText() {
        if (plainText == null)
          plainText = GeneralUtils.stripHTML(text);
        return plainText;
    }

    public boolean isAutoResponse() {
        return autoresponse;
    }


    public void setName(Contact name) {
        this.name = name;
    }

    public Contact getContact() {
        return name;
    }

    public String toString() {
        return name.getName() + " " + outgoing + " " + autoresponse + " [" + text + "]";
    }
}
