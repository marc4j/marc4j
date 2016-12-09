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
 *
 */

package org.marc4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;

import org.marc4j.marc.Record;

/**
 * <p>
 * A Marc reader which instead of handling a single file of MARC records it
 * handles a directory, which it will scan for all .mrc files, and iterate
 * through all of them in turn.
 * </p>
 * 
 * @author Robert Haschart
 */

public class MarcDirStreamReader implements MarcReader {

    File list[];

    MarcReader curFileReader;

    int curFileNum;

    boolean permissive;

    boolean convertToUTF8;

    String defaultEncoding;

    /**
     * Constructs an instance that traverses the directory specified in the
     * parameter.
     * 
     * @param dirName - The path of the directory from which to read all of the
     *        .mrc files
     */
    public MarcDirStreamReader(final String dirName) {
        final File dir = new File(dirName);
        init(dir, false, false, null);
    }

    /**
     * Constructs an instance that traverses the directory specified in the
     * parameter.
     * 
     * @param dir - The path of the directory from which to read all of the .mrc
     *        files
     */
    public MarcDirStreamReader(final File dir) {
        init(dir, false, false, null);
    }

    /**
     * Constructs an instance that traverses the directory specified in the
     * parameter. Takes the values passed in for permissive and convertToUTF8
     * and passes them on to each of the MarcPermissiveStreamReader that it
     * creates.
     * 
     * @param dirName - The path of the directory from which to read all of the
     *        .mrc files
     * @param permissive - Set to true to specify that reader should try to
     *        handle and recover from errors in the input.
     * @param convertToUTF8 - Set to true to specify that reader should convert
     *        the records being read to UTF-8 encoding as they are being read.
     */
    public MarcDirStreamReader(final String dirName, final boolean permissive,
            final boolean convertToUTF8) {
        final File dir = new File(dirName);
        init(dir, permissive, convertToUTF8, null);
    }

    /**
     * Constructs an instance that traverses the directory specified in the
     * parameter. Takes the values passed in for permissive and convertToUTF8
     * and passes them on to each of the MarcPermissiveStreamReader that it
     * creates.
     * 
     * @param dir - The path of the directory from which to read all of the .mrc
     *        files
     * @param permissive - Set to true to specify that reader should try to
     *        handle and recover from errors in the input.
     * @param convertToUTF8 - Set to true to specify that reader should convert
     *        the records being read to UTF-8 encoding as they are being read.
     */
    public MarcDirStreamReader(final File dir, final boolean permissive, final boolean convertToUTF8) {
        init(dir, permissive, convertToUTF8, null);
    }

    /**
     * Constructs an instance that traverses the directory specified in the
     * parameter. Takes the values passed in for permissive and convertToUTF8
     * and passes them on to each of the MarcPermissiveStreamReader that it
     * creates.
     * 
     * @param dirName - The path of the directory from which to read all of the
     *        .mrc files
     * @param permissive - Set to true to specify that reader should try to
     *        handle and recover from errors in the input.
     * @param convertToUTF8 - Set to true to specify that reader should convert
     *        the records being read to UTF-8 encoding as they are being read.
     * @param defaultEncoding - Specifies the character encoding that the
     *        records being read are presumed to be in..
     */
    public MarcDirStreamReader(final String dirName, final boolean permissive,
            final boolean convertToUTF8, final String defaultEncoding) {
        final File dir = new File(dirName);
        init(dir, permissive, convertToUTF8, defaultEncoding);
    }

    /**
     * Constructs an instance that traverses the directory specified in the
     * parameter. Takes the values passed in for permissive and convertToUTF8
     * and passes them on to each of the MarcPermissiveStreamReader that it
     * creates.
     * 
     * @param dir - The path of the directory from which to read all of the .mrc
     *        files
     * @param permissive - Set to true to specify that reader should try to
     *        handle and recover from errors in the input.
     * @param convertToUTF8 - Set to true to specify that reader should convert
     *        the records being read to UTF-8 encoding as they are being read.
     * @param defaultEncoding - Specifies the character encoding that the
     *        records being read are presumed to be in..
     */
    public MarcDirStreamReader(final File dir, final boolean permissive,
            final boolean convertToUTF8, final String defaultEncoding) {
        init(dir, permissive, convertToUTF8, defaultEncoding);
    }

    private void init(final File dir, final boolean permissive, final boolean convertToUTF8,
            final String defaultEncoding) {
        final FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(final File dir, final String name) {
                return name.endsWith("mrc");
            }
        };
        this.permissive = permissive;
        this.convertToUTF8 = convertToUTF8;
        list = dir.listFiles(filter);
        java.util.Arrays.sort(list);
        curFileNum = 0;
        curFileReader = null;
        this.defaultEncoding = defaultEncoding;
    }

    /**
     * Returns true if the iteration has more records, false otherwise.
     */
    @Override
    public boolean hasNext() {
        if (curFileReader == null || curFileReader.hasNext() == false) {
            nextFile();
        }
        return curFileReader == null ? false : curFileReader.hasNext();
    }

    private void nextFile() {
        if (curFileNum != list.length) {
            try {
                System.err.println("Switching to input file: " + list[curFileNum]);
                if (defaultEncoding != null) {
                    curFileReader = new MarcPermissiveStreamReader(new FileInputStream(
                            list[curFileNum++]), permissive, convertToUTF8, defaultEncoding);
                } else {
                    curFileReader = new MarcPermissiveStreamReader(new FileInputStream(
                            list[curFileNum++]), permissive, convertToUTF8);
                }
            } catch (final FileNotFoundException e) {
                nextFile();
            }
        } else {
            curFileReader = null;
        }
    }

    /**
     * Returns the next record in the iteration.
     * 
     * @return Record - the record object
     */
    @Override
    public Record next() {
        if (curFileReader == null || curFileReader.hasNext() == false) {
            nextFile();
        }
        return curFileReader == null ? null : curFileReader.next();
    }

}
