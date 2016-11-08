package org.marc4j.test;

import org.junit.Test;
import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;
import org.marc4j.test.utils.StaticTestRecords;
import org.marc4j.test.utils.TestUtils;
import org.marc4j.util.RawRecord;
import org.marc4j.util.RawRecordReader;

import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RawRecordReaderTest {

    @Test
    public void testRawRecordReaderTooLongMarcRecord() throws Exception {
        InputStream input = getClass().getResourceAsStream(StaticTestRecords.RESOURCES_BAD_TOO_LONG_PLUS_2_MRC);
        assertNotNull(input);
        RawRecordReader reader = new RawRecordReader(input);
        assertTrue("Should have at least one record", reader.hasNext());

        String expectedIDs[] = { "360944", "360945", "360946" };
        RawRecord record;
        int cnt = 0;
        while (reader.hasNext())
        {
            record = reader.next();
            assertTrue( "Too many records in file", cnt < expectedIDs.length);
            assertTrue( "Expected ID mismatch", record.getRecordId().equals(expectedIDs[cnt]));
            cnt++;
        }
        assertTrue( "Too few records in file", cnt == expectedIDs.length);
        input.close();
    }
        
    @Test
    public void testRawRecordReaderTooLongRecord2() throws Exception {
        InputStream input = getClass().getResourceAsStream(StaticTestRecords.RESOURCES_6_BYTE_OFFSET_IN_DIRECTORY);
        assertNotNull(input);
        // This marc file has one record, that is too long for a marc binary record.
        // the directory contains offsets with 13 bytes and 6 byte offsets instead of 12 and 5 

        RawRecordReader reader = new RawRecordReader(input);
        assertTrue("Should have at least one record", reader.hasNext());

        String expectedIDs[] = { "u101755" };
        RawRecord record;
        int cnt = 0;
        while (reader.hasNext())
        {
            record = reader.next();
            assertTrue( "Too many records in file", cnt < expectedIDs.length);
            assertTrue( "Expected ID mismatch", record.getRecordId().equals(expectedIDs[cnt]));
            cnt++;
        }
        assertTrue( "Too few records in file", cnt == expectedIDs.length);
        input.close();
    }
    
    @Test
    public void testRawRecordReaderTooLongRecord3() throws Exception {
        InputStream input = getClass().getResourceAsStream(
                StaticTestRecords.RESOURCES_BAD_TOO_LARGE_HATHI_RECORD);
        assertNotNull(input);
        // This marc file has five records, the fourth one is way too long for a marc binary record,
        // it is so large that even the directory is over 99999 bytes long.
        // the directory contains steadily increasing offsets until the maximum size of 99999 is reached,
        // thereafter the offset is always 99999.

        RawRecordReader reader = new RawRecordReader(input);
        assertTrue("Should have at least one record", reader.hasNext());

        String expectedIDs[] = { "003034057", "003035931", "003051545", "003051567", "003052150" };
        RawRecord record;
        int cnt = 0;
        while (reader.hasNext())
        {
            record = reader.next();
            assertTrue( "Too many records in file", cnt < expectedIDs.length);
            assertTrue( "Expected ID mismatch", record.getRecordId().equals(expectedIDs[cnt]));
            cnt++;
        }
        assertTrue( "Too few records in file", cnt == expectedIDs.length);
        input.close();
    }
    
    @Test
    public void testRawRecordGetAsRecordForTooLongMarcRecord3() throws Exception {
        InputStream input = getClass().getResourceAsStream(StaticTestRecords.RESOURCES_BAD_TOO_LARGE_HATHI_RECORD);
        assertNotNull(input);
        // This marc file has five records, the fourth one is way too long for a marc binary record.
        // the directory contains steadily increasing offsets until the maximum size of 99999 is reached,
        // thereafter the offset is always 99999.

        RawRecordReader reader = new RawRecordReader(input);
        assertTrue("Should have at least one record", reader.hasNext());

 //       String expectedIDs[] = { "003034057", "003035931", "003051545", "003051567", "003052150" };
        RawRecord record;
  //      int cnt = 0;
        while (reader.hasNext())
        {
            record = reader.next();
            if (record.getRecordId().equals("003051567"))
            {
                Record marcRecord = record.getAsRecord(true, true, null, "MARC8");
                List<VariableField> fields = marcRecord.getVariableFields("974");
                assertEquals(fields.size(), 12582);
            }
        }
    }
    



}
