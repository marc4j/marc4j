package org.marc4j.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

public class RecordIODriver {
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

        String input = null;
        String output = null;
        String editProperties = null;
        String convert = null;
        String encoding = "MARC8";
        String deleteSubfieldsSpec = null;
        String filterIfPresent = null;
        String filterIfMissing = null;
        String combineConsecutiveRecordsFieldParm = null;
        boolean normalize = false;
        boolean pretty = true;
        boolean strict = false;
        boolean marc8Flag = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-out") || args[i].equals("-o")) {
                if (i == args.length - 1) {
                    usage("Missing argument for option "+args[i]+ " should be file to write output", 1);
                }
                output = args[++i].trim();
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
                convert = args[++i].trim();
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
            } else if (args[i].equals("-encoding")) {
                if (i == args.length - 1) {
                    usage("Missing argument for option "+args[i]+ " should specify the expected encoding of the input file(s)", 1);
                }
                encoding = args[++i].trim();
           /*  } else if (args[i].equals("-pretty")) {
                pretty = true;*/
            } else if (args[i].equals("-normalize")) {
                normalize = true;
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
            } else if (args[i].startsWith("-") && args[i].length() > 1) {
                usage("Unknown command line option "+args[i], 1);
            } else {
                input = args[i].trim();

                // Must be last arg
                if (i != args.length - 1) {
                    usage("Input file should come after all of the command line options ", 1);
                }
            }
        }

        InputStream in = null;
        try {
            if (input == null || input.equals("-")) {
                in = System.in;
            }
            else {
                in = new FileInputStream(input);
            }
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }

        OutputStream out = null;
        if (output != null) {
            try {
                out = new FileOutputStream(output);
            } catch (final FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            out = System.out;
        }

        MarcReaderConfig config = new MarcReaderConfig();

        config.setCombineConsecutiveRecordsFields(combineConsecutiveRecordsFieldParm);
        config.setUnicodeNormalize(normalize ? "C" : null);
        if (editProperties != null) {
            File editFile = new File(editProperties);
            if (!editFile.exists() || !editFile.canRead()) {
                System.err.println("Error: Unable to read Edit Properties file:  " + editFile.getAbsolutePath());
                System.exit(2);
            }
        }

        config.setMarcRemapFilename(editProperties);
        config.setDeleteSubfieldSpec(deleteSubfieldsSpec);
        config.setDefaultEncoding(encoding);
        config.setFilterParams(filterIfPresent, filterIfMissing);
        config.setToUtf8(true);
        config.setPermissiveReader(!strict);

        MarcReader reader = null;
        try {
            reader = MarcReaderFactory.makeReader(config, in);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        MarcWriter writer = null;

        if (convert.equalsIgnoreCase("text") || convert.equalsIgnoreCase("ASCII")) {
            writer = new MarcTxtWriter(out);
        } else if (convert.equalsIgnoreCase("XML") || convert.equalsIgnoreCase("MARCXML")) {
            MarcXmlWriter xmlwriter = new MarcXmlWriter(out, "UTF8");
            if (pretty)     xmlwriter.setIndent(true);
            if (normalize)  xmlwriter.setUnicodeNormalization(true);
            writer = xmlwriter;
        } else if (convert.equalsIgnoreCase("MARC_IN_JSON") || convert.equalsIgnoreCase("json")) {
            MarcJsonWriter jsonwriter = new MarcJsonWriter(out, MarcJsonWriter.MARC_IN_JSON);
            if (pretty)     jsonwriter.setIndent(true);
            if (normalize)  jsonwriter.setUnicodeNormalization(true);
            writer = jsonwriter;
        } else if (convert.equalsIgnoreCase("MARC_JSON") || convert.equalsIgnoreCase("json2")) {
            MarcJsonWriter jsonwriter = new MarcJsonWriter(out, MarcJsonWriter.MARC_JSON);
            if (pretty)     jsonwriter.setIndent(true);
            if (normalize)  jsonwriter.setUnicodeNormalization(true);
            writer = jsonwriter;
        } else if (convert.equalsIgnoreCase("UTF8") || convert.equalsIgnoreCase("UTF-8")) {
            MarcStreamWriter binwriter = new MarcStreamWriter(out, "UTF8");
            writer = binwriter;
        } else if (convert.equalsIgnoreCase("MARC8")) {
            MarcStreamWriter binwriter = new MarcStreamWriter(out, "ISO8859_1", true);
            binwriter.setConverter(new UnicodeToAnsel());
            marc8Flag = true;
            writer = binwriter;
        } else if (convert.equalsIgnoreCase("MARC8NCR") || convert.equalsIgnoreCase("NCR")) {
            MarcStreamWriter binwriter = new MarcStreamWriter(out, "ISO8859_1", true);
            binwriter.setConverter(new UnicodeToAnsel(true));
            marc8Flag = true;
            writer = binwriter;
        } else if (convert.equalsIgnoreCase("MRK8")) {
            Mrk8StreamWriter mrkwriter = new Mrk8StreamWriter(out);
            writer = mrkwriter;
        } else {
            System.err.println("Error : Unknown output format: "+ convert );
            System.exit(1);
        }

        while (reader.hasNext()) {
            final Record record = reader.next();
            record.getLeader().setCharCodingScheme((marc8Flag ? ' ' : 'a'));
            writer.write(record);
        }
        writer.close();
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
        System.err.println("       -normalize = perform Unicode normalization");
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
