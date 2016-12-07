
package org.marc4j.converter.impl;

public interface CodeTableInterface {

    /**
     * 
     * @param i
     * @param g0
     * @param g1
     * @return Returns <code>true</code> if combining
     */
    public boolean isCombining(int i, int g0, int g1);

    /**
     * 
     * @param c
     * @param mode
     * @return Returns the <code>char</code> for the supplied <code>int</code> and mode
     */
    public char getChar(int c, int mode);

};
