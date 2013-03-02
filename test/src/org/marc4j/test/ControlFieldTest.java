package org.marc4j.test;

import org.junit.Test;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.MarcFactory;

import static org.junit.Assert.assertEquals;


public class ControlFieldTest  {

	MarcFactory factory = MarcFactory.newInstance();

    @Test
	public void testConstructor() throws Exception {
	    ControlField cf = factory.newControlField("001");
	    assertEquals("001", cf.getTag());
	}

    @Test
	public void testSetData() throws Exception {
	    ControlField cf = factory.newControlField("001");
	    cf.setData("12883376");
	    assertEquals("12883376", cf.getData());
	}

    @Test
    public void testComparable() throws Exception {
        ControlField cf1 = factory.newControlField("008", "12345");
        ControlField cf2 = factory.newControlField("008", "12345");
        assertEquals(0, cf1.compareTo(cf2));
        cf2.setTag("009");
        assertEquals(-1, cf1.compareTo(cf2));
        cf2.setTag("007");
        assertEquals(1, cf1.compareTo(cf2));
    }

}
