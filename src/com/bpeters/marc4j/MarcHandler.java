package com.bpeters.marc4j;

import com.bpeters.marc.Leader;

/**
 * <p>Defines Java callbacks to handle MARC records.  </p>
 *
 * @author Bas Peters
 */
public interface MarcHandler {

    /**
     * <p>Receives notification at the start of the file.</p>
     */
    public abstract void startFile();

    /**
     * <p>Receives notification at the end of the file.</p>
     */
    public abstract void endFile();

    /**
     * <p>Receives notification at the start of each record.</p>
     *
     * @param leader the {@link Leader} object containing the record label
     */
    public abstract void startRecord(Leader leader);

    /**
     * <p>Receives notification at the end of each record.</p>
     */
    public abstract void endRecord();

    /**
     * <p>Receives notification of a control field.</p>
     *
     * @param tag the tag name
     * @param data the control field data
     */
    public abstract void controlField(String tag, char[] data);

    /**
     * <p>Receives notification at the start of each data field.</p>
     *
     * @param tag the tag name
     * @param ind1 the first indicator value
     * @param ind2 the second indicator value
     */
    public abstract void startDataField(String tag, char ind1, char ind2);

    /**
     * <p>Receives notification at the end of each data field</p>
     *
     * @param tag the tag name
     */
    public abstract void endDataField(String tag);

    /**
     * <p>Receives notification of a data element (subfield).</p>
     *
     * @param code the data element identifier
     * @param data the data element
     */
    public abstract void subfield(char identifier, char[] data);

}
