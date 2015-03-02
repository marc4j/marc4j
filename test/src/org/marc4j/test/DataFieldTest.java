package org.marc4j.test;

import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Subfield;

import static org.junit.Assert.assertEquals;

public class DataFieldTest  {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    MarcFactory factory = MarcFactory.newInstance();

    @Test
    public void testConstructor() {
        DataField df = factory.newDataField("245", '1', '0');
        assertEquals("245", df.getTag());
        assertEquals('1', df.getIndicator1());
        assertEquals('0', df.getIndicator2());
    }
    @Test
    public void testAddSubfield() {
        DataField df = factory.newDataField("245", '1', '0');
        Subfield sf = factory.newSubfield('a', "Summerland");
        df.addSubfield(sf);
        assertEquals(1, df.getSubfields().size());
    }

    @Test
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

    @Test
    public void testGetSubfields() {
        DataField df = factory.newDataField("245", '0', '4');
        Subfield sf1 = factory.newSubfield('a', "The summer-land ");
        Subfield sf2 = factory.newSubfield('h', "[electronic resource] : ");
        Subfield sf3 = factory.newSubfield('b', "a southern story / ");
        Subfield sf4 = factory.newSubfield('c', "by a child of the sun.");
        df.addSubfield(sf1);
        df.addSubfield(sf2);
        df.addSubfield(sf3);
        df.addSubfield(sf4);
        List<Subfield> sList = (List<Subfield>) df.getSubfields("a");
        assertEquals(1, sList.size());
        assertEquals('a', sList.get(0).getCode());
        
        List<Subfield> sList2 = (List<Subfield>) df.getSubfields("ac");
        assertEquals(2, sList2.size());
        assertEquals('a', sList2.get(0).getCode());
        assertEquals('c', sList2.get(1).getCode());
        
        List<Subfield> sList3 = (List<Subfield>) df.getSubfields("[a-c]");
        assertEquals(3, sList3.size());
        assertEquals('a', sList3.get(0).getCode());
        assertEquals('b', sList3.get(1).getCode());
        assertEquals('c', sList3.get(2).getCode());
        
        List<Subfield> sList4 = (List<Subfield>) df.getSubfields("[a-cg-j]");
        assertEquals(4, sList4.size());
        assertEquals('a', sList4.get(0).getCode());
        assertEquals('h', sList4.get(1).getCode());
        assertEquals('b', sList4.get(2).getCode());
        assertEquals('c', sList4.get(3).getCode());
        
        List<Subfield> sList5 = (List<Subfield>) df.getSubfields("[g-ja-b]");
        assertEquals(3, sList5.size());
        assertEquals('a', sList5.get(0).getCode());
        assertEquals('h', sList5.get(1).getCode());
        assertEquals('b', sList5.get(2).getCode());
        
        List<Subfield> sList6 = (List<Subfield>) df.getSubfields("[a-eb]");
        assertEquals(3, sList6.size());
        assertEquals('a', sList6.get(0).getCode());
        assertEquals('b', sList6.get(1).getCode());
        assertEquals('c', sList6.get(2).getCode());
        
        List<Subfield> sList7 = (List<Subfield>) df.getSubfields("[^h]");
        assertEquals(3, sList7.size());
        assertEquals('a', sList7.get(0).getCode());
        assertEquals('b', sList7.get(1).getCode());
        assertEquals('c', sList7.get(2).getCode());
        
        List<Subfield> sList8 = (List<Subfield>) df.getSubfields("[a-z&&[^bc]]");
        assertEquals(2, sList8.size());
        assertEquals('a', sList8.get(0).getCode());
        assertEquals('h', sList8.get(1).getCode());
    }

    @Test
    public void testGetSubfieldsWithBadSubfieldSpec1() {
        DataField df = factory.newDataField("245", '0', '4');
        Subfield sf1 = factory.newSubfield('a', "The summer-land ");
        Subfield sf2 = factory.newSubfield('h', "[electronic resource] : ");
        Subfield sf3 = factory.newSubfield('b', "a southern story / ");
        Subfield sf4 = factory.newSubfield('c', "by a child of the sun.");
        df.addSubfield(sf1);
        df.addSubfield(sf2);
        df.addSubfield(sf3);
        df.addSubfield(sf4);

        exception.expect(PatternSyntaxException.class);
        df.getSubfields("[c-a]");
    }

    @Test
    public void testGetSubfieldsWithBadSubfieldSpec2() {
        DataField df = factory.newDataField("245", '0', '4');
        Subfield sf1 = factory.newSubfield('a', "The summer-land ");
        Subfield sf2 = factory.newSubfield('h', "[electronic resource] : ");
        Subfield sf3 = factory.newSubfield('b', "a southern story / ");
        Subfield sf4 = factory.newSubfield('c', "by a child of the sun.");
        df.addSubfield(sf1);
        df.addSubfield(sf2);
        df.addSubfield(sf3);
        df.addSubfield(sf4);

        exception.expect(PatternSyntaxException.class);
        df.getSubfields("[abc");
    }

    @Test
    public void testComparable() throws Exception {
        DataField df1 = factory.newDataField("600", '0', '0');
        DataField df2 = factory.newDataField("600", '0', '0');
        assertEquals(0, df1.compareTo(df2));
        df2.setTag("245");
        assertEquals(4, df1.compareTo(df2));
        df2.setTag("700");
        assertEquals(-1, df1.compareTo(df2));
    }

}
