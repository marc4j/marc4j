
package org.marc4j;

import java.util.ArrayList;
import java.util.List;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

/**
 * Binary MARC records have a maximum size of 99999 bytes. In the data dumps
 * from the Sirsi/Dynix Virgo system if a record with all of its holdings
 * information attached would be greater that that size, the records is written
 * out multiple times with each subsequent record containing a subset of the
 * total holdings information. This class reads ahead to determine when the next
 * record in a MaARC file is actually a continuation of the same record. When
 * this occurs, the holdings information in the next record is appended
 * to/merged with the in-memory MARC record representation already read.
 * 
 * @author rh9ec
 */

public class MarcCombiningReader implements MarcReader {

    Record currentRecord = null;

    Record nextRecord = null;

    MarcReader reader;

    String idsToMerge = null;

    String leftControlField = null;

    String rightControlField = null;

    /**
     * Constructor for a "combining" Marc reader, that looks ahead at the Marc
     * file to determine when the next record is a continuation of the currently
     * read record.
     * 
     * @param reader - The Lower level MarcReader that returns Marc4J Record
     *        objects that are read from a Marc file.
     * @param idsToMerge - string representing a regular expression matching
     *        those fields to be merged for continuation records.
     * @param leftControlField - string representing a control field in the
     *        current record to use for matching purposes (null to default to
     *        001).
     * @param rightControlField - string representing a control field in the
     *        next record to use for matching purposes (null to default to 001).
     */
    public MarcCombiningReader(final MarcReader reader, final String idsToMerge,
            final String leftControlField, final String rightControlField) {
        this.reader = reader;
        this.idsToMerge = idsToMerge;
        this.leftControlField = leftControlField;
        this.rightControlField = rightControlField;
        // this.nextErrors = null;
        // this.currentErrors = null;
    }

    /**
     * Constructor for a "combining" Marc reader, that looks ahead at the Marc file to determine 
     * when the next record is a continuation of the currently read record.  Because this reader 
     * needs to have two records in memory to determine when the subsequent record is a continuation,
     * if Error Handling is being performed, this constructor needs to be used, so that the errors 
     * from the "next" record are not appended to the results for the "current" record.
     * Call this constructor in the following way:
     * <pre>
     *          ErrorHandler errors2 = errors;
     *          errors = new ErrorHandler();
     *          reader = new MarcCombiningReader(reader, errors, errors2, combineConsecutiveRecordsFields);
     * </pre>         
     * @param reader - The Lower level MarcReader that returns Marc4J Record
     *        objects that are read from a Marc file.
     * @param currentErrors - ErrorHandler Object to use for attaching errors to
     *        a record.
     * @param nextErrors - ErrorHandler Object that was passed into the lower
     *        level MarcReader
     * @param idsToMerge - string representing a regular expression matching
     *        those fields to be merged for continuation records.
     * @param leftControlField - string representing a control field in the
     *        current record to use for matching purposes (null to default to
     *        001).
     * @param rightControlField - string representing a control field in the
     *        next record to use for matching purposes (null to default to 001).
     */
    @Deprecated
    public MarcCombiningReader(final MarcReader reader, final ErrorHandler currentErrors,
            final ErrorHandler nextErrors, final String idsToMerge, final String leftControlField,
            final String rightControlField) {
        this.reader = reader;
        this.idsToMerge = idsToMerge;
        this.leftControlField = leftControlField;
        this.rightControlField = rightControlField;
        // this.nextErrors = nextErrors;
        // this.currentErrors = currentErrors;
    }

    /**
     * Returns <code>true</code> if there is a next record; else
     * <code>false</code>.
     */
    @Override
    public boolean hasNext() {
        if (currentRecord == null) {
            currentRecord = next();
        }
        return currentRecord != null;
    }

    /**
     * Returns the next {@link Record} record if there is one and
     * <code>null</code> if there isn't.
     */
    @Override
    public Record next() {
        if (currentRecord != null) {
            final Record tmp = currentRecord;
            currentRecord = null;
            return tmp;
        } else if (currentRecord == null) {
            if (nextRecord != null) {
                currentRecord = nextRecord;
                nextRecord = null;
            }
            if (!reader.hasNext()) {
                return currentRecord != null ? next() : null;
            }

            try {
                nextRecord = reader.next();
            } catch (final Exception e) {
                if (currentRecord != null) {
                    final String recCntlNum = currentRecord.getControlNumber();

                    throw new MarcException(
                            "Couldn't get next record after " + (recCntlNum != null ? recCntlNum
                                    : "") + " -- " + e.toString());
                } else {
                    throw new MarcException("Marc record couldn't be read -- " + e.toString());
                }
            }

            while (recordsMatch(currentRecord, nextRecord)) {
                currentRecord = combineRecords(currentRecord, nextRecord, idsToMerge);
                if (reader.hasNext()) {
                    try {
                        nextRecord = reader.next();
                    } catch (final Exception e) {
                        final String recCntlNum = currentRecord.getControlNumber();

                        throw new MarcException(
                                "Couldn't get next record after " + (recCntlNum != null ? recCntlNum
                                        : "") + " -- " + e.toString());
                    }
                } else {
                    nextRecord = null;
                }
            }
            return next();
        }

        return null;
    }

    /**
     * Support method to find a specific control field within a record and
     * return its contents as a string.
     * 
     * @param record - record to search
     * @param tag - tag number to search for
     */
    private String findControlField(final Record record, final String tag) {
        final String tagstart = tag.substring(0, 3);
        final List<VariableField> fields = record.getVariableFields(tagstart);

        for (final VariableField field : fields) {
            if (field instanceof ControlField) {
                final ControlField cf = (ControlField) field;

                if (cf.getTag().matches(tagstart)) {
                    return cf.getData();
                }
            } else if (field instanceof DataField) {
                final DataField df = (DataField) field;

                if (df.getTag().matches(tagstart)) {
                    char subfieldtag = 'a';

                    if (tag.length() > 3) {
                        subfieldtag = tag.charAt(4);
                    }

                    final Subfield sf = df.getSubfield(subfieldtag);
                    if (sf != null) {
                        return sf.getData();
                    }
                }
            }
        }

        return null;
    }

    /**
     * Support method to detect if two records match.
     * 
     * @param left - left side of the comparison (current record)
     * @param right - right side of the comparison (next record)
     */
    private boolean recordsMatch(final Record left, final Record right) {
        // Records can't match if they don't exist!
        if (left == null || right == null) {
            return false;
        }

        // Initialize match strings extracted from records:
        String leftStr = null;
        String rightStr = null;

        // For both sides of the match (left and right), check to see if the
        // user provided a control field setting. If no preference was provided,
        // we'll match using the record ID. If a preference exists, we need to
        // look up the specified control field in the record.
        if (leftControlField == null) {
            leftStr = left.getControlNumber();
        } else {
            leftStr = findControlField(left, leftControlField);
        }

        if (rightControlField == null) {
            rightStr = right.getControlNumber();
        } else {
            rightStr = findControlField(right, rightControlField);
        }

        // Check for a match and return an appropriate status:
        if (leftStr != null && rightStr != null && leftStr.equals(rightStr)) {
            return true;
        }

        return false;
    }

    /**
     * Combines fields (identified by the <code>idsToMerge</code>) from the
     * second {@link Record} into the first.
     * 
     * @param currentRecord - the first record
     * @param nextRecord - the second record
     * @param idsToMerge - a regex pattern of field tags that should be copied from the second record to the first one
     * @return the first record merged with the second record
     */
    static public Record combineRecords(final Record currentRecord, final Record nextRecord,
            final String idsToMerge) {
        final List<VariableField> fields = nextRecord.getVariableFields();

        for (final VariableField field : fields) {
            if (field.getTag().matches(idsToMerge)) {
                currentRecord.addVariableField(field);
            }
        }
        if (nextRecord.hasErrors()) {
            currentRecord.addErrors(nextRecord.getErrors());
        }

        return currentRecord;
    }

    /**
     * Combines fields (identified by the <code>idsToMerge</code> from the
     * second {@link Record} into the first (before the supplied field).
     * 
     * @param currentRecord - the first record
     * @param nextRecord - the second record
     * @param idsToMerge - a regex pattern of field tags that should be copied from the second record to the first one
     * @param fieldInsertBefore - the field tags in the first record before which the copied field should be placed 
     * @return the first record merged with the second record
     */
    static public Record combineRecords(final Record currentRecord, final Record nextRecord,
            final String idsToMerge, final String fieldInsertBefore) {
        final List<VariableField> existingFields = currentRecord.getVariableFields();
        final List<VariableField> fieldsToMove = new ArrayList<VariableField>();

        // temporarily remove some existing fields
        for (final VariableField field : existingFields) {
            if (field.getTag().matches(fieldInsertBefore)) {
                fieldsToMove.add(field);
                currentRecord.removeVariableField(field);
            }
        }

        final List<VariableField> fields = nextRecord.getVariableFields();

        for (final VariableField field : fields) {
            if (field.getTag().matches(idsToMerge)) {
                currentRecord.addVariableField(field);
            }
        }

        // now add back the temporarily removed fields
        for (final VariableField field : fieldsToMove) {
            currentRecord.addVariableField(field);
        }

        if (nextRecord.hasErrors()) {
            currentRecord.addErrors(nextRecord.getErrors());
        }

        return currentRecord;
    }

}
