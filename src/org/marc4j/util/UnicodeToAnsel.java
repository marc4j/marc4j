// $Id: UnicodeToAnsel.java,v 1.9 2003/03/23 12:05:54 bpeters Exp $
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

import java.io.CharArrayReader;
import java.io.IOException;
import java.util.Hashtable;

/**
 * <p>A utility to convert UCS/Unicode data to MARC-8.</p>
 *
 * @author <a href="mailto:mail@bpeters.com">Bas Peters</a>
 * @author <a href="mailto:ckeith@loc.gov">Corey Keith</a>
 * @version $Revision: 1.9 $
 */
public class UnicodeToAnsel implements CharacterConverter {
    protected ReverseCodeTable rct;
    static final char ESC = 0x1b;
    static final char G0 = 0x28;
    static final char G0multibyte = 0x24;
    static final char G1 = 0x29;
    static final int ASCII = 0x42;

    public UnicodeToAnsel() {
	//try {
	//  rct = new ReverseCodeTable(new java.net.URI("http://www.loc.gov/marc/specifications/codetables.xml"));
	//} catch (java.net.URISyntaxException exp)  {
	//  System.err.println("Unable to load character code table");
	//  System.exit(1);
	//}
	rct = new ReverseCodeTable(getClass().getResourceAsStream("resources/codetables.xml"));
    }

    /**
     * <p>Converts UCS/Unicode data to MARC-8.</p>
     *
     * <p>A question mark (0x3F) is returned if there is no match.</p>
     *
     * @param data the UCS/Unicode data
     * @return {@link String} - the MARC-8 data
     */
    public String convert(String data) {
	return new String(convert(data.toCharArray()));
    }

    /**
     * <p>Converts UCS/Unicode data to MARC-8.</p>
     *
     * <p>A question mark (0x3F) is returned if there is no match.</p>
     *
     * @param data the UCS/Unicode data
     * @return char[] - the MARC-8 data
     */
    public char[] convert(char[] data) {
	StringBuffer sb = new StringBuffer();
	CodeTableTracker ctt = new CodeTableTracker();

        boolean technique1 = false;

	for(int i = 0; i < data.length; i++) {
	    Character c = new Character(data[i]);
	    Integer table;
	    StringBuffer marc = new StringBuffer();
	    Hashtable h = rct.codeTableHash(c);

	    if (h.keySet().contains(ctt.getPrevious(CodeTableTracker.G0))) {
		ctt.makePreviousCurrent();
		marc.append((char[])h.get(ctt.getPrevious(CodeTableTracker.G0)));
	    } else if (h.keySet().contains(ctt.getPrevious(CodeTableTracker.G1))) {
		ctt.makePreviousCurrent();
		marc.append((char[])h.get(ctt.getPrevious(CodeTableTracker.G1)));
	    } else {
		table = (Integer)h.keySet().iterator().next();
		char[] marc8 = (char[])h.get(table);

		if (marc8.length == 3) {
		    marc.append(ESC);
		    marc.append(G0multibyte);
		    ctt.setPrevious(CodeTableTracker.G0,table);
		} else if (marc8[0] < 0x80) {
		    marc.append(ESC);
                    if ((table.intValue() == 0x62) || (table.intValue() == 0x70)) {
                      technique1 = true;
                    } else {
                      marc.append(G0);
                    }
		    ctt.setPrevious(CodeTableTracker.G0,table);
		} else {
		    marc.append(ESC);
		    marc.append(G1);
		    ctt.setPrevious(CodeTableTracker.G1,table);
		}
		marc.append((char)table.intValue());
		marc.append(marc8);
	    }

	    if (rct.isCombining(c))
		sb.insert(sb.length()-1,marc);
	    else
		sb.append(marc);
	}

	if (ctt.getPrevious(CodeTableTracker.G0).intValue() != ASCII){
	    sb.append(ESC);
	    sb.append(G0);
	    sb.append((char)ASCII);
	}

	return sb.toString().toCharArray();
    }


    public static void main( String[] args ) {
	char test[] = {'c','o','r','e','[',0x303,0x5d0,']','y',0x660};
	UnicodeToAnsel uta = new UnicodeToAnsel();

	System.out.println(uta.convert(test));
    }
}

