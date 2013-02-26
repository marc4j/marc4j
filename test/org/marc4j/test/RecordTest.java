package org.marc4j.test;

import java.io.InputStream;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;

public class RecordTest extends TestCase {

    Record record = null;

    public void setUp() throws Exception {
        InputStream input = getClass().getResourceAsStream("resources/summerland.mrc");
        MarcReader reader = new MarcStreamReader(input);
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

        List<? extends VariableField> fieldList = record.getVariableFields();
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

    public void testFind() throws Exception {
        VariableField field = record.getVariableField("245");
        assertEquals(true, field.find("Summerland"));
        assertEquals(true, field.find("Sum*erland"));
        assertEquals(true, field.find("[Cc]habo[a-z]"));

        field = record.getVariableField("008");
        assertEquals(true, field.find("eng"));

        List<? extends VariableField> result = record.find("Summerland");
        assertEquals(1, result.size());
        field = (VariableField) result.get(0);
        assertEquals("245", field.getTag());

        result = record.find("Chabon");
        assertEquals(2, result.size());

        result = record.find("100", "Chabon");
        assertEquals(1, result.size());

        String[] tags = { "100", "260", "300" };
        result = record.find(tags, "Chabon");
        assertEquals(1, result.size());

        result = record.find("040", "DLC");
        assertTrue(result.size() > 0);

        DataField df = (DataField) result.get(0);
        String agency = df.getSubfield('a').getData();
        assertTrue(agency.matches("DLC"));

    }

    public void testCreateRecord() throws Exception {
        MarcFactory factory = MarcFactory.newInstance();
        Record record = factory.newRecord("00000cam a2200000 a 4500");
        assertEquals("00000cam a2200000 a 4500", record.getLeader().marshal());

        record.addVariableField(factory.newControlField("001", "12883376"));

        DataField df = factory.newDataField("245", '1', '0');
        df.addSubfield(factory.newSubfield('a', "Summerland /"));
        df.addSubfield(factory.newSubfield('c', "Michael Chabon."));
        record.addVariableField(df);
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
