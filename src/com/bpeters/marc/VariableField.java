package com.bpeters.marc;

/**
 * <p><code>VariableField</code> defines general behaviour for
 * variable fields.  </p>
 *
 * <p>According to the MARC standard the variable fields follow the
 * leader and the directory in the record and consist of control fields
 * and data fields. Control fields precede data fields in the record and
 * are arranged in the same sequence as the corresponding entries in
 * the directory.</p>
 *
 * @author Bas Peters
 *
 * @see ControlField
 * @see DataField
 */
public abstract class VariableField extends MarcConstants {

    /** The tag name. */
    protected String tag;

    /**
     * <p>Default constructor.</p>
     */
    public VariableField() {}

    /**
     * <p>Creates a new <code>VariableField</code> for the supplied tag.</p>
     *
     * @param tag the tag name
     */
    public VariableField(String tag) {
	    setTag(tag);
    }

    /**
     * <p>Registers the tag name.</p>
     *
     * @param tag the tag name
     */
    public void setTag(String tag) {
        if (! Tag.isValid(tag))
            throw new IllegalTagException(tag);
	this.tag = tag;
    }

    /**
     * <p>Returns the tag name.</p>
     *
     * @return <code>String</code> - the tag name
     */
    public String getTag() {
    	return tag;
    }
}

// End of VariableField.java
