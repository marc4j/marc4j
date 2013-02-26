package org.solrmarc.marcoverride;

import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.marc4j.marc.impl.MarcFactoryImpl;

/**
 * 
 * @author Robert Haschart
 * @version $Id: UVAMarcFactoryImpl.java 19 2008-06-20 14:58:26Z wayne.graham $
 *
 */
public class NoSortMarcFactoryImpl  extends MarcFactoryImpl
{

    public Record newRecord(Leader leader) {
        Record record = new NoSortRecordImpl();
        record.setLeader(leader);
        return record;
    }

}
