package org.marc4j.test.utils;

import org.marc4j.marc.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;


public class TestUtils {

    public static void validateKavalieAndClayRecord(Record record) {
        assertEquals("leader", "00759cam a2200229 a 4500", record.getLeader().marshal());
        Iterator<VariableField> it = record.getVariableFields().iterator();
        assertControlFieldInRecordEquals("001", "11939876", it.next());
        assertControlFieldInRecordEquals("005", "20041229190604.0", it.next());
        assertControlFieldInRecordEquals("008", "000313s2000    nyu           000 1 eng  ", it.next());
        assertDataFieldEquals(it.next(), "020", ' ', ' ', "a", "0679450041 (acid-free paper)");
        assertDataFieldEquals(it.next(), "040", ' ', ' ', "a", "DLC", "c", "DLC", "d", "DLC");
        assertDataFieldEquals(it.next(), "100", '1', ' ', "a", "Chabon, Michael.");
        assertDataFieldEquals(it.next(), "245", '1', '4', "a", "The amazing adventures of Kavalier and Clay :", "b", "a novel /", "c", "Michael Chabon.");
        assertDataFieldEquals(it.next(), "260", ' ', ' ', "a", "New York :", "b", "Random House,", "c", "c2000.");
        assertDataFieldEquals(it.next(), "300", ' ', ' ', "a", "639 p. ;", "c", "25 cm.");
        assertDataFieldEquals(it.next(), "650", ' ', '0', "a", "Comic books, strips, etc.", "x", "Authorship", "v", "Fiction.");
        assertDataFieldEquals(it.next(), "650", ' ', '0', "a", "Heroes in mass media", "v", "Fiction.");
        assertDataFieldEquals(it.next(), "650", ' ', '0', "a", "Czech Americans", "v", "Fiction.");
        assertDataFieldEquals(it.next(), "651", ' ', '0', "a", "New York (N.Y.)", "v", "Fiction.");
        assertDataFieldEquals(it.next(), "650", ' ', '0', "a", "Young men", "v", "Fiction.");
        assertDataFieldEquals(it.next(), "650", ' ', '0', "a", "Cartoonists", "v", "Fiction.");
        assertDataFieldEquals(it.next(), "655", ' ', '7', "a", "Humorous stories.", "2", "gsafd");
        assertDataFieldEquals(it.next(), "655", ' ', '7', "a", "Bildungsromane.", "2", "gsafd");
        assertFalse("too many fields", it.hasNext());
    }


    public static void validateSummerlandRecord(Record record) {
        assertEquals("leader", "00714cam a2200205 a 4500", record.getLeader().marshal());
        Iterator<VariableField> it = record.getVariableFields().iterator();
        assertControlFieldInRecordEquals("001", "12883376", it.next());
        assertControlFieldInRecordEquals("005", "20030616111422.0", it.next());
        assertControlFieldInRecordEquals("008", "020805s2002    nyu    j      000 1 eng  ", it.next());
        assertDataFieldEquals(it.next(), "020", ' ', ' ', "a", "0786808772");
        assertDataFieldEquals(it.next(), "020", ' ', ' ', "a", "0786816155 (pbk.)");
        assertDataFieldEquals(it.next(), "040", ' ', ' ', "a", "DLC", "c", "DLC", "d", "DLC");
        assertDataFieldEquals(it.next(), "100", '1', ' ', "a", "Chabon, Michael.");
        assertDataFieldEquals(it.next(), "245", '1', '0', "a", "Summerland /", "c", "Michael Chabon.");
        assertDataFieldEquals(it.next(), "250", ' ', ' ', "a", "1st ed.");
        assertDataFieldEquals(it.next(), "260", ' ', ' ', "a", "New York :", "b", "Miramax Books/Hyperion Books for Children,", "c", "c2002.");
        assertDataFieldEquals(it.next(), "300", ' ', ' ', "a", "500 p. ;", "c", "22 cm.");
        assertDataFieldEquals(it.next(), "520", ' ', ' ', "a", "Ethan Feld, the worst baseball player in the history of the game, finds himself recruited by a 100-year-old scout to help a band of fairies triumph over an ancient enemy.");
        assertDataFieldEquals(it.next(), "650", ' ', '1', "a", "Fantasy.");
        assertDataFieldEquals(it.next(), "650", ' ', '1', "a", "Baseball", "v", "Fiction.");
        assertDataFieldEquals(it.next(), "650", ' ', '1', "a", "Magic", "v", "Fiction.");
        assertFalse("too many fields", it.hasNext());
    }

    public static void validateFreewheelingBobDylanRecord(Record record) {
        assertEquals("leader", "01471cjm a2200349 a 4500", record.getLeader().marshal());
        Iterator<VariableField> it = record.getVariableFields().iterator();
        assertControlFieldInRecordEquals("001", "5674874", it.next());
        assertControlFieldInRecordEquals("005", "20030305110405.0", it.next());
        assertControlFieldInRecordEquals("007", "sdubsmennmplu", it.next());
        assertControlFieldInRecordEquals("008", "930331s1963    nyuppn              eng d", it.next());
        assertDataFieldEquals(it.next(), "035", ' ', ' ', "9", "(DLC)   93707283");
        assertDataFieldEquals(it.next(), "906", ' ', ' ', "a", "7", "b", "cbc", "c", "copycat", "d", "4", "e", "ncip", "f", "19", "g", "y-soundrec");
        assertDataFieldEquals(it.next(), "010", ' ', ' ', "a", "   93707283 ");
        assertDataFieldEquals(it.next(), "028", '0', '2', "a", "CS 8786", "b", "Columbia");
        assertDataFieldEquals(it.next(), "035", ' ', ' ', "a", "(OCoLC)13083787");
        assertDataFieldEquals(it.next(), "040", ' ', ' ', "a", "OClU", "c", "DLC", "d", "DLC");
        assertDataFieldEquals(it.next(), "041", '0', ' ', "d", "eng", "g", "eng");
        assertDataFieldEquals(it.next(), "042", ' ', ' ', "a", "lccopycat");
        assertDataFieldEquals(it.next(), "050", '0', '0', "a", "Columbia CS 8786");
        assertDataFieldEquals(it.next(), "100", '1', ' ', "a", "Dylan, Bob,", "d", "1941-");
        assertDataFieldEquals(it.next(), "245", '1', '4', "a", "The freewheelin' Bob Dylan", "h", "[sound recording].");
        assertDataFieldEquals(it.next(), "260", ' ', ' ', "a", "[New York, N.Y.] :", "b", "Columbia,", "c", "[1963]");
        assertDataFieldEquals(it.next(), "300", ' ', ' ', "a", "1 sound disc :", "b", "analog, 33 1/3 rpm, stereo. ;", "c", "12 in.");
        assertDataFieldEquals(it.next(), "500", ' ', ' ', "a", "Songs.");
        assertDataFieldEquals(it.next(), "511", '0', ' ', "a", "The composer accompanying himself on the guitar ; in part with instrumental ensemble.");
        assertDataFieldEquals(it.next(), "500", ' ', ' ', "a", "Program notes by Nat Hentoff on container.");
        assertDataFieldEquals(it.next(), "505", '0', ' ', "a", "Blowin' in the wind -- Girl from the north country -- Masters of war -- Down the highway -- Bob Dylan's blues -- A hard rain's a-gonna fall -- Don't think twice, it's all right -- Bob Dylan's dream -- Oxford town -- Talking World War III blues -- Corrina, Corrina -- Honey, just allow me one more chance -- I shall be free.");
        assertDataFieldEquals(it.next(), "650", ' ', '0', "a", "Popular music", "y", "1961-1970.");
        assertDataFieldEquals(it.next(), "650", ' ', '0', "a", "Blues (Music)", "y", "1961-1970.");
        assertDataFieldEquals(it.next(), "856", '4', '1', "3", "Preservation copy (limited access)", "u", "http://hdl.loc.gov/loc.mbrsrs/lp0001.dyln");
        assertDataFieldEquals(it.next(), "952", ' ', ' ', "a", "New");
        assertDataFieldEquals(it.next(), "953", ' ', ' ', "a", "TA28");
        assertDataFieldEquals(it.next(), "991", ' ', ' ', "b", "c-RecSound", "h", "Columbia CS 8786", "w", "MUSIC");
        assertFalse("too many fields", it.hasNext());
    }

    static void assertControlFieldInRecordEquals(String tag, String expected, VariableField field) {
        ControlField tmp;
        tmp = (ControlField) field;
        assertEquals("Control field mismatch for tag " + tag, expected, tmp.getData());
    }

    static void assertDataFieldEquals(VariableField vf, String tag, char ind1, char ind2, String... subfieldsAndValues) {
        DataField field = (DataField) vf;
        assertEquals("tag", tag, field.getTag());
        assertEquals("Indicator 1", ind1, field.getIndicator1());
        assertEquals("Indicator 2", ind2, field.getIndicator2());
        List<Subfield> subfields = field.getSubfields();
        Iterator<Subfield> it = subfields.iterator();
        for (int i = 0; i < subfieldsAndValues.length; i++) {
            String expectedCode = subfieldsAndValues[i++];
            String expectedValue = subfieldsAndValues[i];
            if (!it.hasNext()) {
                fail("not enough subfields - expecting $" + expectedCode + " = " + expectedValue);
            }

            Subfield sf = it.next();
            assertEquals("subfieldCode", sf.getCode(), expectedCode.charAt(0));
            assertEquals("subfield value (" + tag + " $" + expectedCode + ") ", expectedValue, sf.getData());
        }
        if (it.hasNext()) {
            fail("Too many subfields for " + tag + " - first unexpected is " + it.next());
        }
    }

    public static void validateBytesAgainstFile(byte[] actual, String fileName) throws IOException {
        InputStream stream = TestUtils.class.getResourceAsStream(fileName);
        assertNotNull("Could't open " + fileName, stream);
        InputStream in = new BufferedInputStream(stream);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int n;
        byte expected[] = new byte[8192];
        while ((n = in.read(expected)) >= 0) {
            os.write(expected, 0, n);
        }
        os.flush();
        expected = os.toByteArray();
        String comparison = compareFilesContentsLineByLine(new String(expected), new String(actual));
        if (comparison != null) {
            fail("actual differs from expected as shown below:" + System.getProperty("line.separator") + comparison);
        }

    }


    public static void validateStringAgainstFile(String actual, String fileName) throws IOException {
        InputStream stream = TestUtils.class.getResourceAsStream(fileName);
        assertNotNull(fileName, stream);
        InputStream in = new BufferedInputStream(stream);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int n;
        byte buf[] = new byte[8192];
        while ((n = in.read(buf)) >= 0) {
            os.write(buf, 0, n);
        }
        os.flush();
        String expected = new String(os.toByteArray());
        String comparison = compareFilesContentsLineByLine(expected, actual);
        if (comparison != null) {
            fail("actual differs from expected as shown below:" + System.getProperty("line.separator") + comparison);
        }
    }

    public static String compareFilesContentsLineByLine(String expected, String actual) {
        String[] expectedLines = expected.split("[\r]?\n");
        String[] actualLines = actual.split("[\r]?\n");
        String separator = System.getProperty("line.separator");
        boolean matches = true;
        int expectedIndex = 0, actualIndex = 0;
        StringBuilder sb = new StringBuilder();
        while (expectedIndex < expectedLines.length && actualIndex < actualLines.length) {
            if (expectedLines[expectedIndex].equals(actualLines[actualIndex])) {
                sb.append("  " + expectedLines[expectedIndex]).append(separator);
                expectedIndex++;
                actualIndex++;
            } else if (actualIndex + 1 < actualLines.length && expectedLines[expectedIndex].equals(actualLines[actualIndex + 1])) {
                sb.append("+ " + actualLines[actualIndex]).append(separator);
                actualIndex++;
                matches = false;
            } else if (expectedIndex + 1 < expectedLines.length && expectedLines[expectedIndex + 1].equals(actualLines[actualIndex])) {
                sb.append("- " + expectedLines[expectedIndex]).append(separator);
                expectedIndex++;
                matches = false;
            } else {
                sb.append("+ " + actualLines[actualIndex]).append(separator);
                actualIndex++;
                sb.append("- " + expectedLines[expectedIndex]).append(separator);
                expectedIndex++;
                matches = false;
            }
        }
        while (expectedIndex < expectedLines.length || actualIndex < actualLines.length) {
            if (actualIndex < actualLines.length) {
                sb.append("+ " + actualLines[actualIndex]).append(separator);
                actualIndex++;
                matches = false;
            } else if (expectedIndex < expectedLines.length) {
                sb.append("- " + expectedLines[expectedIndex]).append(separator);
                expectedIndex++;
                matches = false;
            }
        }

        if (matches) return (null);
        return (sb.toString());
    }

}
