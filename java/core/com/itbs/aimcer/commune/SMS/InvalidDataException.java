package com.itbs.aimcer.commune.SMS;

/**
 * Reports problems with initial data.
 * @author Alex Rass on May 28, 2005
 */
public class InvalidDataException extends Exception {
    public InvalidDataException(String message) {
        super(message);
    }
}
