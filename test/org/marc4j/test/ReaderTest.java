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
        InputStream input = getClass().getResourceAsStream(
                "resources/chabon.mrc");
        MarcStreamReader reader = new MarcStreamReader(input);
        assertTrue("Should have at least one record",reader.hasNext());

        Record record1 = reader.next();
        TestUtils.validateKavalieAndClayRecord(record1);

        assertTrue("Should have at least two records",reader.hasNext());
        Record record2 = reader.next();
        TestUtils.validateSummerlandRecord(record2);

        assertFalse(" have more than two records",reader.hasNext());
        input.close();
    }

    public void testMarcXmlReader() throws Exception {
        InputStream input = getClass().getResourceAsStream(
                "resources/chabon.xml");
        MarcXmlReader reader = new MarcXmlReader(input);

        assertTrue("Should have at least one record",reader.hasNext());

        Record record1 = reader.next();
        TestUtils.validateKavalieAndClayRecord(record1);

        assertTrue("Should have at least two records",reader.hasNext());
        Record record2 = reader.next();
        TestUtils.validateSummerlandRecord(record2);

        assertFalse(" have more than two records",reader.hasNext());
        input.close();
    }

	public static Test suite() {
	    return new TestSuite(ReaderTest.class);
	}
	
	public static void main(String args[]) {
	    TestRunner.run(suite());
	}
}
