package org.marc4j.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.marc4j.MarcJsonWriter;
import org.marc4j.MarcReader;
import org.marc4j.MarcReaderConfig;
import org.marc4j.MarcReaderFactory;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcTxtWriter;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.Mrk8StreamWriter;
import org.marc4j.converter.impl.UnicodeToAnsel;
import org.marc4j.marc.Record;

public class UtilDriver
{
    /**
     * Provides a static entry point.
     * <p>
     * Arguments:
     * </p>
     * <ul>
     * <li>-out &lt;output file&gt; - write to output file</li>
     * <li>-convert &lt;encoding&gt; - convert &lt;encoding&gt; to UTF-8
     * (Supported encodings: MARC8, ISO5426, ISO6937)</li>
     * <li>-encode &lt;encoding&gt; - read data using encoding &lt;encoding&gt;</li>
     * <li>-normalize - perform Unicode normalization</li>
     * <li>-usage - show usage</li>
     * <li>&lt;input file&gt; - input file with MARC records
     * </ul>
     *
     * @param args - the command-line arguments
     */
    public static String[][] commands = { {"marcdiff", "org.marc4j.util.MarcDiff" },
                                          {"getrecord", "org.marc4j.util.RawRecordReader" },
                                          {"getids", "org.marc4j.util.RawRecordReader", "-id" },
                                          {"marcsort", "org.marc4j.util.MarcSorter" },
                                          {"marcupdate", "org.marc4j.util.MarcMerger" },
                                          {"printrecord", "org.marc4j.util.RecordIODriver", "-convert", "text"},
                                          {"to_xml", "org.marc4j.util.RecordIODriver", "-convert", "xml"},
                                          {"to_utf8", "org.marc4j.util.RecordIODriver", "-convert", "utf8"},
                                          {"marcbreaker", "org.marc4j.util.RecordIODriver", "-convert", "mrk8"},
                                          {"to_marc8", "org.marc4j.util.RecordIODriver", "-convert", "marc8"}};
    
    public static void main(final String args[]) {

        if (args.length == 0) {
            usage();
        }
        String initialArg = args[0]; 
        for (String[] command : commands) {
            if (command[0].equals(initialArg)) {
                invokeCommand(command, args);
                System.exit(0);
            }
        }
        usage();
    }

    private static void invokeCommand(String[] command, String[] args) {
        String mainProgram = command[1];
        try {
            Class<?> clazz = Class.forName(mainProgram);
            List<String> newArgs = new ArrayList<String>();
            for (int defArg = 2; defArg < command.length; defArg++) {
                newArgs.add(command[defArg]);
            }
            for (int n = 1; n < args.length; n++) {
                newArgs.add(args[n]);
            }
            Method mainMethod = clazz.getMethod("main", String[].class);
            String argsArr[] = newArgs.toArray(new String[0]);
            mainMethod.invoke(null, (Object)argsArr);
        }
        catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (NoSuchMethodException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (SecurityException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalArgumentException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    private static void usage() {
        System.err.println("Usage: java -jar marc4j.jar <command> <arguments> ");
        System.err.println("       Valid commands are:");
        System.err.println("          getrecord - extract record(s) from a file of binary MARC records");
        System.err.println("          getids - extract only the ids from a file of binary MARC records");
        System.err.println("          marcdiff - compare to files of MARC records showing the differences");
        System.err.println("          marcsort - sort a file of binary MARC records based on the control numbers");
        System.err.println("          marcupdate - merge a set of changes (add, edits and deletes) into a file of binary MARC records");
        System.err.println("          printrecord - read records and print them in a human readable form");
        System.err.println("          to_xml - convert records into MARCXML");
        System.err.println("          to_utf8 - convert records into binary MARC records using the UTF8 character encoding");
        System.err.println("          to_marc8 - convert records into binary MARC records using the MARC8 character encoding");
        System.err.println("          marcbreaker - convert records into MarcEdit ASCII encoding (using the UTF8 character encoding)");
        System.exit(0);
    }


}
