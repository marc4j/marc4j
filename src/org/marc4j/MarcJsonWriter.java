package org.marc4j;

import org.marc4j.converter.CharConverter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.util.Normalizer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class MarcJsonWriter implements MarcWriter
{
    public final static int MARC_IN_JSON = 0;
    public final static int MARC_JSON = 1;
    

    /**
     * Character encoding. Default is UTF-8.
     */
//    private String encoding = "UTF8";
    private CharConverter converter = null;
    private OutputStream os = null;
    private int useJsonFormat = MARC_IN_JSON;
    private boolean indent = false;
    private boolean escapeSlash = false;
    private boolean quoteLabels = true;
    private String ql = "\"";
    private boolean normalize = false;
    
    public MarcJsonWriter(OutputStream os)
    {
        this.os = os;
    }
    
    public MarcJsonWriter(OutputStream os, CharConverter conv)
    {
        this.os = os;
        setConverter(conv);
    }
    
    public MarcJsonWriter(OutputStream os, int jsonFormat)
    {
        this.os = os;
        useJsonFormat = jsonFormat;
        if (useJsonFormat == MARC_JSON) this.setQuoteLabels(false);
    }
    
    public MarcJsonWriter(OutputStream os, CharConverter conv, int jsonFormat)
    {
        setConverter(conv);
        useJsonFormat = jsonFormat;
        if (useJsonFormat == MARC_JSON) this.setQuoteLabels(false);
    }
    
    public void close()
    {
        // TODO Auto-generated method stub
        
    }
    
    protected String toMarcJson(Record record)
    {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        if (indent) buf.append("\n    ");
        buf.append(ql + "leader" + ql + ":\"").append(record.getLeader().toString()).append("\",");
        if (indent) buf.append("\n    ");
        buf.append(ql + "controlfield" + ql + ":");
        if (indent) buf.append("\n    ");
        buf.append("[");
        boolean firstField = true;
        for (ControlField cf : record.getControlFields())
        {
            if (!firstField) buf.append(","); 
            else firstField = false;
            if (indent) buf.append("\n        ");
            buf.append("{ " + ql + "tag" + ql + " : \"" + cf.getTag() + "\", " + ql + "data" + ql + " : ").append("\"" + unicodeEscape(cf.getData()) + "\" }");
        }
        if (indent) buf.append("\n    ");
        buf.append("]");
        if (indent) buf.append("\n    ");
        buf.append("datafield :");
        if (indent) buf.append("\n    ");
        buf.append("[");
        firstField = true;
        for (DataField df : record.getDataFields())
        {
            if (!firstField) buf.append(","); 
            else firstField = false;
            if (indent) buf.append("\n        ");
            buf.append("{");
            if (indent) buf.append("\n            ");
            buf.append(ql + "tag" + ql + " : \"" + df.getTag() + "\", " + ql + "ind" + ql + " : \"" + df.getIndicator1() + df.getIndicator2()+ "\",");
            if (indent) buf.append("\n            ");
            buf.append(ql + "subfield" + ql + " :");
            if (indent) buf.append("\n            ");
            buf.append("[");
            boolean firstSubfield = true;
            for (Subfield sf : df.getSubfields())
            {
                if (!firstSubfield) buf.append(","); 
                else firstSubfield = false;
                if (indent) buf.append("\n                ");
                buf.append("{ " + ql + "code" + ql + " : \"" + sf.getCode() + "\", " + ql + "data" + ql + " : \"" + unicodeEscape(sf.getData()) + "\" }");
            }
            if (indent) buf.append("\n            ");
            buf.append("]");
            if (indent) buf.append("\n        ");
            buf.append("}");
        }
        if (indent) buf.append("\n    ");
        buf.append("]");
        if (indent) buf.append("\n");
        buf.append("}\n");
        return(buf.toString());

    }
    
    protected String toMarcInJson(Record record) 
    {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        if (indent) buf.append("\n    ");
        buf.append(ql + "leader" + ql + ":\"").append(record.getLeader().toString()).append("\",");
        if (indent) buf.append("\n    ");
        buf.append(ql + "fields" + ql + ":");
        if (indent) buf.append("\n    ");
        buf.append("[");
        boolean firstField = true;
        for (ControlField cf : record.getControlFields())
        {
            if (!firstField) buf.append(","); 
            else firstField = false;
            if (indent) buf.append("\n        ");
            buf.append("{");
            if (indent) buf.append("\n            ");
            buf.append(ql + cf.getTag() + ql + ":").append("\"" + unicodeEscape(cf.getData()) + "\"");
            if (indent) buf.append("\n        ");
            buf.append("}");
        }
        for (DataField df : record.getDataFields())
        {
            if (!firstField) buf.append(","); 
            else firstField = false;
            if (indent) buf.append("\n        ");
            buf.append("{");
            if (indent) buf.append("\n            ");
            buf.append(ql + df.getTag() + ql + ":");
            if (indent) buf.append("\n                ");
            buf.append("{");
        //    if (indent) buf.append("\n                ");
            buf.append(ql + "subfields" + ql + ":");
            if (indent) buf.append("\n                ");
            buf.append("[");
            boolean firstSubfield = true;
            for (Subfield sf : df.getSubfields())
            {
                if (!firstSubfield)  buf.append(","); 
                else firstSubfield = false;
                if (indent) buf.append("\n                    ");
                buf.append("{");
                if (indent) buf.append("\n                        ");
                buf.append(ql + sf.getCode() + ql + ":\"" + unicodeEscape(sf.getData()) + "\"");
                if (indent) buf.append("\n                    ");
                buf.append("}");
            }
            if (indent) buf.append("\n                ");
            buf.append("],");
            if (indent) buf.append("\n                ");
            buf.append(ql + "ind1" + ql + ":\"" + df.getIndicator1() + "\",");
            if (indent) buf.append("\n                ");
            buf.append(ql + "ind2" + ql + ":\"" + df.getIndicator2() + "\"");
            if (indent) buf.append("\n            ");
            buf.append("}");
            if (indent) buf.append("\n        ");
            buf.append("}");
        }
        if (indent) buf.append("\n    ");
        buf.append("]");
        if (indent) buf.append("\n");
        buf.append("}\n");
        return(buf.toString());
    }


    private String unicodeEscape(String data)
    {
        if (converter != null)
            data = converter.convert(data);
        if (normalize)
            data = Normalizer.normalize(data, Normalizer.NFC);
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < data.length(); i++)
        {
            char c = data.charAt(i);
            switch (c)
            {
                case '/': 
                {
                    if (escapeSlash) buffer.append("\\/"); 
                    else             buffer.append("/"); 
                }
                break;
                case '"': buffer.append("\\\""); break;
                case '\\': buffer.append("\\\\"); break;
                case '\b': buffer.append("\\b"); break;
                case '\f': buffer.append("\\f"); break;
                case '\n': buffer.append("\\n"); break;
                case '\r': buffer.append("\\r"); break;
                case '\t': buffer.append("\\t"); break;
                default: 
                {
                    if ((int) c > 0xff || (int) c < 0x1f)
                    {
                        String val = "0000"+Integer.toHexString((int)(c));
                        buffer.append("\\u"+ (val.substring(val.length()-4, val.length())) );
                    }
                    else buffer.append(c); break;
                }
            }
        }
        return(buffer.toString());
    }

    /**
     * Returns the character converter.
     * 
     * @return CharConverter the character converter
     */
    public CharConverter getConverter() 
    {
        return converter;
    }

    /**
     * Sets the character converter.
     * 
     * @param converter - the character converter
     */
    public void setConverter(CharConverter converter) 
    {
        this.converter = converter;
    }

    /**
     * Returns true if indentation is active, false otherwise.
     * 
     * @return boolean
     */
    public boolean hasIndent() 
    {
        return indent;
    }

    /**
     * Activates or deactivates indentation. Default value is false.
     * 
     * @param indent - true to enable pretty-print indentation 
     */
    public void setIndent(boolean indent) 
    {
        this.indent = indent;
    }



    public void write(Record record)
    {
        String recordAsJson = "";
        if (useJsonFormat == MARC_IN_JSON)
        {
            recordAsJson = toMarcInJson(record);
        }
        else if (useJsonFormat == MARC_JSON)
        {
            recordAsJson = toMarcJson(record);
        }
        try
        {
            os.write(recordAsJson.getBytes("UTF-8"));
            os.flush();
        }
        catch (UnsupportedEncodingException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean isEscapeSlash()
    {
        return escapeSlash;
    }

    public void setEscapeSlash(boolean escapeSlash)
    {
        this.escapeSlash = escapeSlash;
    }

    public boolean isQuoteLabels()
    {
        return quoteLabels;
    }

    public void setQuoteLabels(boolean quoteLabels)
    {
        this.quoteLabels = quoteLabels;
        ql = (quoteLabels) ? "\"" : "";
    }

    public boolean isIndent()
    {
        return indent;
    }

    public void setUnicodeNormalization(boolean b)
    {
        this.normalize = b;
    }


}
