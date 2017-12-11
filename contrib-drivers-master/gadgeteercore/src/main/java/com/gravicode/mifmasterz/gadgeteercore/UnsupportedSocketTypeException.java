package com.gravicode.mifmasterz.gadgeteercore;

/**
 * Created by mifmasterz on 7/20/17.
 */

public class UnsupportedSocketTypeException extends Exception {
    public UnsupportedSocketTypeException() { }
    public UnsupportedSocketTypeException(String message) { super(message); }
    public UnsupportedSocketTypeException(String message, Exception inner) { super(message, inner); }
}
