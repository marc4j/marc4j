package com.bpeters.marc4j.helpers;

import com.bpeters.marc4j.MarcHandler;
import com.bpeters.marc4j.ErrorHandler;
import com.bpeters.marc4j.MarcReaderException;
import com.bpeters.marc.Leader;

/**
 * <p><code>DefaultHandler</code> provides default implementations
 * for the callbacks in the James package: </p>
 *
 * <ul>
 * <li>{@link MarcHandler}</li>
 * <li>{@link ErrorHandler}</li>
 * </ul>
 *
 * <p>Application writers can extend this class when they need to
 * implement only part of an interface.</p>
 *
 * @author Bas Peters
 */
public class DefaultHandler implements
    MarcHandler, ErrorHandler {

    public void startFile() {
	// Do nothing.
    }

    public void endFile() {
	// Do nothing.
    }

    public void startRecord(Leader leader) {
	// Do nothing.
    }

    public void endRecord() {
	// Do nothing.
    }

    public void controlField(String tag, char[] data) {
	// Do nothing.
    }

    public void startDataField(String tag, char ind1, char ind2) {
	// Do nothing.
    }

    public void endDataField(String tag) {
	// Do nothing.
    }

    public void subfield(char identifier, char[] data) {
	// Do nothing.
    }

    public void warning(MarcReaderException exception) {
	// Do nothing.
    }

    public void error(MarcReaderException exception) {
	// Do nothing.
    }

    public void fatalError(MarcReaderException exception) {
	// Do nothing.
    }

}
