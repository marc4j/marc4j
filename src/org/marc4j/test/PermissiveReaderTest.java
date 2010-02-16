package org.marc4j.test;

import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;

public class PermissiveReaderTest extends TestCase {

  
    public void testBadLeaderBytes10_11() throws Exception {
        int i = 0;
        InputStream input = getClass().getResourceAsStream(
        "resources/bad_leaders_10_11.mrc");
        MarcReader reader = new MarcPermissiveStreamReader(input, true, true);
        while (reader.hasNext()) {
            Record record = reader.next();

            assertEquals(2, record.getLeader().getIndicatorCount());
            assertEquals(2, record.getLeader().getSubfieldCodeLength());
            i++;
        }
        input.close();
        assertEquals(1, i);
    }
    
	public static Test suite() {
	    return new TestSuite(PermissiveReaderTest.class);
	}
	
	public static void main(String args[]) {
	    TestRunner.run(suite());
	}
}
