package org.marc4j.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Subfield;

public class DataFieldTest extends TestCase {

    MarcFactory factory = null;

    public void setUp() {
        factory = MarcFactory.newInstance();
    }

    public void testConstructor() {
        DataField df = factory.newDataField("245", '1', '0');
        assertEquals("245", df.getTag());
        assertEquals('1', df.getIndicator1());
        assertEquals('0', df.getIndicator2());
    }

    public void testAddSubfield() {
        DataField df = factory.newDataField("245", '1', '0');
        Subfield sf = factory.newSubfield('a', "Summerland");
        df.addSubfield(sf);
        assertEquals(1, df.getSubfields().size());
    }

    public void testSetSubfield() {
        DataField df = factory.newDataField("245", '1', '0');
        Subfield sf1 = factory.newSubfield('a', "Summerland");
        Subfield sf2 = factory.newSubfield('c', "Michael Chabon");
        df.addSubfield(sf2);
        df.addSubfield(0, sf1);
        Subfield s = (Subfield) df.getSubfields().get(0);
        assertEquals(2, df.getSubfields().size());
        assertEquals('a', s.getCode());
    }

    public void testComparable() throws Exception {
        DataField df1 = factory.newDataField("600", '0', '0');
        DataField df2 = factory.newDataField("600", '0', '0');
        assertEquals(0, df1.compareTo(df2));
        df2.setTag("245");
        assertEquals(4, df1.compareTo(df2));
        df2.setTag("700");
        assertEquals(-1, df1.compareTo(df2));
    }

    public void tearDown() {
        factory = null;
    }

    public static Test suite() {
        return new TestSuite(DataFieldTest.class);
    }

    public static void main(String args[]) {
        TestRunner.run(suite());
    }
}
