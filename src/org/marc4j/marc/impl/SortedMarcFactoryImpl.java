
package org.marc4j.marc.impl;

import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;

/**
 * 
 * @author Robert Haschart
 */
public class SortedMarcFactoryImpl extends MarcFactoryImpl {

    /**
     * Returns a new {@link Record} from the supplied {@link Leader}.
     */
    @Override
    public Record newRecord(final Leader leader) {
        final Record record = new SortedRecordImpl();
        record.setLeader(leader);
        return record;
    }

}
