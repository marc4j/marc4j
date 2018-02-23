package org.marc4j.test;

import org.junit.Test;
import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.MarcScriptedRecordEditReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.marc4j.test.utils.RecordTestingUtils;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

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

    @Test
    public void testApplyEdit() throws Exception {
        String[] expected700Fields = { "3DD Group (Firm)", "Kanopy (Firm)" };
        InputStream input = getClass().getResourceAsStream("/kan1222221.mrc");
        assertNotNull(input);

        InputStream editmap = getClass().getResourceAsStream("/video_recs_map.properties");
        Properties editmapProperties = new Properties();
        editmapProperties.load(editmap);

        MarcReader reader =  new MarcScriptedRecordEditReader(new MarcStreamReader(input), editmapProperties);
        Record record = reader.next();
        List<VariableField> dfs = record.getVariableFields("700");
        int i = 0;
        for (VariableField df: dfs) {
            assertTrue("fields not edited", ((DataField)df).getSubfield('a').getData().equals(expected700Fields[i++]));
        }

    }

    @Test
    public void testApplyEditGetty() throws Exception {
        InputStream toEditInput = getClass().getResourceAsStream("/getty_test_1.mrc");
        assertNotNull(toEditInput);

        InputStream expectedRecInput = getClass().getResourceAsStream("/getty_test_output.mrc");
        assertNotNull(expectedRecInput);

        InputStream editmap = getClass().getResourceAsStream("/edit_getty.properties");
        Properties editmapProperties = new Properties();
        editmapProperties.load(editmap);

        MarcReader reader1 =  new MarcScriptedRecordEditReader(new MarcStreamReader(toEditInput), editmapProperties);
        Record editedRecord = reader1.next();

        MarcReader reader2 =  new MarcStreamReader(expectedRecInput);
        Record expectedRecord = reader2.next();

        RecordTestingUtils.assertEqualsIgnoreLeader(expectedRecord, editedRecord);
    }

    @Test
    public void testDelete710Field() throws Exception {
        String[] expected710Fields = { "Different College" };
        InputStream input = getClass().getResourceAsStream("/summerlandwith710.xml");
        assertNotNull(input);

        InputStream editmap = getClass().getResourceAsStream("/delete_710_map.properties");
        Properties editmapProperties = new Properties();
        editmapProperties.load(editmap);

        MarcReader reader =  new MarcScriptedRecordEditReader(new MarcXmlReader(input), editmapProperties);
        Record record = reader.next();
        List<VariableField> dfs = record.getVariableFields("710");
        int i = 0;
        for (VariableField df: dfs) {
            assertTrue("fields not edited", ((DataField)df).getSubfield('a').getData().equals(expected710Fields[i++]));
        }

    }

    @Test
    public void testMoveSubfield() throws Exception {
//        String[] expected856Fields = { "Different College" };
        InputStream input = getClass().getResourceAsStream("/rec1.mrc");
        assertNotNull(input);

        InputStream editmap = getClass().getResourceAsStream("/move_subfield.properties");
        Properties editmapProperties = new Properties();
        editmapProperties.load(editmap);

        MarcReader reader =  new MarcScriptedRecordEditReader(new MarcStreamReader(input), editmapProperties);
        Record record = reader.next();
        VariableField df245 = record.getVariableField("245");
        assertTrue("Subfieled 245b not deleted.", ((DataField)df245).getSubfield('b') == null);
        List<VariableField> dfs = record.getVariableFields("856");
        int i = 0;
        for (VariableField df: dfs) {
            assertTrue("fields not edited", ((DataField)df).getSubfield('z') != null);
        }

    }

    @Test
    public void testMoveSubfield2() throws Exception {
//        String[] expected856Fields = { "Different College" };
        InputStream input = getClass().getResourceAsStream("/54-56-008008027.mrc");
        assertNotNull(input);

        InputStream editmap = getClass().getResourceAsStream("/move_subfield.properties");
        Properties editmapProperties = new Properties();
        editmapProperties.load(editmap);

        MarcReader reader =  new MarcScriptedRecordEditReader(new MarcPermissiveStreamReader(input, true, true), editmapProperties);
        Record record;
        while ((record = reader.next()) != null) {
            VariableField df245 = record.getVariableField("245");
            assertTrue("Subfieled 245b not deleted.", ((DataField)df245).getSubfield('b') == null);
            List<VariableField> dfs = record.getVariableFields("856");
            int i = 0;
            for (VariableField df: dfs) {
                assertTrue("fields not edited", ((DataField)df).getSubfield('z') != null);
            }
        }
    }

    @Test
    public void testMoveSubfield3() throws Exception {
//        String[] expected856Fields = { "Different College" };
        InputStream input = getClass().getResourceAsStream("/065001035.mrc");
        assertNotNull(input);

        InputStream editmap = getClass().getResourceAsStream("/move_subfield.properties");
        Properties editmapProperties = new Properties();
        editmapProperties.load(editmap);

        MarcReader reader =  new MarcScriptedRecordEditReader(new MarcStreamReader(input), editmapProperties);
        Record record;
        while ((record = reader.next()) != null) {
            VariableField df245 = record.getVariableField("245");
            assertTrue("Subfieled 245b not deleted.", ((DataField)df245).getSubfield('b') == null);
            List<VariableField> dfs = record.getVariableFields("856");
            int i = 0;
            for (VariableField df: dfs) {
                assertTrue("fields not edited", ((DataField)df).getSubfield('z') != null);
            }
        }
    }
    @Test
    public void testMoveSubfield4() throws Exception {
//        String[] expected856Fields = { "Different College" };
        InputStream input = getClass().getResourceAsStream("/dp_004015001.mrc");
        assertNotNull(input);

        InputStream editmap = getClass().getResourceAsStream("/move_subfield.properties");
        Properties editmapProperties = new Properties();
        editmapProperties.load(editmap);

        MarcReader reader =  new MarcScriptedRecordEditReader(new MarcStreamReader(input), editmapProperties);
        Record record;
        while ((record = reader.next()) != null) {
            VariableField df245 = record.getVariableField("245");
            assertTrue("Subfieled 245b not deleted.", ((DataField)df245).getSubfield('b') == null);
            List<VariableField> dfs = record.getVariableFields("856");
            int i = 0;
            for (VariableField df: dfs) {
                assertTrue("fields not edited", ((DataField)df).getSubfield('z') != null);
            }
        }
    }
}
