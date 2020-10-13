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

import org.marc4j.converter.CharConverter;

import java.lang.reflect.Constructor;
import java.text.Normalizer;
import java.util.Hashtable;

/**
 * <p>
 * A utility to convert UCS/Unicode data to Unimarc.
 * </p>
 *
 * @author SirsiDynix, from Bas Peters original UnicodeToAnsel
 */
public class UnicodeToUnimarc extends CharConverter implements UnimarcConstants {
    protected ReverseCodeTable rct;

    private CodeTableTracker defaultCodeTableTracker = new CodeTableTracker();
    private int workingG0;
    private int workingG1;

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
     * No-arg constructor.
     */
    public UnicodeToUnimarc() {
        rct = loadGeneratedTable();
        resetDefaultGX();
    }

    private ReverseCodeTable loadGeneratedTable() {
        try {
            final Class<?> generated = Class.forName("org.marc4j.converter.impl.UnimarcReverseCodeTableGenerated");
            final Constructor<?> cons = generated.getConstructor();
            final Object rct = cons.newInstance();

            return (ReverseCodeTable) rct;
        } catch (final Exception e) {
            return new UnimarcReverseCodeTableHash(getClass().getResourceAsStream("resources/unimarc.xml"));
        }
    }

    /**
     * Resets the G0 and G1 charsets to the defaults (ASCII/ANSEL)
     */
    public void resetDefaultGX() {
        defaultCodeTableTracker.setPrevious(CodeTableTracker.G0, DEFAULT_G0);
        defaultCodeTableTracker.setPrevious(CodeTableTracker.G1, DEFAULT_G1);
        defaultCodeTableTracker.setPrevious(CodeTableTracker.G2, DEFAULT_G2);
        defaultCodeTableTracker.setPrevious(CodeTableTracker.G3, DEFAULT_G3);
    }

    /**
     * Allows the caller to set the default G0/G1/G2/G3 char sets
     *
     * @param altG0Code string pulled from 100 $a/26-27
     * @param altG1Code string pulled from 100 $a/28-29
     * @param altG2Code string pulled from 100 $a/30-31
     * @param altG3Code string pulled from 100 $a/32-33
     */
    public void setDefaultGX(String altG0Code, String altG1Code, String altG2Code, String altG3Code) {
        int iso = UnimarcCommon.determineCharSet(altG0Code);
        if (iso > 0) {
            defaultCodeTableTracker.setPrevious(CodeTableTracker.G0, iso);
        }

        iso = UnimarcCommon.determineCharSet(altG1Code);
        if (iso > 0) {
            defaultCodeTableTracker.setPrevious(CodeTableTracker.G1, iso);
        }

        iso = UnimarcCommon.determineCharSet(altG2Code);
        if (iso > 0) {
            defaultCodeTableTracker.setPrevious(CodeTableTracker.G2, iso);
        }

        iso = UnimarcCommon.determineCharSet(altG3Code);
        if (iso > 0) {
            defaultCodeTableTracker.setPrevious(CodeTableTracker.G3, iso);
        }
    }


    /**
     * Converts UCS/Unicode data to UNIMARC.
     * <p>
     * If there is no match for a Unicode character, it will be encoded as &amp;#xXXXX; or &lt;U+XXXX&gt; (depending
     * on Marc4jConfig setting) so that if the data is translated back into Unicode, the original data can be recreated.
     * </p>
     *
     * @param dataElement the UCS/Unicode data
     * @return String - the UNIMARC data
     */
    public String convert(char[] dataElement) {
        char[] data;
        if (shouldDecomposeUnicode()) {
            data = Normalizer.normalize(new String(dataElement), Normalizer.Form.NFD).toCharArray();
            data = FixDoubleWidth.decomposeCombinedDoubleChar(data);
        } else {
            data = dataElement;
        }

        StringBuilder sb = new StringBuilder();
        StringBuilder marc = new StringBuilder();
        CodeTableTracker ctt = new CodeTableTracker(defaultCodeTableTracker);
        workingG0 = ctt.getPrevious(CodeTableTracker.G0);
        workingG1 = ctt.getPrevious(CodeTableTracker.G1);

        for (int i = 0; i < data.length; i++) {
            Character c = data[i];
            Integer table;
            marc.setLength(0);
            Hashtable h = rct.codeTableHash(c);

            if (h == null) {
                marc.append(unicodeToNCR(c, ctt));
            } else if (h.keySet().contains(workingG0)) {
                char[] ch = (char[]) h.get(workingG0);
                if (ch.length == 1 && ch[0] >= 0x80) {
                    ch[0] = (char) ((int) ch[0] - 0x80);
                }
                marc.append(ch);
            } else if (h.keySet().contains(workingG1)) {
                char[] ch = (char[]) h.get(workingG1);
                if (ch.length == 1 && ch[0] < 0x80) {
                    ch[0] = (char) ((int) ch[0] + 0x80);
                }
                marc.append(ch);
            } else if (h.keySet().contains(ctt.getPrevious(CodeTableTracker.G0))) {
                ctt.makePreviousCurrent();
                marc.append(LS0);
                workingG0 = ctt.getPrevious(CodeTableTracker.G0);
                char[] ch = (char[]) h.get(workingG0);
                if (ch.length == 1 && ch[0] >= 0x80) {
                    ch[0] = (char) ((int) ch[0] - 0x80);
                }
                marc.append(ch);
            } else if (h.keySet().contains(ctt.getPrevious(CodeTableTracker.G1))) {
                ctt.makePreviousCurrent();
                marc.append(ESC);
                marc.append(LS1R);
                workingG1 = ctt.getPrevious(CodeTableTracker.G1);
                char[] ch = (char[]) h.get(ctt.getPrevious(CodeTableTracker.G1));
                if (ch.length == 1 && ch[0] < 0x80) {
                    ch[0] = (char) ((int) ch[0] + 0x80);
                }
                marc.append(ch);
            } else if (ctt.getPrevious(CodeTableTracker.G2) != null && h.keySet().contains(ctt.getPrevious(CodeTableTracker.G2))) {
                ctt.makePreviousCurrent();
                marc.append(ESC);
                marc.append(LS2R); // place G2 in working g1
                workingG1 = ctt.getPrevious(CodeTableTracker.G2);

                char[] ch = (char[]) h.get(workingG1);
                if (ch.length == 1 && ch[0] < 0x80) {
                    ch[0] = (char) ((int) ch[0] + 0x80);
                }
                marc.append(ch);
            } else if (ctt.getPrevious(CodeTableTracker.G3) != null && h.keySet().contains(ctt.getPrevious(CodeTableTracker.G3))) {
                ctt.makePreviousCurrent();
                marc.append(ESC);
                marc.append(LS3R); // place G3 in working g1
                workingG1 = ctt.getPrevious(CodeTableTracker.G3);
                char[] ch = (char[]) h.get(workingG0);
                if (ch.length == 1 && ch[0] < 0x80) {
                    ch[0] = (char) ((int) ch[0] + 0x80);
                }
                marc.append(ch);
            } else {
                table = (Integer) h.keySet().iterator().next();
                char[] marc8 = (char[]) h.get(table);

                if (marc8.length == 3) {
                    marc.append(ESC);
                    marc.append((char) 0x24);
                    marc.append((char) 0x29);
                    marc.append((char) table.intValue());
                    marc.append(ESC);
                    marc.append(LS1R);
                    workingG1 = table;
                    ctt.setPrevious(CodeTableTracker.G1, table);
                } else {
                    marc.append(ESC);
                    marc.append((char) 0x29);
                    marc.append((char) table.intValue());
                    marc.append(ESC);
                    marc.append(LS1R);
                    workingG1 = table;
                    ctt.setPrevious(CodeTableTracker.G1, table);
                    if (marc8[0] < 0x80) {
                        marc8[0] = (char) ((int) marc8[0] + 0x80);
                    }
                }
                marc.append(marc8);
            }

            if (rct.isCombining(c) && sb.length() > 0) {
                sb.insert(sb.length() - 1, marc);
            } else {
                sb.append(marc);
            }
        }


        if (defaultCodeTableTracker.getPrevious(CodeTableTracker.G3) != null &&
            !defaultCodeTableTracker.getPrevious(CodeTableTracker.G3).equals(ctt.getPrevious(CodeTableTracker.G3))) {
            sb.append(ESC);
            sb.append((char) 0x2F);
            sb.append((char) defaultCodeTableTracker.getPrevious(CodeTableTracker.G3).intValue());
        }

        if (defaultCodeTableTracker.getPrevious(CodeTableTracker.G2) != null &&
            !defaultCodeTableTracker.getPrevious(CodeTableTracker.G2).equals(ctt.getPrevious(CodeTableTracker.G2))) {
            sb.append(ESC);
            sb.append((char) 0x2E);
            sb.append((char) defaultCodeTableTracker.getPrevious(CodeTableTracker.G2).intValue());
        }


        if (!defaultCodeTableTracker.getPrevious(CodeTableTracker.G1).equals(ctt.getPrevious(CodeTableTracker.G1))) {
            sb.append(ESC);
            sb.append((char) 0x2D);
            sb.append((char) defaultCodeTableTracker.getPrevious(CodeTableTracker.G1).intValue());
        }

        if (!defaultCodeTableTracker.getPrevious(CodeTableTracker.G0).equals(ctt.getPrevious(CodeTableTracker.G0))) {
            sb.append(ESC);
            sb.append((char) 0x2C);
            sb.append((char) defaultCodeTableTracker.getPrevious(CodeTableTracker.G0).intValue());
        }

        if (workingG1 != defaultCodeTableTracker.getPrevious(CodeTableTracker.G1)) {
            sb.append(ESC);
            sb.append(LS1R);
        }

        if (workingG0 != defaultCodeTableTracker.getPrevious(CodeTableTracker.G0)) {
            sb.append(LS0);
        }

        return sb.toString();
    }

    /**
     * Handle a Unicode character that has no Unimarc equivalent.  Uses UnicodeToUnimarc with appropriate G0 - G3
     * settings, convert the character to a NCR (either &amp;#xXXXX; or &lt;U+XXXX&gt; format depending on
     * Marc4jConfig), then run through the converter.
     * @param ch - character with no Marc8 equivalent
     * @param currentTracker - Current G0 / G1 tracker
     * @return NCR string.
     */
    private String unicodeToNCR(Character ch, CodeTableTracker currentTracker) {
        String ncr = UnicodeUtils.convertUnicodeToNCR(ch);

        UnicodeToUnimarc converter = new UnicodeToUnimarc();
        converter.defaultCodeTableTracker.setPrevious(CodeTableTracker.G0, currentTracker.getPrevious(CodeTableTracker.G0));
        converter.defaultCodeTableTracker.setPrevious(CodeTableTracker.G1, currentTracker.getPrevious(CodeTableTracker.G1));
        converter.defaultCodeTableTracker.setPrevious(CodeTableTracker.G2, currentTracker.getPrevious(CodeTableTracker.G2));
        converter.defaultCodeTableTracker.setPrevious(CodeTableTracker.G3, currentTracker.getPrevious(CodeTableTracker.G3));

        StringBuilder sb = new StringBuilder();

        if (workingG0 != currentTracker.getPrevious(CodeTableTracker.G0)) {
            sb.append(LS0);
        }
        if (workingG1 != currentTracker.getPrevious(CodeTableTracker.G1)) {
            sb.append(ESC);
            sb.append(LS1R);
        }
        sb.append(converter.convert(ncr));

        workingG0 = converter.workingG0;
        workingG1 = converter.workingG1;

        return sb.toString();
    }

}
