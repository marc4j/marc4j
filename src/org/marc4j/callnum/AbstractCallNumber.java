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
 * Provides an abstract base class for implementing call number objects.
 *
 * @author Tod Olson, University of Chicago
 *
 */
public abstract class AbstractCallNumber implements CallNumber {

    /**
     * Unparsed form of call number. If call number is built from components,
     * not initialized from a <code>String</code>, value may be <code>null</code>.
     */
    protected String rawCallNum;

    /**
     * Indicates whether call number parsed was a valid. Generally assume true unless proven otherwise.
     */
    protected boolean valid = true;

    public boolean isValid() {
        return this.valid;
    }

}
