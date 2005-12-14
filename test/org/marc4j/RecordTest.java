package org.marc4j;

import java.io.InputStream;
import java.util.List;

import org.marc4j.MarcStreamReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class RecordTest extends TestCase {

    Record record = null;

    public void setUp() throws Exception {
        InputStream input = getClass().getResourceAsStream(
                "resources/summerland.mrc");
        MarcStreamReader reader = new MarcStreamReader(input);
        while (reader.hasNext()) {
            record = reader.next();
        }
        input.close();
    }

    public void testGetFields() {
        String cn = record.getControlNumber();
        assertEquals("12883376", cn);
        
        ControlField cf = record.getControlNumberField();
        assertEquals("001", cf.getTag());
        assertEquals("12883376", cf.getData());
        
        List fieldList = record.getVariableFields();
        assertEquals(15, fieldList.size());
        
        fieldList = record.getControlFields();
        assertEquals(3, fieldList.size());

        fieldList = record.getDataFields();
        assertEquals(12, fieldList.size());

        VariableField field = record.getVariableField("245");
        assertEquals("245", field.getTag());
        
        fieldList = record.getVariableFields("650");
        assertEquals(3, fieldList.size());
        
        String[] fields = { "245", "260", "300" };
        fieldList = record.getVariableFields(fields);
        assertEquals(3, fieldList.size());
    }

	public void tearDown() {
		record = null;
	}
	
	public static Test suite() {
	    return new TestSuite(RecordTest.class);
	}
	
	public static void main(String args[]) {
	    TestRunner.run(suite());
	}
}
