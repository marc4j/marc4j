// $Id: CharacterConverterLoader.java,v 1.1 2002/12/11 19:25:21 bpeters Exp $
/**
 * Copyright (C) 2002 Bas Peters (mail@bpeters.com)
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
package org.marc4j.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * <p>Loads a character converter using a system property.</p>
 *
 * @author <a href="mailto:mail@bpeters.com">Bas Peters</a> 
 * @version $Revision: 1.1 $
 *
 */
public final class CharacterConverterLoader {

    private CharacterConverterLoader() {}

    /**
     * <p>Returns a new instance for the class defined by the given system 
     * property, or default class.</p>
     *
     * <p>The method will first look for a system property, then for 
     * a properties file named <code>marc4j.properties</code>. If these 
     * options fail, an instance of the default class is returned.</p>
     *
     * @param label the label for the system property
     * @param defaultClass the default class to use if the system property is null
     * @exception CharacterConverterLoaderException is thrown if an error occurs
     */
    public static Object createCharacterConverter(String label, String defaultClass) 
	throws CharacterConverterLoaderException {
	String name = null;
	ClassLoader loader = Thread.currentThread().getContextClassLoader();
	try {
	    // System property
	    name = System.getProperty(label);

	    // Properties file
	    if (name == null) {
		String javaHome = System.getProperty ("java.home");
		File file = new File(new File(javaHome, "lib"), "marc4j.properties");
		if (file.exists() == true) {
		    FileInputStream in = new FileInputStream(file);
		    Properties props = new Properties();
		    
		    props.load(in);
		    name  = props.getProperty(label);
		    in.close();
		}
	    }

	    // Use default class
	    if (name == null)
		name = defaultClass;

	    if (name != null) {
		// Create an instance of the converter class
		Class c = loader.loadClass(name);
		return c.newInstance();
	    } else
		return null;

	} catch (Exception e) {
	    throw new CharacterConverterLoaderException(e.getMessage());
	}
    }
}
