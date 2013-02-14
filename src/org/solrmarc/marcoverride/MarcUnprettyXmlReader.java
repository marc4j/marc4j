package org.solrmarc.marcoverride;

import java.io.InputStream;
import java.util.List;

import org.marc4j.MarcReader;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;


public class MarcUnprettyXmlReader implements MarcReader
{
    private MarcXmlReader reader = null;
    
    public MarcUnprettyXmlReader(InputStream input) 
    {
        reader = new MarcXmlReader(input);
    }
    
    public boolean hasNext()
    {
        return (reader.hasNext());
    }

    public Record next()
    {
        Record rec = reader.next();
        rec.getLeader().setCharCodingScheme('a');
        List<?> varFields = rec.getVariableFields();
        for (Object f : varFields)
        {
            if (f instanceof ControlField)
            {
                ControlField cf = (ControlField)f;
                String data = cf.getData();
                if (data.contains("\n"))
                {
                    data = data.replaceAll("\\r?\\n[ \t]*", " ");
                    data = data.trim();
                    cf.setData(data);
                }
            }
            else if (f instanceof DataField)
            {
                DataField df = (DataField)f;
                List<?> subFields = df.getSubfields();
                for (Object s : subFields)
                {
                    Subfield sf = (Subfield)s;
                    String data = sf.getData();
                    if (data.contains("\n"))
                    {
                        data = data.replaceAll("\\r?\\n[ \t]*", " ");
                        data = data.trim();
                        sf.setData(data);
                    }
                }
            }
        }
        return(rec);
    }

}
