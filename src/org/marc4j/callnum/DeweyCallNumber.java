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
 * Implements a call number class for Dewey call numbers.
 * 
 * <p>Example call number: {@code 322.44 .F816 V.1 1974}
 * 
 * <p>As unpacked into internal fields:
 * 
 * <table>
 * <caption>Parsing the call number</caption>
 * <tr><th>{@code classification}</th><td>322.44</td></tr>
 * <tr><th>{@code classDigits}</th><td>322</td></tr>
 * <tr><th>{@code classDecimal}</th><td>.44</td></tr>
 * <tr><th>{@code cutter}</th><td>F816</td></tr>
 * <tr><th>{@code suffix}</th><td>V.1 1974</td></tr>
 * </table>
 * 
 * <p>If the call number doesn't look like Dewey (no starting digit) the entire call number
 * goes into {@code suffix}. 
 * 
 * <p>Shelf keys:
 * 
 * <p>With computing shelf keys, we want a string which represents the number but can easily be sorted. 
 * The main issues is sequences of digits: which ones sort numerically, and how to arrange that.
 * 
 * <p>The shelf key algorithm is basically:
 * <ol>
 * <li>trim leading zeros from {@code classDigits} and prepend with the number of remaining digits</li>
 * <li>append {@code classDigits} with the leading period</li>
 * <li>append a space and {@code cutter}</li>
 * <li>
 *   normalize {@code suffix} and append with a space, 
 *   for details see {@link Utils#appendNumericallySortable Utils#appendNumericallySortable}
 * </li>
 * </ol>
 * 
 * <p>Using the above example call number:
 * <table>
 * <caption>Constructing the shelf key</caption>
 * <thead>
 * <tr><th>Field</th><th>Sample</th><th>Shelf Key</th><th>Notes</th></tr>
 * </thead>
 * <tbody>
 * <tr><th>{@code classification}</th><td>{@code 322.44}</td><td></td><td>not used</td></tr>
 * <tr><th>{@code classDigits}</th><td>{@code 322}</td><td>{@code 3322}</td><td></td></tr>
 * <tr><th>{@code classDecimal}</th><td>{@code .44}</td><td>{@code .44}</td><td></td></tr>
 * <tr><th>{@code cutter}</th><td>{@code F816}</td><td>{@code F816}</td><td></td></tr>
 * <tr><th>{@code suffix}</th><td>{@code V.1 1974}</td><td>{@code V 11 41974}</td><td></td></tr>
 * </tbody>
 * </table>
 * 
 * <p>The resulting shelf key is {@code 3322.44 .F816 V.1 1974}.
 * 
 * <p>Run the {@code ExerciseDeweyCallNumber} class from the command line to print out a 
 * number of examples of both parsed call numbers and shelf keys.
 * 
 * <p>Based in part on Naomi Dushay's {@code CallNumUtils}.
 *
 * @author Tod Olson, University of Chicago
 *
 */
public class DeweyCallNumber extends AbstractCallNumber {
    protected String classification = null;
    protected String classDigits = null;
    protected String classDecimal = null;
    protected String cutter = null;
    protected String cutterSuffix = null;

    protected String shelfKey = null;

    /**
     * Regular expression for Dewey call number.
     *  Dewey classification is a three digit number (possibly missing leading
     *   zeros) with an optional fraction portion.
     */
    //public static final String CLASS_REGEX = "(\\d{1,3})(\\.\\d+)?";
    public static final String CLASS_REGEX = "(\\d+)(\\.\\d+)?";
    
    /**
     * Separates the class from the rest of a call number.
     * 
     * Match group 1 contains the classification.
     * Match group 2 contains the class digits (before the decimal).
     * Match group 3 contains the decimal portion of the class number, including the decimal point.
     * Match group 4 contains everything after the classification.
     */
    protected static Pattern classPattern = Pattern.compile("(" + CLASS_REGEX + ")" + "(.*)?");

    /**
     * Regular expression for Dewey cutter.
     * 
     * Dewey cutters start with a letter, followed by a one to three digit
     * number. The number may be followed immediately (i.e. without space) by
     * letters, or followed first by a space and then letters.
     * 
     * NB: {@code CallNumUtils} did not implement the "space letters" part of the cutter,
     * similarly, this class defers that detail. 
     * Challenging to do while leaving volumes and similar suffixes in tact.
     */
    //TODO: support cutter with space-then-letter
    //public static final String CUTTER_REGEX = "[A-Z]\\d{1,3} *(?:[A-Z]+)?";
    // public static final String CUTTER_REGEX = "[A-Z]\\d{1,3}(?:[A-Z]+)?";
    // public static final String CUTTER_REGEX = "[A-Z]\\d+(?:[A-Z]+)?";
    public static final String CUTTER_REGEX = "(?<CutterMain>[A-Z]\\d+)(?<CutterExtra>[A-Za-z0-9:]+)?";

    public static Pattern cutterPattern = Pattern.compile(" *\\.?(?<Cutter>" + CUTTER_REGEX + ") *(?<CutterSuffix>.+)?");

    /**
     * Constructs a call number object from the given string.
     * 
     * The constructor parses the <code>callNumber</code> argument as part of instantiating the object.
     * 
     * @param callNumber        call number to parse
     */
    public DeweyCallNumber(String callNumber) {
        parse(callNumber);
    }

    /**
     * Constructs call number object with no call number.
     * Mainly a convenience for inheritance.
     */
    public DeweyCallNumber() {
        return;
    }

    protected void init() {
        rawCallNum = null;
        classification = null;
        classDigits = null;
        classDecimal = null;
        cutter = null;
        cutterSuffix = null;

        shelfKey = null;
    }

    public void parse(String call) {
        init();
        this.rawCallNum = call;
        parse();
    }

    protected void parse() {
        parseCallNumber();
        buildShelfKey();
    }

    /**
     * Parses the call number, splitting the classification portions from any
     * cutter(s) and other following characters. Sets these internal fields:
     * <ul>
     * <li><code>classification</code></li>
     * <li><code>classDigits</code></li>
     * <li><code>classDecimal</code></li>
     * <li><code>classSuffix</code></li>
     * <li><code>cutter</code></li>
     * <li><code>cutterSuffix</code></li>
     * </ul>
     * 
     * <p>Supplies any missing leading zeroes for {@code classification} and {@code classDigits}.
     */

    protected void parseCallNumber() {
        if (rawCallNum == null || rawCallNum.length() == 0) {
            return;
        }

        String everythingElse;
        
        Matcher m = classPattern.matcher(rawCallNum);
        if (!m.matches()) {
            cutterSuffix = rawCallNum;
        } else {
            classification = m.group(1);
            classDigits = m.group(2);
            classDecimal = m.group(3);
            everythingElse = m.group(4);
            
            Matcher mCut = cutterPattern.matcher(everythingElse);
            if (!mCut.matches()) {
                cutterSuffix = everythingElse;
            } else {
                cutter = mCut.group("CutterMain");
                String cutterExtra = mCut.group("CutterExtra");
                if (cutterExtra != null) {
                    cutter += cutterExtra.toUpperCase();
                }
                cutterSuffix = mCut.group("CutterSuffix");
            }
        }
        
    }

    /**
     * returns the classification of the call number.
     * @return call number classification, or null if not set or found by {@code parse}.
     */
    public String getClassification() {
        return classification;
    }

    /**
     * Returns a normal form of the classification string.
     * 
     * <p>Supplies any missing leading zeroes.
     * 
     * @return classification string
     */
    public String getClassificationNormalized() {
        if (classDigits == null || classDigits.length() >= 3) {
            return classification;
        } 

        StringBuilder norm = new StringBuilder();
        switch (classDigits.length()) {
        case 1:
            norm.append("00");
            break;
        case 2:
            norm.append("0");
            break;
        default:
            // DRYROT: classDigits must be non-empty
        }
        norm.append(classDigits);
        if (classDecimal != null) norm.append(classDecimal);
        return norm.toString();
    }

    /**
     * returns the classification of the call number.
     * @return call number classification, or null if not set or found by {@code parse}.
     */
    public String getClassDigits() {
        return classDigits;
    }

    /**
     * returns the classification of the call number.
     * @return call number classification, or null if not set or found by {@code parse}.
     */
    public String getClassDecimal() {
        return classDecimal;
    }

    /**
     * returns the cutter of the call number.
     * @return call number cutter, or <code>null</code> if no cutter was set or found.
     */
    public String getCutter() {
        return cutter;
    }
    /**
     * returns the cutterSuffix of the call number.
     * @return call number cutterSuffix, or <code>null</code> if no cutterSuffix was set or found.
     */
    public String getSuffix() {
        return cutterSuffix;
    }

    @Override
    public String getShelfKey() {
        return shelfKey;
    }

    protected void buildShelfKey() {
        StringBuilder keyBuf = new StringBuilder();
        
        if (rawCallNum == null) {            
            shelfKey = null;
        } else {
            if (classDigits != null) {
                Utils.appendSortableNumber(keyBuf, classDigits);
            }
            if (classDecimal != null) {
                keyBuf.append(classDecimal);
            }
            if (cutter !=null) {
                if (keyBuf.length() > 0) {
                    keyBuf.append(' ');
                }
                keyBuf.append(cutter);
            }
            if (cutterSuffix != null) {
                if (keyBuf.length() > 0) {
                    keyBuf.append(' ');
                }
                Utils.appendNumericallySortable(keyBuf, cutterSuffix);
            }
            shelfKey = keyBuf.toString();
        }
    }
    
    public boolean isValid() {
        if (classDigits == null) {
            return false;
        } else {
            return true;
        }
    }
    
    public String toString() {
        return rawCallNum;
    }
    /*
    public String debugInfo() {
        String info = "this.raw = " + this.raw
                + "this.classification = " + this.classification
                + "this.cutter = " + this.cutter
                + "this.suffix = " + this.suffix;
        return info;
    }
    */
}
