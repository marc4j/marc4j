
package org.marc4j;

import java.io.InputStream;
import java.util.List;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

public class MarcUnprettyXmlReader implements MarcReader {

    private MarcXmlReader reader = null;

    public MarcUnprettyXmlReader(final InputStream input) {
        reader = new MarcXmlReader(input);
    }

    /**
     * Returns true if the iteration has more records, false otherwise.
     * 
     * @return boolean - true if the iteration has more records, false otherwise
     */
    @Override
    public boolean hasNext() {
        return reader.hasNext();
    }

    /**
     * Returns the next record in the iteration.
     * 
     * @return Record - the record object
     */
    @Override
    public Record next() {

        final Record rec = reader.next();
        rec.getLeader().setCharCodingScheme('a');
        final List<?> varFields = rec.getVariableFields();

        for (final Object f : varFields) {
            if (f instanceof ControlField) {
                final ControlField cf = (ControlField) f;
                String data = cf.getData();
                if (data.contains("\n")) {
                    data = data.replaceAll("\\r?\\n[ \t]*", " ");
                    data = data.trim();
                    cf.setData(data);
                }
            } else if (f instanceof DataField) {
                final DataField df = (DataField) f;
                final List<?> subFields = df.getSubfields();
                for (final Object s : subFields) {
                    final Subfield sf = (Subfield) s;
                    String data = sf.getData();
                    if (data.contains("\n")) {
                        data = data.replaceAll("\\r?\\n[ \t]*", " ");
                        data = data.trim();
                        sf.setData(data);
                    }
                }
            }
        }
        return rec;
    }

}
