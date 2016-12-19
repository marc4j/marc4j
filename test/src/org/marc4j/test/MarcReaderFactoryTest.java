package org.marc4j.test;

import static org.junit.Assert.assertTrue;

import java.io.File;


import org.junit.Test;
import org.marc4j.MarcReader;
import org.marc4j.MarcReaderConfig;
import org.marc4j.MarcReaderFactory;
import org.marc4j.marc.Record;


@SuppressWarnings("unused")
public class MarcReaderFactoryTest {

    @Test
    public void testLoadingBinaryMarcFile() throws Exception {

        MarcReaderConfig config = new MarcReaderConfig().setPermissiveReader(true).setToUtf8(true);
        File file = new File("test/resources/selectedRecs.mrc");
        String absolute = file.getAbsolutePath();
        MarcReader reader = MarcReaderFactory.makeReader(config, null, "test/resources/selectedRecs.mrc");
        int cnt = 0;
        while (reader.hasNext()) {
            Record rec = reader.next();
            cnt++;
        }

        assertTrue("wrong count of records", cnt == 11);
    }

    @Test
    public void testLoadingMarcXMLFile() throws Exception {

        MarcReaderConfig config = new MarcReaderConfig().setPermissiveReader(true).setToUtf8(true);
        File file = new File("test/resources/selectedRecs.mrc");
        String absolute = file.getAbsolutePath();
        MarcReader reader = MarcReaderFactory.makeReader(config, null, "test/resources/chabon.xml");
        int cnt = 0;
        while (reader.hasNext()) {
            Record rec = reader.next();
            cnt++;
        }

        assertTrue("wrong count of records", cnt == 2);
    }

    @Test
    public void testLoadingMarcJsonFile() throws Exception {

        MarcReaderConfig config = new MarcReaderConfig().setPermissiveReader(true).setToUtf8(true);
        File file = new File("test/resources/selectedRecs.mrc");
        String absolute = file.getAbsolutePath();
        MarcReader reader = MarcReaderFactory.makeReader(config, null, "test/resources/marc-in-json.json");
        int cnt = 0;
        while (reader.hasNext()) {
            Record rec = reader.next();
            cnt++;
        }

        assertTrue("wrong count of records", cnt == 1);
    }

    @Test
    public void testLoadingMrkFile() throws Exception {

        MarcReaderConfig config = new MarcReaderConfig().setPermissiveReader(true).setToUtf8(true);
        File file = new File("test/resources/brkrtest.mrk");
        String absolute = file.getAbsolutePath();
        MarcReader reader = MarcReaderFactory.makeReader(config, null, "test/resources/brkrtest.mrk");
        int cnt = 0;
        while (reader.hasNext()) {
            Record rec = reader.next();
            cnt++;
        }

        assertTrue("wrong count of records", cnt == 8);
    }

    @Test
    public void testLoadingMrk8File() throws Exception {

        MarcReaderConfig config = new MarcReaderConfig().setPermissiveReader(true).setToUtf8(true);
        File file = new File("test/resources/brkrtest.mrk8");
        String absolute = file.getAbsolutePath();
        MarcReader reader = MarcReaderFactory.makeReader(config, null, "test/resources/brkrtest.mrk8");
        int cnt = 0;
        while (reader.hasNext()) {
            Record rec = reader.next();
            cnt++;
        }

        assertTrue("wrong count of records", cnt == 8);
    }

    @Test
    public void testLoadingSeveralFiles() throws Exception {

        MarcReaderConfig config = new MarcReaderConfig().setPermissiveReader(true).setToUtf8(true);
        File file = new File("test/resources/brkrtest.mrk");
        String absolute = file.getAbsolutePath();
        MarcReader reader = MarcReaderFactory.makeReader(config, null, "test/resources/brkrtest.mrk",
                                  "test/resources/brkrtest.mrk8", "test/resources/marc-in-json.json",
                                  "test/resources/chabon.xml", "test/resources/selectedRecs.mrc");
        int cnt = 0;
        while (reader.hasNext()) {
            Record rec = reader.next();
            cnt++;
        }

        assertTrue("wrong count of records", cnt == 30);
    }

    @Test
    public void testLoadingTwoFiles() throws Exception {

        MarcReaderConfig config = new MarcReaderConfig().setPermissiveReader(true).setToUtf8(true);
        File file = new File("test/resources/brkrtest.mrk");
        String absolute = file.getAbsolutePath();
        MarcReader reader = MarcReaderFactory.makeReader(config, null, "test/resources/selectedRecs.mrc",
                "test/resources/brkrtest.mrk8");
        int cnt = 0;
        while (reader.hasNext()) {
            Record rec = reader.next();
            cnt++;
        }

        assertTrue("wrong count of records", cnt == 19);
    }

    @Test
    public void testLoadingSeveralNonMrk8Files() throws Exception {

        MarcReaderConfig config = new MarcReaderConfig().setPermissiveReader(true).setToUtf8(true);
        File file = new File("test/resources/brkrtest.mrk");
        String absolute = file.getAbsolutePath();
        MarcReader reader = MarcReaderFactory.makeReader(config, null, "test/resources/marc-in-json.json",
                                  "test/resources/chabon.xml", "test/resources/selectedRecs.mrc");
        int cnt = 0;
        while (reader.hasNext()) {
            Record rec = reader.next();
            cnt++;
        }

        assertTrue("wrong count of records", cnt == 14);
    }

}
