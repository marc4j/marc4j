package org.marc4j;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.marc4j.converter.CharConverter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

public class MarcTxtWriter implements MarcWriter {
    /**
     * Character encoding. Default is UTF-8.
     */
    private String indexkeyprefix = null;

    private PrintWriter out = null;

    private CharConverter conv = null;

    public MarcTxtWriter(OutputStream os) {
        try {
            this.out = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), true);            
        }
        catch (java.lang.NoClassDefFoundError ncdf) {
            try
            {
                this.out = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true);
            }
            catch (UnsupportedEncodingException e)
            {
            }
        }
    }

    public MarcTxtWriter(OutputStream os, String indexkeyprefix) {
        try {
            this.out = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), true);            
        }
        catch (java.lang.NoClassDefFoundError ncdf) {
            try
            {
                this.out = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true);
            }
            catch (UnsupportedEncodingException e)
            {
            }
        }
        this.indexkeyprefix = indexkeyprefix;
    }

    @Override
    public void write(Record record) {
        String recStr;

        if (conv != null) {
            recStr = applyConverter(record, conv);
        }
        else {
            recStr = record.toString();
        }
        if (indexkeyprefix != null) {
            String lines[] = recStr.split("\r?\n");
            for (String line : lines) {
                if (line.length() >= 3 && indexkeyprefix.contains(line.substring(0,3))) {
                    out.println(line);
                }
            }
            if (indexkeyprefix.contains("err") && record.getErrors() != null) {
                for (MarcError err : record.getErrors()) {
                    out.println(err.toString());
                }
            }
        }
        else {
            out.println(recStr);
        }
    }

    private String applyConverter(Record record, CharConverter conv) {
        final StringBuilder sb = new StringBuilder();
        sb.append("LEADER ");
        sb.append(record.getLeader().toString());
        sb.append('\n');
    
        for (final VariableField field : record.getVariableFields()) {
            if (field instanceof ControlField) {
                sb.append(field.toString());
            }
            else if (field instanceof DataField) {
                DataField df = (DataField)field;
                sb.append(df.getTag());
                sb.append(' ');
                sb.append(df.getIndicator1());
                sb.append(df.getIndicator2());

                for (final Subfield sf : df.getSubfields()) {
                    sb.append("$").append(sf.getCode()).append(conv.convert(sf.getData()));
                }
            }
            sb.append('\n');
        }
        return sb.toString();
    }


    @Override
    public void setConverter(CharConverter converter) { 
        conv = converter;
    }

    @Override
    public CharConverter getConverter() {
        return conv;
    }

    @Override
    public void close() {
        this.out.flush();
        this.out.close();
    }
}