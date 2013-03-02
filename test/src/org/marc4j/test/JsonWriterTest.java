package org.marc4j.test;

import org.junit.Test;
import org.marc4j.MarcJsonReader;
import org.marc4j.MarcJsonWriter;
import org.marc4j.MarcStreamReader;
import org.marc4j.converter.impl.AnselToUnicode;
import org.marc4j.marc.Record;
import org.marc4j.test.utils.StaticTestRecords;
import org.marc4j.test.utils.TestUtils;

import java.io.*;

import static org.junit.Assert.*;

public class JsonWriterTest  {

    @Test
    public void testMarcInJsonWriter() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MarcJsonWriter writer = new MarcJsonWriter(out, MarcJsonWriter.MARC_IN_JSON);
        Record record = getSummerlandRecord();
        writer.write(record);
        TestUtils.validateBytesAgainstFile(out.toByteArray(), StaticTestRecords.RESOURCES_SUMMERLAND_MARC_IN_JSON_JSON);
        writer.close();
    }

    @Test
    public void testMarcInJsonWriterIndented() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Record record = getSummerlandRecord();
        MarcJsonWriter writer = new MarcJsonWriter(out, MarcJsonWriter.MARC_IN_JSON);
        writer.setIndent(true);
        writer.write(record);
        TestUtils.validateBytesAgainstFile(out.toByteArray(), StaticTestRecords.RESOURCES_SUMMERLAND_MARC_IN_JSON_INDENTED_JSON);
        writer.close();
    }

    @Test
    public void testMarcJsonWriter() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Record record = getSummerlandRecord();
        MarcJsonWriter writer = new MarcJsonWriter(out, MarcJsonWriter.MARC_JSON);
        writer.write(record);
        TestUtils. validateBytesAgainstFile(out.toByteArray(), StaticTestRecords.RESOURCES_SUMMERLAND_MARC_JSON_JSON);
        writer.close();

    }

    @Test
    public void testMarcJsonWriterIndented() throws Exception {
        Record record = getSummerlandRecord();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MarcJsonWriter writer = new MarcJsonWriter(out, MarcJsonWriter.MARC_JSON);
        writer.setIndent(true);
        writer.write(record);
        TestUtils.validateBytesAgainstFile(out.toByteArray(), StaticTestRecords.RESOURCES_SUMMERLAND_INDENTED_MARC_JSON_JSON);
        writer.close();
    }

    @Test
    public void testMarcJsonWriterConvertedToUTF8() throws Exception 
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
    
        InputStream input = getClass().getResourceAsStream(StaticTestRecords.RESOURCES_BRKRTEST_MRC);
        assertNotNull(StaticTestRecords.RESOURCES_BRKRTEST_MRC,input);
        MarcJsonWriter writer = new MarcJsonWriter(out);
        writer.setIndent(true);
        writer.setConverter(new AnselToUnicode());
        MarcStreamReader reader = new MarcStreamReader(input);
        while (reader.hasNext()) 
        {
            Record record = reader.next();
            writer.write(record);
        }
        writer.close();
    
        BufferedReader testoutput = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(out.toByteArray()), "UTF-8"));
        String line;
        while ((line = testoutput.readLine()) != null)
        {
            if (line.matches("[ ]*\"a\":\"This is a test of diacritics.*"))
            {
                String lineParts[] = line.split(", ");
                for (int i = 0; i < lineParts.length; i++)
                {
                    if (lineParts[i].startsWith("the tilde in "))
                        assertTrue("Incorrect value for tilde", lineParts[i].equals("the tilde in man\\u0303ana"));
                    else if (lineParts[i].startsWith("the grave accent in "))
                        assertTrue("Incorrect value for grave", lineParts[i].equals("the grave accent in tre\\u0300s"));
                    else if (lineParts[i].startsWith("the acute accent in "))
                        assertTrue("Incorrect value for acute", lineParts[i].equals("the acute accent in de\\u0301sire\\u0301e"));
                    else if (lineParts[i].startsWith("the circumflex in "))
                        assertTrue("Incorrect value for macron", lineParts[i].equals("the circumflex in co\\u0302te"));
                    else if (lineParts[i].startsWith("the macron in "))
                        assertTrue("Incorrect value for macron", lineParts[i].equals("the macron in To\\u0304kyo"));
                    else if (lineParts[i].startsWith("the breve in "))
                        assertTrue("Incorrect value for breve", lineParts[i].equals("the breve in russkii\\u0306"));
                    else if (lineParts[i].startsWith("the dot above in "))
                        assertTrue("Incorrect value for dot above", lineParts[i].equals("the dot above in z\\u0307aba"));
                    else if (lineParts[i].startsWith("the dieresis (umlaut) in "))
                        assertTrue("Incorrect value for umlaut", lineParts[i].equals("the dieresis (umlaut) in Lo\\u0308wenbra\\u0308u"));
                }
            }
        }
          testoutput.close();
    }

    @Test
    public void testMarcJsonWriterConvertedToUTF8AndNormalized() throws Exception 
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        InputStream input = getClass().getResourceAsStream(StaticTestRecords.RESOURCES_BRKRTEST_MRC);
        assertNotNull(StaticTestRecords.RESOURCES_BRKRTEST_MRC,input);
        MarcJsonWriter writer = new MarcJsonWriter(out);
        writer.setIndent(true);
        writer.setConverter(new AnselToUnicode());
        writer.setUnicodeNormalization(true);
        MarcStreamReader reader = new MarcStreamReader(input);
        while (reader.hasNext()) 
        {
            Record record = reader.next();
            writer.write(record);
        }
        writer.close();

        BufferedReader testoutput = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(out.toByteArray()), "UTF-8"));
        String line;
        while ((line = testoutput.readLine()) != null)
        {
            if (line.matches("[ ]*\"a\":\"This is a test of diacritics.*"))
            {
                String lineParts[] = line.split(", ");
                for (int i = 0; i < lineParts.length; i++)
                {
                    if (lineParts[i].startsWith("the tilde in "))
                        assertTrue("Incorrect normalized value for tilde accent", lineParts[i].equals("the tilde in ma\u00f1ana"));
                    else if (lineParts[i].startsWith("the grave accent in "))
                        assertTrue("Incorrect normalized value for grave accent", lineParts[i].equals("the grave accent in tr\u00e8s"));
                    else if (lineParts[i].startsWith("the acute accent in "))
                        assertTrue("Incorrect normalized value for acute accent", lineParts[i].equals("the acute accent in d\u00e9sir\u00E9e"));
                    else if (lineParts[i].startsWith("the circumflex in "))
                        assertTrue("Incorrect normalized value for circumflex", lineParts[i].equals("the circumflex in c\u00f4te"));
                    else if (lineParts[i].startsWith("the macron in "))
                        assertTrue("Incorrect normalized value for macron", lineParts[i].equals("the macron in T\\u014dkyo"));
                    else if (lineParts[i].startsWith("the breve in "))
                        assertTrue("Incorrect normalized value for breve", lineParts[i].equals("the breve in russki\\u012d"));
                    else if (lineParts[i].startsWith("the dot above in "))
                        assertTrue("Incorrect normalized value for dot above", lineParts[i].equals("the dot above in \\u017caba"));
                    else if (lineParts[i].startsWith("the dieresis (umlaut) in "))
                        assertTrue("Incorrect normalized value for umlaut", lineParts[i].equals("the dieresis (umlaut) in L\u00f6wenbr\u00e4u"));
                }
            }
        }
        testoutput.close();
    }

    @Test
    public void testJsonWriteAndRead() throws Exception {
        Record record = getJSONRecordFromFile(StaticTestRecords.RESOURCES_LEGAL_JSON_MARC_IN_JSON_JSON);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MarcJsonWriter writer = new MarcJsonWriter(out);
        writer.write(record);
        writer.close();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        MarcJsonReader marcReader = new MarcJsonReader(in);
        assertTrue(marcReader.hasNext());
        record = marcReader.next();
        TestUtils.validateFreewheelingBobDylanRecord(record);
        assertFalse(marcReader.hasNext());
        in.close();
    }

    @Test
    public void testJsonWriteAndRead2() throws Exception {
        String fileName = StaticTestRecords.RESOURCES_MARC_JSON_JSON;
        Record record = getJSONRecordFromFile(fileName);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MarcJsonWriter writer = new MarcJsonWriter(out, MarcJsonWriter.MARC_JSON);
        writer.write(record);
        writer.close();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        MarcJsonReader marcReader = new MarcJsonReader(in);
        assertTrue(marcReader.hasNext());
        TestUtils.validateFreewheelingBobDylanRecord(marcReader.next());
        assertFalse(marcReader.hasNext());
        in.close();
        out.close();
    }

    private Record getJSONRecordFromFile(String fileName) {
        InputStream input = getClass().getResourceAsStream(fileName);
        assertNotNull(fileName,input);
        MarcJsonReader reader = new MarcJsonReader(input);
        assertTrue(reader.hasNext());
        Record record = reader.next();
        TestUtils.validateFreewheelingBobDylanRecord(record);
        assertFalse(reader.hasNext());
        return record;
    }

    private Record getSummerlandRecord() throws IOException {
        InputStream input = getClass().getResourceAsStream(StaticTestRecords.RESOURCES_SUMMERLAND_MRC);
        assertNotNull(StaticTestRecords.RESOURCES_SUMMERLAND_MRC,input);
        MarcStreamReader reader = new MarcStreamReader(input);
        assertTrue("have at least one record", reader.hasNext());
        Record record = reader.next();
        assertFalse("Only one record", reader.hasNext());
        input.close();
        return record;
    }


}