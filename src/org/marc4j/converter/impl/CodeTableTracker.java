// $Id: CodeTableTracker.java,v 1.1 2005/05/04 10:06:46 bpeters Exp $
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

/**
 * <p>A utility to convert UCS/Unicode data to MARC-8.</p>
 *
 * @author Corey Keith
 * @version $Revision: 1.1 $
 */
public class CodeTableTracker {
    static final byte prev=0;
    static final byte curr=1;
    static final byte next=2;
    static final byte G0=0;
    static final byte G1=1;

    protected Integer g[][];

    public CodeTableTracker() {
	g = new Integer[2][3];
	g[G0][prev] = new Integer(0x42);
	g[G1][prev] = new Integer(0x45);
    }

    public void makePreviousCurrent() {
	g[G0][curr] = g[G0][prev];
	g[G1][curr] = g[G1][prev];
    }

    public Integer getPrevious(byte set) {
	return g[set][prev];
    }

    public Integer getCurrent(byte set) {
	return g[set][curr];
    }

    public Integer getNext(byte set) {
	return g[set][next];
    }

    public void setPrevious(byte set, Integer table) {
	g[set][prev]=table;
    }

    public void setCurrent(byte set, Integer table) {
	g[set][curr]=table;
    }

    public void setNext(byte set, Integer table) {
	g[set][next]=table;
    }

    /* public String toString() {
	return "G0: [" + Integer.toHexString(g[G0][prev]) + ", " + Integer.toHexString(g[G0][curr]) + ", "+ Integer.toHexString(g[G0][next]) + "]\n" +
"G1: ["+ Integer.toHexString(g[G1][prev]) + ", " + Integer.toHexString(g[G1][curr]) + ", "+ Integer.toHexString(g[G1][next]) + "]\n";


    }
    */
}
