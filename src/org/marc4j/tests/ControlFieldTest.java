package org.marc4j.tests;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.text.DecimalFormat;

import org.marc4j.marc.*;

public class ControlFieldTest extends TestCase {

    public ControlFieldTest(String name) {
    	super(name);
    }

    public void testConstructor() {
	char[] data = "test".toCharArray(); 
	ControlField cf2 = new ControlField("003", data);
	Assert.assertEquals(cf2.getTag(), "003");
	Assert.assertEquals(cf2.getData(), data);
    }

    public void testEquals() {
	ControlField cf1 = new ControlField("003", "test".toCharArray());
	ControlField cf2 = new ControlField("003", "test".toCharArray());
	ControlField cf3 = new ControlField("003", "uofiytyout".toCharArray());
        Assert.assertEquals(cf1, cf1);
        Assert.assertEquals(cf1.hashCode(), cf1.hashCode());
        Assert.assertTrue(!cf1.equals(cf2));
        Assert.assertTrue(!cf1.equals(cf3));

    }

    public void testSetter() {
	ControlField cf1 = new ControlField();
	try {
	    cf1.setTag("010");
	    fail("Should raise an IllegalTagException");
	} catch (IllegalTagException success) {}

	try {
	    cf1.setData("test");
	    fail("Should raise an IllegalDataElementException");
	} catch (IllegalDataElementException success) {}

    }

    public void testMarshal() {
	ControlField cf1 = new ControlField("003", "test".toCharArray());
	Assert.assertEquals(cf1.marshal(), "test");
    }

}


