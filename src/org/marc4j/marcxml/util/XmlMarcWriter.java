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
package org.marc4j.marcxml.util;

import java.io.*;
import javax.xml.parsers.SAXParser;
import org.xml.sax.InputSource;
import org.marc4j.marcxml.MarcXmlConsumer;

/**
 * <p>Provides a driver for <code>MarcXmlConsumer</code> 
 * to convert a MARCXML document to MARC tape format (ISO 2709).   </p>
 *
 * <p>For usage, run from the command-line with the following command:</p>
 * <p><code>java org.marc4j.util.XmlMarcWriter -help</code></p>
 *
 * <p><b>Note:</b> this class requires a JAXP compliant SAX2 parser.
 * For W3C XML Schema support a JAXP 1.2 compliant parser is needed.</p> 
 *
 * <p>Check the home page for <a href="http://www.loc.gov/standards/marcxml/">
 * MARCXML</a> for more information about the MARCXML format.</p>
 *
 * @author Bas Peters
 * @see MarcXmlConsumer
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
        String schemaSource = null;
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
	    MarcXmlConsumer consumer = new MarcXmlConsumer();
	    consumer.newSaxParser(true, true);
	    SAXParser saxParser = consumer.getSaxParser();

	    // Set the schema language if necessary
	    if (xsdValidate)
                saxParser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);

	    // Set the schema source, if any.
	    if (schemaSource != null)
		saxParser.setProperty(JAXP_SCHEMA_SOURCE, 
				      new File(schemaSource));

	    // Create a Writer.
	    Writer writer;
	    if (output == null) {
		writer = new BufferedWriter(new OutputStreamWriter(System.out));
	    } else {
		writer = new BufferedWriter(new FileWriter(output));
	    }
	    
	    // Parse the file
	    InputSource inputSource = new InputSource(new FileReader(input));
	    consumer.parse(inputSource, writer);

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
        System.err.println("       -out <file> = Output using <file>");
        System.err.println("       -usage or -help = this message");
        System.exit(1);
    }
   
}
