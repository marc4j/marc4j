// $Id: Unicode.java,v 1.2 2002/07/06 13:40:20 bpeters Exp $
/**
 * Copyright (C) 2002 Bas Peters (mail@bpeters.com)
 *
 * This file is part of MARC4J
 *
 * MARC4J is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * MARC4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MARC4J; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * As a special exception, if you link this library with other files to
 * produce an executable, this library does not by itself cause the
 * resulting executable to be covered by the GNU General Public License.
 * This exception does not however invalidate any other reasons why the
 * executable file might be covered by the GNU General Public License.
 */
package org.marc4j.util;

/**
 * <p>Provides a mapping between combining
 * diacritical marks and combining characters in Unicode.   </p>
 *
 * <p><b>Note:</b> currently the mapping is restricted to non-spacing
 * characters in ANSEL. Check the source code for more details about the mapping.</p>
 *
 * @author <a href="mailto:mail@bpeters.com">Bas Peters</a> 
 * @version $Revision: 1.2 $
 */
public class Unicode {

    /**
     * <p>Returns true if the UCS/Unicode character is a combining
     * diacritical mark (Unicode range: 0300-036F).      </p>
     *
     * @param c the character to validate
     * @return <code>boolean</code> - true if the character is a combining
     *                                diacritical mark,
     *                                false if not
     */
    public static boolean isCombiningDiacriticalMark(char c) {
	switch (c) {
	// combining hook above
	case 0x0309 :
	// combining grave accent
	case 0x0300 :
	// combining acute accent
	case 0x0301 :
	    // combining circumflex
	    case 0x0302 :
	    // combining tilde
	    case 0x0303 :
	    // combining macron
	    case 0x0304 :
	    // combining breve
	    case 0x0306 :
	    // combining dot above
	    case 0x0307 :
	    // combining diaeresis
	    case 0x0308 :
	    // combining caron
	    case 0x030C :
	    // combining ring above
	    case 0x030A :
	    // combining ligature left half
	    case 0xFE20 :
	    // combining ligature right half
	    case 0xFE21 :
	    // combining comma above right
	    case 0x0315 :
	    // combining double acute accent
	    case 0x030B :
	    // combining candrabindu
	    case 0x0310 :
	    // combining cedilla
	    case 0x0327 :
	    // combining ogonek
	    case 0x0328 :
	    // combining dot below
	    case 0x0323 :
	    // combining double dot below
	    case 0x0324 :
	    // combining ring below
	    case 0x0325 :
	    // combining double low line
	    case 0x0333 :
	    // combining low line
	    case 0x0332 :
	    // combining comma below
	    case 0x0336 :
	    // combining comma below
	    case 0x0326 :
	    // combining left half ring below
	    case 0x031C :
	    // combining breve below
	    case 0x032E :
	    // combining double tilde left half
	    case 0xFE22 :
	    // combining double tilde right half
	    case 0xFE23 :
	    // combining comma above
	    case 0x0313 :
	        break;
	    default:
	        return false;
	    }
	    return true;
    }

    /**
     * <p>Converts UCS/Unicode non-spacing characters to UCS/Unicode combining
     * characters.       </p>
     *
     * @param c the non-spacing character
     * @param d the base character
     * @return <code>char</code> - returns the converted characters,
     *                           returns 0 if the characters are not converted.
     */
    public static char getCombiningChar(char c, char d) {
	    switch (c) {
        case 0x0300 :
            switch (d) {
            case 0x0041 :
                return 0x00C0;
            case 0x0045 :
                return 0x00C8;
            case 0x0049 :
                return 0x00CC;
            case 0x004F :
                return 0x00D2;
            case 0x0055 :
                return 0x00D9;
            case 0x0061 :
                return 0x00E0;
            case 0x0065 :
                return 0x00E8;
            case 0x0069 :
                return 0x00EC;
            case 0x006F :
                return 0x00F2;
            case 0x0075 :
                return 0x00F9;
            default:
                break;
            }
        case 0x0301 :
            switch (d) {
            case 0x0041 :
                return 0x00C1;
            case 0x0043 :
                return 0x0106;
            case 0x0045 :
                return 0x00C9;
            case 0x0047 :
                return 0x01F4;
            case 0x0049 :
                return 0x00CD;
            case 0x004B :
                return 0x1E30;
            case 0x004C :
                return 0x0139;
            case 0x004D :
                return 0x1E3E;
            case 0x004E :
                return 0x0143;
            case 0x004F :
                return 0x00D3;
            case 0x0050 :
                return 0x1E54;
            case 0x0052 :
                return 0x0154;
            case 0x0053 :
                return 0x015A;
            case 0x0055 :
                return 0x00DA;
            case 0x0059 :
                return 0x00DD;
            case 0x005A :
                return 0x0179;
            case 0x0061 :
                return 0x00E1;
            case 0x0063 :
                return 0x0107;
            case 0x0065 :
                return 0x00E9;
            case 0x0067 :
                return 0x01F5;
            case 0x0069 :
                return 0x00ED;
            case 0x006B :
                return 0x1E31;
            case 0x006C :
                return 0x013A;
            case 0x006D :
                return 0x1E3F;
            case 0x006E :
                return 0x0144;
            case 0x006F :
                return 0x00F3;
            case 0x0070 :
                return 0x1E55;
            case 0x0072 :
                return 0x0155;
            case 0x0073 :
                return 0x015B;
            case 0x0075 :
                return 0x00FA;
            case 0x0079 :
                return 0x00FD;
            case 0x007A :
                return 0x017A;
            case 0x00A2 :
                return 0x01FE;
            case 0x00B2 :
                return 0x01FF;
            default:
                break;
            }
        case 0x0302 :
            switch(d) {
            case 0x0041 :
                return 0x00C2;
            case 0x0043 :
                return 0x0108;
            case 0x0045 :
                return 0x00CA;
            case 0x0047 :
                return 0x011C;
            case 0x0048 :
                return 0x0124;
            case 0x0049 :
                return 0x00CE;
            case 0x004A :
                return 0x0134;
            case 0x004B :
                return 0x01E8;
            case 0x004C :
                return 0x1E3C;
            case 0x004F :
                return 0x00D4;
            case 0x0053 :
                return 0x015C;
            case 0x0054 :
                return 0x1E70;
            case 0x0055 :
                return 0x00DB;
            case 0x0057 :
                return 0x0174;
            case 0x0059 :
                return 0x0176;
            case 0x0061 :
                return 0x00E2;
            case 0x0063 :
                return 0x0109;
            case 0x0065 :
                return 0x00EA;
            case 0x0067 :
                return 0x011D;
            case 0x0068 :
                return 0x0125;
            case 0x0069 :
                return 0x00EE;
            case 0x006A :
                return 0x0135;
            case 0x006B :
                return 0x01E9;
            case 0x006C :
                return 0x1E3D;
            case 0x006F :
                return 0x00F4;
            case 0x0073 :
                return 0x015D;
            case 0x0074 :
                return 0x1E71;
            case 0x0075 :
                return 0x00FB;
            case 0x0077 :
                return 0x0175;
            case 0x0079 :
                return 0x0177;
            default:
                break;
            }
        case 0x0303 :
            switch(d) {
            case 0x0041 :
                return 0x00C3;
            case 0x0049 :
                return 0x0128;
            case 0x004E :
                return 0x00D1;
            case 0x004F :
                return 0x00D5;
            case 0x0055 :
                return 0x0168;
            case 0x0061 :
                return 0x00E3;
            case 0x0069 :
                return 0x0129;
            case 0x006E :
                return 0x00F1;
            case 0x006F :
                return 0x00F5;
            case 0x0075 :
                return 0x0169;
            default:
                break;
            }
        case 0x0304 :
            switch(d) {
            case 0x0041 :
                return 0x0100;
            case 0x0045 :
                return 0x0112;
            case 0x0049 :
                return 0x012A;
            case 0x004F :
                return 0x014C;
            case 0x0055 :
                return 0x016A;
            case 0x0061 :
                return 0x0101;
            case 0x0065 :
                return 0x0113;
            case 0x0069 :
                return 0x012B;
            case 0x006F :
                return 0x014D;
            case 0x0075 :
                return 0x016B;
            default:
                break;
            }
        case 0x0306 :
            switch(d) {
            case 0x0041 :
                return 0x0102;
            case 0x0045 :
                return 0x0114;
            case 0x0047 :
                return 0x011E;
            case 0x0049 :
                return 0x012C;
            case 0x004F :
                return 0x014E;
            case 0x0055 :
                return 0x016C;
            case 0x0061 :
                return 0x0103;
            case 0x0065 :
                return 0x0115;
            case 0x0069 :
                return 0x012D;
            case 0x006F :
                return 0x014F;
            case 0x0075 :
                return 0x016D;
            default:
                break;
            }
        case 0x0307 :
            switch(d) {
            case 0x0041 :
                return 0x0226;
            case 0x0043 :
                return 0x010A;
            case 0x0045 :
                return 0x0116;
            case 0x0046 :
                return 0x1E1E;
            case 0x0047 :
                return 0x0120;
            case 0x0048 :
                return 0x1E22;
            case 0x0049 :
                return 0x0130;
            case 0x004D :
                return 0x1E40;
            case 0x004E :
                return 0x1E44;
            case 0x0054 :
                return 0x1E6A;
            case 0x0057 :
                return 0x1E86;
            case 0x0058 :
                return 0x1E8A;
            case 0x0059 :
                return 0x1E8E;
            case 0x005A :
                return 0x017B;
            case 0x0061 :
                return 0x0227;
            case 0x0063 :
                return 0x010B;
            case 0x0065 :
                return 0x0117;
            case 0x0066 :
                return 0x1E1F;
            case 0x0067 :
                return 0x0121;
            case 0x0068 :
                return 0x1E23;
            case 0x006D :
                return 0x1E41;
            case 0x006E :
                return 0x1E45;
            case 0x0074 :
                return 0x1E6B;
            case 0x0077 :
                return 0x1E87;
            case 0x0078 :
                return 0x1E8B;
            case 0x0079 :
                return 0x1E8F;
            case 0x007A :
                return 0x017C;
            default:
                break;
            }
        case 0x0308 :
            switch(d) {
            case 0x0041 :
                return 0x00C4;
            case 0x0045 :
                return 0x00CB;
            case 0x0049 :
                return 0x00CF;
            case 0x004F :
                return 0x00D6;
            case 0x0055 :
                return 0x00DC;
            case 0x0059 :
                return 0x0178;
            case 0x0061 :
                return 0x00E4;
            case 0x0065 :
                return 0x00EB;
            case 0x0069 :
                return 0x00EF;
            case 0x006F :
                return 0x00F6;
            case 0x0075 :
                return 0x00FC;
            case 0x0079 :
                return 0x00FF;
            default:
                break;
            }
        case 0x030C :
            switch(d) {
            case 0x0043 :
                return 0x010C;
            case 0x0044 :
                return 0x010E;
            case 0x0045 :
                return 0x011A;
            case 0x0047 :
                return 0x01E6;
            case 0x004C :
                return 0x013D;
            case 0x004E :
                return 0x0147;
            case 0x004F :
                return 0x01D1;
            case 0x0052 :
                return 0x0158;
            case 0x0053 :
                return 0x0160;
            case 0x0054 :
                return 0x0164;
            case 0x005A :
                return 0x017D;
            case 0x0063 :
                return 0x010D;
            case 0x0064 :
                return 0x010F;
            case 0x0065 :
                return 0x011B;
            case 0x0067 :
                return 0x01E7;
            case 0x006C :
                return 0x013E;
            case 0x006E :
                return 0x0148;
            case 0x006F :
                return 0x01D2;
            case 0x0072 :
                return 0x0159;
            case 0x0073 :
                    return 0x0161;
            case 0x0074 :
                return 0x0165;
            case 0x007A :
                return 0x017E;
            default:
                break;
            }
        case 0x030A :
            switch(d) {
            case 0x0041 :
                return 0x00C5;
            case 0x0055 :
                return 0x016E;
            case 0x0061 :
                return 0x00E5;
            case 0x0075 :
                return 0x016F;
            default:
                break;
            }
        case 0x0315 :
            switch(d) {
            case 0x006E :
                return 0x0149;
            default:
                break;
            }
        case 0x030B :
            switch(d) {
            case 0x004F :
                return 0x0150;
            case 0x0055 :
                return 0x0170;
            case 0x006F :
                return 0x0151;
            case 0x0075 :
                return 0x0171;
            default:
                break;
            }
        case 0x0327 :
            switch(d) {
            case 0x0043 :
                return 0x00C7;
            case 0x0047 :
                return 0x0122;
            case 0x004B :
                return 0x0136;
            case 0x004C :
                return 0x013B;
            case 0x004E :
                return 0x0145;
            case 0x0053 :
                return 0x015E;
            case 0x0054 :
                return 0x0162;
            case 0x0063 :
                return 0x00E7;
            case 0x0067 :
                return 0x0123;
            case 0x006B :
                return 0x0137;
            case 0x006C :
                return 0x013C;
            case 0x006E :
                return 0x0146;
            case 0x0072 :
                return 0x0157;
            case 0x0073 :
                return 0x015F;
            case 0x0074 :
                return 0x0163;
            default:
                break;
            }
        case 0x0328 :
            switch(d) {
            case 0x0041 :
                return 0x0104;
            case 0x0045 :
                return 0x0118;
            case 0x0049 :
                return 0x012E;
            case 0x004F :
                return 0x01EA;
            case 0x0055 :
                return 0x0172;
            case 0x0061 :
                return 0x0105;
            case 0x0065 :
                return 0x0119;
            case 0x0069 :
                return 0x012F;
            case 0x006F :
                return 0x01EB;
            case 0x0075 :
                return 0x0173;
            default:
                break;
            }
        case 0x0323 :
            switch(d) {
            case 0x0041 :
                return 0x1EA0;
            case 0x0044 :
                return 0x1E0C;
            case 0x004B :
                return 0x1E32;
            case 0x004C :
                return 0x1E36;
            case 0x004D :
                return 0x1E42;
            case 0x004E :
                return 0x1E46;
            case 0x0052 :
                return 0x1E5A;
            case 0x0054 :
                return 0x1E6C;
            case 0x0056 :
                return 0x1E7E;
            case 0x005A :
                return 0x1E92;
            case 0x0061 :
                return 0x1EA1;
            case 0x0064 :
                return 0x1E0D;
            case 0x006B :
                return 0x1E33;
            case 0x006C :
                return 0x1E37;
            case 0x006D :
                return 0x1E43;
            case 0x006E :
                return 0x1E47;
            case 0x0072 :
                return 0x1E5B;
            case 0x0074 :
                return 0x1E6D;
            case 0x0076 :
                return 0x1E7F;
            case 0x007A :
                return 0x1E93;
            default:
                break;
            }
        case 0x0333 :
            switch(d) {
            case 0x0047 :
                return 0x01E4;
            case 0x0048 :
                return 0x0126;
            case 0x0054 :
                return 0x0166;
            case 0x0067 :
                return 0x01E5;
            case 0x0068 :
                return 0x0127;
            case 0x0074 :
                return 0x0167;
            default:
                break;
            }
        case 0x0332 :
            switch(d) {
            case 0x0042 :
                return 0x1E06;
            case 0x0044 :
                return 0x1E0E;
            case 0x004B :
                return 0x1E34;
            case 0x004C :
                return 0x1E3A;
            case 0x004E :
                return 0x1E47;
            case 0x0052 :
                return 0x1E5E;
            case 0x0054 :
                return 0x1E6E;
            case 0x0062 :
                return 0x1E07;
            case 0x0064 :
                return 0x1E0F;
            case 0x006B :
                return 0x1E35;
            case 0x006C :
                return 0x1E3B;
            case 0x006E :
                return 0x1E48;
            case 0x0072 :
                return 0x1E5F;
            case 0x0074 :
                return 0x1E6F;
            default:
                break;
            }
        case 0x031C :
            switch(d) {
            case 0x0052 :
                return 0x0156;
            default:
                break;
            }
        case 0x032E :
            switch(d) {
            case 0x0048 :
                return 0x1E2A;
            case 0x0068 :
                return 0x1E2B;
            default:
                break;
            }
        default:
            break;
        }
        return 0;
    }

}
