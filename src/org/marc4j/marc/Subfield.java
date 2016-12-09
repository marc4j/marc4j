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

import java.io.Serializable;

/**
 * Represents a subfield in a MARC record.
 *
 * @author Bas Peters
 */
public interface Subfield extends Serializable {

    /**
     * Sets the identifier.
     *
     * <p>
     * The purpose of this identifier is to provide an identifier for
     * persistency.
     *
     * @param id
     *            the identifier
     */
    public void setId(Long id);

    /**
     * Returns the identifier.
     *
     * @return Long - the identifier
     */
    public Long getId();

    /**
     * Returns the data element identifier.
     *
     * @return char - the data element identifier
     */
    public char getCode();

    /**
     * Sets the data element identifier.
     *
     * @param code
     *            the data element identifier
     */
    public void setCode(char code);

    /**
     * Returns the data element.
     *
     * @return String - the data element
     */
    public String getData();

    /**
     * Sets the data element.
     *
     * @param data
     *            the data element
     */
    public void setData(String data);

    /**
     * Returns true if the given regular expression matches a subsequence of the
     * data element.
     *
     * See (@link java.util.regex.Pattern} for regular expressions.
     *
     * @param pattern
     *            the regular expression
     * @return true if the pattern matches, false othewise
     */
    public boolean find(String pattern);

}
