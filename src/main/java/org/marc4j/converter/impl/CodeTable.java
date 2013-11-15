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

import org.marc4j.MarcException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * <p>
 * <code>CodeTable</code> defines a data structure to facilitate
 * <code>AnselToUnicode</code> character conversion.
 * </p>
 * 
 * @author Corey Keith
 * 
 */
public class CodeTable implements CodeTableInterface
{
    protected static HashMap<Integer, HashMap<Integer, Character>> charsets = null;

    protected static HashMap<Integer, Vector<Integer>> combining = null;

    public boolean isCombining(int i, int g0, int g1)
    {
        if (i <= 0x7E)
        {
            Vector<Integer> v = combining.get(new Integer(g0));
            return (v != null && v.contains(new Integer(i)));
        }
        else
        {
            Vector<Integer> v = combining.get(new Integer(g1));
            return (v != null && v.contains(new Integer(i)));
        }
    }

    public char getChar(int c, int mode)
    {
        if (c == 0x20)
            return (char) c;
        else
        {
            HashMap<Integer, Character> charset = charsets.get(new Integer(mode));

            if (charset == null)
            {
                // System.err.println("Hashtable not found: "
                // + Integer.toHexString(mode));
                return (char) c;
            }
            else
            {
                Character ch = charset.get(new Integer(c));
                if (ch == null)
                {
                    int newc = (c < 0x80) ? c + 0x80 : c - 0x80;
                    ch = charset.get(new Integer(newc));
                    if (ch == null)
                    {
                        // System.err.println("Character not found: "
                        // + Integer.toHexString(c) + " in Code Table: "
                        // + Integer.toHexString(mode));
                        return (char) 0;
                    }
                    else
                        return ch.charValue();
                }
                else
                    return ch.charValue();
            }
        }
    }

    public CodeTable(InputStream byteStream)
    {
        try
        {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            SAXParser saxParser = factory.newSAXParser();
            XMLReader rdr = saxParser.getXMLReader();

            InputSource src = new InputSource(byteStream);

            CodeTableHandler saxUms = new CodeTableHandler();

            rdr.setContentHandler(saxUms);
            rdr.parse(src);

            charsets = saxUms.getCharSets();
            combining = saxUms.getCombiningChars();
        }
        catch (Exception e)
        {
            throw new MarcException(e.getMessage(), e);
        }
    }

    public CodeTable(String filename)
    {
        try
        {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            SAXParser saxParser = factory.newSAXParser();
            XMLReader rdr = saxParser.getXMLReader();

            File file = new File(filename);
            InputSource src = new InputSource(new FileInputStream(file));

            CodeTableHandler saxUms = new CodeTableHandler();

            rdr.setContentHandler(saxUms);
            rdr.parse(src);

            charsets = saxUms.getCharSets();
            combining = saxUms.getCombiningChars();
        }
        catch (Exception e)
        {
            throw new MarcException(e.getMessage(), e);
        }
    }

    public CodeTable(URI uri)
    {
        try
        {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            SAXParser saxParser = factory.newSAXParser();
            XMLReader rdr = saxParser.getXMLReader();

            InputSource src = new InputSource(uri.toURL().openStream());

            CodeTableHandler saxUms = new CodeTableHandler();

            rdr.setContentHandler(saxUms);
            rdr.parse(src);

            charsets = saxUms.getCharSets();
            combining = saxUms.getCombiningChars();
        }
        catch (Exception e)
        {
            throw new MarcException(e.getMessage(), e);
        }
    }
}
