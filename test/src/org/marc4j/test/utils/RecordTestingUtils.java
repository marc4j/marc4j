package org.marc4j.test.utils;

import static org.junit.Assert.*;

import org.marc4j.marc.*;

import java.io.*;
import java.util.*;

/**
 * Methods to assert when Record objects are equal or not, etc.
 * @author naomi
 *
 */
public class RecordTestingUtils 
{
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
		String expectedId = expected.getControlNumber();
		String errmsg = "Record " + actualId + " wasn't as expected";
	    
	    if (( actualId == null && expectedId == null ) || actualId != null && actualId.equals(expectedId) )
	    	assertTrue(errmsg, expected.toString().substring(24).equals(actual.toString().substring(24)) );
	    else
	    	fail(errmsg);
	}

	
	   /**
     * assert two Record objects are equal by comparing them as strings, skipping over the leader
     */
    public static String getFirstRecordDifferenceIgnoreLeader(Record expected, Record actual)
    {
        String actualId = actual.getControlNumber();
        String errmsg = "Record " + actualId + " wasn't as expected";
        
        String expectedSubstring = expected.toString().substring(24);
        String actualSubstring = actual.toString().substring(24);
        if ( actualId.equals(expected.getControlNumber()) )
        {
            if (!expectedSubstring.equals(actualSubstring) )
            {
                String expectedLines[] = expectedSubstring.split("\n");
                String actualLines[] = actualSubstring.split("\n");
                int i = 0;
                for (; i < Math.min(expectedLines.length, actualLines.length); i++)
                {
                    if (!expectedLines[i].equals(actualLines[i]))
                    {
                        return(expectedLines[i] + "\n" + actualLines[i]);
                    }
                }
                if (i >= expectedLines.length && i < actualLines.length)
                {
                    return(actualLines[i]);
                }
                if (i < expectedLines.length && i >= actualLines.length)
                {
                    return(expectedLines[i]);
                }
            }
        }       
        return(null);
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
    	
    	org.junit.Assert.assertEquals("Records weren't equal", buf.toString(), actualAsStrWithoutLdr);
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
	                org.junit.Assert.assertEquals("output line ["+ actualLine + "]  doesn't match expected [" + expectedLine + "]", expectedLine, actualLine);
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
	    Set<String> resultSet = new LinkedHashSet<String>();
	    for (VariableField vf : record.getVariableFields(fieldTag))
	    {
	    	DataField df = (DataField) vf;
	    	List<Subfield> sfList = df.getSubfields(subfieldCode);
	    	for (Iterator<Subfield> iter2 = sfList.iterator(); iter2.hasNext();) 
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
