package org.solrmarc.marc;
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;

import com.ibm.icu.text.Normalizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.converter.CharConverter;
import org.marc4j.converter.impl.AnselToUnicode;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import org.apache.log4j.Logger;

// Note this class references a trimmed down, version of the icu4j library,
// that has been "re-homed" from com.ibm.icu.text to com.solrmarc.icu.text
// This was done top prevent ClassNotFoundExceptions when solr libraries are 
// included and the solr code references and includes the full icu4j jar.

/**
 * 
 * @author Robert Haschart
 * @version $Id$
 *
 */
public class MarcTranslatedReader implements MarcReader
{
    MarcReader reader;
    CharConverter convert;
    Normalizer.Mode unicodeNormalize = Normalizer.NONE;
    
    // Initialize logging category
    static Log logger = LogFactory.getLog(MarcTranslatedReader.class);
    
    public MarcTranslatedReader(MarcReader r, boolean unicodeNormalizeBool)
    {
        reader = r;
        convert = new AnselToUnicode();
        if (unicodeNormalizeBool) this.unicodeNormalize = Normalizer.NFC;
    }
    
    public MarcTranslatedReader(MarcReader r, String unicodeNormalizeStr)
    {
        reader = r;
        convert = new AnselToUnicode();
        if (unicodeNormalizeStr.equals("KC")) unicodeNormalize = Normalizer.NFKC;
        else if (unicodeNormalizeStr.equals("KD")) unicodeNormalize = Normalizer.NFKD;
        else if (unicodeNormalizeStr.equals("C")) unicodeNormalize = Normalizer.NFC;
        else if (unicodeNormalizeStr.equals("D")) unicodeNormalize = Normalizer.NFD;
        else unicodeNormalize = Normalizer.NONE;
    }
    
//    public MarcTranslatedReader(MarcReader r, boolean unicodeNormalize, 
//    		                    String filenameForRecordsWithError)
//    {
//        reader = r;
//        convert = new AnselToUnicode();
//        this.unicodeNormalize = unicodeNormalize;
//        try
//        {
//            errorWriter = new MarcStreamWriter(new FileOutputStream(filenameForRecordsWithError));
//        }
//        catch (FileNotFoundException e)
//        {
//            System.err.println("Unable to open error output file: "+ filenameForRecordsWithError);
//            errorWriter = null;
//        }
//    }
    
    public boolean hasNext()
    {
        return reader.hasNext();
    }

    public Record next()
    {
        Record rec = reader.next();
        Leader l = rec.getLeader();
        boolean is_utf_8 = false;
        if (l.getCharCodingScheme() == 'a') is_utf_8 = true;
        if (is_utf_8 && unicodeNormalize == Normalizer.NONE) return(rec);
        List fields = rec.getVariableFields();
        Iterator f_iter = fields.iterator();
        while (f_iter.hasNext())
        {
            VariableField f = (VariableField)f_iter.next();
            if (!(f instanceof DataField)) continue;
            DataField field = (DataField)f;
            List subfields = field.getSubfields();
            Iterator<Subfield> s_iter = subfields.iterator();
            while (s_iter.hasNext())
            {
                Subfield sf = (Subfield)s_iter.next();
                String oldData = sf.getData();
                String newData = oldData;
                if (!is_utf_8) newData = convert.convert(newData);
                if (unicodeNormalize != Normalizer.NONE) 
                {
                    newData = Normalizer.normalize(newData, unicodeNormalize);
                }
                if (!oldData.equals(newData))
                {
                    sf.setData(newData);
                }
            }
        }
        l.setCharCodingScheme('a');
        rec.setLeader(l);
        return rec;
    }
    
}
