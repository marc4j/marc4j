package org.marc4j.callnum;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.HashMap;
import java.util.Map;

/**
 * Provides utility functions to support call number manipulation.
 *
 * @author Tod Olson, University of Chicago
 * @author Naomi Dushay, Stanford University
 */

public class Utils {

    public enum State { START, WORD, GAP, NUM }
    public enum InType { LETTER, SPACE, PERIOD, PUNCT, DIGIT, OTHER, END }

    /**
     * Writes a numerically-sortable version of the input to the buffer.
     * 
     * The rules for production the numerically sortable sequence are:
     * <ul>
     * <li>Letters are translated to upper case</li>
     * <li>numeric sequences are prepended with the length of the sequence</li> 
     * <li>any other character is a word separator</li> 
     * <li>sequences of word separators are reduced to a single space</li> 
     * </ul>
     * 
     * <p>Prepending a sequence of digits with the number of digits ensures that they will easily sort numerically:
     * sort keys for 2-digits nubmers start with 2, sort keys for 3-digit numbers start with 3, etc. 
     * Suggested by John Craig at Code4Lib in Ashville, NC.
     * 
     * <p>Implemented as a finite state machine, modeled by <code>switch</code> statements.
     * 
     * @param buf       buffer for appending sortable version of the input
     * @param input     source character sequence
     */
    public static void appendNumericallySortable(StringBuilder buf, CharSequence input) {
        StringBuilder numBuf = new StringBuilder();
        State state = State.START;
        char c;
        InType inType = InType.END;

        for (int i = 0; i < input.length(); i++) {
            c = input.charAt(i);
            // Ugly C-style comparisons to avoid Unicode table lookups, 
            // should be fine for call numbers
            // TODO: remove unneeded input types
            if (c >= 'A' && c <= 'Z') {
                inType = InType.LETTER;
            } else if (c >= 'a' && c <= 'z') {
                inType = InType.LETTER;
                c = Character.toUpperCase(c);
            } else if (c >= '0' && c <= '9') {
                inType = InType.DIGIT;
            } else if (c == '.') {
                inType = InType.PERIOD;
            } else if (Character.isWhitespace(c)) {
                inType = InType.SPACE;
            } else if (c >= '!' && c <= '~') { 
                // Only consider ASCII-style punctuation,
                // have already eliminated digits and letters
                inType = InType.PUNCT;
            } else {
                inType = InType.OTHER;
            }

            switch (state) {
                case START:
                    switch (inType) {
                        case LETTER:
                            state = State.WORD;
                            buf.append(c);
                            break;
                        case DIGIT:
                            state = State.NUM;
                            numBuf.append(c);
                            break;
                        default:
                            // Consume anything else, remain in START state
                            break;
                    }
                    break;
                case WORD:      // Write word characters directly to buffer
                    switch (inType) {
                    case LETTER:
                        state = State.WORD;
                        buf.append(c);
                        break;
                    case DIGIT:
                        state = State.NUM;
                        buf.append(' ');
                        numBuf.append(c);
                        break;
                    default:
                        state = State.GAP;
                        buf.append(' ');
                        break;
                    }
                    break;
                case GAP:       // If we are in a gap, only letters or digits will take us out
                    switch (inType) {
                    case LETTER:
                        state = State.WORD;
                        buf.append(c);
                        break;
                    case DIGIT:
                        state = State.NUM;
                        numBuf.append(c);
                        break;
                    default:
                        // Consume anything else, remain in GAP state
                        break;
                    }
                    break;
                case NUM:       // accumulate number in special buffer, write sort version on state change
                    switch (inType) {
                    case DIGIT: // Stay in NUM state and accumulate another digit
                    case PERIOD:
                        numBuf.append(c);
                        break;
                    case LETTER:
                        state = State.WORD;
                        appendSortableNumber(buf, numBuf);
                        numBuf.setLength(0);
                        buf.append(c);
                        break;
                    default:
                        state = State.GAP;
                        appendSortableNumber(buf, numBuf);
                        numBuf.setLength(0);
                        buf.append(' ');
                        break;
                    }
                    break;
                default:
                    //TODO: Dryrot error?
                    break;
            }
        }
        // Remember any lingering number data
        if (state == State.NUM) {
            appendSortableNumber(buf, numBuf);
            numBuf.setLength(0);
        }
    }

    /**
     * Appends to a buffer a lexicographically sortable version of the number. 
     * The number may be an integer or may include a decimal.
     * 
     * @param buf       buffer to insert the sortable token
     * @param num       sequence of digits, possibly with decimal
     */
    public static void appendSortableNumber(StringBuilder buf, CharSequence num) {
        /*
        if (num.charAt(0) == '0') {
            int i = 0;
            while (i < num.length() && num.charAt(i) == '0') {
                i++;
            }
            buf.append(num.length() - i);
            buf.append(num.subSequence(i, num.length()));
        } else {
            buf.append(num.length());
            buf.append(num);
        }
        */
        // identify integer part
        int intStart = 0;
        int intEnd = 0;
        while (intStart < num.length() && num.charAt(intStart) == '0') {
            intStart++;
        }
        while (intEnd < num.length() && num.charAt(intEnd) >= '0' && num.charAt(intEnd) <= '9') {
            intEnd++;
        }
        // append length of integer part
        buf.append(intEnd - intStart);
        // append number without leading 0s
        buf.append(num.subSequence(intStart, num.length()));
    }

    public static String getCutterFromAuthor(String authorLastname)
    {
        StringBuilder sb = new StringBuilder();
        String uppername = authorLastname.toUpperCase().replaceAll("[^A-Z0-9]", "");
        char first = uppername.length() > 0 ? uppername.charAt(0) : ' ';

        char second = uppername.length() > 1 ? uppername.charAt(1) : ' ';
        char third = uppername.length() > 2 ? uppername.charAt(2) : ' ';
        switch (first)
        {
            case 'A': case 'E': case 'I': case 'O': case 'U':  
            {
                sb.append(first);
                if (second < 'B')                        sb.append('1');
                else if (second >= 'B' && second < 'D')  sb.append('2');
                else if (second >= 'D' && second < 'L')  sb.append('3');
                else if (second >= 'L' && second < 'N')  sb.append('4');
                else if (second >= 'N' && second < 'P')  sb.append('5');
                else if (second >= 'P' && second < 'R')  sb.append('6');
                else if (second >= 'R' && second < 'S')  sb.append('7');
                else if (second >= 'S' && second < 'U')  sb.append('8');
                else if (second >= 'U')                  sb.append('9');
                addCutterExpansion(sb, third);
                break;
            }
            case 'S':
            {
                sb.append(first);
                if (second < 'C' || (second == 'C' && third < 'H')) sb.append('2');
                else if (second >= 'C' && second < 'E')  sb.append('3');
                else if (second >= 'E' && second < 'H')  sb.append('4');
                else if (second >= 'H' && second < 'M')  sb.append('5');
                else if (second >= 'M' && second < 'T')  sb.append('6');
                else if (second >= 'T' && second < 'U')  sb.append('7');
                else if (second >= 'U' && second < 'W')  sb.append('8');
                else if (second >= 'W')                  sb.append('9');
                addCutterExpansion(sb, third);
                break;
            }
            case 'Q':
            {
                sb.append(first);
                if (second >= 'U' )
                {
                    if (third >= 'A' && third < 'E')        sb.append('3');
                    else if (third >= 'E' && third < 'I')   sb.append('4');
                    else if (third >= 'I' && third < 'O')   sb.append('5');
                    else if (third >= 'O' && third < 'R')   sb.append('6');
                    else if (third >= 'R' && third < 'T')   sb.append('7');
                    else if (third >= 'T' && third < 'Y')   sb.append('8');
                    else if (third >= 'Y')                  sb.append('9');
                    addCutterExpansion(sb, uppername.charAt(3));
                }
                else
                {
                    sb.append('2');
                    addCutterExpansion(sb, third);
                }
                break;
            }
            case '0': case '1': case '2': case '3': case '4': 
            case '5': case '6': case '7': case '8': case '9': 
            {
                sb.append("A1");
                sb.append(first);
                sb.append(second);
                break;
            }
            default:
            {
                sb.append(first);
                if (second >= 'A' && second < 'E')        sb.append('3');
                else if (second >= 'E' && second < 'I')   sb.append('4');
                else if (second >= 'I' && second < 'O')   sb.append('5');
                else if (second >= 'O' && second < 'R')   sb.append('6');
                else if (second >= 'R' && second < 'U')   sb.append('7');
                else if (second >= 'U' && second < 'Y')   sb.append('8');
                else if (second >= 'Y')                   sb.append('9');
                addCutterExpansion(sb, third);
            }
        }
        return(sb.toString());
    }

    private static void addCutterExpansion(StringBuilder sb, char third)
    {
        if (third >= 'A' && third < 'E')        sb.append('3');
        else if (third >= 'E' && third < 'I')   sb.append('4');
        else if (third >= 'I' && third < 'M')   sb.append('5');
        else if (third >= 'M' && third < 'P')   sb.append('6');
        else if (third >= 'P' && third < 'T')   sb.append('7');
        else if (third >= 'T' && third < 'W')   sb.append('8');
        else if (third >= 'W')                  sb.append('9');
    }

    /**
     * Given a latin letter with a diacritic, return the latin letter without
     * the diacritic.
     * <p>
     * Note that this is probaby U.S.-centric and should be localized.
     * <p>
     * Shamelessly stolen from UnicodeCharUtil class of UnicodeNormalizeFilter
     * by Bob Haschart, and copied here from {{org.solrmarc.tools.Utils}}.
     * 
     * @param c  character to fold
     * @return   character stripped of diacritic
     */
    public static char foldDiacriticLatinChar(char c)
    {
        switch (c) {
            case 0x0181:
                return (0x0042); // LATIN CAPITAL LETTER B WITH HOOK -> LATIN
                                 // CAPITAL LETTER B
            case 0x0182:
                return (0x0042); // LATIN CAPITAL LETTER B WITH TOPBAR -> LATIN
                                 // CAPITAL LETTER B
            case 0x0187:
                return (0x0043); // LATIN CAPITAL LETTER C WITH HOOK -> LATIN
                                 // CAPITAL LETTER C
            case 0x0110:
                return (0x0044); // LATIN CAPITAL LETTER D WITH STROKE -> LATIN
                                 // CAPITAL LETTER D
            case 0x018A:
                return (0x0044); // LATIN CAPITAL LETTER D WITH HOOK -> LATIN
                                 // CAPITAL LETTER D
            case 0x018B:
                return (0x0044); // LATIN CAPITAL LETTER D WITH TOPBAR -> LATIN
                                 // CAPITAL LETTER D
            case 0x0191:
                return (0x0046); // LATIN CAPITAL LETTER F WITH HOOK -> LATIN
                                 // CAPITAL LETTER F
            case 0x0193:
                return (0x0047); // LATIN CAPITAL LETTER G WITH HOOK -> LATIN
                                 // CAPITAL LETTER G
            case 0x01E4:
                return (0x0047); // LATIN CAPITAL LETTER G WITH STROKE -> LATIN
                                 // CAPITAL LETTER G
            case 0x0126:
                return (0x0048); // LATIN CAPITAL LETTER H WITH STROKE -> LATIN
                                 // CAPITAL LETTER H
            case 0x0197:
                return (0x0049); // LATIN CAPITAL LETTER I WITH STROKE -> LATIN
                                 // CAPITAL LETTER I
            case 0x0198:
                return (0x004B); // LATIN CAPITAL LETTER K WITH HOOK -> LATIN
                                 // CAPITAL LETTER K
            case 0x0141:
                return (0x004C); // LATIN CAPITAL LETTER L WITH STROKE -> LATIN
                                 // CAPITAL LETTER L
            case 0x019D:
                return (0x004E); // LATIN CAPITAL LETTER N WITH LEFT HOOK ->
                                 // LATIN CAPITAL LETTER N
            case 0x0220:
                return (0x004E); // LATIN CAPITAL LETTER N WITH LONG RIGHT LEG
                                 // -> LATIN CAPITAL LETTER N
            case 0x00D8:
                return (0x004F); // LATIN CAPITAL LETTER O WITH STROKE -> LATIN
                                 // CAPITAL LETTER O
            case 0x019F:
                return (0x004F); // LATIN CAPITAL LETTER O WITH MIDDLE TILDE ->
                                 // LATIN CAPITAL LETTER O
            case 0x01FE:
                return (0x004F); // LATIN CAPITAL LETTER O WITH STROKE AND ACUTE
                                 // -> LATIN CAPITAL LETTER O
            case 0x01A4:
                return (0x0050); // LATIN CAPITAL LETTER P WITH HOOK -> LATIN
                                 // CAPITAL LETTER P
            case 0x0166:
                return (0x0054); // LATIN CAPITAL LETTER T WITH STROKE -> LATIN
                                 // CAPITAL LETTER T
            case 0x01AC:
                return (0x0054); // LATIN CAPITAL LETTER T WITH HOOK -> LATIN
                                 // CAPITAL LETTER T
            case 0x01AE:
                return (0x0054); // LATIN CAPITAL LETTER T WITH RETROFLEX HOOK
                                 // -> LATIN CAPITAL LETTER T
            case 0x01B2:
                return (0x0056); // LATIN CAPITAL LETTER V WITH HOOK -> LATIN
                                 // CAPITAL LETTER V
            case 0x01B3:
                return (0x0059); // LATIN CAPITAL LETTER Y WITH HOOK -> LATIN
                                 // CAPITAL LETTER Y
            case 0x01B5:
                return (0x005A); // LATIN CAPITAL LETTER Z WITH STROKE -> LATIN
                                 // CAPITAL LETTER Z
            case 0x0224:
                return (0x005A); // LATIN CAPITAL LETTER Z WITH HOOK -> LATIN
                                 // CAPITAL LETTER Z
            case 0x0180:
                return (0x0062); // LATIN SMALL LETTER B WITH STROKE -> LATIN
                                 // SMALL LETTER B
            case 0x0183:
                return (0x0062); // LATIN SMALL LETTER B WITH TOPBAR -> LATIN
                                 // SMALL LETTER B
            case 0x0253:
                return (0x0062); // LATIN SMALL LETTER B WITH HOOK -> LATIN
                                 // SMALL LETTER B
            case 0x0188:
                return (0x0063); // LATIN SMALL LETTER C WITH HOOK -> LATIN
                                 // SMALL LETTER C
            case 0x0255:
                return (0x0063); // LATIN SMALL LETTER C WITH CURL -> LATIN
                                 // SMALL LETTER C
            case 0x0111:
                return (0x0064); // LATIN SMALL LETTER D WITH STROKE -> LATIN
                                 // SMALL LETTER D
            case 0x018C:
                return (0x0064); // LATIN SMALL LETTER D WITH TOPBAR -> LATIN
                                 // SMALL LETTER D
            case 0x0221:
                return (0x0064); // LATIN SMALL LETTER D WITH CURL -> LATIN
                                 // SMALL LETTER D
            case 0x0256:
                return (0x0064); // LATIN SMALL LETTER D WITH TAIL -> LATIN
                                 // SMALL LETTER D
            case 0x0257:
                return (0x0064); // LATIN SMALL LETTER D WITH HOOK -> LATIN
                                 // SMALL LETTER D
            case 0x0192:
                return (0x0066); // LATIN SMALL LETTER F WITH HOOK -> LATIN
                                 // SMALL LETTER F
            case 0x01E5:
                return (0x0067); // LATIN SMALL LETTER G WITH STROKE -> LATIN
                                 // SMALL LETTER G
            case 0x0260:
                return (0x0067); // LATIN SMALL LETTER G WITH HOOK -> LATIN
                                 // SMALL LETTER G
            case 0x0127:
                return (0x0068); // LATIN SMALL LETTER H WITH STROKE -> LATIN
                                 // SMALL LETTER H
            case 0x0266:
                return (0x0068); // LATIN SMALL LETTER H WITH HOOK -> LATIN
                                 // SMALL LETTER H
            case 0x0268:
                return (0x0069); // LATIN SMALL LETTER I WITH STROKE -> LATIN
                                 // SMALL LETTER I
            case 0x029D:
                return (0x006A); // LATIN SMALL LETTER J WITH CROSSED-TAIL ->
                                 // LATIN SMALL LETTER J
            case 0x0199:
                return (0x006B); // LATIN SMALL LETTER K WITH HOOK -> LATIN
                                 // SMALL LETTER K
            case 0x0142:
                return (0x006C); // LATIN SMALL LETTER L WITH STROKE -> LATIN
                                 // SMALL LETTER L
            case 0x019A:
                return (0x006C); // LATIN SMALL LETTER L WITH BAR -> LATIN SMALL
                                 // LETTER L
            case 0x0234:
                return (0x006C); // LATIN SMALL LETTER L WITH CURL -> LATIN
                                 // SMALL LETTER L
            case 0x026B:
                return (0x006C); // LATIN SMALL LETTER L WITH MIDDLE TILDE ->
                                 // LATIN SMALL LETTER L
            case 0x026C:
                return (0x006C); // LATIN SMALL LETTER L WITH BELT -> LATIN
                                 // SMALL LETTER L
            case 0x026D:
                return (0x006C); // LATIN SMALL LETTER L WITH RETROFLEX HOOK ->
                                 // LATIN SMALL LETTER L
            case 0x0271:
                return (0x006D); // LATIN SMALL LETTER M WITH HOOK -> LATIN
                                 // SMALL LETTER M
            case 0x019E:
                return (0x006E); // LATIN SMALL LETTER N WITH LONG RIGHT LEG ->
                                 // LATIN SMALL LETTER N
            case 0x0235:
                return (0x006E); // LATIN SMALL LETTER N WITH CURL -> LATIN
                                 // SMALL LETTER N
            case 0x0272:
                return (0x006E); // LATIN SMALL LETTER N WITH LEFT HOOK -> LATIN
                                 // SMALL LETTER N
            case 0x0273:
                return (0x006E); // LATIN SMALL LETTER N WITH RETROFLEX HOOK ->
                                 // LATIN SMALL LETTER N
            case 0x00F8:
                return (0x006F); // LATIN SMALL LETTER O WITH STROKE -> LATIN
                                 // SMALL LETTER O
            case 0x01FF:
                return (0x006F); // LATIN SMALL LETTER O WITH STROKE AND ACUTE
                                 // -> LATIN SMALL LETTER O
            case 0x01A5:
                return (0x0070); // LATIN SMALL LETTER P WITH HOOK -> LATIN
                                 // SMALL LETTER P
            case 0x02A0:
                return (0x0071); // LATIN SMALL LETTER Q WITH HOOK -> LATIN
                                 // SMALL LETTER Q
            case 0x027C:
                return (0x0072); // LATIN SMALL LETTER R WITH LONG LEG -> LATIN
                                 // SMALL LETTER R
            case 0x027D:
                return (0x0072); // LATIN SMALL LETTER R WITH TAIL -> LATIN
                                 // SMALL LETTER R
            case 0x0282:
                return (0x0073); // LATIN SMALL LETTER S WITH HOOK -> LATIN
                                 // SMALL LETTER S
            case 0x0167:
                return (0x0074); // LATIN SMALL LETTER T WITH STROKE -> LATIN
                                 // SMALL LETTER T
            case 0x01AB:
                return (0x0074); // LATIN SMALL LETTER T WITH PALATAL HOOK ->
                                 // LATIN SMALL LETTER T
            case 0x01AD:
                return (0x0074); // LATIN SMALL LETTER T WITH HOOK -> LATIN
                                 // SMALL LETTER T
            case 0x0236:
                return (0x0074); // LATIN SMALL LETTER T WITH CURL -> LATIN
                                 // SMALL LETTER T
            case 0x0288:
                return (0x0074); // LATIN SMALL LETTER T WITH RETROFLEX HOOK ->
                                 // LATIN SMALL LETTER T
            case 0x028B:
                return (0x0076); // LATIN SMALL LETTER V WITH HOOK -> LATIN
                                 // SMALL LETTER V
            case 0x01B4:
                return (0x0079); // LATIN SMALL LETTER Y WITH HOOK -> LATIN
                                 // SMALL LETTER Y
            case 0x01B6:
                return (0x007A); // LATIN SMALL LETTER Z WITH STROKE -> LATIN
                                 // SMALL LETTER Z
            case 0x0225:
                return (0x007A); // LATIN SMALL LETTER Z WITH HOOK -> LATIN
                                 // SMALL LETTER Z
            case 0x0290:
                return (0x007A); // LATIN SMALL LETTER Z WITH RETROFLEX HOOK ->
                                 // LATIN SMALL LETTER Z
            case 0x0291:
                return (0x007A); // LATIN SMALL LETTER Z WITH CURL -> LATIN
                                 // SMALL LETTER Z
            default:
                return (0x00);
        }
    }

    // Below authored by Naomi Dushay, pulled in from old CallNumUtils class
    
    private static Map<Character, Character> alphanumReverseMap = new HashMap<Character, Character>();
    static {
        alphanumReverseMap.put('0', 'Z');
        alphanumReverseMap.put('1', 'Y');
        alphanumReverseMap.put('2', 'X');
        alphanumReverseMap.put('3', 'W');
        alphanumReverseMap.put('4', 'V');
        alphanumReverseMap.put('5', 'U');
        alphanumReverseMap.put('6', 'T');
        alphanumReverseMap.put('7', 'S');
        alphanumReverseMap.put('8', 'R');
        alphanumReverseMap.put('9', 'Q');
        alphanumReverseMap.put('A', 'P');
        alphanumReverseMap.put('B', 'O');
        alphanumReverseMap.put('C', 'N');
        alphanumReverseMap.put('D', 'M');
        alphanumReverseMap.put('E', 'L');
        alphanumReverseMap.put('F', 'K');
        alphanumReverseMap.put('G', 'J');
        alphanumReverseMap.put('H', 'I');
        alphanumReverseMap.put('I', 'H');
        alphanumReverseMap.put('J', 'G');
        alphanumReverseMap.put('K', 'F');
        alphanumReverseMap.put('L', 'E');
        alphanumReverseMap.put('M', 'D');
        alphanumReverseMap.put('N', 'C');
        alphanumReverseMap.put('O', 'B');
        alphanumReverseMap.put('P', 'A');
        alphanumReverseMap.put('Q', '9');
        alphanumReverseMap.put('R', '8');
        alphanumReverseMap.put('S', '7');
        alphanumReverseMap.put('T', '6');
        alphanumReverseMap.put('U', '5');
        alphanumReverseMap.put('V', '4');
        alphanumReverseMap.put('W', '3');
        alphanumReverseMap.put('X', '2');
        alphanumReverseMap.put('Y', '1');
        alphanumReverseMap.put('Z', '0');
    }

    /** this character will sort first */
    public static char SORT_FIRST_CHAR = Character.MIN_VALUE;
    public static StringBuilder reverseDefault = new StringBuilder(75);
    static {
        for (int i = 0; i < 50; i++)
// N.B.:  this char is tough to deal with in a variety of contexts.
// Hopefully diacritics and non-latin won't bite us in the butt.
//          reverseDefault.append(Character.toChars(Character.MAX_CODE_POINT));
            reverseDefault.append(Character.toChars('~'));
    }

    /**
     * given a shelfkey (a lexicaly sortable call number), return the reverse
     * shelf key - a sortable version of the call number that will give the
     * reverse order (for getting "previous" call numbers in a list)
     *
     * @param shelfkey  forward-sortable shelfkey
     * @return          reverse-sortable shelfkey
     */
    public static String getReverseShelfKey(String shelfkey) {
        StringBuilder resultBuf = new StringBuilder(reverseDefault);
        if (shelfkey != null && shelfkey.length() > 0)
            resultBuf.replace(0, shelfkey.length(), reverseAlphanum(shelfkey));
        return resultBuf.toString();
    }

    /**
     * return the reverse String value, mapping A --> 9, B --> 8, ...
     * 9 --> A and also non-alphanum to sort properly (before or after alphanum)
     *
     * @param orig  original string
     * @return      reversed string
     */
    static String reverseAlphanum(String orig) {
/*
        char[] origArray = orig.toCharArray();

        char[] reverse = new char[origArray.length];
        for (int i = 0; i < origArray.length; i++) {
            Character ch = origArray[i];
            if (ch != null) {
                if (Character.isLetterOrDigit(ch))
                    reverse[i] = alphanumReverseMap.get(ch);
                else
                    reverse[i] = reverseNonAlphanum(ch);
            }
        }
*/
        StringBuilder reverse = new StringBuilder();
        for (int ix = 0; ix < orig.length(); ) {
            int codePoint = Character.toUpperCase(orig.codePointAt(ix));
            char[] chs = Character.toChars(codePoint);

            if (Character.isLetterOrDigit(codePoint)) {
                if (chs.length == 1) {
                    char c = chs[0];
                    if (alphanumReverseMap.containsKey(c))
                        reverse.append(alphanumReverseMap.get(c));
                    else {
                        // not an ASCII letter or digit

                        // map latin chars with diacritic to char without
                        char foldC;

                        if (Character.UnicodeBlock.of(c) != Character.UnicodeBlock.COMBINING_DIACRITICAL_MARKS &&
                            Character.UnicodeBlock.of(c) != Character.UnicodeBlock.SPACING_MODIFIER_LETTERS &&
                             (foldC = foldDiacriticLatinChar(c)) != 0x00)
                            // we mapped a latin char w diacritic to plain ascii
                            reverse.append(alphanumReverseMap.get(foldC));
                        else
                            // single char, but non-latin, non-digit
                            // ... view it as after Z in regular alphabet, for now
                            reverse.append(SORT_FIRST_CHAR);
                    }
                }
                else  {
                    // multiple 16 bit character unicode letter
                    // ... view it as after Z in regular alphabet, for now
                    reverse.append(SORT_FIRST_CHAR);
                }
            }
            else // not a letter or a digit
                reverse.append(reverseNonAlphanum(chs[0]));

            ix += chs.length;
        }

        return new String(reverse);
    }

    /**
     * for non alpha numeric characters, return a character that will sort
     * first or last, whichever is the opposite of the original character.
     *
     * @param ch  original character
     * @return    character that sorts opposite of input
     */
    public static char[] reverseNonAlphanum(char ch) {
        // use punctuation before or after alphanum as appropriate
        switch (ch) {
            case '.':
                return Character.toChars('}');
            case '{':
            case '|':
            case '}':
            case '~':
// N.B.:  these are tough to deal with in a variety of contexts.
// Hopefully diacritics and non-latin won't bite us in the butt.
//              return Character.toChars(Character.MIN_CODE_POINT);
                return Character.toChars(' ');
            default:
//              return Character.toChars(Character.MAX_CODE_POINT);
                return Character.toChars('~');
        }
    }

}
