package com.bpeters.marc;

/**
 * <p><code>IllegalAddException</code> is thrown when the addition of the
 * supplied object is illegal.  </p>
 *
 * @author Bas Peters
 */
public class IllegalAddException extends IllegalArgumentException {

    /**
     * <p>Creates an <code>Exception</code> indicating that the addttion
     * of the supplied object is illegal.</p>
     *
     * @param tag the tag name
     * @param reason the reason why the exception is thrown
     */
    public IllegalAddException(String name, String reason) {
	super(new StringBuffer()
	    .append("The addition of the object ")
	    .append(name)
	    .append(" is not allowed: ")
	    .append(reason)
	    .append(".")
	    .toString());
  }

}
