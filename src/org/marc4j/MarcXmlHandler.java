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
import java.util.Map;

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
    }

    /**
     * Default constructor.
     *
     * @param queue
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
        final Integer elementType = ELEMENTS.get(realname);

        if (elementType == null) {
            return;
        }

        switch (elementType.intValue()) {
            case COLLECTION_ID:
                break;
            case RECORD_ID:
                record = factory.newRecord();
                break;
            case LEADER_ID:
                sb = new StringBuffer();
                break;
            case CONTROLFIELD_ID:
                tag = atts.getValue(TAG_ATTR);
                controlField = factory.newControlField(tag);
                sb = new StringBuffer();
                break;
            case DATAFIELD_ID:
                tag = atts.getValue(TAG_ATTR);

                String ind1 = atts.getValue(IND_1_ATTR);
                String ind2 = atts.getValue(IND_2_ATTR);

                if (ind1 == null) {
                    throw new MarcException("DataField (" + tag + ") missing first indicator");
                }

                if (ind2 == null) {
                    throw new MarcException("DataField (" + tag + ") missing second indicator");
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
                if (code == null || code.length() == 0) {
                    code = " "; // throw new
                                // MarcException("missing subfield 'code' attribute");
                }
                subfield = factory.newSubfield(code.charAt(0));
                sb = new StringBuffer();
        }
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
     * @param uri
     * @param name
     * @param qName
     */
    @Override
    public void endElement(final String uri, final String name, final String qName) throws SAXException {
        final String realname = name.length() == 0 ? qName : name;
        final Integer elementType = ELEMENTS.get(realname);

        if (elementType == null) {
            return;
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
                controlField.setData(sb.toString());
                record.addVariableField(controlField);
                break;
            case DATAFIELD_ID:
                record.addVariableField(dataField);
                break;
            case SUBFIELD_ID:
                subfield.setData(sb.toString());
                dataField.addSubfield(subfield);
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
     * @param data
     * @param offset
     * @param length
     * @throws SAXException
     */
    @Override
    public void ignorableWhitespace(final char[] data, final int offset, final int length) throws SAXException {
        // not implemented
    }

    /**
     * An event fired at the end of prefix mapping.
     *
     * @param prefix
     * @throws SAXException
     */
    @Override
    public void endPrefixMapping(final String prefix) throws SAXException {}

    /**
     * An event fired while consuming a skipped entity.
     *
     * @param name
     * @throws SAXException
     */
    @Override
    public void skippedEntity(final String name) throws SAXException {
        // not implemented
    }

    /**
     * An event fired while consuming a document locator.
     *
     * @param locator
     */
    @Override
    public void setDocumentLocator(final Locator locator) {
        // not implemented
    }

    /**
     * An event fired while consuming a processing instruction.
     *
     * @param target
     * @param data
     * @throws SAXException
     */
    @Override
    public void processingInstruction(final String target, final String data) throws SAXException {
        // not implemented
    }

    /**
     * An event fired at the start of prefix mapping.
     *
     * @param prefix
     * @param uri
     */
    @Override
    public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
        // not implemented
    }

}
