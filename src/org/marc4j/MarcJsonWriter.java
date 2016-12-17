
package org.marc4j;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;

import org.marc4j.converter.CharConverter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

public class MarcJsonWriter implements MarcWriter {

    public final static int MARC_IN_JSON = 0;

    public final static int MARC_JSON = 1;

    /**
     * Character encoding. Default is UTF-8.
     */
    private CharConverter converter = null;

    private OutputStream os = null;

    private int useJsonFormat = MARC_IN_JSON;

    private boolean indent = false;

    private boolean escapeSlash = false;

    private boolean quoteLabels = true;

    private String ql = "\"";

    private boolean normalize = false;

    /**
     * Creates a {@link MarcJsonWriter} with the supplied {@link OutputStream}.
     *
     * @param os - the OutputStream to write to
     */
    public MarcJsonWriter(final OutputStream os) {
        this.os = os;
    }

    /**
     * Creates a {@link MarcJsonWriter} with the supplied {@link OutputStream} using the supplied {@link CharConverter}.
     *
     * @param os - the OutputStream to write to
     * @param conv - the Character converter to use to transform the data as it is being written out.
     */
    public MarcJsonWriter(final OutputStream os, final CharConverter conv) {
        this.os = os;
        setConverter(conv);
    }

    /**
     * Creates a {@link MarcJsonWriter} with the supplied {@link OutputStream} to write using the supplied JSON format.
     *
     * @param os - the OutputStream to write to
     * @param jsonFormat - whether to use the MARC_IN_JSON format (default) or the MARC_JSON format (1)
     */
    public MarcJsonWriter(final OutputStream os, final int jsonFormat) {
        this.os = os;
        useJsonFormat = jsonFormat;

        if (useJsonFormat == MARC_JSON) {
            this.setQuoteLabels(false);
        }
    }

    /**
     * Creates a {@link MarcJsonWriter} with the supplied {@link OutputStream} using the specified {@link CharConverter}
     * to write using the specified JSON format.
     *
     * @param os - the OutputStream to write to
     * @param conv - the Character converter to use to transform the data as it is being written out.
     * @param jsonFormat - whether to use the MARC_IN_JSON format (default) or the MARC_JSON format (1)
     */
    public MarcJsonWriter(final OutputStream os, final CharConverter conv, final int jsonFormat) {
        setConverter(conv);
        useJsonFormat = jsonFormat;

        if (useJsonFormat == MARC_JSON) {
            this.setQuoteLabels(false);
        }
    }

    /**
     * Closes the {@link MarcJsonWriter}
     */
    @Override
    public void close() {
        // TODO Auto-generated method stub
    }

    protected String toMarcJson(final Record record) {
        final StringBuffer buf = new StringBuffer();
        buf.append("{");

        indent(buf, "\n    ");

        buf.append(ql + "leader" + ql + ":\"").append(record.getLeader().toString()).append("\",");

        indent(buf, "\n    ");

        buf.append(ql + "controlfield" + ql + ":");

        indent(buf, "\n    ");

        buf.append("[");
        boolean firstField = true;

        for (final ControlField cf : record.getControlFields()) {
            if (!firstField) {
                buf.append(",");
            } else {
                firstField = false;
            }

            if (indent) {
                buf.append("\n        ");
            }

            buf.append("{ " + ql + "tag" + ql + " : \"" + cf.getTag() + "\", " + ql + "data" + ql + " : ")
                    .append("\"" + unicodeEscape(cf.getData()) + "\" }");
        }

        indent(buf, "\n    ");

        buf.append("]");

        indent(buf, "\n    ");

        buf.append("datafield :");

        indent(buf, "\n    ");

        buf.append("[");
        firstField = true;

        for (final DataField df : record.getDataFields()) {
            if (!firstField) {
                buf.append(",");
            } else {
                firstField = false;
            }

            if (indent) {
                buf.append("\n        ");
            }

            buf.append("{");

            if (indent) {
                buf.append("\n            ");
            }

            buf.append(ql + "tag" + ql + " : \"" + df.getTag() + "\", " + ql + "ind" + ql + " : \"" +
                    df.getIndicator1() + df.getIndicator2() + "\",");

            if (indent) {
                buf.append("\n            ");
            }

            buf.append(ql + "subfield" + ql + " :");

            if (indent) {
                buf.append("\n            ");
            }

            buf.append("[");
            boolean firstSubfield = true;

            for (final Subfield sf : df.getSubfields()) {
                if (!firstSubfield) {
                    buf.append(",");
                } else {
                    firstSubfield = false;
                }

                if (indent) {
                    buf.append("\n                ");
                }

                buf.append("{ " + ql + "code" + ql + " : \"" + sf.getCode() + "\", " + ql + "data" + ql + " : \"" +
                        unicodeEscape(sf.getData()) + "\" }");
            }

            if (indent) {
                buf.append("\n            ");
            }

            buf.append("]");

            if (indent) {
                buf.append("\n        ");
            }

            buf.append("}");
        }

        indent(buf, "\n    ");

        buf.append("]");

        indent(buf, "\n");

        buf.append("}\n");

        return (buf.toString());
    }

    private void indent(StringBuffer buf, String indentStr) {
        if (indent) {
            buf.append(indentStr);
        }
        
    }

    protected String toMarcInJson(final Record record) {
        final StringBuffer buf = new StringBuffer();
        buf.append("{");

        indent(buf, "\n    ");;

        buf.append(ql + "leader" + ql + ":\"").append(record.getLeader().toString()).append("\",");

        indent(buf, "\n    ");;

        buf.append(ql + "fields" + ql + ":");

        indent(buf, "\n    ");;

        buf.append("[");
        boolean firstField = true;

        for (final ControlField cf : record.getControlFields()) {
            if (!firstField) {
                buf.append(",");
            } else {
                firstField = false;
            }

            if (indent) {
                buf.append("\n        ");
            }

            buf.append("{");

            if (indent) {
                buf.append("\n            ");
            }

            buf.append(ql + cf.getTag() + ql + ":").append("\"" + unicodeEscape(cf.getData()) + "\"");

            if (indent) {
                buf.append("\n        ");
            }

            buf.append("}");
        }

        for (final DataField df : record.getDataFields()) {
            if (!firstField) {
                buf.append(",");
            } else {
                firstField = false;
            }

            if (indent) {
                buf.append("\n        ");
            }

            buf.append("{");

            if (indent) {
                buf.append("\n            ");
            }

            buf.append(ql + df.getTag() + ql + ":");

            if (indent) {
                buf.append("\n                ");
            }

            buf.append("{");
            // if (indent) buf.append("\n                ");
            buf.append(ql + "subfields" + ql + ":");

            if (indent) {
                buf.append("\n                ");
            }

            buf.append("[");
            boolean firstSubfield = true;

            for (final Subfield sf : df.getSubfields()) {
                if (!firstSubfield) {
                    buf.append(",");
                } else {
                    firstSubfield = false;
                }

                if (indent) {
                    buf.append("\n                    ");
                }

                buf.append("{");

                if (indent) {
                    buf.append("\n                        ");
                }

                buf.append(ql + sf.getCode() + ql + ":\"" + unicodeEscape(sf.getData()) + "\"");

                if (indent) {
                    buf.append("\n                    ");
                }

                buf.append("}");
            }

            if (indent) {
                buf.append("\n                ");
            }

            buf.append("],");

            if (indent) {
                buf.append("\n                ");
            }

            buf.append(ql + "ind1" + ql + ":\"" + df.getIndicator1() + "\",");

            if (indent) {
                buf.append("\n                ");
            }

            buf.append(ql + "ind2" + ql + ":\"" + df.getIndicator2() + "\"");

            if (indent) {
                buf.append("\n            ");
            }

            buf.append("}");

            if (indent) {
                buf.append("\n        ");
            }

            buf.append("}");
        }

        indent(buf, "\n    ");;

        buf.append("]");

        indent(buf, "\n");;

        buf.append("}\n");

        return (buf.toString());
    }

    private String unicodeEscape(String data) {
        if (converter != null) {
            data = converter.convert(data);
        }

        if (normalize) {
            data = Normalizer.normalize(data, Normalizer.Form.NFC);
        }

        final StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < data.length(); i++) {
            final char c = data.charAt(i);
            switch (c) {
                case '/': {
                    if (escapeSlash) {
                        buffer.append("\\/");
                    } else {
                        buffer.append("/");
                    }
                }
                    break;
                case '"':
                    buffer.append("\\\"");
                    break;
                case '\\':
                    buffer.append("\\\\");
                    break;
                case '\b':
                    buffer.append("\\b");
                    break;
                case '\f':
                    buffer.append("\\f");
                    break;
                case '\n':
                    buffer.append("\\n");
                    break;
                case '\r':
                    buffer.append("\\r");
                    break;
                case '\t':
                    buffer.append("\\t");
                    break;
                default: {
                    if (c > 0xff || c < 0x1f) {
                        final String val = "0000" + Integer.toHexString(c);
                        buffer.append("\\u" + val.substring(val.length() - 4, val.length()));
                    } else {
                        buffer.append(c);
                    }

                    break;
                }
            }
        }
        return (buffer.toString());
    }

    /**
     * Returns the character converter.
     *
     * @return the character converter being used
     */
    @Override
    public CharConverter getConverter() {
        return converter;
    }

    /**
     * Sets the character converter.
     *
     * @param converter the character converter
     */
    @Override
    public void setConverter(final CharConverter converter) {
        this.converter = converter;
    }

    /**
     * Returns true if indentation is active, false otherwise.
     *
     * @return Returns true if indentation is active
     */
    public boolean hasIndent() {
        return indent;
    }

    /**
     * Activates or deactivates indentation. Default value is false.
     *
     * @param indent - true to produce pretty-printed indented output
     */
    public void setIndent(final boolean indent) {
        this.indent = indent;
    }

    /**
     * Writes the supplied {@link Record}.
     */
    @Override
    public void write(final Record record) {
        String recordAsJson = "";

        if (useJsonFormat == MARC_IN_JSON) {
            recordAsJson = toMarcInJson(record);
        } else if (useJsonFormat == MARC_JSON) {
            recordAsJson = toMarcJson(record);
        }

        try {
            os.write(recordAsJson.getBytes("UTF-8"));
            os.flush();
        } catch (final UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Returns true if escape slashes are turned on; else, false.
     *
     * @return True if escape slashes are turned on; else, false
     */
    public boolean isEscapeSlash() {
        return escapeSlash;
    }

    /**
     * Turns on escape slashes.
     *
     * @param escapeSlash True if escape slashes should be turned on; else, false
     */
    public void setEscapeSlash(final boolean escapeSlash) {
        this.escapeSlash = escapeSlash;
    }

    /**
     * Returns true if quote labels are turned on; else, false.
     *
     * @return True if quote labels are turned on; else, false
     */
    public boolean isQuoteLabels() {
        return quoteLabels;
    }

    /**
     * Turns on quote labels.
     *
     * @param quoteLabels - true to enable quotes being placed around certain items.
     */
    public void setQuoteLabels(final boolean quoteLabels) {
        this.quoteLabels = quoteLabels;
        ql = (quoteLabels) ? "\"" : "";
    }

    /**
     * Returns true if JSON output is indented; else, false.
     *
     * @return True if JSON output is indented; else, false
     */
    public boolean isIndent() {
        return indent;
    }

    /**
     * Turns on Unicode normalization.
     *
     * @param b - true to turn on Unicode normalization (NFC)
     */
    public void setUnicodeNormalization(final boolean b) {
        this.normalize = b;
    }

}
