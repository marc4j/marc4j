// $Id: MarcReaderException.java,v 1.6 2002/08/03 15:14:39 bpeters Exp $
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
package org.marc4j;

/**
 * <p>A <code>MarcReaderException</code> thrown when an error occurs 
 * while parsing MARC records. </p>
 *
 * @author <a href="mailto:mail@bpeters.com">Bas Peters</a> 
 * @version $Revision: 1.6 $
 *
 */
public class MarcReaderException extends Exception {

    int pos;
    String controlNumber;

    /**
     * <p>Creates an <code>Exception</code> indicating that an error
     * occured while parsing MARC records.</p>
     *
     * @param message the reason why the exception is thrown
     * @param pos position in the character stream where the exception is thrown
     */
    public MarcReaderException(String message, int pos) {
	super(message);
	setPosition(pos);
    }

    /**
     * <p>Creates an <code>Exception</code> indicating that an error
     * occured while parsing MARC records.</p>
     *
     * @param message the reason why the exception is thrown
     * @param pos position in the character stream where the exception is thrown
     * @param controlNumber the control number (tag 001)
     */
    public MarcReaderException(String message, int pos, String controlNumber) {
	super(message);
	setPosition(pos);
	setControlNumber(controlNumber);
    }

    private void setPosition(int pos) {
	this.pos = pos;
    }

   /**
     * <p>Returns the position in the character stream where the exception is thrown.</p>
     *
     * @return <code>int</code> - the position
     */
    public int getPosition() {
	return pos;
    }

    private void setControlNumber(String controlNumber) {
	this.controlNumber = controlNumber;
    }

   /**
     * <p>Returns the control number (tag 001).</p>
     *
     * @return <code>String</code> - the control number
     */    
    public String getControlNumber() {
	return controlNumber;
    }
}
