
/*******************************************************************
*  TEXT-TO-MARC 21                      Date: October 7, 2003      *
*  CHARACTER CONVERSION TABLE FOR MARCMaker Version 2.5            *
*  Includes mnemonics for all Latin-1 (Windows 1252) and Latin-2   *
*  (Windows 1250 with letter-with-diacritic characters used in     *
*  Eastern and Western European languages. Also includes mnemonics *
*  used for the ALA-LC romanization tables for Russian and         *
*  Ukrainian                                                       *
*                                                                  *
*  Source: Randall K. BARRY                                        *
*          U.S. Library of Congress                                *
*          Network Development and MARC Standards Office           *
*          101 Independence Ave., S.E.                             *
*          Washington, D.C. 20540-4402  U.S.A.                     *
*          TEL: +1-202-707-5118                                    *
*          FAX: +1-202-707-0115                                    *
*          NET: rbar@loc.gov                                       *
*                                                                  *
* Converts any listed mnemonic to its hexadecimal equivalent.      *
*                                                                  *
* The curly braces "{" (7Bx/123d) and "}" (7Dx/125d) are used      *
* in MARCMaker to delimit mnemonics converted according to this    *
* table.  Any characters not delimited by the curly braces are     *
* passed unchanged into the MARC output record.                    *
*                                                                  *
* Mnemonics encountered that are not listed in this table are      *
* passed to the output record preceded by an ampersand (&) and     *
* followed by a semicolon (;).  Thus "{zilch}" would be come       *
* "&zilch;" in the output record.                                  *
*                                                                  *
* Mnemonics in this table are enclosed in curly braces "{...}".    *
* When a mnemonic is mapped to more than one character, the        *
* character codes are separated by a space in this table.          *
*                                                                  *
* Columns in this table are delimited by a comma ","               *
*                                                                  *
********************************************************************
* Mnemonic, hex value // name/comment                              *
* {**},     ??                // [character name (FORMAT)]         *
*__________________________________________________________________*/

package org.marc4j;

import java.util.LinkedHashMap;
import java.util.Map;

public class Mrk8TranslationTable  {

    public static String fromMrk8(final String datafield) {

        if (datafield.indexOf('{') == -1) {
            return(datafield);
        }
        StringBuilder sb = new StringBuilder();
        int len = datafield.length();
        int i = 0;
        while (i < len) {
            int j = datafield.indexOf('{', i);
            if (j == -1) {
                sb.append(datafield.substring(i));
                i = len;
                continue;
            }
            if (i < j) {
                sb.append(datafield.substring(i, j));
                i = j-1;
            }
            int k = datafield.indexOf('}', j);
            String lookupVal = datafield.substring(j, k+1);
            sb.append(lookup(lookupVal));
            i = k+1;
        }
        return(sb.toString());
    }

    private static String lookup(String toLookup) {
        if (mrk8Map == null) {
            mrk8Map = new LinkedHashMap<String,String>();
            for (int i = 0; i < mrk8Table.length; i++) {
                String translateVal = translate(mrk8Table[i][1]);
                mrk8Map.put(mrk8Table[i][0], translateVal);
            }
        }
//        int maxlen = "DoubleLongLeftRightArrow".length() + 2;
        String lookupVal = mrk8Map.get(toLookup);
        if (lookupVal == null) {
//            if (toLookup.matches("[{][A-Za-z0-9]*[}]") && toLookup.length() < maxlen) {
//                lookupVal = toLookup.replace('{', '&').replace('}', ';');
//            }
//            else {
                lookupVal = toLookup;
//            }
        }
        return(lookupVal);
    }

    private static String translate(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i += 4) {
            sb.append((char)(charToNibble(s.charAt(i)) * 16 + charToNibble(s.charAt(i+1))));
        }
        return sb.toString();
    }

    private static int charToNibble(char c) {
        if (c >= '0' && c <= '9') return ((int)(c - '0'));
        if (c >= 'A' && c <= 'F') return ((int)(c - 'A' + 10));
        if (c >= 'a' && c <= 'f') return ((int)(c - 'a' + 10));
        return 0;
    }

    private static Map<String, String> mrk8Map = null;

    private final static String[][] mrk8Table = {
        {"{0}", "30x" },  // zero
        {"{00}", "00x" },  // hex value 00
        {"{01}", "01x" },  // hex value 01
        {"{02}", "02x" },  // hex value 02
        {"{03}", "03x" },  // hex value 03
        {"{04}", "04x" },  // hex value 04
        {"{05}", "05x" },  // hex value 05
        {"{06}", "06x" },  // hex value 06
        {"{07}", "07x" },  // hex value 07
        {"{08}", "08x" },  // hex value 08
        {"{09}", "09x" },  // hex value 09
        {"{0A}", "0Ax" },  // hex value 0A
        {"{0B}", "0Bx" },  // hex value 0B
        {"{0C}", "0Cx" },  // hex value 0C
        {"{0D}", "0Dx" },  // hex value 0D
        {"{0E}", "0Ex" },  // hex value 0E
        {"{0F}", "0Fx" },  // hex value 0F
        {"{1}", "31x" },  // digit one
        {"{10}", "10x" },  // hex value 10
        {"{11}", "11x" },  // hex value 11
        {"{12}", "12x" },  // hex value 12
        {"{13}", "13x" },  // hex value 13
        {"{14}", "14x" },  // hex value 14
        {"{15}", "15x" },  // hex value 15
        {"{16}", "16x" },  // hex value 16
        {"{17}", "17x" },  // hex value 17
        {"{18}", "18x" },  // hex value 18
        {"{19}", "19x" },  // hex value 19
        {"{1A}", "1Ax" },  // hex value 1A
        {"{1B}", "1Bx" },  // escape
        {"{1C}", "1Cx" },  // hex value 1C
        {"{1D}", "1Dx" },  // end of record
        {"{1E}", "1Ex" },  // end of field
        {"{1F}", "1Fx" },  // subfield delimiter
        {"{2}", "32x" },  // digit two
        {"{20}", "20x" },  // space (blank)
        {"{21}", "21x" },  // exclamation point
        {"{22}", "22x" },  // quotation mark
        {"{23}", "23x" },  // number sign
        {"{24}", "24x" },  // dollar sign
        {"{25}", "25x" },  // percent sign
        {"{26}", "26x" },  // ampersand
        {"{27}", "27x" },  // apostrophe
        {"{28}", "28x" },  // left parenthesis
        {"{29}", "29x" },  // right parenthesis
        {"{2A}", "2Ax" },  // asterisk
        {"{2B}", "2Bx" },  // plus
        {"{2C}", "2Cx" },  // comma
        {"{2D}", "2Dx" },  // hyphen-minus
        {"{2E}", "2Ex" },  // period/decimal point
        {"{2F}", "2Fx" },  // solidus (slash)
        {"{3}", "33x" },  // digit three
        {"{30}", "30x" },  // digit zero
        {"{31}", "31x" },  // digit one
        {"{32}", "32x" },  // digit two
        {"{33}", "33x" },  // digit three
        {"{34}", "34x" },  // digit four
        {"{35}", "35x" },  // digit five
        {"{36}", "36x" },  // digit six
        {"{37}", "37x" },  // digit seven
        {"{38}", "38x" },  // digit eight
        {"{39}", "39x" },  // digit nine
        {"{3A}", "3Ax" },  // colon
        {"{3B}", "3Bx" },  // semicolon
        {"{3C}", "3Cx" },  // less than
        {"{3D}", "3Dx" },  // equals sign
        {"{3E}", "3Ex" },  // greater than
        {"{3F}", "3Fx" },  // question mark
        {"{4}", "34x" },  // digit four
        {"{40}", "40x" },  // commercial at sign
        {"{41}", "41x" },  // latin large letter a
        {"{42}", "42x" },  // latin large letter b
        {"{43}", "43x" },  // latin large letter c
        {"{44}", "44x" },  // latin large letter d
        {"{45}", "45x" },  // latin large letter e
        {"{46}", "46x" },  // latin large letter f
        {"{47}", "47x" },  // latin large letter g
        {"{48}", "48x" },  // latin large letter h
        {"{49}", "49x" },  // latin large letter i
        {"{4A}", "4Ax" },  // latin large letter j
        {"{4B}", "4Bx" },  // latin large letter k
        {"{4C}", "4Cx" },  // latin large letter l
        {"{4D}", "4Dx" },  // latin large letter m
        {"{4E}", "4Ex" },  // latin large letter n
        {"{4F}", "4Fx" },  // latin large letter o
        {"{5}", "35x" },  // digit five
        {"{50}", "50x" },  // latin large letter p
        {"{51}", "51x" },  // latin large letter q
        {"{52}", "52x" },  // latin large letter r
        {"{53}", "53x" },  // latin large letter s
        {"{54}", "54x" },  // latin large letter t
        {"{55}", "55x" },  // latin large letter u
        {"{56}", "56x" },  // latin large letter v
        {"{57}", "57x" },  // latin large letter w
        {"{58}", "58x" },  // latin large letter x
        {"{59}", "59x" },  // latin large letter y
        {"{5A}", "5Ax" },  // latin large letter z
        {"{5B}", "5Bx" },  // left bracket
        {"{5C}", "5Cx" },  // back slash (reverse solidus)
        {"{5D}", "5Dx" },  // right bracket
        {"{5E}", "5Ex" },  // spacing circumflex
        {"{5F}", "5Fx" },  // spacing underscore
        {"{6}", "36x" },  // digit six
        {"{60}", "60x" },  // spacing grave
        {"{61}", "61x" },  // latin small letter a
        {"{62}", "62x" },  // latin small letter b
        {"{63}", "63x" },  // latin small letter c
        {"{64}", "64x" },  // latin small letter d
        {"{65}", "65x" },  // latin small letter e
        {"{66}", "66x" },  // latin small letter f
        {"{67}", "67x" },  // latin small letter g
        {"{68}", "68x" },  // latin small letter h
        {"{69}", "69x" },  // latin small letter i
        {"{6A}", "6Ax" },  // latin small letter j
        {"{6B}", "6Bx" },  // latin small letter k
        {"{6C}", "6Cx" },  // latin small letter l
        {"{6D}", "6Dx" },  // latin small letter m
        {"{6E}", "6Ex" },  // latin small letter n
        {"{6F}", "6Fx" },  // latin small letter o
        {"{7}", "37x" },  // digit seven
        {"{70}", "70x" },  // latin small letter p
        {"{71}", "71x" },  // latin small letter q
        {"{72}", "72x" },  // latin small letter r
        {"{73}", "73x" },  // latin small letter s
        {"{74}", "74x" },  // latin small letter t
        {"{75}", "75x" },  // latin small letter u
        {"{76}", "76x" },  // latin small letter v
        {"{77}", "77x" },  // latin small letter w
        {"{78}", "78x" },  // latin small letter x
        {"{79}", "79x" },  // latin small letter y
        {"{7A}", "7Ax" },  // latin small letter z
        {"{7B}", "7Bx" },  // opening curly brace
        {"{7C}", "7Cx" },  // fill/bar over bar/pipe
        {"{7D}", "7Dx" },  // closing curly brace
        {"{7E}", "7Ex" },  // spacing tilde
        {"{7F}", "7Fx" },  // hex value 7F
        {"{8}", "38x" },  // digit eight
        {"{80}", "80x" },  // hex value 80
        {"{81}", "81x" },  // hex value 81
        {"{82}", "82x" },  // hex value 82
        {"{83}", "83x" },  // hex value 83
        {"{84}", "84x" },  // hex value 84
        {"{85}", "85x" },  // hex value 85
        {"{86}", "86x" },  // hex value 86
        {"{87}", "87x" },  // hex value 87
        {"{88}", "88x" },  // hex value 88
        {"{89}", "89x" },  // hex value 89
        {"{8A}", "8Ax" },  // hex value 8A
        {"{8B}", "8Bx" },  // hex value 8B
        {"{8C}", "8Cx" },  // hex value 8C
        {"{8D}", "8Dx" },  // zero width joiner
        {"{8E}", "8Ex" },  // zero width non-joiner
        {"{8F}", "8Fx" },  // hex value 8F
        {"{9}", "39x" },  // digit nine
        {"{90}", "90x" },  // hex value 90
        {"{91}", "91x" },  // hex value 91
        {"{92}", "92x" },  // hex value 92
        {"{93}", "93x" },  // hex value 93
        {"{94}", "94x" },  // hex value 94
        {"{95}", "95x" },  // hex value 95
        {"{96}", "96x" },  // hex value 96
        {"{97}", "97x" },  // hex value 97
        {"{98}", "98x" },  // hex value 98
        {"{99}", "99x" },  // hex value 99
        {"{9A}", "9Ax" },  // hex value 9A
        {"{9B}", "9Bx" },  // hex value 9B
        {"{9C}", "9Cx" },  // hex value 9C
        {"{9D}", "9Dx" },  // hex value 9D
        {"{9E}", "9Ex" },  // hex value 9E
        {"{9F}", "9Fx" },  // hex value 9F
        {"{A}", "41x" },  // latin large letter a
        {"{a}", "61x" },  // latin small letter a
        {"{A0}", "A0x" },  // no-break space
        {"{A1}", "A1x" },  // latin large letter l with stroke
        {"{A2}", "A2x" },  // latin large letter o with stroke
        {"{A3}", "A3x" },  // latin large letter d with stroke
        {"{A4}", "A4x" },  // latin large letter thorn
        {"{A5}", "A5x" },  // latin large letter AE
        {"{A6}", "A6x" },  // latin large letter OE
        {"{A7}", "A7x" },  // modifier letter prime/soft sign
        {"{A8}", "A8x" },  // middle dot
        {"{A9}", "A9x" },  // musical flat sign
        {"{AA}", "AAx" },  // registered sign
        {"{Aacute}", "E2x 41x" },  // latin large letter a with acute
        {"{aacute}", "E2x 61x" },  // latin small letter a with acute
        {"{AB}", "ABx" },  // plus-minus sign
        {"{Abreve}", "E6x 41x" },  // latin large letter a with breve
        {"{abreve}", "E6x 61x" },  // latin small letter a with breve
        {"{AC}", "ACx" },  // latin large letter o with horn
        {"{Acirc}", "E3x 41x" },  // latin large letter a with circumflex
        {"{acirc}", "E3x 61x" },  // latin small letter a with circumflex
        {"{acute}", "E2x" },  // combining acute
        {"{Acy}", "41x" },  // cyrillic large letter a
        {"{acy}", "61x" },  // cyrillic small letter a
        {"{AD}", "ADx" },  // latin large letter u with horn
        {"{AE}", "AEx" },  // modifier letter right half ring/alif
        {"{AElig}", "A5x" },  // latin large letter AE
        {"{aelig}", "B5x" },  // latin small letter ae
        {"{AF}", "AFx" },  // hex value AF
        {"{agr}", "61x" },  // greek small letter alpha
        {"{Agrave}", "E1x 41x" },  // latin large letter a with grave
        {"{agrave}", "E1x 61x" },  // latin small letter a with grave
        {"{alif}", "AEx" },  // modifier letter right half ring (alif)
        {"{amp}", "26x" },  // ampersand
        {"{Aogon}", "F1x 41x" },  // latin large letter a with ogon (hook right)
        {"{aogon}", "F1x 61x" },  // latin small letter a with ogon (hook right)
        {"{apos}", "27x" },  // apostrophe
        {"{arab}", "28x 33x" },  // begin arabic script
        {"{Aring}", "EAx 41x" },  // latin large letter a with ring
        {"{aring}", "EAx 61x" },  // latin small letter a with ring
        {"{ast}", "2Ax" },  // asterisk
        {"{asuper}", "61x" },  // superscript a
        {"{Atilde}", "E4x 41x" },  // latin large letter A with tilde
        {"{atilde}", "E4x 61x" },  // latin small letter a with tilde
        {"{Auml}", "E8x 41x" },  // latin large letter A with umlaut
        {"{auml}", "E8x 61x" },  // latin small letter a with umlaut
        {"{ayn}", "B0x" },  // modifier letter left half ring (ayn)
        {"{B}", "42x" },  // latin large letter b
        {"{b}", "62x" },  // latin small letter b
        {"{B0}", "B0x" },  // modifier letter left half ring/ayn
        {"{B1}", "B1x" },  // latin small letter l with stroke
        {"{B2}", "B2x" },  // latin small letter o with stroke
        {"{B3}", "B3x" },  // latin small letter d with stroke
        {"{B4}", "B4x" },  // latin small letter thorn
        {"{B5}", "B5x" },  // latin small letter ae
        {"{B6}", "B6x" },  // latin small letter oe
        {"{B7}", "B7x" },  // modifier letter double prime/hard sign
        {"{B8}", "B8x" },  // latin small letter dotless i
        {"{B9}", "B9x" },  // pound sign
        {"{BA}", "BAx" },  // latin small letter eth
        {"{BB}", "BBx" },  // hex value BB
        {"{BC}", "BCx" },  // latin small letter o with horn
        {"{bcy}", "62x" },  // cyrillic small letter be
        {"{Bcy}", "42x" },  // cyrillic large letter be
        {"{BD}", "BDx" },  // latin small letter u with horn
        {"{BE}", "BEx" },  // hex value BE
        {"{BF}", "BFx" },  // hex value BF
        {"{bgr}", "62x" },  // greek small letter beta
        {"{breve}", "E6x" },  // combining breve
        {"{breveb}", "F9x" },  // combining breve below
        {"{brvbar}", "7Cx" },  // broken vertical bar
        {"{bsol}", "5Cx" },  // reverse solidus (back slash)
        {"{bull}", "2Ax" },  // bullet
        {"{C}", "43x" },  // latin large letter c
        {"{c}", "63x" },  // latin small letter c
        {"{C0}", "C0x" },  // degree sign
        {"{C1}", "C1x" },  // latin small letter script l
        {"{C2}", "C2x" },  // sound recording copyright
        {"{C3}", "C3x" },  // copyright sign
        {"{C4}", "C4x" },  // sharp
        {"{C5}", "C5x" },  // inverted question mark
        {"{C6}", "C6x" },  // inverted exclamation mark
        {"{C7}", "C7x" },  // hex value C7
        {"{C8}", "C8x" },  // hex value C8
        {"{C9}", "C9x" },  // hex value C9
        {"{CA}", "CAx" },  // hex value CA
        {"{Cacute}", "E2x 43x" },  // latin large letter c with acute
        {"{cacute}", "E2x 63x" },  // latin small letter c with acute
        {"{candra}", "EFx" },  // combining candrabindu
        {"{caron}", "E9x" },  // combining hacek
        {"{CB}", "CBx" },  // hex value CB
        {"{CC}", "CCx" },  // hex value CC
        {"{Ccaron}", "E9x 43x" },  // latin large letter c with caron
        {"{ccaron}", "E9x 63x" },  // latin small letter c with caron
        {"{Ccedil}", "F0x 43x" },  // latin large letter c with cedilla
        {"{ccedil}", "F0x 63x" },  // latin small letter c with cedilla
        {"{CD}", "CDx" },  // hex value CD
        {"{CE}", "CEx" },  // hex value CE
        {"{cedil}", "F0x" },  // combining cedilla
        {"{cent}", "63x" },  // cent sign
        {"{CF}", "CFx" },  // hex value CF
        {"{CHcy}", "43x 68x" },  // cyrillic large letter cha
        {"{chcy}", "63x 68x" },  // cyrillic small letter cha
        {"{circ}", "E3x" },  // combining circumflex
        {"{circb}", "F4x" },  // combining circumflex below
        {"{cjk}", "24x 31x" },  // begin chinese japanese korean script
        {"{colon}", "3Ax" },  // colon
        {"{comma}", "2Cx" },  // comma
        {"{commaa}", "FEx" },  // combining comma above
        {"{commab}", "F7x" },  // combining comma below (hook left)
        {"{commat}", "40x" },  // commercial at sign
        {"{copy}", "C3x" },  // copyright sign
        {"{curren}", "2Ax" },  // currency sign
        {"{cyril}", "28x 4Ex" },  // begin cyrillic script
        {"{D}", "44x" },  // latin large letter d
        {"{d}", "64x" },  // latin small letter d
        {"{D0}", "D0x" },  // hex value D0
        {"{D1}", "D1x" },  // hex value D1
        {"{D2}", "D2x" },  // hex value D2
        {"{D3}", "D3x" },  // hex value D3
        {"{D4}", "D4x" },  // hex value D4
        {"{D5}", "D5x" },  // hex value D5
        {"{D6}", "D6x" },  // hex value D6
        {"{D7}", "D7x" },  // hex value D7
        {"{D8}", "D8x" },  // hex value D8
        {"{D9}", "D9x" },  // hex value D9
        {"{DA}", "DAx" },  // hex value DA
        {"{Dagger}", "7Cx" },  // double dagger
        {"{dagger}", "7Cx" },  // dagger
        {"{DB}", "DBx" },  // hex value DB
        {"{dblac}", "EEx" },  // combining double acute
        {"{dbldotb}", "F3x" },  // combining double dot below
        {"{dblunder}", "F5x" },  // combining double underscore
        {"{DC}", "DCx" },  // hex value DC
        {"{Dcaron}", "E9x 44x" },  // latin large letter d with caron
        {"{dcaron}", "E9x 64x" },  // latin small letter d with caron
        {"{Dcy}", "44x" },  // cyrillic large letter de
        {"{dcy}", "64x" },  // cyrillic small letter de
        {"{DD}", "DDx" },  // hex value DD
        {"{DE}", "DEx" },  // hex value DE
        {"{deg}", "C0x" },  // degree sign
        {"{DF}", "DFx" },  // hex value DF
        {"{diaer}", "E8x" },  // combining diaeresis
        {"{divide}", "2Fx" },  // divide sign
        {"{djecy}", "B3x" },  // cyrillic small letter dje
        {"{DJEcy}", "A3x" },  // cyrillic large letter dje
        {"{dollar}", "24x" },  // dollar sign
        {"{dot}", "E7x" },  // combining dot above
        {"{dotb}", "F2x" },  // combining dot below
        {"{Dstrok}", "A3x" },  // latin large letter d with stroke
        {"{dstrok}", "B3x" },  // latin small letter d with stroke
        {"{DZEcy}", "44x 7Ax" },  // cyrillic large letter dze
        {"{dzecy}", "64x 7Ax" },  // cyrillic small letter dze
        {"{DZHEcy}", "44x E9x 7Ax" },  // cyrillic large letter dzhe
        {"{dzhecy}", "64x E9x 7Ax" },  // cyrillic small letter dzhe
        {"{E}", "45x" },  // latin large letter e
        {"{e}", "65x" },  // latin small letter e
        {"{E0}", "E0x" },  // combining hook above
        {"{E1}", "E1x" },  // combining grave
        {"{E2}", "E2x" },  // combining acute
        {"{E3}", "E3x" },  // combining circumflex
        {"{E4}", "E4x" },  // combining tilde
        {"{E5}", "E5x" },  // combining macron
        {"{E6}", "E6x" },  // combining breve
        {"{E7}", "E7x" },  // combining dot above
        {"{E8}", "E8x" },  // combining diaeresis
        {"{E9}", "E9x" },  // combining hacek
        {"{EA}", "EAx" },  // combining ring above
        {"{ea}", "eax" },  // combining ring above
        {"{Eacute}", "E2x 45x" },  // latin large letter e with acute
        {"{eacute}", "E2x 65x" },  // latin small letter e with acute
        {"{EB}", "EBx" },  // combining ligature left half
        {"{EC}", "ECx" },  // combining ligature right half
        {"{Ecaron}", "E9x 45x" },  // latin large letter e with caron
        {"{ecaron}", "E9x 65x" },  // latin small letter e with caron
        {"{Ecirc}", "E3x 45x" },  // latin large letter e with circumflex
        {"{ecirc}", "E3x 65x" },  // latin small letter e with circumflex
        {"{Ecy}", "E7x 44x" },  // cyrillic large letter reversed e
        {"{ecy}", "E7x 65x" },  // cyrillic small letter reversed e
        {"{ED}", "EDx" },  // combining comma above right
        {"{EE}", "EEx" },  // combining double acute
        {"{EF}", "EFx" },  // combining candrabindu
        {"{Egrave}", "E1x 45x" },  // latin large letter e with grave
        {"{egrave}", "E1x 65x" },  // latin small letter e with grave
        {"{Ehookr}", "F1x 45x" },  // latin large letter e with right hook (ogonek)
        {"{ehookr}", "F1x 65x" },  // latin small letter e with right hook (ogonek)
        {"{Eogon}", "F1x 45x" },  // latin large letter e with ogonek (right hook)
        {"{eogon}", "F1x 65x" },  // latin small letter e with ogonek (right hook)
        {"{equals}", "3Dx" },  // equals sign
        {"{esc}", "1Bx" },  // escape
        {"{eth}", "BAx" },  // latin small letter eth
        {"{ETH}", "A3x" },  // latin capital letter eth
        {"{Euml}", "E8x 45x" },  // latin large letter e with umlaut
        {"{euml}", "E8x 65x" },  // latin small letter e with umlaut
        {"{excl}", "21x" },  // exclamation point
        {"{F}", "46x" },  // latin large letter f
        {"{f}", "66x" },  // latin small letter f
        {"{F0}", "F0x" },  // combining cedilla
        {"{F1}", "F1x" },  // combining ogonek
        {"{F2}", "F2x" },  // combining dot below
        {"{F3}", "F3x" },  // combining double dot below
        {"{F4}", "F4x" },  // combining ring below
        {"{F5}", "F5x" },  // combining double underscore
        {"{F6}", "F6x" },  // combining underscore
        {"{F7}", "F7x" },  // combining comma below
        {"{F8}", "F8x" },  // combining right cedilla
        {"{F9}", "F9x" },  // combining breve below
        {"{FA}", "FAx" },  // combining double tilde left half
        {"{FB}", "FBx" },  // combining double tilde right half
        {"{FC}", "FCx" },  // hex value FC
        {"{Fcy}", "46x" },  // cyrillic large letter ef
        {"{fcy}", "66x" },  // cyrillic small letter ef
        {"{FD}", "FDx" },  // hex value FD
        {"{FE}", "FEx" },  // combining comma above
        {"{FF}", "FFx" },  // hex value FF
        {"{flat}", "A9x" },  // musical flat sign
        {"{fnof}", "66x" },  // curvy f (CP850)
        {"{frac12}", "31x 2Fx 32x" },  // fraction 1/2
        {"{frac14}", "31x 2Fx 34x" },  // fraction 1/4
        {"{frac34}", "33x 2Fx 34x" },  // fraction 3/4
        {"{G}", "47x" },  // latin large letter g
        {"{g}", "67x" },  // latin small letter g
        {"{Gcy}", "47x" },  // cyrillic large letter ge
        {"{gcy}", "67x" },  // cyrillic small letter ge
        {"{GEcy}", "47x" },  // cyrillic large letter ge
        {"{gecy}", "67x" },  // cyrillic small letter ge
        {"{ggr}", "67x" },  // greek small letter gamma
        {"{GHcy}", "47x" },  // ukrainian/belorussian large letter ghe
        {"{ghcy}", "67x" },  // ukrainian/belorussian small letter ghe
        {"{GJEcy}", "E2x 47x" },  // cyrillic large letter gje
        {"{gjecy}", "E2x 67x" },  // cyrillic small letter gje
        {"{grave}", "E1x" },  // combining grave
        {"{greek}", "67x" },  // begin greek script
        {"{gs}", "1Dx" },  // group separator (end of record)
        {"{gt}", "3Ex" },  // greater than
        {"{H}", "48x" },  // latin large letter h
        {"{h}", "68x" },  // latin small letter h
        {"{HARDcy}", "B7x" },  // cyrillic large letter hardsign
        {"{hardcy}", "B7x" },  // cyrillic small letter hardsign
        {"{hardsign}", "B7x" },  // modifier letter hard sign
        {"{Hcy}", "48x" },  // cyrillic large letter he
        {"{hcy}", "68x" },  // cyrillic small letter he
        {"{hebrew}", "28x 32x" },  // begin hebrew script
        {"{hellip}", "2Ex 2Ex 2Ex" },  // ellipsis
        {"{hooka}", "E0x" },  // combining hook above
        {"{hookl}", "F7x" },  // combining hook left (comma below)
        {"{hookr}", "F1x" },  // combining hook right (ogonek)
        {"{hyphen}", "2Dx" },  // hyphen (minus)
        {"{I}", "49x" },  // latin large letter i
        {"{i}", "69x" },  // latin small letter i
        {"{Iacute}", "E2x 49x" },  // latin large letter i with acute
        {"{iacute}", "E2x 69x" },  // latin small letter i with acute
        {"{Icaron}", "E9x 49x" },  // latin large letter i with caron
        {"{icaron}", "E9x 69x" },  // latin small letter i with caron
        {"{Icirc}", "E3x 49x" },  // latin large letter i with circumflex
        {"{icirc}", "E3x 69x" },  // latin small letter i with circumflex
        {"{Icy}", "49x" },  // cyrillic large letter ii
        {"{icy}", "69x" },  // cyrillic small letter ii
        {"{Idot}", "E7x 49x" },  // latin small letter i with dot
        {"{IEcy}", "EBx 49x ECx 45x" },  // cyrillic large letter ie
        {"{iecy}", "EBx 69x ECx 65x" },  // cyrillic large letter ie
        {"{iexcl}", "C6x" },  // inverted exclamation mark
        {"{Igrave}", "E1x 49x" },  // latin large letter i with grave
        {"{igrave}", "E1x 69x" },  // latin small letter i with grave
        {"{IJlig}", "49x 4Ax" },  // latin large letter ij
        {"{ijlig}", "69x 6Ax" },  // latin small letter ij
        {"{inodot}", "B8x" },  // latin small letter dotless i
        {"{IOcy}", "EBx 49x ECx 4Fx" },  // cyrillic large letter io
        {"{iocy}", "EBx 69x ECx 6Fx" },  // cyrillic small letter io
        {"{iquest}", "C5x" },  // inverted question mark
        {"{Iuml}", "E8x 49x" },  // latin large letter i with umlaut
        {"{iuml}", "E8x 69x" },  // latin small letter i with umlaut
        {"{Iumlcy}", "E8x 49x" },  // cyrillic large letter i with umlaut
        {"{iumlcy}", "E8x 69x" },  // cyrillic small letter i with umlaut
        {"{IYcy}", "59x" },  // cyrillic large letter ukrainian y
        {"{iycy}", "79x" },  // cyrillic small small letter ukrainian y
        {"{J}", "4Ax" },  // latin large letter j
        {"{j}", "6Ax" },  // latin small letter j
        {"{Jcy}", "E6x 49x" },  // cyrillic large letter short ii
        {"{jcy}", "E6x 69x" },  // cyrillic small letter short ii
        {"{JEcy}", "4Ax" },  // cyrillic large letter je
        {"{jecy}", "6Ax" },  // cyrillic small letter je
        {"{JIcy}", "E8x 49x" },  // cyrillic large letter ji
        {"{jicy}", "E8x 69x" },  // cyrillic small letter ji
        {"{joiner}", "8Dx" },  // zero width joiner
        {"{K}", "4Bx" },  // latin large letter k
        {"{k}", "6Bx" },  // latin small letter k
        {"{Kcy}", "4Bx" },  // cyrillic large letter ka
        {"{kcy}", "6Bx" },  // cyrillic small letter ka
        {"{KHcy}", "4Bx 68x" },  // cyrillic large letter kha
        {"{khcy}", "6Bx 68x" },  // cyrillic small letter kha
        {"{KJEcy}", "E2x 4Bx" },  // cyrillic large letter kje
        {"{kjecy}", "E2x 6Bx" },  // cyrillic small letter kje
        {"{L}", "4Cx" },  // latin large letter l
        {"{l}", "6Cx" },  // latin small letter l
        {"{Lacute}", "E2x 4Cx" },  // latin large letter l with acute
        {"{lacute}", "E2x 6Cx" },  // latin small letter l with acute
        {"{laquo}", "22x" },  // left-pointing double angle quote mark
        {"{latin}", "28x 42x" },  // begin latin script
        {"{lcub}", "7Bx" },  // opening curly brace
        {"{Lcy}", "4Cx" },  // cyrillic large letter el
        {"{lcy}", "6Cx" },  // cyrillic small letter el
        {"{ldbltil}", "FAx" },  // combining double tilde left half
        {"{ldquo}", "22x" },  // left double quote mark
        {"{LJEcy}", "4Cx 6Ax" },  // cyrillic large letter lje
        {"{ljecy}", "6Cx 6Ax" },  // cyrillic small letter lje
        {"{llig}", "EBx" },  // combining ligature left half
        {"{lpar}", "28x" },  // left parenthesis
        {"{lsqb}", "5Bx" },  // left bracket
        {"{lsquo}", "27x" },  // left single quotation mark
        {"{lsquor}", "27x" },  // rising single quotation left (low)
        {"{Lstrok}", "A1x" },  // latin large letter l with stroke
        {"{lstrok}", "B1x" },  // latin small letter l with stroke
        {"{lt}", "3Cx" },  // less than
        {"{M}", "4Dx" },  // latin large letter m
        {"{m}", "6Dx" },  // latin small letter m
        {"{macr}", "E5x" },  // combining macron
        {"{Mcy}", "4Dx" },  // cyrillic large letter em
        {"{mcy}", "6Dx" },  // cyrillic small letter em
        {"{mdash}", "2Dx 2Dx" },  // m dash
        {"{middot}", "A8x" },  // middle dot
        {"{mllhring}", "B0x" },  // modifier letter left half ring (ayn)
        {"{mlprime}", "A7x" },  // modifier letter prime (soft sign)
        {"{mlPrime}", "B7x" },  // modifier letter double prime (hard sign)
        {"{mlrhring}", "AEx" },  // modifier letter right half ring (alif)
        {"{N}", "4Ex" },  // latin large letter n
        {"{n}", "6Ex" },  // latin small letter n
        {"{Nacute}", "E2x 4Ex" },  // latin large letter n with acute
        {"{nacute}", "E2x 6Ex" },  // latin small letter n with acute
        {"{Ncaron}", "E9x 4Ex" },  // latin large letter n with caron
        {"{ncaron}", "E9x 6Ex" },  // latin small letter n with caron
        {"{Ncy}", "4Ex" },  // cyrillic large letter en
        {"{ncy}", "6Ex" },  // cyrillic small letter en
        {"{ndash}", "2Dx 2Dx" },  // m dash
        {"{NJEcy}", "4Ex 6Ax" },  // cyrillic large letter nj
        {"{njecy}", "6Ex 6Ax" },  // cyrillic small letter nj
        {"{No}", "4Ex 6Fx 2Ex" },  // cyrillic abbr. for "nomer"
        {"{nonjoin}", "8Ex" },  // zero width non-joiner
        {"{Ntilde}", "E4x 4Ex" },  // latin large letter n with tilde
        {"{ntilde}", "E4x 6Ex" },  // latin small letter n with tilde
        {"{num}", "23x" },  // number sign
        {"{O}", "4Fx" },  // latin large letter o
        {"{o}", "6Fx" },  // latin small letter o
        {"{Oacute}", "E2x 4Fx" },  // latin large letter o with acute
        {"{oacute}", "E2x 6Fx" },  // latin small letter o with acute
        {"{Ocirc}", "E3x 4Fx" },  // latin large letter o with circ
        {"{ocirc}", "E3x 6Fx" },  // latin small letter o with circ
        {"{Ocy}", "4Fx" },  // cyrillic large letter o
        {"{ocy}", "6Fx" },  // cyrillic small letter o
        {"{Odblac}", "EEx 4Fx" },  // latin large letter o double acute
        {"{odblac}", "EEx 6Fx" },  // latin small letter o double acute
        {"{OElig}", "A6x" },  // latin large letter OE
        {"{oelig}", "B6x" },  // latin small letter oe
        {"{ogon}", "F1x" },  // combining ogonek (hook right)
        {"{Ograve}", "E1x 4Fx" },  // latin large letter o with grave
        {"{ograve}", "E1x 6Fx" },  // latin small letter o with grave
        {"{Ohorn}", "ACx" },  // latin large letter o with horn
        {"{ohorn}", "BCx" },  // latin small letter o with horn
        {"{ordf}", "61x" },  // feminine ordinal indicator
        {"{ordm}", "6Fx" },  // masculine ordinal indicator
        {"{Ostrok}", "A2x" },  // latin large letter o with stroke
        {"{ostrok}", "B2x" },  // latin small letter o with stroke
        {"{osuper}", "6Fx" },  // latin small letter superscript o
        {"{Otilde}", "E4x 4Fx" },  // latin large letter o with tilde
        {"{otilde}", "E4x 6Fx" },  // latin small letter o with tilde
        {"{Ouml}", "E8x 4Fx" },  // latin large letter o with uml
        {"{ouml}", "E8x 6Fx" },  // latin small letter o with uml
        {"{P}", "50x" },  // latin large letter p
        {"{p}", "70x" },  // latin small letter p
        {"{para}", "7Cx" },  // pilcrow (paragraph)
        {"{Pcy}", "50x" },  // cyrillic large letter pe
        {"{pcy}", "70x" },  // cyrillic small letter pe
        {"{percnt}", "25x" },  // percent sign
        {"{period}", "2Ex" },  // period (decimal point)
        {"{phono}", "C2x" },  // sound recording copyright
        {"{pipe}", "7Cx" },  // pipe
        {"{plus}", "2Bx" },  // plus
        {"{plusmn}", "ABx" },  // plus-minus sign
        {"{pound}", "B9x" },  // pound sign
        {"{Q}", "51x" },  // latin large letter q
        {"{q}", "71x" },  // latin small letter q
        {"{quest}", "3Fx" },  // question mark
        {"{quot}", "22x" },  // quotation mark
        {"{R}", "52x" },  // latin large letter r
        {"{r}", "72x" },  // latin small letter r
        {"{Racute}", "E2x 52x" },  // latin large letter r with acute
        {"{racute}", "E2x 72x" },  // latin small letter r with acute
        {"{raquo}", "22x" },  // right-pointing double angle quotation mark
        {"{Rcaron}", "E9x 52x" },  // latin large letter r with caron
        {"{rcaron}", "E9x 72x" },  // latin small letter r with caron
        {"{rcedil}", "F8x" },  // combining right cedilla
        {"{rcommaa}", "EDx" },  // combining comma above right
        {"{rcub}", "7Dx" },  // closing curly brace
        {"{Rcy}", "52x" },  // cyrillic large letter er
        {"{rcy}", "72x" },  // cyrillic small letter er
        {"{rdbltil}", "FBx" },  // combining double tilde right half
        {"{rdquofh}", "22x" },  // falling double quotation right (high)
        {"{rdquor}", "22x" },  // rising double quotation right (high)
        {"{reg}", "AAx" },  // registered sign
        {"{ring}", "EAx" },  // combining ring above
        {"{ringb}", "F4x" },  // combining ring below
        {"{rlig}", "ECx" },  // combining ligature right half
        {"{rpar}", "29x" },  // right parenthesis
        {"{rs}", "1Ex" },  // record separator (end of field)
        {"{rsqb}", "5Dx" },  // right bracket
        {"{rsquo}", "27x" },  // right single quotation mark
        {"{rsquor}", "27x" },  // rising single quotation right (high)
        {"{S}", "53x" },  // latin large letter s
        {"{s}", "73x" },  // latin small letter s
        {"{Sacute}", "E2x 53x" },  // latin capital s with acute
        {"{sacute}", "E2x 73x" },  // latin small s with acute
        {"{Scommab}", "F7x 53x" },  // latin large letter s with comma below
        {"{scommab}", "F7x 73x" },  // latin small letter s with comma below
        {"{scriptl}", "C1x" },  // latin small letter script l
        {"{Scy}", "53x" },  // cyrillic large letter es
        {"{scy}", "73x" },  // cyrillic small letter es
        {"{sect}", "7Cx" },  // section sign
        {"{semi}", "3Bx" },  // semicolon
        {"{sharp}", "C4x" },  // sharp
        {"{SHCHcy}", "53x 68x 63x 68x" },  // cyrillic large letter shcha
        {"{shchcy}", "73x 68x 63x 68x" },  // cyrillic small letter shcha
        {"{SHcy}", "53x 68x" },  // cyrillic large letter sha
        {"{shcy}", "73x 68x" },  // cyrillic small letter sha
        {"{shy}", "2Dx" },  // soft hyphen (CP850)
        {"{SOFTcy}", "A7x" },  // cyrillic large letter softsign
        {"{softcy}", "A7x" },  // cyrillic smalll letter softsign
        {"{softsign}", "A7x" },  // modifier letter soft sign
        {"{sol}", "2Fx" },  // slash (solidus)
        {"{space}", "20x" },  // space (blank)
        {"{spcirc}", "5Ex" },  // spacing circumflex
        {"{spgrave}", "60x" },  // spacing grave
        {"{sptilde}", "7Ex" },  // spacing tilde
        {"{spundscr}", "5Fx" },  // spacing underscore
        {"{squf}", "7Cx" },  // fill character
        {"{sub}", "62x" },  // begin subscript
        {"{sup1}", "1Bx 70x 31x 1Bx 73x"},  // 027d 112d 049d 027d 115d
        {"{sup2}", "1Bx 70x 32x 1Bx 73x"},  // 027d 112d 050d 027d 115d
        {"{sup3}", "1Bx 70x 33x 1Bx 73x"},  // 027d 112d 051d 027d 115d
        {"{super}", "70x" },  // begin superscript
        {"{szlig}", "73x 73x" },  // latin small letter sharp s (german)
        {"{T}", "54x" },  // latin large letter t
        {"{t}", "74x" },  // latin small letter t
        {"{Tcaron}", "E9x 54x" },  // latin large letter t with caron
        {"{tcaron}", "E9x 74x" },  // latin small letter t with caron
        {"{Tcommab}", "F7x 54x" },  // latin large letter t with comma below (hook left)
        {"{tcommab}", "F7x 74x" },  // latin small letter t with comma below (hook left)
        {"{Tcy}", "54x" },  // cyrillic large letter te
        {"{tcy}", "74x" },  // cyrillic small letter te
        {"{THORN}", "A4x" },  // latin large letter thorn (icelandic)
        {"{thorn}", "B4x" },  // latin small letter thorn (icelandic)
        {"{tilde}", "E4x" },  // combining tilde
        {"{times}", "78x" },  // times sign
        {"{trade}", "28x 54x 6Dx 29x" },  // trade mark sign
        {"{TScy}", "EBx 54x ECx 53x" },  // cyrillic large letter tse
        {"{tscy}", "EBx 74x ECx 73x" },  // cyrillic small letter tse
        {"{TSHEcy}", "E2x 43x" },  // latin large letter tshe
        {"{tshecy}", "E2x 63x" },  // latin small letter tshe
        {"{U}", "55x" },  // latin large letter u
        {"{u}", "75x" },  // latin small letter u
        {"{Uacute}", "E2x 55x" },  // latin large letter u with acute
        {"{uacute}", "E2x 75x" },  // latin small letter u with acute
        {"{Ubrevecy}", "E6x 55x" },  // cyrillic large letter u with breve
        {"{ubrevecy}", "E6x 75x" },  // cyrillic small letter u with breve
        {"{Ucirc}", "E3x 55x" },  // latin large letter u with circ
        {"{ucirc}", "E3x 75x" },  // latin small letter u with circ
        {"{Ucy}", "55x" },  // cyrillic large letter u
        {"{ucy}", "75x" },  // cyrillic small letter u
        {"{Udblac}", "EEx 55x" },  // latin large letter u with double acute
        {"{udblac}", "EEx 75x" },  // latin small letter u with double acute
        {"{Ugrave}", "E1x 55x" },  // latin large letter u with grave
        {"{ugrave}", "E1x 75x" },  // latin small letter u with grave
        {"{Uhorn}", "ADx" },  // latin large letter u with horn
        {"{uhorn}", "BDx" },  // latin small letter u with horn
        {"{uml}", "E8x" },  // combining umlaut
        {"{under}", "F6x" },  // combining underscore
        {"{Uring}", "EAx 55x" },  // latin large letter u with ring
        {"{uring}", "EAx 75x" },  // latin small letter u with ring
        {"{us}", "1Fx" },  // unit separator (subfield delimiter)
        {"{Uuml}", "E8x 55x" },  // latin large letter u with uml
        {"{uuml}", "E8x 75x" },  // latin small letter u with uml
        {"{V}", "56x" },  // latin large letter v
        {"{v}", "76x" },  // latin small letter v
        {"{Vcy}", "56x" },  // cyrillic large letter ve
        {"{vcy}", "76x" },  // cyrillic small letter ve
        {"{verbar}", "7Cx" },  // vertical bar (fill character)
        {"{vlineb}", "F2x" },  // combining vertical line below
        {"{W}", "57x" },  // latin large letter w
        {"{w}", "77x" },  // latin small letter w
        {"{X}", "58x" },  // latin large letter x
        {"{x}", "78x" },  // latin small letter x
        {"{Y}", "59x" },  // latin large letter y
        {"{y}", "79x" },  // latin small letter y
        {"{Yacute}", "E2x 59x" },  // latin large letter y
        {"{yacute}", "E2x 79x" },  // latin small letter y
        {"{YAcy}", "EBx 49x ECx 41x" },  // cyrillic large letter ia
        {"{yacy}", "EBx 69x ECx 61x" },  // cyrillic small letter ia
        {"{Ycy}", "59x" },  // cyrillic large letter yeri
        {"{ycy}", "79x" },  // cyrillic small letter yeri
        {"{YEcy}", "45x" },  // cyrillic large letter ye
        {"{yecy}", "65x" },  // cyrillic small letter ye
        {"{yen}", "59x" },  // yen (CP850)
        {"{YIcy}", "49x" },  // cyrillic large letter yi
        {"{yicy}", "69x" },  // cyrillic small letter yi
        {"{YUcy}", "EBx 49x ECx 55x" },  // cyrillic large letter iu
        {"{yucy}", "EBx 69x ECx 75x" },  // cyrillic small letter iu
        {"{Z}", "5Ax" },  // latin large letter z
        {"{z}", "7Ax" },  // latin small letter z
        {"{Zacute}", "E2x 5Ax" },  // latin large letter z with acute
        {"{zacute}", "E2x 7Ax" },  // latin small letter z with acute
        {"{Zcy}", "5Ax" },  // cyrillic large letter ze
        {"{zcy}", "7Ax" },  // cyrillic small letter ze
        {"{Zdot}", "E7x 5Ax" },  // latin large letter z with dot above
        {"{zdot}", "E7x 7Ax" },  // latin small letter z with dot above
        {"{ZHcy}", "5Ax 68x" },  // cyrillic large letter zhe
        {"{zhcy}", "7Ax 68x" },  // cyrillic small letter zhe
        {"{ZHuacy}", "EBx 5Ax ECx 68x" },  // ukrainian large letter zhe
        {"{zhuacy}", "EBx 7Ax ECx 68x" },  // ukrainian small letter zhe
        };
}
