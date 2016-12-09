/**
 * Copyright (C) 2005 Bas Peters
 *
 * This file is part of MARC4J
 *
 * MARC4J is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * MARC4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with MARC4J; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.marc4j.util;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.marc4j.Constants;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcXmlWriter;
import org.marc4j.converter.CharConverter;
import org.marc4j.converter.impl.AnselToUnicode;
import org.marc4j.converter.impl.Iso5426ToUnicode;
import org.marc4j.converter.impl.Iso6937ToUnicode;
import org.marc4j.marc.Record;

/**
 * Provides a basic driver to convert MARC records to MARCXML. Output is encoded in UTF-8.
 * <p>
 * The following example reads input.mrc and writes output to the console:
 *
 * <pre>
 *     java org.marc4j.util.MarcXmlDriver input.mrc
 * </pre>
 * <p>
 * The following example reads input.mrc, converts MARC-8 and writes output in UTF-8 to output.xml:
 *
 * <pre>
 *     java org.marc4j.util.MarcXmlDriver -convert MARC8 -out output.xml input.mrc
 * </pre>
 * <p>
 * It is possible to post-process the result using an XSLT stylesheet. The following example converts MARC to MODS:
 *
 * <pre>
 *     java org.marc4j.util.MarcXmlDriver -convert MARC8 \
 *       -xsl http://www.loc.gov/standards/mods/v3/MARC21slim2MODS3.xsl \
 *       -out modsoutput.xml input.mrc
 * </pre>
 * <p>
 * For usage, run from the command-line with the following command:
 *
 * <pre>
 *     java org.marc4j.util.MarcXmlDriver -usage
 * </pre>
 * <p>
 * Check the home page for <a href="http://www.loc.gov/standards/marcxml/"> MARCXML </a> for more information about the
 * MARCXML format.
 *
 * @author Bas Peters
 */
public class MarcXmlDriver {

    private MarcXmlDriver() {
    }

    /**
     * Provides a static entry point.
     * <p>
     * Arguments:
     * </p>
     * <ul>
     * <li>-xsl &lt;stylesheet URL&gt; - post-process using XSLT-stylesheet</li>
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
        final long start = System.currentTimeMillis();

        String input = null;
        String output = null;
        String stylesheet = null;
        String convert = null;
        String encoding = "ISO_8859_1";
        boolean normalize = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-xsl")) {
                if (i == args.length - 1) {
                    usage();
                }
                stylesheet = args[++i].trim();
            } else if (args[i].equals("-out")) {
                if (i == args.length - 1) {
                    usage();
                }
                output = args[++i].trim();
            } else if (args[i].equals("-convert")) {
                if (i == args.length - 1) {
                    usage();
                }
                convert = args[++i].trim();
            } else if (args[i].equals("-encoding")) {
                if (i == args.length - 1) {
                    usage();
                }
                encoding = args[++i].trim();
            } else if (args[i].equals("-normalize")) {
                normalize = true;
            } else if (args[i].equals("-usage")) {
                usage();
            } else if (args[i].equals("-help")) {
                usage();
            } else {
                input = args[i].trim();

                // Must be last arg
                if (i != args.length - 1) {
                    usage();
                }
            }
        }
        if (input == null) {
            usage();
        }

        InputStream in = null;
        try {
            in = new FileInputStream(input);
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
        MarcStreamReader reader = null;
        if (encoding != null) {
            reader = new MarcStreamReader(in, encoding);
        } else {
            reader = new MarcStreamReader(in);
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

        MarcXmlWriter writer = null;

        if (stylesheet == null) {
            if (convert != null) {
                writer = new MarcXmlWriter(out, "UTF8");
            } else {
                writer = new MarcXmlWriter(out, "UTF8");
            }
        } else {
            Writer outputWriter = null;
            if (convert != null) {
                try {
                    outputWriter = new OutputStreamWriter(out, "UTF8");
                } catch (final UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                outputWriter = new BufferedWriter(outputWriter);
            } else {
                outputWriter = new OutputStreamWriter(out);
                outputWriter = new BufferedWriter(outputWriter);
            }
            final Result result = new StreamResult(outputWriter);
            final Source source = new StreamSource(stylesheet);
            writer = new MarcXmlWriter(result, source);
        }
        writer.setIndent(true);

        if (convert != null) {
            CharConverter charconv = null;
            if (Constants.MARC_8_ENCODING.equals(convert)) {
                charconv = new AnselToUnicode();
            } else if (Constants.ISO5426_ENCODING.equals(convert)) {
                charconv = new Iso5426ToUnicode();
            } else if (Constants.ISO6937_ENCODING.equals(convert)) {
                charconv = new Iso6937ToUnicode();
            } else {
                System.err.println("Unknown character set");
                System.exit(1);
            }
            writer.setConverter(charconv);
        }

        if (normalize) {
            writer.setUnicodeNormalization(true);
        }

        while (reader.hasNext()) {
            final Record record = reader.next();
            if (Constants.MARC_8_ENCODING.equals(convert)) {
                record.getLeader().setCharCodingScheme('a');
            }
            writer.write(record);
        }
        writer.close();

        System.err.println("Total time: " + (System.currentTimeMillis() - start) + " miliseconds");
    }

    private static void usage() {
        System.err.println("MARC4J, Copyright (C) 2002-2006 Bas Peters");
        System.err.println("Usage: org.marc4j.util.MarcXmlDriver [-options] <file.mrc>");
        System.err.println("       -convert <encoding> = Converts <encoding> to UTF-8");
        System.err.println("       Valid encodings are: MARC8, ISO5426, ISO6937");
        System.err.println("       -normalize = perform Unicode normalization");
        System.err.println("       -xsl <file> = Post-process MARCXML using XSLT stylesheet <file>");
        System.err.println("       -out <file> = Output using <file>");
        System.err.println("       -usage or -help = this message");
        System.err.println("The program outputs well-formed MARCXML");
        System.err.println("See http://marc4j.tigris.org for more information.");
        System.exit(1);
    }

}
