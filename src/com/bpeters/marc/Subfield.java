package com.bpeters.marc;

/**
 * <p><code>Subfield</code> defines behaviour for a subfield (a data
 * element within a data field).  </p>
 *
 * <p>A subfield consists of a delimiter followed by a data element
 * identifier (together the subfield code) and a data element. The structure
 * of a data element according to the MARC standard is as follows:</p>
 * <pre>
 * DELIMITER DATA_ELEMENT_IDENTIFIER DATA_ELEMENT
 * </pre>
 * <p>This structure is returned by the {@link #marshal()}
 * method.</p>
 *
 * @author Bas Peters
 */
public class Subfield extends MarcConstants {

    /** The data element identifier. */
    protected char identifier;

    /** The data element. */
    protected char[] data;

    /** Default constructor */
    public Subfield() {}

    /**
     * <p>Creates a new <code>Subfield</code> instance and registers the
     * data element identifier and the data element.</p>
     *
     * @param identifier the data element identifier
     * @param data the data element
     */
    public Subfield(char identifier, char[] data) {
        setIdentifier(identifier);
        setData(data);
    }

    /**
     * <p>Creates a new <code>Subfield</code> instance and registers the
     * data element identifier and the data element.</p>
     *
     * @param identifier the data element identifier
     * @param data the data element
     */
    public Subfield(char identifier, String data) {
        setIdentifier(identifier);
        setData(data.toCharArray());
    }

    /**
     * <p>Registers the data element identifier.</p>
     *
     * @param identifier the data element identifier
     * @throws IllegalIdentifierException when the data element identifier
     *                                    is not a valid data element
     *                                    identifier
     */
    public void setIdentifier(char identifier) {
	this.identifier = identifier;
    }

    /**
     * <p>Registers the data element.</p>
     *
     * @param data the data element
     */
    public void setData(char[] data) {
	    this.data = data;
    }

    /**
     * <p>Registers the data element.</p>
     *
     * @param data the data element
     */
    public void setData(String data) {
	    this.data = data.toCharArray();
    }

    /**
     * <p>Returns the data element identifier.</p>
     *
     * @return <code>char</code> - the data element identifier
     */
    public char getIdentifier() {
	    return identifier;
    }

    /**
     * <p>Returns the data element.</p>
     *
     * @return <code>char[]</code> - the data element
     */
    public char[] getData() {
	    return data;
    }

    /**
     * <p>Returns a <code>String</code> representation for a data element
     * following the structure of a MARC data element.</p>
     *
     * @return <code>String</code> - the data element
     */
    public String marshal() {
        return new StringBuffer()
	    .append(US)
	    .append(identifier)
            .append(data)
	    .toString();
    }

}
