package org.marc4j.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

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

    public void testMarcInJsonWriter() throws Exception {
        InputStream input = getClass().getResourceAsStream(
                "resources/summerland.mrc");
        MarcJsonWriter writer = new MarcJsonWriter(System.out, MarcJsonWriter.MARC_IN_JSON);
        MarcStreamReader reader = new MarcStreamReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();
            writer.write(record);
        }
        input.close();
        writer.close();
    }
    
    public void testMarcInJsonWriterIndented() throws Exception {
        InputStream input = getClass().getResourceAsStream(
                "resources/summerland.mrc");
        MarcJsonWriter writer = new MarcJsonWriter(System.out, MarcJsonWriter.MARC_IN_JSON);
        writer.setIndent(true);
        MarcStreamReader reader = new MarcStreamReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();
            writer.write(record);
        }
        input.close();
        writer.close();
    }

    public void testMarcJsonWriter() throws Exception {
        InputStream input = getClass().getResourceAsStream(
                "resources/summerland.mrc");
        MarcJsonWriter writer = new MarcJsonWriter(System.out, MarcJsonWriter.MARC_JSON);
        MarcStreamReader reader = new MarcStreamReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();
            writer.write(record);
        }
        input.close();
        writer.close();
    }
    
    public void testMarcJsonWriterIndented() throws Exception {
        InputStream input = getClass().getResourceAsStream(
                "resources/summerland.mrc");
        MarcJsonWriter writer = new MarcJsonWriter(System.out, MarcJsonWriter.MARC_JSON);
        writer.setIndent(true);
        MarcStreamReader reader = new MarcStreamReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();
            writer.write(record);
        }
        input.close();
        writer.close();
    }

    public void testJsonWriteAndRead() throws Exception {
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
        MarcJsonWriter marcWriter = new MarcJsonWriter(System.out);
        marcWriter.setIndent(true);
        while (marcReader.hasNext()) {
            Record record = marcReader.next();
            marcWriter.write(record);
        }
        in.close();
        marcWriter.close();

        out.close();
    }

    public void testJsonWriteAndRead2() throws Exception {
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
        MarcJsonWriter marcWriter = new MarcJsonWriter(System.out, MarcJsonWriter.MARC_JSON);
        marcWriter.setIndent(true);
        while (marcReader.hasNext()) {
            Record record = marcReader.next();
            marcWriter.write(record);
        }
        in.close();
        marcWriter.close();

        out.close();
    }

    
    public static Test suite() {
        return new TestSuite(JsonWriterTest.class);
    }

    public static void main(String args[]) {
        TestRunner.run(suite());
    }
}
