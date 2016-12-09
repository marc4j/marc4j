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
import java.io.InputStream;
import java.net.URI;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.marc4j.MarcException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * <p>
 * <code>ReverseCodeTableHash</code> defines a data structure to facilitate
 * UnicodeToAnsel character conversion.
 * </p>
 * 
 * @author Corey Keith
 */
public class ReverseCodeTableHash extends ReverseCodeTable {

    protected static Hashtable<Character, Hashtable<Integer, char[]>> charsets = null;

    protected static Vector<Character> combining = null;

    /**
     * Returns <code>true</code> if the supplied {@link Character} is a
     * combining character; else, <code>false</code>.
     * 
     * @param c - the character to test
     * @return Returns <code>true</code> if combining
     */
    @Override
    public boolean isCombining(final Character c) {
        return combining.contains(c);
    }

    /**
     * Gets the character table for the supplied {@link Character}.
     * 
     * @param c - the character to lookup
     * @return The character table for the supplied {@link Character}
     */
    @Override
    public Hashtable<Integer, char[]> getCharTable(final Character c) {
        return charsets.get(c);
    }

    /**
     * Creates a reverse codetable hash from the supplied {@link InputStream}.
     * 
     * @param byteStream - a Stream to read to create the ReverseCodeTable
     */
    public ReverseCodeTableHash(final InputStream byteStream) {
        try {
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            final SAXParser saxParser = factory.newSAXParser();
            final XMLReader rdr = saxParser.getXMLReader();

            final InputSource src = new InputSource(byteStream);

            final ReverseCodeTableHandler saxUms = new ReverseCodeTableHandler();

            rdr.setContentHandler(saxUms);
            rdr.parse(src);

            charsets = saxUms.getCharSets();
            combining = saxUms.getCombiningChars();

        } catch (final Exception e) {
            throw new MarcException(e.getMessage(), e);
        }

    }

    /**
     * Creates a reverse codetable hash from the supplied file name.
     * 
     * @param filename - the name of a file to read to create the ReverseCodeTable
     */
    public ReverseCodeTableHash(final String filename) {
        try {
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            final SAXParser saxParser = factory.newSAXParser();
            final XMLReader rdr = saxParser.getXMLReader();

            final File file = new File(filename);
            final InputSource src = new InputSource(new FileInputStream(file));

            final ReverseCodeTableHandler saxUms = new ReverseCodeTableHandler();

            rdr.setContentHandler(saxUms);
            rdr.parse(src);

            charsets = saxUms.getCharSets();
            combining = saxUms.getCombiningChars();

        } catch (final Exception e) {
            throw new MarcException(e.getMessage(), e);
        }
    }

    /**
     * Creates a reverse codetable hash from the supplied {@link URI}.
     * 
     * @param uri - a URI to access to read data to create the ReverseCodeTable
     */
    public ReverseCodeTableHash(final URI uri) {
        try {
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            final SAXParser saxParser = factory.newSAXParser();
            final XMLReader rdr = saxParser.getXMLReader();

            final InputSource src = new InputSource(uri.toURL().openStream());

            final ReverseCodeTableHandler saxUms = new ReverseCodeTableHandler();

            rdr.setContentHandler(saxUms);
            rdr.parse(src);

            charsets = saxUms.getCharSets();
            combining = saxUms.getCombiningChars();

        } catch (final Exception e) {
            throw new MarcException(e.getMessage(), e);
        }
    }

}
