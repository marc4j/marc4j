// $Id: DataFieldImpl.java,v 1.2 2005/07/18 04:56:18 bpeters Exp $
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
import java.util.Iterator;
import java.util.List;

import org.marc4j.marc.DataField;
import org.marc4j.marc.IllegalAddException;
import org.marc4j.marc.Subfield;

/**
 * Represents a data field in a MARC record.
 * 
 * @author Bas Peters
 * @version $Revision: 1.2 $
 */
public class DataFieldImpl extends VariableFieldImpl implements DataField {

  private char ind1;

  private char ind2;

  private List subfields;

  /**
   * Creates a new <code>DataField</code>.
   */
  public DataFieldImpl() {
    subfields = new ArrayList();
  }

  /**
   * Creates a new <code>DataField</code> and sets the tag name and the first
   * and second indicator.
   * 
   * @param tag
   *          the tag name
   * @param ind1
   *          the first indicator
   * @param ind2
   *          the second indicator
   */
  public DataFieldImpl(String tag, char ind1, char ind2) {
    this();
    this.setTag(tag);
    this.setIndicator1(ind1);
    this.setIndicator2(ind2);
  }

  /**
   * Sets the first indicator.
   * 
   * @param ind1
   *          the first indicator
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
   *          the second indicator
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
   *          the <code>Subfield</code> object
   * @throws IllegalAddException
   *           when the parameter is not a <code>Subfield</code> instance
   */
  public void addSubfield(Subfield subfield) {
    if (subfield instanceof SubfieldImpl)
      subfields.add(subfield);
    else
      throw new IllegalAddException("Subfield");
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
  public List getSubfields() {
    return subfields;
  }

  public List getSubfields(char code) {
    List retSubfields = new ArrayList();
    Iterator i = subfields.iterator();
    while (i.hasNext()) {
      Subfield sf = (Subfield) i.next();
      if (sf.getCode() == code)
        retSubfields.add(sf);
    }
    return retSubfields;
  }

  public Subfield getSubfield(char code) {
    Iterator i = subfields.iterator();
    while (i.hasNext()) {
      Subfield sf = (Subfield) i.next();
      if (sf.getCode() == code)
        return sf;
    }
    return null;
  }

}