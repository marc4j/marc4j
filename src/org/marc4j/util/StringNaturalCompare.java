package org.marc4j.util;

import java.util.Comparator;

public class StringNaturalCompare implements Comparator<String>
{

    public int compare(String s1, String s2)
    {
        int result = strnatcmp0(s1, s2, true);
        return(result);
    }

    /**
     * Compare two right-aligned numbers: the longest run of digits wins,
     * and if the runs are the same length, the earlier-recorded bias
     * (from the first differing digit) decides.
     *
     * FIX: previously checked (ind1 == s1.length()) / (ind2 == s2.length())
     * BEFORE checking whether the current characters were digits. That
     * meant hitting the end of one string was always treated as "that
     * digit run is shorter, so it loses" - even when the other string's
     * digit run ended at exactly the same position (just followed by
     * more non-digit text, e.g. comparing "21" against "12-14": both
     * digit runs are length 2, but s1's string ends right there while
     * s2 continues with "-14"). This caused e.g. "Box 21" to sort before
     * "Box 12-14" instead of after it.
     *
     * The fix treats an out-of-range index as a non-digit sentinel
     * character (mirroring how the original C strnatcmp uses '\0', where
     * isdigit('\0') is false), so "ran out of string" and "ran out of
     * digits" are handled by the same digit-vs-non-digit comparison
     * instead of a separate, premature length check.
     */
    private static int compareRight(String s1, int ind1, String s2, int ind2)
    {
        int bias = 0;

        for (;; ind1++, ind2++)
        {
            char a = ind1 < s1.length() ? s1.charAt(ind1) : 0;
            char b = ind2 < s2.length() ? s2.charAt(ind2) : 0;

            if (!Character.isDigit(a) && !Character.isDigit(b))
                return bias;
            else if (!Character.isDigit(a))
                return -1;
            else if (!Character.isDigit(b))
                return +1;
            else if (a < b)
            {
                if (bias == 0) bias = -1;
            }
            else if (a > b)
            {
                if (bias == 0) bias = +1;
            }
        }
    }

    /**
     * Compare two left-aligned numbers (used when either number has a
     * leading zero): the first differing digit wins.
     *
     * Same end-of-string-vs-end-of-digit-run fix as compareRight above.
     */
    private static int compareLeft(String s1, int ind1, String s2, int ind2)
    {
        for (;; ind1++, ind2++)
        {
            char a = ind1 < s1.length() ? s1.charAt(ind1) : 0;
            char b = ind2 < s2.length() ? s2.charAt(ind2) : 0;

            if (!Character.isDigit(a) && !Character.isDigit(b))
                return 0;
            else if (!Character.isDigit(a))
                return -1;
            else if (!Character.isDigit(b))
                return +1;
            else if (a < b)
                return -1;
            else if (a > b)
                return +1;
        }
    }

    private static int strnatcmp0(String s1, String s2, boolean fold_case)
    {
        int ai, bi;
        char ca, cb;
        boolean fractional;
        int result;

        ai = bi = 0;
        while (true)
        {
            ca = ai < s1.length() ? s1.charAt(ai) : 0;
            cb = bi < s2.length() ? s2.charAt(bi) : 0;

            /* skip over leading spaces or zeros */
            while (Character.isWhitespace(ca))
            {
                ai = ai + 1;
                ca = ai < s1.length() ? s1.charAt(ai) : 0;
            }

            while (Character.isWhitespace(cb))
            {
                bi = bi + 1;
                cb = bi < s2.length() ? s2.charAt(bi) : 0;
            }

            /* process run of digits */
            if (Character.isDigit(ca) && Character.isDigit(cb))
            {
                fractional = (ca == '0' || cb == '0');

                if (fractional)
                {
                    if ((result = compareLeft(s1, ai, s2, bi)) != 0) return result;
                }
                else
                {
                    if ((result = compareRight(s1, ai, s2, bi)) != 0) return result;
                }
            }

            if (ca == 0 && cb == 0)
            {
                return 0;
            }

            if (fold_case)
            {
                ca = Character.toUpperCase(ca);
                cb = Character.toUpperCase(cb);
            }

            if (ca < cb) return -1;
            else if (ca > cb) return +1;

            ++ai;
            ++bi;
        }
    }
}