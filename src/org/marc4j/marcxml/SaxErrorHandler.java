// $Id: SaxErrorHandler.java,v 1.4 2002/08/03 12:33:24 bpeters Exp $
/**
 * Copyright (C) 2002 Bas Peters
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
package org.marc4j.marcxml;

import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;

// This class is derived from an ErrorHandler example
// in the SAX2 book by David Brownell.

/**
 * <p><code>SaxErrorHandler</code> is a SAX2 <code>ErrorHandler</code> 
 * implementation.   </p>
 *
 * @author <a href="mailto:mail@bpeters.com">Bas Peters</a> 
 * @version $Revision: 1.4 $
 *
 * @see ErrorHandler
 */
public class SaxErrorHandler implements ErrorHandler {

    private int flags;

    public static final int ERR_PRINT = 1;
    public static final int ERR_IGNORE = 2;
    public static final int WARN_PRINT = 4;
    public static final int FATAL_PRINT = 8;
    public static final int FATAL_IGNORE = 16;

    public SaxErrorHandler() {
	flags = ~0;
    }

    public SaxErrorHandler(int flags) { 
	this.flags = flags;
    }

    public void error(SAXParseException e) throws SAXParseException {
	if ((flags & ERR_PRINT) != 0)
	    System.err.print (printParseException ("Error", e));
	if ((flags & ERR_IGNORE) == 0)
	    throw e;
    }

    public void fatalError(SAXParseException e) throws SAXParseException {
	if ((flags & FATAL_PRINT) != 0)
	    System.err.print (printParseException ("FATAL", e));
	if ((flags & FATAL_IGNORE) == 0)
	    throw e;
    }

    public void warning (SAXParseException e) throws SAXParseException {
	if ((flags & WARN_PRINT) != 0)
	    System.err.print (printParseException ("Warning", e));
    }

    public static String printParseException(String label, SAXParseException e) {
	StringBuffer buf = new StringBuffer ();
	int temp;
	buf.append("** ");
	buf.append(label);
	buf.append(": ");
	buf.append(e.getMessage ());
	buf.append('\n');
	if (e.getSystemId() != null) {
	    buf.append("   URI:  ");
	    buf.append(e.getSystemId ());
	    buf.append('\n');
	}
	if ((temp = e.getLineNumber()) != -1) {
	    buf.append("   Line: ");
	    buf.append(temp);
	    buf.append('\n');
	}
	if ((temp = e.getColumnNumber ()) != -1) {
	    buf.append("   Character: ");
	    buf.append(temp);
	    buf.append('\n');
	}
	return buf.toString();
    }
}

