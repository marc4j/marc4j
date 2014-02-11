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
import java.lang.reflect.Constructor;

import org.marc4j.converter.CharConverter;
import org.marc4j.util.Normalizer;


/**
 * <p>
 * A utility to convert UCS/Unicode data to MARC-8.
 * </p>
 * <p>
 * The MARC-8 to Unicode mapping used is the version with the March 2005
 * revisions.
 * </p>
 * 
 * @author Bas Peters
 * @author Corey Keith
 * @author Robert Haschart
 */
public class UnicodeToAnsel extends CharConverter {
    protected ReverseCodeTable rct;

    static final char ESC = 0x1b;

    static final char G0 = 0x28;

    static final char G0multibyte = 0x24;

    static final char G1 = 0x29;

    static final int ASCII = 0x42;
    boolean dontChangeCharset = false;
    
    /**
     * Creates a new instance and loads the MARC4J supplied Ansel/Unicode
     * conversion tables based on the official LC tables. Loads in the generated class
     * ReverseCodeTableGenerated which contains switch statements to lookup 
     * the MARC-8 encodings for given Unicode characters.
     */
    public UnicodeToAnsel() {
        rct = loadGeneratedTable();
        //this(UnicodeToAnsel.class
        //        .getResourceAsStream("resources/codetables.xml"));
    }
    /**
     * Creates a new instance and loads the MARC4J supplied Ansel/Unicode
     * conversion tables based on the official LC tables. Loads in the generated class
     * ReverseCodeTableGenerated which contains switch statements to lookup 
     * the MARC-8 encodings for given Unicode characters.
     */
    public UnicodeToAnsel(boolean defaultCharsetOnlyPlusNCR) {
        dontChangeCharset = true;
        rct = loadGeneratedTable();
        //this(UnicodeToAnsel.class
        //        .getResourceAsStream("resources/codetables.xml"));
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
    public UnicodeToAnsel(String pathname) {
        rct = new ReverseCodeTableHash(pathname);
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
    public UnicodeToAnsel(InputStream in) {
        rct = new ReverseCodeTableHash(in);
    }
    
    private ReverseCodeTable loadGeneratedTable() 
    {
        try
        {
            Class<?> generated = Class.forName("org.marc4j.converter.impl.ReverseCodeTableGenerated");
            Constructor<?> cons = generated.getConstructor();
            Object rct = cons.newInstance();
            return((ReverseCodeTable)rct);
        }
        catch (Exception e)
        {
            ReverseCodeTable rct;
            rct = new ReverseCodeTableHash(AnselToUnicode.class.getResourceAsStream("resources/codetables.xml"));                
            return(rct);
        }
    }

    /**
     * Converts UCS/Unicode data to MARC-8.
     * 
     * <p>
     * If there is no match for a Unicode character, it will be encoded as &#xXXXX; 
     * so that if the data is translated back into Unicode, the original data 
     * can be recreated. 
     * </p>
     * 
     * @param data - the UCS/Unicode data in an array of char
     * @return String - the MARC-8 data
     */
    public String convert(char data[]) 
    {
        StringBuffer sb = new StringBuffer();
        
        rct.init();
        
        convertPortion(data, sb);

        if (rct.getPreviousG0() != ASCII) {
            sb.append(ESC);
            sb.append(G0);
            sb.append((char) ASCII);
        }

        return sb.toString();
    }
    
    /**
     * Does the actual work of converting UCS/Unicode data to MARC-8.
     * 
     * <p>
     * If the Unicode data has been normalized into composed form, and the composed character 
     * does not have a corresponding MARC8 character, this routine will normalize that character into
     * its decomposed form, and try to translate that equivalent string into MARC8. 
     * </p>
     * 
     * @param data - the UCS/Unicode data in an array of char
     * @return String - the MARC-8 data
     */
    private void convertPortion(char data[], StringBuffer sb)
    {
        int prev_len = 1;
        for (int i = 0; i < data.length; i++) 
        {
            Character c = new Character(data[i]);
            StringBuffer marc = new StringBuffer();
            int charValue = (int)c.charValue();
            if (charValue == 0x20 && rct.getPreviousG0() != (int)'1')
            {
                if (rct.getPreviousG0() == (int)'1')
                {
                    sb.append(ESC);
                    sb.append(G0);
                    sb.append((char) ASCII);
                    rct.setPreviousG0(ASCII);
                }
                marc.append(" ");
            }
            else if (!rct.charHasMatch(c))
            {
                // Unicode character c has no match in the Marc8 tables.  Try unicode-decompose on it
                // to see whether the decomposed form can be represented.  If when decomposed, all of
                // the characters can be translated to marc8, then use that.  If not and the decomposed form
                // if three (or more) characters long (which indicates multiple diacritic marks), then 
                // re-compose the the main character with the first diacritic, and check whether that 
                // and the remaining diacritics can be translated. If so go with that, otherwise, give up
                // and merely use the &#xXXXX; Numeric Character Reference form to represent the original
                // unicode character
                String tmpnorm = c.toString();
                String tmpNormed = Normalizer.normalize(tmpnorm, Normalizer.NFD);
                if (!tmpNormed.equals(tmpnorm))
                {
                    if (allCharsHaveMatch(rct, tmpNormed))
                    {
                        convertPortion(tmpNormed.toCharArray(), sb);
                        continue;
                    }
                    else if (tmpNormed.length() > 2)
                    {
                        String firstTwo = tmpNormed.substring(0, 2);
                        String partialNormed = Normalizer.normalize(firstTwo, Normalizer.NFC);
                        if (!partialNormed.equals(firstTwo) && allCharsHaveMatch(rct, partialNormed) && 
                                allCharsHaveMatch(rct, tmpNormed.substring(2)))
                        {
                            convertPortion((partialNormed + tmpNormed.substring(2)).toCharArray(), sb);
                            continue;
                        }
                    }
                }
                if (rct.getPreviousG0() != ASCII)
                {
                    sb.append(ESC);
                    sb.append(G0);
                    sb.append((char) ASCII);
                    rct.setPreviousG0(ASCII);
                }
                if (charValue < 0x1000) 
                    marc.append("&#x"+Integer.toHexString(charValue + 0x10000).toUpperCase().substring(1)+";");
                else
                    marc.append("&#x"+Integer.toHexString(charValue).toUpperCase()+";");
               //continue;
            }            
            else if (rct.inPreviousG0CharEntry(c))
            {
                marc.append(rct.getCurrentG0CharEntry(c));
            } 
            else if (rct.inPreviousG1CharEntry(c))
            {
                marc.append(rct.getCurrentG1CharEntry(c));
            } 
            else if (dontChangeCharset)
            {
                if (charValue < 0x1000) 
                    marc.append("&#x"+Integer.toHexString(charValue + 0x10000).toUpperCase().substring(1)+";");
                else
                    marc.append("&#x"+Integer.toHexString(charValue).toUpperCase()+";");
           //     continue;
            }
            else // need to change character set
            {
                // if several MARC-8 character sets contain the given Unicode character, select the
                // best char set to use for encoding the character.  Preference is given to character
                // sets that have been used previously in the field being encoded.  Since the default
                // character sets for Basic and extended latin are pre-loaded, usually if a character
                // can be encoded by one of those character sets, that is what will be chosen.
                int charset = rct.getBestCharSet(c);
                char[] marc8 = rct.getCharEntry(c, charset);

                if (marc8.length == 3) 
                {
                    marc.append(ESC);
                    marc.append(G0multibyte);
                    rct.setPreviousG0(charset);
                } 
                else if (marc8[0] < 0x80) 
                {
                    marc.append(ESC);
                    if (charset == 0x62 || charset == 0x70) 
                    {
//                        technique1 = true;
                    } 
                    else 
                    {
                        marc.append(G0);
                    }
                    rct.setPreviousG0(charset);
                } 
                else 
                {
                    marc.append(ESC);
                    marc.append(G1);
                    rct.setPreviousG1(charset);
                }
                marc.append((char) charset);
                marc.append(marc8);
            }

            if (rct.isCombining(c) && sb.length() > 0)
            {
                sb.insert(sb.length() - prev_len, marc);
                
                // Special case handling to handle the COMBINING DOUBLE INVERTED BREVE 
                // and the COMBINING DOUBLE TILDE where a single double wide accent character
                // in unicode is represented by two half characters in Marc8
                if (((int)c)== 0x360)  sb.append((char)(0xfb));
                if (((int)c)== 0x361)  sb.append((char)(0xec));
            }
            else
            {
                sb.append(marc);
            }
            prev_len = marc.length();
        }    
    }

    private static boolean allCharsHaveMatch(ReverseCodeTable rct, String str)
    {
        for (char c : str.toCharArray())
        {
            if (!rct.charHasMatch(c))  return(false);
        }
        return true;
    }

}
