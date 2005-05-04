// $Id: ReverseCodeTableHandler.java,v 1.1 2005/05/04 10:06:46 bpeters Exp $
/**
 * Copyright (C) 2002 Bas Peters
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
 */
package org.marc4j.converter.impl;

import java.io.File;
import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p><code>ReverseCodeTableHandler</code> is a SAX2 <code>ContentHandler</code>
 * that builds a data structure to facilitate <code>UnicodeToAnsel</code> character conversion.
 *
 * @author Corey Keith
 * @version $Revision: 1.1 $
 *
 * @see DefaultHandler
 */
public class ReverseCodeTableHandler extends DefaultHandler {
    private Hashtable charset;
    private Vector combiningchars;

    /** Data element identifier */
    private Integer isocode;
    private char[] marc;
    private Character ucs;
    private boolean combining;

    /** Tag name */
    private String tag;

    /** StringBuffer to store data */
    private StringBuffer data;

    /** Locator object */
    private Locator locator;

    public Hashtable getCharSets() { return charset; }
    public Vector getCombiningChars() { return combiningchars; }



    /**
     * <p>Registers the SAX2 <code>Locator</code> object.  </p>
     *
     * @param locator the {@link Locator} object
     */
    public void setDocumentLocator(Locator locator) {
	this.locator = locator;
    }

    public void startElement(String uri, String name, String qName,
			     Attributes atts) throws SAXParseException {
	if (name.equals("characterSet"))
	    isocode = Integer.valueOf(atts.getValue("ISOcode"),16);
	else if (name.equals("marc"))
	    data = new StringBuffer();
	else if (name.equals("codeTables")) {
	    charset = new Hashtable();
	    combiningchars = new Vector();
	} else if (name.equals("ucs"))
	    data = new StringBuffer();
	else if (name.equals("code"))
	    combining = false;
	else if (name.equals("isCombining"))
	    data = new StringBuffer();

    }

    public void characters(char[] ch, int start, int length) {
	if (data != null) {
	    data.append(ch, start, length);
	}
    }

    public void endElement(String uri, String name, String qName)
	throws SAXParseException {
	if (name.equals("marc")) {
	    String marcstr = data.toString();
	    if (marcstr.length() == 6) {
		marc = new char[3];
		marc[0] = (char)Integer.parseInt(marcstr.substring(0,2),16);
		marc[1] = (char)Integer.parseInt(marcstr.substring(2,4),16);
		marc[2] = (char)Integer.parseInt(marcstr.substring(4,6),16);
	    } else {
		marc = new char[1];
		marc[0] = (char)Integer.parseInt(marcstr,16);
	    }
	} else if (name.equals("ucs")) {
	    ucs = new Character((char)Integer.parseInt(data.toString(),16));
	}
	else if (name.equals("code")) {
	    if (combining) {
		combiningchars.add(ucs);
	    }

	    if (charset.get(ucs) == null) {
		Hashtable h = new Hashtable(1);
		h.put(isocode,marc);
		charset.put(ucs,h);
	    } else {
		Hashtable h = (Hashtable)charset.get(ucs);
		h.put(isocode,marc);
	    }
	} else if (name.equals("isCombining")) {
	    if (data.toString().equals("true"))
		combining = true;
	}
	data = null;
    }

    public static void main( String[] args ) {
	Hashtable charsets = null;

	try {

	    SAXParserFactory factory = SAXParserFactory.newInstance();
	    factory.setNamespaceAware(true);
	    factory.setValidating(false);
	    SAXParser saxParser = factory.newSAXParser();
	    XMLReader rdr = saxParser.getXMLReader();

	    File file = new File( "C:\\Documents and Settings\\ckeith\\Desktop\\Projects\\Code Tables\\codetables.xml" );
	    InputSource src = new InputSource( new FileInputStream( file ) );

	    ReverseCodeTableHandler saxUms = new ReverseCodeTableHandler();

	    rdr.setContentHandler( saxUms );
	    rdr.parse( src );
	}catch( Exception exc ) {
	    exc.printStackTrace(System.out);
	    System.err.println( "Exception: " + exc );
	}
    }
}
