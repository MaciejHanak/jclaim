package com.itbs.aimcer.commune.smack;

/**
 * @author Created by Alex Rass on Dec 31, 2004
 */
public class AccountInfo {
    private String userName, password;

    public AccountInfo(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
