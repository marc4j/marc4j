package org.marc4j.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.marc4j.MarcReader;
import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.marc.Record;

/**
 * @author tod
 *
 * Provides a basic utility for splitting file of MARC records.
 */
public class SplitFile {

    /**
     * Command line interface for splitting records.
     * 
     * @param args - the command-line arguments
     */
    public static void main(String[] args) {
        final long start = System.currentTimeMillis();

        String input = null;
        InputStream inStream = null;
        int count = 100;
        String outBase = null;
        
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-help") || args[i].equals("-usage")) {
                usage(0);
            } else if (args[i].equals("-count")) {
                if (i == args.length - 1) {
                    usage(1);
                }
                try {
                    count = Integer.parseInt(args[++i].trim());
                } catch (java.lang.NumberFormatException e) {
                    usage(1);
                }
            } else if (args[i].equals("-out")) {
                if (i == args.length - 1) {
                    usage(1);
                }
                outBase = args[++i].trim();
            } else {
                input = args[i].trim();

                // Must be last arg
                if (i != args.length - 1) {
                    usage(1);
                }
            }
        }
        if (input == null) {
            inStream = System.in;
        } else {
            // Use input filename to get output basename, if not specified with -out
            if (outBase == null) {
                int idx = input.lastIndexOf('.');
                if (idx > 0) { // Avoid problem case where input filename begins with '.'
                    outBase = input.substring(0, idx);
                }
            }
            try {
                inStream = new FileInputStream(input);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        MarcReader reader = null;
        try {
            reader = new MarcPermissiveStreamReader(inStream, true, true);
            int fcount = 0;
            while (reader.hasNext()) {
                fcount++;
                String outName = String.format("%s-%02d%s", outBase, fcount, ".mrc");
                OutputStream outStream = new FileOutputStream(outName);
                MarcWriter writer = new MarcStreamWriter(outStream, true);
                // System.out.println(String.format("Opened file %s\n", outName));
                for (int i=0; i<count && reader.hasNext(); i++) {
                    Record record = reader.next();
                    writer.write(record);
                }
                writer.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        System.err.println("Total time: " + (System.currentTimeMillis() - start) + " miliseconds");
    }
    
    private static void usage(int status) {
        System.err.println("Usage: org.marc4j.util.SplitFile [-options] <file.mrc>");
        System.err.println("\t-help: print this message");
        System.err.println("\t-count <num>: max number of records in an output file (default: 100)");
        System.err.println("\t-out: basename for output files (default: ouput)");
        System.err.println("\t-usage: print usage info (same as -help)");
        System.exit(status);
    }

}
