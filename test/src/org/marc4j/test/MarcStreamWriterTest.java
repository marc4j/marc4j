package org.marc4j.test;

import org.junit.Test;
import org.marc4j.MarcStreamWriter;
import org.marc4j.marc.Record;
import org.marc4j.test.utils.StaticTestRecords;
import org.marc4j.test.utils.TestUtils;

import java.io.ByteArrayOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: ses
 * Date: 3/2/13
 * Time: 5:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class MarcStreamWriterTest {
    @Test
    public void testMarcStreamWriter() throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MarcStreamWriter writer = new MarcStreamWriter(out);
        for (Record record : StaticTestRecords.summerland) {
            writer.write(record);
        }
        writer.close();
        TestUtils.validateBytesAgainstFile(out.toByteArray(), StaticTestRecords.RESOURCES_SUMMERLAND_MRC);
    }


}
