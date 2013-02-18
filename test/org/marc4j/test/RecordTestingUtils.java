package org.marc4j.test;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.junit.Test;
import org.marc4j.MarcWriter;
import org.marc4j.marc.*;
import org.marc4j.marc.impl.DataFieldImpl;
import org.solrmarc.marc.RawRecordReader;
import org.solrmarc.marcoverride.MarcSplitStreamWriter;
import org.solrmarc.tools.RawRecord;

/**
 * Methods to assert when Record objects are equal or not, etc.
 * @author naomi
 *
 */
public class RecordTestingUtils 
{
    private static String testDir = "test";
    private static String testDataParentPath = System.getProperty("test.data.path", /*default to */testDir + File.separator + "data");
    private static String smokeTestDir = testDataParentPath + File.separator + "smoketest";
    private static String testConfigFile = System.getProperty("test.config.file", /*default to */smokeTestDir + File.separator + "test_config.properties");
 //   private static String testConfigFile = smokeTestDir + File.separator + testConfigFname;

    private static final String MARC_PRINTER_CLASS_NAME = "org.solrmarc.marc.MarcPrinter";
    private static final String MAIN_METHOD_NAME = "main";
    public static final String BIB_REC_FILE_NAME = "resources" + File.separator + "u335.mrc";

    /**
	 * assert two Record objects are equal by comparing them as strings
	 */
	public static void assertEquals(Record expected, Record actual)
	{
		String actualId = actual.getControlNumber();
		String errmsg = "Record " + actualId + " wasn't as expected";
	    
	    if ( actualId.equals(expected.getControlNumber()) )
	    	assertTrue(errmsg, expected.toString().equals(actual.toString()) );
	    else
	    	fail(errmsg);
	}

	/**
	 * assert two Record objects aren't equal by comparing them as strings
	 */
	public static void assertNotEqual(Record expected, Record actual)
	{
		String actualId = actual.getControlNumber();
	    if ( !actualId.equals(expected.getControlNumber()) )
	    	return;
	
	    assertFalse("Records unexpectedly the same: " + actualId, expected.toString().equals(actual.toString()) );
	}

	/**
	 * assert two Record objects are equal by comparing them as strings, skipping over the leader
	 */
	public static void assertEqualsIgnoreLeader(Record expected, Record actual)
	{
		String actualId = actual.getControlNumber();
		String errmsg = "Record " + actualId + " wasn't as expected";
	    
	    if ( actualId.equals(expected.getControlNumber()) )
	    	assertTrue(errmsg, expected.toString().substring(24).equals(actual.toString().substring(24)) );
	    else
	    	fail(errmsg);
	}

	/**
	 * assert two Record objects are not equal by comparing them as strings, skipping over the leader
	 */
	public static void assertNotEqualIgnoreLeader(Record expected, Record actual)
	{
		String actualId = actual.getControlNumber();
	    if ( !actualId.equals(expected.getControlNumber()) )
	    	return;

	    assertFalse("Records unexpectedly the same: " + actualId, expected.toString().substring(24).equals(actual.toString().substring(24)) );
	}

	/**
	 * assert two RawRecord objects are equal 
	 *  First convert them to Record objects, assuming MARC8 encoding using permissive conversion, 
	 *  not converting them to utf8, combining 999 partials, and assuming
	 */
	public static void assertEquals(RawRecord expected, RawRecord actual, String encoding)
	{
		assertEquals(expected.getAsRecord(true,false, "999", encoding), actual.getAsRecord(true,false, "999", encoding));
	}

	/**
	 * assert two RawRecord objects are not equal by comparing them as byte[]
	 *  First convert them to Record objects, assuming MARC8 encoding using permissive conversion, 
	 *  not converting them to utf8, combining 999 partials, and assuming
	 */
	public static void assertNotEqual(RawRecord expected, RawRecord actual, String encoding)
	{
		assertNotEqual(expected.getAsRecord(true,false, "999", encoding), actual.getAsRecord(true,false, "999", encoding));
	}

    /**
     * compare two marc records;  the expected result is represented as
     *  an array of strings.  The leaders don't match; not sure why or if it
     *  matters.
     * @param expected
     * @param actual
     */
    public static void assertEqualsIgnoreLeader(String[] expected, Record actual) 
    {
    	String actualAsStr = actual.toString();
     	// removing leader is removing "LEADER " and the 24 char leader and the newline
    	String actualAsStrWithoutLdr = actualAsStr.substring(32);

     	StringBuffer buf = new StringBuffer();
    	for (int i = 1; i < expected.length; i++) {
    		buf.append(expected[i] + "\n");
    	}
    	
    	junit.framework.Assert.assertEquals("Records weren't equal", buf.toString(), actualAsStrWithoutLdr);
    }

    /**
	 * Given an expected marc record as an Array of strings corresponding to 
	 *  the lines in the output of MarcPrinter and given the actual marc record as an InputStream,
	 *  assert they are equal
	 */
	public static void assertMarcRecsEqual(String[] expectedAsLines, InputStream actualAsInputStream) 
	{
	    BufferedReader actualAsBuffRdr = null;
	    try
	    {
	        actualAsBuffRdr = new BufferedReader(new InputStreamReader(actualAsInputStream, "UTF-8"));
	    }
	    catch (UnsupportedEncodingException e)
	    {
	        e.printStackTrace();
	        fail("couldn't read record to be tested from InputStream");
	    }
	
	    int numExpectedLines = expectedAsLines.length;
	
	    try
	    {
	        int lineCnt = 0;
	        String actualLine = null;
	        while ((actualLine = actualAsBuffRdr.readLine()) != null)
	        {
	            if (actualLine.length() == 0) 
	            {
	            	// do nothing;
	            }
	            else if (numExpectedLines > 0 && lineCnt < numExpectedLines) 
	            {
	                if (actualLine.equals("Flushing results...") || actualLine.equals("Flushing results done") || actualLine.startsWith("Cobertura:"))
	                    continue;   // skip this line and don't even count it.  I don't know where these "Flushing Results..." lines are coming from.
	
	                String expectedLine = expectedAsLines[lineCnt];
	                junit.framework.Assert.assertEquals("output line ["+ actualLine + "]  doesn't match expected [" + expectedLine + "]", expectedLine, actualLine);
	            }
	            lineCnt++;
	        }
	    }
	    catch (IOException e)
	    {
	        e.printStackTrace();
	        fail("couldn't compare records");
	    }
	}

	/**
	 * Assert that each instance of the subfield is in the expected values
	 *  and that the number of instances match.
	 */
	public static void assertSubfieldHasExpectedValues(Record record, String fieldTag, char subfieldCode, Set<String> expectedVals)
	{
	    List<VariableField> vfList = record.getVariableFields(fieldTag);
	    Set<String> resultSet = new LinkedHashSet<String>();
	    for (Iterator iter = vfList.iterator(); iter.hasNext();)
	    {
	    	DataField df = (DataField) iter.next();
	    	List<Subfield> sfList = df.getSubfields(subfieldCode);
	    	for (Iterator iter2 = sfList.iterator(); iter2.hasNext();) 
	    	{
	    		Subfield sf = (Subfield) iter2.next();
	    		String val = sf.getData();
	    		resultSet.add(val);
    			assertTrue("Got unexpected value " + val, expectedVals.contains(val));
			}
	    }
	    org.junit.Assert.assertEquals("Number of values doesn't match", expectedVals.size(), resultSet.size());
	}
	
	
	/**
	 * convert a Record object to a RawRecord object.  
	 * Uses MarcSplitStreamWriter to output the record so it can be read in again.
	 */
	public static RawRecord convertToRawRecord(Record record)
	{
	    // prepare to trap MarcWriter output stream 
		ByteArrayOutputStream sysBAOS = new ByteArrayOutputStream();
		PrintStream sysMsgs = new PrintStream(sysBAOS);
		System.setOut(sysMsgs);
		
		MarcWriter writer = new MarcSplitStreamWriter(System.out, "ISO-8859-1", 70000, "999");
	    writer.write(record);
	    System.out.flush();
		
		ByteArrayInputStream recAsInStream = new ByteArrayInputStream(sysBAOS.toByteArray());
		
		return new RawRecord(new DataInputStream((InputStream) recAsInStream));
	}
	
	/**
	 * given a file of records as a ByteArrayOutputStream and a record id,
	 *  look for that record.  If it is found, return it as a RawRecord object,
	 *  otherwise, return null
	 */
// FIXME: not used? tested, even?
	public static RawRecord extractRecord(ByteArrayOutputStream recsFileAsBAOutStream, String recId)
	{
	    ByteArrayInputStream fileAsInputStream = new ByteArrayInputStream(recsFileAsBAOutStream.toByteArray());
		RawRecordReader fileRawRecReader = new RawRecordReader(fileAsInputStream);    	
	    while (fileRawRecReader.hasNext())
	    {
	        RawRecord rawRec = fileRawRecReader.next();
	        if (recId == rawRec.getRecordId())
	        	return rawRec;
	    }
	    return null;
	}

	
// Tests for assertion methods ------------------------------------------------	
	
	
	/**
	 * ensure that the assertEquals and assertNotEqual methods work for 
	 *  RawRecord objects
	 */
@Test
    public void testRawRecordAssertEqualsAndNot()
  		throws IOException
  	{

          RawRecordReader bibsRawRecRdr = new RawRecordReader(getTestRecord(BIB_REC_FILE_NAME));
  	    if (bibsRawRecRdr.hasNext()) {
  	    	
  	        RawRecord rawRec1 = bibsRawRecRdr.next();
  	        assertEquals(rawRec1, rawRec1, "MARC8");
  	        
  	        RawRecordReader bibsRawRecRdr2 = new RawRecordReader(getTestRecord(BIB_REC_FILE_NAME));
  	        if (bibsRawRecRdr2.hasNext()) {
  	  	        RawRecord rawRec2 = bibsRawRecRdr2.next();
  	  	        Record rec2 = rawRec2.getAsRecord(true, false, "999", "MARC8");
  	  	        DataField dataFld = new DataFieldImpl("333", ' ', ' ');
  	  	        rec2.addVariableField(dataFld);
  	  	        assertNotEqual(rawRec1, convertToRawRecord(rec2), "MARC8");
  	        }
  	  	    else
  	            fail("shouldn't get here");
  	    }
  	    else
            fail("shouldn't get here");
  	}

	/**
	 * ensure that the assertEquals and assertNotEqual methods work for 
	 *  Record objects
	 */
@Test  	
  	public void testRecordAssertEqualsAndNot()
  	  		throws IOException
  	{

  	    RawRecordReader bibsRawRecRdr = new RawRecordReader(getTestRecord(BIB_REC_FILE_NAME));
  	    if (bibsRawRecRdr.hasNext()) 
  	    {
  	        RawRecord rawRec1 = bibsRawRecRdr.next();
  	        Record rec1 = rawRec1.getAsRecord(true, false, "999", "MARC8");

  	        assertEquals(rec1, rec1);
  	        
  	        RawRecordReader bibsRawRecRdr2 = new RawRecordReader(getTestRecord(BIB_REC_FILE_NAME));
  	        if (bibsRawRecRdr2.hasNext())
  	        {
  	  	        RawRecord rawRec2 = bibsRawRecRdr2.next();
  	  	        Record rec2 = rawRec2.getAsRecord(true, false, "999", "MARC8");
  	  	        DataField dataFld = new DataFieldImpl("333", ' ', ' ');
  	  	        rec2.addVariableField(dataFld);

  	  	        assertNotEqual(rec1, rec2);
  	        }
  	  	    else
  	            fail("shouldn't get here");
  	    }
  	    else
            fail("shouldn't get here");
  	}

	/**
	 * ensure that the assertEquals and assertNotEqual methods work for 
	 *  Record objects
	 */
@Test  	
	public void testRecordAssertEqualsIgnoreLeaderAndNot()
	  		throws IOException
	{

        RawRecordReader bibsRawRecRdr = new RawRecordReader(getTestRecord(BIB_REC_FILE_NAME));
	    if (bibsRawRecRdr.hasNext()) 
	    {
	        RawRecord rawRec1 = bibsRawRecRdr.next();
	        Record rec1 = rawRec1.getAsRecord(true, false, "999", "MARC8");

	        assertEqualsIgnoreLeader(rec1, rec1);
	        
	        RawRecordReader bibsRawRecRdr2 = new RawRecordReader(getTestRecord(BIB_REC_FILE_NAME));
	        if (bibsRawRecRdr2.hasNext())
	        {
	  	        RawRecord rawRec2 = bibsRawRecRdr2.next();
	  	        Record rec2 = rawRec2.getAsRecord(true, false, "999", "MARC8");
	  	        DataField dataFld = new DataFieldImpl("333", ' ', ' ');
	  	        rec2.addVariableField(dataFld);

	  	        assertNotEqualIgnoreLeader(rec1, rec2);
	        }
	  	    else
	            fail("shouldn't get here");
	    }
	    else
        fail("shouldn't get here");
	}

    private InputStream getTestRecord(String bibRecFileName) throws FileNotFoundException {
        return  getClass().getResourceAsStream(bibRecFileName);
    }

    /**
	 * Assign id of record to be the ckey. Our ckeys are in 001 subfield a. 
	 * Marc4j is unhappy with subfields in a control field so this is a kludge 
	 * work around.
	 */
	public static String getRecordIdFrom001(Record record)
	{
		String id = null;
		ControlField fld = (ControlField) record.getVariableField("001");
		if (fld != null && fld.getData() != null) 
		{
			String rawVal = fld.getData();
			// 'u' is for testing
			if (rawVal.startsWith("a") || rawVal.startsWith("u"))
				id = rawVal.substring(1);
		}
		return id;
	}

}
