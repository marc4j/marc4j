package org.marc4j.util;

import java.io.*;
import java.util.*;

import org.marc4j.marc.Record;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.VariableField;
import org.marc4j.MarcCombiningReader;
import org.marc4j.MarcReader;
import org.marc4j.MarcSplitStreamWriter;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;


/**
 * Given a file of MARC bib records and another file of MARC (MHLD) records,
 *  read through the bib file and look for matching MHLD records.  If found,
 *  merge the desired fields from the MHLD record into the bib record, first
 *  removing any existing fields in the bib rec matching a desired field tag.
 *  
 * Note that the MHLD file must have records in StringNaturalCompare ascending
 *  order.
 *  
 * Note that Naomi worked with this code to make it more testable; see
 *  org.solrmarc.tools.MergeSummaryHoldingsTests
 *  
 * But she forked this class for her own use,  ultimately, and did not
 *  continue all the way down this path.
 *  
 * @author Bob Haschart, revised by Naomi Dushay
 *
 */
public class MergeSummaryHoldings implements MarcReader
{
    /** default list of MHLD fields to be merged into the bib record, separated by '|' char */
    public static String DEFAULT_MHLD_FLDS_TO_MERGE = "852|853|863|866|867|868";
    
    public static Comparator<String> ID_COMPARATOR = new StringNaturalCompare();

    static boolean verbose = false;
    static boolean veryverbose = false;

    /** true if we want to attempt to use marc records if they are invalid in ways we can safely ignore 
     * used when reading bib and mhld records */
    private boolean permissive;

    /** true if records should be converted to UTF-8 when they become Record objects
    * used when reading bib and mhld records */
    private boolean toUtf8;
    
    /** the encoding to use as a default for reading the records; usually MARC8
    * used when reading bib and mhld records */
    private String defaultEncoding = null;

    /** list of MHLD fields to be merged into the bib record, separated by '|' char */
    private String mhldFldsToMerge = null;



    /** for the file of MARC bib records */
    private RawRecordReader bibRecsRawRecRdr = null;
    
    /** can be used as an alternative reader for the file of MARC bib records*/
    private MarcReader bibRecsMarcReader = null;

    /** the name of the file containing MHLD records.  It must be a class variable
     * because the file may need to be read multiple times to match bib records */
    private String mhldRecsFileName;
    
    /** for the file of MARC MHLD records */
    private RawRecordReader mhldRawRecRdr = null;

    /**
     * the last mhld record read, but not yet compared with a bib record
     */
    private RawRecord currentMhldRec = null;
    
    

    /**
     * used to find an MHLD record matching the current bib record
     * set to null if:
     *   - we are at the beginning of the MHLD record file
     *   - if we found an MHLD record to match current the bib record 
     * otherwise set to:
     *   - the last mhld record read -- the mhld record with an id greater than the bib id, or the last mhld record in the file
     * if we were able to find a matching record, this is set to null
     */
    private RawRecord unmatchedPrevMhldRec = null;
    
    /**
     *  used to find an MHLD record matching the current bib record - particular
     *   this tells us if we need to start over at the beginning of the MHLD file
     *   to look for matches
     * if we have a matching MHLD record, this is the id of that MHLD record
     * if we don't have a matching MHLD record, this is set to the id of the MHLD record previous to the last mhld record read (unmatchedPrevMhldRec)
     * */
    private String prevMhldRecID = null;

   
    public MergeSummaryHoldings(RawRecordReader bibRecsRawRecRdr, boolean permissive, boolean toUtf8, String defaultEncoding, 
                                String mhldRecsFileName, String mhldFldsToMerge)
    {
        this.bibRecsRawRecRdr = bibRecsRawRecRdr;
        bibRecsMarcReader = null;
        this.mhldRecsFileName = mhldRecsFileName;
        this.permissive = permissive;
        this.toUtf8 = toUtf8;
        this.defaultEncoding = defaultEncoding;
        this.mhldFldsToMerge = mhldFldsToMerge;
        System.setProperty("org.marc4j.marc.MarcFactory", "org.solrmarc.marcoverride.NoSortMarcFactoryImpl");
        readMhldFileFromBeginning(mhldRecsFileName);
    }
    
    public MergeSummaryHoldings(RawRecordReader bibRecsRawRecRdr, String mhldRecsFileName, String mhldFldsToMerge)
    {
        this (bibRecsRawRecRdr, true, false, "MARC8", mhldRecsFileName, mhldFldsToMerge);
    }
    
    public MergeSummaryHoldings(MarcReader bibRecsRawRecRdr, String mhldRecsFileName, String mhldFldsToMerge)
    {
        this.bibRecsRawRecRdr = null;
        bibRecsMarcReader = bibRecsRawRecRdr;
        this.mhldRecsFileName = mhldRecsFileName;
        this.mhldFldsToMerge = mhldFldsToMerge;
        readMhldFileFromBeginning(mhldRecsFileName);
    }
    
    
    /**
     * create a new RawRecordReader for the MHLD records file, and reset
     *  prevMhldRecID and unmatchedPrevMhldRec to null
     * @param mhldRecsFileName
     */
    private void readMhldFileFromBeginning(String mhldRecsFileName)
    {
        try
        {
        	mhldRawRecRdr = new RawRecordReader(new FileInputStream(new File(mhldRecsFileName)));
        }
        catch (FileNotFoundException e)
        {
			System.err.println("No file found at " + mhldRecsFileName);
        	mhldRawRecRdr = null;           
        }
        prevMhldRecID = null;
        unmatchedPrevMhldRec = null;
//    	currentMhldRec = getNextMhld();
    }

    /**
     * NOTE: not used by main()
     * @return true if there is another record in the bib records file
     */
    public boolean hasNext()
    {
        if (bibRecsRawRecRdr != null) 
        	return(bibRecsRawRecRdr.hasNext());
        else if (bibRecsMarcReader != null) 
        	return(bibRecsMarcReader.hasNext());
        return(false);
    }
    
    /**
     * NOTE: not used by main();
     *   Since this class is a MarcReader, it must implement the next() method.
     * Get the next bib record from the file of MARC bib records, then look 
     *  for a matching MARC MHLD record in the MHLD recs file, and if found, 
     *  merge the MHLD fields specified in mhldFldsToMerge into the bib 
     *  record and then return the bib record.
     * @return Record object containing fields merged from matching mhld 
     *  record, if there was one
     */
    public Record next()
    {
        Record bibRec = null;
        if (bibRecsRawRecRdr != null) 
        {
            RawRecord rawrec = bibRecsRawRecRdr.next();
            bibRec = rawrec.getAsRecord(permissive, toUtf8, "999", defaultEncoding);
        }
        else if (bibRecsMarcReader != null)
        {
            bibRec = bibRecsMarcReader.next();
        }
        RawRecord matchingRawMhldRec = getMatchingMhldRawRec(bibRec.getControlNumber());
        if (matchingRawMhldRec != null)
        {
            bibRec = addMhldFieldsToBibRec(bibRec, matchingRawMhldRec);
        }
        
// Naomi's failed experiment - Mhld reader needs to be a combining reader
/*        
        if (bibRecsRawRecRdr != null && bibRecsRawRecRdr.hasNext()) 
        {
            RawRecord rawBibRec = bibRecsRawRecRdr.next();
            bibRec = rawBibRec.getAsRecord(permissive, toUtf8, "999", defaultEncoding);
            if (bibRec != null)
            {
                Set<RawRecord> matchingRawMhldRecs = getMatchingMhldRawRecs(rawBibRec.getRecordId());
                for (Iterator iter = matchingRawMhldRecs.iterator(); iter.hasNext();) 
                {
                    RawRecord matchingRawMhldRec = (RawRecord) iter.next();
                    bibRec = addMhldFieldsToBibRec(bibRec, matchingRawMhldRec);
                }
            }
        }
*/        
        
        return(bibRec);
    }

//    
//    /**
//     * FIXME:  doesn't work:  mhld reader needs to be a (non-existent) RawRecordCombiningReader
//     * NOTE: not used by main(); new method by Naomi Dushay 
//     * Look for records in the MHLD file that match the bibId, returning all
//     *  matching records as a Set of RawRecord objects.  Not that "matching"
//     *  means the Ids match, where id is from RawRecord.getRecordId.
//     *  
//     * @param bibRecID - the id to match
//     * @return Set of RawRecord objects corresponding to MHLD records that match
//     *  the bibId
//     */
//    private Set<RawRecord> getMatchingMhldRawRecs(String bibRecID)
//    {
//        Set<RawRecord> result = new LinkedHashSet<RawRecord>();
//        
//        int compareResult = ID_COMPARATOR.compare(currentMhldRec.getRecordId(), bibRecID);
//        
//        if (compareResult > 0)
//            // MHLD id is after bib id:  we're done and we do not advance in MHLD file
//            return result;
//        else
//        {
//            if (compareResult == 0)
//                // current MHLD matches the bibRec: keep it and look for more matches
//                result.add(currentMhldRec);
//
//            // proceed to next MHLD record and look for another match
//            //  but only if it's not the last MHLD in the file
//            // NOTE:  THIS is where the assumption that the bib file is in ascending ID order is made
//            if (mhldRawRecRdr.hasNext())
//            {
//                currentMhldRec = getNextMhld();
//                result.addAll(getMatchingMhldRawRecs(bibRecID));
//            }
//        }
//
//        return result;
//    }
    
    /**
     * NOTE: not used by main(); new method by Naomi Dushay 
     * NOTE: only call this method if:
     *  1) you are sure there is a next record in the file
     *    OR
     *  2) you want to start over from the beginning of the MHLD file if there
     *    are no more records to read from the file 
     * @return the next record in the MHLD file, if there is one.  Otherwise
     *  start reading the mhld file from the beginning, and return the first record.
     */
    private RawRecord getNextMhld()
    {
    	if (mhldRawRecRdr != null)
    	{
        	if (mhldRawRecRdr.hasNext())
        		// there is another record
                currentMhldRec = mhldRawRecRdr.next(); 
        	else
        		readMhldFileFromBeginning(mhldRecsFileName); // sets currentMhldRec

        	return currentMhldRec;
    	}

    	return null;
    }

    
    /**
     * given a bib record ID, find the next MHLD record with a matching id.  
     *  Also sets  unmatchPrevMhldRec and prevMhldRecID and sometimes mhldRawRecRdr
     *  
     * Note that this method appears to find the next matchingMHLD even if the 
     *  bibRecId is the same as the id for the previously matchingMHLD
     * @param bibRecID
     * @return
     */
    private RawRecord getMatchingMhldRawRec(String bibRecID)
    {
    	// if the id before the last read MHLD id is bigger than the bib id to be 
    	//   matched, then start over in the mhld file
        if (prevMhldRecID != null && ID_COMPARATOR.compare(prevMhldRecID, bibRecID) > 0)
        {
        	readMhldFileFromBeginning(mhldRecsFileName);
        }
        
        // if the most recent MHLD record read was a match, or we have started MHLD file from beginning
        //    (we have no unmatching last retrieved mhld record)
        // then get the next record in the MHLD file before entering loop
        if (unmatchedPrevMhldRec == null && mhldRawRecRdr != null && mhldRawRecRdr.hasNext() )
        {
            unmatchedPrevMhldRec = mhldRawRecRdr.next();
        }
        
        // look for an MHLD record that matches the bib rec id, up until the MHLD record id comes after the bib record id;  
        // also keep track of the prior MHLD rec id while searching 
        while (mhldRawRecRdr != null && mhldRawRecRdr.hasNext() && ID_COMPARATOR.compare(unmatchedPrevMhldRec.getRecordId(), bibRecID) < 0)
        {
        	// keep the previous MHLD id before we get the new MHLD record
            prevMhldRecID = unmatchedPrevMhldRec.getRecordId();
            unmatchedPrevMhldRec = mhldRawRecRdr.next(); // well, it might match, but we'll address that in the next lines
        }
        
        // if we have a matching mhld, then set prevMhldRecID to the matching record and set unmatchedPrevMhldRec to null
        //  before returning the matching MHLD record
        if (unmatchedPrevMhldRec != null && ID_COMPARATOR.compare(unmatchedPrevMhldRec.getRecordId(), bibRecID) == 0)
        {
            RawRecord matchingMhldRec = unmatchedPrevMhldRec; 
            unmatchedPrevMhldRec = null;
            prevMhldRecID = matchingMhldRec.getRecordId();   
            return(matchingMhldRec);
        }
        
        return(null);		// booby prize
    }
    
    /**
     * NOTE: not used by main() - only used by next()
     * 
     * given a MARC bib record as a Record object, and a MARC MHLD record as
     *  a RawRecord object, merge the MHLD fields indicated in class var
     *  mhldFldsToMerge into the bib record, first removing any of those fields
     *  already existing in the bib record.
     * @param bibRecord
     * @param rawMhldRecord
     * @return the bib record with the MHLD fields merged in prior to the 999
     */
    private Record addMhldFieldsToBibRec(Record bibRecord, RawRecord rawMhldRecord)
    {
        Record mhldRecord = rawMhldRecord.getAsRecord(permissive, toUtf8, mhldFldsToMerge, defaultEncoding);
        List<VariableField> lvf = (List<VariableField>) bibRecord.getVariableFields(mhldFldsToMerge.split("[|]"));
        for (VariableField vf : lvf)
        {
            bibRecord.removeVariableField(vf);
        }
        bibRecord = MarcCombiningReader.combineRecords(bibRecord, mhldRecord, mhldFldsToMerge, "999");
        return(bibRecord);
    }

    /**
     * this is a Naomi Dushay rewrite method, not called by main(), written
     *   basically for testing 
     * for each bib record in the bib rec file 
     *  look for a corresponding mhld record.  If a match is found, 
     *    1) remove any existing fields in the bib record that duplicate the mhld fields to be merged into the bib record
     *    2) merge the mhld fields into the bib record
     * then add the bib record (whether it had a match or not) to the List of records
     * @param bibRecsFileName - the name of the file containing MARC Bibliographic records
     * @param mhldRecsFileName - the name of the file containing MARC MHLD records
     * @return Map of ids/Record objects for the bib records, which will include mhld fields if a match was found
     * @throws java.io.IOException if the file bibRecsFileName cannot be opened and read
     */
    public static Map<String, Record> mergeMhldsIntoBibRecordsAsMap(String bibRecsFileName, String mhldRecsFileName)
        throws IOException
    {
        Map<String, Record> results = new HashMap<String, Record>();
        RawRecordReader bibsRawRecRdr = new RawRecordReader(new FileInputStream(new File(bibRecsFileName)));

        boolean permissive = true;
        boolean toUtf8 = false;
        MergeSummaryHoldings merger = new MergeSummaryHoldings(bibsRawRecRdr, permissive, toUtf8, "MARC8", 
                                                               mhldRecsFileName, DEFAULT_MHLD_FLDS_TO_MERGE);
        verbose = true;
        veryverbose = true;
        while (merger.hasNext()) 
        {
            Record bibRecWithPossChanges = merger.next();
            //results.put(getRecordIdFrom001(bibRecWithPossChanges), bibRecWithPossChanges);
// FIXME:  won't currently work w/o next line, but causes a compile error due to dependency on test code
            results.put(bibRecWithPossChanges.getControlNumber(), bibRecWithPossChanges);
        }
        return results;
    }
        
//    /**
//     * NOTE: this is used for mergeMhldsIntoBibRecordsAsMap, which is used for testing
//     * Assign id of record to be the ckey. Our ckeys are in 001 subfield a. 
//     * Marc4j is unhappy with subfields in a control field so this is a kludge 
//     * work around.
//     */
//    private static String getRecordIdFrom001(Record record)
//    {
//        String id = null;
//        ControlField fld = (ControlField) record.getVariableField("001");
//        if (fld != null && fld.getData() != null) 
//        {
//            String rawVal = fld.getData();
//            // 'u' is for testing
//            if (rawVal.startsWith("a") || rawVal.startsWith("u"))
//                id = rawVal.substring(1);
//        }
//        return id;
//    }
//    

    
    /**
     * this is a Naomi Dushay rewrite method, not called by main()
     * for each bib record in the bib rec file 
     *  look for a corresponding mhld record.  If a match is found, 
     *    1) remove any existing fields in the bib record that duplicate the mhld fields to be merged into the bib record
     *    2) merge the mhld fields into the bib record
     * then write the bib record (whether it had a match or not) to stdout
     * @param bibRecsFileName - the name of the file containing MARC Bibliographic records
     * @param mhldRecsFileName - the name of the file containing MARC MHLD records
     * @param outstream - the OutputStream to write the output to
     * @throws java.io.IOException if the file bibRecsFileName cannot be opened and read
     * note this method returns void, but the bib records will be written to standard out
     */
    public static void mergeMhldRecsIntoBibRecsAsStdOut2(String bibRecsFileName, String mhldRecsFileName, OutputStream outstream)
        throws IOException
    {
        RawRecordReader bibsRawRecRdr = new RawRecordReader(new FileInputStream(new File(bibRecsFileName)));
        
        boolean permissive = true;
        boolean toUtf8 = false;
        MergeSummaryHoldings merger = new MergeSummaryHoldings(bibsRawRecRdr, permissive, toUtf8, "MARC8", 
                                                               mhldRecsFileName, DEFAULT_MHLD_FLDS_TO_MERGE);
        verbose = true;
        veryverbose = true;
//      MarcWriter writer = new MarcSplitStreamWriter(outstream, MarcStreamWriter.ENCODING_BY_CHAR_CODE, 70000, "999");
        MarcWriter writer = new MarcSplitStreamWriter(outstream, "per_record", 70000, "999");       
        while (merger.hasNext()) 
        {
            Record bibRecWithPossChanges = merger.next();
            writer.write(bibRecWithPossChanges);
            outstream.flush();
        }
    }

    
    
    /**
     * called from main()
     * 
     * for each bib record in the "file" that has a corresponding mhld record
     *  1) remove any existing fields in the bib record that duplicate the mhld fields to be merged into the bib record
     *  2) merge the mhld fields into the bib record
     *  3) write the resulting record to stdout (always if allRecords = true;  only if bib record changed if allRecords = false)
     * 
     * @param bibsRawRecRdr - a RawRecordReader instantiated for a file of MARC bibliographic records
     * @param mhldRecsFileName - the name of the file containing MARC MHLD records
     * @param outputAllBibs - write the bib record to stdout even if it wasn't changed
     */
    private static void mergeMhldsIntoBibRecsAsStdOut(RawRecordReader bibsRawRecRdr, String mhldRecsFileName, boolean outputAllBibs)
    {
        MergeSummaryHoldings merger = new MergeSummaryHoldings(bibsRawRecRdr, true, false, "MARC8", 
                                                               mhldRecsFileName, DEFAULT_MHLD_FLDS_TO_MERGE);
        RawRecord rawBibRecCurrent = null;
        RawRecord matchingRawMhldRec = null;
//        MarcWriter writer = new MarcSplitStreamWriter(System.out, MarcStreamWriter.ENCODING_BY_CHAR_CODE, 70000, "999");
        MarcWriter writer = new MarcSplitStreamWriter(System.out, "per_record", 70000, "999");
        while (bibsRawRecRdr.hasNext())
        {
        	rawBibRecCurrent = bibsRawRecRdr.next();
            matchingRawMhldRec = merger.getMatchingMhldRawRec(rawBibRecCurrent.getRecordId());
            try
            {
                if (matchingRawMhldRec != null)
                {
                	// remove any existing fields in the bib record that duplicate mhld fields to be merged into the bib record
                    Record bibRecWithChanges = rawBibRecCurrent.getAsRecord(true, false, "999", "MARC8");
                    Record bibRecWithoutChanges = null;                    
                    boolean removedField = false;
                    List<VariableField> lvf = (List<VariableField>) bibRecWithChanges.getVariableFields(DEFAULT_MHLD_FLDS_TO_MERGE.split("[|]"));
                    for (VariableField vf : lvf)
                    {
                        bibRecWithChanges.removeVariableField(vf);
                        removedField = true;
                    }
                    
                    // we will ensure that there is a difference between the orig record and the rec with removed field(s)
                    if (removedField) 
                    	bibRecWithoutChanges = rawBibRecCurrent.getAsRecord(true, false, "999", "MARC8");

                    Record matchingMhldRec = matchingRawMhldRec.getAsRecord(true, false, DEFAULT_MHLD_FLDS_TO_MERGE, "MARC8");
                    
                    // prepare the merged record
                    bibRecWithChanges = MarcCombiningReader.combineRecords(bibRecWithChanges, matchingMhldRec, DEFAULT_MHLD_FLDS_TO_MERGE, "999");
                    
                    // only keep the merged record if it is different from the original record, or if we are retaining all bibs
                    if (outputAllBibs == true || !removedField || !bibRecWithoutChanges.toString().equals(bibRecWithChanges.toString()))
                    {
                        writer.write(bibRecWithChanges);
                        System.out.flush();
                    }
                }
                else if (outputAllBibs == true)
                {
                    System.out.write(rawBibRecCurrent.getRecordBytes());
                    System.out.flush();
                }
            }
            catch (IOException e) 
            {
                System.err.println("Error writing record " + rawBibRecCurrent.getRecordId());
            }
        }
    }

     /**
     * Given a file of MARC MHLD records and a file of MARC Bibliographic records,
     *  merge selected fields from the MHLD records into matching MARC Bib records.  
     *  Ignores MHLD records with no matching bib record.
     *  Selected fields are defined in class constant mhldFldsToMerge.
     * Note that the MHLD file must have records in StringNaturalCompare ascending order.
     * @param args - command line arguments
     */
    public static void main(String[] args)
    {
    	String mhldRecsFileName = null;
        RawRecordReader bibsRawRecRdr = null;
        boolean outputAllBibs = false;
        
        int argoffset = 0;
        if (args.length == 0)
        {
            System.err.println("Usage: MergeSummaryHoldings [-v] [-a] -s marcMhldFile.mrc  marcBibsFile.mrc");
            System.err.println("   or: cat marcBibsFile.mrc | MergeSummaryHoldings [-v] [-a] -s marcMhldFile.mrc ");
        }
        while (argoffset < args.length && args[argoffset].startsWith("-"))
        {
            if (args[argoffset].equals("-v"))
            {
                verbose = true;
                argoffset++;
            }
            if (args[argoffset].equals("-vv"))
            {
                verbose = true;
                veryverbose = true;
                argoffset++;
            }
            if (args[argoffset].equals("-a"))
            {
            	outputAllBibs = true;
                argoffset++;
            }
            if (args[argoffset].equals("-s"))
            {
                mhldRecsFileName = args[1+argoffset];
                argoffset += 2;
            }
        }

        // last argument should be the name of a file containing marc bib records
        if (args.length > argoffset && (args[argoffset].endsWith(".mrc") || args[argoffset].endsWith(".marc") || args[argoffset].endsWith(".xml")))
        {
            try
            {
                bibsRawRecRdr = new RawRecordReader(new FileInputStream(new File(args[argoffset])));
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
                System.exit(1);
            }
        }
        else // or the marc bib records should be read from std in
        {
            bibsRawRecRdr = new RawRecordReader(System.in);
        }

        System.setProperty("org.marc4j.marc.MarcFactory", "org.solrmarc.marcoverride.NoSortMarcFactoryImpl");
        mergeMhldsIntoBibRecsAsStdOut(bibsRawRecRdr, mhldRecsFileName, outputAllBibs);
        System.exit(0);
    }
    
}
