package org.marc4j.marc.impl;

import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.marc4j.marc.impl.MarcFactoryImpl;

/**
 * 
 * @author Robert Haschart
 * @version $Id: SortedMarcFactoryImpl.java,v 1.1 2011/10/18 19:11:53 haschart Exp $
 *
 */
public class SortedMarcFactoryImpl  extends MarcFactoryImpl
{

    public Record newRecord(Leader leader) {
        Record record = new SortedRecordImpl();
        record.setLeader(leader);
        return record;
    }

}
