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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;

/**
 * Collection of utility functions
 */
public class CbamUtils {

    /**
     * Separator for multiple keys concatenated into a single string
     */
    public static final String SEPARATOR = "_";

    private CbamUtils() {
        //use static way
    }

    /**
     * @param parent the parent JSON object
     * @param name   the name of the child
     * @return the child JSON object of parent with given name
     */
    public static JsonObject child(JsonObject parent, String name) {
        return childElement(parent, name).getAsJsonObject();
    }

    /**
     * @param parent the parent JSON object
     * @param name   the name of the child
     * @return the child JSON object of parent with given name
     */
    public static JsonElement childElement(JsonObject parent, String name) {
        JsonElement child = parent.get(name);
        if (child == null) {
            throw new OperationMustBeAborted("Missing child " + name);
        }
        return child;
    }

    /**
     * Logs and returns a runtime exception
     *
     * @param logger the logger
     * @param msg    the error message
     * @param e      the exception to be wrapped
     * @return never reached (runtime exception is thrown)
     */
    public static RuntimeException buildFatalFailure(Logger logger, String msg, Exception e) {
        logger.error(msg, e);
        return new OperationMustBeAborted(e, msg);
    }

    /**
     * Logs and returns a runtime exception
     *
     * @param logger the logger
     * @param msg    the error message
     * @return never reached (runtime exception is thrown)
     */
    public static RuntimeException buildFatalFailure(Logger logger, String msg) {
        logger.error(msg);
        return new OperationMustBeAborted(msg);
    }

    public static class OperationMustBeAborted extends RuntimeException {
        OperationMustBeAborted(String msg) {
            super(msg);
        }

        OperationMustBeAborted(Exception e, String msg) {
            super(msg, e);
        }
    }
}
