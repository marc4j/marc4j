### Mission

The goal of MARC4J is to provide an easy to use API (Application Programming Interface) for working with MARC and MARCXML records in Java. MARC stands for MAchine Readable Cataloging and is a widely used exchange format for bibliographic data. MARCXML provides a loss-less conversion between MARC (MARC21, in particular, but also other formats like UNIMARC) and XML.  It's a literal representation of MARC in XML.

### Background

The current MARC4J is an object-oriented software library.  Releases beta 6 through beta 8a, however, were based on an event based parser, like SAX for XML. The project started as James (Java MARC events), but since there was already an open source project called James, the project was renamed to MARC4J to avoid confusion in open source communities.  The project was started by Bas Peters, but others in the library community have since assumed responsibility for its ongoing maintenance.

### Features

The MARC4J library includes:

* An easy to use interface that can handle large record sets.
* Readers and writers for both MARC and MARCXML.
* A build-in pipeline model to pre- or post-process MARCXML using XSLT stylesheets.
* A MARC record object model (like DOM for XML) for in-memory editing of MARC records.
* Support for data conversions from MARC-8 ANSEL, ISO5426 or ISO6937 to UCS/Unicode and back.
* A forgiving reader which can handle and recover from a number of structural or encoding errors in records.
* Implementation independent XML support through JAXP and SAX2, a high performance XML interface.
* Support for conversions between MARC and MARCXML.
* Tight integration with the JAXP, DOM and SAX2 interfaces.
* Easy to integrate with other XML interfaces like DOM, XOM, JDOM or DOM4J.
* Command-line utilities for MARC and MARCXML conversions.
* Javadoc documentation.

MARC4J provides readers and writers for MARC and MARCXML. A org.marc4j.MarcReader implementation parses input data and provides an iterator over a collection of org.marc4j.marc.Record objects. The record object model is also suitable for in-memory editing of MARC records, just as DOM is used for XML editing purposes. Using a org.marc4j.MarcWriter implementation it is possible to create MARC or MARCXML. Once MARC data has been converted to XML you can further process the result with XSLT, for example to convert MARC to MODS.

Although MARC4J is primarily designed for Java development you can use the command-line utilities `org.marc4j.util.MarcXmlDriver` and `org.marc4j.util.XmlMarcDriver` to convert between MARC and MARCXML. It is also possible to pre- or post-process the result using XSLT, for example to convert directly from MODS to MARC or from MARC to MODS.

### Getting Started

To get started with MARC4J, you can either 1) clone the GitHub repository, 2) download a pre-compiled jar file, or 3) if you use Maven, include MARC4J as a dependency in your Maven-managed project.

Cloning the repository and compiling the code yourself is easy.  The source is available on GitHub (so you will need Git installed), and Maven is used to compile and install the jar in your local Maven repository (so you will need Apache [Maven](http://maven.apache.org/) installed on your local machine, too).  The steps to clone and build are:

    git clone https://github.com/ksclarke/marc4j.git
    cd marc4j
    mvn install

If you just want the jar (and don't care about a local Maven repository, you can find it in the project's "target" folder).  If you want to recompile the project, you can rerun: mvn install.  If you want a completely clean build, you can run: mvn clean install.

If you would like to use a pre-compiled jar, these are available for download from the GitHub project's [download page](http://github.com/ksclarke/marc4j/releases).  There are multiple versions there.  You'll probably just want the most recent.

Finally, if you are using Maven for your projects, you can include MARC4J as a dependency in your project.  To do this, insert the following markup into your pom.xml file's "dependencies" element:

    <dependency>
	    <groupId>org.marc4j</groupId>
	    <artifactId>marc4j</artifactId>
	    <version>2.6.1</version>
    </dependency>

It's that simple.  If you have trouble, or would like to ask questions, feel free to file a ticket in the project's [issues tracker](http://github.com/ksclarke/marc4j/issues).

### Publications and Other Articles of Interest

* [Crosswalking: Processing MARC in XML Environments with MARC4J](http://www.amazon.com/Crosswalking-Processing-MARC-Environments-MARC4J/dp/1847530281)
* [A Proposal to serialize MARC in JSON](http://dilettantes.code4lib.org/blog/2010/09/a-proposal-to-serialize-marc-in-json/)
* [MARC-JSON Draft 2010-03-11](http://www.oclc.org/developer/content/marc-json-draft-2010-03-11)

### Questions?

Feel free to submit an issue to the project's [issues tracker](http://github.com/ksclarke/marc4j/issues).  You might also want to ask a question on the [Code4Lib mailing list](https://listserv.nd.edu/cgi-bin/wa?A0=CODE4LIB) since many folks on that list use MARC4J to process their MARC records.
