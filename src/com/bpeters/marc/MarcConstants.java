package com.bpeters.marc;

/**
 * <p>Defines control characters as used in a MARC record.  </p>
 *
 * @author Bas Peters
 */
public class MarcConstants {

    /** RECORD TERMINATOR */
    public static final char RT = 0x001D;

    /** FIELD TERMINATOR */
    public static final char FT = 0x001E;

    /** SUBFIELD DELIMITER */
    public static final char US = 0x001F;

    /** BLANK */
    public static final char BLANK = 0x0020;

    public MarcConstants() {}

}
