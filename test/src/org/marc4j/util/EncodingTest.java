package org.marc4j.util;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EncodingTest {

    @Test
    public void testGet() {
        assertEquals(Encoding.MARC8, Encoding.get("MARC8"));
        assertEquals(Encoding.MARC8, Encoding.get("MARC-8"));
        assertEquals(Encoding.UTF8, Encoding.get("UTF8"));
        assertEquals(Encoding.UTF8, Encoding.get("UTF-8"));
        assertEquals(Encoding.ISO8859_1, Encoding.get("ISO8859_1"));
        assertEquals(Encoding.ISO8859_1, Encoding.get("ISO-8859-1"));
        assertEquals(Encoding.ISO8859_1, Encoding.get("ISO_8859_1"));
        assertNull(Encoding.get("non-existent"));
    }

    @Test
    public void testValues() {
        Encoding[] encodings = Encoding.values();
        assertEquals(3, encodings.length);
        assertEquals(Encoding.UTF8, encodings[0]);
        assertEquals(Encoding.MARC8, encodings[1]);
        assertEquals(Encoding.ISO8859_1, encodings[2]);
    }

    @Test
    public void testValueOf() {
        assertEquals(Encoding.MARC8, Encoding.valueOf("MARC8"));
        assertEquals(Encoding.UTF8, Encoding.valueOf("UTF8"));
        assertEquals(Encoding.ISO8859_1, Encoding.valueOf("ISO8859_1"));
    }

    @Test
    public void testGetStandardName() {
        assertEquals("MARC-8", Encoding.MARC8.getStandardName());
        assertEquals("UTF-8", Encoding.UTF8.getStandardName());
        assertEquals("ISO-8859-1", Encoding.ISO8859_1.getStandardName());
    }

    @Test
    public void testGetNames() {
        assertEquals(Arrays.asList("MARC-8", "MARC8"), Encoding.MARC8.getNames());
        assertEquals(Arrays.asList("UTF-8", "UTF8"), Encoding.UTF8.getNames());
        assertEquals(Arrays.asList("ISO-8859-1", "ISO8859_1", "ISO_8859_1"), Encoding.ISO8859_1.getNames());
    }
}