package com.bpeters.marc;

/**
 * <p><code>MarcException</code> is thrown when an error occurs
 * while processing a MARC record object.</p>
 *
 * @author Bas Peters
 */
public class MarcException extends Exception {

    /**
     * <p>Creates an <code>Exception</code> indicating that an error
     * occured while processing a Record object.</p>
     *
     * @param message the reason why the exception is thrown
     */
    public MarcException(String message) {
	super(message);
    }
}
