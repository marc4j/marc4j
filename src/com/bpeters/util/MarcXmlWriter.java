package com.bpeters.util;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import com.bpeters.marc4j.MarcReader;
import com.bpeters.marc4j.helpers.ErrorHandlerImpl;


/**
 * <p><code>MarcXmlWriter</code> provides a <code>Driver</code> to convert
 * MARC records to XML using {@link MarcXmlHandler} and optionally transform
 * the result using an XSLT stylesheet.   </p>
 *
 * <p>Usage: <code>MarcXmlWriter -s [stylesheet] -o [output-file] input-file</code></p>
 *
 * <p>If you do not provide a stylesheet the records are serialized to XML
 * using the document format defined in {@link MarcXmlHandler}.</p>
 *
 * @author Bas Peters
 */
public class MarcXmlWriter {

    /**
     * <p>Provides a static entry point for <code>MarcXmlWriter</code> to
     * read a file with MARC records and serialize the file to XML or to
     * a different format using an XSLT stylesheet.  </p>
     *
     * @param args[] the command-line arguments
     */
    public static void main(String args[]) {
        if(args.length < 1) {
            System.out.println("Usage: MarcXmlWriter -s [stylesheet] -o [output-file] input-file");
            return;
	    }
        String infile = args[args.length -1].trim();
        String outfile = null;
        String xslfile = null;

        // Parse the arguments.
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-o")) {
                outfile = (args.length > (i + 1)) ? args[i + 1].trim() : null;
                i++;
            } else if (args[i].equals("-s")) {
                xslfile = (args.length > (i + 1)) ? args[i + 1].trim() : null;
                i++;
            }
        }

        try {

            // Create a TransformerFactory instance.
            TransformerFactory tf = TransformerFactory.newInstance();

            // Check if SAXTransformerFactory is supported.
            if (! tf.getFeature(SAXTransformerFactory.FEATURE)) {
                System.out.println("SAXTransformerFactory is not supported.");
                System.exit(1);
            }

            // Create a SAXTransformerFactory instance.
            SAXTransformerFactory saxtf = (SAXTransformerFactory)tf;

            // Create a TransformerHandler instance.
            TransformerHandler th = null;
            if (xslfile == null) {
                th = saxtf.newTransformerHandler();
            } else {
                th = saxtf.newTransformerHandler(new StreamSource(new File(xslfile)));
            }

            // Set the destination for the XSLT transformer.
            if (outfile == null) {
                th.setResult(new StreamResult(System.out));
            } else {
                FileOutputStream fos = new FileOutputStream(outfile);
                Writer w = new OutputStreamWriter(fos, "UTF-8");
                th.setResult(new StreamResult(w));
            }

            // Create a new MarcHandler object.
            MarcXmlHandler handler = new MarcXmlHandler();
            // Register the TransformerHandler object.
            handler.setContentHandler(th);

            // Create a new  MarcReader object.
            MarcReader marcReader = new MarcReader();
            // Register the MarcHandler implementation.
            marcReader.setMarcHandler(handler);
            // Register the ErrorHandler implementation.
            marcReader.setErrorHandler(new ErrorHandlerImpl());
            // Send the file to the parse method.
            marcReader.parse(infile);

        } catch (TransformerConfigurationException tce) {
            tce.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
