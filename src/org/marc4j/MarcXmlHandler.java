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
 *
 */

package org.marc4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Creates <code>Record</code> objects from SAX events and pushes each item onto the top of the
 * <code>RecordStack</code>.
 *
 * @author Bas Peters
 */
public class MarcXmlHandler implements ContentHandler {

    private final RecordStack queue;

    private StringBuffer sb;

    private Subfield subfield;

    private ControlField controlField;

    private DataField dataField;

    private Record record;

    private String tag;

    private String prev_tag = "n/a";

    /** Constants representing each valid tag type */
    private static final int COLLECTION_ID = 1;

    private static final int LEADER_ID = 2;

    private static final int RECORD_ID = 3;

    private static final int CONTROLFIELD_ID = 4;

    private static final int DATAFIELD_ID = 5;

    private static final int SUBFIELD_ID = 6;

    /** The tag attribute name string */
    private static final String TAG_ATTR = "tag";

    /** The code attribute name string */
    private static final String CODE_ATTR = "code";

    /** The first indicator attribute name string */
    private static final String IND_1_ATTR = "ind1";

    /** The second indicator attribute name string */
    private static final String IND_2_ATTR = "ind2";

    /** The type attribute name string */
    private static final String TYPE_ATTR = "type";

    private static final Set<String> RECORD_TYPES;

    /** Set for mapping of element strings to constants (Integer) */
    private static final Map<String, Integer> ELEMENTS;

    private MarcFactory factory = null;

    static {
        ELEMENTS = new HashMap<String, Integer>();
        ELEMENTS.put("collection", new Integer(COLLECTION_ID));
        ELEMENTS.put("leader", new Integer(LEADER_ID));
        ELEMENTS.put("record", new Integer(RECORD_ID));
        ELEMENTS.put("controlfield", new Integer(CONTROLFIELD_ID));
        ELEMENTS.put("datafield", new Integer(DATAFIELD_ID));
        ELEMENTS.put("subfield", new Integer(SUBFIELD_ID));

        RECORD_TYPES = new HashSet<String>();
        RECORD_TYPES.add("Bibliographic");
        RECORD_TYPES.add("Authority");
        RECORD_TYPES.add("Holdings");
        RECORD_TYPES.add("Classification");
        RECORD_TYPES.add("Community");
    }

    /**
     * Default constructor.
     *
     * @param queue - a queue of records read
     */
    public MarcXmlHandler(final RecordStack queue) {
        this.queue = queue;
        factory = MarcFactory.newInstance();
    }

    /**
     * An event fired at the start of the document.
     */
    @Override
    public void startDocument() throws SAXException {
    }

    /**
     * An event fired at the start of an element.
     */
    @Override
    public void startElement(final String uri, final String name, final String qName, final Attributes atts)
            throws SAXException {
        final String realname = name.length() == 0 ? qName : name;
        final Integer elementType = ELEMENTS.get(stripNsPrefix(realname));

        if (elementType == null) {
            if (record != null) {
                record.addError("n/a", "n/a", MarcError.MINOR_ERROR, "Unexpected XML element: " + realname);
                return;  
            } else { 
                throw new MarcException("Unexpected XML element: " + realname);
            }
        }

        switch (elementType.intValue()) {
            case COLLECTION_ID:
                break;
            case RECORD_ID:
                final String typeAttr = atts.getValue(TYPE_ATTR);

                record = factory.newRecord();

                if (typeAttr != null && RECORD_TYPES.contains(typeAttr)) {
                    record.setType(typeAttr);
                }
                prev_tag = "n/a";
                
                break;
            case LEADER_ID:
                sb = new StringBuffer();
                break;
            case CONTROLFIELD_ID:
                tag = atts.getValue(TAG_ATTR);

                if (tag == null) {
                    if (record != null) {
                        record.addError("n/a", "n/a", MarcError.MINOR_ERROR, "Missing tag element in ControlField after tag: "+ prev_tag);
                    } else {
                        throw new MarcException("ControlField missing tag value, found outside a record element");
                    }
                    break;
                }

                controlField = factory.newControlField(tag);
                sb = new StringBuffer();
                break;
            case DATAFIELD_ID:
                tag = atts.getValue(TAG_ATTR);

                if (tag == null) {
                    if (record != null) {
                        record.addError("n/a", "n/a", MarcError.MINOR_ERROR, "Missing tag element in datafield after tag: "+prev_tag);
                    } else {
                        throw new MarcException("DataField missing tag value, found outside a record element");
                    }
                    break;
                }

                String ind1 = atts.getValue(IND_1_ATTR);
                String ind2 = atts.getValue(IND_2_ATTR);

                if (ind1 == null) {
                    if (record != null) {
                        record.addError(tag, "n/a", MarcError.MINOR_ERROR, "DataField (" + tag + ") missing first indicator");
                    } else {
                        throw new MarcException("DataField (" + tag + ") missing first indicator, found outside a record element");
                    }
                    break;
                }

                if (ind2 == null) {
                    if (record != null) {
                        record.addError(tag, "n/a", MarcError.MINOR_ERROR, "DataField (" + tag + ") missing second indicator");
                    } else {
                        throw new MarcException("DataField (" + tag + ") missing second indicator, found outside a record element");
                    }
                    break;
                }

                if (ind1.length() == 0) {
                    ind1 = " ";
                }

                if (ind2.length() == 0) {
                    ind2 = " ";
                }

                dataField = factory.newDataField(tag, ind1.charAt(0), ind2.charAt(0));
                break;
            case SUBFIELD_ID:
                String code = atts.getValue(CODE_ATTR);

                if (code == null) {
                    if (record != null) {
                        record.addError(tag, "n/a", MarcError.MINOR_ERROR, "Subfield (" + tag + ") missing code attribute");
                    } else {
                        throw new MarcException("Subfield in DataField (" + tag + ") missing code attribute");
                    }
                    break;
                }

                if (code.length() == 0) {
                    code = " ";
                }

                subfield = factory.newSubfield(code.charAt(0));
                sb = new StringBuffer();
        }
        prev_tag = tag;
    }

    /**
     * An event fired as characters are consumed.
     *
     * @param ch - an array of characters to output
     * @param start - the offset into that array to start writing from
     * @param length - the number of characters to write
     */
    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        if (sb != null) {
            sb.append(ch, start, length);
        }
    }

    /**
     * An event fired at the end of an element.
     *
     * @param uri - the uri
     * @param name - the name
     * @param qName - the qname
     */
    @Override
    public void endElement(final String uri, final String name, final String qName) throws SAXException {
        final String realname = name.length() == 0 ? qName : name;
        final Integer elementType = ELEMENTS.get(stripNsPrefix(realname));

        if (elementType == null) {
            if (record != null) {
                //record.addError("n/a", "n/a", MarcError.MINOR_ERROR, "Unexpected XML element: " + realname);
                return;  
            } else { 
                throw new MarcException("Unexpected XML element: " + realname);
            }
        }

        switch (elementType.intValue()) {
            case COLLECTION_ID:
                break;
            case RECORD_ID:
                queue.push(record);
                break;
            case LEADER_ID:
                final Leader leader = factory.newLeader(sb.toString());
                record.setLeader(leader);
                break;
            case CONTROLFIELD_ID:
                if (controlField != null) {
                    controlField.setData(sb.toString());
                    record.addVariableField(controlField);
                    controlField = null;
                }
                break;
            case DATAFIELD_ID:
                if (dataField != null) {
                    record.addVariableField(dataField);
                    dataField = null;
                }
                break;
            case SUBFIELD_ID:
                if (dataField != null && subfield != null) {
                    subfield.setData(sb.toString());
                    dataField.addSubfield(subfield);
                    subfield = null;
                }
                break;
        }

    }

    /**
     * An event fired at the end of the document.
     */
    @Override
    public void endDocument() throws SAXException {
        queue.end();
    }

    /**
     * An event fired while consuming ignorable whitespace.
     *
     * @param data - the white space data to ignore
     * @param offset - the offset at which to start ignoring
     * @param length - how many characters to ignore
     */
    @Override
    public void ignorableWhitespace(final char[] data, final int offset, final int length) throws SAXException {
        // not implemented
    }

    /**
     * An event fired at the end of prefix mapping.
     *
     * @param prefix - the prefix
     */
    @Override
    public void endPrefixMapping(final String prefix) throws SAXException {}

    /**
     * An event fired while consuming a skipped entity.
     *
     * @param name - the entity to skip
     */
    @Override
    public void skippedEntity(final String name) throws SAXException {
        // not implemented
    }

    /**
     * An event fired while consuming a document locator.
     *
     * @param locator - the locator
     */
    @Override
    public void setDocumentLocator(final Locator locator) {
        // not implemented
    }

    /**
     * An event fired while consuming a processing instruction.
     *
     * @param target - the target
     * @param data - the data
     */
    @Override
    public void processingInstruction(final String target, final String data) throws SAXException {
        // not implemented
    }

    /**
     * An event fired at the start of prefix mapping.
     *
     * @param prefix - the prefix
     * @param uri - the uri
     */
    @Override
    public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
        // not implemented
    }

    /**
     * Handle namespace prefixes; also fixes issue with broken SAX emitters that spit out QName instead of local name.
     * None of our MARCXML local names should have colons.
     *
     * @param aName An element name
     * @return The element name without a namespace prefix
     */
    private String stripNsPrefix(final String aName) {
        final int index = aName.indexOf(":");

        if (index == -1 || index + 1 == aName.length()) {
            return aName;
        } else {
            return aName.substring(index + 1);
        }
    }
}
