package org.marc4j.test;

import java.io.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.marc4j.MarcStreamReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcXmlReader;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.Record;

public class WriterTest extends TestCase {

    public void testMarcStreamWriter() throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream input = getClass().getResourceAsStream("resources/summerland.xml");
        MarcStreamWriter writer = new MarcStreamWriter(out);
        MarcXmlReader reader = new MarcXmlReader(input);
        Record record = reader.next();
        writer.write(record);
        writer.close();
        input.close();
        TestUtils.validateBytesAgainstFile(out.toByteArray(), "resources/summerland.mrc");
    }

    public void testMarcXmlWriter() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream input = getClass().getResourceAsStream("resources/summerland.mrc");
        MarcXmlWriter writer = new MarcXmlWriter(out, true);
        MarcStreamReader reader = new MarcStreamReader(input);
        Record record = reader.next();
        input.close();

        writer.write(record);
        writer.close();

        TestUtils.validateStringAgainstFile(new String(out.toByteArray()), "resources/summerland.xml");

    }
    /* TODO: This record does not contain any diacritics, so there isn't much normalization to test.
     */
    public void testMarcXmlWriterNormalized() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        InputStream input = getClass().getResourceAsStream(
                "resources/summerland.mrc");
        MarcXmlWriter writer = new MarcXmlWriter(out, true);
        writer.setUnicodeNormalization(true);
        MarcStreamReader reader = new MarcStreamReader(input);
        Record record = reader.next();
        writer.write(record);
        input.close();
        writer.close();
        TestUtils.validateBytesAgainstFile(out.toByteArray(),"resources/summerland.xml");
    }


    public static Test suite() {
        return new TestSuite(WriterTest.class);
    }

    public static void main(String args[]) {
        TestRunner.run(suite());
    }
}
