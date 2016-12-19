/**
 * Copyright (C) 2004 Bas Peters
 *
 * This file is part of MARC4J
 *
 * MARC4J is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * MARC4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with MARC4J; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.marc4j.marc.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.marc4j.MarcError;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.IllegalAddException;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

/**
 * Represents a MARC record.
 *
 * @author Bas Peters
 */
public class RecordImpl implements Record {

    /**
     * A <code>serialVersionUID</code> for the class.
     */
    private static final long serialVersionUID = -4751021372496524250L;

    private Long id;

    private Leader leader;

    protected List<ControlField> controlFields;

    protected List<DataField> dataFields;

    protected List<MarcError> errors = null;

    protected int maxSeverity;

    private String type;

    /**
     * Creates a new <code>Record</code>.
     */
    public RecordImpl() {
        controlFields = new ArrayList<ControlField>();
        dataFields = new ArrayList<DataField>();
    }

    /**
     * Sets the type of this {@link Record}.
     *
     * @param type A {@link Record} type
     */
    @Override
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Gets the type of this {@link Record}.
     *
     * @return This {@link Record}'s type
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     * Sets this {@link Record}'s {@link Leader}.
     *
     * @param leader A {@link Leader} to use in this record
     */
    @Override
    public void setLeader(final Leader leader) {
        this.leader = leader;
    }

    /**
     * Gets the {@link Leader} for this {@link Record}.
     *
     * @return The {@link Leader} for this {@link Record}
     */
    @Override
    public Leader getLeader() {
        return leader;
    }

    /**
     * Adds a <code>VariableField</code> being a <code>ControlField</code> or <code>DataField</code>.
     *
     * If the <code>VariableField</code> is a control number field (001) and the record already has a control number
     * field, the field is replaced with the new instance.
     *
     * @param field the <code>VariableField</code>
     * @throws IllegalAddException when the parameter is not a <code>VariableField</code>
     *                             instance
     */
    @Override
    public void addVariableField(final VariableField field) {
        final String tag = field.getTag();
        if (field instanceof ControlField) {
            final ControlField controlField = (ControlField) field;

            if (Verifier.isLeaderField(tag)) {
                // invalid operation, do nothing
            } else if (Verifier.isControlNumberField(tag)) {
                if (Verifier.hasControlNumberField(controlFields)) {
                    controlFields.set(0, controlField);
                } else {
                    controlFields.add(0, controlField);
                }
            } else {
                controlFields.add(controlField);
            }
        } else {
            dataFields.add((DataField) field);
        }
    }

    @Override
    public void removeVariableField(final VariableField field) {
        final String tag = field.getTag();
        if (Verifier.isControlField(tag)) {
            controlFields.remove(field);
        } else {
            dataFields.remove(field);
        }
    }

    /**
     * Returns the control number field or <code>null</code> if no control number field is available.
     *
     * @return ControlField - the control number field
     */
    @Override
    public ControlField getControlNumberField() {
        if (Verifier.hasControlNumberField(controlFields)) {
            return controlFields.get(0);
        } else {
            return null;
        }
    }

    /**
     * Gets a {@link List} of {@link ControlField}s from the {@link Record}.
     */
    @Override
    public List<ControlField> getControlFields() {
        return controlFields;
    }

    /**
     * Gets a {@link List} of {@link DataField}s from the {@link Record}.
     */
    @Override
    public List<DataField> getDataFields() {
        return dataFields;
    }

    /**
     * Gets the first {@link VariableField} with the supplied tag.
     *
     * @param tag The tag of the field to be returned
     */
    @Override
    public VariableField getVariableField(final String tag) {
        final List<VariableField> fields = getVariableFieldsWithLeader();

        for (final VariableField field : fields) {
            if (fieldMatches(field, tag)) {
                return field;
            }
        }

        return null;
    }

    private boolean fieldMatches(final VariableField field, final String tag) {
        if (field.getTag().equals(tag)) {
            return true;
        }
        if (tag.startsWith("LNK") && field.getTag().equals("880")) {
            final DataField df = (DataField) field;
            final Subfield link = df.getSubfield('6');
            if (link != null && link.getData().equals(tag.substring(3))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a {@link List} of {@link VariableField}s with the supplied tag.
     */
    @Override
    public List<VariableField> getVariableFields(final String tag) {
        final List<VariableField> result = new ArrayList<VariableField>();
        final List<VariableField> fields = getVariableFieldsWithLeader();

        for (final VariableField field : fields) {
            if (fieldMatches(field, tag)) {
                result.add(field);
            }
        }
        return result;
    }

    /**
     * Gets a {@link List} of {@link VariableField}s from the {@link Record}.
     */
    @Override
    public List<VariableField> getVariableFields() {
        final List<VariableField> fields = new ArrayList<VariableField>();

        fields.addAll(controlFields);
        fields.addAll(dataFields);

        return fields;
    }

    /**
     * Gets a {@link List} of {@link VariableField}s from the {@link Record}
     * including the LEADER recast as ControlField for field matching purposes.
     * 
     * @return a List of all VariableFields plus the Leader represented as a ControlField
     */
    public List<VariableField> getVariableFieldsWithLeader() {
        final List<VariableField> fields = new ArrayList<VariableField>();
        final ControlField leaderAsField = new ControlFieldImpl("000", this.getLeader().toString());
        fields.add(leaderAsField);
        fields.addAll(controlFields);
        fields.addAll(dataFields);
        return fields;
    }

    /**
     * Gets the {@link Record}'s control number.
     */
    @Override
    public String getControlNumber() {
        final ControlField f = getControlNumberField();

        if (f == null || f.getData() == null) {
            return null;
        } else {
            return f.getData();
        }
    }

    /**
     * Gets the {@link VariableField}s in the {@link Record} with the supplied tags.
     */
    @Override
    public List<VariableField> getVariableFields(final String[] tags) {
        final List<VariableField> result = new ArrayList<VariableField>();
        final List<VariableField> fields = getVariableFieldsWithLeader();

        for (final VariableField field : fields) {
            for (final String tag : tags) {
                if (fieldMatches(field, tag)) {
                    result.add(field);
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Returns a string representation of this record.
     *
     * Example:
     * <pre>
     *
     *      LEADER 00714cam a2200205 a 4500
     *      001 12883376
     *      005 20030616111422.0
     *      008 020805s2002 nyu j 000 1 eng
     *      020   $a0786808772
     *      020   $a0786816155 (pbk.)
     *      040   $aDLC$cDLC$dDLC
     *      100 1 $aChabon, Michael.
     *      245 10$aSummerland /$cMichael Chabon.
     *      250   $a1st ed.
     *      260   $aNew York :$bMiramax Books/Hyperion Books for Children,$cc2002.
     *      300   $a500 p. ;$c22 cm.
     *      650  1$aFantasy.
     *      650  1$aBaseball$vFiction.
     *      650  1$aMagic$vFiction.
     *
     * </pre>
     *
     * @return String - a string representation of this record
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("LEADER ");
        sb.append(getLeader().toString());
        sb.append('\n');

        for (final VariableField field : getVariableFields()) {
            sb.append(field.toString());
            sb.append('\n');
        }

        return sb.toString();
    }

    /**
     * Finds all the {@link VariableField}s that match the supplied regular expression pattern.
     */
    @Override
    public List<VariableField> find(final String pattern) {
        final List<VariableField> result = new ArrayList<VariableField>();
        Iterator<? extends VariableField> i = controlFields.iterator();

        while (i.hasNext()) {
            final VariableField field = i.next();

            if (field.find(pattern)) {
                result.add(field);
            }
        }

        i = dataFields.iterator();

        while (i.hasNext()) {
            final VariableField field = i.next();

            if (field.find(pattern)) {
                result.add(field);
            }
        }

        return result;
    }

    /**
     * Finds all the {@link VariableField}s that match the supplied tag and regular expression pattern.
     */
    @Override
    public List<VariableField> find(final String tag, final String pattern) {
        final List<VariableField> result = new ArrayList<VariableField>();

        for (final VariableField field : getVariableFields(tag)) {
            if (field.find(pattern)) {
                result.add(field);
            }
        }

        return result;
    }

    /**
     * Finds all the {@link VariableField}s that match the supplied tags and regular expression pattern.
     */
    @Override
    public List<VariableField> find(final String[] tag, final String pattern) {
        final List<VariableField> result = new ArrayList<VariableField>();

        for (final VariableField field : getVariableFields(tag)) {
            if (field.find(pattern)) {
                result.add(field);
            }
        }

        return result;
    }

    public boolean hasMatch(final String[] tag, final String pattern) {
        for (final VariableField field : getVariableFields(tag)) {
            if (field.find(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the ID for this {@link Record}.
     *
     * @param id The ID for this {@link Record}
     */
    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    /**
     * Returns the ID for this {@link Record}.
     *
     * @return An ID for this {@link Record}
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     *  Logs an error message using the stated severity level.  Uses the values passed  
     *  in id, field, and subfield to note the location of the error.
     * 
     * @param field - the tag of the field currently being processed
     * @param subfield - the subfield tag of the subfield currently being processed
     * @param severity - An indication of the relative severity of the error that was 
     *                      encountered.
     * @param message - A descriptive message about the error that was encountered.
     */
    @Override
    public void addError(final String field, final String subfield, final int severity,
            final String message) {
        if (errors == null) {
            errors = new LinkedList<MarcError>();
        }
        errors.add(new MarcError(field, subfield, severity, message));
        if (severity > maxSeverity) {
            maxSeverity = severity;
        }
    }

    /**
     *  Copies a List of errors into the current error handler
     * 
     * @param newErrors - A list of Errors.
     */
    @Override
    public void addErrors(final List<MarcError> newErrors) {
        if (newErrors == null || newErrors.size() == 0) {
            return;
        }
        if (errors == null) {
            errors = new LinkedList<MarcError>();
        }
        for (final MarcError err : newErrors) {
            errors.add(err);
            if (err.severity > maxSeverity) {
                maxSeverity = err.severity;
            }
        }
    }

    @Override
    public boolean hasErrors() {
        return errors != null && errors.size() > 0;
    }

    @Override
    public List<MarcError> getErrors() {
        return errors;
    }

}
