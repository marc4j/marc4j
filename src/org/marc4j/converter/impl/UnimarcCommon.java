/**
 * Copyright (C) 2018
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

package org.marc4j.converter.impl;

/**
 * Constant values for working with Unimarc records.
 *
 * @author SirsiDynix
 */
public class UnimarcCommon implements UnimarcConstants {
    /**
     * Converts code into the ISOCode
     *
     * @param code Should be a two character code that represents a character set
     *             as defined in the UNIMARC standard for tag 100: http://www.ifla.org/VI/3/p1996-1/uni1.htm#100 <BR>
     *             <UL>
     *             <LI>01 = ISO 646, IRV version (basic Latin set)</LI>
     *             <LI>02 = ISO Registration # 37 (basic Cyrillic set)</LI>
     *             <LI>03 = ISO 5426 (extended Latin set)</LI>
     *             <LI>04 = ISO DIS 5427 (extended Cyrillic set)</LI>
     *             <LI>05 = ISO 5428 (Greek set)</LI>
     *             <LI>06 = ISO 6438 (African coded character set)</LI>
     *             <LI>07 = ISO 10586 (Georgian set)</LI>
     *             <LI>08 = ISO 8957 (Hebrew set) Table 1</LI>
     *             <LI>09 = ISO 8957 (Hebrew set) Table 2</LI>
     *             <LI>10 = [Reserved]</LI>
     *             <LI>11 = ISO 5426-2 (Latin characters used in minor European languages and obsolete typography)</LI>
     *             <LI>50 = ISO 10646 Level 3 (Unicode)</LI>
     *             </UL>
     * @return The corresponding ISOCode
     */
    public static int determineCharSet(String code) {
        if (code != null && code.length() >= 2) {
            switch (code.charAt(0)) {
                case '0':
                    switch (code.charAt(1)) {
                        case '1':
                            return ISO_646;
                        case '2':
                            return ISO_REG_37;
                        case '3':
                            return ISO_5426;
                        case '4':
                            return ISO_5427;
                        case '5':
                            return ISO_5428;
                        case '6':
                            return ISO_6438;
                        case '7':
                            return ISO_10586;
                        case '8':
                            return ISO_8957_1;
                        case '9':
                            return ISO_8957_2;
                    }
                case '1':
                    if (code.charAt(1) == '1') {
                        return ISO_5426_2;
                    }
            }
        }
        return -1;
    }
}
