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

import org.marc4j.ConverterErrorHandler;
import org.marc4j.MarcError;
import org.marc4j.MarcException;
import org.marc4j.converter.CharConverter;

import java.lang.reflect.Constructor;
import java.text.Normalizer;
import java.util.Arrays;
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

    protected CodeTableInterface ct;

    protected CodeTracker altCodeTracker = null;

    // flag that indicates whether Numeric Character References of the form
    // &amp;#XXXX; (Marc-8 NCR) should be translated to the unicode code point
    // specified by the 4 hexidecimal digits. Marc-8 NCR is as described on
    // this page http://www.loc.gov/marc/specifications/speccharconversion.html#lossless
    // Note: Also translates (optional) "<U+XXXX>" (Unicode BNF) format character references.
    protected boolean translateNCR = false;

    // flag that indicates we should normalize the results of the convert method (i.e. compose any
    // decomposed Unicode characters.  Default false.
    protected boolean composeUnicode = false;


    /**
     * Returns true if should translate to NCR.
     *
     * @return True if should translate to NCR
     */
    public boolean shouldTranslateNCR() {
        return translateNCR;
    }

    /**
     * Sets whether we should translate to NCR (i.e. convert "&amp;#XXXX;" sequences to Unicode).
     * If shouldComposeUnicode() is also true, the NCR Translate will happen before the
     * compose.
     * <br>
     * Note: Also translates any (optional) "&lt;U+XXXX&gt;" (Unicode BNF) sequences to Unicode).
     *
     * @param translateNCR True if we should translate to NCR; else, false
     */
    public void setTranslateNCR(final boolean translateNCR) {
        this.translateNCR = translateNCR;
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

    protected ConverterErrorHandler errorHandler = null;

    /**
     * Default constructor.
     */
    public UnimarcToUnicode() {
        ct = loadGeneratedTable();
    }

    /**
     * Creates a new instance, and registers a class that handles it's own errors.  When set, this class will
     * log errors rather than throw exceptions, letting the error handler class handle the errors.
     *
     * @param errorHandler A class that handles its own errors, used for recording Errors detected in translation
     *                  of the field data.
     */
    public UnimarcToUnicode(final ConverterErrorHandler errorHandler) {
        ct = loadGeneratedTable();
        this.errorHandler = errorHandler;
    }

    private CodeTableInterface loadGeneratedTable() {
        try {
            final Class<?> generated = Class
                    .forName("org.marc4j.converter.impl.UnimarcCodeTableGenerated");
            final Constructor<?> cons = generated.getConstructor();
            final Object ct = cons.newInstance();

            return (CodeTableInterface) ct;
        } catch (final Exception e) {
            return new CodeTable(getClass().getResourceAsStream("resources/unimarc.xml"));
        }
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
                        if (cdt.offset + 2 >= data.length) {
                            cdt.offset += 1;
                            if (errorHandler != null) {
                                errorHandler.addError(MarcError.MINOR_ERROR,
                                        "Incomplete character set code found following escape character. Discarding escape character."
                                        + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                            } else {
                                throw new MarcException("Incomplete character set code found following escape character."
                                        + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                            }
                        }
                        cdt.g1 = cdt.workingG1;
                        cdt.offset += 2;
                        cdt.multibyte = cdt.isG1multibyte;
                        break;
                    case LS2:
                        if (cdt.offset + 2 >= data.length) {
                            cdt.offset += 1;
                            if (errorHandler != null) {
                                errorHandler.addError(MarcError.MINOR_ERROR,
                                        "Incomplete character set code found following escape character. Discarding escape character."
                                                + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                            } else {
                                throw new MarcException("Incomplete character set code found following escape character."
                                        + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                            }
                        }
                        cdt.g0 = cdt.workingG2;
                        cdt.offset += 2;
                        cdt.multibyte = cdt.isG2multibyte;
                        break;
                    case LS2R:
                        if (cdt.offset + 2 >= data.length) {
                            cdt.offset += 1;
                            if (errorHandler != null) {
                                errorHandler.addError(MarcError.MINOR_ERROR,
                                        "Incomplete character set code found following escape character. Discarding escape character."
                                                + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                            } else {
                                throw new MarcException("Incomplete character set code found following escape character."
                                        + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                            }
                        }
                        cdt.g1 = cdt.workingG2;
                        cdt.offset += 2;
                        cdt.multibyte = cdt.isG2multibyte;
                        break;
                    case LS3:
                        if (cdt.offset + 2 >= data.length) {
                            cdt.offset += 1;
                            if (errorHandler != null) {
                                errorHandler.addError(MarcError.MINOR_ERROR,
                                        "Incomplete character set code found following escape character. Discarding escape character."
                                                + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                            } else {
                                throw new MarcException("Incomplete character set code found following escape character."
                                        + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                            }
                        }
                        cdt.g0 = cdt.workingG3;
                        cdt.offset += 2;
                        cdt.multibyte = cdt.isG3multibyte;
                        break;
                    case LS3R:
                        if (cdt.offset + 2 >= data.length) {
                            cdt.offset += 1;
                            if (errorHandler != null) {
                                errorHandler.addError(MarcError.MINOR_ERROR,
                                        "Incomplete character set code found following escape character. Discarding escape character."
                                                + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                            } else {
                                throw new MarcException("Incomplete character set code found following escape character."
                                        + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                            }
                        }
                        cdt.g1 = cdt.workingG3;
                        cdt.offset += 2;
                        cdt.multibyte = cdt.isG3multibyte;
                        break;
                    case 0x28:
                    case 0x2C:
                        if (cdt.offset + 3 >= data.length) {
                            cdt.offset += 1;
                            if (errorHandler != null) {
                                errorHandler.addError(MarcError.MINOR_ERROR,
                                        "Incomplete character set code found following escape character. Discarding escape character."
                                                + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                            } else {
                                throw new MarcException("Incomplete character set code found following escape character."
                                        + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                            }
                        }
                        cdt.workingG0 = data[cdt.offset + 2];
                        cdt.offset += 3;
                        cdt.isG0multibyte = false;
                        break;
                    case 0x29:
                    case 0x2D:
                        if (cdt.offset + 3 >= data.length) {
                            cdt.offset += 1;
                            if (errorHandler != null) {
                                errorHandler.addError(MarcError.MINOR_ERROR,
                                        "Incomplete character set code found following escape character. Discarding escape character."
                                                + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                            } else {
                                throw new MarcException("Incomplete character set code found following escape character."
                                        + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                            }
                        }
                        cdt.workingG1 = data[cdt.offset + 2];
                        cdt.offset += 3;
                        cdt.isG1multibyte = false;
                        break;
                    case 0x2A:
                    case 0x2E:
                        if (cdt.offset + 3 >= data.length) {
                            cdt.offset += 1;
                            if (errorHandler != null) {
                                errorHandler.addError(MarcError.MINOR_ERROR,
                                        "Incomplete character set code found following escape character. Discarding escape character."
                                                + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                            } else {
                                throw new MarcException("Incomplete character set code found following escape character."
                                        + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                            }
                        }
                        cdt.workingG2 = data[cdt.offset + 2];
                        cdt.offset += 3;
                        cdt.isG2multibyte = false;
                        break;
                    case 0x2B:
                    case 0x2F:
                        if (cdt.offset + 3 >= data.length) {
                            cdt.offset += 1;
                            if (errorHandler != null) {
                                errorHandler.addError(MarcError.MINOR_ERROR,
                                        "Incomplete character set code found following escape character. Discarding escape character."
                                                + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                            } else {
                                throw new MarcException("Incomplete character set code found following escape character."
                                        + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                            }
                        }
                        cdt.workingG3 = data[cdt.offset + 2];
                        cdt.offset += 3;
                        cdt.isG3multibyte = false;
                        break;
                    case 0x24:
                        if (cdt.offset + 2 >= data.length) {
                            cdt.offset += 1;
                            if (errorHandler != null) {
                                errorHandler.addError(MarcError.MINOR_ERROR,
                                        "Incomplete character set code found following escape character. Discarding escape character."
                                                + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                            } else {
                                throw new MarcException("Incomplete character set code found following escape character."
                                        + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                            }
                        }
                        switch (data[cdt.offset + 2]) {
                            case 0x2C:
                                if (cdt.offset + 4 >= data.length) {
                                    cdt.offset += 1;
                                    if (errorHandler != null) {
                                        errorHandler.addError(MarcError.MINOR_ERROR,
                                                "Incomplete character set code found following escape character. Discarding escape character."
                                                        + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                                    } else {
                                        throw new MarcException("Incomplete character set code found following escape character."
                                                + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                                    }
                                }
                                cdt.workingG0 = data[cdt.offset + 3];
                                cdt.offset += 4;
                                cdt.isG0multibyte = true;
                                break;
                            case 0x29:
                            case 0x2D:
                                if (cdt.offset + 4 >= data.length) {
                                    cdt.offset += 1;
                                    if (errorHandler != null) {
                                        errorHandler.addError(MarcError.MINOR_ERROR,
                                                "Incomplete character set code found following escape character. Discarding escape character."
                                                        + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                                    } else {
                                        throw new MarcException("Incomplete character set code found following escape character."
                                                + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                                    }
                                }
                                cdt.workingG1 = data[cdt.offset + 3];
                                cdt.offset += 4;
                                cdt.isG1multibyte = true;
                                break;
                            case 0x2A:
                            case 0x2E:
                                if (cdt.offset + 4 >= data.length) {
                                    cdt.offset += 1;
                                    if (errorHandler != null) {
                                        errorHandler.addError(MarcError.MINOR_ERROR,
                                                "Incomplete character set code found following escape character. Discarding escape character."
                                                        + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                                    } else {
                                        throw new MarcException("Incomplete character set code found following escape character."
                                                + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                                    }
                                }
                                cdt.workingG2 = data[cdt.offset + 3];
                                cdt.offset += 4;
                                cdt.isG2multibyte = true;
                                break;
                            case 0x2B:
                            case 0x2F:
                                if (cdt.offset + 4 >= data.length) {
                                    cdt.offset += 1;
                                    if (errorHandler != null) {
                                        errorHandler.addError(MarcError.MINOR_ERROR,
                                                "Incomplete character set code found following escape character. Discarding escape character."
                                                        + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                                    } else {
                                        throw new MarcException("Incomplete character set code found following escape character."
                                                + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                                    }
                                }
                                cdt.workingG3 = data[cdt.offset + 3];
                                cdt.offset += 4;
                                cdt.isG3multibyte = true;
                                break;
                            default:
                                if (cdt.offset + 3 >= data.length) {
                                    cdt.offset += 1;
                                    if (errorHandler != null) {
                                        errorHandler.addError(MarcError.MINOR_ERROR,
                                                "Incomplete character set code found following escape character. Discarding escape character."
                                                        + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                                    } else {
                                        throw new MarcException("Incomplete character set code found following escape character."
                                                + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                                    }
                                }
                                cdt.workingG0 = data[cdt.offset + 2];
                                cdt.offset += 3;
                                cdt.isG0multibyte = true;
                                break;
                        }
                    default:
                        // Unknown code character found: discard escape sequence and return (if have a errorHandler)
                        cdt.offset += 1;
                        if (errorHandler != null) {
                            errorHandler.addError(MarcError.MINOR_ERROR,
                                    "Unknown character set code found following escape character. Discarding escape character."
                                            + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                        } else {
                            throw new MarcException("Unknown character set code found following escape character."
                                    + " At offset " + cdt.offset + ":" + Arrays.toString(data));
                        }

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

        boolean unrecognizedUnicode = false;

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
                char c = getChar(data[cdt.offset], cdt.g0, cdt.g1);
                if (c != 0) {
                    sb.append(c);
                } else {
                    // Uh oh.  c == 0.  Don't know what to do with this character.  No Unicode equivalent.  Encode it in the output
                    // as <U+XXXX>, where XXXX is the Marc character.
                    // Note this is odd:  Normally a <U+XXXX> represents a real Unicode character.  In this case, it
                    // represents the original MARC character that can't be converted to Unicode.  So, it's misleading
                    // in the result :(  But - fixing it now could break some other client code.
                    String val = UnicodeUtils.convertUnicodeToUnicodeBNF(data[cdt.offset]);
                    if (translateNCR) {
                        // if translateNCR, then Add it as "<U+>XXXX>" so that the normal convertNCRToUnicode won't immediately change it back to a unicode
                        // value.  After the 'translateNCR' takes place, we'll fix this up.
                        val = val.substring(0,3) + '>' + val.substring(3);
                        unrecognizedUnicode = true;
                    }
                    sb.append(val);
                }
                cdt.offset += 1;
            }
            if (hasNext(cdt.offset, len)) {
                checkMode(data, cdt);
            }
        }

        if (translateNCR) {
            UnicodeUtils.convertNCRToUnicode(sb);
            FixDoubleWidth.removeInvalidSecondHalf(sb);
        }
        String dataElement = sb.toString();
        if (unrecognizedUnicode) {
            // Replace "<U+>XXXX>" with "<U+XXXX>"
            dataElement = dataElement.replaceAll("<U\\+>", "<U+");
        }

        if (shouldComposeUnicode()) {
            // compose: combine diacritics and Hangul
            return Normalizer.normalize(dataElement, Normalizer.Form.NFC);
        } else {
            return dataElement;
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
