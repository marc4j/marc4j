
package org.marc4j;

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

import java.util.List;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;

/**
 * This class provides a capability of filtering {@link Record} objects as they are being read.
 * You can specify one or more fields one of which MUST be present, or one of which must contain the
 * provided character string.  You can also specify one or more fields which must NOT be present,
 * or which must NOT contain the provided character string.
 *
 * This code in this class was only a part of the MarcFilteredReader class from the SolrMarc project.
 * That class was split into two separate classes, this one, and {@link MarcScriptedRecordEditReader}
 *
 * @author Robert Haschart
 */
public class MarcFilteredReader implements MarcReader {

    final String[][] includeRecordIfFieldsPresent;

    final String includeRecordIfFieldContains;

    final String[][] includeRecordIfFieldsMissing;

    final String includeRecordIfFieldDoesntContain;

    Record currentRecord = null;

    final MarcReader reader;

    /**
     *
     * @param reader - the MarcReader to read records that are to be filtered
     * @param ifFieldPresent - a specification of fields the record SHOULD have to be processed
     * @param ifFieldMissing - a specification of fields the record SHOULD NOT have to be processed
     */
    public MarcFilteredReader(final MarcReader reader, final String ifFieldPresent, final String ifFieldMissing) {

        if (ifFieldPresent != null) {
            final String present[] = ifFieldPresent.split("/", 2);
            final String tagPlus[] = present[0].split(":");
            includeRecordIfFieldsPresent = new String[tagPlus.length][2];
            for (int i = 0; i < includeRecordIfFieldsPresent.length; i++) {
                includeRecordIfFieldsPresent[i][0] = tagPlus[i].substring(0, 3);
                includeRecordIfFieldsPresent[i][1] = tagPlus[i].substring(3);
            }
            if (present.length > 1) {
                includeRecordIfFieldContains = present[1];
            } else {
                includeRecordIfFieldContains = null;
            }
        } else {
            includeRecordIfFieldsPresent = null;
            includeRecordIfFieldContains = null;
        }

        if (ifFieldMissing != null) {
            final String missing[] = ifFieldMissing.split("/", 2);
            final String tagPlus[] = missing[0].split(":");
            includeRecordIfFieldsMissing = new String[tagPlus.length][2];
            for (int i = 0; i < includeRecordIfFieldsMissing.length; i++) {
                includeRecordIfFieldsMissing[i][0] = tagPlus[i].substring(0, 3);
                includeRecordIfFieldsMissing[i][1] = tagPlus[i].substring(3);
            }
            if (missing.length > 1) {
                includeRecordIfFieldDoesntContain = missing[1];
            } else {
                includeRecordIfFieldDoesntContain = null;
            }
        } else {
            includeRecordIfFieldsMissing = null;
            includeRecordIfFieldDoesntContain = null;
        }
        this.reader = reader;
    }

    /**
     * Implemented through interface
     * @return Returns true if the iteration has more records, false otherwise
     */
    @Override
    public boolean hasNext() {
        if (currentRecord == null) {
            currentRecord = next();
        }
        return currentRecord != null;
    }

    /**
     * Returns the next marc file in the iteration the meets the filter criteria
     *
     * @return the next marc file in the iteration
     */
    @Override
    public Record next() {

        if (currentRecord != null) {
            final Record tmp = currentRecord;
            currentRecord = null;
            return tmp;
        }

        while (currentRecord == null) {
            if (!reader.hasNext()) {
                return null;
            }
            Record rec = null;

            try {
                rec = reader.next();
            } catch (final MarcException me) {
                throw me;
            }

            if (rec != null && includeRecordIfFieldsPresent != null) {
                for (final String[] tagAndSf : includeRecordIfFieldsPresent) {
                    final List<VariableField> fields = rec.getVariableFields(tagAndSf[0]);

                    for (final VariableField vf : fields) {
                        if (vf instanceof ControlField) {
                            if (includeRecordIfFieldContains == null || ((ControlField) vf)
                                    .getData().contains(includeRecordIfFieldContains)) {
                                currentRecord = rec;
                                break;
                            }
                        } else {
                            if (includeRecordIfFieldContains == null) {
                                currentRecord = rec;
                                break;
                            } else {
                                String subfieldVal = ((DataField) vf).getSubfieldsAsString(tagAndSf[1]);
                                if (subfieldVal != null && subfieldVal.contains(includeRecordIfFieldContains)) {
                                    currentRecord = rec;
                                    break;
                                }
                            }
                        }
                    }
                    if (currentRecord != null) {
                        break;
                    }
                }
            }

            if (rec != null && currentRecord == null && includeRecordIfFieldsMissing != null) {
                boolean useRecord = true;
                for (final String[] tagAndSf : includeRecordIfFieldsMissing) {
                    final List<VariableField> fields = rec.getVariableFields(tagAndSf[0]);

                    for (final VariableField vf : fields) {
                        if (vf instanceof ControlField) {
                            if (includeRecordIfFieldDoesntContain == null || ((ControlField) vf)
                                    .getData().contains(includeRecordIfFieldDoesntContain)) {
                                useRecord = false;
                                break;
                            }
                        } else {
                            if (includeRecordIfFieldDoesntContain == null || ((DataField) vf)
                                    .getSubfieldsAsString(tagAndSf[1]).contains(
                                            includeRecordIfFieldDoesntContain)) {
                                useRecord = false;
                                break;
                            }
                        }
                    }
                    if (useRecord == false) {
                        break;
                    }
                }
                if (useRecord == true) {
                    currentRecord = rec;
                }

            }
            if (rec != null && includeRecordIfFieldsPresent == null && includeRecordIfFieldsMissing == null) {
                currentRecord = rec;
            }
        }
        return currentRecord;
    }
}
