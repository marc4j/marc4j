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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.marc4j.marc.DataField;
import org.marc4j.marc.IllegalAddException;
import org.marc4j.marc.Subfield;

/**
 * DataField defines behavior for a data field (tag 010-999).
 * <p>
 * Data fields are variable fields identified by tags beginning with ASCII numeric values other than two zero's. Data
 * fields contain indicators, subfield codes, data and a field terminator.
 *
 * @author Bas Peters
 * @author Tod Olson, University of Chicago
 */
public class DataFieldImpl extends VariableFieldImpl implements DataField {

    /**
     * A <code>serialVersionUID</code> for the class.
     */
    private static final long serialVersionUID = -9010227887211125868L;

    private Long id;

    private char ind1;

    private char ind2;

    private final List<Subfield> subfields = new ArrayList<Subfield>();

    /**
     * Creates a new <code>DataField</code>.
     */
    DataFieldImpl() {}

    /**
     * Creates a new <code>DataField</code> and sets the tag name and the first and second indicator.
     *
     * @param tag The tag name
     * @param ind1 The first indicator
     * @param ind2 The second indicator
     */
    public DataFieldImpl(final String tag, final char ind1, final char ind2) {
        super(tag);
        this.setIndicator1(ind1);
        this.setIndicator2(ind2);
    }

    /**
     * Sets the field's first indicator.
     *
     * @param ind1 The first indicator
     */
    @Override
    public void setIndicator1(final char ind1) {
        this.ind1 = ind1;
    }

    /**
     * Returns the field's first indicator.
     *
     * @return The field's first indicator
     */
    @Override
    public char getIndicator1() {
        return ind1;
    }

    /**
     * Sets the field's second indicator.
     *
     * @param ind2 The field's second indicator
     */
    @Override
    public void setIndicator2(final char ind2) {
        this.ind2 = ind2;
    }

    /**
     * Returns the field's second indicator
     *
     * @return The field's second indicator
     */
    @Override
    public char getIndicator2() {
        return ind2;
    }

    /**
     * Adds a <code>Subfield</code>.
     *
     * @param subfield The <code>Subfield</code> of a <code>DataField</code>
     * @throws IllegalAddException when the parameter is not an instance of <code>SubfieldImpl</code>
     */
    @Override
    public void addSubfield(final Subfield subfield) {
        if (subfield instanceof SubfieldImpl) {
            subfields.add(subfield);
        } else {
            throw new IllegalAddException("Supplied Subfield isn't an instance of SubfieldImpl");
        }
    }

    /**
     * Inserts a <code>Subfield</code> at the specified position.
     *
     * @param index The subfield's position within the list
     * @param subfield The <code>Subfield</code> object
     * @throws IllegalAddException when supplied Subfield isn't an instance of <code>SubfieldImpl</code>
     */
    @Override
    public void addSubfield(final int index, final Subfield subfield) {
        subfields.add(index, subfield);
    }

    /**
     * Removes a <code>Subfield</code> from the field.
     *
     * @param subfield The subfield to remove from the field.
     */
    @Override
    public void removeSubfield(final Subfield subfield) {
        subfields.remove(subfield);
    }

    /**
     * Returns the list of <code>Subfield</code> objects.
     *
     * @return The list of <code>Subfield</code> objects
     */
    @Override
    public List<Subfield> getSubfields() {
        return subfields;
    }

    /**
     * Returns the {@link Subfield}s with the supplied <code>char</code> code.
     *
     * @param code A subfield code
     * @return A {@link List} of {@link Subfield}s
     */
    @Override
    public List<Subfield> getSubfields(final char code) {
        final List<Subfield> result = new ArrayList<Subfield>();

        for (final Subfield sf : subfields) {
            if (sf.getCode() == code) {
                result.add(sf);
            }
        }

        return result;
    }

    /**
     * Returns a list of subfields from a supplied pattern. The pattern can either be a string of subfield codes or a
     * regular expression to compare subfield codes against. The inclusion of brackets indicates the pattern should be
     * parsed as a regular expression.
     * 
     * @param sfSpec a list or a regex of subfield codes to return 
     */
    @Override
    public List<Subfield> getSubfields(final String sfSpec) {
        final List<Subfield> sfData = new ArrayList<Subfield>();

        if (sfSpec == null || sfSpec.length() == 0) {
            for (final Subfield sf : this.getSubfields()) {
                sfData.add(sf);
            }
        } else if (sfSpec.contains("[")) {
            // Brackets indicate a pattern
            Pattern sfPattern = null;
            try {
                sfPattern = Pattern.compile(sfSpec);
                for (final Subfield sf : this.getSubfields()) {
                    final Matcher m = sfPattern.matcher("" + sf.getCode());

                    if (m.matches()) {
                        sfData.add(sf);
                    }
                }
            } catch (final PatternSyntaxException details) {
                throw new PatternSyntaxException(
                        details.getDescription() + " in subfield pattern " + sfSpec, details
                                .getPattern(), details.getIndex());
            }
        } else {
            // otherwise spec is a list of subfield codes
            for (final Subfield sf : this.getSubfields()) {
                if (sfSpec.contains(String.valueOf(sf.getCode()))) {
                    sfData.add(sf);
                }
            }
        }

        return sfData;
    }

    @Override
    public String getSubfieldsAsString(final String sfSpec) {
        // TODO: after subfield spec is fully developed, optimize to avoid the
        // extra loop and gc from calling getSubfields
        final List<Subfield> sfList = this.getSubfields(sfSpec);
        if (sfList.isEmpty()) {
            return null;
        }
        final StringBuilder buf = new StringBuilder();
        for (final Subfield sf : sfList) {
            buf.append(sf.getData());
        }
        return buf.toString();
    }

    /**
     * Returns the first {@link Subfield} matching the supplied <code>char</code> code.
     *
     * @param code A code for the subfield to be returned
     */
    @Override
    public Subfield getSubfield(final char code) {
        for (final Subfield sf : subfields) {
            if (sf.getCode() == code) {
                return sf;
            }
        }

        return null;
    }

    /**
     * Returns <code>true</code> if a match is found for the supplied regular expression pattern; else,
     * <code>false</code>.
     *
     * @param pattern A regular expression pattern to find in the subfields
     */
    @Override
    public boolean find(final String pattern) {
        for (final Subfield sf : subfields) {
            if (sf.find(pattern)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a string representation of this data field.
     * <p>
     * Example:
     *
     * <pre>
     *    245 10$aSummerland /$cMichael Chabon.
     * </pre>
     *
     * @return A string representation of this data field
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(' ');
        sb.append(getIndicator1());
        sb.append(getIndicator2());

        for (final Subfield sf : subfields) {
            sb.append(sf.toString());
        }
        return sb.toString();
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }

}
