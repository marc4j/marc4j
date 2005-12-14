package org.marc4j;

import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

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

    public static Test suite() {
        return new TestSuite(WriterTest.class);
    }

    public static void main(String args[]) {
        TestRunner.run(suite());
    }
}
