/**
 * Copyright (C) 2002 Bas Peters (mail@bpeters.com)
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

import java.io.Writer;
import java.io.IOException;
import org.marc4j.marc.Record;
import org.marc4j.marc.MarcException;

/**
 * <p>This class implements the <code>RecordHandler</code> interface
 * to write record objects to tape format (ISO 2709).</p>
 *
 * @author Bas Peters
 * @see RecordHandler
 */
public class RecordMarshaller implements RecordHandler {

    /** The Writer object */
    private Writer out;

    /**
     * <p>Default constructor.</p>
     *
     */
    public RecordMarshaller() {}

    /**
     * <p>Creates a new instance and registers the Writer object.</p>
     *
     * @param out the {@link Writer} object
     */
    public RecordMarshaller(Writer out) {
	setWriter(out);
    }

    /**
     * <p>Registers the Writer object.</p>
     *
     * @param out the {@link Writer} object
     */
    public void setWriter(Writer out) {
	this.out = out;
    }

    /**
     * <p>System exits when the Writer object is null.</p>
     *
     */
    public void startCollection() {
	if (out == null)
	    System.exit(0);
    }

    /**
     * <p>Writes each record in MARC tape format (ISO 2709).</p>
     *
     * @param record the {@link Record} object
     */
    public void record(Record record) {
	try {
	    rawWrite(record.marshal());
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (MarcException e) {
	    e.printStackTrace();
	}
    }

    public void endCollection() {
	try {
	    out.flush();
	    out.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void rawWrite(String s) throws IOException {
	out.write(s);
    }

}
