package org.marc4j.test.utils;

import org.marc4j.*;

import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * A set of utilities for loading test record files from the classpath.
 */
public class ResourceLoadUtils {


    // get a buffered input stream from a named classpath resource.
    public static InputStream readResource(String resource) {
        return new BufferedInputStream(ResourceLoadUtils.class.getResourceAsStream(resource));
    }

    /**
     * Get a marc stream reader set to 'permissive' and which converts to UTF-8
     * @param resource
     * @return
     */
    public static MarcReader getPermissiveMarc21Reader(String resource) {
        return new MarcPermissiveStreamReader(readResource(resource), true, true);
    }

    /**
     * Get a (non-permissive) MarcStreamReader on the specified classpath resource.
     * @param resource
     * @return
     */
    public static MarcReader getMARC21Reader(String resource) {
        return new MarcStreamReader(readResource(resource));
    }

    public static MarcReader getMARCXMLReader(String resource) {
        return new MarcXmlReader(readResource(resource));
    }

    public static MarcReader getMARCInJSONReader(String resource) {
        return new MarcJsonReader(readResource(resource));
    }

    public static MarcReader getMrk8Reader(String resource) {
        return new Mrk8StreamReader(readResource(resource), true);
    }

    public static MarcReader getMarcReader(String resource) {
        String ext = resource.substring( resource.lastIndexOf(".") + 1 );

        if ( ext.toLowerCase().equals("xml")) {
            return getMARCXMLReader(resource);
        } else if ( ext.toLowerCase().equals("mrc")) {
            return getMARC21Reader(resource);
        } else if ( ext.toLowerCase().equals("json")) {
            return getMARCInJSONReader(resource);
        } else if ( ext.toLowerCase().equals("mrk") || ext.toLowerCase().equals("mrk8")) {
            return getMARCInJSONReader(resource);
        } else {
            return getMARC21Reader(resource);
        }
    }

}
