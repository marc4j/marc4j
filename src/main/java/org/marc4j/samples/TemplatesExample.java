package org.marc4j.samples;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.marc4j.MarcReader;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.Record;

/**
 * Transformation with compiled stylesheet.
 * 
 * @author Bas Peters
 */
public class TemplatesExample {

    public static void main(String args[]) throws Exception {
        if (args.length != 1)
            throw new Exception("Usage: TemplatesExample: <input-dir>");

        String inputDir = args[0];

        TransformerFactory tFactory = TransformerFactory.newInstance();

        if (tFactory.getFeature(SAXSource.FEATURE)
                && tFactory.getFeature(SAXResult.FEATURE)) {

            // cast the transformer handler to a sax transformer handler
            SAXTransformerFactory saxTFactory = ((SAXTransformerFactory) tFactory);

            Source stylesheet = new StreamSource(
                    "http://www.loc.gov/standards/marcxml/xslt/MODS2MARC21slim.xsl");

            // create an in-memory stylesheet representation
            Templates templates = tFactory.newTemplates(stylesheet);

            File dir = new File(inputDir);

            // create a filter to include only .xml files
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".xml");
                }
            };
            File[] files = dir.listFiles(filter);

            for (int i = 0; i < files.length; i++) {
                InputStream input = new FileInputStream(files[i]);

                TransformerHandler handler = saxTFactory
                        .newTransformerHandler(templates);

                // parse the input
                MarcReader reader = new MarcXmlReader(input, handler);
                while (reader.hasNext()) {
                    Record record = reader.next();
                    System.out.println(record.toString());
                }
            }
        }
    }

}
