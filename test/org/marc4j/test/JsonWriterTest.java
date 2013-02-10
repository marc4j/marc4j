package org.marc4j.test;

import java.io.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.marc4j.MarcJsonReader;
import org.marc4j.MarcJsonWriter;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcXmlReader;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.Record;

public class JsonWriterTest extends TestCase {

    private static File createTempFile() throws IOException {
        File file = File.createTempFile("WriterTest","tmp");
        file.deleteOnExit();
        return file;
    }

    public void testMarcInJsonWriter() throws Exception {
        File tmpFile = createTempFile();
        OutputStream out = new BufferedOutputStream(new FileOutputStream(tmpFile));
        InputStream input = getClass().getResourceAsStream(
                "resources/summerland.mrc");
        MarcJsonWriter writer = new MarcJsonWriter(out, MarcJsonWriter.MARC_IN_JSON);
        MarcStreamReader reader = new MarcStreamReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();
            writer.write(record);
        }
        input.close();
        writer.close();
        fail("Test incomplete does not validate output");
    }
    
    public void testMarcInJsonWriterIndented() throws Exception {
        File tmpFile = createTempFile();
        OutputStream out = new BufferedOutputStream(new FileOutputStream(tmpFile));
        InputStream input = getClass().getResourceAsStream(
                "resources/summerland.mrc");
        MarcJsonWriter writer = new MarcJsonWriter(out, MarcJsonWriter.MARC_IN_JSON);
        writer.setIndent(true);
        MarcStreamReader reader = new MarcStreamReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();
            writer.write(record);
        }
        input.close();
        writer.close();
        fail("Test incomplete -  does not validate output");

    }

    public void testMarcJsonWriter() throws Exception {
        File tmpFile = createTempFile();
        OutputStream out = new BufferedOutputStream(new FileOutputStream(tmpFile));
        InputStream input = getClass().getResourceAsStream(
                "resources/summerland.mrc");
        MarcJsonWriter writer = new MarcJsonWriter(out, MarcJsonWriter.MARC_JSON);
        MarcStreamReader reader = new MarcStreamReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();
            writer.write(record);
        }
        input.close();
        writer.close();
        fail("Test incomplete - does not validate output");

    }
    
    public void testMarcJsonWriterIndented() throws Exception {
        File tmpFile = createTempFile();
        OutputStream out = new BufferedOutputStream(new FileOutputStream(tmpFile));
        InputStream input = getClass().getResourceAsStream(
                "resources/summerland.mrc");
        MarcJsonWriter writer = new MarcJsonWriter(out, MarcJsonWriter.MARC_JSON);
        writer.setIndent(true);
        MarcStreamReader reader = new MarcStreamReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();
            writer.write(record);
        }
        input.close();
        writer.close();
        fail("Test  incomplete - does not validate output");

    }

    public void testJsonWriteAndRead() throws Exception {
        File tmpFile = createTempFile();
        OutputStream outFile = new BufferedOutputStream(new FileOutputStream(tmpFile));
        InputStream input = getClass().getResourceAsStream(
                "resources/marc-in-json.json");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MarcJsonWriter writer = new MarcJsonWriter(out);
        MarcJsonReader reader = new MarcJsonReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();
            writer.write(record);
        }
        input.close();
        writer.close();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        MarcJsonReader marcReader = new MarcJsonReader(in);
        MarcJsonWriter marcWriter = new MarcJsonWriter(outFile);
        marcWriter.setIndent(true);
        while (marcReader.hasNext()) {
            Record record = marcReader.next();
            marcWriter.write(record);
        }
        in.close();
        marcWriter.close();

        out.close();
        fail("Test incomplete - does not validate output");

    }

    public void testJsonWriteAndRead2() throws Exception {
        File tmpFile = createTempFile();
        OutputStream outFile = new BufferedOutputStream(new FileOutputStream(tmpFile));
        InputStream input = getClass().getResourceAsStream(
                "resources/marc-json.json");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MarcJsonWriter writer = new MarcJsonWriter(out, MarcJsonWriter.MARC_JSON);
        MarcJsonReader reader = new MarcJsonReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();
            writer.write(record);
        }
        input.close();
        writer.close();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        MarcJsonReader marcReader = new MarcJsonReader(in);
        MarcJsonWriter marcWriter = new MarcJsonWriter(outFile, MarcJsonWriter.MARC_JSON);
        marcWriter.setIndent(true);
        while (marcReader.hasNext()) {
            Record record = marcReader.next();
            marcWriter.write(record);
        }
        in.close();
        marcWriter.close();

        out.close();
        fail("Test incomplete - does not validate output");

    }

    
    public static Test suite() {
        return new TestSuite(JsonWriterTest.class);
    }

    public static void main(String args[]) {
        TestRunner.run(suite());
    }
}
