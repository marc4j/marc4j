package org.marc4j;

import org.marc4j.marc.Leader;
import org.marc4j.marc.MarcFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class LeaderTest extends TestCase {

    MarcFactory factory = null;

    public void setUp() {
        factory = MarcFactory.newInstance();
    }

    public void testConstructor() {
        Leader leader = factory.newLeader();
        assertNotNull("leader is null", leader);
    }

    public void testUnmarshal() {
        Leader leader = factory.newLeader();
        leader.unmarshal("00714cam a2200205 a 4500");
        assertEquals("00714cam a2200205 a 4500", leader.toString());
    }

    public void testMarshal() {
        Leader leader = factory.newLeader("00714cam a2200205 a 4500");
        assertEquals("00714cam a2200205 a 4500", leader.marshal());
    }

    public void tearDown() {
        factory = null;
    }
    
	public static Test suite() {
	    return new TestSuite(LeaderTest.class);
	}
	
	public static void main(String args[]) {
	    TestRunner.run(suite());
	}
}
