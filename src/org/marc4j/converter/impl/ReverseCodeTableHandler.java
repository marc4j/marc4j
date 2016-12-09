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

import java.util.Hashtable;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>
 * <code>ReverseCodeTableHandler</code> is a SAX2 <code>ContentHandler</code> that builds a data structure to facilitate
 * <code>UnicodeToAnsel</code> character conversion.
 *
 * @author Corey Keith
 * @see DefaultHandler
 */
public class ReverseCodeTableHandler extends DefaultHandler {

    private Hashtable<Character, Hashtable<Integer, char[]>> charsets;

    private Vector<Character> combiningchars;

    /** Data element identifier */
    private Integer isocode;

    private char[] marc;

    private Character ucs;

    private Character altucs;

    private boolean combining;

    /** StringBuffer to store data */
    private StringBuffer data;

    /** Locator object */
    protected Locator locator;

    /**
     * Gets character sets.
     *
     * @return The character sets
     */
    public Hashtable<Character, Hashtable<Integer, char[]>> getCharSets() {
        return charsets;
    }

    /**
     * Gets the combining characters.
     *
     * @return The combining characters
     */
    public Vector<Character> getCombiningChars() {
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

    @Override
    public void startElement(final String uri, final String name, final String qName, final Attributes atts)
            throws SAXParseException {
        if (name.equals("characterSet")) {
            isocode = Integer.valueOf(atts.getValue("ISOcode"), 16);
        } else if (name.equals("marc")) {
            data = new StringBuffer();
        } else if (name.equals("codeTables")) {
            charsets = new Hashtable<Character, Hashtable<Integer, char[]>>();
            combiningchars = new Vector<Character>();
        } else if (name.equals("ucs")) {
            data = new StringBuffer();
        } else if (name.equals("alt")) {
            data = new StringBuffer();
        } else if (name.equals("code")) {
            ucs = null;
            altucs = null;
            combining = false;
        } else if (name.equals("isCombining")) {
            data = new StringBuffer();
        }
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) {
        if (data != null) {
            data.append(ch, start, length);
        }
    }

    @Override
    public void endElement(final String uri, final String name, final String qName) throws SAXParseException {
        if (name.equals("marc")) {
            final String marcstr = data.toString();

            if (marcstr.length() == 6) {
                marc = new char[3];
                marc[0] = (char) Integer.parseInt(marcstr.substring(0, 2), 16);
                marc[1] = (char) Integer.parseInt(marcstr.substring(2, 4), 16);
                marc[2] = (char) Integer.parseInt(marcstr.substring(4, 6), 16);
            } else {
                marc = new char[1];
                marc[0] = (char) Integer.parseInt(marcstr, 16);
            }
        } else if (name.equals("ucs")) {
            if (data.length() > 0) {
                ucs = new Character((char) Integer.parseInt(data.toString(), 16));
            }
        } else if (name.equals("alt")) {
            if (data.length() > 0) {
                altucs = new Character((char) Integer.parseInt(data.toString(), 16));
            }
        } else if (name.equals("code")) {
            if (combining) {
                if (ucs != null) {
                    combiningchars.add(ucs);
                }

                if (altucs != null) {
                    combiningchars.add(altucs);
                }
            }

            if (ucs != null) {
                if (charsets.get(ucs) == null) {
                    final Hashtable<Integer, char[]> h = new Hashtable<Integer, char[]>(1);
                    h.put(isocode, marc);
                    charsets.put(ucs, h);
                } else {
                    final Hashtable<Integer, char[]> h = charsets.get(ucs);
                    h.put(isocode, marc);
                }
            }

            if (altucs != null) {
                if (charsets.get(altucs) == null) {
                    final Hashtable<Integer, char[]> h = new Hashtable<Integer, char[]>(1);

                    h.put(isocode, marc);
                    charsets.put(altucs, h);
                } else {
                    final Hashtable<Integer, char[]> h = charsets.get(altucs);

                    if (!h.containsKey(isocode)) {
                        h.put(isocode, marc);
                    }
                }
            }
        } else if (name.equals("isCombining")) {
            if (data.toString().equals("true")) {
                combining = true;
            }
        }

        data = null;
    }

}
