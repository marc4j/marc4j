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
 * Utilities for dealing with combined double characters -
 * @author SirsiDynix
 */
public class FixDoubleWidth {
    public static final String COMBINED_DOUBLE_LIGATURE = "\u0361";
    public static final char LIGATURE_FIRST_HALF = '\uFE20';
    public static final char LIGATURE_SECOND_HALF = '\uFE21';
    public static final String COMBINED_DOUBLE_TILDE = "\u0360";
    public static final char TILDE_FIRST_HALF = '\uFE22';
    public static final char TILDE_SECOND_HALF = '\uFE23';

    /**
     * The Double Ligature and Double Tilde Characters have a preferred single codepoint. The conversions currently
     * set the first character correctly to the combined single codepoint but are still leaving the second half. If this
     * is present we want it removed. (This isn't easily handled in the base conversion because it is dealing with
     * single character mappings and we don't want the alternate character removed so that it can be used on the reverse
     * mapping.
     *
     * @param data The string to be fixed that potentially contains the base combined character
     */
    public static void removeInvalidSecondHalf(StringBuilder data) {
        removeInvalidSecondHalf(data, COMBINED_DOUBLE_LIGATURE, LIGATURE_SECOND_HALF);
        removeInvalidSecondHalf(data, COMBINED_DOUBLE_TILDE, TILDE_SECOND_HALF);
    }

    /**
     * The Double Ligature and Double Tilde Characters have a preferred single codepoint. The conversions currently
     * set the first character correctly to the combined single codepoint but are still leaving the second half. If this
     * is present we want it removed. (This isn't easily handled in the base conversion because it is dealing with
     * single character mappings and we don't want the alternate character removed so that it can be used on the reverse
     * mapping.
     *
     * @param data         The string to be fixed that potentially contains the base combined character
     * @param combinedChar The combined COMBINED_DOUBLE_LIGATURE or COMBINED_DOUBLE_TILDE
     * @param secondHalf   The second half to be removed (LIGATURE_SECOND_HALF or TILDE_SECOND_HALF)
     */
    private static void removeInvalidSecondHalf(StringBuilder data, String combinedChar, char secondHalf) {
        int offset = data.indexOf(combinedChar);
        while (offset >= 0 && data.length() > offset + 2) {
            if (data.charAt(offset + 2) == secondHalf) {
                data.deleteCharAt(offset + 2);
            }
            offset = data.indexOf(combinedChar, offset + 1);
        }
    }

    public static char[] decomposeCombinedDoubleChar(char[] data) {
        // anticipate that most strings do not contain these characters; so we'll optimize for that
        // and not allocate buffers until then
        for (int i = 0; i < data.length; i++) {
            if (data[i] == COMBINED_DOUBLE_LIGATURE.charAt(0)) {
                StringBuilder mod = new StringBuilder();
                mod.append(data);
                if (mod.length() < i + 2) {
                    mod.append(' ');
                }
                mod.setCharAt(i, LIGATURE_FIRST_HALF);
                mod.insert(i + 2, LIGATURE_SECOND_HALF);
                data = mod.toString().toCharArray();
            } else if (data[i] == COMBINED_DOUBLE_TILDE.charAt(0)) {
                StringBuilder mod = new StringBuilder();
                mod.append(data);
                if (mod.length() < i + 2) {
                    mod.append(' ');
                }
                mod.setCharAt(i, TILDE_FIRST_HALF);
                mod.insert(i + 2, TILDE_SECOND_HALF);
                data = mod.toString().toCharArray();
            }
        }
        return data;
    }
}
