package org.marc4j.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 
 * @author rh9ec
 *
 * This class is a shim interface to handle the normalization of unicode characters.
 * This functionality is available in java 1.6+ and newer, via the java.text.Normalizer class.
 * If for some reason that class is not available, this code will attempt to fall back to using either the 
 * class   com.ibm.icu.text.Normalizer   from the icu4j library or to using the 
 * class   com.solrmarc.icu.text.Normalizer   which is based on (an older version) of the preceding 
 *                                            and which is included as a part of SolrMarc.
 * Since Java 1.6 is now officially "end-of-lifed" it should be a reasonable assumption that users are 
 * not using a version of java that is even older than that, and this shim interface class can be deprecated.
 */

public class Normalizer
{
    private Normalizer()
    {}
    
    private static Class<?> normalizerInAvailableJar = null;
    private static Class<?> normalizerFormClass = null;
    private static boolean hasJavaTextNormalizer = false;
//    private static Class normalizerModeInAvailableJar = null;
//    private static Object normalizerModeNone = null;    
//    private static Object normalizerModeNFD = null;
//    private static Object normalizerModeNFC = null;
//    private static Object normalizerModeNFKD = null;
//    private static Object normalizerModeNFKC = null;
    private static Method composeMethod = null;
    private static Method decomposeMethod = null;
    private static Method normalizeMethod = null;
    
    public static int NONE = 1;
    public static int NFD =  2;
    public static int NFKD = 3;
    public static int NFC =  4;
    public static int NFKC = 5;
    public static Object NFD_obj = null;
    public static Object NFKD_obj = null;
    public static Object NFC_obj = null;
    public static Object NFKC_obj = null;
    
    public static String normalize(String str, int mode)
    {
        String result = str;
        if (normalizerInAvailableJar == null)
        {
            initializeNormalizer();
        }      
        try
        {
            if (hasJavaTextNormalizer)
            {
                if (mode == NFD)
                    result = normalizeMethod.invoke(null, str, NFD_obj).toString();
                else if (mode == NFC)
                    result = normalizeMethod.invoke(null, str, NFC_obj).toString();
                else if (mode == NFKD)
                    result = normalizeMethod.invoke(null, str, NFKD_obj).toString();
                else if (mode == NFKC)
                    result = normalizeMethod.invoke(null, str, NFKC_obj).toString();
                else
                    result = str;                
            }
            else
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
            normalizerInAvailableJar = Class.forName("java.text.Normalizer");
            hasJavaTextNormalizer = true;
            normalizerFormClass = Class.forName("java.text.Normalizer$Form");
  //          Method[] normalizeMethods = normalizerInAvailableJar.getMethods();
            normalizeMethod = normalizerInAvailableJar.getMethod("normalize", java.lang.CharSequence.class, normalizerFormClass);
            Method valueOf = normalizerFormClass.getMethod("valueOf", String.class);
            NFD_obj =  valueOf.invoke(null, "NFD");
            NFKD_obj = valueOf.invoke(null, "NFKD");
            NFC_obj =  valueOf.invoke(null, "NFC");
            NFKC_obj = valueOf.invoke(null, "NFKC");
            
        }
        catch (ClassNotFoundException cnfe)
        {
            try
            {
                normalizerInAvailableJar = Class.forName("com.ibm.icu.text.Normalizer");
                
            }
            catch (ClassNotFoundException e)
            {
                try
                {
                    normalizerInAvailableJar = Class.forName("com.solrmarc.icu.text.Normalizer");
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
        catch (IllegalAccessException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalArgumentException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (NoSuchMethodException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (SecurityException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
