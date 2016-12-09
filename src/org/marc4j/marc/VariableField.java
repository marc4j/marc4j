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
 * Represents a variable field in a MARC record.
 *
 * @author Bas Peters
 */
public interface VariableField extends Serializable, Comparable<VariableField> {

    /**
     * Sets the identifier.
     * <p>
     * The purpose of this identifier is to provide an identifier for
     * persistency.
     *
     * @param id the identifier
     */
    public void setId(Long id);

    /**
     * Gets the identifier.
     * <p>
     * The purpose of this identifier is to provide an identifier for
     * persistency.
     *
     * @return The identifier
     */
    public Long getId();

    /**
     * Returns the tag name.
     *
     * @return String - the tag name
     */
    public String getTag();

    /**
     * Sets the tag name.
     *
     * @param tag the tag name
     */
    public void setTag(String tag);

    /**
     * Returns true if the given regular expression matches a subsequence of a
     * data element within the variable field.
     * <p>
     * See {@link java.util.regex.Pattern} for more information about Java
     * regular expressions.
     * </p>
     *
     * @param pattern the regular expression
     * @return true if the pattern matches, false othewise
     */
    public abstract boolean find(String pattern);

}
