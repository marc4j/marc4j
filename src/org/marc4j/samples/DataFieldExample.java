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
import java.util.Iterator;
import java.util.List;

import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

/**
 * Read data fields.
 * 
 * @author Bas Peters
 */
public class DataFieldExample {

    public static void main(String args[]) throws Exception {

        InputStream input = DataFieldExample.class
                .getResourceAsStream("resources/summerland.mrc");

        MarcReader reader = new MarcStreamReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();

            // get the first field occurence for a given tag
            DataField dataField = (DataField) record.getVariableField("245");
            System.out.println(dataField.toString() + '\n');

            // get all occurences for a particular tag
            List dataFields = record.getVariableFields("650");
            Iterator i = dataFields.iterator();
            while (i.hasNext()) {
                dataField = (DataField) i.next();
                System.out.println(dataField.toString());
            }
            System.out.print('\n');

            // get all occurences for a given list of tags
            String[] tags = { "010", "100", "245", "250", "260", "300" };
            dataFields = record.getVariableFields(tags);
            i = dataFields.iterator();
            while (i.hasNext()) {
                dataField = (DataField) i.next();
                System.out.println(dataField.toString());
            }
            System.out.print('\n');

            // read indicators and subfields
            dataField = (DataField) record.getVariableField("245");
            String tag = dataField.getTag();
            char ind1 = dataField.getIndicator1();
            char ind2 = dataField.getIndicator2();

            System.out.println("Tag: " + tag + " Indicator 1: " + ind1
                    + " Indicator 2: " + ind2);

            List subfields = dataField.getSubfields();
            i = subfields.iterator();

            while (i.hasNext()) {
                Subfield subfield = (Subfield) i.next();
                char code = subfield.getCode();
                String data = subfield.getData();

                System.out.println("Subfield code: " + code + " Data element: "
                        + data);
            }
            System.out.print('\n');

            // retrieve the first occurrence of subfield with code 'a'
            Subfield subfield = dataField.getSubfield('a');
            System.out.println("Title proper: " + subfield.getData());

        }

    }

}
