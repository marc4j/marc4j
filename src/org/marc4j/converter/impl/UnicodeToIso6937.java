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

package org.marc4j.converter.impl;

import org.marc4j.converter.CharConverter;

/**
 * <p>
 * A utility to convert UCS/Unicode data to ISO 6937.
 * </p>
 * 
 * @author Bas Peters
 * @author Yves Pratter
 */
public class UnicodeToIso6937 extends CharConverter {

    /**
     * <p>
     * Converts UCS/Unicode data to ISO 6937.
     * </p>
     * <p>
     * A question mark (0x3F) is returned if there is no match.
     * </p>
     * 
     * @param data - the UCS/Unicode data in an array of char
     * @return {@link String}- the ISO 6937 data
     */
    @Override
    public String convert(final char data[]) {
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            final char c = data[i];
            if (c < 128) {
                sb.append(c);
            } else {
                final int d = convert(c);
                if (d < 256) {
                    sb.append((char) d);
                } else {
                    sb.append((char) (d / 256));
                    sb.append((char) (d % 256));
                }
            }
        }
        return sb.toString();
    }

    private int convert(final int i) {
        switch (i) {
            case 0x00A0:
                return 0xA0; // 10/00 NO-BREAK SPACE
            case 0x00A1:
                return 0xA1; // 10/01 INVERTED EXCLAMATION MARK
            case 0x00A2:
                return 0xA2; // 10/02 CENT SIGN
            case 0x00A3:
                return 0xA3; // 10/03 POUND SIGN
            case 0x00A4:
                return 0xA8; // 10/08 CURRENCY SIGN
            case 0x00A5:
                return 0xA5; // 10/05 YEN SIGN
            case 0x00A6:
                return 0xD7; // 13/07 BROKEN BAR
            case 0x00A7:
                return 0xA7; // 10/07 SECTION SIGN
            case 0x00A8:
                return 0xC820; // DIAERESIS
            case 0x00A9:
                return 0xD3; // 13/03 COPYRIGHT SIGN
            case 0x00AA:
                return 0xE3; // 14/03 FEMININE ORDINAL INDICATOR
            case 0x00AB:
                return 0xAB; // 10/11 LEFT-POINTING DOUBLE ANGLE QUOTATION MARK
            case 0x00AC:
                return 0xD6; // 13/06 NOT SIGN
            case 0x00AD:
                return 0xFF; // 15/15 SOFT HYPHEN
            case 0x00AE:
                return 0xD4; // 13/04 TRADE MARK SIGN
            case 0x00B0:
                return 0xB0; // 11/00 DEGREE SIGN
            case 0x00B1:
                return 0xB1; // 11/01 PLUS-MINUS SIGN
            case 0x00B2:
                return 0xB2; // 11/02 SUPERSCRIPT TWO
            case 0x00B3:
                return 0xB3; // 11/03 SUPERSCRIPT THREE
            case 0x00B4:
                return 0xC220; // ACUTE ACCENT
            case 0x00B5:
                return 0xB5; // 11/05 MICRO SIGN
            case 0x00B6:
                return 0xB6; // 11/06 PILCROW SIGN
            case 0x00B7:
                return 0xB7; // 11/07 MIDDLE DOT
            case 0x00B8:
                return 0xCB20; // CEDILLA
            case 0x00B9:
                return 0xD1; // 13/01 SUPERSCRIPT ONE
            case 0x00BA:
                return 0xEB; // 14/11 MASCULINE ORDINAL INDICATOR
            case 0x00BB:
                return 0xBB; // 11/11 RIGHT-POINTING DOUBLE ANGLE QUOTATION MARK
            case 0x00BC:
                return 0xBC; // 11/12 VULGAR FRACTION ONE QUARTER
            case 0x00BD:
                return 0xBD; // 11/13 VULGAR FRACTION ONE HALF
            case 0x00BE:
                return 0xBE; // 11/14 VULGAR FRACTION THREE QUARTERS
            case 0x00BF:
                return 0xBF; // 11/15 INVERTED QUESTION MARK
            case 0x00C0:
                return 0xC141; // LATIN CAPITAL LETTER A WITH GRAVE
            case 0x00C1:
                return 0xC241; // LATIN CAPITAL LETTER A WITH ACUTE
            case 0x00C2:
                return 0xC341; // LATIN CAPITAL LETTER A WITH CIRCUMFLEX
            case 0x00C3:
                return 0xC441; // LATIN CAPITAL LETTER A WITH TILDE
            case 0x00C4:
                return 0xC841; // LATIN CAPITAL LETTER A WITH DIAERESIS
            case 0x00C5:
                return 0xCA41; // LATIN CAPITAL LETTER A WITH RING ABOVE
            case 0x00C6:
                return 0xE1; // 14/01 LATIN CAPITAL LETTER AE
            case 0x00C7:
                return 0xCB43; // LATIN CAPITAL LETTER C WITH CEDILLA
            case 0x00C8:
                return 0xC145; // LATIN CAPITAL LETTER E WITH GRAVE
            case 0x00C9:
                return 0xC245; // LATIN CAPITAL LETTER E WITH ACUTE
            case 0x00CA:
                return 0xC345; // LATIN CAPITAL LETTER E WITH CIRCUMFLEX
            case 0x00CB:
                return 0xC845; // LATIN CAPITAL LETTER E WITH DIAERESIS
            case 0x00CC:
                return 0xC149; // LATIN CAPITAL LETTER I WITH GRAVE
            case 0x00CD:
                return 0xC249; // LATIN CAPITAL LETTER I WITH ACUTE
            case 0x00CE:
                return 0xC349; // LATIN CAPITAL LETTER I WITH CIRCUMFLEX
            case 0x00CF:
                return 0xC849; // LATIN CAPITAL LETTER I WITH DIAERESIS
            case 0x00D1:
                return 0xC44E; // LATIN CAPITAL LETTER N WITH TILDE
            case 0x00D2:
                return 0xC14F; // LATIN CAPITAL LETTER O WITH GRAVE
            case 0x00D3:
                return 0xC24F; // LATIN CAPITAL LETTER O WITH ACUTE
            case 0x00D4:
                return 0xC34F; // LATIN CAPITAL LETTER O WITH CIRCUMFLEX
            case 0x00D5:
                return 0xC44F; // LATIN CAPITAL LETTER O WITH TILDE
            case 0x00D6:
                return 0xC84F; // LATIN CAPITAL LETTER O WITH DIAERESIS
            case 0x00D7:
                return 0xB4; // 11/04 MULTIPLICATION SIGN
            case 0x00D8:
                return 0xE9; // 14/09 LATIN CAPITAL LETTER O WITH STROKE
            case 0x00D9:
                return 0xC155; // LATIN CAPITAL LETTER U WITH GRAVE
            case 0x00DA:
                return 0xC255; // LATIN CAPITAL LETTER U WITH ACUTE
            case 0x00DB:
                return 0xC355; // LATIN CAPITAL LETTER U WITH CIRCUMFLEX
            case 0x00DC:
                return 0xC855; // LATIN CAPITAL LETTER U WITH DIAERESIS
            case 0x00DD:
                return 0xC259; // LATIN CAPITAL LETTER Y WITH ACUTE
            case 0x00DE:
                return 0xEC; // 14/12 LATIN CAPITAL LETTER THORN
            case 0x00DF:
                return 0xFB; // 15/11 LATIN SMALL LETTER SHARP S
            case 0x00E0:
                return 0xC161; // LATIN SMALL LETTER A WITH GRAVE
            case 0x00E1:
                return 0xC261; // LATIN SMALL LETTER A WITH ACUTE
            case 0x00E2:
                return 0xC361; // LATIN SMALL LETTER A WITH CIRCUMFLEX
            case 0x00E3:
                return 0xC461; // LATIN SMALL LETTER A WITH TILDE
            case 0x00E4:
                return 0xC861; // LATIN SMALL LETTER A WITH DIAERESIS
            case 0x00E5:
                return 0xCA61; // LATIN SMALL LETTER A WITH RING ABOVE
            case 0x00E6:
                return 0xF1; // 15/01 LATIN SMALL LETTER AE
            case 0x00E7:
                return 0xCB63; // LATIN SMALL LETTER C WITH CEDILLA
            case 0x00E8:
                return 0xC165; // LATIN SMALL LETTER E WITH GRAVE
            case 0x00E9:
                return 0xC265; // LATIN SMALL LETTER E WITH ACUTE
            case 0x00EA:
                return 0xC365; // LATIN SMALL LETTER E WITH CIRCUMFLEX
            case 0x00EB:
                return 0xC865; // LATIN SMALL LETTER E WITH DIAERESIS
            case 0x00EC:
                return 0xC169; // LATIN SMALL LETTER I WITH GRAVE
            case 0x00ED:
                return 0xC269; // LATIN SMALL LETTER I WITH ACUTE
            case 0x00EE:
                return 0xC369; // LATIN SMALL LETTER I WITH CIRCUMFLEX
            case 0x00EF:
                return 0xC869; // LATIN SMALL LETTER I WITH DIAERESIS
            case 0x00F0:
                return 0xF3; // 15/03 LATIN SMALL LETTER ETH
            case 0x00F1:
                return 0xC46E; // LATIN SMALL LETTER N WITH TILDE
            case 0x00F2:
                return 0xC16F; // LATIN SMALL LETTER O WITH GRAVE
            case 0x00F3:
                return 0xC26F; // LATIN SMALL LETTER O WITH ACUTE
            case 0x00F4:
                return 0xC36F; // LATIN SMALL LETTER O WITH CIRCUMFLEX
            case 0x00F5:
                return 0xC46F; // LATIN SMALL LETTER O WITH TILDE
            case 0x00F6:
                return 0xC86F; // LATIN SMALL LETTER O WITH DIAERESIS
            case 0x00F7:
                return 0xB8; // 11/08 DIVISION SIGN
            case 0x00F8:
                return 0xF9; // 15/09 LATIN SMALL LETTER O WITH STROKE
            case 0x00F9:
                return 0xC175; // LATIN SMALL LETTER U WITH GRAVE
            case 0x00FA:
                return 0xC275; // LATIN SMALL LETTER U WITH ACUTE
            case 0x00FB:
                return 0xC375; // LATIN SMALL LETTER U WITH CIRCUMFLEX
            case 0x00FC:
                return 0xC875; // LATIN SMALL LETTER U WITH DIAERESIS
            case 0x00FD:
                return 0xC279; // LATIN SMALL LETTER Y WITH ACUTE
            case 0x00FE:
                return 0xFC; // 15/12 LATIN SMALL LETTER THORN
            case 0x00FF:
                return 0xC879; // LATIN SMALL LETTER Y WITH DIAERESIS
            case 0x0100:
                return 0xC541; // LATIN CAPITAL LETTER A WITH MACRON
            case 0x0101:
                return 0xC561; // LATIN SMALL LETTER A WITH MACRON
            case 0x0102:
                return 0xC641; // LATIN CAPITAL LETTER A WITH BREVE
            case 0x0103:
                return 0xC661; // LATIN SMALL LETTER A WITH BREVE
            case 0x0104:
                return 0xCE41; // LATIN CAPITAL LETTER A WITH OGONEK
            case 0x0105:
                return 0xCE61; // LATIN SMALL LETTER A WITH OGONEK
            case 0x0106:
                return 0xC243; // LATIN CAPITAL LETTER C WITH ACUTE
            case 0x0107:
                return 0xC263; // LATIN SMALL LETTER C WITH ACUTE
            case 0x0108:
                return 0xC343; // LATIN CAPITAL LETTER C WITH CIRCUMFLEX
            case 0x0109:
                return 0xC363; // LATIN SMALL LETTER C WITH CIRCUMFLEX
            case 0x010A:
                return 0xC743; // LATIN CAPITAL LETTER C WITH DOT ABOVE
            case 0x010B:
                return 0xC763; // LATIN SMALL LETTER C WITH DOT ABOVE
            case 0x010C:
                return 0xCF43; // LATIN CAPITAL LETTER C WITH CARON
            case 0x010D:
                return 0xCF63; // LATIN SMALL LETTER C WITH CARON
            case 0x010E:
                return 0xCF44; // LATIN CAPITAL LETTER D WITH CARON
            case 0x010F:
                return 0xCF64; // LATIN SMALL LETTER D WITH CARON
            case 0x0110:
                return 0xE2; // 14/02 LATIN CAPITAL LETTER D WITH STROKE
            case 0x0111:
                return 0xF2; // 15/02 LATIN SMALL LETTER D WITH STROKE
            case 0x0112:
                return 0xC545; // LATIN CAPITAL LETTER E WITH MACRON
            case 0x0113:
                return 0xC565; // LATIN SMALL LETTER E WITH MACRON
            case 0x0116:
                return 0xC745; // LATIN CAPITAL LETTER E WITH DOT ABOVE
            case 0x0117:
                return 0xC765; // LATIN SMALL LETTER E WITH DOT ABOVE
            case 0x0118:
                return 0xCE45; // LATIN CAPITAL LETTER E WITH OGONEK
            case 0x0119:
                return 0xCE65; // LATIN SMALL LETTER E WITH OGONEK
            case 0x011A:
                return 0xCF45; // LATIN CAPITAL LETTER E WITH CARON
            case 0x011B:
                return 0xCF65; // LATIN SMALL LETTER E WITH CARON
            case 0x011C:
                return 0xC347; // LATIN CAPITAL LETTER G WITH CIRCUMFLEX
            case 0x011D:
                return 0xC367; // LATIN SMALL LETTER G WITH CIRCUMFLEX
            case 0x011E:
                return 0xC647; // LATIN CAPITAL LETTER G WITH BREVE
            case 0x011F:
                return 0xC667; // LATIN SMALL LETTER G WITH BREVE
            case 0x0120:
                return 0xC747; // LATIN CAPITAL LETTER G WITH DOT ABOVE
            case 0x0121:
                return 0xC767; // LATIN SMALL LETTER G WITH DOT ABOVE
            case 0x0122:
                return 0xCB47; // LATIN CAPITAL LETTER G WITH CEDILLA
                // case 0x0123: return 0xCB67; // small g with cedilla
            case 0x0124:
                return 0xC348; // LATIN CAPITAL LETTER H WITH CIRCUMFLEX
            case 0x0125:
                return 0xC368; // LATIN SMALL LETTER H WITH CIRCUMFLEX
            case 0x0126:
                return 0xE4; // 14/04 LATIN CAPITAL LETTER H WITH STROKE
            case 0x0127:
                return 0xF4; // 15/04 LATIN SMALL LETTER H WITH STROKE
            case 0x0128:
                return 0xC449; // LATIN CAPITAL LETTER I WITH TILDE
            case 0x0129:
                return 0xC469; // LATIN SMALL LETTER I WITH TILDE
            case 0x012A:
                return 0xC549; // LATIN CAPITAL LETTER I WITH MACRON
            case 0x012B:
                return 0xC569; // LATIN SMALL LETTER I WITH MACRON
            case 0x012E:
                return 0xCE49; // LATIN CAPITAL LETTER I WITH OGONEK
            case 0x012F:
                return 0xCE69; // LATIN SMALL LETTER I WITH OGONEK
            case 0x0130:
                return 0xC749; // LATIN CAPITAL LETTER I WITH DOT ABOVE
            case 0x0131:
                return 0xF5; // 15/05 LATIN SMALL LETTER DOTLESS I
            case 0x0132:
                return 0xE6; // 14/06 LATIN CAPITAL LIGATURE IJ
            case 0x0133:
                return 0xF6; // 15/06 LATIN SMALL LIGATURE IJ
            case 0x0134:
                return 0xC34A; // LATIN CAPITAL LETTER J WITH CIRCUMFLEX
            case 0x0135:
                return 0xC36A; // LATIN SMALL LETTER J WITH CIRCUMFLEX
            case 0x0136:
                return 0xCB4B; // LATIN CAPITAL LETTER K WITH CEDILLA
            case 0x0137:
                return 0xCB6B; // LATIN SMALL LETTER K WITH CEDILLA
            case 0x0138:
                return 0xF0; // 15/00 LATIN SMALL LETTER KRA
            case 0x0139:
                return 0xC24C; // LATIN CAPITAL LETTER L WITH ACUTE
            case 0x013A:
                return 0xC26C; // LATIN SMALL LETTER L WITH ACUTE
            case 0x013B:
                return 0xCB4C; // LATIN CAPITAL LETTER L WITH CEDILLA
            case 0x013C:
                return 0xCB6C; // LATIN SMALL LETTER L WITH CEDILLA
            case 0x013D:
                return 0xCF4C; // LATIN CAPITAL LETTER L WITH CARON
            case 0x013E:
                return 0xCF6C; // LATIN SMALL LETTER L WITH CARON
            case 0x013F:
                return 0xE7; // 14/07 LATIN CAPITAL LETTER L WITH MIDDLE DOT
            case 0x0140:
                return 0xF7; // 15/07 LATIN SMALL LETTER L WITH MIDDLE DOT
            case 0x0141:
                return 0xE8; // 14/08 LATIN CAPITAL LETTER L WITH STROKE
            case 0x0142:
                return 0xF8; // 15/08 LATIN SMALL LETTER L WITH STROKE
            case 0x0143:
                return 0xC24E; // LATIN CAPITAL LETTER N WITH ACUTE
            case 0x0144:
                return 0xC26E; // LATIN SMALL LETTER N WITH ACUTE
            case 0x0145:
                return 0xCB4E; // LATIN CAPITAL LETTER N WITH CEDILLA
            case 0x0146:
                return 0xCB6E; // LATIN SMALL LETTER N WITH CEDILLA
            case 0x0147:
                return 0xCF4E; // LATIN CAPITAL LETTER N WITH CARON
            case 0x0148:
                return 0xCF6E; // LATIN SMALL LETTER N WITH CARON
            case 0x0149:
                return 0xEF; // 14/15 LATIN SMALL LETTER N PRECEDED BY
                             // APOSTROPHE
            case 0x014A:
                return 0xEE; // 14/14 LATIN CAPITAL LETTER ENG
            case 0x014B:
                return 0xFE; // 15/14 LATIN SMALL LETTER ENG
            case 0x014C:
                return 0xC54F; // LATIN CAPITAL LETTER O WITH MACRON
            case 0x014D:
                return 0xC56F; // LATIN SMALL LETTER O WITH MACRON
            case 0x0150:
                return 0xCD4F; // LATIN CAPITAL LETTER O WITH DOUBLE ACUTE
            case 0x0151:
                return 0xCD6F; // LATIN SMALL LETTER O WITH DOUBLE ACUTE
            case 0x0152:
                return 0xEA; // 14/10 LATIN CAPITAL LIGATURE OE
            case 0x0153:
                return 0xFA; // 15/10 LATIN SMALL LIGATURE OE
            case 0x0154:
                return 0xC252; // LATIN CAPITAL LETTER R WITH ACUTE
            case 0x0155:
                return 0xC272; // LATIN SMALL LETTER R WITH ACUTE
            case 0x0156:
                return 0xCB52; // LATIN CAPITAL LETTER R WITH CEDILLA
            case 0x0157:
                return 0xCB72; // LATIN SMALL LETTER R WITH CEDILLA
            case 0x0158:
                return 0xCF52; // LATIN CAPITAL LETTER R WITH CARON
            case 0x0159:
                return 0xCF72; // LATIN SMALL LETTER R WITH CARON
            case 0x015A:
                return 0xC253; // LATIN CAPITAL LETTER S WITH ACUTE
            case 0x015B:
                return 0xC273; // LATIN SMALL LETTER S WITH ACUTE
            case 0x015C:
                return 0xC353; // LATIN CAPITAL LETTER S WITH CIRCUMFLEX
            case 0x015D:
                return 0xC373; // LATIN SMALL LETTER S WITH CIRCUMFLEX
            case 0x015E:
                return 0xCB53; // LATIN CAPITAL LETTER S WITH CEDILLA
            case 0x015F:
                return 0xCB73; // LATIN SMALL LETTER S WITH CEDILLA
            case 0x0160:
                return 0xCF53; // LATIN CAPITAL LETTER S WITH CARON
            case 0x0161:
                return 0xCF73; // LATIN SMALL LETTER S WITH CARON
            case 0x0162:
                return 0xCB54; // LATIN CAPITAL LETTER T WITH CEDILLA
            case 0x0163:
                return 0xCB74; // LATIN SMALL LETTER T WITH CEDILLA
            case 0x0164:
                return 0xCF54; // LATIN CAPITAL LETTER T WITH CARON
            case 0x0165:
                return 0xCF74; // LATIN SMALL LETTER T WITH CARON
            case 0x0166:
                return 0xED; // 14/13 LATIN CAPITAL LETTER T WITH STROKE
            case 0x0167:
                return 0xFD; // 15/13 LATIN SMALL LETTER T WITH STROKE
            case 0x0168:
                return 0xC455; // LATIN CAPITAL LETTER U WITH TILDE
            case 0x0169:
                return 0xC475; // LATIN SMALL LETTER U WITH TILDE
            case 0x016A:
                return 0xC555; // LATIN CAPITAL LETTER U WITH MACRON
            case 0x016B:
                return 0xC575; // LATIN SMALL LETTER U WITH MACRON
            case 0x016C:
                return 0xC655; // LATIN CAPITAL LETTER U WITH BREVE
            case 0x016D:
                return 0xC675; // LATIN SMALL LETTER U WITH BREVE
            case 0x016E:
                return 0xCAAD; // LATIN CAPITAL LETTER U WITH RING ABOVE
            case 0x016F:
                return 0xCA75; // LATIN SMALL LETTER U WITH RING ABOVE
            case 0x0170:
                return 0xCD55; // LATIN CAPITAL LETTER U WITH DOUBLE ACUTE
            case 0x0171:
                return 0xCD75; // LATIN SMALL LETTER U WITH DOUBLE ACUTE
            case 0x0172:
                return 0xCE55; // LATIN CAPITAL LETTER U WITH OGONEK
            case 0x0173:
                return 0xCE75; // LATIN SMALL LETTER U WITH OGONEK
            case 0x0174:
                return 0xC357; // LATIN CAPITAL LETTER W WITH CIRCUMFLEX
            case 0x0175:
                return 0xC377; // LATIN SMALL LETTER W WITH CIRCUMFLEX
            case 0x0176:
                return 0xC359; // LATIN CAPITAL LETTER Y WITH CIRCUMFLEX
            case 0x0177:
                return 0xC379; // LATIN SMALL LETTER Y WITH CIRCUMFLEX
            case 0x0178:
                return 0xC859; // LATIN CAPITAL LETTER Y WITH DIAERESIS
            case 0x0179:
                return 0xC25A; // LATIN CAPITAL LETTER Z WITH ACUTE
            case 0x017A:
                return 0xC27A; // LATIN SMALL LETTER Z WITH ACUTE
            case 0x017B:
                return 0xC75A; // LATIN CAPITAL LETTER Z WITH DOT ABOVE
            case 0x017C:
                return 0xC77A; // LATIN SMALL LETTER Z WITH DOT ABOVE
            case 0x017D:
                return 0xCF5A; // LATIN CAPITAL LETTER Z WITH CARON
            case 0x017E:
                return 0xCF7A; // LATIN SMALL LETTER Z WITH CARON
            case 0x01F5:
                return 0xC267; // LATIN SMALL LETTER G WITH CEDILLA(4)
            case 0x02C7:
                return 0xCF20; // CARON
            case 0x02D8:
                return 0xC620; // BREVE
            case 0x02DA:
                return 0xCA20; // RING ABOVE
            case 0x02DB:
                return 0xCE20; // ogonek
            case 0x2015:
                return 0xD0; // 13/00 HORIZONTAL BAR
            case 0x2018:
                return 0xA9; // 10/09 LEFT SINGLE QUOTATION MARK
            case 0x2019:
                return 0xB9; // 11/09 RIGHT SINGLE QUOTATION MARK
            case 0x201C:
                return 0xAA; // 10/10 LEFT DOUBLE QUOTATION MARK
            case 0x201D:
                return 0xBA; // 11/10 RIGHT DOUBLE QUOTATION MARK
            case 0x2117:
                return 0xD2; // 13/02 REGISTERED SIGN
            case 0x2126:
                return 0xE0; // 14/00 OHM SIGN
            case 0x215B:
                return 0xDC; // 13/12 VULGAR FRACTION ONE EIGHTH
            case 0x215E:
                return 0xDF; // 13/15 VULGAR FRACTION SEVEN EIGHTHS
            case 0x2190:
                return 0xAC; // 10/12 LEFTWARDS ARROW
            case 0x2191:
                return 0xAD; // 10/13 UPWARDS ARROW
            case 0x2192:
                return 0xAE; // 10/14 RIGHTWARDS ARROW
            case 0x2193:
                return 0xAF; // 10/15 DOWNWARDS ARROW
            case 0x266A:
                return 0xD5; // 13/05 EIGHTH NOTE

            default:
                return 0x3F; // if no match, return question mark
        }
    }
}
