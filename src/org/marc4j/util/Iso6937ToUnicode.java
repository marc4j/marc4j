// $Id: Iso6937ToUnicode.java,v 1.1 2002/12/13 09:01:05 ypratter Exp $
/**
* Copyright (C) 2002 Bas  Peters  (mail@bpeters.com)
* Copyright (C) 2002 Yves Pratter (ypratter@club-internet.fr)
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
package org.marc4j.util;

import java.util.*;

/**
 * <p>A utility to convert ISO 6937 data to UCS/Unicode.</p>
 *
 * @author <a href="mailto:mail@bpeters.com">Bas Peters</a> 
 * @author <a href="mailto:ypratter@club-internet.fr">Yves Pratter</a> 
 * @version $Revision: 1.1 $
 */
public class Iso6937ToUnicode implements CharacterConverter {

    /**
     * <p>Converts ISO 6937 data to UCS/Unicode.</p>
     *
     * @param data the ISO 6937 data
     * @return {@link String} - the UCS/Unicode data
     */
    public String convert(String s) {
	return new String(convert(s.toCharArray()));
    }

    /**
     * <p>Converts ISO 6937 data to UCS/Unicode.</p>
     *
     * @param data the ISO 6937 data
     * @return char[] - the UCS/Unicode data
     */
    public char[] convert(char[] data) {
	StringBuffer sb = new StringBuffer();

		for(int i=0;i < data.length;i++) {
            char c = data[i];
            int len = data.length;
            if (isAscii(c))
                sb.append(c);
            else if (isCombining(c) && hasNext(i,len)) {
                char d = getCombiningChar(c*256 + data[i + 1]);
                if(d != 0) {
                    sb.append(d);
                    i++;
                } else {
                    sb.append(getChar(c));
                }
            } else
                sb.append(getChar(c));
        }
        return sb.toString().toCharArray();
    }

    private boolean hasNext(int pos, int len) {
	if (pos < (len -1))
	    return true;
	return false;
    }

    private boolean isAscii(int i) {
	if (i >= 0x00 && i <= 0x7F)
	    return true;
	return false;
    }

    private boolean isCombining(int i) {
	if (i >= 0xC0 && i <= 0xDF)
	    return true;
	return false;
    }

    // Source : http://anubis.dkuug.dk/JTC1/SC2/WG3/docs/6937cd.pdf
	private char getChar(int i) {
        switch(i) {
            case 0xA0: return 0x00A0;  // 10/00 NO-BREAK SPACE
            case 0xA1: return 0x00A1;  // 10/01 INVERTED EXCLAMATION MARK
            case 0xA2: return 0x00A2;  // 10/02 CENT SIGN
            case 0xA3: return 0x00A3;  // 10/03 POUND SIGN
									   // 10/04 (This position shall not be used)
            case 0xA5: return 0x00A5;  // 10/05 YEN SIGN
									   // 10/06 (This position shall not be used)
            case 0xA7: return 0x00A7;  // 10/07 SECTION SIGN
            case 0xA8: return 0x00A4;  // 10/08 CURRENCY SIGN
            case 0xA9: return 0x2018;  // 10/09 LEFT SINGLE QUOTATION MARK
            case 0xAA: return 0x201C;  // 10/10 LEFT DOUBLE QUOTATION MARK
            case 0xAB: return 0x00AB;  // 10/11 LEFT-POINTING DOUBLE ANGLE QUOTATION MARK
            case 0xAC: return 0x2190;  // 10/12 LEFTWARDS ARROW
            case 0xAD: return 0x2191;  // 10/13 UPWARDS ARROW
            case 0xAE: return 0x2192;  // 10/14 RIGHTWARDS ARROW
            case 0xAF: return 0x2193;  // 10/15 DOWNWARDS ARROW

            case 0xB0: return 0x00B0;  // 11/00 DEGREE SIGN
            case 0xB1: return 0x00B1;  // 11/01 PLUS-MINUS SIGN
            case 0xB2: return 0x00B2;  // 11/02 SUPERSCRIPT TWO
            case 0xB3: return 0x00B3;  // 11/03 SUPERSCRIPT THREE
            case 0xB4: return 0x00D7;  // 11/04 MULTIPLICATION SIGN
            case 0xB5: return 0x00B5;  // 11/05 MICRO SIGN
            case 0xB6: return 0x00B6;  // 11/06 PILCROW SIGN
            case 0xB7: return 0x00B7;  // 11/07 MIDDLE DOT
            case 0xB8: return 0x00F7;  // 11/08 DIVISION SIGN
            case 0xB9: return 0x2019;  // 11/09 RIGHT SINGLE QUOTATION MARK
            case 0xBA: return 0x201D;  // 11/10 RIGHT DOUBLE QUOTATION MARK
            case 0xBB: return 0x00BB;  // 11/11 RIGHT-POINTING DOUBLE ANGLE QUOTATION MARK
            case 0xBC: return 0x00BC;  // 11/12 VULGAR FRACTION ONE QUARTER
            case 0xBD: return 0x00BD;  // 11/13 VULGAR FRACTION ONE HALF
            case 0xBE: return 0x00BE;  // 11/14 VULGAR FRACTION THREE QUARTERS
            case 0xBF: return 0x00BF;  // 11/15 INVERTED QUESTION MARK


                                       // 4/0 to 5/15 diacritic characters

            case 0xD0: return 0x2015;  // 13/00 HORIZONTAL BAR
            case 0xD1: return 0x00B9;  // 13/01 SUPERSCRIPT ONE
            case 0xD2: return 0x2117;  // 13/02 REGISTERED SIGN
            case 0xD3: return 0x00A9;  // 13/03 COPYRIGHT SIGN
            case 0xD4: return 0x00AE;  // 13/04 TRADE MARK SIGN
            case 0xD5: return 0x266A;  // 13/05 EIGHTH NOTE
            case 0xD6: return 0x00AC;  // 13/06 NOT SIGN
            case 0xD7: return 0x00A6;  // 13/07 BROKEN BAR
                                       // 13/08 (This position shall not be used)
                                       // 13/09 (This position shall not be used)
                                       // 13/10 (This position shall not be used)
                                       // 13/11 (This position shall not be used)
            case 0xDC: return 0x215B;  // 13/12 VULGAR FRACTION ONE EIGHTH
            case 0xDF: return 0x215E;  // 13/15 VULGAR FRACTION SEVEN EIGHTHS

            case 0xE0: return 0x2126;  // 14/00 OHM SIGN
            case 0xE1: return 0x00C6;  // 14/01 LATIN CAPITAL LETTER AE
            case 0xE2: return 0x0110;  // 14/02 LATIN CAPITAL LETTER D WITH STROKE
            case 0xE3: return 0x00AA;  // 14/03 FEMININE ORDINAL INDICATOR
            case 0xE4: return 0x0126;  // 14/04 LATIN CAPITAL LETTER H WITH STROKE
                                       // 14/05 (This position shall not be used)
            case 0xE6: return 0x0132;  // 14/06 LATIN CAPITAL LIGATURE IJ
            case 0xE7: return 0x013F;  // 14/07 LATIN CAPITAL LETTER L WITH MIDDLE DOT
            case 0xE8: return 0x0141;  // 14/08 LATIN CAPITAL LETTER L WITH STROKE
            case 0xE9: return 0x00D8;  // 14/09 LATIN CAPITAL LETTER O WITH STROKE
            case 0xEA: return 0x0152;  // 14/10 LATIN CAPITAL LIGATURE OE
            case 0xEB: return 0x00BA;  // 14/11 MASCULINE ORDINAL INDICATOR
            case 0xEC: return 0x00DE;  // 14/12 LATIN CAPITAL LETTER THORN
            case 0xED: return 0x0166;  // 14/13 LATIN CAPITAL LETTER T WITH STROKE
            case 0xEE: return 0x014A;  // 14/14 LATIN CAPITAL LETTER ENG
            case 0xEF: return 0x0149;  // 14/15 LATIN SMALL LETTER N PRECEDED BY APOSTROPHE

            case 0xF0: return 0x0138;  // 15/00 LATIN SMALL LETTER KRA
            case 0xF1: return 0x00E6;  // 15/01 LATIN SMALL LETTER AE
            case 0xF2: return 0x0111;  // 15/02 LATIN SMALL LETTER D WITH STROKE
            case 0xF3: return 0x00F0;  // 15/03 LATIN SMALL LETTER ETH
            case 0xF4: return 0x0127;  // 15/04 LATIN SMALL LETTER H WITH STROKE
            case 0xF5: return 0x0131;  // 15/05 LATIN SMALL LETTER DOTLESS I
            case 0xF6: return 0x0133;  // 15/06 LATIN SMALL LIGATURE IJ
            case 0xF7: return 0x0140;  // 15/07 LATIN SMALL LETTER L WITH MIDDLE DOT
            case 0xF8: return 0x0142;  // 15/08 LATIN SMALL LETTER L WITH STROKE
            case 0xF9: return 0x00F8;  // 15/09 LATIN SMALL LETTER O WITH STROKE
            case 0xFA: return 0x0153;  // 15/10 LATIN SMALL LIGATURE OE
            case 0xFB: return 0x00DF;  // 15/11 LATIN SMALL LETTER SHARP S
            case 0xFC: return 0x00FE;  // 15/12 LATIN SMALL LETTER THORN
            case 0xFD: return 0x0167;  // 15/13 LATIN SMALL LETTER T WITH STROKE
            case 0xFE: return 0x014B;  // 15/14 LATIN SMALL LETTER ENG
            case 0xFF: return 0x00AD;  // 15/15 SOFT HYPHEN
        default :
            return (char)i;
        }
    }

    private char getCombiningChar(int i) {
        switch(i) {
                                         // 12/00 (This position shall not be used)

                                         // 12/01 non-spacing grave accent
            case 0xC141: return 0x00C0;  // LATIN CAPITAL LETTER A WITH GRAVE
            case 0xC145: return 0x00C8;  // CAPITAL E WITH GRAVE ACCENT
            case 0xC149: return 0x00CC;  // CAPITAL I WITH GRAVE ACCENT
            case 0xC14F: return 0x00D2;  // CAPITAL O WITH GRAVE ACCENT
            case 0xC155: return 0x00D9;  // CAPITAL U WITH GRAVE ACCENT
            case 0xC157: return 0x1E80;  // CAPITAL W WITH GRAVE       
            case 0xC159: return 0x1EF2;  // CAPITAL Y WITH GRAVE       
            case 0xC161: return 0x00E0;  // small   a with grave accent
            case 0xC165: return 0x00E8;  // small   e with grave accent
            case 0xC169: return 0x00EC;  // small   i with grave accent
            case 0xC16F: return 0x00F2;  // small   o with grave accent
            case 0xC175: return 0x00F9;  // small   u with grave accent
            case 0xC177: return 0x1E81;  // small   w with grave
            case 0xC179: return 0x1EF3;  // small   y with grave

                                         // 12/02 non-spacing acute accent
            case 0xC220: return 0x00B4;  // ACUTE ACCENT
            case 0xC241: return 0x00C1;  // LATIN CAPITAL LETTER A WITH ACUTE
            case 0xC243: return 0x0106;  // CAPITAL C WITH ACUTE ACCENT
            case 0xC245: return 0x00C9;  // CAPITAL E WITH ACUTE ACCENT
            case 0xC247: return 0x01F4;  // CAPITAL G WITH ACUTE       
            case 0xC249: return 0x00CD;  // CAPITAL I WITH ACUTE ACCENT
            case 0xC24B: return 0x1E30;  // CAPITAL K WITH ACUTE       
            case 0xC24C: return 0x0139;  // CAPITAL L WITH ACUTE ACCENT
            case 0xC24D: return 0x1E3E;  // CAPITAL M WITH ACUTE       
            case 0xC24E: return 0x0143;  // CAPITAL N WITH ACUTE ACCENT
            case 0xC24F: return 0x00D3;  // CAPITAL O WITH ACUTE ACCENT
            case 0xC250: return 0x1E54;  // CAPITAL P WITH ACUTE       
            case 0xC252: return 0x0154;  // CAPITAL R WITH ACUTE ACCENT
            case 0xC253: return 0x015A;  // CAPITAL S WITH ACUTE ACCENT
            case 0xC255: return 0x00DA;  // CAPITAL U WITH ACUTE ACCENT
            case 0xC257: return 0x1E82;  // CAPITAL W WITH ACUTE       
            case 0xC259: return 0x00DD;  // CAPITAL Y WITH ACUTE ACCENT
            case 0xC25A: return 0x0179;  // CAPITAL Z WITH ACUTE ACCENT
            case 0xC261: return 0x00E1;  // small   a with acute accent
            case 0xC263: return 0x0107;  // small   c with acute accent
            case 0xC265: return 0x00E9;  // small   e with acute accent
            case 0xC267: return 0x01F5;  // small   g with acute
            case 0xC269: return 0x00ED;  // small   i with acute accent
            case 0xC26B: return 0x1E31;  // small   k with acute
            case 0xC26C: return 0x013A;  // small   l with acute accent
            case 0xC26D: return 0x1E3F;  // small   m with acute
            case 0xC26E: return 0x0144;  // small   n with acute accent
            case 0xC26F: return 0x00F3;  // small   o with acute accent
            case 0xC270: return 0x1E55;  // small   p with acute
            case 0xC272: return 0x0155;  // small   r with acute accent
            case 0xC273: return 0x015B;  // small   s with acute accent
            case 0xC275: return 0x00FA;  // small   u with acute accent
            case 0xC277: return 0x1E83;  // small   w with acute
            case 0xC279: return 0x00FD;  // small   y with acute accent
            case 0xC27A: return 0x017A;  // small   z with acute accent
            case 0xC2E1: return 0x01FC;  // CAPITAL AE WITH ACUTE
            case 0xC2F1: return 0x01FD;  // small   ae with acute

                                         // 12/03 non-spacing circumflex accent
            case 0xC341: return 0x00C2;  // LATIN CAPITAL LETTER A WITH CIRCUMFLEX
            case 0xC343: return 0x0108;  // CAPITAL C WITH CIRCUMFLEX       
            case 0xC345: return 0x00CA;  // CAPITAL E WITH CIRCUMFLEX ACCENT
            case 0xC347: return 0x011C;  // CAPITAL G WITH CIRCUMFLEX       
            case 0xC348: return 0x0124;  // CAPITAL H WITH CIRCUMFLEX       
            case 0xC349: return 0x00CE;  // CAPITAL I WITH CIRCUMFLEX ACCENT
            case 0xC34A: return 0x0134;  // CAPITAL J WITH CIRCUMFLEX       
            case 0xC34F: return 0x00D4;  // CAPITAL O WITH CIRCUMFLEX ACCENT
            case 0xC353: return 0x015C;  // CAPITAL S WITH CIRCUMFLEX       
            case 0xC355: return 0x00DB;  // CAPITAL U WITH CIRCUMFLEX       
            case 0xC357: return 0x0174;  // CAPITAL W WITH CIRCUMFLEX       
            case 0xC359: return 0x0176;  // CAPITAL Y WITH CIRCUMFLEX       
            case 0xC35A: return 0x1E90;  // CAPITAL Z WITH CIRCUMFLEX       
            case 0xC361: return 0x00E2;  // small   a with circumflex accent
            case 0xC363: return 0x0109;  // small   c with circumflex
            case 0xC365: return 0x00EA;  // small   e with circumflex accent
            case 0xC367: return 0x011D;  // small   g with circumflex
            case 0xC368: return 0x0125;  // small   h with circumflex
            case 0xC369: return 0x00EE;  // small   i with circumflex accent
            case 0xC36A: return 0x0135;  // small   j with circumflex
            case 0xC36F: return 0x00F4;  // small   o with circumflex accent
            case 0xC373: return 0x015D;  // small   s with circumflex
            case 0xC375: return 0x00FB;  // small   u with circumflex
            case 0xC377: return 0x0175;  // small   w with circumflex
            case 0xC379: return 0x0177;  // small   y with circumflex
            case 0xC37A: return 0x1E91;  // small   z with circumflex

                                         // 12/04 non-spacing tilde
            case 0xC441: return 0x00C3;  // LATIN CAPITAL LETTER A WITH TILDE
            case 0xC445: return 0x1EBC;  // CAPITAL E WITH TILDE
            case 0xC449: return 0x0128;  // CAPITAL I WITH TILDE
            case 0xC44E: return 0x00D1;  // CAPITAL N WITH TILDE
            case 0xC44F: return 0x00D5;  // CAPITAL O WITH TILDE
            case 0xC455: return 0x0168;  // CAPITAL U WITH TILDE
            case 0xC456: return 0x1E7C;  // CAPITAL V WITH TILDE
            case 0xC459: return 0x1EF8;  // CAPITAL Y WITH TILDE
            case 0xC461: return 0x00E3;  // small   a with tilde
            case 0xC465: return 0x1EBD;  // small   e with tilde
            case 0xC469: return 0x0129;  // small   i with tilde
            case 0xC46E: return 0x00F1;  // small   n with tilde
            case 0xC46F: return 0x00F5;  // small   o with tilde
            case 0xC475: return 0x0169;  // small   u with tilde
            case 0xC476: return 0x1E7D;  // small   v with tilde
            case 0xC479: return 0x1EF9;  // small   y with tilde

                                         // 12/05 non-spacing macron
            case 0xC541: return 0x0100;  // LATIN CAPITAL LETTER A WITH MACRON
            case 0xC545: return 0x0112;  // CAPITAL E WITH MACRON 
            case 0xC547: return 0x1E20;  // CAPITAL G WITH MACRON 
            case 0xC549: return 0x012A;  // CAPITAL I WITH MACRON 
            case 0xC54F: return 0x014C;  // CAPITAL O WITH MACRON 
            case 0xC555: return 0x016A;  // CAPITAL U WITH MACRON 
            case 0xC561: return 0x0101;  // small   a with macron
            case 0xC565: return 0x0113;  // small   e with macron
            case 0xC567: return 0x1E21;  // small   g with macron
            case 0xC569: return 0x012B;  // small   i with macron
            case 0xC56F: return 0x014D;  // small   o with macron
            case 0xC575: return 0x016B;  // small   u with macron
            case 0xC5E1: return 0x01E2;  // CAPITAL AE WITH MACRON
            case 0xC5F1: return 0x01E3;  // small   ae with macron

                                         // 12/06 non-spacing breve
            case 0xC620: return 0x02D8;  // BREVE
            case 0xC641: return 0x0102;  // LATIN CAPITAL LETTER A WITH BREVE
            case 0xC645: return 0x0114;  // CAPITAL E WITH BREVE
            case 0xC647: return 0x011E;  // CAPITAL G WITH BREVE
            case 0xC649: return 0x012C;  // CAPITAL I WITH BREVE
            case 0xC64F: return 0x014E;  // CAPITAL O WITH BREVE
            case 0xC655: return 0x016C;  // CAPITAL U WITH BREVE
            case 0xC661: return 0x0103;  // small   a with breve
            case 0xC665: return 0x0115;  // small   e with breve
            case 0xC667: return 0x011F;  // small   g with breve
            case 0xC669: return 0x012D;  // small   i with breve
            case 0xC66F: return 0x014F;  // small   o with breve
            case 0xC675: return 0x016D;  // small   u with breve

                                         // 12/07 non-spacing dot above
            case 0xC742: return 0x1E02;  // CAPITAL B WITH DOT ABOVE
            case 0xC743: return 0x010A;  // CAPITAL C WITH DOT ABOVE
            case 0xC744: return 0x1E0A;  // CAPITAL D WITH DOT ABOVE
            case 0xC745: return 0x0116;  // CAPITAL E WITH DOT ABOVE
            case 0xC746: return 0x1E1E;  // CAPITAL F WITH DOT ABOVE
            case 0xC747: return 0x0120;  // CAPITAL G WITH DOT ABOVE
            case 0xC748: return 0x1E22;  // CAPITAL H WITH DOT ABOVE
            case 0xC749: return 0x0130;  // CAPITAL I WITH DOT ABOVE
            case 0xC74D: return 0x1E40;  // CAPITAL M WITH DOT ABOVE
            case 0xC74E: return 0x1E44;  // CAPITAL N WITH DOT ABOVE
            case 0xC750: return 0x1E56;  // CAPITAL P WITH DOT ABOVE
            case 0xC752: return 0x1E58;  // CAPITAL R WITH DOT ABOVE
            case 0xC753: return 0x1E60;  // CAPITAL S WITH DOT ABOVE
            case 0xC754: return 0x1E6A;  // CAPITAL T WITH DOT ABOVE
            case 0xC757: return 0x1E86;  // CAPITAL W WITH DOT ABOVE
            case 0xC758: return 0x1E8A;  // CAPITAL X WITH DOT ABOVE
            case 0xC759: return 0x1E8E;  // CAPITAL Y WITH DOT ABOVE
            case 0xC75A: return 0x017B;  // CAPITAL Z WITH DOT ABOVE
            case 0xC762: return 0x1E03;  // small   b with dot above
            case 0xC763: return 0x010B;  // small   c with dot above
            case 0xC764: return 0x1E0B;  // small   d with dot above
            case 0xC765: return 0x0117;  // small   e with dot above
            case 0xC766: return 0x1E1F;  // small   f with dot above
            case 0xC767: return 0x0121;  // small   g with dot above
            case 0xC768: return 0x1E23;  // small   h with dot above
            case 0xC76D: return 0x1E41;  // small   m with dot above
            case 0xC76E: return 0x1E45;  // small   n with dot above
            case 0xC770: return 0x1E57;  // small   p with dot above
            case 0xC772: return 0x1E59;  // small   r with dot above
            case 0xC773: return 0x1E61;  // small   s with dot above
            case 0xC774: return 0x1E6B;  // small   t with dot above
            case 0xC777: return 0x1E87;  // small   w with dot above
            case 0xC778: return 0x1E8B;  // small   x with dot above
            case 0xC779: return 0x1E8F;  // small   y with dot above
            case 0xC77A: return 0x017C;  // small   z with dot above

                                         // 12/08 non-spacing diaeresis
            case 0xC820: return 0x00A8;  // DIAERESIS
            case 0xC841: return 0x00C4;  // LATIN CAPITAL LETTER A WITH DIAERESIS
            case 0xC845: return 0x00CB;  // CAPITAL E WITH DIAERESIS
            case 0xC848: return 0x1E26;  // CAPITAL H WITH DIAERESIS
            case 0xC849: return 0x00CF;  // CAPITAL I WITH DIAERESIS
            case 0xC84F: return 0x00D6;  // CAPITAL O WITH DIAERESIS
            case 0xC855: return 0x00DC;  // CAPITAL U WITH DIAERESIS
            case 0xC857: return 0x1E84;  // CAPITAL W WITH DIAERESIS
            case 0xC858: return 0x1E8C;  // CAPITAL X WITH DIAERESIS
            case 0xC859: return 0x0178;  // CAPITAL Y WITH DIAERESIS
            case 0xC861: return 0x00E4;  // small   a with diaeresis
            case 0xC865: return 0x00EB;  // small   e with diaeresis
            case 0xC868: return 0x1E27;  // small   h with diaeresis
            case 0xC869: return 0x00EF;  // small   i with diaeresis
            case 0xC86F: return 0x00F6;  // small   o with diaeresis
            case 0xC874: return 0x1E97;  // small   t with diaeresis
            case 0xC875: return 0x00FC;  // small   u with diaeresis
            case 0xC877: return 0x1E85;  // small   w with diaeresis
            case 0xC878: return 0x1E8D;  // small   x with diaeresis
            case 0xC879: return 0x00FF;  // small   y with diaeresis

                                         // 12/09 (This position shall not be used)

                                         // 12/10 non-spacing ring above
            case 0xCA41: return 0x00C5;  // LATIN CAPITAL LETTER A WITH RING ABOVE
            case 0xCAAD: return 0x016E;  // CAPITAL U WITH RING ABOVE
            case 0xCA61: return 0x00E5;  // small   a with ring above
            case 0xCA75: return 0x016F;  // small   u with ring above
            case 0xCA77: return 0x1E98;  // small   w with ring above
            case 0xCA79: return 0x1E99;  // small   y with ring above

                                         // 12/11 non-spacing cedilla
            case 0xCB20: return 0x00B8;  // CEDILLA
            case 0xCB43: return 0x00C7;  // CAPITAL C WITH CEDILLA
            case 0xCB44: return 0x1E10;  // CAPITAL D WITH CEDILLA               
            case 0xCB47: return 0x0122;  // CAPITAL G WITH CEDILLA               
            case 0xCB48: return 0x1E28;  // CAPITAL H WITH CEDILLA               
            case 0xCB4B: return 0x0136;  // CAPITAL K WITH CEDILLA               
            case 0xCB4C: return 0x013B;  // CAPITAL L WITH CEDILLA               
            case 0xCB4E: return 0x0145;  // CAPITAL N WITH CEDILLA               
            case 0xCB52: return 0x0156;  // CAPITAL R WITH CEDILLA               
            case 0xCB53: return 0x015E;  // CAPITAL S WITH CEDILLA               
            case 0xCB54: return 0x0162;  // CAPITAL T WITH CEDILLA               
            case 0xCB63: return 0x00E7;  // small   c with cedilla
            case 0xCB64: return 0x1E11;  // small   d with cedilla
            case 0xCB67: return 0x0123;  // small   g with cedilla
            case 0xCB68: return 0x1E29;  // small   h with cedilla
            case 0xCB6B: return 0x0137;  // small   k with cedilla
            case 0xCB6C: return 0x013C;  // small   l with cedilla
            case 0xCB6E: return 0x0146;  // small   n with cedilla
            case 0xCB72: return 0x0157;  // small   r with cedilla
            case 0xCB73: return 0x015F;  // small   s with cedilla
            case 0xCB74: return 0x0163;  // small   t with cedilla

                                         // 12/12 (This position shall not be used)

                                         // 12/13 non-spacing double acute accent
            case 0xCD4F: return 0x0150;  // CAPITAL O WITH DOUBLE ACUTE
            case 0xCD55: return 0x0170;  // CAPITAL U WITH DOUBLE ACUTE
            case 0xCD6F: return 0x0151;  // small   o with double acute
            case 0xCD75: return 0x0171;  // small   u with double acute

                                         // 12/14 non-spacing ogonek
            case 0xCE20: return 0x02DB;  // ogonek
            case 0xCE41: return 0x0104;  // LATIN CAPITAL LETTER A WITH OGONEK
            case 0xCE45: return 0x0118;  // CAPITAL E WITH OGONEK 
            case 0xCE49: return 0x012E;  // CAPITAL I WITH OGONEK 
            case 0xCE4F: return 0x01EA;  // CAPITAL O WITH OGONEK 
            case 0xCE55: return 0x0172;  // CAPITAL U WITH OGONEK 
            case 0xCE61: return 0x0105;  // small   a with ogonek
            case 0xCE65: return 0x0119;  // small   e with ogonek
            case 0xCE69: return 0x012F;  // small   i with ogonek
            case 0xCE6F: return 0x01EB;  // small   o with ogonek
            case 0xCE75: return 0x0173;  // small   u with ogonek

                                         // 12/15 non-spacing caron
            case 0xCF20: return 0x02C7;  // CARON
            case 0xCF41: return 0x01CD;  // CAPITAL A WITH CARON
            case 0xCF43: return 0x010C;  // CAPITAL C WITH CARON
            case 0xCF44: return 0x010E;  // CAPITAL D WITH CARON
            case 0xCF45: return 0x011A;  // CAPITAL E WITH CARON
            case 0xCF47: return 0x01E6;  // CAPITAL G WITH CARON
            case 0xCF49: return 0x01CF;  // CAPITAL I WITH CARON
            case 0xCF4B: return 0x01E8;  // CAPITAL K WITH CARON
            case 0xCF4C: return 0x013D;  // CAPITAL L WITH CARON
            case 0xCF4E: return 0x0147;  // CAPITAL N WITH CARON
            case 0xCF4F: return 0x01D1;  // CAPITAL O WITH CARON
            case 0xCF52: return 0x0158;  // CAPITAL R WITH CARON
            case 0xCF53: return 0x0160;  // CAPITAL S WITH CARON
            case 0xCF54: return 0x0164;  // CAPITAL T WITH CARON
            case 0xCF55: return 0x01D3;  // CAPITAL U WITH CARON
            case 0xCF5A: return 0x017D;  // CAPITAL Z WITH CARON
            case 0xCF61: return 0x01CE;  // small   a with caron
            case 0xCF63: return 0x010D;  // small   c with caron
            case 0xCF64: return 0x010F;  // small   d with caron
            case 0xCF65: return 0x011B;  // small   e with caron
            case 0xCF67: return 0x01E7;  // small   g with caron
            case 0xCF69: return 0x01D0;  // small   i with caron
            case 0xCF6A: return 0x01F0;  // small   j with caron
            case 0xCF6B: return 0x01E9;  // small   k with caron
            case 0xCF6C: return 0x013E;  // small   l with caron
            case 0xCF6E: return 0x0148;  // small   n with caron
            case 0xCF6F: return 0x01D2;  // small   o with caron
            case 0xCF72: return 0x0159;  // small   r with caron
            case 0xCF73: return 0x0161;  // small   s with caron
            case 0xCF74: return 0x0165;  // small   t with caron
            case 0xCF75: return 0x01D4;  // small   u with caron
            case 0xCF7A: return 0x017E;  // small   z with caron                            
        default :
            return 0;
        }
    }
}
