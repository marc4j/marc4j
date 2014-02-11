package org.marc4j.test;

import org.junit.Test;
import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.marc4j.test.utils.RecordTestingUtils;
import org.marc4j.test.utils.StaticTestRecords;

import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

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
    
    // This test is targeted toward code that attempts to fix instances where a vertical bar character 
    // has been interpreted as a sub-field separator, specifically in this case when the vertical bar 
    // occurs in a string of cyrillic characters, and is supposed to be a CYRILLIC CAPITAL LETTER E 
    @Test
    public void testCyrillicEFix() throws Exception {
       InputStream input = getClass().getResourceAsStream(
               StaticTestRecords.RESOURCES_CYRILLIC_CAPITAL_E_MRC);
        assertNotNull(input);
       MarcReader reader = new MarcPermissiveStreamReader(input, true, true);
       
       while (reader.hasNext())
       {
           Record record = reader.next();
           
           //Get fields with Cyrillic characters
           List<VariableField> fields = record.getVariableFields("880");
    
           for (VariableField field : fields)
           {
               DataField df = (DataField)field;
               Subfield sf = df.getSubfield('6');
               if (sf.getData().startsWith("26"))
               {
                   sf = df.getSubfield('b');
                   if (!sf.getData().equalsIgnoreCase("Эксмо,"))
                   {
                       fail("broken cyrillic record should have been fixed");
                   }
               }
           }
       }
    }
    
    // This test is targeted toward code that attempts to fix instances where within a string of characters
    // in the greek character set, a character set change back to the default character set is missing.
    // This would be indicated by characters being found for which there is no defined mapping in the greek character set
    // (typically a punctuation mark) or by an unlikely sequence of punctuation marks being found, which typically would
    // indicate a sequence of numerals.  The test reads a marc8 encoded version of a record with greek characters that has
    // been damaged by an ILS system, through having character set changes back to the default character set deleted, 
    // followed by that record represented in utf8, such that no character set changes are expected or needed, followed by a 
    // third copy of the same record represented in marc8 but using numeric character references to encode the greek characters.
    @Test
    public void testGreekMissingCharSetChange() throws Exception {
       InputStream input = getClass().getResourceAsStream(
               StaticTestRecords.RESOURCES_GREEK_MISSING_CHARSET_MRC);
        assertNotNull(input);
       MarcReader reader = new MarcPermissiveStreamReader(input, true, true);
       
       Record record1 = reader.next();
       Record record2 = reader.next();
       Record record3 = reader.next();
       
       if (record1 != null && record2 != null)
           RecordTestingUtils.assertEqualsIgnoreLeader(record1, record2);
       if (record2 != null && record3 != null)
           RecordTestingUtils.assertEqualsIgnoreLeader(record2, record3);
    }
    
    // This test is targeted toward code that attempts to fix instances where Marc8 multibyte-encoded CJK characters
    // are malformed, due to some software program deleting the characters '[' or ']' or '|'.  Each of these can
    // occur as a part of a Marc8 multibyte-encoded CJK characters, but some poorly written software treats the vertical
    // bar characters as subfield separators, and also summarily deletes the square brackets, which damages the Marc8 
    // multibyte-encoded CJK characters and makes translating the data to Unicode extremely difficult.
    @Test
    public void testMangledChineseCharacters() throws Exception {
       InputStream input = getClass().getResourceAsStream(
               StaticTestRecords.RESOURCES_CHINESE_MANGLED_MULTIBYTE_MRC);
       assertNotNull(input);
       MarcReader reader = new MarcPermissiveStreamReader(input, true, true, "MARC8");
       
       Record record1 = reader.next();
       Record record2 = reader.next();
       Record record3 = reader.next();
       Record record4 = reader.next();
//       if (record1 != null && record2 != null)
//           RecordTestingUtils.assertEqualsIgnoreLeader(record1, record2);
       String diff12 = RecordTestingUtils.getFirstRecordDifferenceIgnoreLeader(record1, record2);
       String diff23 = RecordTestingUtils.getFirstRecordDifferenceIgnoreLeader(record2, record3);
       String diff34 = RecordTestingUtils.getFirstRecordDifferenceIgnoreLeader(record3, record4);
       assertNull("Tested records are unexpected different: "+diff23, diff23);
       assertNull("Tested records are unexpected different: "+diff34, diff34);

    }
    

}
