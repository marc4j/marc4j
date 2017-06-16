package org.marc4j.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Comparator;

/**
 * Merge an existing file of binary MARC records, with a set new records, edited records, and deleted records.
 * @author Robert Haschart
 *
 */
public class MarcMerger
{
    public final static String minRecordID = "0";
    public final static String maxRecordID = "zzzzzzzzzzzzzzzz";
    public static boolean verbose = false;
    public static boolean veryverbose = false;


	/**
	 * main program for merging class.
	 * @param args - the provided command line arguments
	 */
    public static void main(String[] args)
    {
    //    try {
        RawRecordReader input0 = null;
        DataInputStream input1 = null;
        RawRecordReader input2;
        DataInputStream input3 = null;
        String segmentMinRecordID = minRecordID;        
        String segmentMaxRecordID = maxRecordID;
        String newRecordsOut = null;
        int argoffset = 0;
        boolean mergeRecords = true;
        if (args[0].equals("-v"))
        {
            verbose = true;
            argoffset = 1;
        }
        if (args[0].equals("-vv"))
        {
            verbose = true;
            veryverbose = true;
            argoffset = 1;
        }
        if (args[0+argoffset].equals("-min"))
        {
            segmentMinRecordID = args[1+argoffset];
            argoffset += 2;
        }
        if (args[0+argoffset].equals("-max"))
        {
            segmentMaxRecordID = args[1+argoffset];
            argoffset += 2;
        }
        if (args[0+argoffset].equals("-new"))
        {
            newRecordsOut = args[1+argoffset];
            argoffset += 2;
        }
        if (args[0+argoffset].endsWith(".del"))
        {
            // merging deletes, not merging records.
            mergeRecords = false;
            try
            {
                input1 = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(args[0+argoffset]))));
            }
            catch (FileNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.exit(1);
            }
        }
        else
        {
            try
            {
                input0 = new RawRecordReader(new FileInputStream(new File(args[0+argoffset])));
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
                System.exit(1);
            }
        }
        try
        {
            String modfile = args[1+argoffset];
            String delfile = null;
            FileOutputStream newRecordsOutStream = null;
            if (modfile.endsWith(".mrc"))
            {
                delfile = modfile.substring(0, modfile.length()-4) + ".del";
            }
            else if (!modfile.substring(Math.max(modfile.lastIndexOf('\\'), modfile.lastIndexOf('/'))).contains("."))
            {
                delfile = modfile + ".del";
                modfile = modfile + ".mrc";
            }
            input2 = new RawRecordReader(new FileInputStream(new File(modfile)));
            try {
                input3 = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(delfile))));
            }
            catch (FileNotFoundException e)
            {
                // no del file,  ignore it be happy
            }
            if (newRecordsOut != null)
            {
                try {
                    newRecordsOutStream = new FileOutputStream(new File(newRecordsOut));
                }
                catch (FileNotFoundException e)
                {
                    newRecordsOutStream = null;
                }
            }
            if (mergeRecords) 
            {
                processMergeRecords(input0, segmentMinRecordID, segmentMaxRecordID, input2, input3, System.out, newRecordsOutStream);
            }
            else
            {
                processMergeDeletes(input1, input2, input3, System.out);
            }
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
 
    static void processMergeRecords(RawRecordReader mainFile, String minID, String maxID, RawRecordReader newOrModified, DataInputStream deleted, OutputStream out, OutputStream newRecsOut) 
    {
        Comparator<String> compare = new StringNaturalCompare();
        try
        {
            RawRecord mainrec = mainFile.hasNext() ? mainFile.next() : null; //new SimpleRecord(mainFile);
            String segmentMinRecordID = minID;
            RawRecord newOrModrec = newOrModified.hasNext() ? newOrModified.next() : null; //new SimpleRecord(newOrModified);
            String deletedId = maxRecordID;
            BufferedReader delReader = null;
            if (deleted != null)
            {
                delReader = new BufferedReader(new InputStreamReader(deleted));
                deletedId = getNextDelId(delReader);
            }
            while (newOrModrec != null && compare.compare(newOrModrec.getRecordId(), segmentMinRecordID) < 0)
            {
                newOrModrec = newOrModified.hasNext() ? newOrModified.next() : null;
            }
            while (compare.compare(deletedId, segmentMinRecordID) < 0)
            {
                deletedId = getNextDelId(delReader);
            }
            
            while (mainrec != null && compare.compare(mainrec.getRecordId(), maxRecordID)< 0)
            {
                if ((newOrModrec == null || compare.compare(mainrec.getRecordId(), newOrModrec.getRecordId())< 0) && compare.compare(mainrec.getRecordId(), deletedId) < 0)
                {
                    // mainrec unchanged, just write it out.
                    if (veryverbose) System.err.println("\nWriting original record "+ mainrec.getRecordId() + " from input file");
                    out.write(mainrec.getRecordBytes());
                    out.flush();
                    mainrec = mainFile.hasNext() ? mainFile.next() : null;
                }
                else if (newOrModrec != null && compare.compare(mainrec.getRecordId(), newOrModrec.getRecordId())== 0  && compare.compare(mainrec.getRecordId(), deletedId)== 0)
                {   
                    // mainrec equals deleteID  AND it equals modifiedRecId,  Delete record.  Although this should not happen.
                    if (verbose) System.err.println("\nDeleting record "+ deletedId);
                    deletedId = getNextDelId(delReader);
                    newOrModrec = newOrModified.hasNext() ? newOrModified.next() : null;
                    mainrec = mainFile.hasNext() ? mainFile.next() : null;
                }
                else if ((newOrModrec == null || compare.compare(mainrec.getRecordId(), newOrModrec.getRecordId())< 0)  && compare.compare(mainrec.getRecordId(), deletedId)== 0)
                {    
                    // mainrec equals deleteID,   Delete record.  
                    if (verbose) System.err.println("\nDeleting record "+ deletedId);
                    deletedId = getNextDelId(delReader);
                    mainrec = mainFile.hasNext() ? mainFile.next() : null;
                }
                else if (newOrModrec != null && compare.compare(mainrec.getRecordId(), newOrModrec.getRecordId())== 0  && compare.compare(mainrec.getRecordId(), deletedId)< 0)
                {    
                    // mainrec equals modifiedRecId,  Write out modified record.
                    if (verbose) System.err.println("\nWriting changed record "+ newOrModrec.getRecordId() + " from Mod file");
                    out.write(newOrModrec.getRecordBytes());
                    out.flush();
                    newOrModrec = newOrModified.hasNext() ? newOrModified.next() : null;
                    mainrec = mainFile.hasNext() ? mainFile.next() : null;
                }
                else // mainrec.id is greater than either newOrModrec.id or deletedId
                {
                    if (newOrModrec != null && compare.compare(mainrec.getRecordId(), newOrModrec.getRecordId())> 0 && compare.compare(newOrModrec.getRecordId(), deletedId)== 0)
                    {
                        // add a record that is not there, and then delete it right away -> net result zero
                        newOrModrec = newOrModified.hasNext() ? newOrModified.next() : null;
                        deletedId = getNextDelId(delReader);
                    }
                    else 
                    {
                        if (newOrModrec != null && compare.compare(mainrec.getRecordId(), newOrModrec.getRecordId())> 0)
                        {    
                            // newOrModrec is a new record,  Write out new record.
                            if (verbose) System.err.println("\nWriting new record "+ newOrModrec.getRecordId() + " from mod file");
                            out.write(newOrModrec.getRecordBytes());
                            out.flush();
                            if (newRecsOut != null)
                            {
                                newRecsOut.write(newOrModrec.getRecordBytes());
                                newRecsOut.flush();
                            }
                            newOrModrec = newOrModified.hasNext() ? newOrModified.next() : null;
                        }
                        if (compare.compare(mainrec.getRecordId(), deletedId)> 0)
                        {    
                            // Trying to delete a record that's already not there.  Be Happy.
                            deletedId = getNextDelId(delReader);
                        }
                    }
                }
            }
            while (newOrModrec != null && compare.compare(newOrModrec.getRecordId(), maxRecordID)< 0 && compare.compare(newOrModrec.getRecordId(), maxID)< 0)
            {
                if (compare.compare(newOrModrec.getRecordId(), deletedId)== 0)
                {
                    // add a record that is not there, and then delete it right away -> net result zero
                    newOrModrec = newOrModified.hasNext() ? newOrModified.next() : null;
                    deletedId = getNextDelId(delReader);
                }
                else 
                {
                    // newOrModrec is a new record,  Write out new record.
                    if (verbose) System.err.println("\nWriting record "+ newOrModrec.getRecordId() + " from mod file");
                    out.write(newOrModrec.getRecordBytes());
                    out.flush();
                    newOrModrec = newOrModified.hasNext() ? newOrModified.next() : null;
                }
            }
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    static void processMergeDeletes(DataInputStream mainFile, RawRecordReader newOrModified, DataInputStream deleted, PrintStream out) 
    {
        Comparator<String> compare = new StringNaturalCompare();
        BufferedReader mainReader = new BufferedReader(new InputStreamReader(mainFile));
        String mainDelete = getNextDelId(mainReader);
        
        RawRecord newOrModrec = newOrModified.hasNext() ? newOrModified.next() : null;
        String deletedId = maxRecordID;
        BufferedReader delReader = null;
        if (deleted != null)
        {
            delReader = new BufferedReader(new InputStreamReader(deleted));
            deletedId = getNextDelId(delReader);
        }
        while (compare.compare(mainDelete, maxRecordID)< 0)
        {
            if ((newOrModrec == null || compare.compare(mainDelete, newOrModrec.getRecordId())< 0)  && compare.compare(mainDelete, deletedId) < 0)
            {
                // mainDeleted rec ID unchanged, just write it out to delete file.
                //if (verbose) System.err.println("Writing original record "+ mainrec.id + " from input file");
                out.println(mainDelete);
                mainDelete = getNextDelId(mainReader);
            }
            else if ((newOrModrec != null && compare.compare(mainDelete, newOrModrec.getRecordId())== 0 ) && compare.compare(mainDelete, deletedId)== 0)
            {   
                // mainrec equals deleteID  AND it equals modifiedRecId,  Delete record.  Although this should not happen.
                if (verbose) System.err.println("Deleting record "+ deletedId);
                deletedId = getNextDelId(delReader);
                newOrModrec = newOrModified.hasNext() ? newOrModified.next() : null;
                out.println(mainDelete);
                mainDelete = getNextDelId(mainReader);
            }
            else if ((newOrModrec == null || compare.compare(mainDelete, newOrModrec.getRecordId())< 0) && compare.compare(mainDelete, deletedId)== 0)
            {    
                // mainrec equals deleteID,   Delete record.  
                if (verbose) System.err.println("Deleting record "+ deletedId);
                deletedId = getNextDelId(delReader);
                out.println(mainDelete);
                mainDelete = getNextDelId(mainReader);
            }
            else if ((newOrModrec != null && compare.compare(mainDelete, newOrModrec.getRecordId())== 0 )  && compare.compare(mainDelete, deletedId)< 0)
            {    
                // mainrec equals modifiedRecId,  Write out modified record.
                if (verbose) System.err.println("Record added, removing id from  "+ newOrModrec.getRecordId() + " from Mod file");
                newOrModrec = newOrModified.hasNext() ? newOrModified.next() : null;
                mainDelete = getNextDelId(mainReader);
            }
            else // mainrec.id is greater than either newOrModrec.id or deletedId
            {
                if (newOrModrec != null && compare.compare(mainDelete, newOrModrec.getRecordId())> 0 && compare.compare(newOrModrec.getRecordId(), deletedId)== 0)
                {    
                    //  Update contains a new 
                    out.println(mainDelete);
                }
                else
                {
                    if (newOrModrec != null && compare.compare(mainDelete, newOrModrec.getRecordId())> 0)
                    {    
                        // newOrModrec is a new record,  Write out new record.
                        if (verbose) System.err.println("New record in mod file "+ newOrModrec.getRecordId() + " skipping it.");
                        newOrModrec = newOrModified.hasNext() ? newOrModified.next() : null;
                    }
                    if (compare.compare(mainDelete, deletedId)> 0)
                    {    
                        // Trying to delete a record that's already not there.  Be Happy.
                        out.println(deletedId);
                        deletedId = getNextDelId(delReader);
                    }
                }
            }
        }
        while (compare.compare(deletedId, maxRecordID)< 0 )
        {
            // deletedId is the id of a newly deleted record,  Write out that record id.
            if (verbose) System.err.println("Writing record "+ newOrModrec.getRecordId() + " from mod file");
            out.println(deletedId);
            deletedId = getNextDelId(delReader);
        }
    }
  

    private static String getNextDelId(BufferedReader delReader)
    {
        if (delReader == null) return(maxRecordID);
        String id = maxRecordID;
        try {
            String line = delReader.readLine();
            if (line != null) 
            {
                id = line.replaceFirst("([-A-Za-z:._0-9]*).*", "$1");
            }
        }
        catch (IOException e)
        {
            // end of file, be Happy.
        }
        return(id);
    }
    
}
