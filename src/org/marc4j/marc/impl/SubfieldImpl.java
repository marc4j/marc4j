// $Id: SubfieldImpl.java,v 1.1 2005/05/04 10:06:47 bpeters Exp $
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

import org.marc4j.marc.Subfield;

/**
 * Represents a subfield in a MARC record.
 * 
 * @author Bas Peters
 * @version $Revision: 1.1 $
 */
public class SubfieldImpl implements Subfield {

  private char code;

  private String data;

  /**
   * Creates a new <code>Subfield</code>.
   */
  public SubfieldImpl() {
  }

  /**
   * Creates a new <code>Subfield</code> and sets the data element identifier.
   * 
   * @param code
   *          the data element identifier
   */
  public SubfieldImpl(char code) {
    this.setCode(code);
  }

  /**
   * Creates a new <code>Subfield</code> and sets the data element identifier
   * and the data element.
   * 
   * @param code
   *          the data element identifier
   * @param data
   *          the data element
   */
  public SubfieldImpl(char code, String data) {
    this.setCode(code);
    this.setData(data);
  }

  /**
   * Sets the data element identifier.
   * 
   * @param code
   *          the data element identifier
   */
  public void setCode(char code) {
    this.code = code;
  }

  /**
   * Returns the data element identifier.
   * 
   * @return char - the data element identifier
   */
  public char getCode() {
    return code;
  }

  /**
   * Sets the data element.
   * 
   * @param data
   *          the data element
   */
  public void setData(String data) {
    this.data = data;
  }

  /**
   * Returns the data element.
   * 
   * @return String - the data element
   */
  public String getData() {
    return data;
  }

}