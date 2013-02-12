/**
 * Copyright (C) 2002 Bas Peters (mail@bpeters.com)
 *
 * ValidatorDriver is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public 
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 *
 * ValidatorDriver is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with ValidatorDriver; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
import java.io.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import com.thaiopensource.relaxng.*;
import com.thaiopensource.relaxng.util.*;

/**
 * <p>This is a driver class to test ValidatorFilter.
 *
 * @author <a href="mailto:mail@bpeters.com">Bas Peters</a>
 * @see ValidatorFilter
 */
public class ValidatorDriver {
    public static void main(String args[]) {
	if(args.length < 1) {
	    System.out.println("Driver <rng-schema> <input-file> [<stylesheet>]");
	    return;
	}
	String rngSchema = args[0];
        String input = args[1];
        String stylesheet = (args.length > 2) ? args[2] : null;
        try {
	    SchemaFactory factory = new SchemaFactory();
	    factory.setXMLReaderCreator(new Jaxp11XMLReaderCreator());
	    factory.setDatatypeLibraryFactory(new org.relaxng.datatype.helpers.DatatypeLibraryLoader());
	    Schema s = factory.createSchema(new InputSource(new File(rngSchema).toURL().toString()));
	    ErrorHandlerImpl eh = new ErrorHandlerImpl(System.out);
	    ValidatorHandler vh = s.createValidator();
	    XMLReader producer = factory.getXMLReaderCreator().createXMLReader();
	    XMLFilter filter = new ValidatorFilter();
	    filter.setProperty("http://org.marc4j/relaxng/validator", vh);
	    filter.setErrorHandler(eh);
	    filter.setParent(producer);
	    InputSource in = new InputSource(new File(input).toURL().toString());
	    Source source = new SAXSource(filter, in);
	    Result result = new StreamResult(System.out);
	    TransformerFactory tFactory = TransformerFactory.newInstance();
	    Transformer transformer;
	    if (stylesheet != null)
		transformer = tFactory.newTransformer(new StreamSource(new File(stylesheet).toURL().toString()));
	    else
		transformer = tFactory.newTransformer();
	    transformer.transform(source, result);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
