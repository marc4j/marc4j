package org.marc4j.tests;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.text.DecimalFormat;

import org.marc4j.marc.*;

public class SubfieldTest extends TestCase {

    public SubfieldTest(String name) {
    	super(name);
    }

    public void testConstructor() {
	char[] data = "test".toCharArray();

	Subfield sf1 = new Subfield('a', data);
	Assert.assertEquals(sf1.getCode(), 'a');
	Assert.assertEquals(sf1.getData(), data);
    }

    public void testEquals() {
	Subfield sf1 = new Subfield('a', "test".toCharArray());
	Subfield sf2 = new Subfield('a', "test".toCharArray());
	Subfield sf3 = new Subfield('a', "piyoitfou".toCharArray());
        Assert.assertEquals(sf1, sf1);
        Assert.assertEquals(sf1.hashCode(), sf1.hashCode());
        Assert.assertTrue(!sf1.equals(sf2));
        Assert.assertTrue(!sf1.equals(sf3));

    }

    public void testMarshal() {
	Subfield sf1 = new Subfield('a', "test".toCharArray());
	Assert.assertEquals(sf1.marshal(), "atest");
    }

}


