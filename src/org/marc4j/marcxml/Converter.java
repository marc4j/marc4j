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
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import org.marc4j.marcxml.MarcXmlHandler;
import org.marc4j.marcxml.MarcResult;
import org.marc4j.MarcReader;

/**
 * <p><code>Converter</code> can be used to apply a conversion 
 * or transformation from a source, populating a result.  </p>
 *
 * @author Bas Peters
 */
public class Converter {

    /** The transformer factory object */
    private TransformerFactory factory;

    /** The transformer object */
    private Transformer transformer;

    /** The cache table */
    private Hashtable cache = new Hashtable();

    /** Default constructor */
    public Converter() {}

    /**
     * <p>This converts a <code>Source</code> into a <code>Result</code>.</p>
     *
     * @param source the {@link Source} object
     * @param result the {@link Result} object
     */
    public void convert(Source source, Result result) 
    	throws TransformerException, SAXException, IOException {
	if (source instanceof MarcSource && result instanceof MarcResult)
	    convert((MarcSource)source, (MarcResult)result);
	Source stylesheet = null;
	convert(stylesheet, source, result);
    }

    /**
     * <p>This converts a <code>Source</code> into a <code>Result</code>.</p>
     *
     * @param stylesheet the stylesheet {@link Source} object
     * @param source the {@link Source} object
     * @param result the {@link Result} object
     */
    public void convert(Source stylesheet, Source source, Result result) 
    	throws TransformerException, SAXException, IOException {
    	if (result instanceof MarcResult) {
    	    convert(stylesheet, (SAXSource)source, (MarcResult)result);
    	} else { 
	    if (stylesheet != null) {
		Templates templates = tryCache(stylesheet);
		transformer = templates.newTransformer();
	    } else {
		factory = TransformerFactory.newInstance();
		transformer = factory.newTransformer();
	    }
	    transformer.transform(source, result);
	}
    }

    private void convert(Source stylesheet, SAXSource source, MarcResult result) 
	throws TransformerException, SAXException, IOException {
	MarcXmlHandler handler = new MarcXmlHandler();
	handler.setMarcHandler(result.getHandler());
	if (stylesheet != null) {
	    SAXResult out = new SAXResult(handler);
	    Templates templates = tryCache(stylesheet);
	    transformer = templates.newTransformer();
	    transformer.transform(source, out);
	} else {
	    XMLReader reader = source.getXMLReader();
	    reader.setContentHandler(handler);
	    reader.parse(source.getInputSource());
	}
    }

    public void convert(MarcSource source, MarcResult result)
	throws IOException {
	MarcReader reader = source.getMarcReader();
	reader.setMarcHandler(result.getHandler());
	reader.parse(source.getInputStream());
    }

    private synchronized Templates tryCache(Source stylesheet)
	throws TransformerException {

	String uri = stylesheet.getSystemId();
        Templates templates = (Templates)cache.get(uri);
        if (templates == null) {
            factory = TransformerFactory.newInstance();
            templates = factory.newTemplates(stylesheet);
            cache.put(uri, templates);
        }
        return templates;
    }

    /**
     * <p>This clears the <code>Templates</code> cache.</p>
     *
     * @see Templates
     */
    public synchronized void clearCache() {
        cache = new Hashtable();
    }

}
