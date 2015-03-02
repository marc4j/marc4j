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

import org.marc4j.marc.DataField;
import org.marc4j.marc.IllegalAddException;
import org.marc4j.marc.Subfield;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Represents a data field in a MARC record.
 * 
 * @author Bas Peters
 * @author Tod Olson, University of Chicago
 */
public class DataFieldImpl extends VariableFieldImpl implements DataField {

    private Long id;
    
    private char ind1;

    private char ind2;

    private List<Subfield> subfields = new ArrayList<Subfield>();

    /**
     * Creates a new <code>DataField</code>.
     */
    public DataFieldImpl() {
    }

    /**
     * Creates a new <code>DataField</code> and sets the tag name and the
     * first and second indicator.
     * 
     * @param tag
     *            the tag name
     * @param ind1
     *            the first indicator
     * @param ind2
     *            the second indicator
     */
    public DataFieldImpl(String tag, char ind1, char ind2) {
        super(tag);
        this.setIndicator1(ind1);
        this.setIndicator2(ind2);
    }

    /**
     * Sets the first indicator.
     * 
     * @param ind1
     *            the first indicator
     */
    public void setIndicator1(char ind1) {
        this.ind1 = ind1;
    }

    /**
     * Returns the first indicator
     * 
     * @return char - the first indicator
     */
    public char getIndicator1() {
        return ind1;
    }

    /**
     * Sets the second indicator.
     * 
     * @param ind2
     *            the second indicator
     */
    public void setIndicator2(char ind2) {
        this.ind2 = ind2;
    }

    /**
     * Returns the second indicator
     * 
     * @return char - the second indicator
     */
    public char getIndicator2() {
        return ind2;
    }

    /**
     * Adds a <code>Subfield</code>.
     * 
     * @param subfield
     *            the <code>Subfield</code> object
     * @throws IllegalAddException
     *             when the parameter is not a <code>Subfield</code> instance
     */
    public void addSubfield(Subfield subfield) {
        if (subfield instanceof SubfieldImpl)
            subfields.add(subfield);
        else
            throw new IllegalAddException("Subfield");
    }

    /**
     * Inserts a <code>Subfield</code> at the specified position.
     * 
     * @param index
     *            the position within the list
     * @param subfield
     *            the <code>Subfield</code> object
     * @throws IllegalAddException
     *             when the parameter is not a <code>Subfield</code> instance
     */
    public void addSubfield(int index, Subfield subfield) {
        subfields.add(index, subfield);
    }

    /**
     * Removes a <code>Subfield</code>.
     */
    public void removeSubfield(Subfield subfield) {
        subfields.remove(subfield);
    }

    /**
     * Returns the list of <code>Subfield</code> objects.
     * 
     * @return List - the list of <code>Subfield</code> objects
     */
    public List<Subfield> getSubfields() {
        return subfields;
    }

    public List<Subfield> getSubfields(char code) {
        List<Subfield> retSubfields = new ArrayList<Subfield>();
        for (Subfield sf : subfields)
        {
            if (sf.getCode() == code)
                retSubfields.add(sf);
        }
        return retSubfields;
    }

    @Override
    public List<Subfield> getSubfields(String sfSpec) {
        List<Subfield> sfData = new ArrayList<Subfield>();
        if (sfSpec == null || sfSpec.length() == 0) {
            for (Subfield sf : this.getSubfields()) {
                sfData.add(sf);
            }
        } else if (sfSpec.contains("[")) {
            // brackets means [a-cm-z] sort of pattern
            //TODO: Pattern may be expensive, possible place for optimization?
            Pattern sfPattern = null;
            try {
                sfPattern = Pattern.compile(sfSpec);
                for (Subfield sf : this.getSubfields()) {
                    Matcher m = sfPattern.matcher("" + sf.getCode());
                    if (m.matches()) {
                        sfData.add(sf);
                    }
                }
            }
            catch (PatternSyntaxException pse) {
                throw new PatternSyntaxException(pse.getDescription()+ " in subfield specification " + sfSpec,
                                                 pse.getPattern(), pse.getIndex());
            }
        } else {
            // otherwise spec is list of subfield codes
            for (Subfield sf : this.getSubfields()) {
                if (sfSpec.contains(String.valueOf(sf.getCode()))) {
                    sfData.add(sf);
                }
            }
        }
        return sfData;
    }

    public String getSubfieldsAsString(String sfSpec) {
        //TODO: after subfield spec is fully developed, optimize to avoid the extra loop and gc from calling getSubfields
        List<Subfield> sfList = this.getSubfields(sfSpec);
        if (sfList.isEmpty()) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        for (Subfield sf : sfList) {
            buf.append(sf.getData());
        }
        return buf.toString();
    }


    public Subfield getSubfield(char code) {
        for (Subfield sf : subfields) 
        {
            if (sf.getCode() == code)
                return sf; 
        }
        return null;
    }

    public boolean find(String pattern) {
        for (Subfield sf : subfields) 
        {
            if (sf.find(pattern))
                return true;
        }
        return false;
    }

    /**
     * Returns a string representation of this data field.
     * 
     * <p>
     * Example:
     * 
     * <pre>
     *    245 10$aSummerland /$cMichael Chabon.
     * </pre>
     * 
     * @return String - a string representation of this data field
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(' ');
        sb.append(getIndicator1());
        sb.append(getIndicator2());
        for (Subfield sf : subfields) 
        {
            sb.append(sf.toString());
        }
        return sb.toString();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

}
