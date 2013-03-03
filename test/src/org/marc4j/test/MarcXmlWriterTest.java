package org.marc4j.test;

import org.junit.Test;
import org.marc4j.MarcException;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcXmlWriter;
import org.marc4j.converter.impl.AnselToUnicode;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.test.utils.StaticTestRecords;
import org.marc4j.test.utils.TestUtils;

import java.io.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MarcXmlWriterTest {

    @Test
    public void testMarcXmlWriter() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MarcXmlWriter writer = new MarcXmlWriter(out, true);
        for (Record record : StaticTestRecords.summerland) {
            writer.write(record);
        }
        writer.close();
        TestUtils.validateStringAgainstFile(new String(out.toByteArray()), StaticTestRecords.RESOURCES_SUMMERLAND_XML);
    }

    @Test
    public void testMarcXmlWriterNormalized() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        InputStream input = getClass().getResourceAsStream(StaticTestRecords.RESOURCES_BRKRTEST_MRC);
        assertNotNull(input);
        MarcXmlWriter writer = new MarcXmlWriter(out, true);
        writer.setConverter(new AnselToUnicode());
        MarcStreamReader reader = new MarcStreamReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();
            writer.write(record);
        }
        input.close();
        writer.close();
        BufferedReader testoutput = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(out.toByteArray()), "UTF-8"));
        String line;
        while ((line = testoutput.readLine()) != null) {
            if (line.matches("[ ]*<subfield code=\"a\">This is a test of diacritics.*")) {
                String lineParts[] = line.split(", ");
                for (String linePart : lineParts) {
                    if (linePart.startsWith("the tilde in "))
                        assertTrue("Incorrect value for tilde", linePart.equals("the tilde in man\u0303ana"));
                    else if (linePart.startsWith("the grave accent in "))
                        assertTrue("Incorrect value for grave", linePart.equals("the grave accent in tre\u0300s"));
                    else if (linePart.startsWith("the acute accent in "))
                        assertTrue("Incorrect value for acute", linePart.equals("the acute accent in de\u0301sire\u0301e"));
                    else if (linePart.startsWith("the circumflex in "))
                        assertTrue("Incorrect value for macron", linePart.equals("the circumflex in co\u0302te"));
                    else if (linePart.startsWith("the macron in "))
                        assertTrue("Incorrect value for macron", linePart.equals("the macron in To\u0304kyo"));
                    else if (linePart.startsWith("the breve in "))
                        assertTrue("Incorrect value for breve", linePart.equals("the breve in russkii\u0306"));
                    else if (linePart.startsWith("the dot above in "))
                        assertTrue("Incorrect value for dot above", linePart.equals("the dot above in z\u0307aba"));
                    else if (linePart.startsWith("the dieresis (umlaut) in "))
                        assertTrue("Incorrect value for umlaut", linePart.equals("the dieresis (umlaut) in Lo\u0308wenbra\u0308u"));
                }
            }
        }
        testoutput.close();
    }

    @Test(expected = MarcException.class)
    public void testWriteOfRecordWithIndicatorlessSubfield() throws Exception {
        Record record = StaticTestRecords.getSummerlandRecord();
        MarcFactory factory = StaticTestRecords.getFactory();
        DataField badField = factory.newDataField();
        badField.setTag("911");
        badField.addSubfield(factory.newSubfield('a', "HAZMARC - INDICATORLESS FIELD DETECTED - MOPP LEVEL 4"));
        record.addVariableField(badField);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MarcXmlWriter writer = new MarcXmlWriter(out, true);
        writer.write(record);
        writer.close();
    }

    @Test
    public void testMarcXmlWriterConvertedToUTF8AndNormalized() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        InputStream input = getClass().getResourceAsStream(StaticTestRecords.RESOURCES_BRKRTEST_MRC);
        assertNotNull(input);
        MarcXmlWriter writer = new MarcXmlWriter(out, true);
        writer.setConverter(new AnselToUnicode());
        writer.setUnicodeNormalization(true);
        MarcStreamReader reader = new MarcStreamReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();
            writer.write(record);
        }
        input.close();
        writer.close();
        BufferedReader testoutput = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(out.toByteArray()), "UTF-8"));
        String line;
        while ((line = testoutput.readLine()) != null) {
            if (line.matches("[ ]*<subfield code=\"a\">This is a test of diacritics.*")) {
                String lineParts[] = line.split(", ");
                for (int i = 0; i < lineParts.length; i++) {
                    if (lineParts[i].startsWith("the tilde in "))
                        assertTrue("Incorrect normalized value for tilde accent", lineParts[i].equals("the tilde in ma\u00F1ana"));
                    else if (lineParts[i].startsWith("the grave accent in "))
                        assertTrue("Incorrect normalized value for grave accent", lineParts[i].equals("the grave accent in tr\u00E8s"));
                    else if (lineParts[i].startsWith("the acute accent in "))
                        assertTrue("Incorrect normalized value for acute accent", lineParts[i].equals("the acute accent in d\u00E9sir\u00E9e"));
                    else if (lineParts[i].startsWith("the circumflex in "))
                        assertTrue("Incorrect normalized value for circumflex", lineParts[i].equals("the circumflex in c\u00F4te"));
                    else if (lineParts[i].startsWith("the macron in "))
                        assertTrue("Incorrect normalized value for macron", lineParts[i].equals("the macron in T\u014Dkyo"));
                    else if (lineParts[i].startsWith("the breve in "))
                        assertTrue("Incorrect normalized value for breve", lineParts[i].equals("the breve in russki\u012D"));
                    else if (lineParts[i].startsWith("the dot above in "))
                        assertTrue("Incorrect normalized value for dot above", lineParts[i].equals("the dot above in \u017Caba"));
                    else if (lineParts[i].startsWith("the dieresis (umlaut) in "))
                        assertTrue("Incorrect normalized value for umlaut", lineParts[i].equals("the dieresis (umlaut) in L\u00F6wenbr\u00E4u"));
                }
            }
        }
    }
}
