// $Id: ErrorHandler.java,v 1.6 2008/09/26 21:17:42 haschart Exp $
/**
 * Copyright (C) 2004 Bas Peters
 *
 * This file is part of MARC4J
 *
 * MARC4J is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public 
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 *
 * MARC4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with MARC4J; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.marc4j;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Defines and describes errors encountered in the processing a given MARC record.
 * Used in conjunction with the MarcPermissiveReader class. 
 *
 * @author Robert Haschart
 * @version $Revision: 1.6 $
 */
public class ErrorHandler {

    public final static int FATAL = 5;
    public final static int MAJOR_ERROR = 4;
    public final static int MINOR_ERROR = 3;
    public final static int ERROR_TYPO = 2;
    public final static int WARNING = 1;
    public final static int INFO = 0;
    
    private List<Object> errors;
    private String curRecordID;
    private String curField;
    private String curSubfield;
    boolean hasMissingID;
    int maxSeverity;
    
    public class Error {
        private String curRecordID;
        private String curField;
        private String curSubfield;
        private int severity;
        private String message;
        
        public Error(String recordID, String field, String subfield, int severity, String message)
        {
            curRecordID = recordID;
            curField = field;
            curSubfield = subfield;
            this.severity = severity;
            this.message = message;
        }
        
        public String toString()
        {
            String severityMsg = getSeverityMsg(severity);
            String ret = severityMsg +" : " + message + " --- [ " + curField + " : " + curSubfield  + " ]" ;
            return(ret);
        }

        public void setCurRecordID(String curRecordID)
        {
            this.curRecordID = curRecordID;
        }
        
        public String getCurRecordID()
        {
            return(curRecordID);
        }

        public int getSeverity()
        {
            return severity;
        }

        public void setSeverity(int severity)
        {
            this.severity = severity;
        }
    }
    
    public ErrorHandler() 
    {
        errors = null;
        hasMissingID = false;
        maxSeverity = INFO;
    }

    public String getSeverityMsg(int severity)
    {
        switch (severity) {
            case FATAL:                 return("FATAL       ");
            case MAJOR_ERROR:          return("Major Error ");
            case MINOR_ERROR:          return("Minor Error ");
            case ERROR_TYPO:            return("Typo        ");
            case WARNING:               return("Warning     ");
            case INFO:                  return("Info        ");
        }
        return(null);
    }

    public boolean hasErrors()
    {
        return (errors != null && errors.size() > 0 && maxSeverity > INFO);
    }
    
    public int getMaxSeverity()
    {
        return (maxSeverity);
    }
    
    public List<Object> getErrors()
    {
        if (errors == null || errors.size() == 0) return null;        
        return(errors);
    }
    
    public void reset()
    {
        errors = null;
        maxSeverity = INFO;
    }
    
    public void addError(String id, String field, String subfield, int severity, String message)
    {
        if (errors == null) 
        {
            errors = new LinkedList<Object>();
            hasMissingID = false;
        }
        if (id != null && id.equals("unknown"))  hasMissingID = true;
        else if (hasMissingID)  
        {
            setRecordIDForAll(id);
        }
        errors.add(new Error(id, field, subfield, severity, message));
        if (severity > maxSeverity)   maxSeverity = severity; 
    }
    
    public void addError(int severity, String message)
    {
        addError(curRecordID, curField, curSubfield, severity, message);
    }

    public String getRecordID()
    {
        return curRecordID;
    }

    private void setRecordIDForAll(String id)
    {
        if (id != null)
        { 
            Iterator<Object> iter = errors.iterator();       
            while (iter.hasNext())
            {
                Error err = (Error)(iter.next());
                if (err.getCurRecordID() == null || err.getCurRecordID().equals("unknown"))
                {
                    err.setCurRecordID(id);
                }
            }
            hasMissingID = false;
        }
    }
    
    public void setRecordID(String recordID)
    {
        curRecordID = recordID;
        if (hasMissingID && errors != null) setRecordIDForAll(recordID);
    }

    public String getField()
    {
        return curField;
    }

    public void setField(String curField)
    {
        this.curField = curField;
    }

    public String getCurSubfield()
    {
        return curSubfield;
    }

    public void setCurSubfield(String curSubfield)
    {
        this.curSubfield = curSubfield;
    }
}
