// $Id: Directory.java,v 1.2 2002/07/06 13:40:20 bpeters Exp $
/**
 * Copyright (C) 2002 Bas Peters
 *
 * This file is part of MARC4J
 *
 * MARC4J is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * MARC4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MARC4J; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * As a special exception, if you link this library with other files to
 * produce an executable, this library does not by itself cause the
 * resulting executable to be covered by the GNU General Public License.
 * This exception does not however invalidate any other reasons why the
 * executable file might be covered by the GNU General Public License.
 */
package org.marc4j.marc;

import java.text.DecimalFormat;

/**
 * <p><code>Directory</code> is a helper class to build a directory.  </p>
 * <p>The directory is an index to the location of the variable
 * fields within a record. The directory consists of entries. Each
 * directory entry is 12 characters in length. The structure of each
 * entry according to the MARC standard is as follows:</p>
 * <pre>
 * TAG     LENGTH_OF_FIELD     STARTING_CHARACTER_POSITION
 * 00-02   03-06               07-11
 * </pre>
 * <p>This structure is returned by the {@link #marshal()}
 * method.</p>
 * <p>The starting character position gives the starting position of
 * the variable field to which the entry corresponds relative to
 * the base address of data of the record. A starting character position
 * or length of field of fewer than five digits is right justified and
 * unused positions contain zero's.</p>
 *
 * @author <a href="mailto:mail@bpeters.com">Bas Peters</a> 
 * @version $Revision: 1.2 $
 *
 */
public class Directory {

    private static final char FT = MarcConstants.FT;

    /** The directory. */
    private StringBuffer directory = new StringBuffer();

    /** Number format for the length of field. */
    private DecimalFormat formatLength = new DecimalFormat("0000");

    /** Number format for the starting character position. */
    private DecimalFormat formatStart = new DecimalFormat("00000");

    /** The starting character position. */
    private int start = 0;

    /** The length of field for the previous variable field. */
    private int prev = 0;

    /**
     * <p>Default constructor.</p>
     */
    public Directory() {}

    /**
     * <p>Adds a new entry to the directory.</p>
     *
     * @param tag the tag name
     * @param length the length of field
     */
    public void add(String tag, int length) {
        start = start + prev;
        prev = length;
        directory.append(tag)
	    .append(formatLength.format(length))
	    .append(formatStart.format(start));
    }

    /**
     * <p>Returns a <code>String</code> representation for the directory
     * following the structure of the MARC directory.</p>
     *
     * @return <code>String</code> - the directory
     */
    public String marshal() {
        return directory.toString() + FT;
    }

    /**
     * <p>Returns the length of the serialized form of the directory.</p>
     *
     * @return <code>int</code> - the directory length
     */
    public int getLength() {
        return this.marshal().length();
    }

}
