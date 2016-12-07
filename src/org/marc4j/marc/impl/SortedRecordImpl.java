
package org.marc4j.marc.impl;

import java.util.Collections;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.VariableField;

/**
 * 
 * @author Robert Haschart
 */
public class SortedRecordImpl extends RecordImpl {

    /**
     * The class' <code>serialVersionUID</code>.
     */
    private static final long serialVersionUID = 21647558104914722L;

    public SortedRecordImpl() {
        super();
    }

    /**
     * Adds a {@link VariableField} to the record.
     */
    @Override
    public void addVariableField(final VariableField field) {
        if (field instanceof ControlField) {
            final ControlField controlField = (ControlField) field;
            final String tag = controlField.getTag();
            
            if (Verifier.isControlNumberField(tag)) {
                if (Verifier.hasControlNumberField(getControlFields())) {
                    getControlFields().set(0, controlField);
                } else {
                    getControlFields().add(0, controlField);
                }

                Collections.sort(controlFields);
            } else if (Verifier.isControlField(tag)) {
                getControlFields().add(controlField);
                Collections.sort(controlFields);
            }
        } else {
            getDataFields().add((DataField) field);
            Collections.sort(dataFields);
        }
    }
}
