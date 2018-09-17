/**
 * Copyright (C) 2018
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

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Hashtable;
import java.util.Vector;

/**
 * <p>
 * <code>ReverseCodeTable</code> defines a data structure to facilitate
 * UnicodeToUnimarc character conversion.
 * </p>
 *
 * @author SirsiDynix from Corey Keith
 *
 * @see DefaultHandler
 */
public class UnimarcReverseCodeTable {
    protected static Hashtable charset = null;

    protected static Vector combining = null;

    public boolean isCombining(Character c) {
        return combining.contains(c);
    }

    public Hashtable codeTableHash(Character c) {
        Hashtable chars = (Hashtable) charset.get(c);
        if (chars == null) {
            System.err.println("Not Found: " + c);
            return null;
        } else {
            return chars;
        }
    }

    public static boolean inPreviousCharCodeTable(Character c, CodeTableTracker ctt) {
        Hashtable chars = (Hashtable) charset.get(c);
        if (chars == null) {
            System.out.println("Not Found: " + c);
            return false;
        } else {

            if ((chars.get(ctt.getPrevious(CodeTableTracker.G0)) != null)
                || (chars.get(ctt.getPrevious(CodeTableTracker.G1)) != null)) {
                ctt.makePreviousCurrent();
                return true;
            } else {
                return false;
            }

        }
    }

    public static char getChar(Character c, CodeTableTracker ctt) {
        Hashtable chars = (Hashtable) charset.get(c);

        Integer marc = (Integer) chars.get(ctt.getCurrent(CodeTableTracker.G0));

        if (marc != null) {
            return (char) marc.intValue();
        }
        marc = (Integer) chars.get(ctt.getCurrent(CodeTableTracker.G1));
        if (marc != null) {
            return (char) marc.intValue();
        }
        return 0x20;
    }

    public UnimarcReverseCodeTable(InputStream byteStream) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            SAXParser saxParser = factory.newSAXParser();
            XMLReader rdr = saxParser.getXMLReader();

            InputSource src = new InputSource(byteStream);

            ReverseCodeTableHandler saxUms = new ReverseCodeTableHandler();

            rdr.setContentHandler(saxUms);
            rdr.parse(src);

            charset = saxUms.getCharSets();
            combining = saxUms.getCombiningChars();

        } catch (Exception exc) {
            exc.printStackTrace(System.out);
            System.err.println("Exception: " + exc);
        }
    }

    public UnimarcReverseCodeTable(String filename) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            SAXParser saxParser = factory.newSAXParser();
            XMLReader rdr = saxParser.getXMLReader();

            File file = new File(filename);
            InputSource src = new InputSource(new FileInputStream(file));

            ReverseCodeTableHandler saxUms = new ReverseCodeTableHandler();

            rdr.setContentHandler(saxUms);
            rdr.parse(src);

            charset = saxUms.getCharSets();
            combining = saxUms.getCombiningChars();

        } catch (Exception exc) {
            exc.printStackTrace(System.out);
            System.err.println("Exception: " + exc);
        }
    }

    public UnimarcReverseCodeTable(URI uri) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            SAXParser saxParser = factory.newSAXParser();
            XMLReader rdr = saxParser.getXMLReader();

            InputSource src = new InputSource(uri.toURL().openStream());

            ReverseCodeTableHandler saxUms = new ReverseCodeTableHandler();

            rdr.setContentHandler(saxUms);
            rdr.parse(src);

            charset = saxUms.getCharSets();
            combining = saxUms.getCombiningChars();

        } catch (Exception exc) {
            exc.printStackTrace(System.out);
            System.err.println("Exception: " + exc);
        }
    }
}

