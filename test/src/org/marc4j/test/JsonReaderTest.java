package org.marc4j.test;

import org.junit.Test;
import org.marc4j.MarcJsonReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.marc4j.test.utils.RecordTestingUtils;
import org.marc4j.test.utils.StaticTestRecords;
import org.marc4j.test.utils.TestUtils;
import org.marc4j.util.JsonParser;

import java.io.InputStream;

import static org.junit.Assert.*;

public class JsonReaderTest {

    @Test
    public void testInvalidMarcInJsonReader() {
        try {
            checkMarcJsonDylanRecordFromFile(StaticTestRecords.RESOURCES_ILLEGAL_MARC_IN_JSON_JSON);
            fail();
        } catch (JsonParser.Escape e) {
            String msg = "controls must be escaped using \\uHHHH; at Input Source: \"MarcInput\", Line: 170, Column: EOL";
            assertTrue("EOL", e.getMessage().contains(msg));
        }
    }
    @Test
    public void testMarcJsonReaders() throws Exception {
        InputStream input1 = getResourceAsStream(StaticTestRecords.RESOURCES_MARC_JSON_JSON);
        MarcReader reader1 = new MarcJsonReader(input1);

        InputStream input2 = getResourceAsStream(StaticTestRecords.RESOURCES_MARC_IN_JSON_JSON);
        MarcReader reader2 = new MarcJsonReader(input2);
        while (reader1.hasNext() && reader2.hasNext()) {
            Record record1 = reader1.next();
            Record record2 = reader2.next();
            String recordAsStrings1[] = record1.toString().split("\n");
            String recordAsStrings2[] = record2.toString().split("\n");
            for (int i = 0; i < recordAsStrings1.length; i++) {
                assertEquals("line mismatch between two records", recordAsStrings1[i], recordAsStrings2[i]);
            }
            if (record1 != null && record2 != null)
                RecordTestingUtils.assertEqualsIgnoreLeader(record1, record2);
        }
        input1.close();
        input2.close();
    }
    @Test
    public void testMarcJsonReader() throws Exception {
        checkMarcJsonDylanRecordFromFile(StaticTestRecords.RESOURCES_MARC_JSON_JSON);
    }

    @Test
    public void testLegalMarcInJson() throws Exception {
        checkMarcJsonDylanRecordFromFile(StaticTestRecords.RESOURCES_LEGAL_JSON_MARC_IN_JSON_JSON);
    }

    private InputStream getResourceAsStream(String fileName) {
        InputStream input1 = getClass().getResourceAsStream(fileName);
        assertNotNull(fileName, input1);
        return input1;
    }

    private void checkMarcJsonDylanRecordFromFile(String fileName) {
        InputStream input = getResourceAsStream(fileName);
        MarcReader reader = new MarcJsonReader(input);
        if (!reader.hasNext()) {
            fail("should have at least one record");
        }

        Record record = reader.next();
        TestUtils.validateFreewheelingBobDylanRecord(record);
        if (reader.hasNext()) {
            fail("should not have more than one record");
        }
    }

}
