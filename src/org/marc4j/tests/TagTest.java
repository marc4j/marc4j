package org.marc4j.tests;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.text.DecimalFormat;

import org.marc4j.marc.*;

public class TagTest extends TestCase {

    public TagTest(String name) {
    	super(name);
    }

    private DecimalFormat df = new DecimalFormat("000");

    protected void setUp() {}

    public void testIsValid() {
	for (int i = 1; i <= 999; i++) {
	    String tag = df.format(i).toString();
	    Assert.assertTrue(Tag.isValid(tag));
	}
	try {
	    Tag.isValid("1234");
	    fail("Should raise an IllegalTagException");
	} catch (IllegalTagException success) {}
    }

    public void testIsControlField() {
	for (int i = 1; i < 10; i++) {
	    String tag = df.format(i).toString();
	    Assert.assertTrue(Tag.isControlField(tag));
	}

	for (int i = 10; i <= 999; i++) {
	    String tag = df.format(i).toString();
	    Assert.assertTrue(! Tag.isControlField(tag));
	}
    }

    public void testIsControlNumberField() {
	String tag = df.format(1).toString();
	Assert.assertTrue(Tag.isControlNumberField(tag));

	for (int i = 2; i <= 999; i++) {
	    tag = df.format(i).toString();
	    Assert.assertTrue(! Tag.isControlNumberField(tag));
	}
    }

    public void isDataField() {
	for (int i = 10; i <= 999; i++) {
	    String tag = df.format(i).toString();
	    Assert.assertTrue(Tag.isDataField(tag));
	}

	for (int i = 1; i < 10; i++) {
	    String tag = df.format(i).toString();
	    Assert.assertTrue(! Tag.isDataField(tag));
	}
    }

}


