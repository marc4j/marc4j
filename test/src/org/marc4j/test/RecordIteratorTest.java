package org.marc4j.test;

import org.junit.Test;
import org.marc4j.MarcReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.test.utils.ResourceLoadUtils;
import org.marc4j.test.utils.StaticTestRecords;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests (and examples) for the <code>RecordIterator</code> class.
 */
public class RecordIteratorTest {


    private List<String> chabonTitles = Arrays.asList("The amazing adventures of Kavalier and Clay :", "Summerland /");

    private Map<String, Integer> expectedCounts = new HashMap<String, Integer>();


    public RecordIteratorTest() {
        expectedCounts.put(StaticTestRecords.RESOURCES_CHABON_MRC, 2);
        expectedCounts.put(StaticTestRecords.RESOURCES_CHABON_XML, 2);
    }

    private int countRecords(MarcReader iterator) {
        int count = 0;
        while (iterator.hasNext() ) {
            count++;
            iterator.next();
        }
        return count;
    }


//    @Test
//    public void testForEachRemaining() {
//        List<String> titles = new ArrayList<String>();
//        String testFile = StaticTestRecords.RESOURCES_CHABON_XML;
//        MarcReader instance = ResourceLoadUtils.getMARCXMLReader(testFile);
//        instance.forEachRemaining( (rec) -> titles.add( getTitle(rec) ) );
//        for(int i = 0, n = titles.size();i<n; i++ ) {
//            assertEquals("Found unexpected title #" + i + " in " + testFile, chabonTitles.get(i), titles.get(i));
//        }
//    }


//    @Test
//    public void testSpliterator() {
//        List<String> titles = new ArrayList<>();
//        String testFile = StaticTestRecords.RESOURCES_CHABON_XML;
//        RecordIterator instance = new RecordIterator( getMARCXMLReader(testFile) );
//        int characteristics = instance.getSpliterator().characteristics();
//        assertEquals("Spliterator should not be sized",0, characteristics & Spliterator.SIZED);
//        assertEquals("Spliterator should not be subsized",0, characteristics & Spliterator.SUBSIZED);
//        assertEquals("Spliterator should be distinct", Spliterator.DISTINCT, characteristics & Spliterator.DISTINCT);
//        assertEquals("Spliterator shoudl be immutable", Spliterator.IMMUTABLE, characteristics & Spliterator.IMMUTABLE);
//    }

    @Test
    public void testCountXML() {
        String testFile = StaticTestRecords.RESOURCES_CHABON_XML;
        int EXPECTED_COUNT = expectedCounts.get(testFile);
        MarcReader reader = ResourceLoadUtils.getMARCXMLReader(testFile);
        assertEquals("Unexpected count of records in " + testFile, EXPECTED_COUNT, countRecords(reader));
    }

    @Test
    public void testCountStreamReader() {
        String testFile = StaticTestRecords.RESOURCES_CHABON_MRC;
        int EXPECTED_COUNT = expectedCounts.get(testFile);
        MarcReader reader = ResourceLoadUtils.getMARC21Reader(testFile);
        assertEquals("Unexpected count of records in " + testFile, EXPECTED_COUNT, countRecords(reader));
    }

    @Test
    public void testStreamCount() {

        String testFile = StaticTestRecords.RESOURCES_CHABON_MRC;
        long EXPECTED_COUNT = (long)expectedCounts.get(testFile);
        MarcReader reader = ResourceLoadUtils.getMARC21Reader(testFile) ;
        long streamCount =  StreamSupport.stream(reader.spliterator(), false).count();
        assertEquals("Unexpected number of records in " + testFile , EXPECTED_COUNT, streamCount);
    }

    @Test
    public void testTitles() {
        String testFile = StaticTestRecords.RESOURCES_CHABON_MRC;
        MarcReader reader = ResourceLoadUtils.getMARC21Reader(testFile);
        List<String> titles = StreamSupport.stream(reader.spliterator(), false).map(RecordIteratorTest::getTitle).collect( Collectors.toList() );
        assertEquals("Found unexpected title count in " + testFile, (long)expectedCounts.get(testFile), titles.size() );
        for(int i = 0, n = titles.size();i<n; i++ ) {
            assertEquals("Found unexpected title #" + i + " in " + testFile, chabonTitles.get(i), titles.get(i));
        }
    }

    private static String getTitle(Record rec) {
        return ((DataField) rec.getVariableField("245")).getSubfieldsAsString("a");
    }


}