package org.marc4j.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.marc4j.util.StringNaturalCompare;

public class SortTest {
    /**
     * array of call numbers for use as test data.
     */
    ArrayList<String> callNums;

    @Before
    public void setup()
    {
        initCallNums();
    }

    private void initCallNums()
    {
        callNums = new ArrayList<String>();
        callNums.add("ViU-2020-0054 Box 46-48");
        callNums.add("ViU-2020-0054 Box 52-54");
        callNums.add("ViU-2020-0054 Box 40-42");
        callNums.add("ViU-2020-0054 Box 43-45");
        callNums.add("ViU-2020-0054 Box 3-5");
        callNums.add("ViU-2020-0054 Box 37-39");
        callNums.add("ViU-2020-0054 Box 64-66");
        callNums.add("ViU-2020-0054 Box 25-27");
        callNums.add("ViU-2020-0054 Box 49-51");
        callNums.add("ViU-2020-0054 Box 1-2");
        callNums.add("ViU-2020-0054 Box 21");
        callNums.add("ViU-2020-0054 Box 15-17");
        callNums.add("ViU-2020-0054 Box 6-8");
        callNums.add("ViU-2020-0054 Box 58-60");
        callNums.add("ViU-2020-0054 Box 31-33");
        callNums.add("ViU-2020-0054 Box 22-24");
        callNums.add("ViU-2020-0054 Box 55-57");
        callNums.add("ViU-2020-0054 Box 18-20");
        callNums.add("ViU-2020-0054 Box 61-63");
        callNums.add("ViU-2020-0054 Box 12-14");
        callNums.add("ViU-2020-0054 Box 34-36");
        callNums.add("ViU-2020-0054 Box 9-11");
        callNums.add("ViU-2020-0054 Box 28-30");
    }

    /**
     * Sanity check / contrast: plain String.compareTo sorts these
     * character-by-character, so multi-digit numbers do NOT come out in
     * numeric order (e.g. "12-14" sorts before "3-5" because '1' < '3').
     * This test pins down that (undesirable, but expected for plain
     * String comparison) behavior, mainly so it's obvious in the test
     * suite why StringNaturalCompare exists at all.
     */
    @Test
    public void plainStringSortIsNotInNumericOrder()
    {
        List<String> expected = Arrays.asList(
            "ViU-2020-0054 Box 1-2",
            "ViU-2020-0054 Box 12-14",
            "ViU-2020-0054 Box 15-17",
            "ViU-2020-0054 Box 18-20",
            "ViU-2020-0054 Box 21",
            "ViU-2020-0054 Box 22-24",
            "ViU-2020-0054 Box 25-27",
            "ViU-2020-0054 Box 28-30",
            "ViU-2020-0054 Box 3-5",
            "ViU-2020-0054 Box 31-33",
            "ViU-2020-0054 Box 34-36",
            "ViU-2020-0054 Box 37-39",
            "ViU-2020-0054 Box 40-42",
            "ViU-2020-0054 Box 43-45",
            "ViU-2020-0054 Box 46-48",
            "ViU-2020-0054 Box 49-51",
            "ViU-2020-0054 Box 52-54",
            "ViU-2020-0054 Box 55-57",
            "ViU-2020-0054 Box 58-60",
            "ViU-2020-0054 Box 6-8",
            "ViU-2020-0054 Box 61-63",
            "ViU-2020-0054 Box 64-66",
            "ViU-2020-0054 Box 9-11");

        Collections.sort(callNums, new Comparator<String>()
        {
            @Override
            public int compare(String o1, String o2)
            {
                return o1.compareTo(o2);
            }
        });

        assertEquals(expected, callNums);
    }

    /**
     * The main regression test: StringNaturalCompare should sort these
     * call numbers in true numeric order by the first number in each
     * "Box N" / "Box N-M" suffix, regardless of digit count.
     */
    @Test
    public void naturalSortOrdersBoxNumbersNumerically()
    {
        List<String> expected = Arrays.asList(
            "ViU-2020-0054 Box 1-2",
            "ViU-2020-0054 Box 3-5",
            "ViU-2020-0054 Box 6-8",
            "ViU-2020-0054 Box 9-11",
            "ViU-2020-0054 Box 12-14",
            "ViU-2020-0054 Box 15-17",
            "ViU-2020-0054 Box 18-20",
            "ViU-2020-0054 Box 21",
            "ViU-2020-0054 Box 22-24",
            "ViU-2020-0054 Box 25-27",
            "ViU-2020-0054 Box 28-30",
            "ViU-2020-0054 Box 31-33",
            "ViU-2020-0054 Box 34-36",
            "ViU-2020-0054 Box 37-39",
            "ViU-2020-0054 Box 40-42",
            "ViU-2020-0054 Box 43-45",
            "ViU-2020-0054 Box 46-48",
            "ViU-2020-0054 Box 49-51",
            "ViU-2020-0054 Box 52-54",
            "ViU-2020-0054 Box 55-57",
            "ViU-2020-0054 Box 58-60",
            "ViU-2020-0054 Box 61-63",
            "ViU-2020-0054 Box 64-66");

        Collections.sort(callNums, new StringNaturalCompare());

        assertEquals(expected, callNums);
    }

    /**
     * Focused regression test for the specific bug fixed in compareRight:
     * a bare single/double-digit number (no trailing "-range") must sort
     * correctly against neighboring ranges of the same digit count, even
     * though its digit run ends at the end of the string rather than at
     * a non-digit character like '-'. Before the fix, "Box 21" sorted
     * before "Box 12-14" because compareRight treated "ran out of string"
     * as automatically shorter/lesser, instead of checking whether the
     * other string's digit run had also ended at that same position.
     */
    @Test
    public void naturalSortHandlesSingleNumberAmongRangesOfSameDigitCount()
    {
        List<String> input = new ArrayList<String>(Arrays.asList(
            "Box 22-24",
            "Box 12-14",
            "Box 21",
            "Box 18-20"));

        List<String> expected = Arrays.asList(
            "Box 12-14",
            "Box 18-20",
            "Box 21",
            "Box 22-24");

        Collections.sort(input, new StringNaturalCompare());

        assertEquals(expected, input);
    }

    /**
     * Simple sequential numbers with no "-range" suffix at all, to make
     * sure the common/simplest case (single number per string, 1-20)
     * sorts numerically rather than lexicographically.
     */
    @Test
    public void naturalSortOrdersSimpleSequentialNumbers()
    {
        List<String> input = new ArrayList<String>(Arrays.asList(
            "ViU-2019-0066 Box 5",
            "ViU-2019-0066 Box 1",
            "ViU-2019-0066 Box 6",
            "ViU-2019-0066 Box 10",
            "ViU-2019-0066 Box 20",
            "ViU-2019-0066 Box 19",
            "ViU-2019-0066 Box 18",
            "ViU-2019-0066 Box 14",
            "ViU-2019-0066 Box 11",
            "ViU-2019-0066 Box 4",
            "ViU-2019-0066 Box 7",
            "ViU-2019-0066 Box 3",
            "ViU-2019-0066 Box 9",
            "ViU-2019-0066 Box 12",
            "ViU-2019-0066 Box 13",
            "ViU-2019-0066 Box 15",
            "ViU-2019-0066 Box 8",
            "ViU-2019-0066 Box 16",
            "ViU-2019-0066 Box 17",
            "ViU-2019-0066 Box 2"));

        List<String> expected = new ArrayList<String>();
        for (int i = 1; i <= 20; i++) {
            expected.add("ViU-2019-0066 Box " + i);
        }

        Collections.sort(input, new StringNaturalCompare());

        assertEquals(expected, input);
    }
}