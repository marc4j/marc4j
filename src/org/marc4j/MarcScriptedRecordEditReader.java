
package org.marc4j;

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
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.Verifier;

/**
 * This class provides a capability of applying scripted edits to {@link Record} objects as
 * they are being read.  You can insert fields or subfields, modify the data in a field, 
 * delete unwanted fields or subfields, or even determine that a given record ought to be skipped.
 * 
 * This code in this class was a part of the MarcFilteredReader class from the SolrMarc project. 
 * That class has been ported to marc4j and has been split into two separate classes.  
 *  
 * @author Robert Haschart
 */
public class MarcScriptedRecordEditReader implements MarcReader {

    Record currentRecord = null;

    final private MarcReader reader;

    final private Properties remapProperties;

    final private String deleteSubfieldsSpec;

    /**
     * 
     * @param reader - The MarcReader to extract records from for Editing. 
     * @param deleteSubfieldStr - A specification of what fields/subfields to delete from the records. 
     * @param remapProperties - The specification of the record edits to perform. 
     */
    public MarcScriptedRecordEditReader(final MarcReader reader, final String deleteSubfieldStr, final Properties remapProperties) {
        this.reader = reader;
        this.deleteSubfieldsSpec = deleteSubfieldStr;
        this.remapProperties = remapProperties;
    }

    /**
     * 
     * @param reader - The MarcReader to extract records from for Editing. 
     * @param deleteSubfieldStr - A specification of what fields/subfields to delete from the records. 
     */
    public MarcScriptedRecordEditReader(final MarcReader reader, final String deleteSubfieldStr) {
        this.reader = reader;
        this.deleteSubfieldsSpec = deleteSubfieldStr;
        this.remapProperties = null;
    }

    /**
     * 
     * @param reader - The MarcReader to extract records from for Editing. 
     * @param remapProperties - The specification of the record edits to perform. 
     */
    public MarcScriptedRecordEditReader(final MarcReader reader, final Properties remapProperties) {
        this.reader = reader;
        this.deleteSubfieldsSpec = null;
        this.remapProperties = remapProperties;
    }

    /**
     * Implemented through interface
     * @return Returns true if the iteration has more records, false otherwise
     */
    @Override
    public boolean hasNext() {
        if (currentRecord == null) {
            currentRecord = next();
        }
        return currentRecord != null;
    }

    /**
     * Returns the next marc file in the iteration
     * @return the next MARC record after editing according to the specification(s)
     */
    @Override
    public Record next() {

        if (currentRecord != null) {
            final Record tmp = currentRecord;
            currentRecord = null;
            return tmp;
        }

        while (currentRecord == null) {
            if (!reader.hasNext()) {
                return null;
            }
            Record rec = null;

            try {
                rec = reader.next();
            } catch (final MarcException me) {
                throw me;
            }
            if (deleteSubfieldsSpec != null) {
                deleteSubfields(rec);
            }
            if (remapProperties != null) {
                final boolean keepRecord = remapRecord(rec);
                if (keepRecord == false) {
                    //
                    // logger.info("Remap Rules say record "+rec.getControlNumber()+" should be skipped");
                    continue;
                }
            }
            currentRecord = rec;
        }
        return currentRecord;
    }

    void deleteSubfields(final Record rec) {
        final String fieldSpecs[] = deleteSubfieldsSpec.split(":");
        for (final String fieldSpec : fieldSpecs) {
            final String tag = fieldSpec.substring(0, 3);
            String subfield = null;
            if (fieldSpec.length() > 3) {
                subfield = fieldSpec.substring(3);
            }
            final List<VariableField> list = rec.getVariableFields(tag);
            for (final VariableField field : list) {
                if (field instanceof DataField) {
                    final DataField df = (DataField) field;
                    if (subfield != null) {
                        final List<Subfield> sfs = df.getSubfields(subfield.charAt(0));
                        if (sfs != null && sfs.size() != 0) {
                            rec.removeVariableField(df);
                            for (final Subfield sf : sfs) {
                                df.removeSubfield(sf);
                            }
                            rec.addVariableField(df);
                        }
                    } else {
                        rec.removeVariableField(df);
                    }
                }
            }
        }
    }

    private boolean remapRecord(final Record rec) {
        final List<VariableField> fields = rec.getVariableFields();
        final List<VariableField> fToDelete = new ArrayList<VariableField>();
        final List<VariableField> fToInsert = new ArrayList<VariableField>();
        boolean keepRecord = true;
        for (final VariableField field : fields) {
            final String tag = field.getTag();
            String tagPlus0 = tag + "_0";
            if (remapProperties.containsKey(tagPlus0)) {
                if (Verifier.isControlNumberField(tag)) {
                    for (int i = 0; remapProperties.containsKey(tag + "_" + i); i++) {
                        final String remapString = remapProperties.getProperty(tag + "_" + i);
                        final String mapParts[] = remapString.split("=>");
                        if (eval(mapParts[0], field, rec)) {
                            keepRecord &= process(mapParts[1], field, null, fToDelete, fToInsert,
                                    rec);
                        }
                    }
                } else {
                    // List<Subfield> subfields =
                    // ((DataField)field).getSubfields();
                    final List<Subfield> sfToDelete = new ArrayList<Subfield>();
                    for (int i = 0; remapProperties.containsKey(tag + "_" + i); i++) {
                        final String remapString = remapProperties.getProperty(tag + "_" + i);
                        final String mapParts[] = remapString.split("=>");
                        if (eval(mapParts[0], field, rec)) {
                            keepRecord &= process(mapParts[1], field, sfToDelete, fToDelete,
                                    fToInsert, rec);
                        }
                    }

                    if (sfToDelete.size() != 0) {
                        for (final Subfield sf : sfToDelete) {
                            ((DataField) field).removeSubfield(sf);
                        }
                    }
                }
            }
            if (!keepRecord) {
                break;
            }
        }
        String tagPlus0 = "once_0";
        if (keepRecord && remapProperties.containsKey(tagPlus0)) {
            // List<Subfield> sfToDelete = new ArrayList<Subfield>();
            for (int i = 0; remapProperties.containsKey("once_" + i); i++) {
                final String remapString = remapProperties.getProperty("once_" + i);
                final String mapParts[] = remapString.split("=>");
                if (eval(mapParts[0], null, rec)) {
                    keepRecord &= process(mapParts[1], null, null, fToDelete, fToInsert, rec);
                }
            }
        }
        if (keepRecord && fToDelete.size() != 0) {
            for (final VariableField field : fToDelete) {
                rec.removeVariableField(field);
            }
        }
        if (keepRecord && fToInsert.size() != 0) {
            for (final VariableField field : fToInsert) {
                if (field instanceof DataField) {
                    int index = 0;
                    for (final DataField df : rec.getDataFields()) {
                        if (df.getTag().compareTo(field.getTag()) >= 0) {
                            break;
                        }
                        index++;
                    }
                    rec.getDataFields().add(index, (DataField) field);
                } else if (field.getTag().equals("001")) {
                    rec.addVariableField(field);
                } else if (field instanceof ControlField) {
                    int index = 0;
                    for (final ControlField df : rec.getControlFields()) {
                        if (df.getTag().compareTo(field.getTag()) >= 0) {
                            break;
                        }
                        index++;
                    }
                    rec.getControlFields().add(index, (ControlField) field);
                }
            }
        }
        return keepRecord;
    }

    private boolean eval(final String conditional, final VariableField field, final Record record) {
        List<Subfield> subfields;
        if (conditional.startsWith("true()")) {
            return true;
        } else if (conditional.startsWith("not(")) {
            final String arg = getOneConditional(conditional);
            if (arg != null) {
                return !eval(arg, field, record);
            }
        } else if (conditional.startsWith("indicatormatches(")) {
            final String args[] = getTwoArgs(conditional);
            if (field != null && field instanceof DataField && args.length == 2 && args[0].length() == 1 && args[1]
                    .length() == 1) {
                final char indicator1 = ((DataField) field).getIndicator1();
                final char indicator2 = ((DataField) field).getIndicator2();
                if ((args[0].charAt(0) == '*' || args[0].charAt(0) == indicator1) && (args[1]
                        .charAt(0) == '*' || args[1].charAt(0) == indicator2)) {
                    return true;
                }
                return false;
            }
        } else if (conditional.startsWith("subfieldmatches(")) {
            final String args[] = getTwoArgs(conditional);
            if (field != null && field instanceof DataField && args.length == 2 && args[0].length() == 1) {
                subfields = ((DataField) field).getSubfields(args[0].charAt(0));
                for (final Subfield sf : subfields) {
                    if (sf.getData().matches(args[1])) {
                        return true;
                    }
                }
            } else if (field != null && field instanceof ControlField && args.length == 2) {
                if (((ControlField) field).getData().matches(args[1])) {
                    return true;
                }
            }
        } else if (conditional.startsWith("subfieldcontains(")) {
            final String args[] = getTwoArgs(conditional);
            if (field != null && field instanceof DataField && args.length == 2 && args[0].length() == 1) {
                subfields = ((DataField) field).getSubfields(args[0].charAt(0));
                for (final Subfield sf : subfields) {
                    if (sf.getData().contains(args[1])) {
                        return true;
                    }
                }
            } else if (field != null && field instanceof ControlField && args.length == 2) {
                if (((ControlField) field).getData().contains(args[1])) {
                    return true;
                }
            }
        } else if (conditional.startsWith("subfieldexists(")) {
            final String arg = getOneArg(conditional);
            if (field != null && field instanceof DataField && arg.length() == 1) {
                subfields = ((DataField) field).getSubfields(arg.charAt(0));
                if (subfields.size() > 0) {
                    return true;
                }
            } else if (field != null && field instanceof ControlField) {
                return true;
            }
        } else if (conditional.startsWith("and(")) {
            final String args[] = getTwoConditionals(conditional);
            if (args.length == 2) {
                return eval(args[0], field, record) && eval(args[1], field, record);
            }
        } else if (conditional.startsWith("or(")) {
            final String args[] = getTwoConditionals(conditional);
            if (args.length == 2) {
                return eval(args[0], field, record) || eval(args[1], field, record);
            }
        } else if (conditional.startsWith("fieldexists(")) {
            final String args[] = getThreeArgs(conditional);
            if (args.length == 3 && args[0].matches("[0-9][0-9][0-9]") && args[1].length() == 1) {
                for (final VariableField vf : record.getVariableFields(args[0])) {
                    if (vf instanceof DataField) {
                        for (final Subfield sf : ((DataField) vf).getSubfields(args[1].charAt(0))) {
                            if (sf.getData().equals(args[2]) || sf.getData().matches(args[2])) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }
        return false;
    }

    private boolean process(final String command, final VariableField field,
            final List<Subfield> sfToDelete, final List<VariableField> fToDelete,
            final List<VariableField> fToInsert, final Record record) {
        List<Subfield> subfields;
        if (command.startsWith("replace(")) {
            final String args[] = getThreeArgs(command);
            if (field != null && field instanceof DataField && args.length == 3 && args[0].length() == 1) {
                subfields = ((DataField) field).getSubfields(args[0].charAt(0));
                for (final Subfield sf : subfields) {
                    final String newData = sf.getData().replaceAll(args[1], args[2]);
                    if (!newData.equals(sf.getData())) {
                        sf.setData(newData);
                    }
                }
            } else if (field != null && field instanceof ControlField && args.length == 3) {
                final String newData = ((ControlField) field).getData()
                        .replaceAll(args[1], args[2]);
                if (!newData.equals(((ControlField) field).getData())) {
                    ((ControlField) field).setData(newData);
                }
            }
        } else if (command.startsWith("append(")) {
            final String args[] = getTwoArgs(command);
            if (field != null && field instanceof DataField && args.length == 2 && args[0].length() == 1) {
                subfields = ((DataField) field).getSubfields(args[0].charAt(0));
                for (final Subfield sf : subfields) {
                    final String newData = sf.getData() + args[1];
                    if (!newData.equals(sf.getData())) {
                        sf.setData(newData);
                    }
                }
            } else if (field != null && field instanceof ControlField && args.length == 2) {
                final String newData = ((ControlField) field).getData() + args[1];
                ((ControlField) field).setData(newData);
            }
        } else if (command.startsWith("prepend(")) {
            final String args[] = getTwoArgs(command);
            if (field != null && field instanceof DataField && args.length == 2 && args[0].length() == 1) {
                subfields = ((DataField) field).getSubfields(args[0].charAt(0));
                for (final Subfield sf : subfields) {
                    final String newData = args[1] + sf.getData();
                    if (!newData.equals(sf.getData())) {
                        sf.setData(newData);
                    }
                }
            } else if (field != null && field instanceof ControlField && args.length == 2) {
                final String newData = args[1] + ((ControlField) field).getData();
                ((ControlField) field).setData(newData);
            }
        } else if (command.startsWith("deletesubfield(")) {
            final String arg = getOneArg(command);
            if (field != null && field instanceof DataField && arg.length() == 1) {
                subfields = ((DataField) field).getSubfields(arg.charAt(0));
                for (final Subfield sf : subfields) {
                    sfToDelete.add(sf);
                }
            } else if (field != null && field instanceof ControlField) {
                fToDelete.add(field);
            }
        } else if (command.startsWith("both(")) {
            final String args[] = getTwoConditionals(command);
            @SuppressWarnings("unused")
            boolean returncode = true;
            if (args.length == 2) {
                returncode = process(args[0], field, sfToDelete, fToDelete, fToInsert, record);
                returncode &= process(args[1], field, sfToDelete, fToDelete, fToInsert, record);
            }
        } else if (command.startsWith("deletefield(")) {
            fToDelete.add(field);
        } else if (command.startsWith("deleteotherfield(")) {
            final String args[] = getThreeArgs(command);
            if (args.length == 3 && args[0].matches("[0-9][0-9][0-9]") && args[1].length() == 1) {
                for (final VariableField vf : record.getVariableFields(args[0])) {
                    subfields = ((DataField) vf).getSubfields(args[1].charAt(0));
                    for (final Subfield sf : subfields) {
                        if (sf.getData().equals(args[2]) || sf.getData().matches(args[2])) {
                            fToDelete.add(vf);
                        }
                    }
                }
            }
        } else if (command.startsWith("insertfield(")) {
            final String arg = getOneArg(command);
            final VariableField vf = createFieldFromString(arg, null);
            if (vf != null) {
                fToInsert.add(vf);
            }
        } else if (command.startsWith("insertparameterizedfield(")) {
            final String args[] = getThreeArgs(command);
            final Pattern p = Pattern.compile(args[2]);
            Matcher m;
            if (field != null && field instanceof DataField) {
                m = p.matcher(((DataField) field).getSubfield(args[1].charAt(0)).getData());
            } else {
                m = p.matcher(((ControlField) field).getData());
            }
            VariableField vf;
            if (m.matches()) {
                vf = createFieldFromString(args[0], stringsFromMatcher(m));
            } else {
                vf = createFieldFromString(args[0], null);
            }
            if (vf != null) {
                fToInsert.add(vf);
            }
        } else if (command.startsWith("reject()")) {
            return false;
        }

        return true;
    }

    private String[] stringsFromMatcher(final Matcher m) {
        final String result[] = new String[m.groupCount() + 1];
        result[0] = m.group(0);
        for (int i = 0; i < m.groupCount(); i++) {
            result[i + 1] = m.group(i + 1);
        }
        return result;
    }

    static Pattern newControlFieldDef = Pattern.compile("=?([0][0][0-9]) [ ]?(.*)");

    static Pattern newDataFieldDef = Pattern
            .compile("=?([0-9][0-9][0-9]) [ ]?([0-9 \\|])([0-9 \\|])([$].*)");

    static Pattern newSubfieldDef = Pattern
            .compile("[$]([a-z0-9])(([^$]|\\[$]|[$][{][0-9]*[}])*)(.*)");

    static MarcFactory factory = null;

    private VariableField createFieldFromString(final String arg, final String argmatches[]) {
        final Matcher mdf = newDataFieldDef.matcher(arg);
        final Matcher cdf = newControlFieldDef.matcher(arg);
        if (factory == null) {
            factory = MarcFactory.newInstance();
        }
        if (cdf.matches()) // make a control field
        {
            final ControlField cf = factory.newControlField(mdf.group(1));
            String data = cdf.group(2);
            if (argmatches != null) {
                data = fillParameters(data, argmatches);
            }
            cf.setData(data);
            return cf;
        } else if (mdf.matches()) {
            char ind1 = mdf.group(2).charAt(0);
            if (ind1 < '0' || ind1 > '9') {
                ind1 = ' ';
            }
            char ind2 = mdf.group(3).charAt(0);
            if (ind2 < '0' || ind2 > '9') {
                ind2 = ' ';
            }
            final DataField df = factory.newDataField(mdf.group(1), ind1, ind2);
            String sfData = mdf.group(4);
            while (!sfData.isEmpty()) {
                final Matcher sm = newSubfieldDef.matcher(sfData);
                if (sm.matches()) {
                    final char code = sm.group(1).charAt(0);
                    String data = sm.group(2);
                    if (argmatches != null) {
                        data = fillParameters(data, argmatches);
                    }
                    sfData = sm.group(4);
                    final Subfield sf = factory.newSubfield(code, data);
                    df.addSubfield(sf);
                }
            }
            return df;
        }
        return null;
    }

    private String fillParameters(String data, final String argmatches[]) {
        for (int i = 0; i < argmatches.length; i++) {
            if (data.contains("${" + (i + 1) + "}")) {
                data = data.replaceAll("[$][{]" + (i + 1) + "[}]", argmatches[i + 1]);
            }
        }
        return data;
    }

    static Pattern oneArg = Pattern.compile("[a-z]*[(]\"((\\\"|[^\"])*)\"[ ]*[)]");

    private String getOneArg(final String conditional) {
        final Matcher m = oneArg.matcher(conditional.trim());
        if (m.matches()) {
            return m.group(1).replaceAll("\\\"", "\"");
        }
        return null;
    }

    static Pattern twoArgs = Pattern
            .compile("[a-z]*[(]\"((\\\"|[^\"])*)\",[ ]*\"((\\\"|[^\"])*)\"[)]");

    private String[] getTwoArgs(final String conditional) {
        final Matcher m = twoArgs.matcher(conditional.trim());
        if (m.matches()) {
            final String result[] = new String[] { m.group(1).replaceAll("\\\"", "\""),
                    m.group(3).replaceAll("\\\"", "\"") };
            return result;
        }
        return null;
    }

    static Pattern threeArgs = Pattern
            .compile("[a-z]*[(][ ]*\"((\\\"|[^\"])*)\",[ ]*\"((\\\"|[^\"])*)\",[ ]*\"((\\\"|[^\"])*)\"[)]");

    private String[] getThreeArgs(final String conditional) {
        final Matcher m = threeArgs.matcher(conditional.trim());
        if (m.matches()) {
            final String result[] = new String[] { m.group(1).replaceAll("\\\"", "\""),
                    m.group(3).replaceAll("\\\"", "\""), m.group(5).replaceAll("\\\"", "\"") };
            return result;
        }
        return null;
    }

    static Pattern twoConditionals = Pattern
            .compile("[a-z]*[(]([a-z]*[(].*[)]),[ ]*([a-z]*[(].*[)])[)]");

    private String[] getTwoConditionals(final String conditional) {
        final Matcher m = twoConditionals.matcher(conditional.trim());
        if (m.matches()) {
            final String result[] = new String[] { m.group(1), m.group(2) };
            return result;
        }
        return null;
    }

    static Pattern oneConditional = Pattern.compile("[a-z]*[(]([a-z]*[(].*[)])[)]");

    private String getOneConditional(final String conditional) {
        final Matcher m = oneConditional.matcher(conditional.trim());
        if (m.matches()) {
            final String result = m.group(1);
            return result;
        }
        return null;
    }

    static Pattern argAndConditional = Pattern.compile("[a-z]*[(][ ]*\"((\\\"|[^\"])*)\",[ ]*([a-z]*[(].*[)])[)]");

    @SuppressWarnings("unused")
    private String[] getArgAndConditional(final String conditional) {
        final Matcher m = argAndConditional.matcher(conditional.trim());
        if (m.matches()) {
            final String result[] = new String[] { m.group(1), m.group(2) };
            return result;
        }
        return null;
    }

}
