package org.marc4j.test;

import java.io.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.marc4j.MarcJsonReader;
import org.marc4j.MarcJsonWriter;
import org.marc4j.MarcStreamReader;
import org.marc4j.converter.impl.AnselToUnicode;
import org.marc4j.marc.Record;

public class JsonWriterTest extends TestCase {

    private static File createTempFile() throws IOException {
        File file = File.createTempFile("WriterTest","tmp");
        file.deleteOnExit();
        return file;
    }
        
    public void testMarcInJsonWriterIndented() throws Exception 
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        OutputStream out = new BufferedOutputStream(buffer);
        InputStream input = getClass().getResourceAsStream("resources/marc-json.json");
        MarcJsonWriter writer = new MarcJsonWriter(out, MarcJsonWriter.MARC_IN_JSON);
        writer.setIndent(true);
        MarcJsonReader reader = new MarcJsonReader(input);
        while (reader.hasNext()) 
        {
            Record record = reader.next();
            writer.write(record);
        }
        input.close();
        writer.close();
        InputStream input2 = getClass().getResourceAsStream("resources/marc-in-json.json");
        BufferedReader reader1 = new BufferedReader(new InputStreamReader(input2, "UTF-8"));
        BufferedReader reader2 = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buffer.toByteArray()), "UTF-8"));
        String str1, str2;
        while ((str1 = reader1.readLine()) != null && (str2 = reader2.readLine()) != null)
        {
//            if (!str1.equals(str2))
//                str1 = str1;
            assertEquals("Mismatch in expected output", str1, str2);
        }
        reader1.close();
        reader2.close();
    }

    
    public void testMarcJsonWriterIndented() throws Exception 
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        OutputStream out = new BufferedOutputStream(buffer);
        InputStream input = getClass().getResourceAsStream("resources/marc-in-json.json");
        MarcJsonWriter writer = new MarcJsonWriter(out, MarcJsonWriter.MARC_JSON);
        writer.setIndent(true);
        MarcJsonReader reader = new MarcJsonReader(input);
        while (reader.hasNext()) 
        {
            Record record = reader.next();
            writer.write(record);
        }
        input.close();
        writer.close();
        InputStream input2 = getClass().getResourceAsStream("resources/marc-json.json");
        BufferedReader reader1 = new BufferedReader(new InputStreamReader(input2, "UTF-8"));
        BufferedReader reader2 = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buffer.toByteArray()), "UTF-8"));
        String str1, str2;
        while ((str1 = reader1.readLine()) != null && (str2 = reader2.readLine()) != null)
        {
//            if (!str1.equals(str2))
//                str1 = str1;
            assertEquals("Mismatch in expected output", str1, str2);
        }
        reader1.close();
        reader2.close();

    }

    public void testMarcJsonWriterConvertedToUTF8() throws Exception 
    {
        File tmpFile = createTempFile();
        OutputStream out = new BufferedOutputStream(new FileOutputStream(tmpFile));

        InputStream input = getClass().getResourceAsStream("resources/brkrtest.mrc");
        MarcJsonWriter writer = new MarcJsonWriter(out);
        writer.setIndent(true);
        writer.setConverter(new AnselToUnicode());
        MarcStreamReader reader = new MarcStreamReader(input);
        while (reader.hasNext()) 
        {
            Record record = reader.next();
            writer.write(record);
        }
        input.close();
        writer.close();
        BufferedReader testoutput = new BufferedReader(new InputStreamReader(new FileInputStream(tmpFile)));
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

    public void testMarcJsonWriterConvertedToUTF8AndNormalized() throws Exception 
    {
        File tmpFile = createTempFile();
        OutputStream out = new BufferedOutputStream(new FileOutputStream(tmpFile));

        InputStream input = getClass().getResourceAsStream("resources/brkrtest.mrc");
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
        input.close();
        writer.close();
        BufferedReader testoutput = new BufferedReader(new InputStreamReader(new FileInputStream(tmpFile), "UTF-8"));
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
    
    
//    public void testJsonWriteAndRead2() throws Exception 
//    {
//        File tmpFile = createTempFile();
//        OutputStream outFile = new BufferedOutputStream(new FileOutputStream(tmpFile));
//        InputStream input = getClass().getResourceAsStream("resources/marc-json.json");
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        MarcJsonWriter writer = new MarcJsonWriter(out, MarcJsonWriter.MARC_JSON);
//        MarcJsonReader reader = new MarcJsonReader(input);
//        while (reader.hasNext()) 
//        {
//            Record record = reader.next();
//            writer.write(record);
//        }
//        input.close();
//        writer.close();
//
//        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
//        MarcJsonReader marcReader = new MarcJsonReader(in);
//        MarcJsonWriter marcWriter = new MarcJsonWriter(outFile, MarcJsonWriter.MARC_JSON);
//        marcWriter.setIndent(true);
//        while (marcReader.hasNext()) {
//            Record record = marcReader.next();
//            marcWriter.write(record);
//        }
//        in.close();
//        marcWriter.close();
//
//        out.close();
//        fail("Test incomplete - does not validate output");
//
//    }

    
    public static Test suite() {
        return new TestSuite(JsonWriterTest.class);
    }

    public static void main(String args[]) {
        TestRunner.run(suite());
    }
}
