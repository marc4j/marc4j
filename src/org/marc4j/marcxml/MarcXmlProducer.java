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
package org.marc4j.marcxml;

import java.io.IOException;
import java.util.Hashtable;
import org.xml.sax.XMLFilter;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.InputSource;
import org.xml.sax.ext.LexicalHandler;
import javax.xml.transform.TransformerException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.sax.SAXTransformerFactory;
import org.marc4j.ErrorHandler;

public class MarcXmlProducer {

    private Hashtable cache;

    private XMLFilter filter;

    private TransformerHandler handler;

    private DocType doctype; 

    public MarcXmlProducer() {
	filter = new MarcXmlFilter();
	cache = new Hashtable();
    }

    public void parse(InputSource source)
	throws IOException, SAXException, TransformerException {
	Source stylesheet = null;
	Result result = new StreamResult(System.out);
	parse(source, stylesheet, result);
    }

    public void parse(InputSource source, Result result)
	throws IOException, SAXException, TransformerException {
	Source stylesheet = null;
	parse(source, stylesheet, result);
    }

    public void parse(InputSource source, Source stylesheet, Result result) 
	throws IOException, SAXException, TransformerException {
	TransformerHandler handler = newTransformerHandler(stylesheet);
	handler.setResult(result);
	filter.setContentHandler(handler);
	if (doctype != null) setDocType(doctype, handler);
	filter.parse(source);
    }

    public void setDocType(DocType doctype) {
	this.doctype = doctype;
    }

    public void setPrettyPrinting(boolean b)
	throws SAXNotRecognizedException, 
	       SAXNotSupportedException {
	filter.setFeature("http://marc4j.org/features/pretty-printing", b);
    }

    public void setAnseltoUnicode(boolean b)
	throws SAXNotSupportedException, 
	       SAXNotRecognizedException {
	filter.setFeature("http://marc4j.org/features/ansel-to-unicode", b);
    }

    public void setSchemaLocation(String location) 
	throws SAXNotSupportedException, 
	       SAXNotRecognizedException {
	filter.setProperty("http://marc4j.org/properties/schema-location", location);
    }

    public void setErrorHandler(ErrorHandler eh)
	throws SAXNotSupportedException, 
	       SAXNotRecognizedException {
	filter.setProperty("http://marc4j.org/properties/error-handler", eh);
    }

    public TransformerHandler newTransformerHandler() 
	throws TransformerException {
	Source stylesheet = null;
	return newTransformerHandler(stylesheet);
    }

    public TransformerHandler newTransformerHandler(Source stylesheet) 
	throws TransformerException {
	TransformerFactory factory = TransformerFactory.newInstance();
	if (factory.getFeature(SAXTransformerFactory.FEATURE)) {
	    SAXTransformerFactory saxFactory = (SAXTransformerFactory)factory;
	    TransformerHandler handler = null;
	    if (stylesheet == null) {
		handler = saxFactory.newTransformerHandler();
	    } else {
		String uri = stylesheet.getSystemId();
		Templates templates = (Templates)cache.get(uri);
		if (templates == null) {
		    templates = factory.newTemplates(stylesheet);
		    cache.put(uri, templates);
		}
		handler = saxFactory.newTransformerHandler(templates);
	    }
	    return handler;
	} else {
	    return null;
	}
    }

    private synchronized Templates tryCache(Source stylesheet)
	throws TransformerException {

	String uri = stylesheet.getSystemId();
        Templates templates = (Templates)cache.get(uri);
        if (templates == null) {
            TransformerFactory factory = TransformerFactory.newInstance();
            templates = factory.newTemplates(stylesheet);
            cache.put(uri, templates);
        }
        return templates;
    }

    public synchronized void clearCache() {
        cache = new Hashtable();
    }

    private void setDocType(DocType doctype, LexicalHandler handler) 
	throws SAXNotSupportedException, 
	       SAXNotRecognizedException {
	filter.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
	filter.setProperty("http://marc4j.org/properties/document-type-declaration", doctype);
    }
    
}
