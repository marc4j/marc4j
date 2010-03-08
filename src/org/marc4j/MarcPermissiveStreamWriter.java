// $Id: MarcPermissiveStreamWriter.java,v 1.1 2010/03/08 22:40:00 haschart Exp $
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

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;

import org.marc4j.marc.Leader;

/**
 * Class for writing MARC record objects in ISO 2709 format.  Note the ONLY difference 
 * between this class and the MarcStreamWriter class that it extends is that in the
 * case of a too-large MARC record (ie. number of bytes > 99999) rather than writing 
 * out a 6 digit number, and producing a record that no one can then successfully read in, 
 * this class ensures that the numbers will only be 5 digits long. Even though the records 
 * it produces through such a process are invalid, there is at least a chance that a 
 * permissive MARC record reader could manage to read in.  For further documentation see the 
 * MarcStreamWriter class.
 * 
 * 
 * @author Robert Haschart
 * @version $Revision: 1.1 $
 */
public class MarcPermissiveStreamWriter extends MarcStreamWriter {
    
    private static DecimalFormat format4 = new DecimalFormat("0000");

    private static DecimalFormat format5 = new DecimalFormat("00000");

    /**
     * Constructs an instance and creates a <code>Writer</code> object with
     * the specified output stream.
     */
    public MarcPermissiveStreamWriter(OutputStream out) {
        super(out);
    }

    /**
     * Constructs an instance and creates a <code>Writer</code> object with
     * the specified output stream and character encoding.
     */
    public MarcPermissiveStreamWriter(OutputStream out, String encoding) {
        super(out, encoding);
    }


    protected void write(Leader ldr) throws IOException {
        int recLength = ldr.getRecordLength();
        if (recLength > 99999) recLength = 99999;
        
        out.write(format5.format(recLength).getBytes(encoding));
        out.write(ldr.getRecordStatus());
        out.write(ldr.getTypeOfRecord());
        out.write(new String(ldr.getImplDefined1()).getBytes(encoding));
        out.write(ldr.getCharCodingScheme());
        out.write(Integer.toString(ldr.getIndicatorCount()).getBytes(encoding));
        out.write(Integer.toString(ldr.getSubfieldCodeLength()).getBytes(
                encoding));
        out
                .write(format5.format(ldr.getBaseAddressOfData()).getBytes(
                        encoding));
        out.write(new String(ldr.getImplDefined2()).getBytes(encoding));
        out.write(new String(ldr.getEntryMap()).getBytes(encoding));
    }
    
    protected byte[] getEntry(String tag, int length, int start) throws IOException {
        if (length > 9999) length = 9999;
        if (start > 99999) start = 99999;
        return (tag + format4.format(length) + format5.format(start)).getBytes(encoding);
    }

}