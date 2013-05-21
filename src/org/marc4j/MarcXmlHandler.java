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
 * Creates <code>Record</code> objects from SAX events and pushes each item
 * onto the top of the <code>RecordStack</code>.
 * 
 * @author Bas Peters
 */
public class MarcXmlHandler implements ContentHandler {

  private RecordStack queue;

//  private InputSource input;

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

  /** Hashset for mapping of element strings to constants (Integer) */
  private static final HashMap<String, Integer> elementMap;

  private MarcFactory factory = null;

  static {
    elementMap = new HashMap<String, Integer>();
    elementMap.put("collection", new Integer(COLLECTION_ID));
    elementMap.put("leader", new Integer(LEADER_ID));
    elementMap.put("record", new Integer(RECORD_ID));
    elementMap.put("controlfield", new Integer(CONTROLFIELD_ID));
    elementMap.put("datafield", new Integer(DATAFIELD_ID));
    elementMap.put("subfield", new Integer(SUBFIELD_ID));
  }

  /**
   * Default constructor.
   * 
   * @param queue
   */
  public MarcXmlHandler(RecordStack queue) {
    this.queue = queue;
    factory = MarcFactory.newInstance();
  }

  public void startDocument() throws SAXException {
  }

  public void startElement(String uri, String name, String qName,
      Attributes atts) throws SAXException {

    String realname = (name.length() == 0) ? qName : name;
    Integer elementType = (Integer) elementMap.get(realname);

    if (elementType == null)
      return;

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
      sb = new StringBuffer();
      tag = atts.getValue(TAG_ATTR);
      controlField = factory.newControlField(tag);
      break;
    case DATAFIELD_ID:
      tag = atts.getValue(TAG_ATTR);
      String ind1 = atts.getValue(IND_1_ATTR);
      String ind2 = atts.getValue(IND_2_ATTR);
      if(ind1 == null) {
          throw new MarcException("missing ind1");
      }
      if(ind2 == null) {
          throw new MarcException("missing ind2");
      }
      if (ind1.length() == 0) ind1 = " ";
      if (ind2.length() == 0) ind2 = " ";
      dataField = factory.newDataField(tag, ind1.charAt(0), ind2.charAt(0));
      break;
    case SUBFIELD_ID:
      sb = new StringBuffer();
      String code = atts.getValue(CODE_ATTR);
      if(code == null || code.length() == 0) {
          code=" "; // throw new MarcException("missing subfield 'code' attribute");
      }
      subfield = factory.newSubfield(code.charAt(0));
    }
  }

  public void characters(char[] ch, int start, int length) throws SAXException {
    if (sb != null)
      sb.append(ch, start, length);
  }

  public void endElement(String uri, String name, String qName)
      throws SAXException {

    String realname = (name.length() == 0) ? qName : name;
    Integer elementType = (Integer) elementMap.get(realname);

    if (elementType == null)
      return;

    switch (elementType.intValue()) {
    case COLLECTION_ID:
      break;
    case RECORD_ID:
      queue.push(record);
      break;
    case LEADER_ID:
      Leader leader = factory.newLeader(sb.toString());
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

  public void endDocument() throws SAXException {
    queue.end();
  }

  public void ignorableWhitespace(char[] data, int offset, int length)
      throws SAXException {
    // not implemented
  }

  public void endPrefixMapping(String prefix) throws SAXException {
  }

  public void skippedEntity(String name) throws SAXException {
    // not implemented
  }

  public void setDocumentLocator(Locator locator) {
    // not implemented
  }

  public void processingInstruction(String target, String data)
      throws SAXException {
    // not implemented
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
//  not implemented
  }

}
