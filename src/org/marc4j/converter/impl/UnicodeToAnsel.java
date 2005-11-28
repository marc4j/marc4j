// $Id: UnicodeToAnsel.java,v 1.2 2005/11/28 16:50:22 bpeters Exp $
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
package org.marc4j.converter.impl;

import java.io.InputStream;
import java.util.Hashtable;

import org.marc4j.converter.CharConverter;

/**
 * <p>
 * A utility to convert UCS/Unicode data to MARC-8.
 * </p>
 * <p>
 * The MARC-8 to Unicode mapping used is the version with the March 2005
 * revisions.
 * </p>
 * 
 * @author Bas Peters
 * @author Corey Keith
 * @version $Revision: 1.2 $
 */
public class UnicodeToAnsel implements CharConverter {
    protected ReverseCodeTable rct;

    static final char ESC = 0x1b;

    static final char G0 = 0x28;

    static final char G0multibyte = 0x24;

    static final char G1 = 0x29;

    static final int ASCII = 0x42;

    /**
     * Creates a new instance and loads the MARC4J supplied Ansel/Unicode
     * conversion tables based on the official LC tables.
     */
    public UnicodeToAnsel() {
        this(UnicodeToAnsel.class
                .getResourceAsStream("resources/codetables.xml"));
    }

    /**
     * Constructs an instance with the specified pathname.
     * 
     * Use this constructor to create an instance with a customized code table
     * mapping. The mapping file should follow the structure of LC's XML MARC-8
     * to Unicode mapping (see:
     * http://www.loc.gov/marc/specifications/codetables.xml).
     *  
     */
    public UnicodeToAnsel(String pathname) {
        rct = new ReverseCodeTable(pathname);
    }
    
    /**
     * Constructs an instance with the specified input stream.
     * 
     * Use this constructor to create an instance with a customized code table
     * mapping. The mapping file should follow the structure of LC's XML MARC-8
     * to Unicode mapping (see:
     * http://www.loc.gov/marc/specifications/codetables.xml).
     *  
     */
    public UnicodeToAnsel(InputStream in) {
        rct = new ReverseCodeTable(in);
    }

    /**
     * Converts UCS/Unicode data to MARC-8.
     * 
     * <p>
     * A question mark (0x3F) is returned if there is no match.
     * </p>
     * 
     * @param dataElement
     *            the UCS/Unicode data
     * @return String - the MARC-8 data
     */
    public String convert(String dataElement) {
        char[] data = dataElement.toCharArray();
        StringBuffer sb = new StringBuffer();
        CodeTableTracker ctt = new CodeTableTracker();

        boolean technique1 = false;

        for (int i = 0; i < data.length; i++) {
            Character c = new Character(data[i]);
            Integer table;
            StringBuffer marc = new StringBuffer();
            Hashtable h = rct.codeTableHash(c);

            if (h.keySet().contains(ctt.getPrevious(CodeTableTracker.G0))) {
                ctt.makePreviousCurrent();
                marc.append((char[]) h
                        .get(ctt.getPrevious(CodeTableTracker.G0)));
            } else if (h.keySet()
                    .contains(ctt.getPrevious(CodeTableTracker.G1))) {
                ctt.makePreviousCurrent();
                marc.append((char[]) h
                        .get(ctt.getPrevious(CodeTableTracker.G1)));
            } else {
                table = (Integer) h.keySet().iterator().next();
                char[] marc8 = (char[]) h.get(table);

                if (marc8.length == 3) {
                    marc.append(ESC);
                    marc.append(G0multibyte);
                    ctt.setPrevious(CodeTableTracker.G0, table);
                } else if (marc8[0] < 0x80) {
                    marc.append(ESC);
                    if ((table.intValue() == 0x62)
                            || (table.intValue() == 0x70)) {
                        technique1 = true;
                    } else {
                        marc.append(G0);
                    }
                    ctt.setPrevious(CodeTableTracker.G0, table);
                } else {
                    marc.append(ESC);
                    marc.append(G1);
                    ctt.setPrevious(CodeTableTracker.G1, table);
                }
                marc.append((char) table.intValue());
                marc.append(marc8);
            }

            if (rct.isCombining(c))
                sb.insert(sb.length() - 1, marc);
            else
                sb.append(marc);
        }

        if (ctt.getPrevious(CodeTableTracker.G0).intValue() != ASCII) {
            sb.append(ESC);
            sb.append(G0);
            sb.append((char) ASCII);
        }

        return sb.toString();
    }

}
