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
import org.xml.sax.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import org.marc4j.MarcReader;
import org.marc4j.helpers.ErrorHandlerImpl;
import org.marc4j.helpers.MarcXmlProducer;


/**
 * <p>Provides a driver for <code>MarcXmlProducer</code> 
 * to convert MARC records to a different format using 
 * an XSLT stylesheet.    </p>
 *
 * <p>Run the utility from the command-line with the following command:</p>
 * <p><code>java org.marc4j.util.MarcXslWriter input-file [stylesheet] [output-file]</code></p>
 *
 * <p>Note: this class requires a JAXP 1.1 compliant XSLT processor.</p> 
 *
 * @author Bas Peters
 * @see MarcXmlProducer
 */
public class MarcXslWriter {

    /**
     * <p>Provides a static entry point.  </p>
     *
     * <p>Argumnets:</p>
     * <ul>
     * <li>First argument: input-file containing MARC records
     * <li>Second argument: stylesheet [optional]
     * <li>Third argument: output-file [optional]
     * </ul>
     *
     * @param args[] the command-line arguments
     */
    public static void main(String args[]) {
	if(args.length < 1) {
            System.out.println("Usage: MarcXslWriter input-file [stylesheet] [output-file]");
            return;
	    }
        String infile = args[0];
        String xslfile = (args.length > 1) ? args[1] : null;
        String outfile = (args.length > 2) ? args[2] : null;
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            if (! tf.getFeature(SAXTransformerFactory.FEATURE)) {
                System.err.println("SAXTransformerFactory is not supported.");
                System.exit(1);
            }
            SAXTransformerFactory saxtf = (SAXTransformerFactory)tf;
            TransformerHandler th = null;
            if (xslfile == null) {
                th = saxtf.newTransformerHandler();
            } else {
                th = saxtf.newTransformerHandler(
                    new StreamSource(new File(xslfile)));
            }

            // Set the destination for the XSLT transformer
	    if (outfile == null) {
		th.setResult(new StreamResult(System.out));
	    } else {
		FileOutputStream fos = new FileOutputStream(outfile);
		Writer w = new OutputStreamWriter(fos, "UTF8");
		th.setResult(new StreamResult(System.out));
	    }

            // Create a new MarcHandler object.
            MarcXmlProducer producer = new MarcXmlProducer();
            // Attach the consumer to the handler object.
            producer.setContentHandler(th);
            // Create a new  MarcReader object.
            MarcReader marcReader = new MarcReader();
            // Register the MarcHandler implementation.
            marcReader.setMarcHandler(producer);
            // Register the ErrorHandler implementation.
            marcReader.setErrorHandler(new ErrorHandlerImpl());
            // Send the file to the parse method.
	    marcReader.parse(infile);
	} catch (TransformerConfigurationException e) {
            e.printStackTrace(System.err);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}
