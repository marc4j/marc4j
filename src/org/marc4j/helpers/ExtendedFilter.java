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
 * As a special exception, if you link this library with other files to
 * produce an executable, this library does not by itself cause the
 * resulting executable to be covered by the GNU General Public License.
 * This exception does not however invalidate any other reasons why the
 * executable file might be covered by the GNU General Public License.
 */
package org.marc4j.helpers;

import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.ext.LexicalHandler; 
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * <p>Extends <code>XMLFilterImpl</code> with an implementation 
 * of the <code>LexicalHandler</code> interface.   </p>
 *
 * @author Bas Peters
 */
public class ExtendedFilter extends XMLFilterImpl 
    implements LexicalHandler {

    public LexicalHandler lh;

    private static String lexicalID = 
	"http://xml.org/sax/properties/lexical-handler";

    public void setProperty(String uri, Object handler) 
	throws SAXNotRecognizedException, SAXNotSupportedException {
	if (lexicalID.equals(uri))
	    lh = (LexicalHandler) handler;
	else 
	    super.setProperty(uri, handler);
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


