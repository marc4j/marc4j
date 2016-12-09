/**
 * Copyright (C) 2004 Bas Peters
 * 
 * This file is part of MARC4J
 * 
 * MARC4J is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * MARC4J is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MARC4J; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.marc4j;

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.InputSource;

/**
 * Extends <code>Thread</code> to produce <code>Record</code> objects from
 * MARCXML data.
 * 
 * @author Bas Peters
 */
public class MarcXmlParserThread extends Thread {

    private final RecordStack queue;

    private volatile InputSource input;

    private volatile TransformerHandler th;

    /**
     * Creates a new instance and registers the <code>RecordQueue</code>.
     * 
     * @param queue the record queue
     */
    public MarcXmlParserThread(final RecordStack queue) {
        this.queue = queue;
    }

    /**
     * Creates a new instance and registers the <code>RecordQueue</code> and the
     * <code>InputStream</code>.
     * 
     * @param queue the record queue
     * @param input the input stream
     */
    public MarcXmlParserThread(final RecordStack queue, final InputSource input) {
        this.queue = queue;
        this.input = input;
    }

    /**
     * Returns the content handler to transform the source to MARCXML.
     * 
     * @return TransformerHandler - the transformation content handler
     */
    public TransformerHandler getTransformerHandler() {
        return th;
    }

    /**
     * Sets the content handler to transform the source to MARCXML.
     * 
     * @param th - the transformation content handler
     */
    public void setTransformerHandler(final TransformerHandler th) {
        this.th = th;
    }

    /**
     * Returns the input stream.
     * 
     * @return InputSource - the input source
     */
    public InputSource getInputSource() {
        return input;
    }

    /**
     * Sets the input stream.
     * 
     * @param input the input stream
     */
    public void setInputSource(final InputSource input) {
        this.input = input;
    }

    /**
     * Creates a new <code>MarcXmlHandler</code> instance, registers the
     * <code>RecordQueue</code> and sends the <code>InputStream</code> to the
     * <code>MarcXmlParser</code> parser.
     */
    @Override
    public void run() {
        try {
            final MarcXmlHandler handler = new MarcXmlHandler(queue);
            final MarcXmlParser parser = new MarcXmlParser(handler);

            if (th == null) {
                parser.parse(input);
            } else {
                parser.parse(input, th);
            }
        } catch (final MarcException me) {
            queue.passException(me);
        } finally {
            queue.end();
        }
    }

}
