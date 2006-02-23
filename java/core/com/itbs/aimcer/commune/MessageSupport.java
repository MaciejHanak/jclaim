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

import com.itbs.aimcer.bean.Message;
import com.itbs.aimcer.bean.Nameable;

import javax.swing.text.html.HTMLEditorKit;
import java.io.IOException;

/**
 * Describes a collection which offers message support.
 * 
 * @author Alex Rass
 * @since Apr 24, 2005
 */
public interface MessageSupport extends Connection {
    public String getUserName();
    public void setUserName(String userName);
    void setPassword(String password);
    String getPassword();

    void sendMessage(Message message);
    void sendTypingNotification();

    boolean isSecureMessageSupported();

    void sendSecureMessage(Message message) throws IOException;

    /**
     * True if this is a system message.
     * @param contact to check
     * @return true if a system message
     */
    boolean isSystemMessage(Nameable contact);

    /**
     * Returns support Account
     * @return support account or null
     */
    String getSupportAccount();

    /**
     * Which editor kit to use to display the incomming content.
     * For future use.
     * @return editor kit
     */
    public HTMLEditorKit getEditorKit();
}
