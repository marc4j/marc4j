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

package org.marc4j;

/**
 * Shared global configuration.  Allows clients to set these once, and have these config values used
 * throughout Marc4j.
 * <br>
 * These are NOT thread safe!  The intent is that these are set once by the client, and then used for all
 * operations by that client.  They are NOT designed to be reset during processing!
 *
 * @author SirsiDynix
 */
public final class Marc4jConfig {

    // Default - Marc-8 NCR
    private static NCR_FORMAT ncr_format = NCR_FORMAT.MARC8_NCR;


    /**
     * Get The NCR format to use when encoding non-encodable Unicode characters into the Marc-8 or UniMarc
     * character set.  Note that when converting Marc-8 or UniMarc to Unicode, BOTH formats are ALWAYS
     * handled.
     * @return the NCR Format.
     */
    public static NCR_FORMAT getNCR_format() {
        return ncr_format;
    }

    /**
     * Set the NCR format to use when converting Unimarc to Marc-8 or UniMarc, for Unicode characters that
     * cannot be encoded in the target character set.
     * @param format NCR_FORMAT
     */
    public static void setNCR_format(NCR_FORMAT format) {
        ncr_format = format;
    }
}
