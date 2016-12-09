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
import java.util.HashMap;
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
 * <code>CodeTableHandler</code> is a SAX2 <code>ContentHandler</code> that
 * builds a data structure to facilitate AnselToUnicode character conversion.
 * 
 * @author Corey Keith
 * @see DefaultHandler
 */
public class CodeTableHandler extends DefaultHandler {

    private HashMap<Integer, HashMap<Integer, Character>> sets;

    private HashMap<Integer, Character> charset;

    private HashMap<Integer, Vector<Integer>> combiningchars;

    /** Data element identifier */
    private Integer isocode;

    private Integer marc;

    private Character ucs;

    private boolean useAlt = false;

    private boolean iscombining;

    private Vector<Integer> combining;

    /** Tag name */
    // private String tag;

    /** StringBuffer to store data */
    private StringBuffer data;

    /** Locator object */
    protected Locator locator;

    /**
     * Gets the character sets hashtable.
     * 
     * @return The character sets
     */
    public HashMap<Integer, HashMap<Integer, Character>> getCharSets() {
        return sets;
    }

    /**
     * Gets the combining characters.
     * 
     * @return The combining characters
     */
    public HashMap<Integer, Vector<Integer>> getCombiningChars() {
        return combiningchars;
    }

    /**
     * <p>
     * Registers the SAX2 <code>Locator</code> object.
     * </p>
     * 
     * @param locator the {@link Locator}object
     */
    @Override
    public void setDocumentLocator(final Locator locator) {
        this.locator = locator;
    }

    /**
     * An event fired at the start of an element.
     * 
     * @param uri - the uri
     * @param name - the name
     * @param qName - the qname
     * @param atts - the attributes
     * @throws SAXParseException - when there is an exception
     */
    @Override
    public void startElement(final String uri, final String name, final String qName,
            final Attributes atts) throws SAXParseException {
        if (name.equals("characterSet")) {
            charset = new HashMap<Integer, Character>();
            isocode = Integer.valueOf(atts.getValue("ISOcode"), 16);
            combining = new Vector<Integer>();
        } else if (name.equals("marc")) {
            data = new StringBuffer();
        } else if (name.equals("codeTables")) {
            sets = new HashMap<Integer, HashMap<Integer, Character>>();
            combiningchars = new HashMap<Integer, Vector<Integer>>();
        } else if (name.equals("ucs")) {
            data = new StringBuffer();
        } else if (name.equals("alt")) {
            data = new StringBuffer();
        } else if (name.equals("isCombining")) {
            data = new StringBuffer();
        } else if (name.equals("code")) {
            iscombining = false;
        }
    }

    /**
     * An event fired as characters are consumed.
     * 
     * @param ch - the array of characters that was found
     * @param start - the starts point in ch for where to start copying data
     * @param length - the number of characters to copy
     */
    @Override
    public void characters(final char[] ch, final int start, final int length) {
        if (data != null) {
            data.append(ch, start, length);
        }
    }

    /**
     * An event fired at the end of parsing an element.
     * 
     * @param uri - the uri
     * @param name - the name
     * @param qName - the qname
     * @throws SAXParseException - when there is an exception
     */
    @Override
    public void endElement(final String uri, final String name, final String qName)
            throws SAXParseException {
        if (name.equals("characterSet")) {
            sets.put(isocode, charset);
            combiningchars.put(isocode, combining);
            combining = null;
            charset = null;
        } else if (name.equals("marc")) {
            marc = Integer.valueOf(data.toString(), 16);
        } else if (name.equals("ucs")) {
            if (data.length() > 0) {
                ucs = new Character((char) Integer.parseInt(data.toString(), 16));
            } else {
                ucs = null;
            }
        } else if (name.equals("alt")) {
            if (useAlt && data.length() > 0) {
                ucs = new Character((char) Integer.parseInt(data.toString(), 16));
                useAlt = false;
            }
        } else if (name.equals("code")) {
            if (iscombining) {
                combining.add(marc);
            }
            charset.put(marc, ucs);
        } else if (name.equals("isCombining")) {
            if (data.toString().equals("true")) {
                iscombining = true;
            }
        }

        data = null;
    }

    /**
     * The main class on the CodeTableHandler. FIXME needed? 
     * 
     * @param args - the command line arguments for the CodeTableHandler program
     */
    public static void main(final String[] args) {
        @SuppressWarnings("unused")
        HashMap<Integer, HashMap<Integer, Character>> charsets = null;

        try {

            final SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            final SAXParser saxParser = factory.newSAXParser();
            final XMLReader rdr = saxParser.getXMLReader();

            final File file = new File(
                    "C:\\Documents and Settings\\ckeith\\Desktop\\Projects\\Code Tables\\codetables.xml");
            final InputSource src = new InputSource(new FileInputStream(file));

            final CodeTableHandler saxUms = new CodeTableHandler();

            rdr.setContentHandler(saxUms);
            rdr.parse(src);

            charsets = saxUms.getCharSets();

            // System.out.println( charsets.toString() );
            System.out.println(saxUms.getCombiningChars());

        } catch (final Exception exc) {
            exc.printStackTrace(System.out);
            // System.err.println( "Exception: " + exc );
        }
    }
}
