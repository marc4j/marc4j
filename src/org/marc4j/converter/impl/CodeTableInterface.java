
package org.marc4j.converter.impl;

public interface CodeTableInterface {

    /**
     * 
     * @param i - the character code to check
     * @param g0 - the current g0 character set in use
     * @param g1 - the current g1 character code in use
     * @return Returns <code>true</code> if combining
     */
    public boolean isCombining(int i, int g0, int g1);

    /**
     * 
     * @param c - the character being looked up
     * @param mode - the current mode of the converter
     * @return Returns the <code>char</code> for the supplied <code>int</code> and mode
     */
    public char getChar(int c, int mode);

};
