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

import org.marc4j.converter.CharConverter;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.text.Normalizer;

/**
 * <p>
 * A utility to convert UCS/Unicode data to MARC-8.
 * </p>
 * <p>
 * The MARC-8 to Unicode mapping used is the version with the March 2005 revisions.
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
    static final int ANSEL = 0x45;

    private CodeTableTracker defaultCodeTableTracker = new CodeTableTracker();

    boolean dontChangeCharset = false;

    // flag that indicates we should de-normalize the results of the convert method (i.e. de-compose any
    // composed Unicode characters.  Default false.
    protected boolean decomposeUnicode = false;

    /**
     * Returns true if Unicode composed characters should be decomposed.
     *
     * @return True if we should decompose Unicode characters, else, false to leave them alone.
     */
    public boolean shouldDecomposeUnicode() {
        return decomposeUnicode;
    }

    /**
     * Sets whether we should decompose Unicode composed charactes.
     *
     * @param decomposeUnicode True if we should decompose Unicode characters, else, false.  Default false.
     */
    public void setDecomposeUnicode(boolean decomposeUnicode) {
        this.decomposeUnicode = decomposeUnicode;
    }

    /**
     * Creates a new instance and loads the MARC4J supplied Ansel/Unicode conversion tables based on the official LC
     * tables. Loads in the generated class ReverseCodeTableGenerated which contains switch statements to lookup the
     * MARC-8 encodings for given Unicode characters.
     */
    public UnicodeToAnsel() {
        rct = loadGeneratedTable();
        // this(UnicodeToAnsel.class
        // .getResourceAsStream("resources/codetables.xml"));
    }

    /**
     * Creates a new instance and loads the MARC4J supplied Ansel/Unicode conversion tables based on the official LC
     * tables. Loads in the generated class ReverseCodeTableGenerated which contains switch statements to lookup the
     * MARC-8 encodings for given Unicode characters.
     *
     * @param defaultCharsetOnlyPlusNCR - true to enable special mode where <i>no alternate character sets are used</i>
     *        anything outside the standard base MARC8 lookup table will be written out using a Numeric Code Point
     */
    public UnicodeToAnsel(final boolean defaultCharsetOnlyPlusNCR) {
        dontChangeCharset = true;
        rct = loadGeneratedTable();
        // this(UnicodeToAnsel.class
        // .getResourceAsStream("resources/codetables.xml"));
    }

    /**
     * Constructs an instance with the specified pathname.
     *
     * Use this constructor to create an instance with a customized code table
     * mapping. The mapping file should follow the structure of LC's XML MARC-8
     * to Unicode mapping (see:
     * http://www.loc.gov/marc/specifications/codetables.xml).
     *
     * @param pathname - the name of a file to read to built the ReverseCodeTable instead of the standard LC table
     */
    public UnicodeToAnsel(final String pathname) {
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
     * @param in - an InputStream to read to built the ReverseCodeTable instead of the standard LC table
     */
    public UnicodeToAnsel(final InputStream in) {
        rct = new ReverseCodeTableHash(in);
    }

    private ReverseCodeTable loadGeneratedTable() {
        try {
            final Class<?> generated = Class.forName("org.marc4j.converter.impl.ReverseCodeTableGenerated");
            final Constructor<?> cons = generated.getConstructor();
            final Object rct = cons.newInstance();

            return (ReverseCodeTable) rct;
        } catch (final Exception e) {
            ReverseCodeTable rct;

            rct = new ReverseCodeTableHash(AnselToUnicode.class.getResourceAsStream("resources/codetables.xml"));

            return rct;
        }
    }

    /**
     * Resets the G0 and G1 charsets to the defaults (ASCII/ANSEL)
     */
    public void resetDefaultG0AndG1() {
        defaultCodeTableTracker.setPrevious(CodeTableTracker.G0, ASCII);
        defaultCodeTableTracker.setPrevious(CodeTableTracker.G1, ANSEL);
    }

    /**
     * Allows the caller to set the default G0/G1 char sets
     * @param altG0Code string pulled from 066 $a
     * @param altG1Code string pulled from 066 $b
     */
    public void setDefaultG0AndG1(String altG0Code, String altG1Code) {
        if (altG0Code != null && altG0Code.length() > 0) {
            char ch = altG0Code.charAt(altG0Code.length()-1);
            defaultCodeTableTracker.setPrevious(CodeTableTracker.G0, (int) ch);
        }
        if (altG1Code != null && altG1Code.length() > 0) {
            char ch = altG1Code.charAt(altG1Code.length()-1);
            defaultCodeTableTracker.setPrevious(CodeTableTracker.G1, (int) ch);
        }
    }

    /**
     * Converts UCS/Unicode data to MARC-8.
     * <p>
     * If there is no match for a Unicode character, it will be encoded as &amp;#xXXXX; or &lt;U+XXXX&gt; (depending
     * on Marc4jConfig setting) so that if the data is translated back into Unicode, the original data can be recreated.
     * </p>
     *
     * @param dataElement - the UCS/Unicode data in an array of char
     * @return String - the MARC-8 data
     */
    @Override
    public String convert(final char dataElement[]) {
        char[] data;
        if (shouldDecomposeUnicode()) {
            data = Normalizer.normalize(new String(dataElement), Normalizer.Form.NFD).toCharArray();
            data = FixDoubleWidth.decomposeCombinedDoubleChar(data);
        } else {
            data = dataElement;
        }

        final StringBuilder sb = new StringBuilder();

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
     * <p>
     * If the Unicode data has been normalized into composed form, and the composed character does not have a
     * corresponding MARC8 character, this routine will normalize that character into its decomposed form, and try to
     * translate that equivalent string into MARC8.
     * </p>
     *
     * @param data - the UCS/Unicode data in an array of char
     * @param sb   - The StringBuilder MARC-8 data object to receive the converted data.
     */
    private void convertPortion(final char data[], final StringBuilder sb) {
        int prev_len = 1;

        final StringBuilder marc = new StringBuilder();
        CodeTableTracker ctt = new CodeTableTracker(defaultCodeTableTracker);

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < data.length; i++) {
            final Character c = data[i];
            marc.setLength(0);
            final int charValue = c;

            if (charValue == 0x20 && rct.getPreviousG0() != '1') {
                if (rct.getPreviousG0() == '1') {
                    sb.append(ESC);
                    sb.append(G0);
                    sb.append((char) ASCII);
                    rct.setPreviousG0(ASCII);
                }

                marc.append(" ");
            } else if (!rct.charHasMatch(c)) {
                // Unicode character c has no match in the Marc8 tables. Try unicode-decompose on it
                // to see whether the decomposed form can be represented. If when decomposed, all of
                // the characters can be translated to marc8, then use that. If not and the decomposed form
                // if three (or more) characters long (which indicates multiple diacritic marks), then
                // re-compose the the main character with the first diacritic, and check whether that
                // and the remaining diacritics can be translated. If so go with that, otherwise, give up
                // and merely use the &#xXXXX; or <U+XXXX> Numeric Character Reference form (depending on Marc4jConfig)
                // to represent the original unicode character
                final String tmpnorm = c.toString();
                final String tmpNormed = Normalizer.normalize(tmpnorm, Normalizer.Form.NFD);

                if (!tmpNormed.equals(tmpnorm)) {
                    if (allCharsHaveMatch(rct, tmpNormed)) {
                        convertPortion(tmpNormed.toCharArray(), sb);
                        continue;
                    } else if (tmpNormed.length() > 2) {
                        final String firstTwo = tmpNormed.substring(0, 2);
                        final String partialNormed = Normalizer.normalize(firstTwo, Normalizer.Form.NFC);

                        if (!partialNormed.equals(firstTwo) && allCharsHaveMatch(rct, partialNormed) &&
                                allCharsHaveMatch(rct, tmpNormed.substring(2))) {
                            convertPortion((partialNormed + tmpNormed.substring(2)).toCharArray(), sb);
                            continue;
                        }
                    }
                }

                if (rct.getPreviousG0() != ASCII) {
                    sb.append(ESC);
                    sb.append(G0);
                    sb.append((char) ASCII);
                    rct.setPreviousG0(ASCII);
                }

                marc.append(unicodeToNCR(c, ctt));
            } else if (rct.inPreviousG0CharEntry(c)) {
                marc.append(rct.getCurrentG0CharEntry(c));
                ctt.makePreviousCurrent();
            } else if (rct.inPreviousG1CharEntry(c)) {
                marc.append(rct.getCurrentG1CharEntry(c));
                ctt.makePreviousCurrent();
            } else if (dontChangeCharset) {
                marc.append(UnicodeUtils.convertUnicodeToNCR(c));
            } else if (rct.codeTableHash(c).keySet().contains(ctt.getPrevious(CodeTableTracker.G1))) {
                ctt.makePreviousCurrent();
                char[] ch = rct.getCharEntry(c, ctt.getPrevious(CodeTableTracker.G1));
                if (ch.length == 1 && ch[0] < 0x80) {
                    ch[0] = (char) (ch[0] + 0x80);
                }
                marc.append(ch);
            } else {
                // need to change character set
                // if several MARC-8 character sets contain the given Unicode character, select the
                // best char set to use for encoding the character. Preference is given to character
                // sets that have been used previously in the field being encoded. Since the default
                // character sets for Basic and extended latin are pre-loaded, usually if a character
                // can be encoded by one of those character sets, that is what will be chosen.
                final int charset = rct.getBestCharSet(c);
                final char[] marc8 = rct.getCharEntry(c, charset);

                if (marc8.length == 3) {
                    marc.append(ESC);
                    marc.append(G0multibyte);
                    rct.setPreviousG0(charset);
                    ctt.setPrevious(CodeTableTracker.G0, charset);
                } else if (marc8[0] < 0x80) {
                    marc.append(ESC);

                    if (charset == 0x62 || charset == 0x70) {
                        // technique1 = true;
                    } else {
                        marc.append(G0);
                    }

                    rct.setPreviousG0(charset);
                    ctt.setPrevious(CodeTableTracker.G0, charset);
                } else {
                    marc.append(ESC);
                    marc.append(G1);
                    rct.setPreviousG1(charset);
                    ctt.setPrevious(CodeTableTracker.G1, charset);
                }

                marc.append((char) charset);
                marc.append(marc8);
            }

            if (rct.isCombining(c) && sb.length() > 0) {
                sb.insert(sb.length() - prev_len, marc);

                // Special case handling to handle the COMBINING DOUBLE INVERTED BREVE
                // and the COMBINING DOUBLE TILDE where a single double wide accent character
                // in unicode is represented by two half characters in Marc8
                if ((int) c == 0x360) {
                    sb.append((char) 0xfb);
                }

                if ((int) c == 0x361) {
                    sb.append((char) 0xec);
                }
            } else {
                sb.append(marc);
            }

            prev_len = marc.length();
        }

        if (ctt.getPrevious(CodeTableTracker.G1).intValue()
                != defaultCodeTableTracker.getPrevious(CodeTableTracker.G1).intValue()) {
            sb.append(ESC);
            sb.append(G1);
            sb.append((char) defaultCodeTableTracker.getPrevious(CodeTableTracker.G1).intValue());
        }

        if (ctt.getPrevious(CodeTableTracker.G0).intValue()
                != defaultCodeTableTracker.getPrevious(CodeTableTracker.G0).intValue()) {
            sb.append(ESC);
            sb.append(G0);
            sb.append((char) defaultCodeTableTracker.getPrevious(CodeTableTracker.G0).intValue());
        }
    }

    private static boolean allCharsHaveMatch(final ReverseCodeTable rct, final String str) {
        for (final char c : str.toCharArray()) {
            if (!rct.charHasMatch(c)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Handle a Unicode character that has no Marc8 equivalent.  Uses UnicodeToAnsel with appropriate G0 and G1
     * settings, convert the character to a NCR (either &amp;#xXXXX; or &lt;U+XXXX&gt; format depending on
     * Marc4jConfig), then run through the converter.
     * @param ch - character with no Marc8 equivalent
     * @param currentTracker - Current G0 / G1 tracker
     * @return NCR string.
     */
    private String unicodeToNCR(Character ch, CodeTableTracker currentTracker) {
        String ncr = UnicodeUtils.convertUnicodeToNCR(ch);

        UnicodeToAnsel converter = new UnicodeToAnsel();
        converter.defaultCodeTableTracker.setPrevious(CodeTableTracker.G0, currentTracker.getPrevious(CodeTableTracker.G0));
        converter.defaultCodeTableTracker.setPrevious(CodeTableTracker.G1, currentTracker.getPrevious(CodeTableTracker.G1));
        return converter.convert(ncr);
    }

}
