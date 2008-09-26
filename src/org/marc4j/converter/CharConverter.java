//$Id: CharConverter.java,v 1.2 2008/09/26 21:18:16 haschart Exp $
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
 * Implement this class to create a character converter.
 * 
 * @author Bas Peters
 * @version $Revision: 1.2 $
 */
public abstract class CharConverter {

  /**
   * Converts the dataElement and returns the result as a <code>String</code>
   * object.
   * 
   * @param dataElement the data to convert
   * @return String the conversion result
   */
    public abstract String convert(char[] dataElement);
    
    public String convert(byte[] data) 
    {
        char cData[] = new char[data.length];
        for (int i = 0; i < data.length; i++)
        {
            byte b = data[i];
            cData[i] =  (char)(b >= 0 ? b : 256 + b);
        }
        return convert(cData);
    }

    public String convert(String dataElement) 
    {
        char[] data = null;
        data = dataElement.toCharArray();
        return (convert(data));
    }
    

}