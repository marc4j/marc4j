package org.marc4j.test;

import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;


public class StaticTestRecords
{
    static Record chabon[] = new Record[2];
    static Record summerland[] = new Record[1];
    static 
    {
        MarcFactory factory = MarcFactory.newInstance();
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
        
        summerland[0] = factory.newRecord("00714cam a2200205 a 4500");
        summerland[0].addVariableField(factory.newControlField("001", "12883376"));
        summerland[0].addVariableField(factory.newControlField("005", "20030616111422.0"));
        summerland[0].addVariableField(factory.newControlField("008", "020805s2002    nyu    j      000 1 eng  "));
        summerland[0].addVariableField(factory.newDataField("020", ' ', ' ', "a", "0786808772"));
        summerland[0].addVariableField(factory.newDataField("020", ' ', ' ', "a", "0786816155 (pbk.)"));
        summerland[0].addVariableField(factory.newDataField("040", ' ', ' ', "a", "DLC", "c", "DLC", "d", "DLC"));
        summerland[0].addVariableField(factory.newDataField("100", '1', ' ', "a", "Chabon, Michael."));
        summerland[0].addVariableField(factory.newDataField("245", '1', '0', "a", "Summerland /", "c", "Michael Chabon."));
        summerland[0].addVariableField(factory.newDataField("250", ' ', ' ', "a", "1st ed."));
        summerland[0].addVariableField(factory.newDataField("260", ' ', ' ', "a", "New York :", "b", "Miramax Books/Hyperion Books for Children,", "c", "c2002."));
        summerland[0].addVariableField(factory.newDataField("300", ' ', ' ', "a", "500 p. ;", "c", "22 cm."));
        summerland[0].addVariableField(factory.newDataField("520", ' ', ' ', "a", "Ethan Feld, the worst baseball player in the history of the game, finds himself recruited by a 100-year-old scout to help a band of fairies triumph over an ancient enemy."));
        summerland[0].addVariableField(factory.newDataField("650", ' ', '1', "a", "Fantasy."));
        summerland[0].addVariableField(factory.newDataField("650", ' ', '1', "a", "Baseball", "v", "Fiction."));
        summerland[0].addVariableField(factory.newDataField("650", ' ', '1', "a", "Magic", "v", "Fiction."));

    }
}
