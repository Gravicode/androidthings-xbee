package com.gravicode.mifmasterz.gadgeteercore;

/**
 * Created by mifmasterz on 7/20/17.
 */

public class UnsupportedPinModeException extends  Exception {
    public UnsupportedPinModeException() { }
    public UnsupportedPinModeException(String message) {  super(message);}
    public UnsupportedPinModeException(String message, Exception inner){ super(message, inner); }
}
