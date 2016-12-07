/**
 * Copyright (C) 2005 Bas Peters
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
 *
 */

package org.marc4j.converter;

/**
 * Extend this class to create a character converter.
 * 
 * @author Bas Peters
 */
public abstract class CharConverter {

    /**
     * The method that needs to be implemented in a subclass to create a
     * CharConverter. Receives a data element extracted from a record as a array
     * of characters, and converts that data and returns the result as a
     * <code>String</code> object.
     * 
     * @param dataElement the data to convert
     * @return String the conversion result
     */
    public abstract String convert(char[] dataElement);

    /**
     * Alternate method for performing a character conversion. Receives the
     * incoming as a byte array, converts the bytes to characters, and calls the
     * above convert method which must be implemented in the subclass.
     * 
     * @param dataElement the data to convert
     * @return String the conversion result
     */
    final public String convert(final byte[] dataElement) {
        final char cData[] = new char[dataElement.length];
        for (int i = 0; i < dataElement.length; i++) {
            final byte b = dataElement[i];
            cData[i] = (char) (b >= 0 ? b : 256 + b);
        }
        return convert(cData);
    }

    /**
     * Alternate method for performing a character conversion. Receives the
     * incoming as a String, converts the String to a character array, and calls
     * the above convert method which must be implemented in the subclass.
     * 
     * @param dataElement the data to convert
     * @return String the conversion result
     */
    final public String convert(final String dataElement) {
        char[] data = null;
        data = dataElement.toCharArray();
        return convert(data);
    }

    /**
     * Should return true if the CharConverter outputs Unicode encoded
     * characters
     * 
     * @return boolean whether the CharConverter returns Unicode encoded
     *         characters
     */
    public boolean outputsUnicode() {
        return (false);
    }

}
