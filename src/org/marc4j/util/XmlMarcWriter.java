// $Id: XmlMarcWriter.java,v 1.16 2003/03/23 12:06:49 bpeters Exp $
/**
 * Copyright (C) 2002 Bas Peters (mail@bpeters.com)
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
package org.marc4j.util;

import java.io.*;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXNotRecognizedException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import org.marc4j.marcxml.SaxErrorHandler;
import org.marc4j.marcxml.MarcXmlHandler;
import org.marc4j.helpers.RecordBuilder;
import org.marc4j.MarcHandler;
import org.marc4j.marcxml.MarcResult;
import org.marc4j.marcxml.Converter;

/**
 * <p>Provides a driver for <code>MarcXmlHandler</code> 
 * to convert MARCXML to MARC tape format (ISO 2709) either 
 * by providing a MARCXML document or by pre-processing a
 * different XML format by using an XSLT stylesheet that 
 * outputs a well-formed MARCXML document.   </p>
 *
 * <p>For usage, run from the command-line with the following command:</p>
 * <p><code>java org.marc4j.util.XmlMarcWriter -usage</code></p>
 *
 * <p><b>Note:</b> this class requires a JAXP compliant SAX2 parser.
 * For W3C XML Schema support a JAXP 1.2 compliant parser is needed.</p> 
 *
 * <p>Check the home page for <a href="http://www.loc.gov/standards/marcxml/">
 * MARCXML</a> for more information about the MARCXML format.</p>
 *
 * @author <a href="mailto:mail@bpeters.com">Bas Peters</a> 
 * @version $Revision: 1.16 $
 *
 * @see MarcXmlHandler
 * @see MarcWriter
 * @see Converter
 */
public class XmlMarcWriter {

    private static final String JAXP_SCHEMA_LANGUAGE =
        "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    private static final String W3C_XML_SCHEMA =
        "http://www.w3.org/2001/XMLSchema";

    private static final String JAXP_SCHEMA_SOURCE =
        "http://java.sun.com/xml/jaxp/properties/schemaSource";

    static public void main(String[] args) {
        String input = null;
	String output = null;
	String stylesheet = null;
        String schemaSource = null;
	String convert = null;
	boolean ansel = false;
        boolean dtdValidate = false;
        boolean xsdValidate = false;
	long start = System.currentTimeMillis();

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-dtd")) {
                dtdValidate = true;
            } else if (args[i].equals("-xsd")) {
                xsdValidate = true;
            } else if (args[i].equals("-xsdss")) {
                if (i == args.length - 1) {
                    usage();
                }
                xsdValidate = true;
                schemaSource = args[++i];
            } else if (args[i].equals("-out")) {
                if (i == args.length - 1) {
                    usage();
                }
                output = args[++i];
            } else if (args[i].equals("-convert")) {
                if (i == args.length - 1) {
                    usage();
                }
                convert = args[++i].trim();
            } else if (args[i].equals("-xsl")) {
                if (i == args.length - 1) {
                    usage();
                }
                stylesheet = args[++i];
            } else if (args[i].equals("-usage")) {
                usage();
            } else if (args[i].equals("-help")) {
                usage();
            } else {
                input = args[i];

                // Must be last arg
                if (i != args.length - 1) {
                    usage();
                }
            }
        }
        if (input == null) {
            usage();
        }

	try {
	    Writer writer;
	    if (output == null) {
		if (convert != null)
		    writer = new BufferedWriter(new OutputStreamWriter(System.out, "ISO8859_1"));
		else
		    writer = new BufferedWriter(new OutputStreamWriter(System.out, "UTF8"));
	    } else {
		if (convert != null)
		    writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(output), "ISO8859_1"));
		else
		    writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(output), "UTF8"));
	    }
	    MarcWriter handler = new MarcWriter(writer);
	    if (convert != null) {
		CharacterConverter charconv = null;
		if ("ANSEL".equals(convert))
		    charconv = new UnicodeToAnsel();
		else if ("ISO5426".equals(convert))
		    charconv = new UnicodeToIso5426();
		else if ("ISO6937".equals(convert))
		    charconv = new UnicodeToIso6937();
		else {
		    System.err.println("Unknown character set");
		    System.exit(1);
		}
	    	handler.setCharacterConverter(charconv);
	    }

	    SAXParserFactory factory = SAXParserFactory.newInstance();
	    factory.setNamespaceAware(true);
	    factory.setValidating(dtdValidate || xsdValidate);
	    SAXParser saxParser = factory.newSAXParser();
	    if (xsdValidate)
                saxParser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
	    if (schemaSource != null)
		saxParser.setProperty(JAXP_SCHEMA_SOURCE, new File(schemaSource));
	    XMLReader xmlReader = saxParser.getXMLReader();
	    xmlReader.setErrorHandler(new SaxErrorHandler());
	    InputSource in = new InputSource(new File(input).toURL().toString());

	    Source source = new SAXSource(xmlReader, in);
	    Result result = new MarcResult(handler);
	    Converter converter = new Converter();
	    if (stylesheet != null) {
		Source style = new StreamSource(new File(stylesheet).toURL().toString());
		converter.convert(style, source, result);
	    } else {
		converter.convert(source, result);
	    }

	} catch (ParserConfigurationException e) {	    
	    e.printStackTrace();
	} catch (SAXNotSupportedException e) {
	    e.printStackTrace();
	} catch (SAXNotRecognizedException e) {
	    e.printStackTrace();
	} catch (SAXException e) {
	    e.printStackTrace();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	System.err.println("Total time: " + (System.currentTimeMillis() - start) + " miliseconds");
    }

    private static void usage() {
	System.err.println("MARC4J version beta 7, Copyright (C) 2002-2003 Bas Peters");
        System.err.println("Usage: org.marc4j.util.XmlMarcWriter [-options] <file.xml>");
        System.err.println("Usage: MarcXmlWriter [-options] <file.xml>");
        System.err.println("       -dtd = DTD validation");
        System.err.println("       -xsd = W3C XML Schema validation: hints in instance document");
        System.err.println("       -xsdss <file> = W3C XML Schema validation using schema source <file>");
        System.err.println("       -xsl <file> = Preprocess XML using XSLT stylesheet <file>");
        System.err.println("       -out <file> = Output using <file>");
        System.err.println("       -convert [ANSEL | ISO5426 | ISO6937] = convert from UTF-8");
	System.err.println("          to specified character set");
        System.err.println("       -usage or -help = this message");
	System.err.println("See http://marc4j.tigris.org for more information.");
        System.exit(1);
    }
   
}
