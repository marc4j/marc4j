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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;

import org.marc4j.converter.CharConverter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.util.CustomDecimalFormat;

/**
 * Class for writing MARC record objects in ISO 2709 format.
 * <p>
 * The following example reads a file with MARCXML records and outputs the record set in ISO 2709 format:
 * </p>
 *
 * <pre>
 * InputStream input = new FileInputStream(&quot;marcxml.xml&quot;);
 * MarcXmlReader reader = new MarcXmlReader(input);
 * MarcWriter writer = new MarcStreamWriter(System.out);
 * while (reader.hasNext()) {
 *     Record record = reader.next();
 *     writer.write(record);
 * }
 * writer.close();
 * </pre>
 * <p>
 * To convert characters like for example from UCS/Unicode to MARC-8 register a
 * {@link org.marc4j.converter.CharConverter}&nbsp;implementation:
 * </p>
 *
 * <pre>
 * InputStream input = new FileInputStream(&quot;marcxml.xml&quot;);
 * MarcXmlReader reader = new MarcXmlReader(input);
 * MarcWriter writer = new MarcStreamWriter(System.out);
 * writer.setConverter(new UnicodeToAnsel());
 * while (reader.hasNext()) {
 *     Record record = reader.next();
 *     writer.write(record);
 * }
 * writer.close();
 * </pre>
 *
 * @author Bas Peters
 */
public class MarcStreamWriter implements MarcWriter {

    protected OutputStream out = null;

    protected String encoding = "ISO8859_1";

    private CharConverter converter = null;

    protected boolean allowOversizeEntry = false;

    protected boolean hasOversizeOffset = false;

    protected boolean hasOversizeLength = false;

    protected static DecimalFormat format4Use = new CustomDecimalFormat(4);

    protected static DecimalFormat format5Use = new CustomDecimalFormat(5);

    /**
     * Constructs an instance and creates a <code>Writer</code> object with the specified output stream.
     *
     * @param out - the OutputStream to write to
     */
    public MarcStreamWriter(final OutputStream out) {
        this.out = out;
    }

    /**
     * Constructs an instance and creates a <code>Writer</code> object with the specified output stream and character
     * encoding.
     *
     * @param out - the OutputStream to write to
     * @param encoding - the encoding to use when writing out the record
     */
    public MarcStreamWriter(final OutputStream out, final String encoding) {
        this.encoding = encoding;
        this.out = out;
    }

    /**
     * Constructs an instance and creates a <code>Writer</code> object with the specified output stream.
     *
     * @param out - the OutputStream to write to
     * @param allowOversizeRecord - true to allow oversized records to be written out.
     */
    public MarcStreamWriter(final OutputStream out, final boolean allowOversizeRecord) {
        this.out = out;
        this.allowOversizeEntry = allowOversizeRecord;
    }

    /**
     * Constructs an instance and creates a <code>Writer</code> object with the specified output stream and character
     * encoding.
     *
     * @param out - the OutputStream to write to
     * @param encoding - the encoding to use when writing out the record
     * @param allowOversizeRecord - true to allow oversized records to be written out.
     */
    public MarcStreamWriter(final OutputStream out, final String encoding, final boolean allowOversizeRecord) {
        this.encoding = encoding;
        this.out = out;
        this.allowOversizeEntry = allowOversizeRecord;
    }

    /**
     * Returns the character converter.
     *
     * @return the character converter
     */
    @Override
    public CharConverter getConverter() {
        return converter;
    }

    /**
     * Sets the character converter.
     *
     * @param converter - the character converter
     */
    @Override
    public void setConverter(final CharConverter converter) {
        this.converter = converter;
    }

    /**
     * Writes a <code>Record</code> object to the writer.
     *
     * @param record - the <code>Record</code> object
     */
    @Override
    public void write(final Record record) {
        int previous = 0;

        try {
            final ByteArrayOutputStream data = new ByteArrayOutputStream();
            final ByteArrayOutputStream dir = new ByteArrayOutputStream();
            hasOversizeOffset = false;
            hasOversizeLength = false;

            // control fields
            for (final ControlField cf : record.getControlFields()) {
                data.write(getDataElement(cf.getData()));
                data.write(Constants.FT);
                dir.write(getEntry(cf.getTag(), data.size() - previous, previous));
                previous = data.size();
            }

            // data fields
            for (final DataField df : record.getDataFields()) {
                data.write(df.getIndicator1());
                data.write(df.getIndicator2());
                for (final Subfield sf : df.getSubfields()) {
                    data.write(Constants.US);
                    data.write(sf.getCode());
                    data.write(getDataElement(sf.getData()));
                }
                data.write(Constants.FT);
                dir.write(getEntry(df.getTag(), data.size() - previous, previous));
                previous = data.size();
            }
            dir.write(Constants.FT);

            // base address of data and logical record length
            final Leader ldr = record.getLeader();

            final int baseAddress = 24 + dir.size();
            ldr.setBaseAddressOfData(baseAddress);
            final int recordLength = ldr.getBaseAddressOfData() + data.size() + 1;
            ldr.setRecordLength(recordLength);

            // write record to output stream
            dir.close();
            data.close();

            if (!allowOversizeEntry && (baseAddress > 99999 || recordLength > 99999 || hasOversizeOffset)) {
                throw new MarcException("Record is too long to be a valid MARC binary record, it's length would be " +
                        recordLength + " which is more thatn 99999 bytes");
            }
            if (!allowOversizeEntry && (hasOversizeLength)) {
                throw new MarcException("Record has field that is too long to be a valid MARC binary record. "
                        + "The maximum length for a field counting all of the sub-fields is 9999 bytes.");
            }
            if (converter != null) {
                ldr.setCharCodingScheme(converter.outputsUnicode() ? 'a' : ' ');
            }
            writeLeader(ldr);
            out.write(dir.toByteArray());
            out.write(data.toByteArray());
            out.write(Constants.RT);

        } catch (final IOException e) {
            throw new MarcException("IO Error occured while writing record", e);
        } catch (final MarcException e) {
            throw e;
        }
    }

    protected void writeLeader(final Leader ldr) throws IOException {
        out.write(format5Use.format(ldr.getRecordLength()).getBytes(encoding));
        out.write(ldr.getRecordStatus());
        out.write(ldr.getTypeOfRecord());
        out.write(new String(ldr.getImplDefined1()).getBytes(encoding));
        out.write(ldr.getCharCodingScheme());
        out.write(Integer.toString(ldr.getIndicatorCount()).getBytes(encoding));
        out.write(Integer.toString(ldr.getSubfieldCodeLength()).getBytes(encoding));
        out.write(format5Use.format(ldr.getBaseAddressOfData()).getBytes(encoding));
        out.write(new String(ldr.getImplDefined2()).getBytes(encoding));
        out.write(new String(ldr.getEntryMap()).getBytes(encoding));
    }

    /**
     * Closes the writer.
     */
    @Override
    public void close() {
        try {
            out.close();
        } catch (final IOException e) {
            throw new MarcException("IO Error occured on close", e);
        }
    }

    protected byte[] getDataElement(final String data) throws IOException {
        if (converter != null) {
            return converter.convert(data).getBytes(encoding);
        }
        return data.getBytes(encoding);
    }

    protected byte[] getEntry(final String tag, final int length, final int start) throws IOException {
        final String entryUse = tag + format4Use.format(length) + format5Use.format(start);
        if (length > 99999) {
            hasOversizeLength = true;
        }
        if (start > 99999) {
            hasOversizeOffset = true;
        }
        return (entryUse.getBytes(encoding));
    }

    /**
     * Returns <code>true</code> if an oversized entry is allowed; else, <code>false</code>.
     *
     * @return <code>true</code> if an oversized entry is allowed
     */
    public boolean allowsOversizeEntry() {
        return allowOversizeEntry;
    }

    /**
     * Sets whether an oversized entry is allowed.
     *
     * @param allowOversizeEntry - true if an oversized entry ought to be allowed
     */
    public void setAllowOversizeEntry(final boolean allowOversizeEntry) {
        this.allowOversizeEntry = allowOversizeEntry;
    }
}
