/**
 * Copyright (C) 2002 Bas Peters
 *
 * This file is part of MARC4J
 *
 * MARC4J is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * MARC4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MARC4J; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * As a special exception, if you link this library with other files to
 * produce an executable, this library does not by itself cause the
 * resulting executable to be covered by the GNU General Public License.
 * This exception does not however invalidate any other reasons why the
 * executable file might be covered by the GNU General Public License.
 */
package org.marc4j.helpers;

import org.marc4j.MarcHandler;
import org.marc4j.MarcReader;
import org.marc4j.MarcReaderException;
import org.marc4j.marc.*;

/**
 * <p>Creates record objects from <code>MarcHandler</code> events and reports
 * events to the <code>RecordHandler</code>.   </p>
 *
 * @author Bas Peters
 * @see RecordHandler
 */
public class MarcRecordBuilder implements MarcHandler {

    /** The RecordHandler object. */
    private RecordHandler recordHandler;

    /** Record object */
    private static Record record;

    /** Data field object */
    private static DataField datafield;

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
        record.add(leader);
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
