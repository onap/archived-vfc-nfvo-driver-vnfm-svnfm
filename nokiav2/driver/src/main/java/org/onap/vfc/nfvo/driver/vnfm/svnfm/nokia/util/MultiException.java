/*
 * Copyright 2016-2017, Nokia Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * The collects multiple exceptions into a single exception
 */
public class MultiException extends RuntimeException {

    private final List<Exception> causes;

    /**
     * @param msg    the message of the collection of exceptions
     * @param causes the causes of the exception
     */
    public MultiException(String msg, Exception... causes) {
        super(msg);
        this.causes = newArrayList(causes);
    }

    /**
     * @param msg    the message of the collection of exceptions
     * @param causes the causes of the exception
     */
    public MultiException(String msg, Iterable<Exception> causes) {
        super(msg);
        this.causes = newArrayList(causes);
    }

    /**
     * @return the causes of the exception
     */
    public List<Exception> getCauses() {
        return causes;
    }
}
