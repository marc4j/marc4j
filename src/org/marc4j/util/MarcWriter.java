// $Id: MarcWriter.java,v 1.11 2003/03/23 12:07:12 bpeters Exp $
/**
 * Copyright (C) 2002 Bas Peters (mail@bpeters.com)
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
package org.marc4j.util;

import java.io.Writer;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import org.marc4j.marc.*;
import org.marc4j.MarcHandler;

/**
 * <p>Implements the <code>MarcHandler</code> interface
 * to write record objects to tape format (ISO 2709).</p>
 *
 * @author <a href="mailto:mail@bpeters.com">Bas Peters</a> 
 * @version $Revision: 1.11 $
 *
 * @see MarcHandler
 */
public class MarcWriter 
    implements MarcHandler {

    /** Record object */
    private Record record;

    /** Data field object */
    private DataField datafield;

    /** The Writer object */
    private Writer out;

    /** The character conversion option */
    private CharacterConverter charconv = null;

    /**
     * <p>Default constructor.</p>
     *
     */
    public MarcWriter() throws IOException { 
	this(System.out); 
    }

    /**
     * <p>Creates a new instance.</p>
     *
     * @param out the {@link OutputStream} object
     *
     */
    public MarcWriter(OutputStream out) throws IOException {
	this(new OutputStreamWriter(out));
    }

    /**
     * <p>Creates a new instance.</p>
     *
     * @param out the {@link OutputStream} object
     * @param encoding the encoding
     *
     */
    public MarcWriter(OutputStream out, String encoding) 
	throws IOException {
	this(new OutputStreamWriter(out, encoding));
    }

    /**
     * <p>Creates a new instance and registers the Writer object.</p>
     *
     * @param out the {@link Writer} object
     */
    public MarcWriter(Writer out) {
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
     * @deprecated As of MARC4J beta 7 replaced by {@link #setCharacterConverter(CharacterConverter charconv)}
     */
    public void setUnicodeToAnsel(boolean convert) {
	if (convert)
	    charconv = new UnicodeToAnsel();
    }

    /**
     * <p>Sets the character conversion table.     </p>
     *
     * <p>A character converter is an instance of {@link CharacterConverter}.</p>
     *
     * @param charconv the character converter
     */
    public void setCharacterConverter(CharacterConverter charconv) {
	this.charconv = charconv;
    }

    /**
     * <p>Registers the Writer object.</p>
     *
     * <p>If the encoding is ANSEL the input 
     * stream will be converted.</p>
     *
     * @param out the {@link Writer} object
     * @param convert the conversion option
     */
    public void setWriter(Writer out, boolean convert) {
	this.out = out;
	setUnicodeToAnsel(convert);
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
	this.record = new Record();
	record.add(leader);
    }

    public void controlField(String tag, char[] data) {
	record.add(new ControlField(tag, data));
    }

    public void startDataField(String tag, char ind1, char ind2) {
	datafield = new DataField(tag, ind1, ind2);
    }

    public void subfield(char code, char[] data) {
	if (charconv != null)
	    datafield.add(new Subfield(code, charconv.convert(data)));
	else
	    datafield.add(new Subfield(code, data));
    }

    public void endDataField(String tag) {
	record.add(datafield);
    }

    public void endRecord() {
	try {
	    rawWrite(record.marshal());
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (MarcException e) {
	    e.printStackTrace();
	}
    }

    public void endCollection() {
	try {
	    out.flush();
	    out.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void rawWrite(String s)
	throws IOException {
	out.write(s);
    }

}
