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
import java.io.OutputStreamWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.XMLReaderFactory;
import org.marc4j.helpers.MarcXmlConsumer;
import org.marc4j.helpers.SaxErrorHandler;

/**
 * <p>Provides a driver for <code>MarcXmlConsumer</code> 
 * to convert MARCXML records to MARC tape format (ISO 2709).</p>
 *
 * <p>Run the utility from the command-line with the following command:</p>
 * <p><code>java org.marc4j.util.XmlMarcWriter input-file [output-file]</code></p>
 *
 * <p>Note: this class requires a SAX2 parser.</p>
 *
 * @author Bas Peters
 * @see MarcXmlConsumer
 */
public class XmlMarcWriter {

    /**
     * <p>Provides a static entry point.  </p>
     *
     * <p>Arguments:</p>
     * <ul>
     * <li>First argument: input-file containing MARC records
     * <li>Second argument: output-file [optional]
     * </ul>
     *
     * @param args[] the command-line arguments
     */
    public static void main(String args[]) {
	if(args.length < 1) {
            System.out.println("Usage: XmlMarcWriter input-file [output-file]");
            return;
	}
	String infile = args[0];
        String outfile = (args.length > 1) ? args[1] : null;
        try {
	    Writer writer;
	    // Create a Writer object
            if (outfile == null) {
                writer = new BufferedWriter(new OutputStreamWriter(System.out));
            } else {
		writer = new BufferedWriter(new FileWriter(outfile));
            }
	    // Create a new instance of the RecordHandler implementation
	    RecordHandlerImpl handler = new RecordHandlerImpl(writer);
	    // Create a new SAX2 content handler object
            MarcXmlConsumer consumer = new MarcXmlConsumer();
	    // Register the record hadler instance
	    consumer.setRecordHandler(handler);
	    // Create a new parser instance
            XMLReader reader = XMLReaderFactory.createXMLReader();
	    // Register the content handler
            reader.setContentHandler(consumer);
	    // Register the error handler
            reader.setErrorHandler(new SaxErrorHandler());
	    // Send the input file to the parser
            reader.parse(new File(infile).toURL().toString());
        } catch (SAXParseException e) {
	    System.err.print(SaxErrorHandler.printParseException("FATAL", e));
        } catch (SAXException e) {
	    e.printStackTrace(System.err);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}
