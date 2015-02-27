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
package org.marc4j.marc;

import java.util.List;

/**
 * Represents a data field in a MARC record.
 * 
 * <h3>Subfield Selectors</h3>
 * <p>
 * The {@code getSubfields*} methods take a subfield selector string. 
 * This string can be a simple list of subfield letters,
 * or a bracket-enslosed character class expression, as in regular expressions.
 * <p>
 * For example:
 * <ul>
 * <li>
 * {@code abc} well match any of subfields {@literal a}, {@literal b} or {@literal c}.
 * <li>
 * {@code [a-cf-g]} will match any of {@literal a-c} or {@literal f-g}, but not {@literal d}.
 * </ul>
 * <p>
 * 
 * @author Bas Peters
 */
public interface DataField extends VariableField {

	/**
	 * Returns the first indicator
	 * 
	 * @return char - the first indicator
	 */
	public char getIndicator1();

	/**
	 * Sets the first indicator.
	 * 
	 * @param ind1
	 *            the first indicator
	 */
	public void setIndicator1(char ind1);

	/**
	 * Returns the second indicator
	 * 
	 * @return char - the second indicator
	 */
	public char getIndicator2();

	/**
	 * Sets the second indicator.
	 * 
	 * @param ind2
	 *            the second indicator
	 */
	public void setIndicator2(char ind2);

	/**
	 * Returns the list of <code>Subfield</code> objects.
	 * 
	 * @return List - the list of <code>Subfield</code> objects
	 */
	public List<Subfield> getSubfields();

        /**
         * Returns the list of <code>Subfield</code> objects for the given
         * subfield code.
         * 
         * @param code - the subfield code
         * @return List - the list of <code>Subfield</code> objects
         */
        public List<Subfield> getSubfields(char code);

        /**
         * Returns the list of <code>Subfield</code> objects that match the
         * subfield spec.
         * <p>
         * The subfield spec can either specify each subfield needed, thusly:    abcfghnp
         *    or the subfield spec can be enclosed in square brackets, and it 
         *    will be treated as a regular expression character class
         *    and all subfields matching the pattern will be returned, thusly:    [a-cf-hnp]
         *    Since it uses the regex.Pattern other valid forms will work:
         *      [^h]  return all subfields except those with code 'h'
         *      [a-z&&[^bc]] return any (lowercase) alphabetic subfield, except for 'b' or 'c'
         * Note for both forms described above the order of the subfields in the specification 
         *    is irrelevant, all subfields that match will be returned in the order that they 
         *    occur in the DataField.
         * Note also that if an invalid regular expression character class is given (such as [c-a] )
         *    this routine will quietly fail, and return no subfields whatsoever rather than 
         *    throwing an exception. 
         * 
         * @param sfSpec - the subfield spec
         * @return List - the list of <code>Subfield</code> objects
         */
        public List<Subfield> getSubfields(String sfSpec);
        
        /**
         * Get the data from the specified subfields.
         * This is essentially a map/reduce over the subfields.
         * <p>
         * The subfield spec can either specify each subfield needed, thusly:    abcfghnp
         *    or the subfield spec can be enclosed in square brackets, and it 
         *    will be treated as a regular expression character class
         *    and all subfields matching the pattern will be returned, thusly:    [a-cf-hnp]
         *    Since it uses the regex.Pattern other valid forms will work:
         *      [^h]  return all subfields except those with code 'h'
         *      [a-z&&[^bc]] return any (lowercase) alphabetic subfield, except for 'b' or 'c'
         * Note for both forms described above the order of the subfields in the specification 
         *    is irrelevant, all subfields that match will be returned in the order that they 
         *    occur in the DataField.
         * Note also that if an invalid regular expression character class is given (such as [c-a] )
         *    this routine will quietly fail, and return no subfields whatsoever rather than 
         *    throwing an exception. 
         * 
         * @param sfSpec - subfield spec
         * @return requested subfield data, concatenated together as a single string, or null if no subfields are matched
         */
        public String getSubfieldsAsString(String sfSpec);

	/**
	 * Returns the first <code>Subfield</code> with the given code.
	 * 
	 * @param code
	 *            the subfield code
	 * @return Subfield - the subfield object or null if no subfield is found
	 */
	public Subfield getSubfield(char code);

	/**
	 * Adds a <code>Subfield</code>.
	 * 
	 * @param subfield
	 *            the <code>Subfield</code> object
	 * @throws IllegalAddException
	 *             when the parameter is not a <code>Subfield</code> instance
	 */
	public void addSubfield(Subfield subfield);

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
	public void addSubfield(int index, Subfield subfield);

	/**
	 * Removes a <code>Subfield</code>.
	 */
	@SuppressWarnings("UnusedDeclaration")
    public void removeSubfield(Subfield subfield);

}
