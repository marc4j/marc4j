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

import org.marc4j.marc.VariableField;

/**
 * Represents a variable field in a MARC record.
 * 
 * @author Bas Peters
 */
public abstract class VariableFieldImpl implements VariableField {

    private static final long serialVersionUID = 1L;
    private String tag;

    /**
     * Creates a new <code>VariableField</code>.
     */
    public VariableFieldImpl() {
    }

    /**
     * Creates a new <code>VariableField</code> and sets the tag name.
     */
    public VariableFieldImpl(String tag) {
        this.setTag(tag);
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
    
    public int compareTo(VariableField obj) {
        if (!(obj instanceof VariableFieldImpl))
            throw new ClassCastException("A VariableField object expected");

        VariableField field = (VariableField) obj;
        return tag.compareTo(field.getTag());
    }

    /**
     * Returns a string representation of this variable field.
     * 
     * @return String - a string representation of this variable field
     */
    public String toString() {
        return tag;
    }

}
