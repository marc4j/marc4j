package com.bpeters.marc4j.helpers;

import com.bpeters.marc4j.MarcHandler;
import com.bpeters.marc4j.MarcReader;
import com.bpeters.marc4j.MarcReaderException;

import com.bpeters.marc.*;

/**
 * <p>Creates record objects from {@link MarcHandler} events and sends 
 * the record objects to a {@link RecordHandler} object.   </p>
 *
 * @author Bas Peters
 */
public class RecordBuilder implements MarcHandler {

    /** The RecordHandler object. */
    protected RecordHandler recordHandler;

    /** Record object */
    protected static Record record;

    /** Data field object */
    protected static DataField datafield;

    /**
     * <p>Registers the <code>RecordHandler</code> object.  </p>
     *
     * @param recordHandler the record handler object
     */
    public void setRecordHandler(RecordHandler recordHandler) {
	    this.recordHandler = recordHandler;
    }

    /**
     * <p>Reports the start of the file.  </p>
     */
    public void startFile() {
        if (recordHandler != null)
            recordHandler.startFile();
    }

    /**
     * <p>Creates a new record object.  </p>
     */
    public void startRecord(Leader leader) {
        this.record = new Record();
        record.setLeader(leader);
    }

    /**
     * <p>Adds a control field to the record object.  </p>
     */
    public void controlField(String tag, char[] data) {
    	record.add(new ControlField(tag, data));
    }

    /**
     * <p>Creates a new data field object.  </p>
     */
    public void startDataField(String tag, char ind1, char ind2) {
	    datafield = new DataField(tag, ind1, ind2);
    }

    /**
     * <p>Adds a subfield to the data field.  </p>
     */
    public void subfield(char identifier, char[] data) {
	    datafield.add(new Subfield(identifier, data));
    }

    /**
     * <p>Adds a data field to the record object.  </p>
     */
    public void endDataField(String tag) {
	    record.add(datafield);
    }

    /**
     * <p>Reports the end of a record and sets the record object.  </p>
     */
    public void endRecord() {
        if (recordHandler != null)
            recordHandler.record(record);
    }

    /**
     * <p>Reports the end of the file.  </p>
     */
    public void endFile() {
        if (recordHandler != null)
            recordHandler.endFile();
    }

}
