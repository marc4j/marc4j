package org.marc4j.test;

import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.marc4j.MarcJsonReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.marc4j.util.JsonParser;

public class JsonReaderTest extends TestCase {

    public void testInvalidMarcInJsonReader()  {
        try {
            checkMarcJsonDylanRecordFromFile("resources/illegal-marc-in-json.json");
            fail();
        } catch (JsonParser.Escape e) {
            String msg = "controls must be escaped using \\uHHHH; at Input Source: \"MarcInput\", Line: 170, Column: EOL";
            assertTrue("EOL",e.getMessage().contains(msg));
        }
    }
    
    public void testMarcJsonReader() throws Exception {
        checkMarcJsonDylanRecordFromFile("resources/marc-json.json");
    }
    public void testLegalMarcInJson() throws Exception {
        checkMarcJsonDylanRecordFromFile("resources/legal-json-marc-in-json.json");
    }

    private void checkMarcJsonDylanRecordFromFile(String fileName) {
        InputStream input = getClass().getResourceAsStream(
                fileName);
        MarcReader reader = new MarcJsonReader(input);
        if(!reader.hasNext()) {
            fail("should have at least one record");
        }

        Record record = reader.next();
        TestUtils.validateFreewheelingBobDylanRecord(record);
        if(reader.hasNext()) {
            fail("should not have more than one record");
        }
    }

	public static Test suite() {
	    return new TestSuite(JsonReaderTest.class);
	}
	
	public static void main(String args[]) {
	    TestRunner.run(suite());
	}
}
