package org.marc4j.test;

import java.io.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.marc4j.MarcStreamReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcXmlReader;
import org.marc4j.MarcXmlWriter;
import org.marc4j.converter.impl.AnselToUnicode;
import org.marc4j.marc.Record;

public class WriterTest extends TestCase {

    public void testMarcStreamWriter() throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MarcStreamWriter writer = new MarcStreamWriter(out);
        for (int i = 0; i < StaticTestRecords.summerland.length; i++)
        {
            writer.write(StaticTestRecords.summerland[i]);
        }
        writer.close();
        TestUtils.validateBytesAgainstFile(out.toByteArray(), "resources/summerland.mrc");
    }

    public void testMarcXmlWriter() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MarcXmlWriter writer = new MarcXmlWriter(out, true);
        for (int i = 0; i < StaticTestRecords.summerland.length; i++)
        {
            writer.write(StaticTestRecords.summerland[i]);
        }
        writer.close();
        TestUtils.validateStringAgainstFile(new String(out.toByteArray()), "resources/summerland.xml");
    }
    
    public void testMarcXmlWriterNormalized() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        InputStream input = getClass().getResourceAsStream("resources/brkrtest.mrc");
        MarcXmlWriter writer = new MarcXmlWriter(out, true);
        writer.setConverter(new AnselToUnicode());
        MarcStreamReader reader = new MarcStreamReader(input);
        while (reader.hasNext()) 
        {
            Record record = reader.next();
            writer.write(record);
        }
        input.close();
        writer.close();
        BufferedReader testoutput = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(out.toByteArray()), "UTF-8"));
        String line;
        while ((line = testoutput.readLine()) != null)
        {
            if (line.matches("[ ]*<subfield code=\"a\">This is a test of diacritics.*"))
            {
                String lineParts[] = line.split(", ");
                for (int i = 0; i < lineParts.length; i++)
                {
                    if (lineParts[i].startsWith("the tilde in "))
                        assertTrue("Incorrect value for tilde", lineParts[i].equals("the tilde in man\u0303ana"));
                    else if (lineParts[i].startsWith("the grave accent in "))
                        assertTrue("Incorrect value for grave", lineParts[i].equals("the grave accent in tre\u0300s"));
                    else if (lineParts[i].startsWith("the acute accent in "))
                        assertTrue("Incorrect value for acute", lineParts[i].equals("the acute accent in de\u0301sire\u0301e"));
                    else if (lineParts[i].startsWith("the circumflex in "))
                        assertTrue("Incorrect value for macron", lineParts[i].equals("the circumflex in co\u0302te"));
                    else if (lineParts[i].startsWith("the macron in "))
                        assertTrue("Incorrect value for macron", lineParts[i].equals("the macron in To\u0304kyo"));
                    else if (lineParts[i].startsWith("the breve in "))
                        assertTrue("Incorrect value for breve", lineParts[i].equals("the breve in russkii\u0306"));
                    else if (lineParts[i].startsWith("the dot above in "))
                        assertTrue("Incorrect value for dot above", lineParts[i].equals("the dot above in z\u0307aba"));
                    else if (lineParts[i].startsWith("the dieresis (umlaut) in "))
                        assertTrue("Incorrect value for umlaut", lineParts[i].equals("the dieresis (umlaut) in Lo\u0308wenbra\u0308u"));
                }
            }
        }
        testoutput.close();
    }

    public void testMarcXmlWriterConvertedToUTF8AndNormalized() throws Exception 
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        InputStream input = getClass().getResourceAsStream("resources/brkrtest.mrc");
        MarcXmlWriter writer = new MarcXmlWriter(out, true);
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
        BufferedReader testoutput = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(out.toByteArray()), "UTF-8"));
        String line;
        while ((line = testoutput.readLine()) != null)
        {
            if (line.matches("[ ]*<subfield code=\"a\">This is a test of diacritics.*"))
            {
                String lineParts[] = line.split(", ");
                for (int i = 0; i < lineParts.length; i++)
                {
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
        testoutput.close();
    }


    public static Test suite() {
        return new TestSuite(WriterTest.class);
    }

    public static void main(String args[]) {
        TestRunner.run(suite());
    }
}
