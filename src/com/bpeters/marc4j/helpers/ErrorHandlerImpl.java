package com.bpeters.marc4j.helpers;

import com.bpeters.marc4j.ErrorHandler;
import com.bpeters.marc4j.MarcReaderException;

/**
 * <p>Implements the {@link ErrorHandler} interface to report about
 * warnings and errors that occur during the parsing of a MARC record.  </p>
 *
 * @author Bas Peters
 */
public class ErrorHandlerImpl implements ErrorHandler {

    public void warning(MarcReaderException exception) {
	System.err.println("Type: warning");
	System.err.println("Position: " + exception.getPosition());
	System.err.println("Message: " + exception.getMessage() + "\n");
    }

    public void error(MarcReaderException exception) {
	System.err.println("Type: error");
	System.err.println("Position: " + exception.getPosition());
	System.err.println("Message: " + exception.getMessage() + "\n");
    }

    public void fatalError(MarcReaderException exception) {
	System.err.println("Type: fatal error");
	System.err.println("Position: " + exception.getPosition());
	System.err.println("Message: " + exception.getMessage() + "\n");
    }

}
