package org.marc4j.callnum;
/*
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

/**
 * Parses and computes sort keys for National Library of Medicine call numbers.
 * This class uses the same logic for computing sort keys as {@link LCCallNumber}
 * but it has changes {@link #isValid()} method.NLM call numbers utilizes schedules QS-QZ and W-WZ
 */
public class NlmCallNumber extends LCCallNumber {

  public NlmCallNumber(String rawCallNumber) {
    super(rawCallNumber);
  }

  @Override
  public boolean isValid() {
    if (this.classLetters == null || this.classLetters.length() < 2 || this.classDigits == null) {
      return false;
    } else {
      char firstChar = this.classLetters.charAt(0);
      char secondChar = this.classLetters.charAt(1);
      return firstChar == 'W' || (firstChar == 'Q' && (secondChar >= 'S' && secondChar <= 'Z'));
    }
  }
}
