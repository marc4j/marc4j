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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

/**
 * Removes local field (tag 9XX).
 * 
 * @author Bas Peters
 */
public class RemoveLocalFieldsExample {

    public static void main(String args[]) throws Exception {

        InputStream input = RemoveLocalFieldsExample.class
                .getResourceAsStream("resources/chabon-loc.mrc");

        MarcReader reader = new MarcStreamReader(input);
        while (reader.hasNext()) {
            Record record = reader.next();
            System.out.println(record.toString());

            Pattern pattern = Pattern.compile("9\\d\\d");

            List fields = record.getDataFields();

            Iterator i = fields.iterator();
            while (i.hasNext()) {
                DataField field = (DataField) i.next();
                Matcher matcher = pattern.matcher(field.getTag());
                if (matcher.matches())
                    i.remove();
            }
            System.out.println(record.toString());
        }

    }

}
