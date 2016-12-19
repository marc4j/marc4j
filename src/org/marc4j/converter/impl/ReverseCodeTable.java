
package org.marc4j.converter.impl;

import java.util.Hashtable;

/**
 * <p>
 * <code>ReverseCodeTable</code> is a set of methods to facilitate Unicode to MARC-8 character conversion, it tracks
 * the current charset encodings that are in use, and defines abstract methods isCombining() and getCharTable()which
 * must be overridden in a sub-class to actually implement the Unicode to MARC8 character conversion. There are two
 * defined subclasses: ReverseCodeTableHash which reads in and parses a large XML file at runtime, and
 * ReverseCodeTableGenerated which consists of a couple of switch statements that implement the same two methods. The
 * code for the second of these two sub-classes, ReverseCodeTableGenerated, is generated when the marc4j jar file is
 * created using the same XML file that ReverseCodeTableHash uses.
 * </p>
 *
 * @author Robert Haschart
 * @author Corey Keith
 */
public abstract class ReverseCodeTable {

    static final byte G0 = 0;

    static final byte G1 = 1;

    /**
     * Abstract method that must be defined in a sub-class, used in the conversion of Unicode to MARC-8. For a given
     * Unicode character, determine whether that character is a combining character (an accent mark or diacritic)
     *
     * @param c - the UCS/Unicode character to look up
     * @return boolean - true if character is a combining character
     */
    abstract public boolean isCombining(Character c);

    /**
     * Abstract method that must be defined in a sub-class, used in the conversion of Unicode to MARC-8. For a given
     * Unicode character, return ALL of the possible MARC-8 representations of that character. These are represented
     * in a Hashtable where the key is the ISOcode of the character set in MARC-8 that the Unicode character appears
     * in, and the value is the MARC-8 character value in that character set that encodes the given Unicode character.
     *
     * @param c - the UCS/Unicode character to look up
     * @return Hashtable - contains all of the possible MARC-8 representations of that Unicode character
     */
    abstract public Hashtable<Integer, char[]> getCharTable(Character c);

    protected Character lastLookupKey = null;

    protected Hashtable<Integer, char[]> lastLookupValue = null;

    protected byte g[];

    protected String charsetsUsed;

    /**
     * Default constructor for the abstract class, allocates and initializes the structures that are used to track the
     * current character sets in use.
     */
    public ReverseCodeTable() {
        g = new byte[2];
        init();
    }

    /**
     * Initializes the ReverseCodeTable state to the default value for encoding a field.
     */
    public void init() {
        g[0] = 0x42;
        g[1] = 0x45;
        charsetsUsed = "BE";
    }

    /**
     * Routine used for tracking which character set is currently in use for characters less than 0x80
     *
     * @return byte - the current G0 character set in use.
     */
    public byte getPreviousG0() {
        return g[G0];
    }

    /**
     * Routine used for tracking which character set is currently in use for characters greater than 0x80
     *
     * @return byte - the current G1 character set in use.
     */
    public byte getPreviousG1() {
        return g[G1];
    }

    /**
     * Routine used for changing which character set is currently in use for characters less than 0x80
     *
     * @param table - the current G0 character set to be used.
     */
    public void setPreviousG0(final int table) {
        g[G0] = (byte) table;
    }

    /**
     * Routine used for changing which character set is currently in use for characters greater than 0x80
     *
     * @param table - the current G1 character set to be used.
     */
    public void setPreviousG1(final int table) {
        g[G1] = (byte) table;
    }

    /**
     * Performs a lookup of the MARC8 translation of a given Unicode character. Caches the results in lastLookupKey
     * and lastLookupValue so that subsequent lookups made in processing the same character will proceed more quickly.
     *
     * @param c - the UCS/Unicode character to look up
     * @return Hashtable - contains all of the possible MARC-8 representations of that Unicode character
     */
    public Hashtable<Integer, char[]> codeTableHash(final Character c) {
        if (lastLookupKey != null && c.equals(lastLookupKey)) {
            return lastLookupValue;
        }

        lastLookupKey = c;
        lastLookupValue = getCharTable(c);

        return lastLookupValue;
    }

    /**
     * Checks whether a MARC8 translation of a given Unicode character exists.
     *
     * @param c - the UCS/Unicode character to look up
     * @return boolean - true if there is one or more MARC-8 representation of the given Unicode character.
     */
    public boolean charHasMatch(final Character c) {
        return codeTableHash(c) != null;
    }

    /**
     * Checks whether a MARC8 translation of a given Unicode character exists in the character set currently loaded as
     * the G0 character set.
     *
     * @param c - the UCS/Unicode character to look up
     * @return boolean - true if there is a MARC-8 representation of the given Unicode character in the current G0
     *         character set
     */
    public boolean inPreviousG0CharEntry(final Character c) {
        final Hashtable<Integer, char[]> chars = codeTableHash(c);

        if (chars != null && chars.get((int) getPreviousG0()) != null) {
            return true;
        }

        return false;
    }

    /**
     * Checks whether a MARC8 translation of a given Unicode character exists in the character set currently loaded as
     * the G1 character set.
     *
     * @param c - the UCS/Unicode character to look up
     * @return boolean - true if there is a MARC-8 representation of the given Unicode character in the current G1
     *         character set
     */
    public boolean inPreviousG1CharEntry(final Character c) {
        final Hashtable<Integer, char[]> chars = codeTableHash(c);

        if (chars != null && chars.get((int) getPreviousG1()) != null) {
            return true;
        }

        return false;
    }

    /**
     * Returns the MARC8 translation of a given Unicode character from the character set currently loaded as the G0
     * character set.
     *
     * @param c - the UCS/Unicode character to look up
     * @return boolean - true if there is a MARC-8 representation of the given Unicode character in the current G0
     *         character set
     */
    public char[] getCurrentG0CharEntry(final Character c) {
        return getCharEntry(c, getPreviousG0());
    }

    /**
     * Returns the MARC8 translation of a given Unicode character from the character set currently loaded as the G0
     * character set.
     *
     * @param c - the UCS/Unicode character to look up
     * @return boolean - true if there is a MARC-8 representation of the given Unicode character in the current G0
     *         character set
     */
    public char[] getCurrentG1CharEntry(final Character c) {
        return getCharEntry(c, getPreviousG1());
    }

    /**
     * Returns the MARC8 translation of a given Unicode character from the character set currently loaded as either
     * the G0 or the G1 character set, as specified by the second parameter.
     *
     * @param c - the UCS/Unicode character to look up
     * @param charset - whether to use the current G0 charset of the current G1 charset to perform the lookup
     * @return boolean - true if there is a MARC-8 representation of the given Unicode character in the current G0
     *         character set
     */
    public char[] getCharEntry(final Character c, final int charset) {
        final Hashtable<Integer, char[]> chars = codeTableHash(c);

        if (chars == null) {
            return new char[0];
        }

        return chars.get(charset);
    }

    /**
     * Lookups up the MARC8 translation of a given Unicode character and determines which of the MARC-8 character sets
     * that have a translation for that Unicode character is the best one to use. If one one charset has a
     * translation, that one will be returned. If more than one charset has a translation then return the first one
     * listed.
     *
     * @param c - the UCS/Unicode character to look up
     * @return boolean - true if there is a MARC-8 representation of the given Unicode character in the current G0
     *         character set
     */
    public char getBestCharSet(final Character c) {
        final Hashtable<Integer, char[]> chars = codeTableHash(c);
        final char returnVal;

        if (chars.keySet().size() == 1) {
            return (char) chars.keySet().iterator().next().intValue();
        }

        for (int i = 0; i < charsetsUsed.length(); i++) {
            final char toUse = charsetsUsed.charAt(i);

            if (chars.containsKey((int) toUse)) {
                return toUse;
            }
        }

        if (chars.containsKey('S')) {
            returnVal = 'S';
        } else {
            returnVal = (char) chars.keySet().iterator().next().intValue();
        }

        charsetsUsed = charsetsUsed + returnVal;

        return returnVal;
    }

    /**
     * Utility function for translating a String consisting of one or more two character hex string of the character
     * values into a character array containing those characters
     *
     * @param str - A string containing the two-character hex strings of characters to decode
     * @return char[] - an array of characters represented by the
     */
    public static char[] deHexify(final String str) {
        char result[] = null;

        if (str.length() == 2) {
            result = new char[1];
            result[0] = (char) Integer.parseInt(str, 16);
        } else if (str.length() == 6) {
            result = new char[3];
            result[0] = (char) Integer.parseInt(str.substring(0, 2), 16);
            result[1] = (char) Integer.parseInt(str.substring(2, 4), 16);
            result[2] = (char) Integer.parseInt(str.substring(4, 6), 16);
        }

        return result;
    }

}
