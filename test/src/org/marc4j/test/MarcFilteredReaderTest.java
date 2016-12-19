package org.marc4j.test;

import org.junit.Test;
import org.marc4j.MarcFilteredReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.Record;

import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MarcFilteredReaderTest {

    @Test
    public void testIncludeIfPresentNoPattern() throws Exception {
        InputStream input = getClass().getResourceAsStream("/selectedRecs.mrc");
        assertNotNull(input);
        String[] expectedIds = { "u55", "u89", "u233", "u377" };

        MarcFilteredReader reader = new MarcFilteredReader(new MarcStreamReader(input), "700a", null);
        int cnt = 0;
        while (reader.hasNext())
        {
            Record record = reader.next();
            assertTrue("Wrong count of records (too many)", cnt < expectedIds.length);
            assertTrue("Mismatch of expected record ID", record.getControlNumber().equals(expectedIds[cnt++]));
        }
        assertTrue("Wrong count of records (too few)", cnt == expectedIds.length);
    }

    @Test
    public void testIncludeIfPresentWithPattern() throws Exception {
        InputStream input = getClass().getResourceAsStream("/selectedRecs.mrc");
        assertNotNull(input);
        String[] expectedIds = { "u144", "u233" };

        MarcFilteredReader reader = new MarcFilteredReader(new MarcStreamReader(input), "600a:650a/Ar", null);
        int cnt = 0;
        while (reader.hasNext())
        {
            Record record = reader.next();
            assertTrue("Wrong count of records (too many)", cnt < expectedIds.length);
            assertTrue("Mismatch of expected record ID", record.getControlNumber().equals(expectedIds[cnt++]));
        }
        assertTrue("Wrong count of records (too few)", cnt == expectedIds.length);
    }

    @Test
    public void testIncludeIfMissingNoPattern() throws Exception {
        InputStream input = getClass().getResourceAsStream("/selectedRecs.mrc");
        assertNotNull(input);
        String[] expectedIds = { "u3", "u377" };

        MarcFilteredReader reader = new MarcFilteredReader(new MarcStreamReader(input), null, "600a:650a");
        int cnt = 0;
        while (reader.hasNext())
        {
            Record record = reader.next();
            assertTrue("Wrong count of records (too many)", cnt < expectedIds.length);
            assertTrue("Mismatch of expected record ID", record.getControlNumber().equals(expectedIds[cnt++]));
        }
        assertTrue("Wrong count of records (too few)", cnt == expectedIds.length);
    }

    @Test
    public void testIncludeIfMissingWithPattern() throws Exception {
        InputStream input = getClass().getResourceAsStream("/selectedRecs.mrc");
        assertNotNull(input);
        String[] expectedIds = { "u2", "u3", "u8", "u13", "u21", "u34", "u55", "u89", "u377" };

        MarcFilteredReader reader = new MarcFilteredReader(new MarcStreamReader(input), null, "600a:650a/Ar");
        int cnt = 0;
        while (reader.hasNext())
        {
            Record record = reader.next();
            assertTrue("Wrong count of records (too many)", cnt < expectedIds.length);
            assertTrue("Mismatch of expected record ID", record.getControlNumber().equals(expectedIds[cnt++]));
        }
        assertTrue("Wrong count of records (too few)", cnt == expectedIds.length);
    }

}
