package com.bpeters.marc;

/**
 * <p><code>ControlField</code> defines behaviour for a MARC control
 * field (tag 001-009).  </p>
 *
 * <p>Control fields are variable fields identified by tags beginning
 * with two zero's. They are comprised of data and a field terminator
 * and do not contain indicators or subfield codes. The structure of a
 * control field according to the MARC standard is as follows:</p>
 * <pre>
 * DATA_ELEMENT FIELD_TERMINATOR
 * </pre>
 * <p>This structure is returned by the {@link #marshal()}
 * method.</p>
 *
 * @author Bas Peters
 */
public class ControlField extends VariableField {

    /** The MARC data element. */
    protected char[] data;

    /**
     * <p>Default constructor.</p>
     */
    public ControlField() {
        super();
    }

    /**
     * <p>Creates a new control field instance and registers the tag
     * and the control field data.</p>
     *
     * @param tag the tag name
     * @param data the control field data
     */
    public ControlField(String tag, char[] data) {
        super(tag);
        setData(data);
    }

    /**
     * <p>Creates a new control field instance and registers the tag
     * and the control field data.</p>
     *
     * @param tag the tag name
     * @param data the control field data
     */
    public ControlField(String tag, String data) {
        super(tag);
        setData(data.toCharArray());
    }

    /**
     * <p>Registers the tag.</p>
     *
     * @param tag the tag name
     * @throws IllegalTagException when the tag is not a valid
     *                                     control field identifier
     */
    public void setTag(String tag) {
        if (Tag.isControlField(tag)) {
            super.setTag(tag);
        } else {
            throw new IllegalTagException(tag,
            "not a control field identifier");
        }
    }

    /**
     * <p>Returns the tag name.</p>
     *
     * @return {@link String} - the tag name
     */
    public String getTag() {
	    return super.getTag();
    }

    /**
     * <p>Registers the control field data.</p>
     *
     * @param data the control field data
     */
    public void setData(char[] data) {
	    this.data = data;
    }

    /**
     * <p>Registers the control field data.</p>
     *
     * @param data the control field data
     */
    public void setData(String data) {
	    this.data = data.toCharArray();
    }

    /**
     * <p>Returns the control field data.</p>
     *
     * @return <code>char[]</code> - control field as a
     *                               character array
     */
    public char[] getData() {
	    return data;
    }

    /**
     * <p>Returns a <code>String</code> representation for a control
     * field following the structure of a MARC control field.</p>
     *
     * @return <code>String</code> - control field
     */
    public String marshal() {
        return new String(data) + FT;
    }

    /**
     * <p>Returns the length of the serialized form of the control field.</p>
     *
     * @return <code>int</code> - length of control field
     */
    public int getLength() {
        return this.marshal().length();
    }

}
