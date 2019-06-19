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

import org.marc4j.Marc4jConfig;
import org.marc4j.NCR_FORMAT;

import java.util.Collections;
import java.util.Formatter;

/**
 * Handles conversions back and forth between Unicode and NCR encodings (i.e. &amp;#xXXXX; (marc-8 NCR) or
 * &lt;U+XXXX&gt; (Unicode BNF) based on Marc4jConfig setting.)
 *
 * @author SirsiDynix
 */
public final class UnicodeUtils {

    /**
     * Private constructor so no one instantiates the class.
     */
    private UnicodeUtils() {
    }

    /**
     * This method converts characters from their escape sequence into Unicode.  Handles both Marc NCR format
     * (&amp;#xXXXX;) and Unicode BNF format (&lt;U+XXXX&gt;) format sequences.
     * e.g. '&amp;#x0021;' (Marc NCR) and '&lt;U+0021&gt;' (Unicode) converts to '!'
     * It is meant to preserve those characters that would have been lost without a conversion point into MARC8
     * or Unimarc
     *
     * @param buffer the buffer with characters to be converted
     */
    public static void convertNCRToUnicode(StringBuilder buffer) {
        // Handle "&#xXXXX;" (Unicode) format character sequences.
        int pos = buffer.indexOf("&#x");
        while (pos >= 0 && buffer.length() >= pos + 8 &&
            buffer.charAt(pos + 7) == ';') {
            String val = buffer.substring(pos + 3, pos + 7);
            try {
                char ch = (char) Integer.parseInt(val, 16);
                buffer.delete(pos, pos + 8);
                buffer.insert(pos, ch);
            } catch (NumberFormatException e) {
                // shouldn't occur - if it does, we'll just ignore it (leave the original
                // "&#xXXXX;" string alone in the buffer.)
            }
            pos = buffer.indexOf("&#x", pos + 1);
        }
        // Handle "<U+XXXX>" (Unicode) format character sequences.
        pos = buffer.indexOf("<U+");
        while (pos >= 0 && buffer.length() >= pos + 8 &&
            buffer.charAt(pos + 7) == '>') {
            String val = buffer.substring(pos + 3, pos + 7);
            try {
                char ch = (char) Integer.parseInt(val, 16);
                buffer.delete(pos, pos + 8);
                buffer.insert(pos, ch);
            } catch (NumberFormatException e) {
                // shouldn't occur - if it does, we'll just ignore it (leave the original
                // "<U+XXXX>" string alone in the buffer.)
            }
            pos = buffer.indexOf("<U+", pos + 1);
        }
    }

    /**
     * Converts a Marc-8 or Unicode character to Unicode NCR format.  Depending on Marc4jConfig, format of result
     * will be either &amp;#xXXXX; or &lt;U+XXXX&gt;
     *
     * @param ch Unicode Character to convert.
     * @return NCR encoded equivalent of ch
     */
    public static String convertUnicodeToNCR(Character ch) {
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        String format = Marc4jConfig.getNCR_format() == NCR_FORMAT.MARC8_NCR ? "&#x%04X;" : "<U+%04X>";
        f.format(format, Collections.singletonList((int) ch).toArray());
        return sb.toString();
    }

    /**
     * Converts a character to Unicode BNF format (i.e. "^lt;U+XXXX&gt;"
     * @param ch Unicode Character to convert
     * @return Unicode BNF encoded equivalent of ch
     */
    public static String convertUnicodeToUnicodeBNF(Character ch) {
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        f.format("<U+%04X>", Collections.singletonList((int) ch).toArray());
        return sb.toString();
    }

}
