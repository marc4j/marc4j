package com.bpeters.marc;

/**
 * <p><code>Tag</code> defines behaviour to validate MARC tags.  </p>
 *
 * <p>A MARC tag is a three character string used to identify an
 * associated variable field. According to the MARC standard the tag may
 * consist of ASCII numeric characters (decimal integers 0-9) and/or
 * ASCII alphabetic characters (uppercase or lowercase, but not both).</p>
 *
 * @author Bas Peters
 */
public class Tag {

    /**
     * <p>Returns true if the given value is a valid tag value.  </p>
     *
     * <p>The method returns true if the tag contains three alphabetic
     * or numeric ASCII graphic characters.</p>
     *
     * <p><b>Note:</b> mixing uppercase and lowercase letters is not
     * validated.</p>
     *
     * @param tag the tag name
     */
    public static boolean isValid(String tag) {
	if (tag.length() != 3)
	    return false;
	return true;
    }

    /**
     * <p>Returns true if the tag identifies a control number field.  </p>
     *
     * <p>The method returns false if the tag does not equals 001.</p>
     *
     * @param tag the tag name
     * @return <code>boolean</code> - tag identifies a control number field
     *                                (true) or not (false)
     */
    public static boolean isControlNumberField(String tag) {
        if (! tag.equals("001"))
            return false;
        return true;
    }

    /**
     * <p>Returns true if the tag identifies a control field.  </p>
     *
     * <p>The method returns false if the tag does not begin with
     * two zero's.</p>
     *
     * @param tag the tag name
     * @return <code>boolean</code> - tag identifies a control field (true)
     *                                or a data field (false)
     */
    public static boolean isControlField(String tag) {
	switch(Integer.parseInt(tag)) {
	case 1 :
	case 2 :
	case 3 :
	case 4 :
	case 5 :
	case 6 :
	case 7 :
	case 8 :
	case 9 :
	    return true;
	default :
	    return false;
	}
    }

    /**
     * <p>Returns true if the tag identifies a data field.  </p>
     *
     * <p>The method returns false if the tag begins with two zero's.</p>
     *
     * @param tag the tag name
     * @return <code>boolean</code> - tag identifies a data field (true)
     *                                or a control field (false)
     */
    public static boolean isDataField(String tag) {
        if (! isControlField(tag))
            return true;
        return false;
    }

}
