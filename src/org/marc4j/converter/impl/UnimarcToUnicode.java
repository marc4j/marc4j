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

import org.marc4j.converter.CharConverter;

import java.text.Normalizer;
import java.util.Vector;

/**
 * A utility to convert UNIMARC data to non-precomposed UCS/Unicode.
 * <p>
 * This is based off of the AnselToUnicode class with modifications for UNIMARC conversion
 *
 * @author SirsiDynix from Bas Peters
 */
public class UnimarcToUnicode extends CharConverter implements UnimarcConstants {

    class Queue extends Vector {

        /**
         * Puts an item into the queue.
         *
         * @param item the item to be put into the queue.
         */
        public Object put(Object item) {
            addElement(item);

            return item;
        }

        /**
         * Gets an item from the front of the queue.
         */
        public Object get() {
            Object obj;

            obj = peek();
            removeElementAt(0);

            return obj;
        }

        /**
         * Peeks at the front of the queue.
         */
        public Object peek() {
            return elementAt(0);
        }

        /**
         * Returns true if the queue is empty.
         */
        public boolean empty() {
            return size() == 0;
        }
    }

    class CodeTracker {
        int offset = 0;

        int g0 = DEFAULT_G0;            // 02-07
        int g1 = DEFAULT_G1;            // 0A-0F
        int workingG0 = DEFAULT_G0;
        int workingG1 = DEFAULT_G1;
        int workingG2 = DEFAULT_G2;
        int workingG3 = DEFAULT_G3;

        boolean multibyte = false;
        boolean isG0multibyte = false;
        boolean isG1multibyte = false;
        boolean isG2multibyte = false;
        boolean isG3multibyte = false;

        CodeTracker() {
        }

        CodeTracker(CodeTracker tracker) {
            if (tracker != null) {
                g0 = tracker.g0;
                g1 = tracker.g1;
                workingG0 = tracker.workingG0;
                workingG1 = tracker.workingG1;
                workingG2 = tracker.workingG2;
                workingG3 = tracker.workingG3;
                multibyte = tracker.multibyte;
                isG0multibyte = tracker.isG0multibyte;
                isG1multibyte = tracker.isG1multibyte;
                isG2multibyte = tracker.isG2multibyte;
                isG3multibyte = tracker.isG3multibyte;
            }
        }

        public String toString() {
            return "Offset: " + offset +
                " G0: " + Integer.toHexString(g0) +
                " G1: " + Integer.toHexString(g1) +
                " Multibyte: " + multibyte;
        }
    }

    protected CodeTable ct;

    protected CodeTracker altCodeTracker = null;

    protected ConvertUnicodeSequence unicodeSeqConv = new ConvertUnicodeSequence();

    // flag that indicates whether we should convert embedded unicode sequences (e.g. <U+xxxx>) to
    // unicode characters.  Default false.
    protected boolean convertUnicodeSequence = false;

    // flag that indicates we should normalize the results of the convert method (i.e. compose any
    // decomposed Unicode characters.  Default false.
    protected boolean composeUnicode = false;


    /**
     * Returns true if embedded Unicode sequences (i.e. of form &lt;U+xxxx$gt;) should be converted to
     * the unicode character.  If true, will be done before composing unicode (if that is also true).
     *
     * @return True if should convert &lt;U+xxxx&gt; sequences to the unicode character.
     */
    public boolean shouldConvertUnicodeSequence() {
        return convertUnicodeSequence;
    }

    /**
     * Setsw whether Unicode sequences of &lt;U+xxxx&gt; should be converted to the Unicode character,
     * or left as is.  If shouldComposeUnicode() is also true, the convert will happen before the
     * compose.
     *
     * @param convertUnicodeSequence True if should translate &lt;U+xxxx&gt; to Unicode characters, false
     *                               otherwise.  Default false.
     */
    public void setShouldConvertUnicodeSequence(boolean convertUnicodeSequence) {
        this.convertUnicodeSequence = convertUnicodeSequence;
    }

    /**
     * Returns true if Unicode decomposed characters should be composed.
     *
     * @return True if we should compose Unicode characters, else, false to leave them alone.
     */
    public boolean shouldComposeUnicode() {
        return composeUnicode;
    }

    /**
     * Sets whether we should compose Unicode decomposed charactes.
     *
     * @param composeUnicode True if we should compose Unicode characters, else, false.  Default false.
     */
    public void setComposeUnicode(boolean composeUnicode) {
        this.composeUnicode = composeUnicode;
    }


    /**
     * Default constructor.
     */
    public UnimarcToUnicode() {
        ct = new CodeTable(getClass().getResourceAsStream("resources/unimarc.xml"));
    }

    private void checkMode(char[] data, CodeTracker cdt) {
        while (cdt.offset < data.length) {
            cdt.multibyte = false;
            if (data[cdt.offset] == LS0) {
                cdt.g0 = cdt.workingG0;
                cdt.offset += 1;
                cdt.multibyte = cdt.isG0multibyte;
            } else if (data[cdt.offset] == LS1) {
                cdt.g0 = cdt.workingG1;
                cdt.offset += 1;
                cdt.multibyte = cdt.isG1multibyte;
            } else if (isEscape(data[cdt.offset])) {
                switch (data[cdt.offset + 1]) {
                    case LS1R:
                        cdt.g1 = cdt.workingG1;
                        cdt.offset += 2;
                        cdt.multibyte = cdt.isG1multibyte;
                        break;
                    case LS2:
                        cdt.g0 = cdt.workingG2;
                        cdt.offset += 2;
                        cdt.multibyte = cdt.isG2multibyte;
                        break;
                    case LS2R:
                        cdt.g1 = cdt.workingG2;
                        cdt.offset += 2;
                        cdt.multibyte = cdt.isG2multibyte;
                        break;
                    case LS3:
                        cdt.g0 = cdt.workingG3;
                        cdt.offset += 2;
                        cdt.multibyte = cdt.isG3multibyte;
                        break;
                    case LS3R:
                        cdt.g1 = cdt.workingG3;
                        cdt.offset += 2;
                        cdt.multibyte = cdt.isG3multibyte;
                        break;
                    case 0x28:
                    case 0x2C:
                        cdt.workingG0 = data[cdt.offset + 2];
                        cdt.offset += 3;
                        cdt.isG0multibyte = false;
                        break;
                    case 0x29:
                    case 0x2D:
                        cdt.workingG1 = data[cdt.offset + 2];
                        cdt.offset += 3;
                        cdt.isG1multibyte = false;
                        break;
                    case 0x2A:
                    case 0x2E:
                        cdt.workingG2 = data[cdt.offset + 2];
                        cdt.offset += 3;
                        cdt.isG2multibyte = false;
                        break;
                    case 0x2B:
                    case 0x2F:
                        cdt.workingG3 = data[cdt.offset + 2];
                        cdt.offset += 3;
                        cdt.isG3multibyte = false;
                        break;
                    case 0x24:
                        switch (data[cdt.offset + 2]) {
                            case 0x2C:
                                cdt.workingG0 = data[cdt.offset + 3];
                                cdt.offset += 4;
                                cdt.isG0multibyte = true;
                                break;
                            case 0x29:
                            case 0x2D:
                                cdt.workingG1 = data[cdt.offset + 3];
                                cdt.offset += 4;
                                cdt.isG1multibyte = true;
                                break;
                            case 0x2A:
                            case 0x2E:
                                cdt.workingG2 = data[cdt.offset + 3];
                                cdt.offset += 4;
                                cdt.isG2multibyte = true;
                                break;
                            case 0x2B:
                            case 0x2F:
                                cdt.workingG3 = data[cdt.offset + 3];
                                cdt.offset += 4;
                                cdt.isG3multibyte = true;
                                break;
                            default:
                                cdt.workingG0 = data[cdt.offset + 2];
                                cdt.offset += 3;
                                cdt.isG0multibyte = true;
                                break;
                        }
                    default:
                        cdt.offset += 1;
                        break;
                }
            } else {
                break;
            }
        }
    }

    /**
     * Resets the G0 and G1 charsets to the defaults (ASCII/ANSEL)
     */
    public void resetDefaultGX() {
        altCodeTracker = null;
    }

    /**
     * Allows the caller to set the default G0/G1/G2/G3 char sets
     *
     * @param altG0Code string pulled from 100 $a/26-27
     * @param altG1Code string pulled from 100 $a/28-29
     * @param altG2Code string pulled from 100 $a/30-31
     * @param altG3Code string pulled from 100 $a/32-33
     */
    public void setDefaultGX(String altG0Code, String altG1Code, String altG2Code, String altG3Code) {
        altCodeTracker = new CodeTracker();


        int iso = UnimarcCommon.determineCharSet(altG0Code);
        if (iso > 0) {
            altCodeTracker.g0 = iso;
            altCodeTracker.isG0multibyte = false;
            altCodeTracker.workingG0 = iso;
        }

        iso = UnimarcCommon.determineCharSet(altG1Code);
        if (iso > 0) {
            altCodeTracker.g1 = iso;
            altCodeTracker.isG1multibyte = false;
            altCodeTracker.workingG1 = iso;
        }

        iso = UnimarcCommon.determineCharSet(altG2Code);
        if (iso > 0) {
            altCodeTracker.isG2multibyte = false;
            altCodeTracker.workingG2 = iso;
        }

        iso = UnimarcCommon.determineCharSet(altG3Code);
        if (iso > 0) {
            altCodeTracker.isG3multibyte = false;
            altCodeTracker.workingG3 = iso;
        }
    }

    /**
     * <p>
     * Converts UNIMARC data to UCS/Unicode.
     * </p>
     *
     * @param data the UNIMARC data
     * @return String - the UCS/Unicode data
     */
    public String convert(char[] data) {
        StringBuilder sb = new StringBuilder();
        int len = data.length;

        CodeTracker cdt = new CodeTracker(altCodeTracker);

        checkMode(data, cdt);

        Queue diacritics = new Queue();

        while (cdt.offset < data.length) {
            if (ct.isCombining(data[cdt.offset], cdt.g0, cdt.g1)
                && hasNext(cdt.offset, len)) {

                while (ct.isCombining(data[cdt.offset], cdt.g0, cdt.g1)
                    && hasNext(cdt.offset, len)) {
                    diacritics.put(getChar(data[cdt.offset], cdt.g0, cdt.g1));
                    cdt.offset++;
                    checkMode(data, cdt);
                }

                char c2 = getChar(data[cdt.offset], cdt.g0, cdt.g1);
                cdt.offset++;
                checkMode(data, cdt);
                sb.append(c2);

                while (!diacritics.isEmpty()) {
                    char c1 = (Character) diacritics.get();
                    sb.append(c1);
                }
            } else if (cdt.multibyte) {
                sb.append(ct.getChar(
                    makeMultibyte(new String(data).substring(cdt.offset, cdt.offset + 4).toCharArray()), cdt.g0));
                cdt.offset += 3;
            } else {
                sb.append(getChar(data[cdt.offset], cdt.g0, cdt.g1));
                cdt.offset += 1;
            }
            if (hasNext(cdt.offset, len)) {
                checkMode(data, cdt);
            }
        }

        if (shouldConvertUnicodeSequence()) {
            unicodeSeqConv.convert(sb);
            FixDoubleWidth.removeInvalidSecondHalf(sb);
        }

        if (shouldComposeUnicode()) {
            // compose: combine diacritics and Hangul
            return Normalizer.normalize(sb.toString(), Normalizer.Form.NFC);
        } else {
            return sb.toString();
        }
    }

    private int makeMultibyte(char[] data) {
        int[] chars = new int[3];
        chars[0] = data[0] << 16;
        chars[1] = data[1] << 8;
        chars[2] = data[2];
        return chars[0] | chars[1] | chars[2];
    }

    private char getChar(int ch, int g0, int g1) {
        if (ch <= 0x7E) {
            return ct.getChar(ch, g0);
        } else {
            return ct.getChar(ch, g1);
        }
    }


    private static boolean hasNext(int pos, int len) {
        if (pos < (len - 1)) {
            return true;
        }
        return false;
    }

    private static boolean isEscape(int i) {
        if (i == ESC) {
            return true;
        }
        return false;
    }
}
