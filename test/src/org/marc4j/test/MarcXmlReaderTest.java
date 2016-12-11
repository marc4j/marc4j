
package org.marc4j.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.junit.Test;
import org.marc4j.MarcException;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.test.utils.TestUtils;

/**
 * Tests the MarcXmlReader.
 */
public class MarcXmlReaderTest {

    /**
     * Tests {@link MarcXmlReader}
     *
     * @throws Exception
     */
    @Test
    public void testMarcXmlReader() throws Exception {
        final InputStream input = getClass().getResourceAsStream("/chabon.xml");
        assertNotNull(input);
        final MarcXmlReader reader = new MarcXmlReader(input);

        assertTrue("Should have at least one record", reader.hasNext());

        final Record record1 = reader.next();
        TestUtils.validateKavalieAndClayRecord(record1);

        assertTrue("Should have at least two records", reader.hasNext());
        final Record record2 = reader.next();
        TestUtils.validateSummerlandRecord(record2);

        assertFalse(" have more than two records", reader.hasNext());
        input.close();
    }

    /**
     * Tests reading record type from MARCXML record.
     */
    @Test
    public void testReadRecordType() {
        final InputStream input = getClass().getResourceAsStream("/chabon-record-type.xml");
        assertNotNull(input);
        final MarcXmlReader reader = new MarcXmlReader(input);

        assertTrue("Should have at least one record", reader.hasNext());

        final Record record = reader.next();
        assertEquals("Bibliographic", record.getType());
    }

    /**
     * Tests reading bad record type from MARCXML record.
     */
    @Test
    public void testReadBadRecordType() {
        final InputStream input = getClass().getResourceAsStream("/chabon-record-type-bad.xml");
        assertNotNull(input);
        final MarcXmlReader reader = new MarcXmlReader(input);

        assertTrue("Should have at least one record", reader.hasNext());

        final Record record = reader.next();
        assertEquals(null, record.getType());
    }

    /**
     * Tests reading an indicator-less {@link Record}.
     */
    @Test(expected = MarcException.class)
    public void testReadIndicatorlessRecord() {
        final InputStream input = getClass().getResourceAsStream("/cruel-cruel-indicatorless-summerland.xml");
        assertNotNull(input);
        final MarcXmlReader reader = new MarcXmlReader(input);
        assertTrue(reader.hasNext());
        final Record record = reader.next();

    }

    /**
     * Tests reading a bad subfields element in {@link Record}
     */
    @Test(expected = MarcException.class)
    public void testBadSubfieldValue() {
        final InputStream input = getClass().getResourceAsStream("/chabon-bad-subfields-element.xml");
        assertNotNull(input);
        final MarcXmlReader reader = new MarcXmlReader(input);
        assertTrue(reader.hasNext());
        final Record record = reader.next();
    }

    /**
     * Tests reading a missing subfield code in {@link Record}
     */
    @Test(expected = MarcException.class)
    public void testBadSubfieldCode() {
        final InputStream input = getClass().getResourceAsStream("/chabon-missing-subfield-code.xml");
        assertNotNull(input);
        final MarcXmlReader reader = new MarcXmlReader(input);
        assertTrue(reader.hasNext());
        final Record record = reader.next();
    }

    /**
     * Tests reading a missing {@link DataField} tag in {@link Record}
     */
    @Test(expected = MarcException.class)
    public void testMissingDataFieldTag() {
        final InputStream input = getClass().getResourceAsStream("/chabon-missing-datafield-tag.xml");
        assertNotNull(input);
        final MarcXmlReader reader = new MarcXmlReader(input);
        assertTrue(reader.hasNext());
        final Record record = reader.next();
    }

    /**
     * Tests reading a missing {@link ControlField} tag in {@link Record}
     */
    @Test(expected = MarcException.class)
    public void testMissingControlFieldTag() {
        final InputStream input = getClass().getResourceAsStream("/chabon-missing-controlfield-tag.xml");
        assertNotNull(input);
        final MarcXmlReader reader = new MarcXmlReader(input);
        assertTrue(reader.hasNext());
        final Record record = reader.next();
    }

}
