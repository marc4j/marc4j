package org.marc4j.test;

import org.junit.Test;
import org.marc4j.Marc4jConfig;
import org.marc4j.NCR_FORMAT;
import org.marc4j.converter.impl.UnicodeUtils;

import static org.junit.Assert.assertEquals;

public class UnicodeUtilsTest {

    @Test
    public void test_convertNCRToUnicode_happyPaths() {
        String source = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder buffer = new StringBuilder(source);
        UnicodeUtils.convertNCRToUnicode(buffer);
        assertEquals(source, buffer.toString());

        // Test Marc NCR format
        source = "abcdef&#x0021;ghijkl"; // 0021 is encoded "!"
        buffer = new StringBuilder(source);
        UnicodeUtils.convertNCRToUnicode(buffer);
        assertEquals("abcdef!ghijkl", buffer.toString());

        // Test Unicode format
        source = "abcdef<U+0021>ghijkl"; // 0021 is encoded "!"
        buffer = new StringBuilder(source);
        UnicodeUtils.convertNCRToUnicode(buffer);
        assertEquals("abcdef!ghijkl", buffer.toString());

        // Test combination and multiples
        source = "&#x0021;a<U+0021>b&#x0023;c<U+0023>"; // 0021 is encoded "!", 0023 is encoded "#"
        buffer = new StringBuilder(source);
        UnicodeUtils.convertNCRToUnicode(buffer);
        assertEquals("!a!b#c#", buffer.toString());

        // Test upper and lower case hex
        source = "&#x002C;<U+002C>"; // comma, upper case hex
        buffer = new StringBuilder(source);
        UnicodeUtils.convertNCRToUnicode(buffer);
        assertEquals(",,", buffer.toString());

        source = "&#x002c;<U+002c>"; // comma, lower case hex
        buffer = new StringBuilder(source);
        UnicodeUtils.convertNCRToUnicode(buffer);
        assertEquals(",,", buffer.toString());
    }

    @Test
    public void test_convertNCRToUnicode_nonHexPath() {
        // Test Marc NCR format
        String source = "abcdef&#x002G;ghijkl"; // 002G is invalid hex
        StringBuilder buffer = new StringBuilder(source);
        UnicodeUtils.convertNCRToUnicode(buffer);
        assertEquals(source, buffer.toString());

        // Test Unicode format
        source = "abcdef<U+002G>ghijkl"; // 002G is invalid hex
        buffer = new StringBuilder(source);
        UnicodeUtils.convertNCRToUnicode(buffer);
        assertEquals("abcdef<U+002G>ghijkl", buffer.toString());

        // Test combination and multiples
        source = "&#x0021;a<U+002G>b&#x002G;c<U+0023>"; // 002G is invalid hex
        buffer = new StringBuilder(source);
        UnicodeUtils.convertNCRToUnicode(buffer);
        assertEquals("!a<U+002G>b&#x002G;c#", buffer.toString());

    }

    @Test
    public void test_convertUnicodeToNCR_Marc8NCR() {
        Marc4jConfig.setNCR_format(NCR_FORMAT.MARC8_NCR);

        Character ch = '!';
        String res = UnicodeUtils.convertUnicodeToNCR(ch);
        assertEquals("&#x0021;", res);

        ch = ',';
        res = UnicodeUtils.convertUnicodeToNCR(ch);
        assertEquals("&#x002C;", res); // checks hex is Upper Cased
    }

    @Test
    public void test_convertUnicodeToNCR_UnicodeBNF() {
        Marc4jConfig.setNCR_format(NCR_FORMAT.UNICODE_BNF);

        Character ch = '!';
        String res = UnicodeUtils.convertUnicodeToNCR(ch);
        assertEquals("<U+0021>", res);

        ch = ',';
        res = UnicodeUtils.convertUnicodeToNCR(ch);
        assertEquals("<U+002C>", res); // checks hex is upper cased
    }

    @Test
    public void test_convertUnicodeToUnicodeNCR() {
        Character ch = ',';
        String res = UnicodeUtils.convertUnicodeToUnicodeBNF(ch);
        assertEquals("<U+002C>", res); // checks hex is upper cased
    }
}
