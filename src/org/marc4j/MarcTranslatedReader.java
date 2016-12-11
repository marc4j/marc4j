
package org.marc4j;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.List;

import org.marc4j.converter.CharConverter;
import org.marc4j.converter.impl.AnselToUnicode;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

/**
 * @author Robert Haschart
 */
public class MarcTranslatedReader implements MarcReader {

    MarcReader reader;

    CharConverter convert;

    Form unicodeNormalize = null;

    /**
     * Creates a MARC translated reader that can normalize Unicode.
     * 
     * @param r - the MarcReader that this object should decorate
     * @param unicodeNormalizeBool - true to apply the Unicode NFC normalization to data
     */
    public MarcTranslatedReader(final MarcReader r, final boolean unicodeNormalizeBool) {
        reader = r;
        convert = new AnselToUnicode();

        if (unicodeNormalizeBool) {
            this.unicodeNormalize = Normalizer.Form.NFC;
        }
    }

    /**
     * Creates a MARC translated reader using the normalizer represented by the
     * supplied string.
     * 
     * @param r - the MarcReader that this object should decorate
     * @param unicodeNormalizeStr - a string specifying which form of Unicode Normalization to use
     *        valid values are "KC"  "KD"  "C"  "D" any other value does no normalization
     */
    public MarcTranslatedReader(final MarcReader r, final String unicodeNormalizeStr) {
        reader = r;
        convert = new AnselToUnicode();

        if (unicodeNormalizeStr.equals("KC")) {
            unicodeNormalize = Normalizer.Form.NFKC;
        } else if (unicodeNormalizeStr.equals("KD")) {
            unicodeNormalize = Normalizer.Form.NFKD;
        } else if (unicodeNormalizeStr.equals("C")) {
            unicodeNormalize = Normalizer.Form.NFC;
        } else if (unicodeNormalizeStr.equals("D")) {
            unicodeNormalize = Normalizer.Form.NFD;
        } else {
            unicodeNormalize = null;
        }
    }

    /**
     * Returns <code>true</code> if the reader has another {@link Record}.
     */
    @Override
    public boolean hasNext() {
        return reader.hasNext();
    }

    /**
     * Returns the next {@link Record}.
     */
    @Override
    public Record next() {
        final Record rec = reader.next();
        final Leader l = rec.getLeader();
        boolean is_utf_8 = false;

        if (l.getCharCodingScheme() == 'a') {
            is_utf_8 = true;
        }

        if (is_utf_8 && unicodeNormalize == null) {
            return (rec);
        }

        final List<VariableField> fields = rec.getVariableFields();

        for (final VariableField f : fields) {
            if (!(f instanceof DataField)) {
                continue;
            }

            final DataField field = (DataField) f;
            final List<Subfield> subfields = field.getSubfields();

            for (final Subfield sf : subfields) {
                final String oldData = sf.getData();
                String newData = oldData;

                if (!is_utf_8) {
                    newData = convert.convert(newData);
                }

                if (unicodeNormalize != null) {
                    newData = Normalizer.normalize(newData, unicodeNormalize);
                }

                if (!oldData.equals(newData)) {
                    sf.setData(newData);
                }
            }
        }

        l.setCharCodingScheme('a');
        rec.setLeader(l);

        return rec;
    }

}
