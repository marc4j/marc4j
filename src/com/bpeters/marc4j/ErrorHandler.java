package com.bpeters.marc4j;

/**
 * <p>Defines Java callbacks to handle exceptions.  </p>
 *
 * @author Bas Peters 
 */
public interface ErrorHandler {

    /**
     * <p>Receive notification of a warning.  </p>
     *
     */
    public abstract void warning(MarcReaderException exception);

    /**
     * <p>Receive notification of an error.  </p>
     *
     */
    public abstract void error(MarcReaderException exception);

    /**
     * <p>Receive notification of a fatal error.  </p>
     *
     */
    public abstract void fatalError(MarcReaderException exception);

}
