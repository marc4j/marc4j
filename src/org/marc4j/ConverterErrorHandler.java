
package org.marc4j;

/**
 * Indicates implementing class handles it's own errors.  Rather than throwing exceptions, AnselToUnicode and
 * UnimarcToUnicode will add errors, and let the calling class handle them.  Processing will continue within
 * the Converters.  Very useful for writing a permissive reader.
 */
public interface ConverterErrorHandler {
    /**
     * Add an error message.
     *
     * @param severity Severity of the error, use constants from {@link MarcError}
     * @param message Error message
     */
    void addError(int severity, String message);
}
