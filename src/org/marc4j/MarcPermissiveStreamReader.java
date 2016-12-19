/**
 * Copyright (C) 2004 Bas Peters
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

package org.marc4j;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.converter.CharConverter;
import org.marc4j.converter.impl.AnselToUnicode;
import org.marc4j.converter.impl.Iso5426ToUnicode;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.Verifier;

/**
 * An iterator over a collection of MARC records in ISO 2709 format, that is designed
 * to be able to handle MARC records that have errors in their structure or their encoding.
 * If the permissive flag is set in the call to the constructor, or if a MarcError object
 * is passed in as a parameter to the constructor, this reader will do its best to detect 
 * and recover from a number of structural or encoding errors that can occur in a MARC record.
 * Note that if this reader is not set to read permissively, its will operate pretty much 
 * identically to the MarcStreamReader class.
 * 
 * Note that no attempt is made to validate the contents of the record at a semantic level.
 * This reader does not know and does not care whether the record has a 245 field, or if the
 * 008 field is the right length, but if the record claims to be UTF-8 or MARC8 encoded and 
 * you are seeing gibberish in the output, or if the reader is throwing an exception in trying
 * to read a record, then this reader may be able to produce a usable record from the bad 
 * data you have.
 * 
 * The ability to directly translate the record to UTF-8 as it is being read in is useful in
 * cases where the UTF-8 version of the record will be used directly by the program that is
 * reading the MARC data, for instance if the marc records are to be indexed into a SOLR search
 * engine.  Previously the MARC record could only be translated to UTF-8 as it was being written 
 * out via a MarcStreamWriter or a MarcXmlWriter.
 * 
 * <p>
 * Example usage:
 * 
 * <pre>
 * InputStream input = new FileInputStream(&quot;file.mrc&quot;);
 * MarcReader reader = new MarcPermissiveStreamReader(input, true, true);
 * while (reader.hasNext()) {
 *     Record record = reader.next();
 *     // Process record
 * }
 * </pre>
 * 
 * <p>
 * Check the {@link org.marc4j.marc}&nbsp;package for examples about the use of
 * the {@link org.marc4j.marc.Record}&nbsp;object model.
 * Check the file org.marc4j.samples.PermissiveReaderExample.java for an
 * example about using the MarcPermissiveStreamReader in conjunction with the 
 * MarcError class to report errors encountered while processing records.
 * </p>
 * 
 * <p>
 * When no encoding is given as an constructor argument the parser tries to
 * resolve the encoding by looking at the character coding scheme (leader
 * position 9) in MARC21 records. For UNIMARC records this position is not
 * defined.   If the reader is operating in permissive mode and no encoding 
 * is given as an constructor argument the reader will look at the leader, 
 * and also at the data of the record to determine to the best of its ability 
 * what character encoding scheme has been used to encode the data in a 
 * particular MARC record.
 *   
 * </p>
 *
 * @author Robert Haschart
 */
public class MarcPermissiveStreamReader implements MarcReader {

    private DataInputStream input = null;

    private Record record;

    private String currentField;

    private String currentSubfield;

    private final MarcFactory factory;

    private String encoding = "ISO8859_1";

    // This represents the expected encoding of the data when a
    // MARC record does not have a 'a' in character 9 of the leader.
    private String defaultEncoding = "ISO8859_1";

    private boolean convertToUTF8 = false;

    private boolean permissive = false;

    private boolean translateLosslessUnicodeNumericCodeReferencesEnabled = true;

    private int marc_file_lookahead_buffer = 200000;

    private AnselToUnicode converterAnsel = null;

    private CharConverter converterUnimarc = null;

    // These are used to algorithmically determine what encoding scheme was
    // used to encode the data in the Marc record
    private String conversionCheck1 = null;

    private String conversionCheck2 = null;

    private String conversionCheck3 = null;

    @SuppressWarnings("deprecation")
    private final ErrorHandler errors;

    static String validSubfieldCodes = "abcdefghijklmnopqrstuvwxyz0123456789";

    static String upperCaseSubfieldsProperty = "org.marc4j.MarcPermissiveStreamReader.upperCaseSubfields";

    /**
     * Constructs an instance with the specified input stream with possible additional functionality
     * being enabled by setting permissive and/or convertToUTF8 to true.
     * 
     * If permissive and convertToUTF8 are both set to false, it functions almost identically to the
     * MarcStreamReader class.
     *
     * @param input - the InputStream to read the records from
     * @param permissive - true to specify that the permissive/error correcting features should be used
     * @param convertToUTF8 - true to specify that records should be converted to UTF8 as they are being read
     */
    public MarcPermissiveStreamReader(final InputStream input, final boolean permissive,
            final boolean convertToUTF8) {
        this.permissive = permissive;
        this.input = new DataInputStream(new BufferedInputStream(input));
        factory = MarcFactory.newInstance();
        this.convertToUTF8 = convertToUTF8;
        errors = null;
        if (permissive) {
            // errors = new ErrorHandler();
            defaultEncoding = "BESTGUESS";
        }
    }

    /**
     * Constructs an instance with the specified input stream with possible additional functionality
     * being enabled by passing in an MarcError object and/or setting convertToUTF8 to true.
     * 
     * If errors and convertToUTF8 are both set to false, it functions almost identically to the
     * MarcStreamReader class.
     * 
     * If an MarcError object is passed in, that object will be used to log and track any errors 
     * in the records as the records are decoded.  After the next() function returns, you can query 
     * to determine whether any errors were detected in the decoding process.
     * 
     * See the  file org.marc4j.samples.PermissiveReaderExample.java to see how this can be done.
     *
     * @param input - the InputStream to read the records from
     * @param errors - an older way of tracking errors found in records
     * @param convertToUTF8 - true to specify that records should be converted to UTF8 as they are being read
     */
    @Deprecated
    public MarcPermissiveStreamReader(final InputStream input, final ErrorHandler errors,
            final boolean convertToUTF8) {
        if (errors != null) {
            permissive = true;
            defaultEncoding = "BESTGUESS";
        }
        this.input = new DataInputStream(input.markSupported() ? input : new BufferedInputStream(
                input));
        factory = MarcFactory.newInstance();
        this.convertToUTF8 = convertToUTF8;
        this.errors = errors;
    }

    /**
     * Constructs an instance with the specified input stream with possible additional functionality
     * being enabled by setting permissive and/or convertToUTF8 to true.
     * 
     * If permissive and convertToUTF8 are both set to false, it functions almost identically to the
     * MarcStreamReader class.
     * 
     * The parameter defaultEncoding is used to specify the character encoding that is used in the records
     * that will be read from the input stream.   If permissive is set to true, you can specify "BESTGUESS"
     * as the default encoding, and the reader will attempt to determine the character encoding used in the 
     * records being read from the input stream.   This is especially useful if you are working with records 
     * downloaded from an external source and the encoding is either unknown or the encoding is different from
     * what the records claim to be.
     *
     * @param input - the InputStream to read the records from
     * @param permissive - true to specify that the permissive/error correcting features should be used
     * @param convertToUTF8 - true to specify that records should be converted to UTF8 as they are being read
     * @param defaultEncoding - the expected encoding to be found in the records being read
     */
    public MarcPermissiveStreamReader(final InputStream input, final boolean permissive,
            final boolean convertToUTF8, final String defaultEncoding) {
        this.permissive = permissive;
        this.input = new DataInputStream(input.markSupported() ? input : new BufferedInputStream(
                input));
        factory = MarcFactory.newInstance();
        this.convertToUTF8 = convertToUTF8;
        this.defaultEncoding = defaultEncoding;
        errors = null;
        // if (permissive) errors = new ErrorHandler();
    }

    /**
     * Constructs an instance with the specified input stream with possible additional functionality
     * being enabled by setting permissive and/or convertToUTF8 to true.
     * 
     * If errors and convertToUTF8 are both set to false, it functions almost identically to the
     * MarcStreamReader class.
     * 
     * The parameter defaultEncoding is used to specify the character encoding that is used in the records
     * that will be read from the input stream.   If permissive is set to true, you can specify "BESTGUESS"
     * as the default encoding, and the reader will attempt to determine the character encoding used in the 
     * records being read from the input stream.   This is especially useful if you are working with records 
     * downloaded from an external source and the encoding is either unknown or the encoding is different from
     * what the records claim to be.
     * 
     * If an MarcError object is passed in, that object will be used to log and track any errors 
     * in the records as the records are decoded.  After the next() function returns, you can query 
     * to determine whether any errors were detected in the decoding process.
     * 
     * See the  file org.marc4j.samples.PermissiveReaderExample.java to see how this can be done.
     *
     * @param input - the InputStream to read the records from
     * @param errors - an older way of tracking errors found in records
     * @param convertToUTF8 - true to specify that records should be converted to UTF8 as they are being read
     * @param defaultEncoding - the expected encoding to be found in the records being read
     */
    @Deprecated
    public MarcPermissiveStreamReader(final InputStream input, final ErrorHandler errors,
            final boolean convertToUTF8, final String defaultEncoding) {
        this.permissive = true;
        this.input = new DataInputStream(new BufferedInputStream(input));
        factory = MarcFactory.newInstance();
        this.convertToUTF8 = convertToUTF8;
        this.defaultEncoding = defaultEncoding;
        this.errors = errors;
    }

    /**
     * @return true if numeric character entities like &amp;#xFFFD; should be converted to their corresponding code point
     *         if converting to unicode. Default is to convert.
     */
    public boolean isTranslateLosslessUnicodeNumericCodeReferencesEnabled() {
        return translateLosslessUnicodeNumericCodeReferencesEnabled;
    }

    /**
     * Enable conversion of numeric code references into their corresponding code points when converting to unicode
     *
     * @param translateLosslessUnicodeNumericCodeReferencesEnabled - true to enable conversion of NCR to Unicode characters
     */
    public void setTranslateLosslessUnicodeNumericCodeReferencesEnabled(
            final boolean translateLosslessUnicodeNumericCodeReferencesEnabled) {
        this.translateLosslessUnicodeNumericCodeReferencesEnabled = translateLosslessUnicodeNumericCodeReferencesEnabled;
    }

    /**
     * Returns true if the iteration has more records, false otherwise.
     */
    @Override
    public boolean hasNext() {
        try {
            input.mark(10);
            int byteread = input.read();
            if (byteread == -1) {
                return false;
            }
            // byte[] recLengthBuf = new byte[5];
            int numBadBytes = 0;
            while (byteread < '0' || byteread > '9') {
                byteread = input.read();
                numBadBytes++;
                if (byteread == -1) {
                    return false;
                }
            }
            input.reset();
            while (numBadBytes > 0) {
                byteread = input.read();
                numBadBytes--;
            }
        } catch (final IOException e) {
            throw new MarcException(e.getMessage(), e);
        }
        return true;
    }

    /**
     * Returns the next record in the iteration.
     *
     * @return Record - the record object
     */
    @SuppressWarnings("deprecation")
    @Override
    public Record next() {
        record = factory.newRecord();
        if (errors != null) {
            errors.reset();
        }

        try {
            final byte[] byteArray = new byte[24];

            input.readFully(byteArray);
            int recordLength = parseRecordLength(byteArray);
            byte[] recordBuf = new byte[recordLength - 24];
            if (permissive) {
                input.mark(marc_file_lookahead_buffer);
                input.readFully(recordBuf);
                if (recordBuf[recordBuf.length - 1] != Constants.RT) {
                    record.addError("n/a", "n/a", MarcError.MAJOR_ERROR,
                            "Record terminator character not found at end of record length");
                    recordBuf = rereadPermissively(record, input, recordBuf, recordLength);
                    recordLength = recordBuf.length + 24;
                }
            } else {
                input.readFully(recordBuf);
            }
            // final String tmp = new String(recordBuf);
            parseRecord(record, byteArray, recordBuf, recordLength);

            if (this.convertToUTF8) {
                final Leader l = record.getLeader();
                l.setCharCodingScheme('a');
                record.setLeader(l);
            }
            if (errors != null && record.hasErrors()) {
                errors.addErrors(record.getControlNumber(), record.getErrors());
            }
            return record;
        } catch (final EOFException e) {
            throw new MarcException("Premature end of file encountered", e);
        } catch (final IOException e) {
            throw new MarcException("an error occured reading input", e);
        }
    }

    private byte[] rereadPermissively(final Record record, final DataInputStream input,
            byte[] recordBuf, int recordLength) throws IOException {
        int loc = arrayContainsAt(recordBuf, Constants.RT);
        if (loc != -1)  // stated record length is too long
        {
            record.addError("n/a", "n/a", MarcError.MAJOR_ERROR,
                    "Record terminator appears before stated record length, using shorter record");
            recordLength = loc + 24;
            input.reset();
            recordBuf = new byte[recordLength - 24];
            input.readFully(recordBuf);
        } else  // stated record length is too short read ahead
        {
            loc = recordLength - 24;

            boolean done = false;

            while (!done) {
                int c = 0;
                do {
                    c = input.read();
                    loc++;
                } while (loc < marc_file_lookahead_buffer - 24 && c != Constants.RT && c != -1);

                if (c == Constants.RT) {
                    record.addError("n/a", "n/a", MarcError.MAJOR_ERROR,
                            "Record terminator appears after stated record length, reading extra bytes");
                    recordLength = loc + 24;
                    input.reset();
                    recordBuf = new byte[recordLength - 24];
                    input.readFully(recordBuf);
                    done = true;
                } else if (c == -1) {
                    record.addError("n/a", "n/a", MarcError.MAJOR_ERROR,
                            "No Record terminator found, end of file reached, Terminator appended");
                    recordLength = loc + 24;
                    input.reset();
                    recordBuf = new byte[recordLength - 24 + 1];
                    input.readFully(recordBuf);
                    recordBuf[recordBuf.length - 1] = Constants.RT;
                    done = true;
                } else {
                    record.addError("n/a", "n/a", MarcError.FATAL,
                            "No Record terminator found within " + marc_file_lookahead_buffer + " bytes of start of record, getting desperate.");
                    input.reset();
                    marc_file_lookahead_buffer *= 2;
                    input.mark(marc_file_lookahead_buffer);
                    loc = 0;
                }
            }
        }

        return recordBuf;
    }

    private void parseRecord(final Record record, byte[] byteArray, byte[] recordBuf,
            final int recordLength) {
        Leader ldr;
        String utfCheck;
        int directoryLength = 0;

        ldr = factory.newLeader();
        ldr.setRecordLength(recordLength);

        // These variables are used when the permissive reader is trying to make
        // its best guess
        // as to what character encoding is actually used in the record being
        // processed.
        conversionCheck1 = "";
        conversionCheck2 = "";
        conversionCheck3 = "";

        try {
            parseLeader(ldr, byteArray);
            directoryLength = ldr.getBaseAddressOfData() - (24 + 1);
        } catch (final IOException e) {
            throw new MarcException("error parsing leader with data: " + new String(byteArray), e);
        } catch (final MarcException e) {
            if (permissive) {
                if (recordBuf[recordBuf.length - 1] == Constants.RT && recordBuf[recordBuf.length - 2] == Constants.FT) {
                    record.addError("n/a", "n/a", MarcError.MAJOR_ERROR,
                            "Error parsing leader, trying to re-read leader either shorter or longer");
                    // make an attempt to recover record.
                    int offset = 0;

                    while (offset < recordBuf.length) {
                        if (recordBuf[offset] == Constants.FT) {
                            break;
                        }

                        offset++;
                    }

                    if (offset % 12 == 1) {
                        // move one byte from body to leader, make new leader,
                        // and try again
                        record.addError("n/a", "n/a", MarcError.MAJOR_ERROR,
                                "Leader appears to be too short, moving one byte from record body to leader, and trying again");
                        final byte oldBody[] = recordBuf;
                        recordBuf = new byte[oldBody.length - 1];
                        System.arraycopy(oldBody, 1, recordBuf, 0, oldBody.length - 1);
                        directoryLength = offset - 1;
                        ldr.setIndicatorCount(2);
                        ldr.setSubfieldCodeLength(2);
                        ldr.setImplDefined1(("" + (char) byteArray[7] + " ").toCharArray());
                        ldr.setImplDefined2(("" + (char) byteArray[18] + (char) byteArray[19] + (char) byteArray[20])
                                .toCharArray());
                        ldr.setEntryMap("4500".toCharArray());

                        // if its ' ' or 'a'
                        if (byteArray[10] == (byte) ' ' || byteArray[10] == (byte) 'a') {
                            ldr.setCharCodingScheme((char) byteArray[10]);
                        }
                    } else if (offset % 12 == 11) {
                        record.addError("n/a", "n/a", MarcError.MAJOR_ERROR,
                                "Leader appears to be too long, moving one byte from leader to record body, and trying again");
                        final byte oldBody[] = recordBuf;
                        recordBuf = new byte[oldBody.length + 1];
                        System.arraycopy(oldBody, 0, recordBuf, 1, oldBody.length);
                        recordBuf[0] = (byte) '0';
                        directoryLength = offset + 1;
                        ldr.setIndicatorCount(2);
                        ldr.setSubfieldCodeLength(2);
                        ldr.setImplDefined1(("" + (char) byteArray[7] + " ").toCharArray());
                        ldr.setImplDefined2(("" + (char) byteArray[16] + (char) byteArray[17] + (char) byteArray[18])
                                .toCharArray());
                        ldr.setEntryMap("4500".toCharArray());

                        // if its ' ' or 'a'
                        if (byteArray[8] == (byte) ' ' || byteArray[8] == (byte) 'a') {
                            ldr.setCharCodingScheme((char) byteArray[10]);
                        }

                        // if its ' ' or 'a'
                        if (byteArray[10] == (byte) ' ' || byteArray[10] == (byte) 'a') {
                            ldr.setCharCodingScheme((char) byteArray[10]);
                        }
                    } else {
                        record.addError("n/a", "n/a", MarcError.FATAL,
                                "error parsing leader with data: " + new String(byteArray));
                        throw new MarcException("error parsing leader with data: " + new String(
                                byteArray), e);
                    }
                }
            } else {
                throw new MarcException("error parsing leader with data: " + new String(byteArray),
                        e);
            }
        }
        final char tmp[] = ldr.getEntryMap();
        if (permissive && !("" + tmp[0] + tmp[1] + tmp[2] + tmp[3]).equals("4500")) {
            if (tmp[0] >= '0' && tmp[0] <= '9' && tmp[1] >= '0' && tmp[1] <= '9' && tmp[2] >= '0' && tmp[2] <= '9' && tmp[3] >= '0' && tmp[3] <= '9') {
                record.addError("n/a", "n/a", MarcError.ERROR_TYPO,
                        "Unusual character found at end of leader [ " + tmp[0] + tmp[1] + tmp[2] + tmp[3] + " ]");
            } else {
                record.addError("n/a", "n/a", MarcError.ERROR_TYPO,
                        "Erroneous character found at end of leader [ " + tmp[0] + tmp[1] + tmp[2] + tmp[3] + " ]; changing them to the standard \"4500\"");
                ldr.setEntryMap("4500".toCharArray());
            }
        }

        // if MARC 21 then check encoding
        switch (ldr.getCharCodingScheme()) {
            case 'a':
                encoding = "UTF8";
                break;
            case ' ':
                if (convertToUTF8) {
                    encoding = defaultEncoding;
                } else {
                    encoding = "ISO8859_1";
                }
                break;
            default:
                if (convertToUTF8) {
                    if (permissive) {
                        record.addError("n/a", "n/a", MarcError.MINOR_ERROR,
                                "Record character encoding should be 'a' or ' ' in this record it is '" + 
                                 ldr.getCharCodingScheme() + "'. Attempting to guess the correct encoding.");
                        encoding = "BESTGUESS";
                    } else {
                        encoding = defaultEncoding;
                    }
                } else {
                    encoding = "ISO8859_1";
                }
                break;

        }

        if (encoding.equalsIgnoreCase("BESTGUESS")) {
            try {
                final String marc8EscSeqCheck = new String(recordBuf, "ISO-8859-1");
                // If record has MARC8 character set selection strings, it must
                // be MARC8 encoded
                if (marc8EscSeqCheck.split("\\e[-(,)$bsp]", 2).length > 1) {
                    encoding = "MARC8";
                } else {
                    boolean hasHighBitChars = false;
                    for (int i = 0; i < recordBuf.length; i++) {
                        if (recordBuf[i] < 0) // the high bit is set
                        {
                            hasHighBitChars = true;
                            break;
                        }
                    }
                    if (!hasHighBitChars) {
                        encoding = "ISO8859_1";  // You can choose any encoding
                                                // you want here, the results
                                                // will be the same.
                    } else {
                        utfCheck = new String(recordBuf, "UTF-8");
                        final byte byteCheck[] = utfCheck.getBytes("UTF-8");
                        encoding = "UTF8";

                        if (recordBuf.length == byteCheck.length) {
                            for (int i = 0; i < recordBuf.length; i++) {
                                if (byteCheck[i] != recordBuf[i]) {
                                    encoding = "MARC8-Maybe";
                                    break;
                                }
                            }
                        } else {
                            encoding = "MARC8-Maybe";
                        }
                    }
                }
            } catch (final UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (permissive && encoding.equals("UTF8")) {
            try {
                utfCheck = new String(recordBuf, "UTF-8");
                final byte byteCheck[] = utfCheck.getBytes("UTF-8");

                if (recordBuf.length != byteCheck.length) {
                    boolean foundESC = false;

                    for (int i = 0; i < recordBuf.length; i++) {
                        if (recordBuf[i] == 0x1B) {
                            record.addError("n/a", "n/a", MarcError.MINOR_ERROR,
                                    "Record claims to be UTF-8, but its not. Its probably MARC8.");
                            encoding = "MARC8-Maybe";
                            foundESC = true;
                            break;
                        }

                        if (!foundESC && byteCheck[i] != recordBuf[i]) {
                            encoding = "MARC8-Maybe";
                        }

                    }

                    if (!foundESC) {
                        record.addError("n/a", "n/a", MarcError.MINOR_ERROR,
                                "Record claims to be UTF-8, but its not. It may be MARC8, or maybe UNIMARC, or maybe raw ISO-8859-1 ");
                    }
                }

                if (utfCheck.contains("a$1!")) {
                    encoding = "MARC8-Broken";
                    record.addError("n/a", "n/a", MarcError.MAJOR_ERROR,
                            "Record claims to be UTF-8, but its not. It seems to be MARC8-encoded but with missing escape codes.");
                }
            } catch (final UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (permissive && !encoding.equals("UTF8") && convertToUTF8) {
            try {
                final String marc8EscSeqCheck = new String(recordBuf, "ISO-8859-1");
                final boolean hasMarc8EscSeq = marc8EscSeqCheck.split("\\e[-(,)$bsp]", 2).length > 1;
                utfCheck = new String(recordBuf, "UTF-8");
                final byte byteCheck[] = utfCheck.getBytes("UTF-8");

                if (recordBuf.length == byteCheck.length) {
                    for (int i = 0; i < recordBuf.length; i++) {
                        // need to check for byte < 0 to see if the high bit is
                        // set, because Java doesn't have unsigned types.
                        if (recordBuf[i] < 0x00 || byteCheck[i] != recordBuf[i]) {
                            // If record has MARC8 character set selection
                            // strings, it must be MARC8 encoded
                            if (hasMarc8EscSeq) {
                                record.addError("n/a", "n/a", MarcError.MINOR_ERROR,
                                        "Record has MARC8 escape sequences, but also seem to have UTF8-encoded characters.");
                                encoding = "MARC8-Maybe";
                            } else {
                                record.addError("n/a", "n/a", MarcError.MINOR_ERROR,
                                        "Record claims not to be UTF-8, but it seems to be.");
                                encoding = "UTF8-Maybe";
                            }
                            break;
                        }
                    }
                }
            } catch (final UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        record.setLeader(ldr);

        int size = directoryLength / 12;

        final ArrayList<String> tags = new ArrayList<String>(size);
        final ArrayList<Integer> lengths = new ArrayList<Integer>(size);
        final ArrayList<Integer> offsets = new ArrayList<Integer>(size);
        final HashMap<Integer, Integer> offsetsMap = new HashMap<Integer, Integer>();
        boolean unsortedOffsets = false;
        int offsetToFT = 0;

        if (directoryLength % 12 == 0 && recordBuf[directoryLength] == Constants.FT) {
            boolean doneWithDirectory = false;
            int totalOffset = 0;
            int offset = 0;
            for (int i = 0; !doneWithDirectory; i++) {
                final int increment = 12;
                final int prevOffset = offset;
                final String dirEntry = new String(recordBuf, offsetToFT, 14);
                final String tag = dirEntry.substring(0, 3);
                final int length = Integer.parseInt(dirEntry.substring(3, 7));
                offset = Integer.parseInt(dirEntry.substring(7, 12));
                tags.add(tag);
                lengths.add(length);
                if (offset >= 99999) {
                    offset = prevOffset + length;
                }
                offsets.add(offset);
                offsetToFT += increment;
                if (recordBuf[offsetToFT] == Constants.FT) {
                    doneWithDirectory = true;
                }
                offsetsMap.put(offset, i);
                if (offset != totalOffset && totalOffset < 99999) {
                    unsortedOffsets = true;
                }
                totalOffset += length;
            }
            size = tags.size();
        } else // if (directoryLength % 12 != 0 || recordBuf[directoryLength] !=
               // Constants.FT)
        {
            int totalOffset = 0;
            boolean flaggedError1 = false, flaggedError2 = false; //, fixedError3 = false;
            boolean doneWithDirectory = false;
            for (int i = 0; !doneWithDirectory; i++) {
                int increment = 12;
                final String dirEntry = new String(recordBuf, offsetToFT, 14);
                final int ftIndex = dirEntry.indexOf(Constants.FT);
                if (ftIndex > 0 && ftIndex < 12) {
                    record.addError("n/a", "n/a", MarcError.MAJOR_ERROR,
                            "Field terminator in the middle of a directory entry. Discarding entry and trying to continue.");
                    offsetToFT += dirEntry.indexOf(Constants.FT);
                    break;
                }

                String tag = dirEntry.substring(0, 3);
                int length = Integer.parseInt(dirEntry.substring(3, 7));
                int offset = Integer.parseInt(dirEntry.substring(7, 12));
                // this looks for the case where the first directory entry is
                // one byte too short.
                if ((directoryLength - offsetToFT) % 12 == 11 && tag.charAt(1) != '0') {
                    final String tagA = "0" + dirEntry.substring(0, 2);
                    final int lengthA = Integer.parseInt(dirEntry.substring(2, 6));
                    final int offsetA = Integer.parseInt(dirEntry.substring(6, 11));
                    if (recordBuf[directoryLength] == Constants.FT && recordBuf[directoryLength + lengthA] == Constants.FT) {
                        record.addError("n/a", "n/a", MarcError.MAJOR_ERROR,
                                "Directory length is not a multiple of 12 bytes long.  Prepending a zero and trying to continue.");
//                        fixedError3 = true;
                        tag = tagA;
                        length = lengthA;
                        offset = offsetA;
                        increment = 11;
                    }
                }
                // this looks for 6 digit offsets
                if (totalOffset != offset && totalOffset > 99999 && offset != 99999) {
                    int offset1, offset2, length2;
                    try {
                        offset1 = Integer.parseInt(dirEntry.substring(7, 13));
                        offset2 = Integer.parseInt(dirEntry.substring(8, 13));
                        length2 = Integer.parseInt(dirEntry.substring(3, 8));
                        if (offset1 == totalOffset) {
                            offset = offset1;
                            if (!flaggedError1) {
                                record.addError("n/a", "n/a", MarcError.MAJOR_ERROR,
                                        "Offset as stored in directory entry has more than 5 digits. Trying to continue.");
                                flaggedError1 = true;
                            }
                            increment = 13;
                        } else if (offset2 == totalOffset && totalOffset > 0) {
                            offset = offset2;
                            length = length2;
                            if (!flaggedError2) {
                                record.addError("n/a", "n/a", MarcError.MAJOR_ERROR,
                                        "Field is longer than 9999 bytes.  Writing this record out will result in a bad record.");
                                flaggedError2 = true;
                            }
                            increment = 13;
                        }
                    } catch (final NumberFormatException nfe) {
                    }
                }
                tags.add(tag);
                lengths.add(length);
                offsets.add(totalOffset);
                offsetToFT += increment;
                if (recordBuf[offsetToFT] == Constants.FT) {
                    doneWithDirectory = true;
                }
                offsetsMap.put(offset, i);
                if (totalOffset < 99999 && offset != totalOffset) {
                    record.addError("n/a", "n/a", MarcError.FATAL,
                            "Offsets to fields are out of order AND the directory is messed up. Unable to continue.");
                    throw new MarcException(
                            "Offsets to fields are out of order AND the directory is messed up");
                }
                totalOffset += length;
            }
            size = tags.size();
        }
        if (directoryLength != offsetToFT) {
            record.addError("n/a", "n/a", MarcError.MINOR_ERROR,
                    "Specified directory length not equal to actual directory length.");
        }

        if (unsortedOffsets) {
            Collections.sort(offsets);
        }

        try {
            final DataInputStream inputrec = new DataInputStream(
                    new ByteArrayInputStream(recordBuf));
            inputrec.skip(offsetToFT + 1);

            int numBadLengths = 0;

            int totalLength = 0;
            int i = 0;
            for (int s = 0; s < size; s++) {
                i = unsortedOffsets ? offsetsMap.get(offsets.get(s)).intValue() : s;
                final int fieldLength = getFieldLength(inputrec);
                if (fieldLength + 1 != lengths.get(i) && permissive) {
                    if (numBadLengths < 5 && totalLength + fieldLength < recordLength + 26) {
                        inputrec.mark(9999);
                        byteArray = new byte[lengths.get(i)];
                        inputrec.readFully(byteArray);
                        inputrec.reset();
                        if (fieldLength + 1 < lengths.get(i) && byteArray[lengths.get(i) - 1] == Constants.FT) {
                            record.addError("n/a", "n/a", MarcError.MINOR_ERROR,
                                    "Field Terminator character found in the middle of a field.");
                        } else {
                            numBadLengths++;
                            lengths.set(i, fieldLength + 1);
                            record.addError("n/a", "n/a", MarcError.MINOR_ERROR,
                                    "Field length found in record different from length stated in the directory.");
                            if (fieldLength + 1 > 9999) {
                                record.addError("n/a", "n/a", MarcError.FATAL,
                                        "Field length is greater than 9999, record cannot be represented as a binary Marc record.");
                            }
                        }

                    }
                }

                totalLength += lengths.get(i);
                if (isControlField(tags.get(i))) {
                    byteArray = new byte[lengths.get(i) - 1];
                    inputrec.readFully(byteArray);

                    if (inputrec.read() != Constants.FT) {
                        record.addError("n/a", "n/a", MarcError.FATAL,
                                "Expected field terminator at end of field. Unable to continue.");
                        throw new MarcException("expected field terminator at end of field");
                    }

                    final ControlField field = factory.newControlField();
                    field.setTag(tags.get(i));
                    field.setData(getDataAsString(byteArray));
                    record.addVariableField(field);

                } else {
                    byteArray = new byte[lengths.get(i)];
                    inputrec.readFully(byteArray);
                    try {
                        record.addVariableField(parseDataField(record, tags.get(i), byteArray));
                    } catch (final IOException e) {
                        throw new MarcException(
                                "error parsing data field for tag: " + tags.get(i) + " with data: " + new String(
                                        byteArray), e);
                    }
                }
            }

            // We've determined that although the record says it is UTF-8, it is
            // not.
            // Here we make an attempt to determine the actual encoding of the
            // data in the record.
            if (permissive && conversionCheck1.length() > 1 && 
                    conversionCheck2.length() > 1 && conversionCheck3.length() > 1) {
                guessAndSelectCorrectNonUTF8Encoding();
            }
            if (inputrec.read() != Constants.RT) {
                record.addError("n/a", "n/a", MarcError.FATAL,
                        "Expected record terminator at end of record. Unable to continue.");
                throw new MarcException("expected record terminator");
            }
        } catch (final IOException e) {
            record.addError("n/a", "n/a", MarcError.FATAL,
                    "Error reading from data file. Unable to continue.");
            throw new MarcException("an error occured reading input", e);
        }
    }

//    private boolean byteCompare(final byte[] lenCheck, final int offset, final int length,
//            final int totalOffset) {
//        int divisor = 1;
//        for (int i = offset + length - 1; i >= offset; i--, divisor *= 10) {
//            if (totalOffset / divisor % 10 + '0' != lenCheck[i]) {
//                return false;
//            }
//        }
//        return true;
//    }
//
    public void addError(final int severity, final String message) {
        record.addError(currentField, currentSubfield, severity, message);
    }

    private boolean isControlField(final String tag) {
        boolean isControl = false;
        try {
            isControl = Verifier.isControlField(tag);
        } catch (final NumberFormatException nfe) {
            if (permissive) {
                record.addError(tag, "n/a", MarcError.ERROR_TYPO,
                        "Field tag contains non-numeric characters (" + tag + ").");
                isControl = false;
            }
        }
        return isControl;
    }

    private void guessAndSelectCorrectNonUTF8Encoding() {
        int defaultPart = 0;

        if (record.getVariableField("245") == null) {
            defaultPart = 1;
        }

        int partToUse = 0;
        final int l1 = conversionCheck1.length();
        final int l2 = conversionCheck2.length();
        final int l3 = conversionCheck3.length();
        int tst;

        if (l1 < l3 && l2 == l3 && defaultPart == 0) {
            addError(MarcError.INFO, "MARC8 translation shorter than ISO-8859-1, choosing MARC8.");
            partToUse = 0;
        } else if (l2 < l1 - 2 && l2 < l3 - 2) {
            addError(MarcError.INFO, "Unimarc translation shortest, choosing it.");
            partToUse = 1;
        } else if ((tst = onlyOneStartsWithUpperCase(conversionCheck1, conversionCheck2,
                conversionCheck3)) != -1) {
            partToUse = tst;
        } else if (l2 < l1 && l2 < l3) {
            addError(MarcError.INFO, "Unimarc translation shortest, choosing it.");
            partToUse = 1;
        } else if (conversionCheck2.equals(conversionCheck3) && !conversionCheck1.trim().contains(
                " ")) {
            addError(MarcError.INFO,
                    "Unimarc and ISO-8859-1 translations identical, choosing ISO-8859-1.");
            partToUse = 2;
        } else if (!specialCharIsBetweenLetters(conversionCheck1)) {
            addError(MarcError.INFO,
                    "To few letters in translations, choosing " + (defaultPart == 0 ? "MARC8" : "Unimarc"));
            partToUse = defaultPart;
        } else if (l2 == l3 && defaultPart == 1) {
            addError(MarcError.INFO,
                    "Unimarc and ISO-8859-1 translations equal length, choosing ISO-8859-1.");
            partToUse = 2;
        } else {
            addError(MarcError.INFO,
                    "No Determination made, defaulting to " + (defaultPart == 0 ? "MARC8" : "Unimarc"));
            partToUse = defaultPart;
        }
        final List<VariableField> fields = record.getVariableFields();
        final Iterator<VariableField> iter = fields.iterator();
        while (iter.hasNext()) {
            final VariableField field = iter.next();
            if (field instanceof DataField) {
                final DataField df = (DataField) field;
                final List<Subfield> subf = df.getSubfields();
                final Iterator<Subfield> sfiter = subf.iterator();
                while (sfiter.hasNext()) {
                    final Subfield sf = sfiter.next();
                    if (sf.getData().contains("%%@%%")) {
                        final String parts[] = sf.getData().split("%%@%%", 3);
                        sf.setData(parts[partToUse]);
                    }
                }
            }
        }
    }

    private int onlyOneStartsWithUpperCase(final String conversionCheck12,
            final String conversionCheck22, final String conversionCheck32) {
        if (conversionCheck1.length() == 0 || conversionCheck2.length() == 0 || conversionCheck3
                .length() == 0) {
            return -1;
        }
        final String check1Parts[] = conversionCheck1.trim().split("[|]>");
        final String check2Parts[] = conversionCheck2.trim().split("[|]>");
        final String check3Parts[] = conversionCheck3.trim().split("[|]>");
        for (int i = 1; i < check1Parts.length && i < check2Parts.length && i < check3Parts.length; i++) {
            final boolean tst1 = Character.isUpperCase(check1Parts[i].charAt(0));
            final boolean tst2 = Character.isUpperCase(check2Parts[i].charAt(0));
            final boolean tst3 = Character.isUpperCase(check3Parts[i].charAt(0));
            if (tst1 && !tst2 && !tst3) {
                return 0;
            }
            if (!tst1 && tst2 && !tst3) {
                return -1;
            }
            if (!tst1 && !tst2 && tst3) {
                return 2;
            }
        }
        return -1;
    }

    private boolean specialCharIsBetweenLetters(final String conversionCheck) {
        boolean bewteenLetters = true;

        for (int i = 0; i < conversionCheck.length(); i++) {
            final int charCode = conversionCheck.charAt(i);

            if (charCode > 0x7f) {
                bewteenLetters = false;
                if (i > 0 && Character.isLetter((int) conversionCheck.charAt(i - 1)) || i < conversionCheck
                        .length() - 1 && Character.isLetter((int) conversionCheck.charAt(i + 1))) {
                    bewteenLetters = true;
                    break;
                }
            }
        }

        return bewteenLetters;
    }

    private int arrayContainsAt(final byte[] byteArray, final int ft) {
        for (int i = 0; i < byteArray.length; i++) {
            if (byteArray[i] == (byte) ft) {
                return i;
            }
        }

        return -1;
    }

    private DataField parseDataField(final Record record, final String tag, final byte[] field)
            throws IOException {
        if (permissive) {
            if (tag.equals("880")) {
                String fieldTag = new String(field);
                fieldTag = fieldTag.replaceFirst("^.*\\x1F6", "").replaceFirst("([-0-9]*).*", "$1");
                currentField = tag + "(" + fieldTag + ")";
            } else {
                currentField = tag;
            }
            currentSubfield = "n/a";
            cleanupBadFieldSeperators(field, record);
        }
        final ByteArrayInputStream bais = new ByteArrayInputStream(field);
        final char ind1 = (char) bais.read();
        final char ind2 = (char) bais.read();

        final DataField dataField = factory.newDataField();
        dataField.setTag(tag);
        dataField.setIndicator1(ind1);
        dataField.setIndicator2(ind2);

        int code;
        int size;
        int readByte;
        byte[] data;
        Subfield subfield;

        while (true) {
            readByte = bais.read();

            if (readByte < 0) {
                break;
            }

            switch (readByte) {
                case Constants.US:
                    code = bais.read();

                    if (code < 0) {
                        throw new IOException("unexpected end of data field");
                    }

                    if (code == Constants.FT) {
                        break;
                    }

                    size = getSubfieldLength(bais);

                    if (size == 0) {
                        if (permissive) {
                            addError(MarcError.MINOR_ERROR,
                                    "Subfield of zero length encountered, ignoring it.");
                            continue;
                        }

                        throw new IOException("Subfield of zero length encountered");
                    }

                    data = new byte[size];
                    bais.read(data);
                    subfield = factory.newSubfield();

                    if (permissive) {
                        currentSubfield = "" + (char) code;
                    }

                    String dataAsString = getDataAsString(data);

                    if (permissive && code == Constants.US) {
                        code = data[0];
                        dataAsString = dataAsString.substring(1);
                        addError(MarcError.MAJOR_ERROR,
                                "Subfield tag is a subfield separator, using first character of field as subfield tag.");
                    } else if (permissive && validSubfieldCodes.indexOf(code) == -1) {
                        if (code >= 'A' && code <= 'Z') {
                            if (Boolean.parseBoolean(System.getProperty(upperCaseSubfieldsProperty,
                                    "false")) == false) {
                                code = Character.toLowerCase(code);
                                addError(MarcError.MINOR_ERROR,
                                        "Subfield tag is an invalid uppercase character, changing it to lower case.");
                            } else {
                                // the System Property
                                // org.marc4j.MarcPermissiveStreamReader.upperCaseSubfields
                                // is
                                // defined to allow upperCaseSubfields
                                // therefore do nothing and be happy
                            }
                        } else if (code > 0x7f) {
                            code = data[0];
                            dataAsString = dataAsString.substring(1);
                            addError(
                                    MarcError.MAJOR_ERROR,
                                    "Subfield tag is an invalid character greater than 0x7f, using first character of field as subfield tag.");
                        } else if (code == '[' && tag.equals("245")) {
                            code = 'h';
                            dataAsString = '[' + dataAsString;
                            addError(MarcError.MAJOR_ERROR,
                                    "Subfield tag is an open bracket, generating a code 'h' and pushing the bracket to the data.");
                        } else if (code == ' ') {
                            addError(MarcError.MAJOR_ERROR,
                                    "Subfield tag is a space which is an invalid character");
                        } else {
                            addError(MarcError.MAJOR_ERROR,
                                    "Subfield tag is an invalid character, [ " + (char) code + " ]");
                        }
                    }
                    subfield.setCode((char) code);
                    subfield.setData(dataAsString);
                    dataField.addSubfield(subfield);
                    break;
                case Constants.FT:
                    break;
            }
        }
        return dataField;
    }

    static AnselToUnicode conv = null;

    public void cleanupBadFieldSeperators(final byte[] field, final Record record) {
        if (conv == null) {
            conv = new AnselToUnicode(true);
        }

        boolean hasEsc = false;
        boolean inMultiByte = false;
        boolean justCleaned = false;
        int mbOffset = 0;
        boolean inCyrillic = false;
        int flen = 0;

        for (int i = 0; i < field.length - 1; i++) {
            if (field[i] == 0x1B) {
                hasEsc = true;

                if ("(,)-'".indexOf((char) field[i + 1]) != -1) {
                    inMultiByte = false;

                    if (i + 2 < field.length && (char) field[i + 2] == 'N') {
                        inCyrillic = true;
                    } else {
                        inCyrillic = false;
                    }
                } else if (i + 2 < field.length && field[i + 1] == '$' && field[i + 2] == '1') {
                    inMultiByte = true;
                    mbOffset = 3;
                } else if (i + 3 < field.length && (field[i + 1] == '$' || field[i + 2] == '$') && (field[i + 2] == '1' || field[i + 3] == '1')) {
                    inMultiByte = true;
                    mbOffset = 4;
                }

            } else if (inMultiByte && field[i] != 0x20 && field[i] >= 0) {
                mbOffset = mbOffset == 0 ? 2 : mbOffset - 1;
            }
            if (inMultiByte && mbOffset == 0 && i + 2 < field.length && field[i] > 0) {
                char c;
                final byte f1 = field[i];
                final byte f2 = field[i + 1] == 0x20 ? field[i + 2] : field[i + 1];
                final byte f3 = field[i + 1] == 0x20 || field[i + 2] == 0x20 ? field[i + 3]
                        : field[i + 2];
                c = conv.getMBChar(conv.makeMultibyte((char) (f1 == Constants.US ? 0x7C : f1),
                        (char) (f2 == Constants.US ? 0x7C : f2), (char) (f3 == Constants.US ? 0x7C
                                : f3)));
                if (c == 0 && !justCleaned) {
                    addError(MarcError.MAJOR_ERROR,
                            "Bad Multibyte character found, reinterpreting data as non-multibyte data");
                    inMultiByte = false;
                } else if (c == 0 && justCleaned) {
                    c = conv.getMBChar(conv.makeMultibyte('!', (char) (f2 == Constants.US ? 0x7C
                            : f2), (char) (f3 == Constants.US ? 0x7C : f3)));
                    if (c == 0) {
                        addError(MarcError.MAJOR_ERROR,
                                "Bad Multibyte character found, reinterpreting data as non-multibyte data");
                        inMultiByte = false;
                    } else {
                        addError(
                                MarcError.MAJOR_ERROR,
                                "Character after restored vertical bar character makes bad multibyte character, changing it to \"!\"");
                        field[i] = '!';
                    }
                }
            }
            justCleaned = false;
            if (field[i] == Constants.US) {
                if (inMultiByte && mbOffset != 0) {
                    field[i] = 0x7C;
                    addError(
                            MarcError.MAJOR_ERROR,
                            "Subfield separator found in middle of a multibyte character, changing it to a vertical bar, and continuing");
                    if (field[i + 1] == '0') {
                        if (field[i + 2] == '(' && field[i + 3] == 'B') {
                            field[i + 1] = 0x1B;
                            addError(MarcError.MAJOR_ERROR,
                                    "Character after restored vertical bar character makes bad multibyte character, changing it to ESC");
                        } else {
                            field[i + 1] = 0x21;
                            addError(
                                    MarcError.MAJOR_ERROR,
                                    "Character after restored vertical bar character makes bad multibyte character, changing it to \"!\"");
                        }
                    }
                    justCleaned = true;
                } else if (hasEsc && inCyrillic) {
                    final String prev = new String(field, i - (flen - 1), flen - 1);

                    if (!(field[i + 1] >= 'a' && field[i + 1] <= 'z') || prev.equals("\u001b(N")) {
                        addError(MarcError.MINOR_ERROR,
                                "Subfield separator found in Cyrillic string, changing separator to a vertical bar, and continuing");
                        field[i] = 0x7C;
                        justCleaned = true;
                    }
                } else if (hasEsc && !(field[i + 1] >= 'a' && field[i + 1] <= 'z' || field[i + 1] >= '0' && field[i + 1] <= '9')) {
                    addError(
                            MarcError.MAJOR_ERROR,
                            "Subfield separator followed by invalid subfield tag, changing separator to a vertical bar, and continuing");
                    field[i] = 0x7C;
                    justCleaned = true;
                } else if (hasEsc && i < field.length - 3 && field[i + 1] == '0' && field[i + 2] == '(' && field[i + 3] == 'B') {
                    addError(
                            MarcError.MAJOR_ERROR,
                            "Subfield separator followed by invalid subfield tag, changing separator to a vertical bar, and continuing");
                    field[i] = 0x7C;
                    field[i + 1] = 0x1B;
                    justCleaned = true;
                } else if (hasEsc && field[i + 1] == '0') {
                    addError(
                            MarcError.MAJOR_ERROR,
                            "Subfield separator followed by invalid subfield tag, changing separator to a vertical bar, and continuing");
                    field[i] = 0x7C;
                    field[i + 1] = 0x21;
                    justCleaned = true;
                } else if (field[i + 1] == Constants.US && field[i + 2] == Constants.US) {
                    addError(MarcError.MAJOR_ERROR,
                            "Three consecutive subfield separators, changing first two to vertical bars.");
                    field[i] = 0x7C;
                    field[i + 1] = 0x7C;
                    justCleaned = true;
                }
            }

            if (field[i] == Constants.US) {
                flen = 0;
            } else {
                flen++;
            }
        }
    }

    private int getFieldLength(final DataInputStream bais) throws IOException {
        bais.mark(9999);
        int bytesRead = 0;
        while (true) {
            switch (bais.read()) {
                case Constants.FT:
                    bais.reset();
                    return bytesRead;
                case -1:
                    bais.reset();
                    if (permissive) {
                        addError(MarcError.MINOR_ERROR, "Field not terminated trying to continue");
                        return bytesRead;
                    } else {
                        throw new IOException("Field not terminated");
                    }
                case Constants.US:
                default:
                    bytesRead++;
            }
        }
    }

    private int getSubfieldLength(final ByteArrayInputStream bais) throws IOException {
        bais.mark(9999);

        int bytesRead = 0;

        while (true) {
            switch (bais.read()) {
                case Constants.FT:
                    bais.reset();
                    return bytesRead;
                case Constants.US:
                    bais.reset();
                    return bytesRead;
                case -1:
                    bais.reset();
                    if (permissive) {
                        addError(MarcError.MINOR_ERROR,
                                "Subfield not terminated trying to continue");
                        return bytesRead;
                    } else {
                        throw new IOException("subfield not terminated");
                    }
                default:
                    bytesRead++;
            }
        }
    }

    private int parseRecordLength(final byte[] leaderData) throws IOException {
        final InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(leaderData));
        int length = -1;
        final char[] tmp = new char[5];
        isr.read(tmp);

        try {
            length = Integer.parseInt(new String(tmp));
        } catch (final NumberFormatException e) {
            addError(MarcError.FATAL, "Unable to parse record length, Unable to Continue");
            throw new MarcException("unable to parse record length", e);
        }

        return length;
    }

    private void parseLeader(final Leader ldr, final byte[] leaderData) throws IOException {
        final InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(leaderData),
                "ISO-8859-1");

        char[] tmp = new char[5];
        isr.read(tmp);
        // Skip over bytes for record length, If we get here, its already been
        // computed.
        ldr.setRecordStatus((char) isr.read());
        ldr.setTypeOfRecord((char) isr.read());
        tmp = new char[2];
        isr.read(tmp);
        ldr.setImplDefined1(tmp);
        ldr.setCharCodingScheme((char) isr.read());

        final char indicatorCount = (char) isr.read();
        final char subfieldCodeLength = (char) isr.read();
        final char baseAddr[] = new char[5];

        isr.read(baseAddr);
        tmp = new char[3];
        isr.read(tmp);
        ldr.setImplDefined2(tmp);
        tmp = new char[4];
        isr.read(tmp);
        ldr.setEntryMap(tmp);
        isr.close();

        try {
            ldr.setIndicatorCount(Integer.parseInt(String.valueOf(indicatorCount)));
        } catch (final NumberFormatException e) {
            if (permissive) {
                // All Marc21 records should have indicatorCount '2'
                addError(MarcError.ERROR_TYPO, "bogus indicator count - byte value =  " + Integer
                        .toHexString(indicatorCount & 0xff));
                ldr.setIndicatorCount(2);
            } else {
                throw new MarcException("unable to parse indicator count", e);
            }
        }
        try {
            ldr.setSubfieldCodeLength(Integer.parseInt(String.valueOf(subfieldCodeLength)));
        } catch (final NumberFormatException e) {
            if (permissive) {
                // All Marc21 records should have subfieldCodeLength '2'
                addError(MarcError.ERROR_TYPO, "bogus subfield count - byte value =  " + Integer
                        .toHexString(subfieldCodeLength & 0xff));
                ldr.setSubfieldCodeLength(2);
            } else {
                throw new MarcException("unable to parse subfield code length", e);
            }
        }

        try {
            ldr.setBaseAddressOfData(Integer.parseInt(new String(baseAddr)));
        } catch (final NumberFormatException e) {
            throw new MarcException("unable to parse base address of data", e);
        }

    }

    private String getDataAsString(final byte[] bytes) {
        String dataElement = null;

        if (encoding.equals("UTF-8") || encoding.equals("UTF8")) {
            try {
                dataElement = new String(bytes, "UTF-8");
            } catch (final UnsupportedEncodingException e) {
                throw new MarcException("unsupported encoding", e);
            }
        } else if (encoding.equals("UTF8-Maybe")) {
            try {
                dataElement = new String(bytes, "UTF-8");
            } catch (final UnsupportedEncodingException e) {
                throw new MarcException("unsupported encoding", e);
            }
        } else if (encoding.equals("MARC-8") || encoding.equals("MARC8")) {
            dataElement = getMarc8Conversion(bytes);
        } else if (encoding.equalsIgnoreCase("Unimarc") || encoding.equals("IS05426")) {
            dataElement = getUnimarcConversion(bytes);
        } else if (encoding.equals("MARC8-Maybe")) {
            final String dataElement1 = getMarc8Conversion(bytes);
            final String dataElement2 = getUnimarcConversion(bytes);

            String dataElement3 = null;

            try {
                dataElement3 = new String(bytes, "ISO-8859-1");
            } catch (final UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (dataElement1.equals(dataElement2) && dataElement1.equals(dataElement3)) {
                dataElement = dataElement1;
            } else {
                conversionCheck1 = conversionCheck1 + "|>" + Normalizer.normalize(dataElement1,
                        Normalizer.Form.NFC);
                conversionCheck2 = conversionCheck2 + "|>" + dataElement2;
                conversionCheck3 = conversionCheck3 + "|>" + dataElement3;
                dataElement = dataElement1 + "%%@%%" + dataElement2 + "%%@%%" + dataElement3;
            }
        } else if (encoding.equals("MARC8-Broken")) {
            try {
                dataElement = new String(bytes, "ISO-8859-1");
            } catch (final UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            String newdataElement = dataElement.replaceAll("&lt;", "<");

            newdataElement = newdataElement.replaceAll("&gt;", ">");
            newdataElement = newdataElement.replaceAll("&amp;", "&");
            newdataElement = newdataElement.replaceAll("&apos;", "'");
            newdataElement = newdataElement.replaceAll("&quot;", "\"");

            if (!newdataElement.equals(dataElement)) {
                dataElement = newdataElement;
                addError(MarcError.ERROR_TYPO,
                        "Subfield contains escaped html character entities, un-escaping them. ");
            }

            final String rep1 = "" + (char) 0x1b + "\\$1$1";
            final String rep2 = "" + (char) 0x1b + "\\(B";

            newdataElement = dataElement.replaceAll("\\$1(.)", rep1);
            newdataElement = newdataElement.replaceAll("\\(B", rep2);

            if (!newdataElement.equals(dataElement)) {
                dataElement = newdataElement;
                addError(MarcError.MAJOR_ERROR,
                        "Subfield seems to be missing MARC8 escape sequences, trying to restore them.");
            }

            try {
                dataElement = getMarc8Conversion(dataElement.getBytes("ISO-8859-1"));
            } catch (final UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (encoding.equals("ISO-8859-1") || encoding.equals("ISO8859_1")) {
            try {
                dataElement = new String(bytes, "ISO-8859-1");
            } catch (final UnsupportedEncodingException e) {
                throw new MarcException("unsupported encoding", e);
            }
        } else {
            try {
                dataElement = new String(bytes, encoding);
            } catch (final UnsupportedEncodingException e) {
                throw new MarcException(
                        "Unknown or unsupported Marc character encoding:" + encoding);
            }
        }

        if (record != null && dataElement.matches("[^&]*&[a-z]*;.*")) {
            String newdataElement = dataElement.replaceAll("&lt;", "<");

            newdataElement = newdataElement.replaceAll("&gt;", ">");
            newdataElement = newdataElement.replaceAll("&amp;", "&");
            newdataElement = newdataElement.replaceAll("&apos;", "'");
            newdataElement = newdataElement.replaceAll("&quot;", "\"");

            if (!newdataElement.equals(dataElement)) {
                dataElement = newdataElement;
                addError(MarcError.ERROR_TYPO,
                        "Subfield contains escaped html character entities, un-escaping them. ");
            }
        }
        return dataElement;
    }

    private static boolean byteArrayContains(final byte[] bytes, final byte[] seq) {
        for (int i = 0; i < bytes.length - seq.length; i++) {
            if (bytes[i] == seq[0]) {
                for (int j = 0; j < seq.length; j++) {
                    if (bytes[i + j] != seq[j]) {
                        break;
                    }

                    if (j == seq.length - 1) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    static byte badEsc[] = { (byte) 'b', (byte) '-', 0x1b, (byte) 's' };

    static byte overbar[] = { (byte) (char) 0xaf };

    /**
     * Assumes the data in bytes is in the MARC8 encoding, and translates it to UTF-8 based on that assumption
     *
     * @param bytes - Bytes to be converted to MARC-8
     * @param conv - An Ansel to Unicode converter
     * @param permissive - Whether this is done in a permissive manner
     * @param record - seems to not be used
     * @param doNCR - Do numeric character reference
     * @return A UTF-8 encoded string produced from the supplied MARC8 encoded data
     */
    public String getMarc8Conversion(byte[] bytes, AnselToUnicode conv, boolean permissive,
            Record record, boolean doNCR) {
        String dataElement = null;

        if (permissive && (byteArrayContains(bytes, badEsc) || byteArrayContains(bytes, overbar))) {
            String newDataElement = null;

            try {
                dataElement = new String(bytes, "ISO-8859-1");
                newDataElement = dataElement.replaceAll("(\\e)b-\\es([psb$()])", "$1$2");

                if (!newDataElement.equals(dataElement)) {
                    dataElement = newDataElement;
                    addError(MarcError.MINOR_ERROR,
                            "Subfield contains odd pattern of subscript or superscript escapes. ");
                }

                newDataElement = dataElement.replace((char) 0xaf, (char) 0xe5);

                if (!newDataElement.equals(dataElement)) {
                    dataElement = newDataElement;
                    addError(MarcError.ERROR_TYPO,
                            "Subfield contains 0xaf overbar character, changing it to proper MARC8 representation ");
                }

                dataElement = conv.convert(dataElement);
            } catch (final UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            dataElement = conv.convert(bytes);
        }

        if (doNCR) {
            // This code handles malformed Numeric Character references that
            // either contain
            // an extraneous %x or which are missing the final semicolon
            if (permissive && dataElement
                    .matches("[^&]*&#x[0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][^;].*")) {
                final Pattern pattern = Pattern
                        .compile("&#x([0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f])(%x)?;?");
                final Matcher matcher = pattern.matcher(dataElement);
                final StringBuffer newElement = new StringBuffer();

                int prevEnd = 0;

                while (matcher.find()) {
                    newElement.append(dataElement.substring(prevEnd, matcher.start()));
                    newElement.append(getChar(matcher.group(1)));

                    if (matcher.group(1).contains("%x") || !matcher.group(1).endsWith(";")) {
                        addError(
                                MarcError.MINOR_ERROR,
                                "Subfield contains malformed Unicode Numeric Character Reference : " + matcher
                                        .group(0));
                    }
                    prevEnd = matcher.end();
                }

                newElement.append(dataElement.substring(prevEnd));
                dataElement = newElement.toString();
            }
        }

        return dataElement;
    }

    private String getMarc8Conversion(final byte[] bytes) {
        String dataElement = null;
        if (converterAnsel == null) {
            converterAnsel = new AnselToUnicode(this);
        }
        if (isTranslateLosslessUnicodeNumericCodeReferencesEnabled()) {
            final AnselToUnicode anselConverter = converterAnsel;
            anselConverter
                    .setTranslateNCR(isTranslateLosslessUnicodeNumericCodeReferencesEnabled());
        }
        dataElement = getMarc8Conversion(bytes, converterAnsel, permissive, record,
                translateLosslessUnicodeNumericCodeReferencesEnabled);
        return dataElement;
    }

    private String getUnimarcConversion(final byte[] bytes) {
        if (converterUnimarc == null) {
            converterUnimarc = new Iso5426ToUnicode();
        }

        String dataElement = converterUnimarc.convert(bytes);

        dataElement = dataElement.replaceAll("\u0088", "");
        dataElement = dataElement.replaceAll("\u0089", "");

        if (dataElement.matches("[^<]*<U[+][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f]>.*")) {
            final Pattern pattern = Pattern
                    .compile("<U[+]([0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f])>");
            final Matcher matcher = pattern.matcher(dataElement);
            final StringBuffer newElement = new StringBuffer();

            int prevEnd = 0;

            while (matcher.find()) {
                newElement.append(dataElement.substring(prevEnd, matcher.start()));
                newElement.append(getChar(matcher.group(1)));
                prevEnd = matcher.end();
            }

            newElement.append(dataElement.substring(prevEnd));
            dataElement = newElement.toString();
        }

        return dataElement;
    }

    private static String getChar(final String charCodePoint) {
        final int charNum = Integer.parseInt(charCodePoint, 16);
        final String result = "" + (char) charNum;
        return result;
    }
}
