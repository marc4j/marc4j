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

import java.text.DecimalFormat;

import org.marc4j.marc.Leader;

/**
 * Represents a record label in a MARC record.
 *
 * @author Bas Peters
 */
public class LeaderImpl implements Leader {

    /**
     * A <code>serialVersionUID</code> for the class.
     */
    private static final long serialVersionUID = 8300445263515491860L;

    private Long id;

    /** The logical record length (Position 0-4). */
    private int recordLength;

    /** The record status (Position 5). */
    private char recordStatus;

    /** Type of record (Position 6). */
    private char typeOfRecord;

    /** Implementation defined (Position 7-8). */
    private char[] implDefined1;

    /** Character coding scheme (Position 9). */
    private char charCodingScheme;

    /** The indicator count (Position 10). */
    private int indicatorCount;

    /** The subfield code length (Position 11). */
    private int subfieldCodeLength;

    /** The base address of data (Position 12-16). */
    private int baseAddressOfData;

    /** Implementation defined (Position 17-18) */
    private char[] implDefined2;

    /** Entry map (Position 19-23). */
    private char[] entryMap;

    /**
     * Default constructor.
     */
    public LeaderImpl() {
    }

    /**
     * Creates a new leader from a String object.
     * 
     * @param ldr the leader string value
     */
    public LeaderImpl(final String ldr) {
        unmarshal(ldr);
    }

    /**
     * Sets the logical record length (positions 00-04).
     * 
     * @param recordLength integer representing the record length
     */
    @Override
    public void setRecordLength(final int recordLength) {
        this.recordLength = recordLength;
    }

    /**
     * Sets the record status (position 05).
     * 
     * @param recordStatus character representing the record status
     */
    @Override
    public void setRecordStatus(final char recordStatus) {
        this.recordStatus = recordStatus;
    }

    /**
     * Sets the type of record (position 06).
     * 
     * @param typeOfRecord character representing the type of record
     */
    @Override
    public void setTypeOfRecord(final char typeOfRecord) {
        this.typeOfRecord = typeOfRecord;
    }

    /**
     * Sets implementation defined values (position 07-08).
     * 
     * @param implDefined1 character array representing the implementation
     *        defined data
     */
    @Override
    public void setImplDefined1(final char[] implDefined1) {
        this.implDefined1 = implDefined1;
    }

    /**
     * Sets the character encoding scheme (position 09).
     * 
     * @param charCodingScheme character representing the character encoding
     */
    @Override
    public void setCharCodingScheme(final char charCodingScheme) {
        this.charCodingScheme = charCodingScheme;
    }

    /**
     * Sets the indicator count (position 10).
     * 
     * @param indicatorCount integer representing the number of indicators
     *        present in a data field
     */
    @Override
    public void setIndicatorCount(final int indicatorCount) {
        this.indicatorCount = indicatorCount;
    }

    /**
     * Sets the subfield code length (position 11).
     * 
     * @param subfieldCodeLength integer representing the subfield code length
     */
    @Override
    public void setSubfieldCodeLength(final int subfieldCodeLength) {
        this.subfieldCodeLength = subfieldCodeLength;
    }

    /**
     * Sets the base address of data (positions 12-16).
     * 
     * @param baseAddressOfData integer representing the base address of data
     */
    @Override
    public void setBaseAddressOfData(final int baseAddressOfData) {
        this.baseAddressOfData = baseAddressOfData;
    }

    /**
     * Sets implementation defined values (positions 17-19).
     * 
     * @param implDefined2 character array representing the implementation
     *        defined data
     */
    @Override
    public void setImplDefined2(final char[] implDefined2) {
        this.implDefined2 = implDefined2;
    }

    /**
     * Sets the entry map (positions 20-23).
     * 
     * @param entryMap character array representing the entry map
     */
    @Override
    public void setEntryMap(final char[] entryMap) {
        this.entryMap = entryMap;
    }

    /**
     * Returns the logical record length (positions 00-04).
     * 
     * @return <code>int</code>- the record length
     */
    @Override
    public int getRecordLength() {
        return recordLength;
    }

    /**
     * Returns the record status (positions 05).
     * 
     * @return <code>char</code>- the record status
     */
    @Override
    public char getRecordStatus() {
        return recordStatus;
    }

    /**
     * Returns the record type (position 06).
     * 
     * @return <code>char</code>- the record type
     */
    @Override
    public char getTypeOfRecord() {
        return typeOfRecord;
    }

    /**
     * Returns implementation defined values (positions 07-08).
     * 
     * @return <code>char[]</code>- implementation defined values
     */
    @Override
    public char[] getImplDefined1() {
        return implDefined1;
    }

    /**
     * Returns the character coding scheme (position 09).
     * 
     * @return <code>char</code>- the character coding scheme
     */
    @Override
    public char getCharCodingScheme() {
        return charCodingScheme;
    }

    /**
     * Returns the indicator count (positions 10).
     * 
     * @return <code>int</code>- the indicator count
     */
    @Override
    public int getIndicatorCount() {
        return indicatorCount;
    }

    /**
     * Returns the subfield code length (position 11).
     * 
     * @return <code>int</code>- the subfield code length
     */
    @Override
    public int getSubfieldCodeLength() {
        return subfieldCodeLength;
    }

    /**
     * Returns the base address of data (positions 12-16).
     * 
     * @return <code>int</code>- the base address of data
     */
    @Override
    public int getBaseAddressOfData() {
        return baseAddressOfData;
    }

    /**
     * Returns implementation defined values (positions 17-19).
     * 
     * @return <code>char[]</code>- implementation defined values
     */
    @Override
    public char[] getImplDefined2() {
        return implDefined2;
    }

    /**
     * Returns the entry map (positions 20-23).
     * 
     * @return <code>char[]</code>- the entry map
     */
    @Override
    public char[] getEntryMap() {
        return entryMap;
    }

    /**
     * <p>
     * Creates a leader object from a string object.
     * </p>
     * <p>
     * Indicator count and subfield code length are defaulted to 2 if they are
     * not integer values.
     * </p>
     * 
     * @param ldr the leader
     */
    @Override
    public void unmarshal(final String ldr) {
        try {
            String s;
            s = ldr.substring(0, 5);
            if (isInteger(s)) {
                setRecordLength(Integer.parseInt(s));
            } else {
                setRecordLength(0);
            }
            setRecordStatus(ldr.charAt(5));
            setTypeOfRecord(ldr.charAt(6));
            setImplDefined1(ldr.substring(7, 9).toCharArray());
            setCharCodingScheme(ldr.charAt(9));
            s = String.valueOf(ldr.charAt(10));
            if (isInteger(s)) {
                setIndicatorCount(Integer.parseInt(s));
            } else {
                setIndicatorCount(2);
            }
            s = String.valueOf(ldr.charAt(11));
            if (isInteger(s)) {
                setSubfieldCodeLength(Integer.parseInt(s));
            } else {
                setSubfieldCodeLength(2);
            }
            s = ldr.substring(12, 17);
            if (isInteger(s)) {
                setBaseAddressOfData(Integer.parseInt(s));
            } else {
                setBaseAddressOfData(0);
            }
            setImplDefined2(ldr.substring(17, 20).toCharArray());
            setEntryMap(ldr.substring(20, 24).toCharArray());
        } catch (final NumberFormatException e) {
            throw new RuntimeException("Unable to parse leader", e);
        }
    }

    /**
     * Creates a string object from this leader object.
     * 
     * @return String - the string object from this leader object
     */
    @Override
    public String marshal() {
        return this.toString();
    }

    /**
     * Returns a string representation of this leader.
     * <p>
     * Example:
     * 
     * <pre>
     *  00714cam a2200205 a 4500
     * </pre>
     */
    @Override
    public String toString() {
        return new StringBuffer().append(format5.format(getRecordLength()))
                .append(getRecordStatus()).append(getTypeOfRecord()).append(
                        getImplDefined1()).append(getCharCodingScheme())
                .append(getIndicatorCount()).append(getSubfieldCodeLength())
                .append(format5.format(getBaseAddressOfData())).append(
                        getImplDefined2()).append(getEntryMap()).toString();
    }

    private boolean isInteger(final String value) {
        final int len = value.length();
        if (len == 0) {
            return false;
        }
        int i = 0;
        do {
            switch (value.charAt(i)) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
                default:
                    return false;
            }
        } while (++i < len);
        return true;
    }

    private static DecimalFormat format5 = new org.marc4j.util.CustomDecimalFormat(5);

    /**
     * Sets the ID for the leader.
     */
    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    /**
     * Gets the ID of the leader.
     */
    @Override
    public Long getId() {
        return id;
    }

}
