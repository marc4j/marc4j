// $Id: AnselToUnicode.java,v 1.6 2002/08/12 20:24:46 bpeters Exp $
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

/**
 * <p><code>AnselToUnicode</code> is a utility to convert
 * MARC-8 data to UCS/Unicode.   </p>
 *
 * This class currently implements
 * <a href="http://www.loc.gov/marc/specifications/speccharlatin.html">
 * Code Table 1: Latin</a>,
 * <a href="http://www.loc.gov/marc/specifications/specchargrsymbols.html">
 * Code Table 2: Greek symbols</a>,
 * <a href="http://www.loc.gov/marc/specifications/speccharsubscript.html">
 * Code Table 3: Subscripts</a>,
 * <a href="http://www.loc.gov/marc/specifications/speccharsuperscript.html">
 * Code Table 4: Superscripts</a>
 * as published by the
 * <a href="http://www.loc.gov/marc/specifications/specchartables.html">
 * Library of Congress</a>.
 * If possible UCS/Unicode non-spacing characters are together with their
 * base character converted to UCS/Unicode combining characters.</p>
 *
 * @author <a href="mailto:mail@bpeters.com">Bas Peters</a>
 * @version $Version$
 *
 */
public class AnselToUnicode {

    /** Escape character. */
    protected static final char ESCAPE = 0x001B;

    /** Redesignate default character set.  */
    protected static final char REDESIGNATE = 0x0073;

    /** Designate greek symbols. */
    protected static final char GREEK = 0x0067;

    /** Designate subscript characters. */
    protected static final char SUBSCRIPT = 0x0070;

    /** Designate superscript characters. */
    protected static final char SUPERSCRIPT = 0X062;

    /**
     * <p>Converts MARC-8 data to UCS/Unicode.    </p>
     *
     * <p>The <code>convert</code> method performs the following steps:</p>
     * <ul>
     * <li>If the character is a reserved character in the MARC-8 environment
     * the character is not converted.</li>
     * <li>If the character is an escape character, the next graphic character
     * is evaluated:
     * <ul>
     * <li>If s (0x0073) - the default character set is redesignated
     * <li>If g (0x0067) - the Greek Symbols are designated
     * <li>If b (0x0062) - the subscript characters are designated
     * <li>If p (0x0070) - the superscript characters are desingated
     * </ul>
     * If there is no match for an alternate graphic character set,
     * the characters are returned.
     * <br>The alternate graphic character set is designated until another escape
     * sequence is encountered.
     * <br>If there is no match for characters within the alternate graphic
     * character set, the characters are not converted.
     * <li>If the character is not an ASCII character it is converted
     * to UCS/Unicode.</li>
     * <li>If the UCS/Unicode character is a combining diacritical mark
     * and it's position is not the last position within the field it is
     * converted together with the next character to a combining
     * Unicode character.</li>
     * <li>If the mapping for combining characters does not return
     * a combining character, the combining diacritical mark and
     * the base character are re-ordered so that the base character
     * precedes the combining diacritical mark.</li>
     * <li>If the converted character is not a combining diacritical mark and/or
     * the last character it is returned.</li>
     * </ul>
     * <br>
     *
     * @param data the input data
     * @return <code>char[]</code> - the output data
     * @see #getChar
     * @see #isReserved
     * @see Ascii#isValid
     * @see Unicode#isCombiningDiacriticalMark
     * @see Unicode#getCombiningChar
     */
    public static char[] convert(char[] data) {

        // Indicator for an alternate graphic character set.
        char alternate = 0;
	
	StringBuffer sb = new StringBuffer();
	for(int i=0;i < data.length;i++) {
            char c = data[i];
            int len = data.length;

            // Throw exception if the character is a reserved character.
            if (isReserved(c)) {
                //throw new ReservedCharacterException(c);
                sb.append(c);
		
            // If the character is an ESCAPE check the alternate
            // graphic character set.
            } else if (c == ESCAPE  && hasNext(i, len)) {
                char d = designate(data[i + 1]);
                // Check the alternate character.
                switch (d) {
	        // If zero append escape character.
                case 0 :
                    sb.append(c);
                    break;
                // If redesignate check current alternate character.
                case REDESIGNATE :
                    switch (alternate) {
                    // If zero append escape since there is no active
                    // alternate character set.
                    case 0 :
                        sb.append(c);
                        break;
                    // In all other cases set the alternate to it's
                    // initial value.
                    default :
                        alternate = 0;
                        i++;
                        break;
                    }
                    break;
                // If not zero or redesignate, set the alternate
                // character set.
                default :
                    alternate = d;
                    i++;
                    break;
                }

            // If an alternate character set is active
            // get alternate graphic character.
            } else if (alternate != 0) {
                char d = getAlternateChar(alternate, c);
                switch(d) {
                case 0 :
                    sb.append(c);
                    break;
                default :
                    sb.append(d);
                    break;
                }
		
	    // If the character is an ASCII character append to buffer.
	    } else if (Ascii.isValid(c)) {
		sb.append(c);

            // Else get the unicode character.
	    } else {
		char d = getChar(c);

		// Check if character is a diacritic and
		// if there is a next character.
		if (Unicode.isCombiningDiacriticalMark(d) && hasNext(i, len)) {
                    char e;
		    if (Ascii.isValid(data[i + 1]))
			e = data[i + 1];
		    else
			e = getChar(data[i + 1]);
		    char f = Unicode.getCombiningChar(d, e);

                    switch(f) {
                    case 0 :
                        sb.append(e);
                        sb.append(d);
                        break;
                    default :
                        sb.append(f);
                        break;
                    }
                    i++;

		// If not a combining character or last in a field
	        // append the character returned by getChar().
		} else {
                    sb.append(d);
		} // End of if (isNonspacingChar()) loop
		
	    } // End of if (isASCII()) loop
	    
	} // End of for loop
	return sb.toString().toCharArray();
    }

    /**
     * <p>Designates the alternate graphic character set.   </p>
     *
     * @param c the code for the alternate graphic character set
     * @return <code>char</code> - returns the code for the alternate character set,
     *                             or the character to redesignate the default
     *                             character set,
     *                             returns 0 if there is no match.
     */
    private static char designate(char c) {
        switch(c) {
        case GREEK :
            return GREEK;
        case SUBSCRIPT :
            return SUBSCRIPT;
        case SUPERSCRIPT :
            return SUPERSCRIPT;
        case REDESIGNATE :
            return REDESIGNATE;
        default :
            return 0;
        }
    }

    /**
     * <p>Returns an alternate graphic character.   </p>
     *
     * @param alternate the code for the alternate character set
     * @param c the character to convert
     * @return <code>char</code> - returns the converted character,
     *                           returns 0 if the character is not converted.
     */
    private static char getAlternateChar(char alternate, char c) {
        switch(alternate) {
        case GREEK :
            return getGreekSymbol(c);
        case SUBSCRIPT :
            return getSubscript(c);
        case SUPERSCRIPT :
            return getSuperscript(c);
        default :
            return 0;
        }
    }

    /**
     * <p>Converts a single MARC-8 character to it's UCS/Unicode
     * equivalent.         </p>
     *
     * <p>This method implements the following mapping between MARC-8
     * and UCS/Unicode:</p>
     * <pre>
     * MARC  UCS
     * (hex) (hex)   MARC / UCS NAME
     * 8D    200D    JNR (Joiner) / ZERO WIDTH JOINER
     * 8E    200C    NJR (Non-joiner) / ZERO WIDTH NON-JOINER
     * A1    0141    UPPERCASE POLISH L / LATIN CAPITAL LETTER L WITH STROKE
     * A2    00D8    UPPERCASE SCANDINAVIAN O / LATIN CAPITAL LETTER O WITH STROKE
     * A3    0110    UPPERCASE D WITH CROSSBAR / LATIN CAPITAL LETTER D WITH STROKE
     * A4    00DE    UPPERCASE ICELANDIC THORN / LATIN CAPITAL LETTER THORN (Icelandic)
     * A5    00C6    UPPERCASE DIGRAPH AE / LATIN CAPITAL LIGATURE AE
     * A6    0152    UPPERCASE DIGRAPH OE / LATIN CAPITAL LIGATURE OE
     * A7    02B9    SOFT SIGN, PRIME / MODIFIER LETTER PRIME
     * A8    00B7    MIDDLE DOT
     * A9    266D    MUSIC FLAT SIGN
     * AA    00AE    PATENT MARK / REGISTERED SIGN
     * AB    00B1    PLUS OR MINUS / PLUS-MINUS SIGN
     * AC    01A0    UPPERCASE O-HOOK / LATIN CAPITAL LETTER O WITH HORN
     * AD    01AF    UPPERCASE U-HOOK / LATIN CAPITAL LETTER U WITH HORN
     * AE    02BE    ALIF / MODIFIER LETTER RIGHT HALF RING
     * B0    02BB    AYN / MODIFIER LETTER TURNED COMMA
     * B1    0142    LOWERCASE POLISH L / LATIN SMALL LETTER L WITH STROKE
     * B2    00F8    LOWERCASE SCANDINAVIAN O / LATIN SMALL LETTER O WITH STROKE
     * B3    0111    LOWERCASE D WITH CROSSBAR / LATIN SMALL LETTER D WITH STROKE
     * B4    00FE    LOWERCASE ICELANDIC THORN / LATIN SMALL LETTER THORN (Icelandic)
     * B5    00E6    LOWERCASE DIGRAPH AE / LATIN SMALL LIGATURE AE
     * B6    0153    LOWERCASE DIGRAPH OE / LATIN SMALL LIGATURE OE
     * B7    02BA    HARD SIGN, DOUBLE PRIME / MODIFIER LETTER DOUBLE PRIME
     * B8    0131    LOWERCASE TURKISH I / LATIN SMALL LETTER DOTLESS I
     * B9    00A3    BRITISH POUND / POUND SIGN
     * BA    00F0    LOWERCASE ETH / LATIN SMALL LETTER ETH (Icelandic)
     * BC    01A1    LOWERCASE O-HOOK / LATIN SMALL LETTER O WITH HORN
     * BD    01B0    LOWERCASE U-HOOK / LATIN SMALL LETTER U WITH HORN
     * C0    00B0    DEGREE SIGN
     * C1    2113    SCRIPT SMALL L
     * C2    2117    SOUND RECORDING COPYRIGHT
     * C3    00A9    COPYRIGHT SIGN
     * C4    266F    MUSIC SHARP SIGN
     * C5    00BF    INVERTED QUESTION MARK
     * C6    00A1    INVERTED EXCLAMATION MARK
     * E0    0309    PSEUDO QUESTION MARK / COMBINING HOOK ABOVE
     * E1    0300    GRAVE / COMBINING GRAVE ACCENT (Varia)
     * E2    0301    ACUTE / COMBINING ACUTE ACCENT (Oxia)
     * E3    0302    CIRCUMFLEX / COMBINING CIRCUMFLEX ACCENT
     * E4    0303    TILDE / COMBINING TILDE
     * E5    0304    MACRON / COMBINING MACRON
     * E6    0306    BREVE / COMBINING BREVE (Vrachy)
     * E7    0307    SUPERIOR DOT / COMBINING DOT ABOVE
     * E8    0308    UMLAUT, DIAERESIS / COMBINING DIAERESIS (Dialytika)
     * E9    030C    HACEK / COMBINING CARON
     * EA    030A    CIRCLE ABOVE, ANGSTROM / COMBINING RING ABOVE
     * EB    FE20    LIGATURE, FIRST HALF / COMBINING LIGATURE LEFT HALF
     * EC    FE21    LIGATURE, SECOND HALF / COMBINING LIGATURE RIGHT HALF
     * ED    0315    HIGH COMMA, OFF CENTER / COMBINING COMMA ABOVE RIGHT
     * EE    030B    DOUBLE ACUTE / COMBINING DOUBLE ACUTE ACCENT
     * EF    0310    CANDRABINDU / COMBINING CANDRABINDU
     * F0    0327    CEDILLA / COMBINING CEDILLA
     * F1    0328    RIGHT HOOK, OGONEK / COMBINING OGONEK
     * F2    0323    DOT BELOW / COMBINING DOT BELOW
     * F3    0324    DOUBLE DOT BELOW / COMBINING DIAERESIS BELOW
     * F4    0325    CIRCLE BELOW / COMBINING RING BELOW
     * F5    0333    DOUBLE UNDERSCORE / COMBINING DOUBLE LOW LINE
     * F6    0332    UNDERSCORE / COMBINING LOW LINE
     * F7    0326    LEFT HOOK (COMMA BELOW) / COMBINING COMMA BELOW
     * F8    031C    RIGHT CEDILLA / COMBINING LEFT HALF RING BELOW
     * F9    032E    UPADHMANIYA / COMBINING BREVE BELOW
     * FA    FE22    DOUBLE TILDE, FIRST HALF / COMBINING DOUBLE TILDE LEFT HALF
     * FB    FE23    DOUBLE TILDE, SECOND HALF / COMBINING DOUBLE TILDE RIGHT HALF
     * FE    0313    HIGH COMMA, CENTERED / COMBINING COMMA ABOVE (Psili)
     * </pre>
     *
     * @param c the character to be converted
     * @return <code>char</code> - returns the converted character,
     *                           returns 0 if the character is not converted.
     * @see com.bpeters.util.Unicode
     */
    public static char getChar(char c) {
	    switch (c) {
	    // JNR (Joiner) [?]
	    case 0x008D :
	        return 0x200D;
	    // NJR (Non-joiner) [?]
	    case 0x008E :
	        return 0x200C;
	    // latin capital letter l with stroke
	    case 0x00A1 :
	        return 0x0141;
	    // latin capital letter o with stroke
	    case 0x00A2 :
	        return 0x00D8;
	    // latin capital letter d with stroke
	    case 0x00A3 :
	        return 0x0110;
	    // latin capital letter thorn
	    case 0x00A4 :
	        return 0x00DE;
	    // latin capital letter AE
	    case 0x00A5 :
	        return 0x00C6;
	    // latin capital letter OE
	    case 0x00A6 :
	        return 0x0152;
	    // modifier letter prime
	    case 0x00A7 :
	        return 0x02B9;
	    // middle dot
	    case 0x00A8 :
	        return 0x00B7;
	    // musical flat sign
	    case 0x00A9 :
	        return 0x266D;
	    // registered sign
	    case 0x00AA :
	        return 0x00AE;
	    // plus-minus sign
	    case 0x00AB :
	        return 0x00B1;
	    // latin capital letter o with horn
	    case 0x00AC :
	        return 0x01A0;
	    // latin capital letter u with horn
	    case 0x00AD :
	        return 0x01AF;
	    // modifier letter right half ring
	    case 0x00AE :
	        return 0x02BE;

	    // modifier letter left half ring
	    case 0x00B0 :
	        return 0x02BF;
	    // latin small letter l with stroke
	    case 0x00B1 :
	        return 0x0142;
	    // latin small letter o with stroke
	    case 0x00B2 :
	        return 0x00F8;
	    // latin small letter d with stroke
	    case 0x00B3 :
	        return 0x0111;
	    // latin small letter thorn
	    case 0x00B4 :
	        return 0x00FE;
	    // latin small letter ae
	    case 0x00B5 :
	        return 0x00E6;
	    // latin small letter oe
	    case 0x00B6 :
	        return 0x009C;
	    // modifier letter double prime
	    case 0x00B7 :
	        return 0x02BA;
	    // latin small letter dotless i
	    case 0x00B8 :
		return 0x0131;
	    // pound sign
	    case 0x00B9 :
	        return 0x00A3;
	    // latin small letter eth
	    case 0x00BA :
	        return 0x00F0;
	    // latin small letter o with horn
	    case 0x00BC :
	        return 0x01A1;
	    // latin small letter u with horn
	    case 0x00BD :
	        return 0x01B0;

	    // degree sign
	    case 0x00C0 :
	        return 0x00B0;
	    // script small l
	    case 0x00C1 :
	        return 0x2113;
	    // sound recording copyright
	    case 0x00C2 :
	        return 0x2117;
	    // copyright sign
	    case 0x00C3 :
	        return 0x00A9;
	    // sharp
	    case 0x00C4 :
	        return 0x266F;
	    // inverted question mark
	    case 0x00C5 :
	        return 0x00BF;
	    // inverted exclamation mark
	    case 0x00C6 :
	        return 0x00A1;

	    // combining hook above
	    case 0x00E0 :
	        return 0x0309;
	    // combining grave accent
	    case 0x00E1 :
	        return 0x0300;
	    // combining acute accent
	    case 0x00E2 :
	        return 0x0301;
	    // combining circumflex
	    case 0x00E3 :
	        return 0x0302;
	    // combining tilde
	    case 0x00E4 :
	        return 0x0303;
	    // combining macron
	    case 0x00E5 :
	        return 0x0304;
	    // combining breve
	    case 0x00E6 :
	        return 0x0306;
	    // combining dot above
	    case 0x00E7 :
	        return 0x0307;
	    // combining diaeresis
	    case 0x00E8 :
	        return 0x0308;
	    // combining caron
	    case 0x00E9 :
	        return 0x030C;
	    // combining ring above
	    case 0x00EA :
	        return 0x030A;
	    // combining ligature left half
	    case 0x00EB :
	        return 0xFE20;
	    // combining ligature right half
	    case 0x00EC :
	        return 0xFE21;
	    // combining comma above right
	    case 0x00ED :
	        return 0x0315;
	    // combining double acute accent
	    case 0x00EE :
	        return 0x030B;
	    // combining candrabindu
	    case 0x00EF :
	        return 0x0310;

	    // combining cedilla
	    case 0x00F0 :
	        return 0x0327;
	    // combining ogonek
	    case 0x00F1 :
	        return 0x0328;
	    // combining dot below
	    case 0x00F2 :
	        return 0x0323;
	    // combining double dot below
	    case 0x00F3 :
	        return 0x0324;
	    // combining ring below
	    case 0x00F4 :
	        return 0x0325;
	    // combining double low line
	    case 0x00F5 :
	        return 0x0333;
	    // combining low line
	    case 0x00F6 :
	        return 0x0332;
	    // combining comma below
	    case 0x00F7 :
	        return 0x0326;
	    // combining left half ring below
	    case 0x00F8 :
	        return 0x031C;
	    // combining breve below
	    case 0x00F9 :
	        return 0x032E;
	    // combining double tilde left half
	    case 0x00FA :
	        return 0xFE22;
	    // combining double tilde right half
	    case 0x00FB :
	        return 0xFE23;
	    // combining comma above
	    case 0x00FE :
	        return 0x0313;

	    default:
	        return 0;
    	}
    }

    /**
     * <p>Converts a single MARC-8 Greek symbol to it's
     * UCS/Unicode equivalent.   </p>
     *
     * <pre>
     * MARC  UCS
     * (hex) (hex)   MARC / UCS NAME
     * 61    03B1    GREEK SMALL LETTER ALPHA
     * 62    03B2    GREEK SMALL LETTER BETA
     * 63    03B3    GREEK SMALL LETTER GAMMA
     * </pre>
     *
     * @param c the character to be converted
     * @return <code>char</code> - returns the converted character,
     *                           returns 0 if the character is not converted.
     */
    public static char getGreekSymbol(char c) {
        switch(c) {
        case 0x0061 :
            return 0x03B1;
        case 0x0062 :
            return 0x03B2;
        case 0x0063 :
            return 0x03B3;
        default :
            return 0;
        }
    }

    /**
     * <p>Converts a single MARC-8 subscript character to it's
     * UCS/Unicode equivalent.   </p>
     *
     * <pre>
     * MARC  UCS
     * (hex) (hex)   MARC / UCS NAME
     * 28    208D    SUBSCRIPT OPENING PARENTHESIS / SUBSCRIPT LEFT PARENTHESIS
     * 29    208E    SUBSCRIPT CLOSING PARENTHESIS / SUBSCRIPT RIGHT PARENTHESIS
     * 2B    208A    SUBSCRIPT PLUS SIGN
     * 2D    208B    SUBSCRIPT HYPHEN-MINUS / SUBSCRIPT MINUS
     * 30    2080    SUBSCRIPT DIGIT ZERO
     * 31    2081    SUBSCRIPT DIGIT ONE
     * 32    2082    SUBSCRIPT DIGIT TWO
     * 33    2083    SUBSCRIPT DIGIT THREE
     * 34    2084    SUBSCRIPT DIGIT FOUR
     * 35    2085    SUBSCRIPT DIGIT FIVE
     * 36    2086    SUBSCRIPT DIGIT SIX
     * 37    2087    SUBSCRIPT DIGIT SEVEN
     * 38    2088    SUBSCRIPT DIGIT EIGHT
     * 39    2089    SUBSCRIPT DIGIT NINE
     * </pre>
     *
     * @param c the character to be converted
     * @return <code>char</code> - returns the converted character,
     *                           returns 0 if the character is not converted.
     */
    public static char getSubscript(char c) {
        switch(c) {
        case 0x0028 :
            return 0x208D;
        case 0x0029 :
            return 0x208E;
        case 0x002B :
            return 0x208A;
        case 0x002D :
            return 0x208B;
        case 0x0030 :
            return 0x2080;
        case 0x0031 :
            return 0x2081;
        case 0x0032 :
            return 0x2082;
        case 0x0033 :
            return 0x2083;
        case 0x0034 :
            return 0x2084;
        case 0x0035 :
            return 0x2085;
        case 0x0036 :
            return 0x2086;
        case 0x0037 :
            return 0x2087;
        case 0x0038 :
            return 0x2088;
        case 0x0039 :
            return 0x2089;
        default :
            return 0;
        }
    }

    /**
     * <p>Converts a single MARC-8 superscript character to it's
     * UCS/Unicode equivalent.   </p>
     *
     * <pre>
     * MARC  UCS
     * (hex) (hex)   MARC / UCS NAME
     *
     * 28    207D    SUPERSCRIPT OPENING PARENTHESIS / SUPERSCRIPT LEFT PARENTHESIS
     * 29    207E    SUPERSCRIPT CLOSING PARENTHESIS / SUPERSCRIPT RIGHT PARENTHESIS
     * 2B    207A    SUPERSCRIPT PLUS SIGN
     * 2D    207B    SUPERSCRIPT HYPHEN-MINUS / SUPERSCRIPT MINUS
     * 30    2070    SUPERSCRIPT DIGIT ZERO
     * 31    00B9    SUPERSCRIPT DIGIT ONE
     * 32    00B2    SUPERSCRIPT DIGIT TWO
     * 33    00B3    SUPERSCRIPT DIGIT THREE
     * 34    2074    SUPERSCRIPT DIGIT FOUR
     * 35    2075    SUPERSCRIPT DIGIT FIVE
     * 36    2076    SUPERSCRIPT DIGIT SIX
     * 37    2077    SUPERSCRIPT DIGIT SEVEN
     * 38    2078    SUPERSCRIPT DIGIT EIGHT
     * 39    2079    SUPERSCRIPT DIGIT NINE
     * </pre>
     *
     * @param c the character to be converted
     * @return <code>char</code> - returns the converted character,
     *                           returns 0 if the character is not converted.
     */
    public static char getSuperscript(char c) {
        switch(c) {
        case 0x0028 :
            return 0x207D;
        case 0x0029 :
            return 0x207E;
        case 0x002B :
            return 0x207A;
        case 0x002D :
            return 0x207B;
        case 0x0030 :
            return 0x2070;
        case 0x0031 :
            return 0x00B9;
        case 0x0032 :
            return 0x00B2;
        case 0x0033 :
            return 0x00B3;
        case 0x0034 :
            return 0x2074;
        case 0x0035 :
            return 0x2075;
        case 0x0036 :
            return 0x2076;
        case 0x0037 :
            return 0x2077;
        case 0x0038 :
            return 0x2078;
        case 0x0039 :
            return 0x2079;
        default :
            return 0;
        }
    }

    /**
     * <p>Returns true if the character position is not the last
     * position within the character array.   </p>
     *
     * <p>This method is used to check if a particular MARC-8 non-spacing
     * character has a base character.</p>
     *
     * @param position the character position integer
     * @param length the length of the character array
     * @return <code>boolean</code> - true if the character is not the last character,
     *                                false if the character is the last character
     */
    public static boolean hasNext(int position, int length) {
	    if (position < (length -1))
	        return true;
	    return false;
    }

    /**
     * <p>Returns true if the character is a reserved character.   </p>
     *
     * <p>This method implements the following character map:</p>
     * <pre>
     * MARC
     * (hex)
     * 00-1A         [RESERVED]
     * 1C            [RESERVED]
     * 7F-87         [RESERVED]
     * 8A-8C         [RESERVED]
     * 8F-A0         [RESERVED]
     * AF            [RESERVED]
     * BB            [RESERVED]
     * BE            [RESERVED]
     * BF            [RESERVED]
     * C7-DF         [RESERVED]
     * FC            [RESERVED]
     * FD            [RESERVED]
     * FF            [RESERVED]
     * </pre>
     *
     * @param <code>char</code> c the character to validate
     * @return <code>boolean</code> - true if the character is a reserved character,
     *                                false if the character is not a reserved character
     */
    public static boolean isReserved(char c) {
	    switch(c) {
        case 0x0000 :
        case 0x0001 :
        case 0x0002 :
        case 0x0003 :
        case 0x0004 :
        case 0x0005 :
        case 0x0006 :
        case 0x0007 :
        case 0x0008 :
        case 0x0009 :
        case 0x000A :
        case 0x000B :
        case 0x000C :
        case 0x000D :
        case 0x000E :
        case 0x000F :
        case 0x0010 :
        case 0x0011 :
        case 0x0012 :
        case 0x0013 :
        case 0x0014 :
        case 0x0015 :
        case 0x0016 :
        case 0x0017 :
        case 0x0018 :
        case 0x0019 :
        case 0x001A :
        case 0x001C :
        case 0x007F :
        case 0x0080 :
        case 0x0081 :
        case 0x0082 :
        case 0x0083 :
        case 0x0084 :
        case 0x0085 :
        case 0x0086 :
        case 0x0087 :
        case 0x008A :
        case 0x008B :
        case 0x008C :
        case 0x008F :
        case 0x0090 :
        case 0x0091 :
        case 0x0092 :
        case 0x0093 :
        case 0x0094 :
        case 0x0095 :
        case 0x0096 :
        case 0x0097 :
        case 0x0098 :
        case 0x0099 :
        case 0x009A :
        case 0x009B :
        case 0x009C :
        case 0x009D :
        case 0x009E :
        case 0x009F :
        case 0x00A0 :
        case 0x00AF :
        case 0x00BB :
        case 0x00BE :
        case 0x00BF :
        case 0x00C7 :
        case 0x00C8 :
        case 0x00C9 :
        case 0x00CA :
        case 0x00CB :
        case 0x00CC :
        case 0x00CD :
        case 0x00CE :
        case 0x00CF :
        case 0x00D0 :
        case 0x00D1 :
        case 0x00D2 :
        case 0x00D3 :
        case 0x00D4 :
        case 0x00D5 :
        case 0x00D6 :
        case 0x00D7 :
        case 0x00D8 :
        case 0x00D9 :
        case 0x00DA :
        case 0x00DB :
        case 0x00DC :
        case 0x00DD :
        case 0x00DE :
        case 0x00DF :
        case 0x00FC :
        case 0x00FD :
        case 0x00FF :
            break;
        default :
            return false;
        }
        return true;
    }

}

// End of AnselToUnicode.java
