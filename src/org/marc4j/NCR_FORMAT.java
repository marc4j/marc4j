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
 * Controls what format to use for NCR (Numeric Character Reference).  The default is the Marc-8 format "&amp;#xXXXX;",
 * as outlined here:
 * http://www.loc.gov/marc/specifications/speccharconversion.html#lossless
 * Optionally, it can be set to the Unicode extended BNF format of "%ltU+XXXX&gt;"
 *
 * @author SirsiDynix
 */
public enum NCR_FORMAT {
    MARC8_NCR,
    UNICODE_BNF
}
