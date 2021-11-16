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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses and computes sort keys for Library of Congress call numbers.
 * 
 * <p>The purpose of this class is to parse LC call numbers well and produce useful sort keys.
 * Often there are local extensions to LC call numbers, so we are loose with pattern matching.
 * It is also common for for non-LC call numbers to be coded as LC,
 * so this class also tries to sensibly handle such input, and compute a sortkey 
 * that will file the call number in a place that will make sense to the user.
 * 
 * <p>Parsing the call number
 * 
 * <p>The call number is parsed as follows
 * 
 * <table>
 * <caption>Parsing the call number</caption>
 * <tr><th>{@code classification}</th><td>everything before the first cutter</td></tr>
 * <tr><th>{@code classLetters}</th><td>leading sequence of letters</td></tr>
 * <tr><th>{@code classDigits}</th><td>following digits</td></tr>
 * <tr><th>{@code classDecimal}</th><td>decimal with following digits (if exist)</td></tr>
 * <tr><th>{@code classSuffix}</th><td>whatever remains before the first cutter</td></tr>
 * <tr>
 *   <th>{@code cutter}</th>
 *   <td>the first occurrence of the pattern {@code [ .][A-Z]\\d+} after the classification letters and digits.</td>
 * </tr>
 * </table>
 * 
 * <p>For example, the call number {@code PR9199.3 1920 .L33 1475 .A6} parses like so:
 * 
 * <table>
 * <caption>Call number parsed into fields</caption>
 * <tr><th>{@code classification}</th><td>{@code PR9199.3 1920}</td></tr>
 * <tr><th>{@code classLetters}</th><td>{@code PR}</td></tr>
 * <tr><th>{@code classDigits}</th><td>{@code 9199}</td></tr>
 * <tr><th>{@code classDecimal}</th><td>{@code .3}</td></tr>
 * <tr><th>{@code classSuffix}</th><td>{@code 1920}</td></tr>
 * <tr><th>{@code cutter}</th><td>{@code .L33 1475 .A6}</td></tr>
 * </table>
 * 
 * <p>Shelf keys:
 * 
 * <p>With computing shelf keys, we want a string which represents the number but can easily be sorted. 
 * The main issues is sequences of digits: which ones sort numerically, and how to arrange that.
 * 
 * <p>The shelf key algorithm is basically:
 * <ol>
 * <li>{@code classLetters} followed by a space</li>
 * <li>{@code classDigits} prepended with the number of digits</li>
 * <li>
 *   normalize {@code classSuffix}, 
 *   for details see {@link Utils#appendNumericallySortable Utils#appendNumericallySortable};
 *   if suffix is alphabetic, prefix with {@code _} so it sorts after cutters
 * </li>
 * <li>
 *  parse {@code cutter} to separate cutters from any additional information (years, military regiments, etc.),
 *  actual cutters are used as-is (without decimals), 
 *  any other data is normalized according to {@code Utils#appendNumericallySortable}
 * </li>
 * </ol>
 * 
 * <p>Using the above example call number:
 * <table>
 * <caption>Constructing the shelf key</caption>
 * <tr><th>{@code classLetters}</th><td>{@code PR}</td><td>{@code PR}</td></tr>
 * <tr><th>{@code classDigits}</th><td>{@code 9199}</td><td>{@code 49199}</td></tr>
 * <tr><th>{@code classDecimal}</th><td>{@code .3}</td><td>{@code .3}</td></tr>
 * <tr><th>{@code classSuffix}</th><td>{@code 1920}</td><td>{@code 41920}</td></tr>
 * <tr><th>{@code cutter}</th><td>{@code .L33 1475 .A6}</td><td>{@code L33 41475 A6}</td></tr>
 * </table>
 * 
 * <p>The resulting shelf key will be:
 * <p>{@code PR 49199.3 41920 L33 41475 A6}
 * 
 * <p>A note on music call numbers: this class does no special processing for music call numbers.
 * Letters in the suffix that introduce Köchel numbers, Burghauser numbers, etc should properly use 
 * a period indicating the fact that it is an abbreviation and not a cutter.
 * 
 * <p>Run the {@code ExerciseLCCallNumber} class from the command line to print out a 
 * number of examples of both parsed call numbers and shelf keys.
 * 
 * @author Tod Olson, University of Chicago
 * @author Anna Headley, Tri-College Library Consortium
 *
 */
public class LCCallNumber extends AbstractCallNumber {

    /* Class variables */
    protected String classification;
    protected String classLetters;
    protected String classDigits;
    protected String classDecimal;
    protected String classSuffix;

    protected String cutter;

    protected String shelfKey;
    protected String paddedShelfKey;

    /* Regexp and patterns */
    /* Original strict regex, from CallNumUtils:
     * 
     * regular expression string for the required portion of the LC classification
     *  LC classification is
     *    1-3 capital letters followed by  float number (may be an integer)
     *    optionally followed by a space and then a year or other number,
     *      e.g. "1987" "15th"
     * LC call numbers can't begin with I, O, W, X, or Y
     * As a regex pattern, group 1 matches the classification letter, group 2 matches the numbers.
     */
//    public static final String LC_CLASS_REQ_REGEX = "([A-Z&&[^IOWXY]]{1}[A-Z]{0,2}) *(\\d+(?:\\.\\d+)?)";


    /**
     * Liberally matches LC call number.
     * 
     * This regex matchex any string of letters, followed by optional spaces, digits, and decimal digits.
     * 
     * Match group 1 contains the letters.
     * Match group 2 contains the classification number
     * Match group 3 contains any classification decimal plus digits
     */
    public static final String CLASS_REGEX = "^([a-zA-Z]+) *(?:(\\d+)(\\.\\d+)?)?";

    /**
     * regular expression string for the cutter, without preceding characters
     * (such as the "required" period, which is sometimes missing, or spaces).
     * A Cutter is a single letter followed by digits.
     * 
     * Must match uppper and lower case, catalog patrons expect to type in either case.
     */
    public static final String CUTTER_REGEX = "[A-Za-z]\\d+";

    /**
     * Separates the class from the rest of a call number.
     * 
     * Match group 1 contains the classification.
     * Match group 2 contains the class letters.
     * Match group 3 contains the class digits (before the decimal).
     * Match group 4 contains the decimal portion of the class number, including the decimal point.
     * Match group 5 contains everything after the classification.
     */
    protected static Pattern classPattern = Pattern.compile("(" + CLASS_REGEX + ")" + "(.*)$");

    /**
     *  Matches a single cutter.
     *  
     *  This Pattern assumes that matching will begin after any classification or class suffix,
     *  and will identify exactly one letter-followed-by-digits cutter.
     *  
     *  Matching group 1 contains the cutter.
     */
    protected static Pattern cutterPat = Pattern.compile("(" + CUTTER_REGEX + ")");

    /**
     * Matches the cutter after the classification suffix.
     * 
     * Assumes that the classification part has already been removed 
     * and we just need to separate the cutter from any suffix.
     * Matching group 1 contains the cutter.
     */
    protected static Pattern cutterAfterSuffixPat = Pattern.compile("(\\.?[A-Za-z]\\d+|^\\.[A-Za-z]| \\.[A-Za-z])");

    /* Constructors */
    /**
     * Creates a call number object from the given string.
     * 
     * The constructor parses the {@code rawCallNumber} argument as part of instantiating the object.
     * 
     * @param rawCallNumber the call number as a string
     */
    public LCCallNumber(String rawCallNumber) {
        parse(rawCallNumber);
    }

    /**
     * Create call number object with no call number.
     * Mainly a convenience for inheritance.
     */
    public LCCallNumber() {
        // TODO Auto-generated constructor stub
    }

    /* Accessors */
    public String getClassification() {
        return classification;
    }

    public String getClassLetters() {
        return classLetters;
    }

    public String getClassDigits() {
        return classDigits;
    }

    public String getClassDecimal() {
        return classDecimal;
    }

    /**
     * Returns the digit and decimal part of the classification.
     * 
     * @return numeric portion of the classification
     */
    public String getClassNumber() {
        String digits = classDigits == null ? "" : classDigits;
        String decimal = classDecimal == null ? "" : classDecimal;
        return digits + decimal;
    }

    public String getClassSuffix() {
        return classSuffix;
    }

    public String getCutter() {
        return cutter;
    }

    /* Methods proper */
    protected void init() {
        rawCallNum = null;
        classification = null;
        classLetters = null;
        classDigits = null;
        classDecimal = null;
        classSuffix = null;
        cutter = null;
        shelfKey = null;
        paddedShelfKey = null;
    }

    /**
     * This parse can be used in conjunction with the empty constructor.
     * 
     * Leading and training whitespace will automatically be trimmed before call number is stored and parsed.
     * 
     * @param call call number to parse
     */
    @Override
    public void parse(String call) {
        init();
        if (call == null) {
            this.rawCallNum = null;
        } else {
            this.rawCallNum = call.trim();
        }
        parse();
    }

    protected void parse() {
        if (this.rawCallNum != null) {
            parseCallNumber();
//            buildShelfKey();
        }
    }

    /**
     * Parses the call number, splitting the classification portions from any
     * cutter(s) and other following characters. Sets these internal fields:
     * <ul>
     * <li><code>classLetters</code></li>
     * <li><code>classDigits</code></li>
     * <li><code>classDecimal</code></li>
     * <li><code>classSuffix</code></li>
     * <li><code>classification</code></li>
     * <li><code>cutter</code></li>
     * </ul>
     */
    protected void parseCallNumber() {
        String everythingElse = null;

        Matcher mClass = classPattern.matcher(rawCallNum);
        if (mClass.matches()) {
            classification = mClass.group(1) == null ? null 
                : mClass.group(1).trim();
            classLetters = mClass.group(2) == null ? null 
                : mClass.group(2).trim();
            classDigits = mClass.group(3) == null ? null 
                : mClass.group(3).trim();
            classDecimal = mClass.group(4) == null ? null 
                : mClass.group(4).trim();
            everythingElse = mClass.group(5) == null ? null 
                : mClass.group(5).trim();
            // (.*) matches on "" but we trade in nulls
            everythingElse = mClass.group(5).length() < 1 ? null
                : everythingElse;
        } else {
            everythingElse = rawCallNum;
        }

        cutter = null;
        if (everythingElse != null) {
            // split any classSuffix from first cutter
            Matcher mCut = cutterAfterSuffixPat.matcher(everythingElse);
            if (mCut.find()) {
                int start = mCut.start(1);
                classSuffix = start > 0 ? everythingElse.substring(0, start).trim() : null;
                cutter = everythingElse.substring(mCut.start(1)).trim();
            } else {
                classSuffix = everythingElse.trim();
            }
            
            // clean up the class suffix
            if (classSuffix != null && classSuffix.length() == 0) {
                classSuffix = null;
            }

            // add suffix on to classification
            if (classSuffix != null) {
                if (classification != null) {
                    classification += " " + classSuffix;
                } else {
                    classification = classSuffix;
                }
            }
        }
    }

    /**
     * Builds the shelf key from the parsed call number.
     */
    protected void buildShelfKey() {
        //TODO: Painful procedural logic, want a null-sensitive map over an array
        
        //TODO: Question: better to upcase here, or force to upper at parse time?
        StringBuilder key = new StringBuilder();
        if (classLetters != null) {
            key.append(classLetters.toUpperCase());
        }
        if (classDigits != null) {
            if (key.length() > 0) {
                key.append(' ');
            }
            key.append(classDigits.length());
            key.append(classDigits);
        }
        // class decimal includes ., easier to visually check, and sorts after [space] [year]
        if (classDecimal != null) {
            key.append(classDecimal);
        }
        if (classSuffix != null) {
            //TODO: pad-if-not-null utility helper; or null-ignoring builder subclass with right-pad method
            if (key.length() > 0) {
                key.append(' ');
                // sort alphabetic suffixes after cutters
                if (Character.isAlphabetic(classSuffix.charAt(0))) {
                    key.append('_');
                }
            }
            Utils.appendNumericallySortable(key, classSuffix.toUpperCase());
        }
        if (cutter != null) {
            appendCutterShelfKey(key, cutter.toUpperCase());
        }
        // TODO: better way to deal with trailing . or space in call num, as in "BF199.", 
        //       causes meaningless class suffix resulting in trailing space on shelf key
        if (key.length() > 0) {
            int i = key.length() - 1;
            char last = key.charAt(i);
            if (last == ' ') {
                key.deleteCharAt(i);
            }
        }
        shelfKey = key.toString();
    }
    
    /**
     * Builds the shelf key from the parsed call number.
     */
    protected void buildPaddedShelfKey() {
        //TODO: Painful procedural logic, want a null-sensitive map over an array
        
        //TODO: Question: better to upcase here, or force to upper at parse time?
        StringBuilder key = new StringBuilder();
        if (classLetters != null) {
            key.append(classLetters.toUpperCase());
            key.append("   ".substring(classLetters.length()));
        }
        if (classDigits != null) {
            if (key.length() > 0) {
                key.append(' ');
            }
            key.append((classDigits.length() < 4 ? "0000".substring(classDigits.length()) : ""));
            key.append(classDigits);
        }
        // class decimal includes ., easier to visually check, and sorts after [space] [year]
        if (classDecimal != null) {
            key.append(classDecimal);
            key.append((classDecimal.length() < 7 ? "000000".substring(classDecimal.length()-1) : ""));
        }
        else {
            key.append(".000000");
        }
        if (classSuffix != null) {
            //TODO: pad-if-not-null utility helper; or null-ignoring builder subclass with right-pad method
            if (key.length() > 0) {
                key.append(' ');
                // sort alphabetic suffixes after cutters
                if (Character.isAlphabetic(classSuffix.charAt(0))) {
                    key.append('_');
                }
            }
            Utils.appendNumericallySortable(key, classSuffix.toUpperCase());
        }
        if (cutter != null) {
            appendPaddedCutterShelfKey(key, cutter.toUpperCase());
        }
        // TODO: better way to deal with trailing . or space in call num, as in "BF199.", 
        //       causes meaningless class suffix resulting in trailing space on shelf key
        if (key.length() > 0) {
            int i = key.length() - 1;
            char last = key.charAt(i);
            if (last == ' ') {
                key.deleteCharAt(i);
            }
        }
        paddedShelfKey = key.toString();
    }

    /**
     * Computes the shelf key for the cutter, appending it to the shelf key buffer.
     * 
     * @param keyBuf    buffer with the in-progress shelf key
     * @param cutter    cutter sequence to parse
     */
    protected static void appendCutterShelfKey(StringBuilder keyBuf, CharSequence cutter) {
        Matcher m = cutterPat.matcher(cutter);
        appendCutterShelfKeyLoop(keyBuf, cutter, m, 0);
    }
    
    /**
     * Computes the shelf key for the cutter, appending it to the shelf key buffer.
     * 
     * @param keyBuf    buffer with the in-progress shelf key
     * @param cutter    cutter sequence to parse
     */
    protected static void appendPaddedCutterShelfKey(StringBuilder keyBuf, CharSequence cutter) {
        Matcher m = cutterPat.matcher(cutter);
        appendPaddedCutterShelfKeyLoop(keyBuf, cutter, m, 0);
    }
    
    /**
     * Recursively builds up the key in the buffer.
     * 
     * This method marches through the cutter, consumes up through the next cutter pattern.
     * It formats what has been consumed into a shelf key and appends it to {@code buf},
     * the, calls itself recursively, starting at the end of the current match.
     * 
     * @param keyBuf    buffer with the in-progress shelf key
     * @param cutter    cutter sequence to parse
     * @param m         matcher with the cutter pattern
     * @param offset    current position in the cutter
     */
    protected static void appendCutterShelfKeyLoop(StringBuilder keyBuf, CharSequence cutter, Matcher m, int offset) {
        if (offset >= cutter.length()) {      // all done
            return;
        } else if (m.find(offset)) {    // found another cutter
            CharSequence previousCutterSuffix = cutter.subSequence(offset, m.start());
            CharSequence matchSeq = cutter.subSequence(m.start(), m.end());

            //TODO: pad-if-not-null utility helper; or null-ignoring builder subclass with right-pad method
            if (keyBuf.length() > 0 && keyBuf.charAt(keyBuf.length()-1) != ' ') {
                keyBuf.append(' ');
            }
            Utils.appendNumericallySortable(keyBuf, previousCutterSuffix);
            if (keyBuf.length() > 0 && keyBuf.charAt(keyBuf.length()-1) != ' ') {
                keyBuf.append(' ');
            }
            keyBuf.append(matchSeq);
            
            appendCutterShelfKeyLoop(keyBuf, cutter, m, m.end());
        } else {        // no more cutters
            if (keyBuf.length() > 0 && keyBuf.charAt(keyBuf.length()-1) != ' ') {
                keyBuf.append(' ');
            }
            Utils.appendNumericallySortable(keyBuf, cutter.subSequence(offset, cutter.length()));
        }
    }
    /**
     * Recursively builds up the key in the buffer.
     * 
     * This method marches through the cutter, consumes up through the next cutter pattern.
     * It formats what has been consumed into a shelf key and appends it to {@code buf},
     * the, calls itself recursively, starting at the end of the current match.
     * 
     * @param keyBuf    buffer with the in-progress shelf key
     * @param cutter    cutter sequence to parse
     * @param m         matcher with the cutter pattern
     * @param offset    current position in the cutter
     */
    protected static void appendPaddedCutterShelfKeyLoop(StringBuilder keyBuf, CharSequence cutter, Matcher m, int offset) {
        if (offset >= cutter.length()) {      // all done
            return;
        } else if (m.find(offset)) {    // found another cutter
            CharSequence previousCutterSuffix = cutter.subSequence(offset, m.start());
            CharSequence matchSeq = cutter.subSequence(m.start(), m.end());

            //TODO: pad-if-not-null utility helper; or null-ignoring builder subclass with right-pad method
            if (keyBuf.length() > 0 && keyBuf.charAt(keyBuf.length()-1) != ' ') {
                keyBuf.append(' ');
            }
            Utils.appendNumericallySortable(keyBuf, previousCutterSuffix);
            if (keyBuf.length() > 0 && keyBuf.charAt(keyBuf.length()-1) != ' ') {
                keyBuf.append(' ');
            }
            appendCutterPadded(keyBuf, matchSeq);
            
            appendPaddedCutterShelfKeyLoop(keyBuf, cutter, m, m.end());
        } else {        // no more cutters
            if (keyBuf.length() > 0 && keyBuf.charAt(keyBuf.length()-1) != ' ') {
                keyBuf.append(' ');
            }
            Utils.appendNumericallySortable(keyBuf, cutter.subSequence(offset, cutter.length()));
        }
    }


    private static void appendCutterPadded(StringBuilder keyBuf, CharSequence cutter)
    {
        int offset = 0;
        for (; Character.isAlphabetic(cutter.charAt(offset)); offset++)
        {
            keyBuf.append(cutter.charAt(offset));
        }
        CharSequence number = cutter.subSequence(offset,  cutter.length());
        keyBuf.append("0.").append(number).append((number.length() < 6 ? "000000".substring(number.length()) : ""));
    }

    /**
     * Initial implementation checks for:
     *  - invalid classes (beginning with I,O,W,X, or Y)
     *  - null classDigits
     */
    public boolean isValid() {
        boolean valid = true;
        if (this.classLetters == null) {
            valid = false;
        }
        else {
            char firstChar = this.classLetters.charAt(0);
            // LC call numbers can't begin with I, O, W, X, or Y
            if (firstChar == 'I' || firstChar == 'O' || firstChar == 'W'
                    || firstChar == 'X' || firstChar == 'Y') {
                valid = false;
            }
        }
        if (this.classDigits == null) valid = false;
        return valid;
    }

    @Override
    public String getShelfKey() {
        if (shelfKey == null) {
            buildShelfKey(); 
        }
        return shelfKey;
    }

    public String getPaddedShelfKey() {
        if (paddedShelfKey == null) {
            buildPaddedShelfKey(); 
        }
        return paddedShelfKey;
    }

    /**
     * Formats the call number from its parsed components into a display format
     */
    public String toString() {
        // TODO: this method was based on buildShelfKey and therefore would
        // benefit from the same refactoring efforts.
        StringBuilder formatted = new StringBuilder();
        if (classLetters != null) {
            formatted.append(classLetters);
        }
        if (classDigits != null) {
            formatted.append(classDigits);
        }
        // class decimal includes ., easier to visually check, and sorts after [space] [year]
        if (classDecimal != null) {
            formatted.append(classDecimal);
        }
        if (classSuffix != null) {
            if (formatted.length() > 0) {
                formatted.append(' ');
            }
            formatted.append(classSuffix);
        }
        if (cutter != null) {
            formatted.append(" ");
            // fix cutter, but only for valid LC
            if (this.isValid() && cutter.charAt(0) != '.') {
                formatted.append('.');
            }
            formatted.append(cutter);
        }
        return formatted.toString();
    }
}
