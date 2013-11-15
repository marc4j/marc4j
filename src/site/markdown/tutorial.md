# Tutorial

Copyright (C) 2002-2006 Bas Peters

__Table of Contents__

* Introduction
* What you should already know
* Getting the Software
* Reading MARC data
* The record object model
* Reading MARC XML data
* Reading MODS data
* Writing MARC data
* Perform character conversions
* Advanced MARC XML features
* Summary

## Introduction

This tutorial is for library programmers who want to learn to use MARC4J to process MARC and XML data. [MARC4J](http://github.com/ksclarke/marc4j) is an open source software library for working with MARC records in Java, a popular platform independent programming language. The MARC (Machine Readable Cataloging) format was originally designed to enable the exchange of bibliographic data between computer systems by providing a structure and format for the storage of bibliographic records on half-inch magnetic tape. Though today most records are transferred by other media, the exchange format has not changed since its first release in 1967 and is still widely used worldwide. At the same time, there is a growing interest in the use of XML in libraries, mainly because the Web is moving towards a platform- and application-independent interface for information services, with XML as its universal data format.

MARC4J is designed to bridge the gap between MARC and XML. The software library has build-in support for reading and writing MARC and MARC XML data. MARC XML is a simple XML schema for MARC data published by the Library of Congress. MARC4J also provides a "pipeline" to enable MARC records to go through further transformations using XSLT, for example to convert MARC records to MODS (Metadata Object Description Schema). This feature is particular useful there is currently no agreed-upon standard for XML in
library applications.

Although MARC4J can be used as a command-line tool for conversions between MARC and XML, its main goal is to provide an Application Programming Interface (API) to develop any kind of Java program or servlet that involves reading or writing MARC data. The core piece is a MARC reader that hides the complexity of the MARC record by providing a simple interface to extract information from MARC records. Support for XML is implemented using the standard Java XML interfaces as specified in Sun's Java API for XML Processing ([JAXP](http://www.ifla.org/VI/3/p1996-1/sec-uni.htm)). By limiting itself to the JAXP API, is XML processor-independent and easy to integrate in applications that build on industry standards such as SAX (Simple API for XML) or DOM (Document Object Model).

## What you should already know

This tutorial assumes that you are interested in developing Java applications that involve MARC and XML. You have a basic understanding of a MARC format like [MARC 21](http://www.loc.gov/marc/) or [UNIMARC](http://www.ifla.org/VI/3/p1996-1/sec-uni.htm) and you are familiar
with the basics of [XML](http://www.w3.org/XML/) and related standards like XML Namespaces and XSLT. Working with MARC4J does not require exceptional skills in Java programming. The API is designed to be easy to learn and easy to use. It works very straight-forwardly, and has a very shallow learning curve, so you should be able to get up and running with MARC4J very quickly. If you have no experience with the Java programming language at all, you should start with getting familiar with the basic concepts of the language. Sun's [Java Technology](http://java.sun.com/) site provides some good introductory tutorials on Java.

## Getting the Software

You can download a MARC4J distribution at http://github.com/ksclarke/marc4j. On the project home page you can find a direct link to the distribution at the Download section. You can also find links to MARC4J distributions on the Documents & Files page. A link to this page can be found in the Project tools menu. The latest version at the time of this writing is MARC4J 2.2. The download includes Javadoc documentation, source code and two JAR files: `marc4j.jar` and `normalizer.jar`. Add both files to your CLASSPATH environment variable.

### Note

Starting from release 2.0 MARC4J was completely rebuild. The 2.0 and later releases are not compatible with older versions of MARC4J. The event based parser is replaced by an easier to use interface that uses a simple iterator over a collection of MARC records.

MARC4J requires Sun JDK 1.4 or later because it uses the `java.util.regex` package (since version 2.1). The JDK already contains the JAXP and SAX2 compliant XML parser and XSLT processor required by MARC4J, but you can use a different implementation.

## Reading MARC data

For reading MARC data, MARC4J provides implementations of an interface called `org.marc4j.MarcReader`. This interface has two methods that provide an iterator to read MARC data from an input source:

    hasNext()

Returns true if the iteration has more records, false otherwise.

    next()

Returns the next record in the iteration as a `org.marc4j.marc.Record` object.

If you are familiar with the Java Collections Framework you might have used iterators. For example when you have `java.util.List` in Java you can access the items on the list through a `java.util.Iterator` that can be obtained from the `List` object:

    Iterator i = list.iterator();
    
    while (i.hasNext()) {
        Object item = i.next();
        // do something with the item object
    }

MARC4J provides two classes that implement `MarcReader`:

    org.marc4j.MarcStreamReader

An iterator over a collection of MARC records in ISO 2709 format.

    org.marc4j.MarcXmlReader

An iterator over a collection of MARC records in MARC XML format.

Let's start with reading MARC records in ISO 2709 format. To do this we need to import some classes:

    import org.marc4j.MarcReader;
    import org.marc4j.MarcStreamReader;
    import org.marc4j.marc.Record;

The first two classes are required to read MARC data and the third class
imports the class that represents a MARC record. We also need an input stream
to read records from, for example:

    InputStream in = new FileInputStream("summerland.mrc");

We can then initialize the `org.marc4j.MarcReader` implementation with the given input stream:

    MarcReader reader = new MarcStreamReader(in);

And start reading records:

    while (reader.hasNext()) {
        Record record = reader.next();
    }

If we simply want to examine the records we can write each record to standard output using the `toString()` method:

    System.out.println(record.toString());

Here is the complete program:

**Example 1. A first example**

    import org.marc4j.MarcReader;
    import org.marc4j.MarcStreamReader;
    import org.marc4j.marc.Record;
    import java.io.InputStream;
    import java.io.FileInputStream;
    
    public class ReadMarcExample {
        public static void main(String args[]) throws Exception {
            InputStream in = new FileInputStream("summerland.mrc");
            MarcReader reader = new MarcStreamReader(in);
    
            while (reader.hasNext()) {
                Record record = reader.next();
                System.out.println(record.toString());
            }
        }
    }

When you compile and run this program it will write each record in tagged display format to standard output:

**Example 2. Output in tagged display format**

    LEADER 00714cam a2200205 a 4500
    001 12883376
    005 20030616111422.0
    008 020805s2002    nyu    j      000 1 eng
    020   $a0786808772
    020   $a0786816155 (pbk.)
    040   $aDLC$cDLC$dDLC
    100 1 $aChabon, Michael.
    245 10$aSummerland /$cMichael Chabon.
    250   $a1st ed.
    260   $aNew York :$bMiramax Books/Hyperion Books for Children,$cc2002.
    300   $a500 p. ;$c22 cm.
    520   $aEthan Feld, the worst baseball player in the history of the game, finds himself recruited by a 100-year-old scout to help a band of fairies triumph over an ancient enemy.
    650  1$aFantasy.
    650  1$aBaseball$vFiction.
    650  1$aMagic$vFiction.

## The record object model

Now let's examine the `org.marc4j.marc.Record` class more closely. Basically a `Record` object provides acces to the leader and variable fields. For example the following method returns the leader:

    Leader leader = record.getLeader();

The `org.marc4j.marc.Leader` class provides access to all the leader values. While the Leader represents mostly MARC structural information, some character positions provide bibliographic information. The method `getTypeOfRecord()` for example identifies the type of material being cataloged, such as map, musical sound recording, or projected medium.

There are several methods available to retrieve variable fields. The `getVariableFields()` method for example returns all variable fields as a
`java.util.List`, but in most cases you will use methods that provide more control. The following method for example returns all control fields:

    // returns fields for tags 001 through 009
    List fields = record.getControlFields();

And this method return all data fields:

    // returns fields for tags 010 through 999
    List fields = record.getDataFields();

For control fields MARC4J does not provide you with the level of detail you might expect. You can retrieve the tag and the data, but to retrieve specific data elements at character positions you need to use some standard Java. This is because MARC4J is designed to handle different MARC formats like MARC 21 and UNIMARC. To retrieve the language of the item in a MARC 21 record, for example, you should do something like this:

    // get control field with tag 008
    ControlField field = (ControlField) record.getVariableField("008");
    String data = field.getData();
    
    // the three-character MARC language code takes character positions 35-37
    String lang = data.substring(35,38);
    System.out.println("Language: " + lang);

For our example record this would produce the following output:

    Language: eng

For the control number field MARC4J provides two specific methods. Use `getControlNumberField()` to retrieve the control number object for tag 001, or use `getControlNumber()` to retieve the control number as a `String` object.

The previous example also showed how you can retrieve variable fields for a given tag using the `getVariableField(String tag)` method. If you want to retrieve specific fields you can use one of the following methods:

    // get the first field occurence for a given tag
    DataField title = (DataField) record.getVariableField("245");
    
    // get all occurences for a particular tag
    List subjects = record.getVariableFields("650");
    
    // get all occurences for a given list of tags
    String[] tags = {"010", "100", "245", "250", "260", "300"};
    List fields = record.getVariableFields(tags);

These methods return a `org.marc4j.marc.VariableField`, so if you need to access specific methods, like `getData()` for a control field, you need to cast the variable field to a `org.marc4j.marc.ControlField` or `org.marc4j.marc.DataField`. A `DataField` is slightly more complex than a
control field since it has indicators and subfields. The following example retrieves the title information field and writes the tag, indicators and
subfields to standard output:

    DataField field = (DataField) record.getVariableField("245");
    String tag = field.getTag();
    char ind1 = field.getIndicator1();
    char ind2 = field.getIndicator2();
    
    System.out.println("Tag: " + tag + " Indicator 1: " + ind1 + " Indicator 2: " + ind2);
    
    List subfields = field.getSubfields();
    Iterator i = subfields.iterator();
    
    while (i.hasNext()) {
        Subfield subfield = (Subfield) i.next();
        char code = subfield.getCode();
        String data = subfield.getData();
    
        System.out.println("Subfield code: " + code + " Data element: " + data);
    }

For our record for _Summerland_ by Michael Chabon this would produce the following output:

    Tag: 245 Indicator 1: 1 Indicator 2: 0
    Subfield code: a Data element: Summerland /
    Subfield code: c Data element: Michael Chabon.

The `org.marc4j.marc.DataField` class also provides some methods to retrieve specific subfields:

    // retrieve the first occurrence of subfield with code 'a'
    Subfield subfield = field.getSubfield('a');
    
    // retrieve all subfields with code 'a'
    List subfields = field.getSubfields('a');

The following code snippet uses `getSubfield(char code)` to retrieve the title proper. It then removes the non-sort characters:

    // get data field 245
    DataField field = (DataField) record.getVariableField("245");
    
    // get indicator as int value
    char ind2 = field.getIndicator2();
    
    // get the title proper
    Subfield subfield = field.getSubfield('a');
    String title = subfield.getData();
    
    // remove the non sorting characters
    int nonSort = Character.digit(c, 10);
    title = title.substring(nonSort);

In addition to retrieving fields by tag name, you can also retrieve fields by data element values using the `find()` methods. The search capabilities are limited, but they can be useful when processing records. The following code snippet provides some basic examples:

    // find any field containing 'Chabon'
    List fields = record.find("Chabon");
    
    // find 'Summerland' in a title field
    List fields = record.find("245", "Summerland");
    
    // find 'Graham, Paul' in main or added entries for a personal name:
    String tags = {"100", "600"};
    List fields = record.find(tags, "Graham, Paul")

The find method is also useful if you want to retrieve records that meet certain criteria, such as a specific control number, title words or a
particular publisher or subject. The example below checks if the cataloging agency is DLC. It also shows how you can extend the find capabilities to specific subfields, a feature not directly available in MARC4J, since it is easy to accomplish using the record object model together with the standard Java API's.

**Example 3. A check agency program**
    
    import java.io.InputStream;
    import java.io.FileInputStream;
    import org.marc4j.MarcReader;
    import org.marc4j.MarcStreamReader;
    import org.marc4j.marc.Record;
    import org.marc4j.marc.DataField;
    import org.marc4j.marc.Subfield;
    import java.util.List;
    
    public class CheckAgencyExample {
        public static void main(String args[]) throws Exception {
            InputStream input = new FileInputStream("file.mrc");
            MarcReader reader = new MarcStreamReader(input);
    
            while (reader.hasNext()) {
                Record record = reader.next();
    
                // check if the cataloging agency is DLC
                List result = record.find("040", "DLC");
                if (result.size() > 0)
                    System.out.println("Agency for this record is DLC");
    
                // there is no specific find for a specific subfield
                // so to check if it is the orignal cataloging agency
                DataField field = (DataField)result.get(0);
                String agency = field.getSubfield('a').getData();
                if (agency.matches("DLC"))
                    System.out.println("DLC is the original agency");
                }
            }
        }

By using `find()` you can also implement a kind of search and replace to batch update records that meet certain criteria. You can use Java regular
expressions in `find()` methods. Check the java.util.regex package for more information and examples.

## Reading MARC XML data

Until now we have been processing MARC data in ISO 2709 format, but you can also read MARC data in [MARC XML](http://www.loc.gov/standards/marcxml/) format. The MARC 21 XML schema was published in June 2002 by the Library of Congress to encourage the standardization of MARC 21 records in the XML environment. The schema was developed in collaboration with OCLC and RLG after a survey of schema's that where used in various projects trying to bridge the gap between MARC and XML, including a MARC XML schema developed by the OAI (Open Archives Initiative) and the one used in early versions of MARC4J, published as James (Java MARC Events). The MARC XML schema is specified in a W3C XML Schema and provides lossless conversion between MARC ISO 2709 and MARC XML. As a consequence of the lossless conversion, information in a MARC XML record enables recreation of a MARC ISO 2709 record without loss of data. This is the record for _Summerland_ by Michael Chabon in MARC XML:

**Example 4. MARC XML record**

    <?xml version="1.0" encoding="UTF-8"?>
    <collection xmlns="http://www.loc.gov/MARC21/slim">
        <record>
            <leader>00714cam a2200205 a 4500</leader>
            <controlfield tag="001">12883376</controlfield>
            <controlfield tag="005">20030616111422.0</controlfield>
            <controlfield tag="008">020805s2002    nyu    j      000 1 eng  </controlfield>
            <datafield tag="020" ind1=" " ind2=" ">
                <subfield code="a">0786808772</subfield>
            </datafield>
            <datafield tag="020" ind1=" " ind2=" ">
                <subfield code="a">0786816155 (pbk.)</subfield>
            </datafield>
            <datafield tag="040" ind1=" " ind2=" ">
                <subfield code="a">DLC</subfield>
                <subfield code="c">DLC</subfield>
                <subfield code="d">DLC</subfield>
            </datafield>
            <datafield tag="100" ind1="1" ind2=" ">
                <subfield code="a">Chabon, Michael.</subfield>
            </datafield>
            <datafield tag="245" ind1="1" ind2="0">
                <subfield code="a">Summerland /</subfield>
                <subfield code="c">Michael Chabon.</subfield>
            </datafield>
            <datafield tag="250" ind1=" " ind2=" ">
                <subfield code="a">1st ed.</subfield>
            </datafield>
            <datafield tag="260" ind1=" " ind2=" ">
                <subfield code="a">New York :</subfield>
                <subfield code="b">Miramax Books/Hyperion Books for Children,</subfield>
                <subfield code="c">c2002.</subfield>
            </datafield>
            <datafield tag="300" ind1=" " ind2=" ">
                <subfield code="a">500 p. ;</subfield>
                <subfield code="c">22 cm.</subfield>
            </datafield>
            <datafield tag="520" ind1=" " ind2=" ">
                <subfield code="a">Ethan Feld, the worst baseball player in the history of the game, finds himself recruited by a 100-year-old scout to help a band of fairies triumph over an ancient enemy.</subfield>
            </datafield>
            <datafield tag="650" ind1=" " ind2="1">
                <subfield code="a">Fantasy.</subfield>
            </datafield>
            <datafield tag="650" ind1=" " ind2="1">
                <subfield code="a">Baseball</subfield>
                <subfield code="v">Fiction.</subfield>
            </datafield>
            <datafield tag="650" ind1=" " ind2="1">
                <subfield code="a">Magic</subfield>
                <subfield code="v">Fiction.</subfield>
            </datafield>
        </record>
    </collection>

Reading MARC XML data is not different from reading MARC data in ISO 2709 format, but MARC XML reader provides some additional XML related features. Here is our first exampe, but now reading a file containing records in MARC XML format:

**Example 5. Reading MARC XML**

    import org.marc4j.MarcReader;
    import org.marc4j.MarcXmlReader;
    import org.marc4j.marc.Record;
    import java.io.InputStream;
    import java.io.FileInputStream;
    
    public class ReadMarcXmlExample {
        public static void main(String args[]) throws Exception {
            InputStream in = new FileInputStream("summerland.xml");
            MarcReader reader = new MarcXmlReader(in);
            while (reader.hasNext()) {
                Record record = reader.next();
                System.out.println(record.toString());
            }
        }
    }

When you compile and run this program it will write each record in tagged display format to standard output:

**Example 6. Output from MARC XML in tagged display format**

    LEADER 00714cam a2200205 a 4500
    001 12883376
    005 20030616111422.0
    008 020805s2002    nyu    j      000 1 eng
    020   $a0786808772
    020   $a0786816155 (pbk.)
    040   $aDLC$cDLC$dDLC
    100 1 $aChabon, Michael.
    245 10$aSummerland /$cMichael Chabon.
    250   $a1st ed.
    260   $aNew York :$bMiramax Books/Hyperion Books for Children,$cc2002.
    300   $a500 p. ;$c22 cm.
    520   $aEthan Feld, the worst baseball player in the history of the game, finds himself recruited by a 100-year-old scout to help a band of fairies triumph over an ancient enemy.
    650  1$aFantasy.
    650  1$aBaseball$vFiction.
    650  1$aMagic$vFiction.

## Reading MODS data

Now let's look at the specific XML related features of `org.marc4j.MarcXmlreader`. Probably the most interesting feature is that you can pre-pocess the input using a stylesheet. This makes it possible to create a stylesheet in XSLT that transforms some kind of XML data to MARC XML. You
can then process the result like you would do with MARC XML or MARC in ISO 2709 format. The Library of congress, for example, provides a stylesheet that transforms MODS (Metadata Object Description Schema) to MARC XML. MODS is a schema for a bibliographic element set that is maintained by The Library of Congress. The schema provides a subset of the MARC standard, but an advantage to the MARC XML format is that it uses language-based tags rather than numeric ones. A bibliographic record for _Summerland_ by Michael Chabon in MODS looks
like this:

**Example 7. MODS record**

    <?xml version="1.0" encoding="UTF-8"?>
    <modsCollection xmlns="http://www.loc.gov/mods/v3">
        <mods version="3.0">
            <titleInfo>
                <title>Summerland</title>
            </titleInfo>
            <name type="personal">
                <namePart>Chabon, Michael.</namePart>
                <role>
                    <roleTerm authority="marcrelator" type="text">creator</roleTerm>
                </role>
            </name>
            <typeOfResource>text</typeOfResource>
            <originInfo>
                <place>
                    <placeTerm type="code" authority="marccountry">nyu</placeTerm>
                </place>
                <place>
                    <placeTerm type="text">New York</placeTerm>
                </place>
                <publisher>Miramax Books/Hyperion Books for Children</publisher>
                <dateIssued>c2002</dateIssued>
                <dateIssued encoding="marc">2002</dateIssued>
                <edition>1st ed.</edition>
                <issuance>monographic</issuance>
            </originInfo>
            <language>
                <languageTerm authority="iso639-2b" type="code">eng</languageTerm>
            </language>
            <physicalDescription>
                <form authority="marcform">print</form>
                <extent>500 p. ; 22 cm.</extent>
            </physicalDescription>
            <abstract>Ethan Feld, the worst baseball player in the history of the game, finds himself recruited by a 100-year-old scout to help a band of fairies triumph over an ancient enemy.</abstract>
            <targetAudience authority="marctarget">juvenile</targetAudience>
            <note type="statement of responsibility">Michael Chabon.</note>
            <subject>
                <topic>Fantasy</topic>
            </subject>
            <subject>
                <topic>Baseball</topic>
                <topic>Fiction</topic>
            </subject>
            <subject>
                <topic>Magic</topic>
                <topic>Fiction</topic>
            </subject>
            <identifier type="isbn">0786808772</identifier>
            <identifier type="isbn">0786816155 (pbk.)</identifier>
            <recordInfo>
                <recordContentSource authority="marcorg">DLC</recordContentSource>
                <recordCreationDate encoding="marc">020805</recordCreationDate>
                <recordChangeDate encoding="iso8601">20030616111422.0</recordChangeDate>
                <recordIdentifier>12883376</recordIdentifier>
            </recordInfo>
        </mods>
    </modsCollection>

By using a [stylesheet](http://www.loc.gov/standards/marcxml/xslt/MODS2MARC21slim.xsl) available from The Library of Congress you can process the bibliographic information contained in a collection of MODS records as MARC data:

**Example 8. Reading MODS data**

    import org.marc4j.MarcReader;
    import org.marc4j.MarcXmlReader;
    import org.marc4j.marc.Record;
    import java.io.InputStream;
    import java.io.FileInputStream;
    
    public class ModsToMarc21Example {
        public static void main(String args[]) throws Exception {
            InputStream in = new FileInputStream("mods.xml");
            MarcXmlReader reader = new MarcXmlReader(in, "http://www.loc.gov/standards/marcxml/xslt/MODS2MARC21slim.xsl");
            while (reader.hasNext()) {
                 Record record = reader.next();
                 System.out.println(record.toString());
            }
        }
    }

When you compile and run this program it will write each record in tagged display format to standard output:

**Example 9. Output from MODS data in tagged format**

    LEADER 00000nam  2200000uu 4500
    001 12883376
    005 20030616111422.0
    008 020805|2002    nyu||||j |||||||||||eng||
    020   $a0786808772
    020   $a0786816155 (pbk.)
    040   $aDLC
    100 1 $aChabon, Michael.$ecreator
    245 10$aSummerland$cMichael Chabon.
    250   $a1st ed.
    260   $aNew York$bMiramax Books/Hyperion Books for Children$cc2002$c2002
    300   $a500 p. ; 22 cm.
    520   $aEthan Feld, the worst baseball player in the history of the game, finds himself recruited by a 100-year-old scout to help a band of fairies triumph over an ancient enemy.
    650 1 $aFantasy
    650 1 $aBaseball$xFiction
    650 1 $aMagic$xFiction

The stylesheet first transforms the MODS record to MARC XML and the XSLT output is then parsed by the `org.marc4j.MarcXmlReader`.

## Writing MARC data

For writing MARC data MARC4J provides a `org.marc4j.MarcWriter` interface. This interfaces provides two important methods:

    write(Record record)

Writes a single `org.marc4j.marc.Record` to the output stream.

    close()

Closes the writer.

Let's look at an example. The following program reads the record for _Summerland_ and writes the same record back in ISO 2709 format:

**Example 10. Write MARC in ISO 2709**

    import java.io.InputStream;
    import java.io.FileInputStream;
    import org.marc4j.MarcReader;
    import org.marc4j.MarcStreamReader;
    import org.marc4j.MarcStreamWriter;
    import org.marc4j.MarcWriter;
    import org.marc4j.marc.Record;
    
    public class WriteMarcExample {
        public static void main(String args[]) throws Exception {
            InputStream input = new FileInputStream("summerland.mrc");
            MarcReader reader = new MarcStreamReader(input);
            MarcWriter writer = new MarcStreamWriter(System.out);
    
            while (reader.hasNext()) {
                Record record = reader.next();
                writer.write(record);
            }
    
            writer.close();
        }
    }

Make sure that you close the `MarcWriter` using the `close()` method.

To write the same record as MARC XML:

**Example 11. Write MARC in MARC XML format**

    import java.io.InputStream;
    import org.marc4j.MarcReader;
    import org.marc4j.MarcStreamReader;
    import org.marc4j.MarcWriter;
    import org.marc4j.MarcXmlWriter;
    import org.marc4j.converter.impl.AnselToUnicode;
    import org.marc4j.marc.Record;
    
    public class Marc2MarcXmlExample {
        public static void main(String args[]) throws Exception {
            InputStream input = new FileInputStream("summerland.mrc");
            MarcReader reader = new MarcStreamReader(input);
            MarcWriter writer = new MarcXmlWriter(System.out, true);
    
            while (reader.hasNext()) {
                Record record = reader.next();
                writer.write(record);
            }
    
            writer.close();
        }
    }

Of course you can also write MARC XML data to MARC in ISO 2709 format by using a `org.marc4j.MarcXmlReader` to read MARC XML data and a
`org.marc4j.MarcStreamWriter` to write MARC data in ISO 2709 format.

## Perform character conversions

When serializing `Record` objects you can perform character conversions. This feature is especially important when you convert MARC data between ISO 2709 and MARC XML formats. Most MARC formats use specific character sets and MARC4J is able to convert some of them to UCS/Unicode and back. Converters are available for the following encodings:

* MARC-8
* ISO 5426
* ISO 6937

Using the converters is not difficult, but there are some things to remember. MARC4J reads and writes ISO 2709 records as binary data, but data elements in control fields and subfields are converted to `String` values. When Java converts a byte array to a `String` it needs a character encoding. Java can use a default character encoding, but this might not always be the right encoding to use. Therefore both `org.marc4j.MarcReader` and `org.marc4j.MarWriter` implementations provide you with the ability to register a character encoding when constructing a new instance. If you do not provide a character encoding the following defaults are used:

**Table 1. Character encodings in MARC4J**

_org.marc4j.MarcStreamReader_

Tries to detect the encoding from the `org.marc4j.marc.Leader` by reading the character encoding scheme in the leader using the `getCharEncoding()` method. You can override the value when instantiating a `MarcStreamReader`:

    MarcReader reader = new MarcStreamReader(input, "UTF8");

_org.marc4j.MarcXmlReader_

Relies on the underlying XML parser implementation. Normally you would provide the encoding in the XML declaration of the input file:

        <?xml version="1.0" encoding="UTF-8"?>

_org.marc4j.MarcStreamWriter_

By default uses ISO 8859-1 (Latin 1) as 8-bit character set alternative since encodings like MARC-8 are not supported by Java. You can override the value when instantiating a `MarcStreamWriter`:

    MarcWriter writer = new MarcStreamWriter(ouput, "UTF8");

_org.marc4j.MarcXmlWriter_

Uses UTF-8 by default. You can override the value when instantiating a `MarcXmlWriter`:

    MarcWriter writer = new MarcXMLWriter(ouput, "UTF8");

For the encoding in the XML declaration MARC4J relies on the underlying parser.

Check the Java [supported encodings](http://java.sun.com/j2se/1.4.2/docs/guide/intl/encoding.doc.html) for the canonical name to use for a specific encoding.

Now let's look at some examples. The following program reads ISO 2709 records using the default encoding and writes the records in ISO 2709 performing a MARC-8 to UCS/Unicode conversion:

**Example 12. Write MARC in ISO 2709**

    import java.io.InputStream;
    import java.io.FileInputStream;
    import org.marc4j.MarcReader;
    import org.marc4j.MarcStreamReader;
    import org.marc4j.MarcStreamWriter;
    import org.marc4j.MarcWriter;
    import org.marc4j.marc.Record;
    
    public class Marc8ToUnicodeExample {
        public static void main(String args[]) throws Exception {
            InputStream input = new FileInputStream("summerland.mrc");
            MarcReader reader = new MarcStreamReader(input);
            MarcWriter writer = new MarcStreamWriter(System.out, "UTF8");
    
            AnselToUnicode converter = new AnselToUnicode();
            writer.setConverter(converter);
            while (reader.hasNext()) {
                Record record = reader.next();
                writer.write(record);
            }
    
            writer.close();
        }
    }

Since `MarcStreamWriter` uses the Latin-1 character encoding by default, we instantiate the writer with the UTF-8 character encoding.

To convert ISO 2709 in MARC-8 to MARC XML in UCS/Unicode:

**Example 13. Write MARC in ISO 2709**

    import java.io.InputStream;
    import java.io.FileInputStream;
    import java.io.OutputStream;
    import java.io.FileOutputStream;
    import org.marc4j.MarcReader;
    import org.marc4j.MarcStreamReader;
    import org.marc4j.MarcStreamWriter;
    import org.marc4j.MarcWriter;
    import org.marc4j.marc.Record;

    public class Marc8ToMarcXmlExample {
        public static void main(String args[]) throws Exception {
            InputStream input = new FileInputStream("summerland.mrc");
            OutputStream out = new FileOutputStream("summerland.xml");
            MarcReader reader = new MarcStreamReader(input);
            MarcWriter writer = new MarcXmlWriter(out, true);
            AnselToUnicode converter = new AnselToUnicode();
            writer.setConverter(converter);
    
            while (reader.hasNext()) {
                Record record = reader.next();
                writer.write(record);
            }
    
            writer.close();
        }
    }

In addition to using a character converter, you can perform Unicode normalization. This is for example not done by the MARC-8 to UCS/Unicode
converter. With Unicode normalization text is transformed into the canonical composed form. For example "a´bc" is normalized to "abc". To perform
normalization set Unicode normalization to true:

    MarcXmlWriter writer = new MarcXmlWriter(out, true);
    AnselToUnicode converter = new AnselToUnicode();
    writer.setConverter(converter);
    
    writer.setUnicodeNormalization(true);

### Note

Please note that it's not garanteed to work if you try to convert normalized Unicode back to MARC-8.

## Advanced MARC XML features

You can write the output of `org.marc4j.MarcXmlWriter` to an implementation of the `javax.xml.transform.Result` interface. This enables you to tightly integrate MARC4J with your XML application. Below are just some examples of what you can do using the `Result` interface. If you want to know more about Java and XML there are numerous books and tutorials available. A good tutorial is [Processing XML with Java](http://www.cafeconleche.org/books/xmljava/) by Elliotte Rusty Harold.

The `org.marc4j.MarcXmlWriter` class provides very basic formatting options. If you need more advanced formatting options, you can use a `SAXResult` containing a `ContentHandler` derived from a dedicated XML serializer. The following example uses `org.apache.xml.serialize.XMLSerializer` to write MARC records to XML using MARC-8 to UCS/Unicode conversion and Unicode
normalization:

**Example 14. Formatting output with the Xerces serializer**

    import java.io.InputStream;
    import javax.xml.transform.Result;
    import javax.xml.transform.sax.SAXResult;
    import org.apache.xml.serialize.OutputFormat;
    import org.apache.xml.serialize.XMLSerializer;
    import org.marc4j.MarcReader;
    import org.marc4j.MarcStreamReader;
    import org.marc4j.MarcXmlWriter;
    import org.marc4j.converter.impl.AnselToUnicode;
    import org.marc4j.marc.Record;
    
    public class XercesSerializerExample {
        public static void main(String args[]) throws Exception {
            InputStream input = new FileInputStream("summerland.mrc");
            MarcReader reader = new MarcStreamReader(input);
            OutputFormat format = new OutputFormat("xml", "UTF-8", true);
            XMLSerializer serializer = new XMLSerializer(System.out, format);
            Result result = new SAXResult(serializer.asContentHandler());
            MarcXmlWriter writer = new MarcXmlWriter(result);
            writer.setConverter(new AnselToUnicode());
    
            while (reader.hasNext()) {
                Record record = reader.next();
                writer.write(record);
            }
    
            writer.close();
        }
    }

You can post-process the result using a `Source` object pointing to a stylesheet resource and a Result object to hold the transformation result
tree. The example below converts MARC to MARCXML and transforms the result tree to MODS using the [stylesheet](http://www.loc.gov/standards/mods/v3/MARC21slim2MODS3.xsl) provided by The Library of Congress:

**Example 15. Write MODS data**

    import java.io.InputStream;
    import javax.xml.transform.Result;
    import javax.xml.transform.Source;
    import javax.xml.transform.stream.StreamResult;
    import javax.xml.transform.stream.StreamSource;
    import org.marc4j.MarcReader;
    import org.marc4j.MarcStreamReader;
    import org.marc4j.MarcXmlWriter;
    import org.marc4j.converter.impl.AnselToUnicode;
    import org.marc4j.marc.Record;
    
    public class Marc2ModsExample {
        public static void main(String args[]) throws Exception {
            String stylesheetUrl = "http://www.loc.gov/standards/mods/v3/MARC21slim2MODS3.xsl";
            Source stylesheet = new StreamSource(stylesheetUrl);
            Result result = new StreamResult(System.out);
            InputStream input = new FileInputStream("summerland.mrc");
            MarcReader reader = new MarcStreamReader(input);
            MarcXmlWriter writer = new MarcXmlWriter(result, stylesheet);
            writer.setConverter(new AnselToUnicode());
    
            while (reader.hasNext()) {
                Record record = (Record) reader.next();
                writer.write(record);
            }
    
            writer.close();
        }
    }

It is also possible to write the result into a DOM Node. You can then use the DOM document for further processing in your XML applicaiton, for example to embed MARC XML or MODS data in other XML documents.

**Example 16. Write output to a DOM tree**

    import java.io.InputStream;
    import javax.xml.transform.dom.DOMResult;
    import org.marc4j.MarcReader;
    import org.marc4j.MarcStreamReader;
    import org.marc4j.MarcXmlWriter;
    import org.marc4j.converter.impl.AnselToUnicode;
    import org.marc4j.marc.Record;
    import org.w3c.dom.Document;
    
    public class Marc2DomExample {
        public static void main(String args[]) throws Exception {
            InputStream input = new FileInputStream("summerland.mrc");
            MarcReader reader = new MarcStreamReader(input);
            DOMResult result = new DOMResult();
            MarcXmlWriter writer = new MarcXmlWriter(result);
            writer.setConverter(new AnselToUnicode());
    
            while (reader.hasNext()) {
                Record record = (Record) reader.next();
                writer.write(record);
            }
    
            writer.close();

            Document doc = (Document) result.getNode();
        }
    }

## Summary

This tutorial covers a lot of features of MARC4J. If this tutorial didn't show you how to do what you need to do, try looking in the Javadoc that is included in the MARC4J distribution or send an e-mail to the [Code4Lib mailing list](https://listserv.nd.edu/cgi-bin/wa?A0=CODE4LIB). Most of the samples in this tutorial are available in the `org.marc4j.samples` package.
