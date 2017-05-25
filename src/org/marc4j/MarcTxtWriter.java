package org.marc4j;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import org.marc4j.converter.CharConverter;
import org.marc4j.marc.Record;

public class MarcTxtWriter implements MarcWriter {
    /**
     * Character encoding. Default is UTF-8.
     */
    private String indexkeyprefix = null;

    private PrintWriter out = null;

    public MarcTxtWriter(OutputStream os) {
        this.out = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), true);
    }

    public MarcTxtWriter(OutputStream os, String indexkeyprefix) {
        this.out = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), true);
        this.indexkeyprefix = indexkeyprefix;
    }

    @Override
    public void write(Record record) {
        String recStr = record.toString();
        if (indexkeyprefix != null) {
            String lines[] = recStr.split("\r?\n");
            for (String line : lines) {
                if (line.substring(0,3).matches(indexkeyprefix)) {
                    out.println(line);
                }
            }
        }
        else {
            out.println(recStr);
        }
    }

    @Override
    public void setConverter(CharConverter converter) {        
    }

    @Override
    public CharConverter getConverter() {
        return null;
    }

    @Override
    public void close() {
        this.out.flush();
        this.out.close();
    }
}