package org.marc4j.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Normalizer
{
    private Normalizer()
    {}
    
    private static Class normalizerInAvailableJar = null;
 //   private static Class normalizerModeInAvailableJar = null;
 //   private static Object normalizerModeNone = null;    
 //   private static Object normalizerModeNFD = null;
 //   private static Object normalizerModeNFC = null;
    private static Method composeMethod = null;
    private static Method decomposeMethod = null;
    
    public static int NFD =  2;
    public static int NFKD = 3;
    public static int NFC =  4;
    public static int NFKC = 5;
    public static String normalize(String str, int mode)
    {
        String result = str;
        if (normalizerInAvailableJar == null)
        {
            initializeNormalizer();
        }      
        try
        {
            if (mode == NFD)
                result = decomposeMethod.invoke(null, str, false).toString();
            else if (mode == NFC)
                result = composeMethod.invoke(null, str, false).toString();
            else if (mode == NFKD)
                result = decomposeMethod.invoke(null, str, true).toString();
            else if (mode == NFKC)
                result = composeMethod.invoke(null, str, true).toString();
            else
                result = str;
        }
        catch (IllegalArgumentException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return(result);
    }
    
    private static void initializeNormalizer()
    {
        try
        {
            normalizerInAvailableJar = Class.forName("com.ibm.icu.text.Normalizer");
  //          normalizerModeInAvailableJar = Class.forName("com.ibm.icu.text.Normalizer$Mode");
            
        }
        catch (ClassNotFoundException e)
        {
            try
            {
                normalizerInAvailableJar = Class.forName("com.solrmarc.icu.text.Normalizer");
   //             normalizerModeInAvailableJar = Class.forName("com.solrmarc.icu.text.Normalizer$Mode");
            }
            catch (ClassNotFoundException e1)
            {
                throw new RuntimeException(e);
            }
        }
        try
        {
            composeMethod = normalizerInAvailableJar.getMethod("compose", String.class, boolean.class);
            decomposeMethod = normalizerInAvailableJar.getMethod("decompose", String.class, boolean.class);
  //         normalizerModeNFD = normalizerInAvailableJar.getField("NFD");
  //          normalizerModeNFC = normalizerInAvailableJar.getField("NFC");
  //          normalizerModeNone = normalizerInAvailableJar.getField("NONE");
        }
        catch (SecurityException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (NoSuchMethodException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
