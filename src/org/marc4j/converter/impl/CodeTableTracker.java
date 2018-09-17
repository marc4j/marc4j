/**
 * Copyright (C) 2018
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
 * <p>For tracking graphic set, and curr, prev, and next pointer when parsing</p>
 *
 * @author SirsiDynix from Corey Keith
 */
public class CodeTableTracker {
    static final byte prev = 0;
    static final byte curr = 1;
    static final byte next = 2;
    static final byte G0 = 0;
    static final byte G1 = 1;
    static final byte G2 = 2; // only for UNIMARC
    static final byte G3 = 3; // only for UNIMARC

    protected Integer g[][];

    public CodeTableTracker() {
        g = new Integer[4][3];
        g[G0][prev] = 0x42; // B
        g[G1][prev] = 0x45; // E
        g[G2][prev] = null;
        g[G3][prev] = null;
    }

    public CodeTableTracker(CodeTableTracker ctt) {
        g = new Integer[4][3];
        g[G0][prev] = ctt.g[G0][prev];
        g[G1][prev] = ctt.g[G1][prev];
        g[G2][prev] = ctt.g[G2][prev];
        g[G3][prev] = ctt.g[G3][prev];
    }

    public void makePreviousCurrent() {
        g[G0][curr] = g[G0][prev];
        g[G1][curr] = g[G1][prev];
        g[G2][curr] = g[G2][prev];
        g[G3][curr] = g[G3][prev];
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
        g[set][prev] = table;
    }

    public void setCurrent(byte set, Integer table) {
        g[set][curr] = table;
    }

    public void setNext(byte set, Integer table) {
        g[set][next] = table;
    }
}
