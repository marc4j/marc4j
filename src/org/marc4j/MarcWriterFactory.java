package org.marc4j;

import java.io.OutputStream;

import org.marc4j.converter.impl.UnicodeToAnsel;

public class MarcWriterFactory
{

//  static protected MarcWriter makeWriterFromConvertParm(String convert, boolean pretty, boolean normalize, boolean[] mustBeUtf8, boolean[] marc8Flag, boolean allowOversize, OutputStream out) {

    public static MarcWriter makeWriterFromConvertString(String convertString, OutputStream out)
    {
        String convert = "text";
        boolean pretty = false;
        boolean normalize = false;
        boolean oversize = false;
        int splitAt = 0;
        
        String[] convertParts = convertString.split("[;:_, ]+");
        for (String part : convertParts)
        {
            if (part.equalsIgnoreCase("text") || part.equalsIgnoreCase("ASCII"))             convert = "text";
            else if (part.equalsIgnoreCase("errors"))                                        convert = "errors";
            else if (part.equalsIgnoreCase("XML") || part.equalsIgnoreCase("MARCXML"))       convert = "xml";
            else if (part.equalsIgnoreCase("MARC_IN_JSON") || part.equalsIgnoreCase("json")) convert = "json";
            else if (part.equalsIgnoreCase("MARC_JSON") || part.equalsIgnoreCase("json2"))   convert = "json2";
            else if (part.equalsIgnoreCase("UTF8") || part.equalsIgnoreCase("UTF-8"))        convert = "utf8";
            else if (part.equalsIgnoreCase("RAW") || part.equalsIgnoreCase("raw"))           convert = "raw";
            else if (part.equalsIgnoreCase("MARC8"))                                         convert = "marc8";
            else if (part.equalsIgnoreCase("MARC8NCR") || part.equalsIgnoreCase("NCR"))      convert = "marc8ncr"; 
            else if (part.equalsIgnoreCase("MRK8") || part.equalsIgnoreCase("MARCEDIT"))     convert = "mrk8";
            else if (part.matches("([0-9][0-9][0-9]|err)(:([0-9][0-9][0-9]|err))*"))         convert = part;

            else if (part.equalsIgnoreCase("pretty") || part.equalsIgnoreCase("indent") )           pretty = true;
            else if (part.equalsIgnoreCase("normalize") || part.equalsIgnoreCase("normalized") )    normalize = true;
            else if (part.startsWith("split")) 
            {
                splitAt = 70000;
            }
            else if (part.equalsIgnoreCase("oversize")) 
            {
                oversize = true;
            }
        }
        return  makeWriterFromConvertParm(convert, pretty, normalize, oversize, splitAt, out );
    }
    
    public static MarcWriter makeWriterFromConvertParm(String convert, boolean pretty, boolean normalize, boolean oversize, int splitAt, OutputStream out) 
    {
        MarcWriter writer = null;
        
        if (convert.equalsIgnoreCase("text") || convert.equalsIgnoreCase("ASCII")) 
        {
            writer = new MarcTxtWriter(out);
        } 
        else if (convert.equalsIgnoreCase("errors")) 
        {
            writer = new MarcTxtWriter(out, "001;err");
        }
        else if (convert.matches("([0-9][0-9][0-9]|err)(:([0-9][0-9][0-9]|err))*")) 
        {
            writer = new MarcTxtWriter(out, convert);
        } 
        else if (convert.equalsIgnoreCase("XML") || convert.equalsIgnoreCase("MARCXML")) 
        {
            MarcXmlWriter xmlwriter = new MarcXmlWriter(out, "UTF8");
            if (pretty)     xmlwriter.setIndent(true);
            if (normalize)  xmlwriter.setUnicodeNormalization(true);
            writer = xmlwriter;
        } 
        else if (convert.equalsIgnoreCase("MARC_IN_JSON") || convert.equalsIgnoreCase("json")) 
        {
            MarcJsonWriter jsonwriter = new MarcJsonWriter(out, MarcJsonWriter.MARC_IN_JSON);
            if (pretty)     jsonwriter.setIndent(true);
            if (normalize)  jsonwriter.setUnicodeNormalization(true);
            writer = jsonwriter;
        } 
        else if (convert.equalsIgnoreCase("MARC_JSON") || convert.equalsIgnoreCase("json2")) 
        {
            MarcJsonWriter jsonwriter = new MarcJsonWriter(out, MarcJsonWriter.MARC_JSON);
            if (pretty)     jsonwriter.setIndent(true);
            if (normalize)  jsonwriter.setUnicodeNormalization(true);
            writer = jsonwriter;
        } 
        else if (convert.equalsIgnoreCase("UTF8") || convert.equalsIgnoreCase("UTF-8")) 
        {
            MarcStreamWriter binwriter;
            if (splitAt == 0)
            {
                binwriter = new MarcStreamWriter(out, "UTF8", true);
                binwriter.setAllowOversizeEntry(oversize);
            }
            else
            {
                binwriter = new MarcSplitStreamWriter(out, splitAt, "UTF8");
            }
            writer = binwriter;
        } 
        else if (convert.equalsIgnoreCase("raw") ) 
        {
            MarcStreamWriter binwriter;
            if (splitAt == 0)
            {
                binwriter = new MarcStreamWriter(out, "per_record", true);
                binwriter.setAllowOversizeEntry(oversize);
            }
            else
            {
                binwriter = new MarcSplitStreamWriter(out, splitAt, "UTF8");
            }
            writer = binwriter;
        } 
        else if (convert.equalsIgnoreCase("MARC8") || convert.equalsIgnoreCase("MARC8NCR") || convert.equalsIgnoreCase("NCR")) 
        {
            MarcStreamWriter binwriter;
            if (splitAt == 0)
            {
                binwriter = new MarcStreamWriter(out, "ISO8859_1", true);
                binwriter.setAllowOversizeEntry(oversize);
            }
            else
            {
                binwriter = new MarcSplitStreamWriter(out, splitAt, "ISO8859_1");
            }
            binwriter.setConverter(new UnicodeToAnsel(convert.contains("NCR")));
            writer = binwriter;
        } 
        else if (convert.equalsIgnoreCase("MRK8")) 
        {
            Mrk8StreamWriter mrkwriter = new Mrk8StreamWriter(out);
            writer = mrkwriter;
        } 
        else 
        {
            throw new IllegalArgumentException("Error : Unknown output format: "+ convert );
        }
        return(writer);
    }

//    public static MarcWriter makeWriterFromConvertParm(String convert, boolean pretty, boolean normalize, boolean[] mustBeUtf8, boolean[] marc8Flag, boolean allowOversize, OutputStream out) {
//        MarcWriter writer = null;
//        
//        if (convert.equalsIgnoreCase("text") || convert.equalsIgnoreCase("ASCII")) {
//            writer = new MarcTxtWriter(out);
//        } else if (convert.equalsIgnoreCase("errors")) {
//            writer = new MarcTxtWriter(out, "001;err");
//        } else if (convert.matches("([0-9][0-9][0-9]|err)(:([0-9][0-9][0-9]|err))*")) {
//            writer = new MarcTxtWriter(out, convert);
//        } else if (convert.equalsIgnoreCase("XML") || convert.equalsIgnoreCase("MARCXML")) {
//            mustBeUtf8[0] = true;
//            MarcXmlWriter xmlwriter = new MarcXmlWriter(out, "UTF8");
//            if (pretty)     xmlwriter.setIndent(true);
//            if (normalize)  xmlwriter.setUnicodeNormalization(true);
//            writer = xmlwriter;
//        } else if (convert.equalsIgnoreCase("MARC_IN_JSON") || convert.equalsIgnoreCase("json")) {
//            mustBeUtf8[0] = true;
//            MarcJsonWriter jsonwriter = new MarcJsonWriter(out, MarcJsonWriter.MARC_IN_JSON);
//            if (pretty)     jsonwriter.setIndent(true);
//            if (normalize)  jsonwriter.setUnicodeNormalization(true);
//            writer = jsonwriter;
//        } else if (convert.equalsIgnoreCase("MARC_JSON") || convert.equalsIgnoreCase("json2")) {
//            mustBeUtf8[0] = true;
//            MarcJsonWriter jsonwriter = new MarcJsonWriter(out, MarcJsonWriter.MARC_JSON);
//            if (pretty)     jsonwriter.setIndent(true);
//            if (normalize)  jsonwriter.setUnicodeNormalization(true);
//            writer = jsonwriter;
//        } else if (convert.equalsIgnoreCase("UTF8") || convert.equalsIgnoreCase("UTF-8")) {
//            mustBeUtf8[0] = true;
//            MarcStreamWriter binwriter = new MarcStreamWriter(out, "UTF8");
//            if (allowOversize) {
//                binwriter.setAllowOversizeEntry(true);
//            }
//            writer = binwriter;
//        } else if (convert.equalsIgnoreCase("MARC8")) {
//            MarcStreamWriter binwriter = new MarcStreamWriter(out, "ISO8859_1", true);
//            binwriter.setConverter(new UnicodeToAnsel());
//            marc8Flag[0] = true;
//            if (allowOversize) {
//                binwriter.setAllowOversizeEntry(true);
//            }
//            writer = binwriter;
//        } else if (convert.equalsIgnoreCase("MARC8NCR") || convert.equalsIgnoreCase("NCR")) {
//            MarcStreamWriter binwriter = new MarcStreamWriter(out, "ISO8859_1", true);
//            binwriter.setConverter(new UnicodeToAnsel(true));
//            marc8Flag[0] = true;
//            if (allowOversize) {
//                binwriter.setAllowOversizeEntry(true);
//            }
//            writer = binwriter;
//        } else if (convert.equalsIgnoreCase("MRK8")) {
//            mustBeUtf8[0] = true;
//            Mrk8StreamWriter mrkwriter = new Mrk8StreamWriter(out);
//            writer = mrkwriter;
//        } else {
//            throw new IllegalArgumentException("Error : Unknown output format: "+ convert );
////            System.exit(1);
//        }
//        return(writer);
//    }

}
