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
package org.marc4j.util;

import java.io.File;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.marc4j.helpers.RecordHandler;
import org.marc4j.marc.*;

/**
 * <p>This class consumes SAX2 events from MARC21 XML documents
 * and reports events to the <code>RecordHandler</code> interface.</p>
 *
 * @author Bas Peters
 * @see RecordHandler
 */
public class MarcXmlConsumer extends DefaultHandler {

    /** Record object */
    private Record record;

    /** Leader object */
    private Leader leader;

    /** DataField object */
    private DataField dataField;

    /** ControlField object */
    private ControlField controlField;

    /** Subfield object */
    private Subfield subfield;

    /** StringBuffer object */
    private StringBuffer data;

    /** RecordHandler object. */
    private RecordHandler rh;

    /** Locator object. */
    private Locator locator;

    /**
     * <p>Registers the <code>RecordHandler</code> object.  </p>
     *
     * @param recordHandler the record handler object
     */
    public void setRecordHandler(RecordHandler rh) {
	    this.rh = rh;
    }

    /**
     * <p>Registers the SAX2 <code>Locator</code> object.  </p>
     *
     * @param locator the locator object
     */
    public void setDocumentLocator(Locator locator) {
	this.locator = locator;
    }

    public void startDocument() {
	if (rh != null)
	    rh.startFile();
    }

    public void startElement(String uri, String name, String qName, 
			     Attributes atts) throws SAXParseException {
	if (name.equals("record")) {
	    record = new Record();
	} else if (name.equals("leader")) {
	    data = new StringBuffer();
	} else if (name.equals("controlfield")) {
	    controlField = new ControlField();

	    String tag = atts.getValue("tag");
	    if (tag.length() > 0)
		controlField.setTag(tag);
	    else
		throw new SAXParseException("Attribute tag is empty", locator);

	    data = new StringBuffer();
	} else if (name.equals("datafield")) {
	    if (atts.getLength() == 0)
		throw new SAXParseException("Invalid datafield element: tag and indicators are missing", locator);
	    else if (atts.getLength() == 1)
		throw new SAXParseException("Invalid datafield element: indicators are missing", locator);
	    dataField = new DataField();

	    String tag = atts.getValue("tag");
	    if (tag.length() > 0)
		dataField.setTag(tag);
	    else
		throw new SAXParseException("Attribute tag is empty", locator);

	    String ind1 = atts.getValue("ind1");
	    String ind2 = atts.getValue("ind2");
	    if (ind1.length() == 1)
		dataField.setIndicator1(ind1.charAt(0));
	    else
		throw new SAXParseException("Attribute ind1 is empty or length is not 1", locator);
	    if (ind2.length() == 1)
		dataField.setIndicator2(ind2.charAt(0));
	    else
		throw new SAXParseException("Attribute ind2 is empty or length is not 1", locator);

	    data = new StringBuffer();

	} else if (name.equals("subfield")) {
	    subfield = new Subfield();

	    String code = atts.getValue("code");
	    if (code.length() == 1)
		subfield.setCode(code.charAt(0));
	    else
		throw new SAXParseException("Attribute code is empty or length is not 1", locator);

	    data = new StringBuffer();
	}
    }

    public void characters(char[] ch, int start, int length) {
	if (data != null) {
	  data.append(ch, start, length);
	}
    }

    public void endElement(String uri, String name, String qName) throws SAXParseException {
	if (name.equals("record")) {
	    if (rh != null)
		rh.record(record);
	} else if (name.equals("leader")) {
	    try {
		record.add(new Leader(data.toString()));
	    } catch (MarcException e) {
		throw new SAXParseException("Unable to unmarshal leader", locator);
	    }
	} else if (name.equals("controlfield")) {
	    controlField.setData(data.toString());
	    record.add(controlField);
	} else if (name.equals("datafield")) {
	    record.add(dataField);
	} else if (name.equals("subfield")) {
	    subfield.setData(data.toString());
	    dataField.add(subfield);
	}
	data = null;
    }

    public void endDocument() {
	if (rh != null)
	    rh.endFile();
    }

}
