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
 * to convert MARC records to a MARCXML collection document or to a different format using 
 * an XSLT stylesheet.    </p>
 *
 * <p>For usage, run the command-line with the following command:</p>
 * <p><code>java org.marc4j.util.MarcXmlWriter -help</code></p>
 *
 * <p>Check the home page for <a href="http://www.loc.gov/standards/marcxml/">
 * MARCXML</a> for more information about the MARCXML format.</p>
 *
 * <p><b>Note:</b> this class requires a JAXP compliant XSLT processor.</p> 
 *
 * @author Bas Peters
 * @see MarcXmlProducer
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
            TransformerFactory tf = TransformerFactory.newInstance();
            if (! tf.getFeature(SAXTransformerFactory.FEATURE)) {
                System.err.println("SAXTransformerFactory is not supported.");
                System.exit(1);
            }
            SAXTransformerFactory saxtf = (SAXTransformerFactory)tf;
            TransformerHandler th = null;
            if (stylesheet == null) {
                th = saxtf.newTransformerHandler();
            } else {
                th = saxtf.newTransformerHandler(
		    new StreamSource(new File(stylesheet).toURL().toString()));
            }

            // Set the destination for the XSLT transformer
	    if (output == null) {
		th.setResult(new StreamResult(System.out));
	    } else {
		FileOutputStream fos = new FileOutputStream(output);
		Writer w = new OutputStreamWriter(fos, "UTF-8");
		th.setResult(new StreamResult(w));
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
	    marcReader.parse(input);

	} catch (TransformerConfigurationException e) {
            e.printStackTrace(System.err);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private static void usage() {
        System.err.println("Usage: MarcXmlWriter [-options] <file.xml>");
        System.err.println("       -xsl <file> = Perform XSLT transformation using <file>");
        System.err.println("       -out <file> = Output using <file>");
        System.err.println("       -usage or -help = this message");
        System.err.println("       Without a stylesheet the program outputs XML");
        System.exit(1);
    }
}
