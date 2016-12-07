/**
 * Copyright (C) 2004 Bas Peters
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
 *
 */

package org.marc4j;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * Creates <code>Record</code> objects from SAX events and pushes each item
 * onto the top of the <code>RecordStack</code>. Used by
 * <code>MarcXmlParserThread</code>.
 * 
 * This class requires a JAXP compliant XML parser and XSLT processor. The
 * underlying SAX2 parser should be namespace aware.
 * 
 * @author Bas Peters
 */
public class MarcXmlParser {

    private ContentHandler handler = null;

    /**
     * Default constructor.
     * 
     * @param handler the <code>MarcXmlHandler</code> object
     */
    public MarcXmlParser(final MarcXmlHandler handler) {
        this.handler = handler;
    }

    /**
     * Calls the parser.
     * 
     * @param input the input source
     */
    public void parse(final InputSource input) {
        parse(handler, input);
    }

    /**
     * Calls the parser and tries to transform the source into MARCXML using the
     * given stylesheet source before creating <code>Record</code> objects.
     * 
     * @param input the input source
     * @param th the transformation content handler
     */
    public void parse(final InputSource input, final TransformerHandler th) {
        final SAXResult result = new SAXResult();
        result.setHandler(handler);
        th.setResult(result);
        parse(th, input);

    }

    private void parse(final ContentHandler handler, final InputSource input) {
        final SAXParserFactory spf = SAXParserFactory.newInstance();
        XMLReader reader = null;
        try {
            reader = spf.newSAXParser().getXMLReader();
            reader.setFeature("http://xml.org/sax/features/namespaces", true);
            reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            reader.setContentHandler(handler);
            reader.parse(input);
        } catch (final Exception e) {
            throw new MarcException("Unable to parse input", e);
        }
    }

}
