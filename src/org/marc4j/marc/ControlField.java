// $Id: ControlField.java,v 1.6 2003/03/31 19:55:26 ceyates Exp $
/**
 * Copyright (C) 2002 Bas Peters
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
package org.marc4j.marc;

import java.io.Serializable;

/**
 * <p><code>ControlField</code> defines behaviour for a control
 * field (tag 001-009).  </p>
 *
 * <p>Control fields are variable fields identified by tags beginning
 * with two zero's. They are comprised of data and a field terminator
 * and do not contain indicators or subfield codes. The structure of a
 * control field according to the MARC standard is as follows:</p>
 * <pre>
 * DATA_ELEMENT FIELD_TERMINATOR
 * </pre>
 * <p>This structure is returned by the {@link #marshal()}
 * method.</p>
 *
 * @author <a href="mailto:mail@bpeters.com">Bas Peters</a> 
 * @version $Revision: 1.6 $
 *
 */
public class ControlField extends VariableField implements Serializable {

    /** The MARC data element. */
    private char[] data;

    /**
     * <p>Default constructor.</p>
     */
    public ControlField() {
        super();
    }

    /**
     * <p>Creates a new control field instance and registers the tag
     * and the control field data.</p>
     *
     * @param tag the tag name
     * @param data the control field data
     */
    public ControlField(String tag, char[] data) {
        super(tag);
        setData(data);
    }

    /**
     * <p>Creates a new control field instance and registers the tag
     * and the control field data.</p>
     *
     * @param tag the tag name
     * @param data the control field data
     */
    public ControlField(String tag, String data) {
        super(tag);
        setData(data.toCharArray());
    }

    /**
     * <p>Registers the tag.</p>
     *
     * @param tag the tag name
     * @throws IllegalTagException when the tag is not a valid
     *                                     control field identifier
     */
    public void setTag(String tag) {
        if (Tag.isControlField(tag)) {
            super.setTag(tag);
        } else {
            throw new IllegalTagException(tag,
            "not a control field identifier");
        }
    }

    /**
     * <p>Returns the tag name.</p>
     *
     * @return {@link String} - the tag name
     */
    public String getTag() {
	    return super.getTag();
    }

    /**
     * <p>Registers the control field data.</p>
     *
     * @param data the control field data
     */
    public void setData(char[] data) {
	Verifier.checkDataElement(data);
	this.data = data;
    }

    /**
     * <p>Registers the control field data.</p>
     *
     * @param data the control field data
     */
    public void setData(String data) {
	setData(data.toCharArray());
    }

    /**
     * <p>Returns the control field data.</p>
     *
     * @return <code>char[]</code> - control field as a
     *                               character array
     */
    public char[] getData() {
	    return data;
    }

    /**
     * <p>Returns a <code>String</code> representation for a control
     * field following the structure of a MARC control field.</p>
     *
     * @return <code>String</code> - control field
     */
    public String marshal() {
        return new String(data) + FT;
    }

    /**
     * <p>Returns the length of the serialized form of the control field.</p>
     *
     * @return <code>int</code> - length of control field
     */
    public int getLength() {
        return this.marshal().length();
    }

}
