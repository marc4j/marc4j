// $Id: MarcStreamWriter.java,v 1.3 2006/07/28 12:26:23 bpeters Exp $
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
import java.util.Iterator;
import java.util.List;

import org.marc4j.converter.CharConverter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

/**
 * Class for writing MARC record objects in ISO 2709 format.
 * 
 * <p>
 * The following example reads a file with MARCXML records and outputs the
 * record set in ISO 2709 format:
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
 * 
 * <p>
 * To convert characters like for example from UCS/Unicode to MARC-8 register
 * a {@link org.marc4j.converter.CharConverter}&nbsp;implementation:
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
 * @version $Revision: 1.3 $
 */
public class MarcStreamWriter implements MarcWriter {

    private OutputStream out = null;

    private String encoding = "ISO_8859_1";

    private CharConverter converter = null;

    private static DecimalFormat format4 = new DecimalFormat("0000");

    private static DecimalFormat format5 = new DecimalFormat("00000");

    /**
     * Constructs an instance and creates a <code>Writer</code> object with
     * the specified output stream.
     */
    public MarcStreamWriter(OutputStream out) {
        this.out = out;
    }

    /**
     * Constructs an instance and creates a <code>Writer</code> object with
     * the specified output stream and character encoding.
     */
    public MarcStreamWriter(OutputStream out, String encoding) {
        this.encoding = encoding;
        this.out = out;
    }

    /**
     * Returns the character converter.
     * 
     * @return CharConverter the character converter
     */
    public CharConverter getConverter() {
        return converter;
    }

    /**
     * Sets the character converter.
     * 
     * @param converter
     *            the character converter
     */
    public void setConverter(CharConverter converter) {
        this.converter = converter;
    }

    /**
     * Writes a <code>Record</code> object to the writer.
     * 
     * @param record -
     *            the <code>Record</code> object
     */
    public void write(Record record) {
        int previous = 0;

        try {
            ByteArrayOutputStream data = new ByteArrayOutputStream();
            ByteArrayOutputStream dir = new ByteArrayOutputStream();

            // control fields
            List fields = record.getControlFields();
            Iterator i = fields.iterator();
            while (i.hasNext()) {
                ControlField cf = (ControlField) i.next();

                data.write(getDataElement(cf.getData()));
                data.write(Constants.FT);
                dir.write(getEntry(cf.getTag(), data.size() - previous,
                        previous));
                previous = data.size();
            }

            // data fields
            fields = record.getDataFields();
            i = fields.iterator();
            while (i.hasNext()) {
                DataField df = (DataField) i.next();
                data.write(df.getIndicator1());
                data.write(df.getIndicator2());
                List subfields = df.getSubfields();
                Iterator si = subfields.iterator();
                while (si.hasNext()) {
                    Subfield sf = (Subfield) si.next();
                    data.write(Constants.US);
                    data.write(sf.getCode());
                    data.write(getDataElement(sf.getData()));
                }
                data.write(Constants.FT);
                dir.write(getEntry(df.getTag(), data.size() - previous,
                        previous));
                previous = data.size();
            }
            dir.write(Constants.FT);

            // base address of data and logical record length
            Leader ldr = record.getLeader();

            ldr.setBaseAddressOfData(24 + dir.size());
            ldr.setRecordLength(ldr.getBaseAddressOfData() + data.size() + 1);

            // write record to output stream
            dir.close();
            data.close();
            write(ldr);
            out.write(dir.toByteArray());
            out.write(data.toByteArray());
            out.write(Constants.RT);

        } catch (IOException e) {
            throw new MarcException("IO Error occured while writing record", e);
        }
    }

    private void write(Leader ldr) throws IOException {
        out.write(format5.format(ldr.getRecordLength()).getBytes(encoding));
        out.write(ldr.getRecordStatus());
        out.write(ldr.getTypeOfRecord());
        out.write(new String(ldr.getImplDefined1()).getBytes(encoding));
        out.write(ldr.getCharCodingScheme());
        out.write(Integer.toString(ldr.getIndicatorCount()).getBytes(encoding));
        out.write(Integer.toString(ldr.getSubfieldCodeLength()).getBytes(
                encoding));
        out
                .write(format5.format(ldr.getBaseAddressOfData()).getBytes(
                        encoding));
        out.write(new String(ldr.getImplDefined2()).getBytes(encoding));
        out.write(new String(ldr.getEntryMap()).getBytes(encoding));
    }

    /**
     * Closes the writer.
     */
    public void close() {
        try {
            out.close();
        } catch (IOException e) {
            throw new MarcException("IO Error occured on close", e);
        }
    }

    private byte[] getDataElement(String data) throws IOException {
        if (converter != null)
            return converter.convert(data).getBytes(encoding);
        return data.getBytes(encoding);
    }

    private byte[] getEntry(String tag, int length, int start)
            throws IOException {
        return (tag + format4.format(length) + format5.format(start))
                .getBytes(encoding);
    }
}