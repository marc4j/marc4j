// $Id: Ascii.java,v 1.2 2002/07/06 13:40:20 bpeters Exp $
/**
 * Copyright (C) 2002 Bas Peters (mail@bpeters.com)
 *
 * This file is part of MARC4J
 *
 * MARC4J is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * MARC4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MARC4J; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * As a special exception, if you link this library with other files to
 * produce an executable, this library does not by itself cause the
 * resulting executable to be covered by the GNU General Public License.
 * This exception does not however invalidate any other reasons why the
 * executable file might be covered by the GNU General Public License.
 */
package org.marc4j.util;

/**
 * <p>Defines a datatype for ASCII characters.  </p>
 *
 * @author <a href="mailto:mail@bpeters.com">Bas Peters</a> 
 * @version $Revision: 1.2 $
 *
 */
public class Ascii {

    /**
     * <p>Returns true if the given character is an ASCII character.</p>
     *
     * @param c the char to validate
     * @return <code>boolean</code> - true if the character is ASCII,
     *                                false if the character is not ASCII
     */
    public static boolean isValid(char c) {
        switch(c) {
        case 0x0000 :
        case 0x0001 :
        case 0x0002 :
        case 0x0003 :
        case 0x0004 :
        case 0x0005 :
        case 0x0006 :
        case 0x0007 :
        case 0x0008 :
        case 0x0009 :
        case 0x000A :
        case 0x000B :
        case 0x000C :
        case 0x000D :
        case 0x000E :
        case 0x000F :
        case 0x0010 :
        case 0x0011 :
        case 0x0012 :
        case 0x0013 :
        case 0x0014 :
        case 0x0015 :
        case 0x0016 :
        case 0x0017 :
        case 0x0018 :
        case 0x0019 :
        case 0x001A :
        case 0x001B :
        case 0x001C :
        case 0x001D :
        case 0x001E :
        case 0x001F :
        case 0x0020 :
        case 0x0021 :
        case 0x0022 :
        case 0x0023 :
        case 0x0024 :
        case 0x0025 :
        case 0x0026 :
        case 0x0027 :
        case 0x0028 :
        case 0x0029 :
        case 0x002A :
        case 0x002B :
        case 0x002C :
        case 0x002D :
        case 0x002E :
        case 0x002F :
        case 0x0030 :
        case 0x0031 :
        case 0x0032 :
        case 0x0033 :
        case 0x0034 :
        case 0x0035 :
        case 0x0036 :
        case 0x0037 :
        case 0x0038 :
        case 0x0039 :
        case 0x003A :
        case 0x003B :
        case 0x003C :
        case 0x003D :
        case 0x003E :
        case 0x003F :
        case 0x0040 :
        case 0x0041 :
        case 0x0042 :
        case 0x0043 :
        case 0x0044 :
        case 0x0045 :
        case 0x0046 :
        case 0x0047 :
        case 0x0048 :
        case 0x0049 :
        case 0x004A :
        case 0x004B :
        case 0x004C :
        case 0x004D :
        case 0x004E :
        case 0x004F :
        case 0x0050 :
        case 0x0051 :
        case 0x0052 :
        case 0x0053 :
        case 0x0054 :
        case 0x0055 :
        case 0x0056 :
        case 0x0057 :
        case 0x0058 :
        case 0x0059 :
        case 0x005A :
        case 0x005B :
        case 0x005C :
        case 0x005D :
        case 0x005E :
        case 0x005F :
        case 0x0060 :
        case 0x0061 :
        case 0x0062 :
        case 0x0063 :
        case 0x0064 :
        case 0x0065 :
        case 0x0066 :
        case 0x0067 :
        case 0x0068 :
        case 0x0069 :
        case 0x006A :
        case 0x006B :
        case 0x006C :
        case 0x006D :
        case 0x006E :
        case 0x006F :
        case 0x0070 :
        case 0x0071 :
        case 0x0072 :
        case 0x0073 :
        case 0x0074 :
        case 0x0075 :
        case 0x0076 :
        case 0x0077 :
        case 0x0078 :
        case 0x0079 :
        case 0x007A :
        case 0x007B :
        case 0x007C :
        case 0x007D :
        case 0x007E :
        case 0x007F :
            break;
        default :
            return false;
        }
        return true;
    }

}

// End of Ascii.java
