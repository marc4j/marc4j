package org.marc4j.util;

public class DiffColorize
{
    static class ReturnStructure {
        int lcs = 0;
        double similarity = 0;
        int distance = 0;
        String s1;
        String s2;
    }
    
    public static ReturnStructure stringSimilarity(String s1, String s2, String h1s, String h1e, String h2s, String h2e, int maxOffset)
    {
        int c = 0;
        int offset1 = 0;
        int offset2 = 0;
        int lcs = 0;
        // These two strings will contain the "highlighted" version
        StringBuffer _s1 = new StringBuffer(s1.length() * 3);
        StringBuffer _s2 = new StringBuffer(s2.length() * 3);
        // These characters will surround differences in the strings 
        // (Inserted into _s1 and _s2)
        ReturnStructure return_struct = new ReturnStructure();
        // If both strings are empty 
        if (s1.trim().length() == 0 && s2.trim().length() == 0)
        {   
            return_struct.lcs = 0;
            return_struct.similarity = 1;
            return_struct.distance = 0;
            return_struct.s1 = "";
            return_struct.s2 = "";
            return return_struct;
        }
        // If s2 is empty, but s1 isn't
        if (s1.trim().length() > 0 && s2.trim().length() == 0)
        {
            return_struct.lcs = 0;
            return_struct.similarity = 0;
            return_struct.distance = s1.length();
            return_struct.s1 = h1s + s1 + h1e;
            return_struct.s2 = "";
            return return_struct;
        }
        // If s1 is empty, but s2 isn't
        else if (s1.trim().length() == 0 && s2.trim().length() > 0)
        {
            return_struct.lcs = 0;
            return_struct.similarity = 0;
            return_struct.distance = s2.length();
            return_struct.s1 = "";
            return_struct.s2 = h2s + s2 + h2e;
            return return_struct;
        }
            
        // Examine the strings, one character at a time, anding at the shortest string
        // The offset adjusts for extra characters in either string.
        while ((c + offset1 < s1.length()) && (c + offset2 < s2.length()))
        {
            // Pull the next characters out of s1 and s2
            String next_s1 = substring(s1, c + offset1, (c == 0 ? 3 : 1)); // First time through check the first three
            String next_s2 = substring(s2, c + offset2, (c == 0 ? 3 : 1)); // First time through check the first three
            // If they are equal
            if (next_s1.compareTo(next_s2) == 0)
            {
                // Our longeset Common String just got one bigger
                lcs = lcs + 1;
                // Append the characters onto the "highlighted" version
                _s1.append(substring(next_s1, 0, 1));
                _s2.append(substring(next_s2, 0, 1));
            }
            // The next two characters did not match
            // Now we will go into a sub-loop while we attempt to 
            // find our place again.  We will only search as long as
            // our maxOffset allows us to.
            else
            {
                // Don't reset the offsets, just back them up so you 
                // have a point of reference
                int old_offset1 = offset1;
                int old_offset2 = offset2;
                String _s1_deviation = "";
                String _s2_deviation = "";
                // Loop for as long as allowed by our offset 
                // to see if we can match up again
                for (int i = 0; i < maxOffset; i = i+1)
                {
                    next_s1 = substring(s1, c + offset1 + i, 3); // Increments each time through.
                    int len_next_s1 = next_s1.length();
                    String bookmarked_s1 = substring(s1, c + offset1, 3); // stays the same
                    next_s2 = substring(s2, c + offset2 + i, 3); // Increments each time through.
                    int len_next_s2 = next_s2.length();
                    String bookmarked_s2 = substring(s2, c + offset2, 3); // stays the same
                    
                    // If we reached the end of both of the strings
                    if(next_s1.length() == 0 &&  next_s2.length() == 0)
                    {
                        // Quit
                        break;
                    }
                    // These variables keep track of how far we have deviated in the
                    // string while trying to find our match again.
                    _s1_deviation = _s1_deviation + substring(next_s1, 0, 1);
                    _s2_deviation = _s2_deviation + substring(next_s2, 0, 1);
                    // It looks like s1 has a match down the line which fits
                    // where we left off in s2
                    if (next_s1.equals(bookmarked_s2))
                    {
                        // s1 is now offset THIS far from s2
                        offset1 =  offset1+i;
                        // Our longeset Common String just got bigger
                        lcs = lcs + 1;
                        // Now that we match again, break to the main loop
                        break;
                    }
                        
                    // It looks like s2 has a match down the line which fits
                    // where we left off in s1
                    if (next_s2.equals(bookmarked_s1))
                    {
                        // s2 is now offset THIS far from s1
                        offset2 = offset2+i;
                        // Our longeset Common String just got bigger
                        lcs = lcs + 1;
                        // Now that we match again, break to the main loop
                        break;
                    }
                }
                //This is the number of inserted characters were found
                int added_offset1 = offset1 - old_offset1;
                int added_offset2 = offset2 - old_offset2;
                
                // We reached our maxoffset and couldn't match up the strings
                if(added_offset1 == 0 && added_offset2 == 0)
                {
                    _s1.append(h1s).append(substring(_s1_deviation, 0, added_offset1+1)).append(h1e);
                    _s2.append(h2s).append(substring(_s2_deviation, 0, added_offset2+1)).append(h2e);
                }
                // s2 had extra characters
                else if(added_offset1 == 0 && added_offset2 > 0)
                {
                    _s1.append(substring(_s1_deviation, 0, 1));
                    _s2.append(h2s).append(substring(_s2_deviation, 0, added_offset2)).append(h2e).append(substring(_s2_deviation, _s2_deviation.length()-1, _s2_deviation.length()));
                }
                // s1 had extra characters
                else if(added_offset1 > 0 && added_offset2 == 0)
                {
                    _s1.append(h1s).append(substring(_s1_deviation, 0, added_offset1)).append(h1e).append(substring(_s1_deviation, _s1_deviation.length()-1, _s1_deviation.length()));
                    _s2.append(substring(_s2_deviation, 0, 1));
                }
            }
            c = c+1;  
        }
        // Anything left at the end of s1 is extra
        if(c + offset1 < s1.length())
        {
            _s1.append(h1s).append(substring(s1, (s1.length() - (s1.length()-(c + offset1))), s1.length())).append(h1e);
        }
        // Anything left at the end of s2 is extra
        if(c + offset2 < s2.length())
        {
            _s2.append(h2s).append(substring(s2, (s2.length() - (s2.length()-(c + offset2))), s2.length())).append(h2e);
        }
            
        // Distance is the average string length minus the longest common string
        int distance = (s1.length() + s2.length())/2 - lcs;
        // Whcih string was longest?
        int maxLen = (s1.length() > s2.length() ? s1.length() : s2.length());
        // Similarity is the distance divided by the max length
        double similarity = (maxLen == 0) ? 1 : 1-(distance/maxLen);
        // Return what we found.
        return_struct.lcs = lcs;
        return_struct.similarity = similarity;
        return_struct.distance = distance;
//        return_struct.s1 = _s1.toString(); // "highlighted" version
//        return_struct.s2 = _s2.toString(); // "highlighted" version
        return_struct.s1 = _s1.toString().replace(h1e+h1s, "").replace("[ESC]", "\u001b"); // "highlighted" version
        return_struct.s2 = _s2.toString().replace(h2e+h2s, "").replace("[ESC]", "\u001b"); // "highlighted" version
        
        return return_struct;
    }

    private static String substring(String str, int offset, int length)
    {
        if (offset >= str.length()) return("");
        if (offset+length >= str.length()) return(str.substring(offset));
        return(str.substring(offset, offset+length));
    }

    public static void main(String args[])
    {
        ReturnStructure rs;
        rs = stringSimilarity("The rain in Spain stays mainly on the plains", "The rain in Madrid stays totally on the plains", "<<", ">>", "<<", ">>", 10);
        System.out.println(rs.s1);
        System.out.println(rs.s2);

    }
}
