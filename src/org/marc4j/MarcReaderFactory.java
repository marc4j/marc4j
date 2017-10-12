
package org.marc4j;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.marc4j.util.FileFinderUtil;

public class MarcReaderFactory {

    private MarcReaderFactory() {}

    public static MarcReader makeReader(MarcReaderConfig config, final String[] searchDirectories,
            final String... inputFilenames) throws IOException {

        if (inputFilenames.length == 0) {
            return makeReader(config, searchDirectories, "stdin");
        } else if (inputFilenames.length == 1) {
            return makeReader(config, searchDirectories, inputFilenames[0]);
        }
        final List<MarcReader> readers = new ArrayList<MarcReader>();
        for (final String inputFilename : inputFilenames) {
            final MarcReader reader = makeReaderPart(config, inputFilename);
            readers.add(reader);
        }
        MarcReader result = new MarcMultiplexReader(readers, Arrays.asList(inputFilenames));
        result = decorateMarcReader(result, config, searchDirectories);
        return result;
    }

    public static MarcReader makeReader(MarcReaderConfig config, final String[] searchDirectories,
            final List<String> inputFilenames) throws IOException {

        if (inputFilenames.size() == 0) {
            return makeReader(config, searchDirectories, "stdin");
        } else if (inputFilenames.size() == 1) {
            return makeReader(config, searchDirectories, inputFilenames.iterator().next());
        }

        final List<MarcReader> readers = new ArrayList<MarcReader>();

        for (final String inputFilename : inputFilenames) {
            final MarcReader reader = makeReaderPart(config, inputFilename);
            readers.add(reader);
        }
        MarcReader result = new MarcMultiplexReader(readers, inputFilenames);
        result = decorateMarcReader(result, config, searchDirectories);
        return result;

    }

    public static MarcReader makeReader(MarcReaderConfig config, final String[] searchDirectories,
            final String inputFilename) throws IOException {

        InputStream is;
        if (inputFilename.equals("-") || inputFilename.equals("stdin")) {
            is = new BufferedInputStream(System.in);
        } else {
            is = new BufferedInputStream(new FileInputStream(inputFilename));
        }
        MarcReader reader = makeReaderInternal(config, is);
        reader = decorateMarcReader(reader, config, searchDirectories);
        return reader;
    }

    private static MarcReader makeReaderPart(MarcReaderConfig config, final String inputFilename)
            throws IOException {

        InputStream is = new FileInputStream(inputFilename);
        MarcReader reader = makeReaderInternal(config, is);
        return reader;
    }

    public static MarcReader makeReader(MarcReaderConfig config, final InputStream input)
            throws IOException {
        MarcReader reader = makeReaderInternal(config, input);
        reader = decorateMarcReader(reader, config, new String[]{"."});
        return(reader);
    }


    private static MarcReader makeReaderInternal(MarcReaderConfig config, final InputStream input)
            throws IOException {

        boolean inputTypeXML = false;

        boolean inputTypeBinary = false;

        boolean inputTypeJSON = false;

        boolean inputTypeMrk8 = false;

        MarcReader reader;

        InputStream is;
        if (input.markSupported()) {
            is = input;
        } else {
            is = new BufferedInputStream(input);
        }
        is.mark(30);
        final byte[] buffer = new byte[25];

        int numRead;

        try {
            numRead = is.read(buffer);
            is.reset();
        } catch (final IOException e) {
            // logger.error("Fatal error: Exception reading from InputStream");
            throw new IllegalArgumentException("Fatal error: Exception reading from InputStream");
        }
        final String filestart = new String(buffer);
        inputTypeXML = false;
        inputTypeBinary = false;
        inputTypeJSON = false;

        if (numRead == -1 || filestart.length() == 0) {
            inputTypeBinary = true;
        } else if (filestart.substring(0, 5).equalsIgnoreCase("<?xml")) {
            inputTypeXML = true;
        } else if (filestart.startsWith("{")) {
            inputTypeJSON = true;
        } else if (filestart.substring(0, 5).matches("\\d\\d\\d\\d\\d")) {
            inputTypeBinary = true;
        } else if (filestart.contains("<?xml") || filestart.contains("<?XML")) {
            inputTypeXML = true;
        } else if (filestart.contains("<collection")) {
            inputTypeXML = true;
        } else if (filestart.matches("[^<]*<[^:>]+:collection[ >].*")) {
            inputTypeXML = true;
        } else if (filestart.contains("<record")) {
            inputTypeXML = true;
        } else if (filestart.matches("[^<]*<[^:>]+:record[ >].*")) {
            inputTypeXML = true;
        } else if (filestart.contains("<!--")) {
            inputTypeXML = true;
        } else if (filestart.contains("=LDR  ")) {
            inputTypeMrk8 = true;
        }

        if (inputTypeXML) {
            // to_utf_8 = true;
            reader = new MarcUnprettyXmlReader(is);
        } else if (inputTypeJSON) {
            // to_utf_8 = true;
            reader = new MarcJsonReader(is);
        } else if (inputTypeMrk8) {
            // to_utf_8 = true;
            reader = new Mrk8StreamReader(is, config.toUtf8());
        } else if (inputTypeBinary) {
            reader = new MarcPermissiveStreamReader(is, config.isPermissiveReader(), 
                    config.toUtf8(), config.getDefaultEncoding());
        } else {
            // logger.error("Fatal error: Unable to determine type of inputfile");
            throw new IllegalArgumentException(
                    "Fatal error: Unable to determine type of inputfile.  File starts with: " + filestart);
        }
        return (reader);
    }

    public static MarcReader decorateMarcReader(final MarcReader r, MarcReaderConfig config,
            final String[] searchDirectories) throws IOException {

        MarcReader reader = r;

        // Add Combine Record reader if requested

        if (reader != null && config.getCombineConsecutiveRecordsFields() != null) {
            reader = new MarcCombiningReader(reader, config.getCombineConsecutiveRecordsFields(),
                    config.getCombineRecordsLeftField(), config.getCombineRecordsRightField());
        }

        // Add FilteredReader if requested

        if (reader != null && (config.shouldFilter())) {
            reader = new MarcFilteredReader(reader, config.getIncludeIfPresent(), config
                    .getIncludeIfMissing());
        }

        // Add ScriptedRecordEditReader if requested

        String marcDeleteSubfields = config.getDeleteSubfieldSpec();

        String marcRemapRecord = config.getMarcRemapFilename();

        if (reader != null && (marcDeleteSubfields != null || marcRemapRecord != null)) {
            if (marcRemapRecord != null) {

                final InputStream remapInputStream = FileFinderUtil.getFileInputStream(
                        searchDirectories, marcRemapRecord);

                final Properties remapProps = new Properties();
                remapProps.load(remapInputStream);

                reader = new MarcScriptedRecordEditReader(reader, marcDeleteSubfields, remapProps);
            }
            else if (marcDeleteSubfields != null) {
                reader = new MarcScriptedRecordEditReader(reader, marcDeleteSubfields, null);
            }
        }

        // Do translating last so that if we are Filtering as well as
        // translating, we don't expend the
        // effort to translate records, which may then be filtered out and
        // discarded.
        if (reader != null && config.toUtf8() && config.getUnicodeNormalize() != null) {
            reader = new MarcTranslatedReader(reader, config.getUnicodeNormalize());
        }

        return reader;

    }

    // private static void setMarc4JProperties(final Properties configProps) {
    // for (final String prop : configProps.stringPropertyNames()) {
    // if (prop.startsWith("org.marc4j.")) {
    // final String value = configProps.getProperty(prop);
    // System.setProperty(prop, value);
    // } else if (configProps.getProperty("marc.override") != null) {
    // System.setProperty("org.marc4j.marc.MarcFactory",
    // configProps.getProperty(
    // "marc.override").trim());
    // }
    // }
    // }

}
