package org.marc4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.marc4j.converter.impl.AnselToUnicode;
import org.marc4j.marc.Record;

public class RoundtripTest extends TestCase {

    public void testWriteRead() throws Exception {

        int counter = 0;

        InputStream input = getClass().getResourceAsStream(
                "resources/summerland.mrc");

        MarcStreamReader reader = new MarcStreamReader(input);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MarcStreamWriter writer = new MarcStreamWriter(out);
        while (reader.hasNext()) {
            Record record = reader.next();
            writer.write(record);
            counter++;
        }
        input.close();
        writer.close();

        assertEquals(1, counter);

        counter = 0;

        input = new ByteArrayInputStream(out.toByteArray());

        reader = new MarcStreamReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();
            counter++;
        }
        input.close();

        assertEquals(1, counter);
    }

    public void testWriteReadUtf8() throws Exception {
        InputStream input = getClass().getResourceAsStream(
                "resources/brkrtest.mrc");

        int counter = 0;

        MarcStreamReader reader = new MarcStreamReader(input);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MarcStreamWriter writer = new MarcStreamWriter(out, "UTF8");
        writer.setConverter(new AnselToUnicode());
        while (reader.hasNext()) {
            Record record = reader.next();
            writer.write(record);
            counter++;
        }
        input.close();
        writer.close();

        assertEquals(8, counter);

        counter = 0;

        input = new ByteArrayInputStream(out.toByteArray());

        reader = new MarcStreamReader(input, "UTF8");

        while (reader.hasNext()) {
            Record record = reader.next();
            counter++;
        }
        input.close();

        assertEquals(8, counter);
    }

    public static Test suite() {
        return new TestSuite(RoundtripTest.class);
    }

    public static void main(String args[]) {
        TestRunner.run(suite());
    }

}
