// $Id: Verifier.java,v 1.1 2003/01/10 09:34:29 bpeters Exp $
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
 * <p><code>Verifier</code> checks tags and data elements.</p>
 *
 * <p><b>Note:</b> Currently a tag is only checked for it's length 
 * and data elements are checked for MARC control characters.</p>
 *
 * @author <a href="mailto:mail@bpeters.com">Bas Peters</a> 
 * @version $Revision: 1.1 $
 *
 */
public class Verifier {

    private static final char US = MarcConstants.US;

    private static final char FT = MarcConstants.FT;

    private static final char RT = MarcConstants.RT;

    private Verifier() {}

    /**
     * <p>Checks if the tag is a valid tag name.</p>
     *
     * @param tag the tag name to check
     * @throws IllegalTagException if the tag is not valid
     */
    public static void checkTag(String tag) {
	if (tag.length() != 3)
	    throw new IllegalTagException
		(tag, "not a variable field identifier");
	return;
    }

    /**
     * <p>Checks if the data element does not contain control charecters.</p>
     *
     * @param data the characters to check
     * @throws IllegalDataElementException if the data element 
     *                                     contains control characters
     */
    public static void checkDataElement(char[] data) {
	int len = data.length;
	if (len == 0)
	    return;
	int i = 0;
	do {
	    checkDataElement(data[i]);
	} while (++i < len);
    }


    /**
     * <p>Checks if the data element does not contain control charecters.</p>
     *
     * @param ch - the character to check
     * @throws IllegalDataElementException if the data element 
     *                                     contains control characters
     */
    public static void checkDataElement(char ch) {
	switch (ch) {
	case RT :
	    throw new IllegalDataElementException
		("Invalid character: record terminator");
	case FT :
	    throw new IllegalDataElementException
		("Invalid character: field Terminator");
	case US :
	    throw new IllegalDataElementException
		("Invalid character: subfield code identifier");
	default:
	    return;
	}
    }
}
