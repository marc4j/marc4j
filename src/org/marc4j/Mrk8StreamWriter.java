/**
 * Copyright (C) 2016 Binaek Sarkar
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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import org.marc4j.converter.CharConverter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

/**
 * Class for writing MARC record objects in MRK8 format.
 * <p>
 * The following example reads a file with MARCXML records and outputs the
 * record set in MRK8 format:
 * </p>
 *
 * <pre>
 * InputStream input = new FileInputStream(&quot;marcxml.xml&quot;);
 * MarcXmlReader reader = new MarcXmlReader(input);
 * MarcWriter writer = new Mrk8StreamWriter(System.out);
 * while (reader.hasNext()) {
 *     Record record = reader.next();
 *     writer.write(record);
 * }
 * writer.close();
 * </pre>
 *
 *
 * @author Binaek Sarkar
 */
public class Mrk8StreamWriter implements MarcWriter {

    private final PrintWriter mrk8Writer;
//    private final CharsetEncoder encoder;

    /**
     * Constructs an instance and creates a {@link MarcWriter} object with the
     * specified output stream.
     * 
     * @param output The {@link OutputStream} to write to
     */
    public Mrk8StreamWriter(final OutputStream output) {
 //       this.encoder = StandardCharsets.UTF_8.newEncoder();
        this.mrk8Writer = new PrintWriter(output);
    }

    /**
     * Writes a {@link Record} object to the given {@link OutputStream}
     *
     * @param record - the <code>Record</code> object
     */
    @Override
    public void write(final Record record) {
        final StringBuilder recordStringBuilder = new StringBuilder();

        final Leader ldr = record.getLeader();
        recordStringBuilder.append("=").append("LDR").append("  ").append(ldr.marshal()).append(System.lineSeparator());;

        for (final VariableField field : record.getVariableFields()) {
            recordStringBuilder.append("=").append(field.getTag()).append("  ");

            if (field instanceof ControlField) {
                final ControlField controlField = (ControlField) field;
                String data;
//                try {
//                    data = this.encoder.encode(CharBuffer.wrap(controlField.getData())).asCharBuffer().toString();
//                } catch (CharacterCodingException cce) {
                    data = controlField.getData();
                    data = data.replace(' ', '\\');
//                }
                recordStringBuilder.append(data);
            } else if (field instanceof DataField) {
                final DataField dataField = (DataField) field;
                recordStringBuilder.append((dataField.getIndicator1() == ' ') ? "\\" : dataField.getIndicator1());
                recordStringBuilder.append((dataField.getIndicator2() == ' ') ? "\\" : dataField.getIndicator2());

                for (final Subfield subField : dataField.getSubfields()) {
                    String data;
                    data =  Mrk8TranslationTable.toMrk8(subField.getData());
                    recordStringBuilder.append("$").append(subField.getCode()).append(data);
                }
            }
            recordStringBuilder.append(System.lineSeparator());
        }
        recordStringBuilder.append(System.lineSeparator());

        this.mrk8Writer.append(recordStringBuilder);
        this.mrk8Writer.flush();
    }

    /**
     * This should set the character converter.
     *
     * However, since the {@link Mrk8StreamWriter} uses the UTF-8 encoder from
     * {@link StandardCharsets} this method has no effect
     *
     * @param converter the character converter
     */
    @Override
    public void setConverter(final CharConverter converter) {
    }

    /**
     * Returns the character converter.
     *
     * In this case, always returns <code>null</code>
     *
     * @return CharConverter always <code>null</code>
     */
    @Override
    public CharConverter getConverter() {
        return null;
    }

    /**
     * Closes the writer and the underlying {@link OutputStream}
     */
    @Override
    public void close() {
        this.mrk8Writer.flush();
        this.mrk8Writer.close();
    }
}
