/** A formatter that is like a << new DecimalFormat("00000") with 
  * setMaximumIntegerDigits(5), but if
  * the number to be formatted is greater than 99999, it will truncate
  * it to 99999 instead of just writing out 5 least significant digits.  
  * If you'd like 00000 instead of 99999, you can set static variable
  * overflowRepresntation to 0. 
*/

package org.marc4j.util;

import java.text.DecimalFormat;
import java.text.FieldPosition;

public class CustomDecimalFormat extends DecimalFormat {
  private static final long serialVersionUID = 1L;
  static String formatString =   "00000000000000000000";
  static String maxString =      "99999999999999999999";
  public final static int REP_ALL_ZEROS = 0;
  public final static int REP_ALL_NINES = 1;
  public final static int REP_TRUNCATE = 2;

  int overflowRepresentation = REP_ALL_NINES;
  long maximumValue;

  public CustomDecimalFormat(int numberDigits) {
      super(formatString.substring(0,numberDigits));
      maximumValue = Long.parseLong(maxString.substring(0, numberDigits));
      overflowRepresentation = REP_ALL_NINES;
      this.setMaximumIntegerDigits(numberDigits); 
    }
    
  public CustomDecimalFormat(int numberDigits, int overflowType) {
      super(formatString.substring(0,numberDigits));
      maximumValue = Long.parseLong(maxString.substring(0, numberDigits));
      overflowRepresentation = overflowType;
      this.setMaximumIntegerDigits(5); 
    }
    
  
  @Override
  public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
    if (number > maximumValue)
      number = getOverflowRepresentation((long)number);
    return super.format(number, toAppendTo, pos);
  }
  
  
  @Override
  public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
    if (number > maximumValue)
        number = getOverflowRepresentation(number);
    return super.format(number, toAppendTo, pos);
  }

  private long getOverflowRepresentation(long number) {
    switch (overflowRepresentation) {
        case REP_ALL_ZEROS: return (0);
        default:
        case REP_ALL_NINES: return (maximumValue);
        case REP_TRUNCATE : return (number % (maximumValue + 1));
    }
  }
  
}
