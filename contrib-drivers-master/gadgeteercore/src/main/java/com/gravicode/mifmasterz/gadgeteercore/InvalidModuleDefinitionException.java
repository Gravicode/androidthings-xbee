package com.gravicode.mifmasterz.gadgeteercore;

/**
 * Created by mifmasterz on 7/20/17.
 */

public class InvalidModuleDefinitionException extends Exception {
    public InvalidModuleDefinitionException() { }
    public InvalidModuleDefinitionException(String message) { super(message); }
    public InvalidModuleDefinitionException(String message, Exception inner)  { super(message, inner); }
}
