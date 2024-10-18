/**
 * Copyright (C) 2002 Bas Peters
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

import java.text.Normalizer;

import org.marc4j.converter.CharConverter;

/**
 * <p>
 * A utility to convert UCS/Unicode data to UNIMARC (ISO 5426 charset).
 * </p>
 * 
 * @author Bas Peters
 * @author Yves Pratter
 */
public class UnicodeToIso5426 extends CharConverter {

    /**
     * <p>
     * Converts UCS/Unicode data to UNIMARC (ISO 5426 charset).
     * </p>
     * <p>
     * A question mark (0x3F) is returned if there is no match.
     * </p>
     * 
     * @param data - the UCS/Unicode data in an array of char
     * @return {@link String}- the UNIMARC (ISO 5426 charset) data
     */
    @Override
    public String convert(final char data[]) {
        // Conversion does not support "combining diacritical" characters
        // Must normalize first for correct results
        final char[] normalizedData = Normalizer.normalize(String.valueOf(data),
                Normalizer.Form.NFC).toCharArray();

        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < normalizedData.length; i++) {
            final char c = normalizedData[i];
            if (c < 128) {
                sb.append(c);
            } else {
                final int d = convert(c);
                if (d < 256) {
                    sb.append((char) d);
                } else {
                    sb.append((char) (d / 256));
                    sb.append((char) (d % 256));
                }
            }
        }
        return sb.toString();
    }

    private int convert(final int i) {
        switch (i) {
            case 0x0024:
                return 0xA4; // 2/4 dollar sign
            case 0x005F:
                return 0xD820; // underline
            case 0x00A1:
                return 0xA1; // 2/1 inverted exclamation mark
            case 0x00A3:
                return 0xA3; // 2/3 pound sign
            case 0x00A5:
                return 0xA5; // 2/5 yen sign
            case 0x00A7:
                return 0xA7; // 2/7 paragraph (section)
            case 0x00A8:
                return 0xC820; // diaeresis
            case 0x00A9:
                return 0xAD; // 2/13 copyright sign
            case 0x00AB:
                return 0xAB; // 2/11 left angle quotation mark
            case 0x00AE:
                return 0xAF; // 2/15 trade mark sign
            case 0x00B7:
                return 0xB7; // 3/7 middle dot
            case 0x00B8:
                return 0xD020; // cedilla
            case 0x00BB:
                return 0xBB; // 3/11 right angle quotation mark
            case 0x00BF:
                return 0xBF; // 3/15 inverted question mark
            case 0x00C0:
                return 0xC141; // CAPITAL A WITH GRAVE ACCENT
            case 0x00C1:
                return 0xC241; // CAPITAL A WITH ACUTE ACCENT
            case 0x00C2:
                return 0xC341; // CAPITAL A WITH CIRCUMFLEX ACCENT
            case 0x00C3:
                return 0xC441; // CAPITAL A WITH TILDE
            case 0x00C4:
                return 0xC841; // CAPITAL A WITH DIAERESIS
            case 0x00C5:
                return 0xCA41; // CAPITAL A WITH RING ABOVE
            case 0x00C6:
                return 0xE1; // 6/1 CAPITAL DIPHTHONG A WITH E
            case 0x00C7:
                return 0xD043; // CAPITAL C WITH CEDILLA
            case 0x00C8:
                return 0xC145; // CAPITAL E WITH GRAVE ACCENT
            case 0x00C9:
                return 0xC245; // CAPITAL E WITH ACUTE ACCENT
            case 0x00CA:
                return 0xC345; // CAPITAL E WITH CIRCUMFLEX ACCENT
            case 0x00CB:
                return 0xC845; // CAPITAL E WITH DIAERESIS
            case 0x00CC:
                return 0xC149; // CAPITAL I WITH GRAVE ACCENT
            case 0x00CD:
                return 0xC249; // CAPITAL I WITH ACUTE ACCENT
            case 0x00CE:
                return 0xC349; // CAPITAL I WITH CIRCUMFLEX ACCENT
            case 0x00CF:
                return 0xC849; // CAPITAL I WITH DIAERESIS
            case 0x00D1:
                return 0xC44E; // CAPITAL N WITH TILDE
            case 0x00D2:
                return 0xC14F; // CAPITAL O WITH GRAVE ACCENT
            case 0x00D3:
                return 0xC24F; // CAPITAL O WITH ACUTE ACCENT
            case 0x00D4:
                return 0xC34F; // CAPITAL O WITH CIRCUMFLEX ACCENT
            case 0x00D5:
                return 0xC44F; // CAPITAL O WITH TILDE
            case 0x00D6:
                return 0xC84F; // CAPITAL O WITH DIAERESIS
            case 0x00D8:
                return 0xE9; // 6/9 CAPITAL LETTER O WITH SOLIDUS [oblique
                             // stroke]
            case 0x00D9:
                return 0xC155; // CAPITAL U WITH GRAVE ACCENT
            case 0x00DA:
                return 0xC255; // CAPITAL U WITH ACUTE ACCENT
            case 0x00DB:
                return 0xC355; // CAPITAL U WITH CIRCUMFLEX
            case 0x00DC:
                return 0xC855; // CAPITAL U WITH DIAERESIS
            case 0x00DD:
                return 0xC259; // CAPITAL Y WITH ACUTE ACCENT
            case 0x00DE:
                return 0xEC; // 6/12 CAPITAL LETTER THORN
            case 0x00DF:
                return 0xFB; // 7/11 small letter sharp s
            case 0x00E0:
                return 0xC161; // small a with grave accent
            case 0x00E1:
                return 0xC261; // small a with acute accent
            case 0x00E2:
                return 0xC361; // small a with circumflex accent
            case 0x00E3:
                return 0xC461; // small a with tilde
            case 0x00E4:
                return 0xC861; // small a with diaeresis
            case 0x00E5:
                return 0xCA61; // small a with ring above
            case 0x00E6:
                return 0xF1; // 7/1 small diphthong a with e
            case 0x00E7:
                return 0xD063; // small c with cedilla
            case 0x00E8:
                return 0xC165; // small e with grave accent
            case 0x00E9:
                return 0xC265; // small e with acute accent
            case 0x00EA:
                return 0xC365; // small e with circumflex accent
            case 0x00EB:
                return 0xC865; // small e with diaeresis
            case 0x00EC:
                return 0xC169; // small i with grave accent
            case 0x00ED:
                return 0xC269; // small i with acute accent
            case 0x00EE:
                return 0xC369; // small i with circumflex accent
            case 0x00EF:
                return 0xC869; // small i with diaeresis
            case 0x00F0:
                return 0xF3; // 7/3 small letter eth
            case 0x00F1:
                return 0xC46E; // small n with tilde
            case 0x00F2:
                return 0xC16F; // small o with grave accent
            case 0x00F3:
                return 0xC26F; // small o with acute accent
            case 0x00F4:
                return 0xC36F; // small o with circumflex accent
            case 0x00F5:
                return 0xC46F; // small o with tilde
            case 0x00F6:
                return 0xC86F; // small o with diaeresis
            case 0x00F8:
                return 0xF9; // 7/9 small letter o with solidus (oblique stroke)
            case 0x00F9:
                return 0xC175; // small u with grave accent
            case 0x00FA:
                return 0xC275; // small u with acute accent
            case 0x00FB:
                return 0xC375; // small u with circumflex
            case 0x00FC:
                return 0xC875; // small u with diaeresis
            case 0x00FD:
                return 0xC279; // small y with acute accent
            case 0x00FE:
                return 0xFC; // 7/12 small letter thorn
            case 0x00FF:
                return 0xC879; // small y with diaeresis
            case 0x0100:
                return 0xC541; // CAPITAL A WITH MACRON
            case 0x0101:
                return 0xC561; // small a with macron
            case 0x0102:
                return 0xC641; // CAPITAL A WITH BREVE
            case 0x0103:
                return 0xC661; // small a with breve
            case 0x0104:
                return 0xD341; // CAPITAL A WITH OGONEK
            case 0x0105:
                return 0xD361; // small a with ogonek
            case 0x0106:
                return 0xC243; // CAPITAL C WITH ACUTE ACCENT
            case 0x0107:
                return 0xC263; // small c with acute accent
            case 0x0108:
                return 0xC343; // CAPITAL C WITH CIRCUMFLEX
            case 0x0109:
                return 0xC363; // small c with circumflex
            case 0x010A:
                return 0xC743; // CAPITAL C WITH DOT ABOVE
            case 0x010B:
                return 0xC763; // small c with dot above
            case 0x010C:
                return 0xCF43; // CAPITAL C WITH CARON
            case 0x010D:
                return 0xCF63; // small c with caron
            case 0x010E:
                return 0xCF44; // CAPITAL D WITH CARON
            case 0x010F:
                return 0xCF64; // small d with caron
            case 0x0110:
                return 0xE2; // 6/2 CAPITAL LETTER D WITH STROKE
            case 0x0111:
                return 0xF2; // 7/2 small letter d with stroke
            case 0x0112:
                return 0xC545; // CAPITAL E WITH MACRON
            case 0x0113:
                return 0xC565; // small e with macron
            case 0x0114:
                return 0xC645; // CAPITAL E WITH BREVE
            case 0x0115:
                return 0xC665; // small e with breve
            case 0x0116:
                return 0xC745; // CAPITAL E WITH DOT ABOVE
            case 0x0117:
                return 0xC765; // small e with dot above
            case 0x0118:
                return 0xD345; // CAPITAL E WITH OGONEK
            case 0x0119:
                return 0xD365; // small e with ogonek
            case 0x011A:
                return 0xCF45; // CAPITAL E WITH CARON
            case 0x011B:
                return 0xCF65; // small e with caron
            case 0x011C:
                return 0xC347; // CAPITAL G WITH CIRCUMFLEX
            case 0x011D:
                return 0xC367; // small g with circumflex
            case 0x011E:
                return 0xC647; // CAPITAL G WITH BREVE
            case 0x011F:
                return 0xC667; // small g with breve
            case 0x0120:
                return 0xC747; // CAPITAL G WITH DOT ABOVE
            case 0x0121:
                return 0xC767; // small g with dot above
            case 0x0122:
                return 0xD047; // CAPITAL G WITH CEDILLA
            case 0x0123:
                return 0xD067; // small g with cedilla
            case 0x0124:
                return 0xC348; // CAPITAL H WITH CIRCUMFLEX
            case 0x0125:
                return 0xC368; // small h with circumflex
            case 0x0128:
                return 0xC449; // CAPITAL I WITH TILDE
            case 0x0129:
                return 0xC469; // small i with tilde
            case 0x012A:
                return 0xC549; // CAPITAL I WITH MACRON
            case 0x012B:
                return 0xC569; // small i with macron
            case 0x012C:
                return 0xC649; // CAPITAL I WITH BREVE
            case 0x012D:
                return 0xC669; // small i with breve
            case 0x012E:
                return 0xD349; // CAPITAL I WITH OGONEK
            case 0x012F:
                return 0xD369; // small i with ogonek
            case 0x0130:
                return 0xC749; // CAPITAL I WITH DOT ABOVE
            case 0x0131:
                return 0xF5; // 7/5 small letter i without dot
            case 0x0132:
                return 0xE6; // 6/6 CAPITAL LETTER IJ
            case 0x0133:
                return 0xF6; // 7/6 small letter ij
            case 0x0134:
                return 0xC34A; // CAPITAL J WITH CIRCUMFLEX
            case 0x0135:
                return 0xC36A; // small j with circumflex
            case 0x0136:
                return 0xD04B; // CAPITAL K WITH CEDILLA
            case 0x0137:
                return 0xD06B; // small k with cedilla
            case 0x0139:
                return 0xC24C; // CAPITAL L WITH ACUTE ACCENT
            case 0x013A:
                return 0xC26C; // small l with acute accent
            case 0x013B:
                return 0xD04C; // CAPITAL L WITH CEDILLA
            case 0x013C:
                return 0xD06C; // small l with cedilla
            case 0x013D:
                return 0xCF4C; // CAPITAL L WITH CARON
            case 0x013E:
                return 0xCF6C; // small l with caron
            case 0x0141:
                return 0xE8; // 6/8 CAPITAL LETTER L WITH STROKE
            case 0x0142:
                return 0xF8; // 7/8 small letter l with stroke
            case 0x0143:
                return 0xC24E; // CAPITAL N WITH ACUTE ACCENT
            case 0x0144:
                return 0xC26E; // small n with acute accent
            case 0x0145:
                return 0xD04E; // CAPITAL N WITH CEDILLA
            case 0x0146:
                return 0xD06E; // small n with cedilla
            case 0x0147:
                return 0xCF4E; // CAPITAL N WITH CARON
            case 0x0148:
                return 0xCF6E; // small n with caron
            case 0x014C:
                return 0xC54F; // CAPITAL O WITH MACRON
            case 0x014D:
                return 0xC56F; // small o with macron
            case 0x014E:
                return 0xC64F; // CAPITAL O WITH BREVE
            case 0x014F:
                return 0xC66F; // small o with breve
            case 0x0150:
                return 0xCD4F; // CAPITAL O WITH DOUBLE ACUTE
            case 0x0151:
                return 0xCD6F; // small o with double acute
            case 0x0152:
                return 0xEA; // 6/10 CAPITAL DIPHTONG OE
            case 0x0153:
                return 0xFA; // 7/10 small diphtong oe
            case 0x0154:
                return 0xC252; // CAPITAL R WITH ACUTE ACCENT
            case 0x0155:
                return 0xC272; // small r with acute accent
            case 0x0156:
                return 0xD052; // CAPITAL R WITH CEDILLA
            case 0x0157:
                return 0xD072; // small r with cedilla
            case 0x0158:
                return 0xCF52; // CAPITAL R WITH CARON
            case 0x0159:
                return 0xCF72; // small r with caron
            case 0x015A:
                return 0xC253; // CAPITAL S WITH ACUTE ACCENT
            case 0x015B:
                return 0xC273; // small s with acute accent
            case 0x015C:
                return 0xC353; // CAPITAL S WITH CIRCUMFLEX
            case 0x015D:
                return 0xC373; // small s with circumflex
            case 0x015E:
                return 0xD053; // CAPITAL S WITH CEDILLA
            case 0x015F:
                return 0xD073; // small s with cedilla
            case 0x0160:
                return 0xCF53; // CAPITAL S WITH CARON
            case 0x0161:
                return 0xCF73; // small s with caron
            case 0x0162:
                return 0xD054; // CAPITAL T WITH CEDILLA
            case 0x0163:
                return 0xD074; // small t with cedilla
            case 0x0164:
                return 0xCF54; // CAPITAL T WITH CARON
            case 0x0165:
                return 0xCF74; // small t with caron
            case 0x0168:
                return 0xC455; // CAPITAL U WITH TILDE
            case 0x0169:
                return 0xC475; // small u with tilde
            case 0x016A:
                return 0xC555; // CAPITAL U WITH MACRON
            case 0x016B:
                return 0xC575; // small u with macron
            case 0x016C:
                return 0xC655; // CAPITAL U WITH BREVE
            case 0x016D:
                return 0xC675; // small u with breve
            case 0x016E:
                return 0xCAAD; // CAPITAL U WITH RING ABOVE
            case 0x016F:
                return 0xCA75; // small u with ring above
            case 0x0170:
                return 0xCD55; // CAPITAL U WITH DOUBLE ACUTE
            case 0x0171:
                return 0xCD75; // small u with double acute
            case 0x0172:
                return 0xD355; // CAPITAL U WITH OGONEK
            case 0x0173:
                return 0xD375; // small u with ogonek
            case 0x0174:
                return 0xC357; // CAPITAL W WITH CIRCUMFLEX
            case 0x0175:
                return 0xC377; // small w with circumflex
            case 0x0176:
                return 0xC359; // CAPITAL Y WITH CIRCUMFLEX
            case 0x0177:
                return 0xC379; // small y with circumflex
            case 0x0178:
                return 0xC859; // CAPITAL Y WITH DIAERESIS
            case 0x0179:
                return 0xC25A; // CAPITAL Z WITH ACUTE ACCENT
            case 0x017A:
                return 0xC27A; // small z with acute accent
            case 0x017B:
                return 0xC75A; // CAPITAL Z WITH DOT ABOVE
            case 0x017C:
                return 0xC77A; // small z with dot above
            case 0x017D:
                return 0xCF5A; // CAPITAL Z WITH CARON
            case 0x017E:
                return 0xCF7A; // small z with caron
            case 0x01A0:
                return 0xCE54; // LATIN CAPITAL LETTER O WITH HORN
            case 0x01A1:
                return 0xCE74; // latin small letter o with horn
            case 0x01AF:
                return 0xCE55; // LATIN CAPITAL LETTER U WITH HORN
            case 0x01B0:
                return 0xCE75; // latin small letter u with horn
            case 0x01CD:
                return 0xCF41; // CAPITAL A WITH CARON
            case 0x01CE:
                return 0xCF61; // small a with caron
            case 0x01CF:
                return 0xCF49; // CAPITAL I WITH CARON
            case 0x01D0:
                return 0xCF69; // small i with caron
            case 0x01D1:
                return 0xCF4F; // CAPITAL O WITH CARON
            case 0x01D2:
                return 0xCF6F; // small o with caron
            case 0x01D3:
                return 0xCF55; // CAPITAL U WITH CARON
            case 0x01D4:
                return 0xCF75; // small u with caron
            case 0x01E2:
                return 0xC5E1; // CAPITAL AE WITH MACRON
            case 0x01E3:
                return 0xC5F1; // small ae with macron
            case 0x01E6:
                return 0xCF47; // CAPITAL G WITH CARON
            case 0x01E7:
                return 0xCF67; // small g with caron
            case 0x01E8:
                return 0xCF4B; // CAPITAL K WITH CARON
            case 0x01E9:
                return 0xCF6B; // small k with caron
            case 0x01EA:
                return 0xD34F; // CAPITAL O WITH OGONEK
            case 0x01EB:
                return 0xD36F; // small o with ogonek
            case 0x01F0:
                return 0xCF6A; // small j with caron
            case 0x01F4:
                return 0xC247; // CAPITAL G WITH ACUTE
            case 0x01F5:
                return 0xC267; // small g with acute
            case 0x01FC:
                return 0xC2E1; // CAPITAL AE WITH ACUTE
            case 0x01FD:
                return 0xC2F1; // small ae with acute
            case 0x02B9:
                return 0xBD; // 3/13 mjagkij znak
            case 0x02BA:
                return 0xBE; // 3/14 tverdyj znak
            case 0x02BB:
                return 0xB0; // 3/0 ayn
            case 0x02BC:
                return 0xB1; // 3/1 alif/hamzah
            case 0x02CC:
                return 0xDA20; // small low vertical bar
            case 0x02DB:
                return 0xD320; // ogonek
            case 0x1E00:
                return 0xD441; // CAPITAL A WITH RING BELOW
            case 0x1E01:
                return 0xD461; // small a with ring below
            case 0x1E02:
                return 0xC742; // CAPITAL B WITH DOT ABOVE
            case 0x1E03:
                return 0xC762; // small b with dot above
            case 0x1E04:
                return 0xD642; // CAPITAL B WITH DOT BELOW
            case 0x1E05:
                return 0xD662; // small b with dot below
            case 0x1E0A:
                return 0xC744; // CAPITAL D WITH DOT ABOVE
            case 0x1E0B:
                return 0xC764; // small d with dot above
            case 0x1E0C:
                return 0xD644; // CAPITAL D WITH DOT BELOW
            case 0x1E0D:
                return 0xD664; // small d with dot below
            case 0x1E10:
                return 0xD044; // CAPITAL D WITH CEDILLA
            case 0x1E11:
                return 0xD064; // small d with cedilla
            case 0x1E1E:
                return 0xC746; // CAPITAL F WITH DOT ABOVE
            case 0x1E1F:
                return 0xC766; // small f with dot above
            case 0x1E20:
                return 0xC547; // CAPITAL G WITH MACRON
            case 0x1E21:
                return 0xC567; // small g with macron
            case 0x1E22:
                return 0xC748; // CAPITAL H WITH DOT ABOVE
            case 0x1E23:
                return 0xC768; // small h with dot above
            case 0x1E24:
                return 0xD648; // CAPITAL H WITH DOT BELOW
            case 0x1E25:
                return 0xD668; // small h with dot below
            case 0x1E26:
                return 0xC848; // CAPITAL H WITH DIAERESIS
            case 0x1E27:
                return 0xC868; // small h with diaeresis
            case 0x1E28:
                return 0xD048; // CAPITAL H WITH CEDILLA
            case 0x1E29:
                return 0xD068; // small h with cedilla
            case 0x1E2A:
                return 0xF948; // CAPITAL H WITH BREVE BELOW
            case 0x1E2B:
                return 0xF968; // small h with breve below
            case 0x1E30:
                return 0xC24B; // CAPITAL K WITH ACUTE
            case 0x1E31:
                return 0xC26B; // small k with acute
            case 0x1E32:
                return 0xD64B; // CAPITAL K WITH DOT BELOW
            case 0x1E33:
                return 0xD66B; // small k with dot below
            case 0x1E36:
                return 0xD64C; // CAPITAL L WITH DOT BELOW
            case 0x1E37:
                return 0xD66C; // small l with dot below
            case 0x1E3E:
                return 0xC24D; // CAPITAL M WITH ACUTE
            case 0x1E3F:
                return 0xC26D; // small m with acute
            case 0x1E40:
                return 0xC74D; // CAPITAL M WITH DOT ABOVE
            case 0x1E41:
                return 0xC76D; // small m with dot above
            case 0x1E42:
                return 0xD64D; // CAPITAL M WITH DOT BELOW
            case 0x1E43:
                return 0xD66D; // small m with dot below
            case 0x1E44:
                return 0xC74E; // CAPITAL N WITH DOT ABOVE
            case 0x1E45:
                return 0xC76E; // small n with dot above
            case 0x1E46:
                return 0xD64E; // CAPITAL N WITH DOT BELOW
            case 0x1E47:
                return 0xD66E; // small n with dot below
            case 0x1E54:
                return 0xC250; // CAPITAL P WITH ACUTE
            case 0x1E55:
                return 0xC270; // small p with acute
            case 0x1E56:
                return 0xC750; // CAPITAL P WITH DOT ABOVE
            case 0x1E57:
                return 0xC770; // small p with dot above
            case 0x1E58:
                return 0xC752; // CAPITAL R WITH DOT ABOVE
            case 0x1E59:
                return 0xC772; // small r with dot above
            case 0x1E5A:
                return 0xD652; // CAPITAL R WITH DOT BELOW
            case 0x1E5B:
                return 0xD672; // small r with dot below
            case 0x1E60:
                return 0xC753; // CAPITAL S WITH DOT ABOVE
            case 0x1E61:
                return 0xC773; // small s with dot above
            case 0x1E62:
                return 0xD653; // CAPITAL S WITH DOT BELOW
            case 0x1E63:
                return 0xD673; // small s with dot below
            case 0x1E6A:
                return 0xC754; // CAPITAL T WITH DOT ABOVE
            case 0x1E6B:
                return 0xC774; // small t with dot above
            case 0x1E6C:
                return 0xD654; // CAPITAL T WITH DOT BELOW
            case 0x1E6D:
                return 0xD674; // small t with dot below
            case 0x1E72:
                return 0xD755; // CAPITAL U WITH DIAERESIS BELOW
            case 0x1E73:
                return 0xD775; // small u with diaeresis below
            case 0x1E7C:
                return 0xC456; // CAPITAL V WITH TILDE
            case 0x1E7D:
                return 0xC476; // small v with tilde
            case 0x1E7E:
                return 0xD656; // CAPITAL V WITH DOT BELOW
            case 0x1E7F:
                return 0xD676; // small v with dot below
            case 0x1E80:
                return 0xC157; // CAPITAL W WITH GRAVE
            case 0x1E81:
                return 0xC177; // small w with grave
            case 0x1E82:
                return 0xC257; // CAPITAL W WITH ACUTE
            case 0x1E83:
                return 0xC277; // small w with acute
            case 0x1E84:
                return 0xC857; // CAPITAL W WITH DIAERESIS
            case 0x1E85:
                return 0xC877; // small w with diaeresis
            case 0x1E86:
                return 0xC757; // CAPITAL W WITH DOT ABOVE
            case 0x1E87:
                return 0xC777; // small w with dot above
            case 0x1E88:
                return 0xD657; // CAPITAL W WITH DOT BELOW
            case 0x1E89:
                return 0xD677; // small w with dot below
            case 0x1E8A:
                return 0xC758; // CAPITAL X WITH DOT ABOVE
            case 0x1E8B:
                return 0xC778; // small x with dot above
            case 0x1E8C:
                return 0xC858; // CAPITAL X WITH DIAERESIS
            case 0x1E8D:
                return 0xC878; // small x with diaeresis
            case 0x1E8E:
                return 0xC759; // CAPITAL Y WITH DOT ABOVE
            case 0x1E8F:
                return 0xC779; // small y with dot above
            case 0x1E90:
                return 0xC35A; // CAPITAL Z WITH CIRCUMFLEX
            case 0x1E91:
                return 0xC37A; // small z with circumflex
            case 0x1E92:
                return 0xD65A; // CAPITAL Z WITH DOT BELOW
            case 0x1E93:
                return 0xD67A; // small z with dot below
            case 0x1E97:
                return 0xC874; // small t with diaeresis
            case 0x1E98:
                return 0xCA77; // small w with ring above
            case 0x1E99:
                return 0xCA79; // small y with ring above
            case 0x1EA0:
                return 0xD641; // CAPITAL A WITH DOT BELOW
            case 0x1EA1:
                return 0xD661; // small a with dot below
            case 0x1EA2:
                return 0xC041; // CAPITAL A WITH HOOK ABOVE
            case 0x1EA3:
                return 0xC061; // small a with hook above
            case 0x1EB8:
                return 0xD645; // CAPITAL E WITH DOT BELOW
            case 0x1EB9:
                return 0xD665; // small e with dot below
            case 0x1EBA:
                return 0xC045; // CAPITAL E WITH HOOK ABOVE
            case 0x1EBB:
                return 0xC065; // small e with hook above
            case 0x1EBC:
                return 0xC445; // CAPITAL E WITH TILDE
            case 0x1EBD:
                return 0xC465; // small e with tilde
            case 0x1EC8:
                return 0xC049; // CAPITAL I WITH HOOK ABOVE
            case 0x1EC9:
                return 0xC069; // small i with hook above
            case 0x1ECA:
                return 0xD649; // CAPITAL I WITH DOT BELOW
            case 0x1ECB:
                return 0xD669; // small i with dot below
            case 0x1ECC:
                return 0xD64F; // CAPITAL O WITH DOT BELOW
            case 0x1ECD:
                return 0xD66F; // small o with dot below
            case 0x1ECE:
                return 0xC04F; // CAPITAL O WITH HOOK ABOVE
            case 0x1ECF:
                return 0xC06F; // small o with hook above
            case 0x1EE4:
                return 0xD655; // CAPITAL U WITH DOT BELOW
            case 0x1EE5:
                return 0xD675; // small u with dot below
            case 0x1EE6:
                return 0xC055; // CAPITAL U WITH HOOK ABOVE
            case 0x1EE7:
                return 0xC075; // small u with hook above
            case 0x1EF2:
                return 0xC159; // CAPITAL Y WITH GRAVE
            case 0x1EF3:
                return 0xC179; // small y with grave
            case 0x1EF4:
                return 0xD659; // CAPITAL Y WITH DOT BELOW
            case 0x1EF5:
                return 0xD679; // small y with dot below
            case 0x1EF6:
                return 0xC059; // CAPITAL Y WITH HOOK ABOVE
            case 0x1EF7:
                return 0xC079; // small y with hook above
            case 0x1EF8:
                return 0xC459; // CAPITAL Y WITH TILDE
            case 0x1EF9:
                return 0xC479; // small y with tilde
            case 0x2017:
                return 0xD920; // double underline
            case 0x2018:
                return 0xA9; // 2/9 left high single quotation mark
            case 0x2019:
                return 0xB9; // 3/9 right high single quotation mark
            case 0x201A:
                return 0xB2; // 3/2 left low single quotation
            case 0x201C:
                return 0xAA; // 2/10 left high double quotation mark
            case 0x201D:
                return 0xBA; // 3/10 right high double quotation mark
            case 0x201E:
                return 0xA2; // 2/2 left low double quotation mark
            case 0x2020:
                return 0xA6; // 2/6 single dagger
            case 0x2021:
                return 0xB6; // 3/6 double dagger
            case 0x2032:
                return 0xA8; // 2/8 prime
            case 0x2033:
                return 0xB8; // 3/8 double prime
            case 0x2117:
                return 0xAE; // 2/14 sound recording copyright sign
            case 0x266D:
                return 0xAC; // 2/12 music flat
            case 0x266F:
                return 0xBC; // 3/12 musical sharp

            default:
                return 0x3F; // if no match, return question mark
        }
    }
}
