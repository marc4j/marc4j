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
 * @author SirsiDynix
 */
public interface UnimarcConstants {
    int ISO_646 = 0x40;  // ISO 646, IRV version (basic Latin set)
    int ISO_5426 = 0x50;  // ISO 5426 (extended Latin set)
    int ISO_REG_37 = 0x4E;  // ISO Registration # 37 (basic Cyrillic set)
    int ISO_5427 = 0x51;  // ISO DIS 5427 (extended Cyrillic set)
    int ISO_5428 = 0x53;  // ISO 5428 (Greek set)
    int ISO_6438 = 0x4D;  // ISO 6438 (African coded character set)
    int ISO_10586 = 0xFF;  // @todo define this ISO 10586 (Georgian set)
    int ISO_8957_1 = 0xFF;  // @todo define this ISO 8957 (Hebrew set) Table 1
    int ISO_8957_2 = 0xFF;  // @todo define this ISO 8957 (Hebrew set) Table 2
    int ISO_5426_2 = 0xFF;  // @todo define this ISO 5426-2 (Latin characters used in minor European languages and obsolete typography)
    int ISO_10646 = 0xFF;  // ISO 10646 Level 3 (Unicode)

    int DEFAULT_G0 = ISO_646;
    int DEFAULT_G1 = ISO_5426;
    int DEFAULT_G2 = 0;
    int DEFAULT_G3 = 0;

    char ESC = 0x1B;     // ESCAPE definition
    char LS0 = 0x0F;                        //  shift to G0 02-07
    char LS1 = 0x0E;                        //  shift to G1 02-07
    char LS1R = 0x7E;     // used with ESC       shift to G1 0A-0F
    char LS2 = 0x6E;     // used with ESC       shift to G2 02-07
    char LS2R = 0x7D;     // used with ESC       shift to G2 0A-0F
    char LS3 = 0x6F;     // used with ESC       shift to G3 02-07
    char LS3R = 0x7C;     // used with ESC       shift to G3 0A-0F
}
