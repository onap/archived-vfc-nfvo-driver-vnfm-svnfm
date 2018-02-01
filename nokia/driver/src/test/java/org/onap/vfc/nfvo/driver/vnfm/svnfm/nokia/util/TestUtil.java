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

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.lang.reflect.Constructor;

public class TestUtil {
    /**
     * Due to sonar issue you have to create a private constructor for classes
     * that only have static methods to prevent the creation of instances. This
     * constructor can not be coveraged / tested since it is private.
     *
     * @param clazz
     */
    public static void coveragePrivateConstructorForClassesWithStaticMethodsOnly(Class<?> clazz) {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.getParameterTypes().length == 0) {
                constructor.setAccessible(true);
                try {
                    constructor.newInstance();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            } else {
                throw new IllegalStateException("This should only be used if the class has a singe private constructor");
            }
        }
    }

    public static byte[] loadFile(String url) {
        try {
            InputStream stream = SystemFunctions.class.getClassLoader().getResourceAsStream(url);
            byte[] bytes = IOUtils.toByteArray(stream);
            return bytes;
        } catch (Exception e) {
            throw new RuntimeException("Unable to load " + url, e);
        }
    }

}
