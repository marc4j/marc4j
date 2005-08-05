// $Id: MarcStreamWriter.java,v 1.2 2005/08/05 17:29:55 bpeters Exp $
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
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
 * 	Record record = reader.next();
 * 	writer.write(record);
 * }
 * writer.close();
 * </pre>
 * 
 * <p>
 * To convert characters like to converting from UCS/Unicode to MARC-8 register
 * a {@link org.marc4j.converter.CharConverter}implementation:
 * </p>
 * 
 * <pre>
 * InputStream input = new FileInputStream(&quot;marcxml.xml&quot;);
 * MarcXmlReader reader = new MarcXmlReader(input);
 * MarcWriter writer = new MarcStreamWriter(System.out);
 * writer.setConverter(new UnicodeToAnsel());
 * while (reader.hasNext()) {
 * 	Record record = reader.next();
 * 	writer.write(record);
 * }
 * writer.close();
 * </pre>
 * 
 * @author Bas Peters
 * @version $Revision: 1.2 $
 */
public class MarcStreamWriter implements MarcWriter {

	private Writer writer = null;

	private String encoding = null;

	private CharConverter converter = null;

	private static DecimalFormat format4 = new DecimalFormat("0000");

	private static DecimalFormat format5 = new DecimalFormat("00000");

	/**
	 * Constructs an instance and creates a <code>Writer</code> object with
	 * the specified output stream.
	 */
	public MarcStreamWriter(OutputStream out) {
		writer = new OutputStreamWriter(out);
	}

	/**
	 * Constructs an instance and creates a <code>Writer</code> object with
	 * the specified output stream and character encoding.
	 */
	public MarcStreamWriter(OutputStream out, String encoding) {
		this.encoding = encoding;
		try {
			writer = new OutputStreamWriter(out, encoding);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
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
	 * Returns the <code>Writer</code> instance.
	 * 
	 * @return Writer - the writer instance
	 */
	public Writer getWriter() {
		return writer;
	}

	/**
	 * Writes a <code>Record</code> object to the writer.
	 * 
	 * @param record -
	 *            the <code>Record</code> object
	 */
	public void write(Record record) {
		int previous = 0;
		StringBuffer data = new StringBuffer();
		StringBuffer dir = new StringBuffer();

		// control fields
		List fields = record.getControlFields();
		Iterator i = fields.iterator();
		while (i.hasNext()) {
			ControlField cf = (ControlField) i.next();
			data.append(getDataElement(cf.getData()));
			data.append((char) Constants.FT);
			dir
					.append(getEntry(cf.getTag(), data.length() - previous,
							previous));
			previous = data.length();
		}

		// data fields
		fields = record.getDataFields();
		i = fields.iterator();
		while (i.hasNext()) {
			DataField df = (DataField) i.next();
			data.append(df.getIndicator1());
			data.append(df.getIndicator2());
			List subfields = df.getSubfields();
			Iterator si = subfields.iterator();
			while (si.hasNext()) {
				Subfield sf = (Subfield) si.next();
				data.append((char) Constants.US);
				data.append(sf.getCode());
				data.append(getDataElement(sf.getData()));
			}
			data.append((char) Constants.FT);
			dir
					.append(getEntry(df.getTag(), data.length() - previous,
							previous));
			previous = data.length();
		}
		dir.append((char) Constants.FT);

		// base address of data and logical record length
		Leader ldr = record.getLeader();
		ldr.setBaseAddressOfData(24 + dir.length());
		ldr.setRecordLength(ldr.getBaseAddressOfData() + data.length() + 1);

		// write record to output stream
		try {
			write(ldr);
			writer.write(dir.toString());
			writer.write(data.toString());
			writer.write(Constants.RT);
		} catch (IOException e) {
			throw new MarcException("IO Error occured while writing record", e);
		}
	}

	private void write(Leader ldr) throws IOException {
		writer.write(format5.format(ldr.getRecordLength()));
		writer.write(ldr.getRecordStatus());
		writer.write(ldr.getTypeOfRecord());
		writer.write(ldr.getImplDefined1());
		writer.write(ldr.getCharCodingScheme());
		writer.write(Integer.toString(ldr.getIndicatorCount()));
		writer.write(Integer.toString(ldr.getSubfieldCodeLength()));
		writer.write(format5.format(ldr.getBaseAddressOfData()));
		writer.write(ldr.getImplDefined2());
		writer.write(ldr.getEntryMap());
	}

	/**
	 * Closes the writer.
	 */
	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			throw new MarcException("IO Error occured on close", e);
		}
	}

	private String getDataElement(String data) {
		if (converter != null)
			return converter.convert(data);
		return data;
	}

	private String getEntry(String tag, int length, int start) {
		return tag + format4.format(length) + format5.format(start);
	}

}