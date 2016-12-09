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

import org.marc4j.MarcException;
import org.marc4j.marc.VariableField;

/**
 * Represents a variable field in a MARC record.
 * 
 * @author Bas Peters
 */
public abstract class VariableFieldImpl implements VariableField {

    /**
     * A <code>serialVersionUID</code> for the class.
     */
    private static final long serialVersionUID = -8396090810780390995L;

    private String tag;

    /**
     * Creates a new <code>VariableField</code>.
     */
    public VariableFieldImpl() {}

    /**
     * Creates a new <code>VariableField</code> and sets the tag name.
     *
     * @param tag The tag for the <code>VariableField</code>
     */
    public VariableFieldImpl(final String tag) throws MarcException {
        if (tag == null) {
            throw new MarcException("Attempt to create field with null tag");
        }
        this.setTag(tag);
    }

    /**
     * Sets this field's tag.
     * 
     * @param tag This field's tag
     */
    @Override
    public void setTag(final String tag) {
        this.tag = tag;
    }

    /**
     * Returns this field's tag.
     * 
     * @return This field's tag
     */
    @Override
    public String getTag() {
        return tag;
    }

    /**
     * Compare's this {@link VariableField} to the supplied one.
     * 
     * @param obj A {@link VariableField} to compare to this one
     * @return 0 for a match, -1 if this one sorts first, or 1 if it sorts last
     */
    @Override
    public int compareTo(final VariableField obj) {
        if (!(obj instanceof VariableFieldImpl)) {
            throw new ClassCastException("A VariableField object expected");
        }

        return tag.compareTo(obj.getTag());
    }

    /**
     * Returns a string representation of this variable field.
     * 
     * @return A string representation of this variable field
     */
    @Override
    public String toString() {
        return tag;
    }

}
