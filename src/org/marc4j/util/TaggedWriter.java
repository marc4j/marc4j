// $Id: TaggedWriter.java,v 1.2 2002/08/03 12:33:24 bpeters Exp $
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
package org.marc4j.util;

import java.io.Writer;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import org.marc4j.MarcHandler;
import org.marc4j.marc.Leader;

/**
 * <p>Implements the <code>MarcHandler</code> interface
 * to write MARC data in tagged display format.</p>
 *
 * @author <a href="mailto:mail@bpeters.com">Bas Peters</a> 
 * @version $Revision: 1.2 $
 *
 * @see MarcHandler
 */
public class TaggedWriter implements MarcHandler {

    /** The Writer object */
    private Writer out;

    /**
     * <p>Default constructor.</p>
     *
     */
    public TaggedWriter() throws IOException { 
	this(System.out); 
    }

    /**
     * <p>Creates a new instance.</p>
     *
     * @param out the {@link OutputStream} object
     *
     */
    public TaggedWriter(OutputStream out) throws IOException {
	this(new OutputStreamWriter(out));
    }

    /**
     * <p>Creates a new instance.</p>
     *
     * @param out the {@link OutputStream} object
     * @param encoding the encoding
     *
     */
    public TaggedWriter(OutputStream out, String encoding) 
	throws IOException {
	this(new OutputStreamWriter(out, encoding));
    }

    /**
     * <p>Creates a new instance and registers the Writer object.</p>
     *
     * @param out the {@link Writer} object
     */
    public TaggedWriter(Writer out) {
	setWriter(out);
    }

    /**
     * <p>Registers the Writer object.</p>
     *
     * @param out the {@link Writer} object
     */
    public void setWriter(Writer out) {
	this.out = out;
    }

    /**
     * <p>System exits when the Writer object is null.</p>
     *
     */
    public void startCollection() {
	if (out == null)
	    System.exit(0);
    }

    public void startRecord(Leader leader) {
	rawWrite("Leader ");
	rawWrite(leader.marshal());
	rawWrite('\n');
    }

    public void controlField(String tag, char[] data) {
	rawWrite(tag);
	rawWrite(' ');
	rawWrite(new String(data));
	rawWrite('\n');
    }

    public void startDataField(String tag, char ind1, char ind2) {
	rawWrite(tag);
	rawWrite(' ');
	rawWrite(ind1);
	rawWrite(ind2);
    }

    public void subfield(char code, char[] data) {
	rawWrite('$');
	rawWrite(code);
	rawWrite(new String(data));
    }

    public void endDataField(String tag) {
	rawWrite('\n');
    }

    public void endRecord() {
	rawWrite('\n');
    }

    public void endCollection() {
	try {
	    out.flush();
	    out.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void rawWrite(char c) {
	try {
	    out.write(c);
	} catch (IOException e) {
	    e.printStackTrace();
	} 
    }

    private void rawWrite(String s) {
	try {
	    out.write(s);
	} catch (IOException e) {
	    e.printStackTrace();
	} 
    }
    
}
