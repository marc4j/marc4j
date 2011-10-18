package org.marc4j.test;

import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.marc4j.MarcJsonReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;

public class JsonReaderTest extends TestCase {

    public void testMarcInJsonReader() throws Exception {
        int i = 0;
        InputStream input = getClass().getResourceAsStream(
                "resources/marc-in-json.json");
        MarcReader reader = new MarcJsonReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();
            System.err.println(record.toString());
            i++;
        }
        input.close();
        assertEquals(1, i);
    }
    
    public void testMarcJsonReader() throws Exception {
        int i = 0;
        InputStream input = getClass().getResourceAsStream(
                "resources/marc-json.json");
        MarcReader reader = new MarcJsonReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();
            System.err.println(record.toString());
            i++;
        }
        input.close();
        assertEquals(1, i);
    }

	public static Test suite() {
	    return new TestSuite(JsonReaderTest.class);
	}
	
	public static void main(String args[]) {
	    TestRunner.run(suite());
	}
}
