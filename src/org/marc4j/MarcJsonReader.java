
package org.marc4j;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.util.JsonParser;

public class MarcJsonReader implements MarcReader {

    MarcFactory factory;

    JsonParser parser;

    int parserLevel = 0;

    public final static int NO_ARRAY = 0;

    // These are used in MARC-in-JSON
    public final static int FIELDS_ARRAY = 1;

    public final static int SUBFIELDS_ARRAY = 2;

    // These are used in MARC-JSON
    public final static int CONTROLFIELD_ARRAY = 3;

    public final static int DATAFIELD_ARRAY = 4;

    public final static int SUBFIELD_ARRAY = 5;

    /**
     * Creates a MarcJsonReader from a supplied {@link InputStream}
     * 
     * @param is - an InputStream to read
     */
    public MarcJsonReader(final InputStream is) {
        parser = new JsonParser(JsonParser.OPT_INTERN_KEYWORDS |
                        JsonParser.OPT_UNQUOTED_KEYWORDS |
                        JsonParser.OPT_SINGLE_QUOTE_STRINGS);
        parser.setInput("MarcInput", new InputStreamReader(is), false);
        // if(System.getProperty("org.marc4j.marc.MarcFactory") == null)
        // {
        // System.setProperty("org.marc4j.marc.MarcFactory",
        // "org.marc4j.marc.impl.NoSortMarcFactoryImpl");
        // }
        factory = MarcFactory.newInstance();
    }

    /**
     * Creates a MarcJsonReader from the supplied {@link Reader}.
     * 
     * @param in - A Reader to use for input
     */
    public MarcJsonReader(final Reader in) {
        parser = new JsonParser(0);
        parser.setInput("MarcInput", in, false);
        // if(System.getProperty("org.marc4j.marc.MarcFactory") == null)
        // {
        // System.setProperty("org.marc4j.marc.MarcFactory",
        // "org.marc4j.marc.impl.NoSortMarcFactoryImpl");
        // }
        factory = MarcFactory.newInstance();
    }

    /**
     * Returns <code>true</code> if there is a next record; else,
     * <code>false</code>.
     */
    @Override
    public boolean hasNext() {
        int code = parser.getEventCode();

        if (code == 0 || code == JsonParser.EVT_OBJECT_ENDED) {
            code = parser.next();
        }

        if (code == JsonParser.EVT_OBJECT_BEGIN) {
            return true;
        }

        if (code == JsonParser.EVT_INPUT_ENDED) {
            return false;
        }

        throw new MarcException("Malformed JSON input");
    }

    /**
     * Returns the next {@link Record}.
     */
    @Override
    public Record next() {
        int code = parser.getEventCode();
        Record record = null;
        ControlField cf = null;
        DataField df = null;
        Subfield sf = null;
        int inArray = NO_ARRAY;

        while (true) {
            final String mname = parser.getMemberName();

            switch (code) {
                case JsonParser.EVT_OBJECT_BEGIN:
                    if (parserLevel == 0) {
                        record = factory.newRecord();
                    } else if (inArray == FIELDS_ARRAY &&
                            mname.matches("[A-Z0-9][A-Z0-9][A-Z0-9]")) {
                        df = factory.newDataField();
                        df.setTag(mname);
                    }

                    parserLevel++;
                    break;
                case JsonParser.EVT_OBJECT_ENDED:
                    parserLevel--;
                    if (parserLevel == 0) {
                        return record;
                    } else if (inArray == FIELDS_ARRAY &&
                            mname.matches("[A-Z0-9][A-Z0-9][A-Z0-9]")) {
                        record.addVariableField(df);
                        df = null;
                    } else if (inArray == DATAFIELD_ARRAY &&
                            mname.matches("datafield")) {
                        record.addVariableField(df);
                        df = null;
                    }

                    break;
                case JsonParser.EVT_ARRAY_BEGIN:
                    if (mname.equals("fields")) {
                        inArray = FIELDS_ARRAY;
                    } else if (mname.equals("subfields")) {
                        inArray = SUBFIELDS_ARRAY;
                    } else if (mname.equals("controlfield")) {
                        inArray = CONTROLFIELD_ARRAY;
                    } else if (mname.equals("datafield")) {
                        inArray = DATAFIELD_ARRAY;
                    } else if (mname.equals("subfield")) {
                        inArray = SUBFIELD_ARRAY;
                    }

                    break;
                case JsonParser.EVT_ARRAY_ENDED:
                    if (mname.equals("fields")) {
                        inArray = NO_ARRAY;
                    } else if (mname.equals("subfields")) {
                        inArray = FIELDS_ARRAY;
                    } else if (mname.equals("controlfield")) {
                        inArray = NO_ARRAY;
                    } else if (mname.equals("datafield")) {
                        inArray = NO_ARRAY;
                    } else if (mname.equals("subfield")) {
                        inArray = DATAFIELD_ARRAY;
                    }

                    break;
                case JsonParser.EVT_OBJECT_MEMBER:
                    String value = parser.getMemberValue();
                    if (JsonParser.isQuoted(value)) {
                        value = JsonParser.stripQuotes(value);
                    }

                    value = value.replaceAll("⁄", "/");

                    if (mname.equals("ind1")) {
                        df.setIndicator1(value.length() >= 1 ? value.charAt(0) : ' ');
                    } else if (mname.equals("ind2")) {
                        df.setIndicator2(value.length() >= 1 ? value.charAt(0) : ' ');
                    } else if (mname.equals("leader")) {
                        record.setLeader(factory.newLeader(value));
                    } else if (inArray == FIELDS_ARRAY && mname.matches("[A-Z0-9][A-Z0-9][A-Z0-9]")) {
                        cf = factory.newControlField(mname, value);
                        record.addVariableField(cf);
                    } else if (inArray == SUBFIELDS_ARRAY && mname.matches("[a-z0-9]")) {
                        sf = factory.newSubfield(mname.charAt(0), value);
                        df.addSubfield(sf);
                    } else if (inArray == CONTROLFIELD_ARRAY && mname.equals("tag")) {
                        cf = factory.newControlField();
                        cf.setTag(value);
                    } else if (inArray == CONTROLFIELD_ARRAY && mname.equals("data")) {
                        cf.setData(value);
                        record.addVariableField(cf);
                    } else if (inArray == DATAFIELD_ARRAY && mname.equals("tag")) {
                        df = factory.newDataField();
                        df.setTag(value);
                    } else if (inArray == DATAFIELD_ARRAY && mname.equals("ind")) {
                        df.setIndicator1(value.length() >= 1 ? value.charAt(0) : ' ');
                        df.setIndicator2(value.length() > 1 ? value.charAt(1) : ' ');
                    } else if (inArray == SUBFIELD_ARRAY && mname.equals("code")) {
                        sf = factory.newSubfield();
                        sf.setCode(value.charAt(0));
                    } else if (inArray == SUBFIELD_ARRAY && mname.equals("data")) {
                        sf.setData(value);
                        df.addSubfield(sf);
                    }

                    break;
                case JsonParser.EVT_INPUT_ENDED:
                    throw new MarcException("Premature end of input in JSON file");
            }
            code = parser.next();
        }

        // return record;
    }

}
