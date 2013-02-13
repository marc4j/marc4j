/**
 * Copyright (C) 2002 Bas Peters (mail@bpeters.com)
 *
 * ValidatorFilter is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public 
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 *
 * ValidatorFilter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with ValidatorFilter; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.helpers.XMLFilterImpl;
import com.thaiopensource.relaxng.ValidatorHandler;

/**
 * <p>Extends <code>XMLFilterImpl</code> to implement 
 * <code>com.thaiopensource.relaxng.ValidatorHandler</code> 
 * as an XML filter.</p>
 *
 * @author <a href="mailto:mail@bpeters.com">Bas Peters</a>
 * @see ValidatorHandler
 * @see XMLFilterImpl
 */
public class ValidatorFilter extends XMLFilterImpl 
    implements ValidatorHandler {

    /** Property for ValidatorHandler */
    private static final String VALIDATOR_PROPERTY = "http://org.marc4j/relaxng/validator";

    /** ValidatorHandler object */
    private ValidatorHandler validatorHandler = null;

    public boolean isComplete() {
	if (validatorHandler != null)
	    return validatorHandler.isComplete();
	else
	    return false;
    }

    public boolean isValidSoFar() {
	if (validatorHandler != null)
	    return validatorHandler.isValidSoFar();
	else
	    return false;
    }

    public void reset() {
	if (validatorHandler != null)
	    validatorHandler.reset();
    }

    /**
     * <p>Sets the property for {@link ValidatorHandler}.</p>
     *
     * @param name the name of the property
     * @param obj the property object
     */
    public void setProperty(String name, Object obj) 
	throws SAXNotRecognizedException, SAXNotSupportedException {
	if (VALIDATOR_PROPERTY.equals(name))
	    this.validatorHandler = (ValidatorHandler)obj;
	else
	    super.setProperty(name, obj);
    }

    public void setErrorHandler(ErrorHandler handler) {
	if (validatorHandler != null) {
	    if (validatorHandler.getErrorHandler() == null)
		validatorHandler.setErrorHandler(handler);
	}
	super.setErrorHandler(handler);
    }

    public ErrorHandler getErrorHandler() {
	return super.getErrorHandler();
    }

    public void notationDecl (String name, String publicId, String systemId)
	throws SAXException {
	if (validatorHandler != null) {
	    validatorHandler.notationDecl(name, publicId, systemId);
	}
	super.notationDecl(name, publicId, systemId);
    }

    public void unparsedEntityDecl (String name, String publicId,
				    String systemId, String notationName)
	throws SAXException {
	if (validatorHandler != null) {
	    validatorHandler.unparsedEntityDecl(name, publicId, systemId,
						notationName);
	}
	super.unparsedEntityDecl(name, publicId, systemId,
				 notationName);
    }

    public void startDocument ()
	throws SAXException {
	if (validatorHandler != null) {
	    validatorHandler.startDocument();
	}
	super.startDocument();
    }

    public void endDocument ()
	throws SAXException {
	if (validatorHandler != null) {
	    validatorHandler.endDocument();
	}
	super.endDocument();

    }

    public void startPrefixMapping (String prefix, String uri)
	throws SAXException {
	if (validatorHandler != null) {
	    validatorHandler.startPrefixMapping(prefix, uri);
	}
	super.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping (String prefix)
	throws SAXException {
	if (validatorHandler != null) {
	    validatorHandler.endPrefixMapping(prefix);
	}
	super.endPrefixMapping(prefix);
    }

    public void startElement (String uri, String localName, String qName,
			      Attributes atts)
	throws SAXException {
	if (validatorHandler != null) {
	    validatorHandler.startElement(uri, localName, qName, atts);
	}
	super.startElement(uri, localName, qName, atts);
    }

    public void endElement (String uri, String localName, String qName)
	throws SAXException {
	if (validatorHandler != null) {
	    validatorHandler.endElement(uri, localName, qName);
	}
	super.endElement(uri, localName, qName);
    }

    public void characters (char ch[], int start, int length)
	throws SAXException {
	if (validatorHandler != null) {
	    validatorHandler.characters(ch, start, length);
	}
	super.characters(ch, start, length);
    }

    public void ignorableWhitespace (char ch[], int start, int length)
	throws SAXException {
	if (validatorHandler != null) {
	    validatorHandler.ignorableWhitespace(ch, start, length);
	}
	super.ignorableWhitespace(ch, start, length);
    }

    public void processingInstruction (String target, String data)
	throws SAXException {
	if (validatorHandler != null) {
	    validatorHandler.processingInstruction(target, data);
	}
	super.processingInstruction(target, data);
    }

    public void skippedEntity (String name)
	throws SAXException {
	if (validatorHandler != null) {
	    validatorHandler.skippedEntity(name);
	}
	super.skippedEntity(name);
    }
}
