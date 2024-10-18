/**
 * Copyright (C) 2002 Bas  Peters
 * Copyright (C) 2002 Yves Pratter
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

/**
 * <p>
 * A utility to convert UNIMARC data to UCS/Unicode.
 * </p>
 * 
 * @author Bas Peters
 * @author Yves Pratter
 */
public class Iso5426ToUnicode extends CharConverter {

    /**
     * <p>
     * Converts UNIMARC (ISO 5426 charset) data to UCS/Unicode.
     * </p>
     * 
     * @param data - the UNIMARC data in an array of char
     * @return {@link String}- the UCS/Unicode data
     */
    @Override
    public String convert(final char data[]) {
        final StringBuffer sb = new StringBuffer();

        for (int i = 0; i < data.length; i++) {
            final char c = data[i];
            final int len = data.length;
            if (isAscii(c)) {
                sb.append(c);
            } else if (isCombining(c) && hasNext(i, len)) {
                final char d = getCombiningChar(c * 256 + data[i + 1]);
                if (d != 0) {
                    sb.append(d);
                    i++;
                } else {
                    sb.append(getChar(c));
                }
            } else {
                sb.append(getChar(c));
            }
        }
        return sb.toString();
    }

    private boolean hasNext(final int pos, final int len) {
        if (pos < (len - 1)) {
            return true;
        }
        return false;
    }

    private boolean isAscii(final int i) {
        if (i >= 0x00 && i <= 0x7F) {
            return true;
        }
        return false;
    }

    private boolean isCombining(final int i) {
        // if (i > 0xE0 && i < 0xFF)
        if (i >= 0xC0 && i <= 0xDF) {
            return true;
        }
        return false;
    }

    /**
     * Should return true if the CharConverter outputs Unicode encoded
     * characters
     * 
     * @return boolean whether the CharConverter returns Unicode encoded
     *         characters
     */
    @Override
    public boolean outputsUnicode() {
        return (true);
    }

    // Source: http://www.unicode.org/L2/L2000/00220-map-5426.pdf
    // Finalized Mapping between Characters of ISO 5426 and ISO/IEC 10646-1 (UCS)
    private char getChar(final int i) {
        switch (i) {
            case 0xA1:
                return 0x00A1; // 2/1 inverted exclamation mark
            case 0xA2:
                return 0x201E; // 2/2 left low double quotation mark
            case 0xA3:
                return 0x00A3; // 2/3 pound sign
            case 0xA4:
                return 0x0024; // 2/4 dollar sign
            case 0xA5:
                return 0x00A5; // 2/5 yen sign
            case 0xA6:
                return 0x2020; // 2/6 single dagger
            case 0xA7:
                return 0x00A7; // 2/7 paragraph (section)
            case 0xA8:
                return 0x2032; // 2/8 prime
            case 0xA9:
                return 0x2018; // 2/9 left high single quotation mark
            case 0xAA:
                return 0x201C; // 2/10 left high double quotation mark
            case 0xAB:
                return 0x00AB; // 2/11 left angle quotation mark
            case 0xAC:
                return 0x266D; // 2/12 music flat
            case 0xAD:
                return 0x00A9; // 2/13 copyright sign
            case 0xAE:
                return 0x2117; // 2/14 sound recording copyright sign
            case 0xAF:
                return 0x00AE; // 2/15 trade mark sign

            case 0xB0:
                return 0x02BB; // 3/0 ayn
            case 0xB1:
                return 0x02BC; // 3/1 alif/hamzah
            case 0xB2:
                return 0x201A; // 3/2 left low single quotation mark
                // 3/3 (this position shall not be used)
                // 3/4 (this position shall not be used)
                // 3/5 (this position shall not be used)
            case 0xB6:
                return 0x2021; // 3/6 double dagger
            case 0xB7:
                return 0x00B7; // 3/7 middle dot
            case 0xB8:
                return 0x2033; // 3/8 double prime
            case 0xB9:
                return 0x2019; // 3/9 right high single quotation mark
            case 0xBA:
                return 0x201D; // 3/10 right high double quotation mark
            case 0xBB:
                return 0x00BB; // 3/11 right angle quotation mark
            case 0xBC:
                return 0x266F; // 3/12 musical sharp
            case 0xBD:
                return 0x02B9; // 3/13 mjagkij znak
            case 0xBE:
                return 0x02BA; // 3/14 tverdyj znak
            case 0xBF:
                return 0x00BF; // 3/15 inverted question mark

                // 4/0 to 5/15 diacritic characters

                // 6/0 (this position shall not be used)
            case 0xE1:
                return 0x00C6; // 6/1 CAPITAL DIPHTHONG A WITH E
            case 0xE2:
                return 0x0110; // 6/2 CAPITAL LETTER D WITH STROKE
                // 6/3 (this position shall not be used)
                // 6/4 (this position shall not be used)
                // 6/5 (this position shall not be used)
            case 0xE6:
                return 0x0132; // 6/6 CAPITAL LETTER IJ
                // 6/7 (this position shall not be used)
            case 0xE8:
                return 0x0141; // 6/8 CAPITAL LETTER L WITH STROKE
            case 0xE9:
                return 0x00D8; // 6/9 CAPITAL LETTER O WITH SOLIDUS [oblique
                               // stroke]
            case 0xEA:
                return 0x0152; // 6/10 CAPITAL DIPHTONG OE
                // 6/11 (this position shall not be used)
            case 0xEC:
                return 0x00DE; // 6/12 CAPITAL LETTER THORN
                // 6/13 (this position shall not be used)
                // 6/14 (this position shall not be used)
                // 6/15 (this position shall not be used)

                // 7/0 (this position shall not be used)
            case 0xF1:
                return 0x00E6; // 7/1 small diphthong a with e
            case 0xF2:
                return 0x0111; // 7/2 small letter d with stroke
            case 0xF3:
                return 0x00F0; // 7/3 small letter eth
                // 7/4 (this position shall not be used)
            case 0xF5:
                return 0x0131; // 7/5 small letter i without dot
            case 0xF6:
                return 0x0133; // 7/6 small letter ij
                // 7/7 (this position shall not be used)
            case 0xF8:
                return 0x0142; // 7/8 small letter l with stroke
            case 0xF9:
                return 0x00F8; // 7/9 small letter o with solidus (oblique
                               // stroke)
            case 0xFA:
                return 0x0153; // 7/10 small diphtong oe
            case 0xFB:
                return 0x00DF; // 7/11 small letter sharp s
            case 0xFC:
                return 0x00FE; // 7/12 small letter thorn
                // 7/13 (this position shall not be used)
                // 7/14 (this position shall not be used)
            default:
                return (char) i;
        }
    }

    private char getCombiningChar(final int i) {
        switch (i) {
        // 4/0 low rising tone mark
            case 0xC041:
                return 0x1EA2; // CAPITAL A WITH HOOK ABOVE
            case 0xC045:
                return 0x1EBA; // CAPITAL E WITH HOOK ABOVE
            case 0xC049:
                return 0x1EC8; // CAPITAL I WITH HOOK ABOVE
            case 0xC04F:
                return 0x1ECE; // CAPITAL O WITH HOOK ABOVE
            case 0xC055:
                return 0x1EE6; // CAPITAL U WITH HOOK ABOVE
            case 0xC059:
                return 0x1EF6; // CAPITAL Y WITH HOOK ABOVE
            case 0xC061:
                return 0x1EA3; // small a with hook above
            case 0xC065:
                return 0x1EBB; // small e with hook above
            case 0xC069:
                return 0x1EC9; // small i with hook above
            case 0xC06F:
                return 0x1ECF; // small o with hook above
            case 0xC075:
                return 0x1EE7; // small u with hook above
            case 0xC079:
                return 0x1EF7; // small y with hook above

                // 4/1 grave accent
            case 0xC141:
                return 0x00C0; // CAPITAL A WITH GRAVE ACCENT
            case 0xC145:
                return 0x00C8; // CAPITAL E WITH GRAVE ACCENT
            case 0xC149:
                return 0x00CC; // CAPITAL I WITH GRAVE ACCENT
            case 0xC14F:
                return 0x00D2; // CAPITAL O WITH GRAVE ACCENT
            case 0xC155:
                return 0x00D9; // CAPITAL U WITH GRAVE ACCENT
            case 0xC157:
                return 0x1E80; // CAPITAL W WITH GRAVE
            case 0xC159:
                return 0x1EF2; // CAPITAL Y WITH GRAVE
            case 0xC161:
                return 0x00E0; // small a with grave accent
            case 0xC165:
                return 0x00E8; // small e with grave accent
            case 0xC169:
                return 0x00EC; // small i with grave accent
            case 0xC16F:
                return 0x00F2; // small o with grave accent
            case 0xC175:
                return 0x00F9; // small u with grave accent
            case 0xC177:
                return 0x1E81; // small w with grave
            case 0xC179:
                return 0x1EF3; // small y with grave

                // 4/2 acute accent
            case 0xC241:
                return 0x00C1; // CAPITAL A WITH ACUTE ACCENT
            case 0xC243:
                return 0x0106; // CAPITAL C WITH ACUTE ACCENT
            case 0xC245:
                return 0x00C9; // CAPITAL E WITH ACUTE ACCENT
            case 0xC247:
                return 0x01F4; // CAPITAL G WITH ACUTE
            case 0xC249:
                return 0x00CD; // CAPITAL I WITH ACUTE ACCENT
            case 0xC24B:
                return 0x1E30; // CAPITAL K WITH ACUTE
            case 0xC24C:
                return 0x0139; // CAPITAL L WITH ACUTE ACCENT
            case 0xC24D:
                return 0x1E3E; // CAPITAL M WITH ACUTE
            case 0xC24E:
                return 0x0143; // CAPITAL N WITH ACUTE ACCENT
            case 0xC24F:
                return 0x00D3; // CAPITAL O WITH ACUTE ACCENT
            case 0xC250:
                return 0x1E54; // CAPITAL P WITH ACUTE
            case 0xC252:
                return 0x0154; // CAPITAL R WITH ACUTE ACCENT
            case 0xC253:
                return 0x015A; // CAPITAL S WITH ACUTE ACCENT
            case 0xC255:
                return 0x00DA; // CAPITAL U WITH ACUTE ACCENT
            case 0xC257:
                return 0x1E82; // CAPITAL W WITH ACUTE
            case 0xC259:
                return 0x00DD; // CAPITAL Y WITH ACUTE ACCENT
            case 0xC25A:
                return 0x0179; // CAPITAL Z WITH ACUTE ACCENT
            case 0xC261:
                return 0x00E1; // small a with acute accent
            case 0xC263:
                return 0x0107; // small c with acute accent
            case 0xC265:
                return 0x00E9; // small e with acute accent
            case 0xC267:
                return 0x01F5; // small g with acute
            case 0xC269:
                return 0x00ED; // small i with acute accent
            case 0xC26B:
                return 0x1E31; // small k with acute
            case 0xC26C:
                return 0x013A; // small l with acute accent
            case 0xC26D:
                return 0x1E3F; // small m with acute
            case 0xC26E:
                return 0x0144; // small n with acute accent
            case 0xC26F:
                return 0x00F3; // small o with acute accent
            case 0xC270:
                return 0x1E55; // small p with acute
            case 0xC272:
                return 0x0155; // small r with acute accent
            case 0xC273:
                return 0x015B; // small s with acute accent
            case 0xC275:
                return 0x00FA; // small u with acute accent
            case 0xC277:
                return 0x1E83; // small w with acute
            case 0xC279:
                return 0x00FD; // small y with acute accent
            case 0xC27A:
                return 0x017A; // small z with acute accent
            case 0xC2E1:
                return 0x01FC; // CAPITAL AE WITH ACUTE
            case 0xC2F1:
                return 0x01FD; // small ae with acute

                // 4/3 circumflex accent
            case 0xC341:
                return 0x00C2; // CAPITAL A WITH CIRCUMFLEX ACCENT
            case 0xC343:
                return 0x0108; // CAPITAL C WITH CIRCUMFLEX
            case 0xC345:
                return 0x00CA; // CAPITAL E WITH CIRCUMFLEX ACCENT
            case 0xC347:
                return 0x011C; // CAPITAL G WITH CIRCUMFLEX
            case 0xC348:
                return 0x0124; // CAPITAL H WITH CIRCUMFLEX
            case 0xC349:
                return 0x00CE; // CAPITAL I WITH CIRCUMFLEX ACCENT
            case 0xC34A:
                return 0x0134; // CAPITAL J WITH CIRCUMFLEX
            case 0xC34F:
                return 0x00D4; // CAPITAL O WITH CIRCUMFLEX ACCENT
            case 0xC353:
                return 0x015C; // CAPITAL S WITH CIRCUMFLEX
            case 0xC355:
                return 0x00DB; // CAPITAL U WITH CIRCUMFLEX
            case 0xC357:
                return 0x0174; // CAPITAL W WITH CIRCUMFLEX
            case 0xC359:
                return 0x0176; // CAPITAL Y WITH CIRCUMFLEX
            case 0xC35A:
                return 0x1E90; // CAPITAL Z WITH CIRCUMFLEX
            case 0xC361:
                return 0x00E2; // small a with circumflex accent
            case 0xC363:
                return 0x0109; // small c with circumflex
            case 0xC365:
                return 0x00EA; // small e with circumflex accent
            case 0xC367:
                return 0x011D; // small g with circumflex
            case 0xC368:
                return 0x0125; // small h with circumflex
            case 0xC369:
                return 0x00EE; // small i with circumflex accent
            case 0xC36A:
                return 0x0135; // small j with circumflex
            case 0xC36F:
                return 0x00F4; // small o with circumflex accent
            case 0xC373:
                return 0x015D; // small s with circumflex
            case 0xC375:
                return 0x00FB; // small u with circumflex
            case 0xC377:
                return 0x0175; // small w with circumflex
            case 0xC379:
                return 0x0177; // small y with circumflex
            case 0xC37A:
                return 0x1E91; // small z with circumflex

                // 4/4 tilde
            case 0xC441:
                return 0x00C3; // CAPITAL A WITH TILDE
            case 0xC445:
                return 0x1EBC; // CAPITAL E WITH TILDE
            case 0xC449:
                return 0x0128; // CAPITAL I WITH TILDE
            case 0xC44E:
                return 0x00D1; // CAPITAL N WITH TILDE
            case 0xC44F:
                return 0x00D5; // CAPITAL O WITH TILDE
            case 0xC455:
                return 0x0168; // CAPITAL U WITH TILDE
            case 0xC456:
                return 0x1E7C; // CAPITAL V WITH TILDE
            case 0xC459:
                return 0x1EF8; // CAPITAL Y WITH TILDE
            case 0xC461:
                return 0x00E3; // small a with tilde
            case 0xC465:
                return 0x1EBD; // small e with tilde
            case 0xC469:
                return 0x0129; // small i with tilde
            case 0xC46E:
                return 0x00F1; // small n with tilde
            case 0xC46F:
                return 0x00F5; // small o with tilde
            case 0xC475:
                return 0x0169; // small u with tilde
            case 0xC476:
                return 0x1E7D; // small v with tilde
            case 0xC479:
                return 0x1EF9; // small y with tilde

                // 4/5 macron
            case 0xC541:
                return 0x0100; // CAPITAL A WITH MACRON
            case 0xC545:
                return 0x0112; // CAPITAL E WITH MACRON
            case 0xC547:
                return 0x1E20; // CAPITAL G WITH MACRON
            case 0xC549:
                return 0x012A; // CAPITAL I WITH MACRON
            case 0xC54F:
                return 0x014C; // CAPITAL O WITH MACRON
            case 0xC555:
                return 0x016A; // CAPITAL U WITH MACRON
            case 0xC561:
                return 0x0101; // small a with macron
            case 0xC565:
                return 0x0113; // small e with macron
            case 0xC567:
                return 0x1E21; // small g with macron
            case 0xC569:
                return 0x012B; // small i with macron
            case 0xC56F:
                return 0x014D; // small o with macron
            case 0xC575:
                return 0x016B; // small u with macron
            case 0xC5E1:
                return 0x01E2; // CAPITAL AE WITH MACRON
            case 0xC5F1:
                return 0x01E3; // small ae with macron

                // 4/6 breve
            case 0xC641:
                return 0x0102; // CAPITAL A WITH BREVE
            case 0xC645:
                return 0x0114; // CAPITAL E WITH BREVE
            case 0xC647:
                return 0x011E; // CAPITAL G WITH BREVE
            case 0xC649:
                return 0x012C; // CAPITAL I WITH BREVE
            case 0xC64F:
                return 0x014E; // CAPITAL O WITH BREVE
            case 0xC655:
                return 0x016C; // CAPITAL U WITH BREVE
            case 0xC661:
                return 0x0103; // small a with breve
            case 0xC665:
                return 0x0115; // small e with breve
            case 0xC667:
                return 0x011F; // small g with breve
            case 0xC669:
                return 0x012D; // small i with breve
            case 0xC66F:
                return 0x014F; // small o with breve
            case 0xC675:
                return 0x016D; // small u with breve

                // 4/7 dot above
            case 0xC742:
                return 0x1E02; // CAPITAL B WITH DOT ABOVE
            case 0xC743:
                return 0x010A; // CAPITAL C WITH DOT ABOVE
            case 0xC744:
                return 0x1E0A; // CAPITAL D WITH DOT ABOVE
            case 0xC745:
                return 0x0116; // CAPITAL E WITH DOT ABOVE
            case 0xC746:
                return 0x1E1E; // CAPITAL F WITH DOT ABOVE
            case 0xC747:
                return 0x0120; // CAPITAL G WITH DOT ABOVE
            case 0xC748:
                return 0x1E22; // CAPITAL H WITH DOT ABOVE
            case 0xC749:
                return 0x0130; // CAPITAL I WITH DOT ABOVE
            case 0xC74D:
                return 0x1E40; // CAPITAL M WITH DOT ABOVE
            case 0xC74E:
                return 0x1E44; // CAPITAL N WITH DOT ABOVE
            case 0xC750:
                return 0x1E56; // CAPITAL P WITH DOT ABOVE
            case 0xC752:
                return 0x1E58; // CAPITAL R WITH DOT ABOVE
            case 0xC753:
                return 0x1E60; // CAPITAL S WITH DOT ABOVE
            case 0xC754:
                return 0x1E6A; // CAPITAL T WITH DOT ABOVE
            case 0xC757:
                return 0x1E86; // CAPITAL W WITH DOT ABOVE
            case 0xC758:
                return 0x1E8A; // CAPITAL X WITH DOT ABOVE
            case 0xC759:
                return 0x1E8E; // CAPITAL Y WITH DOT ABOVE
            case 0xC75A:
                return 0x017B; // CAPITAL Z WITH DOT ABOVE
            case 0xC762:
                return 0x1E03; // small b with dot above
            case 0xC763:
                return 0x010B; // small c with dot above
            case 0xC764:
                return 0x1E0B; // small d with dot above
            case 0xC765:
                return 0x0117; // small e with dot above
            case 0xC766:
                return 0x1E1F; // small f with dot above
            case 0xC767:
                return 0x0121; // small g with dot above
            case 0xC768:
                return 0x1E23; // small h with dot above
            case 0xC76D:
                return 0x1E41; // small m with dot above
            case 0xC76E:
                return 0x1E45; // small n with dot above
            case 0xC770:
                return 0x1E57; // small p with dot above
            case 0xC772:
                return 0x1E59; // small r with dot above
            case 0xC773:
                return 0x1E61; // small s with dot above
            case 0xC774:
                return 0x1E6B; // small t with dot above
            case 0xC777:
                return 0x1E87; // small w with dot above
            case 0xC778:
                return 0x1E8B; // small x with dot above
            case 0xC779:
                return 0x1E8F; // small y with dot above
            case 0xC77A:
                return 0x017C; // small z with dot above

                // 4/8 trema, diaresis
            case 0xC820:
                return 0x00A8; // diaeresis
            case 0xC841:
                return 0x00C4; // CAPITAL A WITH DIAERESIS
            case 0xC845:
                return 0x00CB; // CAPITAL E WITH DIAERESIS
            case 0xC848:
                return 0x1E26; // CAPITAL H WITH DIAERESIS
            case 0xC849:
                return 0x00CF; // CAPITAL I WITH DIAERESIS
            case 0xC84F:
                return 0x00D6; // CAPITAL O WITH DIAERESIS
            case 0xC855:
                return 0x00DC; // CAPITAL U WITH DIAERESIS
            case 0xC857:
                return 0x1E84; // CAPITAL W WITH DIAERESIS
            case 0xC858:
                return 0x1E8C; // CAPITAL X WITH DIAERESIS
            case 0xC859:
                return 0x0178; // CAPITAL Y WITH DIAERESIS
            case 0xC861:
                return 0x00E4; // small a with diaeresis
            case 0xC865:
                return 0x00EB; // small e with diaeresis
            case 0xC868:
                return 0x1E27; // small h with diaeresis
            case 0xC869:
                return 0x00EF; // small i with diaeresis
            case 0xC86F:
                return 0x00F6; // small o with diaeresis
            case 0xC874:
                return 0x1E97; // small t with diaeresis
            case 0xC875:
                return 0x00FC; // small u with diaeresis
            case 0xC877:
                return 0x1E85; // small w with diaeresis
            case 0xC878:
                return 0x1E8D; // small x with diaeresis
            case 0xC879:
                return 0x00FF; // small y with diaeresis

                // 4/9 umlaut
            case 0xC920:
                return 0x00A8; // [diaeresis]
            case 0xC941:
                return 0x00C4; // CAPITAL A WITH DIAERESIS
            case 0xC945:
                return 0x00CB; // CAPITAL E WITH DIAERESIS
            case 0xC948:
                return 0x1E26; // CAPITAL H WITH DIAERESIS
            case 0xC949:
                return 0x00CF; // CAPITAL I WITH DIAERESIS
            case 0xC94F:
                return 0x00D6; // CAPITAL O WITH DIAERESIS
            case 0xC955:
                return 0x00DC; // CAPITAL U WITH DIAERESIS
            case 0xC957:
                return 0x1E84; // CAPITAL W WITH DIAERESIS
            case 0xC958:
                return 0x1E8C; // CAPITAL X WITH DIAERESIS
            case 0xC959:
                return 0x0178; // CAPITAL Y WITH DIAERESIS
            case 0xC961:
                return 0x00E4; // small a with diaeresis
            case 0xC965:
                return 0x00EB; // small e with diaeresis
            case 0xC968:
                return 0x1E27; // small h with diaeresis
            case 0xC969:
                return 0x00EF; // small i with diaeresis
            case 0xC96F:
                return 0x00F6; // small o with diaeresis
            case 0xC974:
                return 0x1E97; // small t with diaeresis
            case 0xC975:
                return 0x00FC; // small u with diaeresis
            case 0xC977:
                return 0x1E85; // small w with diaeresis
            case 0xC978:
                return 0x1E8D; // small x with diaeresis
            case 0xC979:
                return 0x00FF; // small y with diaeresis

                // 4/10 circle above
            case 0xCA41:
                return 0x00C5; // CAPITAL A WITH RING ABOVE
            case 0xCAAD:
                return 0x016E; // CAPITAL U WITH RING ABOVE
            case 0xCA61:
                return 0x00E5; // small a with ring above
            case 0xCA75:
                return 0x016F; // small u with ring above
            case 0xCA77:
                return 0x1E98; // small w with ring above
            case 0xCA79:
                return 0x1E99; // small y with ring above

                // 4/11 high comma off centre

                // 4/12 inverted high comma centred

                // 4/13 double acute accent
            case 0xCD4F:
                return 0x0150; // CAPITAL O WITH DOUBLE ACUTE
            case 0xCD55:
                return 0x0170; // CAPITAL U WITH DOUBLE ACUTE
            case 0xCD6F:
                return 0x0151; // small o with double acute
            case 0xCD75:
                return 0x0171; // small u with double acute

                // 4/14 horn
            case 0xCE54:
                return 0x01A0; // LATIN CAPITAL LETTER O WITH HORN
            case 0xCE55:
                return 0x01AF; // LATIN CAPITAL LETTER U WITH HORN
            case 0xCE74:
                return 0x01A1; // latin small letter o with horn
            case 0xCE75:
                return 0x01B0; // latin small letter u with horn

                // 4/15 caron (hacek)
            case 0xCF41:
                return 0x01CD; // CAPITAL A WITH CARON
            case 0xCF43:
                return 0x010C; // CAPITAL C WITH CARON
            case 0xCF44:
                return 0x010E; // CAPITAL D WITH CARON
            case 0xCF45:
                return 0x011A; // CAPITAL E WITH CARON
            case 0xCF47:
                return 0x01E6; // CAPITAL G WITH CARON
            case 0xCF49:
                return 0x01CF; // CAPITAL I WITH CARON
            case 0xCF4B:
                return 0x01E8; // CAPITAL K WITH CARON
            case 0xCF4C:
                return 0x013D; // CAPITAL L WITH CARON
            case 0xCF4E:
                return 0x0147; // CAPITAL N WITH CARON
            case 0xCF4F:
                return 0x01D1; // CAPITAL O WITH CARON
            case 0xCF52:
                return 0x0158; // CAPITAL R WITH CARON
            case 0xCF53:
                return 0x0160; // CAPITAL S WITH CARON
            case 0xCF54:
                return 0x0164; // CAPITAL T WITH CARON
            case 0xCF55:
                return 0x01D3; // CAPITAL U WITH CARON
            case 0xCF5A:
                return 0x017D; // CAPITAL Z WITH CARON
            case 0xCF61:
                return 0x01CE; // small a with caron
            case 0xCF63:
                return 0x010D; // small c with caron
            case 0xCF64:
                return 0x010F; // small d with caron
            case 0xCF65:
                return 0x011B; // small e with caron
            case 0xCF67:
                return 0x01E7; // small g with caron
            case 0xCF69:
                return 0x01D0; // small i with caron
            case 0xCF6A:
                return 0x01F0; // small j with caron
            case 0xCF6B:
                return 0x01E9; // small k with caron
            case 0xCF6C:
                return 0x013E; // small l with caron
            case 0xCF6E:
                return 0x0148; // small n with caron
            case 0xCF6F:
                return 0x01D2; // small o with caron
            case 0xCF72:
                return 0x0159; // small r with caron
            case 0xCF73:
                return 0x0161; // small s with caron
            case 0xCF74:
                return 0x0165; // small t with caron
            case 0xCF75:
                return 0x01D4; // small u with caron
            case 0xCF7A:
                return 0x017E; // small z with caron

                // 5/0 cedilla
            case 0xD020:
                return 0x00B8; // cedilla
            case 0xD043:
                return 0x00C7; // CAPITAL C WITH CEDILLA
            case 0xD044:
                return 0x1E10; // CAPITAL D WITH CEDILLA
            case 0xD047:
                return 0x0122; // CAPITAL G WITH CEDILLA
            case 0xD048:
                return 0x1E28; // CAPITAL H WITH CEDILLA
            case 0xD04B:
                return 0x0136; // CAPITAL K WITH CEDILLA
            case 0xD04C:
                return 0x013B; // CAPITAL L WITH CEDILLA
            case 0xD04E:
                return 0x0145; // CAPITAL N WITH CEDILLA
            case 0xD052:
                return 0x0156; // CAPITAL R WITH CEDILLA
            case 0xD053:
                return 0x015E; // CAPITAL S WITH CEDILLA
            case 0xD054:
                return 0x0162; // CAPITAL T WITH CEDILLA
            case 0xD063:
                return 0x00E7; // small c with cedilla
            case 0xD064:
                return 0x1E11; // small d with cedilla
            case 0xD067:
                return 0x0123; // small g with cedilla
            case 0xD068:
                return 0x1E29; // small h with cedilla
            case 0xD06B:
                return 0x0137; // small k with cedilla
            case 0xD06C:
                return 0x013C; // small l with cedilla
            case 0xD06E:
                return 0x0146; // small n with cedilla
            case 0xD072:
                return 0x0157; // small r with cedilla
            case 0xD073:
                return 0x015F; // small s with cedilla
            case 0xD074:
                return 0x0163; // small t with cedilla

                // 5/1 rude

                // 5/2 hook to left

                // 5/3 ogonek (hook to right)
            case 0xD320:
                return 0x02DB; // ogonek
            case 0xD341:
                return 0x0104; // CAPITAL A WITH OGONEK
            case 0xD345:
                return 0x0118; // CAPITAL E WITH OGONEK
            case 0xD349:
                return 0x012E; // CAPITAL I WITH OGONEK
            case 0xD34F:
                return 0x01EA; // CAPITAL O WITH OGONEK
            case 0xD355:
                return 0x0172; // CAPITAL U WITH OGONEK
            case 0xD361:
                return 0x0105; // small a with ogonek
            case 0xD365:
                return 0x0119; // small e with ogonek
            case 0xD369:
                return 0x012F; // small i with ogonek
            case 0xD36F:
                return 0x01EB; // small o with ogonek
            case 0xD375:
                return 0x0173; // small u with ogonek

                // 5/4 circle below
            case 0xD441:
                return 0x1E00; // CAPITAL A WITH RING BELOW
            case 0xD461:
                return 0x1E01; // small a with ring below

                // 5/5 half circle below
            case 0xF948:
                return 0x1E2A; // CAPITAL H WITH BREVE BELOW
            case 0xF968:
                return 0x1E2B; // small h with breve below

                // 5/6 dot below
            case 0xD641:
                return 0x1EA0; // CAPITAL A WITH DOT BELOW
            case 0xD642:
                return 0x1E04; // CAPITAL B WITH DOT BELOW
            case 0xD644:
                return 0x1E0C; // CAPITAL D WITH DOT BELOW
            case 0xD645:
                return 0x1EB8; // CAPITAL E WITH DOT BELOW
            case 0xD648:
                return 0x1E24; // CAPITAL H WITH DOT BELOW
            case 0xD649:
                return 0x1ECA; // CAPITAL I WITH DOT BELOW
            case 0xD64B:
                return 0x1E32; // CAPITAL K WITH DOT BELOW
            case 0xD64C:
                return 0x1E36; // CAPITAL L WITH DOT BELOW
            case 0xD64D:
                return 0x1E42; // CAPITAL M WITH DOT BELOW
            case 0xD64E:
                return 0x1E46; // CAPITAL N WITH DOT BELOW
            case 0xD64F:
                return 0x1ECC; // CAPITAL O WITH DOT BELOW
            case 0xD652:
                return 0x1E5A; // CAPITAL R WITH DOT BELOW
            case 0xD653:
                return 0x1E62; // CAPITAL S WITH DOT BELOW
            case 0xD654:
                return 0x1E6C; // CAPITAL T WITH DOT BELOW
            case 0xD655:
                return 0x1EE4; // CAPITAL U WITH DOT BELOW
            case 0xD656:
                return 0x1E7E; // CAPITAL V WITH DOT BELOW
            case 0xD657:
                return 0x1E88; // CAPITAL W WITH DOT BELOW
            case 0xD659:
                return 0x1EF4; // CAPITAL Y WITH DOT BELOW
            case 0xD65A:
                return 0x1E92; // CAPITAL Z WITH DOT BELOW
            case 0xD661:
                return 0x1EA1; // small a with dot below
            case 0xD662:
                return 0x1E05; // small b with dot below
            case 0xD664:
                return 0x1E0D; // small d with dot below
            case 0xD665:
                return 0x1EB9; // small e with dot below
            case 0xD668:
                return 0x1E25; // small h with dot below
            case 0xD669:
                return 0x1ECB; // small i with dot below
            case 0xD66B:
                return 0x1E33; // small k with dot below
            case 0xD66C:
                return 0x1E37; // small l with dot below
            case 0xD66D:
                return 0x1E43; // small m with dot below
            case 0xD66E:
                return 0x1E47; // small n with dot below
            case 0xD66F:
                return 0x1ECD; // small o with dot below
            case 0xD672:
                return 0x1E5B; // small r with dot below
            case 0xD673:
                return 0x1E63; // small s with dot below
            case 0xD674:
                return 0x1E6D; // small t with dot below
            case 0xD675:
                return 0x1EE5; // small u with dot below
            case 0xD676:
                return 0x1E7F; // small v with dot below
            case 0xD677:
                return 0x1E89; // small w with dot below
            case 0xD679:
                return 0x1EF5; // small y with dot below
            case 0xD67A:
                return 0x1E93; // small z with dot below

                // 5/7 double dot below
            case 0xD755:
                return 0x1E72; // CAPITAL U WITH DIAERESIS BELOW
            case 0xD775:
                return 0x1E73; // small u with diaeresis below

                // 5/8 underline
            case 0xD820:
                return 0x005F; // underline

                // 5/9 double underline
            case 0xD920:
                return 0x2017; // double underline

                // 5/10 small low vertical bar
            case 0xDA20:
                return 0x02CC; //

                // 5/11 circumflex below

                // 5/12 (this position shall not be used)

                // 5/13 left half of ligature sign and of double tilde

                // 5/14 right half of ligature sign

                // 5/15 right half of double tilde

            default:
                return 0;
        }
    }
}
