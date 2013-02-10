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
import java.util.List;

import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

/**
 * Demostrates the use of the find method.
 * 
 * @author Bas Peters
 */
public class CheckAgencyExample {

    public static void main(String args[]) throws Exception {

        InputStream input = ReadMarcExample.class
                .getResourceAsStream("resources/summerland.mrc");

        MarcReader reader = new MarcStreamReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();

            // check if the cataloging agency is DLC
            List result = record.find("040", "DLC");
            if (result.size() > 0)
                System.out.println("Agency for this record is DLC");

            // there is no specific find for a specific subfield
            // so to check if it is the orignal cataloging agency
            DataField field = (DataField) result.get(0);
            String agency = field.getSubfield('a').getData();
            if (agency.matches("DLC"))
                System.out.println("DLC is the original agency");
        }
    }
}
