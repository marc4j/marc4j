// $Id: Tag.java,v 1.5 2003/01/10 09:39:43 bpeters Exp $
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

/**
 * <p><code>Tag</code> defines behaviour for a MARC tag.  </p>
 *
 * <p>A MARC tag is a three character string used to identify an
 * associated variable field. According to the MARC standard the tag may
 * consist of ASCII numeric characters (decimal integers 0-9) and/or
 * ASCII alphabetic characters (uppercase or lowercase, but not both).</p>
 *
 * @author <a href="mailto:mail@bpeters.com">Bas Peters</a> 
 * @version $Revision: 1.5 $
 *
 */
public class Tag {

    /** DIGIT ZERO. */
    private static final char ZERO = 0x0030;

    /**
     * <p>Returns true if the given value is a valid tag value.  </p>
     *
     * <p>The method returns true if the tag contains three alphabetic
     * or numeric ASCII graphic characters.</p>
     *
     * <p><b>Note:</b> mixing uppercase and lowercase letters is not
     * validated.</p>
     *
     * @param tag the tag name
     */
    public static boolean isValid(String tag) {
	Verifier.checkTag(tag);
	return true;
    }

    /**
     * <p>Returns true if the tag identifies a control number field.  </p>
     *
     * <p>The method returns false if the tag does not equals 001.</p>
     *
     * @param tag the tag name
     * @return <code>boolean</code> - tag identifies a control number field
     *                                (true) or not (false)
     */
    public static boolean isControlNumberField(String tag) {
	Verifier.checkTag(tag);
        if (! tag.equals("001"))
            return false;
        return true;
    }

    /**
     * <p>Returns true if the tag identifies a control field.  </p>
     *
     * <p>The method returns false if the tag does not begin with
     * two zero's.</p>
     *
     * @param tag the tag name
     * @return <code>boolean</code> - tag identifies a control field (true)
     *                                or a data field (false)
     */
    public static boolean isControlField(String tag) {
	Verifier.checkTag(tag);
        if (tag.charAt(0) != ZERO)
            return false;
        if (tag.charAt(1) != ZERO)
            return false;
        return true;
    }

    /**
     * <p>Returns true if the tag identifies a data field.  </p>
     *
     * <p>The method returns false if the tag begins with two zero's.</p>
     *
     * @param tag the tag name
     * @return <code>boolean</code> - tag identifies a data field (true)
     *                                or a control field (false)
     */
    public static boolean isDataField(String tag) {
	Verifier.checkTag(tag);
        if (! isControlField(tag))
            return true;
        return false;
    }

}
