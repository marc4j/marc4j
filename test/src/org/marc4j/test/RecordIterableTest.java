package org.marc4j.test;

import org.junit.Test;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.marc4j.test.utils.ResourceLoadUtils;
import org.marc4j.test.utils.StaticTestRecords;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for <code>RecordIterable</code>
 */
public class RecordIterableTest {

    @Test
    public void testCount() {
        int EXPECTED_COUNT = 1;
        String testFile = StaticTestRecords.RESOURCES_OCLC814388508_XML;
        MarcReader reader = ResourceLoadUtils.getMARCXMLReader(testFile);
        int count = 0;
        for( Record rec : reader ) {
            count++;
        }
        assertEquals("Unexpected number of records in " + testFile, EXPECTED_COUNT, count);
    }

}
