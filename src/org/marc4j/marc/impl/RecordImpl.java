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

import org.marc4j.MarcError;
import org.marc4j.marc.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a MARC record.
 *
 * @author Bas Peters
 */
public class RecordImpl implements Record {

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

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setLeader(Leader leader) {
        this.leader = leader;
    }

    public Leader getLeader() {
        return leader;
    }

    /**
     * Adds a <code>VariableField</code> being a <code>ControlField</code>
     * or <code>DataField</code>.
     * <p/>
     * If the <code>VariableField</code> is a control number field (001) and
     * the record already has a control number field, the field is replaced with
     * the new instance.
     *
     * @param field the <code>VariableField</code>
     * @throws IllegalAddException when the parameter is not a <code>VariableField</code>
     *                             instance
     */
    public void addVariableField(VariableField field) {
        String tag = field.getTag();
        if (field instanceof ControlField) 
        {
            ControlField controlField = (ControlField) field;
            if (Verifier.isLeaderField(tag))
            {
                // invalid operation, do nothing
            }
            else if (Verifier.isControlNumberField(tag)) 
            {
                if (Verifier.hasControlNumberField(controlFields))
                    controlFields.set(0, controlField);
                else
                    controlFields.add(0, controlField);
            }
            else
            {
                controlFields.add(controlField);
            }
        }
        else 
        {
            dataFields.add((DataField)field);
        }

    }

    public void removeVariableField(VariableField field) {
        String tag = field.getTag();
        if (Verifier.isControlField(tag))
            controlFields.remove(field);
        else
            dataFields.remove(field);
    }

    /**
     * Returns the control number field or <code>null</code> if no control
     * number field is available.
     *
     * @return ControlField - the control number field
     */
    public ControlField getControlNumberField() {
        if (Verifier.hasControlNumberField(controlFields))
            return controlFields.get(0);
        else
            return null;
    }

    public List<ControlField> getControlFields() {
        return controlFields;
    }

    public List<DataField> getDataFields() {
        return dataFields;
    }

    public VariableField getVariableField(String tag) 
    {
        List<VariableField> fields = getVariableFieldsWithLeader();
        for (VariableField field : fields)
        {
            if (fieldMatches(field, tag))
                return(field);
        }
        return(null);
    }
 
    private boolean fieldMatches(VariableField field, String tag)
    {
        if (field.getTag().equals(tag)) return true;
        if (tag.startsWith("LNK") && field.getTag().equals("880")) 
        {
            DataField df = (DataField)field;
            Subfield link = df.getSubfield('6');
            if (link != null && link.getData().equals(tag.substring(3)))
            {
                return(true);
            }
        }
        return false;
    }

    public List<VariableField> getVariableFields(String tag) 
    {
        List<VariableField> result = new ArrayList<VariableField>();
        List<VariableField> fields = getVariableFieldsWithLeader();
        for (VariableField field : fields)
        {
            if (fieldMatches(field, tag))
                result.add(field);
        }
        return(result);
    }
    
    public List<VariableField> getVariableFields(String[] tags) 
    {
        List<VariableField> result = new ArrayList<VariableField>();
        List<VariableField> fields = getVariableFieldsWithLeader();
        for (VariableField field : fields)
        {
            for (String tag : tags)
            {
                if (fieldMatches(field, tag))
                {
                    result.add(field);
                    break;
                }
            }  
        }
        return(result);
    }

    public List<VariableField> getVariableFields() 
    {
        List<VariableField> fields = new ArrayList<VariableField>();
        fields.addAll(controlFields);
        fields.addAll(dataFields);
        return fields;
    }
    
    public List<VariableField> getVariableFieldsWithLeader() 
    {
        List<VariableField> fields = new ArrayList<VariableField>();
        ControlField leaderAsField = new ControlFieldImpl("000", this.getLeader().toString());
        fields.add(leaderAsField);
        fields.addAll(controlFields);
        fields.addAll(dataFields);
        return fields;
    }

    public String getControlNumber() {
        ControlField f = getControlNumberField();

        if (f == null || f.getData() == null)
            return null;
        else
            return f.getData();
    }

    /**
     * Returns a string representation of this record.
     * <p/>
     * <p/>
     * Example:
     * <p/>
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
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LEADER ");
        sb.append(getLeader().toString());
        sb.append('\n');
        for (VariableField field : getVariableFields())
        {
            sb.append(field.toString());
            sb.append('\n');
        }
        return sb.toString();
    }

    public List<VariableField> find(String pattern) {
        List<VariableField> result = new ArrayList<VariableField>();
        Iterator<? extends VariableField> i = controlFields.iterator();
        while (i.hasNext()) 
        {
            VariableField field = (VariableField) i.next();
            if (field.find(pattern))
                result.add(field);
        }
        i = dataFields.iterator();
        while (i.hasNext()) 
        {
            VariableField field = (VariableField) i.next();
            if (field.find(pattern))
                result.add(field);
        }
        return result;
    }

    public List<VariableField> find(String tag, String pattern) {
        List<VariableField> result = new ArrayList<VariableField>();
        for (VariableField field : getVariableFields(tag))
        {
            if (field.find(pattern))
                result.add(field);
        }
        return result;
    }

    public List<VariableField> find(String[] tag, String pattern) {
        List<VariableField> result = new ArrayList<VariableField>();
        for (VariableField field : getVariableFields(tag)) {
            if (field.find(pattern))
                result.add(field);
        }
        return result;
    }
    
    public boolean hasMatch(String[] tag, String pattern)
    {
        List<VariableField> result = new ArrayList<VariableField>();
        for (VariableField field : getVariableFields(tag))
        {
            if (field.find(pattern))
                return(true);
        }
        return false;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
    /**
     *  Logs an error message using the stated severity level.  Uses the values passed  
     *  in id, field, and subfield to note the location of the error.
     * 
     * @param id - the record ID of the record currently being processed
     * @param field - the tag of the field currently being processed
     * @param subfield - the subfield tag of the subfield currently being processed
     * @param severity - An indication of the relative severity of the error that was 
     *                      encountered.
     * @param message - A descriptive message about the error that was encountered.
     */
    @Override
    public void addError(String field, String subfield, int severity, String message)
    {
        if (errors == null) 
        {
            errors = new LinkedList<MarcError>();
        }
        errors.add(new MarcError(field, subfield, severity, message));
        if (severity > maxSeverity)   maxSeverity = severity; 
    }
    
    /**
     *  Copies a List of errors into the current error handler
     * 
     * @param newErrors - A list of Errors.
     */
    @Override
    public void addErrors(List<MarcError> newErrors)
    {
        if (newErrors == null || newErrors.size() == 0) return;
        if (errors == null) 
        {
            errors = new LinkedList<MarcError>();
        }
        for (MarcError err : newErrors)
        {
            errors.add(err);
            if (err.severity > maxSeverity)   maxSeverity = err.severity;   
        }
    }

    @Override
    public boolean hasErrors()
    {
        return (errors != null && errors.size() > 0);
    }

    @Override
    public List<MarcError> getErrors()
    {
        return errors;
    }


}
