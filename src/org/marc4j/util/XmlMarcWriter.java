// $Id: XmlMarcWriter.java,v 1.12 2002/07/09 20:26:43 bpeters Exp $
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
package org.marc4j.util;

import java.io.Writer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
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
 * @version $Revision: 1.12 $
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
	boolean convert = false;
        boolean dtdValidate = false;
        boolean xsdValidate = false;

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
                convert = true;
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
		if (convert)
		    writer = new BufferedWriter(new OutputStreamWriter(System.out));
		else
		    writer = new BufferedWriter(new OutputStreamWriter(System.out, "UTF8"));
	    } else {
		if (convert)
		    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
		else
		    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF8"));
	    }
	    MarcWriter handler = new MarcWriter(writer);
	    if (convert)
		handler.setUnicodeToAnsel(true);

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
    }

    private static void usage() {
        System.err.println("Usage: MarcXmlWriter [-options] <file.xml>");
        System.err.println("       -dtd = DTD validation");
        System.err.println("       -xsd | -xsdss <file.xsd> = W3C XML Schema validation using xsi: hints");
        System.err.println("           in instance document or schema source <file.xsd>");
        System.err.println("       -xsdss <file> = W3C XML Schema validation using schema source <file>");
        System.err.println("       -xsl <file> = Preprocess XML using XSLT stylesheet <file>");
        System.err.println("       -out <file> = Output using <file>");
        System.err.println("       -convert = Convert UTF-8 to ANSEL");
        System.err.println("       -usage or -help = this message");
        System.exit(1);
    }
   
}
