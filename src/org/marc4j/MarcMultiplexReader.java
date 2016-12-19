
package org.marc4j;

import java.util.Collection;
import java.util.Iterator;

import org.marc4j.marc.Record;

public class MarcMultiplexReader implements MarcReader {

    final Collection<MarcReader> readers;

    final Collection<String> readerNames;

    final Iterator<MarcReader> readerIterator;

    final Iterator<String> nameIterator;

    int readerCnt = 0;

    MarcReader curReader = null;

    public MarcMultiplexReader(final Collection<MarcReader> marcReaders,
            final Collection<String> readerNames) {
        readers = marcReaders;
        this.readerNames = readerNames;
        readerIterator = readers.iterator();
        nameIterator = this.readerNames.iterator();
    }

    @Override
    public boolean hasNext() {
        boolean hasNext = false;
        while (curReader == null || !(hasNext = curReader.hasNext())) {
            if (readerIterator.hasNext()) {
//                final String readerName = nameIterator.hasNext() ? nameIterator.next()
//                        : "" + readerCnt;
                // logger.info("Switching to reader: "+readerName);
                curReader = readerIterator.next();
                readerCnt++;
            } else {
                curReader = null;
                return false;
            }
        }
        return hasNext;
    }

    @Override
    public Record next() {
        return curReader.next();
    }

}
