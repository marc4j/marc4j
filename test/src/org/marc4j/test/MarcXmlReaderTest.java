
package org.marc4j.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Iterator;

import org.junit.Test;
import org.marc4j.MarcError;
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
     * Tests reading MARCXML record with no collection element .
     */
    @Test
    public void testReadRecordNoCollectionElement() {
        final InputStream input = getClass().getResourceAsStream("/1474920681_KOHA_LFL_5.xml");
        assertNotNull(input);
        final MarcXmlReader reader = new MarcXmlReader(input);

        assertTrue("Should have at least one record", reader.hasNext());

        final Record record = reader.next();
        assertEquals(null, record.getType());
    }

    /**
     * Tests reading MARCXML record with no collection element .
     */
    @Test
    public void testReadRecordNoCollectionElementWithComment() {
        final InputStream input = getClass().getResourceAsStream("/1474920681_KOHA_LFL_6_with_comment.xml");
        assertNotNull(input);
        final MarcXmlReader reader = new MarcXmlReader(input);

        assertTrue("Should have at least one record", reader.hasNext());

        final Record record = reader.next();
        assertEquals(null, record.getType());
    }

    /**
     * Tests reading an indicator-less {@link Record}.
     */
    @Test
    public void testReadIndicatorlessRecord() {
        final InputStream input = getClass().getResourceAsStream("/cruel-cruel-indicatorless-summerland.xml");
        assertNotNull(input);
        final MarcXmlReader reader = new MarcXmlReader(input);
        assertTrue(reader.hasNext());
        final Record record = reader.next();
        assertTrue(record.hasErrors());
        assertTrue(record.getErrors().size() == 1);
        assertTrue(record.getErrors().iterator().next().message.contains("DataField (911) missing first indicator"));
    }

    /**
     * Tests reading a bad subfields element in {@link Record}
     */
    @Test
    public void testBadSubfieldValue() {
        final InputStream input = getClass().getResourceAsStream("/chabon-bad-subfields-element.xml");
        assertNotNull(input);
        final MarcXmlReader reader = new MarcXmlReader(input);
        assertTrue(reader.hasNext());
        final Record record = reader.next();
        assertTrue(record.hasErrors());
        assertTrue(record.getErrors().size() == 1);
        assertTrue(record.getErrors().iterator().next().message.contains("Unexpected XML element: subfields"));
    }

    /**
     * Tests reading a missing subfield code in {@link Record}
     */
    @Test
    public void testBadSubfieldCode() {
        final InputStream input = getClass().getResourceAsStream("/chabon-missing-subfield-code.xml");
        assertNotNull(input);
        final MarcXmlReader reader = new MarcXmlReader(input);
        assertTrue(reader.hasNext());
        final Record record = reader.next();
        assertTrue(record.hasErrors());
        assertTrue(record.getErrors().size() == 1);
        assertTrue(record.getErrors().iterator().next().message.contains("Subfield (020) missing code attribute"));
    }

    /**
     * Tests reading a missing {@link DataField} tag in {@link Record}
     */
    @Test
    public void testMissingDataFieldTag() {
        final InputStream input = getClass().getResourceAsStream("/chabon-missing-datafield-tag.xml");
        assertNotNull(input);
        final MarcXmlReader reader = new MarcXmlReader(input);
        assertTrue(reader.hasNext());
        final Record record = reader.next();
        assertTrue(record.hasErrors());
        assertTrue(record.getErrors().size() == 2);
        Iterator<MarcError> iter = record.getErrors().iterator();
        assertTrue(iter.next().message.contains("Unexpected XML element: subfields"));
        assertTrue(iter.next().message.contains("Missing tag element in datafield after tag: 655"));
    }

    /**
     * Tests reading a missing {@link ControlField} tag in {@link Record}
     */
    @Test
    public void testMissingControlFieldTag() {
        final InputStream input = getClass().getResourceAsStream("/chabon-missing-controlfield-tag.xml");
        assertNotNull(input);
        final MarcXmlReader reader = new MarcXmlReader(input);
        assertTrue(reader.hasNext());
        final Record record = reader.next();
        assertTrue(record.hasErrors());
        assertTrue(record.getErrors().size() == 2);
        assertTrue(record.getErrors().iterator().next().message.contains("Missing tag element in ControlField after tag: 001"));
    }

}
