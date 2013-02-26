package org.marc4j.test;

import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.marc4j.MarcJsonReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;

public class JsonReaderTest extends TestCase {

//    public void testMarcInJsonReader() throws Exception 
//    {
//        int i = 0;
//        InputStream input = getClass().getResourceAsStream("resources/marc-in-json.json");
//        MarcReader reader = new MarcJsonReader(input);
//        while (reader.hasNext()) 
//        {
//            Record record = reader.next();
//            String recordAsString = record.toString();
//            //System.err.println(recordAsString);
//            i++;
//        }
//        input.close();
//        assertEquals(1, i);
//        fail("Test incomplete - only record count is validated");
//    }
    
    public void testMarcJsonReaders() throws Exception 
    {
        InputStream input1 = getClass().getResourceAsStream("resources/marc-json.json");
        MarcReader reader1 = new MarcJsonReader(input1);
       
        InputStream input2 = getClass().getResourceAsStream("resources/marc-in-json.json");
        MarcReader reader2 = new MarcJsonReader(input2);
        while (reader1.hasNext() && reader2.hasNext()) 
        {
            Record record1 = reader1.next();
            Record record2 = reader2.next();
            String recordAsStrings1[] = record1.toString().split("\n");
            String recordAsStrings2[] = record2.toString().split("\n");
            for (int i = 0; i < recordAsStrings1.length ; i++)
            {
//                if (!recordAsStrings1[i].equals(recordAsStrings2[i]))
//                {
//                    i = i;
//                }
                assertEquals("line mismatch between two records", recordAsStrings1[i], recordAsStrings2[i]);
            }
            if (record1 != null && record2 != null) 
                RecordTestingUtils.assertEqualsIgnoreLeader(record1, record2);
        }
        input1.close();
        input2.close();
    }

	public static Test suite() {
	    return new TestSuite(JsonReaderTest.class);
	}
	
	public static void main(String args[]) {
	    TestRunner.run(suite());
	}
}
