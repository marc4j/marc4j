// $Id: IllegalAddException.java,v 1.3 2002/08/03 12:33:24 bpeters Exp $
/**
 * Copyright (C) 2002 Bas Peters (mail@bpeters.com)
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
 */
package org.marc4j.marc;

/**
 * <p><code>IllegalAddException</code> is thrown when the addition of the
 * supplied object is illegal.  </p>
 *
 * @author <a href="mailto:mail@bpeters.com">Bas Peters</a> 
 * @version $Revision: 1.3 $
 *
 */
public class IllegalAddException extends IllegalArgumentException {

    /**
     * <p>Creates an <code>Exception</code> indicating that the addttion
     * of the supplied object is illegal.</p>
     *
     * @param tag the tag name
     * @param reason the reason why the exception is thrown
     */
    public IllegalAddException(String name, String reason) {
	super(new StringBuffer()
	    .append("The addition of the object ")
	    .append(name)
	    .append(" is not allowed: ")
	    .append(reason)
	    .append(".")
	    .toString());
  }

}
