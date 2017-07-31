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

package org.marc4j.converter.impl;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.MarcError;
import org.marc4j.MarcException;
import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.converter.CharConverter;

//import org.marc4j.ErrorHandler;

/**
 * <p>
 * A utility to convert MARC-8 data to non-precomposed UCS/Unicode.
 * </p>
 * 
 * <p>
 * The MARC-8 to Unicode mapping used is the version with the March 2005
 * revisions.
 * </p>
 * 
 * @author Bas Peters
 * @author Corey Keith
 */
public class AnselToUnicode extends CharConverter {

    class Queue extends Vector<Character> {

        private static final long serialVersionUID = 1L;

        /**
         * Puts an item into the queue.
         *
         * @param item the item to be put into the queue.
         */
        public Object put(final Character item) {
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

        int offset;

        int g0;

        int g1;

        boolean multibyte;

        @Override
        public String toString() {
            return "Offset: " + offset + " G0: " + Integer.toHexString(g0) + " G1: " + Integer
                    .toHexString(g1) + " Multibyte: " + multibyte;
        }
    }

    protected CodeTableInterface ct;

    protected boolean loadedMultibyte = false;

    // flag that indicates whether Numeric Character References of the form
    // &#XXXX; should be translated to the
    // unicode code point specified by the 4 hexidecimal digits. As described on
    // this page
    // http://www.loc.gov/marc/specifications/speccharconversion.html#lossless
    protected boolean translateNCR = false;

    /**
     * Returns true if should translate to NCR.
     *
     * @return True if should translate to NCR
     */
    public boolean shouldTranslateNCR() {
        return translateNCR;
    }

    /**
     * Sets whether we should translate to NCR.
     *
     * @param translateNCR True if we should translate to NCR; else, false
     */
    public void setTranslateNCR(final boolean translateNCR) {
        this.translateNCR = translateNCR;
    }

    /**
     * Should return true if the CharConverter outputs Unicode encoded characters
     *
     * @return boolean whether the CharConverter returns Unicode encoded characters
     */
    @Override
    public boolean outputsUnicode() {
        return true;
    }

    protected MarcPermissiveStreamReader curReader = null;

    // protected ErrorHandler errorList = null;
    /**
     * Creates a new instance and loads the MARC4J supplied conversion tables based on the official LC tables.
     */
    public AnselToUnicode() {
        ct = loadGeneratedTable(false);
    }

    /**
     * Creates a new instance and loads the MARC4J supplied conversion tables based on the official LC tables.
     *
     * @param loadMultibyte - true to cause the full translation table to be loaded, including the multi-byte CJK characters
     */
    public AnselToUnicode(final boolean loadMultibyte) {
        ct = loadGeneratedTable(loadMultibyte);
    }

    /**
     * Creates a new instance and loads the MARC4J supplied conversion tables based on the official LC tables.
     *
     * @param curReader - the MarcPermissiveStreamReader currently in use, 
     *       used for recording Errors detected in translation the field data.
     */
    public AnselToUnicode(final MarcPermissiveStreamReader curReader) {
        ct = loadGeneratedTable(false);
        this.curReader = curReader;
    }

    private CodeTableInterface loadGeneratedTable(final boolean loadMultibyte) {
        try {
            final Class<?> generated = Class
                    .forName("org.marc4j.converter.impl.CodeTableGenerated");
            final Constructor<?> cons = generated.getConstructor();
            final Object ct = cons.newInstance();

            loadedMultibyte = true;
            return (CodeTableInterface) ct;
        } catch (final Exception e) {
            CodeTableInterface ct;

            if (loadMultibyte) {
                ct = new CodeTable(AnselToUnicode.class
                        .getResourceAsStream("resources/codetables.xml"));
            } else {
                ct = new CodeTable(AnselToUnicode.class
                        .getResourceAsStream("resources/codetablesnocjk.xml"));
            }

            loadedMultibyte = loadMultibyte;
            return ct;
        }
    }

    /**
     * Constructs an instance with the specified pathname.
     * 
     * Use this constructor to create an instance with a customized code table
     * mapping. The mapping file should follow the structure of LC's XML MARC-8
     * to Unicode mapping (see: http://www.loc.gov/marc/specifications/codetables.xml).
     *
     * @param pathname - path to file to use instead of the official LC codetable
     */
    public AnselToUnicode(final String pathname) {
        ct = new CodeTable(pathname);
        loadedMultibyte = true;
    }

    /**
     * Constructs an instance with the specified input stream.
     * 
     * Use this constructor to create an instance with a customized code table
     * mapping. The mapping file should follow the structure of LC's XML MARC-8
     * to Unicode mapping (see: http://www.loc.gov/marc/specifications/codetables.xml).
     *  
     * @param in - an InputStream to use instead of the official LC codetable data
     */
    public AnselToUnicode(final InputStream in) {
        ct = new CodeTable(in);
        loadedMultibyte = true;
    }

    /**
     * Loads the entire mapping (including multibyte characters) from the Library of Congress.
     */
    private void loadMultibyte() {
        ct = new CodeTable(getClass().getResourceAsStream("resources/codetables.xml"));
    }

    private void checkMode(final char[] data, final CodeTracker cdt) {
        int extra = 0;
        int extra2 = 0;

        while (cdt.offset + extra + extra2 < data.length && isEscape(data[cdt.offset])) {
            if (cdt.offset + extra + extra2 + 1 == data.length) {
                cdt.offset += 1;
                if (curReader != null) {
                    curReader.addError(MarcError.MINOR_ERROR,
                            "Escape character found at end of field, discarding it.");
                } else {
                    throw new MarcException("Escape character found at end of field");
                }

                break;
            }

            switch (data[cdt.offset + 1 + extra]) {
                case 0x28: // '('
                case 0x2c: // ','
                    set_cdt(cdt, 0, data, 2 + extra, false);
                    break;
                case 0x29: // ')'
                case 0x2d: // '-'
                    set_cdt(cdt, 1, data, 2 + extra, false);
                    break;
                case 0x24: // '$'
                    if (!loadedMultibyte) {
                        loadMultibyte();
                        loadedMultibyte = true;
                    }

                    int switchOffset = cdt.offset + 2 + extra + extra2;
                    if (switchOffset >= data.length) {
                        cdt.offset += 1;
                        if (curReader != null) {
                            curReader.addError(MarcError.MINOR_ERROR, "Incomplete character set code found following escape character. Discarding escape character.");
                        } else {
                            throw new MarcException("Incomplete character set code found following escape character.");
                        }
                        break;
                    }
                    switch (data[switchOffset]) {
                        case 0x29: // ')'
                        case 0x2d: // '-'
                            int offset2d = 3 + extra + extra2;
                            if (cdt.offset + offset2d >= data.length) {
                                cdt.offset += 1;
                                if (curReader != null) {
                                    curReader.addError(MarcError.MINOR_ERROR, "Incomplete character set code found following escape character. Discarding escape character.");
                                } else {
                                    throw new MarcException("Incomplete character set code found following escape character.");
                                }
                                break;
                            }
                            set_cdt(cdt, 1, data, offset2d, true);
                            break;
                        case 0x2c: // ','
                            int offset2c = 3 + extra + extra2;
                            if (cdt.offset + offset2c >= data.length) {
                                cdt.offset += 1;
                                if (curReader != null) {
                                    curReader.addError(MarcError.MINOR_ERROR, "Incomplete character set code found following escape character. Discarding escape character.");
                                } else {
                                    throw new MarcException("Incomplete character set code found following escape character.");
                                }
                                break;
                            }
                            set_cdt(cdt, 0, data, offset2c, true); 
                            break;
                        case 0x31: // '1'
                            cdt.g0 = data[cdt.offset + 2 + extra + extra2];
                            cdt.offset += 3 + extra + extra2;
                            cdt.multibyte = true;
                            break;
                        case 0x20: // ' '
                            // space found in escape code: look ahead and try to
                            // proceed
                            extra2++;
                            break;
                        default:
                            // unknown code character found: discard escape
                            // sequence and return
                            cdt.offset += 1;

                            if (curReader != null) {
                                curReader.addError(MarcError.MINOR_ERROR,
                                                "Unknown character set code found following escape character. Discarding escape character.");
                            } else {
                                throw new MarcException(
                                        "Unknown character set code found following escape character.");
                            }

                            break;
                    }

                    break;
                case 0x67: // 'g'
                case 0x62: // 'b'
                case 0x70: // 'p'
                    cdt.g0 = data[cdt.offset + 1 + extra];
                    cdt.offset += 2 + extra;
                    cdt.multibyte = false;
                    break;
                case 0x73: // 's'
                    cdt.g0 = 0x42;
                    cdt.offset += 2 + extra;
                    cdt.multibyte = false;
                    break;
                case 0x20: // ' '
                    // space found in escape code: look ahead and try to proceed
                    if (curReader == null) {
                        throw new MarcException(
                                "Extraneous space character found within MARC8 character set escape sequence");
                    }
                    extra++;
                    break;
                default:
                    // unknown code character found: discard escape sequence and
                    // return
                    cdt.offset += 1;

                    if (curReader != null) {
                        curReader.addError(MarcError.MINOR_ERROR,
                                        "Unknown character set code found following escape character. Discarding escape character.");
                    } else {
                        throw new MarcException(
                                "Unknown character set code found following escape character.");
                    }

                    break;
            }
        }
        if (curReader != null && (extra != 0 || extra2 != 0)) {
            curReader.addError(MarcError.ERROR_TYPO,
                            "" + (extra + extra2) + " extraneous space characters found within MARC8 character set escape sequence");
        }
    }

    private void set_cdt(final CodeTracker cdt, final int g0_or_g1, final char[] data,
            final int aAddnlOffset, final boolean multibyte) {
        int addnlOffset = aAddnlOffset;

        if (data[cdt.offset + addnlOffset] == '!' && data[cdt.offset + addnlOffset + 1] == 'E') {
            addnlOffset++;
        } else if (data[cdt.offset + addnlOffset] == ' ') {
            if (curReader != null) {
                curReader.addError(MarcError.ERROR_TYPO,
                                "Extraneous space character found within MARC8 character set escape sequence. Skipping over space.");
            } else {
                throw new MarcException(
                        "Extraneous space character found within MARC8 character set escape sequence");
            }
            addnlOffset++;
        } else if ("(,)-$!".indexOf(data[cdt.offset + addnlOffset]) != -1) {
            if (curReader != null) {
                curReader.addError(MarcError.MINOR_ERROR,
                                "Extraneaous intermediate character found following escape character. Discarding intermediate character.");
            } else {
                throw new MarcException(
                        "Extraneaous intermediate character found following escape character.");
            }

            addnlOffset++;
        }

        if ("34BE1NQS2".indexOf(data[cdt.offset + addnlOffset]) == -1) {
            cdt.offset += 1;
            cdt.multibyte = false;

            if (curReader != null) {
                curReader.addError(MarcError.MINOR_ERROR,
                                "Unknown character set code found following escape character. Discarding escape character.");
            } else {
                throw new MarcException(
                        "Unknown character set code found following escape character.");
            }
        } else {
            // All is well, proceed normally
            if (g0_or_g1 == 0) {
                cdt.g0 = data[cdt.offset + addnlOffset];
            } else {
                cdt.g1 = data[cdt.offset + addnlOffset];
            }

            cdt.offset += 1 + addnlOffset;
            cdt.multibyte = multibyte;
        }
    }

    /**
     * <p>
     * Converts MARC-8 data to UCS/Unicode.
     * </p>
     *
     * @param data - the MARC-8 data in an array of char
     * @return String - the UCS/Unicode data
     */
    @Override
    public String convert(final char data[]) {
        final StringBuffer sb = new StringBuffer();
        final int len = data.length;
        final CodeTracker cdt = new CodeTracker();

        cdt.g0 = 0x42;
        cdt.g1 = 0x45;
        cdt.multibyte = false;
        cdt.offset = 0;

        checkMode(data, cdt);

        final Queue diacritics = new Queue();

        while (cdt.offset < data.length) {
            if (ct.isCombining(data[cdt.offset], cdt.g0, cdt.g1) && hasNext(cdt.offset, len)) {
                while (cdt.offset < len && ct.isCombining(data[cdt.offset], cdt.g0, cdt.g1) && 
                        hasNext(cdt.offset, len)) {
                    final char c = getCharCDT(data, cdt);
                    if (c != 0) {
                        diacritics.put(new Character(c));
                    }
                    checkMode(data, cdt);
                }
                if (cdt.offset >= len) {
                    if (curReader != null) {
                        curReader.addError(MarcError.MINOR_ERROR,
                                        "Diacritic found at the end of field, without the character that it is supposed to decorate");
                        break;
                    }
                }

                final char c2 = getCharCDT(data, cdt);

                checkMode(data, cdt);

                if (c2 != 0) {
                    sb.append(c2);
                }

                while (!diacritics.isEmpty()) {
                    final char c1 = ((Character) diacritics.get()).charValue();
                    sb.append(c1);
                }

            } else if (cdt.multibyte) {
                final String mbstr = convertMultibyte(cdt, data);
                sb.append(mbstr);
            } else {
                final int offset = cdt.offset;
                final char cdtchar = data[offset];

                char c = getCharCDT(data, cdt);
                boolean greekErrorFixed = false;
                if (c == '\r' || c == '\n') {
                    if (curReader != null) {
                        curReader.addError(MarcError.MINOR_ERROR,
                                        "Subfield contains new line or carriage return, which are invalid, deleting them");
                    }
                    c = ' ';
                }

                if (curReader != null && cdt.g0 == 0x53 && data[offset] > 0x20 && data[offset] < 0x40) {
                    if (c == 0 && data[offset] > 0x20 && data[offset] < 0x40) {
                        curReader.addError(MarcError.MINOR_ERROR,
                                        "Unknown punctuation mark found in Greek character set, inserting change to default character set");
                        cdt.g0 = 0x42; // change to default character set
                        c = getChar(data[offset], cdt.g0, cdt.g1);

                        if (c != 0) {
                            sb.append(c);
                            greekErrorFixed = true;
                        }
                    } else if (offset + 1 < data.length && data[offset] >= '0' && data[offset] <= '9' && data[offset + 1] >= '0' && data[offset + 1] <= '9') {
                        curReader.addError(MarcError.MINOR_ERROR,
                                        "Unlikely sequence of punctuation mark found in Greek character set, it likely a number, inserting change to default character set");

                        cdt.g0 = 0x42; // change to default character set

                        final char c1 = getChar(data[offset], cdt.g0, cdt.g1);

                        if (c1 != 0) {
                            sb.append(c1);
                            greekErrorFixed = true;
                        }
                    }
                }

                if (!greekErrorFixed && c != 0) {
                    sb.append(c);
                } else if (!greekErrorFixed && c == 0) {
                    final String val = "0000" + Integer.toHexString(cdtchar);
                    sb.append("<U+" + val.substring(val.length() - 4, val.length()) + ">");
                    if (curReader != null) {
                        curReader.addError(MarcError.MINOR_ERROR,
                            "Unknown MARC8 character code "+ val.substring(val.length() - 4, val.length()) +" found for code table: " + (char) cdt.g0 + " inserting <U+XXXX>");
                    }
                }
            }

            if (hasNext(cdt.offset, len)) {
                checkMode(data, cdt);
            }
        }

        String dataElement = sb.toString();

        if (translateNCR && dataElement
                .matches("[^&]*&#x[0-9A-Fa-f]+;.*")) {
            final Pattern pattern = Pattern
                    .compile("&#x([0-9A-Fa-f]+);");
            final Matcher matcher = pattern.matcher(dataElement);
            final StringBuffer newElement = new StringBuffer();

            int prevEnd = 0;

            while (matcher.find()) {
                newElement.append(dataElement.substring(prevEnd, matcher.start()));
                newElement.append(getCharFromCodePoint(matcher.group(1)));
                prevEnd = matcher.end();
            }

            newElement.append(dataElement.substring(prevEnd));
            dataElement = newElement.toString();
        }

        return dataElement;
    }

    private String convertMultibyte(final CodeTracker cdt, final char[] data) {
        final StringBuffer sb = new StringBuffer();

        int offset = cdt.offset;

        while (offset < data.length && data[offset] != 0x1b) {
            final int length = getRawMBLength(data, offset);
            final int spaces = getNumSpacesInMBLength(data, offset);

            boolean errorsPresent = false;

            if ((length - spaces) % 3 != 0) {
                errorsPresent = true;
            }

            // if a 0x20 byte occurs amidst a sequence of multibyte characters
            // skip over it and output a space.
            if (data[offset] == 0x20) {
                sb.append(' ');
                offset++;
            } else if (data[offset] >= 0x80) {
                final char c2 = getChar(data[offset], cdt.g0, cdt.g1);
                sb.append(c2);
                offset += 1;
            } else if (curReader == null) {
                if (offset + 3 <= data.length) {
                    final char c = getMBChar(makeMultibyte(data[offset], data[offset + 1],
                            data[offset + 2]));
                    if (c != 0) {
                        sb.append(c);
                        offset += 3;
                    } else {
                        sb.append(data[offset]);
                        sb.append(data[offset + 1]);
                        sb.append(data[offset + 2]);
                        offset += 3;
                    }
                } else {
                    while (offset < data.length) {
                        sb.append(data[offset++]);
                    }
                }
            } else if (errorsPresent == false && offset + 3 <= data.length && (curReader == null || data[offset + 1] != 0x20 && data[offset + 2] != 0x20) && getMBChar(makeMultibyte(
                    data[offset], data[offset + 1], data[offset + 2])) != 0) {
                final char c = getMBChar(makeMultibyte(data[offset], data[offset + 1],
                        data[offset + 2]));

                if (curReader == null || c != 0) {
                    sb.append(c);
                    offset += 3;
                }
            } else if (offset + 6 < data.length && noneEquals(data, offset, offset + 3, ' ') && 
                    (getMBChar(makeMultibyte(data[offset + 0], data[offset + 1], data[offset + 2])) == 0 || 
                     getMBChar(makeMultibyte(data[offset + 3], data[offset + 4], data[offset + 5])) == 0) && 
                     getMBChar(makeMultibyte(data[offset + 2], data[offset + 3], data[offset + 4])) != 0 && 
                     noneEquals(data, offset, offset + 5, 0x1b) && 
                     noneInRange(data, offset, offset + 5, 0x80, 0xFF) && 
                     !nextEscIsMB(data, offset, data.length)) {

                final String mbstr = getMBCharStr(makeMultibyte(data[offset], '[', data[offset + 1])) + 
                                     getMBCharStr(makeMultibyte(data[offset], ']', data[offset + 1])) + 
                                     getMBCharStr(makeMultibyte(data[offset], data[offset + 1], '[')) + 
                                     getMBCharStr(makeMultibyte(data[offset], data[offset + 1], ']'));
                if (mbstr.length() == 1) {
                    if (curReader != null) {
                        curReader.addError(MarcError.MINOR_ERROR,
                                        "Missing square brace character in MARC8 multibyte character, inserting one to create the only valid option");
                    }

                    sb.append(mbstr);
                    offset += 2;
                } else if (mbstr.length() > 1) {
                    if (curReader != null) {
                        curReader.addError(MarcError.MAJOR_ERROR,
                                        "Missing square brace character in MARC8 multibyte character, inserting one to create a randomly chosen valid option");
                    }

                    sb.append(mbstr.subSequence(0, 1));
                    offset += 2;
                } else if (mbstr.length() == 0) {
                    if (curReader != null) {
                        curReader.addError(MarcError.MINOR_ERROR,
                                        "Erroneous MARC8 multibyte character, Discarding bad character and continuing reading Multibyte characters");
                    }

                    sb.append("[?]");
                    offset += 2;
                }
            } else if (offset + 7 < data.length && noneEquals(data, offset, offset + 3, ' ') && 
                    (getMBChar(makeMultibyte(data[offset + 0], data[offset + 1], data[offset + 2])) == 0 || 
                     getMBChar(makeMultibyte(data[offset + 3], data[offset + 4], data[offset + 5])) == 0) && 
                     getMBChar(makeMultibyte(data[offset + 4], data[offset + 5], data[offset + 6])) != 0 && 
                     noneEquals(data, offset, offset + 6, 0x1b) && 
                     noneInRange(data, offset, offset + 6, 0x80, 0xFF) && !nextEscIsMB(data, offset, data.length)) {

                final String mbstr = getMBCharStr(makeMultibyte(data[offset], '[', data[offset + 1])) + 
                        getMBCharStr(makeMultibyte(data[offset], ']', data[offset + 1])) + 
                        getMBCharStr(makeMultibyte(data[offset], data[offset + 1], '[')) + 
                        getMBCharStr(makeMultibyte(data[offset], data[offset + 1], ']'));
                if (mbstr.length() == 1) {
                    if (curReader != null) {
                        curReader.addError(MarcError.MINOR_ERROR,
                                        "Missing square brace character in MARC8 multibyte character, inserting one to create the only valid option");
                    }
                    sb.append(mbstr);
                    offset += 2;
                } else if (mbstr.length() > 1) {
                    if (curReader != null) {
                        curReader.addError(MarcError.MAJOR_ERROR,
                                        "Missing square brace character in MARC8 multibyte character, inserting one to create a randomly chosen valid option");
                    }
                    sb.append(mbstr.subSequence(0, 1));
                    offset += 2;
                } else if (mbstr.length() == 0) {
                    if (curReader != null) {
                        curReader.addError(MarcError.MINOR_ERROR,
                                        "Erroneous MARC8 multibyte character, Discarding bad character and continuing reading Multibyte characters");
                    }
                    sb.append("[?]");
                    offset += 2;
                }
            } else if (offset + 4 <= data.length && data[offset] > 0x7f && 
                    getMBChar(makeMultibyte(data[offset + 1], data[offset + 2], data[offset + 3])) != 0) {

                if (curReader != null) {
                    curReader
                            .addError(
                                    MarcError.MINOR_ERROR,
                                    "Erroneous character in MARC8 multibyte character, Copying bad character and continuing reading Multibyte characters");
                    sb.append(getChar(data[offset], 0x42, 0x45));
                    offset += 1;
                }
            } else if (curReader != null && offset + 4 <= data.length && (data[offset + 1] == 0x20 || data[offset + 2] == 0x20)) {
                final int multiByte = makeMultibyte(data[offset],
                        data[offset + 1] != 0x20 ? data[offset + 1] : data[offset + 2],
                        data[offset + 3]);
                final char c = getMBChar(multiByte);
                if (c != 0) {
                    if (curReader != null) {
                        curReader.addError(MarcError.ERROR_TYPO,
                                "Extraneous space found within MARC8 multibyte character");
                    }
                    sb.append(c);
                    sb.append(' ');
                    offset += 4;
                } else {
                    if (curReader != null) {
                        curReader.addError(MarcError.MINOR_ERROR,
                                        "Erroneous MARC8 multibyte character, inserting change to default character set");
                    }
                    cdt.multibyte = false;
                    cdt.g0 = 0x42;
                    cdt.g1 = 0x45;
                    break;
                }
            } else if (offset + 3 > data.length || offset + 3 == data.length && (data[offset + 1] == 0x20 || data[offset + 2] == 0x20)) {
                if (curReader != null) {
                    curReader.addError(MarcError.MINOR_ERROR,
                                    "Partial MARC8 multibyte character, inserting change to default character set");
                }
                cdt.multibyte = false;
                cdt.g0 = 0x42;
                cdt.g1 = 0x45;
                break;
            } else if (offset + 3 <= data.length && getMBChar(makeMultibyte(data[offset + 0],
                    data[offset + 1], data[offset + 2])) != 0) {
                final char c = getMBChar(makeMultibyte(data[offset], data[offset + 1],
                        data[offset + 2]));
                if (curReader == null || c != 0) {
                    sb.append(c);
                    offset += 3;
                }
            } else {
                if (curReader != null) {
                    curReader.addError(MarcError.MINOR_ERROR,
                                    "Erroneous MARC8 multibyte character, inserting change to default character set");
                }
                cdt.multibyte = false;
                cdt.g0 = 0x42;
                cdt.g1 = 0x45;

                break;
            }
        }

        cdt.offset = offset;

        return sb.toString();
    }

    private boolean nextEscIsMB(final char[] data, final int start, final int length) {
        for (int offset = start; offset < length - 1; offset++) {
            if (data[offset] == (char) 0x1b) {
                if (data[offset + 1] == '$') {
                    return true;
                } else {
                    break;
                }
            }
        }
        return false;
    }

    private boolean noneEquals(final char[] data, final int start, final int end, final int val) {
        for (int offset = start; offset <= end; offset++) {
            if (data[offset] == (char) val) {
                return false;
            }
        }
        return true;
    }

    private boolean noneInRange(final char[] data, final int start, final int end, final int val1,
            final int val2) {
        for (int offset = start; offset <= end; offset++) {
            if (data[offset] >= (char) val1 && data[offset] <= (char) val2) {
                return false;
            }
        }
        return true;
    }

    private int getRawMBLength(final char[] data, int offset) {
        int length = 0;

        while (offset < data.length && data[offset] != 0x1b) {
            offset++;
            length++;
        }
        return length;
    }

    private int getNumSpacesInMBLength(final char[] data, int offset) {
        int cnt = 0;

        while (offset < data.length && data[offset] != 0x1b) {
            if (data[offset] == ' ') {
                cnt++;
            }

            offset++;
        }

        return cnt;
    }

    private char getCharCDT(final char[] data, final CodeTracker cdt) {
        char c = getChar(data[cdt.offset], cdt.g0, cdt.g1);

        if (translateNCR && c == '&' && data.length >= cdt.offset + 5) {
            if (data[cdt.offset+1] == '#' && data[cdt.offset+2] == 'x') {
                int len = 0;
                for (; cdt.offset + 3 + len < data.length; len++ ) {
                    char c1 = data[cdt.offset + 3 + len];
                    if ((c1 >= '0' && c1 <= '9') || (c1 >= 'A' && c1 <= 'F') || (c1 >= 'a' && c1 <= 'f')) {
                        continue;
                    } else if (len >= 1 && c1 == ';') {
                        c = getCharFromCodePoint(new String(data, cdt.offset+3, len));
                        cdt.offset += len + 4;
                        if (c == '\r' || c == '\n') {
                            if (curReader != null) {
                                curReader.addError(MarcError.MINOR_ERROR,
                                                "Subfield contains Unicode Numeric Character Reference for new line or carriage return, which are invalid");
                            }
                        }
                        return c;
                    } else if (len == 0 && c1 == ';') {
                        if (curReader != null) {
                            curReader.addError(MarcError.MAJOR_ERROR,
                                            "Subfield contains missing Unicode Numeric Character Reference : " + new String(data, cdt.offset, 4));
                        }
                        cdt.offset += 4;
                        c = getCharCDT(data, cdt);
                        return c;
                    } else if (len >= 1 && c1 == '%' && data.length > cdt.offset + len + 4 && 
                            data[cdt.offset + 3 + len + 1] =='x' && (data.length == cdt.offset + len + 5 || data[cdt.offset + 3 + len + 2] !=';' )) {
                        c = getCharFromCodePoint(new String(data, cdt.offset+3, len));
                        if (curReader != null) {
                            curReader.addError(MarcError.MINOR_ERROR,
                                            "Subfield contains malformed Unicode Numeric Character Reference : " + new String(data, cdt.offset, len+5));
                        }
                        cdt.offset += len + 5;
                        return c;
                    } else if (len >= 1 && c1 == '%' && data.length > cdt.offset + len + 5 && 
                         data[cdt.offset + 3 + len + 1] =='x' && data[cdt.offset + 3 + len + 2] ==';' ) {
                        c = getCharFromCodePoint(new String(data, cdt.offset+3, len));
                        if (curReader != null) {
                            curReader.addError(MarcError.MINOR_ERROR,
                                          "Subfield contains malformed Unicode Numeric Character Reference : " + new String(data, cdt.offset, len+6));
                        }
                        cdt.offset += len + 6;
                        return c;
                    } else {
                        if (curReader != null) {
                            curReader.addError(MarcError.MINOR_ERROR,
                                            "Subfield contains malformed Unicode Numeric Character Reference : " + new String(data, cdt.offset, len+3));
                        }
                        cdt.offset++;
                        return c;
                    }
                }
                if (curReader != null) {
                    curReader.addError(MarcError.MINOR_ERROR,
                                    "Subfield contains unterminated Unicode Numeric Character Reference : " + new String(data, cdt.offset, len+3));
                }
                c = getCharFromCodePoint(new String(data, cdt.offset+3, len));
                cdt.offset += len + 3;
                return c;
            } else {
                cdt.offset++;
            }
        } else {
            cdt.offset++;
        }
        return c;
    }

    private char getCharFromCodePoint(final String charCodePoint) {
        final int charNum = Integer.parseInt(charCodePoint, 16);
        return (char) charNum;
    }

    /**
     * Makes a multibyte.
     *
     * @param c1 Character one
     * @param c2 Character two
     * @param c3 Character three
     * @return A multibyte
     */
    public int makeMultibyte(final char c1, final char c2, final char c3) {
        final int[] chars = new int[3];

        chars[0] = c1 << 16;
        chars[1] = c2 << 8;
        chars[2] = c3;

        return chars[0] | chars[1] | chars[2];
    }

    private char getChar(final int ch, final int g0, final int g1) {
        if (ch <= 0x7E) {
            return ct.getChar(ch, g0);
        } else {
            return ct.getChar(ch, g1);
        }
    }

    /**
     * Gets the multibyte character.
     *
     * @param ch The int from which to get the multibyte character
     * @return The multibyte character
     */
    public char getMBChar(final int ch) {
        return ct.getChar(ch, 0x31);
    }

    /**
     * Gets the multibyte character string.
     *
     * @param ch The int from which to get the multibyte character
     * @return The multibyte character string
     */
    public String getMBCharStr(final int ch) {
        final char c = ct.getChar(ch, 0x31);

        if (c == 0) {
            return "";
        } else {
            return "" + c;
        }
    }

    private static boolean hasNext(final int pos, final int len) {
        if (pos < len - 1) {
            return true;
        }

        return false;
    }

    private static boolean isEscape(final int i) {
        if (i == 0x1B) {
            return true;
        }

        return false;
    }

}
