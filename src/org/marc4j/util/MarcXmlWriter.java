/**
 * Copyright (C) 2002 Bas Peters
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

import java.io.*;
import org.xml.sax.XMLFilter;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.InputSource;
import org.marc4j.helpers.ErrorHandlerImpl;
import org.marc4j.marcxml.DoctypeDecl;
import org.marc4j.marcxml.MarcXmlFilter;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import org.marc4j.marcxml.Converter;

/**
 * <p>Provides a driver for <code>MarcXmlFilter</code> 
 * to convert MARC records to MARCXML or to a different format using 
 * an XSLT stylesheet.    </p>
 *
 * <p>For usage, run from the command-line with the following command:</p>
 * <p><code>java org.marc4j.util.MarcXmlWriter -usage</code></p>
 *
 * <p>Check the home page for <a href="http://www.loc.gov/standards/marcxml/">
 * MARCXML</a> for more information about the MARCXML format.</p>
 *
 * <p><b>Note:</b> this class requires a JAXP compliant XSLT processor.</p> 
 *
 * @author Bas Peters
 * @see MarcXmlFilter
 */
public class MarcXmlWriter {

    /**
     * <p>Provides a static entry point.  </p>
     *
     */
    public static void main(String args[]) {
        String input = null;
        String output = null;
        String stylesheet = null;
	boolean dtd = false;
	boolean xsd = false;
	boolean convert = false;
        
	for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-xsl")) {
                if (i == args.length - 1) {
                    usage();
                }
                stylesheet = args[++i].trim();
            } else if (args[i].equals("-out")) {
                if (i == args.length - 1) {
                    usage();
                }
                output = args[++i].trim();
            } else if (args[i].equals("-dtd")) {
                dtd = true;
            } else if (args[i].equals("-xsd")) {
                xsd = true;
            } else if (args[i].equals("-convert")) {
                convert = true;
            } else if (args[i].equals("-usage")) {
                usage();
            } else if (args[i].equals("-help")) {
                usage();
            } else {
                input = args[i].trim();

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
            XMLFilter producer = new MarcXmlFilter();
	    producer.setProperty("http://marc4j.org/properties/error-handler", 
				 new ErrorHandlerImpl());
	    if (xsd)
		producer.setProperty("http://marc4j.org/properties/schema-location", 
				     "http://www.loc.gov/MARC21/slim " + 
				     "http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd");
	    if (convert)
		producer.setFeature("http://marc4j.org/features/ansel-to-unicode", true);
	    InputSource in = new InputSource(new FileReader(input));
	    Source source = new SAXSource(producer, in);
	    Writer writer;
	    if (output == null) {
		writer = new BufferedWriter(new OutputStreamWriter(System.out));
	    } else {
		writer = new BufferedWriter(new FileWriter(output));
	    }
	    Result result = new StreamResult(writer);
	    Converter converter = new Converter();
	    if (stylesheet != null) {
		Source style = new StreamSource(new File(stylesheet).toURL().toString());
		converter.convert(style, source, result);
	    } else {
		converter.convert(source, result);
	    }

	} catch (SAXNotSupportedException e) {
            e.printStackTrace(System.err);
	} catch (SAXNotRecognizedException e) {
            e.printStackTrace(System.err);
	} catch (SAXException e) {
            e.printStackTrace(System.err);
	} catch (TransformerException e) {
            e.printStackTrace(System.err);
	} catch (IOException e) {
            e.printStackTrace(System.err);
	}
    }

    private static void usage() {
        System.err.println("Usage: MarcXmlWriter [-options] <file.xml>");
        System.err.println("       -xsd = Add W3C XML Schema Location");
        System.err.println("       -xsl <file> = Perform XSLT transformation using <file>");
        System.err.println("       -out <file> = Output using <file>");
        System.err.println("       -convert = Convert ANSEL to Unicode");
        System.err.println("       -usage or -help = this message");
        System.err.println("       Without a stylesheet the program outputs well-formed XML");
        System.exit(1);
    }
}
