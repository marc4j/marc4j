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

import java.io.*;
import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.AttributesImpl;
import org.marc4j.MarcHandler;
import org.marc4j.marc.MarcConstants;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Tag;

/**
 * <p>This is a <code>MarcHandler</code> that produces SAX2 events 
 * from MARC records.   </p> 
 * <p>The XML structure follows the 
 * <a href="http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd">MARC21 XML Schema</a> 
 * as published by the Library of Congress.</p>
 *
 * @author Bas Peters
 */
public class MarcXmlProducer extends XMLFilterImpl implements MarcHandler {

    /** Namespace and XML Schema location */
    private static final String NS_URI = 
	"http://www.loc.gov/MARC21/slim";
    private static final String NS_XSI = 
	"http://www.w3.org/2001/XMLSchema-instance";
    private static final String schemaLocation = 
	"http://www.loc.gov/MARC21/slim " +  
	"http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd";

    private boolean schema = false;

    /** ContentHandler object */
    private ContentHandler contentHandler;

    /**
     * <p>If true the schema location and the namespace 
     * declarations are added.</p>
     *
     * @param schema the boolean value
     */
    public void setSchemaLocation(boolean schema) {
	this.schema = schema;
    }

    /**
     * <p>Returns the document handler being used, starts the document
     * and reports the root element.  </p>
     *
     */
    public void startFile() {
    	try {
            contentHandler = getContentHandler();
            contentHandler.startDocument();
            contentHandler.ignorableWhitespace("\n".toCharArray(), 0, 1);
	    AttributesImpl atts = new AttributesImpl();
	    // Outputting namespace declarations through the attribute object,
	    // since the startPrefixMapping refuses to output namespace declarations.
	    if (schema) {
		atts.addAttribute("", "", "xmlns", "CDATA", NS_URI);
		atts.addAttribute("", "xsi", "xmlns:xsi", "CDATA", NS_XSI);
		atts.addAttribute(NS_XSI, "schemaLocation", "xsi:schemaLocation", 
				  "CDATA", schemaLocation);
		contentHandler.startPrefixMapping("xsi", NS_XSI);
	    }
	    contentHandler.startPrefixMapping("", NS_URI);
            contentHandler.startElement(NS_URI, "collection", "collection", atts);
        } catch (SAXException se) {
            se.printStackTrace();
        }
    }

    /**
     * <p>Reports the starting element for a record and the leader node.  </p>
     *
     * @param leader the leader
     */
    public void startRecord(Leader leader) {
	try {
	    contentHandler.ignorableWhitespace("\n  ".toCharArray(), 0, 3);
	    contentHandler.startElement(NS_URI, "record", "record", new AttributesImpl());
	    contentHandler.ignorableWhitespace("\n    ".toCharArray(), 0, 5);
	    writeElement(NS_URI,"leader","leader", new AttributesImpl(), leader.marshal());
	} catch (SAXException se) {
	    se.printStackTrace();
	}
    }

    /**
     * <p>Reports a control field node (001-009).</p>
     *
     * @param tag the tag name
     * @param data the data element
     */
    public void controlField(String tag, char[] data) {
	try {
	    AttributesImpl atts = new AttributesImpl();
	    atts.addAttribute("", "tag", "tag", "CDATA", tag);
	    contentHandler.ignorableWhitespace("\n    ".toCharArray(), 0, 5);
	    writeElement(NS_URI,"controlfield","controlfield", atts, data);
	} catch (SAXException se) {
	    se.printStackTrace();
	}
    }

    /**
     * <p>Reports the starting element for a data field (010-999).</p>
     *
     * @param tag the tag name
     * @param ind1 the first indicator value
     * @param ind2 the second indicator value
     */
    public void startDataField(String tag, char ind1, char ind2) {
	try {
	    AttributesImpl atts = new AttributesImpl();
	    atts.addAttribute("", "tag", "tag", "CDATA", tag);
	    atts.addAttribute("", "ind1", "ind1", "CDATA", String.valueOf(ind1));
	    atts.addAttribute("", "ind2", "ind2", "CDATA", String.valueOf(ind2));
            contentHandler.ignorableWhitespace("\n    ".toCharArray(), 0, 5);
	    contentHandler.startElement(NS_URI,"datafield","datafield", atts);
	} catch (SAXException se) {
	    se.printStackTrace();
	}
    }

    /**
     * <p>Reports a subfield node.</p>
     *
     * @param identifier the data element identifier
     * @param data the data element
     */
    public void subfield(char code, char[] data) {
	try {
	    AttributesImpl atts = new AttributesImpl();
	    atts.addAttribute("", "code", "code", "CDATA", String.valueOf(code));
            contentHandler.ignorableWhitespace("\n      ".toCharArray(), 0, 7);
	    contentHandler.startElement(NS_URI,"subfield","subfield", atts);
	    contentHandler.characters(data,0,data.length);
	    contentHandler.endElement(NS_URI,"subfield","subfield");
	} catch (SAXException se) {
	    se.printStackTrace();
    	}
    }
    
    /**
     * <p>Reports the closing element for a data field.</p>
     *
     * @param tag the tag name
     */
    public void endDataField(String tag) {
	try {
            contentHandler.ignorableWhitespace("\n    ".toCharArray(), 0, 5);
	    contentHandler.endElement(NS_URI,"datafield","datafield");
	} catch (SAXException se) {
	    se.printStackTrace();
	}
    }

    /**
     * <p>Reports the closing element for a record.</p>
     *
     */
    public void endRecord() {
	try {
            contentHandler.ignorableWhitespace("\n  ".toCharArray(), 0, 3);
	    contentHandler.endElement(NS_URI,"record","record");
	} catch (SAXException se) {
	    se.printStackTrace();
	}
    }
    
    /**
     * <p>Reports the closing element for the root and the end a document.  </p>
     *
     */
    public void endFile() {
	try {
            contentHandler.ignorableWhitespace("\n".toCharArray(), 0, 1);
	    contentHandler.endElement(NS_URI,"collection","collection");
	    if (schema) contentHandler.endPrefixMapping("xsi");
	    contentHandler.endPrefixMapping("");
	    contentHandler.endDocument();
	} catch (SAXException e) {
	    e.printStackTrace();
	}
    }

    private void writeElement(String uri, String localName,
			      String qName, Attributes atts, String content)
        throws SAXException {
        writeElement(uri, localName, qName, atts, content.toCharArray());
    }

    private void writeElement(String uri, String localName,
			      String qName, Attributes atts, char content)
        throws SAXException {
        writeElement(uri, localName, qName, atts, String.valueOf(content).toCharArray());
    }

    private void writeElement(String uri, String localName,
			      String qName, Attributes atts, char[] content)
        throws SAXException {
        contentHandler.startElement(uri, localName, qName, atts);
        contentHandler.characters(content, 0, content.length);
        contentHandler.endElement(uri, localName, qName);
    }

}
