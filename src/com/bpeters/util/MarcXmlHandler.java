package com.bpeters.util;

import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.AttributesImpl;
import com.bpeters.marc4j.MarcHandler;
import com.bpeters.marc.MarcConstants;
import com.bpeters.marc.Leader;
import com.bpeters.marc.Tag;
import com.bpeters.util.AnselToUnicode;

/**
 * <p><code>MarcXmlHandler</code> implements the {@link MarcHandler} interface
 * to convert MARC21 records to XML using SAX2 events.   </p>
 *
 * <p>The records are structured using the following document format:</p>
 *
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8" ?&gt;
 * &lt;file&gt;
 *   &lt;record&gt;
 *     &lt;leader&gt;
 *       &lt;recordStatus&gt;&lt;/recordStatus&gt;
 *       &lt;recordType&gt;&lt;/recordType&gt;
 *       &lt;bibliographicLevel&gt;&lt;/bibliographicLevel&gt;
 *       &lt;typeOfControl&gt;&lt;/typeOfControl&gt;
 *       &lt;characterCodingScheme&gt;&lt;/characterCodingScheme&gt;
 *       &lt;encodingLevel&gt;&lt;/encodingLevel&gt;
 *       &lt;descriptiveCatalogingForm&gt;&lt;/descriptiveCatalogingForm&gt;
 *       &lt;linkedRecordRequirement&gt;&lt;/linkedRecordRequirement&gt;
 *     &lt;/leader&gt;
 *     &lt;controlNumber&gt;&lt;/controlNumber&gt;
 *     &lt;controlField tag=""&gt;&lt;/ControlField&gt;
 *     ...
 *     &lt;dataField tag="" ind1="" ind2=""&gt;
 *       &lt;subfield identifier=""&gt;&lt;/subfield&gt;
 *       ...
 *     &lt;/dataField&gt;
 *     ...
 *   &lt;/record&gt;
 *   ...
 * &lt;/file&gt;
 * </pre>
 * <p>A RELAX NG schema describing the structure of the MARC XML document
 * that <code>MarcXmlHandler</code> generates is available at
 * <a href="http://www.bpeters.com/relaxng/marcfile.rng">
 * http://www.bpeters.com/relaxng/marcfile.rng</a>.</p>
 *
 * <p>Notes about the behaviour of <code>MarcXmlHandler</code>:</p>
 * <ul>
 * <li>If <code>bibliographicLevel</code>, <code>typeOfControl</code>,
 * <code>encodingLevel</code>, <code>descriptiveCatalogingForm</code>
 * or <code>linkedRecordRequirement</code> contains a blank, the element
 * is implied.</li>
 * <li>By default ANSEL characters are converted to UCS/Unicode:
 * <ul>
 * <li>If the character coding scheme (leader character position 09) is a blank,
 * the subfield data will be converted to UCS/Unicode and Leader character
 * position 9 is set to 'a'.</li>
 * <li>Check {@link AnselToUnicode} for details about
 * ANSEL to UCS/Unicode conversions in James.</li>
 * <li>Use {@link #setAnselToUnicode} to control the conversion behaviour of
 * <code>MarcXmlHandler</code>.</li>
 * </ul>
 * </li>
 * </ul>
 *
 *
 * @author Bas Peters - <a href="mailto:mail@bpeters.com">mail@bpeters.com</a>
 * @version $$Version: 0.5$$
 */
public class MarcXmlHandler extends XMLFilterImpl implements MarcHandler {

    /** UCS/Unicode value for Leader character position 9 */
    protected static final char UNICODE = 0x61;

    protected static final char BLANK = MarcConstants.BLANK;

    /** True if the record is ANSEL encoded */
    protected boolean ansel = false;

    /**
     * <p>If true ANSEL characters will be converted to UCS/Unicode when the
     * character encoding is ANSEL. If false characters will not be converted.</p>
     */
    protected boolean convert = true;

    /** ContentHandler object */
    protected ContentHandler contentHandler;

    /**
     * <p>If true ANSEL encoded records will be converted ti UCS/Unicode. </p>
     *
     * <p>Records will only be converted when record label position 9 is
     * a blank (0x20(hex)).</p>
     *
     * @param convert if convert is true ANSEL characters will be converted to
     *                UCS/Unicode, if false characters will not be converted.
     */
    public void setAnselToUnicode(boolean convert) {
        this.convert = convert;
    }

    /**
     * <p>Returns the document handler being used, starts the document
     * and reports the root element.  </p>
     *
     */
    public void startFile() {
    	try {
            contentHandler = getContentHandler();
            contentHandler.startDocument();
            contentHandler.ignorableWhitespace("\n".toCharArray(), 0, 1);
            contentHandler.startElement("", "file", "file", new AttributesImpl());
        } catch (SAXException se) {
            se.printStackTrace();
        }
    }

    /**
     * <p>Reports the starting element for a record and the leader node.  </p>
     *
     * <p>If one of the leader elements <code>bibliographicLevel</code>,
     * <code>typeOfControl</code>, <code>encodingLevel</code>,
     * <code>descriptiveCatalogingForm</code> or
     * <code>linkedRecordRequirement</code> contains a blank,
     * the element is implied.</p>
     *
     * <p>By default ANSEL encoded records are converted to UCS/Unicode if
     * leader position 09 contains a blank.</p>
     *
     * @param leader the leader
     * @see #setAnselToUnicode
     */
    public void startRecord(Leader leader) {
        // Check the character coding scheme
        // if blank (ANSEL) create instance of Converter.
        char encoding = leader.getCharCodingScheme();
        if((leader.getCharCodingScheme() == BLANK) && convert)
            ansel = true;
            encoding = UNICODE;
	    try {
            contentHandler.ignorableWhitespace("\n  ".toCharArray(), 0, 3);
	        contentHandler.startElement("", "record", "record", new AttributesImpl());
            contentHandler.ignorableWhitespace("\n    ".toCharArray(), 0, 5);
	        contentHandler.startElement("","leader","leader", new AttributesImpl());

            // leader position 05
	        addChildToLeader("recordStatus", leader.getRecordStatus());

		    // leader position 06
	        addChildToLeader("typeOfRecord", leader.getTypeOfRecord());

            // Get implementation defined 1
            char[] impl1 = leader.getImplDefined1();

            // leader position 07
            if (impl1[0] != BLANK)
    	        addChildToLeader("bibliographicLevel", impl1[0]);

	        // leader position 08
            if (impl1[1] != BLANK)
    	        addChildToLeader("typeOfControl", impl1[1]);

	        // leader position 09
            if (encoding != BLANK)
                addChildToLeader("characterCodingScheme", encoding);

            // Get implementation defined 2
            char[] impl2 = leader.getImplDefined2();

	        // leader position 17
            if (impl2[0] != BLANK)
    	        addChildToLeader("encodingLevel", impl2[0]);

	        // leader position 18
            if (impl2[1] != BLANK)
    	        addChildToLeader("descriptiveCatalogingForm", impl2[1]);

	        // leader position 19
            if (impl2[2] != BLANK)
    	        addChildToLeader("linkedRecordRequirement", impl2[2]);

            contentHandler.ignorableWhitespace("\n    ".toCharArray(), 0, 5);
	        contentHandler.endElement("","leader","leader");
	    } catch (SAXException se) {
	        se.printStackTrace();
	    }
    }

    /**
     * <p>Reports a control field node (001-009).</p>
     *
     * @param tag the tag name
     * @param data the data element
     */
    public void controlField(String tag, char[] data) {
	    try {
            // Check if the current control field is the control number.
            if (Tag.isControlNumberField(tag)) {
                contentHandler.ignorableWhitespace("\n    ".toCharArray(), 0, 5);
                writeElement("","controlNumber","controlNumber", new AttributesImpl(), data);
            } else {
                AttributesImpl atts = new AttributesImpl();
                atts.addAttribute("", "tag", "tag", "CDATA", tag);
                contentHandler.ignorableWhitespace("\n    ".toCharArray(), 0, 5);
                writeElement("","controlNumber","controlNumber", new AttributesImpl(), data);
            }
	    } catch (SAXException se) {
	        se.printStackTrace();
	    }
    }

    /**
     * <p>Reports the starting element for a data field (010-999).</p>
     *
     * @param tag the tag name
     * @param ind1 the first indicator value
     * @param ind2 the second indicator value
     */
    public void startDataField(String tag, char ind1, char ind2) {
	    try {
	        AttributesImpl atts = new AttributesImpl();
	        atts.addAttribute("", "tag", "tag", "CDATA", tag);
	        atts.addAttribute("", "ind1", "ind1", "CDATA", String.valueOf(ind1));
	        atts.addAttribute("", "ind2", "ind2", "CDATA", String.valueOf(ind2));
            contentHandler.ignorableWhitespace("\n    ".toCharArray(), 0, 5);
	        contentHandler.startElement("","dataField","dataField", atts);
	    } catch (SAXException se) {
	        se.printStackTrace();
	    }
    }

    /**
     * <p>Reports a subfield node.</p>
     *
     * @param identifier the data element identifier
     * @param data the data element
     */
    public void subfield(char identifier, char[] data) {
	    try {
	        AttributesImpl atts = new AttributesImpl();
	        atts.addAttribute("", "identifier", "identifier", "CDATA",
			      String.valueOf(identifier));
            contentHandler.ignorableWhitespace("\n      ".toCharArray(), 0, 7);
	        contentHandler.startElement("","subfield","subfield", atts);
            if (ansel) {
                char[] unicodeData = AnselToUnicode.convert(data);
                contentHandler.characters(unicodeData,0,unicodeData.length);
            } else {
                contentHandler.characters(data,0,data.length);
            }
	        contentHandler.endElement("","subfield","subfield");
	    } catch (SAXException se) {
	        se.printStackTrace();
    	}
    }

    /**
     * <p>Reports the closing element for a data field.</p>
     *
     */
    public void endDataField(String tag) {
	    try {
            contentHandler.ignorableWhitespace("\n    ".toCharArray(), 0, 5);
	        contentHandler.endElement("","dataField","dataField");
	    } catch (SAXException se) {
	        se.printStackTrace();
	    }
    }

    /**
     * <p>Reports the closing element for a record.</p>
     *
     */
    public void endRecord() {
	    try {
            contentHandler.ignorableWhitespace("\n  ".toCharArray(), 0, 3);
	        contentHandler.endElement("","record","record");
	    } catch (SAXException se) {
	        se.printStackTrace();
	    }
    }

    /**
     * <p>Reports the closing element for the root and the end a document.  </p>
     *
     */
    public void endFile() {
	    try {
            contentHandler.ignorableWhitespace("\n".toCharArray(), 0, 1);
	        contentHandler.endElement("","file","file");
	        contentHandler.endDocument();
	    } catch (SAXException e) {
	        e.printStackTrace();
	    }
    }

    private void addChildToLeader(String element, char value)
        throws SAXException {
            contentHandler.ignorableWhitespace("\n      ".toCharArray(), 0, 7);
            writeElement("", element, element, new AttributesImpl(), value);
        }

    private void writeElement(String uri, String localName,
			      String qName, Attributes atts, char[] content)
        throws SAXException {
        contentHandler.startElement(uri, localName, qName, atts);
        contentHandler.characters(content, 0, content.length);
        contentHandler.endElement(uri, localName, qName);
    }

    private void writeElement(String uri, String localName,
			      String qName, Attributes atts, char content)
        throws SAXException {
        writeElement(uri, localName, qName, atts, String.valueOf(content).toCharArray());
    }



}
