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

package org.marc4j.marc;

/**
 * ControlField defines behavior for a control field (tag 001-009).
 * <p>
 * Control fields are variable fields identified by tags beginning with two
 * zero's. They are comprised of data and a field terminator and do not contain
 * indicators or subfield codes. The structure of a control field according to
 * the MARC standard is as follows:
 *
 * <pre>DATA_ELEMENT FIELD_TERMINATOR</pre>
 *
 * @author Bas Peters
 */
public interface ControlField extends VariableField {

    /**
     * Returns the data element.
     *
     * @return The data element
     */
    public String getData();

    /**
     * Sets the data element.
     *
     * @param data The data element
     */
    public void setData(String data);

}
