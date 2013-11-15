package org.marc4j.test;

import org.junit.Test;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.Record;
import org.marc4j.test.utils.StaticTestRecords;
import org.marc4j.test.utils.TestUtils;

import java.io.InputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ReaderTest {

    @Test
    public void testMarcStreamReader() throws Exception {
        InputStream input = getClass().getResourceAsStream(
                StaticTestRecords.RESOURCES_CHABON_MRC);
        assertNotNull(input);

        MarcStreamReader reader = new MarcStreamReader(input);
        assertTrue("Should have at least one record", reader.hasNext());

        Record record1 = reader.next();
        TestUtils.validateKavalieAndClayRecord(record1);

        assertTrue("Should have at least two records", reader.hasNext());
        Record record2 = reader.next();
        TestUtils.validateSummerlandRecord(record2);

        assertFalse(" have more than two records", reader.hasNext());
        input.close();
    }


}
