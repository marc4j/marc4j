// $Id: IllegalDataElementException.java,v 1.1 2003/01/10 09:34:03 bpeters Exp $
/**
 * Copyright (C) 2002 Bas Peters
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

/**
 * <p><code>IllegalDataElementException</code> is thrown when a data element
 * contains invalid characters, like a field or record terminator 
 * or a delimiter.</p>
 *
 * @author <a href="mailto:mail@bpeters.com">Bas Peters</a> 
 * @version $Revision: 1.1 $
 *
 */
public class IllegalDataElementException 
    extends IllegalArgumentException {

    /**
     * <p>Creates a new <code>IllegalDataElementException</code>.</p>
     */
    public IllegalDataElementException() {
	super();
  }

    /**
     * <p>Creates a new <code>IllegalDataElementException</code>.</p>
     *
     * @param reason the reason why the exception is thrown
     */
    public IllegalDataElementException(String reason) {
	super(reason);
  }

}
