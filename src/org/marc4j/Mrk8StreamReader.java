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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.marc4j.converter.CharConverter;
import org.marc4j.converter.impl.AnselToUnicode;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

/**
 * An iterator over a collection of MARC records in MRK8 format.
 * <p>
 * Example usage:
 * <pre>
 * InputStream input = new FileInputStream(&quot;file.mrk8&quot;);
 * MarcReader reader = new MarcStreamReader(input);
 * while (reader.hasNext()) {
 *     Record record = reader.next();
 *     // Process record
 * }
 * </pre>
 * <p>
 * Check the {@link org.marc4j.marc}&nbsp;package for examples about the use of
 * the {@link org.marc4j.marc.Record} &nbsp;object model.
 * </p>
 *
 * @author Binaek Sarkar
 * @author Robert Haschart
 */
public class Mrk8StreamReader implements MarcReader {

    private final Scanner input;

    private final MarcFactory factory;

    private boolean toUTF8;

    private String lastLineRead;

    private Pattern nonAsciiChar = Pattern.compile("[^\\u0020-\\u007F]");

    private CharConverter Marc8ToUTF8 = null;

    /**
     * Constructs an instance with the specified input stream.
     *
     * @param input - the data to read
     */
    public Mrk8StreamReader(final InputStream input) {
        this(input, false);
    }

    /**
     * Constructs an instance with the specified input stream.
     *
     * @param input - the data to read
     * @param toUtf8 - true if the record returned should be converted to UTF-8
     */
    public Mrk8StreamReader(final InputStream input, boolean toUtf8) {
        this.input = new Scanner(new BufferedInputStream(input), StandardCharsets.UTF_8.name());
        this.factory = MarcFactory.newInstance();
        this.toUTF8 = toUtf8;
    }

    /**
     * Returns true if the iteration has more records, false otherwise.
     *
     * @return Returns true if the iteration has more records, false otherwise.
     */
    @Override
    public boolean hasNext() {
        return this.input.hasNextLine();
    }

    /**
     * Returns the next record in the given {@link InputStream}
     *
     * @return Record - the record object
     */
    @Override
    public Record next() {
        final List<String> lines = new ArrayList<String>();
        if (!this.hasNext()) {
            return null;
        }
        if (this.lastLineRead != null && this.lastLineRead.substring(1, 4).equalsIgnoreCase("LDR")) {
            lines.add(lastLineRead);
            this.lastLineRead = null;
        }
        boolean hasHiBitCharacters = false;
        while (this.input.hasNextLine()) {
            final String line = this.input.nextLine();

            if (line.trim().length() == 0) {
                // this is a blank line. We do not need it
                continue;
            }
            if (line.substring(1, 4).equalsIgnoreCase("LDR") && lines.size() > 0) {
                // we have reached the next record... break for parsing;
                this.lastLineRead = line;
                break;
            }

            lines.add(line);
            if (hasHiBitCharacters == false && nonAsciiChar.matcher(line).find()) {
                hasHiBitCharacters = true;
            }
        }
        return this.parse(lines, hasHiBitCharacters);
    }

    protected Record parse(final List<String> lines, boolean isUTF8) {
        if (lines == null || lines.isEmpty()) {
            return null;
        }

        final Record record = this.factory.newRecord();

        for (final String line : lines) {
            if (line.trim().length() == 0) {
                continue;
            }

            final String tag = line.substring(1, 4);

            if (tag.equalsIgnoreCase("LDR")) {
                record.setLeader(getLeader(line.substring(6)));
            } else {
                final VariableField field;
                if (this.isControlField(tag)) {
                    field = this.factory.newControlField(tag, unescapeFieldValue(line.substring(6)));;
                } else {
                    // this is obviously a data field
                    final String data = line.substring(6);

                    final char indicator1 = (data.charAt(0) == '\\' ? ' ' : data.charAt(0));
                    final char indicator2 = (data.charAt(1) == '\\' ? ' ' : data.charAt(1));

                    if (!this.isValidIndicator(indicator1) || !this.isValidIndicator(indicator2)) {
                        throw new MarcException("Wrong indicator format. It has to be a number or a space");
                    }

                    field = this.factory.newDataField(tag, indicator1, indicator2);

                    final List<String> subs = Arrays.asList(data.substring(3).split("\\$"));

                    for (String sub : subs) {
                        String subData;
                        subData = Mrk8TranslationTable.fromMrk8(sub.substring(1));
                        if (!isUTF8 && toUTF8) {
                            if (Marc8ToUTF8 == null) {
                                Marc8ToUTF8 = new AnselToUnicode();
                            }
                            subData = Marc8ToUTF8.convert(subData);
                        }
                        final Subfield subfield = this.factory.newSubfield(sub.charAt(0), subData);
                        ((DataField) field).addSubfield(subfield);
                    }
                }
                record.addVariableField(field);
            }
        }
        return record;
    }

    protected boolean isValidIndicator(final char indicator) {
        return (indicator == ' ' || (indicator >= '0' && indicator <= '9'));
    }

    protected Leader getLeader(final String substring) {
        final Leader leader = this.factory.newLeader();
        leader.unmarshal(substring);
        return leader;
    }

    protected String unescapeFieldValue(String fieldValue)
    {
        StringBuilder sb = new StringBuilder();
        for (char c : fieldValue.toCharArray()) {
            if (c == '\\') sb.append(' ');
            else           sb.append(c);
        }
        return(sb.toString());
    }

    protected boolean isControlField(final String tag) {
        // can probably be replaced with (Integer.parseInt(tag)<10)
        return ((tag.length() == 3) && tag.startsWith("00") &&
                (tag.charAt(2) >= '0') && (tag.charAt(2) <= '9'));
    }
}
