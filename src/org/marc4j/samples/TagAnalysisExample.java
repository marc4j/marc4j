package org.marc4j.samples;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;

/**
 * Outputs list of used tags.
 * 
 * @author Bas Peters
 */
public class TagAnalysisExample {

    public static void main(String args[]) throws Exception {

        InputStream input = AddLocationExample.class
                .getResourceAsStream("resources/chabon.mrc");

        Hashtable<String,Integer> table = new Hashtable<String,Integer>();

        int counter = 0;

        MarcReader reader = new MarcStreamReader(input);
        while (reader.hasNext()) {
            counter++;

            Record record = reader.next();

            List<VariableField> fields = record.getVariableFields();
            Iterator<VariableField> i = fields.iterator();
            while (i.hasNext()) {
                VariableField field = (VariableField) i.next();
                String tag = field.getTag();
                if (table.containsKey(tag)) {
                    Integer counts = (Integer) table.get(tag);
                    table.put(tag, new Integer(counts.intValue() + 1));
                } else {
                    table.put(tag, new Integer(1));
                }
            }

        }

        System.out.println("Analyzed " + counter + " records");
        System.out.println("Tag\tCount");

        List<String> list = new ArrayList<String>(table.keySet());
        Collections.sort(list);
        Iterator<String> i = list.iterator();
        while (i.hasNext()) {
            String tag = (String) i.next();
            Integer value = (Integer) table.get(tag);
            System.out.println(tag + "\t" + value);
        }

    }
}
