/**
 * Copyright (C) 2004 Bas Peters
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
package org.marc4j;

import org.marc4j.converter.CharConverter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.util.Normalizer;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.regex.Pattern;

/**
 * <p>
 * Class for writing MARC record objects in MARCXML format. This class outputs a
 * SAX event stream to the given {@link java.io.OutputStream}&nbsp; or
 * {@link javax.xml.transform.Result}&nbsp;object. It can be used in a SAX
 * pipeline to postprocess the result. By default this class uses a nulll
 * transform. It is strongly recommended to use a dedicated XML serializer.
 * </p>
 * <p>
 * This class requires a JAXP compliant XML parser and XSLT processor. The
 * underlying SAX2 parser should be namespace aware. In addition this class
 * requires <a href="http://icu.sourceforge.net/">ICU4J </a> to perform Unicode
 * normalization. A stripped down version of 2.6 originating from the <a
 * href="http://www.cafeconleche.org/XOM/">XOM </a> project is included in this
 * distribution.
 * </p>
 * <p>
 * The following example reads a file with MARC records and writes MARCXML
 * records in UTF-8 encoding to the console:
 * </p>
 *
 * <pre>
 *
 *      InputStream input = new FileInputStream(&quot;input.mrc&quot;)
 *      MarcReader reader = new MarcStreamReader(input);
 *
 *      MarcWriter writer = new MarcXmlWriter(System.out, true);
 *      while (reader.hasNext()) {
 *          Record record = reader.next();
 *          writer.write(record);
 *      }
 *      writer.close();
 *
 * </pre>
 * 
 * <p>
 * To perform a character conversion like MARC-8 to UCS/Unicode register a
 * <code>CharConverter</code>:
 * </p>
 * 
 * <pre>
 * writer.setConverter(new AnselToUnicode());
 * </pre>
 * 
 * <p>
 * In addition you can perform Unicode normalization. This is for example not
 * done by the MARC-8 to UCS/Unicode converter. With Unicode normalization text
 * is transformed into the canonical composed form. For example &quot;a�bc&quot;
 * is normalized to &quot;�bc&quot;. To perform normalization set Unicode
 * normalization to true:
 * </p>
 * 
 * <pre>
 * writer.setUnicodeNormalization(true);
 * </pre>
 * 
 * <p>
 * Please note that it's not garanteed to work if you try to convert normalized
 * Unicode back to MARC-8 encoding using
 * {@link org.marc4j.converter.impl.UnicodeToAnsel}.
 * </p>
 * <p>
 * This class provides very basic formatting options. For more advanced options
 * create an instance of this class with a
 * {@link javax.xml.transform.sax.SAXResult}&nbsp;containing a
 * {@link org.xml.sax.ContentHandler}&nbsp;derived from a dedicated XML
 * serializer.
 * </p>
 * 
 * <p>
 * The following example uses
 * <code>org.apache.xml.serialize.XMLSerializer</code> to write MARC records
 * to XML using MARC-8 to UCS/Unicode conversion and Unicode normalization:
 * </p>
 * 
 * <pre>
 *
 *      InputStream input = new FileInputStream(&quot;input.mrc&quot;)
 *      MarcReader reader = new MarcStreamReader(input);
 *
 *      OutputFormat format = new OutputFormat(&quot;xml&quot;,&quot;UTF-8&quot;, true);
 *      OutputStream out = new FileOutputStream(&quot;output.xml&quot;);
 *      XMLSerializer serializer = new XMLSerializer(out, format);
 *      Result result = new SAXResult(serializer.asContentHandler());
 *
 *      MarcXmlWriter writer = new MarcXmlWriter(result);
 *      writer.setConverter(new AnselToUnicode());
 *      while (reader.hasNext()) {
 *          Record record = reader.next();
 *          writer.write(record);
 *      }
 *      writer.close();
 *
 * </pre>
 * 
 * <p>
 * You can post-process the result using a <code>Source</code> object pointing
 * to a stylesheet resource and a <code>Result</code> object to hold the
 * transformation result tree. The example below converts MARC to MARCXML and
 * transforms the result tree to MODS using the stylesheet provided by The
 * Library of Congress:
 * </p>
 * 
 * <pre>
 *
 *      String stylesheetUrl = &quot;http://www.loc.gov/standards/mods/v3/MARC21slim2MODS3.xsl&quot;;
 *      Source stylesheet = new StreamSource(stylesheetUrl);
 *
 *      Result result = new StreamResult(System.out);
 *
 *      InputStream input = new FileInputStream(&quot;input.mrc&quot;)
 *      MarcReader reader = new MarcStreamReader(input);
 *      MarcXmlWriter writer = new MarcXmlWriter(result, stylesheet);
 *      writer.setConverter(new AnselToUnicode());
 *      while (reader.hasNext()) {
 *          Record record = (Record) reader.next();
 *          writer.write(record);
 *      }
 *      writer.close();
 *
 * </pre>
 * 
 * <p>
 * It is also possible to write the result into a DOM Node:
 * </p>
 * 
 * <pre>
 *
 *      InputStream input = new FileInputStream(&quot;input.mrc&quot;)
 *      MarcReader reader = new MarcStreamReader(input);
 *      DOMResult result = new DOMResult();
 *      MarcXmlWriter writer = new MarcXmlWriter(result);
 *      writer.setConverter(new AnselToUnicode());
 *      while (reader.hasNext()) {
 *          Record record = (Record) reader.next();
 *          writer.write(record);
 *      }
 *      writer.close();
 *
 *      Document doc = (Document) result.getNode();
 *
 * </pre>
 *
 * @author Bas Peters
 */
public class MarcXmlWriter implements MarcWriter {

    protected static final String CONTROL_FIELD = "controlfield";

    protected static final String DATA_FIELD = "datafield";

    protected static final String SUBFIELD = "subfield";

    protected static final String COLLECTION = "collection";

    protected static final String RECORD = "record";

    protected static final String LEADER = "leader";

    private boolean indent = false;

    private TransformerHandler handler = null;

    private Writer writer = null;


    /**
     * Character encoding. Default is UTF-8.
     */
    private String encoding = "UTF8";

    private CharConverter converter = null;

    private boolean normalize = false;
    
    private boolean checkNonXMLChars = false;

    /**
     * Constructs an instance with the specified output stream.
     * 
     * The default character encoding for UTF-8 is used.
     *
     * @throws MarcException
     */
    public MarcXmlWriter(OutputStream out) {
        this(out, false);
    }

    /**
     * Constructs an instance with the specified output stream and indentation.
     * 
     * The default character encoding for UTF-8 is used.
     *
     * @throws MarcException
     */
    public MarcXmlWriter(OutputStream out, boolean indent) {
        this(out, "UTF8", indent);
    }

    /**
     * Constructs an instance with the specified output stream and character
     * encoding.
     *
     * @throws MarcException
     */
    public MarcXmlWriter(OutputStream out, String encoding) {
        this(out, encoding, false);
    }

    /**
     * Constructs an instance with the specified output stream, character
     * encoding and indentation.
     *
     * @throws MarcException
     */
    public MarcXmlWriter(OutputStream out, String encoding, boolean indent) {
        this.encoding = encoding;
        if (out == null) {
            throw new NullPointerException("null OutputStream");
        }
        if (this.encoding == null) {
            throw new NullPointerException("null encoding");
        }
        try {
            setIndent(indent);
            writer = new OutputStreamWriter(out, encoding);
            writer = new BufferedWriter(writer);
            setHandler(new StreamResult(writer), null);
        } catch (UnsupportedEncodingException e) {
            throw new MarcException(e.getMessage(), e);
        }
        writeStartDocument();
    }

    /**
     * Constructs an instance with the specified result.
     *
     * @param result
     * @throws SAXException
     */
    public MarcXmlWriter(Result result) {
        if (result == null)
            throw new NullPointerException("null Result");
        setHandler(result, null);
        writeStartDocument();
    }

    /**
     * Constructs an instance with the specified stylesheet location and result.
     *
     * @param result
     * @throws SAXException
     */
    public MarcXmlWriter(Result result, String stylesheetUrl) {
        this(result, new StreamSource(stylesheetUrl));
    }

    /**
     * Constructs an instance with the specified stylesheet source and result.
     *
     * @param result
     * @throws SAXException
     */
    public MarcXmlWriter(Result result, Source stylesheet) {
        if (stylesheet == null)
            throw new NullPointerException("null Source");
        if (result == null)
            throw new NullPointerException("null Result");
        setHandler(result, stylesheet);
        writeStartDocument();
    }

    public void close() {
        writeEndDocument();
        try {
            if (writer != null) {
                writer.write("\n");
                writer.close();
            }
        } catch (IOException e) {
            throw new MarcException(e.getMessage(), e);
        }
    }

    /**
     * Returns the character converter.
     *
     * @return CharConverter the character converter
     */
    public CharConverter getConverter() {
        return converter;
    }

    /**
     * Sets the character converter.
     *
     * @param converter the character converter
     */
    public void setConverter(CharConverter converter) {
        this.converter = converter;
    }

    /**
     * If set to true this writer will perform Unicode normalization on data
     * elements using normalization form C (NFC). The default is false.
     * <p/>
     * The implementation used is ICU4J 2.6. This version is based on Unicode
     * 4.0.
     *
     * @param normalize true if this writer performs Unicode normalization, false
     *                  otherwise
     */
    public void setUnicodeNormalization(boolean normalize) {
        this.normalize = normalize;
    }

    /**
     * Returns true if this writer will perform Unicode normalization, false
     * otherwise.
     *
     * @return boolean - true if this writer performs Unicode normalization,
     *         false otherwise.
     */
    public boolean getUnicodeNormalization() {
        return normalize;
    }

    /**
     * Optional check for characters that are invalid for xml (e.g. control characters),
     * will convert to a form like "&lt;U+xxxx&gt;" where 'xxxx' is the equivalent hex value
     * of the invalid character.  Useful for poor source data, but slows down the XML emission.
     * @param checkNonXMLChars true if want to check (and replace) non-XML chars, false (default) otherwise.
     */
    public void setCheckNonXMLChars(boolean checkNonXMLChars)
    {
        this.checkNonXMLChars = checkNonXMLChars;
    }

    /**
     * Returns true if this writer will check for non-XML characters and replace them with a form like "&lt;U+xxxx&gt;", 
     * false otherwise.
     * 
     * @return boolean - true if this writer checks for (and replaces) non-XML characters, false otherwise. 
     */
    public boolean getCheckNonXMLChars()
    {
        return checkNonXMLChars;
    }
    

    protected void setHandler(Result result, Source stylesheet)
            throws MarcException {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            if (!factory.getFeature(SAXTransformerFactory.FEATURE))
                throw new UnsupportedOperationException(
                        "SAXTransformerFactory is not supported");

            SAXTransformerFactory saxFactory = (SAXTransformerFactory) factory;
            if (stylesheet == null)
                handler = saxFactory.newTransformerHandler();
            else
                handler = saxFactory.newTransformerHandler(stylesheet);
            handler.getTransformer()
                    .setOutputProperty(OutputKeys.METHOD, "xml");
            handler.setResult(result);

        } catch (Exception e) {
            throw new MarcException(e.getMessage(), e);
        }
    }

    /**
     * Writes the root start tag to the result.
     *
     * @throws SAXException
     */
    protected void writeStartDocument() {
        try {
            AttributesImpl atts = new AttributesImpl();
            handler.startDocument();
            handler.startElement(Constants.MARCXML_NS_URI, COLLECTION, COLLECTION, atts);
        } catch (SAXException e) {
            throw new MarcException(
                    "SAX error occured while writing start document", e);
        }
    }

    /**
     * Writes the root end tag to the result.
     *
     * @throws SAXException
     */
    protected void writeEndDocument() {
        try {
            if (indent)
                handler.ignorableWhitespace("\n".toCharArray(), 0, 1);

            handler
                    .endElement(Constants.MARCXML_NS_URI, COLLECTION,
                            COLLECTION);
            handler.endPrefixMapping("");
            handler.endDocument();
        } catch (SAXException e) {
            throw new MarcException(
                    "SAX error occured while writing end document", e);
        }
    }

    /**
     * Writes a Record object to the result.
     *
     * @param record -
     *               the <code>Record</code> object
     * @throws SAXException
     */
    public void write(Record record) {
        try {
            toXml(record);
        } catch (SAXException e) {
            throw new MarcException("SAX error occured while writing record", e);
        }
    }

    /**
     * Returns true if indentation is active, false otherwise.
     *
     * @return boolean
     */
    public boolean hasIndent() {
        return indent;
    }

    /**
     * Activates or deactivates indentation. Default value is false.
     *
     * @param indent
     */
    public void setIndent(boolean indent) {
        this.indent = indent;
    }

    protected void toXml(Record record) throws SAXException {
        if(!MarcFactory.newInstance().validateRecord(record)) {
            throw new MarcException("Marc record didn't validate");
        }
        char temp[];
        AttributesImpl atts = new AttributesImpl();
        if (indent)
            handler.ignorableWhitespace("\n  ".toCharArray(), 0, 3);

        handler.startElement(Constants.MARCXML_NS_URI, RECORD, RECORD, atts);

        if (indent)
            handler.ignorableWhitespace("\n    ".toCharArray(), 0, 5);

        handler.startElement(Constants.MARCXML_NS_URI, LEADER, LEADER, atts);
        Leader leader = record.getLeader();
        temp = getDataElement(leader.toString());
        handler.characters(temp, 0, temp.length);
        handler.endElement(Constants.MARCXML_NS_URI, LEADER, LEADER);

        for (ControlField field : record.getControlFields()) {
            atts = new AttributesImpl();
            atts.addAttribute("", "tag", "tag", "CDATA", getDataElementString(field.getTag()));

            if (indent)
                handler.ignorableWhitespace("\n    ".toCharArray(), 0, 5);

            handler.startElement(Constants.MARCXML_NS_URI, CONTROL_FIELD, CONTROL_FIELD, atts);
            temp = getDataElement(field.getData());
            handler.characters(temp, 0, temp.length);
            handler.endElement(Constants.MARCXML_NS_URI, CONTROL_FIELD, CONTROL_FIELD);
        }

        for (DataField field : record.getDataFields()) {
            atts = new AttributesImpl();
            atts.addAttribute("", "tag", "tag", "CDATA", getDataElementString(field.getTag()));
            atts.addAttribute("", "ind1", "ind1", "CDATA", getDataElementString(String.valueOf(field.getIndicator1())));
            atts.addAttribute("", "ind2", "ind2", "CDATA", getDataElementString(String.valueOf(field.getIndicator2())));

            if (indent)
                handler.ignorableWhitespace("\n    ".toCharArray(), 0, 5);

            handler.startElement(Constants.MARCXML_NS_URI, DATA_FIELD, DATA_FIELD, atts);
            for (Subfield subfield : field.getSubfields()) {
                atts = new AttributesImpl();
                atts.addAttribute("", "code", "code", "CDATA", getDataElementString(String.valueOf(subfield.getCode())));

                if (indent)
                    handler.ignorableWhitespace("\n      ".toCharArray(), 0, 7);

                handler.startElement(Constants.MARCXML_NS_URI, SUBFIELD, SUBFIELD, atts);
                temp = getDataElement(subfield.getData());
                handler.characters(temp, 0, temp.length);
                handler.endElement(Constants.MARCXML_NS_URI, SUBFIELD, SUBFIELD);
            }

            if (indent)
                handler.ignorableWhitespace("\n    ".toCharArray(), 0, 5);

            handler.endElement(Constants.MARCXML_NS_URI, DATA_FIELD, DATA_FIELD);
        }

        if (indent)
            handler.ignorableWhitespace("\n  ".toCharArray(), 0, 3);

        handler.endElement(Constants.MARCXML_NS_URI, RECORD, RECORD);
    }

    protected String getDataElementString(String data) {
        String dataElement = null;
        if (converter == null)
            dataElement = data;
        else
            dataElement = converter.convert(data);
        if (normalize)
            dataElement = Normalizer.normalize(dataElement, Normalizer.NFC);
        if (checkNonXMLChars)
            dataElement = CheckNonXMLChars(dataElement);
        return dataElement;
    }
    
    protected char[] getDataElement(String data) {
        return getDataElementString(data).toCharArray();
    }
    
    protected String CheckNonXMLChars(String dataElement)
    {
        StringBuffer out = new StringBuffer(dataElement.length());
        for (char ch : dataElement.toCharArray())
        {
            if (isInvalidXmlChar(ch))
            {
                out.append("<U+");
                String hex = ("0000" + Integer.toString(ch));
                out.append(hex.substring(hex.length()-4));
                out.append('>');
            }
            else
            {
                out.append(ch);
            }
        }
        return out.toString();
    }
    
    static Pattern valid = Pattern.compile("[\\u0001-\\uD7FF\\uE000-\uFFFD\\x{10000}-\\x{10FFFF}]");
    static Pattern ctrlChar = Pattern.compile("[\\u0001-\\u0008\\u000B-\\u000C\\u000E-\\u001F\\u007F-\\u0084\\u0086-\\u009F]");
    static Pattern discouraged = Pattern.compile("[\\uFDD0-\\uFDEF\\x{1FFFE}-\\x{1FFFF}\\x{2FFFE}-\\x{2FFFF}\\x{3FFFE}-\\x{3FFFF}\\x{4FFFE}-\\x{4FFFF}\\x{5FFFE}-\\x{5FFFF}\\x{6FFFE}-\\x{6FFFF}\\x{7FFFE}-\\x{7FFFF}\\x{8FFFE}-\\x{8FFFF}\\x{9FFFE}-\\x{9FFFF}\\x{AFFFE}-\\x{AFFFF}\\x{BFFFE}-\\x{BFFFF}\\x{CFFFE}-\\x{CFFFF}\\x{DFFFE}-\\x{DFFFF}\\x{EFFFE}-\\x{EFFFF}\\x{FFFFE}-\\x{FFFFF}\\x{10FFFE}-\\x{10FFFF}]");
    
    /**
     * A replacement for the method XmlChar.isInvalid(char ch) found in the 
     * class com.sun.org.apache.xerces.internal.util.XMLChar;  which, since its an 
     * internal class, apparently shouldn't be used.
     */
    private static boolean isInvalidXmlChar(char ch)
    {
        final String s = Character.toString(ch);
        // xml 1.1 spec http://en.wikipedia.org/wiki/Valid_characters_in_XML
        if (!valid.matcher(s).matches())      return true;         // not in valid ranges
        if (ctrlChar.matcher(s).matches())    return true;         // a control character
        if (discouraged.matcher(s).matches()) return true;        // "Characters allowed but discouraged"
        return false;
      }
}
