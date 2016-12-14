
package org.marc4j.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class FileFinderUtil {

    public static InputStream getFileInputStream(String propertyFileURLStr) throws IOException {
        InputStream in = null;
        URL url = new URL(propertyFileURLStr);
        in = url.openStream();

        return (in);
    }

    public static InputStream getFileInputStream(String[] propertyPaths, String propertyFileName)
            throws IOException {
        // InputStream in = null;
        String fullPropertyFileURLStr = getFileAbsoluteURL(propertyPaths, propertyFileName);
        return (getFileInputStream(fullPropertyFileURLStr));
    }

    public static String getFileAbsoluteURL(String[] directoryPaths, String fileName)
            throws IOException {
        File fileToReturn = null;
        String fullPathNameToReturn = null;
        int numFound = 0;

        // Check for Absolute path
        File file = new File(fileName);
        if (file.isAbsolute() && file.exists() && file.isFile() && file.canRead()) {
            numFound = 1;
            fileToReturn = file;
            try {
                fullPathNameToReturn = file.toURI().toURL().toExternalForm();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else if (directoryPaths != null && directoryPaths.length != 0) {
            for (String pathPrefix : directoryPaths) {
                file = new File(pathPrefix, fileName);
                if (file.exists() && file.isFile() && file.canRead()) {
                    if (fileToReturn == null) {
                        fileToReturn = file;
                    }
                    numFound++;
                }
            }
        }
        if (fileToReturn != null) {
            try {
                fullPathNameToReturn = fileToReturn.toURI().toURL().toExternalForm();
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (numFound == 0) {
            String errmsg = "Fatal error: Unable to find specified properties file: " + fileName;
            // logger.error(errmsg);
            throw new IOException(errmsg);
        } else if (numFound == 1) {
            // logger.debug("Opening file: " + fileToReturn.getAbsolutePath());
        } else if (numFound > 1) {
            // logger.info("Opening file (instead of "+(numFound-1)+
            // " other options): " + fileToReturn.getAbsolutePath());
        }
        return (fullPathNameToReturn);
    }
}
