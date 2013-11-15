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
package org.marc4j;

import org.marc4j.converter.CharConverter;
import org.marc4j.marc.Record;

/**
 * Implement this interface to provide a writer for <code>Record</code>
 * objects.
 * 
 * <p>See the {@link org.marc4j.marc} package Javadoc for more information
 * about the {@link org.marc4j.marc.Record} object model.</p>
 * 
 * @author Bas Peters
 *  
 */
public interface MarcWriter {

  /**
   * Writes a single <code>Record</code> to the output stream.
   * 
   * @param record
   *          the Record object
   */
  public void write(Record record);

  /**
   * Sets the character converter.
   * 
   * @param converter
   *          the character converter
   */
  public void setConverter(CharConverter converter);

  /**
   * Returns the character converter.
   * 
   * @return CharConverter the character converter
   */
  public CharConverter getConverter();

  /**
   * Closes the writer.
   *  
   */
  public void close();

}
