/**
 * Copyright (C) 2002-2006 Bas Peters
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
package org.marc4j.samples;

import java.io.InputStream;

import org.marc4j.MarcException;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.Record;

/**
 * Reads MARC input.
 * 
 * @author Bas Peters
 */
public class HandleExceptionExample {

    public static void main(String args[]) throws Exception {

        InputStream input = HandleExceptionExample.class
                .getResourceAsStream("resources/error.mrc");

        try {
            MarcReader reader = new MarcStreamReader(input);
            while (reader.hasNext()) {
                Record record = reader.next();
                System.out.println(record.toString());
            }
        } catch (MarcException e) {
            System.out.println("something went wrong man!");

        }

        System.out.println("damn!");
    }

}
