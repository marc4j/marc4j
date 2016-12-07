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

package org.marc4j.marc.impl;

import org.marc4j.MarcException;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

/**
 * Factory for creating MARC record objects.
 * 
 * @author Bas Peters
 */
public class MarcFactoryImpl extends MarcFactory {

    /**
     * Default constructor.
     */
    public MarcFactoryImpl() {
    }

    /**
     * Returns a new control field instance.
     * 
     * @return ControlField
     */
    @Override
    public ControlField newControlField() {
        return new ControlFieldImpl();
    }

    /**
     * Creates a new control field with the given tag and returns the instance.
     * 
     * @return ControlField
     */
    @Override
    public ControlField newControlField(final String tag) {
        return new ControlFieldImpl(tag);
    }

    /**
     * Creates a new control field with the given tag and data and returns the
     * instance.
     * 
     * @return ControlField
     */
    @Override
    public ControlField newControlField(final String tag, final String data) {
        return new ControlFieldImpl(tag, data);
    }

    /**
     * Returns a new data field instance.
     * 
     * @return DataField
     */
    @Override
    public DataField newDataField() {
        return new DataFieldImpl();
    }

    /**
     * Creates a new data field with the given tag and indicators and returns
     * the instance.
     * 
     * @return DataField
     */
    @Override
    public DataField newDataField(final String tag, final char ind1, final char ind2) {
        return new DataFieldImpl(tag, ind1, ind2);
    }

    /**
     * Creates a new data field with the given tag and indicators and subfields
     * and returns the instance.
     * 
     * @return DataField
     */
    @Override
    public DataField newDataField(final String tag, final char ind1, final char ind2,
            final String... subfieldCodesAndData) {
        final DataField df = new DataFieldImpl(tag, ind1, ind2);
        if (subfieldCodesAndData.length % 2 == 1) {
            throw new MarcException(
                    "Error: must provide even number of parameters for subfields: code, data, code, data, ...");
        }
        for (int i = 0; i < subfieldCodesAndData.length; i += 2) {
            if (subfieldCodesAndData[i].length() != 1) {
                throw new MarcException("Error: subfieldCode must be a single character");
            }
            final Subfield sf = newSubfield(subfieldCodesAndData[i].charAt(0),
                    subfieldCodesAndData[i + 1]);
            df.addSubfield(sf);
        }
        return (df);
    }

    /**
     * Returns a new leader instance.
     * 
     * @return Leader
     */
    @Override
    public Leader newLeader() {
        return new LeaderImpl();
    }

    /**
     * Creates a new leader with the given <code>String</code> object.
     * 
     * @return Leader
     */
    @Override
    public Leader newLeader(final String ldr) {
        return new LeaderImpl(ldr);
    }

    /**
     * Returns a new record instance with a default leader.
     * 
     * @return Record
     */
    @Override
    public Record newRecord() {
        return newRecord(new LeaderImpl("00000nam a2200000 a 4500"));
    }

    /**
     * Returns a new subfield instance.
     * 
     * @return Leader
     */
    @Override
    public Subfield newSubfield() {
        return new SubfieldImpl();
    }

    /**
     * Creates a new subfield with the given identifier.
     * 
     * @return Subfield
     */
    @Override
    public Subfield newSubfield(final char code) {
        return new SubfieldImpl(code);
    }

    /**
     * Creates a new subfield with the given identifier and data.
     * 
     * @return Subfield
     */
    @Override
    public Subfield newSubfield(final char code, final String data) {
        return new SubfieldImpl(code, data);
    }

    /**
     * Returns a new {@link Record} with the supplied {@link Leader}.
     */
    @Override
    public Record newRecord(final Leader leader) {
        final Record record = new RecordImpl();
        record.setLeader(leader);
        return record;
    }

    /**
     * Returns a new {@link Record} with a {@link Leader} from the supplied
     * string.
     */
    @Override
    public Record newRecord(final String leader) {
        return newRecord(new LeaderImpl(leader));
    }

}
