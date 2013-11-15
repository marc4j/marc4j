package org.marc4j.test;

import org.junit.Test;
import org.marc4j.MarcException;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.Record;
import org.marc4j.test.utils.StaticTestRecords;
import org.marc4j.test.utils.TestUtils;

import java.io.InputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: ses
 * Date: 3/2/13
 * Time: 7:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class MarcXmlReaderTest {
    @Test
    public void testMarcXmlReader() throws Exception {
        InputStream input = getClass().getResourceAsStream(StaticTestRecords.RESOURCES_CHABON_XML);
        assertNotNull(input);
        MarcXmlReader reader = new MarcXmlReader(input);

        assertTrue("Should have at least one record", reader.hasNext());

        Record record1 = reader.next();
        TestUtils.validateKavalieAndClayRecord(record1);

        assertTrue("Should have at least two records", reader.hasNext());
        Record record2 = reader.next();
        TestUtils.validateSummerlandRecord(record2);

        assertFalse(" have more than two records", reader.hasNext());
        input.close();
    }

    @Test(expected = MarcException.class)
    public void testReadIndicatorlessRecord() {
        InputStream input = getClass().getResourceAsStream("/cruel-cruel-indicatorless-summerland.xml");
        assertNotNull(input);
        MarcXmlReader reader = new MarcXmlReader(input);
        assertTrue(reader.hasNext());
        Record record = reader.next();


    }

}
