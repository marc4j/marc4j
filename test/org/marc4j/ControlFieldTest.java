package org.marc4j;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.MarcFactory;


public class ControlFieldTest extends TestCase {

	MarcFactory factory = null;

	public void setUp() throws Exception {
		factory = MarcFactory.newInstance();
	}

	public void testConstructor() throws Exception {
	    ControlField cf = factory.newControlField("001");
	    assertEquals("001", cf.getTag());
	}

	public void testSetData() throws Exception {
	    ControlField cf = factory.newControlField("001");
	    cf.setData("12883376");
	    assertEquals("12883376", cf.getData());
	}
    
    public void testComparable() throws Exception {
        ControlField cf1 = factory.newControlField("008", "12345");
        ControlField cf2 = factory.newControlField("008", "12345");
        assertEquals(0, cf1.compareTo(cf2));
        cf2.setTag("009");
        assertEquals(-1, cf1.compareTo(cf2));
        cf2.setTag("007");
        assertEquals(1, cf1.compareTo(cf2));
    }
	
	public void tearDown() {
		factory = null;
	}
	
	public static Test suite() {
	    return new TestSuite(ControlFieldTest.class);
	}
	
	public static void main(String args[]) {
	    TestRunner.run(suite());
	}

}
