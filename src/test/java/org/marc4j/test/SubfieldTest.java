package org.marc4j.test;

import org.junit.Test;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Subfield;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class SubfieldTest  {

	MarcFactory  factory = MarcFactory.newInstance();


    @Test
	public void testContructor() {
	    Subfield sf = factory.newSubfield();
	    assertNotNull("subfield is null", sf);
	    sf = factory.newSubfield('a');
	    assertEquals('a', sf.getCode());
	    sf = factory.newSubfield('a', "Summerland");
	    assertEquals('a', sf.getCode());
	    assertEquals("Summerland", sf.getData());
	}

}
