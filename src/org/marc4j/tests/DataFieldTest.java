package org.marc4j.tests;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.text.DecimalFormat;

import org.marc4j.marc.*;

public class DataFieldTest extends TestCase {

    public DataFieldTest(String name) {
    	super(name);
    }

    private DataField df;

    public void setUp() {
	df = new DataField("245", '1', '2');
	df.add(new Subfield('a', "test".toCharArray()));
    }

    public void testConstructor() {
	DataField df1 = new DataField("245", '1','2');
	Assert.assertEquals(df1.getTag(), "245");
	Assert.assertEquals(df1.getIndicator1(), '1');
	Assert.assertEquals(df1.getIndicator2(), '2');
    }

    public void testEquals() {
	DataField df1 = new DataField("245", '1','2');
	DataField df2 = new DataField("245", '1','2');
	DataField df3 = new DataField("100", '1','0');
        Assert.assertEquals(df1, df1);
        Assert.assertEquals(df1.hashCode(), df1.hashCode());
        Assert.assertTrue(!df1.equals(df2));
        Assert.assertTrue(!df1.equals(df3));
    }

    public void testSetters() {
	DataField df1 = new DataField("245", '1','2');

	df1.setTag("100");
	Assert.assertEquals(df1.getTag(), "100");

	Subfield sf1 = new Subfield('a', "test".toCharArray());
	df1.add(sf1);
	Assert.assertEquals(df1.getSubfield('a'), sf1);

	df1.setIndicator1('1');
	Assert.assertEquals(df1.getIndicator1(), '1');

	df1.setIndicator2('2');
	Assert.assertEquals(df1.getIndicator2(), '2');

	try {
	    df1.setTag("009");
	    fail("Should raise an IllegalTagException");
	} catch (IllegalTagException success) {}

	try {
	    df1.setIndicator1('');
	    fail("Should raise an IllegalDataElementException");
	} catch (IllegalDataElementException success) {}

	try {
	    df1.setIndicator2('');
	    fail("Should raise an IllegalDataElementException");
	} catch (IllegalDataElementException success) {}

   }

    public void testHasSubfield() {
	Assert.assertEquals(df.hasSubfield('a'), true);
	Assert.assertEquals(df.hasSubfield('x'), false);
    }

    public void testGetLength() {
	Assert.assertTrue(df.getLength() == 9);
	Assert.assertTrue(df.getLength() != 12);
    }

    public void testMarshal() {
        Assert.assertTrue(df.marshal().equals("12atest"));
        Assert.assertTrue(!df.marshal().equals("12adioger"));
    }

}

