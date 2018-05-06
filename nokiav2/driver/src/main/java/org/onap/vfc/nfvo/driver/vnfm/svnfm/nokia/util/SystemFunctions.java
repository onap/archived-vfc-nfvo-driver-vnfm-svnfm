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

import com.google.common.io.ByteStreams;
import java.io.InputStream;
import java.io.PrintStream;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.SpringApplication;

/**
 * Wrapper class for static method calls to core or core libraries.
 * Calls to static methods in core or core libraries are wrapped to be able to test
 * the classes that uses static calls.
 */
public class SystemFunctions {
    private static SystemFunctions singletonInstance;

    /**
     * @return singleton instance
     */
    public static SystemFunctions systemFunctions() {
        if (singletonInstance != null) {
            return singletonInstance;
        } else {
            synchronized (SystemFunctions.class) {
                singletonInstance = new SystemFunctions();
            }
            return singletonInstance;
        }
    }

    /**
     * Causes the currently executing thread to sleep (temporarily cease
     * execution) for the specified number of milliseconds, subject to
     * the precision and accuracy of system timers and schedulers. The thread
     * does not lose ownership of any monitors.
     *
     * @param millis the length of time to sleep in milliseconds
     */
    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UserInvisibleError("Interrupted while sleeping", e);
        }
    }

    /**
     * Returns the current time in milliseconds.  Note that
     * while the unit of time of the return value is a millisecond,
     * the granularity of the value depends on the underlying
     * operating system and may be larger.  For example, many
     * operating systems measure time in units of tens of
     * milliseconds.
     *
     * <p> See the description of the class <code>Date</code> for
     * a discussion of slight discrepancies that may arise between
     * Unable to load /unittests/missing     * "computer time" and coordinated universal time (UTC).
     *
     * @return the difference, measured in milliseconds, between
     * the current time and midnight, January 1, 1970 UTC.
     * @see java.util.Date
     */
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Loads a file from the class path
     *
     * @param url the URL of the file
     * @return the content of the file
     */
    public byte[] loadFile(String url) {
        try {
            InputStream stream = SystemFunctions.class.getClassLoader().getResourceAsStream(url);
            return ByteStreams.toByteArray(stream);
        } catch (Exception e) {
            throw new UserVisibleError("Unable to load " + url, e);
        }
    }

    /**
     * The "standard" error output stream. This stream is already
     * open and ready to accept output data.
     * <p>
     * Typically this stream corresponds to display output or another
     * output destination specified by the host environment or user. By
     * convention, this output stream is used to display error messages
     * or other information that should come to the immediate attention
     * of a user even if the principal output stream, the value of the
     * variable <code>out</code>, has been redirected to a file or other
     * destination that is typically not continuously monitored.
     */
    @SuppressWarnings("squid:S106") // (intentional wrapping of system err)
    public PrintStream err() {
        return System.err;
    }

    /**
     * The "standard" output stream. This stream is already
     * open and ready to accept output data. Typically this stream
     * corresponds to display output or another output destination
     * specified by the host environment or user.
     * <p>
     * For simple stand-alone Java applications, a typical way to write
     * a line of output data is:
     * <blockquote><pre>
     *     System.out.println(data)
     * </pre></blockquote>
     * <p>
     * See the <code>println</code> methods in class <code>PrintStream</code>.
     *
     * @see java.io.PrintStream#println()
     * @see java.io.PrintStream#println(boolean)
     * @see java.io.PrintStream#println(char)
     * @see java.io.PrintStream#println(char[])
     * @see java.io.PrintStream#println(double)
     * @see java.io.PrintStream#println(float)
     * @see java.io.PrintStream#println(int)
     * @see java.io.PrintStream#println(long)
     * @see java.io.PrintStream#println(java.lang.Object)
     * @see java.io.PrintStream#println(java.lang.String)
     */
    @SuppressWarnings("squid:S106") // (intentional wrapping of system err)
    public PrintStream out() {
        return System.out;
    }

    /**
     * The "standard" input stream. This stream is already
     * open and ready to supply input data. Typically this stream
     * corresponds to keyboard input or another input source specified by
     * the host environment or user.
     */
    public InputStream in() {
        return System.in; //NO SONAR (intentional wrapping of system in)
    }

    /**
     * Wraps the static call (required for being able to test)
     *
     * @return the default HTTP client
     */
    public CloseableHttpClient getHttpClient() {
        return HttpClients.createDefault();
    }

    /**
     * @param clazz the main source of the Spring application
     * @return a new Spring application
     */
    public SpringApplication newSpringApplication(Class clazz) {
        return new SpringApplication(clazz);
    }
}