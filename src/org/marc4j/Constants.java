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
 *
 */

package org.marc4j;

/**
 * Defines constant values.
 *
 * @author Bas Peters
 */
public class Constants {

    private Constants() { }

    /** RECORD TERMINATOR */
    public static final int RT = 0x001D;

    /** FIELD TERMINATOR */
    public static final int FT = 0x001E;

    /** SUBFIELD DELIMITER */
    public static final int US = 0x001F;

    /** BLANK */
    public static final int BLANK = 0x0020;

    /** NS URI */
    public static final String MARCXML_NS_URI = "http://www.loc.gov/MARC21/slim";

    /** NS Prefix */
    public static final String MARCXML_NS_PREFIX = "marc";

    /** MARC-8 ANSEL ENCODING **/
    public static final String MARC_8_ENCODING = "MARC8";

    /** ISO5426 ENCODING **/
    public static final String ISO5426_ENCODING = "ISO5426";

    /** ISO6937 ENCODING **/
    public static final String ISO6937_ENCODING = "ISO6937";

}
