package org.marc4j.test;

import org.junit.Test;
import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.Record;
import org.marc4j.test.utils.StaticTestRecords;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PermissiveReaderTest  {

    @Test
    public void testBadLeaderBytes10_11() throws Exception {
        int i = 0;
        InputStream input = getClass().getResourceAsStream(StaticTestRecords.RESOURCES_BAD_LEADERS_10_11_MRC);
        assertNotNull(input);
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

    @Test
    public void testTooLongMarcRecord() throws Exception {
       InputStream input = getClass().getResourceAsStream(StaticTestRecords.RESOURCES_BAD_TOO_LONG_PLUS_2_MRC);
       assertNotNull(input);
       // This marc file has three records, but the first one
       // is too long for a marc binary record. Can we still read
       // the next two?
       MarcReader reader = new MarcPermissiveStreamReader(input, true, true);
              
       Record bad_record = reader.next();
       
       // Bad record is a total loss, don't even bother trying to read
       // it, but do we get the good records next?
       Record good_record1 = reader.next();
       ControlField good001 = good_record1.getControlNumberField();
       assertEquals(good001.getData(), "360945"); 
       
       
       Record good_record2 = reader.next();
       good001 = good_record2.getControlNumberField();
       assertEquals(good001.getData(), "360946"); 
       
    }

    @Test
    public void testTooLongLeaderByteRead() throws Exception {
       InputStream input = getClass().getResourceAsStream(
               StaticTestRecords.RESOURCES_BAD_TOO_LONG_PLUS_2_MRC);
        assertNotNull(input);
       MarcReader reader = new MarcPermissiveStreamReader(input, true, true);
       
       //First record is the long one. 
       Record weird_record = reader.next();
       
       //is it's marshal'd leader okay?
       String strLeader = weird_record.getLeader().marshal();

       // Make sure only five digits for length is used in the leader,
       // even though it's not big enough to hold the leader, we need to
       // make sure byte offsets in the rest of the leader are okay. 
       assertEquals("nas", strLeader.substring(5,8) );
       
       // And length should be set to our 99999 overflow value
       assertEquals("99999", strLeader.substring(0, 5));
    }
    
}
