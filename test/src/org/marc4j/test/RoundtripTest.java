package org.marc4j.test;

import org.junit.Test;
import org.marc4j.*;
import org.marc4j.converter.impl.AnselToUnicode;
import org.marc4j.converter.impl.UnicodeToAnsel;
import org.marc4j.marc.Record;
import org.marc4j.test.utils.RecordTestingUtils;
import org.marc4j.test.utils.StaticTestRecords;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;

public class RoundtripTest  {

    /**
     * This test reads in a file of utf-8 encoded binary Marc records
     * then writes those records out as utf-8 encoded binary Marc records,
     * then reads those records.  The test then compares those records with
     * the original records, expecting them to be identical.
     *
     * @throws Exception
     */
    @Test
    public void testWriteAndReadRoundtrip() throws Exception {
        InputStream input = getClass().getResourceAsStream(StaticTestRecords.RESOURCES_CHABON_MRC);
        assertNotNull(input);
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        MarcStreamReader marcReader = new MarcStreamReader(input);
        MarcXmlWriter xmlWriter = new MarcXmlWriter(out1);
        while (marcReader.hasNext()) {
            Record record = marcReader.next();
            xmlWriter.write(record);
        }
        input.close();
        xmlWriter.close();
        out1.close();
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(out1.toByteArray());
        MarcXmlReader xmlReader = new MarcXmlReader(in);
        MarcStreamWriter marcWriter = new MarcStreamWriter(out2);
        while (xmlReader.hasNext()) {
            Record record = xmlReader.next();
            marcWriter.write(record);
        }
        in.close();
        marcWriter.close();

        out2.close();

        InputStream inputCompare1 = getClass().getResourceAsStream(StaticTestRecords.RESOURCES_CHABON_MRC);
        InputStream inputCompare2 = new ByteArrayInputStream(out2.toByteArray());
        MarcReader readComp1 = new MarcStreamReader(inputCompare1);
        MarcReader readComp2 = new MarcStreamReader(inputCompare2);
        Record r1, r2;
        do {
            r1 = (readComp1.hasNext()) ? readComp1.next() : null;
            r2 = (readComp2.hasNext()) ? readComp2.next() : null;
            if (r1 != null && r2 != null)
                RecordTestingUtils.assertEqualsIgnoreLeader(r1, r2);
        } while (r1 != null && r2 != null);
    }

    /**
     * This test reads in a file of Marc8 encoded binary Marc records
     * then writes those records out as utf-8 encoded binary Marc records,
     * then reads those records back in and writes them out as Marc8 encoded binary
     * Marc records. The test then compares those records with the original
     * records, expecting them to be identical.
     *
     * @throws Exception
     */
    @Test
    public void testWriteAndReadRoundtripConverted() throws Exception {
        InputStream input = getClass().getResourceAsStream(StaticTestRecords.RESOURCES_BRKRTEST_MRC);
       assertNotNull(input);
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        MarcStreamReader marcReader1 = new MarcStreamReader(input);
        MarcStreamWriter marcWriter1 = new MarcStreamWriter(out1, "UTF-8");
        marcWriter1.setConverter(new AnselToUnicode());
        while (marcReader1.hasNext()) {
            Record record = marcReader1.next();
            marcWriter1.write(record);
        }
        input.close();
        marcWriter1.close();
        out1.close();
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(out1.toByteArray());
        MarcStreamReader marcReader2 = new MarcStreamReader(in);
        MarcStreamWriter marcWriter2 = new MarcStreamWriter(out2);
        marcWriter2.setConverter(new UnicodeToAnsel());
        while (marcReader2.hasNext()) {
            Record record = marcReader2.next();
            marcWriter2.write(record);
        }
        in.close();
        marcWriter2.close();
        out2.close();

        InputStream inputCompare1 = getClass().getResourceAsStream(StaticTestRecords.RESOURCES_BRKRTEST_MRC);
        InputStream inputCompare2 = new ByteArrayInputStream(out2.toByteArray());
        MarcReader readComp1 = new MarcStreamReader(inputCompare1);
        MarcReader readComp2 = new MarcStreamReader(inputCompare2);
        Record r1, r2;
        do {
            r1 = (readComp1.hasNext()) ? readComp1.next() : null;
            r2 = (readComp2.hasNext()) ? readComp2.next() : null;
            if (r1 != null && r2 != null)
                RecordTestingUtils.assertEqualsIgnoreLeader(r1, r2);
        } while (r1 != null && r2 != null);
    }
    
    /**
     * This test reads in a file of UTF8 encoded binary Marc records
     * then writes those records out as MARC8 encoded binary Marc records,
     * then reads those records back in and writes them out as UTF8 encoded binary
     * Marc records. The test then compares those records with the original
     * records, expecting them to be identical.  Specifically this tests for handling when 
     * sequence of Multibyte characters has one (or more) characters from G1 character set.
     * e.g.  a center dot punctuation mark (0xA8) between two Chinese characters.
     * Previously the UnicodeToAnsel conversion would produce output in this situation 
     * that the AnselToUnicode converter would claim had errors.
     *
     * @throws Exception
     */
    @Test
    public void testWriteAndReadRoundtripChineseConverted() throws Exception {
        InputStream input = getClass().getResourceAsStream(StaticTestRecords.RESOURCES_CHINESE_WITH_CENTRAL_DOT_MRC);
        assertNotNull(input);
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        MarcStreamReader marcReader1 = new MarcStreamReader(input);
        MarcStreamWriter marcWriter1 = new MarcStreamWriter(out1);
        marcWriter1.setConverter(new UnicodeToAnsel());
        while (marcReader1.hasNext()) {
            Record record = marcReader1.next();
            marcWriter1.write(record);
        }
        input.close();
        marcWriter1.close();
        out1.close();
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(out1.toByteArray());
        MarcStreamReader marcReader2 = new MarcStreamReader(in);
        MarcStreamWriter marcWriter2 = new MarcStreamWriter(out2, "UTF-8");
        marcWriter2.setConverter(new AnselToUnicode());
        while (marcReader2.hasNext()) {
            Record record = marcReader2.next();
            marcWriter2.write(record);
        }
        in.close();
        marcWriter2.close();
        out2.close();

        InputStream inputCompare1 = getClass().getResourceAsStream(StaticTestRecords.RESOURCES_CHINESE_WITH_CENTRAL_DOT_MRC);
        InputStream inputCompare2 = new ByteArrayInputStream(out2.toByteArray());
        MarcReader readComp1 = new MarcStreamReader(inputCompare1);
        MarcReader readComp2 = new MarcStreamReader(inputCompare2);
        Record r1, r2;
        do {
            r1 = (readComp1.hasNext()) ? readComp1.next() : null;
            r2 = (readComp2.hasNext()) ? readComp2.next() : null;
            if (r1 != null && r2 != null)
                RecordTestingUtils.assertEqualsIgnoreLeader(r1, r2);
        } while (r1 != null && r2 != null);
    }
    
    /**
     * This test reads in a file of UTF8 encoded binary Marc records
     * then writes those records out as MARC8 encoded binary Marc records,
     * then reads those records back in and writes them out as UTF8 encoded binary
     * Marc records. The test then compares those records with the original
     * records, expecting them to be identical.  Specifically this tests for handling when 
     * sequence of Multibyte characters has one (or more) characters from G1 character set.
     * e.g.  a center dot punctuation mark (0xA8) between two Chinese characters.
     * Previously the UnicodeToAnsel conversion would produce output in this situation 
     * that the AnselToUnicode converter would claim had errors.
     *
     * @throws Exception
     */
    @Test
    public void testWriteAndReadRoundtripChineseConvertedPermissive() throws Exception {
        InputStream input = getClass().getResourceAsStream(StaticTestRecords.RESOURCES_CHINESE_WITH_CENTRAL_DOT_MRC);
        assertNotNull(input);
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        MarcStreamReader marcReader1 = new MarcStreamReader(input);
        MarcStreamWriter marcWriter1 = new MarcStreamWriter(out1);
        marcWriter1.setConverter(new UnicodeToAnsel());
        while (marcReader1.hasNext()) {
            Record record = marcReader1.next();
            marcWriter1.write(record);
        }
        input.close();
        marcWriter1.close();
        out1.close();
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(out1.toByteArray());
        MarcReader marcReader2 = new MarcPermissiveStreamReader(in, true, true);
        MarcStreamWriter marcWriter2 = new MarcStreamWriter(out2, "UTF-8");
        //marcWriter2.setConverter(new AnselToUnicode());
        while (marcReader2.hasNext()) {
            Record record = marcReader2.next();
            marcWriter2.write(record);
        }
        in.close();
        marcWriter2.close();
        out2.close();

        InputStream inputCompare1 = getClass().getResourceAsStream(StaticTestRecords.RESOURCES_CHINESE_WITH_CENTRAL_DOT_MRC);
        InputStream inputCompare2 = new ByteArrayInputStream(out2.toByteArray());
        MarcReader readComp1 = new MarcStreamReader(inputCompare1);
        MarcReader readComp2 = new MarcStreamReader(inputCompare2);
        Record r1, r2;
        do {
            r1 = (readComp1.hasNext()) ? readComp1.next() : null;
            r2 = (readComp2.hasNext()) ? readComp2.next() : null;
            if (r1 != null && r2 != null)
                RecordTestingUtils.assertEqualsIgnoreLeader(r1, r2);
        } while (r1 != null && r2 != null);
    }

    /**
     * This test reads in a file of Marc8 encoded binary Marc records
     * then writes those records out as utf-8 encoded MarcXML records,
     * then reads those records back in and writes them out as Marc8 binary
     * Marc records. The test then compares those binary Marc records with the original
     * records, expecting them to be identical.
     *
     * @throws Exception
     */
    @Test
    public void testConvertToXMLRoundtrip() throws Exception {
        InputStream input = getClass().getResourceAsStream(StaticTestRecords.RESOURCES_BRKRTEST_MRC);
       assertNotNull(input);
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        MarcStreamReader marcReader = new MarcStreamReader(input);
        MarcXmlWriter xmlWriter = new MarcXmlWriter(out1);
        xmlWriter.setConverter(new AnselToUnicode());
        while (marcReader.hasNext()) {
            Record record = marcReader.next();
            xmlWriter.write(record);
        }
        input.close();
        xmlWriter.close();
        out1.close();
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(out1.toByteArray());
        MarcXmlReader xmlReader = new MarcXmlReader(in);
        MarcStreamWriter marcWriter = new MarcStreamWriter(out2);
        marcWriter.setConverter(new UnicodeToAnsel());
        while (xmlReader.hasNext()) {
            Record record = xmlReader.next();
            marcWriter.write(record);
        }
        in.close();
        marcWriter.close();

        out2.close();
        ByteArrayOutputStream out3 = new ByteArrayOutputStream();
        ByteArrayInputStream in2 = new ByteArrayInputStream(out2.toByteArray());
        MarcStreamReader marcReader2 = new MarcStreamReader(in2);
        MarcXmlWriter xmlWriter2 = new MarcXmlWriter(out3);
        xmlWriter2.setConverter(new AnselToUnicode());
        while (marcReader2.hasNext()) {
            Record record = marcReader2.next();
            xmlWriter2.write(record);
        }
        in2.close();
        xmlWriter2.close();

        out3.close();

        InputStream inputCompare1 = new ByteArrayInputStream(out1.toByteArray());
        InputStream inputCompare2 = new ByteArrayInputStream(out3.toByteArray());
        MarcXmlReader readComp1 = new MarcXmlReader(inputCompare1);
        MarcXmlReader readComp2 = new MarcXmlReader(inputCompare2);
        Record r1, r2;
        do {
            r1 = (readComp1.hasNext()) ? readComp1.next() : null;
            r2 = (readComp2.hasNext()) ? readComp2.next() : null;
            if (r1 != null && r2 != null)
                RecordTestingUtils.assertEqualsIgnoreLeader(r1, r2);
        } while (r1 != null && r2 != null);
    }

    /**
     * This test reads in a file of Marc8 encoded binary Marc records
     * then writes those records out as utf-8 encoded MarcXML records using unicode
     * normalization, which combines diacritics with the character they adorn (whenever
     * possible).  It then reads those records back in and writes them out as Marc8 binary
     * Marc records. The test then compares those binary Marc records with original binary
     * Marc records, expecting them to be identical.
     * Note: Since there are multiple ways of representing some unicode characters in marc8,
     * it is not possible to guarantee roundtripping from marc8 encoded records to utf-8 and
     * back to marc8, the likelihood is even higher when the utf-8 characters are normalized.
     * It is possible to guarantee roundtripping from normalized utf-8 encoded records to
     * marc8 encoded binary marc records back to normalized utf-8 records.
     *
     * @throws Exception
     */
    @Test
    public void testConvertToXMLNormalizedRoundtrip() throws Exception {
        InputStream input = getClass().getResourceAsStream(StaticTestRecords.RESOURCES_BRKRTEST_MRC);
        assertNotNull(input);
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        MarcStreamReader marcReader = new MarcStreamReader(input);
        MarcXmlWriter xmlWriter = new MarcXmlWriter(out1);
        xmlWriter.setConverter(new AnselToUnicode());
        xmlWriter.setUnicodeNormalization(true);
        while (marcReader.hasNext()) {
            Record record = marcReader.next();
            xmlWriter.write(record);
        }
        input.close();
        xmlWriter.close();
        out1.close();
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(out1.toByteArray());
        MarcXmlReader xmlReader = new MarcXmlReader(in);
        MarcStreamWriter marcWriter = new MarcStreamWriter(out2);
        marcWriter.setConverter(new UnicodeToAnsel());
        while (xmlReader.hasNext()) {
            Record record = xmlReader.next();
            marcWriter.write(record);
        }
        in.close();
        marcWriter.close();

        out2.close();
        InputStream inputCompare1 = getClass().getResourceAsStream(StaticTestRecords.RESOURCES_BRKRTEST_MRC);
        assertNotNull(inputCompare1);
        InputStream inputCompare2 = new ByteArrayInputStream(out2.toByteArray());
        MarcStreamReader readComp1 = new MarcStreamReader(inputCompare1);
        MarcStreamReader readComp2 = new MarcStreamReader(inputCompare2);
        Record r1, r2;
        do {
            r1 = (readComp1.hasNext()) ? readComp1.next() : null;
            r2 = (readComp2.hasNext()) ? readComp2.next() : null;
            if (r1 != null && r2 != null)
                RecordTestingUtils.assertEqualsIgnoreLeader(r1, r2);
        } while (r1 != null && r2 != null);

    }
    
    /**
     * This test reads in a file of utf-8 encoded MarcXML records
     * then writes those records out as marc8 encoded binary records using numeric characters 
     * representations (NCR) instead of the standard marc-8 encodings.  It then reads those records 
     * back in and writes them out as utf-8 encoded MarcXML records. The test then compares those MarcXML records 
     * with the utf-8 encoded MarcXML records, expecting them to be identical.
     *
     * @throws Exception
     */
    @Test
    public void testConvertToMarc8NCRRoundtrip() throws Exception {
        InputStream input = getClass().getResourceAsStream(StaticTestRecords.RESOURCES_OCLC814388508_XML);
        assertNotNull(input);
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        MarcXmlReader marcReader1 = new MarcXmlReader(input);
        MarcStreamWriter marcWriter1 = new MarcStreamWriter(out1);
        marcWriter1.setConverter(new UnicodeToAnsel(true));
        while (marcReader1.hasNext()) {
            Record record = marcReader1.next();
            marcWriter1.write(record);
        }
        input.close();
        marcWriter1.close();
        out1.close();
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(out1.toByteArray());
        MarcStreamReader marcReader2 = new MarcStreamReader(in);
        MarcXmlWriter marcWriter2 = new MarcXmlWriter(out2);
        AnselToUnicode conv = new AnselToUnicode();
        conv.setTranslateNCR(true);
        marcWriter2.setConverter(conv);
        while (marcReader2.hasNext()) {
            Record record = marcReader2.next();
            marcWriter2.write(record);
        }
        in.close();
        marcWriter2.close();
        out2.close();

        InputStream inputCompare1 = getClass().getResourceAsStream(StaticTestRecords.RESOURCES_OCLC814388508_XML);
        InputStream inputCompare2 = new ByteArrayInputStream(out2.toByteArray());
        MarcReader readComp1 = new MarcXmlReader(inputCompare1);
        MarcReader readComp2 = new MarcXmlReader(inputCompare2);
        Record r1, r2;
        do {
            r1 = (readComp1.hasNext()) ? readComp1.next() : null;
            r2 = (readComp2.hasNext()) ? readComp2.next() : null;
            if (r1 != null && r2 != null)
                RecordTestingUtils.assertEqualsIgnoreLeader(r1, r2);
        } while (r1 != null && r2 != null);
    }


}
