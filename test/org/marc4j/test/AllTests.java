package org.marc4j.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("All Tests");
        suite.addTest(LeaderTest.suite());
        suite.addTest(ControlFieldTest.suite());
        suite.addTest(SubfieldTest.suite());
        suite.addTest(DataFieldTest.suite());
        suite.addTest(RecordTest.suite());
        suite.addTest(ReaderTest.suite());
        suite.addTest(WriterTest.suite());
        suite.addTest(RoundtripTest.suite());
        suite.addTest(PermissiveReaderTest.suite());
        suite.addTest(JsonReaderTest.suite());
        suite.addTest(JsonWriterTest.suite());
        return suite;
    }

    public static void main(String args[]) {
        TestRunner.run(suite());
    }

}
