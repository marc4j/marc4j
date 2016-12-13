package org.marc4j;
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.marc4j.MarcException;
import org.marc4j.MarcReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import java.util.List;
import java.util.Properties;

/**
 * 
 * @author Robert Haschart
 * @version $Id: MarcFilteredReader.java 1718 2013-11-08 21:35:12Z rh9ec@virginia.edu $
 *
 */
public class MarcFilteredReader implements MarcReader
{
    //String includeRecordIfFieldPresent = null;
    String[][] includeRecordIfFieldsPresent = null;
    String includeRecordIfFieldContains = null;
//    String includeRecordIfFieldMissing = null;
    String[][] includeRecordIfFieldsMissing = null;
    String includeRecordIfFieldDoesntContain = null;
    String deleteSubfieldsSpec = null;
    Record currentRecord = null;
    MarcReader reader;

    private Properties remapProperties = null;
    
    /**
     * 
     * @param r
     * @param ifFieldPresent
     * @param ifFieldMissing
     */
    public MarcFilteredReader(MarcReader r, String ifFieldPresent, String ifFieldMissing, String deleteSubfields)
    {
        deleteSubfieldsSpec = deleteSubfields;
        if (ifFieldPresent != null)
        {
            String present[] = ifFieldPresent.split("/", 2);
            String tagPlus[] = present[0].split(":");
            includeRecordIfFieldsPresent = new String[tagPlus.length][2];
            for (int i = 0; i < includeRecordIfFieldsPresent.length; i++)
            {
                includeRecordIfFieldsPresent[i][0] = tagPlus[i].substring(0, 3);
                includeRecordIfFieldsPresent[i][1] = tagPlus[i].substring(3);
            }
            if (present.length > 1)
            {
                includeRecordIfFieldContains = present[1];
            }
        }
        if (ifFieldMissing != null)
        {
            String missing[] = ifFieldMissing.split("/", 2);
            String tagPlus[] = missing[0].split(":");
            includeRecordIfFieldsMissing = new String[tagPlus.length][2];
            for (int i = 0; i < includeRecordIfFieldsMissing.length; i++)
            {
                includeRecordIfFieldsMissing[i][0] = tagPlus[i].substring(0, 3);
                includeRecordIfFieldsMissing[i][1] = tagPlus[i].substring(3);
            }
            if (missing.length > 1)
            {
                includeRecordIfFieldDoesntContain = missing[1];
            }
        }
        reader = r;
    }

    public MarcFilteredReader(MarcReader r, String ifFieldPresent, String ifFieldMissing, String deleteSubfields, Properties remapProperties)
    {
        this(r, ifFieldPresent, ifFieldMissing, deleteSubfields);
        this.remapProperties = remapProperties;
    }

    /**
     * Implemented through interface
     * @return Returns true if the iteration has more records, false otherwise
     */
    public boolean hasNext()
    {
        if (currentRecord == null) 
        { 
            currentRecord = next(); 
        }
        return(currentRecord != null);
    }

    /**
     * Returns the next marc file in the iteration
     */
    public Record next()
    {
        
    	if (currentRecord != null) 
        { 
            Record tmp = currentRecord; 
            currentRecord = null; 
            return(tmp);
        }
        
        while (currentRecord == null)
        {
            if (!reader.hasNext()) return(null);
            Record rec = null;
            
            try {
                rec = reader.next();
            }
            catch (MarcException me)
            {
            	throw me;
            }
            if (deleteSubfieldsSpec != null) 
            {
                deleteSubfields(rec);
            }
//            if (remapProperties != null)
//            {
//                boolean keepRecord = remapRecord(rec);
//                if (keepRecord == false) 
//                {
////                    logger.info("Remap Rules say record "+rec.getControlNumber()+" should be skipped");
//                    continue;
//                }
//            }
            if (rec != null && includeRecordIfFieldsPresent != null)
            {
                for (String[] tagAndSf : includeRecordIfFieldsPresent) 
                {
                    List<VariableField> fields = rec.getVariableFields(tagAndSf[0]);
                    for (VariableField vf : fields)
                    {
                        if (vf instanceof ControlField)
                        {
                            if (includeRecordIfFieldContains == null || 
                                ((ControlField)vf).getData().contains(includeRecordIfFieldContains))
                            {
                                currentRecord = rec;
                                break;
                            }
                        }
                        else 
                        {
                            if (includeRecordIfFieldContains == null || 
                                ((DataField)vf).getSubfieldsAsString(tagAndSf[1]).contains(includeRecordIfFieldContains))
                            {
                                currentRecord = rec;
                                break;
                            }
                        }
                    }
                    if (currentRecord != null) break;
                }
            }
           
            if (rec != null && currentRecord == null && includeRecordIfFieldsMissing != null)
            {
                boolean useRecord = true;
                for (String[] tagAndSf : includeRecordIfFieldsMissing) 
                {
                    List<VariableField> fields = rec.getVariableFields(tagAndSf[0]);
                    for (VariableField vf : fields)
                    {
                        if (vf instanceof ControlField)
                        {
                            if (includeRecordIfFieldDoesntContain == null || 
                                ((ControlField)vf).getData().contains(includeRecordIfFieldDoesntContain))
                            {
                                useRecord = false;
                                break;
                            }
                        }
                        else 
                        {
                            if (includeRecordIfFieldDoesntContain == null || 
                                ((DataField)vf).getSubfieldsAsString(tagAndSf[1]).contains(includeRecordIfFieldDoesntContain))
                            {
                                useRecord = false;
                                break;
                            }
                        }
                    }
                    if (useRecord == false) break;
                }
                if (useRecord == true) currentRecord = rec;
                
            }
            if (rec != null && includeRecordIfFieldsPresent == null && includeRecordIfFieldsMissing == null)
            {
                currentRecord = rec;
            }
        }
        return currentRecord ;
    }

    void deleteSubfields(Record rec)
    {
        String fieldSpecs[] = deleteSubfieldsSpec.split(":");
        for (String fieldSpec : fieldSpecs)
        {
            String tag = fieldSpec.substring(0,3);
            String subfield = null;
            if (fieldSpec.length() > 3)  subfield = fieldSpec.substring(3);                    
            List<VariableField> list = (List<VariableField>)rec.getVariableFields(tag);
            for (VariableField field : list)
            {
                if (field instanceof DataField)
                {
                    DataField df = ((DataField)field);
                    if (subfield != null) 
                    {
                        List<Subfield> sfs = (List<Subfield>)df.getSubfields(subfield.charAt(0));
                        if (sfs != null && sfs.size() != 0)
                        {
                            rec.removeVariableField(df);
                            for (Subfield sf : sfs)
                            {
                                df.removeSubfield(sf);
                            }
                            rec.addVariableField(df);
                        }
                    }
                    else
                    {
                        rec.removeVariableField(df);
                    }
                }
            }
        }
    }
    
//    private boolean remapRecord(Record rec)
//    {
//        List<VariableField> fields = rec.getVariableFields();
//        List<VariableField> fToDelete = new ArrayList<VariableField>();
//        List<VariableField> fToInsert = new ArrayList<VariableField>();
//        boolean keepRecord = true;
//        for (VariableField field : fields)
//        {
//            String tag = field.getTag();
//            if (remapProperties.containsKey(tag))
//            {
//                if (Verifier.isControlNumberField(tag)) 
//                {
//                    for (int i = 0; remapProperties.containsKey(tag+"_"+i); i++)
//                    {
//                        String remapString = remapProperties.getProperty(tag+"_"+i);
//                        String mapParts[] = remapString.split("=>");
//                        if (eval(mapParts[0], (ControlField)field, rec))
//                        {
//                            keepRecord &= process(mapParts[1], field, null, fToDelete, fToInsert, rec);
//                        }
//                    }                    
//                }
//                else
//                {
//                  //  List<Subfield> subfields = ((DataField)field).getSubfields();
//                    List<Subfield> sfToDelete = new ArrayList<Subfield>();
//                    for (int i = 0; remapProperties.containsKey(tag+"_"+i); i++)
//                    {
//                        String remapString = remapProperties.getProperty(tag+"_"+i);
//                        String mapParts[] = remapString.split("=>");
//                        if (eval(mapParts[0], (DataField)field, rec))
//                        {
//                            keepRecord &= process(mapParts[1], field, sfToDelete, fToDelete, fToInsert, rec);
//                        }
//                    }
//
//                    if (sfToDelete.size() != 0)
//                    {
//                        for (Subfield sf : sfToDelete)
//                        {
//                            ((DataField)field).removeSubfield(sf);
//                        }
//                    }
//                }
//            }
//            if (!keepRecord) break;
//        }
//        if (keepRecord && remapProperties.containsKey("once"))
//        {
// //           List<Subfield> sfToDelete = new ArrayList<Subfield>();
//            for (int i = 0; remapProperties.containsKey("once_"+i); i++)
//            {
//                String remapString = remapProperties.getProperty("once_"+i);
//                String mapParts[] = remapString.split("=>");
//                if (eval(mapParts[0], null, rec))
//                {
//                    keepRecord &= process(mapParts[1], null, null, fToDelete, fToInsert, rec);
//                }
//            }
//        }
//        if (keepRecord && fToDelete.size() != 0)
//        {
//            for (VariableField field : fToDelete)
//            {
//                rec.removeVariableField(field);
//            }
//        }
//        if (keepRecord && fToInsert.size() != 0)
//        {
//            for (VariableField field : fToInsert)
//            {
//                if (field instanceof DataField)
//                {
//                    int index = 0;
//                    for (DataField df : (List<DataField>)rec.getDataFields())
//                    {
//                        if (df.getTag().compareTo(field.getTag()) >= 0) 
//                            break;
//                        index++;
//                    }
//                    rec.getDataFields().add(index, (DataField)field);
//                }
//                else if (field.getTag().equals("001"))
//                {
//                    rec.addVariableField(field);
//                }
//                else if (field instanceof ControlField)
//                {
//                    int index = 0;
//                    for (ControlField df : (List<ControlField>)rec.getControlFields())
//                    {
//                        if (df.getTag().compareTo(field.getTag()) >= 0) 
//                            break;
//                        index++;
//                    }
//                    rec.getControlFields().add(index, (ControlField)field);
//                }
//            }
//        }
//        return(keepRecord);
//    }
//    
//    private boolean eval(String conditional, VariableField field, Record record)
//    {
//        List<Subfield> subfields;
//        if (conditional.startsWith("true()"))
//        {
//            return(true);
//        }
//        else if (conditional.startsWith("not("))
//        {
//            String arg = getOneConditional(conditional);
//            if (arg != null)
//            {
//                return(!eval(arg, field, record));
//            }
//        }
//        else if (conditional.startsWith("indicatormatches("))
//        {
//            String args[] = getTwoArgs(conditional);
//            if (field != null && field instanceof DataField && args.length == 2 && args[0].length() == 1 && args[1].length() == 1)
//            {
//                char indicator1 = ((DataField)field).getIndicator1();
//                char indicator2 = ((DataField)field).getIndicator2();
//                if ((args[0].charAt(0) == '*' || args[0].charAt(0) == indicator1) && 
//                    (args[1].charAt(0) == '*' || args[1].charAt(0) == indicator2))
//                {
//                    return(true);
//                }
//                return(false);
//            }
//        }
//        else if (conditional.startsWith("subfieldmatches("))
//        {
//            String args[] = getTwoArgs(conditional);
//            if (field != null && field instanceof DataField && args.length == 2 && args[0].length() == 1)
//            {
//                subfields = ((DataField)field).getSubfields(args[0].charAt(0));
//                for (Subfield sf : subfields)
//                {
//                    if (sf.getData().matches(args[1]))
//                        return(true);
//                }
//            }
//            else if (field != null && field instanceof ControlField && args.length == 2)
//            {
//                if (((ControlField)field).getData().matches(args[1])) return(true);
//            }
//        }
//        else if (conditional.startsWith("subfieldcontains("))
//        {
//            String args[] = getTwoArgs(conditional);
//            if (field != null && field instanceof DataField && args.length == 2 && args[0].length() == 1)
//            {
//                subfields = ((DataField)field).getSubfields(args[0].charAt(0));
//                for (Subfield sf : subfields)
//                {
//                    if (sf.getData().contains(args[1]))
//                        return(true);
//                }
//            }
//            else if (field != null && field instanceof ControlField && args.length == 2)
//            {
//                if (((ControlField)field).getData().contains(args[1])) return(true);
//            }
//        }
//        else if (conditional.startsWith("subfieldexists("))
//        {
//            String arg = getOneArg(conditional);
//            if (field != null && field instanceof DataField && arg.length() == 1)
//            {
//                subfields = ((DataField)field).getSubfields(arg.charAt(0));
//                if (subfields.size() > 0) return(true);
//            }
//            else if (field != null && field instanceof ControlField)
//            {
//                return(true);
//            }
//        }
//        else if (conditional.startsWith("and("))
//        {
//            String args[] = getTwoConditionals(conditional);
//            if (args.length == 2)
//            {
//                return(eval(args[0], field, record) && eval(args[1], field, record));
//            }
//        }
//        else if (conditional.startsWith("or("))
//        {
//            String args[] = getTwoConditionals(conditional);
//            if (args.length == 2)
//            {
//                return(eval(args[0], field, record) || eval(args[1], field, record));
//            }
//        }
//        else if (conditional.startsWith("fieldexists("))
//        {
//            String args[] = getThreeArgs(conditional);
//            if (args.length == 3 && args[0].matches("[0-9][0-9][0-9]") && args[1].length() == 1)
//            {
//                for (VariableField vf : (List<VariableField>)record.getVariableFields(args[0]))
//                {
//                    if (vf instanceof DataField)
//                    {
//                        for (Subfield sf : (List<Subfield>)((DataField)vf).getSubfields(args[1].charAt(0)))
//                        {
//                            if (sf.getData().equals(args[2]) || sf.getData().matches(args[2]))  
//                                return(true);
//                        }
//                    }
//                }
//            }
//            return(false);
//        }
//        return false;
//    }
//    
//    private boolean process(String command, VariableField field, List<Subfield> sfToDelete, List<VariableField> fToDelete, List<VariableField> fToInsert, Record record)
//    {
//        List<Subfield> subfields;
//        if (command.startsWith("replace("))
//        {
//            String args[] = getThreeArgs(command);
//            if (field != null && field instanceof DataField && args.length == 3 && args[0].length() == 1)
//            {
//                subfields = ((DataField)field).getSubfields(args[0].charAt(0));
//                for (Subfield sf : subfields)
//                {
//                    String newData = sf.getData().replaceAll(args[1], args[2]);
//                    if (!newData.equals(sf.getData()))
//                    {
//                        sf.setData(newData);
//                    }
//                }
//            }
//            else if (field != null && field instanceof ControlField && args.length == 3)
//            {
//                String newData = ((ControlField)field).getData().replaceAll(args[1], args[2]);
//                if (!newData.equals(((ControlField)field).getData()))
//                {
//                    ((ControlField)field).setData(newData);
//                }
//            }
//        }
//        else if (command.startsWith("append("))
//        {
//            String args[] = getTwoArgs(command);
//            if (field != null && field instanceof DataField && args.length == 2 && args[0].length() == 1)
//            {
//                subfields = ((DataField)field).getSubfields(args[0].charAt(0));
//                for (Subfield sf : subfields)
//                {
//                    String newData = sf.getData() + args[1];
//                    if (!newData.equals(sf.getData()))
//                    {
//                        sf.setData(newData);
//                    }
//                }
//            }
//            else if (field != null && field instanceof ControlField && args.length == 2)
//            {
//                String newData = ((ControlField)field).getData() + args[1];
//                ((ControlField)field).setData(newData);
//            }
//        }
//        else if (command.startsWith("prepend("))
//        {
//            String args[] = getTwoArgs(command);
//            if (field != null && field instanceof DataField && args.length == 2 && args[0].length() == 1)
//            {
//                subfields = ((DataField)field).getSubfields(args[0].charAt(0));
//                for (Subfield sf : subfields)
//                {
//                    String newData = args[1] + sf.getData();
//                    if (!newData.equals(sf.getData()))
//                    {
//                        sf.setData(newData);
//                    }
//                }
//            }
//            else if (field != null && field instanceof ControlField && args.length == 2)
//            {
//                String newData = args[1] + ((ControlField)field).getData();
//                ((ControlField)field).setData(newData);
//            }
//        }
//        else if (command.startsWith("deletesubfield("))
//        {
//            String arg = getOneArg(command);
//            if (field != null && field instanceof DataField && arg.length() == 1)
//            {
//                subfields = ((DataField)field).getSubfields(arg.charAt(0));
//                for (Subfield sf : subfields)
//                {
//                    sfToDelete.add(sf);
//                }
//            }
//            else if (field != null && field instanceof ControlField)
//            {
//                fToDelete.add(field);
//            }
//        }
//        else if (command.startsWith("both("))
//        {
//            String args[] = getTwoConditionals(command);
//            @SuppressWarnings("unused")
//            boolean returncode = true;
//            if (args.length == 2)
//            {
//                returncode = process(args[0], field, sfToDelete, fToDelete, fToInsert, record);
//                returncode &= process(args[1], field, sfToDelete, fToDelete, fToInsert, record);
//            }
//        }
//        else if (command.startsWith("deletefield("))
//        {
//            fToDelete.add(field);
//        }
//        else if (command.startsWith("deleteotherfield("))
//        {
//            String args[] = getThreeArgs(command);
//            if (args.length == 3 && args[0].matches("[0-9][0-9][0-9]") && args[1].length() == 1)
//            {
//                for (VariableField vf : (List<VariableField>)record.getVariableFields(args[0]))
//                {
//                    subfields = ((DataField)vf).getSubfields(args[1].charAt(0));
//                    for (Subfield sf : subfields)
//                    {
//                        if (sf.getData().equals(args[2]) || sf.getData().matches(args[2]))
//                        {
//                            fToDelete.add(vf);
//                        }
//                    }
//                }
//            }
//        }
//        else if (command.startsWith("insertfield("))
//        {
//            String arg = getOneArg(command);
//            VariableField vf = createFieldFromString(arg, null);
//            if (vf != null) fToInsert.add(vf);
//        }
//        else if (command.startsWith("insertparameterizedfield("))
//        {
//            String args[] = getThreeArgs(command);
//            Pattern p = Pattern.compile(args[2]);
//            Matcher m;
//            if (field != null && field instanceof DataField)
//            {
//                m = p.matcher(((DataField)field).getSubfield(args[1].charAt(0)).getData());
//            }
//            else
//            {
//                m = p.matcher(((ControlField)field).getData());
//            }
//            VariableField vf;
//            if (m.matches())
//            {
//                vf = createFieldFromString(args[0], stringsFromMatcher(m));
//            }
//            else 
//            {
//                vf = createFieldFromString(args[0], null);
//            }
//            if (vf != null) fToInsert.add(vf);
//        }
//        else if (command.startsWith("reject()"))
//        {
//            return(false);
//        }
//
//        return(true);
//    }
//    
//    private String[] stringsFromMatcher(Matcher m)
//    {
//        String result[] = new String[m.groupCount()+1];
//        result[0] = m.group(0);
//        for (int i = 0; i < m.groupCount(); i++)
//        {
//            result[i+1] = m.group(i+1);
//        }
//        return result;
//    }
//
//    static Pattern newControlFieldDef = Pattern.compile("=?([0][0][0-9]) [ ]?(.*)");
//    static Pattern newDataFieldDef = Pattern.compile("=?([0-9][0-9][0-9]) [ ]?([0-9 \\|])([0-9 \\|])([$].*)");
//    static Pattern newSubfieldDef = Pattern.compile("[$]([a-z0-9])(([^$]|\\[$]|[$][{][0-9]*[}])*)(.*)");
//    static MarcFactory factory = null;
//    
//    private VariableField createFieldFromString(String arg, String argmatches[])
//    {
//        Matcher mdf = newDataFieldDef.matcher(arg);
//        Matcher cdf = newControlFieldDef.matcher(arg);
//        if (factory == null) factory = MarcFactory.newInstance();
//        if (cdf.matches())  // make a control field
//        {
//            ControlField cf = factory.newControlField(mdf.group(1));
//            String data = cdf.group(2);
//            if (argmatches != null)
//            {
//                data = fillParameters(data, argmatches);
//            }
//            cf.setData(data);
//            return(cf);
//        }
//        else if (mdf.matches())
//        {
//            char ind1 = mdf.group(2).charAt(0);
//            if (ind1 < '0' || ind1 > '9') ind1 = ' ';
//            char ind2 = mdf.group(3).charAt(0);
//            if (ind2 < '0' || ind2 > '9') ind2 = ' ';
//            DataField df = factory.newDataField(mdf.group(1), ind1, ind2);
//            String sfData = mdf.group(4);
//            while (!sfData.isEmpty())
//            {
//                Matcher sm = newSubfieldDef.matcher(sfData);
//                if (sm.matches())
//                {
//                    char code = sm.group(1).charAt(0);
//                    String data = sm.group(2);
//                    if (argmatches != null)
//                    {
//                        data = fillParameters(data, argmatches);
//                    }
//                    sfData = sm.group(4);
//                    Subfield sf = factory.newSubfield(code, data);
//                    df.addSubfield(sf);
//                }
//            }
//            return(df);
//        }
//        return null;
//    }
//
//    private String fillParameters(String data, String argmatches[])
//    {
//        for (int i = 0; i < argmatches.length; i++)
//        {
//            if (data.contains("${"+(i+1)+"}"))
//            {
//                data = data.replaceAll("[$][{]"+(i+1)+"[}]", argmatches[i+1]);
//            }
//        }
//        return data;
//    }
//
//    static Pattern oneArg = Pattern.compile("[a-z]*[(]\"((\\\"|[^\"])*)\"[ ]*[)]");
//    private String getOneArg(String conditional)
//    {
//        Matcher m = oneArg.matcher(conditional.trim());
//        if (m.matches())
//        {
//            return(m.group(1).replaceAll("\\\"", "\""));
//        }
//        return null;
//    }
//    
//    static Pattern twoArgs = Pattern.compile("[a-z]*[(]\"((\\\"|[^\"])*)\",[ ]*\"((\\\"|[^\"])*)\"[)]");
//    private String[] getTwoArgs(String conditional)
//    {
//        Matcher m = twoArgs.matcher(conditional.trim());
//        if (m.matches())
//        {
//            String result[] = new String[]{m.group(1).replaceAll("\\\"", "\""), m.group(3).replaceAll("\\\"", "\"")};
//            return(result);
//        }
//        return null;
//    }
//    
//    static Pattern threeArgs = Pattern.compile("[a-z]*[(][ ]*\"((\\\"|[^\"])*)\",[ ]*\"((\\\"|[^\"])*)\",[ ]*\"((\\\"|[^\"])*)\"[)]");
//    private String[] getThreeArgs(String conditional)
//    {
//        Matcher m = threeArgs.matcher(conditional.trim());
//        if (m.matches())
//        {
//            String result[] = new String[]{m.group(1).replaceAll("\\\"", "\""), m.group(3).replaceAll("\\\"", "\""), m.group(5).replaceAll("\\\"", "\"")};
//            return(result);
//        }
//        return null;
//    }
//    
//    static Pattern twoConditionals = Pattern.compile("[a-z]*[(]([a-z]*[(].*[)]),[ ]*([a-z]*[(].*[)])[)]");
//    private String[] getTwoConditionals(String conditional)
//    {
//        Matcher m = twoConditionals.matcher(conditional.trim());
//        if (m.matches())
//        {
//            String result[] = new String[]{m.group(1), m.group(2)};
//            return(result);
//        }
//        return null;
//    }
//    
//    
//    static Pattern oneConditional = Pattern.compile("[a-z]*[(]([a-z]*[(].*[)])[)]");
//    private String getOneConditional(String conditional)
//    {
//        Matcher m = oneConditional.matcher(conditional.trim());
//        if (m.matches())
//        {
//            String result = m.group(1);
//            return(result);
//        }
//        return null;
//    }
//    
//    static Pattern argAndConditional = Pattern.compile("[a-z]*[(][ ]*\"((\\\"|[^\"])*)\",[ ]*([a-z]*[(].*[)])[)]");
//    private String[] getArgAndConditional(String conditional)
//    {
//        Matcher m = argAndConditional.matcher(conditional.trim());
//        if (m.matches())
//        {
//            String result[] = new String[]{m.group(1), m.group(2)};
//            return(result);
//        }
//        return null;
//    }

}
