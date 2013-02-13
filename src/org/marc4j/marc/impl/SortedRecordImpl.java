package org.marc4j.marc.impl;

import java.util.Collections;

import org.marc4j.marc.IllegalAddException;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.RecordImpl;
import org.marc4j.marc.impl.Verifier;

/**
 * 
 * @author Robert Haschart
 *
 */
public class SortedRecordImpl extends RecordImpl
{
    /**
     * 
     */
    private static final long serialVersionUID = -5870251915056214892L;


	public SortedRecordImpl()
    {
        super();
    }
    
    public void addVariableField(VariableField field) {
        if (!(field instanceof VariableField))
            throw new IllegalAddException("Expected VariableField instance");

        String tag = field.getTag();
        if (Verifier.isControlNumberField(tag)) {
            if (Verifier.hasControlNumberField(getControlFields()))
                getControlFields().set(0, field);
            else
                getControlFields().add(0, field);
            Collections.sort(controlFields);
        } else if (Verifier.isControlField(tag)) {
            getControlFields().add(field);
            Collections.sort(controlFields);
        } else {
            getDataFields().add(field);
            Collections.sort(dataFields);
        }

    }


}
