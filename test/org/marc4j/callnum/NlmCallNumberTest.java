package org.marc4j.callnum;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class NlmCallNumberTest {

  private static final List<String> validNlmNumbers = Arrays.asList(
    "QS 11 .GA1 E53 2005",
    "QS 11 .GA1 F875d 1999",
    "QS 11 .GA1 Q6 2012",
    "QS 11 .GI8 P235s 2006",
    "QS 124 B811m 1875",
    "QT 104 B736 2003",
    "QT 104 B736 2009",
    "WA 102.5 B5315 2018",
    "WA 102.5 B62 2018",
    "WB 102.5 B62 2018",
    "WC 250 M56 2011",
    "WC 250 M6 2011"
  );

  private static final List<String> invalidNlmNumbers = Arrays.asList(
    "QA 11 .GA1 E53 2005",
    "QB 11 .GA1 F875d 1999",
    "QC 11 .GA1 Q6 2012",
    "QD 11 .GI8 P235s 2006",
    "QG 124 B811m 1875",
    "W 250 M56 2011",
    "Z 250 M6 2011"
  );

  @Test
  public void isValidNlmNumber() {
    for (String validNlmNumber : validNlmNumbers) {
      assertTrue(new NlmCallNumber(validNlmNumber).isValid());
    }
  }

  @Test
  public void isInvalidNlmNumber() {
    for (String validNlmNumber : invalidNlmNumbers) {
      assertFalse(new NlmCallNumber(validNlmNumber).isValid());
    }
  }

}
