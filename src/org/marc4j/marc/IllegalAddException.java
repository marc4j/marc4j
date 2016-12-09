/**
 * Copyright (C) 2004 Bas Peters (mail@bpeters.com)
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
 * Thrown when the addition of the supplied object is illegal.
 * 
 * @author Bas Peters
 */
public class IllegalAddException extends IllegalArgumentException {

    private static final long serialVersionUID = 8756226018321264604L;

    /**
     * Creates a new <code>Exception</code> indicating that the addition of the
     * supplied object is illegal.
     * 
     * @param className the class name
     */
    public IllegalAddException(final String className) {
        super(new StringBuffer().append("The addition of the object of type ")
                .append(className).append(" is not allowed.").toString());
    }

    /**
     * Creates a new <code>Exception</code> indicating that the addition of the
     * supplied object is illegal.
     * 
     * @param className the class name
     * @param reason the reason why the exception is thrown
     */
    public IllegalAddException(final String className, final String reason) {
        super(new StringBuffer().append("The addition of the object of type ")
                .append(className).append(" is not allowed: ").append(reason)
                .append(".").toString());
    }

}
