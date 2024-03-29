package org.marc4j.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.marc4j.MarcJsonWriter;
import org.marc4j.MarcReader;
import org.marc4j.MarcReaderConfig;
import org.marc4j.MarcReaderFactory;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcTxtWriter;
import org.marc4j.MarcWriter;
import org.marc4j.MarcWriterFactory;
import org.marc4j.MarcXmlWriter;
import org.marc4j.Mrk8StreamWriter;
import org.marc4j.converter.impl.UnicodeToAnsel;
import org.marc4j.marc.Record;

public class RecordIODriver {

    String input;
    String output;
    String convert;
    boolean normalize = false;
    boolean pretty = true;
    boolean oversize = false;
    int splitAt = 0;
    boolean writeErrorRecs = true;
    boolean writeNoErrorRecs = true;
    List<String>otherArgs = new ArrayList<String>();

    MarcReaderConfig readerConfig;

    public RecordIODriver()
    {
        readerConfig = new MarcReaderConfig();
    }

    public String getInputName()
    {
        return input;
    }

    public String getOutputName()
    {
        return output;
    }

    public String getConvertValue()
    {
        return convert;
    }

    public boolean shouldNormalize()
    {
        return normalize;
    }

    public boolean shouldPrettify()
    {
        return pretty;
    }

    public boolean shouldWriteErrorRecs()
    {
        return writeErrorRecs;
    }

    public boolean shouldWriteNoErrorRecs()
    {
        return writeNoErrorRecs;
    }

    public MarcReaderConfig getReaderConfig()
    {
        return readerConfig;
    }
    
    public List<String> getOtherArgs()
    {
        return otherArgs;
    }



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
    public static void main(final String args[]) {

        RecordIODriver driverInfo = new RecordIODriver();
        processArgumentsToConfig(driverInfo, args);

        InputStream in = null;
        try {
            if (driverInfo.input == null || driverInfo.input.equals("-")) {
                in = System.in;
            }
            else {
                in = new FileInputStream(driverInfo.input);
            }
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }

        OutputStream out = null;
        if (driverInfo.output != null) {
            try {
                out = new FileOutputStream(driverInfo.output);
            } catch (final FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            out = System.out;
        }

        MarcWriter writer = null;
        try { 
            writer = makeWriterFromConvertParm(driverInfo, out);
        }
        catch (IllegalArgumentException iae) {
            System.err.println("Error: "+ iae.getMessage());
            System.exit(1);
        }

        MarcReader reader = null;
        try {
            reader = MarcReaderFactory.makeReader(driverInfo.readerConfig, in);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        String previousControlNumber = "n/a";
        while (reader.hasNext()) {
            String controlNumber = "n/a";
            String location = "after";
            try {
                location = "after";
                final Record record = reader.next();
                location = "in";
                controlNumber = record.getControlNumber();
                if ((driverInfo.writeErrorRecs && driverInfo.writeNoErrorRecs) ||
                    (driverInfo.writeErrorRecs && record.hasErrors()) ||
                    (driverInfo.writeNoErrorRecs && !record.hasErrors())) {
                    writer.write(record);
                }
            }
            catch (RuntimeException re) {
                System.err.printf("Exception %s record: %s -- %s\n",
                  location, (location.equals("after") ? previousControlNumber : controlNumber), re.getMessage());
                re.printStackTrace(System.err);
                break;
            }
            previousControlNumber = controlNumber;

        }
        writer.close();
    }

    static protected RecordIODriver processArgumentsToConfig(RecordIODriver driverInfo, String[] args)
    {
        String editProperties = null;
        String encoding = "MARC8";
        String deleteSubfieldsSpec = null;
        String filterIfPresent = null;
        String filterIfMissing = null;
        String combineConsecutiveRecordsFieldParm = null;
        boolean toUtf8 = true;
        boolean strict = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-out") || args[i].equals("-o")) {
                if (i == args.length - 1) {
                    usage("Missing argument for option "+args[i]+ " should be file to write output", 1);
                }
                driverInfo.output = args[++i].trim();
            } else if (args[i].equals("-edit")) {
                if (i == args.length - 1) {
                    usage("Missing argument for option "+args[i]+ " should be properties file describing edits to perform", 1);
                }
                editProperties = args[++i].trim();

            } else if (args[i].equals("-delete")) {
                if (i == args.length - 1) {
                    usage("Missing argument for option "+args[i]+ " should be a colon-spearated list of fields/subfields to delete from the records that are read", 1);
                }
                deleteSubfieldsSpec = args[++i].trim();
            } else if (args[i].equals("-convert")) {
                if (i == args.length - 1) {
                    usage("Missing argument for option "+args[i]+ " should specify format in which to generate output", 1);
                }
                driverInfo.convert = args[++i].trim();
            } else if (args[i].equals("-matches")) {
                if (i == args.length - 1) {
                    usage("Missing argument for option "+args[i]+ " should be field that must exist, with optional string that must be present", 1);
                }
                filterIfPresent = args[++i].trim();
            } else if (args[i].equals("-notmatches")) {
                if (i == args.length - 1) {
                    usage("Missing argument for option "+args[i]+ " should be field that must NOT exist, with optional string that must NOT be present", 1);
                }
                filterIfMissing = args[++i].trim();
            } else if (args[i].equals("-errors")) {
                driverInfo.writeErrorRecs = true;
                driverInfo.writeNoErrorRecs = false;
            } else if (args[i].equals("-raw")) {
                toUtf8 = false;
            } else if (args[i].equals("-noerrors")) {
                driverInfo.writeErrorRecs = false;
                driverInfo.writeNoErrorRecs = true;
            } else if (args[i].equals("-encoding")) {
                if (i == args.length - 1) {
                    usage("Missing argument for option "+args[i]+ " should specify the expected encoding of the input file(s)", 1);
                }
                encoding = args[++i].trim();
            } else if (args[i].equals("-pretty")) {
                driverInfo.pretty = true;
            } else if (args[i].equals("-split")) {
                driverInfo.splitAt = 70000;
            } else if (args[i].equals("-oversize")) {
                driverInfo.oversize = true;
            } else if (args[i].equals("-normalize")) {
                driverInfo.normalize = true;
            } else if (args[i].equals("-strict")) {
                strict = true;
            } else if (args[i].equals("-combine")) {
                if (i == args.length - 1) {
                    usage("Missing argument for option "+args[i]+ " should specify how consecutive records with matching ids should be combined", 1);
                }
                combineConsecutiveRecordsFieldParm = args[++i].trim();
            } else if (args[i].equals("-usage")) {
                usage(null, 0);
            } else if (args[i].equals("-help")) {
                usage("Missing argument for option "+args[i]+ " should specify command-line option ", 0);
            } else if (driverInfo.input == null && args[i].startsWith("-") && args[i].length() > 1) {
                usage("Unknown command line option "+args[i], 1);
            } else if (driverInfo.input == null) {
                driverInfo.input = args[i].trim();
            } else {
                driverInfo.otherArgs.add(args[i]);
            }
        }

        driverInfo.readerConfig.setCombineConsecutiveRecordsFields(combineConsecutiveRecordsFieldParm);
        driverInfo.readerConfig.setUnicodeNormalize(driverInfo.normalize ? "C" : null);
        if (editProperties != null) {
            File editFile = new File(editProperties);
            if (!editFile.exists() || !editFile.canRead()) {
                System.err.println("Error: Unable to read Edit Properties file:  " + editFile.getAbsolutePath());
                System.exit(2);
            }
        }

        driverInfo.readerConfig.setMarcRemapFilename(editProperties);
        driverInfo.readerConfig.setDeleteSubfieldSpec(deleteSubfieldsSpec);
        driverInfo.readerConfig.setDefaultEncoding(encoding);
        driverInfo.readerConfig.setFilterParams(filterIfPresent, filterIfMissing);
        driverInfo.readerConfig.setToUtf8(toUtf8);
        driverInfo.readerConfig.setPermissiveReader(!strict);

        return(driverInfo);
    }

    static protected MarcWriter makeWriterFromConvertParm(RecordIODriver driverInfo, OutputStream out) {
        MarcWriter result = MarcWriterFactory.makeWriterFromConvertParm(driverInfo.convert, driverInfo.pretty, driverInfo.normalize, driverInfo.oversize, driverInfo.splitAt, out);
        
        if (result.expectsUnicode() && driverInfo.readerConfig.toUtf8() == false) {
            driverInfo.readerConfig.setToUtf8(true);
        }
        return(result);
    }

    private static void usage(String error, int exitcode) {
        if (error != null) {
            System.err.println("Error: "+ error);
        }
        System.err.println("Usage: org.marc4j.util.RecordIODriver [-options] <file.mrc>");
        System.err.println("   or: org.marc4j.util.RecordIODriver [-options] <file.xml>");
        System.err.println("   or: org.marc4j.util.RecordIODriver [-options] <file.json>");
        System.err.println("       -convert <format> = Produce output in the specified format");
        System.err.println("           Valid formats are: xml, json, utf8, marc8, mrk8, ncr, text ");
        System.err.println("       -encoding <inputFile encoding> = expected character encoding of input file");
        System.err.println("       -normalize = perform Unicode normalization");
        System.err.println("       -combine = combine consecutive that have the same record id");
        System.err.println("       -errors = only output records that are flagged as containing errors");
        System.err.println("       -noerrors = only output records that are NOT flagged as containing errors");
        System.err.println("       -delete <fields> = a colon-spearated list of fields/subfields to delete from the records that are read");
        System.err.println("       -edit <file.properties> = apply all the edits specified in the file <file.properties> to the records");
        System.err.println("       -matches <pattern> = only output records that match the specified pattern");
        System.err.println("       -notmatches <pattern> = only output records that do not match the specified pattern");
        System.err.println("       -out <file> = Output to <file> instead of stdout");
        System.err.println("       -help <option> = more verbose help message about the specified option");
        System.err.println("       -usage = this message");
        System.exit(exitcode);
    }


}
