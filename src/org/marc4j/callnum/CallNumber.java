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
 * Provides a generic interface for building call number objects.
 * 
 * Constructors:
 * 
 * Implementing classes are encourage to provide two constructors:
 * <ol>
 * <li>a constructor with no parameters which will just instantiate the object, and</li>
 * <li>a constructor with a call number parameter, which will parse the parameter.</li>
 * </ol>
 * 
 * Parsing and fields:
 * 
 * <code>parse</code> will set internal fields to represent logical parts of the call number.
 * Use <code>null</code> when some part of the call number is absent.
 * For example, if there is an internal field to represent a cutter but the parser finds no
 * cutter, set the field to <code>null</code>.
 *
 * @author Tod Olson, University of Chicago
 *
 */
public interface CallNumber {
    /**
     * Parse call number and populate any fields.
     * 
     * @param callNumber        call number
     */
    public void parse(String callNumber);

    /**
     * Reports whether the string given to <code>parse</code> matched the pattern for a call number.
     * Behavior is unspecified if call number was built from setters or if object has been initialized
     * since the last <code>parse</code>.
     * 
     * @return true if this parsed call number looks valid
     */
    public boolean isValid();

    /**
     * Returns a shelf key for the call number.
     * The shelf key can be sorted lexicographically, in Unicode order.
     *
     * @return sort key, may return null if {@code parse} was not called.
     */
    public String getShelfKey();
}
