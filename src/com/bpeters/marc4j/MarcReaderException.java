package com.bpeters.marc4j;

/**
 * <p>A <code>MarcReaderException</code> thrown when an error occurs 
 * while parsing MARC records. </p>
 *
 * @author Bas Peters
 */
public class MarcReaderException extends Exception {

    int pos;

    /**
     * <p>Creates an <code>Exception</code> indicating that an error
     * occured while parsing MARC records.</p>
     *
     * @param message the reason why the exception is thrown
     * @param position position in the character stream where the exception is thrown
     */
    public MarcReaderException(String message, int pos) {
	super(message);
	setPosition(pos);
    }

    /**
     * <p>Returns the position in the character stream where the exception is thrown.</p>
     *
     * @return <code>int</code> - the position
     */
    public int getPosition() {
	return pos;
    }

    private void setPosition(int pos) {
	this.pos = pos;
    }

}
