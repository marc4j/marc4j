package com.bpeters.marc4j.helpers;

import com.bpeters.marc.Record;

/**
 * <p>Defines a set of Java callbacks to handle {@link Record} objects. </p>
 *
 * @author Bas Peters
 */
public interface RecordHandler {

    /**
     * <p>Receives notification at the start of the file.  </p>
     */
    public void startFile();


    /**
     * <p>Receives notification when a record is parsed.  </p>
     *
     * @param record the {@link Record} object.
     */
    public void record(Record record);

    /**
     * <p>Receives notification at the end of the file.</p>
     */
    public void endFile();

}
