package org.marc4j.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.Mrk8StreamReader;
import org.marc4j.marc.Record;
import org.marc4j.test.utils.RecordTestingUtils;
import org.marc4j.test.utils.StaticTestRecords;

public class Mrk8StreamReaderWriterTest
{
    /**
     * Test reading a UTF8 encoded Ascii MarcBreaker formatted Record and compare the result to the same record loaded 
     * @throws Exception
     */
    @Test
    public void testMrk8Read() throws Exception {
        MarcReader reader1 = new Mrk8StreamReader( getClass().getResourceAsStream(StaticTestRecords.RESOURCES_BRKRTEST_MRK8));
        MarcReader reader2 = new MarcPermissiveStreamReader(getClass().getResourceAsStream(StaticTestRecords.RESOURCES_BRKRTEST_UTF8_MRC), true, false);
        assertTrue("Input file "+ StaticTestRecords.RESOURCES_BRKRTEST_MRK8 +" doesn't contain any records", reader1.hasNext());
        assertTrue("Input file "+ StaticTestRecords.RESOURCES_BRKRTEST_UTF8_MRC +" doesn't contain any records", reader2.hasNext());
        while (reader1.hasNext() && reader2.hasNext())
        {
            Record record1 = reader1.next();
            Record record2 = reader2.next();
            RecordTestingUtils.assertEqualsIgnoreLeader(record1, record2);
        }
    }

    @Test
    public void testMrkRead() throws Exception {
        MarcReader reader1 = new Mrk8StreamReader( getClass().getResourceAsStream(StaticTestRecords.RESOURCES_BRKRTEST_MRK), true);
        MarcReader reader2 = new MarcPermissiveStreamReader(getClass().getResourceAsStream(StaticTestRecords.RESOURCES_BRKRTEST_MRC), true, true);
        assertTrue("Input file "+ StaticTestRecords.RESOURCES_BRKRTEST_MRK +" doesn't contain any records", reader1.hasNext());
        assertTrue("Input file "+ StaticTestRecords.RESOURCES_BRKRTEST_MRC +" doesn't contain any records", reader2.hasNext());
        while (reader1.hasNext() && reader2.hasNext())
        {
            Record record1 = reader1.next();
            Record record2 = reader2.next();
            RecordTestingUtils.assertEqualsIgnoreLeader(record1, record2);
        }
    }

    @Test
    public void testMrkReadContainingDollar() throws Exception {
        MarcReader reader1 = new Mrk8StreamReader( getClass().getResourceAsStream(StaticTestRecords.RESOURCES_MRK8_WITH_DOLLAR), true);
        MarcReader reader2 = new MarcPermissiveStreamReader(getClass().getResourceAsStream(StaticTestRecords.RESOURCES_CONVERTED_MRK8), true, true);
        assertTrue("Input file "+ StaticTestRecords.RESOURCES_MRK8_WITH_DOLLAR +" doesn't contain any records", reader1.hasNext());
        assertTrue("Input file "+ StaticTestRecords.RESOURCES_CONVERTED_MRK8 +" doesn't contain any records", reader2.hasNext());
        while (reader1.hasNext() && reader2.hasNext())
        {
            Record record1 = reader1.next();
            Record record2 = reader2.next();
            RecordTestingUtils.assertEqualsIgnoreLeader(record1, record2);
        }
    }

}
