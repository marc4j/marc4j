package org.marc4j.samples;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.Record;

/**
 * A chain of transformation stages.
 * 
 * @author Bas Peters
 */
public class StylesheetChainExample {

    public static void main(String args[]) throws Exception {

        TransformerFactory tFactory = TransformerFactory.newInstance();

        if (tFactory.getFeature(SAXSource.FEATURE)
                && tFactory.getFeature(SAXResult.FEATURE)) {

            // cast the transformer handler to a sax transformer handler
            SAXTransformerFactory saxTFactory = ((SAXTransformerFactory) tFactory);

            // create a TransformerHandler for each stylesheet.
            TransformerHandler tHandler1 = saxTFactory
                    .newTransformerHandler(new StreamSource(
                            "http://www.loc.gov/standards/mods/v3/MARC21slim2MODS3.xsl"));
            TransformerHandler tHandler2 = saxTFactory
                    .newTransformerHandler(new StreamSource(
                            "http://www.loc.gov/standards/marcxml/xslt/MODS2MARC21slim.xsl"));
            TransformerHandler tHandler3 = saxTFactory
                    .newTransformerHandler(new StreamSource(
                            "http://www.loc.gov/standards/marcxml/xslt/MARC21slim2HTML.xsl"));

            // chain the transformer handlers
            tHandler1.setResult(new SAXResult(tHandler2));
            tHandler2.setResult(new SAXResult(tHandler3));
            
            OutputStream out = new FileOutputStream("c:/temp/output.html");
            tHandler3.setResult(new StreamResult(out));

            // create a SAXResult with the first handler
            Result result = new SAXResult(tHandler1);

            // create the input stream
            InputStream input = ReadMarcExample.class
                    .getResourceAsStream("resources/summerland.mrc");

            // parse the input
            MarcReader reader = new MarcStreamReader(input);
            MarcWriter writer = new MarcXmlWriter(result);
            while (reader.hasNext()) {
                Record record = reader.next();
                writer.write(record);
            }
            writer.close();
            
            out.close();
        }
    }

}
