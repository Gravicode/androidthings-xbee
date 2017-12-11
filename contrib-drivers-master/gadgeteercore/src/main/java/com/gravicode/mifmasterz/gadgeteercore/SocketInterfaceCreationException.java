package com.gravicode.mifmasterz.gadgeteercore;

/**
 * Created by mifmasterz on 7/20/17.
 */

public class SocketInterfaceCreationException extends Exception {

    public SocketInterfaceCreationException() { }
    public SocketInterfaceCreationException(String message) {
        super(message);
    }
    public SocketInterfaceCreationException(String message, Exception inner) {
        super(message, inner);
    }

}