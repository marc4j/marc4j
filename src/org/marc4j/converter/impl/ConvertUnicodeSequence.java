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
 * Converts all substrings of form "&lt;U+xxxx&gt;" into the corresponding unicode character.
 *
 * @author SirsiDynix
 */
public class ConvertUnicodeSequence {
    /**
     * This method converts characters from their escape sequence into Unicode
     * '&lt;U+0021&gt;' converts to '!'
     * It is meant to preserve those characters that would have been lost without a conversion point into MARC8
     * or Unimarc
     *
     * @param buffer the buffer with characters to be converted
     */
    public void convert(StringBuilder buffer) {
        int pos = buffer.indexOf("<U+");
        while (pos >= 0 && buffer.length() >= pos + 8 &&
            buffer.charAt(pos + 7) == '>') {
            String val = buffer.substring(pos + 3, pos + 7);
            try {
                char ch = (char) Integer.parseInt(val, 16);
                buffer.delete(pos, pos + 8);
                buffer.insert(pos, ch);
            } catch (NumberFormatException e) {
                // shouldn't occur
            }
            pos = buffer.indexOf("<U+", pos + 1);
        }
    }
}
