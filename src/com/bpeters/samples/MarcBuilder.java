package com.bpeters.samples;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

import com.bpeters.marc4j.helpers.DefaultHandler;
import com.bpeters.marc4j.helpers.ErrorHandlerImpl;
import com.bpeters.marc4j.MarcReader;
import com.bpeters.marc4j.MarcReaderException;
import com.bpeters.marc.*;

/**
 * <p><code>MarcBuilder</code> demonstrates both the use of the MARC event
 * model by using the {@link DefaultHandler}
 * and the {@link Record} object model and shows how
 * <code>Record</code> objects can be serialized to MARC tape format.</p>
 *
 * @author Bas Peters
 */
public class MarcBuilder extends DefaultHandler {

    protected static PrintWriter pw;

    /** Record object */
    protected static Record record;

    /** Data field object */
    protected static DataField datafield;

    public void startRecord(Leader leader) {
        // create an new instance of a Record object
	    this.record = new Record();
	    // register the Leader object
	    record.setLeader(leader);
    }

    public void controlField(String tag, char[] data) {
	    // add a new control field to the record object
	    record.add(new ControlField(tag, data));
    }

    public void startDataField(String tag, char ind1, char ind2) {
	    // create a new data field
	    datafield = new DataField(tag, ind1, ind2);
    }

    public void subfield(char identifier, char[] data) {
	    // register a new data element to the current data field
	    datafield.add(new Subfield(identifier, data));
    }

    public void endDataField(String tag) {
	    // add a new data field to the record object
	    record.add(datafield);
    }

    public void endRecord() {
        // serialize the Record object to tape format
        // and write tape format record to standard output
	    try {
	        pw.print(record.marshal());
	    } catch (MarcException e) {
	        e.printStackTrace();
	    }
    }

    public static void print(String infile, String outfile) {

	    try {
	        FileWriter fw = new FileWriter(new File(outfile));
            pw = new PrintWriter(fw);

	        MarcBuilder mb = new MarcBuilder();

	        // Create a new MarcReader instance.
	        MarcReader marcReader = new MarcReader();

	        // Register the MarcHandler implementation.
	        marcReader.setMarcHandler(mb);

	        // Register the MarcHandler implementation.
	        marcReader.setErrorHandler(new ErrorHandlerImpl());

	        // Send the file to the parse method.
            marcReader.parse(infile);

	        fw.close();

	    } catch (IOException e) {
	        e.printStackTrace();
	    }
    }

    /**
     * <p>Provides a static entry point for <code>MarcBuilder</code> to
     * read a file with MARC records, build a {@link com.bpeters.marc.Record}
     * object for each record and serialize each record back to MARC tape
     * format.  </p>
     *
     * @param args[] the command-line arguments
     * <ul>
     * <li>First argument: name of the input-file
     * <li>Second argument: name of the output-file
     * </ul>
     */
    public static void main(String args[]) {
	    if (args.length < 2) {
	        System.out.println("Usage: MarcBuilder input-file output-file");
	        return;
	    }
	    print(args[0], args[1]);
    }

}
