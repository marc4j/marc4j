package org.marc4j.util;

import java.util.Comparator;

public class StringNaturalCompare implements Comparator<String>
{

    public int compare(String s1, String s2)
    {
        int result = strnatcmp0(s1, s2, true);
        return(result);
    }


    private static int compareRight(String s1, int ind1, String s2, int ind2)
    {
         int bias = 0;

         /* The longest run of digits wins.  That aside, the greatest
        value wins, but we can't know that it will until we've scanned
        both numbers to know that they have the same magnitude, so we
        remember it in BIAS. */
         for (;; ind1++, ind2++)
         {
             if (ind1 == s1.length() && ind2 == s2.length())
                 return(bias);
             else if (ind1 == s1.length())
                 return(-1);
             else if (ind2 == s2.length())
                 return(+1);
             char a = s1.charAt(ind1);
             char b = s2.charAt(ind2);
             if (!Character.isDigit(a) && !Character.isDigit(b))
                 return bias;
             else if (!Character.isDigit(a))
                 return -1;
             else if (!Character.isDigit(b))
                 return +1;
             else if (a < b)
             {
                 if (bias == 0)  bias = -1;
             }
             else if (a > b)
             {
                 if (bias == 0)  bias = +1;
             }
         }
    }


    public static int compareLeft(String s1, int ind1, String s2, int ind2)
    {
         /* Compare two left-aligned numbers: the first to have a
            different value wins. */
        for (;; ind1++, ind2++)
        {
            if (ind1 == s1.length() && ind2 == s2.length())
                return 0;
            else if (ind1 == s1.length())
                return(-1);
            else if (ind2 == s2.length())
                return(+1);
            char a = s1.charAt(ind1);
            char b = s2.charAt(ind2);
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

    public static int strnatcmp0(String s1, String s2, boolean fold_case)
    {
        int ai, bi;
        char ca, cb;
        boolean fractional;
        int result;

        //   assert(a && b);
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
                /* The strings compare the same.  Perhaps the caller
                       will want to call strcmp to break the tie. */
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
