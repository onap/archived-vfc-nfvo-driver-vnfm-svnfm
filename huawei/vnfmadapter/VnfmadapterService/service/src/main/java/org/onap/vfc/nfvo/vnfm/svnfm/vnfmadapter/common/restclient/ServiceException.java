/*
 * Copyright 2016 Huawei Technologies Co., Ltd.
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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.restclient;

import java.text.MessageFormat;

/**
 * The base class for all common exception.<br/>
 * <p>
 * </p>
 * 
 * @author
 * @version 28-May-2016
 */
public class ServiceException extends Exception {

    /**
     * default exception id.
     */
    public static final String DEFAULT_ID = "framwork.remote.SystemError";

    /**
     * Serial number.
     */
    private static final long serialVersionUID = 5703294364555144738L;

    /**
     * Exception id.
     */
    private String id = DEFAULT_ID;

    private Object[] args = null;

    private int httpCode = 500;

    private ExceptionArgs exceptionArgs = null;

    /**
     * The default constructor<br/>
     * <p>
     * This method is only used as deserialized, in other cases, use parameterized constructor.
     * </p>
     * 
     * @since
     */
    public ServiceException() {
        super("");
    }

    /**
     * Constructor<br/>
     * <p>
     * </p>
     * 
     * @since
     * @param id: details.
     * @param cause: reason.
     */
    public ServiceException(final String id, final Throwable cause) {
        super(cause);
        this.setId(id);
    }

    /**
     * Constructor<br/>
     * <p>
     * </p>
     * 
     * @since
     * @param message: details.
     */
    public ServiceException(final String message) {
        super(message);
    }

    /**
     * Constructor<br/>
     * <p>
     * </p>
     * 
     * @since
     * @param id: exception id.
     * @param message: details.
     */
    public ServiceException(final String id, final String message) {
        super(message);
        this.setId(id);
    }

    /**
     * Constructor<br/>
     * <p>
     * </p>
     * 
     * @since
     * @param id: exception id.
     * @param httpCode: http status code.
     */
    public ServiceException(final String id, final int httpCode) {
        super();
        this.setId(id);
        this.setHttpCode(httpCode);
    }

    /**
     * Constructor<br/>
     * <p>
     * the exception include the httpcode and message.
     * </p>
     * 
     * @since
     * @param httpCode http code.
     * @param message details.
     */
    public ServiceException(final int httpCode, final String message) {
        super(message);
        this.setHttpCode(httpCode);
    }

    /**
     * Constructor<br/>
     * <p>
     * </p>
     * 
     * @since
     * @param id: exception id.
     * @param httpCode: http code.
     * @param exceptionArgs: Exception handling frame parameters.
     */
    public ServiceException(final String id, final int httpCode, final ExceptionArgs exceptionArgs) {
        super();
        this.setId(id);
        this.setHttpCode(httpCode);
        this.setExceptionArgs(exceptionArgs);
    }

    /**
     * Constructor<br/>
     * <p>
     * Have a placeholder exception, use args formatted message.
     * </p>
     * 
     * @since
     * @param id: exception id.
     * @param message: details.
     * @param args: Placeholders for parameters
     */
    public ServiceException(final String id, final String message, final Object... args) {
        super(MessageFormat.format(message, args));
        this.setId(id);
        this.args = args;
    }

    /**
     * Constructor<br/>
     * <p>
     * Have a placeholder exception, use args formatted message
     * </p>
     * 
     * @since
     * @param id: exception id.
     * @param message: details.
     * @param cause: reason.
     * @param args: placeholder for parameters
     */
    public ServiceException(final String id, final String message, final Throwable cause, final Object... args) {
        super(MessageFormat.format(message, args), cause);
        this.setId(id);
        this.args = args;
    }

    /**
     * Constructor<br/>
     * <p>
     * </p>
     * 
     * @since
     * @param id: exception id.
     * @param message: details.
     * @param cause: reason.
     */
    public ServiceException(final String id, final String message, final Throwable cause) {
        super(message, cause);
        this.setId(id);
    }

    /**
     * Constructor<br/>
     * <p>
     * </p>
     * 
     * @since
     * @param cause: reason.
     */
    public ServiceException(final Throwable cause) {
        super(cause);
    }

    /**
     * Get exceptoin id.<br/>
     * 
     * @return
     * @since
     */
    public String getId() {
        if(id == null || id.isEmpty()) {
            return DEFAULT_ID;
        }
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public int getHttpCode() {
        return this.httpCode;
    }

    public void setHttpCode(final int httpCode) {
        this.httpCode = httpCode;
    }

    /**
     * Obtain the ROA exception handling framework parameters<br/>
     * 
     * @return exception args.
     * @since
     */
    public ExceptionArgs getExceptionArgs() {
        return exceptionArgs;
    }

    public void setExceptionArgs(final ExceptionArgs exceptionArgs) {
        this.exceptionArgs = exceptionArgs;
    }

    /**
     * Gets the parameter information<br/>
     * 
     * @return parameter list.
     * @since
     */
    protected Object[] getArgs() {
        if(args == null || args.length == 0 || DEFAULT_ID.equals(getId())) {
            return new Object[] {};
        }
        return args;
    }

    @Override
    public String toString() {
        return "exception.id: " + getId() + "; " + super.toString();
    }

}
