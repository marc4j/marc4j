package org.marc4j.test;

import java.io.*;
import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.marc4j.MarcJsonReader;
import org.marc4j.MarcJsonWriter;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.Record;

public class JsonWriterTest extends TestCase {

    public void testMarcInJsonWriter() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MarcJsonWriter writer = new MarcJsonWriter(out, MarcJsonWriter.MARC_IN_JSON);
        Record record = getSummerlandRecord();
        writer.write(record);
        validateBytesAgainstFile(out.toByteArray(), "resources/summerland-marc-in-json.json");
        writer.close();
    }

    public void testMarcInJsonWriterIndented() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Record record = getSummerlandRecord();
        MarcJsonWriter writer = new MarcJsonWriter(out, MarcJsonWriter.MARC_IN_JSON);
        writer.setIndent(true);
        writer.write(record);
        validateBytesAgainstFile(out.toByteArray(), "resources/summerland-marc-in-json-indented.json");

        writer.close();

    }

    public void testMarcJsonWriter() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Record record = getSummerlandRecord();
        MarcJsonWriter writer = new MarcJsonWriter(out, MarcJsonWriter.MARC_JSON);
        writer.write(record);
        validateBytesAgainstFile(out.toByteArray(), "resources/summerland-marc-json.json");
        writer.close();

    }

    public void testMarcJsonWriterIndented() throws Exception {
        Record record = getSummerlandRecord();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MarcJsonWriter writer = new MarcJsonWriter(out, MarcJsonWriter.MARC_JSON);
        writer.setIndent(true);
        writer.write(record);
        validateBytesAgainstFile(out.toByteArray(), "resources/summerland-indented-marc-json.json");
        writer.close();

    }

    public void testJsonWriteAndRead() throws Exception {
        Record record = getJSONRecordFromFile("resources/legal-marc-in-json.json");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MarcJsonWriter writer = new MarcJsonWriter(out);
        writer.write(record);
        writer.close();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        MarcJsonReader marcReader = new MarcJsonReader(in);
        assertTrue(marcReader.hasNext());
        record = marcReader.next();
        TestUtils.validateFreewheelingBobDylanRecord(record);
        assertFalse(marcReader.hasNext());
        in.close();
    }

    public void testJsonWriteAndRead2() throws Exception {
        String fileName = "resources/marc-json.json";
        Record record = getJSONRecordFromFile(fileName);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MarcJsonWriter writer = new MarcJsonWriter(out, MarcJsonWriter.MARC_JSON);
            writer.write(record);
        writer.close();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        MarcJsonReader marcReader = new MarcJsonReader(in);
        assertTrue(marcReader.hasNext());
        TestUtils.validateFreewheelingBobDylanRecord(marcReader.next());
        assertFalse(marcReader.hasNext());
        in.close();

        out.close();

    }
    private Record getJSONRecordFromFile(String fileName) {
        InputStream input = getClass().getResourceAsStream(
                fileName);
        MarcJsonReader reader = new MarcJsonReader(input);
        assertTrue(reader.hasNext());
        Record record = reader.next();
        TestUtils.validateFreewheelingBobDylanRecord(record);
        assertFalse(reader.hasNext());
        return record;
    }


    private Record getSummerlandRecord() throws IOException {
        InputStream input = getClass().getResourceAsStream(
                "resources/summerland.mrc");
        MarcStreamReader reader = new MarcStreamReader(input);
        assertTrue("have at least one record", reader.hasNext());
        Record record = reader.next();
        assertFalse("Only one record", reader.hasNext());
        input.close();
        return record;
    }
    private void validateBytesAgainstFile(byte[] actual, String fileName) throws IOException {
        InputStream in = new BufferedInputStream(getClass().getResourceAsStream(fileName));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int n;
        byte expected[] = new byte[8192];
        while ((n = in.read(expected)) >= 0) {
            os.write(expected, 0, n);
        }
        os.flush();
        expected = os.toByteArray();
        if (!Arrays.equals(expected, actual)) {
            fail("expected: " + new String(expected) + ": actual" + new String(actual));
        }
    }



    public static Test suite() {
        return new TestSuite(JsonWriterTest.class);
    }

    public static void main(String args[]) {
        TestRunner.run(suite());
    }
}
