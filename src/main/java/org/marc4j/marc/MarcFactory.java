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
package org.marc4j.marc;

import java.io.*;
import java.util.Properties;

/**
 * Factory for creating MARC record objects.
 * <p/>
 * You can use <code>MarcFactory</code> to create records from scratch:
 * <p/>
 * <pre>
 *
 *  MarcFactory factory = MarcFactory.newInstance();
 *  Record record = factory.newRecord();
 *  ControlField cf = factory.newControlField(&quot;001&quot;);
 *  record.addVariableField(cf);
 *  etc...
 *
 * </pre>
 *
 * @author Bas Peters
 */
public abstract class MarcFactory {

    protected MarcFactory() {
    }

    /**
     * Creates a new factory instance. The implementation class to load is the
     * first found in the following locations:
     * <ol>
     * <li>the <code>org.marc4j.marc.MarcFactory</code> system property</li>
     * <li>the above named property value in the
     * <code><i>$JAVA_HOME</i>/lib/marc4j.properties</code> file</li>
     * <li>the class name specified in the
     * <code>META-INF/services/org.marc4j.marc.MarcFactory</code> system
     * resource</li>
     * <li>the default factory class</li>
     * </ol>
     */
    public static MarcFactory newInstance() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = MarcFactory.class.getClassLoader();
        }
        String className = null;
        int count = 0;
        do {
            className = getFactoryClassName(loader, count++);
            if (className != null) {
                try {
                    Class<?> t = (loader != null) ? loader.loadClass(className) : Class.forName(className);
                    return (MarcFactory) t.newInstance();
                } catch (ClassNotFoundException e) {
                    className = null;
                } catch (Exception e) {
                }
            }
        } while (className == null && count < 3);
        return new org.marc4j.marc.impl.MarcFactoryImpl();
    }

    private static String getFactoryClassName(ClassLoader loader, int attempt) {
        final String propertyName = "org.marc4j.marc.MarcFactory";
        switch (attempt) {
            case 0:
                return System.getProperty(propertyName);
            case 1:
                try {
                    File file = new File(System.getProperty("java.home"));
                    file = new File(file, "lib");
                    file = new File(file, "marc4j.properties");
                    InputStream in = new FileInputStream(file);
                    Properties props = new Properties();
                    props.load(in);
                    in.close();
                    return props.getProperty(propertyName);
                } catch (IOException e) {
                    return null;
                }
            case 2:
                try {
                    String serviceKey = "/META-INF/services/" + propertyName;
                    InputStream in = (loader != null) ? loader.getResourceAsStream(serviceKey) :
                            MarcFactory.class.getResourceAsStream(serviceKey);
                    if (in != null) {
                        BufferedReader r = new BufferedReader(new InputStreamReader(in));
                        String ret = r.readLine();
                        r.close();
                        return ret;
                    }
                } catch (IOException e) {
                }
                return null;
            default:
                return null;
        }
    }

    /**
     * Returns a new control field instance.
     *
     * @return ControlField
     */
    public abstract ControlField newControlField();

    /**
     * Creates a new control field with the given tag and returns the instance.
     *
     * @return ControlField
     */
    public abstract ControlField newControlField(String tag);

    /**
     * Creates a new control field with the given tag and data and returns the
     * instance.
     *
     * @return ControlField
     */
    public abstract ControlField newControlField(String tag, String data);

    /**
     * Returns a new data field instance.
     *
     * @return DataField
     */
    public abstract DataField newDataField();

    /**
     * Creates a new data field with the given tag and indicators and returns the
     * instance.
     *
     * @return DataField
     */
    public abstract DataField newDataField(String tag, char ind1, char ind2);

    /**
     * Creates a new data field with the given tag and indicators and subfields and returns
     * the instance.
     *
     * @return DataField
     */
    public abstract DataField newDataField(String tag, char ind1, char ind2, String... subfieldCodesAndData);

    /**
     * Returns a new leader instance.
     *
     * @return Leader
     */
    public abstract Leader newLeader();

    /**
     * Creates a new leader with the given <code>String</code> object.
     *
     * @return Leader
     */
    public abstract Leader newLeader(String ldr);

    /**
     * Returns a new record instance.
     *
     * @return Record
     */
    public abstract Record newRecord();

    /**
     * Returns a new record instance.
     *
     * @return Record
     */
    public abstract Record newRecord(Leader leader);

    /**
     * Returns a new record instance.
     *
     * @return Record
     */
    public abstract Record newRecord(String leader);

    /**
     * Returns a new subfield instance.
     *
     * @return Leader
     */
    public abstract Subfield newSubfield();

    /**
     * Creates a new subfield with the given identifier.
     *
     * @return Subfield
     */
    public abstract Subfield newSubfield(char code);

    /**
     * Creates a new subfield with the given identifier and data.
     *
     * @return Subfield
     */
    public abstract Subfield newSubfield(char code, String data);

    public boolean validateRecord(Record record) {
        if (record.getLeader() == null) {
            return false;
        }
        for (ControlField controlField : record.getControlFields()) {
            if (!validateControlField(controlField)) {
                return false;
            }
        }
        for (DataField dataField : record.getDataFields()) {
            if (!validateDataField(dataField)) {
                return false;
            }
        }
        return true;
    }

    public boolean validateVariableField(VariableField field) {
        return field.getTag() != null;
    }

    public boolean validateControlField(ControlField field) {
        return validateVariableField(field) && field.getData() != null;
    }

    public boolean validateDataField(DataField field) {
        if (!validateVariableField(field)) {
            return false;
        }
        if (field.getIndicator1() == 0 || field.getIndicator2() == 0) {
            return false;
        }
        for (Subfield subfield : field.getSubfields()) {
            if (!validateSubField(subfield)) {
                return false;
            }
        }
        return true;
    }

    public boolean validateSubField(Subfield subfield) {
        return subfield.getCode() != 0 && subfield.getData() != null;
    }

}
