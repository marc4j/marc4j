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

import org.marc4j.marc.Subfield;

/**
 * Represents a subfield in a MARC record.
 * 
 * @author Bas Peters
 */
public class SubfieldImpl implements Subfield {

    private Long id;
    
    private char code;

    private String data;

    /**
     * Creates a new <code>Subfield</code>.
     */
    public SubfieldImpl() {
    }

    /**
     * Creates a new <code>Subfield</code> and sets the data element
     * identifier.
     * 
     * @param code
     *            the data element identifier
     */
    public SubfieldImpl(char code) {
        this.setCode(code);
    }

    /**
     * Creates a new <code>Subfield</code> and sets the data element
     * identifier and the data element.
     * 
     * @param code
     *            the data element identifier
     * @param data
     *            the data element
     */
    public SubfieldImpl(char code, String data) {
        this.setCode(code);
        this.setData(data);
    }

    public void setCode(char code) {
        this.code = code;
    }

    public char getCode() {
        return code;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public boolean find(String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(getData());
        return m.find();
    }

    /**
     * Returns a string representation of this subfield.
     * 
     * <p>
     * Example:
     * 
     * <pre>
     * $aSummerland /
     * </pre>
     * 
     * @return String - a string representation of this subfield
     */
    public String toString() {
        return "$" + getCode() + getData();
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

}
