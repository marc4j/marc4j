// $Id: AnselToUnicode.java,v 1.3 2005/12/14 17:11:30 bpeters Exp $
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
import java.util.Vector;

import org.marc4j.converter.CharConverter;

/**
 * <p>
 * A utility to convert MARC-8 data to non-precomposed UCS/Unicode.
 * </p>
 * 
 * <p>
 * The MARC-8 to Unicode mapping used is the version with the March 2005
 * revisions.
 * </p>
 * 
 * @author Bas Peters
 * @author Corey Keith
 * @version $Revision: 1.3 $
 */
public class AnselToUnicode implements CharConverter {

    class Queue extends Vector {

        /**
         * Puts an item into the queue.
         * 
         * @param item
         *            the item to be put into the queue.
         */
        public Object put(Object item) {
            addElement(item);

            return item;
        }

        /**
         * Gets an item from the front of the queue.
         */
        public Object get() {
            Object obj;
            int len = size();

            obj = peek();
            removeElementAt(0);

            return obj;
        }

        /**
         * Peeks at the front of the queue.
         */
        public Object peek() {
            int len = size();

            return elementAt(0);
        }

        /**
         * Returns true if the queue is empty.
         */
        public boolean empty() {
            return size() == 0;
        }
    }

    class CodeTracker {
        int offset;

        int g0;

        int g1;

        boolean multibyte;

        public String toString() {
            return "Offset: " + offset + " G0: " + Integer.toHexString(g0)
                    + " G1: " + Integer.toHexString(g1) + " Multibyte: "
                    + multibyte;
        }
    }

    protected CodeTable ct;

    protected boolean loadedMultibyte = false;

    /**
     * Creates a new instance and loads the MARC4J supplied
     * conversion tables based on the official LC tables.
     *  
     */
    public AnselToUnicode() {
        this(AnselToUnicode.class
                .getResourceAsStream("resources/codetablesnocjk.xml"));
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
    public AnselToUnicode(String pathname) {
        ct = new CodeTable(pathname);
        loadedMultibyte = true;
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
    public AnselToUnicode(InputStream in) {
        ct = new CodeTable(in);
        loadedMultibyte = true;
    }

    /**
     * Loads the entire maping (including multibyte characters) from the Library
     * of Congress.
     */
    private void loadMultibyte() {
        ct = new CodeTable(getClass().getResourceAsStream(
                "resources/codetables.xml"));
    }

    private void checkMode(char[] data, CodeTracker cdt) {
        while (cdt.offset < data.length && isEscape(data[cdt.offset])) {
            switch (data[cdt.offset + 1]) {
            case 0x28:
            case 0x2c:
                cdt.g0 = data[cdt.offset + 2];
                cdt.offset += 3;
                cdt.multibyte = false;
                break;
            case 0x29:
            case 0x2d:
                cdt.g1 = data[cdt.offset + 2];
                cdt.offset += 3;
                cdt.multibyte = false;
                break;
            case 0x24:
                cdt.multibyte = true;
                if (!loadedMultibyte) {
                    loadMultibyte();
                    loadedMultibyte = true;
                }
                switch (data[cdt.offset + 1]) {
                case 0x29:
                case 0x2d:
                    cdt.g1 = data[cdt.offset + 3];
                    cdt.offset += 4;
                    break;
                case 0x2c:
                    cdt.g0 = data[cdt.offset + 3];
                    cdt.offset += 4;
                    break;
                default:
                    cdt.g0 = data[cdt.offset + 2];
                    cdt.offset += 3;
                    break;
                }
                break;
            case 0x67:
            case 0x62:
            case 0x70:
                cdt.g0 = data[cdt.offset + 1];
                cdt.offset += 2;
                cdt.multibyte = false;
                break;
            case 0x73:
                cdt.g0 = 0x42;
                cdt.offset += 2;
                cdt.multibyte = false;
                break;
            }
        }
    }

    /**
     * <p>
     * Converts MARC-8 data to UCS/Unicode.
     * </p>
     * 
     * @param dataElement
     *            the MARC-8 data
     * @return String - the UCS/Unicode data
     */
    public String convert(String dataElement) {
        char[] data = null;
        data = dataElement.toCharArray();
        StringBuffer sb = new StringBuffer();
        int len = data.length;

        CodeTracker cdt = new CodeTracker();

        cdt.g0 = 0x42;
        cdt.g1 = 0x45;
        cdt.multibyte = false;

        cdt.offset = 0;

        checkMode(data, cdt);

        Queue diacritics = new Queue();

        while (cdt.offset < data.length) {
            if (ct.isCombining(data[cdt.offset], cdt.g0, cdt.g1)
                    && hasNext(cdt.offset, len)) {

                while (ct.isCombining(data[cdt.offset], cdt.g0, cdt.g1)
                        && hasNext(cdt.offset, len)) {
                    diacritics.put(new Character(getChar(data[cdt.offset],
                            cdt.g0, cdt.g1)));
                    cdt.offset++;
                    checkMode(data, cdt);
                }

                char c2 = getChar(data[cdt.offset], cdt.g0, cdt.g1);
                cdt.offset++;
                checkMode(data, cdt);
                sb.append(c2);

                while (!diacritics.isEmpty()) {
                    char c1 = ((Character) diacritics.get()).charValue();
                    sb.append(c1);
                }

            } else if (cdt.multibyte) {
                sb.append(ct.getChar(makeMultibyte(new String(data).substring(
                        cdt.offset, cdt.offset + 4).toCharArray()), cdt.g0));
                cdt.offset += 3;
            } else {
                sb.append(getChar(data[cdt.offset], cdt.g0, cdt.g1));
                cdt.offset += 1;
            }
            if (hasNext(cdt.offset, len))
                checkMode(data, cdt);
        }
        return sb.toString();
    }

    private int makeMultibyte(char[] data) {
        int[] chars = new int[3];
        chars[0] = data[0] << 16;
        chars[1] = data[1] << 8;
        chars[2] = data[2];
        return chars[0] | chars[1] | chars[2];
    }

    private char getChar(int ch, int g0, int g1) {
        if (ch <= 0x7E)
            return ct.getChar(ch, g0);
        else
            return ct.getChar(ch, g1);
    }

    private char getMBChar(int ch) {
        return ct.getChar(ch, 0x31);
    }

    private static boolean hasNext(int pos, int len) {
        if (pos < (len - 1))
            return true;
        return false;
    }

    private static boolean isEscape(int i) {
        if (i == 0x1B)
            return true;
        return false;
    }

}