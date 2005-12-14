package org.marc4j;

import org.marc4j.converter.impl.AnselToUnicode;
import org.marc4j.converter.impl.UnicodeToAnsel;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class CharConverterTest extends TestCase {
    
    private static final String ANSEL = "\u0088\u0089\u00AA\u00AB\u00B9\u00BA\u00F9";
    
    private static final String UNICODE = "\u0098\u009C\u00AE\u00B1\u00A3\u00F0\u032E";
    
    public void testAnselToUnicode() {
        AnselToUnicode converter = new AnselToUnicode();
        String out = converter.convert(ANSEL);
        assertEquals(UNICODE, out);        
    }
    
    public void testUnicodeToAnsel() {
        UnicodeToAnsel converter = new UnicodeToAnsel();
        String out = converter.convert(UNICODE);
        assertEquals(ANSEL, out);        
    }
    
	public static Test suite() {
	    return new TestSuite(CharConverterTest.class);
	}
	
	public static void main(String args[]) {
	    TestRunner.run(suite());
	}

}
