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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.marc.ControlField;

/**
 * Represents a control field in a MARC record.
 * 
 * @author Bas Peters
 */
public class ControlFieldImpl extends VariableFieldImpl implements ControlField {

    /**
     * A <code>serialVersionUID</code> for the class.
     */
    private static final long serialVersionUID = 583390927730540000L;

    private Long id;

    private String data;

    /**
     * Creates a new <code>ControlField</code>.
     */
    ControlFieldImpl() {}

    /**
     * Creates a new <code>ControlField</code> and sets the tag name.
     * 
     * @param tag The tag for the <code>ControlField</code>
     */
    public ControlFieldImpl(final String tag) {
        super(tag);
    }

    /**
     * Creates a new <code>ControlField</code> and sets the tag name and the
     * data element.
     * 
     * @param tag The tag for the <code>ControlField</code>
     * @param data The data for the <code>ControlField</code>
     */
    public ControlFieldImpl(final String tag, final String data) {
        super(tag);
        this.setData(data);
    }

    /**
     * Sets the {@link ControlField} data.
     * 
     * @param data The data for the <code>ControlField</code>
     */
    @Override
    public void setData(final String data) {
        this.data = data;
    }

    @Override
    /**
     * Returns the {@link ControlField} data.
     * 
     * @return Returns the {@link ControlField} data.
     */
    public String getData() {
        return data;
    }

    /**
     * Returns a string representation of this control field.
     * <p>
     * For example:
     * 
     * <pre>
     *     001 12883376
     * </pre>
     * 
     * @return A string representation of this control field
     */
    @Override
    public String toString() {
        return super.toString() + " " + getData();
    }

    /**
     * Finds a match to a regular expression pattern in the {@link ControlField}'s data.
     * 
     * @param pattern The regular expression pattern to compare against the
     *        <code>ControlField</code>'s data
     */
    @Override
    public boolean find(final String pattern) {
        final Pattern p = Pattern.compile(pattern);
        final Matcher m = p.matcher(getData());
        return m.find();
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }

}
