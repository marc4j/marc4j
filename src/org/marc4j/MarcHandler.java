/**
 * Copyright (C) 2002 Bas Peters
 *
 * This file is part of MARC4J
 *
 * MARC4J is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * MARC4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MARC4J; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * As a special exception, if you link this library with other files to
 * produce an executable, this library does not by itself cause the
 * resulting executable to be covered by the GNU General Public License.
 * This exception does not however invalidate any other reasons why the
 * executable file might be covered by the GNU General Public License.
 */
package org.marc4j;

import org.marc4j.marc.Leader;

/**
 * <p>Defines Java callbacks to handle MARC records.  </p>
 *
 * @author Bas Peters
 */
public interface MarcHandler {

    /**
     * <p>Receives notification at the start of the file.</p>
     */
    public abstract void startFile();

    /**
     * <p>Receives notification at the end of the file.</p>
     */
    public abstract void endFile();

    /**
     * <p>Receives notification at the start of each record.</p>
     *
     * @param leader the {@link Leader} object containing the record label
     */
    public abstract void startRecord(Leader leader);

    /**
     * <p>Receives notification at the end of each record.</p>
     */
    public abstract void endRecord();

    /**
     * <p>Receives notification of a control field.</p>
     *
     * @param tag the tag name
     * @param data the control field data
     */
    public abstract void controlField(String tag, char[] data);

    /**
     * <p>Receives notification at the start of each data field.</p>
     *
     * @param tag the tag name
     * @param ind1 the first indicator value
     * @param ind2 the second indicator value
     */
    public abstract void startDataField(String tag, char ind1, char ind2);

    /**
     * <p>Receives notification at the end of each data field</p>
     *
     * @param tag the tag name
     */
    public abstract void endDataField(String tag);

    /**
     * <p>Receives notification of a data element (subfield).</p>
     *
     * @param code the data element identifier
     * @param data the data element
     */
    public abstract void subfield(char code, char[] data);

}
