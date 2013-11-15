package org.solrmarc.marcoverride;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import org.marc4j.Constants;
import org.marc4j.MarcException;
import org.marc4j.MarcStreamWriter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

public class MarcSplitStreamWriter extends MarcStreamWriter
{

    private int recordThreshhold;
    private String fieldsToSplit;
    
    public MarcSplitStreamWriter(OutputStream out, int threshhold, String fieldsToSplit)
    {
        super(out, false);
        recordThreshhold = threshhold;
        this.fieldsToSplit = fieldsToSplit;
    }

    public MarcSplitStreamWriter(OutputStream out, String encoding, int threshhold, String fieldsToSplit)
    {
        super(out, encoding, false);
        recordThreshhold = threshhold;
        this.fieldsToSplit = fieldsToSplit;
        // TODO Auto-generated constructor stub
    }

    /**
     * Writes a <code>Record</code> object to the writer.
     * 
     * @param record -
     *            the <code>Record</code> object
     */
    public void write(Record record) {
        boolean doneWithRec = false;
        List<?> fields = record.getDataFields();
        Iterator<?> i = fields.iterator();
        while (i.hasNext()) 
        {
            DataField df = (DataField) i.next();
            if (!df.getTag().matches(fieldsToSplit)) continue;
            df.setId(null);
        }
        
        while (!doneWithRec)
        {
            try {
                int previous = 0;
                ByteArrayOutputStream data = new ByteArrayOutputStream();
                ByteArrayOutputStream dir = new ByteArrayOutputStream();
                
                // control fields
                fields = record.getControlFields();
                i = fields.iterator();
                while (i.hasNext()) {
                    ControlField cf = (ControlField) i.next();
    
                    data.write(getDataElement(cf.getData()));
                    data.write(Constants.FT);
                    dir.write(getEntry(cf.getTag(), data.size() - previous,
                            previous));
                    previous = data.size();
                }
    
                // data fields
                fields = record.getDataFields();
                i = fields.iterator();
                while (i.hasNext()) {
                    DataField df = (DataField) i.next();
                    if (df.getTag().matches(fieldsToSplit)) 
                    {
                        continue;
                    }
                    data.write(df.getIndicator1());
                    data.write(df.getIndicator2());
                    List subfields = df.getSubfields();
                    Iterator si = subfields.iterator();
                    while (si.hasNext()) {
                        Subfield sf = (Subfield) si.next();
                        data.write(Constants.US);
                        data.write(sf.getCode());
                        data.write(getDataElement(sf.getData()));
                    }
                    data.write(Constants.FT);
                    dir.write(getEntry(df.getTag(), data.size() - previous,
                            previous));
                    previous = data.size();
                }
                // data fields
                fields = record.getDataFields();
                i = fields.iterator();
                while (i.hasNext() && previous < recordThreshhold) {
                    DataField df = (DataField) i.next();
                    if (!df.getTag().matches(fieldsToSplit)) continue;
                    if (!(df.getId()== null || df.getId().intValue() != 0)) continue;
                    df.setId(new Long(0));
                    data.write(df.getIndicator1());
                    data.write(df.getIndicator2());
                    List subfields = df.getSubfields();
                    Iterator si = subfields.iterator();
                    while (si.hasNext()) {
                        Subfield sf = (Subfield) si.next();
                        data.write(Constants.US);
                        data.write(sf.getCode());
                        data.write(getDataElement(sf.getData()));
                    }
                    data.write(Constants.FT);
                    dir.write(getEntry(df.getTag(), data.size() - previous,
                            previous));
                    previous = data.size();
                }
                if (!i.hasNext()) doneWithRec = true;
                dir.write(Constants.FT);
    
                // base address of data and logical record length
                Leader ldr = record.getLeader();
    
                int baseAddress = 24 + dir.size();
                ldr.setBaseAddressOfData(baseAddress);
                int recordLength = ldr.getBaseAddressOfData() + data.size() + 1;
                ldr.setRecordLength(recordLength);
    
                // write record to output stream
                dir.close();
                data.close();
                
                if (!allowOversizeEntry && (hasOversizeLength))
                {
                    throw new MarcException("Record has field that is too long to be a valid MARC binary record. The maximum length for a field counting all of the sub-fields is 9999 bytes.");
                }
                writeLeader(ldr);
                out.write(dir.toByteArray());
                out.write(data.toByteArray());
                out.write(Constants.RT);
    
            } catch (IOException e) {
                throw new MarcException("IO Error occured while writing record", e);
            } catch (MarcException e) {
                throw e;
            }
        }
    }

}
