package com.bpeters.marc;

/**
 * <p><code>IllegalTagException</code> is thrown when a tag is supplied
 * that is invalid.  </p>
 *
 * @author Bas Peters
 */
public class IllegalTagException extends IllegalArgumentException {

    /**
     * <p>Creates an <code>Exception</code> indicating that the name of the
     * tag is invalid.</p>
     *
     * @param tag the tag name
     * @see Tag
     */
    public IllegalTagException(String tag) {
	super(new StringBuffer()
	    .append("The tag name ")
	    .append(tag)
	    .append(" is not a valid tag name.")
	    .toString());
  }

    /**
     * <p>Creates an <code>Exception</code> indicating that the name of the
     * tag is invalid.</p>
     *
     * @param tag the tag name
     * @param reason the reason why the exception is thrown
     */
    public IllegalTagException(String tag, String reason) {
	super(new StringBuffer()
	    .append("The tag name ")
	    .append(tag)
	    .append(" is not valid: ")
	    .append(reason)
	    .append(".")
	    .toString());
  }

}
