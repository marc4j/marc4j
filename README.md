# MARC4J

This is an experimental Mavenized fork of the MARC4J project.  For more detailed information about this fork, read its [documentation](http://ksclarke.github.io/marc4j/).

__TL;DR__

Is that too much to read?  If you just want to jump right in (and already have [Maven](http://maven.apache.org) installed), clone this project from its GitHub page, change into its project directory and type:

    mvn install

This will compile the project and run its tests.  It will install a jar file into your local Maven repository and also leave one in the 'target' folder (in case you don't care about Maven and just want the jar file).  If you want to recompile you can run the above command again.  If you want a completely clean recompile, you can type:

    mvn clean install

This will delete the whole target folder (the product of running Maven) and create a new one.  If you want to create the Javadocs for the project, you can type:

    mvn javadoc:javadoc

and they will be created in ${PROJECT}/target/site/apidocs

If you want to create the site that you see at http://ksclarke.github.io/marc4j/ you can type:

    mvn site
 
and it will be created in ${PROJECT}/target/site/

This project is ready to be saved pushed into the [Sonatype OSS](https://oss.sonatype.org/index.html#welcome) Maven repository once (and if) a MARC4J account is created.  From there, its artifacts can be easily moved into the central Maven repository.

So, poke around... experiment, and if you have any problems or questions, [feel free to contact me](mailto:ksclarke@gmail.com).

### Related Resources for Further Study

* [Format for Information Exchange](http://www.niso.org/standards/resources/Z39-2.pdf)
* [MARC21](http://www.loc.gov/marc/)
* [UNIMARC](http://www.ifla.org/VI/3/p1996-1/sec-uni.htm)
* [MARCXML](http://www.loc.gov/standards/marcxml/)
* [SAX2](http://www.saxproject.org)
* [JAXP](http://java.sun.com/xml/jaxp/index.html)
