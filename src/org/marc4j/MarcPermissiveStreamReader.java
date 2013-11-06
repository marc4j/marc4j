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

import org.marc4j.converter.CharConverter;
import org.marc4j.converter.impl.AnselToUnicode;
import org.marc4j.converter.impl.Iso5426ToUnicode;
import org.marc4j.marc.*;
import org.marc4j.marc.impl.Verifier;
import org.marc4j.util.Normalizer;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An iterator over a collection of MARC records in ISO 2709 format, that is designed
 * to be able to handle MARC records that have errors in their structure or their encoding.
 * If the permissive flag is set in the call to the constructor, or if a ErrorHandler object
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
 * ErrorHandler class to report errors encountered while processing records.
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
 * 
 */
public class MarcPermissiveStreamReader implements MarcReader {

    private DataInputStream input = null;

    private Record record;

    private MarcFactory factory;

    private String encoding = "ISO8859_1";

    // This represents the expected encoding of the data when a 
    // MARC record does not have a 'a' in character 9 of the leader.
    private String defaultEncoding = "ISO8859_1";

    private boolean convertToUTF8 = false;
   
    private boolean permissive = false;

    private boolean translateLosslessUnicodeNumericCodeReferencesEnabled=true;
    private int marc_file_lookahead_buffer = 200000;
    
    private CharConverter converterAnsel = null;

    private CharConverter converterUnimarc = null;
    
    // These are used to algorithmically determine what encoding scheme was 
    // used to encode the data in the Marc record
    private String conversionCheck1 = null;    
    private String conversionCheck2 = null;
    private String conversionCheck3 = null;

    private ErrorHandler errors;
    static String validSubfieldCodes = "abcdefghijklmnopqrstuvwxyz0123456789";
    static String upperCaseSubfieldsProperty = "org.marc4j.MarcPermissiveStreamReader.upperCaseSubfields";
    /**
     * Constructs an instance with the specified input stream with possible additional functionality
     * being enabled by setting permissive and/or convertToUTF8 to true.
     * 
     * If permissive and convertToUTF8 are both set to false, it functions almost identically to the
     * MarcStreamReader class.
     */
    public MarcPermissiveStreamReader(InputStream input, boolean permissive, boolean convertToUTF8) {
        this.permissive = permissive;
        this.input = new DataInputStream(new BufferedInputStream(input));
        factory = MarcFactory.newInstance();
        this.convertToUTF8 = convertToUTF8;
        errors = null;
        if (permissive) 
        {
            errors = new ErrorHandler();
            defaultEncoding = "BESTGUESS";
        }
    }
    
    /**
     * Constructs an instance with the specified input stream with possible additional functionality
     * being enabled by passing in an ErrorHandler object and/or setting convertToUTF8 to true.
     * 
     * If errors and convertToUTF8 are both set to false, it functions almost identically to the
     * MarcStreamReader class.
     * 
     * If an ErrorHandler object is passed in, that object will be used to log and track any errors 
     * in the records as the records are decoded.  After the next() function returns, you can query 
     * to determine whether any errors were detected in the decoding process.
     * 
     * See the  file org.marc4j.samples.PermissiveReaderExample.java to see how this can be done.
     */     
    public MarcPermissiveStreamReader(InputStream input, ErrorHandler errors, boolean convertToUTF8 ) 
    {
        if (errors != null) 
        {
            permissive = true;
            defaultEncoding = "BESTGUESS";
        }
        this.input = new DataInputStream((input.markSupported()) ? input : new BufferedInputStream(input));
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
     */
    public MarcPermissiveStreamReader(InputStream input, boolean permissive, boolean convertToUTF8, String defaultEncoding) 
    {
        this.permissive = permissive;
        this.input = new DataInputStream((input.markSupported()) ? input : new BufferedInputStream(input));
        factory = MarcFactory.newInstance();
        this.convertToUTF8 = convertToUTF8;
        this.defaultEncoding = defaultEncoding;
        errors = null;
        if (permissive) errors = new ErrorHandler();
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
     * If an ErrorHandler object is passed in, that object will be used to log and track any errors 
     * in the records as the records are decoded.  After the next() function returns, you can query 
     * to determine whether any errors were detected in the decoding process.
     * 
     * See the  file org.marc4j.samples.PermissiveReaderExample.java to see how this can be done.
     */          
    public MarcPermissiveStreamReader(InputStream input, ErrorHandler errors, boolean convertToUTF8, String defaultEncoding) 
    {
        this.permissive = true;
        this.input = new DataInputStream(new BufferedInputStream(input));
        factory = MarcFactory.newInstance();
        this.convertToUTF8 = convertToUTF8;
        this.defaultEncoding = defaultEncoding;
        this.errors = errors;
    }


    /**
     * @return  true if numeric character entities like &#xFFFD; should be converted to their corresponding code point
     * if converting to unicode. Default is to convert.
     */
    public boolean isTranslateLosslessUnicodeNumericCodeReferencesEnabled() {
        return translateLosslessUnicodeNumericCodeReferencesEnabled;
    }

    /**
     * Enable convesion of numeric code references into their corresponding code points when converting to unicode
     * @param translateLosslessUnicodeNumericCodeReferencesEnabled
     */
    public void setTranslateLosslessUnicodeNumericCodeReferencesEnabled(boolean translateLosslessUnicodeNumericCodeReferencesEnabled) {
        this.translateLosslessUnicodeNumericCodeReferencesEnabled = translateLosslessUnicodeNumericCodeReferencesEnabled;
    }


    /**
     * Returns true if the iteration has more records, false otherwise.
     */
    public boolean hasNext() 
    {
        try {
            input.mark(10);
            int byteread = input.read(); 
            if (byteread == -1)
                return false;
            //    byte[] recLengthBuf = new byte[5];
            int numBadBytes = 0;
            while (byteread < '0' || byteread > '9')
            {
                byteread = input.read();
                numBadBytes++;
                if (byteread == -1) return false;
            }
            input.reset();
            while (numBadBytes > 0)
            {
                byteread = input.read();
                numBadBytes--;
            }
        } catch (IOException e) {
            throw new MarcException(e.getMessage(), e);
        }
        return true;
    }

    /**
     * Returns the next record in the iteration.
     * 
     * @return Record - the record object
     */
    public Record next() 
    {
        record = factory.newRecord();
        if (errors != null) errors.reset();
        
        try {
            byte[] byteArray = new byte[24];
            
            input.readFully(byteArray);
            int recordLength = parseRecordLength(byteArray);
            byte[] recordBuf = new byte[recordLength - 24];
            if (permissive) 
            {
                input.mark(marc_file_lookahead_buffer);
                input.readFully(recordBuf);
                if (recordBuf[recordBuf.length-1] != Constants.RT)
                {
                    errors.addError("unknown", "n/a", "n/a", ErrorHandler.MAJOR_ERROR, 
                                    "Record terminator character not found at end of record length");
                    recordBuf = rereadPermissively(input, recordBuf, recordLength);
                    recordLength = recordBuf.length + 24;
                }
            }
            else
            {
                input.readFully(recordBuf);
            }
            String tmp = new String(recordBuf);
            parseRecord(record, byteArray, recordBuf, recordLength);

            if (this.convertToUTF8)
            {
                Leader l = record.getLeader();
                l.setCharCodingScheme('a');
                record.setLeader(l);
            }
            return(record);
        }
        catch (EOFException e) {
            throw new MarcException("Premature end of file encountered", e);
        } 
        catch (IOException e) {
            throw new MarcException("an error occured reading input", e);
        }   
    }
    
    private byte[] rereadPermissively(DataInputStream input, byte[] recordBuf, int recordLength) throws IOException
    {
        int loc = arrayContainsAt(recordBuf, Constants.RT);
        if (loc != -1)  // stated record length is too long
        {
            errors.addError("unknown", "n/a", "n/a", ErrorHandler.MAJOR_ERROR, 
                            "Record terminator appears before stated record length, using shorter record");
            recordLength = loc + 24;
            input.reset();
            recordBuf = new byte[recordLength - 24];
            input.readFully(recordBuf);
        }
        else  // stated record length is too short read ahead
        {
            loc = recordLength - 24;
            boolean done = false;
            while (!done)
            {
                int c = 0;
                do 
                {
                    c = input.read();
                    loc++;
                } while (loc < (marc_file_lookahead_buffer-24) && c != Constants.RT && c != -1);
     
                if (c == Constants.RT)
                {
                    errors.addError("unknown", "n/a", "n/a", ErrorHandler.MAJOR_ERROR, 
                                    "Record terminator appears after stated record length, reading extra bytes");
                    recordLength = loc + 24;
                    input.reset();
                    recordBuf = new byte[recordLength - 24];
                    input.readFully(recordBuf);
                    done = true;
                }
                else if (c == -1)
                {
                    errors.addError("unknown", "n/a", "n/a", ErrorHandler.MAJOR_ERROR, 
                                    "No Record terminator found, end of file reached, Terminator appended");
                    recordLength = loc + 24;
                    input.reset();
                    recordBuf = new byte[recordLength - 24 + 1];
                    input.readFully(recordBuf);
                    recordBuf[recordBuf.length-1] = Constants.RT; 
                    done = true;
                }
                else
                {
                    errors.addError("unknown", "n/a", "n/a", ErrorHandler.FATAL, 
                                    "No Record terminator found within "+marc_file_lookahead_buffer+" bytes of start of record, getting desperate.");
                    input.reset();
                    marc_file_lookahead_buffer *= 2;
                    input.mark(marc_file_lookahead_buffer);
                    loc = 0;
                }
            }
        }
        return(recordBuf);
    }
        
    private void parseRecord(Record record, byte[] byteArray, byte[] recordBuf, int recordLength)
    {
        Leader ldr;
        ldr = factory.newLeader();
        ldr.setRecordLength(recordLength);
        int directoryLength=0;
        // These variables are used when the permissive reader is trying to make its best guess 
        // as to what character encoding is actually used in the record being processed.
        conversionCheck1 = "";
        conversionCheck2 = "";
        conversionCheck3 = "";
        
        try {                
            parseLeader(ldr, byteArray);
            directoryLength = ldr.getBaseAddressOfData() - (24 + 1);
        } 
        catch (IOException e) {
            throw new MarcException("error parsing leader with data: "
                    + new String(byteArray), e);
        } 
        catch (MarcException e) {
            if (permissive)
            {
                if (recordBuf[recordBuf.length-1] == Constants.RT && recordBuf[recordBuf.length-2] == Constants.FT)
                {
                    errors.addError("unknown", "n/a", "n/a", ErrorHandler.MAJOR_ERROR, 
                                    "Error parsing leader, trying to re-read leader either shorter or longer");
                    // make an attempt to recover record.
                    int offset = 0;
                    while (offset < recordBuf.length)
                    {
                        if (recordBuf[offset] == Constants.FT)
                        {
                            break;
                        }
                        offset++;
                    }
                    if (offset % 12 == 1)
                    {
                        // move one byte from body to leader, make new leader, and try again
                        errors.addError("unknown", "n/a", "n/a", ErrorHandler.MAJOR_ERROR, 
                                        "Leader appears to be too short, moving one byte from record body to leader, and trying again");
                        byte oldBody[] = recordBuf;
                        recordBuf = new byte[oldBody.length-1];
                        System.arraycopy(oldBody, 1, recordBuf, 0, oldBody.length-1);
                        directoryLength = offset-1;
                        ldr.setIndicatorCount(2);
                        ldr.setSubfieldCodeLength(2);
                        ldr.setImplDefined1((""+(char)byteArray[7]+" ").toCharArray());
                        ldr.setImplDefined2((""+(char)byteArray[18]+(char)byteArray[19]+(char)byteArray[20]).toCharArray());
                        ldr.setEntryMap("4500".toCharArray());
                        if (byteArray[10] == (byte)' ' || byteArray[10] == (byte)'a') // if its ' ' or 'a'
                        {
                            ldr.setCharCodingScheme((char)byteArray[10]);
                        }
                    }
                    else if (offset % 12 == 11) 
                    {
                        errors.addError("unknown", "n/a", "n/a", ErrorHandler.MAJOR_ERROR, 
                                        "Leader appears to be too long, moving one byte from leader to record body, and trying again");
                        byte oldBody[] = recordBuf;
                        recordBuf = new byte[oldBody.length+1];
                        System.arraycopy(oldBody, 0, recordBuf, 1, oldBody.length);
                        recordBuf[0] = (byte)'0';
                        directoryLength = offset+1;
                        ldr.setIndicatorCount(2);
                        ldr.setSubfieldCodeLength(2);
                        ldr.setImplDefined1((""+(char)byteArray[7]+" ").toCharArray());
                        ldr.setImplDefined2((""+(char)byteArray[16]+(char)byteArray[17]+(char)byteArray[18]).toCharArray());
                        ldr.setEntryMap("4500".toCharArray());
                        if (byteArray[8] == (byte)' ' || byteArray[8] == (byte)'a') // if its ' ' or 'a'
                        {
                            ldr.setCharCodingScheme((char)byteArray[10]);
                        }
                        if (byteArray[10] == (byte)' ' || byteArray[10] == (byte)'a') // if its ' ' or 'a'
                        {
                            ldr.setCharCodingScheme((char)byteArray[10]);
                        }
                    }
                    else
                    {
                        errors.addError("unknown", "n/a", "n/a", ErrorHandler.FATAL, 
                                       "error parsing leader with data: " + new String(byteArray));
                        throw new MarcException("error parsing leader with data: "
                                + new String(byteArray), e);
                    }
                }
            }
            else
            {
                throw new MarcException("error parsing leader with data: "
                        + new String(byteArray), e);
            }
        }
        char tmp[] = ldr.getEntryMap();
        if (permissive && !(""+ tmp[0]+tmp[1]+tmp[2]+tmp[3]).equals("4500"))
        {
            if (tmp[0] >= '0' && tmp[0] <= '9' && 
                    tmp[1] >= '0' && tmp[1] <= '9' && 
                    tmp[2] >= '0' && tmp[2] <= '9' && 
                    tmp[3] >= '0' && tmp[3] <= '9')
            {
                errors.addError("unknown", "n/a", "n/a", ErrorHandler.ERROR_TYPO, 
                            "Unusual character found at end of leader [ "+tmp[0]+tmp[1]+tmp[2]+tmp[3]+" ]");
            }
            else
            {
                errors.addError("unknown", "n/a", "n/a", ErrorHandler.ERROR_TYPO, 
                                "Erroneous character found at end of leader [ "+tmp[0]+tmp[1]+tmp[2]+tmp[3]+" ]; changing them to the standard \"4500\"");
                ldr.setEntryMap("4500".toCharArray());
            }
        }

        // if MARC 21 then check encoding
        switch (ldr.getCharCodingScheme()) {
        case 'a':
            encoding = "UTF8";
            break;
        case ' ':
            if (convertToUTF8)
                encoding = defaultEncoding;
            else 
                encoding = "ISO8859_1";
            break;
        default: 
            if (convertToUTF8)
                if (permissive)
                {
                    errors.addError("unknown", "n/a", "n/a", ErrorHandler.MINOR_ERROR, 
                                    "Record character encoding should be 'a' or ' ' in this record it is '"+ldr.getCharCodingScheme()+"'. Attempting to guess the correct encoding.");
                    encoding = "BESTGUESS";
                }
                else
                    encoding = defaultEncoding;
            else 
                encoding = "ISO8859_1";
            break;

        }
        String utfCheck;
        if (encoding.equalsIgnoreCase("BESTGUESS"))
        {
            try
            {
                String marc8EscSeqCheck = new String(recordBuf, "ISO-8859-1");
                //  If record has MARC8 character set selection strings, it must be MARC8 encoded
                if (marc8EscSeqCheck.split("\\e[-(,)$bsp]", 2).length > 1)
                {
                    encoding = "MARC8";
                }
                else
                {
                    boolean hasHighBitChars = false;
                    for (int i = 0; i < recordBuf.length; i++)
                    {
                        if (recordBuf[i] < 0) // the high bit is set
                        {
                            hasHighBitChars = true; 
                            break;
                        }
                    }
                    if (!hasHighBitChars)
                    {
                        encoding = "ISO8859_1";  //  You can choose any encoding you want here, the results will be the same.
                    }
                    else
                    {
                        utfCheck = new String(recordBuf, "UTF-8");
                        byte byteCheck[] = utfCheck.getBytes("UTF-8");
                        encoding = "UTF8";  
                        if (recordBuf.length == byteCheck.length)
                        {
                            for (int i = 0; i < recordBuf.length; i++)
                            {
                                if (byteCheck[i] != recordBuf[i])
                                {
                                    encoding = "MARC8-Maybe";
                                    break;
                                }
                            }
                        }
                        else 
                        {
                            encoding = "MARC8-Maybe";
                        }
                    }
                }
            }
            catch (UnsupportedEncodingException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else if (permissive && encoding.equals("UTF8"))
        {
            try
            {
                utfCheck = new String(recordBuf, "UTF-8");
                byte byteCheck[] = utfCheck.getBytes("UTF-8");
                if (recordBuf.length != byteCheck.length)
                {
                    boolean foundESC = false;
                    for (int i = 0; i < recordBuf.length; i++)
                    {
                        if (recordBuf[i] == 0x1B)
                        {
                            errors.addError("unknown", "n/a", "n/a", ErrorHandler.MINOR_ERROR, 
                                            "Record claims to be UTF-8, but its not. Its probably MARC8.");
                            encoding = "MARC8-Maybe";
                            foundESC = true;
                            break;
                        }
                        if (byteCheck[i] != recordBuf[i])
                        {
                            encoding = "MARC8-Maybe";
                        }
                        
                    }
                    if (!foundESC)
                    {
                        errors.addError("unknown", "n/a", "n/a", ErrorHandler.MINOR_ERROR, 
                                "Record claims to be UTF-8, but its not. It may be MARC8, or maybe UNIMARC, or maybe raw ISO-8859-1 ");
                    }
                }
                if (utfCheck.contains("a$1!"))
                {
                    encoding = "MARC8-Broken";
                    errors.addError("unknown", "n/a", "n/a", ErrorHandler.MAJOR_ERROR, 
                                "Record claims to be UTF-8, but its not. It seems to be MARC8-encoded but with missing escape codes.");
                }
            }
            catch (UnsupportedEncodingException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else if (permissive && !encoding.equals("UTF8") && convertToUTF8)
        {
            try
            {
                utfCheck = new String(recordBuf, "UTF-8");
                byte byteCheck[] = utfCheck.getBytes("UTF-8");
                if (recordBuf.length == byteCheck.length)
                {
	                for (int i = 0; i < recordBuf.length; i++)
	                {
	                    // need to check for byte < 0 to see if the high bit is set, because Java doesn't have unsigned types.
	                    if (recordBuf[i] < 0x00 || byteCheck[i] != recordBuf[i])
	                    {
	                        errors.addError("unknown", "n/a", "n/a", ErrorHandler.MINOR_ERROR, 
                                        "Record claims not to be UTF-8, but it seems to be.");
                            encoding = "UTF8-Maybe";
                            break;
	                    }
	                }
                }
             }
            catch (UnsupportedEncodingException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        record.setLeader(ldr);
        
        boolean discardOneAtStartOfDirectory = false;
        boolean discardOneSomewhereInDirectory = false;
        
        if ((directoryLength % 12) != 0)
        {
            if (permissive && directoryLength == 99974 && recordLength > 200000) //  which equals 99999 - (24 + 1) its a BIG record (its directory is over 100000 bytes)
            {
                directoryLength = 0; 
                int tmpLength = 0;
                for (tmpLength = 0; tmpLength < recordLength; tmpLength += 12)
                {
                    if (recordBuf[tmpLength] == Constants.FT) 
                    {
                        directoryLength = tmpLength;
                        break;
                    }
                }
                if (directoryLength == 0)
                {
                    throw new MarcException("Directory is too big (> 99999 bytes) and it doesn't end with a field terminator character, I give up. Unable to continue.");
                }
            }
            else if (permissive && directoryLength % 12 == 11 && recordBuf[1] != (byte)'0') 
            {
                errors.addError("unknown", "n/a", "n/a", ErrorHandler.MAJOR_ERROR, 
                                "Directory length is not a multiple of 12 bytes long.  Prepending a zero and trying to continue.");
                byte oldBody[] = recordBuf;
                recordBuf = new byte[oldBody.length+1];
                System.arraycopy(oldBody, 0, recordBuf, 1, oldBody.length);
                recordBuf[0] = (byte)'0';
                directoryLength = directoryLength+1;
            }
            else
            {
                if (permissive && directoryLength % 12 == 1 && recordBuf[1] == (byte)'0' && recordBuf[2] == (byte)'0') 
                {
                    discardOneAtStartOfDirectory = true;
                    errors.addError("unknown", "n/a", "n/a", ErrorHandler.MAJOR_ERROR, 
                                    "Directory length is not a multiple of 12 bytes long. Discarding byte from start of directory and trying to continue.");
                }
                else if (permissive && directoryLength % 12 == 1 && recordLength > 10000 && recordBuf[0] == (byte)'0' && 
                         recordBuf[1] == (byte)'0' && recordBuf[2] > (byte)'0' && recordBuf[2] <= (byte)'9')
                {
                    discardOneSomewhereInDirectory = true;
                    errors.addError("unknown", "n/a", "n/a", ErrorHandler.MAJOR_ERROR, 
                                    "Directory length is not a multiple of 12 bytes long.  Will look for oversized field and try to work around it.");
                }                
                else 
                {
                    if (errors != null)                
                    {    
                        errors.addError("unknown", "n/a", "n/a", ErrorHandler.FATAL, 
                                "Directory length is not a multiple of 12 bytes long. Unable to continue.");
                    }
                    throw new MarcException("Directory length is not a multiple of 12 bytes long. Unable to continue.");
                }
            }
        }
        DataInputStream inputrec = new DataInputStream(new ByteArrayInputStream(recordBuf));
        int size = directoryLength / 12;

        String[] tags = new String[size];
        int[] lengths = new int[size];

        byte[] tag = new byte[3];
        byte[] length = new byte[4];
        byte[] start = new byte[5];

        String tmpStr;
        try {
            if (discardOneAtStartOfDirectory)  inputrec.read();
            int totalOffset = 0;
            for (int i = 0; i < size; i++) 
            {
                inputrec.readFully(tag);                
                tmpStr = new String(tag);
                tags[i] = tmpStr;
    
                boolean proceedNormally = true;
                if (discardOneSomewhereInDirectory)
                {
                    byte lenCheck[] = new byte[10];
                    inputrec.mark(20);
                    inputrec.readFully(lenCheck);                
                    if (byteCompare(lenCheck, 4, 5, totalOffset)) // proceed normally
                    {
                        proceedNormally = true;
                    }
                    else if (byteCompare(lenCheck, 5, 5, totalOffset)) // field length is 5 bytes!  Bad Marc record, proceed normally
                    {
                        discardOneSomewhereInDirectory = false;
                        errors.addError("unknown", "n/a", "n/a", ErrorHandler.FATAL, 
                                        "Field is longer than 9999 bytes.  Writing this record out will result in a bad record.");
                        proceedNormally = false;
                    }
                    else
                    {
                        errors.addError("unknown", "n/a", "n/a", ErrorHandler.FATAL, 
                                        "Unable to reconcile problems in directory. Unable to continue.");                    
                        throw new MarcException("Directory length is not a multiple of 12 bytes long. Unable to continue.");
                    }
                    inputrec.reset();
                }
                if (proceedNormally)
                {
                    inputrec.readFully(length);
                    tmpStr = new String(length);
                    lengths[i] = Integer.parseInt(tmpStr);
    
                    inputrec.readFully(start);
                }
                else // length is 5 bytes long 
                {
                    inputrec.readFully(start);
                    tmpStr = new String(start);
                    lengths[i] = Integer.parseInt(tmpStr);
    
                    inputrec.readFully(start);                    
                }
                totalOffset += lengths[i];
            }
            
            // If we still haven't found the extra byte, throw out the last byte and try to continue;
            if (discardOneSomewhereInDirectory)  inputrec.read();
    
            if (inputrec.read() != Constants.FT)
            {
                errors.addError("unknown", "n/a", "n/a", ErrorHandler.FATAL, 
                                "Expected field terminator at end of directory. Unable to continue.");
                throw new MarcException("expected field terminator at end of directory");
            }
            
            int numBadLengths = 0;
            
            int totalLength = 0;
            for (int i = 0; i < size; i++) 
            {
                int fieldLength = getFieldLength(inputrec);
                if (fieldLength+1 != lengths[i] && permissive)
                {
                    if (numBadLengths < 5 && (totalLength + fieldLength < recordLength + 26))
                    {
                        inputrec.mark(9999);
                        byteArray = new byte[lengths[i]];
                        inputrec.readFully(byteArray);
                        inputrec.reset();
                        if (fieldLength+1 < lengths[i] && byteArray[lengths[i]-1] == Constants.FT)
                        {
                            errors.addError("unknown", "n/a", "n/a", ErrorHandler.MINOR_ERROR, 
                                            "Field Terminator character found in the middle of a field.");
                        }
                        else 
                        {
                            numBadLengths++;
                            lengths[i] = fieldLength+1;
                            errors.addError("unknown", "n/a", "n/a", ErrorHandler.MINOR_ERROR, 
                                            "Field length found in record different from length stated in the directory.");
                            if (fieldLength+1 > 9999)
                            {
                                errors.addError("unknown", "n/a", "n/a", ErrorHandler.FATAL, 
                                            "Field length is greater than 9999, record cannot be represented as a binary Marc record.");
                            }
                        }

                    }
                }
                totalLength += lengths[i];
                if (isControlField(tags[i])) 
                {
                    byteArray = new byte[lengths[i] - 1];
                    inputrec.readFully(byteArray);
    
                    if (inputrec.read() != Constants.FT)
                    {
                        errors.addError("unknown", "n/a", "n/a", ErrorHandler.FATAL, 
                                        "Expected field terminator at end of field. Unable to continue.");
                        throw new MarcException("expected field terminator at end of field");
                    }
    
                    ControlField field = factory.newControlField();
                    field.setTag(tags[i]);
                    field.setData(getDataAsString(byteArray));
                    record.addVariableField(field);
    
                } 
                else 
                {
                    byteArray = new byte[lengths[i]];
                    inputrec.readFully(byteArray);
                    try {
                        record.addVariableField(parseDataField(tags[i], byteArray));
                    } catch (IOException e) {
                        throw new MarcException(
                                "error parsing data field for tag: " + tags[i]
                                        + " with data: "
                                        + new String(byteArray), e);
                    }
                }
            }
            
            // We've determined that although the record says it is UTF-8, it is not. 
            // Here we make an attempt to determine the actual encoding of the data in the record.
            if (permissive && conversionCheck1.length() > 1 && 
                    conversionCheck2.length() > 1 && conversionCheck3.length() > 1)
            {
                guessAndSelectCorrectNonUTF8Encoding();
            }
            if (inputrec.read() != Constants.RT)
            {
                errors.addError("unknown", "n/a", "n/a", ErrorHandler.FATAL, 
                                "Expected record terminator at end of record. Unable to continue.");
                throw new MarcException("expected record terminator");
            } 
        }
        catch (IOException e)
        {
            errors.addError("unknown", "n/a", "n/a", ErrorHandler.FATAL, 
                            "Error reading from data file. Unable to continue.");
            throw new MarcException("an error occured reading input", e);            
        }
    }

    private boolean byteCompare(byte[] lenCheck, int offset, int length, int totalOffset)
    {
        int divisor = 1;
        for (int i = offset + length - 1; i >= offset; i-- , divisor *= 10)
        {
            if (((totalOffset / divisor) % 10) + '0' != lenCheck[i])
            {
                return(false);
            }
        }
        return true;
    }

    private boolean isControlField(String tag)
    {
        boolean isControl = false;
        try {
            isControl = Verifier.isControlField(tag);
        }
        catch (NumberFormatException nfe)
        {
            if (permissive) 
            {
                errors.addError(record.getControlNumber(), tag, "n/a", ErrorHandler.ERROR_TYPO, 
                                "Field tag contains non-numeric characters (" + tag + ").");
                isControl = false;
            }
        }
        return isControl;
    }

    private void guessAndSelectCorrectNonUTF8Encoding()
    {
        int defaultPart = 0;
        if (record.getVariableField("245") == null)  defaultPart = 1;
        int partToUse = 0;
        int l1 = conversionCheck1.length();
        int l2 = conversionCheck2.length();
        int l3 = conversionCheck3.length();
        int tst;

        if (l1 < l3 && l2 == l3 && defaultPart == 0)
        {
            errors.addError(ErrorHandler.INFO, "MARC8 translation shorter than ISO-8859-1, choosing MARC8.");
            partToUse = 0;
        }
        else if (l2 < l1-2 && l2 < l3-2 )             
        {
            errors.addError(ErrorHandler.INFO, "Unimarc translation shortest, choosing it.");
            partToUse = 1;
        }
        else if ((tst = onlyOneStartsWithUpperCase(conversionCheck1, conversionCheck2, conversionCheck3)) != -1)
        {
            partToUse = tst;
        }
        else if (l2 < l1 && l2 < l3 )             
        {
            errors.addError(ErrorHandler.INFO, "Unimarc translation shortest, choosing it.");
            partToUse = 1;
        }
        else if (conversionCheck2.equals(conversionCheck3) && !conversionCheck1.trim().contains(" "))
        {
            errors.addError(ErrorHandler.INFO, "Unimarc and ISO-8859-1 translations identical, choosing ISO-8859-1.");
            partToUse = 2;
        }
        else if (!specialCharIsBetweenLetters(conversionCheck1))
        {
            errors.addError(ErrorHandler.INFO, "To few letters in translations, choosing "+(defaultPart == 0 ? "MARC8" : "Unimarc"));
            partToUse = defaultPart;
        }
//        else if (l2 == l1 && l2 == l3)
//        {
//            errors.addError(ErrorHandler.INFO, "All three version equal length. Choosing ISO-8859-1 ");
//            partToUse = 2;
//        }
        else if (l2 == l3 && defaultPart == 1)
        {
            errors.addError(ErrorHandler.INFO, "Unimarc and ISO-8859-1 translations equal length, choosing ISO-8859-1.");
            partToUse = 2;
        }
        else
        {
            errors.addError(ErrorHandler.INFO, "No Determination made, defaulting to "+ (defaultPart == 0 ? "MARC8" : "Unimarc") );
            partToUse = defaultPart;
        }
        List<VariableField> fields = record.getVariableFields();
        Iterator<VariableField> iter = fields.iterator();
        while (iter.hasNext())
        {
            VariableField field = iter.next();
            if (field instanceof DataField)
            {
                DataField df = (DataField)field;
                List<Subfield> subf = df.getSubfields();
                Iterator<Subfield> sfiter = subf.iterator();
                while (sfiter.hasNext())
                {
                    Subfield sf = sfiter.next();
                    if (sf.getData().contains("%%@%%"))
                    {
                        String parts[] = sf.getData().split("%%@%%", 3);
                        sf.setData(parts[partToUse]);
                    }
                }
            }
        }                      
    }
        
    private int onlyOneStartsWithUpperCase(String conversionCheck12, String conversionCheck22, String conversionCheck32)
    {
        if (conversionCheck1.length() == 0 || conversionCheck2.length() == 0 || conversionCheck3.length() == 0) return -1;
        String check1Parts[] = conversionCheck1.trim().split("[|]>");
        String check2Parts[] = conversionCheck2.trim().split("[|]>");
        String check3Parts[] = conversionCheck3.trim().split("[|]>");
        for (int i = 1; i < check1Parts.length && i < check2Parts.length  && i < check3Parts.length; i++)
        {
            boolean tst1 = Character.isUpperCase(check1Parts[i].charAt(0));
            boolean tst2 = Character.isUpperCase(check2Parts[i].charAt(0));
            boolean tst3 = Character.isUpperCase(check3Parts[i].charAt(0));
            if (tst1 && !tst2 && !tst3)  
                return(0);
            if (!tst1 && tst2 && !tst3)  
                return(-1);
            if (!tst1 && !tst2 && tst3)  
                return(2);
        }
        return -1;
    }

    private boolean specialCharIsBetweenLetters(String conversionCheck)
    {
        boolean bewteenLetters = true;
        for (int i = 0; i < conversionCheck.length(); i++)
        {
            int charCode = (int)(conversionCheck.charAt(i));
            if (charCode > 0x7f)
            {
                bewteenLetters = false;
                if (i > 0 && Character.isLetter((int)(conversionCheck.charAt(i-1))) || 
                   (i < conversionCheck.length()-1 && Character.isLetter((int)(conversionCheck.charAt(i+1)))))
                {
                    bewteenLetters = true;
                    break;
                }
            }                
        }
        return(bewteenLetters);
    }

    private int arrayContainsAt(byte[] byteArray, int ft)
    {
        for (int i = 0; i < byteArray.length; i++)
        {
            if (byteArray[i] == (byte)ft)  return(i);
        }
        return(-1);
    }

    private DataField parseDataField(String tag, byte[] field)  throws IOException 
    {
        if (permissive)
        {
            errors.setRecordID(record.getControlNumber());
            if (tag.equals("880"))
            {
                String fieldTag = new String(field);
                fieldTag = fieldTag.replaceFirst("^.*\\x1F6", "").replaceFirst("([-0-9]*).*", "$1");
                errors.setCurrentField(tag+"("+fieldTag+")"); 
            }
            else
                errors.setCurrentField(tag); 
            errors.setCurrentSubfield("n/a");
            cleanupBadFieldSeperators(field);
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(field);
        char ind1 = (char) bais.read();
        char ind2 = (char) bais.read();

        DataField dataField = factory.newDataField();
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
            if (readByte < 0)
                break;
            switch (readByte) {
            case Constants.US:
                code = bais.read();
                if (code < 0)
                    throw new IOException("unexpected end of data field");
                if (code == Constants.FT)
                    break;
                size = getSubfieldLength(bais);
                if (size == 0)
                {
                    if (permissive)
                    {
                        errors.addError(ErrorHandler.MINOR_ERROR, "Subfield of zero length encountered, ignoring it.");
                        continue;
                    }
                    throw new IOException("Subfield of zero length encountered");
                }
                data = new byte[size];
                bais.read(data);
                subfield = factory.newSubfield();
                if (permissive) errors.setCurrentSubfield("" + (char)code);
                String dataAsString = getDataAsString(data);
                if (permissive && code == Constants.US)
                {
                    code = data[0];
                    dataAsString = dataAsString.substring(1);
                    errors.addError(ErrorHandler.MAJOR_ERROR, 
                                    "Subfield tag is a subfield separator, using first character of field as subfield tag.");
                }
                else if (permissive && validSubfieldCodes.indexOf(code) == -1)
                {
                    if (code >= 'A' && code <= 'Z')
                    { 
                        if ( Boolean.parseBoolean(System.getProperty(upperCaseSubfieldsProperty, "false")) == false)
                        {
                            code = Character.toLowerCase(code);    
                            errors.addError(ErrorHandler.MINOR_ERROR, 
                                        "Subfield tag is an invalid uppercase character, changing it to lower case.");
                        }
                        else // the System Property org.marc4j.MarcPermissiveStreamReader.upperCaseSubfields is defined to allow upperCaseSubfields
                        {
                            // therefore do nothing and be happy
                        }
                    }
                    else if (code > 0x7f)
                    { 
                        code = data[0];    
                        dataAsString = dataAsString.substring(1);
                        errors.addError(ErrorHandler.MAJOR_ERROR, 
                                        "Subfield tag is an invalid character greater than 0x7f, using first character of field as subfield tag.");
                    }
                    else if (code == '[' && tag.equals("245"))
                    { 
                        code = 'h';
                        dataAsString = '[' + dataAsString; 
                        errors.addError(ErrorHandler.MAJOR_ERROR, 
                                        "Subfield tag is an open bracket, generating a code 'h' and pushing the bracket to the data.");
                    }
                    else if (code == ' ')
                    {
                     	errors.addError(ErrorHandler.MAJOR_ERROR, 
                                        "Subfield tag is a space which is an invalid character");
                    }
                  	else 
                    {
                     	errors.addError(ErrorHandler.MAJOR_ERROR, 
                                        "Subfield tag is an invalid character, [ "+((char)code)+" ]");
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
 
    private void cleanupBadFieldSeperators(byte[] field)
    {
        if (conv == null) conv = new AnselToUnicode(true);
        boolean hasEsc = false;
        boolean inMultiByte = false;
        boolean justCleaned = false;
        int mbOffset = 0;
        
        for (int i = 0 ; i < field.length-1; i++)
        {
            if (field[i] == 0x1B)
            {   
                hasEsc = true;
                if ("(,)-'".indexOf((char)field[i+1]) != -1)
                {
                    inMultiByte = false;
                }
                else if (i + 2 < field.length && field[i+1] == '$' && field[i+2] == '1')
                {
                    inMultiByte = true;
                    mbOffset = 3;
                }
                else if (i + 3 < field.length && (field[i+1] == '$' || field[i+2] == '$')&& ( field[i+2] == '1' || field[i+3] == '1'))
                {
                    inMultiByte = true;
                    mbOffset = 4;
                }

            }
            else if (inMultiByte && field[i] != 0x20)   mbOffset = ( mbOffset == 0) ? 2 : mbOffset - 1;
            if (inMultiByte && mbOffset == 0 && i + 2 < field.length)
            {
                char c;
                byte f1 = field[i];
                byte f2 = field[i+1] == 0x20 ? field[i+2] : field[i+1];
                byte f3 = (field[i+1] == 0x20 || field[i+2] == 0x20) ? field[i+3] : field[i+2];
                c = conv.getMBChar(conv.makeMultibyte((char)((f1 == Constants.US) ? 0x7C : f1),
                                                      (char)((f2 == Constants.US) ? 0x7C : f2),
                                                      (char)((f3 == Constants.US) ? 0x7C : f3)));
                if (c == 0 && !justCleaned) 
                {
                    errors.addError(ErrorHandler.MAJOR_ERROR, 
                                    "Bad Multibyte character found, reinterpreting data as non-multibyte data");
                    inMultiByte = false; 
                }
                else if (c == 0 && justCleaned)
                {
                    c = conv.getMBChar(conv.makeMultibyte('!',(char)((f2 == Constants.US) ? 0x7C : f2),
                                                          (char)((f3 == Constants.US) ? 0x7C : f3)));
                    if (c == 0)
                    {
                        errors.addError(ErrorHandler.MAJOR_ERROR, 
                                        "Bad Multibyte character found, reinterpreting data as non-multibyte data");
                        inMultiByte = false; 
                    }                        
                    else
                    {
                        errors.addError(ErrorHandler.MAJOR_ERROR, 
                                        "Character after restored vertical bar character makes bad multibyte character, changing it to \"!\"");
                        field[i] = '!';
                    }
                }
            }
            justCleaned = false;
            if (field[i] == Constants.US )
            {
                if (inMultiByte && mbOffset != 0)
                {
                    field[i] = 0x7C;
                    errors.addError(ErrorHandler.MAJOR_ERROR, 
                                    "Subfield separator found in middle of a multibyte character, changing it to a vertical bar, and continuing");
                    if (field[i+1] == '0')
                    { 
                        if (field[i+2] == '(' && field[i+3] == 'B' )  
                        {
                            field[i+1] = 0x1B;
                            errors.addError(ErrorHandler.MAJOR_ERROR, 
                                            "Character after restored vertical bar character makes bad multibyte character, changing it to ESC");
                        }
                        else
                        {
                            field[i+1] = 0x21;
                            errors.addError(ErrorHandler.MAJOR_ERROR, 
                                            "Character after restored vertical bar character makes bad multibyte character, changing it to \"!\"");
                        }
                    }
                    justCleaned = true;
                }
                else if (hasEsc && !((field[i+1] >= 'a' && field[i+1] <= 'z') || (field[i+1] >= '0' && field[i+1] <= '9')))
                {
                    errors.addError(ErrorHandler.MAJOR_ERROR, 
                                    "Subfield separator followed by invalid subfield tag, changing separator to a vertical bar, and continuing");
                    field[i] = 0x7C;
                    justCleaned = true;
                }
                else if (hasEsc && i < field.length-3 && 
                        (field[i+1] == '0' && field[i+2] == '('  && field[i+3] == 'B' ))
                {
                    errors.addError(ErrorHandler.MAJOR_ERROR, 
                                    "Subfield separator followed by invalid subfield tag, changing separator to a vertical bar, and continuing");
                    field[i] = 0x7C;
                    field[i+1] = 0x1B;
                    justCleaned = true;
                }
                else if (hasEsc && (field[i+1] == '0' ))
                {
                    errors.addError(ErrorHandler.MAJOR_ERROR, 
                                    "Subfield separator followed by invalid subfield tag, changing separator to a vertical bar, and continuing");
                    field[i] = 0x7C;
                    field[i+1] = 0x21;
                    justCleaned = true;
                }
                else if (field[i+1] == Constants.US && field[i+2] == Constants.US )
                {
                    errors.addError(ErrorHandler.MAJOR_ERROR, 
                                    "Three consecutive subfield separators, changing first two to vertical bars.");
                    field[i] = 0x7C;
                    field[i+1] = 0x7C;
                    justCleaned = true;
                }
            }
        }
    }

    private int getFieldLength(DataInputStream bais) throws IOException 
    {
        bais.mark(9999);
        int bytesRead = 0;
        while (true) {
            switch (bais.read()) {
             case Constants.FT:
                bais.reset();
                return bytesRead;
            case -1:
                bais.reset();
                if (permissive)
                {
                    errors.addError(ErrorHandler.MINOR_ERROR, 
                                    "Field not terminated trying to continue");
                    return (bytesRead);
                }
                else
                    throw new IOException("Field not terminated");
            case Constants.US:
            default:
                bytesRead++;
            }
        }
    }

    private int getSubfieldLength(ByteArrayInputStream bais) throws IOException {
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
                if (permissive)
                {
                    errors.addError(ErrorHandler.MINOR_ERROR, "Subfield not terminated trying to continue");
                    return (bytesRead);
                }
                else
                    throw new IOException("subfield not terminated");
            default:
                bytesRead++;
            }
        }
    }

    private int parseRecordLength(byte[] leaderData) throws IOException {
        InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(
                leaderData));
        int length = -1;
        char[] tmp = new char[5];
        isr.read(tmp);
        try {
            length = Integer.parseInt(new String(tmp));
        } catch (NumberFormatException e) {
            errors.addError(ErrorHandler.FATAL, 
                            "Unable to parse record length, Unable to Continue");
            throw new MarcException("unable to parse record length", e);
        }
        return(length);
    }
    
    private void parseLeader(Leader ldr, byte[] leaderData) throws IOException {
        //System.err.println("leader is: ("+new String(leaderData, "ISO-8859-1")+")");
        InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(leaderData),"ISO-8859-1");
        char[] tmp = new char[5];
        isr.read(tmp);
        //  Skip over bytes for record length, If we get here, its already been computed.
        ldr.setRecordStatus((char) isr.read());
        ldr.setTypeOfRecord((char) isr.read());
        tmp = new char[2];
        isr.read(tmp);
        ldr.setImplDefined1(tmp);
        ldr.setCharCodingScheme((char) isr.read());
        char indicatorCount = (char) isr.read();
        char subfieldCodeLength = (char) isr.read();
        char baseAddr[] = new char[5];
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
        } catch (NumberFormatException e) {
            if (permissive) {
                // All Marc21 records should have indicatorCount '2'
                errors.addError(ErrorHandler.ERROR_TYPO, "bogus indicator count - byte value =  " + Integer.toHexString(indicatorCount & 0xff));
                ldr.setIndicatorCount(2);  
            }
            else {
                throw new MarcException("unable to parse indicator count", e);
            }
        }
        try {
            ldr.setSubfieldCodeLength(Integer.parseInt(String
                    .valueOf(subfieldCodeLength)));
        } catch (NumberFormatException e) {
            if (permissive) {
                // All Marc21 records should have subfieldCodeLength '2' 
                errors.addError(ErrorHandler.ERROR_TYPO, "bogus subfield count - byte value =  " + Integer.toHexString(subfieldCodeLength & 0xff));
                ldr.setSubfieldCodeLength(2);
            }
            else {
                throw new MarcException("unable to parse subfield code length", e);
            }
        }
        try {
            ldr.setBaseAddressOfData(Integer.parseInt(new String(baseAddr)));
        } catch (NumberFormatException e) {
            throw new MarcException("unable to parse base address of data", e);
        }

    }

    private String getDataAsString(byte[] bytes) 
    {
        String dataElement = null;
        if (encoding.equals("UTF-8") || encoding.equals("UTF8"))
        {
            try {
                dataElement = new String(bytes, "UTF-8");
            } 
            catch (UnsupportedEncodingException e) {
                throw new MarcException("unsupported encoding", e);
            }
        }
        else if (encoding.equals("UTF8-Maybe"))
        {
            try {
                dataElement = new String(bytes, "UTF-8");
            } 
            catch (UnsupportedEncodingException e) {
                throw new MarcException("unsupported encoding", e);
            }
        }
        else if (encoding.equals("MARC-8") || encoding.equals("MARC8"))
        {
            dataElement = getMarc8Conversion(bytes);
        }
        else if (encoding.equalsIgnoreCase("Unimarc") || encoding.equals("IS05426"))
        {
            dataElement = getUnimarcConversion(bytes);
        }
        else if (encoding.equals("MARC8-Maybe"))
        {
            String dataElement1 = getMarc8Conversion(bytes);
            String dataElement2 = getUnimarcConversion(bytes);
            String dataElement3 = null;
            try
            {
                dataElement3 = new String(bytes, "ISO-8859-1");
            }
            catch (UnsupportedEncodingException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (dataElement1.equals(dataElement2) && dataElement1.equals(dataElement3))
            {
                dataElement = dataElement1;
            }
            else 
            {
                conversionCheck1 = conversionCheck1 + "|>" + Normalizer.normalize(dataElement1, Normalizer.NFC);
                conversionCheck2 = conversionCheck2 + "|>" + dataElement2;
                conversionCheck3 = conversionCheck3 + "|>" + dataElement3;
                dataElement = dataElement1 + "%%@%%" + dataElement2 + "%%@%%" + dataElement3;                
            }            
        }
        else if (encoding.equals("MARC8-Broken"))
        {
            try
            {
                dataElement = new String(bytes, "ISO-8859-1");
            }
            catch (UnsupportedEncodingException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String newdataElement = dataElement.replaceAll("&lt;", "<");
            newdataElement = newdataElement.replaceAll("&gt;", ">");
            newdataElement = newdataElement.replaceAll("&amp;", "&");
            newdataElement = newdataElement.replaceAll("&apos;", "'");
            newdataElement = newdataElement.replaceAll("&quot;", "\"");
            if (!newdataElement.equals(dataElement))   
            {
                dataElement = newdataElement;
                errors.addError(ErrorHandler.ERROR_TYPO, "Subfield contains escaped html character entities, un-escaping them. ");
            }
            String rep1 = ""+(char)0x1b+"\\$1$1";
            String rep2 = ""+(char)0x1b+"\\(B";                    
            newdataElement = dataElement.replaceAll("\\$1(.)", rep1);
            newdataElement = newdataElement.replaceAll("\\(B", rep2);
            if (!newdataElement.equals(dataElement))   
            {
                dataElement = newdataElement;
                errors.addError(ErrorHandler.MAJOR_ERROR, "Subfield seems to be missing MARC8 escape sequences, trying to restore them.");
            }
            try
            {
                dataElement = getMarc8Conversion(dataElement.getBytes("ISO-8859-1"));
            }
            catch (UnsupportedEncodingException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        else if (encoding.equals("ISO-8859-1") || encoding.equals("ISO8859_1"))
        {
            try {
                dataElement = new String(bytes, "ISO-8859-1");
            } 
            catch (UnsupportedEncodingException e) {
                throw new MarcException("unsupported encoding", e);
            }
        }
        else 
        {
            try {
                dataElement = new String(bytes, encoding);
            } 
            catch (UnsupportedEncodingException e) {
                throw new MarcException("Unknown or unsupported Marc character encoding:" + encoding);  
            }                   
        }
        if (errors != null && dataElement.matches("[^&]*&[a-z]*;.*"))
        {
            String newdataElement = dataElement.replaceAll("&lt;", "<");
            newdataElement = newdataElement.replaceAll("&gt;", ">");
            newdataElement = newdataElement.replaceAll("&amp;", "&");
            newdataElement = newdataElement.replaceAll("&apos;", "'");
            newdataElement = newdataElement.replaceAll("&quot;", "\"");
            if (!newdataElement.equals(dataElement))   
            {
                dataElement = newdataElement;
                errors.addError(ErrorHandler.ERROR_TYPO, "Subfield contains escaped html character entities, un-escaping them. ");
            }
        }
        return dataElement;
    }

    private boolean byteArrayContains(byte[] bytes, byte[] seq)
    {
        for ( int i = 0; i < bytes.length - seq.length; i++)
        {
            if (bytes[i] == seq[0])
            {
                for (int j = 0; j < seq.length; j++)
                {
                    if (bytes[i+j] != seq[j])
                    {
                        break;
                    }
                    if (j == seq.length-1) return(true);
                }
            }
        }
        return(false);
    }
    
    static byte badEsc[] = { (byte)('b'), (byte)('-'), 0x1b, (byte)('s') };
    static byte overbar[] = { (byte)(char)(0xaf) };
     
    private String getMarc8Conversion(byte[] bytes)
    {
        String dataElement = null;
        if (converterAnsel == null) converterAnsel = new AnselToUnicode(errors);
        if(isTranslateLosslessUnicodeNumericCodeReferencesEnabled()) {
            AnselToUnicode anselConverter = (AnselToUnicode) converterAnsel;
            anselConverter.setTranslateNCR(isTranslateLosslessUnicodeNumericCodeReferencesEnabled());
        }
        if (permissive && (byteArrayContains(bytes, badEsc) || byteArrayContains(bytes, overbar)))  
        {
            String newDataElement = null;
            try
            {
                dataElement = new String(bytes, "ISO-8859-1");
                newDataElement = dataElement.replaceAll("(\\e)b-\\es([psb$()])", "$1$2");
                if (!newDataElement.equals(dataElement))
                {
                    dataElement = newDataElement;
                    errors.addError(ErrorHandler.MINOR_ERROR, "Subfield contains odd pattern of subscript or superscript escapes. ");
                }
                newDataElement = dataElement.replace((char)0xaf, (char)0xe5);
                if (!newDataElement.equals(dataElement))
                {
                    dataElement = newDataElement;
                    errors.addError(ErrorHandler.ERROR_TYPO, "Subfield contains 0xaf overbar character, changing it to proper MARC8 representation ");
                }
                dataElement = converterAnsel.convert(dataElement);                    
            }
            catch (UnsupportedEncodingException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else 
        {
            dataElement = converterAnsel.convert(bytes);
        }

        return(dataElement);
    }
    
    private String getUnimarcConversion(byte[] bytes)
    {
        if (converterUnimarc == null) converterUnimarc = new Iso5426ToUnicode();
        String dataElement = converterUnimarc.convert(bytes);
        dataElement = dataElement.replaceAll("\u0088", "");
        dataElement = dataElement.replaceAll("\u0089", "");
//        for ( int i = 0 ; i < bytes.length; i++)
//        {
//            if (bytes[i] == -120 || bytes[i] == -119)
//            {
//                char tmp = (char)bytes[i]; 
//                char temp2 = dataElement.charAt(0);
//                char temp3 = dataElement.charAt(4);
//                int tmpi = (int)tmp;
//                int tmp2 = (int)temp2;
//                int tmp3 = (int)temp3;
//                i = i;
//
//            }
//        }
        if (dataElement.matches("[^<]*<U[+][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f]>.*"))
        {
            Pattern pattern = Pattern.compile("<U[+]([0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f])>"); 
            Matcher matcher = pattern.matcher(dataElement);
            StringBuffer newElement = new StringBuffer();
            int prevEnd = 0;
            while (matcher.find())
            {
                newElement.append(dataElement.substring(prevEnd, matcher.start()));
                newElement.append(getChar(matcher.group(1)));
                prevEnd = matcher.end();
            }
            newElement.append(dataElement.substring(prevEnd));
            dataElement = newElement.toString();
        }
        return(dataElement);

    }
    
    private String getChar(String charCodePoint)
    {
        int charNum = Integer.parseInt(charCodePoint, 16);
        String result = ""+((char)charNum);
        return(result);
    }

}
