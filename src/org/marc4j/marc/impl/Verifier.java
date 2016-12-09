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

import org.marc4j.marc.ControlField;

import java.util.Collection;

/**
 * Handles MARC checks on tags, data elements and <code>Record</code> objects.
 * 
 * @author Bas Peters
 */
public class Verifier
{
    public static final String LEADER_AS_FIELD = "000";

    private Verifier() { }

    /**
     * Returns true if the given <code>String</code> value identifies a tag for a control field (001 through 009).
     *
     * @param tag - the tag to check whether it refers to a ControlField
     * @return true if the tag is a valid ControlField tag
     */
    public static boolean isControlField(String tag) {
        if (tag.length() == 3 && tag.charAt(0) == '0' && tag.charAt(1) == '0' && tag.charAt(2) >= '0' && tag.charAt(2) <= '9')// if (Integer.parseInt(tag) < 10)
        {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the given <code>String</code> value identifies a tag for the Leader as a control field
     *
     * @param tag - the tag to check whether it refers to the special case of the Leader being treated as a ControlField
     * @return true if the tag is "000"
     */
    public static boolean isLeaderField(String tag) {
        return (tag.equals(LEADER_AS_FIELD));
    }

    /**
     * Returns true if the given <code>String</code> value identifies a tag for a control number field (001).
     *
     * @param tag - the tag to check whether it refers to a DataField
     * @return true if the tag is a valid DataField tag
     */
    public static boolean isControlNumberField(String tag) {
        return (tag.equals("001"));
    }

    /**
     * Returns true if the given <code>Collection</code> contains an instance of a <code>ControlField</code> with a control number field tag (001).
     * 
     * @param col - the collection of <code>ControlField</code> objects.
     * @return true of one of the ControlFields passed in has tag "001"
     */
    public static boolean hasControlNumberField(Collection<ControlField> col)
    {
        for (ControlField field : col)
        {
            String tag = field.getTag();
            if (isControlNumberField(tag)) return true;
        }
        return false;
    }
}
