package org.marc4j.test;

import org.junit.Test;
import org.marc4j.converter.impl.AnselToUnicode;
import org.marc4j.converter.impl.UnicodeToAnsel;
import org.marc4j.converter.impl.UnicodeToUnimarc;
import org.marc4j.converter.impl.UnimarcToUnicode;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertTrue;

/**
 * Substantially, tests the XxxToXxx routines, with optional compose unicode / convert unicode sequence
 * elements set to true (i.e. non-default)
 */
public class ComposeDecomposeSettingsTest {

    @Test
    public void testAnselToUnicode() {
        AnselToUnicode converter = new AnselToUnicode();
        converter.setComposeUnicode(true);
        converter.setShouldConvertUnicodeSequence(true);

        // Test plain ASCII data
        String testdata = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String result = converter.convert(testdata);
        assertTrue("ASCII converstion failed", testdata.equals(result));

        // Test Unicode sequence
        testdata = "Test: <U+0023><U+00C0><U+91D1>";
        result = converter.convert(testdata);
        assertTrue("Unicode escape sequence failed", result.equals("Test: #\u00C0\u91D1"));

        // Test some ANSEL characters
        // AE OE A with Grave
        String marc8 = "\u00A5\u00A6\u00E1A";
        result = converter.convert(marc8);
        assertTrue("ANSEL conversion failed", result.equals("\u00C6\u0152\u00C0"));


        // Test setting Greek G1
        // ESC Greek abc gamma delta ESC ANSEL AE abc
        String greek = "\u001B)Sabc\u00C4\u00C5\u001B)!E\u00A5abc";
        result = converter.convert(greek);
        assertTrue("Greek conversion failed", result.equals("abc\u0393\u0394\u00C6abc"));


        // Test setting Greek as default
        converter.setDefaultG0AndG1(null, ")S");
        greek = "abc\u00C4\u00C5abc";
        result = converter.convert(greek);
        assertTrue("Greek default G1 failed", result.equals("abc\u0393\u0394abc"));

        // Test CJK ; encountered this strange sequence in jhu database; while odd it should probably work
        // notice that it switches to Hebrew instead of ASCII for the punctuation and then uses the ASCII escape
        // at the end to return to ASCII (Technique 1 rather than Technique 2 from manual)
        converter.resetDefaultG0AndG1();
        String cjk = "\u001F6100-01/$1\u001Fa\u001B$,1!]>!`5\u001B,2.\u001Bs\u001E";
        result = converter.convert(cjk);
        assertTrue("CJK conversion failed", result.equals("\u001F6100-01/$1\u001Fa\u91D1\u97FB.\u001E"));
    }

    @Test
    public void testUnimarcToUnicode() {
        UnimarcToUnicode converter = new UnimarcToUnicode();
        converter.setComposeUnicode(true);
        converter.setShouldConvertUnicodeSequence(true);

        // Test plain ASCII data
        String testdata = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String result = converter.convert(testdata);
        assertTrue("ASCII converstion failed", testdata.equals(result));

        // Test Unicode sequence
        testdata = "Test: <U+0023><U+00C0><U+91D1>";
        result = converter.convert(testdata);
        assertTrue("Unicode escape sequence failed", result.equals("Test: #\u00C0\u91D1"));

        // Test some Latin characters
        // AE OE A with Grave
        String unimarc = "\u00E1\u00EA\u00C1A";
        result = converter.convert(unimarc);
        assertTrue("LATIN conversion failed", result.equals("\u00C6\u0152\u00C0"));

        // Test UNIMARC set
        converter.resetDefaultGX();
        converter.setDefaultGX("01", "05", "03", "05");
        unimarc = "[/]\u005E\u00A3\u004F\u00A3\u0055abcdefghijklmnopqrstuvwxyz";
        result = converter.convert(unimarc);
        String expected = "[/]\u005E\u00D6\u00DCabcdefghijklmnopqrstuvwxyz";
        assertTrue("UNIMARC set 0 failed", result.equals(expected));

        converter.resetDefaultGX();
        converter.setDefaultGX("01", "05", "03", "05");
        unimarc = "\u001B\u007D\u00C4\u0061\u007C\u001B\u007E\u00A5\u00A7\u00C1" +   //  0-9
            "\u00A6\u00A7\u00C1\u00A5\u00A1\u00A7\u00C1\u00A6\u00A1\u00A7" +   // 10-19
            "\u00C1\u00A5\u00A2\u00A7\u00C1\u00A6\u00A2\u00A7\u00C1\u00A5" +   // 20-29
            "\u00A4\u00A7\u00C1\u00A6\u00A4\u00A7\u00C1\u00A5\u00A7\u00CA" +   // 30-39
            "\u00A6\u00A7\u00CA\u00A5\u00A1\u00A7\u00CA\u00A6\u00A1\u00A7" +   // 40-49
            "\u00CA\u00A5\u00A2\u00A7\u00CA\u00A6\u00A2\u00A7\u00CA\u00A5" +   // 50-59
            "\u00A4\u00A7\u00CA\u00A6\u00A4\u00A7\u00CA\u00A5\u00A7\u00FD" +   // 60-69
            "\u00A6\u00A7\u00FD\u00A5\u00A1\u00A7\u00FD\u00A6\u00A1\u00A7" +   // 70-79
            "\u00FD\u00A5\u00A2\u00A7\u00FD\u00A6\u00A2\u00A7\u00FD\u00A5" +   // 80-89
            "\u00A4\u00A7\u00FD\u00A6\u00A4\u00A7\u00FD\u00A5\u00A7\u00DD" +   // 90-99
            "\u00A6\u00A7\u00DD\u00A5\u00A1\u00A7\u00DD";                      //100-106

        result = converter.convert(unimarc);
        expected = "\u00E3|\u1F88\u1F89\u1F8A\u1F8B\u1F8C\u1F8D\u1F8E\u1F8F" +        //  0-9
            "\u1F98\u1F99\u1F9A\u1F9B\u1F9C\u1F9D\u1F9E\u1F9F\u1FA0\u1FA1" +   // 10-19
            "\u1FA2\u1FA3\u1FA4\u1FA5\u1FA6\u1FA7\u1FA8\u1FA9\u1FAA";
        assertTrue("UNIMARC set 1 failed", result.equals(expected));

        converter.resetDefaultGX();
        converter.setDefaultGX("01", "05", "03", "05");
        unimarc = "\u00C1\u00C2\u00C4\u00C5\u00C6\u00C9\u00CA\u00CB\u00CC\u00CD\u00CE\u00CF\u00D0\u00D1\u00D2\u00D3" +
            "\u00D5\u00D6\u00D8\u00D9\u00DA\u00DB\u00DC\u00DD";
        result = converter.convert(unimarc);
        expected = "\u0391\u0392\u0393\u0394\u0395\u0396\u0397\u0398\u0399\u039A" + // 0-9
            "\u039B\u039C\u039D\u039E\u039F\u03A0\u03A1\u03A3\u03A4\u03A5" + // 10-19
            "\u03A6\u03A7\u03A8\u03A9";                                      // 20-23
        assertTrue("UNIMARC set 2 failed", result.equals(expected));


        converter.resetDefaultGX();
        converter.setDefaultGX("01", "05", "03", "05");
        unimarc = "\u00E1\u00E2\u00E4\u00E5\u00E6\u00E9\u00EA\u00EB\u00EC\u00ED" +  //  0-9
            "\u00EE\u00EF\u00F0\u00F1\u00F2\u00F3\u00F5\u00F6\u00F7\u00F8" +  // 10-19
            "\u00F9\u00FA\u00FB\u00FC\u00FD\u00A5\u00E1\u00A5\u00A2\u00E1" +  // 20-29
            "\u00A6\u00E1\u00A6\u00A2\u00E1\u00A5\u00A4\u00E1\u00A6\u00A4" +  // 30-39
            "\u00E1\u00A5\u00A7\u00E1\u00A6\u00A7\u00E1\u00A5\u00A2\u00A7" +  // 40-49
            "\u00E1\u00A6\u00A2\u00A7\u00E1\u00A4\u00E1\u00A5\u00A4\u00A7" +  // 50-59
            "\u00E1\u00A6\u00A4\u00A7\u00E1\u00A7\u00E1";                     // 60-66
        result = converter.convert(unimarc);
        expected = "\u03B1\u03B2\u03B3\u03B4\u03B5\u03B6\u03B7\u03B8\u03B9\u03BA" + // 0-9
            "\u03BB\u03BC\u03BD\u03BE\u03BF\u03C0\u03C1\u03C3\u03C2\u03C4" + // 10-19
            "\u03C5\u03C6\u03C7\u03C8\u03C9\u1F00\u1F04\u1F01\u1F05\u1F06" + // 20-29
            "\u1F07\u1F80\u1F81\u1F84\u1F85\u1FB6\u1F86\u1F87\u1FB3";        // 30-38
        assertTrue("UNIMARC set 3 failed", result.equals(expected));


        converter.resetDefaultGX();
        converter.setDefaultGX("01", "05", "03", "05");
        unimarc = "\u00A5\u00E6\u00A5\u00A2\u00E6\u00A6\u00E6\u00A6\u00A2\u00E6" + //  0-9
            "\u00A5\u00EA\u00A5\u00A2\u00EA\u00A6\u00EA\u00A6\u00A2\u00EA" + // 10-19
            "\u00A4\u00EA\u00A5\u00A4\u00EA\u00A6\u00A4\u00EA\u00A4\u00A7" + // 20-29
            "\u00EA\u00A7\u00EA\u00A5\u00EC\u00A5\u00A2\u00EC\u00A6\u00EC" + // 30-39
            "\u00A6\u00A2\u00EC\u00A4\u00EC\u00A5\u00A4\u00EC\u00A6\u00A4" + // 40-49
            "\u00EC\u00A5\u00F2\u00A5\u00A2\u00F2\u00A6\u00F2\u00A6\u00A2" + // 50-59
            "\u00F2\u00A5\u00F9\u00A5\u00A2\u00F9\u00A6\u00F9\u00A6\u00A2" + // 60-69
            "\u00F9\u00A4\u00F9\u00A5\u00A4\u00F9\u00A6\u00A4\u00F9\u00A5" + // 70-79
            "\u00FD\u00A5\u00A2\u00FD\u00A6\u00FD\u00A6\u00A2\u00FD\u00A4" + // 80-89
            "\u00FD\u00FD\u00A2\u00E1\u00A2\u00E6\u00A2\u00EA\u00A3\u00EC" + // 90-99
            "\u00A2\u00EC\u00A2\u00F2\u00A2\u00F9\u00A2\u00FD\u00A3\u00A2" + //100-109
            "\u00EC\u00A3\u00A2\u00F9\u00A5\u00A4\u00FD\u00A6\u00A4\u00FD" + //110-119
            "\u00A7\u00FD\u00A4\u00A7\u00FD";                                //120-124
        result = converter.convert(unimarc);
        expected = "\u1F10\u1F14\u1F11\u1F15\u1F20\u1F24\u1F21\u1F25\u1FC6\u1F26" + //  0-9
            "\u1F27\u1FC7\u1FC3\u1F30\u1F34\u1F31\u1F35\u1FD6\u1F36\u1F37" + // 10-19
            "\u1F40\u1F44\u1F41\u1F45\u1F50\u1F54\u1F51\u1F55\u1FE6\u1F56" + // 20-29
            "\u1F57\u1F60\u1F64\u1F61\u1F65\u1FF6\u03C9\u03AC\u03AD\u03AE" + // 30-39
            "\u03CA\u03AF\u03CC\u03CD\u03CE\u0390\u03B0\u1F66\u1F67\u1FF3" + // 40-49
            "\u1FF7";                                                        // 50
        assertTrue("UNIMARC set 4 failed", result.equals(expected));


        converter.resetDefaultGX();
        converter.setDefaultGX("01", "05", "03", "05");
        unimarc = "\u00A3\u00CC\u00A3\u00D9\u00A2\u0061\u00A1\u0061\u001B\u007D" + //  0-9
            "\u00C3\u0061\u00C8\u0061\u00C2\u0065\u00C1\u0065\u00C3\u0065" + // 10-19
            "\u00C8\u0065\u00C8\u006F\u00C1\u0075\u00C3\u0075\u00C8\u0075" + // 20-29
            "\u00D0\u0063\u00D0\u0073\u001B\u007E";                          // 30-35
        result = converter.convert(unimarc);
        expected = "\u03AA\u03AB\u00E1\u00E0\u00E2\u00E4\u00E9\u00E8\u00EA\u00EB" + // 0-9
            "\u00F6\u00F9\u00FB\u00FC\u00E7\u015F";                          // 10-19
        assertTrue("UNIMARC set 5 failed", result.equals(expected));


        // Test setting Greek G1
        // ESC G1 as Greek set g1 abc gamma delta ESC LATIN AE abc
        converter.resetDefaultGX();
        String greek = "\u001B\u0029\u0053\u001B\u007Eabc\u00C4\u00C5\u001B\u0029\u0050\u001B\u007E\u00E1abc";
        result = converter.convert(greek);
        assertTrue("Greek conversion failed", result.equals("abc\u0393\u0394\u00C6abc"));

        // Test setting Greek as default G1
        // abc gamma delta abc
        converter.setDefaultGX(null, "05", null, null);
        greek = "abc\u00C4\u00C5abc";
        result = converter.convert(greek);
        assertTrue("Greek default G1 failed", result.equals("abc\u0393\u0394abc"));

        // Test setting Greek as default G2
        // abc AE gamma delta OE abc
        converter.resetDefaultGX();
        converter.setDefaultGX(null, null, "05", null);
        greek = "abc\u00E1\u001B\u007D\u00C4\u00C5\u001B\u007E\u00EAabc";
        result = converter.convert(greek);
        assertTrue("Greek default G2 failed", result.equals("abc\u00C6\u0393\u0394\u0152abc"));


        // Test special charcter taken from test data
        converter.resetDefaultGX();
        testdata = "Dipl\u00C3om\u00C2ee de l'Ecole sup\u00C2erieure d'intrepr\u00C1etes et de traducteurs (1972)";
        unimarc = "Dipl\u00F4m\u00E9e de l'Ecole sup\u00E9rieure d'intrepr\u00E8tes et de traducteurs (1972)";
        result = converter.convert(testdata);
        assertTrue("Special UNIMARC conversion failed", result.equals(unimarc));

        // Test NSB/NSE
        converter.resetDefaultGX();
        testdata = "\u0088Le \u0089D\u00C2eclin de la Troisi\u00C1eme R\u00C2epublique";
        unimarc = "\u0098Le \u009CD\u00E9clin de la Troisi\u00E8me R\u00E9publique";
        result = converter.convert(testdata);
        assertTrue("NSB/NSE conversion failed", result.equals(unimarc));
    }

    @Test
    public void testUnicodeToAnsel() {
        UnicodeToAnsel converter = new UnicodeToAnsel();
        converter.setDecomposeUnicode(true);

        // Test plain ASCII data
        String testdata = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String result = converter.convert(testdata);
        assertTrue("ASCII converstion failed", testdata.equals(result));

        // Test Unicode sequence
        testdata = "Test: #\u1100\u1173\u11B7\u1109\u1165";
        result = converter.convert(testdata);
        assertTrue("Unicode escape sequence failed", result.equals("Test: #<U+1100><U+1173><U+11B7><U+1109><U+1165>"));

        // Test some ANSEL characters
        // AE OE A with Grave
        testdata = "\u00C6\u0152A\u0300";
        result = converter.convert(testdata);
        assertTrue("ANSEL conversion failed", result.equals("\u00A5\u00A6\u00E1A"));

        // Test combined/non combined
        // A with Grave combined; A with Grave non combined
        testdata = "\u00C0A\u0300";
        result = converter.convert(testdata);
        assertTrue("ANSEL combined/noncombined failed", result.equals("\u00E1A\u00E1A"));

        // Test setting Greek G1
        testdata = "abc\u0393\u0394\u00C6abc";
        String greek = "abc\u001B(S\u0044\u0045\u00A5\u001B(Babc";
        result = converter.convert(testdata);
        assertTrue("Greek conversion failed", result.equals(greek));


        // Test setting Greek as default
        converter.setDefaultG0AndG1(null, ")S");
        testdata = "abc\u0393\u0394abc";
        greek = "abc\u00C4\u00C5abc";
        result = converter.convert(testdata);
        assertTrue("Greek default G1 failed", result.equals(greek));

        // Test CJK
        converter.resetDefaultG0AndG1();
        String cjk = "\u001B$1!]>!`5\u001B(B.";
        result = converter.convert(cjk);
        assertTrue("CJK conversion failed", result.equals(cjk));
    }

    @Test
    public void testUnicodeToUnimarc() {
        UnicodeToUnimarc converter = new UnicodeToUnimarc();
        converter.setDecomposeUnicode(true);

        // Test plain ASCII data
        String testdata = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String result = converter.convert(testdata);
        assertTrue("ASCII converstion failed", testdata.equals(result));

        // Test Unicode sequence
        testdata = "Test: #\u1100\u1173\u11B7\u1109\u1165";
        result = converter.convert(testdata);
        assertTrue("Unicode escape sequence failed", result.equals("Test: #<U+1100><U+1173><U+11B7><U+1109><U+1165>"));

        // Test some Latin characters
        // AE OE A with Grave
        testdata = "\u00C6\u0152A\u0300";
        String unimarc = "\u00E1\u00EA\u00C1A";
        result = converter.convert(testdata);
        assertTrue("LATIN conversion failed", result.equals(unimarc));

        // Test UNIMARC set
        converter.resetDefaultGX();
        converter.setDefaultGX("01", "05", "03", "05");
        testdata = "a\u0303|\u0391\u0313\u0345\u0391\u0314\u0345\u0391" +             //  0-9
            "\u0313\u0300\u0345\u0391\u0314\u0300\u0345\u0391\u0313\u0301" +   // 10-19
            "\u0345\u0391\u0314\u0301\u0345\u0391\u0313\u0342\u0345\u0391" +   // 20-29
            "\u0314\u0342\u0345\u0397\u0313\u0345\u0397\u0314\u0345\u0397" +   // 30-39
            "\u0313\u0300\u0345\u0397\u0314\u0300\u0345\u0397\u0313\u0301" +   // 40-49
            "\u0345\u0397\u0314\u0301\u0345\u0397\u0313\u0342\u0345\u0397" +   // 50-59
            "\u0314\u0342\u0345\u03C9\u0313\u0345\u03C9\u0314\u0345\u03C9" +   // 60-69
            "\u0313\u0300\u0345\u03C9\u0314\u0300\u0345\u03C9\u0313\u0301" +   // 70-79
            "\u0345\u03C9\u0314\u0301\u0345\u03C9\u0313\u0342\u0345\u03C9" +   // 80-89
            "\u0314\u0342\u0345\u03A9\u0313\u0345\u03A9\u0314\u0345\u03A9" +   // 90-99
            "\u0313\u0300\u0345";                                              //100-102

        unimarc = "\u001B\u007D\u00C4\u0061\u007C\u001B\u007E\u00A5\u00A7\u00C1" +   //  0-9
            "\u00A6\u00A7\u00C1\u00A5\u00A1\u00A7\u00C1\u00A6\u00A1\u00A7" +   // 10-19
            "\u00C1\u00A5\u00A2\u00A7\u00C1\u00A6\u00A2\u00A7\u00C1\u00A5" +   // 20-29
            "\u00A4\u00A7\u00C1\u00A6\u00A4\u00A7\u00C1\u00A5\u00A7\u00CA" +   // 30-39
            "\u00A6\u00A7\u00CA\u00A5\u00A1\u00A7\u00CA\u00A6\u00A1\u00A7" +   // 40-49
            "\u00CA\u00A5\u00A2\u00A7\u00CA\u00A6\u00A2\u00A7\u00CA\u00A5" +   // 50-59
            "\u00A4\u00A7\u00CA\u00A6\u00A4\u00A7\u00CA\u00A5\u00A7\u00FD" +   // 60-69
            "\u00A6\u00A7\u00FD\u00A5\u00A1\u00A7\u00FD\u00A6\u00A1\u00A7" +   // 70-79
            "\u00FD\u00A5\u00A2\u00A7\u00FD\u00A6\u00A2\u00A7\u00FD\u00A5" +   // 80-89
            "\u00A4\u00A7\u00FD\u00A6\u00A4\u00A7\u00FD\u00A5\u00A7\u00DD" +   // 90-99
            "\u00A6\u00A7\u00DD\u00A5\u00A1\u00A7\u00DD";                      //100-106

        result = converter.convert(testdata);
        assertTrue("UNIMARC set 1 failed", result.equals(unimarc));

        converter.resetDefaultGX();
        converter.setDefaultGX("01", "05", "03", "05");
        testdata = "\u0391\u0392\u0393\u0394\u0395\u0396\u0397\u0398\u0399\u039A" + // 0-9
            "\u039B\u039C\u039D\u039E\u039F\u03A0\u03A1\u03A3\u03A4\u03A5" + // 10-19
            "\u03A6\u03A7\u03A8\u03A9";                                      // 20-23
        unimarc = "\u00C1\u00C2\u00C4\u00C5\u00C6\u00C9\u00CA\u00CB\u00CC\u00CD\u00CE\u00CF\u00D0\u00D1\u00D2\u00D3" +
            "\u00D5\u00D6\u00D8\u00D9\u00DA\u00DB\u00DC\u00DD";
        result = converter.convert(testdata);
        assertTrue("UNIMARC set 2 failed", result.equals(unimarc));


        converter.resetDefaultGX();
        converter.setDefaultGX("01", "05", "03", "05");
        testdata = "\u03B1\u03B2\u03B3\u03B4\u03B5\u03B6\u03B7\u03B8\u03B9\u03BA" + // 0-9
            "\u03BB\u03BC\u03BD\u03BE\u03BF\u03C0\u03C1\u03C3\u03C2\u03C4" + // 10-19
            "\u03C5\u03C6\u03C7\u03C8\u03C9\u03B1\u0313\u03B1\u0313\u0301" + // 20-29
            "\u03B1\u0314\u03B1\u0314\u0301\u03B1\u0313\u0342\u03B1\u0314" + // 30-39
            "\u0342\u03B1\u0313\u0345\u03B1\u0314\u0345\u03B1\u0313\u0301" + // 40-49
            "\u0345\u03B1\u0314\u0301\u0345\u03B1\u0342\u03B1\u0313\u0342" + // 50-59
            "\u0345\u03B1\u0314\u0342\u0345\u03B1\u0345";                    // 60-66
        unimarc = "\u00E1\u00E2\u00E4\u00E5\u00E6\u00E9\u00EA\u00EB\u00EC\u00ED" +  //  0-9
            "\u00EE\u00EF\u00F0\u00F1\u00F2\u00F3\u00F5\u00F6\u00F7\u00F8" +  // 10-19
            "\u00F9\u00FA\u00FB\u00FC\u00FD\u00A5\u00E1\u00A5\u00A2\u00E1" +  // 20-29
            "\u00A6\u00E1\u00A6\u00A2\u00E1\u00A5\u00A4\u00E1\u00A6\u00A4" +  // 30-39
            "\u00E1\u00A5\u00A7\u00E1\u00A6\u00A7\u00E1\u00A5\u00A2\u00A7" +  // 40-49
            "\u00E1\u00A6\u00A2\u00A7\u00E1\u00A4\u00E1\u00A5\u00A4\u00A7" +  // 50-59
            "\u00E1\u00A6\u00A4\u00A7\u00E1\u00A7\u00E1";                     // 60-66

        result = converter.convert(testdata);
        assertTrue("UNIMARC set 3 failed", result.equals(unimarc));

        converter.resetDefaultGX();
        converter.setDefaultGX("01", "05", "03", "05");
        testdata = "\u03B5\u0313\u03B5\u0313\u0301\u03B5\u0314\u03B5\u0314\u0301" + //  0-9
            "\u03B7\u0313\u03B7\u0313\u0301\u03B7\u0314\u03B7\u0314\u0301" + // 10-19
            "\u03B7\u0342\u03B7\u0313\u0342\u03B7\u0314\u0342\u03B7\u0342" + // 20-29
            "\u0345\u03B7\u0345\u03B9\u0313\u03B9\u0313\u0301\u03B9\u0314" + // 30-39
            "\u03B9\u0314\u0301\u03B9\u0342\u03B9\u0313\u0342\u03B9\u0314" + // 40-49
            "\u0342\u03BF\u0313\u03BF\u0313\u0301\u03BF\u0314\u03BF\u0314" + // 50-59
            "\u0301\u03C5\u0313\u03C5\u0313\u0301\u03C5\u0314\u03C5\u0314" + // 60-69
            "\u0301\u03C5\u0342\u03C5\u0313\u0342\u03C5\u0314\u0342\u03C9" + // 70-79
            "\u0313\u03C9\u0313\u0301\u03C9\u0314\u03C9\u0314\u0301\u03C9" + // 80-89
            "\u0342\u03C9\u03B1\u0301\u03B5\u0301\u03B7\u0301\u03B9\u0308" + // 90-99
            "\u03B9\u0301\u03BF\u0301\u03C5\u0301\u03C9\u0301\u03B9\u0308" + //100-109
            "\u0301\u03C5\u0308\u0301\u03C9\u0313\u0342\u03C9\u0314\u0342" + //110-119
            "\u03C9\u0345\u03C9\u0342\u0345";                                //120-124
        unimarc = "\u00A5\u00E6\u00A5\u00A2\u00E6\u00A6\u00E6\u00A6\u00A2\u00E6" + //  0-9
            "\u00A5\u00EA\u00A5\u00A2\u00EA\u00A6\u00EA\u00A6\u00A2\u00EA" + // 10-19
            "\u00A4\u00EA\u00A5\u00A4\u00EA\u00A6\u00A4\u00EA\u00A4\u00A7" + // 20-29
            "\u00EA\u00A7\u00EA\u00A5\u00EC\u00A5\u00A2\u00EC\u00A6\u00EC" + // 30-39
            "\u00A6\u00A2\u00EC\u00A4\u00EC\u00A5\u00A4\u00EC\u00A6\u00A4" + // 40-49
            "\u00EC\u00A5\u00F2\u00A5\u00A2\u00F2\u00A6\u00F2\u00A6\u00A2" + // 50-59
            "\u00F2\u00A5\u00F9\u00A5\u00A2\u00F9\u00A6\u00F9\u00A6\u00A2" + // 60-69
            "\u00F9\u00A4\u00F9\u00A5\u00A4\u00F9\u00A6\u00A4\u00F9\u00A5" + // 70-79
            "\u00FD\u00A5\u00A2\u00FD\u00A6\u00FD\u00A6\u00A2\u00FD\u00A4" + // 80-89
            "\u00FD\u00FD\u00A2\u00E1\u00A2\u00E6\u00A2\u00EA\u00A3\u00EC" + // 90-99
            "\u00A2\u00EC\u00A2\u00F2\u00A2\u00F9\u00A2\u00FD\u00A3\u00A2" + //100-109
            "\u00EC\u00A3\u00A2\u00F9\u00A5\u00A4\u00FD\u00A6\u00A4\u00FD" + //110-119
            "\u00A7\u00FD\u00A4\u00A7\u00FD";                                //120-124
        result = converter.convert(testdata);
        assertTrue("UNIMARC set 4 failed", result.equals(unimarc));


        converter.resetDefaultGX();
        converter.setDefaultGX("01", "05", "03", "05");
        testdata = "\u0399\u0308\u03A5\u0308a\u0301a\u0300a\u0302" + // 0-9
            "a\u0308e\u0301e\u0300e\u0302e\u0308" + // 10-19
            "o\u0308u\u0300u\u0302u\u0308c\u0327" + // 20-29
            "s\u0327";                                                       // 30-31
        unimarc = "\u00A3\u00CC\u00A3\u00D9\u00A2\u0061\u00A1\u0061\u001B\u007D" + //  0-9
            "\u00C3\u0061\u00C9\u0061\u00C2\u0065\u00C1\u0065\u00C3\u0065" + // 10-19
            "\u00C9\u0065\u00C9\u006F\u00C1\u0075\u00C3\u0075\u00C9\u0075" + // 20-29
            "\u00D0\u0063\u00D0\u0073\u001B\u007E";                          // 30-35
        result = converter.convert(testdata);
        assertTrue("UNIMARC set 5 failed", result.equals(unimarc));

        // Test setting Greek G1
        // ESC G1 as Greek set g1 abc gamma delta ESC LATIN AE abc
        converter.resetDefaultGX();
        testdata = "abc\u0393\u0394\u00C6abc";
        String greek = "abc\u001B\u0029\u0053\u001B\u007E\u00C4\u00C5\u001B\u0029\u0050\u001B\u007E\u00E1abc";
        result = converter.convert(testdata);
        assertTrue("Greek conversion failed", result.equals(greek));

        // Test setting Greek as default G1
        // abc gamma delta abc
        converter.setDefaultGX(null, "05", null, null);
        testdata = "abc\u0393\u0394abc";
        greek = "abc\u00C4\u00C5abc";
        result = converter.convert(testdata);
        assertTrue("Greek default G1 failed", result.equals(greek));

        // Test setting Greek as default G2
        // abc AE gamma delta OE abc
        converter.resetDefaultGX();
        converter.setDefaultGX(null, null, "05", null);
        testdata = "abc\u00C6\u0393\u0394\u0152abc";
        greek = "abc\u00E1\u001B\u007D\u00C4\u00C5\u001B\u007E\u00EAabc";
        result = converter.convert(testdata);
        assertTrue("Greek default G2 failed", result.equals(greek));
    }

    @Test
    public void testGarbageInAnselToUnicode() {
        AnselToUnicode converter = new AnselToUnicode();
        converter.setComposeUnicode(true);
        converter.setShouldConvertUnicodeSequence(true);

        // Test invalid input
        // It used to throw an exception; instead we want it to keep the invalid data and continue
        String testdata = "\u001FaAt head of title: Centre d'\u00E2etudes ib\u00E2eriques et ib\u00E2ero-am\u00E2ericaines du XIX\u001Bp0\u001Bs\u001B-R si\u00E1ecle.";
        String result = converter.convert(testdata);
        String expected = "\u001FaAt head of title: Centre d'\u00E9tudes ib\u00E9riques et ib\u00E9ro-am\u00E9ricaines du XIX\u2070 si\u00E1ecle.";
        assertTrue("Garbage In failed", result.equals(expected));

        // Test more garbage : example from Univ. of Chicago database bib# 1787519
        // Following is wrong with this entry of a leader tag
        // - Leader's are supposed to have only ASCII data
        // - 3 non ASCII characters
        // - Escape (0x1B) was causing an exception
        testdata = "00449dz|\u00E3\u001B2200121n  4500\u001E";
        result = converter.convert(testdata);
        expected = "00449dz|\u001B\u03022200121n  4500\u001E";
        assertTrue("2 - Garbage In failed", expected.equals(result));
    }

    @Test
    public void testCJKInAnselToUnicode() {
        AnselToUnicode converter = new AnselToUnicode();
        converter.setComposeUnicode(true);
        converter.setShouldConvertUnicodeSequence(true);

        // CJK record from Univ. of Chicago
        String testdata = "\u001F6245-02/$1" +        //  0-10
            "\u001Fa\u001B$1!C! !" +    // 11-20
            "3U!Ci!#!!X" +              // 21-29
            "$!<}!#!!X<" +              // 30-39
            "!#!KN7!ON!" +              // 40-49
            "#!\u001B(B/\u001Fc\u001B" +// 50-59
            "$1'Uq!#!!C" +              // 60-69
            "*!ln !UN\u001B(" +         // 70-79
            "B.\u001E";                 // 80-82
        String result = converter.convert(testdata);
        String expected = "\u001F6245-02/$1" +          //  0-10
            "\u001Fa\u660E \u520A\u672C\u3000\u897F\u5EC2\u3000" +
            "\u8A18\u3000\u7814\u7A76\u3000/\u001Fc\u848B\u3000" +
            "\u661F\u530A \u8457.\u001E";
        assertTrue("CJK with spaces failed", expected.equals(result));
    }

    @Test
    public void testDoubleLigatureAnselToUnicode() {
        AnselToUnicode converter = new AnselToUnicode();
        converter.setComposeUnicode(true);
        converter.setShouldConvertUnicodeSequence(true);

        String testdata = "\u001Fa\u00A7 m\u00EBi\u00ECagki\u00E6i";
        String result = converter.convert(testdata);
        String expected = "\u001Fa\u02B9 mi\u0361agki\u012D";
        assertTrue("Double Ligature failed", expected.equals(result));
    }

    @Test
    public void testDoubleLigatureUnicodeToAnsel() {
        UnicodeToAnsel converter = new UnicodeToAnsel();
        converter.setDecomposeUnicode(true);

        String testdata = "\u001Fa\u02B9 mi\u0361agkii\u0306";
        String result = converter.convert(testdata);
        String expected = "\u001Fa\u00A7 m\u00EBi\u00ECagki\u00E6i";
        assertTrue("Double Ligature 1 failed", expected.equals(result));

        testdata = "\u001Fa\u02B9 mi\uFE20a\uFE21gkii\u0306";
        result = converter.convert(testdata);
        expected = "\u001Fa\u00A7 m\u00EBi\u00ECagki\u00E6i";
        assertTrue("Double Ligature 2 failed", expected.equals(result));
    }

    @Test
    public void testHangul() {
        AnselToUnicode converter = new AnselToUnicode();
        converter.setComposeUnicode(true);
        converter.setShouldConvertUnicodeSequence(true);

        String testdata = "<U+1109><U+1165><U+110B><U+116E><U+11AF> :";
        String result = converter.convert(testdata);
        String expected = "\uC11C\uC6B8 :";
        assertTrue("Hangul failed", expected.equals(result));
    }

    @Test
    public void testDoubleByteLigature() throws Exception {
        // There was a problem with the double width at the end of a string.
        // The data is basically corrupt but we don't want to throw an out of bounds exception
        String name = "White, Zo.";
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] utf8data = name.getBytes("UTF8");
        output.write(utf8data, 0, utf8data.length);
        output.write(0x00CD);
        output.write(0x00A1);
        utf8data = output.toByteArray();
        String testdata = new String(utf8data, "UTF8");

        UnicodeToAnsel converter = new UnicodeToAnsel();
        converter.setDecomposeUnicode(true);
        String result = converter.convert(testdata);
        String expected = "White, Zo\u00EB.\u00EC ";
        assertTrue("DoubleByteLigature failed!!!", expected.equals(result));
    }
}
