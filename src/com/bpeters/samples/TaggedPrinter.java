package com.bpeters.samples;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

import com.bpeters.marc4j.MarcReader;
import com.bpeters.marc4j.MarcReaderException;
import com.bpeters.marc4j.helpers.DefaultHandler;
import com.bpeters.marc4j.helpers.ErrorHandlerImpl;
import com.bpeters.marc.Leader;

/**
 * <p><code>TaggedPrinter</code> demonstrates the use of the
 * {@link DefaultHandler} interface and converts MARC records to tagged
 * display format.  </p>
 *
 * @author Bas Peters
 */
public class TaggedPrinter extends DefaultHandler {

    protected static PrintWriter pw;

    public void startRecord(Leader leader) {
	    pw.println("Leader " + leader.marshal());
    }

    public void controlField(String tag, char[] data) {
	    pw.println(tag + " " + new String(data));
    }

    public void startDataField(String tag, char ind1, char ind2) {
	    pw.print(tag + " " + ind1 + ind2);
    }

    public void subfield(char identifier, char[] data) {
	    // the dollar sign is used as a character
	    // representation for delimiter
	    pw.print("$" + identifier + new String(data));
    }

    public void endDataField(String tag) {
	    pw.println();
    }

    public void endRecord() {
	    pw.println();
    }

    public static void print(String infile, String outfile) {
        try {
            FileWriter fw = new FileWriter(new File(outfile));
                pw = new PrintWriter(fw);

            TaggedPrinter tp = new TaggedPrinter();

            // Create a new MarcReader instance.
            MarcReader marcReader = new MarcReader();

            // Register the MarcHandler implementation.
            marcReader.setMarcHandler(tp);

            // Register the ErrorHandler implementation.
            marcReader.setErrorHandler(new ErrorHandlerImpl());

            // Send the file to the parse method.
                marcReader.parse(infile);

            fw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>Provides a static entry point for <code>TaggedPrinter</code> to
     * convert a file with MARC records to tagged display format.  </p>
     *
     * @param args[] the command-line arguments
     * <ul>
     * <li>First argument: name of the input-file
     * <li>Second argument: name of the output-file
     * </ul>
     */
    public static void main(String args[]) {
        if (args.length < 2) {
            System.out.println("Usage: TaggedPrinter input-file output-file");
            return;
        }
        print(args[0], args[1]);
    }

}
