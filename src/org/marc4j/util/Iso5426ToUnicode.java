// $Id: Iso5426ToUnicode.java,v 1.2 2002/12/05 17:03:44 ypratter Exp $
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
 * <p>A utility to convert UNIMARC data to UCS/Unicode.</p>
 *
 * @author <a href="mailto:mail@bpeters.com">Bas Peters</a> 
 * @author <a href="mailto:ypratter@club-internet.fr">Yves Pratter</a> 
 * @version $Revision: 1.2 $
 */
public class Iso5426ToUnicode {

    /**
     * <p>Converts UNIMARC (ISO 5426 charset) data to UCS/Unicode.</p>
     *
     * @param data the UNIMARC data
     * @return {@link String} - the UCS/Unicode data
     */
    public static String convert(String s) {
	return new String(convert(s.toCharArray()));
    }

    /**
     * <p>Converts UNIMARC data to UCS/Unicode.</p>
     *
     * @param data the UNIMARC data
     * @return char[] - the UCS/Unicode data
     */
    public static char[] convert(char[] data) {
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

    private static boolean hasNext(int pos, int len) {
	if (pos < (len -1))
	    return true;
	return false;
    }

    private static boolean isAscii(int i) {
	if (i > 0x00 && i < 0x7F)
	    return true;
	return false;
    }

    private static boolean isCombining(int i) {
//	if (i > 0xE0 && i < 0xFF)
	if (i > 0xC0 && i < 0xFF)
	    return true;
	return false;
    }

    private static char getChar(int i) {
	switch(i) {
        case 0x8D: return 0x200D;  // zero width joiner
        case 0x8E: return 0x200C;  // zero width non-joiner
        case 0xA1: return 0x0141;  // capital L with stroke
        case 0xA2: return 0x00D8;  // capital O with oblique stroke
        case 0xA3: return 0x0110;  // capital D with stroke
        case 0xA4: return 0x00DE;  // capital Icelandic letter Thorn
     // case 0xA5: return 0x00C6;  // capital diphthong A with E
        case 0xA6: return 0x0152;  // capital ligature OE
        case 0xA7: return 0x02B9;  // modified letter prime
        case 0xA8: return 0x00B7;  // middle dot
        case 0xA9: return 0x266D;  // music flat sign
        case 0xAA: return 0x00AE;  // registered trade mark sign
        case 0xAB: return 0x00B1;  // plus-minus sign
        case 0xAC: return 0x01A0;  // capital O with horn
        case 0xAD: return 0x01AF;  // capital U with horn
        case 0xAE: return 0x02BE;  // modifier letter right half ring
        case 0xB0: return 0x02BF;  // modifier letter turned comma
        case 0xB1: return 0x0142;  // small l with stroke
        case 0xB2: return 0x00F8;  // small o with oblique stroke
        case 0xB3: return 0x0111;  // small D with stroke
        case 0xB4: return 0x00FE;  // small Icelandic letter Thorn
     // case 0xB5: return 0x00E6;  // small diphthong a with e
        case 0xB6: return 0x0153;  // small ligature OE
        case 0xB7: return 0x02BA;  // modified letter double prime
        case 0xB8: return 0x0131;  // small dotless i
        case 0xB9: return 0x00A3;  // pound sign
        case 0xBA: return 0x00F0;  // small Icelandic letter Eth
        case 0xBC: return 0x01A1;  // small o with horn
        case 0xBD: return 0x01B0;  // small u with horn
        case 0xC0: return 0x00B0;  // degree sign, ring above
        case 0xC1: return 0x2113;  // script small l
     // case 0xC2: return 0x2117;  // sound recording copyright
        case 0xC3: return 0x00A9;  // copyright sign
        case 0xC4: return 0x266F;  // music sharp sign
        case 0xC5: return 0x00BF;  // inverted question mark
        case 0xC6: return 0x00A1;  // inverted exclamation mark
        case 0xE0: return 0x0309;  // hook above
        case 0xE1: return 0x0300;  // grave accent
        case 0xE2: return 0x0301;  // acute accent
        case 0xE3: return 0x0302;  // circumflex accent
        case 0xE4: return 0x0303;  // tilde
        case 0xE5: return 0x0304;  // combining macron
        case 0xE6: return 0x0306;  // breve
        case 0xE7: return 0x0307;  // dot above
        case 0xE8: return 0x0308;  // combining diaeresis
        case 0xE9: return 0x030C;  // caron
        case 0xEA: return 0x030A;  // ring above
        case 0xEB: return 0xFE20;  // ligature left half
        case 0xEC: return 0xFE21;  // ligature right half
        case 0xED: return 0x0315;  // comma above right
        case 0xEE: return 0x030B;  // double acute accent
        case 0xEF: return 0x0310;  // candrabindu
        case 0xF0: return 0x0327;  // combining cedilla
     // case 0xF1: return 0x0328;  // ogonek
        case 0xF2: return 0x0323;  // dot below
        case 0xF3: return 0x0324;  // diaeresis below
        case 0xF4: return 0x0325;  // ring below
        case 0xF5: return 0x0333;  // double low line
        case 0xF6: return 0x0332;  // low line (= line below?)
        case 0xF7: return 0x0326;  // comma below
        case 0xF8: return 0x031C;  // combining half ring below
        case 0xF9: return 0x032E;  // breve below
        case 0xFA: return 0xFE22;  // double tilde left half
        case 0xFB: return 0xFE23;  // double tilde right half
        case 0xFE: return 0x0313;  // comma above
	default :
	    return (char)i;
	}
    }

    private static char getCombiningChar(int i) {
	switch(i) {
	case 0xE041: return 0x1EA2;  // capital a with hook above

	case 0xC386: return 0x00C6;  // capital diphthong A with E
    case 0xC3A6: return 0x00E6;  // small   diphthong a with e

    //  case 0xE045: return 0x1EBA;  // capital e with hook above
    //  case 0xE049: return 0x1EC8;  // capital i with hook above
    //  case 0xE04F: return 0x1ECE;  // capital o with hook above
    //  case 0xE055: return 0x1EE6;  // capital u with hook above
    //  case 0xE059: return 0x1EF6;  // capital y with hook above
    //  case 0xE061: return 0x1EA3;  // small a with hook above
    //  case 0xE065: return 0x1EBB;  // small e with hook above
    //  case 0xE069: return 0x1EC9;  // small i with hook above
    //  case 0xE06F: return 0x1ECF;  // small o with hook above
    //  case 0xE075: return 0x1EE7;  // small u with hook above
    //  case 0xE079: return 0x1EF7;  // small y with hook above

        case 0xC141: return 0x00C0;  // capital A with grave accent
        case 0xC145: return 0x00C8;  // capital E with grave accent
        case 0xC149: return 0x00CC;  // capital I with grave accent
        case 0xC14F: return 0x00D2;  // capital O with grave accent
        case 0xC155: return 0x00D9;  // capital U with grave accent
        
	//	case 0xE157: return 0x1E80;  // capital W with grave
    //  case 0xE159: return 0x1EF2;  // capital y with grave

        case 0xC161: return 0x00E0;  // small a with grave accent
        case 0xC165: return 0x00E8;  // small e with grave accent
        case 0xC169: return 0x00EC;  // small i with grave accent
        case 0xC16F: return 0x00F2;  // small o with grave accent
        case 0xC175: return 0x00F9;  // small u with grave accent
    //  case 0xE177: return 0x1E81;  // small w with grave
    //  case 0xE179: return 0x1EF3;  // small y with grave
        case 0xC241: return 0x00C1;  // capital A with acute accent
        case 0xC243: return 0x0106;  // capital C with acute accent
        case 0xC245: return 0x00C9;  // capital E with acute accent
    //  case 0xE247: return 0x01F4;  // capital g with acute
        case 0xC249: return 0x00CD;  // capital I with acute accent
    //  case 0xE24B: return 0x1E30;  // capital k with acute
        case 0xC24C: return 0x0139;  // capital L with acute accent
    //  case 0xE24D: return 0x1E3E;  // capital m with acute
        case 0xC24E: return 0x0143;  // capital N with acute accent
        case 0xC24F: return 0x00D3;  // capital O with acute accent
    //  case 0xE250: return 0x1E54;  // capital p with acute
        case 0xC252: return 0x0154;  // capital R with acute accent
        case 0xC253: return 0x015A;  // capital S with acute accent
        case 0xC255: return 0x00DA;  // capital U with acute accent
    //  case 0xE257: return 0x1E82;  // capital w with acute
        case 0xC259: return 0x00DD;  // capital Y with acute accent
        case 0xC25A: return 0x0179;  // capital Z with acute accent
        case 0xC261: return 0x00E1;  // small a with acute accent
        case 0xC263: return 0x0107;  // small c with acute accent
        case 0xC265: return 0x00E9;  // small e with acute accent
    //  case 0xE267: return 0x01F5;  // small g with acute
        case 0xC269: return 0x00ED;  // small i with acute accent
    //  case 0xE26B: return 0x1E31;  // small k with acute
        case 0xC26C: return 0x013A;  // small l with acute accent
    //  case 0xE26D: return 0x1E3F;  // small m with acute
        case 0xC26E: return 0x0144;  // small n with acute accent
        case 0xC26F: return 0x00F3;  // small o with acute accent
    //  case 0xE270: return 0x1E55;  // small p with acute
        case 0xC272: return 0x0155;  // small r with acute accent
        case 0xC273: return 0x015B;  // small s with acute accent
        case 0xC275: return 0x00FA;  // small u with acute accent
    //  case 0xE277: return 0x1E83;  // small w with acute
        case 0xC279: return 0x00FD;  // small y with acute accent
        case 0xC27A: return 0x017A;  // small z with acute accent

    //  case 0xE2A5: return 0x01FC;  // capital AE with acute
    //  case 0xE2B5: return 0x01FD;  // small ae with acute

        case 0xC341: return 0x00C2;  // capital A with circumflex accent
        case 0xC343: return 0x0108;  // capital C with circumflex
        case 0xC345: return 0x00CA;  // capital E with circumflex accent
        case 0xC347: return 0x011C;  // capital G with circumflex
        case 0xC348: return 0x0124;  // capital H with circumflex
        case 0xC349: return 0x00CE;  // capital I with circumflex accent
        case 0xC34A: return 0x0134;  // capital J with circumflex
        case 0xC34F: return 0x00D4;  // capital O with circumflex accent
        case 0xC353: return 0x015C;  // capital S with circumflex
        case 0xC355: return 0x00DB;  // capital U with circumflex
        case 0xC357: return 0x0174;  // capital W with circumflex
        case 0xC359: return 0x0176;  // capital Y with circumflex
     // case 0xE35A: return 0x1E90;  // capital Z with circumflex
        case 0xC361: return 0x00E2;  // small a with circumflex accent
        case 0xC363: return 0x0109;  // small c with circumflex
        case 0xC365: return 0x00EA;  // small e with circumflex accent
        case 0xC367: return 0x011D;  // small g with circumflex
        case 0xC368: return 0x0125;  // small h with circumflex
        case 0xC369: return 0x00EE;  // small i with circumflex accent
        case 0xC36A: return 0x0135;  // small j with circumflex
        case 0xC36F: return 0x00F4;  // small o with circumflex accent
        case 0xC373: return 0x015D;  // small s with circumflex
        case 0xC375: return 0x00FB;  // small u with circumflex
        case 0xC377: return 0x0175;  // small w with circumflex
        case 0xC379: return 0x0177;  // small y with circumflex
     // case 0xE37A: return 0x1E91;  // small z with circumflex

        case 0xE441: return 0x00C3;  // capital A with tilde
        case 0xE445: return 0x1EBC;  // capital e with tilde
        case 0xE449: return 0x0128;  // capital i with tilde
        case 0xE44E: return 0x00D1;  // capital N with tilde
        case 0xE44F: return 0x00D5;  // capital O with tilde
        case 0xE455: return 0x0168;  // capital u with tilde
        case 0xE456: return 0x1E7C;  // capital v with tilde
        case 0xE459: return 0x1EF8;  // capital y with tilde
        case 0xE461: return 0x00E3;  // small a with tilde
        case 0xE465: return 0x1EBD;  // small e with tilde
        case 0xE469: return 0x0129;  // small i with tilde
        case 0xE46E: return 0x00F1;  // small n with tilde
        case 0xE46F: return 0x00F5;  // small o with tilde
        case 0xE475: return 0x0169;  // small u with tilde
        case 0xE476: return 0x1E7D;  // small v with tilde
        case 0xE479: return 0x1EF9;  // small y with tilde

        case 0xE541: return 0x0100;  // capital a with macron
        case 0xE545: return 0x0112;  // capital e with macron
        case 0xE547: return 0x1E20;  // capital g with macron
        case 0xE549: return 0x012A;  // capital i with macron
        case 0xE54F: return 0x014C;  // capital o with macron
        case 0xE555: return 0x016A;  // capital u with macron
        case 0xE561: return 0x0101;  // small a with macron
        case 0xE565: return 0x0113;  // small e with macron
        case 0xE567: return 0x1E21;  // small g with macron
        case 0xE569: return 0x012B;  // small i with macron
        case 0xE56F: return 0x014D;  // small o with macron
        case 0xE575: return 0x016B;  // small u with macron
        case 0xE5A5: return 0x01E2;  // capital ae with macron
        case 0xE5B5: return 0x01E3;  // small ae with macron

        case 0xE641: return 0x0102;  // capital A with breve
        case 0xE645: return 0x0114;  // capital e with breve
        case 0xE647: return 0x011E;  // capital g with breve
        case 0xE649: return 0x012C;  // capital i with breve
        case 0xE64F: return 0x014E;  // capital o with breve
        case 0xE655: return 0x016C;  // capital u with breve
        case 0xE661: return 0x0103;  // small a with breve
        case 0xE665: return 0x0115;  // small e with breve
        case 0xE667: return 0x011F;  // small g with breve
        case 0xE669: return 0x012D;  // small i with breve
        case 0xE66F: return 0x014F;  // small o with breve
        case 0xE675: return 0x016D;  // small u with breve

        case 0xE742: return 0x1E02;  // capital b with dot above
        case 0xE743: return 0x010A;  // capital c with dot above
        case 0xE744: return 0x1E0A;  // capital d with dot above
        case 0xE745: return 0x0116;  // capital e with dot above
        case 0xE746: return 0x1E1E;  // capital f with dot above
        case 0xE747: return 0x0120;  // capital g with dot above
        case 0xE748: return 0x1E22;  // capital h with dot above
        case 0xE749: return 0x0130;  // capital i with dot above
        case 0xE74D: return 0x1E40;  // capital m with dot above
        case 0xE74E: return 0x1E44;  // capital n with dot above
        case 0xE750: return 0x1E56;  // capital p with dot above
        case 0xE752: return 0x1E58;  // capital r with dot above
        case 0xE753: return 0x1E60;  // capital s with dot above
        case 0xE754: return 0x1E6A;  // capital t with dot above
        case 0xE757: return 0x1E86;  // capital w with dot above
        case 0xE758: return 0x1E8A;  // capital x with dot above
        case 0xE759: return 0x1E8E;  // capital y with dot above
        case 0xE75A: return 0x017B;  // capital Z with dot above
        case 0xE762: return 0x1E03;  // small b with dot above
        case 0xE763: return 0x010B;  // small c with dot above
        case 0xE764: return 0x1E0B;  // small d with dot above
        case 0xE765: return 0x0117;  // small e with dot above
        case 0xE766: return 0x1E1F;  // small f with dot above
        case 0xE767: return 0x0121;  // small g with dot above
        case 0xE768: return 0x1E23;  // small h with dot above
        case 0xE76D: return 0x1E41;  // small m with dot above
        case 0xE76E: return 0x1E45;  // small n with dot above
        case 0xE770: return 0x1E57;  // small p with dot above
        case 0xE772: return 0x1E59;  // small r with dot above
        case 0xE773: return 0x1E61;  // small s with dot above
        case 0xE774: return 0x1E6B;  // small t with dot above
        case 0xE777: return 0x1E87;  // small w with dot above
        case 0xE778: return 0x1E8B;  // small x with dot above
        case 0xE779: return 0x1E8F;  // small y with dot above
        case 0xE77A: return 0x017C;  // small z with dot above

        case 0xE841: return 0x00C4;  // capital A with diaeresis
        case 0xE845: return 0x00CB;  // capital E with diaeresis
        case 0xE848: return 0x1E26;  // capital h with diaeresis
        case 0xE849: return 0x00CF;  // capital I with diaeresis
        case 0xE84F: return 0x00D6;  // capital O with diaeresis
        case 0xE855: return 0x00DC;  // capital U with diaeresis
        case 0xE857: return 0x1E84;  // capital w with diaeresis
        case 0xE858: return 0x1E8C;  // capital x with diaeresis
        case 0xE859: return 0x0178;  // capital y with diaeresis
        case 0xE861: return 0x00E4;  // small a with diaeresis
        case 0xE865: return 0x00EB;  // small e with diaeresis
        case 0xE868: return 0x1E27;  // small h with diaeresis
        case 0xE869: return 0x00EF;  // small i with diaeresis
        case 0xE86F: return 0x00F6;  // small o with diaeresis
        case 0xE874: return 0x1E97;  // small t with diaeresis
        case 0xE875: return 0x00FC;  // small u with diaeresis
        case 0xE877: return 0x1E85;  // small w with diaeresis
        case 0xE878: return 0x1E8D;  // small x with diaeresis
        case 0xE879: return 0x00FF;  // small y with diaeresis

        case 0xE941: return 0x01CD;  // capital a with caron
        case 0xE943: return 0x010C;  // capital C with caron
        case 0xE944: return 0x010E;  // capital D with caron
        case 0xE945: return 0x011A;  // capital E with caron
        case 0xE947: return 0x01E6;  // capital g with caron
        case 0xE949: return 0x01CF;  // capital i with caron
        case 0xE94B: return 0x01E8;  // capital k with caron
        case 0xE94C: return 0x013D;  // capital L with caron
        case 0xE94E: return 0x0147;  // capital N with caron
        case 0xE94F: return 0x01D1;  // capital o with caron
        case 0xE952: return 0x0158;  // capital R with caron
        case 0xE953: return 0x0160;  // capital S with caron
        case 0xE954: return 0x0164;  // capital T with caron
        case 0xE955: return 0x01D3;  // capital u with caron
        case 0xE95A: return 0x017D;  // capital Z with caron
        case 0xE961: return 0x01CE;  // small a with caron
        case 0xE963: return 0x010D;  // small c with caron
        case 0xE964: return 0x010F;  // small d with caron
        case 0xE965: return 0x011B;  // small e with caron
        case 0xE967: return 0x01E7;  // small g with caron
        case 0xE969: return 0x01D0;  // small i with caron
        case 0xE96A: return 0x01F0;  // small j with caron
        case 0xE96B: return 0x01E9;  // small k with caron
        case 0xE96C: return 0x013E;  // small l with caron
        case 0xE96E: return 0x0148;  // small n with caron
        case 0xE96F: return 0x01D2;  // small o with caron
        case 0xE972: return 0x0159;  // small r with caron
        case 0xE973: return 0x0161;  // small s with caron
        case 0xE974: return 0x0165;  // small t with caron
        case 0xE975: return 0x01D4;  // small u with caron
        case 0xE97A: return 0x017E;  // small z with caron

        case 0xEA41: return 0x00C5;  // capital A with ring above
        case 0xEA61: return 0x00E5;  // small a with ring above
        case 0xEA75: return 0x016F;  // small u with ring above
        case 0xEA77: return 0x1E98;  // small w with ring above
        case 0xEA79: return 0x1E99;  // small y with ring above
        case 0xEAAD: return 0x016E;  // capital U with ring above

        case 0xEE4F: return 0x0150;  // capital O with double acute
        case 0xEE55: return 0x0170;  // capital U with double acute
        case 0xEE6F: return 0x0151;  // small o with double acute
        case 0xEE75: return 0x0171;  // small u with double acute

        case 0xC520: return 0x00B8;  // cedilla
     // case 0xCB43: return 0x00C7;  // capital C with cedilla !! ISO 6937 !!
        case 0xD043: return 0x00C7;  // capital C with cedilla !! UNIMARC !!
        case 0xF044: return 0x1E10;  // capital d with cedilla
        case 0xF047: return 0x0122;  // capital g with cedilla
        case 0xF048: return 0x1E28;  // capital h with cedilla
        case 0xF04B: return 0x0136;  // capital k with cedilla
        case 0xF04C: return 0x013B;  // capital l with cedilla
        case 0xF04E: return 0x0145;  // capital n with cedilla
        case 0xF052: return 0x0156;  // capital r with cedilla
        case 0xF053: return 0x015E;  // capital S with cedilla
        case 0xF054: return 0x0162;  // capital T with cedilla
     // case 0xCB63: return 0x00E7;  // small c with cedilla    !! ISO 6937 !!
        case 0xD063: return 0x00E7;  // small c with cedilla    !! UNIMARC !!
        case 0xF064: return 0x1E11;  // small d with cedilla
        case 0xF067: return 0x0123;  // small g with cedilla
        case 0xF068: return 0x1E29;  // small h with cedilla
        case 0xF06B: return 0x0137;  // small k with cedilla
        case 0xF06C: return 0x013C;  // small l with cedilla
        case 0xF06E: return 0x0146;  // small n with cedilla
        case 0xF072: return 0x0157;  // small r with cedilla
        case 0xF073: return 0x015F;  // small s with cedilla
        case 0xF074: return 0x0163;  // small t with cedilla

        case 0xCE20: return 0x0328;  // ogonek ?????????????????????????????????? 02DB ?
        case 0xF141: return 0x0104;  // capital A with ogonek
        case 0xF145: return 0x0118;  // capital E with ogonek
        case 0xF149: return 0x012E;  // capital i with ogonek
        case 0xF14F: return 0x01EA;  // capital o with ogonek
        case 0xF155: return 0x0172;  // capital u with ogonek
        case 0xF161: return 0x0105;  // small a with ogonek
        case 0xF165: return 0x0119;  // small e with ogonek
        case 0xF169: return 0x012F;  // small i with ogonek
        case 0xF16F: return 0x01EB;  // small o with ogonek
        case 0xF175: return 0x0173;  // small u with ogonek

        case 0xF241: return 0x1EA0;  // capital a with dot below
        case 0xF242: return 0x1E04;  // capital b with dot below
        case 0xF244: return 0x1E0C;  // capital d with dot below
        case 0xF245: return 0x1EB8;  // capital e with dot below
        case 0xF248: return 0x1E24;  // capital h with dot below
        case 0xF249: return 0x1ECA;  // capital i with dot below
        case 0xF24B: return 0x1E32;  // capital k with dot below
        case 0xF24C: return 0x1E36;  // capital l with dot below
        case 0xF24D: return 0x1E42;  // capital m with dot below
        case 0xF24E: return 0x1E46;  // capital n with dot below
        case 0xF24F: return 0x1ECC;  // capital o with dot below
        case 0xF252: return 0x1E5A;  // capital r with dot below
        case 0xF253: return 0x1E62;  // capital s with dot below
        case 0xF254: return 0x1E6C;  // capital t with dot below
        case 0xF255: return 0x1EE4;  // capital u with dot below
        case 0xF256: return 0x1E7E;  // capital v with dot below
        case 0xF257: return 0x1E88;  // capital w with dot below
        case 0xF259: return 0x1EF4;  // capital y with dot below
        case 0xF25A: return 0x1E92;  // capital z with dot below
        case 0xF261: return 0x1EA1;  // small a with dot below
        case 0xF262: return 0x1E05;  // small b with dot below
        case 0xF264: return 0x1E0D;  // small d with dot below
        case 0xF265: return 0x1EB9;  // small e with dot below
        case 0xF268: return 0x1E25;  // small h with dot below
        case 0xF269: return 0x1ECB;  // small i with dot below
        case 0xF26B: return 0x1E33;  // small k with dot below
        case 0xF26C: return 0x1E37;  // small l with dot below
        case 0xF26D: return 0x1E43;  // small m with dot below
        case 0xF26E: return 0x1E47;  // small n with dot below
        case 0xF26F: return 0x1ECD;  // small o with dot below
        case 0xF272: return 0x1E5B;  // small r with dot below
        case 0xF273: return 0x1E63;  // small s with dot below
        case 0xF274: return 0x1E6D;  // small t with dot below
        case 0xF275: return 0x1EE5;  // small u with dot below
        case 0xF276: return 0x1E7F;  // small v with dot below
        case 0xF277: return 0x1E89;  // small w with dot below
        case 0xF279: return 0x1EF5;  // small y with dot below
        case 0xF27A: return 0x1E93;  // small z with dot below

        case 0xF355: return 0x1E72;  // capital u with diaeresis below
        case 0xF375: return 0x1E73;  // small u with diaeresis below

        case 0xF441: return 0x1E00;  // capital a with ring below
        case 0xF461: return 0x1E01;  // small a with ring below

        case 0xF948: return 0x1E2A;  // capital h with breve below
        case 0xF968: return 0x1E2B;  // small h with breve below
	default :
	    return 0;
	}
    }
}
