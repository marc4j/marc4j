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

/**
 * Thrown in various situations by MARC4J; may contain a nested exception.
 * 
 * @author Bas Peters
 */
public class MarcException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -7600942667740838717L;

    /**
     * Create a new <code>MarcException</code> with no detail message.
     */
    public MarcException() {
        super();
    }

    /**
     * Create a new <code>MarcException</code> with the <code>String</code>
     * specified as an error message.
     * 
     * @param message information about the cause of the exception
     */
    public MarcException(final String message) {
        super(message);
    }

    /**
     * Create a new <code>MarcException</code> with the given
     * <code>Exception</code> base cause and detail message.
     * 
     * @param message information about the cause of the exception
     * @param ex the nested exception that caused this exception
     */
    public MarcException(final String message, final Throwable ex) {
        super(message, ex);
    }

}
