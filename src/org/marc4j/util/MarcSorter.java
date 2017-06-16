package org.marc4j.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;

/**
 * Read a file of binary MARC records, sort them by their control numbers, write them out to stdout
 * @author Robert Haschart
 *
 */
public class MarcSorter
{
    static TreeMap<String, byte[]> recordMap = null;
    static boolean verbose = false;
    static boolean check = false;
    static boolean quiet = false;
    static StringNaturalCompare compare = null;

    /**
	 * main program for sorting class.
	 * @param args - the provided command line arguments
	 */
    public static void main(String[] args)
    {
    //    try {
        InputStream input;
        compare = new StringNaturalCompare();
        recordMap = new TreeMap<String, byte[]>(compare);
        int offset = 0;
        while (args[offset].startsWith("-") && args[offset].length() > 1) {
            if (args[offset].equals("-v")) { quiet = false; verbose = true; offset++; }
            if (args[offset].equals("-c")) { check = true; offset++; }
            if (args[offset].equals("-q")) { verbose = false; quiet = true; offset++; }
            if (args[offset].equals("-usage")) { usage(null, 0); }
        }
        String filename = args[offset];

        try {
            if (filename.equals("-")) {
                input = System.in;
                if (verbose)  System.err.println("reading Stdin");
            }
            else {
                input = new FileInputStream(new File(filename));
                if (verbose)  System.err.println("reading file "+ filename);
            }
            processInput(input);
        }
        catch (FileNotFoundException e) {
            System.err.println("Error: Cannot find file: " + filename);
        }
        catch (Exception e){
            System.err.println("Exception: "+e.getMessage());
            e.printStackTrace();
        }

    }

    private static void usage(String error, int exitcode) {
        if (error != null) {
            System.err.println("Error: "+ error);
        }
        System.err.println("Usage: org.marc4j.util.MarcSorter  [-options] <file1.mrc> ");
        System.err.println("       -v      Write verbose output");
        System.err.println("       -c      Merely check whether a file is sorted or not");
        System.err.println("       -q      In conjunction with -c check whether the file is sorted, but don't generate any output, just set the return code (0 = sorted) (-1 = not sorted)");
        System.err.println("       -usage = this message");
        System.exit(exitcode);
    }

    static void processInput(InputStream input) {
        RawRecordReader rawReader = new RawRecordReader(input);
        RawRecord rec = rawReader.hasNext() ? rawReader.next() : null;
        String prevField001 = "";
        int rec_count = 0;
        while (rec != null) {
            String field001 = "Undefined";
            field001 = rec.getRecordId();
            byte newRec[] = rec.getRecordBytes();
            if (check) {
                if (prevField001 != "" && compare.compare(prevField001, field001) > 0) {
                    if (!quiet) {
                        System.err.println("ERROR: File not sorted: record "+rec_count + " has id="+ prevField001+ " the following record has id="+field001);
                    }
                    System.exit(-1);
                }
                rec_count++;
                prevField001 = field001;
                rec = rawReader.hasNext() ? rawReader.next() : null;
            }
            else {
                if (recordMap.containsKey(field001)) {
                    byte existingRec[] = recordMap.get(field001);
                    byte combinedRec[] = new byte[existingRec.length + newRec.length];
                    System.arraycopy(existingRec, 0, combinedRec, 0, existingRec.length);
                    System.arraycopy(newRec, 0, combinedRec, existingRec.length, newRec.length);
                    recordMap.put(field001, combinedRec);
                }
                else {
                    recordMap.put(field001, newRec);
                }
                if (verbose) System.err.println("Record read : "+ field001);
                rec = rawReader.hasNext() ? rawReader.next() : null;
            }
        }
        if (check) {
            if (verbose) {
                System.err.println("File correctly sorted");
            }
            System.exit(0);
        }
        try {
            while (recordMap.size() > 0) {
                String firstKey = recordMap.firstKey();
                byte recValue[] = recordMap.remove(firstKey);
                System.out.write(recValue);
                System.out.flush();
                if (!quiet) System.err.println("Record written : "+ firstKey);
            }
        }
        catch (IOException e) {
            //  e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }
}
