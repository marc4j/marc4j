package org.marc4j.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class UtilDriver
{
    public static String[][] commands = { {"marcdiff", "org.marc4j.util.MarcDiff" },
                                          {"getrecord", "org.marc4j.util.RawRecordReader" },
                                          {"getids", "org.marc4j.util.RawRecordReader", "-id" },
                                          {"marcsort", "org.marc4j.util.MarcSorter" },
                                          {"marcupdate", "org.marc4j.util.MarcMerger" },
                                          {"printrecord", "org.marc4j.util.RecordIODriver", "-convert", "text"},
                                          {"to_xml", "org.marc4j.util.RecordIODriver", "-convert", "xml"},
                                          {"to_utf8", "org.marc4j.util.RecordIODriver", "-convert", "utf8"},
                                          {"marcbreaker", "org.marc4j.util.RecordIODriver", "-convert", "mrk8"},
                                          {"to_marc8", "org.marc4j.util.RecordIODriver", "-convert", "marc8"},
                                          {"marcsplit", "org.marc4j.util.SplitFile" },
                                          {"marcpatcher", "org.marc4j.util.MarcPatcher" },
                                          {"mergesummary", "org.marc4j.util.MergeSummaryHoldings" }};
    
    /**
     * Provides a single entry point for starting one of several command line utilities 
     * <p>
     * Such as:
     * <ul>
     * <li>marcdiff     -   org.marc4j.util.MarcDiff </li>
     * <li>getrecord    -   org.marc4j.util.RawRecordReader </li>
     * <li>getids       -   org.marc4j.util.RawRecordReader -id </li>
     * <li>marcsort     -   org.marc4j.util.MarcSorter </li>
     * <li>marcupdate   -   org.marc4j.util.MarcMerger </li>
     * <li>printrecord  -   org.marc4j.util.RecordIODriver -convert text</li>
     * <li>to_xml       -   org.marc4j.util.RecordIODriver -convert xml </li>
     * <li>to_utf8      -   org.marc4j.util.RecordIODriver -convert utf8 </li>
     * <li>marcbreaker  -   org.marc4j.util.RecordIODriver -convert mrk8 </li>
     * <li>to_marc8     -   org.marc4j.util.RecordIODriver -convert marc8 </li>
     * <li>marcsplit    -   org.marc4j.util.SplitFile </li>
     * <li>marcpatcher  -   org.marc4j.util.MarcPatcher </li>
     * <li>mergesummary -   org.marc4j.util.MergeSummaryHoldings </li>
     * </ul>
     *
     * @param args - the command-line arguments
     */
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
        System.err.println("          marcsplit - split a file of binary MARC records into smaller chunks");
        System.err.println("          marcpatcher - patch the location fields in a MARC record");
        System.err.println("          marcsummary - merge summary holdings into the corresponding MARC record");
        System.err.println("");
        System.err.println("Note: the arguments accepted by many of the above utilities are different.");
        System.err.println("      For most of them passing an argument of -help or -usage will describe the arguments that utility accepts.");
        System.exit(0);
    }


}
