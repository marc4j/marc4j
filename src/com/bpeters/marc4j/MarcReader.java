package com.bpeters.marc4j;

import java.io.Reader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ResourceBundle;
import java.text.MessageFormat;
import com.bpeters.marc.MarcConstants;
import com.bpeters.marc.Tag;
import com.bpeters.marc.Leader;


/**
 * <p>Parses MARC records and reports events to the {@link MarcHandler}
 * and optionally the {@link ErrorHandler}.  </p>
 *
 * @author Bas Peters
 */
public class MarcReader {

    protected static final char RT = MarcConstants.RT;
    protected static final char FT = MarcConstants.FT;
    protected static final char US = MarcConstants.US;
    protected static final char BLANK = MarcConstants.BLANK;

    int fileCounter = 0;
    int recordCounter = 0;

    /** The MarcHandler object. */
    protected MarcHandler mh;

    /** The ErrorHandler object. */
    protected ErrorHandler eh;

    /**
     * <p>Registers the <code>MarcHandler</code> implementation.</p>
     *
     * @param marcHandler the {@link MarcHandler} implementation
     */
    public void setMarcHandler(MarcHandler mh) {
	this.mh = mh;
    }

     /**
     * <p>Registers the <code>ErrorHandler</code> implementation.</p>
     *
     * @param errorHandler the {@link ErrorHandler} implementation
     */
    public void setErrorHandler(ErrorHandler eh) {
	this.eh = eh;
    }

    /**
     * <p>Sends a file to the MARC parser.</p>
     *
     * @param filename the filename
     */
    public void parse(String file) throws IOException {
	parse(new BufferedReader(new FileReader(file)));
     }

    /**
     * <p>Sends an input stream reader to the MARC parser.</p>
     *
     * @param src the input stream reader
     */
    public void parse(InputStreamReader input) throws IOException {
	parse(new BufferedReader(input));
    }

    private void parse(Reader input) throws IOException {
	int ldrLength = 24;

	if (! input.ready())
	    reportFatalError("Unable to read input");

	if (mh != null) 
	    mh.startFile();

	while(true) {
	    Leader ldr = new Leader();
	    char[] ldr00 = new char[5];
	    char[] ldr07 = new char[2];
	    char[] ldr12 = new char[5];
	    char[] ldr17 = new char[3];
	    char[] ldr20 = new char[4];

	    if (input.read(ldr00) == -1) break;
	    ldr.setRecordLength(Integer.parseInt(new String(ldr00)));
	    ldr.setRecordStatus((char)input.read());
	    ldr.setTypeOfRecord((char)input.read());
	    input.read(ldr07);
	    ldr.setImplDefined1(ldr07);
	    ldr.setCharCodingScheme((char)input.read());
	    ldr.setIndicatorCount(Integer.parseInt(String.valueOf((char)input.read())));
	    ldr.setSubfieldCodeLength(Integer.parseInt(String.valueOf((char)input.read())));
	    input.read(ldr12);
	    ldr.setBaseAddressOfData(Integer.parseInt(new String(ldr12)));
	    input.read(ldr17);
	    ldr.setImplDefined2(ldr17);
	    input.read(ldr20);
	    ldr.setEntryMap(ldr20);

	    recordCounter += 24;
	    if (mh != null) 
		mh.startRecord(ldr);

	    int dirLength = ldr.getBaseAddressOfData() - (ldrLength + 1);
	    int dirEntries = dirLength / 12;
	    String[] tag = new String[dirEntries];
	    int[] length = new int[dirEntries]; 

	    if ((dirLength % 12) != 0)
		reportError("Invalid directory length");
     
	    for (int i = 0; i < dirEntries; i++) {
		char[] d = new char[3];
		char[] e = new char[4];
		char[] f = new char[5];
		input.read(d);
		input.read(e);
		input.read(f);
		recordCounter += 12;
		tag[i] = new String(d);
		length[i] = Integer.parseInt(new String(e));
	    }

	    if (input.read() != FT)
		reportError("Directory not terminated");
	    recordCounter++;

	    for (int i = 0; i < dirEntries; i++) {
		char field[] = new char[length[i]];
		input.read(field);

		if (field[field.length -1] != FT)
		    reportError("Field not terminated");

		recordCounter += length[i];
		if (Tag.isControlField(tag[i])) {
		    parseControlField(tag[i], field);
		} else {
		    parseDataField(tag[i], field);
		}
	    }

	    if (input.read() != RT)
		reportError("Record not terminated");
	    recordCounter++;

	    if (recordCounter != ldr.getRecordLength())
		reportError("Record length not equal to characters read");

	    fileCounter += recordCounter;
	    recordCounter = 0;
	    if (mh != null) 
		mh.endRecord();

	}
	input.close();
	if (mh != null) 
	    mh.endFile();
    }

    private void parseControlField(String tag, char[] field) {
	if (mh != null) 
	    mh.controlField(tag, new String(field).trim().toCharArray());
    }

    private void parseDataField(String tag, char[] field) 
	throws IOException {
	char code = BLANK;
	char ind1 = BLANK;
	char ind2 = BLANK;
	StringBuffer data = null;
	if (field.length >= 3) {
	    ind1 = field[0];
	    ind2 = field[1];
	} else {
	    reportWarning("Field contains no data elements");
	}
	if (mh != null)
	    mh.startDataField(tag, ind1, ind2);
	for (int i = 2; i < field.length; i++) {
	    char c = field[i];
	    switch(c) {
	    case US :
		if (data != null) 
		    reportSubfield(code, data);
		code = field[i+1];
		i++;
		data = new StringBuffer();
		break;
	    case FT :
		if (data != null) 
		    reportSubfield(code, data);
		break;
	    default :
		data.append(c);
	    }
	}
	if (mh != null) 
	    mh.endDataField(tag);
    }

    private void reportSubfield(char code, StringBuffer data) {
	if (mh != null) 
	    mh.subfield(code, new String(data).toCharArray());
    }

    private void reportWarning(String message) {
	if (eh != null) 
	    eh.warning(new MarcReaderException(message, getPosition()));
    }

    private void reportError(String message) {
	if (eh != null) 
	    eh.error(new MarcReaderException(message, getPosition()));
    }

    private void reportFatalError(String message) {
	if (eh != null) 
	    eh.fatalError(new MarcReaderException(message, getPosition()));
    }

    private int getPosition() {
	return fileCounter + recordCounter;
    }

}
