package org.marc4j.callnum;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.regex.Matcher;

import org.junit.Before;
import org.junit.Test;

/**
 * Exercise the {@code DeweyCallNumber} class from the command line.
 * Illustrates parsing a shelf key construction.
 * Useful for debugging during development.
 *
 * <p>From the root of the solrmarc distribution:
 * <p>{@code java -cp lib/solrmarc/build org.solrmarc.callnum.ExerciseDeweyCallNumber}
 *
 * @author Tod Olson, University of Chicago
 *
 */
public class DeweyCallNumberTests
{
    /**
     * array of call numbers for use as test data.
     */
    ArrayList<String> validCallNums;
    ArrayList<String> invalidCallNums;

    @Before
    public void setup()
    {
        initCallNums();
    }

    @Test
    public void exercisePatterns()
    {
        for (String callnum : validCallNums)
        {
            Matcher m = DeweyCallNumber.classPattern.matcher(callnum);
            assertTrue(m.matches());
        }
        for (String callnum : invalidCallNums)
        {
            Matcher m = DeweyCallNumber.classPattern.matcher(callnum);
            assertTrue(!m.matches());
        }
    }

    @SuppressWarnings("unused")
    @Test
    public void exerciseClass()
    {
        for (String callnum : validCallNums)
        {
            DeweyCallNumber call = new DeweyCallNumber(callnum);
            String classification = call.getClassification();
            String classDigits = call.getClassDigits();
            String classDecimal = call.getClassDecimal();
            String cutter = call.getCutter();
            String cutterSuffix = call.getSuffix();
            String shelfKey = call.getShelfKey();
        }
    }

    @Test
    public void exerciseShelfKey()
    {
        String prevShelfKey = "";
        for (String callnum : validCallNums)
        {
            DeweyCallNumber call = new DeweyCallNumber(callnum);
            String shelfKey = call.getShelfKey();
            if (shelfKey.compareTo(prevShelfKey) < 0)
                System.out.println("shelfKey order problem: "+ prevShelfKey + " - > - " + shelfKey);
            assertTrue(shelfKey.compareTo(prevShelfKey) >= 0);
            prevShelfKey = shelfKey;
        }
        prevShelfKey = "";
        for (String callnum : invalidCallNums)
        {
            DeweyCallNumber call = new DeweyCallNumber(callnum);
            String shelfKey = call.getShelfKey();
            if (shelfKey.compareTo(prevShelfKey) < 0)
                System.out.println("shelfKey order problem: "+ prevShelfKey + " - > - " + shelfKey);
            assertTrue(shelfKey.compareTo(prevShelfKey) >= 0);
            prevShelfKey = shelfKey;
        }
    }

    private void initCallNums()
    {
        validCallNums = new ArrayList<String>();
        validCallNums.add("1 .I39");                 // one digit no fraction
        validCallNums.add("1.23 .I39");              // one digit fraction
        validCallNums.add("11 .I39");                // two digits no fraction
        validCallNums.add("11.34 .I39");             // two digits fraction
        validCallNums.add("11.34567 .I39");          // two digits fraction
        validCallNums.add("111 .I39");               // no fraction in class
        validCallNums.add("111 I39");                // no fraction no period before cutter
        validCallNums.add("111Q39");                 // no fraction, no period or space before cutter
        validCallNums.add("111.12 .I39");            // fraction in class, space period
        validCallNums.add("111.123 I39");            // space but no period before cutter
        validCallNums.add("111.134Q39");             // no period or space before cutter
        validCallNums.add("322.44 .F816 V.1 1974");  // cutterSuffix - volume and year
        validCallNums.add("322.45 .R513 1957");      // cutterSuffix year
        validCallNums.add("323 .A512RE NO.23-28");   // cutterSuffix no.
        validCallNums.add("323 .A778 ED.2");         // cutterSuffix ed
        validCallNums.add("323.09 .K43 V.1");        // cutterSuffix volume
        validCallNums.add("324.54 .I39 F");          // letter with space
        validCallNums.add("324.548 .C425R");         // letter without space
        validCallNums.add("324.6 .A75CUA");          // letters without space
        validCallNums.add("792.0944 T374 v.1");      // long cutter
        validCallNums.add("792.0944 T374 v.2");      // long cutter
        validCallNums.add("792.0944 T3741");         // long cutter
        validCallNums.add("800 .A123L");             // case-insensitive cutter letter suffixes
        validCallNums.add("800 .A123m");             // case-insensitive cutter letter suffixes

        invalidCallNums = new ArrayList<String>();
        invalidCallNums.add("");
        invalidCallNums.add("MC1 259");
        invalidCallNums.add("T1 105");
    }
}
