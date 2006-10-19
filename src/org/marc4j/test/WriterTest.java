package org.marc4j.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

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
        InputStream input = getClass().getResourceAsStream(
                "resources/summerland.xml");
        MarcStreamWriter writer = new MarcStreamWriter(System.out);
        MarcXmlReader reader = new MarcXmlReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();
            writer.write(record);
        }
        input.close();
        writer.close();
    }

    public void testMarcXmlWriter() throws Exception {
        InputStream input = getClass().getResourceAsStream(
                "resources/summerland.mrc");
        MarcXmlWriter writer = new MarcXmlWriter(System.out, true);
        MarcStreamReader reader = new MarcStreamReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();
            writer.write(record);
        }
        input.close();
        writer.close();
    }

    public void testWriteAndRead() throws Exception {
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
        MarcStreamWriter marcWriter = new MarcStreamWriter(System.out);
        while (marcReader.hasNext()) {
            Record record = marcReader.next();
            marcWriter.write(record);
        }
        in.close();
        marcWriter.close();

        out.close();
    }

    public static Test suite() {
        return new TestSuite(WriterTest.class);
    }

    public static void main(String args[]) {
        TestRunner.run(suite());
    }
}
