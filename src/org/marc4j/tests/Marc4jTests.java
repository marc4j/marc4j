package org.marc4j.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class Marc4jTests extends TestCase {

    public Marc4jTests() {
        super("All Tests for MARC4J");   
    }

    public static Test suite() {
        TestSuite result = new TestSuite();
        result.addTest(new TestSuite(TagTest.class));
        result.addTest(new TestSuite(SubfieldTest.class));
        result.addTest(new TestSuite(ControlFieldTest.class));
        result.addTest(new TestSuite(DataFieldTest.class));
        return result;
    }

}
