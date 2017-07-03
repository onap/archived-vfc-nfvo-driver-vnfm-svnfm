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

package org.openo.nfvo.vnfmadapter.service.rest.exceptionmapper;

import org.apache.http.HttpStatus;

/**
 * Exception response model.<br>
 *
 * @author
 * @version NFVO 0.5 Sep 27, 2016
 */
public class ExceptionMessage {

	private String errorCode = "unknown.error";

	private int httpCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;

	private String message;

	@Override
	public String toString() {
	    return "Error {errorCode=" + this.errorCode + ", httpCode=" + this.httpCode + ", message="
	            + this.message + "}";
	}

	public String getErrorCode() {
	    return this.errorCode;
	}

	public void setErrorCode(String errorCode) {
	    this.errorCode = errorCode;
	}

	public String getMessage() {
	    return this.message;
	}

	public void setMessage(String message) {
	    this.message = message;
	}

	public int getHttpCode() {
	    return this.httpCode;
	}

	public void setHttpCode(int httpCode) {
	    this.httpCode = httpCode;
	}
}
