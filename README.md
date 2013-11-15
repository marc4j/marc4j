# MARC4J

This is an experimental Mavenized fork of the MARC4J project.  For more detailed information about this fork, read its [documentation](http://ksclarke.github.io/marc4j/).

### TL;DR

Is that too much to read?  If you just want to jump right in... and already have [Maven](http://maven.apache.org) installed... clone this project from its [GitHub page](http://github.com/ksclarke/marc4j), change into its project directory, and type:

    mvn install

This will compile the project and run its tests.  It will install a jar file into your local Maven repository and also leave one in the 'target' folder (in case you don't care about Maven and just want the jar file artifact).  If you want to recompile/rebuild, you can run the above command again.  If you want a completely clean build, you can type:

    mvn clean install

This will delete the whole target folder (the product of running Maven) and create a new one.

If you want to create the Javadocs for the project, you can type:

    mvn javadoc:javadoc

and they will be created in ${PROJECT}/target/site/apidocs

If you want to create the site that you see at http://ksclarke.github.io/marc4j/ you can type:

    mvn site
 
and it will be created in ${PROJECT}/target/site/

This project is ready to be pushed into the [Sonatype OSS](https://oss.sonatype.org/index.html#welcome) Maven repository once (and if) a MARC4J account is created.  From there, its artifacts can be easily moved into the central Maven repository.

So, poke around... experiment, and if you have any problems or questions, [feel free to contact me](mailto:ksclarke@gmail.com).

