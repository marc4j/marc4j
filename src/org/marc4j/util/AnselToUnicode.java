// $Id: AnselToUnicode.java,v 1.10 2003/01/10 09:41:56 bpeters Exp $
/**
 * Copyright (C) 2002 Bas Peters (mail@bpeters.com)
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
 * <p>A utility to convert MARC-8 data to UCS/Unicode.</p>
 *
 * @author <a href="mailto:mail@bpeters.com">Bas Peters</a> 
 * @version $Revision: 1.10 $
 */
public class AnselToUnicode implements CharacterConverter {

    /**
     * <p>Converts MARC-8 data to UCS/Unicode.</p>
     *
     * @param data the MARC-8 data
     * @return {@link String} - the UCS/Unicode data
     */
    public String convert(String s) {
	return new String(convert(s.toCharArray()));
    }

    /**
     * <p>Converts MARC-8 data to UCS/Unicode.</p>
     *
     * @param data the MARC-8 data
     * @return char[] - the UCS/Unicode data
     */
    public char[] convert(char[] data) {
	StringBuffer sb = new StringBuffer();
	int mode = 0x73;
	int newmode;
	boolean multibyte = false;
	
	for(int i=0; i < data.length; i++) {
	    char c = data[i];
	    int len = data.length;
	    
	    if (isEscape(c)) {
			newmode = data[i+1];

			if (newmode == 0x28 || newmode == 0x2C || newmode == 0x29 || newmode == 0x2D) {
				newmode = data[i+2];
				mode = newmode;
				i += 2;
				multibyte = false;
			}
	
			if (newmode == 0x24) {
			    multibyte = true;
				if (data[i+1] == 0x2C || data[i+1] == 0x29 ||  data[i+1] == 0x2D) {
					mode = data[i+3];
					i += 3;
				} else {
					mode = data[i+2];
					i += 2;
				}
			}
	
			if (newmode == 0x67 || newmode == 0x62 || newmode == 0x73 || newmode == 0x70) {
				mode = data[i+1];
				i++;
				multibyte = false;
			}
	    } else if (multibyte) {
		int[] chars = new int[3];
		chars[0] = data[i]<<16;
		chars[1] = data[i+1]<<8;
		chars[2] = data[i+2];
		int mbchar = chars[0] | chars[1] | chars[2];
		sb.append(getMultibyteChar(mbchar));
		i+=2;
	    } else if (isCombining(c, mode) && hasNext(i,len)) {
			char d = data[i + 1];
			sb.append(getChar(d,mode));
			sb.append(getChar(c,mode));
			i++;
	    } else {
	    	sb.append(getChar(c,mode));
		}
	}
	return sb.toString().toCharArray();
    }

    private static char getMultibyteChar(int i) {
	switch (i) {
		case 0x212320: return   0x3000;          //Ideographic space in some implementations
		case 0x212321: return   0x3000;          //Ideographic space per ANSI Z39.64
		case 0x212328: return   0xFF08;          //Ideographic left parenthesis
		case 0x212329: return   0xFF09;          //Ideographic right parenthesis
		case 0x21232D: return   0xFF0D;          //Ideographic hyphen minus
		case 0x212A46: return   0x3013;          //Ideographic geta symbol
		case 0x212B25: return   0x300C;          //Ideographic left corner bracket
		case 0x212B26: return   0x300D;          //Ideographic right corner bracket
		case 0x212B31: return   0xFF3B;          //Ideographic left square bracket
		case 0x212B32: return   0xFF3D;          //Ideographic right square bracket
		case 0x212B33: return   0x3002;          //Ideographic full stop
		case 0x212B34: return   0xFF0E;          //Ideographic variant full stop
		case 0x212B35: return   0x3001;          //Ideographic comma
		case 0x212B38: return   0xFF0C;          //Ideographic variant comma
		case 0x212B39: return   0xFF1B;          //Ideographic semicolon
		case 0x212B3A: return   0xFF1A;          //Ideographic colon
		case 0x212B3B: return   0xFF1F;          //Ideographic question mark
		case 0x212B3D: return   0xFF01;          //Ideographic exclamation point
		case 0x212B59: return   0xFF0F;          //Ideographic solidus
		case 0x692126: return   0x30FB;          //Ideographic centered point
		case 0x692139: return   0x3005;          //Ideographic iteration mark
		case 0x692152: return   0x3008;          //Ideographic less than sign
		case 0x692153: return   0x3009;          //Ideographic greater than sign
		case 0x692154: return   0x300A;          //Ideographic left double angle bracket
		case 0x692155: return   0x300B;          //Ideographic right double angle bracket

		case 0x213042: return   0x4E52;          //East Asian ideograph
		case 0x213043: return   0x4E53;          //East Asian ideograph
		case 0x213044: return   0x4E56;          //East Asian ideograph
		case 0x213045: return   0x4E58;          //East Asian ideograph
		case 0x213046: return   0x4E59;          //East Asian ideograph
		case 0x213047: return   0x4E5D;          //East Asian ideograph
		case 0x213048: return   0x4E5F;          //East Asian ideograph
		case 0x213049: return   0x4E5E;          //East Asian ideograph
		case 0x21304B: return   0x4E73;          //East Asian ideograph
		case 0x21304C: return   0x4E7E;          //East Asian ideograph
		case 0x21304D: return   0x4E82;          //East Asian ideograph
		case 0x213050: return   0x4E8B;          //East Asian ideograph
		case 0x213051: return   0x4E8C;          //East Asian ideograph
		case 0x213052: return   0x4E8E;          //East Asian ideograph	default: 
                case 0x213053: return   0x4E95;
	default:
	    return ' ';
	}
    }

    private boolean isCombining(int i) {
	if (i > 0xE0 && i < 0xFF)
	    return true;
	return false;
    }

    private static char getChar(int c, int mode) {
		switch (mode){
		case 0x73: return getLatinChar(c);
		case 0x67: case 0x53: return getGreekChar(c);
		case 0x62: return getSubChar(c);
		case 0x70: return getSuperChar(c);
		case 0x33: case 0x34: return getArabicChar(c);
		case 0x4E: case 0x51: return getCyrillicChar(c); 
		case 0x32: return getHebrewChar(c); 
		default: return getLatinChar(c); 
		}
	}
	
    private static boolean hasNext(int pos, int len) {
		if (pos < (len -1))
		    return true;
		return false;
    }

    private static boolean isEscape(int i) {
		if (i == 0x1B)
		    return true;
		return false;
    }

    private static boolean isAscii(int i) {
		if (i > 0x00 && i < 0x7F)
		    return true;
		return false;
    }

    private static boolean isCombining(int i, int mode) {
		switch(mode) {
		case 0x73:  //ASCII default
		case 0x45:  //Extended Latin
		if (i >= 0xE0 && i <= 0xFF)
			return true;
		return false;
		case 0x32:  //Basic Hebrew
		if (i >= 0x40 && i <= 0x4E)
			return true;
		return false;
		case 0x33:  //Basic Arabic
		if (i >= 0x6B && i <= 0x72)
			return true;
		return false;
		case 0x34:  //Extended Arabic
		if (i >= 0xFD && i <= 0xFE)
			return true;
		return false;
		case 0x53:  //Basic Greek
		if (i >= 0x21 && i <= 0x27)
			return true;
		return false;
		default:
			return false;
		}
    }

    private static boolean isFinal(int i) {
		if (i == 0x33 || i == 0x34  || i == 0x42  || i == 0x31 || i == 0x4E || i == 0x51 || i == 0x53 || i == 0x32)
		    return true;
		return false;
    }

    private static char getGreekChar(int i) {
		switch(i) {
		case 0x21: return 0x0300; //    COMBINING GRAVE ACCENT
		case 0x22: return 0x0301; //    COMBINING ACUTE ACCENT
		case 0x23: return 0x0308; //    COMBINING DIAERESIS
		case 0x24: return 0x0342; //    COMBINING GREEK PERISPOMENI / CIRCUMFLEX
		case 0x25: return 0x0313; //    COMBINING COMMA ABOVE / SMOOTH BREATHING
		case 0x26: return 0x0314; //    COMBINING REVERSED COMMA ABOVE / ROUGH BREATHING
		case 0x27: return 0x0345; //    COMBINING GREEK YPOGEGRAMMENI / IOTA SUBSCRIPT
		case 0x30: return 0x00AB; //    LEFT-POINTING DOUBLE ANGLE QUOTATION MARK
		case 0x31: return 0x00BB; //    RIGHT-POINTING DOUBLE ANGLE QUOTATION MARK
		case 0x32: return 0x201C; //    LEFT DOUBLE QUOTATION MARK
		case 0x33: return 0x201D; //    RIGHT DOUBLE QUOTATION MARK
		case 0x34: return 0x0374; //    GREEK NUMERAL SIGN / UPPER PRIME
		case 0x35: return 0x0375; //    GREEK LOWER NUMERAL SIGN / LOWER PRIME
		case 0x3B: return 0x0387; //    GREEK ANO TELEIA / RAISED DOT, GREEK SEMICOLON
		case 0x3F: return 0x037E; //    GREEK QUESTION MARK
		case 0x41: return 0x0391; //    GREEK CAPITAL LETTER ALPHA
		case 0x42: return 0x0392; //    GREEK CAPITAL LETTER BETA
		case 0x44: return 0x0393; //    GREEK CAPITAL LETTER GAMMA
		case 0x45: return 0x0394; //    GREEK CAPITAL LETTER DELTA
		case 0x46: return 0x0395; //    GREEK CAPITAL LETTER EPSILON
		case 0x47: return 0x03DA; //    GREEK LETTER STIGMA
		case 0x48: return 0x03DC; //    GREEK LETTER DIGAMMA
		case 0x49: return 0x0396; //    GREEK CAPITAL LETTER ZETA
		case 0x4A: return 0x0397; //    GREEK CAPITAL LETTER ETA
		case 0x4B: return 0x0398; //    GREEK CAPITAL LETTER THETA
		case 0x4C: return 0x0399; //    GREEK CAPITAL LETTER IOTA
		case 0x4D: return 0x039A; //    GREEK CAPITAL LETTER KAPPA
		case 0x4E: return 0x039B; //    GREEK CAPITAL LETTER LAMDA
		case 0x4F: return 0x039C; //    GREEK CAPITAL LETTER MU
		case 0x50: return 0x039D; //    GREEK CAPITAL LETTER NU
		case 0x51: return 0x039E; //    GREEK CAPITAL LETTER XI
		case 0x52: return 0x039F; //    GREEK CAPITAL LETTER OMICRON
		case 0x53: return 0x03A0; //    GREEK CAPITAL LETTER PI
		case 0x54: return 0x03DE; //    GREEK LETTER KOPPA
		case 0x55: return 0x03A1; //    GREEK CAPITAL LETTER RHO
		case 0x56: return 0x03A3; //    GREEK CAPITAL LETTER SIGMA
		case 0x58: return 0x03A4; //    GREEK CAPITAL LETTER TAU
		case 0x59: return 0x03A5; //    GREEK CAPITAL LETTER UPSILON
		case 0x5A: return 0x03A6; //    GREEK CAPITAL LETTER PHI
		case 0x5B: return 0x03A7; //    GREEK CAPITAL LETTER CHI
		case 0x5C: return 0x03A8; //    GREEK CAPITAL LETTER PSI
		case 0x5D: return 0x03A9; //    GREEK CAPITAL LETTER OMEGA
		case 0x5E: return 0x03E0; //    GREEK LETTER SAMPI
		case 0x61: return 0x03B1; //    GREEK SMALL LETTER ALPHA
		case 0x62: return 0x03B2; //    GREEK SMALL LETTER BETA / SMALL LETTER BETA BEGINNING OF WORD
		case 0x63: return 0x03D0; //    GREEK BETA SYMBOL / SMALL LETTER BETA MIDDLE OF WORD
		case 0x64: return 0x03B3; //    GREEK SMALL LETTER GAMMA
		case 0x65: return 0x03B4; //    GREEK SMALL LETTER DELTA
		case 0x66: return 0x03B5; //    GREEK SMALL LETTER EPSILON
		case 0x67: return 0x03DB; //    GREEK SMALL LETTER STIGMA
		case 0x68: return 0x03DD; //    GREEK SMALL LETTER DIGAMMA
		case 0x69: return 0x03B6; //    GREEK SMALL LETTER ZETA
		case 0x6A: return 0x03B7; //    GREEK SMALL LETTER ETA
		case 0x6B: return 0x03B8; //    GREEK SMALL LETTER THETA
		case 0x6C: return 0x03B9; //    GREEK SMALL LETTER IOTA
		case 0x6D: return 0x03BA; //    GREEK SMALL LETTER KAPPA
		case 0x6E: return 0x03BB; //    GREEK SMALL LETTER LAMDA
		case 0x6F: return 0x03BC; //    GREEK SMALL LETTER MU
		case 0x70: return 0x03BD; //    GREEK SMALL LETTER NU
		case 0x71: return 0x03BE; //    GREEK SMALL LETTER XI
		case 0x72: return 0x03BF; //    GREEK SMALL LETTER OMICRON
		case 0x73: return 0x03C0; //    GREEK SMALL LETTER PI
		case 0x74: return 0x03DF; //    GREEK SMALL LETTER KOPPA
		case 0x75: return 0x03C1; //    GREEK SMALL LETTER RHO
		case 0x76: return 0x03C3; //    GREEK SMALL LETTER SIGMA
		case 0x77: return 0x03C2; //    GREEK SMALL LETTER FINAL SIGMA / SMALL LETTER SIGMA END OF WORD
		case 0x78: return 0x03C4; //    GREEK SMALL LETTER TAU
		case 0x79: return 0x03C5; //    GREEK SMALL LETTER UPSILON
		case 0x7A: return 0x03C6; //    GREEK SMALL LETTER PHI
		case 0x7B: return 0x03C7; //    GREEK SMALL LETTER CHI
		case 0x7C: return 0x03C8; //    GREEK SMALL LETTER PSI
		case 0x7D: return 0x03C9; //    GREEK SMALL LETTER OMEGA
		case 0x7E: return 0x03E1; //    GREEK SMALL LETTER SAMPI
		default :
	    	return (char)i;
		}
    }

    private static char getSuperChar(int i) {
		switch(i) {
		case 0x28: return   0x207D;          //SUPERSCRIPT OPENING PARENTHESIS / SUPERSCRIPT LEFT PARENTHESIS
		case 0x29: return   0x207E;          //SUPERSCRIPT CLOSING PARENTHESIS / SUPERSCRIPT RIGHT PARENTHESIS
		case 0x2B: return   0x207A;          //SUPERSCRIPT PLUS SIGN
		case 0x2D: return   0x207B;          //SUPERSCRIPT HYPHEN-MINUS / SUPERSCRIPT MINUS
		case 0x30: return   0x2070;          //SUPERSCRIPT DIGIT ZERO
		case 0x31: return   0x00B9;          //SUPERSCRIPT DIGIT ONE
		case 0x32: return   0x00B2;          //SUPERSCRIPT DIGIT TWO
		case 0x33: return   0x00B3;          //SUPERSCRIPT DIGIT THREE
		case 0x34: return   0x2074;          //SUPERSCRIPT DIGIT FOUR
		case 0x35: return   0x2075;          //SUPERSCRIPT DIGIT FIVE
		case 0x36: return   0x2076;          //SUPERSCRIPT DIGIT SIX
		case 0x37: return   0x2077;          //SUPERSCRIPT DIGIT SEVEN
		case 0x38: return   0x2078;          //SUPERSCRIPT DIGIT EIGHT
		case 0x39: return   0x2079;          //SUPERSCRIPT DIGIT NINE
		default :
		    return (char)i;
		}
    }

    private static char getSubChar(int i) {
		switch(i) {
		case 0x28: return   0x208D;          //SUBSCRIPT OPENING PARENTHESIS / SUBSCRIPT LEFT PARENTHESIS 
		case 0x29: return   0x208E;          //SUBSCRIPT CLOSING PARENTHESIS / SUBSCRIPT RIGHT PARENTHESIS 
		case 0x2B: return   0x208A;          //SUBSCRIPT PLUS SIGN 
		case 0x2D: return   0x208B;          //SUBSCRIPT HYPHEN-MINUS / SUBSCRIPT MINUS 
		case 0x30: return   0x2080;          //SUBSCRIPT DIGIT ZERO 
		case 0x31: return   0x2081;          //SUBSCRIPT DIGIT ONE 
		case 0x32: return   0x2082;          //SUBSCRIPT DIGIT TWO 
		case 0x33: return   0x2083;          //SUBSCRIPT DIGIT THREE 
		case 0x34: return   0x2084;          //SUBSCRIPT DIGIT FOUR 
		case 0x35: return   0x2085;          //SUBSCRIPT DIGIT FIVE 
		case 0x36: return   0x2086;          //SUBSCRIPT DIGIT SIX 
		case 0x37: return   0x2087;          //SUBSCRIPT DIGIT SEVEN 
		case 0x38: return   0x2088;          //SUBSCRIPT DIGIT EIGHT 
		case 0x39: return   0x2089;          //SUBSCRIPT DIGIT NINE 

		default :
		    return (char)i;
		}
    }

    private static char getHebrewChar(int i) {
		switch(i) {
		case 0x21: return 0x0021; //    EXCLAMATION MARK
		case 0x22: return 0x05F4; //    QUOTATION MARK, GERSHAYIM / HEBREW PUNCTUATION
		case 0x23: return 0x0023; //    NUMBER SIGN
		case 0x24: return 0x0024; //    DOLLAR SIGN
		case 0x25: return 0x0025; //    PERCENT SIGN
		case 0x26: return 0x0026; //    AMPERSAND
		case 0x27: return 0x05F3; //    APOSTROPHE, GERESH / HEBREW PUNCTUATION GERESH
		case 0x28: return 0x0028; //    OPENING PARENTHESIS / LEFT PARENTHESIS
		case 0x29: return 0x0029; //    CLOSING PARENTHESIS / RIGHT PARENTHESIS
		case 0x2A: return 0x002A; //    ASTERISK
		case 0x2B: return 0x002B; //    PLUS SIGN
		case 0x2C: return 0x002C; //    COMMA
		case 0x2D: return 0x05BE; //    HYPHEN-MINUS, MAKEF / HEBREW PUNCTUATION MAQAF
		case 0x2E: return 0x002E; //    PERIOD, DECIMAL POINT / FULL STOP
		case 0x2F: return 0x002F; //    SLASH / SOLIDUS
		case 0x30: return 0x0030; //    DIGIT ZERO
		case 0x31: return 0x0031; //    DIGIT ONE
		case 0x32: return 0x0032; //    DIGIT TWO
		case 0x33: return 0x0033; //    DIGIT THREE
		case 0x34: return 0x0034; //    DIGIT FOUR
		case 0x35: return 0x0035; //    DIGIT FIVE
		case 0x36: return 0x0036; //    DIGIT SIX
		case 0x37: return 0x0037; //    DIGIT SEVEN
		case 0x38: return 0x0038; //    DIGIT EIGHT
		case 0x39: return 0x0039; //    DIGIT NINE
		case 0x3A: return 0x003A; //    COLON
		case 0x3B: return 0x003B; //    SEMICOLON
		case 0x3C: return 0x003C; //    LESS-THAN SIGN
		case 0x3D: return 0x003D; //    EQUALS SIGN
		case 0x3E: return 0x003E; //    GREATER-THAN SIGN
		case 0x3F: return 0x003F; //    QUESTION MARK
		case 0x40: return 0x05B7; //    HEBREW POINT PATAH
		case 0x41: return 0x05B8; //    KAMATS / HEBREW POINT QAMATS
		case 0x42: return 0x05B6; //    HEBREW POINT SEGOL
		case 0x43: return 0x05B5; //    TSEREH / HEBREW POINT TSERE
		case 0x44: return 0x05B4; //    HIRIK / HEBREW POINT HIRIQ
		case 0x45: return 0x05B9; //    HOLAM, LEFT SIN DOT / HEBREW POINT HOLAM
		case 0x46: return 0x05BB; //    KUBUTS / HEBREW POINT QUBUTS
		case 0x47: return 0x05B0; //    HEBREW POINT SHEVA
		case 0x48: return 0x05B2; //    HEBREW POINT HATAF PATAH
		case 0x49: return 0x05B3; //    HATAF KAMATS / HEBREW POINT HATAF QAMATS
		case 0x4A: return 0x05B1; //    HEBREW POINT HATAF SEGOL
		case 0x4B: return 0x05BC; //    HEBREW POINT DAGESH OR MAPIQ
		case 0x4C: return 0x05BF; //    RAFEH / HEBREW POINT RAFE
		case 0x4D: return 0x05C1; //    RIGHT SHIN DOT / HEBREW POINT  SHIN DOT
		case 0x4E: return 0xFB1E; //    VARIKA / HEBREW POINT JUDEO-SPANISH VARIKA
		case 0x5B: return 0x005B; //    OPENING SQUARE BRACKET / LEFT SQUARE BRACKET
		case 0x5D: return 0x005D; //    CLOSING SQUARE BRACKET / RIGHT SQUARE BRACKET
		case 0x60: return 0x05D0; //    HEBREW LETTER ALEF
		case 0x61: return 0x05D1; //    HEBREW LETTER BET
		case 0x62: return 0x05D2; //    HEBREW LETTER GIMEL
		case 0x63: return 0x05D3; //    HEBREW LETTER DALET
		case 0x64: return 0x05D4; //    HEBREW LETTER HE
		case 0x65: return 0x05D5; //    HEBREW LETTER VAV
		case 0x66: return 0x05D6; //    HEBREW LETTER ZAYIN
		case 0x67: return 0x05D7; //    HEBREW LETTER HET
		case 0x68: return 0x05D8; //    HEBREW LETTER TET
		case 0x69: return 0x05D9; //    HEBREW LETTER YOD
		case 0x6A: return 0x05DA; //    HEBREW LETTER FINAL KAF
		case 0x6B: return 0x05DB; //    HEBREW LETTER KAF
		case 0x6C: return 0x05DC; //    HEBREW LETTER LAMED
		case 0x6D: return 0x05DD; //    HEBREW LETTER FINAL MEM
		case 0x6E: return 0x05DE; //    HEBREW LETTER MEM
		case 0x6F: return 0x05DF; //    HEBREW LETTER FINAL NUN
		case 0x70: return 0x05E0; //    HEBREW LETTER NUN
		case 0x71: return 0x05E1; //    HEBREW LETTER SAMEKH
		case 0x72: return 0x05E2; //    HEBREW LETTER AYIN
		case 0x73: return 0x05E3; //    HEBREW LETTER FINAL PE
		case 0x74: return 0x05E4; //    HEBREW LETTER PE
		case 0x75: return 0x05E5; //    HEBREW LETTER FINAL TSADI
		case 0x76: return 0x05E6; //    HEBREW LETTER TSADI
		case 0x77: return 0x05E7; //    HEBREW LETTER QOF / KOF
		case 0x78: return 0x05E8; //    HEBREW LETTER RESH
		case 0x79: return 0x05E9; //    HEBREW LETTER SHIN
		case 0x7A: return 0x05EA; //    HEBREW LETTER TAV
		case 0x7B: return 0x05F0; //    HEBREW LIGATURE YIDDISH DOUBLE VAV / TSVEY VOVN
		case 0x7C: return 0x05F1; //    HEBREW LIGATURE YIDDISH VAV YOD / VOV YUD
		case 0x7D: return 0x05F2; //    HEBREW LIGATURE YIDDISH DOUBLE YOD / TSVEY YUDN
		default :
		    return (char)i;
		}
    }	

    private static char getArabicChar(int i) {
		switch(i) {
		case 0x21: return 0x0021; //    EXCLAMATION MARK
		case 0x22: return 0x0022; //    QUOTATION MARK
		case 0x23: return 0x0023; //    NUMBER SIGN
		case 0x24: return 0x0024; //    DOLLAR SIGN
		case 0x25: return 0x066A; //    PERCENT SIGN / ARABIC PERCENT SIGN
		case 0x26: return 0x0026; //    AMPERSAND
		case 0x27: return 0x0027; //    APOSTROPHE
		case 0x28: return 0x0028; //    OPENING PARENTHESIS / LEFT PARENTHESIS
		case 0x29: return 0x0029; //    CLOSING PARENTHESIS / RIGHT PARENTHESIS
		case 0x2A: return 0x066D; //    ASTERISK / ARABIC FIVE POINTED STAR
		case 0x2B: return 0x002B; //    PLUS SIGN
		case 0x2C: return 0x060C; //    ARABIC COMMA
		case 0x2D: return 0x002D; //    HYPHEN-MINUS
		case 0x2E: return 0x002E; //    PERIOD, DECIMAL POINT / FULL STOP
		case 0x2F: return 0x002F; //    SLASH / SOLIDUS
		case 0x30: return 0x0660; //    ARABIC-INDIC DIGIT ZERO
		case 0x31: return 0x0661; //    ARABIC-INDIC DIGIT ONE
		case 0x32: return 0x0662; //    ARABIC-INDIC DIGIT TWO
		case 0x33: return 0x0663; //    ARABIC-INDIC DIGIT THREE
		case 0x34: return 0x0664; //    ARABIC-INDIC DIGIT FOUR
		case 0x35: return 0x0665; //    ARABIC-INDIC DIGIT FIVE
		case 0x36: return 0x0666; //    ARABIC-INDIC DIGIT SIX
		case 0x37: return 0x0667; //    ARABIC-INDIC DIGIT SEVEN
		case 0x38: return 0x0668; //    ARABIC-INDIC DIGIT EIGHT
		case 0x39: return 0x0669; //    ARABIC-INDIC DIGIT NINE
		case 0x3A: return 0x003A; //    COLON
		case 0x3B: return 0x061B; //    ARABIC SEMICOLON
		case 0x3C: return 0x003C; //    LESS-THAN SIGN
		case 0x3D: return 0x003D; //    EQUALS SIGN
		case 0x3E: return 0x003E; //    GREATER-THAN SIGN
		case 0x3F: return 0x061F; //    ARABIC QUESTION MARK
		case 0x41: return 0x0621; //    HAMZAH / ARABIC LETTER HAMZA
		case 0x42: return 0x0622; //    ARABIC LETTER ALEF WITH MADDA ABOVE
		case 0x43: return 0x0623; //    ARABIC LETTER ALEF WITH HAMZA ABOVE
		case 0x44: return 0x0624; //    ARABIC LETTER WAW WITH HAMZA ABOVE
		case 0x45: return 0x0625; //    ARABIC LETTER ALEF WITH HAMZA BELOW
		case 0x46: return 0x0626; //    ARABIC LETTER YEH WITH HAMZA ABOVE
		case 0x47: return 0x0627; //    ARABIC LETTER ALEF
		case 0x48: return 0x0628; //    ARABIC LETTER BEH
		case 0x49: return 0x0629; //    ARABIC LETTER TEH MARBUTA
		case 0x4A: return 0x062A; //    ARABIC LETTER TEH
		case 0x4B: return 0x062B; //    ARABIC LETTER THEH
		case 0x4C: return 0x062C; //    ARABIC LETTER JEEM
		case 0x4D: return 0x062D; //    ARABIC LETTER HAH
		case 0x4E: return 0x062E; //    ARABIC LETTER KHAH
		case 0x4F: return 0x062F; //    ARABIC LETTER DAL
		case 0x50: return 0x0630; //    ARABIC LETTER THAL
		case 0x51: return 0x0631; //    ARABIC LETTER REH
		case 0x52: return 0x0632; //    ARABIC LETTER ZAIN
		case 0x53: return 0x0633; //    ARABIC LETTER SEEN
		case 0x54: return 0x0634; //    ARABIC LETTER SHEEN
		case 0x55: return 0x0635; //    ARABIC LETTER SAD
		case 0x56: return 0x0636; //    ARABIC LETTER DAD
		case 0x57: return 0x0637; //    ARABIC LETTER TAH
		case 0x58: return 0x0638; //    ARABIC LETTER ZAH
		case 0x59: return 0x0639; //    ARABIC LETTER AIN
		case 0x5A: return 0x063A; //    ARABIC LETTER GHAIN
		case 0x5B: return 0x005B; //    OPENING SQUARE BRACKET / LEFT SQUARE BRACKET
		case 0x5D: return 0x005D; //    CLOSING SQUARE BRACKET / RIGHT SQUARE BRACKET
		case 0x60: return 0x0640; //    ARABIC TATWEEL
		case 0x61: return 0x0641; //    ARABIC LETTER FEH
		case 0x62: return 0x0642; //    ARABIC LETTER QAF
		case 0x63: return 0x0643; //    ARABIC LETTER KAF
		case 0x64: return 0x0644; //    ARABIC LETTER LAM
		case 0x65: return 0x0645; //    ARABIC LETTER MEEM
		case 0x66: return 0x0646; //    ARABIC LETTER NOON
		case 0x67: return 0x0647; //    ARABIC LETTER HEH
		case 0x68: return 0x0648; //    ARABIC LETTER WAW
		case 0x69: return 0x0649; //    ARABIC LETTER ALEF MAKSURA
		case 0x6A: return 0x064A; //    ARABIC LETTER YEH
		case 0x6B: return 0x064B; //    ARABIC FATHATAN
		case 0x6C: return 0x064C; //    ARABIC DAMMATAN
		case 0x6D: return 0x064D; //    ARABIC KASRATAN
		case 0x6E: return 0x064E; //    ARABIC FATHA
		case 0x6F: return 0x064F; //    ARABIC DAMMA
		case 0x70: return 0x0650; //    ARABIC KASRA
		case 0x71: return 0x0651; //    ARABIC SHADDA
		case 0x72: return 0x0652; //    ARABIC SUKUN
		case 0x73: return 0x0671; //    ARABIC LETTER ALEF WASLA
		case 0x74: return 0x0670; //    ARABIC LETTER SUPERSCRIPT ALEF
		case 0x78: return 0x066C; //    ARABIC THOUSANDS SEPARATOR
		case 0x79: return 0x201D; //    RIGHT DOUBLE QUOTATION MARK
		case 0x7A: return 0x201C; //    LEFT DOUBLE QUOTATION MARK
		case 0xA1: return 0x06FD; //    DOUBLE ALEF WITH HAMZA ABOVE / ARABIC SIGN SINDHI
		case 0xA2: return 0x0672; //    ARABIC LETTER ALEF WITH WAVY HAMZA ABOVE
		case 0xA3: return 0x0673; //    ARABIC LETTER ALEF WITH WAVY HAMZA BELOW
		case 0xA4: return 0x0679; //    ARABIC LETTER TTEH
		case 0xA5: return 0x067A; //    ARABIC LETTER TTEHEH
		case 0xA6: return 0x067B; //    ARABIC LETTER BBEH
		case 0xA7: return 0x067C; //    ARABIC LETTER TEH WITH RING
		case 0xA8: return 0x067D; //    ARABIC LETTER TEH WITH THREE DOTS ABOVE DOWNWARDS
		case 0xA9: return 0x067E; //    ARABIC LETTER PEH
		case 0xAA: return 0x067F; //    ARABIC LETTER TEHEH
		case 0xAB: return 0x0680; //    ARABIC LETTER BEHEH
		case 0xAC: return 0x0681; //    ARABIC LETTER HAH WITH HAMZA ABOVE
		case 0xAD: return 0x0682; //    ARABIC LETTER HAH WITH TWO ABOVE DOTS VERTICAL ABOVE
		case 0xAE: return 0x0683; //    ARABIC LETTER NYEH
		case 0xAF: return 0x0684; //    ARABIC LETTER DYEH
		case 0xB0: return 0x0685; //    ARABIC LETTER HAH WITH THREE DOTS ABOVE
		case 0xB1: return 0x0686; //    ARABIC LETTER TCHEH
		case 0xB2: return 0x06BF; //    ARABIC LETTER TCHEH WITH DOT ABOVE
		case 0xB3: return 0x0687; //    ARABIC LETTER TCHEHEH
		case 0xB4: return 0x0688; //    ARABIC LETTER DDAL
		case 0xB5: return 0x0689; //    ARABIC LETTER DAL WITH RING
		case 0xB6: return 0x068A; //    ARABIC LETTER DAL WITH DOT BELOW
		case 0xB7: return 0x068B; //    ARABIC LETTER DAL WITH DOT BELOW AND SMALL TAH
		case 0xB8: return 0x068C; //    ARABIC LETTER DAHAL
		case 0xB9: return 0x068D; //    ARABIC LETTER DDAHAL
		case 0xBA: return 0x068E; //    ARABIC LETTER DUL
		case 0xBB: return 0x068F; //    ARABIC LETTER DAL WITH THREE DOTS ABOVE DOWNWARDS
		case 0xBC: return 0x0690; //    ARABIC LETTER DAL WITH FOUR DOTS ABOVE
		case 0xBD: return 0x0691; //    ARABIC LETTER RREH
		case 0xBE: return 0x0692; //    ARABIC LETTER REH WITH SMALL V
		case 0xBF: return 0x0693; //    ARABIC LETTER REH WITH RING
		case 0xC0: return 0x0694; //    ARABIC LETTER REH WITH DOT BELOW
		case 0xC1: return 0x0695; //    ARABIC LETTER REH WITH SMALL V BELOW
		case 0xC2: return 0x0696; //    ARABIC LETTER REH WITH DOT BELOW AND DOT ABOVE
		case 0xC3: return 0x0697; //    ARABIC LETTER REH WITH TWO DOTS ABOVE
		case 0xC4: return 0x0698; //    ARABIC LETTER JEH
		case 0xC5: return 0x0699; //    ARABIC LETTER REH WITH FOUR DOTS ABOVE
		case 0xC6: return 0x069A; //    ARABIC LETTER SEEN WITH DOT BELOW AND DOT ABOVE
		case 0xC7: return 0x069B; //    ARABIC LETTER SEEN WITH THREE DOTS BELOW
		case 0xC8: return 0x069C; //    ARABIC LETTER SEEN WITH THREE DOTS BELOW AND THREE DOTS
		case 0xC9: return 0x06FA; //    ARABIC LETTER SHEEN WITH DOT BELOW
		case 0xCA: return 0x069D; //    ARABIC LETTER SAD WITH TWO DOTS BELOW
		case 0xCB: return 0x069E; //    ARABIC LETTER SAD WITH THREE DOTS ABOVE
		case 0xCC: return 0x06FB; //    ARABIC LETTER DAD WITH DOT BELOW
		case 0xCD: return 0x069F; //    ARABIC LETTER TAH WITH THREE DOTS ABOVE
		case 0xCE: return 0x06A0; //    ARABIC LETTER AIN WITH THREE DOTS ABOVE
		case 0xCF: return 0x06FC; //    ARABIC LETTER GHAIN WITH DOT BELOW
		case 0xD0: return 0x06A1; //    ARABIC LETTER DOTLESS FEH
		case 0xD1: return 0x06A2; //    ARABIC LETTER FEH WITH DOT MOVED BELOW
		case 0xD2: return 0x06A3; //    ARABIC LETTER FEH WITH DOT BELOW
		case 0xD3: return 0x06A4; //    ARABIC LETTER VEH
		case 0xD4: return 0x06A5; //    ARABIC LETTER FEH WITH THREE DOTS BELOW
		case 0xD5: return 0x06A6; //    ARABIC LETTER PEHEH
		case 0xD6: return 0x06A7; //    ARABIC LETTER QAF WITH DOT ABOVE
		case 0xD7: return 0x06A8; //    ARABIC LETTER QAF WITH THREE DOTS ABOVE
		case 0xD8: return 0x06A9; //    ARABIC LETTER KEHEH
		case 0xD9: return 0x06AA; //    ARABIC LETTER SWASH KAF
		case 0xDA: return 0x06AB; //    ARABIC LETTER KAF WITH RING
		case 0xDB: return 0x06AC; //    ARABIC LETTER KAF WITH DOT ABOVE
		case 0xDC: return 0x06AD; //    ARABIC LETTER NG
		case 0xDD: return 0x06AE; //    ARABIC LETTER KAF WITH THREE DOTS BELOW
		case 0xDE: return 0x06AF; //    ARABIC LETTER GAF
		case 0xDF: return 0x06B0; //    ARABIC LETTER GAF WITH RING
		case 0xE0: return 0x06B1; //    ARABIC LETTER NGOEH
		case 0xE1: return 0x06B2; //    ARABIC LETTER GAF WITH TWO DOTS BELOW
		case 0xE2: return 0x06B3; //    ARABIC LETTER GUEH
		case 0xE3: return 0x06B4; //    ARABIC LETTER GAF WITH THREE DOTS ABOVE
		case 0xE4: return 0x06B5; //    ARABIC LETTER LAM WITH SMALL V
		case 0xE5: return 0x06B6; //    ARABIC LETTER LAM WITH DOT ABOVE
		case 0xE6: return 0x06B7; //    ARABIC LETTER LAM WITH THREE DOTS ABOVE
		case 0xE7: return 0x06B8; //    ARABIC LETTER LAM WITH THREE DOTS BELOW
		case 0xE8: return 0x06BA; //    ARABIC LETTER NOON GHUNNA
		case 0xE9: return 0x06BB; //    ARABIC LETTER RNOON
		case 0xEA: return 0x06BC; //    ARABIC LETTER NOON WITH RING
		case 0xEB: return 0x06BD; //    ARABIC LETTER NOON WITH THREE DOTS ABOVE
		case 0xEC: return 0x06B9; //    ARABIC LETTER NOON WITH DOT BELOW
		case 0xED: return 0x06BE; //    ARABIC LETTER HEH DOACHASHMEE
		case 0xEE: return 0x06C0; //    HEH WITH HAMZA ABOVE / ARABIC LETTER HEH WITH YEH ABOVE
		case 0xEF: return 0x06C4; //    ARABIC LETTER WAW WITH RING
		case 0xF0: return 0x06C5; //    KYRGHYZ OE / ARABIC LETTER KIRGHIZ OE
		case 0xF1: return 0x06C6; //    ARABIC LETTER OE
		case 0xF2: return 0x06CA; //    ARABIC LETTER WAW WITH TWO DOTS ABOVE
		case 0xF3: return 0x06CB; //    ARABIC LETTER VE
		case 0xF4: return 0x06CD; //    ARABIC LETTER YEH WITH TAIL
		case 0xF5: return 0x06CE; //    ARABIC LETTER YEH WITH SMALL V
		case 0xF6: return 0x06D0; //    ARABIC LETTER E
		case 0xF7: return 0x06D2; //    ARABIC LETTER YEH BARREE
		case 0xF8: return 0x06D3; //    ARABIC LETTER YEH BARREE WITH HAMZA ABOVE
		case 0xFD: return 0x0306; //    SHORT E / COMBINING BREVE
		case 0xFE: return 0x030C; //    SHORT U / COMBINING CARON
		default :
		    return (char)i;
		}
    }

    private static char getCyrillicChar(int i) {
		switch(i) {
		case 0x21: return 0x0021; //    EXCLAMATION MARK
		case 0x22: return 0x0022; //    QUOTATION MARK
		case 0x23: return 0x0023; //    NUMBER SIGN
		case 0x24: return 0x0024; //    DOLLAR SIGN
		case 0x25: return 0x0025; //    PERCENT SIGN
		case 0x26: return 0x0026; //    AMPERSAND
		case 0x27: return 0x0027; //    APOSTROPHE
		case 0x28: return 0x0028; //    OPENING PARENTHESIS / LEFT PARENTHESIS
		case 0x29: return 0x0029; //    CLOSING PARENTHESIS / RIGHT PARENTHESIS
		case 0x2A: return 0x002A; //    ASTERISK
		case 0x2B: return 0x002B; //    PLUS SIGN
		case 0x2C: return 0x002C; //    COMMA
		case 0x2D: return 0x002D; //    HYPHEN-MINUS
		case 0x2E: return 0x002E; //    PERIOD, DECIMAL POINT / FULL STOP
		case 0x2F: return 0x002F; //    SLASH / SOLIDUS
		case 0x30: return 0x0030; //    DIGIT ZERO
		case 0x31: return 0x0031; //    DIGIT ONE
		case 0x32: return 0x0032; //    DIGIT TWO
		case 0x33: return 0x0033; //    DIGIT THREE
		case 0x34: return 0x0034; //    DIGIT FOUR
		case 0x35: return 0x0035; //    DIGIT FIVE
		case 0x36: return 0x0036; //    DIGIT SIX
		case 0x37: return 0x0037; //    DIGIT SEVEN
		case 0x38: return 0x0038; //    DIGIT EIGHT
		case 0x39: return 0x0039; //    DIGIT NINE
		case 0x3A: return 0x003A; //    COLON
		case 0x3B: return 0x003B; //    SEMICOLON
		case 0x3C: return 0x003C; //    LESS-THAN SIGN
		case 0x3D: return 0x003D; //    EQUALS SIGN
		case 0x3E: return 0x003E; //    GREATER-THAN SIGN
		case 0x3F: return 0x003F; //    QUESTION MARK
		case 0x40: return 0x044E; //    LOWERCASE IU / CYRILLIC SMALL LETTER YU
		case 0x41: return 0x0430; //    CYRILLIC SMALL LETTER A
		case 0x42: return 0x0431; //    CYRILLIC SMALL LETTER BE
		case 0x43: return 0x0446; //    CYRILLIC SMALL LETTER TSE
		case 0x44: return 0x0434; //    CYRILLIC SMALL LETTER DE
		case 0x45: return 0x0435; //    CYRILLIC SMALL LETTER IE
		case 0x46: return 0x0444; //    CYRILLIC SMALL LETTER EF
		case 0x47: return 0x0433; //    LOWERCASE GE / CYRILLIC SMALL LETTER GHE
		case 0x48: return 0x0445; //    LOWERCASE KHA / CYRILLIC SMALL LETTER HA
		case 0x49: return 0x0438; //    LOWERCASE II / CYRILLIC SMALL LETTER I
		case 0x4A: return 0x0439; //    LOWERCASE SHORT II / CYRILLIC SMALL LETTER SHORT I
		case 0x4B: return 0x043A; //    CYRILLIC SMALL LETTER KA
		case 0x4C: return 0x043B; //    CYRILLIC SMALL LETTER EL
		case 0x4D: return 0x043C; //    CYRILLIC SMALL LETTER EM
		case 0x4E: return 0x043D; //    CYRILLIC SMALL LETTER EN
		case 0x4F: return 0x043E; //    CYRILLIC SMALL LETTER O
		case 0x50: return 0x043F; //    CYRILLIC SMALL LETTER PE
		case 0x51: return 0x044F; //    LOWERCASE IA / CYRILLIC SMALL LETTER YA
		case 0x52: return 0x0440; //    CYRILLIC SMALL LETTER ER
		case 0x53: return 0x0441; //    CYRILLIC SMALL LETTER ES
		case 0x54: return 0x0442; //    CYRILLIC SMALL LETTER TE
		case 0x55: return 0x0443; //    CYRILLIC SMALL LETTER U
		case 0x56: return 0x0436; //    CYRILLIC SMALL LETTER ZHE
		case 0x57: return 0x0432; //    CYRILLIC SMALL LETTER VE
		case 0x58: return 0x044C; //    CYRILLIC SMALL LETTER SOFT SIGN
		case 0x59: return 0x044B; //    LOWERCASE YERI / CYRILLIC SMALL LETTER YERI
		case 0x5A: return 0x0437; //    CYRILLIC SMALL LETTER ZE
		case 0x5B: return 0x0448; //    CYRILLIC SMALL LETTER SHA
		case 0x5C: return 0x044D; //    LOWERCASE REVERSED E / CYRILLIC SMALL LETTER E
		case 0x5D: return 0x0449; //    CYRILLIC SMALL LETTER SHCHA
		case 0x5E: return 0x0447; //    CYRILLIC SMALL LETTER CHE
		case 0x5F: return 0x044A; //    CYRILLIC SMALL LETTER HARD SIGN
		case 0x60: return 0x042E; //    UPPERCASE IU / CYRILLIC CAPITAL LETTER YU
		case 0x61: return 0x0410; //    CYRILLIC CAPITAL LETTER A
		case 0x62: return 0x0411; //    CYRILLIC CAPITAL LETTER BE
		case 0x63: return 0x0426; //    CYRILLIC CAPITAL LETTER TSE
		case 0x64: return 0x0414; //    CYRILLIC CAPITAL LETTER DE
		case 0x65: return 0x0415; //    CYRILLIC CAPITAL LETTER IE
		case 0x66: return 0x0424; //    CYRILLIC CAPITAL LETTER EF
		case 0x67: return 0x0413; //    UPPERCASE GE / CYRILLIC CAPITAL LETTER GHE
		case 0x68: return 0x0425; //    UPPERCASE KHA / CYRILLIC CAPITAL LETTER HA
		case 0x69: return 0x0418; //    UPPERCASE II / CYRILLIC CAPITAL LETTER I
		case 0x6A: return 0x0419; //    UPPERCASE SHORT II / CYRILLIC CAPITAL LETTER SHORT I
		case 0x6B: return 0x041A; //    CYRILLIC CAPITAL LETTER KA
		case 0x6C: return 0x041B; //    CYRILLIC CAPITAL LETTER EL
		case 0x6D: return 0x041C; //    CYRILLIC CAPITAL LETTER EM
		case 0x6E: return 0x041D; //    CYRILLIC CAPITAL LETTER EN
		case 0x6F: return 0x041E; //    CYRILLIC CAPITAL LETTER O
		case 0x70: return 0x041F; //    CYRILLIC CAPITAL LETTER PE
		case 0x71: return 0x042F; //    UPPERCASE IA / CYRILLIC CAPITAL LETTER YA
		case 0x72: return 0x0420; //    CYRILLIC CAPITAL LETTER ER
		case 0x73: return 0x0421; //    CYRILLIC CAPITAL LETTER ES
		case 0x74: return 0x0422; //    CYRILLIC CAPITAL LETTER TE
		case 0x75: return 0x0423; //    CYRILLIC CAPITAL LETTER U
		case 0x76: return 0x0416; //    CYRILLIC CAPITAL LETTER ZHE
		case 0x77: return 0x0412; //    CYRILLIC CAPITAL LETTER VE
		case 0x78: return 0x042C; //    CYRILLIC CAPITAL LETTER SOFT SIGN
		case 0x79: return 0x042B; //    UPPERCASE YERI / CYRILLIC CAPITAL LETTER YERI
		case 0x7A: return 0x0417; //    CYRILLIC CAPITAL LETTER ZE
		case 0x7B: return 0x0428; //    CYRILLIC CAPITAL LETTER SHA
		case 0x7C: return 0x042D; //    CYRILLIC CAPITAL LETTER E
		case 0x7D: return 0x0429; //    CYRILLIC CAPITAL LETTER SHCHA
		case 0x7E: return 0x0427; //    CYRILLIC CAPITAL LETTER CHE
		case 0xC0: return 0x0491; //    LOWERCASE GE WITH UPTURN / CYRILLIC SMALL LETTER GHE WITH
		case 0xC1: return 0x0452; //    LOWERCASE DJE / CYRILLIC SMALL LETTER DJE (Serbian)
		case 0xC2: return 0x0453; //    CYRILLIC SMALL LETTER GJE
		case 0xC3: return 0x0454; //    LOWERCASE E / CYRILLIC SMALL LETTER UKRAINIAN IE
		case 0xC4: return 0x0451; //    CYRILLIC SMALL LETTER IO
		case 0xC5: return 0x0455; //    CYRILLIC SMALL LETTER DZE
		case 0xC6: return 0x0456; //    LOWERCASE I / CYRILLIC SMALL LETTER BYELORUSSIAN-UKRANIAN
		case 0xC7: return 0x0457; //    LOWERCASE YI / CYRILLIC SMALL LETTER YI (Ukrainian)
		case 0xC8: return 0x0458; //    CYRILLIC SMALL LETTER JE
		case 0xC9: return 0x0459; //    CYRILLIC SMALL LETTER LJE
		case 0xCA: return 0x045A; //    CYRILLIC SMALL LETTER NJE
		case 0xCB: return 0x045B; //    LOWERCASE TSHE / CYRILLIC SMALL LETTER TSHE (Serbian)
		case 0xCC: return 0x045C; //    CYRILLIC SMALL LETTER KJE
		case 0xCD: return 0x045E; //    LOWERCASE SHORT U / CYRILLIC SMALL LETTER SHORT U (Byelorussian)
		case 0xCE: return 0x045F; //    CYRILLIC SMALL LETTER DZHE
		case 0xD0: return 0x0463; //    CYRILLIC SMALL LETTER YAT
		case 0xD1: return 0x0473; //    CYRILLIC SMALL LETTER FITA
		case 0xD2: return 0x0475; //    CYRILLIC SMALL LETTER IZHITSA
		case 0xD3: return 0x046B; //    CYRILLIC SMALL LETTER BIG YUS
		case 0xDB: return 0x005B; //    OPENING SQUARE BRACKET / LEFT SQUARE BRACKET
		case 0xDD: return 0x005D; //    CLOSING SQUARE BRACKET / RIGHT SQUARE BRACKET
		case 0xDF: return 0x005F; //    SPACING UNDERSCORE / LOW LINE
		case 0xE0: return 0x0490; //    UPPERCASE GE WITH UPTURN / CYRILLIC CAPITAL LETTER GHE WITH UPTURN
		case 0xE1: return 0x0402; //    UPPERCASE DJE / CYRILLIC CAPITAL LETTER DJE (Serbian)
		case 0xE2: return 0x0403; //    CYRILLIC CAPITAL LETTER GJE
		case 0xE3: return 0x0404; //    UPPERCASE E / CYRILLIC CAPITAL LETTER UKRAINIAN IE
		case 0xE4: return 0x0401; //    CYRILLIC CAPITAL LETTER IO
		case 0xE5: return 0x0405; //    CYRILLIC CAPITAL LETTER DZE
		case 0xE6: return 0x0406; //    UPPERCASE I / CYRILLIC CAPITAL LETTER BYELORUSSIAN-UKRANIAN
		case 0xE7: return 0x0407; //    UPPERCASE YI / CYRILLIC CAPITAL LETTER YI (Ukrainian)
		case 0xE8: return 0x0408; //    CYRILLIC CAPITAL LETTER JE
		case 0xE9: return 0x0409; //    CYRILLIC CAPITAL LETTER LJE
		case 0xEA: return 0x040A; //    CYRILLIC CAPITAL LETTER NJE
		case 0xEB: return 0x040B; //    UPPERCASE TSHE / CYRILLIC CAPITAL LETTER TSHE (Serbian)
		case 0xEC: return 0x040C; //    CYRILLIC CAPITAL LETTER KJE
		case 0xED: return 0x040E; //    UPPERCASE SHORT U / CYRILLIC CAPITAL LETTER SHORT U
		case 0xEE: return 0x040F; //    CYRILLIC CAPITAL LETTER DZHE
		case 0xEF: return 0x042A; //    CYRILLIC CAPITAL LETTER HARD SIGN
		case 0xF0: return 0x0462; //    CYRILLIC CAPITAL LETTER YAT
		case 0xF1: return 0x0472; //    CYRILLIC CAPITAL LETTER FITA
		case 0xF2: return 0x0474; //    CYRILLIC CAPITAL LETTER IZHITSA
		case 0xF3: return 0x046A; //    CYRILLIC CAPITAL LETTER BIG YUS
		default :
		    return (char)i;
		}
	}

    private static char getLatinChar(int i) {
		switch(i) {
		case 0x8D: return 0x200D;  // zero width joiner
		case 0x8E: return 0x200C;  // zero width non-joiner
		case 0xA1: return 0x0141;  // capital L with stroke
		case 0xA2: return 0x00D8;  // capital O with oblique stroke
		case 0xA3: return 0x0110;  // capital D with stroke
		case 0xA4: return 0x00DE;  // capital Icelandic letter Thorn
		case 0xA5: return 0x00C6;  // capital diphthong A with E
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
		case 0xB5: return 0x00E6;  // small diphthong a with e
		case 0xB6: return 0x0153;  // small ligature OE
		case 0xB7: return 0x02BA;  // modified letter double prime
		case 0xB8: return 0x0131;  // small dotless i
		case 0xB9: return 0x00A3;  // pound sign
		case 0xBA: return 0x00F0;  // small Icelandic letter Eth
		case 0xBC: return 0x01A1;  // small o with horn
		case 0xBD: return 0x01B0;  // small u with horn
		case 0xC0: return 0x00B0;  // degree sign, ring above
		case 0xC1: return 0x2113;  // script small l
		case 0xC2: return 0x2117;  // sound recording copyright
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
		case 0xF1: return 0x0328;  // ogonek
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
}
