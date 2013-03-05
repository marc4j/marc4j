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
import java.util.List;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.IllegalAddException;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;

/**
 * Represents a MARC record.
 * 
 * @author Bas Peters
 */
public class RecordImpl implements Record {

	private static final long serialVersionUID = 1L;

	private Long id;

	private Leader leader;

	protected List<ControlField> controlFields;

	protected List<DataField> dataFields;

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
	 * Adds a <code>VariableField</code> being a <code>ControlField</code> or
	 * <code>DataField</code>.
	 * <p/>
	 * If the <code>VariableField</code> is a control number field (001) and the
	 * record already has a control number field, the field is replaced with the
	 * new instance.
	 * 
	 * @param field
	 *            the <code>VariableField</code>
	 * @throws IllegalAddException
	 *             when the parameter is not a <code>VariableField</code>
	 *             instance
	 */
	public void addVariableField(VariableField field) {
		String tag = field.getTag();
		if (field instanceof ControlField) {
			ControlField controlField = (ControlField) field;
			if (Verifier.isControlNumberField(tag)) {
				if (Verifier.hasControlNumberField(controlFields))
					controlFields.set(0, controlField);
				else
					controlFields.add(0, controlField);
			} else {
				controlFields.add(controlField);
			}
		} else {
			dataFields.add((DataField) field);
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

	public VariableField getVariableField(final String tag) {
		final Iterator<? extends VariableField> i;
		if (Verifier.isControlField(tag))
			i = controlFields.iterator();
		else
			i = dataFields.iterator();
		while (i.hasNext()) {
			VariableField field = (VariableField) i.next();
			if (field.getTag().equals(tag))
				return field;
		}
		return null;
	}

	public List<VariableField> getVariableFields(final String tag) {
		final List<VariableField> fields = new ArrayList<VariableField>();
		Iterator<? extends VariableField> i;
		if (Verifier.isControlField(tag))
			i = controlFields.iterator();
		else
			i = dataFields.iterator();
		while (i.hasNext()) {
			VariableField field = (VariableField) i.next();
			if (field.getTag().equals(tag))
				fields.add(field);
		}
		return fields;
	}

	public List<VariableField> getVariableFields() {
		final List<VariableField> fields = new ArrayList<VariableField>();
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

	public List<VariableField> getVariableFields(final String[] tags) {
		final List<VariableField> list = new ArrayList<VariableField>();
		for (final String tag : tags) {
			list.addAll(getVariableFields(tag));
		}
		return list;
	}

	/**
	 * Returns a string representation of this record.
	 * <p/>
	 * <p/>
	 * Example:
	 * <p/>
	 * 
	 * <pre>
	 * <<<<<<< HEAD
	 *     
	 * =======
	 * 
	 * >>>>>>> refs/remotes/choose_remote_name/reader-test-improvements
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
		for (VariableField field : getVariableFields()) {
			sb.append(field.toString());
			sb.append('\n');
		}
		return sb.toString();
	}

	public List<VariableField> find(final String pattern) {
		final List<VariableField> result = new ArrayList<VariableField>();
		for (final VariableField field : getVariableFields()) {
			if (field.find(pattern))
				result.add(field);
		}
		return result;
	}

	public List<VariableField> find(final String tag, final String pattern) {
		final List<VariableField> result = new ArrayList<VariableField>();
		for (final VariableField field : getVariableFields(tag)) {
			if (field.find(pattern))
				result.add(field);
		}
		return result;
	}

	public List<VariableField> find(final String[] tag, final String pattern) {
		final List<VariableField> result = new ArrayList<VariableField>();
		for (final VariableField field : getVariableFields(tag)) {
			if (field.find(pattern))
				result.add(field);
		}
		return result;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

}
