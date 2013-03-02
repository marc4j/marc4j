package org.marc4j.test;

import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Subfield;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


public class SubfieldTest extends TestCase {

	MarcFactory factory = null;

	public void setUp() {
		factory = MarcFactory.newInstance();
	}

	public void testContructor() {
	    Subfield sf = factory.newSubfield();
	    assertNotNull("subfield is null", sf);
	    sf = factory.newSubfield('a');
	    assertEquals('a', sf.getCode());
	    sf = factory.newSubfield('a', "Summerland");
	    assertEquals('a', sf.getCode());
	    assertEquals("Summerland", sf.getData());
	}
	
	public void tearDown() {
		factory = null;
	}
	
	public static Test suite() {
	    return new TestSuite(SubfieldTest.class);
	}
	
	public static void main(String args[]) {
	    TestRunner.run(suite());
	}
}
