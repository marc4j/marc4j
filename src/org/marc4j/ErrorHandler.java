/**
 * Copyright (C) 2004 Bas Peters
 *
 * This file is part of MARC4J
 *
 * MARC4J is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * MARC4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with MARC4J; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.marc4j;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Defines and describes errors encountered in the processing a given MARC record. Used in conjunction with the
 * MarcPermissiveReader class.
 *
 * @author Robert Haschart
 */
@Deprecated
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ErrorHandler {

    /**
     * FATAL is the most severe error, it is usually set in conjunction with throwing an exception, generally no record
     * is returned when a FATAL error occurs. Although in some instances (a record with a field greater than 9999 bytes 
     * long) a record will be returned that can be used, but it cannot be written back out without causing an error.
     */
    public final static int FATAL = 4;

    /**
     * MAJOR_ERROR indicates that a serious problem existed with the record, such as a malformed directory or an invalid
     * subfield tag, or an encoding error where missing data had to be inferred through some heuristic process. This
     * indicates that although a record is returned, you cannot be sure that the record is not corrupted.
     */
    public final static int MAJOR_ERROR = 3;

    /**
     * MINOR_ERROR indicates that a less serious problem existed with the record, such as a mismatch between the
     * directory stated field sizes and the actual field sizes, or an encoding error where extraneous data had to be
     * discarded to correctly interpret the data.
     */
    public final static int MINOR_ERROR = 2;

    /**
     * ERROR_TYPO indicates that an even less severe problem was found with the record, such as the record leader ends
     * with characters other than "4500" or a field tag contains non-numeric characters the record contains a html-style
     * entity reference such as &amp;amp; or &amp;quot; which was replaced with the unescaped version.
     */
    public final static int ERROR_TYPO = 1;

    /**
     * INFO is used to pass information about the record translation process. It does not indicate an error. It usually
     * will occur when a defaultEncoding value of "BESTGUESS" is passed in. INFO statements are generated to indicate
     * which character encoding was determined to be the best fit for the data, and why.
     */
    public final static int INFO = 0;

    private List errors;

    private String curRecordID;

    private String curField;

    private String curSubfield;

    private boolean hasMissingID;

    private int maxSeverity;

    public class Error {

        public String curRecordID;

        public String curField;

        public String curSubfield;

        public int severity;

        public String message;

        public Error(final String recordID, final String field, final String subfield,
                final int severity, final String message) {
            curRecordID = recordID;
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

        private void setCurRecordID(final String curRecordID) {
            this.curRecordID = curRecordID;
        }

        private String getCurRecordID() {
            return curRecordID;
        }
    }

    /**
     * Constructs an error handler.
     */
    public ErrorHandler() {
        errors = null;
        hasMissingID = false;
        maxSeverity = INFO;
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

    /**
     * Returns true if any errors (or warnings) were encountered in processing the current record. Note that if only
     * INFO level messages are encountered for a given record, this method will return false.
     *
     * @return boolean - The highest error severity level encountered for the current record.
     */
    public boolean hasErrors() {
        return errors != null && errors.size() > 0 && maxSeverity > INFO;
    }

    /**
     * Returns the highest error severity level encountered in processing the current record.
     *
     * @return int - The highest error severity level encountered for the current record.
     */
    public int getMaxSeverity() {
        return maxSeverity;
    }

    /**
     * Returns a list of all of the errors encountered in processing the current record.
     *
     * @return List - A list of all of the errors encountered for the current record.
     */
    public List getErrors() {
        if (errors == null || errors.size() == 0) {
            return null;
        }
        return errors;
    }

    /**
     * Resets the list of errors to empty. This should be called at the beginning of processing of each record.
     */
    public void reset() {
        errors = null;
        maxSeverity = INFO;
    }

    /**
     *  Logs an error message using the stated severity level.  Uses the values passed  
     *  in id, field, and subfield to note the location of the error.
     * 
     * @param id - the record ID of the record currently being processed
     * @param field - the tag of the field currently being processed
     * @param subfield - the subfield tag of the subfield currently being processed
     * @param severity - An indication of the relative severity of the error that was 
     *                      encountered.
     * @param message - A descriptive message about the error that was encountered.
     */
    public void addError(final String id, final String field, final String subfield,
            final int severity, final String message) {

        if (errors == null) {
            errors = new LinkedList();
            hasMissingID = false;
        }
        if (id != null && id.equals("unknown")) {
            hasMissingID = true;
        } else if (hasMissingID) {
            setRecordIDForAll(id);
        }
        errors.add(new Error(id, field, subfield, severity, message));
        if (severity > maxSeverity) {
            maxSeverity = severity;
        }
    }

    /**
     * Logs an error message using the stated severity level. Uses the values stored in curRecordID, curField, and
     * curSubfield to note the location of the error.
     *
     * @param severity - An indication of the relative severity of the error that was encountered.
     * @param message - A descriptive message about the error that was encountered.
     */
    public void addError(final int severity, final String message) {
        addError(curRecordID, curField, curSubfield, severity, message);
    }

    /**
     * Copys a List of errors into the current error handler
     *
     * @param newErrors - A list of Errors.
     */
    public void addErrors(final List newErrors) {
        if (newErrors == null || newErrors.size() == 0) {
            return;
        }
        if (errors == null) {
            errors = new LinkedList();
            hasMissingID = false;
        }
        for (final Object err : newErrors) {
            final Error errobj = (Error) err;
            errors.add(errobj);
            if (errobj.severity > maxSeverity) {
                maxSeverity = errobj.severity;
            }
        }
    }

    /**
     *  Copies a List of errors into the current error handler
     * 
     * @param recID - the id of the record being read
     * @param recordErrors - A list of Errors.
     */
    public void addErrors(final String recID, final List<MarcError> recordErrors) {

        if (recordErrors == null || recordErrors.size() == 0) {
            return;
        }
        if (errors == null) {
            errors = new LinkedList();
            hasMissingID = false;
        }
        for (final MarcError err : recordErrors) {
            final Error errobj = new Error(recID, err.curField, err.curSubfield, err.severity,
                    err.message);
            errors.add(errobj);
            if (errobj.severity > maxSeverity) {
                maxSeverity = errobj.severity;
            }
        }
    }

    private void setRecordIDForAll(final String id) {
        if (id != null) {
            final Iterator iter = errors.iterator();
            while (iter.hasNext()) {
                final Error err = (Error) iter.next();
                if (err.getCurRecordID() == null || err.getCurRecordID().equals("unknown")) {
                    err.setCurRecordID(id);
                }
            }
            hasMissingID = false;
        }
    }

    /**
     * Sets the record ID to be stored for subsequent error messages that are logged If any previous messages are stored
     * for the current record that don't have a stored record ID, set the value for those entries to this value also.
     *
     * @param recordID - the record ID of the record currently being processed
     */
    public void setRecordID(final String recordID) {
        curRecordID = recordID;
        if (hasMissingID && errors != null) {
            setRecordIDForAll(recordID);
        }
    }

    /**
     * Sets the field tag to be stored for subsequent error messages that are logged
     *
     * @param curField - the tag of the field currently being processed
     */
    public void setCurrentField(final String curField) {
        this.curField = curField;
    }

    /**
     * Sets the subfield tag to be stored for subsequent error messages that are logged
     *
     * @param curSubfield - the subfield tag of the subfield currently being processed
     */
    public void setCurrentSubfield(final String curSubfield) {
        this.curSubfield = curSubfield;
    }
}
