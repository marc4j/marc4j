package org.marc4j.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.Map;

import org.marc4j.marc.Record;
import org.marc4j.util.DiffColorize.ReturnStructure;

public class MarcDiff
{
    static boolean verbose = false;
    static boolean noCompare = false;
    static boolean cntOnly = false;
    static String writeDifferentRecords = null;
    static String color = null;
    static String[] colors = null;
    static int cntMissing = 0;
    static int cntNew = 0;
    static int cntBoth = 0;
    static int cntDiffRough = 0;
    static int cntDiffFine = 0;
    static int cntSameWhenNormalized = 0;
    static int cntSameWhenLigature = 0;
    static int cntDiffLeaderOnly = 0;
    static int cntDiffLeaderLengthOnly = 0;
    static int cntMissingFields = 0;
    static int cntNewFields = 0;
    static int cntMissingFieldsOnly = 0;
    static int cntNewFieldsOnly = 0;
    static int cntMissingAndNewFieldsOnly = 0;
    static int cntNumSignificantDiff = 0;
    static int fixCnt = 0;
    
    public static void main(String[] args)
    {
        int i = 0;
        if (args.length == 0) {
           usage(null, 0);
        }
        while (i < args.length && args[i].startsWith("-") && args[i].length() > 1) {
            if (args[i].equals("-v")) {
                verbose = true;
                System.err.println("Verbose = true");
                i++;
            } else if (args[i].startsWith("-mrc")) {
                writeDifferentRecords = args[0].substring(1);
                i++;
            } else if (args[i].startsWith("-color")) {
                i++;
                color = args[i];
                colors = color.split(":");
                i++;
            } else if (args[i].startsWith("-nc")) {
                noCompare = true;
                i++;
            } else if (args[i].startsWith("-cnt")) {
                cntOnly = true;
                i++;
            } else if (args[i].startsWith("-usage")) {
                usage(null, 0);
            } else {
                usage("Unknown command-line option" + args[i], 1);
            }
        }
        if (i + 1 >= args.length) {
            System.err.println("Error: Must provide two files for program to diff ");
            System.exit(1);
        }
        if (i + 2 < args.length) {
            System.err.println("Error: Must provide no more than two files for program to diff ");
            System.exit(1);
        }
        
        String fileStr1 = args[i];
        File file1 = new File(fileStr1);
        String fileStr2 = args[i+1];
        File file2 = new File(fileStr2);
        RawRecordReader reader1 = null;
        if (fileStr1.equals("-") && fileStr2.equals("-")) {
            System.err.println("Error: Both input files mapped from stdin");
            System.exit(1);
            
        }
        if (fileStr1.equals("-")) {
            reader1 = new RawRecordReader(System.in);
        }
        else {
            try {
                reader1 = new RawRecordReader(new FileInputStream(file1));
            }
            catch (FileNotFoundException e) {
                System.err.println("Error: Opening input file: "+ file2.getAbsolutePath());
                System.exit(1);
            }
        }
        RawRecordReader reader2 = null;;
        if (fileStr2.equals("-")) {
            reader2 = new RawRecordReader(System.in);
        } else {
            try {
                reader2 = new RawRecordReader(new FileInputStream(file2));
            }
            catch (FileNotFoundException e) {
                System.err.println("Error: Opening input file: "+ file2.getAbsolutePath());
                System.exit(1);
            }
        }

        RawRecord rec1 = null;
        RawRecord rec2 = null;
        Comparator<String> comp = new StringNaturalCompare();
        try {
            if (reader1.hasNext()) rec1 = reader1.next();
            if (reader2.hasNext()) rec2 = reader2.next();
            while (rec1 != null && rec2 != null) {
                int compVal = noCompare ? 0 : comp.compare(rec1.getRecordId(), rec2.getRecordId());
                if (compVal == 0) {
                    byte rec1bytes[] = rec1.getRecordBytes();
                    byte rec2bytes[] = rec2.getRecordBytes();
                    if (!java.util.Arrays.equals(rec1bytes, rec2bytes)) {
                        writeRecord(writeDifferentRecords, verbose, rec1, rec2);
                        cntDiffRough ++;
                    }

                    if (cntDiffRough != cntDiffFine + fixCnt) {
                        boolean fixit = false;
                        int length = Math.min(rec1bytes.length, rec2bytes.length);
                        for (int r1 = 0; r1 <length; r1++) {
                            if (rec1bytes[r1] != rec2bytes[r1]) {
                                fixit = true;
                            }
                        }
                        if (rec1bytes.length != rec2bytes.length) fixit = true;
                        Record r1 = rec1.getAsRecord(true, true, "999", "MARC8");
                        Record r2 = rec2.getAsRecord(true, true, "999", "MARC8");
                        String str1 = r1.toString();
                        String str2 = r2.toString();

                        if (fixit) fixCnt++;
                    }
                    rec1 = (reader1.hasNext()) ? reader1.next() : null;
                    rec2 = (reader2.hasNext()) ? reader2.next() : null;
                    cntBoth ++;
                }
                else if (compVal < 0) {
                    writeRecord(writeDifferentRecords, verbose, rec1, null);
                    rec1 = (reader1.hasNext()) ? reader1.next() : null;
                    cntMissing ++;
                }
                else if (compVal > 0) {
                    writeRecord(writeDifferentRecords, verbose, null, rec2);
                    rec2 = (reader2.hasNext()) ? reader2.next() : null;
                    cntNew ++;
                }
            }
            while (rec1 != null) {
                writeRecord(writeDifferentRecords, verbose, rec1, null);
                rec1 = (reader1.hasNext()) ? reader1.next() : null;
                cntMissing ++;
            }
            while (rec2 != null) {
                writeRecord(writeDifferentRecords, verbose, null, rec2);
                rec2 = (reader2.hasNext()) ? reader2.next() : null;
                cntNew ++;
            }
        }
        catch (IOException ioe) {
            System.err.println("Error: Trouble writing to stdout, can this even happen?");
        }
        if (cntOnly) {
            System.out.println("" + cntMissing + " records in file1 but not in file2");
            System.out.println("" + cntNew + " records in file2 but not in file1");
            System.out.println("" + cntBoth + " records in both files");
            System.out.println("" + (cntBoth - ( cntDiffFine + cntMissingFieldsOnly + cntNewFieldsOnly + cntMissingAndNewFieldsOnly)) + " are the same in both files");
            System.out.println("" + (cntMissingFields) + " have missing fields");
            System.out.println("  " + (cntMissingFieldsOnly) + " of those are otherwise are the same");
            System.out.println("" + (cntNewFields) + " have new fields");
            System.out.println("  " + (cntNewFieldsOnly) + " of those are otherwise are the same");
            System.out.println("  " + (cntMissingAndNewFieldsOnly) + " have both missing fields and new fields but are otherwise are the same");
            System.out.println("" + (cntDiffRough) + " are different");
            System.out.println("" + (cntDiffFine) + " have different field contents");
            System.out.println("  " + (cntDiffLeaderLengthOnly) + " of these only differ in the lengths in the leader");
            System.out.println("  " + (cntDiffLeaderOnly) + " of these only differ in the other fields in the leader");
            System.out.println("  " + (cntSameWhenNormalized) + " of these are the same when accented characters are normalized");
            System.out.println("  " + (cntSameWhenLigature) + " of these are the same when two part combining characters are fixed");
            System.out.println("  " + (cntNumSignificantDiff) + " have significant differences");
            System.out.println("  " + (cntDiffRough - cntDiffFine) + " have no detectable differences");
        }
    }

    private static void usage(String error, int exitcode) {
        if (error != null) {
            System.err.println("Error: "+ error);
        }
        System.err.println("Usage: marcdiff  [-options] <file1.mrc> <file2.mrc>");
        System.err.println("       -v      Write verbose output");
        System.err.println("       -mrc    Write out binary MARC records that are different from one file to the other");
        System.err.println("       -mrc1   Write out binary MARC records from the first file only that are new or different from the corresponding record in file 2");
        System.err.println("       -mrc2   Write out binary MARC records from the second file only that are new or different from the corresponding record in file 1");
        System.err.println("       -nc     Simply compare files based on the position of binary MARC records, rather than checking for correspondence of record ids");
        System.err.println("       -cnt    Simply count the differences between the files without showing them");
        System.err.println("       -usage = this message");
        System.exit(exitcode);
    }

    private static void writeRecord(String writeDifferentRecords, boolean verbose, RawRecord rec1, RawRecord rec2) throws IOException {
        if (writeDifferentRecords != null) {
            if (rec1 != null && rec2 != null) {
                if (writeDifferentRecords.equals("mrc") || writeDifferentRecords.startsWith("mrc2")) {
                    System.out.write(rec2.getRecordBytes());
                } else if (writeDifferentRecords.startsWith("mrc1")) {
                    System.out.write(rec1.getRecordBytes());
                }
            } else if (writeDifferentRecords.contains("1") && rec1 != null) {
                System.out.write(rec1.getRecordBytes());
            } else if (writeDifferentRecords.contains("2") && rec2 != null) {
                System.out.write(rec2.getRecordBytes());
            }
            System.out.flush();
        }
        else {
            if (rec1 != null && rec2 != null) {
                Record r1 = rec1.getAsRecord(true, true, "999", "MARC8");
                Record r2 = rec2.getAsRecord(true, true, "999", "MARC8");
                String str1 = r1.toString();
                String str2 = r2.toString();
                if (!verbose && !cntOnly) System.out.println("record with id: " + rec1.getRecordId() + " different in file1 and file2");
                if (!str1.equals(str2)) {
                    if (!cntOnly) {
                        showDiffs(System.out, str1, str2, verbose, null);
                    }
                    else {
                        cntDiffs(str1, str2, verbose); 
                    }
                }
            }
            else if (rec1 != null) {
                if (!cntOnly) System.out.println("record with id: " + rec1.getRecordId() + " found in file1 but not in file2");
                if (verbose && !cntOnly) {
                    Record rec = rec1.getAsRecord(true, true, "999", "MARC8");
                    System.out.println(rec.toString());
                }
            }
            else {
                if (!cntOnly) System.out.println("record with id: " + rec2.getRecordId() + " found in file2 but not in file1");
                if (verbose && !cntOnly) {
                    Record rec = rec2.getAsRecord(true, true, "999", "MARC8");
                    System.out.println(rec.toString());
                }
            }
        }
    }

    
    private static void cntDiffs(String str1, String str2, boolean verbose) {
        boolean incrementStillDiffWhenNormalized = false;
        boolean incrementStillDiffWhenLigature = false;
        boolean incrementDiffLeaderImpt = false;
        boolean incrementDiffLeaderLength = false;
        boolean incrementNewFields = false;
        boolean incrementMissingFields = false;
        boolean incrementDiff = false;
        String str1Lines[] = str1.split("\n");
        String str2Lines[] = str2.split("\n");
        int index1 = 0;
        int index2 = 0;
        while (index1 < str1Lines.length && index2 < str2Lines.length) {
            if (str1Lines[index1].equals(str2Lines[index2])) {
                index1++; index2++;
            }
            else if (hasMatch(str2Lines, index2+1, str1Lines[index1])) {
                incrementNewFields = true;
                index2++;
            }
            else if (hasMatch(str1Lines, index1+1, str2Lines[index2])) {
                incrementMissingFields = true;
                index1++;
            }
            else {
                String s1 = str1Lines[index1];
                String s2 = str2Lines[index2];
                String s1Norm = Normalizer.normalize(s1,  Normalizer.Form.NFC);
                String s2Norm = Normalizer.normalize(s2,  Normalizer.Form.NFC);
                String s1Ligature = ligatureNormalize(s1Norm);
                String s2Ligature = ligatureNormalize(s2Norm);
                if (s1.startsWith("LEADER") && s2.startsWith("LEADER")) {
                    String s1Leader = s1.replaceFirst("(LEADER )(.....)(.......)(.....)(.......)" , "$1XXXXX$3XXXXX$5");
                    String s2Leader = s2.replaceFirst("(LEADER )(.....)(.......)(.....)(.......)" , "$1XXXXX$3XXXXX$5");
                    if (!s1Leader.equals(s2Leader)) {
                         incrementDiffLeaderImpt = true;
                    } 
                    else {
                        incrementDiffLeaderLength = true;
                    }
               }
               else {
                    incrementDiff = true;
                    if (!s1Norm.equals(s2Norm)) {
                        incrementStillDiffWhenNormalized = true;
                    }
                    else {
                        incrementStillDiffWhenNormalized = incrementStillDiffWhenNormalized;
                    }
                    if (!s1Ligature.equals(s2Ligature)) {
                        incrementStillDiffWhenLigature = true;
                    }
                    else {
                        incrementStillDiffWhenLigature = incrementStillDiffWhenLigature;
                    }
                }
                index1++; index2++;
            }
        }
        while (index1 < str1Lines.length) {
            incrementMissingFields = true;
            index1++;
        }
        while (index2 < str2Lines.length) {
            incrementNewFields = true;
            index2++;
        }
        if (incrementDiff && incrementStillDiffWhenNormalized && incrementStillDiffWhenLigature)
        {
            if (verbose) showDiffs(System.out, str1, str2, verbose, null);
            cntNumSignificantDiff ++;
        }
        if (incrementDiff && !incrementStillDiffWhenNormalized) 
            cntSameWhenNormalized ++;
        if (incrementDiff && incrementStillDiffWhenNormalized && !incrementStillDiffWhenLigature)   
            cntSameWhenLigature ++;
        if (incrementDiffLeaderImpt  &&  !incrementMissingFields && !incrementNewFields)     
            cntDiffLeaderOnly ++;
        if (incrementDiffLeaderLength &&  !incrementDiff && !incrementMissingFields && !incrementNewFields)     
            cntDiffLeaderLengthOnly ++;
        if (!incrementDiff && incrementMissingFields && !incrementNewFields )   cntMissingFieldsOnly++;
        if (!incrementDiff && incrementNewFields && !incrementMissingFields)    cntNewFieldsOnly++;
        if (!incrementDiff && incrementMissingFields && incrementNewFields )    cntMissingAndNewFieldsOnly++;
        if (incrementMissingFields)   cntMissingFields++;
        if (incrementNewFields)       cntNewFields++;
        if (incrementDiff)            cntDiffFine++;
        else {
            index2++;
        }
        if (!incrementDiff && !incrementMissingFields && !incrementNewFields && !incrementDiffLeaderImpt && !incrementDiffLeaderLength)
        {
            incrementDiff = !incrementDiff;
        }
    }

    private static String ligatureNormalize(String str)
    {
        while (str.matches("(.*)\uFE20(.)\uFE21(.*)")) {
            str = str.replaceFirst("(.*)\uFE20(.)\uFE21(.*)", "$1\u0361$2$3"); 
        }
        while (str.matches("(.*)\uFE22(.)\uFE23(.*)")) {
            str = str.replaceFirst("(.*)\uFE22(.)\uFE23(.*)", "$1\u0360$2$3"); 
        }
        return str;
    }

    public static void showDiffs(PrintStream out, String strNorm, String strPerm, boolean verbose, Map<Character,String> map)
    {
        if (strNorm != null) {
            String normLines[] = strNorm.split("\n");
            String permLines[] = strPerm.split("\n");
 /*           if (normLines.length == permLines.length) {
                for (int i = 0; i < normLines.length; i++) {
                    if (normLines[i].equals(permLines[i])) {
                        if (verbose) out.println("   " + normLines[i]);
                    }
                    else if (map != null) {
                        int index1 = 0;
                        int index2 = 0;
                        while (index1 < normLines[i].length() && index2 < permLines[i].length()) {
                            while (index1 < normLines[i].length() && index2 < permLines[i].length() &&
                                   normLines[i].charAt(index1) == permLines[i].charAt(index2)) {
                                index1++; index2++;
                            }
                            if (index1 < normLines[i].length() && index2 < permLines[i].length()) {
                                if (!map.containsKey(permLines[i].charAt(index2))) {
                                    Character key = permLines[i].charAt(index2);
                                    map.put(key, normLines[i] + "@@" +  permLines[i]);
                                    out.println(" "+key+" : " + normLines[i]);
                                    out.println(" "+key+" : " + permLines[i]);

                                }
                                index2++;
                                index1++;
                                if (index1 < normLines[i].length() && index2 < permLines[i].length())  {
                                    while (permLines[i].substring(index2,index2+1).matches("\\p{M}") ) {
                                        index2++;
                                    }
                                    while (normLines[i].substring(index1,index1+1).matches("\\p{M}") ) {
                                        index1++;
                                    }
                                }
                            }
                        }
                    }
                    else {
                        String s1 = normLines[i];
                        String s2 = permLines[i];
                        String s1Norm = Normalizer.normalize(s1,  Normalizer.Form.NFC);
                        String s2Norm = Normalizer.normalize(s2,  Normalizer.Form.NFC);
                        String s1Ligature = ligatureNormalize(s1Norm);
                        String s2Ligature = ligatureNormalize(s2Norm);
                        String label1 = ">>>"; 
                        String label2 = "<<<";
                        if (s1.startsWith("LEADER") && s2.startsWith("LEADER")) {
                            String s1Leader = s1.replaceFirst("(LEADER )(.....)(.......)(.....)(.......)" , "$1XXXXX$3XXXXX$5");
                            String s2Leader = s2.replaceFirst("(LEADER )(.....)(.......)(.....)(.......)" , "$1XXXXX$3XXXXX$5");
                            if (s1Leader.equals(s2Leader)) {
                                label1 = " > "; 
                                label2 = " < ";
                            }
                                
                       }
                       else {
                            if (s1Norm.equals(s2Norm)) 
                            {
                                label1 = " > "; 
                                label2 = " < ";
                            }
                            else if (s1Ligature.equals(s2Ligature)) {
                                label1 = " >>"; 
                                label2 = " <<";
                            }
                            else {
                                label1 = ">>>"; 
                                label2 = "<<<";
                            }
                        }

                        out.println(label2 + normLines[i]);
                        out.println(label1 + permLines[i]);
                    }
                }
            }
            else {*/
                int index1 = 0;
                int index2 = 0;
                while (index1 < normLines.length && index2 < permLines.length) {
                    if (normLines[index1].equals(permLines[index2])) {
                        if (verbose) out.println("   " + normLines[index1]);
                        index1++; index2++;
                    }
                    else if (hasMatch(permLines, index2+1, normLines[index1])) {
                        out.println("+++" + permLines[index2]);
                        index2++;
                    }
                    else if (hasMatch(normLines, index1+1, permLines[index2])) {
                        out.println("---" + normLines[index1]);
                        index1++;
                    }
                    else {
                        String s1 = normLines[index1];
                        String s2 = permLines[index2];
                        String s1Norm = Normalizer.normalize(s1,  Normalizer.Form.NFC);
                        String s2Norm = Normalizer.normalize(s2,  Normalizer.Form.NFC);
                        String s1Ligature = ligatureNormalize(s1Norm);
                        String s2Ligature = ligatureNormalize(s2Norm);
                        String label1 = ">>>"; 
                        String label2 = "<<<";
                        if (s1.startsWith("LEADER") && s2.startsWith("LEADER")) {
                            String s1Leader = s1.replaceFirst("(LEADER )(.....)(.......)(.....)(.......)" , "$1XXXXX$3XXXXX$5");
                            String s2Leader = s2.replaceFirst("(LEADER )(.....)(.......)(.....)(.......)" , "$1XXXXX$3XXXXX$5");
                            if (s1Leader.equals(s2Leader)) {
                                label1 = " > "; 
                                label2 = " < ";
                            }
                                
                       }
                       else {
                            if (s1Norm.equals(s2Norm)) 
                            {
                                label1 = " > "; 
                                label2 = " < ";
                            }
                            else if (s1Ligature.equals(s2Ligature)) {
                                label1 = " >>"; 
                                label2 = " <<";
                            }
                            else {
                                label1 = ">>>"; 
                                label2 = "<<<";
                            }
                        }

                        if (color != null)
                        {
                            DiffColorize.ReturnStructure rs;
                            rs = DiffColorize.stringSimilarity(normLines[index1], permLines[index2], colors[0], colors[1], (colors.length > 3 ? colors[2] : colors[0]), (colors.length > 3 ? colors[3] : colors[1]), 50);

                            out.println(label2 + rs.s1);
                            out.println(label1 + rs.s2);
                        }
                        else
                        {
                            out.println(label2 + normLines[index1]);
                            out.println(label1 + permLines[index2]);
                        }
                        index1++; index2++;
                    }
                }
                while (index1 < normLines.length) {
                    out.println("---" + normLines[index1]);
                    index1++;
                }
                while (index2 < permLines.length) {
                    out.println("+++" + permLines[index2]);
                    index2++;
                }
            
        }
        else {
            String permLines[] = strPerm.split("\n");
            for (int i = 0; i < permLines.length; i++) {
                if (verbose) out.println("   " + permLines[i]);
            }
        }

    }

    private static boolean hasMatch(String[] lines, int index, String string)
    {
        for (int i = index; i < lines.length; i++) {
            if (lines[i].equals(string))
                return(true);
        }
        for (int i = index; i < lines.length; i++) {
            if (string.substring(0, 3).equals(lines[i].substring(0, 3))) {
                int lDist = MarcPatcher.getLevenshteinDistance(lines[i], string);
                   int minLen = Math.min(lines[i].length(), string.length());
                   if (lDist < 10 || lDist / (0.0 + minLen) < 0.4 ) {
                       return(true);
                   }
            }
        }
        return false;
    }
}
