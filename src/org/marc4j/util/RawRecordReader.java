
package org.marc4j.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * Read a binary marc file, treating the records mostly as opaque blocks of data.
 * Its purpose is to quickly iterate through records looking for one that matches certain
 * simple criteria, at which point the full marc record can be unpacked for more extensive processing
 * @author Robert Haschart
 */
public class RawRecordReader {

    private final DataInputStream input;

    RawRecord nextRec = null;

    RawRecord afterNextRec = null;

    boolean mergeRecords = true;

    /**
     * Creates a raw record reader from the supplied {@link InputStream}.
     *
     * @param is - the InputStream to read
     */
    public RawRecordReader(final InputStream is) {
        input = new DataInputStream(new BufferedInputStream(is));
    }

    /**
     * Creates a raw record reader from the supplied {@link InputStream} and merge records boolean flag.
     *
     * @param is - the InputStream to read
     * @param mergeRecords - true to cause subsequent records with identical record ids to be combined.
     */
    public RawRecordReader(final InputStream is, final boolean mergeRecords) {
        this.mergeRecords = mergeRecords;
        input = new DataInputStream(new BufferedInputStream(is));
    }

    /**
     * Returns <code>true</code> if there is another raw record to read; else, <code>false</code>.
     *
     * @return returns <code>true</code> if there is another raw record to read
     */
    public boolean hasNext() {
        if (nextRec == null) {
            nextRec = new RawRecord(input);
        }

        if (nextRec != null && nextRec.getRecordBytes() != null) {
            if (afterNextRec == null) {
                afterNextRec = new RawRecord(input);
                if (mergeRecords) {
                    while (afterNextRec != null && afterNextRec.getRecordBytes() != null && afterNextRec
                            .getRecordId().equals(nextRec.getRecordId())) {
                        nextRec = new RawRecord(nextRec, afterNextRec);
                        afterNextRec = new RawRecord(input);
                    }
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Returns the next raw record.
     *
     * @return The next raw record
     */
    public RawRecord next() {
        final RawRecord tmpRec = nextRec;

        nextRec = afterNextRec;
        afterNextRec = null;

        return tmpRec;
    }

    /**
     * main routine for reading a file of binary MarcRecord mostly as chunks of 
     * uninterpreted data. The accepted command line arguments are:
     * <ul>
     * <li>-skip &lt;num&gt;      the number of records to skip over without processing (default 0)</li>
     * <li>-num &lt;num&gt;       the number of records to process (default all of them)</li>
     * <li>-nomerge                    disable the automatic merging of subsequent records that have the same id</li>
     * <li>-id                         instead of outputting the record data, only output the id of the records</li>
     * <li>-h &lt;pattern&gt;  a regex specifying field tags. Only those records that have one of the specified field tag(s) will be returned.</li>
     * <li>&lt;pattern&gt;     a regex specifying record ids. Only those records whose id matches the pattern will be returned</li>
     * <li>&lt;filename.txt&gt;   the name of a file containing records ids, (one per line)
     *             Only those records whose id matches one of the ids in that file will be returned</li>
     * </ul>
     * @param args - the command-line arguments
     */
    public static void main(final String[] args) {
        RawRecordReader reader;

        if (args.length < 2) {
            System.err.println("Error: No records specified for extraction");
        }

        try {
            int numToSkip = 0;
            int numToOutput = -1;
            int offset = 0;

            if (args[offset].equals("-")) {
                reader = new RawRecordReader(System.in);
            } else {
                reader = new RawRecordReader(new FileInputStream(new File(args[offset])));
            }
            offset++;

            while (offset < args.length && (args[offset].equals("-skip") || args[offset]
                    .equals("-num"))) {
                if (args[offset].equals("-skip")) {
                    numToSkip = Integer.parseInt(args[offset + 1]);
                    offset += 2;
                } else if (args[offset].equals("-num")) {
                    numToOutput = Integer.parseInt(args[offset + 1]);
                    offset += 2;
                }
            }

            if (offset < args.length && args[offset].equals("-nomerge")) {
                reader.mergeRecords = false;
                offset++;
            }

            if (numToSkip != 0 || numToOutput != -1) {
                processInput(reader, numToSkip, numToOutput);
            } else if (args[offset].equals("-id")) {
                printIds(reader);
            } else if (args[offset].equals("-h") && args.length >= 3) {
                final String idRegex = args[offset + 1].trim();
                processInput(reader, null, idRegex, null);
            } else if (!args[offset].endsWith(".txt")) {
                final String idRegex = args[offset].trim();
                processInput(reader, idRegex, null, null);
            } else {
                final File idList = new File(args[offset]);
                final BufferedReader idStream = new BufferedReader(new InputStreamReader(
                        new BufferedInputStream(new FileInputStream(idList))));
                String line;
                String findReplace[] = null;

                if (args.length > 2) {
                    findReplace = args[2].split("->");
                }

                final LinkedHashSet<String> idsLookedFor = new LinkedHashSet<String>();

                while ((line = idStream.readLine()) != null) {
                    if (findReplace != null) {
                        line = line.replaceFirst(findReplace[0], findReplace[1]);
                    }

                    idsLookedFor.add(line);
                }

                idStream.close();
                processInput(reader, null, null, idsLookedFor);

            }
        } catch (final EOFException e) {
            // Done Reading input, Be happy
        } catch (final IOException e) {
            // e.printStackTrace();
            // logger.error(e.getMessage());
        }

    }

    private static void processInput(final RawRecordReader reader, final int numToSkip,
            final int numToOutput) throws IOException {
        int num = 0;
        int numOutput = 0;

        while (reader.hasNext()) {
            final RawRecord rec = reader.next();
            num++;

            if (num <= numToSkip) {
                continue;
            }

            if (numToOutput == -1 || numOutput < numToOutput) {
                final byte recordBytes[] = rec.getRecordBytes();

                System.out.write(recordBytes);
                System.out.flush();

                numOutput++;
            }
        }
    }

    static void printIds(final RawRecordReader reader) throws IOException {
        while (reader.hasNext()) {
            final RawRecord rec = reader.next();
            final String id = rec.getRecordId();
            System.out.println(id);
        }
    }

    static void processInput(final RawRecordReader reader, final String idRegex,
            final String recordHas, final HashSet<String> idsLookedFor) throws IOException {
        while (reader.hasNext()) {
            final RawRecord rec = reader.next();
            final String id = rec.getRecordId();
            if (idsLookedFor == null && recordHas == null && id.matches(idRegex) || idsLookedFor != null && idsLookedFor
                    .contains(id)) {
                final byte recordBytes[] = rec.getRecordBytes();
                System.out.write(recordBytes);
                System.out.flush();
            } else if (idsLookedFor == null && idRegex == null && recordHas != null) {
                final String tag = recordHas.substring(0, 3);
                final String field = rec.getFieldVal(tag);
                if (field != null) {
                    final byte recordBytes[] = rec.getRecordBytes();
                    System.out.write(recordBytes);
                    System.out.flush();
                }
            }
        }
    }

}
