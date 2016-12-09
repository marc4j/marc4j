
package org.marc4j.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.marc4j.Constants;
import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;

//import org.marc4j.MarcReader;

public class RawRecord {

    String id;

    byte rawRecordData[];

    byte leader[] = null;

    /**
     * Creates a RawRecord from the supplied {@link DataInputStream}.
     *
     * @param ds - a DataStreamObject to read data from.
     */
    public RawRecord(final DataInputStream ds) {
        init(ds);

        if (rawRecordData != null) {
            id = getRecordId();
        }
    }

    private void init(final DataInputStream ds) {
        id = null;
        ds.mark(24);

        if (leader == null) {
            leader = new byte[24];
        }

        try {
            ds.readFully(leader);
            int length = parseRecordLength(leader);
            ds.reset();
            ds.mark(length * 2);
            rawRecordData = new byte[length];

            try {
                ds.readFully(rawRecordData);
            } catch (final EOFException e) {
                ds.reset();
                int c;
                int cnt = 0;

                while ((c = ds.read()) != -1) {
                    rawRecordData[cnt++] = (byte) c;
                }

                final int location = byteArrayContains(rawRecordData, Constants.RT);

                if (location != -1) {
                    length = location + 1;
                } else {
                    throw (e);
                }
            }

            if (rawRecordData[length - 1] != Constants.RT) {
                final int location = byteArrayContains(rawRecordData, Constants.RT);

                // Specified length was longer that actual length
                if (location != -1) {
                    ds.reset();
                    rawRecordData = new byte[location];
                    ds.readFully(rawRecordData);
                } else {
                    // keep reading until end of record found
                    final ArrayList<Byte> recBuf = new ArrayList<Byte>();
                    ds.reset();

                    final byte byteRead[] = new byte[1];

                    while (true) {
                        final int numRead = ds.read(byteRead);

                        if (numRead == -1) {
                            break; // probably should throw something here.
                        }
                        recBuf.add(byteRead[0]);

                        if (byteRead[0] == Constants.RT) {
                            break;
                        }
                    }

                    rawRecordData = new byte[recBuf.size()];

                    for (int i = 0; i < recBuf.size(); i++) {
                        rawRecordData[i] = recBuf.get(i);
                    }
                }
            }
        } catch (final IOException e) {
            try {
                rawRecordData = null;
                ds.reset();
            } catch (final IOException e1) {
            }
        }

    }

    private static int byteArrayContains(final byte data[], final int value) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] == value) {
                return (i);
            }
        }
        return (-1);
    }

    /**
     * Creates a new raw record from the two supplied raw records.
     *
     * @param rec1 - the first RawRecord to combine 
     * @param rec2 - the second RawRecord to combine
     */
    public RawRecord(final RawRecord rec1, final RawRecord rec2) {

        rawRecordData = new byte[rec1.getRecordBytes().length + rec2.getRecordBytes().length];
        System.arraycopy(rec1.getRecordBytes(), 0, rawRecordData, 0, rec1.getRecordBytes().length);
        System.arraycopy(rec2.getRecordBytes(), 0, rawRecordData, rec1.getRecordBytes().length,
                rec2.getRecordBytes().length);
        id = getRecordId();
    }

    /**
     * Gets the record ID.
     *
     * @return The record ID
     */
    public String getRecordId() {
        if (id != null) {
            return (id);
        }
        id = getFieldVal("001");
        return (id);
    }

    /**
     *  A shortcut method for getting a single field value from a RawRecord.
     *  If multiple fields with that tag exist this will return the first one encountered.
     *
     * @param idField - the tag of the field to extract from the RawRecord data.
     * @return The value of the field with the supplied ID
     */
    public String getFieldVal(final String idField) {
        String recordStr = null;

        try {
            recordStr = new String(rawRecordData, "ISO-8859-1");
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        int offset = Integer.parseInt(recordStr.substring(12, 17));
        if (offset == 99999 || recordStr.charAt(offset - 1) != Constants.FT) {
            offset = recordStr.indexOf(Constants.FT) + 1;
        }
        int dirOffset = 24;
        String fieldNum = recordStr.substring(dirOffset, dirOffset + 3);

        while (dirOffset < offset) {
            if (fieldNum.equals(idField)) {
                final int length = Integer.parseInt(recordStr.substring(dirOffset + 3,
                        dirOffset + 7));
                final int offset2 = Integer.parseInt(recordStr.substring(dirOffset + 7,
                        dirOffset + 12));
                final String id = recordStr.substring(offset + offset2,
                        offset + offset2 + length - 1).trim();
                return id;
            }

            dirOffset += 12;
            fieldNum = recordStr.substring(dirOffset, dirOffset + 3);
        }

        return null;
    }

    /**
     * Gets the record in byte form.
     *
     * @return the record in byte form.
     */
    public byte[] getRecordBytes() {
        return rawRecordData;
    }

    /**
     * Gets the raw record as a {@link Record}.
     *
     * @param permissive - true to enable permissive error handling
     * @param toUtf8 - true to specify should be converted to UTF-8 
     * @param combinePartials - a list of field tags that should be copied from the 2nd 
     *        (and 3rd etc.) RawRecord in the rawRecordData member to the Record being created.
     * @param defaultEncoding - the expected encoding that should be found in the rawRecordData
     * @return a {@link Record} built from the current record data
     */
    public Record getAsRecord(final boolean permissive, final boolean toUtf8,
            final String combinePartials, final String defaultEncoding) {
        final ByteArrayInputStream bais = new ByteArrayInputStream(rawRecordData);
        final MarcPermissiveStreamReader reader = new MarcPermissiveStreamReader(bais, permissive,
                toUtf8, defaultEncoding);
        final Record next = reader.next();
        if (combinePartials != null) {
            while (reader.hasNext()) {
                final Record nextNext = reader.next();
                final List<VariableField> fieldsAll = nextNext.getVariableFields();
                final Iterator<VariableField> fieldIter = fieldsAll.iterator();
                while (fieldIter.hasNext()) {
                    final VariableField vf = fieldIter.next();
                    if (combinePartials.contains(vf.getTag())) {
                        next.addVariableField(vf);
                    }
                }
            }
        }
        return (next);
    }

    private static int parseRecordLength(final byte[] leaderData) throws IOException {
        final InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(leaderData));
        int length = -1;
        final char[] tmp = new char[5];
        isr.read(tmp);
        try {
            length = Integer.parseInt(new String(tmp));
        } catch (final NumberFormatException e) {
            throw new IOException("unable to parse record length");
        }
        return (length);
    }

    // public boolean hasNext()
    // {
    // // TODO Auto-generated method stub
    // return false;
    // }
    //
    // public Record next()
    // {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    //

}
