package org.marc4j.test;

import org.junit.Test;
import org.marc4j.MarcFilteredReader;
import org.marc4j.MarcReader;
import org.marc4j.MarcScriptedRecordEditReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.marc4j.test.utils.StaticTestRecords;
import org.marc4j.test.utils.TestUtils;

import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MarcScriptedEditRecordReaderTest {

    @Test
    public void testDeleteFields() throws Exception {
        InputStream input = getClass().getResourceAsStream("/u335.mrc");
        assertNotNull(input);
        
        MarcReader reader = new MarcScriptedRecordEditReader(new MarcStreamReader(input), "999");
        Record record = reader.next();
        List<DataField> dfs = record.getDataFields();
        assertTrue("Wrong count of datafields in processed record", dfs.size() == 15);
    }
    
    @Test
    public void testDeleteSubFields() throws Exception {
        InputStream input = getClass().getResourceAsStream("/u335.mrc");
        assertNotNull(input);
        
        MarcReader reader = new MarcScriptedRecordEditReader(new MarcStreamReader(input), "999w:999c:999i:999d:999l:999m:999k");
        Record record = reader.next();
        List<VariableField> dfs = record.getVariableFields("999");
        for (VariableField df: dfs) {
            List<Subfield> sfs = ((DataField)df).getSubfields("wcidlmk");
            assertTrue("subfields not deleted", sfs.size() == 0);
        }
    }
}
