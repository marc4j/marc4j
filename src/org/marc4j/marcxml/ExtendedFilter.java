// $Id: ExtendedFilter.java,v 1.4 2002/08/03 12:33:24 bpeters Exp $
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

import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.ext.LexicalHandler; 
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * <p><code>ExtendedFilter</code> extends <code>XMLFilterImpl</code> 
 * with an implementation of the <code>LexicalHandler</code> interface.  </p>
 *
 * @author <a href="mailto:mail@bpeters.com">Bas Peters</a> 
 * @version $Revision: 1.4 $
 *
 */
public class ExtendedFilter extends XMLFilterImpl 
    implements LexicalHandler {

    /** The lexical handler property */
    private static final String LEXICAL_HANDLER = 
	"http://xml.org/sax/properties/lexical-handler";

    /** the lexical handler object */
    public LexicalHandler lh;

    /**
     * <p>Sets the object for the given property.</p>
     *
     * @param uri the property name
     * @param obj the property object
     */
    public void setProperty(String uri, Object obj) 
	throws SAXNotRecognizedException, SAXNotSupportedException {
	if (LEXICAL_HANDLER.equals(uri))
	    lh = (LexicalHandler)obj;
	else 
	    super.setProperty(uri, obj);
    }

    public void startEntity(String name) throws SAXException {
	if (lh != null)
	    lh.startEntity(name);
    }

    public void endEntity(String name) throws SAXException {
	if (lh != null)
	    lh.endEntity(name);
    }

    public void startCDATA() throws SAXException {
	if (lh != null)
	    lh.startCDATA();
    }

    public void endCDATA() throws SAXException {
	if (lh != null)
	    lh.endCDATA();
    }

    public void startDTD(String name, String publicId, String systemId) 
	throws SAXException {
	if (lh != null)
	    lh.startDTD(name, publicId, systemId);
    }

    public void endDTD() throws SAXException {
	if (lh != null)
	    lh.endDTD();
    }

    public void comment (char ch[], int start, int length)
	throws SAXException {
	if (lh != null)
	    lh.comment(ch, start, length);
    }
}
