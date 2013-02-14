package org.solrmarc.marc;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.apache.log4j.Logger;
import org.solrmarc.tools.RawRecord;

/**
 * Read a binary marc file
 * @author Robert Haschart
 * @version $Id$
 *
 */
public class RawRecordReader
{
    // Initialize logging category
    static Logger logger = Logger.getLogger(RawRecordReader.class.getName());

    private DataInputStream input;
    RawRecord nextRec = null;
    RawRecord afterNextRec = null;
    boolean mergeRecords = true;
    
    public RawRecordReader(InputStream is)
    {
        input = new DataInputStream(new BufferedInputStream(is));
    }
    
    public RawRecordReader(InputStream is, boolean mergeRecords)
    {
        this.mergeRecords = mergeRecords;
        input = new DataInputStream(new BufferedInputStream(is));
    }
    
    public boolean hasNext()
    {
        if (nextRec == null)
        {
            nextRec = new RawRecord(input);
        }
        if (nextRec != null && nextRec.getRecordBytes() != null)
        {
            if (afterNextRec == null)
            {
                afterNextRec = new RawRecord(input);
                if (mergeRecords)
                {
                    while (afterNextRec != null && afterNextRec.getRecordBytes() != null && afterNextRec.getRecordId().equals(nextRec.getRecordId()))
                    {
                        nextRec = new RawRecord(nextRec, afterNextRec);
                        afterNextRec = new RawRecord(input);
                    }
                }
           }
            return(true);
        }
        return(false);
    }
    
    public RawRecord next() 
    {
        RawRecord tmpRec = nextRec;
        nextRec = afterNextRec;
        afterNextRec = null;
        return(tmpRec);
    }
    
	
//    private static int parseRecordLength(byte[] leaderData) throws IOException {
//        InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(
//                leaderData));
//        int length = -1;
//        char[] tmp = new char[5];
//        isr.read(tmp);
//        try {
//            length = Integer.parseInt(new String(tmp));
//        } catch (NumberFormatException e) {
//            throw new IOException("unable to parse record length");
//        }
//        return(length);
//    }
    
	/**
	 * 
	 * @param args
	 */
    public static void main(String[] args)
    {
    //    try {
        RawRecordReader reader;
        
        if (args.length < 2)
        {
            System.err.println("Error: No records specified for extraction");
        }
        try
        {
            int numToSkip = 0;
            int numToOutput = -1;
            int offset = 0;
            if (args[offset].equals("-"))
            {
                reader = new RawRecordReader(System.in);
            }
            else
            {    
                reader = new RawRecordReader(new FileInputStream(new File(args[offset])));
            }
            offset++;
            while (offset < args.length && ( args[offset].equals("-skip")|| args[offset].equals("-num")))
            {
                if (args[offset].equals("-skip"))
                {
                    numToSkip = Integer.parseInt(args[offset+1]);
                    offset += 2;
                }
                else if (args[offset].equals("-num"))
                {
                    numToOutput = Integer.parseInt(args[offset+1]);
                    offset += 2;
                }  
            }
            if (numToSkip != 0 || numToOutput != -1)
            {
                processInput(reader, numToSkip, numToOutput);
            }
            else if (args[offset].equals("-id"))
            {
                printIds(reader);
            }
            else if (args[offset].equals("-h") && args.length >= 3)
            {
                String idRegex = args[offset+1].trim();
                processInput(reader, null, idRegex, null);
            }
            else if (!args[offset].endsWith(".txt"))
            {
                String idRegex = args[offset].trim();
                processInput(reader, idRegex, null, null);
            }
            else 
            {
                File idList = new File(args[offset]);
                BufferedReader idStream = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(idList))));
                String line;
                String findReplace[] = null;
                if (args.length > 2) findReplace = args[2].split("->");
                LinkedHashSet<String> idsLookedFor = new LinkedHashSet<String>();
                while ((line = idStream.readLine()) != null)
                {
                    if (findReplace != null)
                    {
                        line = line.replaceFirst(findReplace[0], findReplace[1]);
                    }
                    idsLookedFor.add(line);
                }
                processInput(reader, null, null, idsLookedFor);

            }
        }
        catch (EOFException e)
        {
            //  Done Reading input,   Be happy
        }
        catch (IOException e)
        {
            //  e.printStackTrace();
            logger.error(e.getMessage());
        }

    }
    
    private static void processInput(RawRecordReader reader, int numToSkip, int numToOutput) throws IOException
    {
        int num = 0;
        int numOutput = 0;
        while (reader.hasNext())
        {
            RawRecord rec = reader.next();
            num++;
            if (num <= numToSkip) continue;
            if (numToOutput == -1 || numOutput < numToOutput)
            { 
                byte recordBytes[] = rec.getRecordBytes();
                System.out.write(recordBytes);
                System.out.flush();
                numOutput++;
            }
        }
    }

    static void printIds(RawRecordReader reader) throws IOException
    {
        while (reader.hasNext())
        {
            RawRecord rec = reader.next();
            String id = rec.getRecordId();
            System.out.println(id);
        }
    }

    static void processInput(RawRecordReader reader, String idRegex, String recordHas, HashSet<String>idsLookedFor) throws IOException
    {
        while (reader.hasNext())
        {
            RawRecord rec = reader.next();
            String id = rec.getRecordId();
            if ( (idsLookedFor == null && recordHas == null && id.matches(idRegex)) ||
                 (idsLookedFor != null && idsLookedFor.contains(id) ) )
            { 
                byte recordBytes[] = rec.getRecordBytes();
                System.out.write(recordBytes);
                System.out.flush();
            }
            else if (idsLookedFor == null && idRegex == null && recordHas != null)
            {
                String tag = recordHas.substring(0, 3);
                String field = rec.getFieldVal(tag);
                if (field != null)
                {
                    byte recordBytes[] = rec.getRecordBytes();
                    System.out.write(recordBytes);
                    System.out.flush();
                }
            }
        }
    }

}
