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

    private static File createTempFile() throws IOException {
        File file = File.createTempFile("WriterTest","tmp");
        file.deleteOnExit();
        return file;
    }

    public void testMarcStreamWriter() throws Exception {

        File tmpFile = createTempFile();
        OutputStream out = new BufferedOutputStream(new FileOutputStream(tmpFile));
        InputStream input = getClass().getResourceAsStream(
                "resources/summerland.xml");
        MarcStreamWriter writer = new MarcStreamWriter(out);
        MarcXmlReader reader = new MarcXmlReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();
            writer.write(record);
        }
        input.close();
        writer.close();
        fail("Incomplete Test -  does not validate output");
    }

    public void testMarcXmlWriter() throws Exception {
        File tmpFile = createTempFile();
        OutputStream out = new BufferedOutputStream(new FileOutputStream(tmpFile));

        InputStream input = getClass().getResourceAsStream(
                "resources/summerland.mrc");
        MarcXmlWriter writer = new MarcXmlWriter(out, true);
        MarcStreamReader reader = new MarcStreamReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();
            writer.write(record);
        }
        input.close();
        writer.close();
        fail("Incomplete Test -  does not validate output");

    }
    
    public void testMarcXmlWriterNormalized() throws Exception {
        File tmpFile = createTempFile();
        OutputStream out = new BufferedOutputStream(new FileOutputStream(tmpFile));

        InputStream input = getClass().getResourceAsStream(
                "resources/summerland.mrc");
        MarcXmlWriter writer = new MarcXmlWriter(out, true);
        writer.setUnicodeNormalization(true);
        MarcStreamReader reader = new MarcStreamReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();
            writer.write(record);
        }
        input.close();
        writer.close();
        fail("Incomplete Test -  does not validate output");

    }

    public void testWriteAndRead() throws Exception {
        File tmpFile = createTempFile();
        OutputStream fileout = new BufferedOutputStream(new FileOutputStream(tmpFile));

        InputStream input = getClass().getResourceAsStream(
                "resources/summerland.xml");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MarcStreamWriter writer = new MarcStreamWriter(out);
        MarcXmlReader reader = new MarcXmlReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();
            writer.write(record);
        }
        input.close();
        writer.close();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        MarcStreamReader marcReader = new MarcStreamReader(in);
        MarcStreamWriter marcWriter = new MarcStreamWriter(fileout);
        while (marcReader.hasNext()) {
            Record record = marcReader.next();
            marcWriter.write(record);
        }
        in.close();
        marcWriter.close();

        out.close();
        fail("Incomplete Test -  does not validate output");

    }

    public static Test suite() {
        return new TestSuite(WriterTest.class);
    }

    public static void main(String args[]) {
        TestRunner.run(suite());
    }
}
