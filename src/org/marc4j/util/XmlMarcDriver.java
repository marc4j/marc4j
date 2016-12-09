/**
 * Copyright (C) 2004 Bas Peters
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.marc4j.Constants;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcXmlReader;
import org.marc4j.converter.CharConverter;
import org.marc4j.converter.impl.UnicodeToAnsel;
import org.marc4j.converter.impl.UnicodeToIso5426;
import org.marc4j.converter.impl.UnicodeToIso6937;
import org.marc4j.marc.Record;
/**
 * Provides a driver to convert MARCXML records to MARC format. The following example reads input.xml and writes output
 * to the console:
 *
 * <pre>
 *       java org.marc4j.util.XmlMarcDriver input.xml
 * </pre>
 * <p>
 * The following example reads input.xml, converts UTF-8 and writes output in MARC-8 to output.mrc:
 * </p>
 *
 * <pre>
 *       java org.marc4j.util.XmlMarcDriver -convert MARC8 -out output.mrc input.xml
 * </pre>
 * <p>
 * It is possible to pre-process the input file using an XSLT stylesheet. The transformation should produce valid
 * MARCXML. The following example transforms a MODS file to MARCXML and outputs MARC records.
 * </p>
 *
 * <pre>
 *       java org.marc4j.util.XmlMarcDriver -convert MARC8 -out output.mrc \
 *         -xsl http://www.loc.gov/standards/marcxml/xslt/MODS2MARC21slim.xsl modsfile.xml
 * </pre>
 * <p>
 * For usage, run from the command-line with the following command:
 * </p>
 *
 * <pre>
 *       java org.marc4j.util.XmlMarcDriver -usage
 * </pre>
 * <p>
 * Check the home page for <a href="http://www.loc.gov/standards/marcxml/"> MARCXML </a> for more information about the
 * MARCXML format.
 * </p>
 *
 * @author Bas Peters
 */

public class XmlMarcDriver {

    private XmlMarcDriver() {
    }

    /**
     * Provides a static entry point.
     * <p>
     * Arguments:
     * </p>
     * <ul>
     * <li>-xsl &lt;stylesheet URL&gt; - pre-process using XSLT-stylesheet</li>
     * <li>-out &lt;output file&gt; - write to output file</li>
     * <li>-convert &lt;encoding&gt; - convert UTF-8 to &lt;encoding&gt;
     * (Supported encodings: MARC8, ISO5426, ISO6937)</li>
     * <li>-encoding &lt;encoding&gt; - Output using specified Java character encoding</li>
     * <li>-usage - show usage</li>
     * <li>&lt;input file&gt; - input file with MARCXML records or a transformation source
     * </ul>
     * @param args - the command-line arguments
     */
    public static void main(final String args[]) {
        final long start = System.currentTimeMillis();

        String input = null;
        String output = null;
        String stylesheet = null;
        String convert = null;
        String encoding = null;

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
            in = new FileInputStream(new File(input));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
        MarcXmlReader reader = null;
        if (stylesheet == null) {
            reader = new MarcXmlReader(in);
        } else {
            final Source source = new StreamSource(stylesheet);
            reader = new MarcXmlReader(in, source);
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

        MarcStreamWriter writer = null;
        if (encoding != null) {
            writer = new MarcStreamWriter(out, encoding);
        } else {
            writer = new MarcStreamWriter(out);
        }

        if (convert != null) {
            CharConverter charconv = null;
            if (Constants.MARC_8_ENCODING.equals(convert)) {
                charconv = new UnicodeToAnsel();
            } else if (Constants.ISO5426_ENCODING.equals(convert)) {
                charconv = new UnicodeToIso5426();
            } else if (Constants.ISO6937_ENCODING.equals(convert)) {
                charconv = new UnicodeToIso6937();
            } else {
                System.err.println("Unknown character set");
                System.exit(1);
            }
            writer.setConverter(charconv);
        }

        while (reader.hasNext()) {
            final Record record = reader.next();
            if (Constants.MARC_8_ENCODING.equals(convert)) {
                record.getLeader().setCharCodingScheme(' ');
            }
            writer.write(record);
        }
        writer.close();

        System.err.println("Total time: " + (System.currentTimeMillis() - start) + " miliseconds");
    }

    private static void usage() {
        System.err.println("MARC4J, Copyright (C) 2002-2006 Bas Peters");
        System.err.println("Usage: org.marc4j.util.XmlMarcDriver [-options] <file.mrc>");
        System.err.println("       -convert <encoding> = Converts UTF-8 to <encoding>");
        System.err.println("       Valid encodings are: MARC8, ISO5426, ISO6937");
        System.err
                .println("       -encoding <encoding> = Output using specified Java character encoding");
        System.err.println("       -xsl <file> = Pre-process MARCXML using XSLT stylesheet <file>");
        System.err.println("       -out <file> = Output using <file>");
        System.err.println("       -usage or -help = this message");
        System.err.println("The program outputs MARC records in ISO 2709 format");
        System.err.println("See http://marc4j.tigris.org for more information.");
        System.exit(1);
    }

}
