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
package org.marc4j.marcxml;

import java.io.Writer;
import java.io.IOException;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;
import org.marc4j.helpers.RecordHandlerImpl;
import org.marc4j.marcxml.SaxErrorHandler;
import org.marc4j.marcxml.MarcXmlHandler;

public class MarcXmlConsumer {

    private SAXParser saxParser;

    public MarcXmlConsumer() {}

    public void newSaxParser(boolean namespaceAware, boolean validate  )
	throws ParserConfigurationException, SAXException {

	// Create a JAXP SAXParserFactory instance
	SAXParserFactory factory = SAXParserFactory.newInstance();

	// JAXP is not by default namespace aware
	factory.setNamespaceAware(namespaceAware);

	// Configure the validation
	factory.setValidating(validate);

	// Create a JAXP SAXParser
	saxParser = factory.newSAXParser();
    }

    public SAXParser getSaxParser() {
	return saxParser;
    }

    public void parse(InputSource inputSource, Writer writer)
	throws IOException, SAXException {
    
	// Create a new instance of the RecordHandler implementation.
	RecordHandlerImpl handler = new RecordHandlerImpl(writer);
		
	// Create a new SAX2 consumer.
	MarcXmlHandler consumer = new MarcXmlHandler();

	// Set the record handler.
	consumer.setRecordHandler(handler);

	// Get the encapsulated SAX XMLReader.
	XMLReader xmlReader = saxParser.getXMLReader();

	// Set the ContentHandler of the XMLReader.
	xmlReader.setContentHandler(consumer);

	// Set the ErrorHandler of the XMLReader.
	xmlReader.setErrorHandler(new SaxErrorHandler());

	// Parse the XML document
	xmlReader.parse(inputSource);
    }
}
