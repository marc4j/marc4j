
package org.marc4j;

public class MarcError {

    /**
     * FATAL is the most severe error, it is usually set in conjunction with throwing an
     * exception, generally no record is returned when a FATAL error occurs.  Although in 
     * some instances (a record with a field greater than 9999 bytes long) a record will be
     * returned that can be used, but it cannot be written back out without causing an error.
     */
    public final static int FATAL = 4;

    /**
     * MAJOR_ERROR indicates that a serious problem existed with the record, such as a 
     * malformed directory or an invalid subfield tag, or an encoding error where missing 
     * data had to be inferred through some heuristic process.  This indicates that 
     * although a record is returned, you cannot be sure that the record is not corrupted.
     */
    public final static int MAJOR_ERROR = 3;

    /**
     * MINOR_ERROR indicates that a less serious problem existed with the record, such as 
     * a mismatch between the directory stated field sizes and the actual field sizes, 
     * or an encoding error where extraneous data had to be discarded to correctly 
     * interpret the data.  
     */
    public final static int MINOR_ERROR = 2;

    /**
     * ERROR_TYPO indicates that an even less severe problem was found with the record,
     * such as the record leader ends with characters other than "4500" or a field tag 
     * contains non-numeric characters the record contains a html-style entity reference 
     * such as &amp;amp; or &amp;quot; which was replaced with the unescaped version. 
     */
    public final static int ERROR_TYPO = 1;

    /**
     * INFO is used to pass information about the record translation process.  It does 
     * not indicate an error.  It usually will occur when a defaultEncoding value of "BESTGUESS"
     * is passed in.  INFO statements are generated to indicate which character encoding was 
     * determined to be the best fit for the data, and why.
     */
    public final static int INFO = 0;

    public String curField;

    public String curSubfield;

    public int severity;

    public String message;

    public MarcError(final String field, final String subfield, final int severity,
            final String message) {
        curField = field;
        curSubfield = subfield;
        this.severity = severity;
        this.message = message;
    }

    /**
     * Formats the error message for display
     *  
     * @return String - a formatted representation of the error.
     */
    @Override
    public String toString() {
        final String severityMsg = getSeverityMsg(severity);
        final String ret = severityMsg + " : " + message + " --- [ " + curField + " : " + curSubfield + " ]";
        return ret;
    }

    /**
     * Provides a descriptive string representation of the severity level.
     *  
     * @return String - a descriptive string representation of the severity level
     */
    private String getSeverityMsg(final int severity) {
        switch (severity) {
            case FATAL:
                return "FATAL       ";
            case MAJOR_ERROR:
                return "Major Error ";
            case MINOR_ERROR:
                return "Minor Error ";
            case ERROR_TYPO:
                return "Typo        ";
            case INFO:
                return "Info        ";
        }
        return null;
    }
}
