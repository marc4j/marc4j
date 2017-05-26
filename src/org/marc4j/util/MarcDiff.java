package org.marc4j.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.Map;

import org.marc4j.marc.Record;

public class MarcDiff
{
    static boolean verbose = false;
    static boolean noCompare = false;
    static String writeDifferentRecords = null;

    public static void main(String[] args)
    {
        if (args[0].equals("-v"))
        {
            verbose = true;
            String newArgs[] = new String[args.length-1];
            System.arraycopy(args, 1, newArgs, 0, args.length-1);
            args = newArgs;
        }
        if (args[0].startsWith("-mrc"))
        {
            writeDifferentRecords = args[0].substring(1);
            String newArgs[] = new String[args.length-1];
            System.arraycopy(args, 1, newArgs, 0, args.length-1);
            args = newArgs;
        }
        if (args[0].startsWith("-nc"))
        {
            noCompare = true;
            String newArgs[] = new String[args.length-1];
            System.arraycopy(args, 1, newArgs, 0, args.length-1);
            args = newArgs;
        }
        String fileStr1 = args[0];
        File file1 = new File(fileStr1);
        String fileStr2 = args[1];
        File file2 = new File(fileStr2);
        RawRecordReader reader1 = null;
        if (fileStr1.equals("-"))
        {
            reader1 = new RawRecordReader(System.in);
        }
        else
        {
            try
            {
                reader1 = new RawRecordReader(new FileInputStream(file1));
            }
            catch (FileNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        RawRecordReader reader2 = null;;
        if (fileStr2.equals("-"))
        {
            reader2 = new RawRecordReader(System.in);
        }
        else
        {
            try
            {
                reader2 = new RawRecordReader(new FileInputStream(file2));
            }
            catch (FileNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        RawRecord rec1 = null;
        RawRecord rec2 = null;
        Comparator<String> comp = new StringNaturalCompare();
        try {
            if (reader1.hasNext()) rec1 = reader1.next();
            if (reader2.hasNext()) rec2 = reader2.next();
            while (rec1 != null && rec2 != null)
            {
                int compVal = noCompare ? 0 : comp.compare(rec1.getRecordId(), rec2.getRecordId());
                if (compVal == 0)
                {
                    byte rec1bytes[] = rec1.getRecordBytes();
                    byte rec2bytes[] = rec2.getRecordBytes();
                    if (!java.util.Arrays.equals(rec1bytes, rec2bytes))
                    {
                        writeRecord(writeDifferentRecords, verbose, rec1, rec2);
                    }

                    rec1 = (reader1.hasNext()) ? reader1.next() : null;
                    rec2 = (reader2.hasNext()) ? reader2.next() : null;
                }
                else if (compVal < 0)
                {
                    writeRecord(writeDifferentRecords, verbose, rec1, null);
                    rec1 = (reader1.hasNext()) ? reader1.next() : null;
                }
                else if (compVal > 0)
                {
                    writeRecord(writeDifferentRecords, verbose, null, rec2);
                    rec2 = (reader2.hasNext()) ? reader2.next() : null;
                }
            }
            while (rec1 != null)
            {
                writeRecord(writeDifferentRecords, verbose, rec1, null);
                rec1 = (reader1.hasNext()) ? reader1.next() : null;
            }
            while (rec2 != null)
            {
                writeRecord(writeDifferentRecords, verbose, null, rec2);
                rec2 = (reader2.hasNext()) ? reader2.next() : null;
            }
        }
        catch (IOException ioe)
        {
            System.err.println("Error: Trouble writing to stdout, can this even happen?");
        }
    }

    private static void writeRecord(String writeDifferentRecords, boolean verbose, RawRecord rec1, RawRecord rec2) throws IOException
    {
        if (writeDifferentRecords != null)
        {
            if (rec1 != null && rec2 != null)
            {
                if (writeDifferentRecords.equals("mrc") || writeDifferentRecords.startsWith("mrc2"))
                {
                    System.out.write(rec2.getRecordBytes());
                }
                else if (writeDifferentRecords.startsWith("mrc1"))
                {
                    System.out.write(rec1.getRecordBytes());
                }
            }
            else if (writeDifferentRecords.contains("1") && rec1 != null)
            {
                System.out.write(rec1.getRecordBytes());
            }
            else if (writeDifferentRecords.contains("2") && rec2 != null)
            {
                System.out.write(rec2.getRecordBytes());
            }
            System.out.flush();
        }
        else
        {
            if (rec1 != null && rec2 != null)
            {
                Record r1 = rec1.getAsRecord(true, true, "999", "MARC8");
                Record r2 = rec2.getAsRecord(true, true, "999", "MARC8");
                String str1 = r1.toString();
                String str2 = r2.toString();
                if (!verbose) System.out.println("record with id: " + rec1.getRecordId() + " different in file1 and file2");
                if (!str1.equals(str2))
                {
                    showDiffs(System.out, str1, str2, verbose, null);
                }
            }
            else if (rec1 != null)
            {
                System.out.println("record with id: " + rec1.getRecordId() + " found in file1 but not in file2");
                if (verbose)
                {
                    Record rec = rec1.getAsRecord(true, true, "999", "MARC8");
                    System.out.println(rec.toString());
                }
            }
            else
            {
                System.out.println("record with id: " + rec2.getRecordId() + " found in file2 but not in file1");
                if (verbose)
                {
                    Record rec = rec2.getAsRecord(true, true, "999", "MARC8");
                    System.out.println(rec.toString());
                }
            }
        }
    }

    public static void showDiffs(PrintStream out, String strNorm, String strPerm, boolean verbose, Map<Character,String> map)
    {
        if (strNorm != null)
        {
            String normLines[] = strNorm.split("\n");
            String permLines[] = strPerm.split("\n");
            if (normLines.length == permLines.length)
            {
                for (int i = 0; i < normLines.length; i++)
                {
                    if (normLines[i].equals(permLines[i]))
                    {
                        if (verbose) out.println("   " + normLines[i]);
                    }
                    else if (map != null)
                    {
                        int index1 = 0;
                        int index2 = 0;
                        boolean showLines = false;
                        while (index1 < normLines[i].length() && index2 < permLines[i].length())
                        {
                            while (index1 < normLines[i].length() && index2 < permLines[i].length() &&
                                   normLines[i].charAt(index1) == permLines[i].charAt(index2))
                            {
                                index1++; index2++;
                            }
                            if (index1 < normLines[i].length() && index2 < permLines[i].length())
                            {
                                if (!map.containsKey(permLines[i].charAt(index2)))
                                {
                                    Character key = permLines[i].charAt(index2);
                                    map.put(key, normLines[i] + "@@" +  permLines[i]);
                                    showLines = true;
                                    out.println(" "+key+" : " + normLines[i]);
                                    out.println(" "+key+" : " + permLines[i]);

                                }
                                index2++;
                                index1++;
                                if (index1 < normLines[i].length() && index2 < permLines[i].length())
                                {
                                    while (permLines[i].substring(index2,index2+1).matches("\\p{M}") )
                                    {
                                        index2++;
                                    }
                                    while (normLines[i].substring(index1,index1+1).matches("\\p{M}") )
                                    {
                                        index1++;
                                    }
                                }
                            }
                        }
//                            if (showLines)
//                            {
//                                out.println(" < " + normLines[i]);
//                                out.println(" > " + permLines[i]);
//                            }
                    }
                    else
                    {
                        out.println(" < " + normLines[i]);
                        out.println(" > " + permLines[i]);
                    }
                }
            }
            else
            {
                int index1 = 0;
                int index2 = 0;
                while (index1 < normLines.length && index2 < permLines.length)
                {
                    if (normLines[index1].equals(permLines[index2]))
                    {
                        if (verbose) out.println("   " + normLines[index1]);
                        index1++; index2++;
                    }
                    else if (hasMatch(permLines, index2+1, normLines[index1]))
                    {
                        out.println(" > " + permLines[index2]);
                        index2++;
                    }
                    else if (hasMatch(normLines, index1+1, permLines[index2]))
                    {
                        out.println(" < " + normLines[index1]);
                        index1++;
                    }
                    else
                    {
                        out.println(" < " + normLines[index1]);
                        out.println(" > " + permLines[index2]);
                        index1++; index2++;
                    }
                }
                while (index1 < normLines.length)
                {
                    out.println(" < " + normLines[index1]);
                    index1++;
                }
                while (index2 < permLines.length)
                {
                    out.println(" > " + permLines[index2]);
                    index2++;
                }
            }
        }
        else
        {
            String permLines[] = strPerm.split("\n");
            for (int i = 0; i < permLines.length; i++)
            {
                if (verbose) out.println("   " + permLines[i]);
            }
        }

    }

    private static boolean hasMatch(String[] lines, int index, String string)
    {
        for (int i = index; i < lines.length; i++)
        {
            if (lines[i].equals(string))
                return(true);
        }
        return false;
    }


}
