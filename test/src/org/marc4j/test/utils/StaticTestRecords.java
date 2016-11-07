package org.marc4j.test.utils;

import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;


public class StaticTestRecords {
    public static final String RESOURCES_ILLEGAL_MARC_IN_JSON_JSON = "/illegal-marc-in-json.json";
    public static final String RESOURCES_MARC_IN_JSON_JSON = "/marc-in-json.json";
    public static final String RESOURCES_MARC_JSON_JSON = "/marc-json.json";
    public static final String RESOURCES_LEGAL_JSON_MARC_IN_JSON_JSON = "/legal-json-marc-in-json.json";
    public static final String RESOURCES_BAD_LEADERS_10_11_MRC = "/bad_leaders_10_11.mrc";
    public static final String RESOURCES_BAD_TOO_LONG_PLUS_2_MRC = "/bad_too_long_plus_2.mrc";
    public static final String RESOURCES_SUMMERLAND_MRC = "/summerland.mrc";
    public static final String RESOURCES_SUMMERLAND_XML = "/summerland.xml";
    public static final String RESOURCES_SUMMERLAND_MARC_IN_JSON_JSON = "/summerland-marc-in-json.json";
    public static final String RESOURCES_SUMMERLAND_MARC_IN_JSON_INDENTED_JSON = "/summerland-marc-in-json-indented.json";
    public static final String RESOURCES_SUMMERLAND_MARC_JSON_JSON = "/summerland-marc-json.json";
    public static final String RESOURCES_SUMMERLAND_INDENTED_MARC_JSON_JSON = "/summerland-indented-marc-json.json";
    public static final String RESOURCES_BRKRTEST_MRC = "/brkrtest.mrc";
    public static final String RESOURCES_CHABON_MRC = "/chabon.mrc";
    public static final String RESOURCES_CHABON_XML = "/chabon.xml";
    public static final String RESOURCES_OCLC814388508_XML = "/OCLC_814388508.xml";    
    public static final String RESOURCES_CYRILLIC_CAPITAL_E_MRC = "/cyrillic_capital_e.mrc";
    public static final String RESOURCES_GREEK_MISSING_CHARSET_MRC = "/greekmissingcharsetchange.mrc";
    public static final String RESOURCES_CHINESE_MANGLED_MULTIBYTE_MRC = "/chinese_mangled_multibyte.mrc";
    public static final String RESOURCES_BAD_NUMERIC_CHARACTER_REFERENCE_MRC = "/loongboonmee.mrc";
    public static final String RESOURCES_CHINESE_WITH_CENTRAL_DOT_MRC = "/oclc_63111280_export_as_UTF8_from_connexion.mrc";
    public static final String RESOURCES_BAD_CHARACTERS_IN_VARIOUS_FIELDS_MRC = "/bad-characters-in-various-fields.mrc";
    public static final String RESOURCES_PRIDE_AND_PREJUDICE_ERRORS_MRC = "/pride-and-prejudice-with-many-errors.mrc";
    public static final String RESOURCES_PRIDE_AND_PREJUDICE_FIXED_MRC = "/pride-and-prejudice-fixed.mrc";
    public static final String RESOURCES_6_BYTE_OFFSET_IN_DIRECTORY = "/datos-20161010-slice.mrc";
    public static final String RESOURCES_UNORDERED_DIRECTORY = "/unordered-directory-entries.mrc";
    public static final String RESOURCES_BAD_TOO_LARGE_HATHI_RECORD = "/bad_hathi_records.mrc";

    public static Record chabon[] = new Record[2];
    public static Record summerland[] = new Record[1];
    private static MarcFactory factory;

    static {
        factory = MarcFactory.newInstance();
        chabon[0] = factory.newRecord("00759cam a2200229 a 4500");
        chabon[0].addVariableField(factory.newControlField("001", "11939876"));
        chabon[0].addVariableField(factory.newControlField("005", "20041229190604.0"));
        chabon[0].addVariableField(factory.newControlField("008", "000313s2000    nyu           000 1 eng  "));
        chabon[0].addVariableField(factory.newDataField("020", ' ', ' ', "a", "0679450041 (acid-free paper)"));
        chabon[0].addVariableField(factory.newDataField("040", ' ', ' ', "a", "DLC", "c", "DLC", "d", "DLC"));
        chabon[0].addVariableField(factory.newDataField("100", '1', ' ', "a", "Chabon, Michael."));
        chabon[0].addVariableField(factory.newDataField("245", '1', '4', "a", "The amazing adventures of Kavalier and Clay :", "b", "a novel /", "c", "Michael Chabon."));
        chabon[0].addVariableField(factory.newDataField("260", ' ', ' ', "a", "New York :", "b", "Random House,", "c", "c2000."));
        chabon[0].addVariableField(factory.newDataField("300", ' ', ' ', "a", "639 p. ;", "c", "25 cm."));
        chabon[0].addVariableField(factory.newDataField("650", ' ', '0', "a", "Comic books, strips, etc.", "x", "Authorship", "v", "Fiction."));
        chabon[0].addVariableField(factory.newDataField("650", ' ', '0', "a", "Heroes in mass media", "v", "Fiction."));
        chabon[0].addVariableField(factory.newDataField("650", ' ', '0', "a", "Czech Americans", "v", "Fiction."));
        chabon[0].addVariableField(factory.newDataField("651", ' ', '0', "a", "New York (N.Y.)", "v", "Fiction."));
        chabon[0].addVariableField(factory.newDataField("650", ' ', '0', "a", "Young men", "v", "Fiction."));
        chabon[0].addVariableField(factory.newDataField("650", ' ', '0', "a", "Cartoonists", "v", "Fiction."));
        chabon[0].addVariableField(factory.newDataField("655", ' ', '7', "a", "Humorous stories.", "2", "gsafd"));
        chabon[0].addVariableField(factory.newDataField("655", ' ', '7', "a", "Bildungsromane.", "2", "gsafd"));

        chabon[1] = factory.newRecord("00714cam a2200205 a 4500");
        chabon[1].addVariableField(factory.newControlField("001", "12883376"));
        chabon[1].addVariableField(factory.newControlField("005", "20030616111422.0"));
        chabon[1].addVariableField(factory.newControlField("008", "020805s2002    nyu    j      000 1 eng  "));
        chabon[1].addVariableField(factory.newDataField("020", ' ', ' ', "a", "0786808772"));
        chabon[1].addVariableField(factory.newDataField("020", ' ', ' ', "a", "0786816155 (pbk.)"));
        chabon[1].addVariableField(factory.newDataField("040", ' ', ' ', "a", "DLC", "c", "DLC", "d", "DLC"));
        chabon[1].addVariableField(factory.newDataField("100", '1', ' ', "a", "Chabon, Michael."));
        chabon[1].addVariableField(factory.newDataField("245", '1', '0', "a", "Summerland /", "c", "Michael Chabon."));
        chabon[1].addVariableField(factory.newDataField("250", ' ', ' ', "a", "1st ed."));
        chabon[1].addVariableField(factory.newDataField("260", ' ', ' ', "a", "New York :", "b", "Miramax Books/Hyperion Books for Children,", "c", "c2002."));
        chabon[1].addVariableField(factory.newDataField("300", ' ', ' ', "a", "500 p. ;", "c", "22 cm."));
        chabon[1].addVariableField(factory.newDataField("520", ' ', ' ', "a", "Ethan Feld, the worst baseball player in the history of the game, finds himself recruited by a 100-year-old scout to help a band of fairies triumph over an ancient enemy."));
        chabon[1].addVariableField(factory.newDataField("650", ' ', '1', "a", "Fantasy."));
        chabon[1].addVariableField(factory.newDataField("650", ' ', '1', "a", "Baseball", "v", "Fiction."));
        chabon[1].addVariableField(factory.newDataField("650", ' ', '1', "a", "Magic", "v", "Fiction."));

        summerland[0] = makeSummerlandRecord();
    }

    private static Record makeSummerlandRecord() {
        Record sumland = factory.newRecord("00714cam a2200205 a 4500");
        sumland.addVariableField(factory.newControlField("001", "12883376"));
        sumland.addVariableField(factory.newControlField("005", "20030616111422.0"));
        sumland.addVariableField(factory.newControlField("008", "020805s2002    nyu    j      000 1 eng  "));
        sumland.addVariableField(factory.newDataField("020", ' ', ' ', "a", "0786808772"));
        sumland.addVariableField(factory.newDataField("020", ' ', ' ', "a", "0786816155 (pbk.)"));
        sumland.addVariableField(factory.newDataField("040", ' ', ' ', "a", "DLC", "c", "DLC", "d", "DLC"));
        sumland.addVariableField(factory.newDataField("100", '1', ' ', "a", "Chabon, Michael."));
        sumland.addVariableField(factory.newDataField("245", '1', '0', "a", "Summerland /", "c", "Michael Chabon."));
        sumland.addVariableField(factory.newDataField("250", ' ', ' ', "a", "1st ed."));
        sumland.addVariableField(factory.newDataField("260", ' ', ' ', "a", "New York :", "b", "Miramax Books/Hyperion Books for Children,", "c", "c2002."));
        sumland.addVariableField(factory.newDataField("300", ' ', ' ', "a", "500 p. ;", "c", "22 cm."));
        sumland.addVariableField(factory.newDataField("520", ' ', ' ', "a", "Ethan Feld, the worst baseball player in the history of the game, finds himself recruited by a 100-year-old scout to help a band of fairies triumph over an ancient enemy."));
        sumland.addVariableField(factory.newDataField("650", ' ', '1', "a", "Fantasy."));
        sumland.addVariableField(factory.newDataField("650", ' ', '1', "a", "Baseball", "v", "Fiction."));
        sumland.addVariableField(factory.newDataField("650", ' ', '1', "a", "Magic", "v", "Fiction."));
        return sumland;
    }


    public static Record getSummerlandRecord() {
        return makeSummerlandRecord();
    }

    public static MarcFactory getFactory() {
        return factory;
    }
}
