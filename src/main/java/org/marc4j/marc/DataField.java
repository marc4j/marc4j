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
	 * Returns the list of <code>Subfield</code> objects for the goven
	 * subfield code.
	 * 
	 * @param code
	 *            the subfield code
	 * @return List - the list of <code>Subfield</code> objects
	 */
	public List<Subfield> getSubfields(char code);

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
