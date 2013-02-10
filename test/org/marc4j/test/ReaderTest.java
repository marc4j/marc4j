package org.marc4j.test;

import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.marc4j.MarcStreamReader;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.Record;

public class ReaderTest extends TestCase {

    public void testMarcStreamReader() throws Exception {
        int i = 0;
        InputStream input = getClass().getResourceAsStream(
                "resources/chabon.mrc");
        MarcStreamReader reader = new MarcStreamReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();
            String recordAsString = record.toString();
            //System.err.println(recordAsString);
            i++;
        }
        input.close();
        assertEquals(2, i);
        fail("Test incomplete - only record count is checked");
    }

    public void testMarcXmlReader() throws Exception {
        int i = 0;
        InputStream input = getClass().getResourceAsStream(
                "resources/chabon.xml");
        MarcXmlReader reader = new MarcXmlReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();
            String recordAsString = record.toString();
            //System.err.println(recordAsString);
            i++;
        }
        input.close();
        assertEquals(2, i);
        fail("Test incomplete - only record count is checked");
    }

	public static Test suite() {
	    return new TestSuite(ReaderTest.class);
	}
	
	public static void main(String args[]) {
	    TestRunner.run(suite());
	}
}
