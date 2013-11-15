package org.marc4j;

import org.marc4j.ErrorHandler;
import org.marc4j.MarcException;
import org.marc4j.MarcReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import java.util.ArrayList;
import java.util.List;


/**
 * @author rh9ec
 * 
 * Binary Marc records have a maximum size of 99999 bytes.  In the data dumps from 
 * the Sirsi/Dynix Virgo system if a record with all of its holdings information 
 * attached would be greater that that size, the records is written out multiple
 * times with each subsequent record containing a subset of the total holdings information.
 * This class reads ahead to determine when the next record in a Marc file is actually 
 * a continuation of the same record.   When this occurs, the holdings information in the
 * next record is appended to/merged with the in-memory Marc record representation already 
 * read. 
 *
 */

public class MarcCombiningReader implements MarcReader
{
    Record currentRecord = null;
    Record nextRecord = null;
    MarcReader reader;
    String idsToMerge = null;
    String leftControlField = null;
    String rightControlField = null;
    ErrorHandler nextErrors;
    ErrorHandler currentErrors;

    
    /**
     * Constructor for a "combining" Marc reader, that looks ahead at the Marc file to determine 
     * when the next record is a continuation of the currently read record.  
     * 
     * @param reader - The Lower level MarcReader that returns Marc4J Record objects that are read from a Marc file.
     * @param idsToMerge - string representing a regular expression matching those fields to be merged for continuation records.
     * @param leftControlField - string representing a control field in the current record to use for matching purposes (null to default to 001).
     * @param rightControlField - string representing a control field in the next record to use for matching purposes (null to default to 001).
     */
    public MarcCombiningReader(MarcReader reader, String idsToMerge, String leftControlField, String rightControlField)
    {
        this.reader = reader;
        this.idsToMerge = idsToMerge;
        this.leftControlField = leftControlField;
        this.rightControlField = rightControlField;
        this.nextErrors = null;
        this.currentErrors = null;
    }
    
    /**
     * Constructor for a "combining" Marc reader, that looks ahead at the Marc file to determine 
     * when the next record is a continuation of the currently read record.  Because this reader 
     * needs to have two records in memory to determine when the subsequent record is a continuation,
     * if Error Handling is being performed, this constructor needs to be used, so that the errors 
     * from the "next" record are not appended to the results for the "current" record.
     * Call this constructor in the following way:
     *          ErrorHandler errors2 = errors;
     *          errors = new ErrorHandler();
     *          reader = new MarcCombiningReader(reader, errors, errors2, combineConsecutiveRecordsFields);
     *          
     * @param reader - The Lower level MarcReader that returns Marc4J Record objects that are read from a Marc file.
     * @param currentErrors - ErrorHandler Object to use for attaching errors to a record.
     * @param nextErrors - ErrorHandler Object that was passed into the lower level MarcReader
     * @param idsToMerge - string representing a regular expression matching those fields to be merged for continuation records.
     * @param leftControlField - string representing a control field in the current record to use for matching purposes (null to default to 001).
     * @param rightControlField - string representing a control field in the next record to use for matching purposes (null to default to 001).
     */
    public MarcCombiningReader(MarcReader reader, ErrorHandler currentErrors, ErrorHandler nextErrors, String idsToMerge,
        String leftControlField, String rightControlField)
    {
        this.reader = reader;
        this.idsToMerge = idsToMerge;
        this.leftControlField = leftControlField;
        this.rightControlField = rightControlField;
        this.nextErrors = nextErrors;
        this.currentErrors = currentErrors;
    }
   
    public boolean hasNext()
    {
        if (currentRecord == null) 
        { 
            currentRecord = next(); 
        }
        return(currentRecord != null);
    }

    public Record next()
    {
        if (currentRecord != null) 
        { 
            Record tmp = currentRecord; 
            currentRecord = null; 
            return(tmp);
        }
        
        else if (currentRecord == null)
        {
            if (nextRecord != null) 
            { 
                currentRecord = nextRecord;
                copyErrors(currentErrors, nextErrors);
                nextRecord = null; 
            }
            if (!reader.hasNext()) 
            {
                return ((currentRecord != null) ? next() : null);
            }
                       
            try {
                nextRecord = reader.next();
            }
			catch (Exception e)
			{
				if (currentRecord != null) 
				{
					String recCntlNum = currentRecord.getControlNumber();
                    throw new MarcException("Couldn't get next record after " + (recCntlNum != null ? recCntlNum : "") + " -- " + e.toString());
				}
				else
                    throw new MarcException("Marc record couldn't be read -- " + e.toString());
			}


            while (recordsMatch(currentRecord, nextRecord))
            {
                currentRecord = combineRecords(currentRecord, nextRecord, idsToMerge);
                mergeErrors(currentErrors, nextErrors);
                if (reader.hasNext())
                {
                    try {
                        nextRecord = reader.next();
                    }
                    catch (Exception e)
                    {
						String recCntlNum = currentRecord.getControlNumber();
	                    throw new MarcException("Couldn't get next record after " + (recCntlNum != null ? recCntlNum : "") + " -- " + e.toString());
                    }
                }
                else 
                {
                    nextRecord = null;
                }
            }
            return(next());
        }
        return(null);
    }

    /**
     * Support method to find a specific control field within a record and return
     * its contents as a string.
     * @param record - record to search
     * @param tag - tag number to search for
     */
    private String findControlField(Record record, String tag)
    {
        String tagstart = tag.substring(0,3);
        List<VariableField> fields = record.getVariableFields(tagstart);
        for (VariableField field : fields)
        {
            if (field instanceof ControlField)
            {
                ControlField cf = (ControlField) field;
                if (cf.getTag().matches(tagstart))
                {
                    return((String)cf.getData());
                }
            }
            else if (field instanceof DataField)
            {
                DataField df = (DataField)field;
                if (df.getTag().matches(tagstart))
                {
                    char subfieldtag = 'a';
                    if (tag.length() > 3) subfieldtag = tag.charAt(4);
                    Subfield sf = df.getSubfield(subfieldtag);
                    if (sf != null) return(sf.getData());
                }
            }
        }
        return(null);
    }

    /**
     * Support method to detect if two records match.
     * @param left - left side of the comparison (current record)
     * @param right - right side of the comparison (next record)
     */
    private boolean recordsMatch(Record left, Record right)
    {
        // Records can't match if they don't exist!
        if (left == null || right == null) {
            return false;
        }

        // Initialize match strings extracted from records:
        String leftStr = null;
        String rightStr = null;

        // For both sides of the match (left and right), check to see if the user
        // provided a control field setting.  If no preference was provided, we'll
        // match using the record ID.  If a preference exists, we need to look up
        // the specified control field in the record.
        if (leftControlField == null) 
        {
            leftStr = left.getControlNumber();
        } 
        else 
        {
            leftStr = findControlField(left, leftControlField);
        }
        if (rightControlField == null) 
        {
            rightStr = right.getControlNumber();
        } 
        else 
        {
            rightStr = findControlField(right, rightControlField);
        }

        // Check for a match and return an appropriate status:
        if (leftStr != null && rightStr != null && leftStr.equals(rightStr)) 
        {
            return true;
        }
        return false;
    }

    
    private void copyErrors(ErrorHandler currentErr, ErrorHandler nextErr)
    {
        if (currentErr != null && nextErr != null)
        {
            currentErr.reset();
            mergeErrors(currentErr, nextErr);
        }
    }

    private void mergeErrors(ErrorHandler currentErr, ErrorHandler nextErr)
    {
        if (currentErr != null && nextErr != null)
        {
            currentErr.addErrors(nextErr.getErrors());
        }
    }

    static public Record combineRecords(Record currentRecord, Record nextRecord, String idsToMerge)
    {
        List<VariableField> fields = nextRecord.getVariableFields();
        for (VariableField field : fields)
        {
            if (field.getTag().matches(idsToMerge))
            {
                currentRecord.addVariableField(field);
            }
        }
        return(currentRecord);
    }
    
    static public Record combineRecords(Record currentRecord, Record nextRecord, String idsToMerge, String fieldInsertBefore)
    {
        List<VariableField> existingFields = currentRecord.getVariableFields();
        List<VariableField> fieldsToMove = new ArrayList<VariableField>();
        // temporarily remove some existing fields
        for (VariableField field : existingFields)
        {
            if (field.getTag().matches(fieldInsertBefore))
            {
                fieldsToMove.add(field);
                currentRecord.removeVariableField(field);
            }
        }

        List<VariableField> fields = nextRecord.getVariableFields();
        for (VariableField field : fields)
        {
            if (field.getTag().matches(idsToMerge))
            {
                currentRecord.addVariableField(field);
            }
        }
        
        // now add back the temporarily removed fields
        for (VariableField field : fieldsToMove)
        {
            currentRecord.addVariableField(field);
        }
        return(currentRecord);
    }

}
